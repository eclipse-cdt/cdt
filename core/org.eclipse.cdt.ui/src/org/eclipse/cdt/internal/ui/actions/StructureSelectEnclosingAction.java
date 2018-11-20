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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.internal.core.model.ext.SourceRange;
import org.eclipse.cdt.internal.ui.editor.SelectionHistory;
import org.eclipse.ui.texteditor.ITextEditor;

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
	 * Package visibility to serve as a fallback for other expansions.
	 */
	static ISourceRange expandToEnclosing(IASTTranslationUnit ast, SourceRange current) {
		final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
		IASTNode node = nodeSelector.findStrictlyEnclosingNode(current.getStartPos(), current.getLength());
		if (node == null)
			return null;

		if (node.getPropertyInParent() == ICPPASTTemplateDeclaration.OWNED_DECLARATION)
			node = node.getParent();

		final IASTFileLocation fileLocation = node.getFileLocation();
		return new SourceRange(fileLocation.getNodeOffset(), fileLocation.getNodeLength());
	}
}
