/*******************************************************************************
 * Copyright (c) 2011, 2015 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * 	   Martin Schwab & Thomas Kallenberg - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
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

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;

import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.CPPASTAllVisitor;

public class ToggleFromImplementationToHeaderOrClassStrategy implements IToggleRefactoringStrategy {
	private ToggleRefactoringContext context;
	private TextEditGroup infoText;
	private IASTTranslationUnit otherAst;
	private ASTLiteralNode includeNode;

	public ToggleFromImplementationToHeaderOrClassStrategy(ToggleRefactoringContext context) {
		this.context = context;
		this.infoText = new TextEditGroup(Messages.EditGroupName);
	}

	private boolean isFreeFunction(IASTFunctionDefinition definition) {
		return definition.getDeclarator().getName() instanceof ICPPASTQualifiedName;
	}
	
	@Override
	public void run(ModificationCollector modifications) throws CoreException {
		newFileCheck();
		ASTRewrite implAst = modifications.rewriterForTranslationUnit(context.getDefinitionAST());
		List<IASTComment>leadingComments = implAst.getComments(context.getDefinition(), CommentPosition.leading);
		removeDefinitionFromImplementation(implAst);
		if (includeNode != null) {
			implAst.insertBefore(context.getDefinitionAST(), 
					context.getDefinitionAST().getChildren()[0], includeNode, infoText);
		}
		if (context.getDeclarationAST() != null) {
			addDefinitionToClass(modifications, leadingComments);
		} else {
			addDefinitionToHeader(modifications, leadingComments);
		}
	}

	private void newFileCheck() throws CoreException {
		if (context.getDeclarationAST() == null) {
			if (isFreeFunction(context.getDefinition())) {
				throw new NotSupportedException(Messages.ToggleFromImplementationToHeaderOrClassStrategy_CanNotToggle);
			}
			otherAst = context.getASTForPartnerFile();
			if (otherAst == null) {
				ToggleFileCreator fileCreator = new ToggleFileCreator(context, ".h"); //$NON-NLS-1$
				if (fileCreator.askUserForFileCreation(context)) {
					IFile file = fileCreator.createNewFile();
					otherAst = context.getAST(file, null);
					includeNode = new ASTLiteralNode(fileCreator.getIncludeStatement() + "\n\n"); //$NON-NLS-1$
				} else {
					throw new NotSupportedException(Messages.ToggleFromImplementationToHeaderOrClassStrategy_CanNotCreateNewFile);
				}
			}
		}
	}

	private void addDefinitionToHeader(ModificationCollector modifications, List<IASTComment> leadingComments) {
		ASTRewrite headerRewrite = modifications.rewriterForTranslationUnit(otherAst);
		IASTFunctionDefinition newDefinition = ToggleNodeHelper.createFunctionSignatureWithEmptyBody(
				context.getDefinition().getDeclSpecifier().copy(CopyStyle.withLocations),
				context.getDefinition().getDeclarator().copy(CopyStyle.withLocations),
				context.getDefinition().copy(CopyStyle.withLocations));
		newDefinition.setParent(otherAst);
		headerRewrite.insertBefore(otherAst.getTranslationUnit(), null, newDefinition, infoText);
		restoreBody(headerRewrite, newDefinition, modifications);
		for (IASTComment comment : leadingComments) {			
			headerRewrite.addComment(newDefinition, comment, CommentPosition.leading);
		}
	}

	private void addDefinitionToClass(ModificationCollector modifications, List<IASTComment> leadingComments) {
		ASTRewrite headerRewrite = modifications.rewriterForTranslationUnit(
				context.getDeclarationAST());
		IASTFunctionDefinition newDefinition = ToggleNodeHelper.createInClassDefinition(
				context.getDeclaration(), context.getDefinition(), context.getDeclarationAST());
		newDefinition.setParent(getParent());
		restoreBody(headerRewrite, newDefinition, modifications);
		headerRewrite.replace(context.getDeclaration().getParent(), newDefinition, infoText);
		for (IASTComment comment : leadingComments) {			
			headerRewrite.addComment(newDefinition, comment, CommentPosition.leading);
		}
	}

	private IASTNode getParent() {
		IASTNode parent = CPPVisitor.findAncestorWithType(context.getDefinition(),
				ICPPASTCompositeTypeSpecifier.class);
		IASTNode parentnode = null;
		if (parent != null) {
			parentnode = parent;
		} else {
			parentnode = context.getDeclarationAST();
		}
		return parentnode;
	}

	private void restoreBody(ASTRewrite headerRewrite, IASTFunctionDefinition newDefinition,
			ModificationCollector modifications) {
		IASTFunctionDefinition oldDefinition = context.getDefinition();
		newDefinition.setBody(oldDefinition.getBody().copy(CopyStyle.withLocations));
		
		if (newDefinition instanceof ICPPASTFunctionWithTryBlock &&
				oldDefinition instanceof ICPPASTFunctionWithTryBlock) {
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
		ICPPASTNamespaceDefinition ns = findOutermostNonemptyNamspace();
		if (ns != null) {
			implast.remove(ns, infoText);
		} else {
			implast.remove(context.getDefinition(), infoText);
		}
	}

	private ICPPASTNamespaceDefinition findOutermostNonemptyNamspace() {
		List<ICPPASTNamespaceDefinition> namespaces = ToggleNodeHelper.findSurroundingNamespaces(context.getDefinition());
		Collections.reverse(namespaces);
		IASTFunctionDefinition definition = context.getDefinition();
		ICPPASTNamespaceDefinition ns = null;
		for (ICPPASTNamespaceDefinition namespace : namespaces) {
			if (isSingleElementInNamespace(namespace, definition)) {
				ns = namespace;
			} else {
				break;
			}
		}
		return ns;
	}

	private boolean isSingleElementInNamespace(ICPPASTNamespaceDefinition ns,
			IASTFunctionDefinition definition) {
		return ns.getChildren().length == 2 && (ns.contains(definition));
	}
}
