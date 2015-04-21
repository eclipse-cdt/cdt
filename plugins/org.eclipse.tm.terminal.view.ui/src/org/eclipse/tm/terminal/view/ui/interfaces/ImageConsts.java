/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361363] [TERMINALS] Implement "Pin&Clone" for the "Terminals" view
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.interfaces;

/**
 * Image registry constants.
 */
public interface ImageConsts {
	/**
	 * The root directory where to load the images from, relative to
	 * the bundle directory.
	 */
    public final static String IMAGE_DIR_ROOT = "icons/"; //$NON-NLS-1$

    /**
     * The directory where to load colored local toolbar images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_CLCL = "clcl16/"; //$NON-NLS-1$

    /**
     * The directory where to load disabled local toolbar images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_DLCL = "dlcl16/"; //$NON-NLS-1$

    /**
     * The directory where to load enabled local toolbar images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_ELCL = "elcl16/"; //$NON-NLS-1$

    /**
     * The directory where to load view related images from, relative to
     * the image root directory.
     */
    public final static String  IMAGE_DIR_EVIEW = "eview16/"; //$NON-NLS-1$

    /**
     * The key to access the terminals console view image.
     */
    public static final String VIEW_Terminals = "TerminalsView"; //$NON-NLS-1$

    /**
     * The key to access the scroll lock action image (enabled).
     */
    public static final String  ACTION_ScrollLock_Enabled = "ScrollLockAction_enabled"; //$NON-NLS-1$

    /**
     * The key to access the scroll lock action image (disabled).
     */
    public static final String  ACTION_ScrollLock_Disabled = "ScrollLockAction_disabled"; //$NON-NLS-1$

    /**
     * The key to access the scroll lock action image (hover).
     */
    public static final String  ACTION_ScrollLock_Hover = "ScrollLockAction_hover"; //$NON-NLS-1$

    /**
     * The key to access the pin terminal action image (enabled).
     */
    public static final String  ACTION_PinTerminal_Enabled = "PinTerminalAction_enabled"; //$NON-NLS-1$

    /**
     * The key to access the pin terminal action image (disabled).
     */
    public static final String  ACTION_PinTerminal_Disabled = "PinTerminalAction_disabled"; //$NON-NLS-1$

    /**
     * The key to access the pin terminal action image (hover).
     */
    public static final String  ACTION_PinTerminal_Hover = "PinTerminalAction_hover"; //$NON-NLS-1$

    /**
     * The key to access the toggle command field action image (enabled).
     */
    public static final String  ACTION_ToggleCommandField_Enabled = "ToggleCommandField_enabled"; //$NON-NLS-1$

    /**
     * The key to access the toggle command field action image (disabled).
     */
    public static final String  ACTION_ToggleCommandField_Disabled = "ToggleCommandField_disabled"; //$NON-NLS-1$

    /**
     * The key to access the toggle command field action image (hover).
     */
    public static final String  ACTION_ToggleCommandField_Hover = "ToggleCommandField_hover"; //$NON-NLS-1$
}
