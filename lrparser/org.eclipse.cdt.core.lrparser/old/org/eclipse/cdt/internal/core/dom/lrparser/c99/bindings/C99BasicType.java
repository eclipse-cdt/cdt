/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;

public class C99BasicType implements ICBasicType {

	/* Type flags given in IBasicType */
	private int type;

	private boolean isLong;
	private boolean isShort;
	private boolean isSigned;
	private boolean isUnsigned;
	private boolean isComplex;
	private boolean isImaginary;
	private boolean isLongLong;

	public C99BasicType() {
	}

	public C99BasicType(int type) {
		this.type = type;
	}

	@Override
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public boolean isLong() {
		return isLong;
	}

	public void setLong(boolean isLong) {
		this.isLong = isLong;
	}

	@Override
	public boolean isShort() {
		return isShort;
	}

	public void setShort(boolean isShort) {
		this.isShort = isShort;
	}

	@Override
	public boolean isSigned() {
		return isSigned;
	}

	public void setSigned(boolean isSigned) {
		this.isSigned = isSigned;
	}

	@Override
	public boolean isUnsigned() {
		return isUnsigned;
	}

	public void setUnsigned(boolean isUnsigned) {
		this.isUnsigned = isUnsigned;
	}

	@Override
	public boolean isComplex() {
		return isComplex;
	}

	public void setComplex(boolean isComplex) {
		this.isComplex = isComplex;
	}

	@Override
	public boolean isImaginary() {
		return isImaginary;
	}

	public void setImaginary(boolean isImaginary) {
		this.isImaginary = isImaginary;
	}

	@Override
	public boolean isLongLong() {
		return isLongLong;
	}

	public void setLongLong(boolean isLongLong) {
		this.isLongLong = isLongLong;
	}

	@Override
	@Deprecated
	public IASTExpression getValue() {
		return null;
	}

	@Override
	public boolean isSameType(IType t) {
		if (t == this)
			return true;
		if (!(t instanceof C99BasicType))
			return false;

		C99BasicType bt = (C99BasicType) t;
		return bt.type == this.type && bt.isLong == this.isLong && bt.isShort == this.isShort
				&& bt.isSigned == this.isSigned && bt.isUnsigned == this.isUnsigned && bt.isComplex == this.isComplex
				&& bt.isImaginary == this.isImaginary && bt.isLongLong == this.isLongLong;
	}

	@Override
	public C99BasicType clone() {
		try {
			return (C99BasicType) super.clone();
		} catch (CloneNotSupportedException e) {
			assert false;
			return null;
		}
	}

	@Override
	public Kind getKind() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getModifiers() {
		// TODO Auto-generated method stub
		return 0;
	}

}
