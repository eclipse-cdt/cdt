/*******************************************************************************
 * Copyright (c) 2009, 2013 Andrew Gvozdev and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.internal.checkers.ui.Messages;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;

/**
 * quick fix for assignment in condition
 */
public class QuickFixAssignmentInCondition extends AbstractAstRewriteQuickFix {
	@Override
	public String getLabel() {
		return Messages.QuickFixAssignmentInCondition_Message;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		try {
			IASTTranslationUnit ast = getTranslationUnitViaEditor(marker).getAST(index,
					ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			int markerStart = marker.getAttribute(IMarker.CHAR_START, -1);
			int markerEnd = marker.getAttribute(IMarker.CHAR_END, -1);
			if (markerStart == -1 || markerEnd == -1 || markerStart >= markerEnd)
				return;
			IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
			IASTNode containedNode = nodeSelector.findEnclosingNode(markerStart, markerEnd - markerStart);
			if (containedNode instanceof IASTBinaryExpression) {
				IASTBinaryExpression expr = (IASTBinaryExpression) containedNode;
				IASTNodeLocation[] leftSubexprLocations = expr.getOperand1().getNodeLocations();
				if (leftSubexprLocations.length != 1) // don't handle expressions in macro expansions
					return;
				IASTNodeLocation leftSubexprLocation = leftSubexprLocations[0];
				int leftSubexprEnd = leftSubexprLocation.getNodeOffset() + leftSubexprLocation.getNodeLength();

				// Assignment operator will be following the end of the left subexpression.
				FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(getDocument());
				adapter.find(leftSubexprEnd, "=", ///$NON-NLS-1$
						true, /* forwardSearch */
						false, /* caseSensitive */
						false, /* wholeWord */
						false); /* regExSearch */
				adapter.replace("==", false /* regExReplace */); ///$NON-NLS-1$
			}
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		} catch (BadLocationException e) {
			CheckersUiActivator.log(e);
		}
	}
}
