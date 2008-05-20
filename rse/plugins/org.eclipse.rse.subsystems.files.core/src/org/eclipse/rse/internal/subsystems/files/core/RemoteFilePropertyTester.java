/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Anna Dushistova (MontaVista) - initial API and implementation
 ********************************************************************************/
package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

public class RemoteFilePropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		boolean test = ((Boolean) expectedValue).booleanValue();
		if (receiver != null && receiver instanceof IRemoteFile) {
			return ((IRemoteFile) receiver).isDirectory() && test;

		}
		return !test;
	}

}
