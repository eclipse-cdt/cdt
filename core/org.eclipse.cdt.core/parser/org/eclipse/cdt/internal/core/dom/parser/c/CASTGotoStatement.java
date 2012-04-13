/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Yuan Zhang / Beth Tibbitts (IBM Research)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeOwner;

/**
 * @author jcamelon
 */
public class CASTGotoStatement extends ASTAttributeOwner implements IASTGotoStatement {
    private IASTName name;

    public CASTGotoStatement() {
	}

	public CASTGotoStatement(IASTName name) {
		setName(name);
	}

	@Override
	public CASTGotoStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTGotoStatement copy(CopyStyle style) {
		CASTGotoStatement copy = new CASTGotoStatement(name == null ? null : name.copy(style));
		return copy(copy, style);
	}
	
	@Override
	public IASTName getName() {
        return this.name;
    }

    @Override
    public void setName(IASTName name) {
    	assertNotFrozen();
    	this.name = name;
    	if (name != null) {
    		name.setParent(this);
    		name.setPropertyInParent(NAME);
    	}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitStatements) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}

        if (!acceptByAttributes(action)) return false;
        if (name != null && !name.accept(action)) return false;

        if (action.shouldVisitStatements) {
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
		if (n == name) return r_reference;
		return r_unclear;
	}
}
