/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.ResourceBundle;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * Editor action to toggle between source and header files.
 *
 * @since 4.0
 */
public class ToggleSourceAndHeaderAction extends TextEditorAction {
	private static ITranslationUnit fgLastPartnerUnit;
	private static ITranslationUnit fgLastSourceUnit;

	/**
	 * Create a toggle source/header action for the given editor.
	 *
	 * @param bundle  the resource bundle to take the label, tooltip and description from.
	 * @param prefix  the prefix to be prepended to the resource bundle keys
	 * @param editor  the text editor this action is associated with
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public ToggleSourceAndHeaderAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	@Override
	public void run() {
		IWorkingCopy currentUnit = getWorkingCopy();
		if (currentUnit == null) {
			return;
		}
		ITranslationUnit partnerUnit = computePartnerFile(currentUnit);
		if (partnerUnit != null) {
			fgLastSourceUnit = currentUnit.getOriginalElement();
			fgLastPartnerUnit = partnerUnit;
			try {
				EditorUtility.openInEditor(partnerUnit);
			} catch (PartInitException exc) {
				CUIPlugin.log(exc.getStatus());
			} catch (CModelException exc) {
				CUIPlugin.log(exc.getStatus());
			}
		}
	}

	private IWorkingCopy getWorkingCopy() {
		IEditorPart editor = getTextEditor();
		if (editor == null) {
			return null;
		}
		IEditorInput input = editor.getEditorInput();
		IWorkingCopyManager manager = CUIPlugin.getDefault().getWorkingCopyManager();
		return manager.getWorkingCopy(input);
	}

	@Override
	public void update() {
		setEnabled(getWorkingCopy() != null);
	}

	/**
	 * Compute the corresponding translation unit for the given unit.
	 *
	 * @param tUnit  the current source/header translation unit
	 * @return the partner translation unit
	 */
	private ITranslationUnit computePartnerFile(ITranslationUnit tUnit) {
		// Try shortcut for fast toggling.
		if (fgLastPartnerUnit != null) {
			final ITranslationUnit originalUnit;
			if (tUnit instanceof IWorkingCopy) {
				originalUnit = ((IWorkingCopy) tUnit).getOriginalElement();
			} else {
				originalUnit = tUnit;
			}
			if (originalUnit.getTranslationUnit().equals(fgLastPartnerUnit)) {
				if (fgLastSourceUnit.exists()) {
					// toggle back
					return fgLastSourceUnit;
				}
			}
		}

		// Search partner file based on filename/extension.
		return SourceHeaderPartnerFinder.getPartnerTranslationUnit(tUnit);
	}
}
