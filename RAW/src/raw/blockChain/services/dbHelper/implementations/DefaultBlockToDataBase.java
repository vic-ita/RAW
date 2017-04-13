/*******************************************************************************
 *  Copyright 2017 Vincenzo-Maria Cappelleri <vincenzo.cappelleri@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package raw.blockChain.services.dbHelper.implementations;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import raw.blockChain.api.Block;
import raw.blockChain.api.BlockCompactRepresentation;
import raw.blockChain.api.BlockHeader;
import raw.blockChain.api.HashValue;
import raw.blockChain.api.Merkler;
import raw.blockChain.api.Transaction;
import raw.blockChain.api.Merkler.IntermediateValue;
import raw.blockChain.api.Merkler.IntermediateValues;
import raw.blockChain.api.implementations.DefaultBlock;
import raw.blockChain.api.implementations.DefaultBlockCompactRepresentation;
import raw.blockChain.api.implementations.DefaultIntermediateValuesBuilder;
import raw.blockChain.api.implementations.DefaultMerkler;
import raw.blockChain.api.implementations.utils.BlockUtils;
import raw.blockChain.exceptions.TransactionNotPresentException;
import raw.blockChain.services.CommonNode;
import raw.blockChain.services.dbHelper.BlocksToDataBase;
import raw.blockChain.services.thickNode.ThickNode;
import raw.db.DBManager;
import raw.db.exceptions.DataBaseNotRunning;
import raw.db.exceptions.NoSuchDatabaseException;
import raw.dht.implementations.utils.DhtSigningUtils;
import raw.logger.Log;
import raw.settings.BlockChainProperties;
import raw.settings.ModuleProperty;
import raw.settings.PropertiesManager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

/**
 * Default implementation of {@link BlocksToDataBase}.
 * 
 * @see BlocksToDataBase
 * 
 * @author vic
 *
 */
public class DefaultBlockToDataBase implements BlocksToDataBase {
	
	private CommonNode owner;
	
	private DBManager database;
	private String dbName;
	
	private Log log;
	
