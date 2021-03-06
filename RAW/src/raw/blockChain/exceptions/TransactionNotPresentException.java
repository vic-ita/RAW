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
package raw.blockChain.exceptions;

import raw.blockChain.api.Transaction;

/**
 * Exception rised when a {@link Transaction} object is not present in
 * a given set of transactions.
 * 
 * @author vic
 *
 */
public class TransactionNotPresentException extends Exception {

	/**
	 * generated UID
	 */
	private static final long serialVersionUID = -9076634996225322173L;
	
	public TransactionNotPresentException(String msg) {
		super(msg);
	}
}
