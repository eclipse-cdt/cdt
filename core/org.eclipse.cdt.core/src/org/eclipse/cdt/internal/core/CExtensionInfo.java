/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */package org.eclipse.cdt.internal.core;

import java.util.HashMap;

public class CExtensionInfo {

	protected HashMap attribMap = new HashMap(4);
	
	protected HashMap getAttributes() {
		return attribMap;
	}

	public void setAttribute(String key, String value) {
		attribMap.put(key, value);
	}

	public String getAttribute(String key) {
		return (String) attribMap.get(key);
	}

}
