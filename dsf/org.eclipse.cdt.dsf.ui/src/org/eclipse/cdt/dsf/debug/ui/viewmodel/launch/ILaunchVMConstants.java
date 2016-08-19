/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import org.eclipse.swt.graphics.RGB;

/**
 * @since 2.0
 */
public interface ILaunchVMConstants {

    public static final String PROP_ID = "id";  //$NON-NLS-1$
    
    public static final String PROP_IS_SUSPENDED = "is_suspended";  //$NON-NLS-1$

    public static final String PROP_IS_STEPPING = "is_stepping";  //$NON-NLS-1$

    public static final String PROP_FRAME_ADDRESS = "frame_address";  //$NON-NLS-1$

    public static final String PROP_FRAME_FUNCTION = "frame_function";  //$NON-NLS-1$

    public static final String PROP_FRAME_FILE = "frame_file";  //$NON-NLS-1$

    public static final String PROP_FRAME_LINE = "frame_line";  //$NON-NLS-1$

    public static final String PROP_FRAME_COLUMN = "frame_column";  //$NON-NLS-1$

    public static final String PROP_FRAME_MODULE = "frame_module";  //$NON-NLS-1$
    
    public static final String PROP_STATE_CHANGE_REASON = "state_change_reason";  //$NON-NLS-1$

    /**
     * @since 2.1
     */
    public static final String PROP_STATE_CHANGE_DETAILS = "state_change_details";  //$NON-NLS-1$

    public static final String PROP_ELEMENT_SELECTED_KNOWN = "element_selected_known"; //$NON-NLS-1$  
    
    public static final String PROP_ELEMENT_SELECTED = "element_selected"; //$NON-NLS-1$
    
    
    // temp, for prototyping purposes
    public boolean SELECTION_HIGHLIGHT_BG = true;
    public boolean SELECTION_HIGHLIGHT_FG = false;
    public RGB SELECTION_HIGHLIGHT_BG_COLOR = new RGB(238,238,224); 
    public RGB SELECTION_HIGHLIGHT_FG_COLOR = new RGB(45,45,215);
    
//    public boolean SELECTION_HIGHLIGHT_BOLD = false;
//    public boolean SELECTION_HIGHLIGHT_ITALIC = true;
    public boolean SELECTION_HIGHLIGHT_BOLD = false;
    public boolean SELECTION_HIGHLIGHT_ITALIC = false;
    
    public boolean SELECTION_HIGHLIGHT_ASCII_MARKER = true; // <-- gdb N
    
    public int SELECTION_HIGHLIGHT_FONT_SIZE = 12;
    

//    public boolean SELECTION_HIGHLIGHT_IMAGE = true;
    
//    public boolean SELECTION_HIGHLIGHT_2_COLUMNS_IN_DV = true;
    
}
