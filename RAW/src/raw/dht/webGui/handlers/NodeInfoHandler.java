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
package raw.dht.webGui.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import raw.blockChain.api.Transaction;
import raw.blockChain.api.implementations.utils.TransactionUtils;
import raw.blockChain.services.implementations.DefaultBlockChainCore;
import raw.dht.DhtCore;
import raw.dht.DhtID;
import raw.dht.DhtNode;
import raw.dht.DhtNodeExtended;
import raw.dht.implementations.DefaultDhtCore;
import raw.dht.implementations.DefaultDhtID;
import raw.dht.implementations.exceptions.IncoherentTransactionException;
import raw.dht.implementations.exceptions.IncompleteNodeExtendedException;
import raw.dht.webGui.HtmlBuilder;
import raw.dht.webGui.WebGuiHandler;
import raw.logger.Log;

import com.sun.net.httpserver.HttpExchange;

public class NodeInfoHandler implements WebGuiHandler {
	
	private final String myContext = "/nodeInspector";
	private final String title = "Node Inspector";

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String body = createResponseBody();
		exchange.sendResponseHeaders(HTTP_OK_STATUS, body.getBytes().length);
		OutputStream os = exchange.getResponseBody();
		os.write(body.getBytes());
		os.close();
	}
	
	private String createResponseBody(){
		Log.getLogger().verboseDebug("Request for NodeInfoHandler received.");
		DhtCore core = DefaultDhtCore.getCore();
		DhtNode node;
		try {
			node = core.getNodeExtended();
		} catch (IncoherentTransactionException | IncompleteNodeExtendedException e) {
			node = core.getNode();
		}
		
		Transaction transaction = null;
		if(node instanceof DhtNodeExtended){
			transaction = ((DhtNodeExtended) node).getTransaction();
		}
//		long blockNumber = core.getBlockNumber();
		
		DhtID seed = new DefaultDhtID(core.getCurrentSeed());
		
//		DhtID proof = core.getCurrentHasher().hashDhtIDwithLongNonce(node.getID(), node.getTransaction().getTransactionNonce());
		
		String valid;
		if(core.isThisNodeOld()){
			valid = "an \"old\" node";
		} else {
			valid = "a still invalid node";
		}
		
//		boolean transactionIsValid = TransactionUtils.isValid(transaction, core.getCurrentHasher());
		boolean transactionIsValid = TransactionUtils.isValid(transaction, DefaultBlockChainCore.getBlockChainCore());
		
		HtmlBuilder html = new HtmlBuilder(title);
			html.openH1().text("Node Transaction inspector").closeH1()
			.openH2().text("Node's Info").closeH2().br().br()
			.openBold().text("Node description: ").closeBold().text(node.toString()).br().br();
			if(transaction == null){				
				html.openBold().text("Node transaction IS NULL!").closeBold().br().br();
			}
//			.openBold().text("Transaction block number: ").closeBold().text(Long.toString(blockNumber)).br().br()
//			.openBold().text("Node full ID: ").closeBold().text(node.getID().toHexString()).br().br()
			html.text("This node appears to be: ").openBold().text(valid).closeBold().text(".").br().br()
			.br().br().openBold().text("Current seed is: ").closeBold().text(seed.toHexString()).br().br()
//			.openBold().text("Epoch proof of work: ").closeBold().text(Long.toString(token)).br()
//			.text("which hashes id to: ").openBold().text(proof.toHexString()).closeBold().br()
			.text("Transaction is valid: ").openBold().text(Boolean.toString(transactionIsValid)).closeBold();
			
		Collection<DhtNodeExtended> knownNodes = core.getRoutingTable().getFullSetOfNodes(false);
		
		html.hr().openH2().text("Known nodes:").closeH2();
		
		if(knownNodes == null || knownNodes.size() == 0){
			html.text("...no other node is known...");
		} else {			
			for(DhtNodeExtended pal : knownNodes){
				html.text(pal.toString()).br();
			}
		}
		return html.build();
	}

	@Override
	public String getContext() {
		return myContext;
	}

	@Override
	public String getTitle() {
		return title;
	}

}
