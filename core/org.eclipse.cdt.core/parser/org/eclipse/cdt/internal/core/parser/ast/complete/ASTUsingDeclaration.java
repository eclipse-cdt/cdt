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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.internal.core.parser.ast.Offsets;
import org.eclipse.cdt.internal.core.parser.ast.SymbolIterator;

/**
 * @author jcamelon
 *
 */
public class ASTUsingDeclaration extends ASTNode implements IASTUsingDeclaration
{
	private final IASTScope ownerScope;
    private final boolean isTypeName;
	private final List declarations = new ArrayList(); 
	private Offsets offsets = new Offsets();
	private List references;
	private String name; 
	
    /**
     * 
     */
    public ASTUsingDeclaration( IASTScope ownerScope, String name, List declarations, boolean isTypeName, int startingOffset, int startingLine, int endingOffset, int endingLine, List references )
    {
    	this.ownerScope = ownerScope;
    	this.isTypeName = isTypeName;
    	this.name = name;
    	this.declarations.addAll( declarations );
    	setStartingOffsetAndLineNumber(startingOffset, startingLine);
    	setEndingOffsetAndLineNumber(endingOffset, endingLine);
    	this.references = references;
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
        return name;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
        offsets.setStartingOffsetAndLineNumber(offset, lineNumber);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
        offsets.setEndingOffsetAndLineNumber(offset, lineNumber);
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
        ASTReferenceStore.processReferences(references, requestor);
        references = null;
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
    public Iterator getUsingTypes()
    {
        return new SymbolIterator( declarations.iterator() );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public int getStartingLine() {
    	return offsets.getStartingLine();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public int getEndingLine() {
    	return offsets.getEndingLine();
    }
}
