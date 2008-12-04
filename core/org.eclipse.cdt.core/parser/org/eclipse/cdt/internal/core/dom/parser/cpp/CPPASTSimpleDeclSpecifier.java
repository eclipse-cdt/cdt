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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

/**
 * @author jcamelon
 */
public class CPPASTSimpleDeclSpecifier extends CPPASTBaseDeclSpecifier
        implements ICPPASTSimpleDeclSpecifier {

    private int type;
    private boolean isSigned;
    private boolean isUnsigned;
    private boolean isShort;
    private boolean isLong;

    /**
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier
     */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        assertNotFrozen();
        this.type = type;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public boolean isUnsigned() {
        return isUnsigned;
    }

    public boolean isShort() {
        return isShort;
    }

    public boolean isLong() {
        return isLong;
    }

    public void setSigned(boolean value) {
        assertNotFrozen();
        isSigned = value;
    }

    public void setUnsigned(boolean value) {
        assertNotFrozen();
        isUnsigned = value;
    }

    public void setLong(boolean value) {
        assertNotFrozen();
        isLong = value;
    }

    public void setShort(boolean value) {
        assertNotFrozen();
        isShort = value;
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitDeclSpecifiers) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        if (action.shouldVisitDeclSpecifiers) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }
}
