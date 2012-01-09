/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

public class CASTPointer extends ASTNode implements ICASTPointer {

    private boolean isRestrict;
    private boolean isVolatile;
    private boolean isConst;

    @Override
	public CASTPointer copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTPointer copy(CopyStyle style) {
		CASTPointer copy = new CASTPointer();
		copy.isRestrict = isRestrict;
		copy.isVolatile = isVolatile;
		copy.isConst = isConst;
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
    
	@Override
	public boolean isRestrict() {
        return isRestrict;
    }

    @Override
	public void setRestrict(boolean value) {
        assertNotFrozen();
        isRestrict = value;
    }

    @Override
	public boolean isConst() {
        return isConst;
    }

    @Override
	public boolean isVolatile() {
        return isVolatile;
    }

    @Override
	public void setConst(boolean value) {
        assertNotFrozen();
        isConst = value;
    }

    @Override
	public void setVolatile(boolean value) {
        assertNotFrozen();
        isVolatile = value;
    }

    @Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitPointerOperators) {
			switch (action.visit(this)) {
    		case ASTVisitor.PROCESS_ABORT : return false;
    		case ASTVisitor.PROCESS_SKIP  : return true;
    		}
			if (action.leave(this) == ASTVisitor.PROCESS_ABORT)
				return false;
    	}
		return true;	    
    }
}
