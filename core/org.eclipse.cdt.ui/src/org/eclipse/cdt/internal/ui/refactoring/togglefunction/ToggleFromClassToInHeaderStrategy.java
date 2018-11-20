/*******************************************************************************
 * Copyright (c) 2011, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Martin Schwab & Thomas Kallenberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.text.edits.TextEditGroup;

public class ToggleFromClassToInHeaderStrategy implements IToggleRefactoringStrategy {
	protected TextEditGroup infoText = new TextEditGroup(Messages.EditGroupName);
	private ToggleRefactoringContext context;

	public ToggleFromClassToInHeaderStrategy(ToggleRefactoringContext context) {
		if (isInClass(context.getDeclaration()) && isInClass(context.getDefinition()))
			throw new NotSupportedException(Messages.ToggleFromClassToInHeaderStrategy_DefAndDecInsideClass);
		this.context = context;
	}

	private boolean isInClass(IASTNode node) {
		return node != null && ASTQueries.findAncestorWithType(node, ICPPASTCompositeTypeSpecifier.class) != null;
	}

	@Override
	public void run(ModificationCollector modifications) {
		IASTNode parentNamespace = getParentNamespace();
		IASTNode newDefinition = getNewDefinition(parentNamespace);
		IASTSimpleDeclaration newDeclaration = getNewDeclaration();
		ASTRewrite rewriter = replaceDefinitionWithDeclaration(modifications, newDeclaration);
		IASTNode insertion_point = getInsertionPoint(parentNamespace);
		rewriter.insertBefore(parentNamespace, insertion_point, newDefinition, infoText);
	}

	private IASTNode getNewDefinition(IASTNode parentNamespace) {
		IASTNode newDefinition = ToggleNodeHelper.getQualifiedNameDefinition(context.getDefinition(),
				context.getDefinitionAST(), parentNamespace);
		((IASTFunctionDefinition) newDefinition)
				.setBody(context.getDefinition().getBody().copy(CopyStyle.withLocations));
		if (newDefinition instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTFunctionWithTryBlock newTryFun = (ICPPASTFunctionWithTryBlock) newDefinition;
			ICPPASTFunctionWithTryBlock oldTryFun = (ICPPASTFunctionWithTryBlock) context.getDefinition();
			for (ICPPASTCatchHandler catchH : oldTryFun.getCatchHandlers()) {
				newTryFun.addCatchHandler(catchH.copy(CopyStyle.withLocations));
			}
		}
		ICPPASTTemplateDeclaration templdecl = ToggleNodeHelper.getTemplateDeclaration(context.getDefinition(),
				(IASTFunctionDefinition) newDefinition);
		if (templdecl != null) {
			newDefinition = templdecl;
		}
		newDefinition.setParent(context.getDefinitionAST());
		return newDefinition;
	}

	private IASTNode getParentNamespace() {
		IASTNode parentNamespace = ASTQueries.findAncestorWithType(context.getDefinition(),
				ICPPASTNamespaceDefinition.class);
		if (parentNamespace == null)
			parentNamespace = context.getDefinitionAST();
		return parentNamespace;
	}

	private IASTNode getInsertionPoint(IASTNode parentNamespace) {
		IASTTranslationUnit unit = parentNamespace.getTranslationUnit();
		IASTNode insertion_point = InsertionPointFinder.findInsertionPoint(unit, unit,
				context.getDefinition().getDeclarator());
		return insertion_point;
	}

	private ASTRewrite replaceDefinitionWithDeclaration(ModificationCollector modifications,
			IASTSimpleDeclaration newDeclaration) {
		ASTRewrite rewriter = modifications.rewriterForTranslationUnit(context.getDefinitionAST());
		rewriter.replace(context.getDefinition(), newDeclaration, infoText);
		return rewriter;
	}

	private IASTSimpleDeclaration getNewDeclaration() {
		INodeFactory factory = context.getDefinitionAST().getASTNodeFactory();
		IASTDeclSpecifier newDeclSpecifier = context.getDefinition().getDeclSpecifier().copy(CopyStyle.withLocations);
		newDeclSpecifier.setInline(false);
		IASTSimpleDeclaration newDeclaration = factory.newSimpleDeclaration(newDeclSpecifier);
		IASTFunctionDeclarator newDeclarator = context.getDefinition().getDeclarator().copy(CopyStyle.withLocations);
		newDeclaration.addDeclarator(newDeclarator);
		newDeclaration.setParent(context.getDefinition().getParent());
		return newDeclaration;
	}
}
