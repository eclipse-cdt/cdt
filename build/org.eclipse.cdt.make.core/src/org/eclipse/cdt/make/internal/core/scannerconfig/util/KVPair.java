/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

/**
 * Key - Value Pair
 * 
 * @author vhirsl
 */
public class KVPair {
	private SCDOptionsEnum key;
	private String value;
	
	/**
	 * 
	 */
	public KVPair(SCDOptionsEnum key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public SCDOptionsEnum getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(this.getClass())) {
			KVPair arg = (KVPair) arg0;
			return (key.equals(arg.getKey()) && value.equals(arg.getValue()));
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 17 * key.hashCode() + value.hashCode();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return key + " -> " + value;
	}
}
