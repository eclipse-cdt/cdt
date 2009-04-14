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
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;


public class GetterSetterInsertEditProvider implements Comparable<GetterSetterInsertEditProvider>{
	public enum Type{
		getter,
		setter;
	}
	
	
	
	private IASTFunctionDefinition function;
	private Type type;
	
	
	public GetterSetterInsertEditProvider(IASTFunctionDefinition function, Type type){
		this.function = function;
		this.type = type;
	}
	
	@Override
	public String toString(){
		return function.getDeclarator().getName().toString();
	}

	public IASTFunctionDefinition getFunction() {
		return function;
	}

	public Type getType() {
		return type;
	}

	public int compareTo(GetterSetterInsertEditProvider o) {
		return toString().compareTo(o.toString());
	}
	
	
}
