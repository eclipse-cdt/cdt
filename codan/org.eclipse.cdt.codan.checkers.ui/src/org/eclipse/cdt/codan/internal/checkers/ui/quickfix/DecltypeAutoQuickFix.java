/*******************************************************************************
* Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

public class DecltypeAutoQuickFix extends AbstractAstRewriteQuickFix {
	
	@Override
	public String getLabel() {
		return QuickFixMessages.DecltypeAutoQuickFix_remove_specifiers;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
        try {
			IASTTranslationUnit ast = getTranslationUnitViaEditor(marker).getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
			int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
			int length = marker.getAttribute(IMarker.CHAR_END, -1) - charStart;
			getASTNameFromMarker(marker, ast);
			ICPPASTSimpleDeclSpecifier simpleDecl = (ICPPASTSimpleDeclSpecifier) ast.getNodeSelector(null).findNode(charStart, length);
			ICPPASTSimpleDeclSpecifier simpleDeclCopy = simpleDecl.copy(CopyStyle.withLocations);
			simpleDeclCopy.setConst(false);
			simpleDeclCopy.setVolatile(false);
			ASTRewrite rewrite = ASTRewrite.create(ast);
			rewrite.replace(simpleDecl, simpleDeclCopy, null);
			Change change = rewrite.rewriteAST();
			change.perform(new NullProgressMonitor());
			marker.delete();
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}
}
