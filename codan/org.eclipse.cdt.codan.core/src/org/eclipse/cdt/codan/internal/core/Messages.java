/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import org.eclipse.osgi.util.NLS;

/**
 * Core Messages
 */
class Messages extends NLS {
	public static String CodanApplication_all_option;
	public static String CodanApplication_Error_ProjectDoesNotExists;
	public static String CodanApplication_LogRunProject;
	public static String CodanApplication_LogRunWorkspace;
	public static String CodanApplication_Options;
	public static String CodanApplication_Usage;
	public static String CodanApplication_verbose_option;
	public static String CodanRunner_Code_analysis_on;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
