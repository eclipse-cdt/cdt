/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Listener to be informed on text selection changes in an editor (post selection), including the corresponding AST.
 * The AST is shared and must not be modified.
 * Listeners can be registered in a <code>SelectionListenerWithASTManager</code>.
 */
public interface ISelectionListenerWithAST {
	
	/**
	 * Called when a selection has changed. The method is called in a post selection event in an background
	 * thread.
	 * 
	 * @param part The editor part in which the selection change has occurred.
	 * @param selection The new text selection
	 * @param astRoot The AST tree corresponding to the editor's input. This AST is shared and must
	 * not be modified.
	 */
	void selectionChanged(IEditorPart part, ITextSelection selection, IASTTranslationUnit astRoot);
}
