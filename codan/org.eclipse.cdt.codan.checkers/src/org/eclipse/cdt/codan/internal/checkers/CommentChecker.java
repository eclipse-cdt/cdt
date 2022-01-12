/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software System and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Elena Laskavaia (QNX Software System) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Checker for some specific code style violations in comments
 */
public class CommentChecker extends AbstractIndexAstChecker {
	public static final String COMMENT_NO_LINE = "org.eclipse.cdt.codan.checkers.nolinecomment"; //$NON-NLS-1$
	public static final String COMMENT_NO_START = "org.eclipse.cdt.codan.checkers.nocommentinside"; //$NON-NLS-1$
	private boolean conly;

	@Override
	public void processAst(IASTTranslationUnit ast) {
		IASTComment[] comments = ast.getComments();
		if (comments == null)
			return;
		conly = ast.getLinkage().getLinkageID() == ILinkage.C_LINKAGE_ID;
		if (!conly
				&& shouldProduceProblem(getProblemById(COMMENT_NO_START, getFile()), getFile().getFullPath()) == false)
			return; // c++ file and COMMENT_NO_START is disabled - optimize and bail
		for (IASTComment comment : comments) {
			processComment(comment);
		}
	}

	protected void processComment(IASTComment comment) {
		boolean blockComment = comment.isBlockComment();
		if (blockComment) {
			String commentStr = comment.getRawSignature();
			int pos = commentStr.indexOf("/*", 2); //$NON-NLS-1$
			if (pos >= 0) {
				reportProblem(COMMENT_NO_START, getProblemLocation(comment.getFileLocation(), pos));
			}
		} else {
			if (conly) {
				reportProblem(COMMENT_NO_LINE, comment);
			}
		}
	}

	private IProblemLocation getProblemLocation(IASTFileLocation astLocation, int pos) {
		IProblemLocationFactory locFactory = getRuntime().getProblemLocationFactory();
		int newPosition = astLocation.getNodeOffset() + pos;
		return locFactory.createProblemLocation(getFile(), newPosition, newPosition + 2, -1);
	}
}