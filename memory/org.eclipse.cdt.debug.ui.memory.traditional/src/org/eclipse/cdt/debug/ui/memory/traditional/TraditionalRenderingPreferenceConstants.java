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

	public static final String MEM_COLOR_CHANGED = "memoryColorChanged";
	public static final String MEM_COLOR_CHANGED_ITALIC = "memoryColorChanged.italic";
	public static final String MEM_COLOR_CHANGED_BOLD = "memoryColorChanged.bold";
	public static final String MEM_COLOR_CHANGED_BOX = "memoryColorChanged.box";

	public static final String MEM_COLOR_EDIT = "memoryColorEdit";
	public static final String MEM_COLOR_EDIT_ITALIC = "memoryColorEdit.italic";
	public static final String MEM_COLOR_EDIT_BOLD = "memoryColorEdit.bold";
	public static final String MEM_COLOR_EDIT_BOX = "memoryColorEdit.box";

	public static final String MEM_USE_GLOBAL_BACKGROUND = "memUseGlobalBackground";

	public static final String MEM_COLOR_BACKGROUND = "memoryColorBackground";

	public static final String MEM_COLOR_TEXT = "memoryColorText";

	public static final String MEM_USE_GLOBAL_SELECTION = "memUseGlobalSelection";

	public static final String MEM_COLOR_SELECTION = "memoryColorSelection";

	public static final String MEM_USE_GLOBAL_TEXT = "memUseGlobalText";

	public static final String MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS = "memoryColorScaleTextAlternate";

	public static final String MEM_EDIT_BUFFER_SAVE = "memoryEditBufferSave";

	public static final String MEM_EDIT_BUFFER_SAVE_ON_ENTER_ONLY = "saveOnEnterCancelOnFocusLost";

	public static final String MEM_EDIT_BUFFER_SAVE_ON_ENTER_OR_FOCUS_LOST = "saveOnEnterOrFocusLost";

	public static final String MEM_HISTORY_TRAILS_COUNT = "memoryHistoryTrailsCount";

	public static final String MEM_DEFAULT_COPY_ACTION = "memoryDefaultCopyAction";

	// support for memory space - specific coloring
	/**
	 * @since 1.4
	 */
	public static final String MEM_KNOWN_MEMORY_SPACE_ID_LIST_CSV = "memorySpaceIdList";

	/**
	 * @since 1.4
	 */
	public static final String MEM_MEMORY_SPACE_ID_PREFIX = MEM_COLOR_BACKGROUND + "MemorySpace-";

	/**
	 * @since 1.4
	 */
	public static final String MEM_CROSS_REFERENCE_INFO = "memCrossReferenceInfo";
}
