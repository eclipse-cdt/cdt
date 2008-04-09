/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * 
 * Visits the nodes in consideration of {@link ASTModification}s.
 * 
 * @since 5.0
 * @author Emanuel Graf IFS
 * 
 */
public class ChangeGeneratorWriterVisitor extends ASTWriterVisitor {

	private static final String DEFAULT_INDENTATION = ""; //$NON-NLS-1$
	private final ASTModificationStore modificationStore;
	private final String fileScope;
	private ModificationScopeStack stack;

	public ChangeGeneratorWriterVisitor(CPPASTVisitor delegateVisitor,
			ASTModificationStore modificationStore, String fileScope, NodeCommentMap commentMap) {
		super(commentMap);

		this.modificationStore = modificationStore;
		this.fileScope = fileScope;
		this.stack = new ModificationScopeStack(modificationStore);

		shouldVisitExpressions = delegateVisitor.shouldVisitExpressions;
		shouldVisitStatements = delegateVisitor.shouldVisitStatements;
		shouldVisitNames = delegateVisitor.shouldVisitNames;
		shouldVisitDeclarations = delegateVisitor.shouldVisitDeclarators;
		shouldVisitDeclSpecifiers = delegateVisitor.shouldVisitDeclSpecifiers;
		shouldVisitDeclarators = delegateVisitor.shouldVisitDeclarators;
		shouldVisitInitializers = delegateVisitor.shouldVisitInitializers;
		shouldVisitBaseSpecifiers = delegateVisitor.shouldVisitBaseSpecifiers;
		shouldVisitNamespaces = delegateVisitor.shouldVisitNamespaces;
		shouldVisitTemplateParameters = delegateVisitor.shouldVisitTemplateParameters;
		shouldVisitParameterDeclarations = delegateVisitor.shouldVisitParameterDeclarations;
		shouldVisitTranslationUnit = delegateVisitor.shouldVisitTranslationUnit;
		shouldVisitProblems = delegateVisitor.shouldVisitProblems;
		shouldVisitTypeIds = delegateVisitor.shouldVisitTypeIds;

	}

	public ChangeGeneratorWriterVisitor(ASTModificationStore modStore, NodeCommentMap nodeMap) {
		this(modStore, DEFAULT_INDENTATION, null, nodeMap);
	}

	public ChangeGeneratorWriterVisitor(ASTModificationStore modStore,
			String givenIndentation, String fileScope, NodeCommentMap commentMap) {
		super(givenIndentation, commentMap);
		declaratorWriter = new ModifiedASTDeclaratorWriter(scribe, this,
				modStore, commentMap);
		expWriter = new ModifiedASTExpressionWriter(scribe, this, macroHandler,
				modStore, commentMap);
		statementWriter = new ModifiedASTStatementWriter(scribe, this, modStore, commentMap);
		declSpecWriter = new ModifiedASTDeclSpecWriter(scribe, this, modStore, commentMap);

		this.modificationStore = modStore;
		this.fileScope = fileScope;
		this.shouldVisitTranslationUnit = true;
		this.stack = new ModificationScopeStack(modificationStore);
	}

