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
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;

/**
 * @author jcamelon
 *
 */
public class ASTTemplateParameter implements IASTTemplateParameter, IASTOffsetableNamedElement
{
	private final NamedOffsets offsets = new NamedOffsets();
    private final List templateParms;
    private final IASTParameterDeclaration parameter;
    private final ParamKind kind;
    private final String identifier;
    private final String defaultValue;
    /**
     * @param kind
     * @param identifier
     * @param defaultValue
     * @param parameter
     */
    public ASTTemplateParameter(ParamKind kind, String identifier, String defaultValue, IASTParameterDeclaration parameter, List templateParms, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine )
    {
        this.kind = kind; 
        this.identifier = identifier; 
        this.defaultValue = defaultValue; 
        this.parameter = parameter;
        this.templateParms = templateParms;
        setStartingOffsetAndLineNumber( startingOffset, startingLine );
		setEndingOffsetAndLineNumber( endingOffset, endingLine );
		setNameOffset( nameOffset );
		setNameEndOffsetAndLineNumber(nameEndOffset, nameLine);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getTemplateParameterKind()
     */
    public ParamKind getTemplateParameterKind()
    {
        return kind;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getIdentifier()
     */
    public String getIdentifier()
    {
        return identifier;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getDefaultValueIdExpression()
     */
    public String getDefaultValueIdExpression()
    {
        return defaultValue;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameterList#getTemplateParameters()
     */
    public Iterator getTemplateParameters()
    {
        return templateParms.iterator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getParameterDeclaration()
     */
    public IASTParameterDeclaration getParameterDeclaration()
    {
        return parameter;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void enterScope(ISourceElementRequestor requestor) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void exitScope(ISourceElementRequestor requestor) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTNode#lookup(java.lang.String, org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind[], org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	public ILookupResult lookup(String prefix, LookupKind[] lookupKind, IASTNode context) throws LookupError, ASTNotImplementedException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
	 */
	public String getName() {
		return identifier;
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
		offsets.setNameOffset( o );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
	 */
	public int getNameEndOffset() {
		return offsets.getNameEndOffset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffsetAndLineNumber(int, int)
	 */
	public void setNameEndOffsetAndLineNumber(int offset, int lineNumber) {
		offsets.setNameEndOffsetAndLineNumber( offset, lineNumber );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
	 */
	public int getNameLineNumber() {
		return offsets.getNameLineNumber();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffsetAndLineNumber(int, int)
	 */
	public void setStartingOffsetAndLineNumber(int offset, int lineNumber) {
		offsets.setStartingOffsetAndLineNumber( offset, lineNumber );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffsetAndLineNumber(int, int)
	 */
	public void setEndingOffsetAndLineNumber(int offset, int lineNumber) {
		offsets.setEndingOffsetAndLineNumber(offset, lineNumber);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
	 */
	public int getStartingOffset() {
		return offsets.getStartingOffset();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
	 */
	public int getEndingOffset() {
		return offsets.getEndingOffset();
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
