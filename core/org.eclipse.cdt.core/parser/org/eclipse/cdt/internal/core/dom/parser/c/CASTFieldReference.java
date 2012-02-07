/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
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
public class CASTFieldReference extends ASTNode
		implements IASTFieldReference, IASTAmbiguityParent, IASTCompletionContext {
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
	
	@Override
	public CASTFieldReference copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTFieldReference copy(CopyStyle style) {
		CASTFieldReference copy = new CASTFieldReference();
		copy.setFieldOwner(owner == null ? null : owner.copy(style));
		copy.setFieldName(name == null ? null : name.copy(style));
		copy.ptr = ptr;
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public IASTExpression getFieldOwner() {
        return owner;
    }

    @Override
	public void setFieldOwner(IASTExpression expression) {
        assertNotFrozen();
        this.owner = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(FIELD_OWNER);
		}
    }

    @Override
	public IASTName getFieldName() {
        return name;
    }

    @Override
	public void setFieldName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(FIELD_NAME);
		}
    }

    @Override
	public boolean isPointerDereference() {
        return ptr;
    }

    @Override
	public void setIsPointerDereference(boolean value) {
        assertNotFrozen();
        ptr = value;
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
      
        if (owner != null && !owner.accept(action)) return false;
        if (name != null && !name.accept(action)) return false;

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
	public int getRoleForName(IASTName n) {
		if (n  == this.name)
			return r_reference;
		return r_unclear;
	}

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (child == owner) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            owner = (IASTExpression) other;
        }
    }
    
    @Override
	public IType getExpressionType() {
        IBinding binding = getFieldName().resolveBinding();
		if (binding instanceof IVariable) {
			return ((IVariable)binding).getType();
		}
    	return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
    }

	@Override
	public boolean isLValue() {
		if (isPointerDereference())
			return true;

		return getFieldOwner().isLValue();
	}

	@Override
	public final ValueCategory getValueCategory() {
		return isLValue() ? ValueCategory.LVALUE : ValueCategory.PRVALUE;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return CVisitor.findBindingsForContentAssist(n, isPrefix);
	}
}
