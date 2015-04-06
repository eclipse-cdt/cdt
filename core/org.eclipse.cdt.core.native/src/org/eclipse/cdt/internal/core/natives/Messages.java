/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Oberhuber (Wind River) - [303083] Split out the Spawner
 *******************************************************************************/
package org.eclipse.cdt.internal.core.natives;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String Util_exception_cannotCreatePty;
	public static String Util_exception_cannotSetTerminalSize;
	public static String Util_error_cannotRun;
	public static String Util_exception_closeError;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
