/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Mar 3, 2004
 */
package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;
import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ASTTemplateParameter extends ASTSymbol implements IASTTemplateParameter, IASTOffsetableNamedElement {
	
//	private ParamKind kind;
//	private String identifier;
	//private ISymbol symbol;
	private IASTTypeId defaultValue;
	private ASTParameterDeclaration parameter;
	private List parms;
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}


	/**
	 * @param filename
	 * @param symbol
	 * @param defaultValue2
	 * @param parameter2
	 * @param parms2
	 */
	public ASTTemplateParameter(ISymbol sym, IASTTypeId defVal, IASTParameterDeclaration param, List parms, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, char[] filename ) {
		super( sym );
		symbol = sym;
		defaultValue = defVal;
		parameter = (ASTParameterDeclaration) param;
		this.parms = parms;
		
		setStartingOffsetAndLineNumber(startingOffset, startingLine);
		setEndingOffsetAndLineNumber(endingOffset, endingLine);
		setNameOffset(nameOffset);
		setNameEndOffsetAndLineNumber( nameEndOffset, nameLine );
		fn = filename;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getTemplateParameterKind()
	 */
	public ParamKind getTemplateParameterKind() {
		ITypeInfo.eType type = symbol.getTypeInfo().getTemplateParameterType();
		if( type == ITypeInfo.t_typeName )
			//TODO: difference between class & typename?
			return ParamKind.TYPENAME;
		else if( type == ITypeInfo.t_template )
			return ParamKind.TEMPLATE_LIST;
		else 
			return ParamKind.PARAMETER;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getIdentifier()
	 */
	public String getIdentifier() {
		return String.valueOf(symbol.getName());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getDefaultValueIdExpression()
	 */
	public String getDefaultValueIdExpression() {
		return ( defaultValue != null ) ? defaultValue.toString() : ""; //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameter#getParameterDeclaration()
	 */
	public IASTParameterDeclaration getParameterDeclaration() {
		return parameter;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTTemplateParameterList#getTemplateParameters()
	 */
	public Iterator getTemplateParameters() {
		return parms.iterator();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
		if( defaultValue != null )
			defaultValue.acceptElement( requestor, manager );
		if( parameter != null )
			parameter.acceptElement( requestor, manager );
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void enterScope(ISourceElementRequestor requestor, IReferenceManager manager) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void exitScope(ISourceElementRequestor requestor, IReferenceManager manager) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
	 */
	public String getName() {
		return String.valueOf(symbol.getName());
	}
	public char[] getNameCharArray(){
	    return symbol.getName();
	}
	
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset, nameStartOffset, nameEndOffset, nameLineNumber;
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
    	return nameStartOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public void setNameOffset(int o)
    {
        nameStartOffset = o;
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
}
