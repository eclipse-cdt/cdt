/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

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
	private OverloadableOperator op = null;
	
	/**
	 * Simple constructor.  token is wrapped by this class.
	 * @param token
	 */
	public OperatorTokenDuple(ITokenDuple token, OverloadableOperator op) {
		this.token=token;
		this.op = op;
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

	public OverloadableOperator getOperator() {
		return op;
	}
	
	// below are ITokenDuple functions
	public IToken getFirstToken() {
		return token.getFirstToken();
	}

	public IToken getLastToken() {
		return token.getLastToken();
	}

	public List<IASTNode>[] getTemplateIdArgLists() {
		return token.getTemplateIdArgLists();
	}

	public ITokenDuple getLastSegment() {
		return token.getLastSegment();
	}

	public ITokenDuple getLeadingSegments() {
		return token.getLeadingSegments();
	}

	public int getSegmentCount() {
		return token.getSegmentCount();
	}

	public Iterator<IToken> iterator() {
		return token.iterator();
	}

	public char[] toCharArray() {
		return token.toCharArray();
	}

	public int length() {
		return token.length();
	}

	public IToken getToken(int index) {
		return token.getToken(index);
	}

	public ITokenDuple[] getSegments() {
		return token.getSegments();
	}

	public int getStartOffset() {
		return token.getStartOffset();
	}

	public int getEndOffset() {
		return token.getEndOffset();
	}

	public char[] extractNameFromTemplateId() {
		return token.extractNameFromTemplateId();
	}

	@Override
	public String toString() {
		return token.toString();
	}
}
