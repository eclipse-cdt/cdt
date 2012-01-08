/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Type-id or unary operation on a type-id.
 */
public class CASTTypeIdExpression extends ASTNode implements IASTTypeIdExpression {

    private int op;
    private IASTTypeId typeId;

    public CASTTypeIdExpression() {
	}

	public CASTTypeIdExpression(int op, IASTTypeId typeId) {
		this.op = op;
		setTypeId(typeId);
	}
	
	@Override
	public CASTTypeIdExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTTypeIdExpression copy(CopyStyle style) {
		CASTTypeIdExpression copy = new CASTTypeIdExpression(op, typeId == null ? null
				: typeId.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public int getOperator() {
        return op;
    }

    @Override
	public void setOperator(int value) {
        assertNotFrozen();
        this.op = value;
    }

    @Override
	public void setTypeId(IASTTypeId typeId) {
       assertNotFrozen();
       this.typeId = typeId;
       if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TYPE_ID);
		}
    }

    @Override
	public IASTTypeId getTypeId() {
        return typeId;
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        if( typeId != null ) if( !typeId.accept( action ) ) return false;

        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
    
    @Override
	public IType getExpressionType() {
    	if (getOperator() == op_sizeof) {
			return CVisitor.get_SIZE_T(this);
		}
    	return CVisitor.createType(typeId.getAbstractDeclarator());
    }

	@Override
	public boolean isLValue() {
		return false;
	}
	
	@Override
	public ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}
}
