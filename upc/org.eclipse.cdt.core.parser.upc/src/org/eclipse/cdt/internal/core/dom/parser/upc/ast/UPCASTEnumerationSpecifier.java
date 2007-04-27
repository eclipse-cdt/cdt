/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTEnumerationSpecifier;

public class UPCASTEnumerationSpecifier extends CASTEnumerationSpecifier
		implements IUPCASTEnumerationSpecifier {

	private int referenceType;
	private int sharedQualifier;
	private IASTExpression blockSizeExpression;
	
	
	public IASTExpression getBlockSizeExpression() {
		return blockSizeExpression;
	}

	public int getReferenceType() {
		return referenceType;
	}

	public int getSharedQualifier() {
		return sharedQualifier;
	}

	public void setBlockSizeExpression(IASTExpression expr) {
		this.blockSizeExpression = expr;
	}

	public void setReferenceType(int referenceType) {
		this.referenceType = referenceType;
	}

	public void setSharedQualifier(int shared) {
		this.sharedQualifier = shared;
	}

	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( getName() != null ) if( !getName().accept( action ) ) return false;
        if( blockSizeExpression != null) if( !blockSizeExpression.accept( action ) ) return false;    
        
        IASTEnumerator[] etors = getEnumerators();
        for ( int i = 0; i < etors.length; i++ ) {
            if( !etors[i].accept( action ) ) return false;
        }
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
