/*******************************************************************************
 * Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 		Martin Schwab & Thomas Kallenberg - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import java.util.List;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite.CommentPosition;

import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;

import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.CPPASTAllVisitor;

public class ToggleFromImplementationToHeaderOrClassStrategy implements IToggleRefactoringStrategy {

	private ToggleRefactoringContext context;
	private TextEditGroup infoText;
	private IASTTranslationUnit other_tu;
	private ASTLiteralNode includenode;

	public ToggleFromImplementationToHeaderOrClassStrategy(
			ToggleRefactoringContext context) {
		this.context = context;
		this.infoText = new TextEditGroup(Messages.EditGroupName);
	}

	private boolean isFreeFunction(IASTFunctionDefinition definition) {
		return definition.getDeclarator().getName() instanceof ICPPASTQualifiedName;
	}
	
	@Override
	public void run(ModificationCollector modifications) {
		newFileCheck();
		ASTRewrite implast = modifications.rewriterForTranslationUnit(context.getDefinitionUnit());
		List<IASTComment>leadingComments = implast.getComments(context.getDefinition(), CommentPosition.leading);
		removeDefinitionFromImplementation(implast);
		if (includenode != null) {
			implast.insertBefore(context.getDefinitionUnit(), 
					context.getDefinitionUnit().getChildren()[0], includenode, infoText);
		}
		if (context.getDeclarationUnit() != null) {
			addDefinitionToClass(modifications, leadingComments);
		} else {
			addDefinitionToHeader(modifications, leadingComments);
		}
	}

	private void newFileCheck() {
		if (context.getDeclarationUnit() == null) {
			if (isFreeFunction(context.getDefinition())) {
				throw new NotSupportedException(Messages.ToggleFromImplementationToHeaderOrClassStrategy_CanNotToggle);
			}
			other_tu = context.getTUForSiblingFile();
			if (other_tu == null) {
				ToggleFileCreator filecreator = new ToggleFileCreator(context, ".h"); //$NON-NLS-1$
				if (filecreator.askUserForFileCreation(context)) {
					filecreator.createNewFile();
					other_tu = filecreator.loadTranslationUnit();
					includenode = new ASTLiteralNode(filecreator.getIncludeStatement() + "\n\n"); //$NON-NLS-1$
				} else {
					throw new NotSupportedException(Messages.ToggleFromImplementationToHeaderOrClassStrategy_CanNotCreateNewFile);
				}
			}
		}
	}

	private void addDefinitionToHeader(ModificationCollector modifications, List<IASTComment> leadingComments) {
		ASTRewrite headerRewrite = modifications.rewriterForTranslationUnit(other_tu);
		IASTFunctionDefinition newDefinition = ToggleNodeHelper.createFunctionSignatureWithEmptyBody(
context
				.getDefinition().getDeclSpecifier().copy(CopyStyle.withLocations), context.getDefinition()
				.getDeclarator().copy(CopyStyle.withLocations),
				context.getDefinition().copy(CopyStyle.withLocations));
		newDefinition.setParent(other_tu);
		headerRewrite.insertBefore(other_tu.getTranslationUnit(), null, newDefinition, infoText);
		restoreBody(headerRewrite, newDefinition, modifications);
		for (IASTComment comment : leadingComments) {			
			headerRewrite.addComment(newDefinition, comment, CommentPosition.leading);
		}
	}

	private void addDefinitionToClass(ModificationCollector modifications, List<IASTComment> leadingComments) {
		ASTRewrite headerRewrite = modifications.rewriterForTranslationUnit(
				context.getDeclarationUnit());
		IASTFunctionDefinition newDefinition = ToggleNodeHelper.createInClassDefinition(
				context.getDeclaration(), context.getDefinition(), 
				context.getDeclarationUnit());
		newDefinition.setParent(getParent());
		restoreBody(headerRewrite, newDefinition, modifications);
		headerRewrite.replace(context.getDeclaration().getParent(), newDefinition, infoText);
		for (IASTComment comment : leadingComments) {			
			headerRewrite.addComment(newDefinition, comment, CommentPosition.leading);
		}
	}

	private IASTNode getParent() {
		IASTNode parent = ToggleNodeHelper.getAncestorOfType(context.getDefinition(), 
				ICPPASTCompositeTypeSpecifier.class);
		IASTNode parentnode = null;
		if (parent != null) {
			parentnode  = parent;
		}
		else {
			parentnode =context.getDeclarationUnit();
		}
		return parentnode;
	}

	private void restoreBody(ASTRewrite headerRewrite, IASTFunctionDefinition newDefinition,
			ModificationCollector modifications) {
		IASTFunctionDefinition oldDefinition = context.getDefinition();
		newDefinition.setBody(oldDefinition.getBody().copy(CopyStyle.withLocations));
		
		if (newDefinition instanceof ICPPASTFunctionWithTryBlock && oldDefinition instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTFunctionWithTryBlock newTryDef = (ICPPASTFunctionWithTryBlock) newDefinition;
			ICPPASTFunctionWithTryBlock oldTryDef = (ICPPASTFunctionWithTryBlock) oldDefinition;
			for (ICPPASTCatchHandler handler : oldTryDef.getCatchHandlers()) {
				newTryDef.addCatchHandler(handler.copy(CopyStyle.withLocations));
			}
		}
		copyAllCommentsToNewLocation(oldDefinition, modifications.rewriterForTranslationUnit(oldDefinition.getTranslationUnit()), headerRewrite);
	}
	
	private void copyAllCommentsToNewLocation(IASTNode node, final ASTRewrite oldRw, final ASTRewrite newRw) {
		node.accept(new CPPASTAllVisitor() {
			@Override
			public int visitAll(IASTNode node){
				copyComments(oldRw, newRw, node, CommentPosition.leading);
				copyComments(oldRw, newRw, node, CommentPosition.trailing);
				copyComments(oldRw, newRw, node, CommentPosition.freestanding);
				return PROCESS_CONTINUE;
			}

			private void copyComments(final ASTRewrite oldRw, final ASTRewrite newRw, IASTNode node,
					CommentPosition pos) {
				List<IASTComment> comments = oldRw.getComments(node, pos);
				for (IASTComment comment : comments) {
					newRw.addComment(node, comment, pos);
				}
			}
		});
		
	}

	private void removeDefinitionFromImplementation(ASTRewrite implast) {
		ICPPASTNamespaceDefinition ns = ToggleNodeHelper.getAncestorOfType(
				context.getDefinition(), ICPPASTNamespaceDefinition.class);
		if (ns != null && isSingleElementInNamespace(ns, context.getDefinition())) {
			implast.remove(ns, infoText);
		} else {
			implast.remove(context.getDefinition(), infoText);
		}
	}

	private boolean isSingleElementInNamespace(ICPPASTNamespaceDefinition ns,
			IASTFunctionDefinition definition) {
		return ns.getChildren().length == 2 && (ns.contains(definition));
	}
}
