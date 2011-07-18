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
	private IASTName fieldName;
	private IASTSimpleDeclaration fieldDeclaration;
	
	public GetterSetterInsertEditProvider(IASTName fieldName, IASTSimpleDeclaration fieldDeclaration,
			AccessorKind kind) {
		this.kind = kind;
		this.fieldName = fieldName;
		this.fieldDeclaration = fieldDeclaration;
		
		createFunctionDeclaration();
	}

	public void createFunctionDeclaration() {
		switch (this.kind) {
		case GETTER:
			this.functionDeclaration = FunctionFactory.createGetterDeclaration(fieldName, fieldDeclaration);
			break;
		case SETTER:
			this.functionDeclaration = FunctionFactory.createSetterDeclaration(fieldName, fieldDeclaration);
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
			qname = getClassname();
		} else {
			qname = null;
		}
		
		switch (kind) {
		case GETTER:
			definition = FunctionFactory.createGetterDefinition(fieldName, fieldDeclaration, qname);
			break;
		case SETTER:
			definition = FunctionFactory.createSetterDefinition(fieldName, fieldDeclaration, qname);
			break;
		}
		return definition;
	}
	
	private ICPPASTQualifiedName getClassname() {
		IASTNode n = fieldDeclaration.getParent();
		while (!(n instanceof IASTCompositeTypeSpecifier)) {
			n = n.getParent();
		}
		IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) n;
		
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

	public int compareTo(GetterSetterInsertEditProvider o) {
		return toString().compareTo(o.toString());
	}
}
