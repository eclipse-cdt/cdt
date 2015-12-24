/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.lrparser.xlc.ast;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.lrparser.xlc.ast.IXlcCPPASTModifiedArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayModifier;

@SuppressWarnings("restriction")
public class XlcCPPASTModifiedArrayModifier extends CPPASTArrayModifier implements IXlcCPPASTModifiedArrayModifier {

	private boolean isVolatile;
    private boolean isRestrict;
    private boolean isStatic;
    private boolean isConst;
    private boolean varSized;

	public XlcCPPASTModifiedArrayModifier() {
	}

	public XlcCPPASTModifiedArrayModifier(IASTExpression exp) {
		super(exp);
	}

	@Override
	public XlcCPPASTModifiedArrayModifier copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public XlcCPPASTModifiedArrayModifier copy(CopyStyle style) {
		IASTExpression exp = getConstantExpression();
		XlcCPPASTModifiedArrayModifier copy = new XlcCPPASTModifiedArrayModifier(exp == null ? null : exp.copy());
		copy.isVolatile = isVolatile;
		copy.isRestrict = isRestrict;
		copy.isStatic = isStatic;
		copy.isConst = isConst;
		copy.varSized = varSized;
		return copy(copy, style);
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
