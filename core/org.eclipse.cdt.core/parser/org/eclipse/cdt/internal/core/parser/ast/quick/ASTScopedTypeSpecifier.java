/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTScopedTypeSpecifier;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;

/**
 * @author jcamelon
 *
 */
public class ASTScopedTypeSpecifier extends ASTQualifiedNamedElement implements IASTScopedTypeSpecifier
{
    private final IASTScope scope;
    
    public ASTScopedTypeSpecifier( IASTScope scope, char[] name )
    {
    	super( scope, name );
    	this.scope = scope;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTScopedElement#getOwnerScope()
     */
    public IASTScope getOwnerScope()
    {
        return scope;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTNode#lookup(java.lang.String, org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind[], org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	public ILookupResult lookup(
		String prefix,
		LookupKind[] kind,
		IASTNode context, IASTExpression functionParameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
