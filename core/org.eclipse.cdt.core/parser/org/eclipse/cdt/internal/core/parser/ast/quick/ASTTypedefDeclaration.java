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
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;

/**
 * @author jcamelon
 *
 */
public class ASTTypedefDeclaration extends ASTDeclaration implements IASTTypedefDeclaration
{
    protected static final ASTNotImplementedException NOT_IMPLEMENTED = new ASTNotImplementedException();
	private final char[] name;
    private final IASTAbstractDeclaration mapping;
    private final ASTQualifiedNamedElement qualifiedName; 
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

    /**
     * @param scope
     * @param name
     * @param mapping
     * @param filename
     */
    public ASTTypedefDeclaration(IASTScope scope, char[] name, IASTAbstractDeclaration mapping, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, char[] filename )
    {
        super( scope );
        this.name = name; 
        this.mapping = mapping;
        setStartingOffsetAndLineNumber(startingOffset, startingLine);
        setNameOffset(nameOffset);
        setNameEndOffsetAndLineNumber(nameEndOffset, nameLine);
        qualifiedName = new ASTQualifiedNamedElement( scope, name );
        fn = filename;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypedef#getName()
     */
    public String getName()
    {
        return String.valueOf(name);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTypedef#getAbstractDeclarator()
     */
    public IASTAbstractDeclaration getAbstractDeclarator()
    {
        return mapping;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        return qualifiedName.getFullyQualifiedName();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#accept(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    	try
        {
            requestor.acceptTypedefDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enter(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exit(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration#getFinalTypeSpecifier()
	 */
	public IASTTypeSpecifier getFinalTypeSpecifier() throws ASTNotImplementedException {
		throw NOT_IMPLEMENTED;
	}
	
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset, nameStartOffset, nameEndOffset, nameLineNumber;
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
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
     */
    public final int getNameLineNumber() {
    	return nameLineNumber;
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
     */
    public final int getNameOffset()
    {
    	return nameStartOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public final void setNameOffset(int o)
    {
        nameStartOffset = o;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
     */
    public final int getNameEndOffset()
    {
        return nameEndOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
     */
    public final void setNameEndOffsetAndLineNumber(int offset, int lineNumber)
    {
    	nameEndOffset = offset;
    	nameLineNumber = lineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return name;
    }
}
