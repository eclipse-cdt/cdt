/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
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

package org.eclipse.cdt.dsf.debug.ui.viewmodel.modules;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForModulesVM extends NLS {
	public static String ModulesVMNode_No_columns__text_format;

	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForModulesVM.class.getName(), MessagesForModulesVM.class);
	}

	private MessagesForModulesVM() {
	}
}
