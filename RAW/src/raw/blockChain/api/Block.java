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
package raw.blockChain.api;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This interface represents blocks of the block-chain.
 * Such blocks should work as containers for a 
 * {@link BlockHeader} and a collection of {@link Transaction}.
 * 
 * @author vic
 *
 */
public interface Block extends Serializable{
	
	/**
	 * This is a convenience method to access {@link BlockHeader#getVersion()}.
	 * 
	 * @return integer representing this block version
	 */
	public int getVersion();
	
	/**
	 * Access a reference of the object
	 * representing the header of this
	 * block.
	 * 
	 * @return a {@link BlockHeader} representation of header
	 */
	public BlockHeader getHeader();
	
	/**
	 * Access the list of transactions contained
	 * in this block. The list should be an (ordered)
	 * array list.
	 * 
	 * @return an {@link ArrayList} of {@link Transaction}s
	 */
	public ArrayList<Transaction> getTransactions();

}
