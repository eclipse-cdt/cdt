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

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTEnumerator extends ASTSymbol implements IASTEnumerator
{
    
	private final IASTExpression initialValue;
	private final IASTEnumerationSpecifier owner; 
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

    /**
     * @param enumeratorSymbol
     * @param startingOffset
     * @param endingOffset
     * @param initialValue
     * @param filename
     */
    public ASTEnumerator(ISymbol enumeratorSymbol, IASTEnumerationSpecifier owner, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, IASTExpression initialValue, char[] filename)
    {
        super( enumeratorSymbol );
        setStartingOffsetAndLineNumber(startingOffset, startingLine);
        setNameOffset( nameOffset );
        setNameEndOffsetAndLineNumber( nameEndOffset, nameLine );
        setEndingOffsetAndLineNumber( endingOffset, endingLine );
        this.initialValue = initialValue;
        this.owner = owner;
        fn = filename;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTEnumerator#getOwnerEnumerationSpecifier()
     */
    public IASTEnumerationSpecifier getOwnerEnumerationSpecifier()
    {
        return owner;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTEnumerator#getInitialValue()
     */
    public IASTExpression getInitialValue()
    {
        return initialValue;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
     */
    public String getName()
    {
        return String.valueOf(symbol.getName());
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTEnumerator#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences(IReferenceManager referenceManager) {
		if( initialValue != null )
			initialValue.freeReferences(referenceManager);
	}
	
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset, nameEndOffset;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public int getStartingLine() {
    	return startingLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public int getEndingLine() {
    	return endingLineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
     */
    public int getNameLineNumber() {
    	return getStartingLine();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	startingOffset = offset;
    	startingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	endingOffset = offset;
    	endingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public int getStartingOffset()
    {
        return startingOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public int getEndingOffset()
    {
        return endingOffset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
     */
    public int getNameOffset()
    {
    	return getStartingOffset();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public void setNameOffset(int o)
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
    public void setNameEndOffsetAndLineNumber(int offset, int lineNumber)
    {
    	nameEndOffset = offset;
    }
}
