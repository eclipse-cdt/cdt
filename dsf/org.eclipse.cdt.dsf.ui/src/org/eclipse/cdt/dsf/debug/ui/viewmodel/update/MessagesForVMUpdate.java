/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.debug.ui.viewmodel.update;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForVMUpdate extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.dsf.debug.ui.viewmodel.update.messages"; //$NON-NLS-1$

	public static String BreakpointHitUpdatePolicy_name;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, MessagesForVMUpdate.class);
	}

	private MessagesForVMUpdate() {
	}
}
