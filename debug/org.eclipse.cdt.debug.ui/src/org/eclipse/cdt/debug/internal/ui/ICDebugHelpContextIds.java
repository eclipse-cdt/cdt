/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.ui.ICDebugUIConstants;

/**
 * 
 * Help context ids for the C/C++ debug ui.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 * @since Jul 23, 2002
 */
public interface ICDebugHelpContextIds
{
	public static final String PREFIX = ICDebugUIConstants.PLUGIN_ID + "."; //$NON-NLS-1$

	// Views
	public static final String REGISTERS_VIEW = PREFIX + "registers_view_context"; //$NON-NLS-1$
	public static final String MEMORY_VIEW = PREFIX + "memory_view_context"; //$NON-NLS-1$

	// Preference pages
	public static final String MEMORY_PREFERENCE_PAGE = PREFIX + "memory_preference_page_context"; //$NON-NLS-1$
}
