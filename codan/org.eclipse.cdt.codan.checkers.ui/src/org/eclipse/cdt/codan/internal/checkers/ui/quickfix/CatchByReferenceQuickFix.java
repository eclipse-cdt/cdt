/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.util.Optional;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.internal.checkers.ui.Messages;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Quick fix for catch by value
 */
public class CatchByReferenceQuickFix extends AbstractAstRewriteQuickFix {
	@Override
	public String getLabel() {
		return Messages.CatchByReferenceQuickFix_Message;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		IASTSimpleDeclaration declaration = getDeclaration(index, marker);
		if (declaration == null) {
			CheckersUiActivator.log("Could not find declaration"); //$NON-NLS-1$
			return;
		}

		IASTTranslationUnit ast = declaration.getTranslationUnit();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		getNewDeclSpecifier(declaration).ifPresent(ds -> rewrite.replace(declaration.getDeclSpecifier(), ds, null));
		rewrite.replace(declaration.getDeclarators()[0], getNewDeclarator(declaration), null);

		try {
			rewrite.rewriteAST().perform(new NullProgressMonitor());
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	/**
	 * Calculate the new IASTDeclSpecifier for the changed declaration
	 * <p>
	 * Subclasses can override this method to provide custom behavior.
	 * </p>
	 *
	 * @param declaration The original declaration
	 * @return A, possibly empty, {@link Optional} containing the new
	 *         IASTDeclSpecifier
	 */
	protected Optional<IASTDeclSpecifier> getNewDeclSpecifier(IASTSimpleDeclaration declaration) {
		return Optional.empty();
	}

	private static IASTDeclarator getNewDeclarator(IASTSimpleDeclaration declaration) {
		ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		ICPPASTReferenceOperator reference = nodeFactory.newReferenceOperator(false);
		IASTDeclarator declarator = declaration.getDeclarators()[0];
		IASTDeclarator replacement = declarator.copy(CopyStyle.withLocations);
		replacement.addPointerOperator(reference);
		return replacement;
	}

	private IASTSimpleDeclaration getDeclaration(IIndex index, IMarker marker) {
		try {
			ITranslationUnit tu = getTranslationUnitViaEditor(marker);
			IASTTranslationUnit ast = tu.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
			int start = marker.getAttribute(IMarker.CHAR_START, -1);
			int end = marker.getAttribute(IMarker.CHAR_END, -1);
			if (start != -1 && end != -1) {
				IASTNode node = ast.getNodeSelector(null).findNode(start, end - start);
				while (node != null && !(node instanceof IASTSimpleDeclaration)) {
					node = node.getParent();
				}
				return (IASTSimpleDeclaration) node;
			}
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
		return null;
	}

}
