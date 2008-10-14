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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
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
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

/**
 * Utility class to search for siblings of an ast node
 */
public class ASTNodeSearch extends ASTVisitor implements ICASTVisitor, ICPPASTVisitor {
	private static final int LEFT = 0;
	private static final int RIGHT= 1;
	private int fMode;
	private IASTNode fLeft;
	private IASTNode fRight;
	private final IASTNode fNode;
	private final IASTNode fParent;

	public ASTNodeSearch(IASTNode node) {
		super(true);
		fNode= node;
		fParent= node.getParent();
	}
	
	public IASTNode findLeftSibling() {
		if (fParent == null)
			return null;

		fMode= LEFT;
		fLeft= fRight= null;
		fParent.accept(this);
		return fLeft;
	}

	public IASTNode findRightSibling() {
		if (fParent == null)
			return null;

		fMode= RIGHT;
		fLeft= fRight= null;
		fParent.accept(this);
		return fRight;
	}

	private int process(IASTNode node) {
		if (node == fParent)
			return PROCESS_CONTINUE;
		
		switch(fMode) {
		case LEFT:
			if (node == fNode)
				return PROCESS_ABORT;
			fLeft= node;
			return PROCESS_SKIP;
		case RIGHT:
			if (node == fNode) {
				fLeft= fNode;
			} else if (fLeft != null) {
				fRight= node;
				return PROCESS_ABORT;
			}
			return PROCESS_SKIP;
		}
		return PROCESS_SKIP;
	}


	public int visit(ICASTDesignator designator) {
		return process(designator);
	}

	@Override
	public int visit(IASTArrayModifier arrayModifier) {
		return process(arrayModifier);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		return process(declaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		return process(declarator);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		return process(declSpec);
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		return process(enumerator);
	}

	@Override
	public int visit(IASTExpression expression) {
		return process(expression);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		return process(initializer);
	}

	@Override
	public int visit(IASTName name) {
		return process(name);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return process(parameterDeclaration);
	}

	@Override
	public int visit(IASTProblem problem) {
		return process(problem);
	}

	@Override
	public int visit(IASTStatement statement) {
		return process(statement);
	}

	@Override
	public int visit(IASTTranslationUnit tu) {
		return process(tu);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		return process(typeId);
	}

	public int visit(ICPPASTBaseSpecifier baseSpecifier) {
		return process(baseSpecifier);
	}

	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		return process(namespaceDefinition);
	}

	public int visit(ICPPASTTemplateParameter templateParameter) {
		return process(templateParameter);
	}

	public int leave(ICASTDesignator designator) {
		return PROCESS_CONTINUE;
	}
	public int leave(ICPPASTBaseSpecifier baseSpecifier) {
		return PROCESS_CONTINUE;
	}

	public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
		return PROCESS_CONTINUE;
	}

	public int leave(ICPPASTTemplateParameter templateParameter) {
		return PROCESS_CONTINUE;
	}
}
