/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class CPPASTPointer extends ASTNode implements IASTPointer {

    private boolean isConst;

    private boolean isVolatile;

    
    public CPPASTPointer copy() {
		CPPASTPointer copy = new CPPASTPointer();
		copy.isConst = isConst;
		copy.isVolatile = isVolatile;
		copy.setOffsetAndLength(this);
		return copy;
	}
    
    public boolean isConst() {
        return isConst;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public void setConst(boolean value) {
        assertNotFrozen();
        isConst = value;
    }

    public void setVolatile(boolean value) {
        assertNotFrozen();
        isVolatile = value;
    }

    @Override
	public boolean accept(ASTVisitor action) {
        return true;
    }
}
