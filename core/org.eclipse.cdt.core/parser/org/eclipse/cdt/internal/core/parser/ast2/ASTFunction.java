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
import org.eclipse.cdt.core.parser.ast2.IASTStatement;

/**
 * @author Doug Schaefer
 */
public class ASTFunction extends ASTNode implements IASTFunction {

	private IASTFunctionDeclaration declaration;
	private IASTStatement body;

	public IASTFunctionDeclaration getDeclaration() {
		return declaration;
	}
	
	public void setDeclaration(IASTFunctionDeclaration declaration) {
		this.declaration = declaration;
	}
	
	public IASTStatement getBody() {
		return body;
	}
	
}
