/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - initial API and implementation
 * Kevin Doyle (IBM) - [239700] Compile Commands are available on items it shouldn't
 ********************************************************************************/
package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

public class RemoteFilePropertyTester extends PropertyTester {

	public static final String PROPERTY_ISDIRECTORY = "isdirectory"; //$NON-NLS-1$
	public static final String PROPERTY_ISROOT = "isroot"; //$NON-NLS-1$
	public static final String PROPERTY_ISVIRTUAL = "isvirtual"; //$NON-NLS-1$
	public static final String PROPERTY_ISARCHIVE = "isarchive"; //$NON-NLS-1$
	
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		boolean test = ((Boolean) expectedValue).booleanValue();
		if (receiver != null && receiver instanceof IRemoteFile) {
			if (property.equals(PROPERTY_ISDIRECTORY))
				return ((IRemoteFile) receiver).isDirectory() == test;
			else if (property.equals(PROPERTY_ISROOT))
				return ((IRemoteFile) receiver).isRoot() == test;
			else if (property.equals(PROPERTY_ISVIRTUAL))
				return ArchiveHandlerManager.isVirtual(((IRemoteFile) receiver).getAbsolutePath()) == test;
			else if (property.equals(PROPERTY_ISARCHIVE))
				return ((IRemoteFile) receiver).isArchive() == test;
		}
		return !test;
	}

}
