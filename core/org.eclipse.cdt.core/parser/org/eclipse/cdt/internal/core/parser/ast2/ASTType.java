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

import org.eclipse.cdt.core.parser.ast2.IASTType;
import org.eclipse.cdt.core.parser.ast2.IASTTypeDeclaration;


/**
 * @author Doug Schaefer
 */
public class ASTType extends ASTNode implements IASTType {
	
	private IASTTypeDeclaration declaration;
	
	public IASTTypeDeclaration getDeclaration() {
		return declaration;
	}
	
	public void setDeclaration(IASTTypeDeclaration declaration) {
		this.declaration = declaration;
	}
	
}
