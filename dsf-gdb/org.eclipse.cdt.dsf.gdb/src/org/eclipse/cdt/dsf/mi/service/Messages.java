/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.osgi.util.NLS;

/**
 * Preference strings.
 * @since 3.0
 */
class Messages extends NLS {
	public static String Breakpoint_attribute_problem;
	public static String Breakpoint_installation_failed;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
