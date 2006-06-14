/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @author jcamelon
 */
public class CASTTypeIdExpression extends CASTNode implements
        IASTTypeIdExpression {

    private int op;
    private IASTTypeId typeId;

    public int getOperator() {
        return op;
    }

    public void setOperator(int value) {
        this.op = value;
    }

    public void setTypeId(IASTTypeId typeId) {
       this.typeId = typeId;
    }

    public IASTTypeId getTypeId() {
        return typeId;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        if( typeId != null ) if( !typeId.accept( action ) ) return false;
        return true;
    }
    
    public IType getExpressionType() {
    	return CVisitor.getExpressionType(this);
    }
    
}
