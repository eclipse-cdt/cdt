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
package org.eclipse.cdt.core.parser.ast2;

/**
 * @author Doug Schaefer
 */
public interface IASTFunctionDeclaration extends IASTDeclaration {

	/**
	 * @return the type of the return value of the function
	 */
	public IASTType getReturnType();
	
	public void setReturnType(IASTType returnType);

	/**
	 * @return the first parameter to the function
	 */
	public IASTParameterDeclaration getFirstParameterDeclaration();
	
	public void setFirstParameterDeclaration(IASTParameterDeclaration parameterDeclaration);

	/**
	 * @return the function this declaration is declaring,
	 * if available in the current translation unit. Forward declarations
	 * may not be satisfied here.
	 */
	public IASTFunction getFunction();
	
	public void setFunction(IASTFunction function);
	
}
