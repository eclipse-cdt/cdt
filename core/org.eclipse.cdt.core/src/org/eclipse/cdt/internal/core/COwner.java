/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public String getID() {
		return fConfig.getOwnerID();
	}

	@Override
	public String getName() {
		return fConfig.getName();
	}

	@Override
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

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof COwner) {
			return ((COwner) obj).getID().equals(getID());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getID().hashCode();
	}
}
