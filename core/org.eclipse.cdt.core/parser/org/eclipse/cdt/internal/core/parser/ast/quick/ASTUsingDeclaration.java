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

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.internal.core.parser.ast.Offsets;

/**
 * @author jcamelon
 *
 */
public class ASTUsingDeclaration
	extends ASTDeclaration
	implements IASTUsingDeclaration {

	private final boolean isTypename; 
	private final String  mappingName; 
	
	public ASTUsingDeclaration( IASTScope scope, boolean isTypeName, String mappingName, int startingOffset, int startingLine, int endingOffset, int endingLine )
	{
		super( scope );
		isTypename = isTypeName;
		this.mappingName = mappingName;
		setStartingOffsetAndLineNumber(startingOffset, startingLine);
		setEndingOffsetAndLineNumber(endingOffset, endingLine);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration#isTypename()
	 */
	public boolean isTypename() {
		return isTypename;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration#usingTypeName()
	 */
	public String usingTypeName() {
		return mappingName;
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
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementStartingOffset()
	 */
	public int getStartingOffset()
	{
		return offsets.getStartingOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getElementEndingOffset()
	 */
	public int getEndingOffset()
	{
		return offsets.getEndingOffset();
	}
	private Offsets offsets = new Offsets();
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#accept(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
		try
        {
            requestor.acceptUsingDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        } 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enter(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exit(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration#getUsingType()
     */
    public IASTDeclaration getUsingType() throws ASTNotImplementedException
    {
    	throw new ASTNotImplementedException();
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
