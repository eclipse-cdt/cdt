/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui;

import org.eclipse.cdt.debug.internal.ui.actions.ShowFullPathsAction;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 * @since 1.0
 */
public interface IDsfDebugUIConstants {

	/**
	 * Debug UI plug-in identifier (value <code>"org.eclipse.cdt.dsf.ui"</code>).
	 */
	public static final String PLUGIN_ID = DsfUIPlugin.PLUGIN_ID; 
	
	/** Loaded shared library symbols image identifier. */
	public static final String IMG_OBJS_SHARED_LIBRARY_SYMBOLS_LOADED = "icons/library_syms_obj.gif"; //$NON-NLS-1$
	
	/** Unloaded Shared library symbols image identifier. */
	public static final String IMG_OBJS_SHARED_LIBRARY_SYMBOLS_UNLOADED = "icons/library_obj.gif"; //$NON-NLS-1$
	
	/**
	 * Integer preference to control the maximum amount of stack frames to
	 * retrieve from the backend. Default value is <code>10</code>.
	 * @see {@link #PREF_STACK_FRAME_LIMIT_ENABLE}
	 * 
	 * @since 1.1
	 */
	public static final String PREF_STACK_FRAME_LIMIT = "stackFrameLimit"; //$NON-NLS-1$
	
	/**
	 * Boolean preference whether to apply the stack frame limit preference. Default is <code>true</code>.
	 * @see {@link #PREF_STACK_FRAME_LIMIT}
	 * 
	 * @since 1.1
	 */
	public static final String PREF_STACK_FRAME_LIMIT_ENABLE = "stackFrameLimitEnable"; //$NON-NLS-1$

	/**
	 * Boolean preference whether to keep stepping speed in sync with UI updates. Default is <code>false</code>.
	 * 
	 * @since 1.1
	 */
	public static final String PREF_WAIT_FOR_VIEW_UPDATE_AFTER_STEP_ENABLE = "delaySteppingForViewUpdatesEnable"; //$NON-NLS-1$

	/**
	 * Integer preference to enforce a minimum time interval between steps. Default is <code>100</code>.
	 * 
	 * @since 1.1
	 */
	public static final String PREF_MIN_STEP_INTERVAL= "minStepInterval"; //$NON-NLS-1$
	
    /**
	 * @since 1.1
	 */
    public static final String PREFERENCE_PAGE= PLUGIN_ID + ".preference_page_context"; //$NON-NLS-1$
    
    /**
     * Stale data foreground color preference key.  
     * 
     * @since 2.0 
     */
    public static final String PREF_COLOR_STALE_DATA_FOREGROUND= PLUGIN_ID + ".staledata.foreground"; //$NON-NLS-1$

    /**
     * Stale data foreground color preference key.  
     * 
     * @since 2.0 
     */
    public static final String PREF_COLOR_STALE_DATA_BACKGROUND= PLUGIN_ID + ".staledata.background"; //$NON-NLS-1$

    /**
     * Presentation context id for the expression hover.
     * 
     * @since 2.1
     */
	public static final String ID_EXPRESSION_HOVER= PLUGIN_ID + ".expression_hover"; //$NON-NLS-1$
	
	/** 
	 * Property id to know if we should show full paths in the debug view.
	 * The value of this id must match what is being used as a full key in ShowFullPathsAction.run()
	 * 
	 * @since 2.1 */
	public  static final String DEBUG_VIEW_SHOW_FULL_PATH_PROPERTY = IDebugUIConstants.ID_DEBUG_VIEW + "." + ShowFullPathsAction.PREF_KEY; //$NON-NLS-1$
}
