/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
