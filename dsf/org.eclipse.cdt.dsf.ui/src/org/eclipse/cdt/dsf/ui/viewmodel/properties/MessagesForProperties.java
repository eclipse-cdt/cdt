/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import org.eclipse.osgi.util.NLS;

class MessagesForProperties extends NLS {
	public static String DefaultLabelMessage_label;
	public static String PropertiesUpdateStatus_message;

	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForProperties.class.getName(), MessagesForProperties.class);
	}

	private MessagesForProperties() {
	}
}
