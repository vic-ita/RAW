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
package raw.dht.implementations;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import raw.dht.DhtValue;

/**
 * Default implementation of {@link DhtValue}
 * 
 * @author vic
 *
 */
public class DefaultDhtValue implements DhtValue {

	/**
	 * random generated UID
	 */
	private static final long serialVersionUID = 4092418734345567523L;
	
	private Serializable value;
	private String annotation;
	
	/**
	 * Builds an instance of {@link DhtValue} with a given value and
	 * {@link DhtValue#getAnnotations()} set to null.
	 * 
	 * @param value a {@link Serializable} value
	 */
	public DefaultDhtValue(Serializable value) {
		this(value, null);
	}
	
	/**
	 * Builds an instance of {@link DhtValue} with a given value and
	 * {@link DhtValue#getAnnotations()} set to a certain value.
	 * 
	 * @param value a {@link Serializable} value
	 * @param annotation a {@link String} annotation
	 */
	public DefaultDhtValue(Serializable value, String annotation) {
		this.value = value;
		this.annotation = annotation;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtValue#getValue()
	 */
	@Override
	public Serializable getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see raw.dht.DhtValue#getAnnotations()
	 */
	@Override
	public String getAnnotations() {
		return annotation;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(value).
		append(annotation);
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DhtValue)){
			return false;
		}
		DhtValue other = (DhtValue) obj;
		if(!value.equals(other.getValue())){
			return false;
		}
		if(annotation != null && !annotation.equals(other.getAnnotations())){
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DhtValue: "+value.toString()+" [annotation: "+annotation+"]";
	}

}
