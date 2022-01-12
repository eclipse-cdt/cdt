/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems, Inc. and others.
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
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class TraditionalRenderingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public TraditionalRenderingPreferencePage() {
		super(GRID);
		setPreferenceStore(TraditionalRenderingPlugin.getDefault().getPreferenceStore());
		setDescription(TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_description")); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				TraditionalRenderingPlugin.getUniqueIdentifier() + ".TraditionalRenderingPreferencePage_context"); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_TEXT,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_UseGlobalTextColor"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new ColorFieldEditor(TraditionalRenderingPreferenceConstants.MEM_COLOR_TEXT,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_TextColor"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new ScaleFieldEditor(TraditionalRenderingPreferenceConstants.MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_BrightenAlternateCells"), //$NON-NLS-1$
				getFieldEditorParent(), 0, 8, 1, 1));

		addField(new BooleanFieldEditor(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_BACKGROUND,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_UseGlobalBackgroundColor"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new ColorFieldEditor(TraditionalRenderingPreferenceConstants.MEM_COLOR_BACKGROUND,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_BackgroundColor"), //$NON-NLS-1$
				getFieldEditorParent()));

		// are there known memory spaces? If so make their background color configurable
		IMemorySpacePreferencesHelper util = TraditionalMemoryRenderingFactory.getMemorySpacesPreferencesHelper();

		Map<String, String> memSpacesLabels = util.getMemorySpaceLabels();
		for (String key : memSpacesLabels.keySet()) {
			addField(new ColorFieldEditor(key, memSpacesLabels.get(key), getFieldEditorParent()));
		}

		addField(new ColorAndEffectFieldEditor(TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED,
				TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED_BOLD,
				TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED_ITALIC,
				TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED_BOX,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_ChangedColor"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new ColorAndEffectFieldEditor(TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT,
				TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT_BOLD,
				TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT_ITALIC,
				TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT_BOX,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_EditColor"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_SELECTION,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_UseGlobalSelectionColor"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new ColorFieldEditor(TraditionalRenderingPreferenceConstants.MEM_COLOR_SELECTION,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_SelectionColor"), //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(TraditionalRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_EditBuffer"), 1, //$NON-NLS-1$
				new String[][] {
						{ TraditionalRenderingMessages
								.getString("TraditionalRenderingPreferencePage_SaveOnEnterCancelOnFocusLost"), //$NON-NLS-1$
								"saveOnEnterCancelOnFocusLost" }, //$NON-NLS-1$
						{ TraditionalRenderingMessages
								.getString("TraditionalRenderingPreferencePage_SaveOnEnterOrFocusLost"), //$NON-NLS-1$
								"saveOnEnterOrFocusLost" } }, //$NON-NLS-1$
				getFieldEditorParent()));

		addField(new ScaleFieldEditor(TraditionalRenderingPreferenceConstants.MEM_HISTORY_TRAILS_COUNT,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_HistoryTrailLevels"), //$NON-NLS-1$
				getFieldEditorParent(), 1, 10, 1, 1));

		addField(new BooleanFieldEditor(TraditionalRenderingPreferenceConstants.MEM_CROSS_REFERENCE_INFO,
				TraditionalRenderingMessages.getString("TraditionalRenderingPreferencePage_ShowCrossRefInfo"), //$NON-NLS-1$
				getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

}