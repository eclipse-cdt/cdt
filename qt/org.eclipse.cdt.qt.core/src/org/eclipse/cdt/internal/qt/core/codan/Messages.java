/*
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.codan;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.qt.core.codan.messages"; //$NON-NLS-1$
	public static String Function_Not_Resolved_Msg;
	public static String Parameter_Not_Resolved_Msg;
	public static String Missing_Parameter_Msg;
	public static String SignalSlot_Not_Defined_Msg;

	public static String QtConnect_macro_without_method_1;
	public static String QtConnect_macro_method_not_found_3;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
