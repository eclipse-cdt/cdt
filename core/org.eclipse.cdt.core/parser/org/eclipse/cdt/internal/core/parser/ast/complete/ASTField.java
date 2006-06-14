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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTField extends ASTVariable implements IASTField
{
    private final ASTAccessVisibility visibility;
    /**
     * @param newSymbol
     * @param abstractDeclaration
     * @param initializerClause
     * @param bitfieldExpression
     * @param startingOffset
     * @param nameOffset
     * @param references
     * @param visibility
     * @param filename
     */
    public ASTField(ISymbol newSymbol, IASTAbstractDeclaration abstractDeclaration, IASTInitializerClause initializerClause, IASTExpression bitfieldExpression, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, List references, boolean previouslyDeclared, IASTExpression constructorExpression, ASTAccessVisibility visibility, char [] filename)
    {
        super( newSymbol, abstractDeclaration, initializerClause, bitfieldExpression, startingOffset, startingLine, nameOffset, nameEndOffset, nameLine, references, constructorExpression, previouslyDeclared, filename );
        this.visibility = visibility;  
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTMember#getVisiblity()
     */
    public ASTAccessVisibility getVisiblity()
    {
        return visibility;
    }
    
	public void acceptElement(ISourceElementRequestor requestor)
	{
		try
        {
            requestor.acceptField(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
		Parser.processReferences(references, requestor);
		references = null;
		
		if( getInitializerClause() != null )
			getInitializerClause().acceptElement(requestor);
		if( getAbstractDeclaration() != null )
			getAbstractDeclaration().acceptElement(requestor);			
	}
}
