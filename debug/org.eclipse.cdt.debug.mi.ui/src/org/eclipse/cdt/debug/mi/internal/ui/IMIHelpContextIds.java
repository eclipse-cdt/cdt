/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui;

/**
 * 
 * Help context ids for the C/C++ debug ui.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 * @since Oct 4, 2002
 */
public interface IMIHelpContextIds
{
	public static final String PREFIX = IMIUIConstants.PLUGIN_ID + "."; //$NON-NLS-1$

	// Preference pages
	public static final String MI_PREFERENCE_PAGE = PREFIX + "mi_preference_page_context"; //$NON-NLS-1$
}
