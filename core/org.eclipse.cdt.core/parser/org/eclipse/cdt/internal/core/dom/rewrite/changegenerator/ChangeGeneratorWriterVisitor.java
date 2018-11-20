/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.changegenerator;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModification.ModificationKind;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTModificationStore;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriterVisitor;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.NodeCommentMap;

/**
 * Visits the nodes in consideration of {@link ASTModification}s.
 *
 * @since 5.0
 * @author Emanuel Graf IFS
 */
public class ChangeGeneratorWriterVisitor extends ASTWriterVisitor {
	private final ASTModificationStore modificationStore;
	private final String fileScope;
	private ModificationScopeStack stack;

	public ChangeGeneratorWriterVisitor(ASTVisitor delegateVisitor, ASTModificationStore modificationStore,
			String fileScope, NodeCommentMap commentMap, boolean placeConstRight) {
		super(commentMap);

		this.modificationStore = modificationStore;
		this.fileScope = fileScope;
		this.stack = new ModificationScopeStack(modificationStore);

		shouldVisitArrayModifiers = delegateVisitor.shouldVisitArrayModifiers;
		shouldVisitBaseSpecifiers = delegateVisitor.shouldVisitBaseSpecifiers;
		shouldVisitDeclarations = delegateVisitor.shouldVisitDeclarators;
		shouldVisitDeclarators = delegateVisitor.shouldVisitDeclarators;
		shouldVisitDeclSpecifiers = delegateVisitor.shouldVisitDeclSpecifiers;
		shouldVisitExpressions = delegateVisitor.shouldVisitExpressions;
		shouldVisitInitializers = delegateVisitor.shouldVisitInitializers;
		shouldVisitNames = delegateVisitor.shouldVisitNames;
		shouldVisitNamespaces = delegateVisitor.shouldVisitNamespaces;
		shouldVisitParameterDeclarations = delegateVisitor.shouldVisitParameterDeclarations;
		shouldVisitPointerOperators = delegateVisitor.shouldVisitPointerOperators;
		shouldVisitProblems = delegateVisitor.shouldVisitProblems;
		shouldVisitStatements = delegateVisitor.shouldVisitStatements;
		shouldVisitTemplateParameters = delegateVisitor.shouldVisitTemplateParameters;
		shouldVisitTranslationUnit = delegateVisitor.shouldVisitTranslationUnit;
		shouldVisitTypeIds = delegateVisitor.shouldVisitTypeIds;
	}

	public ChangeGeneratorWriterVisitor(ASTModificationStore modStore, NodeCommentMap nodeMap) {
		this(modStore, null, nodeMap, false);
	}

	public ChangeGeneratorWriterVisitor(ASTModificationStore modStore, NodeCommentMap nodeMap,
			boolean placeConstRight) {
		this(modStore, null, nodeMap, placeConstRight);
	}

	public ChangeGeneratorWriterVisitor(ASTModificationStore modStore, String fileScope, NodeCommentMap commentMap) {
		this(modStore, fileScope, commentMap, false);
	}

	public ChangeGeneratorWriterVisitor(ASTModificationStore modStore, String fileScope, NodeCommentMap commentMap,
			boolean placeConstRight) {
		super(commentMap);
		this.modificationStore = modStore;
		this.fileScope = fileScope;
		this.shouldVisitTranslationUnit = true;
		this.stack = new ModificationScopeStack(modificationStore);
		declaratorWriter = new ModifiedASTDeclaratorWriter(scribe, this, stack, commentMap);
		expWriter = new ModifiedASTExpressionWriter(scribe, this, macroHandler, stack, commentMap);
		statementWriter = new ModifiedASTStatementWriter(scribe, this, stack, commentMap);
		declSpecWriter = new ModifiedASTDeclSpecWriter(scribe, this, stack, commentMap, placeConstRight);
		declarationWriter = new ModifiedASTDeclarationWriter(scribe, this, stack, commentMap);
	}

	@Override
	protected IASTDeclarator getParameterDeclarator(IASTParameterDeclaration parameterDeclaration) {
		IASTDeclarator newDecl = parameterDeclaration.getDeclarator();
		if (stack.getModifiedNodes().contains(newDecl)) {
			for (ASTModification currentModification : stack.getModificationsForNode(newDecl)) {
				if (currentModification.getKind() == ASTModification.ModificationKind.REPLACE
						&& currentModification.getTargetNode() == parameterDeclaration) {
					newDecl = (IASTDeclarator) currentModification.getNewNode();
				}
			}
		}
		return newDecl;
	}

