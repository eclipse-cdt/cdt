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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.Assert;

import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;


public class GotoMatchingBracketAction extends Action {

	public final static String GOTO_MATCHING_BRACKET= "GotoMatchingBracket"; //$NON-NLS-1$
	
	private final CEditor fEditor;
	
	public GotoMatchingBracketAction(CEditor editor) {
		super(CEditorMessages.getString("GotoMatchingBracket.label")); //$NON-NLS-1$
		Assert.isNotNull(editor);
		fEditor= editor;
		setEnabled(true);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.GOTO_MATCHING_BRACKET_ACTION);
	}
	
	public void run() {
		fEditor.gotoMatchingBracket();
	}
}
