/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.breakpoint.actions;

import org.eclipse.osgi.util.NLS;

/** @since 5.0 */
public class Messages extends NLS {
	public static String MIReverseDebugEnabler_UnableEnable;
	public static String MIReverseDebugEnabler_UnableDisable;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
