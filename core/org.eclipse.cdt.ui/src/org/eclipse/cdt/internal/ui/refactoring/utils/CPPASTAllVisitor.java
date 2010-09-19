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
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

public class CPPASTAllVisitor extends ASTVisitor {
	
	{
		shouldVisitNames = true;
		shouldVisitDeclarations = true;
		shouldVisitInitializers = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitDeclarators = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitExpressions = true;
		shouldVisitStatements = true;
		shouldVisitTypeIds = true;
		shouldVisitEnumerators = true;
		shouldVisitTranslationUnit = true;
		shouldVisitProblems = true;

		shouldVisitBaseSpecifiers = true;
		shouldVisitNamespaces = true;
		shouldVisitTemplateParameters = true;
	}
	
	
	@Override
	public int visit(IASTTranslationUnit tu) {
		return visitAll(tu);
	}

	@Override
	public int visit(IASTName name) {
		return visitAll(name);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		return visitAll(declaration);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		return visitAll(initializer);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return visitAll(parameterDeclaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		return visitAll(declarator);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		return visitAll(declSpec);
	}

	@Override
	public int visit(IASTExpression expression) {
		return visitAll(expression);
	}

	@Override
	public int visit(IASTStatement statement) {
		return visitAll(statement);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		return visitAll(typeId);
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		return visitAll(enumerator);
	}
	
	@Override
	public int visit( IASTProblem problem ){
		return visitAll(problem);
	}
	
	@Override
	public int visit( IASTComment comment ){
		return visitAll(comment);
	}
	

	/**
	 * Visit BaseSpecifiers.
	 */
	@Override
	public int visit(ICPPASTBaseSpecifier specifier) {
		return visitAll(specifier);
	}

	/**
	 * Visit namespace definitions.
	 */
	@Override
	public int visit(ICPPASTNamespaceDefinition namespace) {
		return visitAll(namespace);
	}

	/**
	 * Visit template parameter.
	 */
	@Override
	public int visit(ICPPASTTemplateParameter parameter) {
		return visitAll(parameter);
	}
	
	public int visitAll(IASTNode node){
		return PROCESS_CONTINUE;
	}
}
