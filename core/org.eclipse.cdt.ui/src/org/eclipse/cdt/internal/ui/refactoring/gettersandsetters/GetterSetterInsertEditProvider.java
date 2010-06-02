/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;


public class GetterSetterInsertEditProvider implements Comparable<GetterSetterInsertEditProvider>{
	public enum Type{
		getter,
		setter;
	}
	
	
	
	private IASTSimpleDeclaration functionDeclaration;
	private Type type;
	private String name;
	private IASTSimpleDeclaration fieldDeclaration;
	
	
	public GetterSetterInsertEditProvider(String name, IASTSimpleDeclaration fieldDeclaration, Type type){
		switch(type) {
		case getter:
		
			this.functionDeclaration = FunctionFactory.createGetterDeclaration(name, fieldDeclaration);
			break;
		case setter:
			this.functionDeclaration = FunctionFactory.createSetterDeclaration(name, fieldDeclaration);
			break;
		}
		
		this.type = type;
		this.name = name;
		this.fieldDeclaration = fieldDeclaration;
	}
	
	@Override
	public String toString(){
		return functionDeclaration.getDeclarators()[0].getName().toString();
	}

	public IASTFunctionDefinition getFunctionDefinition(boolean qualifedName) {
		IASTFunctionDefinition definition = null;
		ICPPASTQualifiedName qname;
		if(qualifedName) {
			qname = getClassname();
		}else {
			qname = null;
		}
		
		switch(type) {
		case getter:
			definition = FunctionFactory.createGetterDefinition(name, fieldDeclaration, qname);
			break;
		case setter:
			definition = FunctionFactory.createSetterDefinition(name, fieldDeclaration, qname);
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
		qname.addName(comp.getName().copy());
		return qname;
	}

	public IASTSimpleDeclaration getFunctionDeclaration() {
		return functionDeclaration;
	}

	public Type getType() {
		return type;
	}

	public int compareTo(GetterSetterInsertEditProvider o) {
		return toString().compareTo(o.toString());
	}
	
	
}
