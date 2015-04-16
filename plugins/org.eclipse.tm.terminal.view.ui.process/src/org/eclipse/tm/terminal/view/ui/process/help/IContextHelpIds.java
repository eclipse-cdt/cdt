/*******************************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.process.help;

import org.eclipse.tm.terminal.view.ui.process.activator.UIPlugin;


/**
 * Context help id definitions.
 */
public interface IContextHelpIds {

	/**
	 * UI plug-in common context help id prefix.
	 */
	public final static String PREFIX = UIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$

	// ***** Message dialog boxes *****

	/**
	 * Process connector: Create process failed
	 */
	public final static String MESSAGE_CREATE_PROCESS_FAILED = PREFIX + ".status.messageCreateProcessFailed"; //$NON-NLS-1$
}
