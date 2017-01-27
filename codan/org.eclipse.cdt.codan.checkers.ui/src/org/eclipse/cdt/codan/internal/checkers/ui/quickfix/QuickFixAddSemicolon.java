/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTNodeSearch;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

@SuppressWarnings("restriction")
public class QuickFixAddSemicolon extends AbstractAstRewriteQuickFix {
	
	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixAddSemicolon_add_semicolon;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		IASTTranslationUnit ast;
		try {
			ITranslationUnit tu = getTranslationUnitViaEditor(marker);
			ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
			return;
		}
		IASTNode astNode;
		if (isCodanProblem())
			return;
		astNode = getAstNodeFromProblemArgument(marker, ast, 0);
		if (astNode == null) {
			return;
		}
		// semicolon asked for before a node...find previous node and
		// add semicolon after it instead
		IASTNode left = new ASTNodeSearch(astNode).findLeftSibling();
		IDocument document = getDocument();
		IASTFileLocation location = left.getFileLocation();
		try {
			// replace the character after the left node
			document.replace(location.getNodeOffset() + 1, 0, ";"); //$NON-NLS-1$
		} catch (BadLocationException e1) {
			return;
		}

		try {
			marker.delete();
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	/**
	 * @param marker
	 * @param ast
	 * @param argumentIndex TODO
	 * @return
	 * @throws BadLocationException
	 */
	public IASTNode getAstNodeFromProblemArgument(IMarker marker, IASTTranslationUnit ast, int argumentIndex) {
		IASTNode astNode = null;
		int pos = getOffset(marker, getDocument());
		String name = null;
		try {
			name = getProblemArgument(marker, argumentIndex);
		} catch (Exception e) {
			return null;
		}
		if (name == null)
			return null;
		FindReplaceDocumentAdapter dad = new FindReplaceDocumentAdapter(getDocument());
		IRegion region;
		try {
			region = dad.find(pos, name,
			/* forwardSearch */true, /* caseSensitive */true,
			/* wholeWord */true, /* regExSearch */false);
		} catch (BadLocationException e) {
			return null;
		}
		astNode = getASTNodeFromPositions(ast, region.getOffset(), region.getLength());
		return astNode;
	}
	/**
	 * @param ast
	 * @param charStart
	 * @param length
	 * @return
	 */
	protected IASTNode getASTNodeFromPositions(IASTTranslationUnit ast, final int charStart, final int length) {
		IASTNode node = ast.getNodeSelector(null).findEnclosingNode(charStart, length);
		return node;
	}
	
	@Override
	public boolean isApplicable(IMarker marker) {
		return true; // gcc problem that matched the pattern
	}
}
