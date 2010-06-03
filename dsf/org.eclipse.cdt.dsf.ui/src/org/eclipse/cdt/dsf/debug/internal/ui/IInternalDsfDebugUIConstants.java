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

import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;

/**
 * @since 2.0
 */
public interface IInternalDsfDebugUIConstants {
    /**
     * Boolean preference controlling whether the text in the detail panes is
     * wrapped. When <code>true</code> the text in the detail panes will be
     * wrapped in new variable view.
     */
    public static final String PREF_DETAIL_PANE_WORD_WRAP = IDsfDebugUIConstants.PLUGIN_ID + ".detail_pane_word_wrap"; //$NON-NLS-1$
}
