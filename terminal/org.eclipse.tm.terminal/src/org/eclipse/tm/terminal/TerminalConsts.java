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

public interface TerminalConsts 
{
    public static final String  TERMINAL_CONNTYPE_SERIAL                = "Serial"; //$NON-NLS-1$
    public static final String  TERMINAL_CONNTYPE_NETWORK               = "Network"; //$NON-NLS-1$

    public final static String  TERMINAL_IMAGE_DIR_ROOT                 = "icons/"; //$NON-NLS-1$
    public final static String  TERMINAL_IMAGE_DIR_CTOOL                = "ctool16/";   // basic colors - size 16x16 //$NON-NLS-1$
    public final static String  TERMINAL_IMAGE_DIR_LOCALTOOL            = "clcl16/";    // basic colors - size 16x16 //$NON-NLS-1$
    public final static String  TERMINAL_IMAGE_DIR_DLCL                 = "dlcl16/";    // disabled - size 16x16 //$NON-NLS-1$
    public final static String  TERMINAL_IMAGE_DIR_ELCL                 = "elcl16/";    // enabled - size 16x16 //$NON-NLS-1$
    public final static String  TERMINAL_IMAGE_DIR_OBJECT               = "obj16/";     // basic colors - size 16x16 //$NON-NLS-1$
    public final static String  TERMINAL_IMAGE_DIR_WIZBAN               = "wizban/";    // basic colors - size 16x16 //$NON-NLS-1$
    public final static String  TERMINAL_IMAGE_DIR_OVR                  = "ovr16/";     // basic colors - size 7x8 //$NON-NLS-1$
    public final static String  TERMINAL_IMAGE_DIR_VIEW                 = "cview16/";   // views //$NON-NLS-1$
    public final static String  TERMINAL_IMAGE_DIR_EVIEW                = "eview16/";   // views //$NON-NLS-1$

    public static final String  TERMINAL_IMAGE_NEW_TERMINAL             = "TerminalViewNewTerminal"; //$NON-NLS-1$
    public static final String  TERMINAL_IMAGE_CLCL_CONNECT             = "ImageClclConnect"; //$NON-NLS-1$
    public static final String  TERMINAL_IMAGE_CLCL_DISCONNECT          = "ImageClclDisconnect"; //$NON-NLS-1$
    public static final String  TERMINAL_IMAGE_CLCL_SETTINGS            = "ImageClclSettings"; //$NON-NLS-1$

    public static final String  TERMINAL_IMAGE_DLCL_CONNECT             = "ImageDlclConnect"; //$NON-NLS-1$
    public static final String  TERMINAL_IMAGE_DLCL_DISCONNECT          = "ImageDlclDisconnect"; //$NON-NLS-1$
    public static final String  TERMINAL_IMAGE_DLCL_SETTINGS            = "ImageDlclSettings"; //$NON-NLS-1$

    public static final String  TERMINAL_IMAGE_ELCL_CONNECT             = "ImageElclConnect"; //$NON-NLS-1$
    public static final String  TERMINAL_IMAGE_ELCL_DISCONNECT          = "ImageElclDisconnect"; //$NON-NLS-1$
    public static final String  TERMINAL_IMAGE_ELCL_SETTINGS            = "ImageElclSettings"; //$NON-NLS-1$

    public static final String  TERMINAL_PROP_TITLE                     = Messages.getString("TerminalConsts.Terminal_7"); //$NON-NLS-1$
    public static final String  TERMINAL_PROP_NAMENET                   = "net"; //$NON-NLS-1$
    public static final String  TERMINAL_PROP_NAMETGTCONST              = "tgtcons"; //$NON-NLS-1$
    public static final String  TERMINAL_PROP_NAMETELNET                = "telnet"; //$NON-NLS-1$
    public static final String  TERMINAL_PROP_VALUENET                  = "1233"; //$NON-NLS-1$
    public static final String  TERMINAL_PROP_VALUETGTCONST             = "1232"; //$NON-NLS-1$
    public static final String  TERMINAL_PROP_VALUETELNET               = "23"; //$NON-NLS-1$

    public static final String  TERMINAL_PREF_LIMITOUTPUT               = "TerminalPrefLimitOutput"; //$NON-NLS-1$
    public static final String  TERMINAL_PREF_BUFFERLINES                = "TerminalPrefBufferLines"; //$NON-NLS-1$
    public static final String  TERMINAL_PREF_TIMEOUT_SERIAL            = "TerminalPrefTimeoutSerial"; //$NON-NLS-1$
    public static final String  TERMINAL_PREF_TIMEOUT_NETWORK           = "TerminalPrefTimeoutNetwork"; //$NON-NLS-1$
    
    public static final String  PLUGIN_HOME                                     = "org.eclipse.tm.terminal"; //$NON-NLS-1$
    public static final String  HELP_VIEW                                       = PLUGIN_HOME + ".terminal_view"; //$NON-NLS-1$

