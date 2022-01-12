/*******************************************************************************
 * Copyright (c) 2006, 2010, 2012 Wind River Systems, Inc. and others.
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
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.floatingpoint;

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
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class FPRenderingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public FPRenderingPreferencePage() {
		super(GRID);
		setPreferenceStore(FPRenderingPlugin.getDefault().getPreferenceStore());
		setDescription("Floating Point Memory Rendering"); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				FPRenderingPlugin.getUniqueIdentifier() + ".FPRenderingPreferencePage_context"); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(FPRenderingPreferenceConstants.MEM_USE_GLOBAL_TEXT, "Use Global Te&xt Color", //$NON-NLS-1$
				getFieldEditorParent()));
		addField(new ColorFieldEditor(FPRenderingPreferenceConstants.MEM_COLOR_TEXT, "&Text Color:", //$NON-NLS-1$
				getFieldEditorParent()));
		addField(new ScaleFieldEditor(FPRenderingPreferenceConstants.MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS,
				"Brighten Alternate Cells", getFieldEditorParent(), 0, 8, 1, 1)); //$NON-NLS-1$
		addField(new BooleanFieldEditor(FPRenderingPreferenceConstants.MEM_USE_GLOBAL_BACKGROUND,
				"Use Global B&ackground Color", getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(FPRenderingPreferenceConstants.MEM_COLOR_BACKGROUND, "&Background Color:", //$NON-NLS-1$
				getFieldEditorParent()));
		addField(new ColorFieldEditor(FPRenderingPreferenceConstants.MEM_COLOR_CHANGED, "&Changed Color:", //$NON-NLS-1$
				getFieldEditorParent()));
		addField(new ColorFieldEditor(FPRenderingPreferenceConstants.MEM_COLOR_EDIT, "&Edit Color:", //$NON-NLS-1$
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(FPRenderingPreferenceConstants.MEM_USE_GLOBAL_SELECTION,
				"Use Global Se&lection Color", getFieldEditorParent())); //$NON-NLS-1$
		addField(new ColorFieldEditor(FPRenderingPreferenceConstants.MEM_COLOR_SELECTION, "&Selection Color:", //$NON-NLS-1$
				getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(FPRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE, "Edit Buffer", 1, //$NON-NLS-1$
				new String[][] { { "Save on E&nter, Cancel on Focus Lost", "saveOnEnterCancelOnFocusLost" }, //$NON-NLS-1$ //$NON-NLS-2$
						{ "Save on Enter or Focus L&ost", "saveOnEnterOrFocusLost" } }, //$NON-NLS-1$//$NON-NLS-2$
				getFieldEditorParent()));
		addField(new ScaleFieldEditor(FPRenderingPreferenceConstants.MEM_HISTORY_TRAILS_COUNT, "History &Trail Levels", //$NON-NLS-1$
				getFieldEditorParent(), 1, 10, 1, 1));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
