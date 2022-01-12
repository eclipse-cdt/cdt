/*******************************************************************************
 * Copyright (c) 2011, 2012 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
