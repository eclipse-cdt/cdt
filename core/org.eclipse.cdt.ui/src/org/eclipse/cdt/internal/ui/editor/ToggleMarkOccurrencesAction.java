/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;


/**
 * A toolbar action which toggles the {@linkplain org.eclipse.cdt.ui.PreferenceConstants#EDITOR_MARK_OCCURRENCES mark occurrences preference}.
 *
 * @since 5.0
 */
public class ToggleMarkOccurrencesAction extends TextEditorAction implements IPropertyChangeListener {

	private IPreferenceStore fStore;

	/**
	 * Constructs and updates the action.
	 */
	public ToggleMarkOccurrencesAction() {
		super(ConstructedCEditorMessages.getResourceBundle(), "ToggleMarkOccurrencesAction.", null, IAction.AS_CHECK_BOX); //$NON-NLS-1$
		CPluginImages.setToolImageDescriptors(this, "mark_occurrences.gif"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.TOGGLE_MARK_OCCURRENCES_ACTION);
		update();
	}

	/*
	 * @see IAction#actionPerformed
	 */
	@Override
	public void run() {
		fStore.setValue(PreferenceConstants.EDITOR_MARK_OCCURRENCES, isChecked());
	}

	/*
	 * @see TextEditorAction#update
	 */
	@Override
	public void update() {
		ITextEditor editor= getTextEditor();

		boolean checked= false;
		if (editor instanceof CEditor)
			checked= ((CEditor)editor).isMarkingOccurrences();

		setChecked(checked);
		setEnabled(editor != null);
	}

	/*
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	@Override
	public void setEditor(ITextEditor editor) {

		super.setEditor(editor);

		if (editor != null) {

			if (fStore == null) {
				fStore= CUIPlugin.getDefault().getPreferenceStore();
				fStore.addPropertyChangeListener(this);
			}

		} else if (fStore != null) {
			fStore.removePropertyChangeListener(this);
			fStore= null;
		}

		update();
	}

	/*
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.EDITOR_MARK_OCCURRENCES))
			setChecked(Boolean.valueOf(event.getNewValue().toString()).booleanValue());
	}
}
