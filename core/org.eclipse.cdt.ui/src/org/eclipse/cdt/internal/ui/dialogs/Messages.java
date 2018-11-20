/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	public static String FormattingScopeDialog_do_not_ask_again;
	public static String FormattingScopeDialog_format_file;
	public static String FormattingScopeDialog_format_statement;
	public static String FormattingScopeDialog_message;
	public static String FormattingScopeDialog_title;

	static {
		// Initialize resource bundle.
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
