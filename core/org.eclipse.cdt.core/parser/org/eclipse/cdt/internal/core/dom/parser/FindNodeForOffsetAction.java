/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
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
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

/**
 * Visitor to search for nodes by file offsets.
 * @since 5.0
 */
public class FindNodeForOffsetAction extends CPPASTVisitor implements ICASTVisitor {
	private final ASTNodeSpecification<?> fNodeSpec;

	public FindNodeForOffsetAction(ASTNodeSpecification<?> nodeSpec) {
		fNodeSpec= nodeSpec;

		shouldVisitNames = true;
		shouldVisitDeclarations= true;
		
		shouldVisitArrayModifiers=
		shouldVisitInitializers=
		shouldVisitParameterDeclarations=
		shouldVisitDeclarators=
		shouldVisitDeclSpecifiers=
		shouldVisitDesignators=
		shouldVisitEnumerators=
		shouldVisitExpressions=
		shouldVisitStatements=
		shouldVisitTypeIds=
		shouldVisitEnumerators=
		shouldVisitBaseSpecifiers=
		shouldVisitNamespaces=
		shouldVisitTemplateParameters=
		shouldVisitTranslationUnit= !nodeSpec.requiresClass(IASTName.class);
	}

	public int processNode(IASTNode node) {
		if (node instanceof ASTNode) {
			final ASTNode astNode = (ASTNode) node;
			if (!fNodeSpec.canContainMatches(astNode)) {
				return PROCESS_SKIP;
			}
			fNodeSpec.visit(astNode);
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		// use declarations to determine if the search has gone past the
		// offset (i.e. don't know the order the visitor visits the nodes)
		if (declaration instanceof ASTNode && ((ASTNode) declaration).getOffset() > fNodeSpec.getSequenceEnd())
			return PROCESS_ABORT;

		return processNode(declaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		int ret = processNode(declarator);

		IASTPointerOperator[] ops = declarator.getPointerOperators();
		for (int i = 0; i < ops.length; i++)
			processNode(ops[i]);

		return ret;
	}

	
	@Override
	public int visit(IASTArrayModifier arrayModifier) {
		return processNode(arrayModifier);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		return processNode(declSpec);
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		return processNode(enumerator);
	}

	@Override
	public int visit(IASTExpression expression) {
		return processNode(expression);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		return processNode(initializer);
	}

	@Override
	public int visit(IASTName name) {
		return processNode(name);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return processNode(parameterDeclaration);
	}

	@Override
	public int visit(IASTStatement statement) {
		return processNode(statement);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		return processNode(typeId);
	}
	
	@Override
	public int visit(ICPPASTBaseSpecifier baseSpecifier) {
		return processNode(baseSpecifier);
	}
				
	@Override
	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		return processNode(namespaceDefinition);
	}

	@Override
	public int visit(ICPPASTTemplateParameter templateParameter) {
		return processNode(templateParameter);
	}

	@Override
	public int visit(IASTProblem problem) {
		return processNode(problem);
	}

	public int visit(ICASTDesignator designator) {
		return processNode(designator);
	}

	public int leave(ICASTDesignator designator) {
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTTranslationUnit tu) {
		return processNode(tu);
	}
}