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

import raw.blockChain.api.Merkler.IntermediateValues;

/**
 * This is a compact version of a {@link Block}.
 * It just store <b>ONE</b> {@link Transaction} and the set of
 * {@link IntermediateValues} needed to compute the correct
 * merkle root of its {@link BlockHeader}
 * 
 * @author vic
 *
 */
public interface BlockCompactRepresentation extends Serializable {
	
	/**
	 * @return the {@link BlockHeader} header of this {@link BlockCompactRepresentation}
	 */
	public BlockHeader getHeader();
	
	/**
	 * @return a single {@link Transaction} 
	 */
	public Transaction getTransaction();
	
	/**
	 * @return {@link IntermediateValues} needed to compute {@link BlockHeader} merkle root with the single {@link Transaction} returned from {@link BlockCompactRepresentation#getTransaction()}
	 */
	public IntermediateValues getIntermediateValues();

}
