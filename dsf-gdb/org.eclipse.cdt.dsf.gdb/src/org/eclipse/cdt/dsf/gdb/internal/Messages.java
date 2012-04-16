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

package org.eclipse.cdt.dsf.gdb.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String CustomTimeoutsMap_Error_initializing_custom_timeouts;

	public static String CustomTimeoutsMap_Invalid_custom_timeout_data;

	public static String CustomTimeoutsMap_Invalid_custom_timeout_value;

	public static String GDBControl_Session_is_terminated;
	static {
		// initialize resource bundle
		NLS.initializeMessages( Messages.class.getName(), Messages.class );
	}

	private Messages() {
	}
}
