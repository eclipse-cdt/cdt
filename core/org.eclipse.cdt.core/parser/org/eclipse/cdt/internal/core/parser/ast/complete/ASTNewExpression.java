/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;

/**
 * @author jcamelon
 *
 */
public class ASTNewExpression extends ASTExpression {
	
	private final IASTTypeId typeId;
	private final IASTNewExpressionDescriptor newDescriptor;

	/**
	 * @param kind
	 * @param references
	 * @param newDescriptor
	 * @param typeId
	 */
	public ASTNewExpression(Kind kind, List references, IASTNewExpressionDescriptor newDescriptor, IASTTypeId typeId) {
		super( kind, references );
		this.newDescriptor = newDescriptor;
		this.typeId = typeId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getTypeId()
	 */
	public IASTTypeId getTypeId() {
		return typeId;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getNewExpressionDescriptor()
	 */
	public IASTNewExpressionDescriptor getNewExpressionDescriptor() {
		return newDescriptor;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#processCallbacks(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	protected void processCallbacks(ISourceElementRequestor requestor ) {
		super.processCallbacks(requestor );
		typeId.acceptElement(requestor);
		newDescriptor.acceptElement(requestor);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTExpression#findNewDescriptor(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public IASTExpression findNewDescriptor(ITokenDuple finalDuple) {
		if( ((ASTTypeId)typeId).getTokenDuple().contains( finalDuple ))
			return this;
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
		super.freeReferences();
		typeId.freeReferences( );
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
	
	public ASTExpression findOwnerExpressionForIDExpression(ITokenDuple duple) {
	    ASTTypeId ti = (ASTTypeId) getTypeId();
	    ITokenDuple typeDuple = ti.getTokenDuple();
	    
	    if( typeDuple.equals( duple ) )
			return this;
		// check subduple
		if( typeDuple.contains( duple ) )
			return this;
		
		//else, check the parameters
		ASTExpression ownerExpression = null;
		ASTNewDescriptor nd = (ASTNewDescriptor)getNewExpressionDescriptor();
		List newInitializerExpressions = nd.getNewInitializerExpressionsList();
		int size = newInitializerExpressions.size();
		for( int i = 0; i < size; i++ )
		{
			ASTExpression expressionList = (ASTExpression) newInitializerExpressions.get(i);
			ownerExpression = expressionList.findOwnerExpressionForIDExpression( duple );
			if( ownerExpression != null ){
			    break;
			}
		}
		
		return ownerExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffsetAndLineNumber(int, int)
	 */
	public void setStartingOffsetAndLineNumber(int offset, int lineNumber) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffsetAndLineNumber(int, int)
	 */
	public void setEndingOffsetAndLineNumber(int offset, int lineNumber) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
	 */
	public int getStartingOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
	 */
	public int getEndingOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
	 */
	public int getStartingLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
	 */
	public int getEndingLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		// TODO Auto-generated method stub
		return null;
	}
}
