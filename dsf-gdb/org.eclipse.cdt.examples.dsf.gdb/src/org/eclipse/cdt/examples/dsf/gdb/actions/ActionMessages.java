/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
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

package org.eclipse.cdt.examples.dsf.gdb.actions;

import org.eclipse.osgi.util.NLS;

public class ActionMessages extends NLS {
	static {
		// initialize resource bundle
		NLS.initializeMessages(ActionMessages.class.getName(), ActionMessages.class);
	}

	private ActionMessages() {
	}

	public static String DsfExtendedTerminateCommand_Confirm_Termination;
	public static String DsfExtendedTerminateCommand_Terminate_the_session;
}
