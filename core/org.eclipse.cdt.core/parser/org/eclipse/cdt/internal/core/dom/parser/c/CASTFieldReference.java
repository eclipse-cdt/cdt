/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;

/**
 * Field reference in C.
 */
public class CASTFieldReference extends ASTNode implements IASTFieldReference, IASTAmbiguityParent, IASTCompletionContext {

    private IASTExpression owner;
    private IASTName name;
    private boolean ptr;

    public CASTFieldReference() {
	}

    
	public CASTFieldReference(IASTName name, IASTExpression owner) {
		this(name, owner, false);
	}


	public CASTFieldReference(IASTName name, IASTExpression owner, boolean ptr) {
		setFieldOwner(owner);
		setFieldName(name);
		this.ptr = ptr;
	}
	
	public CASTFieldReference copy() {
		CASTFieldReference copy = new CASTFieldReference();
		copy.setFieldOwner(owner == null ? null : owner.copy());
		copy.setFieldName(name == null ? null : name.copy());
		copy.ptr = ptr;
		copy.setOffsetAndLength(this);
		return copy;
	}

	public IASTExpression getFieldOwner() {
        return owner;
    }

    public void setFieldOwner(IASTExpression expression) {
        assertNotFrozen();
        this.owner = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(FIELD_OWNER);
		}
    }

    public IASTName getFieldName() {
        return name;
    }

    public void setFieldName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(FIELD_NAME);
		}
    }

    public boolean isPointerDereference() {
        return ptr;
    }

    public void setIsPointerDereference(boolean value) {
        assertNotFrozen();
        ptr = value;
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
      
        if( owner != null ) if( !owner.accept( action ) ) return false;
        if( name != null )  if( !name.accept( action ) ) return false;

        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	public int getRoleForName(IASTName n ) {
		if( n  == this.name )
			return r_reference;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( child == owner)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            owner = (IASTExpression) other;
        }
    }
    
    public IType getExpressionType() {
        IBinding binding = getFieldName().resolveBinding();
		if (binding instanceof IVariable) {
			return ((IVariable)binding).getType();
		}
    	return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
    }

    
	public boolean isLValue() {
		if (isPointerDereference())
			return true;

		return getFieldOwner().isLValue();
	}

	public final ValueCategory getValueCategory() {
		return isLValue() ? ValueCategory.LVALUE : ValueCategory.PRVALUE;
	}

	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return CVisitor.findBindingsForContentAssist(n, isPrefix);
	}
}
