/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisitor;

/**
 * Generic visitor for ast-nodes. 
 * <p> Clients may subclass. </p> 
 * @since 5.1
 */
public abstract class ASTGenericVisitor extends ASTVisitor implements ICPPASTVisitor, ICASTVisitor {
	public ASTGenericVisitor(boolean visitNodes) {
		super(visitNodes);
	}

	protected int genericVisit(IASTNode node) {
		return PROCESS_CONTINUE;
	}
	
	protected int genericLeave(IASTNode node) {
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(ICPPASTBaseSpecifier baseSpecifier) {
		return genericVisit(baseSpecifier);
	}

	@Override
	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		return genericVisit(namespaceDefinition);
	}

	@Override
	public int visit(ICPPASTTemplateParameter templateParameter) {
		return genericVisit(templateParameter);
	}
	
	@Override
	public int visit(ICPPASTCapture capture) {
		return genericVisit(capture);
	}

	@Override
	public int visit(IASTArrayModifier arrayModifier) {
		return genericVisit(arrayModifier);
	}
	
	@Override
	public int visit(IASTPointerOperator ptrOperator) {
		return genericVisit(ptrOperator);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		return genericVisit(declaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		return genericVisit(declarator);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		return genericVisit(declSpec);
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		return genericVisit(enumerator);
	}

	@Override
	public int visit(IASTExpression expression) {
		return genericVisit(expression);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		return genericVisit(initializer);
	}

	@Override
	public int visit(IASTName name) {
		return genericVisit(name);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return genericVisit(parameterDeclaration);
	}

	@Override
	public int visit(IASTProblem problem) {
		return genericVisit(problem);
	}

	@Override
	public int visit(IASTStatement statement) {
		return genericVisit(statement);
	}

	@Override
	public int visit(IASTTranslationUnit tu) {
		return genericVisit(tu);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		return genericVisit(typeId);
	}

	@Override
	public int visit(ICASTDesignator designator) {
		return genericVisit(designator);
	}

	@Override
	public int leave(ICASTDesignator designator) {
		return genericLeave(designator);
	}

	@Override
	public int leave(ICPPASTBaseSpecifier baseSpecifier) {
		return genericLeave(baseSpecifier);
	}

	@Override
	public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
		return genericLeave(namespaceDefinition);
	}

	@Override
	public int leave(ICPPASTTemplateParameter templateParameter) {
		return genericLeave(templateParameter);
	}

	@Override
	public int leave(ICPPASTCapture capture) {
		return genericLeave(capture);
	}

	@Override
	public int leave(IASTArrayModifier arrayModifier) {
		return genericLeave(arrayModifier);
	}
	
	@Override
	public int leave(IASTPointerOperator ptrOperator) {
		return genericLeave(ptrOperator);
	}

	@Override
	public int leave(IASTDeclaration declaration) {
		return genericLeave(declaration);
	}

	@Override
	public int leave(IASTDeclarator declarator) {
		return genericLeave(declarator);
	}

	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		return genericLeave(declSpec);
	}

	@Override
	public int leave(IASTEnumerator enumerator) {
		return genericLeave(enumerator);
	}

	@Override
	public int leave(IASTExpression expression) {
		return genericLeave(expression);
	}

	@Override
	public int leave(IASTInitializer initializer) {
		return genericLeave(initializer);
	}

	@Override
	public int leave(IASTName name) {
		return genericLeave(name);
	}

	@Override
	public int leave(IASTParameterDeclaration parameterDeclaration) {
		return genericLeave(parameterDeclaration);
	}

	@Override
	public int leave(IASTProblem problem) {
		return genericLeave(problem);
	}

	@Override
	public int leave(IASTStatement statement) {
		return genericLeave(statement);
	}

	@Override
	public int leave(IASTTranslationUnit tu) {
		return genericLeave(tu);
	}

	@Override
	public int leave(IASTTypeId typeId) {
		return genericLeave(typeId);
	}
}
