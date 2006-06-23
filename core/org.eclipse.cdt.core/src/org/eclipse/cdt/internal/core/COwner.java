/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICOwner;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class COwner implements ICOwnerInfo {

	final COwnerConfiguration fConfig;

	public COwner(COwnerConfiguration config) throws CoreException {
		fConfig = config;
	}

	public String getID() {
		return fConfig.getOwnerID();
	}

	public String getName() {
		return fConfig.getName();
	}

	public String getPlatform() {
		return fConfig.getPlatform();
	}

	void configure(IProject project, ICDescriptor cproject) throws CoreException {
		ICOwner owner = fConfig.createOwner();
		if (owner != null) {
			owner.configure(cproject);
		}
	}

	void update(IProject project, ICDescriptor cproject, String extensionID) throws CoreException {
		ICOwner owner = fConfig.createOwner();
		if (owner != null) {
			owner.update(cproject, extensionID);
		}
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof COwner) {
			return ((COwner) obj).getID().equals(getID());
		}
		return false;
	}

	public int hashCode() {
		return getID().hashCode();
	}
}
