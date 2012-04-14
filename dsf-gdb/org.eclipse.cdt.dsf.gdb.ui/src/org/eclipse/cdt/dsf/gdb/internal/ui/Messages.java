/*******************************************************************************
 * Copyright (c) 2011 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String GdbStatusHandler_Error;

	public static String GdbStatusHandler_Information;

	public static String GdbStatusHandler_Warning;
	static {
		// initialize resource bundle
		NLS.initializeMessages( Messages.class.getName(), Messages.class );
	}

	private Messages() {
	}
}
