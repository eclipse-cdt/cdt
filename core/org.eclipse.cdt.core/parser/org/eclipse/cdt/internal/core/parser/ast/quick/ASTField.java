/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 */
public class ASTField extends ASTVariable implements IASTField
{
    private final ASTAccessVisibility visibility;

    /**
     * @param scope
     * @param name
     * @param isAuto
     * @param initializerClause
     * @param bitfieldExpression
     * @param abstractDeclaration
     * @param isMutable
     * @param isExtern
     * @param isRegister
     * @param isStatic
     */
    public ASTField(IASTScope scope, char[] name, boolean isAuto, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, IASTAbstractDeclaration abstractDeclaration, boolean isMutable, boolean isExtern, boolean isRegister, boolean isStatic, int startingOffset, int startLine, int nameOffset, int nameEndOffset, int nameLine, IASTExpression constructorExpression, ASTAccessVisibility visibility, char [] filename)
    {
        super(
            scope,
            name,
            isAuto,
            initializerClause,
            bitfieldExpression,
            abstractDeclaration,
            isMutable,
            isExtern,
            isRegister,
            isStatic, startingOffset, startLine, nameOffset, nameEndOffset, nameLine, constructorExpression, filename );
		this.visibility = visibility; 
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMember#getVisiblity()
     */
    public ASTAccessVisibility getVisiblity()
    {
        return visibility;
    }
    
	public void acceptElement( ISourceElementRequestor requestor )
	{
		try
        {
            requestor.acceptField( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }	
	}
	
	public void enterScope( ISourceElementRequestor requestor )
	{
	}
	
	public void exitScope( ISourceElementRequestor requestor )
	{
	}
    
}
