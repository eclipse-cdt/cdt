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
import org.eclipse.cdt.internal.core.parser.Parser;
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
	private List references;
	private char[] name; 
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

    /**
     * @param filename
     * 
     */
    public ASTUsingDeclaration( IASTScope ownerScope, char[] name, List declarations, boolean isTypeName, int startingOffset, int startingLine, int endingOffset, int endingLine, List references, char[] filename )
    {
    	this.ownerScope = ownerScope;
    	this.isTypeName = isTypeName;
    	this.name = name;
    	this.declarations.addAll( declarations );
    	setStartingOffsetAndLineNumber(startingOffset, startingLine);
    	setEndingOffsetAndLineNumber(endingOffset, endingLine);
    	this.references = references;
    	fn = filename;
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
        return String.valueOf(name);
    }
    public char[] usingTypeNameCharArray(){
        return name;
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
        Parser.processReferences(references, requestor);
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
    
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public final int getStartingLine() {
    	return startingLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public final int getEndingLine() {
    	return endingLineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public final void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	startingOffset = offset;
    	startingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public final void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	endingOffset = offset;
    	endingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public final int getStartingOffset()
    {
        return startingOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public final int getEndingOffset()
    {
        return endingOffset;
    }

}
