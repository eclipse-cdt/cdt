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
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.ast.ASTQualifiedNamedElement;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTElaboratedTypeSpecifier extends ASTSymbol implements IASTElaboratedTypeSpecifier
{
	private List references;
    private final boolean isForwardDeclaration;
    private final ASTClassKind kind;
	private final ASTQualifiedNamedElement qualifiedName;
    private NamedOffsets offsets = new NamedOffsets();
	
    /**
     * @param checkSymbol
     * @param kind
     * @param startingOffset
     * @param endOffset
     */
    public ASTElaboratedTypeSpecifier(ISymbol checkSymbol, ASTClassKind kind, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endOffset, int endingLine, List references, boolean isDecl )
    {
        super( checkSymbol );
        this.kind = kind;
        setStartingOffsetAndLineNumber( startingOffset, startingLine );
        setNameOffset( nameOffset );
        setNameEndOffsetAndLineNumber(nameEndOffset, nameLine);
        setEndingOffsetAndLineNumber( endOffset, endingLine );
        qualifiedName = new ASTQualifiedNamedElement( getOwnerScope(), checkSymbol.getName() );
        isForwardDeclaration = isDecl;
        this.references = references;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier#getName()
     */
    public String getName()
    {
        return getSymbol().getName();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier#getClassKind()
     */
    public ASTClassKind getClassKind()
    {
        return kind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier#isResolved()
     */
    public boolean isResolved()
    {
        return ! getSymbol().isForwardDeclaration();
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
        offsets.setEndingOffsetAndLineNumber( offset, lineNumber );
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
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager)
    {
    	if( isForwardDeclaration )
			try
            {
                requestor.acceptElaboratedForewardDeclaration(this);
            }
            catch (Exception e)
            {
                /* do nothing */
            }
        manager.processReferences( references, requestor );
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
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        return qualifiedName.getFullyQualifiedName();
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
	 */
	public int getNameOffset() {
		return offsets.getNameOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
	 */
	public void setNameOffset(int o) {
		offsets.setNameOffset(o);
	}
	
	public List getReferences() 
	{
		return references;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
	 */
	public int getNameEndOffset()
	{
		return offsets.getNameEndOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
	 */
	public void setNameEndOffsetAndLineNumber(int offset, int lineNumber)
	{
		offsets.setNameEndOffsetAndLineNumber(offset, lineNumber);
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
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
	 */
	public int getNameLineNumber() {
		return offsets.getNameLineNumber();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if( obj == null ) return false;
		if( ! (obj instanceof IASTElaboratedTypeSpecifier )) return false;
		IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier) obj;
		if( elab.getClassKind() != getClassKind() ) return false;
		if( elab.getName() != getName() ) return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		String coded =getName().toString(); 
		return coded.hashCode();
	}
}
