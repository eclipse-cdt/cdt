/*******************************************************************************
 * Copyright (c) 2011, 2013 Mentor Graphics and others.
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
 * Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String CoreBuildLocalDebugLaunchDelegate_FailureLaunching;

	public static String CoreBuildLocalDebugLaunchDelegate_FailureStart;

	public static String CustomTimeoutsMap_Error_initializing_custom_timeouts;

	public static String CustomTimeoutsMap_Invalid_custom_timeout_data;

	public static String CustomTimeoutsMap_Invalid_custom_timeout_value;

	public static String GDBControl_Session_is_terminated;

	public static String StepIntoSelection;

	public static String StepIntoSelection_Execution_did_not_enter_function;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
