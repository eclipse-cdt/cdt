/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IValue;

/**
 * Base class for C and C++ enumerators.
 */
public abstract class ASTEnumerator extends ASTNode implements IASTEnumerator, IASTAmbiguityParent {
    private IASTName name;
    private IASTExpression value;
    private IValue integralValue;

    public ASTEnumerator() {
	}

	public ASTEnumerator(IASTName name, IASTExpression value) {
		setName(name);
		setValue(value);
	}

	protected <T extends ASTEnumerator> T copy(T copy, CopyStyle style) {
		copy.setName(name == null ? null : name.copy(style));
		copy.setValue(value == null ? null : value.copy(style));
		return super.copy(copy, style);
	}

	@Override
	public void setName(IASTName name) {
		assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ENUMERATOR_NAME);
		}
    }

    @Override
	public IASTName getName() {
        return name;
    }

    @Override
	public void setValue(IASTExpression expression) {
    	assertNotFrozen();
        this.value = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(ENUMERATOR_VALUE);
		}
    }

    @Override
	public IASTExpression getValue() {
        return value;
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitEnumerators) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        if (name != null && !name.accept(action)) return false;
        if (value != null && !value.accept(action)) return false;
        if (action.shouldVisitEnumerators) {
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
		if (n == name)
			return r_definition;
		
		return r_reference;
	}

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (child == value) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            value = (IASTExpression) other;
        }
    }

	public IValue getIntegralValue() {
		if (integralValue == null) {
			IASTNode parent= getParent();
			if (parent instanceof IASTInternalEnumerationSpecifier) {
				IASTInternalEnumerationSpecifier ies= (IASTInternalEnumerationSpecifier) parent;
				if (ies.startValueComputation()) { // prevents infinite recursions
					createEnumValues((IASTEnumerationSpecifier) parent);
				}
			}		
			if (integralValue == null) {
				integralValue= Value.UNKNOWN;
			}
		}
		return integralValue;
	}

	private void createEnumValues(IASTEnumerationSpecifier parent) {
		IValue previousExplicitValue = null;
		int delta = 0;
		IASTEnumerator[] etors= parent.getEnumerators();
		for (IASTEnumerator etor : etors) {
			IValue val;
			IASTExpression expr= etor.getValue();
			if (expr != null) {
				val= Value.create(expr, Value.MAX_RECURSION_DEPTH);
				previousExplicitValue = val;
				delta = 1;
			} else {
				if (previousExplicitValue != null) {
					val = Value.incrementedValue(previousExplicitValue, delta);
				} else {
					val = Value.create(delta);
				}
				delta++;
			}
			if (etor instanceof ASTEnumerator) {
				((ASTEnumerator) etor).integralValue= val;
			}
		}
	}
}
