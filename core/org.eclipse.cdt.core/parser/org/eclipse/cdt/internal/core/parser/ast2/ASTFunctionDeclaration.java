/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast2;

import org.eclipse.cdt.core.parser.ast2.IASTFunction;
import org.eclipse.cdt.core.parser.ast2.IASTFunctionDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTType;

/**
 * @author Doug Schaefer
 */
public class ASTFunctionDeclaration extends ASTDeclaration implements IASTFunctionDeclaration {

	private IASTType returnType;
	private IASTParameterDeclaration firstParameterDeclaration;
	private IASTFunction function;

	public IASTType getReturnType() {
		return returnType;
	}
	
	public void setReturnType(IASTType returnType) {
		this.returnType = returnType;
	}
	
	public IASTParameterDeclaration getFirstParameterDeclaration() {
		return firstParameterDeclaration;
	}
	
	public void setFirstParameterDeclaration(IASTParameterDeclaration parameterDeclaration) {
		firstParameterDeclaration = parameterDeclaration;
	}

	public IASTFunction getFunction() {
		return function;
	}
	
	public void setFunction(IASTFunction function) {
		this.function = function;
	}
	
}
