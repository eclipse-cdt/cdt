/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import org.eclipse.cdt.internal.ui.editor.TogglePresentationAction;

public class ASMEditorActionContributor extends TextEditorActionContributor {

	private TogglePresentationAction fTogglePresentation;

	/**
	 * Default constructor is mandatory (executable extension).
	 */
	public ASMEditorActionContributor() {
		fTogglePresentation= new TogglePresentationAction();
		fTogglePresentation.setActionDefinitionId(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY);
	}
	
	/*
	 * @see org.eclipse.ui.editors.text.TextEditorActionContributor#init(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void init(IActionBars bars) {
		super.init(bars);
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY, fTogglePresentation);
	}
	
	/*
	 * @see org.eclipse.ui.editors.text.TextEditorActionContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		internalSetActiveEditor(part);
	}
	
	private void internalSetActiveEditor(IEditorPart part) {
		ITextEditor textEditor= null;
		if (part instanceof ITextEditor)
			textEditor= (ITextEditor) part;
		
		fTogglePresentation.setEditor(textEditor);
	}
	
	/*
	 * @see org.eclipse.ui.editors.text.TextEditorActionContributor#dispose()
	 */
	@Override
	public void dispose() {
		internalSetActiveEditor(null);
		super.dispose();
	}
}
