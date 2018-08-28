/*******************************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.rse.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;



/**
 * Terminal RSE add-on property tester implementation.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	// Supported system type id's
	private final static String[] VALID_SYSTEM_TYPES = new String[] {
			"org.eclipse.rse.systemtype.linux", //$NON-NLS-1$
			"org.eclipse.rse.systemtype.unix", //$NON-NLS-1$
			"org.eclipse.rse.systemtype.aix", //$NON-NLS-1$
			"org.eclipse.rse.systemtype.ssh" //$NON-NLS-1$
	};

	private final static List<String> VALID_SYSTEM_TYPES_LIST = Arrays.asList(VALID_SYSTEM_TYPES);

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// Determine the host object
		IHost host = null;

		if (receiver instanceof IHost) host = (IHost) receiver;
		if (host == null && receiver instanceof ISubSystem) host = ((ISubSystem) receiver).getHost();
		if (host == null && receiver instanceof ISystemFilterReference) host = ((ISystemFilterReference) receiver).getSubSystem().getHost();
		if (host == null && receiver instanceof IRemoteFile) host = ((IRemoteFile) receiver).getHost();

		if (host != null) {
			if ("isVisible".equals(property) && expectedValue instanceof Boolean) { //$NON-NLS-1$
				String systemTypeID = host.getSystemType().getId();
				boolean validID = systemTypeID != null ? VALID_SYSTEM_TYPES_LIST.contains(systemTypeID) : false;
				return ((Boolean)expectedValue).booleanValue() == validID;
			}
		}

		return false;
	}

}
