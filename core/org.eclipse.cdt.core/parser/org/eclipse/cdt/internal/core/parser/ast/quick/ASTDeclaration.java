/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 */
public abstract class ASTDeclaration implements IASTDeclaration {

	private final IASTScope scope; 
	public ASTDeclaration( IASTScope scope )
	{
		this.scope = scope; 
		if( scope != null && scope instanceof IASTQScope )
			((IASTQScope)scope).addDeclaration(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTDeclaration#getOwnerScope()
	 */
	public IASTScope getOwnerScope() {
		return scope;
	}

}
