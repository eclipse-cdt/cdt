/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.core.runtime.CoreException;

public class CExtensionReference implements ICExtensionReference {
	private CDescriptor fDescriptor;
	private String fName;
	private String fId;
		
	public CExtensionReference(CDescriptor descriptor, String name, String id) {
		fDescriptor = descriptor;
		fName = name;
		fId = id;
	}

	public String getExtension() {
		return fName;
	}

	public String getID() {
		return fId;
	}

	private CExtensionInfo getInfo() {
		return fDescriptor.getInfo(this);
	}

	public void setExtensionData(String key, String value) {
		getInfo().setAttribute(key, value);
	}

	public String getExtensionData(String key) {
		return getInfo().getAttribute(key);
	}
	
	public ICExtension createExtension() throws CoreException {
		return fDescriptor.createExtensions(this);
	}

}
