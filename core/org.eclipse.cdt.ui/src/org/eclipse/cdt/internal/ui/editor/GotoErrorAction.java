package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;


public class GotoErrorAction extends TextEditorAction {
	
	
	private boolean fForward;
	
	
	public GotoErrorAction(String prefix, boolean forward) {
		super(CEditorMessages.getResourceBundle(), prefix, null);
		fForward= forward;
	}
	/**
	 * @see Action#run()
	 */
	public void run() {
		CEditor e= (CEditor) getTextEditor();
		e.gotoError(fForward);
	}
	/**
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		if (editor instanceof CEditor) 
			super.setEditor(editor);
	}
	/**
	 * @see TextEditorAction#update()
	 */
	public void update() {
		setEnabled(true);
	}
}
