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

import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;

/**
 * @author jcamelon
 */
public class CASTModifiedArrayModifier extends CASTArrayModifier implements
        ICASTArrayModifier {

    private boolean isVolatile;
    private boolean isRestrict;
    private boolean isStatic;
    private boolean isConst;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier#isConst()
     */
    public boolean isConst() {
        return isConst;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier#isStatic()
     */
    public boolean isStatic() {
        return isStatic;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier#isRestrict()
     */
    public boolean isRestrict() {
        return isRestrict;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier#isVolatile()
     */
    public boolean isVolatile() {
        return isVolatile;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier#setConst(boolean)
     */
    public void setConst(boolean value) {
        this.isConst = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier#setVolatile(boolean)
     */
    public void setVolatile(boolean value) {
        this.isVolatile = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier#setRestrict(boolean)
     */
    public void setRestrict(boolean value) {
        this.isRestrict = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier#setStatic(boolean)
     */
    public void setStatic(boolean value) {
        this.isStatic = value;
    }

}
