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


import org.eclipse.cdt.core.parser.IMacroDescriptor;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;

/**
 * @author jcamelon
 *
 */
public class ASTMacro implements IASTMacro {

	private int nameEndOffset = 0;
    private final char[] name;
    private final IMacroDescriptor innerMacro;
	private final char[] fn;
    
	public ASTMacro( char[] name, IMacroDescriptor info, int start, int startLine, int nameBeg, int nameEnd, int nameLine, int end, int endLine, char[] fn )
	{
		this.name =name; 
		setStartingOffsetAndLineNumber(start, startLine);
		setNameOffset(nameBeg);
		setNameEndOffsetAndLineNumber(nameEnd, nameLine);
		setEndingOffsetAndLineNumber(end, endLine);
		innerMacro = info;
		this.fn = fn;
	}
	
	private int startingOffset = 0, endingOffset = 0, nameOffset = 0;
	 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTMacro#getName()
	 */
	public String getName() {
		return String.valueOf(name);
	}
	public char[] getNameCharArray(){
	    return name;
	    
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElementRW#setStartingOffset(int)
	 */
	public void setStartingOffsetAndLineNumber(int offset, int lineNumber) {
		startingOffset = offset;
		startingLineNumber = lineNumber;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElementRW#setEndingOffset(int)
	 */
	public void setEndingOffsetAndLineNumber(int offset, int lineNumber) {
		endingOffset = offset; 
		endingLineNumber = lineNumber;
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
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
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
		nameLineNumber = lineNumber;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getMacroType()
	 */
	public IMacroDescriptor.MacroType getMacroType() {
		return innerMacro.getMacroType();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getParameters()
	 */
	public String[] getParameters() {
		return innerMacro.getParameters();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getTokenizedExpansion()
	 */
	public IToken[] getTokenizedExpansion() {
		return innerMacro.getTokenizedExpansion();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getCompleteSignature()
	 */
	public String getCompleteSignature() {
		return innerMacro.getCompleteSignature();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getExpansionSignature()
	 */
	public String getExpansionSignature() {
		return innerMacro.getExpansionSignature();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#compatible(org.eclipse.cdt.core.parser.IMacroDescriptor)
	 */
	public boolean compatible(IMacroDescriptor descriptor) {
		return innerMacro.compatible(descriptor);
	}
	
	private int startingLineNumber, endingLineNumber, nameLineNumber;
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
		return nameLineNumber;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#isCircular()
	 */
	public boolean isCircular() {
		return innerMacro.isCircular();
	}
	
	private int fileIndex;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFileIndex()
	 */
	public int getFileIndex() {
		return fileIndex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setFileIndex()
	 */
	public void setFileIndex(int index) {
		fileIndex = index;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

}
