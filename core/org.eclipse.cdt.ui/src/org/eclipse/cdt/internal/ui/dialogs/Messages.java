/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
