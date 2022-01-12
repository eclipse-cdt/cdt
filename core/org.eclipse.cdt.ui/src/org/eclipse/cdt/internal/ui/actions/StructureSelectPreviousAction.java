/*******************************************************************************
 * Copyright (c) 2010, 2015 Tomasz Wesolowski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.internal.core.model.ext.SourceRange;
import org.eclipse.cdt.internal.ui.editor.SelectionHistory;
import org.eclipse.ui.texteditor.ITextEditor;

public class StructureSelectPreviousAction extends StructureSelectionAction {
	public static final String PREFIX = "StructureSelectPrevious."; //$NON-NLS-1$

	public StructureSelectPreviousAction(ResourceBundle bundle, ITextEditor editor, SelectionHistory history) {
		super(bundle, PREFIX, editor, history);
	}

	@Override
	public ISourceRange doExpand(IASTTranslationUnit ast, SourceRange current) {
		ISourceRange newSourceRange = expandToPrevious(ast, current);
		if (newSourceRange == null) {
			newSourceRange = StructureSelectEnclosingAction.expandToEnclosing(ast, current);
		}
		if (newSourceRange != null) {
			history.remember(current);
		}
		return newSourceRange;
	}

	private ISourceRange expandToPrevious(IASTTranslationUnit ast, SourceRange current) {
		IASTNodeSelector selector = ast.getNodeSelector(null);
		IASTNode enclosingNode = selector.findEnclosingNode(current.getStartPos(), current.getLength());
		if (samePosition(enclosingNode, current)) {
			enclosingNode = enclosingNode.getParent();
		}
		if (enclosingNode == null) {
			return null;
		}

		// Find the first child of enclosingNode containing selection end.

		int selectionStart = current.getStartPos();

		int firstSelectedChildIndex = -1;
		IASTNode[] children = enclosingNode.getChildren();
		for (int i = 0; i < children.length; i++) {
			IASTNode node = children[i];
			if (nodeContains(node, selectionStart)) {
				firstSelectedChildIndex = i;
				break;
			}
		}

		if (firstSelectedChildIndex > 0) {
			IASTNode prevNode = children[firstSelectedChildIndex - 1];
			int endingOffset = current.getStartPos() + current.getLength();
			return new SourceRange(prevNode.getFileLocation().getNodeOffset(),
					endingOffset - prevNode.getFileLocation().getNodeOffset());
		}
		return null;
	}
}
