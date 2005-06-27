/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation */
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
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

    public boolean accept(ASTVisitor visitor) {
        return true;
    }

}
