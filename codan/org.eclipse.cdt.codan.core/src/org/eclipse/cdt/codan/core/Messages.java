/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core;

import org.eclipse.osgi.util.NLS;

/**
 * Core Messages
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.codan.core.messages"; //$NON-NLS-1$
	public static String CodanApplication_all_option;
	public static String CodanApplication_Error_ProjectDoesNotExists;
	public static String CodanApplication_LogRunProject;
	public static String CodanApplication_LogRunWorkspace;
	public static String CodanApplication_Options;
	public static String CodanApplication_Usage;
	public static String CodanApplication_verbose_option;
	public static String CodanBuilder_Code_Analysis_On;
	public static String FileScopeProblemPreference_Label;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
