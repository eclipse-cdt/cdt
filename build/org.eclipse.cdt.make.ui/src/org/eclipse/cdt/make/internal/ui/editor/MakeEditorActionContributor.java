/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 */
public class MakeEditorActionContributor extends BasicTextEditorActionContributor {
	private MakeEditorTogglePresentationAction togglePresentationAction;
	private static final String TOGGLE_PRESENTATION = "make_toggle_presentation"; //$NON-NLS-1$

	/**
	 * Constructor for MakeEditorActionContributor.
	 */
	public MakeEditorActionContributor() {
		super();
		togglePresentationAction = new MakeEditorTogglePresentationAction();
	}

	/**
	 * @see org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		ITextEditor textEditor = null;
		if (targetEditor instanceof ITextEditor)
			textEditor = (ITextEditor) targetEditor;

		togglePresentationAction.setEditor(textEditor);
	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#init(IActionBars)
	 */
	public void init(IActionBars bars) {
		super.init(bars);
		bars.setGlobalActionHandler(TOGGLE_PRESENTATION, togglePresentationAction);
	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(IToolBarManager)
	 */
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(togglePresentationAction);
	}

}
