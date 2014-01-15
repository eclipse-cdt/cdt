/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;


import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String Default_LineDynamicPrintf_String;
	public static String Default_FunctionDynamicPrintf_String;
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
