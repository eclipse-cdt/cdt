package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ResourceBundle;


import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;


import org.eclipse.ui.texteditor.TextEditorAction;


public class BuildConsoleAction extends TextEditorAction {


	private int fOperationCode= -1;
	private ITextOperationTarget fOperationTarget;


	public BuildConsoleAction(ResourceBundle bundle, String prefix, TextViewer viewer, int operationCode) {
		super(bundle, prefix, null);
		fOperationCode= operationCode;
		fOperationTarget= viewer.getTextOperationTarget();
		update();
	}
	/**
	 * @see Action
	 */
	public void run() {
		if (fOperationCode != -1 && fOperationTarget != null) {
			fOperationTarget.doOperation(fOperationCode);
		}
	}
	/**
	 * @see TextEditorAction
	 */
	public void update() {
				
		boolean wasEnabled= isEnabled();
		boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));

		if (wasEnabled != isEnabled) {
			setEnabled(isEnabled);
		}
	}
}
