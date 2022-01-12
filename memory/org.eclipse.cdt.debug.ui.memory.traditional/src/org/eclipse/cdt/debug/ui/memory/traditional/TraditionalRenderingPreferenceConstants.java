/*******************************************************************************
 * Copyright (c) 2006-2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

/**
 * Constant definitions for plug-in preferences
 */
public class TraditionalRenderingPreferenceConstants {

	public static final String MEM_COLOR_CHANGED = "memoryColorChanged"; //$NON-NLS-1$
	public static final String MEM_COLOR_CHANGED_ITALIC = "memoryColorChanged.italic"; //$NON-NLS-1$
	public static final String MEM_COLOR_CHANGED_BOLD = "memoryColorChanged.bold"; //$NON-NLS-1$
	public static final String MEM_COLOR_CHANGED_BOX = "memoryColorChanged.box"; //$NON-NLS-1$

	public static final String MEM_COLOR_EDIT = "memoryColorEdit"; //$NON-NLS-1$
	public static final String MEM_COLOR_EDIT_ITALIC = "memoryColorEdit.italic"; //$NON-NLS-1$
	public static final String MEM_COLOR_EDIT_BOLD = "memoryColorEdit.bold"; //$NON-NLS-1$
	public static final String MEM_COLOR_EDIT_BOX = "memoryColorEdit.box"; //$NON-NLS-1$

	public static final String MEM_USE_GLOBAL_BACKGROUND = "memUseGlobalBackground"; //$NON-NLS-1$

	public static final String MEM_COLOR_BACKGROUND = "memoryColorBackground"; //$NON-NLS-1$

	public static final String MEM_COLOR_TEXT = "memoryColorText"; //$NON-NLS-1$

	public static final String MEM_USE_GLOBAL_SELECTION = "memUseGlobalSelection"; //$NON-NLS-1$

	public static final String MEM_COLOR_SELECTION = "memoryColorSelection"; //$NON-NLS-1$

	public static final String MEM_USE_GLOBAL_TEXT = "memUseGlobalText"; //$NON-NLS-1$

	public static final String MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS = "memoryColorScaleTextAlternate"; //$NON-NLS-1$

	public static final String MEM_EDIT_BUFFER_SAVE = "memoryEditBufferSave"; //$NON-NLS-1$

	public static final String MEM_EDIT_BUFFER_SAVE_ON_ENTER_ONLY = "saveOnEnterCancelOnFocusLost"; //$NON-NLS-1$

	public static final String MEM_EDIT_BUFFER_SAVE_ON_ENTER_OR_FOCUS_LOST = "saveOnEnterOrFocusLost"; //$NON-NLS-1$

	public static final String MEM_HISTORY_TRAILS_COUNT = "memoryHistoryTrailsCount"; //$NON-NLS-1$

	public static final String MEM_DEFAULT_COPY_ACTION = "memoryDefaultCopyAction"; //$NON-NLS-1$

	// support for memory space - specific coloring
	/**
	 * @since 1.4
	 */
	public static final String MEM_KNOWN_MEMORY_SPACE_ID_LIST_CSV = "memorySpaceIdList"; //$NON-NLS-1$

	/**
	 * @since 1.4
	 */
	public static final String MEM_MEMORY_SPACE_ID_PREFIX = MEM_COLOR_BACKGROUND + "MemorySpace-"; //$NON-NLS-1$

	/**
	 * @since 1.4
	 */
	public static final String MEM_CROSS_REFERENCE_INFO = "memCrossReferenceInfo"; //$NON-NLS-1$
}
