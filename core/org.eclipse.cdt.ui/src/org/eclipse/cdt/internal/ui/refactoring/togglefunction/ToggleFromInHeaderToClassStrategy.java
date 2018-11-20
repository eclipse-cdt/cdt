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

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite.CommentPosition;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.text.edits.TextEditGroup;

public class ToggleFromInHeaderToClassStrategy implements IToggleRefactoringStrategy {
	private TextEditGroup infoText;
	private ToggleRefactoringContext context;

	public ToggleFromInHeaderToClassStrategy(ToggleRefactoringContext context) {
		if (isFreeFunction(context))
			throw new NotSupportedException(
					Messages.ToggleFromInHeaderToClassStrategy_CanNotToggleTemplateFreeFunction);
		this.context = context;
		this.infoText = new TextEditGroup(Messages.EditGroupName);
	}

	private boolean isFreeFunction(ToggleRefactoringContext context) {
		return isNotInsideAClass(context.getDefinition().getDeclarator(), context.getDeclaration());
	}

	boolean isNotInsideAClass(IASTFunctionDeclarator declarator, IASTFunctionDeclarator backup) {
		if (declarator.getName() instanceof ICPPASTQualifiedName) {
			declarator = backup;
		}
		return (ASTQueries.findAncestorWithType(declarator, IASTCompositeTypeSpecifier.class) == null);
	}

	@Override
	public void run(ModificationCollector modifications) {
		ASTRewrite rewriter = removeDefinition(modifications);
		IASTFunctionDefinition newDefinition = getNewDefinition();
		replaceDeclarationWithDefinition(rewriter, newDefinition);

		IASTNode parentTemplateDeclaration = ToggleNodeHelper.getParentTemplateDeclaration(context.getDeclaration());
		if (!(parentTemplateDeclaration instanceof ICPPASTTemplateDeclaration)) {
			restoreLeadingComments(rewriter, newDefinition);
		}
	}

	private void restoreLeadingComments(ASTRewrite rewriter, IASTFunctionDefinition newDefinition) {
		List<IASTComment> comments = rewriter.getComments(context.getDefinition().getParent(), CommentPosition.leading);
		if (comments != null) {
			for (IASTComment comment : comments) {
				rewriter.addComment(newDefinition, comment, CommentPosition.leading);
				rewriter.remove(comment, infoText);
			}
		}
	}

	private ASTRewrite removeDefinition(ModificationCollector modifications) {
		ASTRewrite rewriter = modifications.rewriterForTranslationUnit(context.getDefinitionAST());
		IASTNode parentRemovePoint = ToggleNodeHelper.getParentRemovePoint(context.getDefinition());
		rewriter.remove(parentRemovePoint, infoText);
		return rewriter;
	}

	private IASTFunctionDefinition getNewDefinition() {
		IASTFunctionDefinition newDefinition = ToggleNodeHelper.createInClassDefinition(context.getDeclaration(),
				context.getDefinition(), context.getDefinitionAST());
		newDefinition.setBody(context.getDefinition().getBody().copy(CopyStyle.withLocations));
		if (newDefinition instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTFunctionWithTryBlock newTryFun = (ICPPASTFunctionWithTryBlock) newDefinition;
			ICPPASTFunctionWithTryBlock oldTryFun = (ICPPASTFunctionWithTryBlock) context.getDefinition();
			for (ICPPASTCatchHandler catchH : oldTryFun.getCatchHandlers()) {
				newTryFun.addCatchHandler(catchH.copy(CopyStyle.withLocations));
			}
		}

		IASTNode parent = ASTQueries.findAncestorWithType(context.getDefinition(), ICPPASTCompositeTypeSpecifier.class);
		if (parent != null) {
			newDefinition.setParent(parent);
		} else {
			newDefinition.setParent(context.getDefinitionAST());
		}
		return newDefinition;
	}

	private ASTRewrite replaceDeclarationWithDefinition(ASTRewrite rewriter, IASTFunctionDefinition newDefinition) {
		IASTSimpleDeclaration fullDeclaration = ASTQueries.findAncestorWithType(context.getDeclaration(),
				CPPASTSimpleDeclaration.class);
		ASTRewrite newRewriter = rewriter.replace(fullDeclaration, newDefinition, infoText);
		return newRewriter;
	}
}
