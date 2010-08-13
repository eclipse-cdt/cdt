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

public class StructureSelectEnclosingAction extends StructureSelectionAction {

	public static final String PREFIX = "StructureSelectEnclosing."; //$NON-NLS-1$

	public StructureSelectEnclosingAction(ResourceBundle bundle, ITextEditor editor, SelectionHistory history) {
		super(bundle, PREFIX, editor, history);
	}

	@Override
	public ISourceRange doExpand(IASTTranslationUnit ast, SourceRange current) {

		ISourceRange newSourceRange = expandToEnclosing(ast, current);
		if (newSourceRange != null) {
			history.remember(current);
		}
		return newSourceRange;
	}
	
	/**
	 * Made public to serve as fallback for other expansions
	 */
	public static ISourceRange expandToEnclosing(IASTTranslationUnit ast, SourceRange current) {
		IASTNode enclosingNode = ast.getNodeSelector(null).findEnclosingNode(current.getStartPos(),
				current.getLength());

		int newOffset = enclosingNode.getFileLocation().getNodeOffset();
		int newLength = enclosingNode.getFileLocation().getNodeLength();

		// we can have some nested nodes with same position, so traverse until we have a new position.
		while (newOffset == current.getStartPos() && newLength == current.getLength()) {
			IASTNode toBeSelected = enclosingNode.getParent();
			// if we can't traverse further, give up
			if (toBeSelected == null
					|| toBeSelected.getFileLocation().getFileName() != enclosingNode.getFileLocation()
							.getFileName()) {
				return null;
			}
			newOffset = toBeSelected.getFileLocation().getNodeOffset();
			newLength = toBeSelected.getFileLocation().getNodeLength();

			enclosingNode = toBeSelected;
		}

		return new SourceRange(newOffset, newLength);
	}
	

}
