/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * A toolbar action which toggles the presentation model of the
 * connected text editor. The editor shows either the highlight range
 * only or always the whole document.
 */
public class TogglePresentationAction extends TextEditorAction implements IPropertyChangeListener {

	private IPreferenceStore fStore;

	/**
	 * Constructs and updates the action.
	 */
	public TogglePresentationAction() {
		super(ConstructedCEditorMessages.getResourceBundle(), "TogglePresentation.", null); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_SEGMENT_EDIT);
		setActionDefinitionId(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.TOGGLE_PRESENTATION_ACTION);
		update();
	}

	/*
	 * @see IAction#actionPerformed
	 */
	@Override
	public void run() {

		ITextEditor editor = getTextEditor();
		if (editor == null)
			return;

		IRegion remembered = editor.getHighlightRange();
		editor.resetHighlightRange();

		boolean showAll = !editor.showsHighlightRangeOnly();
		setChecked(showAll);

		editor.showHighlightRangeOnly(showAll);
		if (remembered != null)
			editor.setHighlightRange(remembered.getOffset(), remembered.getLength(), true);

		fStore.removePropertyChangeListener(this);
		fStore.setValue(PreferenceConstants.EDITOR_SHOW_SEGMENTS, showAll);
		fStore.addPropertyChangeListener(this);
	}

	/*
	 * @see TextEditorAction#update
	 */
	@Override
	public void update() {
		ITextEditor editor = getTextEditor();
		boolean checked = (editor != null && editor.showsHighlightRangeOnly());
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
				fStore = CUIPlugin.getDefault().getPreferenceStore();
				fStore.addPropertyChangeListener(this);
			}
			synchronizeWithPreference(editor);

		} else if (fStore != null) {
			fStore.removePropertyChangeListener(this);
			fStore = null;
		}

		update();
	}

	/**
	 * Synchronizes the appearance of the editor with what the preference store tells him.
	 */
	private void synchronizeWithPreference(ITextEditor editor) {

		if (editor == null)
			return;

		boolean showSegments = fStore.getBoolean(PreferenceConstants.EDITOR_SHOW_SEGMENTS);
		setChecked(showSegments);

		if (editor.showsHighlightRangeOnly() != showSegments) {
			IRegion remembered = editor.getHighlightRange();
			editor.resetHighlightRange();
			editor.showHighlightRangeOnly(showSegments);
			if (remembered != null)
				editor.setHighlightRange(remembered.getOffset(), remembered.getLength(), true);
		}
	}

	/*
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.EDITOR_SHOW_SEGMENTS))
			synchronizeWithPreference(getTextEditor());
	}
}
