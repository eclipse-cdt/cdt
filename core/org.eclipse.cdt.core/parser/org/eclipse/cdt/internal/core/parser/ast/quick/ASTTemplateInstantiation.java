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

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;

/**
 * @author jcamelon
 *
 */
public class ASTTemplateInstantiation extends ASTDeclaration implements IASTTemplateInstantiation
{
    private IASTDeclaration declaration;
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}


    /**
     * @param scope
     * @param filename
     */
    public ASTTemplateInstantiation(IASTScope scope, int startingOffset, int startingLine, char[] filename)
    {
        super( scope );
        setStartingOffsetAndLineNumber(startingOffset, startingLine);
        fn = filename;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplate#getOwnedDeclaration()
     */
    public IASTDeclaration getOwnedDeclaration()
    {
        return declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplate#setOwnedDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
     */
    public void setOwnedDeclaration(IASTDeclaration declaration)
    {
        this.declaration = declaration;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#accept(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enter(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.enterTemplateInstantiation(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exit(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.exitTemplateExplicitInstantiation(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation#getInstantiatedTemplate()
	 */
	public IASTTemplateDeclaration getInstantiatedTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTScope#getDeclarations()
	 */
	public Iterator getDeclarations() throws ASTNotImplementedException {
		// TODO Auto-generated method stub
		return null;
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
