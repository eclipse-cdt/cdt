/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ISourceRange;

import org.eclipse.cdt.internal.core.model.ext.SourceRange;

import org.eclipse.cdt.internal.ui.editor.SelectionHistory;

public class StructureSelectNextAction extends StructureSelectionAction {

	public static final String PREFIX = "StructureSelectNext."; //$NON-NLS-1$

	public StructureSelectNextAction(ResourceBundle bundle, ITextEditor editor,
			SelectionHistory history) {
		super(bundle, PREFIX, editor, history);
	}

	@Override
	public ISourceRange doExpand(IASTTranslationUnit ast, SourceRange current) {
		
		ISourceRange newSourceRange = expandToNext(ast, current);
		if (newSourceRange == null) {
			newSourceRange = StructureSelectEnclosingAction.expandToEnclosing(ast, current); 
		}
		if (newSourceRange != null) {
			history.remember(current);
		}			
		return newSourceRange;
	}

	private ISourceRange expandToNext(IASTTranslationUnit ast, SourceRange current) {
		
		IASTNode enclosingNode = ast.getNodeSelector(null).findEnclosingNode(current.getStartPos(),
				current.getLength());
		if (samePosition(enclosingNode, current)) {
			enclosingNode = enclosingNode.getParent();
		}
		if (enclosingNode == null) {
			return null;
		}
		
		// find the last child of enclosingNode containing selection end
		
		int selectionEnd = current.getStartPos()+current.getLength();
		
		int lastSelectedChildIndex = -1;
		IASTNode[] children = enclosingNode.getChildren();
		for (int i = 0; i < children.length; i++) {
			IASTNode node = children[i];
			if (nodeContains(node, selectionEnd)) {
				lastSelectedChildIndex = i;
				break;
			}
		}
		
		if (lastSelectedChildIndex != -1 && lastSelectedChildIndex+1 < children.length) {
			IASTNode nextNode = children[lastSelectedChildIndex+1];
			int endingOffset = nextNode.getFileLocation().getNodeOffset()+nextNode.getFileLocation().getNodeLength();
			return new SourceRange(current.getStartPos(),endingOffset-current.getStartPos());
		}
		return null;
	}

}
