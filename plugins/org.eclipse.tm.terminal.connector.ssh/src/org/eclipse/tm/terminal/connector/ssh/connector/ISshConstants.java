/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - extracted from various team.cvs plugins
 * Martin Oberhuber (Wind River) - [175686] Adapted to new IJSchService API 
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.connector;


/**
 * Defines the constants used by the terminal.ssh Plugin
 */
public interface ISshConstants {

    // These are from cvs.ui.IHelpContextIds
	public static final String CVSUIPREFIX = "org.eclipse.team.cvs.ui."; //$NON-NLS-1$
	public static final String HELP_USER_VALIDATION_DIALOG = CVSUIPREFIX + "user_validation_dialog_context"; //$NON-NLS-1$
	public static final String HELP_KEYBOARD_INTERACTIVE_DIALOG = CVSUIPREFIX + "keyboard_interactive_dialog_context"; //$NON-NLS-1$

}
