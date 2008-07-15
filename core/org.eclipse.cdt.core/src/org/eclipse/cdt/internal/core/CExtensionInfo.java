/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.HashMap;

public class CExtensionInfo {

	protected HashMap<String, String> attribMap = new HashMap<String, String>(4);

	public CExtensionInfo(){
		
	}

	public CExtensionInfo(CExtensionInfo base){
		attribMap.putAll(base.attribMap);
	}

	public HashMap<String, String> getAttributes() {
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
		return attribMap.get(key);
	}

}
