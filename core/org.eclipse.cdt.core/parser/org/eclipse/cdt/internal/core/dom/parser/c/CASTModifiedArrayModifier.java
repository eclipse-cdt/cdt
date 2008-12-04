/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;

/**
 * @author jcamelon
 */
public class CASTModifiedArrayModifier extends CASTArrayModifier implements ICASTArrayModifier {

    private boolean isVolatile;
    private boolean isRestrict;
    private boolean isStatic;
    private boolean isConst;
    private boolean varSized;

	public CASTModifiedArrayModifier() {
	}

	public CASTModifiedArrayModifier(IASTExpression exp) {
		super(exp);
	}

	public boolean isConst() {
        return isConst;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isRestrict() {
        return isRestrict;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public void setConst(boolean value) {
        assertNotFrozen();
        this.isConst = value;
    }

    public void setVolatile(boolean value) {
        assertNotFrozen();
        this.isVolatile = value;
    }

    public void setRestrict(boolean value) {
        assertNotFrozen();
        this.isRestrict = value;
    }

    public void setStatic(boolean value) {
        assertNotFrozen();
        this.isStatic = value;
    }

    public boolean isVariableSized() {
        return varSized;
    }

    public void setVariableSized(boolean value) {
        assertNotFrozen();
        varSized = value;
    }
}