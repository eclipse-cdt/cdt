/*******************************************************************************
 * Copyright (c) 2010, 2012 Tomasz Wesolowski
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
 *     Marc-Andre Laperle
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.util.HashMap;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.CxxAstUtils.NameFinderVisitor;
import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.CompositeChange;

public class QuickFixCreateParameter extends AbstractAstRewriteQuickFix {
	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixCreateParameter_create_parameter;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		CompositeChange c = new CompositeChange(QuickFixMessages.QuickFixCreateParameter_create_parameter);
		try {
			ITranslationUnit baseTU = getTranslationUnitViaEditor(marker);
			IASTTranslationUnit baseAST = baseTU.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IASTName astName = getASTNameFromMarker(marker, baseAST);
			if (astName == null) {
				return;
			}
			IASTDeclaration declaration = CxxAstUtils.createDeclaration(astName, baseAST.getASTNodeFactory(), index);
			// We'll need a FunctionParameterDeclaration later
			final IASTDeclSpecifier finalDeclSpec = (IASTDeclSpecifier) declaration.getChildren()[0];
			final IASTDeclarator finalDeclarator = (IASTDeclarator) declaration.getChildren()[1];
			IASTFunctionDefinition function = CxxAstUtils.getEnclosingFunction(astName);
			if (function == null) {
				return;
			}
			// Find the function declarations
			NameFinderVisitor nameFinderVisitor = new NameFinderVisitor();
			function.accept(nameFinderVisitor);
			IASTName funcName = nameFinderVisitor.name;
			IBinding binding = funcName.resolveBinding();
			IIndexName[] declarations = index.findNames(binding, IIndex.FIND_DECLARATIONS_DEFINITIONS);
			if (declarations.length == 0) {
				return;
			}
			HashMap<ITranslationUnit, IASTTranslationUnit> cachedASTs = new HashMap<>();
			HashMap<ITranslationUnit, ASTRewrite> cachedRewrites = new HashMap<>();
			for (IIndexName iname : declarations) {
				ITranslationUnit declTU = CxxAstUtils.getTranslationUnitFromIndexName(iname);
				if (declTU == null) {
					continue;
				}
				ASTRewrite rewrite;
				IASTTranslationUnit declAST;
				if (!cachedASTs.containsKey(declTU)) {
					declAST = declTU.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
					rewrite = ASTRewrite.create(declAST);
					cachedASTs.put(declTU, declAST);
					cachedRewrites.put(declTU, rewrite);
				} else {
					declAST = cachedASTs.get(declTU);
					rewrite = cachedRewrites.get(declTU);
				}
				IASTFileLocation fileLocation = iname.getFileLocation();
				IASTName declName = declAST.getNodeSelector(null).findEnclosingName(fileLocation.getNodeOffset(),
						fileLocation.getNodeLength());
				if (declName == null) {
					continue;
				}
				INodeFactory factory = declAST.getASTNodeFactory();
				IASTFunctionDeclarator functionDecl;
				{
					IASTNode n = declName;
					while (n instanceof IASTName) {
						n = n.getParent();
					}
					functionDecl = (IASTFunctionDeclarator) n;
				}
				IASTParameterDeclaration newParam = factory.newParameterDeclaration(finalDeclSpec, finalDeclarator);
				rewrite.insertBefore(functionDecl, null, newParam, null);
			}
			for (ASTRewrite rewrite : cachedRewrites.values()) {
				c.add(rewrite.rewriteAST());
			}
			c.perform(new NullProgressMonitor());
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	@Override
	public boolean isApplicable(IMarker marker) {
		String problemArgument = getProblemArgument(marker, 1);
		return problemArgument.contains(":func"); //$NON-NLS-1$
	}
}
