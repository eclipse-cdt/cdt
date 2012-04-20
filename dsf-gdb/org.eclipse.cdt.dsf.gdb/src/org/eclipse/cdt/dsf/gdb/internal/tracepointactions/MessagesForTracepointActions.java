/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Remove strings that should not be translated
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.tracepointactions;

import org.eclipse.osgi.util.NLS;

/**
 * Preference strings.
 * @since 3.0
 */
class MessagesForTracepointActions extends NLS {
	private static final String BUNDLE_NAME= "org.eclipse.cdt.dsf.gdb.internal.tracepointactions.messages"; //$NON-NLS-1$

	public static String TracepointActions_Untitled_Collect;
	public static String TracepointActions_Untitled_Evaluate;
	public static String TracepointActions_Untitled_WhileStepping;
	public static String TracepointActions_Collect_Name;
	public static String TracepointActions_Evaluate_Name;
	public static String TracepointActions_WhileStepping_Name;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, MessagesForTracepointActions.class);
	}

	private MessagesForTracepointActions() {
	}
}
