/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;

/**
 * This class is used by the GNUCPPSourceParser as an intermediate determinant of whether a 
 * BasicTokenDuple represents an IASTConversionFunction or an IASTOperatorFunction. 
 *  
 * @author dsteffle
 */
public class OperatorTokenDuple implements ITokenDuple {

	private ITokenDuple token = null;
	private IASTTypeId typeId = null;
	private boolean isConversionOperator=false;
	
	/**
	 * Simple constructor.  token is wrapped by this class.
	 * @param token
	 */
	public OperatorTokenDuple(ITokenDuple token) {
		this.token=token;
	}

	// below are functions used by GNUCPPSourceParser, see IOperatorTokenDuple
	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.token.IOperatorTokenDuple#isConversionOperator()
     */
	public boolean isConversionOperator() {
		return isConversionOperator;
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.token.IOperatorTokenDuple#setConversionOperator(boolean)
     */
	public void setConversionOperator(boolean isConversionOperator) {
		this.isConversionOperator = isConversionOperator;
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.token.IOperatorTokenDuple#getTypeId()
     */
	public IASTTypeId getTypeId() {
		return typeId;
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.token.IOperatorTokenDuple#
     * isConversionOperator(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
	public void setTypeId(IASTTypeId typeId) {
		this.typeId = typeId;
	}

	// below are ITokenDuple functions
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getFirstToken()
     */
	public IToken getFirstToken() {
		return token.getFirstToken();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getLastToken()
     */
	public IToken getLastToken() {
		return token.getLastToken();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getTemplateIdArgLists()
     */
	public List[] getTemplateIdArgLists() {
		return token.getTemplateIdArgLists();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getLastSegment()
     */
	public ITokenDuple getLastSegment() {
		return token.getLastSegment();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getLeadingSegments()
     */
	public ITokenDuple getLeadingSegments() {
		return token.getLeadingSegments();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getSegmentCount()
     */
	public int getSegmentCount() {
		return token.getSegmentCount();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#iterator()
     */
	public Iterator iterator() {
		return token.iterator();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#toCharArray()
     */
	public char[] toCharArray() {
		return token.toCharArray();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getFilename()
     */
	public char[] getFilename() {
		return token.getFilename();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#isIdentifier()
     */
	public boolean isIdentifier() {
		return token.isIdentifier();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#length()
     */
	public int length() {
		return token.length();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getSubrange(int, int)
     */
	public ITokenDuple getSubrange(int startIndex, int endIndex) {
		return token.getSubrange(startIndex, endIndex);
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getToken(int)
     */
	public IToken getToken(int index) {
		return token.getToken(index);
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getSegments()
     */
	public ITokenDuple[] getSegments() {
		return token.getSegments();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#findLastTokenType(int)
     */
	public int findLastTokenType(int type) {
		return token.findLastTokenType(type);
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getStartOffset()
     */
	public int getStartOffset() {
		return token.getStartOffset();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getEndOffset()
     */
	public int getEndOffset() {
		return token.getEndOffset();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#getLineNumber()
     */
	public int getLineNumber() {
		return token.getLineNumber();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#syntaxOfName()
     */
	public boolean syntaxOfName() {
		return token.syntaxOfName();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#extractNameFromTemplateId()
     */
	public char[] extractNameFromTemplateId() {
		return token.extractNameFromTemplateId();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#contains(org.eclipse.cdt.core.parser.ITokenDuple)
     */
	public boolean contains(ITokenDuple duple) {
		return token.contains(duple);
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#toQualifiedName()
     */
	public String[] toQualifiedName() {
		return token.toQualifiedName();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#freeReferences()
     */
	public void freeReferences() {
		token.freeReferences();
	}
	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ITokenDuple#
     * acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
	public void acceptElement(ISourceElementRequestor requestor) {
		token.acceptElement(requestor);
	}
	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
	public String toString() {
		return token.toString();
	}
}
