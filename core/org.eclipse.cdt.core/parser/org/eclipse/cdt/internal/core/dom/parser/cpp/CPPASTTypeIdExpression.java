/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

public class CPPASTTypeIdExpression extends ASTNode implements ICPPASTTypeIdExpression {
    private int op;
    private IASTTypeId typeId;

    public CPPASTTypeIdExpression() {
	}

	public CPPASTTypeIdExpression(int op, IASTTypeId typeId) {
		this.op = op;
		setTypeId(typeId);
	}

	@Override
	public CPPASTTypeIdExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}
	
	@Override
	public CPPASTTypeIdExpression copy(CopyStyle style) {
		CPPASTTypeIdExpression copy = new CPPASTTypeIdExpression(op, typeId == null ? null
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
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
      
        if (typeId != null && !typeId.accept(action)) return false;
        
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }
    
	@Override
	public IType getExpressionType() {
		switch (getOperator()) {
		case op_sizeof:
		case op_alignof:
			return CPPVisitor.get_SIZE_T(this);
		case op_typeid:
			return CPPVisitor.get_type_info(this);
		case op_has_nothrow_copy:
		case op_has_nothrow_constructor:
		case op_has_trivial_assign:
		case op_has_trivial_constructor:
		case op_has_trivial_copy:
		case op_has_trivial_destructor:
		case op_has_virtual_destructor:
		case op_is_abstract:
		case op_is_class:
		case op_is_empty:
		case op_is_enum:
		case op_is_pod:
		case op_is_polymorphic:
		case op_is_union:
			return CPPBasicType.BOOLEAN;
		}
		return CPPVisitor.createType(getTypeId());
	}

	@Override
	public boolean isLValue() {
		switch (getOperator()) {
		case op_typeid:
			return true;
		}
		return false;
	}

	@Override
	public ValueCategory getValueCategory() {
		return isLValue() ? LVALUE : PRVALUE;
	}
}
