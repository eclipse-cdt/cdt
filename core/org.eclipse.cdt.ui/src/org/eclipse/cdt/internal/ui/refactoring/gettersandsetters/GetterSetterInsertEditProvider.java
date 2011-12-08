/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;

public class GetterSetterInsertEditProvider implements Comparable<GetterSetterInsertEditProvider> {
	public enum AccessorKind {
		GETTER,
		SETTER;
	}
	
	private IASTSimpleDeclaration functionDeclaration;
	private AccessorKind kind;
	private IASTSimpleDeclaration fieldDeclaration;
	private GetterSetterFactory getterSetterFactory;
	
	public GetterSetterInsertEditProvider(IASTName fieldName, IASTSimpleDeclaration fieldDeclaration,
			AccessorKind kind) {
		this.kind = kind;
		this.fieldDeclaration = fieldDeclaration;
		this.getterSetterFactory = new GetterSetterFactory(fieldName, fieldDeclaration);
		
		createFunctionDeclaration();
	}

	public void createFunctionDeclaration() {
		switch (this.kind) {
		case GETTER:
			this.functionDeclaration = getterSetterFactory.createGetterDeclaration();
			break;
		case SETTER:
			this.functionDeclaration = getterSetterFactory.createSetterDeclaration();
			break;
		}
	}
	
	@Override
	public String toString() {
		IASTDeclarator declarator = functionDeclaration.getDeclarators()[0];
		while (declarator.getNestedDeclarator() != null) {
			declarator = declarator.getNestedDeclarator();
		}
		return declarator.getName().toString();
	}

	public IASTFunctionDefinition getFunctionDefinition(boolean qualifedName) {
		IASTFunctionDefinition definition = null;
		ICPPASTQualifiedName qname;
		if (qualifedName) {
			qname = getClassName();
		} else {
			qname = null;
		}
		
		switch (kind) {
		case GETTER:
			definition = getterSetterFactory.createGetterDefinition(qname);
			break;
		case SETTER:
			definition = getterSetterFactory.createSetterDefinition(qname);
			break;
		}
		return definition;
	}
	
	private ICPPASTQualifiedName getClassName() {
		IASTNode node = fieldDeclaration.getParent();
		while (!(node instanceof IASTCompositeTypeSpecifier)) {
			node = node.getParent();
		}
		IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) node;
		
		CPPASTQualifiedName qname = new CPPASTQualifiedName();
		qname.addName(comp.getName().copy(CopyStyle.withLocations));
		return qname;
	}

	public IASTSimpleDeclaration getFunctionDeclaration() {
		return functionDeclaration;
	}

	public AccessorKind getType() {
		return kind;
	}

	@Override
	public int compareTo(GetterSetterInsertEditProvider o) {
		return toString().compareTo(o.toString());
	}
}
