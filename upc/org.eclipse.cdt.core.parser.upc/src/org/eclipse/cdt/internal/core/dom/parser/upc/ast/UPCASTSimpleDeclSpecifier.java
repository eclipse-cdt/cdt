/*******************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;

@SuppressWarnings("restriction")
public class UPCASTSimpleDeclSpecifier extends CASTSimpleDeclSpecifier
		implements IUPCASTSimpleDeclSpecifier {

	private int referenceType;
	private int sharedQualifier;
	private IASTExpression blockSizeExpression;


	public UPCASTSimpleDeclSpecifier() {
	}

	public UPCASTSimpleDeclSpecifier(IASTExpression blockSizeExpression) {
		setBlockSizeExpression(blockSizeExpression);
	}

	@Override
	public UPCASTSimpleDeclSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public UPCASTSimpleDeclSpecifier copy(CopyStyle style) {
		UPCASTSimpleDeclSpecifier copy = new UPCASTSimpleDeclSpecifier();
		copySimpleDeclSpec(copy, style);
		copy.referenceType = referenceType;
		copy.sharedQualifier = sharedQualifier;
		copy.setBlockSizeExpression(blockSizeExpression == null ? null : blockSizeExpression.copy(style));
		if(style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTExpression getBlockSizeExpression() {
		return blockSizeExpression;
	}

	@Override
	public int getReferenceType() {
		return referenceType;
	}

	@Override
	public int getSharedQualifier() {
		return sharedQualifier;
	}

	@Override
	public void setBlockSizeExpression(IASTExpression expr) {
		this.blockSizeExpression = expr;
		if(expr != null) {
			expr.setParent(this);
			expr.setPropertyInParent(BLOCK_SIZE_EXPRESSION);
		}
	}

	@Override
	public void setReferenceType(int referenceType) {
		this.referenceType = referenceType;
	}

	@Override
	public void setSharedQualifier(int shared) {
		this.sharedQualifier = shared;
	}


	@Override
	public boolean accept(ASTVisitor action) {
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( blockSizeExpression != null) if( !blockSizeExpression.accept( action ) ) return false;

        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

}
