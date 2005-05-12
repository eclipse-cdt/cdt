/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;



public class GotoAnnotationAction extends TextEditorAction {
		
	private boolean fForward;
	
	public GotoAnnotationAction(String prefix, boolean forward) {
		super(CEditorMessages.getResourceBundle(), prefix, null);
		fForward= forward;
		if (forward)
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.GOTO_NEXT_ERROR_ACTION);
		else
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.GOTO_PREVIOUS_ERROR_ACTION);
	}
	
	public void run() {
		CEditor e= (CEditor) getTextEditor();
		e.gotoAnnotation(fForward);
	}
	
	public void setEditor(ITextEditor editor) {
		if (editor instanceof CEditor) 
			super.setEditor(editor);
		update();
	}
	
	public void update() {
		setEnabled(getTextEditor() instanceof CEditor);
	}
}