    public static final String  TERMINAL_TEXT_NEW_TERMINAL              = Messages.getString("TerminalConsts.New_terminal"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_CONNECT                   = Messages.getString("TerminalConsts.Connect_2"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_DISCONNECT                = Messages.getString("TerminalConsts.Disconnect_3"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_SETTINGS_ELLIPSE          = Messages.getString("TerminalConsts.Settings..._4"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_COPY                      = Messages.getString("TerminalConsts.Copy_5"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_CUT                       = Messages.getString("TerminalConsts.0"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_PASTE                     = Messages.getString("TerminalConsts.Paste_6"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_SELECTALL                 = Messages.getString("TerminalConsts.Select_All_7"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_CLEARALL                  = Messages.getString("TerminalConsts.Clear_All_8"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_TERMINALSETTINGS          = Messages.getString("TerminalConsts.Terminal_Settings_1"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_CONNECTIONTYPE            = Messages.getString("TerminalConsts.Connection_Type_2"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_SETTINGS                  = Messages.getString("TerminalConsts.Settings_3"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_PORT                      = Messages.getString("TerminalConsts.Port_4"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_BAUDRATE                  = Messages.getString("TerminalConsts.Baud_Rate_5"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_DATABITS                  = Messages.getString("TerminalConsts.Data_Bits_6"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_STOPBITS                  = Messages.getString("TerminalConsts.Stop_Bits_7"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_PARITY                    = Messages.getString("TerminalConsts.Parity_8"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_FLOWCONTROL               = Messages.getString("TerminalConsts.1"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_HOST                      = Messages.getString("TerminalConsts.Host_11"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_LIMITOUTPUT               = Messages.getString("TerminalConsts.Limit_terminal_output_16"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_BUFFERLINES               = Messages.getString("TerminalConsts.Terminal_buffer_lines__17"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_SERIALTIMEOUT             = Messages.getString("TerminalConsts.Serial_timeout_(seconds)__18"); //$NON-NLS-1$
    public static final String  TERMINAL_TEXT_NETWORKTIMEOUT            = Messages.getString("TerminalConsts.Network_timeout_(seconds)__19"); //$NON-NLS-1$

    public static final String  TERMINAL_MSG_ERROR_1                    = Messages.getString("TerminalConsts.Terminal_Error_12"); //$NON-NLS-1$
    public static final String  TERMINAL_MSG_ERROR_2                    = Messages.getString("TerminalConsts.Socket_Error_13"); //$NON-NLS-1$
    public static final String  TERMINAL_MSG_ERROR_3                    = Messages.getString("TerminalConsts.IO_Error_14"); //$NON-NLS-1$
    public static final String  TERMINAL_MSG_ERROR_4                    = Messages.getString("TerminalConsts.Serial_port___{0}___is_currently_in_use_!_nDo_you_want_to_close_the_port__15"); //$NON-NLS-1$
    public static final String  TERMINAL_MSG_ERROR_5                    = Messages.getString("TerminalConsts.Error_16"); //$NON-NLS-1$
    public static final String  TERMINAL_MSG_ERROR_6                    = Messages.getString("TerminalConsts.Emulator_is_not_supported._17"); //$NON-NLS-1$

    public static final String  TERMINAL_FONT_DEFINITION                = "terminal.views.view.font.definition"; //$NON-NLS-1$
        
    public static final String  TERMINAL_TRACE_DEBUG_LOG                = "org.eclipse.tm.terminal/debug/log"; //$NON-NLS-1$
    public static final String  TERMINAL_TRACE_DEBUG_LOG_ERROR          = "org.eclipse.tm.terminal/debug/log/error"; //$NON-NLS-1$
    public static final String  TERMINAL_TRACE_DEBUG_LOG_INFO           = "org.eclipse.tm.terminal/debug/log/info"; //$NON-NLS-1$
    public static final String  TERMINAL_TRACE_DEBUG_LOG_CHAR           = "org.eclipse.tm.terminal/debug/log/char"; //$NON-NLS-1$
    public static final String  TERMINAL_TRACE_DEBUG_LOG_BUFFER_SIZE    = "org.eclipse.tm.terminal/debug/log/buffer/size"; //$NON-NLS-1$

    public static final boolean TERMINAL_DEFAULT_LIMITOUTPUT            = true;
    public static final int     TERMINAL_DEFAULT_BUFFERLINES            = 1000;
    public static final int     TERMINAL_DEFAULT_TIMEOUT_SERIAL         = 5;
    public static final int     TERMINAL_DEFAULT_TIMEOUT_NETWORK        = 5;

    public static final int     TERMINAL_ID_OK                          = 0;
    public static final int     TERMINAL_ID_CANCEL                      = 1;
    public static final int     TERMINAL_ID_CONNECT                     = 2;
    
    public static final int     TERMINAL_KEY_ESCAPE                     = 27;
    public static final int     TERMINAL_KEY_H                          = 104;
    public static final int     TERMINAL_KEY_J                          = 106;
    public static final int     TERMINAL_KEY_K                          = 107;
    public static final int     TERMINAL_KEY_L                          = 108;
    public static final int     TERMINAL_KEY_CR                         = 13;
}
