/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.tm.terminal;

public interface TerminalMsg
{
    public static final String ON_TERMINAL_FOCUS               = "OnTerminalFocus"; //$NON-NLS-1$
    public static final String ON_TERMINAL_NEW_TERMINAL        = "OnTerminalNew"; //$NON-NLS-1$
    public static final String ON_TERMINAL_CONNECT             = "OnTerminalConnect"; //$NON-NLS-1$
    public static final String ON_TERMINAL_CONNECTING          = "OnTerminalConnecting"; //$NON-NLS-1$
    public static final String ON_TERMINAL_DISCONNECT          = "OnTerminalDisconnect"; //$NON-NLS-1$
    public static final String ON_TERMINAL_SETTINGS            = "OnTerminalSettings"; //$NON-NLS-1$
    public static final String ON_TERMINAL_STATUS              = "OnTerminalStatus"; //$NON-NLS-1$
    public static final String ON_TERMINAL_DATAAVAILABLE       = "OnTerminalDataAvailable"; //$NON-NLS-1$
    public static final String ON_TERMINAL_FONTCHANGED         = "OnTerminalFontChanged"; //$NON-NLS-1$
    public static final String ON_EDIT_COPY                    = "OnEditCopy"; //$NON-NLS-1$
    public static final String ON_EDIT_CUT                     = "OnEditCut"; //$NON-NLS-1$
    public static final String ON_EDIT_PASTE                   = "OnEditPaste"; //$NON-NLS-1$
    public static final String ON_EDIT_CLEARALL                = "OnEditClearAll"; //$NON-NLS-1$
    public static final String ON_EDIT_SELECTALL               = "OnEditSelectAll"; //$NON-NLS-1$
    public static final String ON_UPDATE_TERMINAL_CONNECT      = "OnUpdateTerminalConnect"; //$NON-NLS-1$
    public static final String ON_UPDATE_TERMINAL_DISCONNECT   = "OnUpdateTerminalDisconnect"; //$NON-NLS-1$
    public static final String ON_UPDATE_TERMINAL_SETTINGS     = "OnUpdateTerminalSettings"; //$NON-NLS-1$
    public static final String ON_UPDATE_EDIT_COPY             = "OnUpdateEditCopy"; //$NON-NLS-1$
    public static final String ON_UPDATE_EDIT_CUT              = "OnUpdateEditCut"; //$NON-NLS-1$
    public static final String ON_UPDATE_EDIT_PASTE            = "OnUpdateEditPaste"; //$NON-NLS-1$
    public static final String ON_UPDATE_EDIT_CLEARALL         = "OnUpdateEditClearAll"; //$NON-NLS-1$
    public static final String ON_UPDATE_EDIT_SELECTALL        = "OnUpdateEditSelectAll"; //$NON-NLS-1$
    public static final String ON_CONNTYPE_SELECTED            = "OnConnTypeSelected"; //$NON-NLS-1$
    public static final String ON_LIMITOUTPUT_SELECTED         = "OnLimitOutputSelected"; //$NON-NLS-1$
    public static final String ON_OK                           = "OnOK"; //$NON-NLS-1$
    public static final String ON_CANCEL                       = "OnCancel"; //$NON-NLS-1$
    public static final String ON_HELP                         = "OnHelp"; //$NON-NLS-1$
}
