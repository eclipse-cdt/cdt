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
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;

/**
 * @author jcamelon
 *
 */
public class ASTInclusion implements IASTInclusion {

	public ASTInclusion( String name, String fileName, boolean local, int startingOffset, int startLine, int nameOffset, int nameEndOffset, int nameLine, int endOffset, int endLine, char [] fn  )
	{
		this.name = name; 
		this.fileName = fileName;
		this.local = local;
		setStartingOffsetAndLineNumber(startingOffset, startLine);
		setNameOffset(nameOffset);
		setNameEndOffsetAndLineNumber(nameEndOffset, nameLine);
		setEndingOffsetAndLineNumber(endOffset, endLine);
		this.filename = fn;
	}

	private int nameEndOffset;
    private final String name, fileName;
	private final boolean local; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInclusion#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInclusion#getFullFileName()
	 */
	public String getFullFileName() {
		return fileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInclusion#isLocal()
	 */
	public boolean isLocal() {
		return local;
	}


	private int startingOffset = 0, nameOffset = 0, endingOffset = 0; 
	
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
	 * @see org.eclipse.cdt.internal.core.parser.ast.IOffsetableElementRW#setStartingOffset(int)
	 */
	public void setStartingOffsetAndLineNumber(int offset, int lineNumber) {
		startingOffset = offset; 
		startingLineNumber = lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IOffsetableElementRW#setEndingOffset(int)
	 */
	public void setEndingOffsetAndLineNumber(int offset, int lineNumber) {
		endingOffset = offset;
		endingLineNumber = lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IOffsetableElementRW#setNameOffset(int)
	 */
	public void setNameOffset(int o) {
		nameOffset = o;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
		try
        {
            requestor.enterInclusion(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    	try
        {
            requestor.exitInclusion(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
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

	private int fileIndex;
	private final char[] filename;
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
		return filename;
	}
}
