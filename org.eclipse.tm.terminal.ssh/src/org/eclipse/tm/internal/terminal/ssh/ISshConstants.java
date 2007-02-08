/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - extracted from various team.cvs plugins
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.ssh;


/**
 * Defines the constants used by the terminal.ssh Plugin
 */
public interface ISshConstants {

    // These are from ISSHContants
    public static final String KEY_SSH2HOME="CVSSSH2PreferencePage.SSH2HOME"; //$NON-NLS-1$
    public static final String KEY_PRIVATEKEY="CVSSSH2PreferencePage.PRIVATEKEY"; //$NON-NLS-1$

    // These are from ICVSUIConstants
    public static final String PREF_USE_PROXY = "proxyEnabled"; //$NON-NLS-1$
    public static final String PREF_PROXY_TYPE = "proxyType"; //$NON-NLS-1$
    public static final String PREF_PROXY_HOST = "proxyHost"; //$NON-NLS-1$
    public static final String PREF_PROXY_PORT = "proxyPort"; //$NON-NLS-1$
    public static final String PREF_PROXY_AUTH = "proxyAuth"; //$NON-NLS-1$
    
    // These are from CVSProviderPlugin
    public static final String PROXY_TYPE_HTTP = "HTTP"; //$NON-NLS-1$
    public static final String PROXY_TYPE_SOCKS5 = "SOCKS5"; //$NON-NLS-1$
    public static final String HTTP_DEFAULT_PORT="80"; //$NON-NLS-1$
    public static final String SOCKS5_DEFAULT_PORT="1080"; //$NON-NLS-1$

    // These are from cvs.ui.IHelpContextIds
	public static final String CVSUIPREFIX = "org.eclipse.team.cvs.ui."; //$NON-NLS-1$
	public static final String HELP_USER_VALIDATION_DIALOG = CVSUIPREFIX + "user_validation_dialog_context"; //$NON-NLS-1$
	public static final String HELP_KEYBOARD_INTERACTIVE_DIALOG = CVSUIPREFIX + "keyboard_interactive_dialog_context"; //$NON-NLS-1$

}
