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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;

/**
 * @author jcamelon
 *
 */
public abstract class ASTAnonymousDeclaration extends ASTNode implements IASTDeclaration
{
	private final IContainerSymbol ownerScope; 
    /**
     * 
     */
    public ASTAnonymousDeclaration( IContainerSymbol ownerScope )
    {
        this.ownerScope = ownerScope;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTScopedElement#getOwnerScope()
     */
    public IASTScope getOwnerScope()
    {
        return (IASTScope)ownerScope.getASTExtension().getPrimaryDeclaration();
    }

}
