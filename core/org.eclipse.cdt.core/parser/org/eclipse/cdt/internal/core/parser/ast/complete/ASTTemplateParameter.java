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
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
import org.eclipse.cdt.internal.core.parser.ast.NamedOffsets;
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
	private final NamedOffsets offsets = new NamedOffsets();

	/**
	 * @param symbol
	 * @param defaultValue2
	 * @param parameter2
	 * @param parms2
	 */
	public ASTTemplateParameter(ISymbol sym, IASTTypeId defVal, IASTParameterDeclaration param, List parms, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine ) {
		super( sym );
		symbol = sym;
		defaultValue = defVal;
		parameter = (ASTParameterDeclaration) param;
		this.parms = parms;
		
		setStartingOffsetAndLineNumber(startingOffset, startingLine);
		setEndingOffsetAndLineNumber(endingOffset, endingLine);
		setNameOffset(nameOffset);
		setNameEndOffsetAndLineNumber( nameEndOffset, nameLine );
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
		return symbol.getName();
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
		return symbol.getName();
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
		offsets.setEndingOffsetAndLineNumber( offset, lineNumber );
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
