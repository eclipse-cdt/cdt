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

import org.eclipse.cdt.core.parser.ast2.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTIdentifier;
import org.eclipse.cdt.core.parser.ast2.IASTScope;

/**
 * @author Doug Schaefer
 */
public class ASTDeclaration extends ASTNode implements IASTDeclaration {
	
	private IASTIdentifier name;
	private IASTDeclaration nextDeclaration;
	private IASTScope scope;

	public IASTIdentifier getName() {
		return name;
	}

	void setName(IASTIdentifier name) {
		this.name = name;
	}
	
	public IASTDeclaration getNextDeclaration() {
		return nextDeclaration;
	}
	
	void setNextDeclaration(IASTDeclaration nextDeclaration) {
		this.nextDeclaration = nextDeclaration;
	}
	
	public IASTScope getScope() {
		return scope;
	}

	void setScope(IASTScope scope) {
		this.scope = scope;
	}

}
