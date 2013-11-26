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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;

/**
 * Compound literals for c and c++.
 */
public abstract class ASTTypeIdInitializerExpression extends ASTNode implements IASTTypeIdInitializerExpression {
    private IASTTypeId typeId;
    private IASTInitializer initializer;

    public ASTTypeIdInitializerExpression() {
	}

	public ASTTypeIdInitializerExpression(IASTTypeId t, IASTInitializer i) {
		setTypeId(t);
		setInitializer(i);
	}

	protected void initializeCopy(ASTTypeIdInitializerExpression copy, CopyStyle style) {
		copy.setTypeId(typeId == null ? null : typeId.copy(style));
		copy.setInitializer(initializer == null ? null : initializer.copy(style));
		copy(copy, style);
	}
	
	@Override
	public IASTTypeId getTypeId() {
        return typeId;
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
	public IASTInitializer getInitializer() {
        return initializer;
    }

    @Override
	public void setInitializer(IASTInitializer initializer) {
        assertNotFrozen();
        this.initializer = initializer;
        if (initializer != null) {
			initializer.setParent(this);
			initializer.setPropertyInParent(INITIALIZER);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch(action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        
        if (typeId != null && !typeId.accept(action)) return false;
        if (initializer != null && !initializer.accept(action)) return false;

        if (action.shouldVisitExpressions) {
		    switch(action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

	@Override
	public final boolean isLValue() {
		return false;
	}
	
	@Override
	public ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}
}
