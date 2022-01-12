/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Wind River Systems, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForDebugVM extends NLS {
	public static String ErrorLabelText__text_format;
	public static String ErrorLabelText_Error_message__text_page_break_delimiter;

	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForDebugVM.class.getName(), MessagesForDebugVM.class);
	}

	private MessagesForDebugVM() {
	}
}
