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

import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;

/**
 * @author jcamelon
 *
 */
public class ASTUsingDirective
	extends ASTDeclaration
	implements IASTUsingDirective {


	public ASTUsingDirective( IASTScope scope, String name )
	{
		super( scope );
		this.namespaceName = name; 
	}
	private final String namespaceName; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTUsingDirective#getNamespaceName()
	 */
	public String getNamespaceName() {
		return namespaceName;
	}

}
