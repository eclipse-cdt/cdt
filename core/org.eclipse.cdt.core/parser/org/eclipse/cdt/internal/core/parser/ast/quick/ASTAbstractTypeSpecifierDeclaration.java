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
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;

/**
 * @author jcamelon
 *
 */
public class ASTAbstractTypeSpecifierDeclaration
    extends ASTDeclaration
    implements IASTAbstractTypeSpecifierDeclaration
{
	private final IASTTemplate ownerTemplate;
    private final IASTTypeSpecifier typeSpecifier;
    private final boolean isFriendDeclaration;
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

    /**
     * @param scope
     * @param typeSpecifier
     * @param filename
     */
    public ASTAbstractTypeSpecifierDeclaration(IASTScope scope, IASTTypeSpecifier typeSpecifier, IASTTemplate ownerTemplate, int startingOffset, int endingOffset, int startingLine, int endingLine, boolean isFriend, char[] filename)
    {
        super( ownerTemplate != null ? null : scope  ); 
        this.typeSpecifier = typeSpecifier;
        this.ownerTemplate = ownerTemplate;
        this.isFriendDeclaration = isFriend;
        if( ownerTemplate != null )
        	ownerTemplate.setOwnedDeclaration( this );
		setStartingOffsetAndLineNumber(startingOffset, startingLine);
		setEndingOffsetAndLineNumber(endingOffset, endingLine);
		fn = filename;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration#getTypeSpecifier()
     */
    public IASTTypeSpecifier getTypeSpecifier()
    {
        return typeSpecifier;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplatedDeclaration#getOwnerTemplateDeclaration()
     */
    public IASTTemplate getOwnerTemplateDeclaration()
    {
        return ownerTemplate;
    }
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#accept(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    	try
        {
    		if( isFriendDeclaration() )
    			requestor.acceptFriendDeclaration( this );
    		else
    			requestor.acceptAbstractTypeSpecDeclaration(this);
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
	 * @see org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration#isFriendDeclaration()
	 */
	public boolean isFriendDeclaration() {
		return isFriendDeclaration;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
	 */
	public String getName() {
		if (typeSpecifier instanceof IASTOffsetableNamedElement)
			return ((IASTOffsetableNamedElement)typeSpecifier).getName();
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		if (typeSpecifier instanceof IASTOffsetableNamedElement)
			return ((IASTOffsetableNamedElement)typeSpecifier).getNameCharArray();
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
	 */
	public int getNameOffset() {
		if (typeSpecifier instanceof IASTOffsetableNamedElement)
			return ((IASTOffsetableNamedElement)typeSpecifier).getNameOffset();
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
	 */
	public void setNameOffset(int o) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
	 */
	public int getNameEndOffset() {
		if (typeSpecifier instanceof IASTOffsetableNamedElement)
			return ((IASTOffsetableNamedElement)typeSpecifier).getNameEndOffset();
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffsetAndLineNumber(int, int)
	 */
	public void setNameEndOffsetAndLineNumber(int offset, int lineNumber) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
	 */
	public int getNameLineNumber() {
		if (typeSpecifier instanceof IASTOffsetableNamedElement)
			return ((IASTOffsetableNamedElement)typeSpecifier).getNameLineNumber();
		
		return 0;
	}
    
}
