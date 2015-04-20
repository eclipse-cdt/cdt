/*******************************************************************************
 * Copyright (c) 2003, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - extracted from TerminalConsts
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Anna Dushistova (MontaVista) - [227537] moved actions from terminal.view to terminal plugin
 * Michael Scharf (Wind River) - [172483] added some more icons
 * Michael Scharf (Wind River) - [240023] Get rid of the terminal's "Pin" button
 *******************************************************************************/
package org.eclipse.remote.internal.console;

public interface ImageConsts
{
    public final static String  IMAGE_DIR_ROOT                 = "icons/"; //$NON-NLS-1$
    public final static String  IMAGE_DIR_LOCALTOOL            = "clcl16/";    // basic colors - size 16x16 //$NON-NLS-1$
    public final static String  IMAGE_DIR_DLCL                 = "dlcl16/";    // disabled - size 16x16 //$NON-NLS-1$
    public final static String  IMAGE_DIR_ELCL                 = "elcl16/";    // enabled - size 16x16 //$NON-NLS-1$
    public final static String  IMAGE_DIR_VIEW                 = "cview16/";   // views //$NON-NLS-1$
    public final static String  IMAGE_DIR_EVIEW                = "eview16/";   // views //$NON-NLS-1$

    public static final String  IMAGE_NEW_TERMINAL             = "TerminalViewNewTerminal"; //$NON-NLS-1$
    public static final String  IMAGE_TERMINAL_VIEW            = "TerminalView"; //$NON-NLS-1$
    public static final String  IMAGE_CLCL_CONNECT             = "ImageClclConnect"; //$NON-NLS-1$
    public static final String  IMAGE_CLCL_DISCONNECT          = "ImageClclDisconnect"; //$NON-NLS-1$
    public static final String  IMAGE_CLCL_SETTINGS            = "ImageClclSettings"; //$NON-NLS-1$
    public static final String  IMAGE_CLCL_SCROLL_LOCK         = "ImageClclScrollLock"; //$NON-NLS-1$

    public static final String  IMAGE_DLCL_CONNECT             = "ImageDlclConnect"; //$NON-NLS-1$
    public static final String  IMAGE_DLCL_DISCONNECT          = "ImageDlclDisconnect"; //$NON-NLS-1$
    public static final String  IMAGE_DLCL_SETTINGS            = "ImageDlclSettings"; //$NON-NLS-1$
    public static final String  IMAGE_DLCL_SCROLL_LOCK         = "ImageDlclScrollLock"; //$NON-NLS-1$
    public static final String  IMAGE_DLCL_REMOVE         	   = "ImageDlclRemove"; //$NON-NLS-1$

    public static final String  IMAGE_ELCL_CONNECT             = "ImageElclConnect"; //$NON-NLS-1$
    public static final String  IMAGE_ELCL_DISCONNECT          = "ImageElclDisconnect"; //$NON-NLS-1$
    public static final String  IMAGE_ELCL_SETTINGS            = "ImageElclSettings"; //$NON-NLS-1$
    public static final String  IMAGE_ELCL_SCROLL_LOCK         = "ImageElclScrollLock"; //$NON-NLS-1$
    public static final String  IMAGE_ELCL_REMOVE         	   = "ImageElclRemove"; //$NON-NLS-1$
	public static final String  IMAGE_CLCL_COMMAND_INPUT_FIELD        = "ImageClclCommandInputField";//$NON-NLS-1$
	public static final String  IMAGE_ELCL_COMMAND_INPUT_FIELD        = "ImageDlclCommandInputField";//$NON-NLS-1$
	public static final String  IMAGE_DLCL_COMMAND_INPUT_FIELD        = "ImageDlclCommandInputField";//$NON-NLS-1$
 }
