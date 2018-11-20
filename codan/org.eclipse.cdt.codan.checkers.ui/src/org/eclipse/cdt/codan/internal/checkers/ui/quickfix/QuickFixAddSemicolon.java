/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

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
		IASTNode astNode = null;
		if (isCodanProblem(marker))
			return;

		// We need to back up in the file

		// Start by finding the original reported position and line number
		int lineNum = marker.getAttribute(IMarker.LINE_NUMBER, 0) - 1;

		if (lineNum < 1) {
			return;
		}

		IDocument document = getDocument();

		int lineOffset;
		int lineLength;
		try {
			lineOffset = document.getLineOffset(lineNum);
			lineLength = document.getLineLength(lineNum);
		} catch (BadLocationException e2) {
			return;
		}

		// find the position of the reported token
		int pos = getOffset(marker, getDocument());
		String name = null;
		try {
			name = getProblemArgument(marker, 0);
		} catch (Exception e) {
			return;
		}
		if (name == null)
			return;
		FindReplaceDocumentAdapter dad = new FindReplaceDocumentAdapter(getDocument());
		IRegion region;
		try {
			region = dad.find(pos, name, /* forwardSearch */true, /* caseSensitive */true, /* wholeWord */false,
					/* regExSearch */false);
		} catch (BadLocationException e) {
			return;
		}

		if (region == null)
			return;

		// now we have the offset
		int offset = region.getOffset();
		IASTNode prevNode = null;

		// see if there are previous nodes on same line
		if (lineOffset < offset) {
			astNode = getASTFirstContainedNodeFromPosition(ast, lineOffset, lineLength);
			if (astNode != null) {
				IASTFileLocation fileLoc = astNode.getFileLocation();
				if (fileLoc == null)
					return;
				int length = lineLength;
				while (fileLoc.getNodeOffset() < offset) {
					prevNode = astNode;
					astNode = getASTFirstContainedNodeFromPosition(ast,
							fileLoc.getNodeOffset() + fileLoc.getNodeLength(), length);
					fileLoc = astNode.getFileLocation();
					if (fileLoc == null)
						return;
					length -= fileLoc.getNodeLength();
				}
			}
		}

		// if we didn't find the previous node on the same line, go back a line at a time and find last node on line
		while (prevNode == null) {
			lineNum -= 1;
			if (lineNum < 0)
				return; // don't bother once we have reached start of file
			try {
				lineOffset = document.getLineOffset(lineNum);
				lineLength = document.getLineLength(lineNum);
			} catch (BadLocationException e) {
				return;
			}
			int x = lineOffset;
			int leftover = lineLength;
			// get a node at a time from line and keep track of last node found
			while (x < lineOffset + lineLength) {
				astNode = getASTFirstContainedNodeFromPosition(ast, x, leftover);
				if (astNode == null)
					break;
				prevNode = astNode;
				IASTFileLocation fileLoc = astNode.getFileLocation();
				if (fileLoc == null)
					break;
				x += fileLoc.getNodeLength();
				leftover -= fileLoc.getNodeLength();
			}

		}

		IASTFileLocation location = prevNode.getFileLocation();
		if (location == null)
			return;
		int replacementLoc = location.getNodeOffset() + location.getNodeLength();
		// in the case of a Problem statement, it might include \n or \r\n as part
		// of the node, so we must just assume the semi-colon belongs at the end of
		// the line
		if (replacementLoc == offset)
			replacementLoc -= System.lineSeparator().length();
		try {
			// insert the semi-colon
			document.replace(replacementLoc, 0, ";"); //$NON-NLS-1$
		} catch (BadLocationException e1) {
			return;
		}

		try {
			// remove marker now that has been dealt with
			marker.delete();
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	/**
	 * @param ast
	 * @param charStart
	 * @param length
	 * @return
	 */
	private IASTNode getASTFirstContainedNodeFromPosition(IASTTranslationUnit ast, final int charStart,
			final int length) {
		IASTNode node = ast.getNodeSelector(null).findFirstContainedNode(charStart, length);
		return node;
	}

}
