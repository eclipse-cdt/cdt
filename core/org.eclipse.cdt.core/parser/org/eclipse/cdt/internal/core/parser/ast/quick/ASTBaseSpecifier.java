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

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;

/**
 * @author jcamelon
 *
 */
public class ASTBaseSpecifier implements IASTBaseSpecifier {

	private final ASTAccessVisibility visibility; 
	private final boolean isVirtual; 
	private final IASTClassSpecifier parentClass;
	
	public ASTBaseSpecifier( IASTClassSpecifier classSpec, boolean v, ASTAccessVisibility a )
	{
		parentClass = classSpec; 
		isVirtual = v; 
		visibility = a;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier#getAccess()
	 */
	public ASTAccessVisibility getAccess() {
		return visibility;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier#isVirtual()
	 */
	public boolean isVirtual() {
		return isVirtual;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier#getParent()
	 */
	public IASTClassSpecifier getParent() {
		return parentClass;
	}

}
