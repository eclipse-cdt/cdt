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

import java.util.ArrayList;
import java.util.List;

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
 * Collector to find all children for an ast-node.
 */
class ChildCollector extends ASTVisitor implements ICPPASTVisitor, ICASTVisitor {

	private final IASTNode fNode;
	private List<IASTNode> fNodes;

	public ChildCollector(IASTNode node) {
		super(true);
		fNode= node;
	}

	public IASTNode[] getChildren() {
		fNode.accept(this);
		if (fNodes == null)
			return IASTNode.EMPTY_NODE_ARRAY;
		
		return fNodes.toArray(new IASTNode[fNodes.size()]);
	}
	
	private int addChild(IASTNode child) {
		if (fNodes == null) {
			if (child == fNode)
				return PROCESS_CONTINUE;
			fNodes= new ArrayList<IASTNode>();
		}
		fNodes.add(child);
		return PROCESS_SKIP;
	}
	
	public int visit(ICPPASTBaseSpecifier baseSpecifier) {
		return addChild(baseSpecifier);
	}

	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		return addChild(namespaceDefinition);
	}

	public int visit(ICPPASTTemplateParameter templateParameter) {
		return addChild(templateParameter);
	}

	@Override
	public int visit(IASTArrayModifier arrayModifier) {
		return addChild(arrayModifier);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		return addChild(declaration);
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		return addChild(declarator);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {
		return addChild(declSpec);
	}

	@Override
	public int visit(IASTEnumerator enumerator) {
		return addChild(enumerator);
	}

	@Override
	public int visit(IASTExpression expression) {
		return addChild(expression);
	}

	@Override
	public int visit(IASTInitializer initializer) {
		return addChild(initializer);
	}

	@Override
	public int visit(IASTName name) {
		return addChild(name);
	}

	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		return addChild(parameterDeclaration);
	}

	@Override
	public int visit(IASTProblem problem) {
		return addChild(problem);
	}

	@Override
	public int visit(IASTStatement statement) {
		return addChild(statement);
	}

	@Override
	public int visit(IASTTranslationUnit tu) {
		return addChild(tu);
	}

	@Override
	public int visit(IASTTypeId typeId) {
		return addChild(typeId);
	}

	public int visit(ICASTDesignator designator) {
		return addChild(designator);
	}

	public int leave(ICASTDesignator designator) {
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTBaseSpecifier baseSpecifier) {
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
		return PROCESS_SKIP;
	}

	public int leave(ICPPASTTemplateParameter templateParameter) {
		return PROCESS_SKIP;
	}
}
