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
public class ASTScope implements IASTScope {

	IASTDeclaration firstDeclaration;
	IASTScope parentScope; 
	
	public IASTDeclaration getFirstDeclaration() {
		return firstDeclaration;
	}

	public void setFirstDeclaration(IASTDeclaration firstDeclaration) {
		this.firstDeclaration = firstDeclaration; 
	}
	
	public IASTScope getParentScope() {
		return parentScope;
	}

	public void setParentScope(IASTScope parentScope) {
		this.parentScope = parentScope;
	}
	
	protected IASTDeclaration getDeclaration(IASTIdentifier name) {
		for (IASTDeclaration decl = firstDeclaration;
			 decl != null;
			 decl = decl.getNextDeclaration()) {
			if (decl.getName().equals(name))
				return decl;
		}
		
		return null;
	}
	
	public IASTDeclaration findDeclaration(IASTIdentifier name) {
		IASTDeclaration decl = getDeclaration(name);
		if (decl != null)
			return decl;
		if (parentScope != null)
			return parentScope.findDeclaration(name);
		return null;
	}
	
}