	@Override
	protected IASTName getParameterName(IASTDeclarator declarator) {
		IASTName newName = declarator.getName();
		if (stack.getModifiedNodes().contains(newName)) {
			for (ASTModification currentModification : stack.getModificationsForNode(newName)) {
				if (currentModification.getKind() == ASTModification.ModificationKind.REPLACE
						&& currentModification.getTargetNode() == newName) {
					newName = (IASTName) currentModification.getNewNode();
				}
			}
		}
		return newName;
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
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTDeclarator declarator) {
		super.leave(declarator);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		super.leave(declSpec);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTEnumerator enumerator) {
		super.leave(enumerator);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTExpression expression) {
		super.leave(expression);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTInitializer initializer) {
		super.leave(initializer);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTName name) {
		super.leave(name);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTParameterDeclaration parameterDeclaration) {
		super.leave(parameterDeclaration);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTPointerOperator pointerOperator) {
		super.leave(pointerOperator);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTProblem problem) {
		super.leave(problem);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTStatement statement) {
		super.leave(statement);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		super.leave(tu);
		return PROCESS_SKIP;
	}

	@Override
	public int leave(IASTTypeId typeId) {
		super.leave(typeId);
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
	public int visit(IASTArrayModifier arrayModifier) {
		if (doBeforeEveryNode(arrayModifier) == PROCESS_CONTINUE) {
			return super.visit(arrayModifier);
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
	public int visit(IASTPointerOperator pointerOperator) {
		if (doBeforeEveryNode(pointerOperator) == PROCESS_CONTINUE) {
			return super.visit(pointerOperator);
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
		ASTModificationHelper helper = new ASTModificationHelper(stack);
		IASTDeclaration[] declarations = helper.createModifiedChildArray(tu, tu.getDeclarations(),
				IASTDeclaration.class, commentMap);
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

	protected int doBeforeEveryNode(IASTNode node) {
		stack.clean(node);
		if (fileScope != null) {
			String file = getCorrespondingFile(node);
			if (!fileScope.equals(file)) {
				return PROCESS_SKIP;
			}
		}
		// Check all insert before and append modifications for the current node.
		// If necessary put it onto the stack.
		for (IASTNode currentModifiedNode : stack.getModifiedNodes()) {
			for (ASTModification currentMod : stack.getModificationsForNode(currentModifiedNode)) {
				if (currentMod.getNewNode() == node && currentMod.getKind() != ModificationKind.REPLACE) {
					stack.pushScope(currentModifiedNode);
					return PROCESS_CONTINUE;
				}
			}
		}
		// Check all replace modifications for the current node. Visit the replacing node if found.
		for (IASTNode currentModifiedNode : stack.getModifiedNodes()) {
			for (ASTModification currentMod : stack.getModificationsForNode(currentModifiedNode)) {
				if (currentMod.getTargetNode() == node && currentMod.getKind() == ModificationKind.REPLACE) {
					if (currentMod.getNewNode() != null) {
						stack.pushScope(node);
						currentMod.getNewNode().accept(this);
						stack.popScope(node);
					}
					return PROCESS_SKIP;
				}
			}
		}

		// Check replace modifications for the current node. Is required as nodes could have been replaced
		// externally, e.g. in ASTModificationHelper.
		for (IASTNode currentModifiedNode : stack.getModifiedNodes()) {
			for (ASTModification currentMod : stack.getModificationsForNode(currentModifiedNode)) {
				if (currentMod.getNewNode() == node && currentMod.getKind() == ModificationKind.REPLACE) {
					stack.pushScope(currentModifiedNode);
					return PROCESS_CONTINUE;
				}
			}
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

		for (IASTNode modifiedNode : modificationStore.getRootModifications().getModifiedNodes()) {
			for (ASTModification modification : modificationStore.getRootModifications()
					.getModificationsForNode(modifiedNode)) {
				if (modification.getNewNode() == node) {
					return getCorrespondingFile(modification.getTargetNode());
				}
			}
		}
		return null;
	}
}
