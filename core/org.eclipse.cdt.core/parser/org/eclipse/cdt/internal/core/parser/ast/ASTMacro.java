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
package org.eclipse.cdt.internal.core.parser.ast;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTMacro;

/**
 * @author jcamelon
 *
 */
public class ASTMacro implements IASTMacro {

	private int nameEndOffset = 0;
    private final String name; 
	public ASTMacro( String name, int start, int end, int nameBeg, int nameEnd )
	{
		this.name =name; 
		setStartingOffset(start);
		setNameOffset(nameBeg);
		setNameEndOffset(nameEnd);
		setEndingOffset(end);
	}
	
	private int startingOffset = 0, endingOffset = 0, nameOffset = 0;
	 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTMacro#getName()
	 */
	public String getName() {
		return name;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElementRW#setStartingOffset(int)
	 */
	public void setStartingOffset(int o) {
		startingOffset = o; 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElementRW#setEndingOffset(int)
	 */
	public void setEndingOffset(int o) {
		endingOffset = o; 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElementRW#setNameOffset(int)
	 */
	public void setNameOffset(int o) {
		nameOffset = o; 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElement#getElementStartingOffset()
	 */
	public int getStartingOffset() {
		return startingOffset;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElement#getElementEndingOffset()
	 */
	public int getEndingOffset() {
		return endingOffset;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElement#getElementNameOffset()
	 */
	public int getNameOffset() {
		return nameOffset;
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.acceptMacro( this );
        }
        catch (Exception e)
        {
            /* do nothing */
        }
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
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
	 */
	public int getNameEndOffset()
	{
		return nameEndOffset;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
	 */
	public void setNameEndOffset(int o)
	{
		nameEndOffset = o;
	}
}
