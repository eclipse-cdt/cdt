/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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

import org.eclipse.cdt.core.ICOwner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class COwnerConfiguration {

	final IConfigurationElement fElement;
	final String fOwnerID, fName;

	public COwnerConfiguration(IConfigurationElement element) {
		fElement = element;
		fOwnerID = fElement.getDeclaringExtension().getUniqueIdentifier();
		fName = fElement.getDeclaringExtension().getLabel();
	}

	public COwnerConfiguration(String id, String name) {
		fElement = null;
		fOwnerID = id;
		fName = name;
	}

	public String getOwnerID() {
		return fOwnerID;
	}

	public String getName() {
		return fName;
	}

	public String getPlatform() {
		String platform = null;
		if (fElement != null) {
			platform = fElement.getAttribute("platform"); //$NON-NLS-1$
		}
		return platform == null ? "*" : platform; //$NON-NLS-1$
	}

	public ICOwner createOwner() throws CoreException {
		if (fElement != null) {
			return (ICOwner) fElement.createExecutableExtension("class"); //$NON-NLS-1$
		}
		return null;
	}

	public String getNature() {
		return fElement != null ? fElement.getAttribute("natureID") : null; //$NON-NLS-1$
	}

}
