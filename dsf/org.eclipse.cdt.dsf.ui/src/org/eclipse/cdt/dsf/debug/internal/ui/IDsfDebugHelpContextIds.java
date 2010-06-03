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
package org.eclipse.cdt.dsf.debug.internal.ui;

import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @since 2.0
 */
public interface IDsfDebugHelpContextIds {

    public static final String PREFIX = IDebugUIConstants.PLUGIN_ID + "."; //$NON-NLS-1$

    public static final String DETAIL_PANE = PREFIX + "detail_pane_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_ASSIGN_VALUE_ACTION = PREFIX + "detail_pane_assign_value_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_CONTENT_ASSIST_ACTION = PREFIX + "detail_pane_content_assist_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_CUT_ACTION = PREFIX + "detail_pane_cut_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_COPY_ACTION = PREFIX + "detail_pane_copy_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_PASTE_ACTION = PREFIX + "detail_pane_paste_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_SELECT_ALL_ACTION = PREFIX + "detail_pane_select_all_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_FIND_REPLACE_ACTION = PREFIX + "detail_pane_find_replace_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_WORD_WRAP_ACTION = PREFIX + "detail_pane_word_wrap_action_context"; //$NON-NLS-1$
    public static final String DETAIL_PANE_MAX_LENGTH_ACTION = PREFIX + "detail_pane_max_length_action_context"; //$NON-NLS-1$
}