	public DefaultBlockToDataBase(CommonNode owner) {
		this.owner = owner;
		BlockChainProperties properties = (BlockChainProperties) PropertiesManager.getManager().getProperties(ModuleProperty.BLOCK_CHAIN);
		dbName = properties.getBlockChainDBName();
		database = DBManager.getDBManager();
		
		log = Log.getLogger();
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.thickNode.BlocksToDataBase#storeOnDataBase(raw.blockChain.api.Block)
	 */
	@Override
	public void storeOnDataBase(Block block) throws SQLIntegrityConstraintViolationException {
		TransactionManager atomifier = null;
		try {
			atomifier = database.getTransactionManager(dbName);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		}
		
		try {
			atomifier.callInTransaction(new AtomicBlockStore(block));
		} catch (SQLException e) {
			if(e instanceof SQLIntegrityConstraintViolationException){
				log.verboseDebug("Possibly duplicated block. Aborting store. (Block: "+block.getHeader()+")");
				SQLIntegrityConstraintViolationException ex = (SQLIntegrityConstraintViolationException) e;
				throw ex;
			} else {				
				log.exception(e);
			}
		}
		
	}
	
	private class AtomicBlockStore implements Callable<Void>{
		private Block block;
		
		public AtomicBlockStore(Block block) {
			this.block = block;
		}
		
		@Override
		public Void call() throws Exception {
			DataBaseBlockHeader header = storeAndReturnHeaderOnDataBase(block.getHeader());
			
			for(int i = 0; i < block.getTransactions().size(); i++){
				storeTransaction(block.getTransactions().get(i), i, header);
			}
			return null;
		}
	}
	
	/**
	 * Save a {@link Transaction} associated to a given {@link DataBaseBlockHeader}
	 * header rapresentation. The position in the transactions list can be also saved
	 * with this method.
	 * 
	 * @param transaction the {@link Transaction} to be saved
	 * @param positionInList {@link Transaction}'s position in {@link Block} set of transactions
	 * @param header a {@link DataBaseBlockHeader} representation of a {@link BlockHeader}
	 * @return a {@link DataBaseTransaction} reference
	 * @throws SQLException 
	 */
	private DataBaseTransaction storeTransaction(Transaction transaction, int positionInList, DataBaseBlockHeader header) throws SQLException{
		Dao<DataBaseTransaction, Integer> transactionsDao = null;
		
		try {
			transactionsDao = database.getDAO(dbName, DataBaseTransaction.class);
		} catch (SQLException e) {
			log.exception(e);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		} catch (DataBaseNotRunning e) {
			log.exception(e);
		}
		
		DataBaseTransaction dataBaseTransaction = new DataBaseTransaction(transaction, positionInList, header);
		int inserted = 0;
		try {
			inserted = transactionsDao.create(dataBaseTransaction);
		} catch (SQLException e) {
			log.exception(e);
		}
		if(inserted != 1){
			throw new SQLException("Transaction insertion failed!");
		}
		
		return dataBaseTransaction;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#storeHeaderOnDataBase(raw.blockChain.api.BlockHeader)
	 */
	@Override
	public void storeHeaderOnDataBase(BlockHeader header) throws SQLException {
		storeAndReturnHeaderOnDataBase(header);
	}
	
	/**
	 * private method to store a {@link BlockHeader} returning a reference to the
	 * {@link DataBaseBlockHeader} stored.
	 * 
	 * @param header the {@link BlockHeader} to be stored
	 * @return the {@link DataBaseBlockHeader} stored
	 * @throws SQLException 
	 */
	private DataBaseBlockHeader storeAndReturnHeaderOnDataBase(BlockHeader header) throws SQLException{
		DataBaseBlockHeader dbHeader = new DataBaseBlockHeader(header);
		Dao<DataBaseBlockHeader, Integer> headerDao = null;
		try {
			headerDao = database.getDAO(dbName, DataBaseBlockHeader.class);
		} catch (SQLException e) {
			log.exception(e);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		} catch (DataBaseNotRunning e) {
			log.exception(e);
		}
		
		int inserted;
		try {
			inserted = headerDao.create(dbHeader);
		} catch (SQLException e) {
			if(e.getCause() instanceof SQLIntegrityConstraintViolationException){
				SQLIntegrityConstraintViolationException ex = (SQLIntegrityConstraintViolationException) e.getCause();
				throw ex;
			}
			log.exception(e);
			throw e;
		}
		if(inserted != 1){
			throw new SQLException("Header insertion failed!");
		}
		
		return dbHeader;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#getBlockCompatRepresentation(raw.blockChain.api.BlockHeader, raw.blockChain.api.Transaction)
	 */
	@Override
	public BlockCompactRepresentation getBlockCompatRepresentation(BlockHeader header, Transaction transaction) throws SQLException {
		DataBaseBlockHeader dbHeader = getDBHeader(header.hash());
		if(dbHeader == null){
			return null;
		}
	
		DataBaseTransaction retrievedTransaction = getDataBaseTransaction(dbHeader, transaction);
		if(retrievedTransaction == null){
			return null;
		}
		
		DataBaseIntermediateValues intermediateValues = getDBIntermediateValues(dbHeader, retrievedTransaction);
		if(intermediateValues != null){ // it was already stored on database.
			IntermediateValues intermValues = getIntermediateValuesFromDB(dbHeader, retrievedTransaction, intermediateValues);
			if(intermValues == null){
				return null;
			}
			BlockCompactRepresentation retVal = new DefaultBlockCompactRepresentation(header, transaction, intermValues);
			return retVal;
		}
		// data was not already available in database.
		// try to build it from a full block.
		Block originalBlock = getBlockFromHash(header.hash());
		if(originalBlock == null){
			// the full block is not available on database.
			// the compact version is not computable. sorry.
			return null;
		}
		
		Merkler merkler = new DefaultMerkler(1);
		IntermediateValues computatedIntermediateValues = null;
		try {
			computatedIntermediateValues = merkler.getIntermediateValues(transaction, originalBlock);
		} catch (TransactionNotPresentException e) {
			log.exception(e);
		}
		
		BlockCompactRepresentation compactRepresentation = new DefaultBlockCompactRepresentation(header, transaction, computatedIntermediateValues);
		
		storeBlockCompactRepresentation(compactRepresentation);
		
		return compactRepresentation;
	}
	
	private IntermediateValues getIntermediateValuesFromDB(DataBaseBlockHeader dbHeader, DataBaseTransaction transaction, DataBaseIntermediateValues dbIntValues){
		Dao<DataBaseIntermediateValue, Integer> intermediateDAO;
		try {
			intermediateDAO = database.getDAO(dbName, DataBaseIntermediateValue.class);
		} catch (SQLException | NoSuchDatabaseException | DataBaseNotRunning e) {
			log.exception(e);
			return null;
		}
		
		List<DataBaseIntermediateValue> vals;
		try {
			vals = intermediateDAO.queryForEq(DataBaseIntermediateValue.INTERMEDIATE_VALUES_COLLECTION_ID_FIELD_NAME, dbIntValues.getId());
		} catch (SQLException e) {
			log.exception(e);
			return null;
		}
		Collections.sort(vals, new Comparator<DataBaseIntermediateValue>() {
			@Override
			public int compare(DataBaseIntermediateValue o1,
					DataBaseIntermediateValue o2) {
				return o1.getPositionInStack() - o2.getPositionInStack();
			}
		});
		DefaultIntermediateValuesBuilder builder = new DefaultIntermediateValuesBuilder(transaction.getPlainTransaction(), dbHeader.getBlockNumber());
		for(DataBaseIntermediateValue val : vals){
			builder.pushNextHash(val.getPlainIntermediateValue());
		}
		IntermediateValues retIntVals = builder.build();
		return retIntVals;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#storeCompactTransactionsRepresentation(raw.blockChain.api.BlockHeader, raw.blockChain.api.Transaction, raw.blockChain.api.Merkler.IntermediateValues)
	 */
	@Override
	public void storeBlockCompactRepresentation(BlockCompactRepresentation compactBlock) throws SQLException {
		TransactionManager atomifier = null;
		try {
			atomifier = database.getTransactionManager(dbName);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		}
		
		try {
			atomifier.callInTransaction(new AtomicBlockCompactRepresentationStore(compactBlock));
		} catch (Exception e) {
			log.exception(e);
		}
	}
	
	private class AtomicBlockCompactRepresentationStore implements Callable<Void>{
		
		private BlockCompactRepresentation compactRepresentation;
		
		public AtomicBlockCompactRepresentationStore(BlockCompactRepresentation blockCompactRepresentation) {
			compactRepresentation = blockCompactRepresentation;
		}

		@Override
		public Void call() throws Exception {
			DataBaseBlockHeader dbHeader = getDBHeader(compactRepresentation.getHeader().hash());
			if(dbHeader == null){
				storeAndReturnHeaderOnDataBase(dbHeader);
			}
			DataBaseTransaction dbTransaction = getDataBaseTransaction(dbHeader, compactRepresentation.getTransaction());
			if(dbTransaction == null){
				storeTransaction(compactRepresentation.getTransaction(), -1, dbHeader);
			}
			DataBaseIntermediateValues intermediateValues = getDBIntermediateValues(dbHeader, dbTransaction);
			if(intermediateValues == null){
				Dao<DataBaseIntermediateValues, Integer> intermediateValueSDao = database.getDAO(dbName, DataBaseIntermediateValues.class);

				DataBaseIntermediateValues storableIntermediateValues = new DataBaseIntermediateValues(dbHeader, dbTransaction);
				intermediateValueSDao.create(storableIntermediateValues);

				Dao<DataBaseIntermediateValue, Integer> intermediateValueDao = database.getDAO(dbName, DataBaseIntermediateValue.class); 

				IntermediateValues values = compactRepresentation.getIntermediateValues();
				IntermediateValue value = values.popNextHash();
				int position = 0;
				while(value != null){
					DataBaseIntermediateValue dbValue = new DataBaseIntermediateValue(value, position, storableIntermediateValues);
					intermediateValueDao.create(dbValue);
					position++;
					value = values.popNextHash();
				}
			}
			return null;
		}
		
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.thickNode.BlocksToDataBase#open()
	 */
	@Override
	public void open() {
		database.startDB(dbName);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.thickNode.BlocksToDataBase#close()
	 */
	@Override
	public void close() {
		database.stopDB(dbName);
	}

	@Override
	public BlockHeader getLastBlockHeaderInChain(boolean generateGenesis) {
		log.verboseDebug("Last BlockHeader asked.");
		Dao<DataBaseBlockHeader, Integer> headerDao = null;
		try {
			headerDao = database.getDAO(dbName, DataBaseBlockHeader.class);
		} catch (SQLException e) {
			log.exception(e);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		} catch (DataBaseNotRunning e) {
			log.exception(e);
		}
		
		QueryBuilder<DataBaseBlockHeader, Integer> builder = headerDao.queryBuilder();
		builder.orderBy(DataBaseBlockHeader.BLOCK_NUMBER_FIELD_NAME, false).limit(1L);
		
		DataBaseBlockHeader lastHeader = null;
		
		try {
			lastHeader = headerDao.queryForFirst(builder.prepare());
		} catch (SQLException e) {
			log.exception(e);
		}
		
		if(lastHeader == null){
			if((owner instanceof ThickNode) && generateGenesis){
				//there is no block in database. So we must generate a genesis block.
				log.verboseDebug("There is no BlockHeader. Generating one...");
				Block genesis = BlockUtils.generateGenesisBlock(owner.getNodeAddress().toString());
				try {
					storeOnDataBase(genesis);
				} catch (SQLIntegrityConstraintViolationException e) {
					log.debug("Unexpected! This should not have happened!!");
					log.exception(e);
				}
				return getLastBlockHeaderInChain(false);				
			} else {
				return null;
			}
		}
		
		BlockHeader header = lastHeader.getPlainBlockHeader();
		log.verboseDebug("Last BlockHeader retrieved from DB.");
		
		return header;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#getBlockHeaderByNumber(long)
	 */
	@Override
	public BlockHeader getBlockHeaderByNumber(long blockNumber) {
		DataBaseBlockHeader found = getDBHeaderByNubmber(blockNumber);
		if(found == null){
			return null;
		}
		return found.getPlainBlockHeader();
	}
	
	private DataBaseBlockHeader getDBHeaderByNubmber(long blockNumber){
		Dao<DataBaseBlockHeader, Integer> headerDao = null;
		try {
			headerDao = database.getDAO(dbName, DataBaseBlockHeader.class);
		} catch (SQLException e) {
			log.exception(e);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		} catch (DataBaseNotRunning e) {
			log.exception(e);
		}
		
		DataBaseBlockHeader found = null;
		List<DataBaseBlockHeader> result = null;
		try {
			result = headerDao.queryForEq(DataBaseBlockHeader.BLOCK_NUMBER_FIELD_NAME, blockNumber);
		} catch (SQLException e) {
			log.exception(e);
		}
		if(result.size()!=0){
			found = result.get(0);
		}
		return found;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#getBlockByNumber(long)
	 */
	@Override
	public Block getBlockByNumber(long blockNumber) {
		BlockHeader header = getBlockHeaderByNumber(blockNumber);
		if(header == null){
			return null;
		}
		Block block = null;
		try {
			block = getBlockFromHash(header.hash());
		} catch (SQLException e) {
			log.exception(e);
			return null;
		}
		return block;
	}

	@Override
	public Block getBlockFromHash(HashValue hash) throws SQLException {
		DataBaseBlockHeader dbFoundHeader = getDBHeader(hash);
		if(dbFoundHeader == null){
			return null;
		}
		
		Dao<DataBaseTransaction, Integer> transactionsDao = null;
		try {
			transactionsDao = database.getDAO(dbName, DataBaseTransaction.class);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		} catch (DataBaseNotRunning e) {
			log.exception(e);
		}
		
		List<DataBaseTransaction> transactionsResult = transactionsDao.queryForEq(DataBaseTransaction.HEADER_ID_FIELD_NAME, dbFoundHeader.getId());
		Collections.sort(transactionsResult, new Comparator<DataBaseTransaction>() {
			@Override
			public int compare(DataBaseTransaction o1, DataBaseTransaction o2) {
				return o1.getPositionInList()-o2.getPositionInList();
			}
		});
		
		ArrayList<Transaction> blockTransactions = new ArrayList<Transaction>();
		for(DataBaseTransaction dbTransaction : transactionsResult){
			blockTransactions.add(dbTransaction.getPlainTransaction());
		}
		
		BlockHeader foundHeader = dbFoundHeader.getPlainBlockHeader();
		
		Block foundBlock = new DefaultBlock(foundHeader, blockTransactions);
		
		return foundBlock;
	}
	
	/**
	 * search the database for a {@link DataBaseBlockHeader} identified by
	 * its {@link HashValue} header's hash.
	 * 
	 * @param hash the header's {@link HashValue}
	 * @return the found {@link DataBaseBlockHeader} or <tt>null</tt> if nothing is found
	 * @throws SQLException
	 */
	private DataBaseBlockHeader getDBHeader(HashValue hash) throws SQLException{
		Dao<DataBaseBlockHeader, Integer> headerDao = null;
		try {
			headerDao = database.getDAO(dbName, DataBaseBlockHeader.class);
		} catch (SQLException e) {
			log.exception(e);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		} catch (DataBaseNotRunning e) {
			log.exception(e);
		}
		
		List<DataBaseBlockHeader> result = headerDao.queryForEq(DataBaseBlockHeader.HASH_FIELD_NAME, hash.toHexString());
		if(result.size() == 0){
			return null;
		}
		
		DataBaseBlockHeader dbFoundHeader = result.get(0);
		
		return dbFoundHeader;
	}

	private DataBaseIntermediateValues getDBIntermediateValues(DataBaseBlockHeader dbHeader, DataBaseTransaction dbTransaction) throws SQLException{
		Dao<DataBaseIntermediateValues, Integer> intermediateDao = null;
		try {
			intermediateDao = database.getDAO(dbName, DataBaseIntermediateValues.class);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		} catch (DataBaseNotRunning e) {
			log.exception(e);
		}
		QueryBuilder<DataBaseIntermediateValues, Integer> intermediateValuesBuilder = intermediateDao.queryBuilder();
		Where<DataBaseIntermediateValues, Integer> intermediateValuesWhere = intermediateValuesBuilder.where();
		intermediateValuesWhere.eq(DataBaseIntermediateValues.HEADER_ID_FIELD_NAME, dbHeader.getId()).
		and().eq(DataBaseIntermediateValues.TRANSACTION_ID_FIELD_NAME, dbTransaction.getId());
		intermediateValuesBuilder.setWhere(intermediateValuesWhere);
		DataBaseIntermediateValues intermediateValues = intermediateDao.queryForFirst(intermediateValuesBuilder.prepare());
		return intermediateValues;
	}
	
	/**
	 * Try to find if there is in the database a record of a given {@link Transaction}
	 * with a relation to given {@link DataBaseBlockHeader}.
	 * 
	 * @param databaseHeader the {@link DataBaseBlockHeader}
	 * @param transaction the {@link Transaction}
	 * @return a {@link DataBaseTransaction} if found in the database or <tt>null</tt> otherwise
	 * @throws SQLException 
	 */
	private DataBaseTransaction getDataBaseTransaction(DataBaseBlockHeader databaseHeader, Transaction transaction) throws SQLException{
		Dao<DataBaseTransaction, Integer> transactionDao = null;
		try {
			transactionDao = database.getDAO(dbName, DataBaseTransaction.class);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		} catch (DataBaseNotRunning e) {
			log.exception(e);
		}
		
		QueryBuilder<DataBaseTransaction, Integer> trasnactionQueryBuilder = transactionDao.queryBuilder();
		Where<DataBaseTransaction, Integer> transactionWhere = trasnactionQueryBuilder.where();
		transactionWhere.eq(DataBaseTransaction.HEADER_ID_FIELD_NAME, databaseHeader.getId()).
		and().eq(DataBaseTransaction.PUBLIC_KEY_FIELD_NAME, DhtSigningUtils.publicKeyHexRepresentation(transaction.getPublicKey())).
		and().eq(DataBaseTransaction.DHT_ID_FIELD_NAME, transaction.getDhtID().toHexString());
		trasnactionQueryBuilder.setWhere(transactionWhere);
		DataBaseTransaction retrievedTransaction = transactionDao.queryForFirst(trasnactionQueryBuilder.prepare());
		return retrievedTransaction;
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#getBlockHeaderByHash(raw.blockChain.api.HashValue)
	 */
	@Override
	public BlockHeader getBlockHeaderByHash(HashValue hash) {
		DataBaseBlockHeader dbHeader = null;
		try {
			dbHeader = getDBHeader(hash);
		} catch (SQLException e) {
			log.exception(e);
		}
		if(dbHeader == null){
			return null;
		}
		return dbHeader.getPlainBlockHeader();
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#deleteBlockByHeader(raw.blockChain.api.BlockHeader)
	 */
	@Override
	public void deleteBlockByHeader(BlockHeader header) {
		TransactionManager atomifier = null;
		try {
			atomifier = database.getTransactionManager(dbName);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		}
		try {
			atomifier.callInTransaction(new AtomicBlockDelete(header));
		} catch (SQLException e) {
			log.exception(e);
		}
	}
	
	private class AtomicBlockDelete implements Callable<Void>{
		private BlockHeader header;
		
		public AtomicBlockDelete(BlockHeader header) {
			this.header = header;
		}

		@Override
		public Void call() throws Exception {
			DataBaseBlockHeader dbHeader = getDBHeader(header.hash());
			
			Dao<DataBaseTransaction, Integer> transactionDao = database.getDAO(dbName, DataBaseTransaction.class);
			
			List<DataBaseTransaction> transactionList = transactionDao.queryForEq(DataBaseTransaction.HEADER_ID_FIELD_NAME, dbHeader.getId());
			int deleted;
			if(transactionList.size() > 0){
				deleted = transactionDao.delete(transactionList);
				if(deleted != transactionList.size()){
					throw new SQLException("Deleted transactions are not the right number.");
				}				
			}
			
			Dao<DataBaseBlockHeader, Integer> headerDao = database.getDAO(dbName, DataBaseBlockHeader.class);
			deleted = headerDao.delete(dbHeader);
			if(deleted != 1){
				throw new SQLException("Deleted transactions are not the right number.");
			}
			return null;
		}
		
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#deleteBlockByNumber(long)
	 */
	@Override
	public void deleteBlockByNumber(long blockNumber) {
		BlockHeader header = getBlockHeaderByNumber(blockNumber);
		deleteBlockByHeader(header);
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#deleteBlocksBulk(java.util.ArrayList)
	 */
	@Override
	public boolean deleteBlocksBulk(ArrayList<Block> blocksBulk) {
		TransactionManager atomifier = null;
		try {
			atomifier = database.getTransactionManager(dbName);
		} catch (NoSuchDatabaseException e) {
			log.exception(e);
		}
		try {
			atomifier.callInTransaction(new AtomicBlockBulkDelete(blocksBulk));
		} catch (SQLException e) {
			return false;
		}

		return true;
	}
	
	private class AtomicBlockBulkDelete implements Callable<Void>{
		
		private ArrayList<Block> bulk;
		
		public AtomicBlockBulkDelete(ArrayList<Block> bulk) {
			this.bulk = bulk;
		}

		@Override
		public Void call() throws Exception {
			ArrayList<DataBaseBlockHeader> headers = new ArrayList<DataBaseBlockHeader>();
			ArrayList<DataBaseTransaction> transactions = new ArrayList<DataBaseTransaction>();
			Dao<DataBaseTransaction, Integer> transactionDao = database.getDAO(dbName, DataBaseTransaction.class);
			for(Block block : bulk){
				DataBaseBlockHeader dbHeader = getDBHeader(block.getHeader().hash()); 
				headers.add(dbHeader);
				List<DataBaseTransaction> transactionList = transactionDao.queryForEq(DataBaseTransaction.HEADER_ID_FIELD_NAME, dbHeader.getId());
				transactions.addAll(transactionList);				
			}
			int deleted;
			if(transactions.size()>0){
				deleted = transactionDao.delete(transactions);
				if(deleted != transactions.size()){
					throw new SQLException("Deleted transactions are not the right number.");
				}				
			}
			
			if(headers.size() > 0){				
				Dao<DataBaseBlockHeader, Integer> headerDao = database.getDAO(dbName, DataBaseBlockHeader.class);
				deleted = headerDao.delete(headers);
				if(deleted != headers.size()){
					throw new SQLException("Deleted transactions are not the right number.");
				}
			}
			return null;
		}
		
	}

	/* (non-Javadoc)
	 * @see raw.blockChain.services.dbHelper.BlocksToDataBase#searchTranscationBlockNumber(raw.blockChain.api.Transaction)
	 */
	@Override
	public long searchTranscationBlockNumber(Transaction transaction) {
		Dao<DataBaseTransaction, Integer> transactionDao;
		try {
			transactionDao = database.getDAO(dbName, DataBaseTransaction.class);
		} catch (SQLException | NoSuchDatabaseException | DataBaseNotRunning e) {
			log.exception(e);
			return -1;
		}
		QueryBuilder<DataBaseTransaction, Integer> trasnactionQueryBuilder = transactionDao.queryBuilder();
		Where<DataBaseTransaction, Integer> transactionWhere = trasnactionQueryBuilder.where();
		try {
			transactionWhere.eq(DataBaseTransaction.PUBLIC_KEY_FIELD_NAME, DhtSigningUtils.publicKeyHexRepresentation(transaction.getPublicKey())).
			and().eq(DataBaseTransaction.DHT_ID_FIELD_NAME, transaction.getDhtID().toHexString())
			.and().eq(DataBaseTransaction.CREATION_SEED_NUMBER_NAME, transaction.getCreationSeedNumber())
			.and().eq(DataBaseTransaction.TRANSACTION_NONCE_NAME, transaction.getTransactionNonce());
		} catch (SQLException e) {
			log.exception(e);
			return -1;
		}
		trasnactionQueryBuilder.setWhere(transactionWhere);
		
		List<DataBaseTransaction> transactions;
		try {
			transactions = transactionDao.query(trasnactionQueryBuilder.prepare());
		} catch (SQLException e) {
			log.exception(e);
			return -1;
		}
		if(transactions.size() == 0){
			return -1;
		}
		Dao<DataBaseBlockHeader, Integer> headerDao;
		try {
			headerDao = database.getDAO(dbName, DataBaseBlockHeader.class);
		} catch (SQLException | NoSuchDatabaseException | DataBaseNotRunning e) {
			log.exception(e);
			return -1;
		}
		
		HashSet<DataBaseBlockHeader> headers = new HashSet<DataBaseBlockHeader>();		
		for(DataBaseTransaction trans : transactions){
			DataBaseBlockHeader header;
			try {
				header = headerDao.queryForId(trans.getBlockHeaderId());
			} catch (SQLException e) {
				log.exception(e);
				continue;
			}
			headers.add(header);
		}
		
		if(headers.size() == 0){
			return -1;
		}
		
		DataBaseBlockHeader maxHeader = Collections.max(headers, new Comparator<DataBaseBlockHeader>() {
			@Override
			public int compare(DataBaseBlockHeader o1, DataBaseBlockHeader o2) {
				if(o1.getBlockNumber()-o2.getBlockNumber() < 0){
					return -1;
				} else if(o1.getBlockNumber()-o2.getBlockNumber() > 0){
					return 1;
				}
				return 0;
			}
		});
		return maxHeader.getBlockNumber();
	}

}
