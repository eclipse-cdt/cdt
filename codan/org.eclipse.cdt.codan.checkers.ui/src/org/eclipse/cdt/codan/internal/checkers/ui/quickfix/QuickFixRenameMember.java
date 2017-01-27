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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

public class QuickFixRenameMember extends AbstractAstRewriteQuickFix {
	
	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixRenameMember_rename_member;
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
		IASTName astName;
		if (isCodanProblem()) {
			astName = getASTNameFromMarker(marker, ast);
		} else {
			astName = getAstNameFromProblemArgument(marker, ast, 1);
		}
		if (astName == null) {
			return;
		}
		ASTRewrite r = ASTRewrite.create(ast);
		INodeFactory factory = ast.getASTNodeFactory();
		
		String[] args = getProblemArguments(marker);
		if (args == null || args.length < 3)
			return;
		IASTName newName = factory.newName(args[2]);
		r.replace(astName, newName, null);
		Change c = r.rewriteAST();
		try {
			c.perform(new NullProgressMonitor());
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
			return;
		}
		try {
			marker.delete();
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	private String[] getProblemArguments(IMarker marker) {
		String attrs = marker.getAttribute("args", ""); //$NON-NLS-1$ //$NON-NLS-2$
		Properties prop = new Properties();
		ByteArrayInputStream bin = new ByteArrayInputStream(attrs.getBytes());
		try {
			prop.load(bin);
		} catch (IOException e) {
			// not happening
		}
		String len = prop.getProperty("len", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		int length = Integer.valueOf(len);
		String args[] = new String[length];
		for (int i = 0; i < length; i++) {
			args[i] = prop.getProperty("a" + i); //$NON-NLS-1$
		}
		return args;
	}
	
	@Override
	public boolean isApplicable(IMarker marker) {
		if (isCodanProblem()) {
			String problemArgument = getProblemArgument(marker, 1);
			return problemArgument.contains(":func"); //$NON-NLS-1$
		}
		return true; // gcc problem that matched the pattern
	}
}
