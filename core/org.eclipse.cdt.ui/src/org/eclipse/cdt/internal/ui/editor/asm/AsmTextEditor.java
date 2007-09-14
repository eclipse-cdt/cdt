/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;


import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.navigator.ICommonMenuConstants;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.IContextMenuConstants;


/**
 * Assembly text editor
 */
public class AsmTextEditor extends TextEditor {	
	/**
	 * Creates a new text editor.
	 */
	public AsmTextEditor() {
		super();
	}
	/**
	 * Initializes this editor.
	 */
	protected void initializeEditor() {
		AsmTextTools textTools= CUIPlugin.getDefault().getAsmTextTools();
		IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
		setSourceViewerConfiguration(new AsmSourceViewerConfiguration(textTools, store));
		setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());
		// FIXME: Should this editor have a different preference store ?
		// For now we are sharing with the CEditor and any changes in the
		// setting of the CEditor will be reflected in this editor.
		setPreferenceStore(store);
		setEditorContextMenuId("#ASMEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#ASMEditorRulerContext"); //$NON-NLS-1$
		//setOutlinerContextMenuId("#ASMEditorOutlinerContext"); //$NON-NLS-1$
	}
	
	/*
	 * @see AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 * Pulled in from 2.0
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		boolean affects= false;
		AsmTextTools textTools= CUIPlugin.getDefault().getAsmTextTools();
		affects |= textTools.affectsBehavior(event);
									
		return affects ? affects : super.affectsTextPresentation(event);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		// marker for contributions to the top
		menu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
		// separator for debug related actions (similar to ruler context menu)
		menu.add(new Separator(IContextMenuConstants.GROUP_DEBUG));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_DEBUG+".end")); //$NON-NLS-1$

		super.editorContextMenuAboutToShow(menu);
	}
}
