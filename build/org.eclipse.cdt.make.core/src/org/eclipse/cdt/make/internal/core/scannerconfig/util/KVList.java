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

import java.util.ArrayList;
import java.util.List;

/**
 * Key - Value (List) pair
 * 
 * @author vhirsl
 */
public class KVList {
	String key;
	List value;
	
	public KVList(String key) {
		this.key = key;
		this.value = new ArrayList();
	}

	/**
	 * List must not be <code>null</code>. 
	 */
	public KVList(String key, List value) {
		this.key = key;
		this.value = value;
	}
	
	String getKey() {
		return key;
	}

	List getValue() {
		return value;
	}
}
