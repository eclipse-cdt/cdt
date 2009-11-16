/*******************************************************************************
 * Copyright (c) 2006, 2009 Siemens AG and others.
 * All rights reserved. This content and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Norbert Ploett - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.errorparsers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.errorparsers.messages"; //$NON-NLS-1$
	public static String GCCErrorParser_sikp_instantiatedFromHere;
	public static String GCCErrorParser_skip_forEachFunction;
	public static String GCCErrorParser_skip_note;
	public static String GCCErrorParser_skip_UndeclaredOnlyOnce;
	public static String GCCErrorParser_varPattern_conflictTypes;
	public static String GCCErrorParser_varPattern_defdNotUsed;
	public static String GCCErrorParser_varPattern_parseError;
	public static String GCCErrorParser_varPattern_undeclared;
	public static String GCCErrorParser_Warnings;
	public static String GLDErrorParser_error_general;
	public static String GLDErrorParser_error_text;
	public static String GLDErrorParser_warning_general;
	public static String GLDErrorParser_warning_text;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
