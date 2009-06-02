/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Gvozdev (Quoin Inc.) - contributed to CDT from org.eclipse.core.tests.resources v20090320
 *******************************************************************************/
package org.eclipse.cdt.core.internal.tests.filesystem.ram;

import org.eclipse.core.runtime.*;

/**
 * 
 */
public class Policy {

	public static void error(String message) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.cdt.core.tests.internal.filesystem.ram", 1, message, null));
	}

	private Policy() {
		super();
	}

}
