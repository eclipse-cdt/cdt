/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTToken;

/**
 * Base class for C and C++ attributes.
 */
public abstract class ASTAttribute extends ASTNode implements IASTAttribute {
    private final char[] name;
    private final IASTToken argumentClause;

	public ASTAttribute(char[] name, IASTToken arguments) {
		this.name = name;
		this.argumentClause = arguments;
	}

    @Override
	public char[] getName() {
        return name;
    }

	@Override
	public IASTToken getArgumentClause() {
		return argumentClause;
	}

	@Override
	public void setArgumentClause(IASTToken argumentClause) {
		assertNotFrozen();
		if (argumentClause != null) {
			argumentClause.setParent(this);
			argumentClause.setPropertyInParent(ARGUMENT_CLAUSE);
		}
	}

	@Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitAttributes) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}

        if (argumentClause != null && !argumentClause.accept(action)) return false;

        if (action.shouldVisitAttributes && action.leave(this) == ASTVisitor.PROCESS_ABORT)
            return false;

        return true;
    }
}
