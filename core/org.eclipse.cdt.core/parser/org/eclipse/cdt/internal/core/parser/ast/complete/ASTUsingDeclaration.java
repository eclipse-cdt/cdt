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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.internal.core.parser.ast.Offsets;

/**
 * @author jcamelon
 *
 */
public class ASTUsingDeclaration extends ASTNode implements IASTUsingDeclaration
{
	private final IASTScope ownerScope;
    private final boolean isTypeName;
	private final IASTDeclaration declaration; 
	private Offsets offsets = new Offsets();
	private final ASTReferenceStore delegate; 
	
    /**
     * 
     */
    public ASTUsingDeclaration( IASTScope ownerScope, IASTDeclaration declaration, boolean isTypeName, int startingOffset, int endingOffset, List references )
    {
    	this.ownerScope = ownerScope;
    	this.isTypeName = isTypeName;
    	this.declaration = declaration;
    	setStartingOffset(startingOffset);
    	setEndingOffset(endingOffset);
    	delegate = new ASTReferenceStore( references );
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration#isTypename()
     */
    public boolean isTypename()
    {
        return isTypeName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration#usingTypeName()
     */
    public String usingTypeName()
    {
        return ((IASTOffsetableNamedElement)declaration).getName();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffset(int o)
    {
        offsets.setStartingOffset(o);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffset(int o)
    {
        offsets.setEndingOffset(o);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public int getStartingOffset()
    {
        return offsets.getStartingOffset();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public int getEndingOffset()
    {
        return offsets.getEndingOffset();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTScopedElement#getOwnerScope()
     */
    public IASTScope getOwnerScope()
    {
        return ownerScope;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.acceptUsingDeclaration( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        delegate.processReferences(requestor);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration#getUsingType()
     */
    public IASTDeclaration getUsingType()
    {
        return declaration;
    }
}