	@Override
	public int leave(ICPPASTBaseSpecifier specifier) {
		super.leave(specifier);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(ICPPASTNamespaceDefinition namespace) {
		super.leave(namespace);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(ICPPASTTemplateParameter parameter) {
		super.leave(parameter);
		return PROCESS_SKIP;
	}

	@Override
	public int visit(ICPPASTBaseSpecifier specifier) {
		if (doBeforeEveryNode(specifier) == PROCESS_CONTINUE) {
			return super.visit(specifier);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(ICPPASTNamespaceDefinition namespace) {
		if (doBeforeEveryNode(namespace) == PROCESS_CONTINUE) {
			return super.visit(namespace);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(ICPPASTTemplateParameter parameter) {
		if (doBeforeEveryNode(parameter) == PROCESS_CONTINUE) {
			return super.visit(parameter);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTDeclaration declaration) {
		super.leave(declaration);
		doAfterEveryNode(declaration);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTDeclarator declarator) {
		super.leave(declarator);
		doAfterEveryNode(declarator);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		super.leave(declSpec);
		doAfterEveryNode(declSpec);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTEnumerator enumerator) {
		super.leave(enumerator);
		doAfterEveryNode(enumerator);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTExpression expression) {
		super.leave(expression);
		doAfterEveryNode(expression);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTInitializer initializer) {
		super.leave(initializer);
		doAfterEveryNode(initializer);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTName name) {
		super.leave(name);
		doAfterEveryNode(name);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTParameterDeclaration parameterDeclaration) {
		super.leave(parameterDeclaration);
		doAfterEveryNode(parameterDeclaration);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTProblem problem) {
		super.leave(problem);
		doAfterEveryNode(problem);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTStatement statement) {
		super.leave(statement);
		doAfterEveryNode(statement);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		super.leave(tu);
		doAfterEveryNode(tu);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTTypeId typeId) {
		super.leave(typeId);
		doAfterEveryNode(typeId);
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		if (doBeforeEveryNode(declaration) == PROCESS_CONTINUE) {
			return super.visit(declaration);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		if (doBeforeEveryNode(declarator) == PROCESS_CONTINUE) {
			return super.visit(declarator);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		if (doBeforeEveryNode(declSpec) == PROCESS_CONTINUE) {
			return super.visit(declSpec);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		if (doBeforeEveryNode(enumerator) == PROCESS_CONTINUE) {
			return super.visit(enumerator);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTExpression expression) {
		if (doBeforeEveryNode(expression) == PROCESS_CONTINUE) {
			return super.visit(expression);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTInitializer initializer) {
		if (doBeforeEveryNode(initializer) == PROCESS_CONTINUE) {
			return super.visit(initializer);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTName name) {
		if (doBeforeEveryNode(name) == PROCESS_CONTINUE) {
			return super.visit(name);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		if (doBeforeEveryNode(parameterDeclaration) == PROCESS_CONTINUE) {
			return super.visit(parameterDeclaration);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTProblem problem) {
		if (doBeforeEveryNode(problem) == PROCESS_CONTINUE) {
			return super.visit(problem);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTStatement statement) {
		if (doBeforeEveryNode(statement) == PROCESS_CONTINUE) {
			return super.visit(statement);
		}
		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTTranslationUnit tu) {
		ASTModificationHelper helper = new ASTModificationHelper(
				modificationStore);

		IASTDeclaration[] declarations = helper.createModifiedChildArray(tu, tu
				.getDeclarations());
		for (IASTDeclaration currentDeclaration : declarations) {
			currentDeclaration.accept(this);
		}

		return PROCESS_SKIP;
	}

	@Override
	public int visit(IASTTypeId typeId) {
		if (doBeforeEveryNode(typeId) == PROCESS_CONTINUE) {
			return super.visit(typeId);
		}
		return PROCESS_SKIP;
	}
	
	protected void doAfterEveryNode(IASTNode node) {
		stack.popScope(node);
	}

	protected int doBeforeEveryNode(IASTNode node) {
		if (fileScope != null) {
			String file = getCorrespondingFile(node);
			if (!fileScope.equals(file)) {
				return PROCESS_SKIP;
			}
		}

		if (stack.getModifiedNodes().contains(node)) {
			List<ASTModification> modificationList = stack
					.getModificationsForNode(node);
			if (modificationList == null) {
				return PROCESS_CONTINUE;
			}

			for (ASTModification currentMod : modificationList) {
				switch (currentMod.getKind()) {
				case APPEND_CHILD:
					stack.pushScope(node);
					return PROCESS_CONTINUE;
				case INSERT_BEFORE:
					stack.pushScope(node);
					return PROCESS_CONTINUE;
				case REPLACE:
					
					if (currentMod.getNewNode() == null) {
						continue;
					}
					stack.pushScope(node);
					currentMod.getNewNode().accept(this);
					stack.popScope(node);
					break;
				}
			}

			return PROCESS_SKIP;
		}
		return PROCESS_CONTINUE;
	}

	private String getCorrespondingFile(IASTNode node) {

		if (node.getFileLocation() != null) {
			return node.getFileLocation().getFileName();
		}

		if (node.getParent() != null) {
			return getCorrespondingFile(node.getParent());
		}

		for (IASTNode modifiedNode : modificationStore.getRootModifications()
				.getModifiedNodes()) {
			for (ASTModification modification : modificationStore
					.getRootModifications().getModificationsForNode(
							modifiedNode)) {
				if (modification.getNewNode() == node) {
					return getCorrespondingFile(modification.getTargetNode());
				}
			}
		}
		return null;
	}
}
