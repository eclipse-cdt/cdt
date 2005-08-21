/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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


import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.TextEditor;


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
		setSourceViewerConfiguration(new AsmSourceViewerConfiguration(textTools, this));
		setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());
		// FIXME: Should this editor have a different preference store ?
		// For now we are sharing with the CEditor and any changes will in the
		// setting of the CEditor will be reflected in this editor.
		setPreferenceStore(CUIPlugin.getDefault().getCombinedPreferenceStore());
		setEditorContextMenuId("#ASMEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#ASMEditorRulerContext"); //$NON-NLS-1$
		//setOutlinerContextMenuId("#ASMEditorOutlinerContext"); //$NON-NLS-1$
	}
	
	/*
	 * @see AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 * Pulled in from 2.0
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		// String p= event.getProperty();
		
		boolean affects= false;
		AsmTextTools textTools= CUIPlugin.getDefault().getAsmTextTools();
		affects |= textTools.affectsBehavior(event);
									
		return affects ? affects : super.affectsTextPresentation(event);
	}

}
