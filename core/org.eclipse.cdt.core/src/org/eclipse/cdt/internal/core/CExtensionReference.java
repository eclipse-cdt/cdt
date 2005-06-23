/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.CDescriptorEvent;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtension;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class CExtensionReference implements ICExtensionReference {

	private CDescriptor fDescriptor;
	private String fExtPoint;
	private String fId;

	public CExtensionReference(CDescriptor descriptor, String extPoint, String id) {
		fDescriptor = descriptor;
		fExtPoint = extPoint;
		fId = id;
	}

	public String getExtension() {
		return fExtPoint;
	}

	public String getID() {
		return fId;
	}

	public ICDescriptor getCDescriptor() {
		return fDescriptor;
	}

	private CExtensionInfo getInfo() {
		return fDescriptor.getInfo(this);
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof CExtensionReference) {
			CExtensionReference ext = (CExtensionReference)obj;
			if (ext.fExtPoint.equals(fExtPoint) && ext.fId.equals(fId)) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		return fExtPoint.hashCode() + fId.hashCode();
	}

	public void setExtensionData(String key, String value) throws CoreException {
		getInfo().setAttribute(key, value);
		fDescriptor.updateOnDisk();
		if (!fDescriptor.isInitializing) {
			fDescriptor.fManager.fireEvent(new CDescriptorEvent(fDescriptor, CDescriptorEvent.CDTPROJECT_CHANGED, 0));
		}
	}

	public String getExtensionData(String key) {
		return getInfo().getAttribute(key);
	}

	public ICExtension createExtension() throws CoreException {
		return fDescriptor.createExtensions(this);
	}

	public IConfigurationElement[] getExtensionElements() throws CoreException {
		return fDescriptor.getConfigurationElement(this);
	}
}
