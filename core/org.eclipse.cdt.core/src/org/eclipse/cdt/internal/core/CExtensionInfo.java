/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.HashMap;

public class CExtensionInfo {

	protected HashMap attribMap = new HashMap(4);

	protected HashMap getAttributes() {
		return attribMap;
	}

	public void setAttribute(String key, String value) {
		if (value == null) {
			attribMap.remove(key);
		} else {
			attribMap.put(key, value);
		}
	}

	public String getAttribute(String key) {
		return (String) attribMap.get(key);
	}

}
