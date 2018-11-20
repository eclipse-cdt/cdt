/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public boolean isConst() {
		return isConst;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public boolean isRestrict() {
		return isRestrict;
	}

	@Override
	public boolean isVolatile() {
		return isVolatile;
	}

	@Override
	public void setConst(boolean value) {
		assertNotFrozen();
		this.isConst = value;
	}

	@Override
	public void setVolatile(boolean value) {
		assertNotFrozen();
		this.isVolatile = value;
	}

	@Override
	public void setRestrict(boolean value) {
		assertNotFrozen();
		this.isRestrict = value;
	}

	@Override
	public void setStatic(boolean value) {
		assertNotFrozen();
		this.isStatic = value;
	}

	@Override
	public boolean isVariableSized() {
		return varSized;
	}

	@Override
	public void setVariableSized(boolean value) {
		assertNotFrozen();
		varSized = value;
	}

}
