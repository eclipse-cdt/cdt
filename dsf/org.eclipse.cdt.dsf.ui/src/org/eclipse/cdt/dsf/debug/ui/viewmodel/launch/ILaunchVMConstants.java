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

}
