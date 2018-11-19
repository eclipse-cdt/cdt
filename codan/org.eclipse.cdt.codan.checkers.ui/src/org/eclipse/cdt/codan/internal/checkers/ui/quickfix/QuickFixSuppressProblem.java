/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Felix Morgner <fmorgner@hsr.ch> - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.core.param.SuppressionCommentProblemPreference;
import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.codan.ui.ICodanMarkerResolutionExtension;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.InsertEdit;

public class QuickFixSuppressProblem extends AbstractAstRewriteQuickFix implements ICodanMarkerResolutionExtension {
	private static final String COMMENT_TEMPLATE = " // %s"; //$NON-NLS-1$
	private String problemName;

	@Override
	public String getLabel() {
		return String.format(QuickFixMessages.QuickFixSuppressProblem_Label, problemName);
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		String supressionComment = getSupressionComment(getProblem(marker));
		if (supressionComment == null) {
			return;
		}
		try {
			int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
			IRegion lineInformation = getDocument().getLineInformation(line - 1);
			int offset = lineInformation.getOffset() + lineInformation.getLength();
			String commentString = String.format(COMMENT_TEMPLATE, supressionComment);
			InsertEdit edit = new InsertEdit(offset, commentString);
			edit.apply(getDocument());
		} catch (BadLocationException e) {
			CheckersUiActivator.log(e);
		}
	}

	private String getSupressionComment(IProblem problem) {
		IProblemPreference preference = problem.getPreference();
		if (preference instanceof RootProblemPreference) {
			RootProblemPreference root = (RootProblemPreference) preference;
			Object value = root.getChildValue(SuppressionCommentProblemPreference.KEY);
			if (value instanceof String && ((String) value).trim().length() > 0) {
				return (String) value;
			}
		}
		return null;
	}

	@Override
	public void prepareFor(IMarker marker) {
		IProblem problem = getProblem(marker);
		if (problem != null) {
			problemName = problem.getName();
		}
	}

	@Override
	public boolean isApplicable(IMarker marker) {
		return getProblem(marker) != null;
	}

}