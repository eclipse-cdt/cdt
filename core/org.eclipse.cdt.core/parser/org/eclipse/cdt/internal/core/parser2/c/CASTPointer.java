/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2.c;

import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;

/**
 * @author jcamelon
 */
public class CASTPointer extends CASTNode implements ICASTPointer {

    private boolean isRestrict;
    private boolean isVolatile;
    private boolean isConst;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTPointer#isRestrict()
     */
    public boolean isRestrict() {
        return isRestrict;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTPointer#setRestrict(boolean)
     */
    public void setRestrict(boolean value) {
        isRestrict = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTPointer#isConst()
     */
    public boolean isConst() {
        return isConst;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTPointer#isVolatile()
     */
    public boolean isVolatile() {
        return isVolatile;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTPointer#setConst(boolean)
     */
    public void setConst(boolean value) {
        isConst = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTPointer#setVolatile(boolean)
     */
    public void setVolatile(boolean value) {
        isVolatile = value;
    }

}
