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

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICPointerType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

@SuppressWarnings("restriction")
public class C99PointerType implements ITypeContainer, ICPointerType {

	private IType type;
	private boolean isConst;
	private boolean isRestrict;
	private boolean isVolatile;

	public C99PointerType() {
	}

	public C99PointerType(IType type) {
		this.type = type;
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public boolean isConst() {
		return isConst;
	}

	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	@Override
	public boolean isRestrict() {
		return isRestrict;
	}

	public void setRestrict(boolean isRestrict) {
		this.isRestrict = isRestrict;
	}

	@Override
	public boolean isVolatile() {
		return isVolatile;
	}

	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	@Override
	public boolean isSameType(IType t) {
		if (t == this)
			return true;

		if (t instanceof ICPointerType) {
			ICPointerType pointerType = (ICPointerType) t;
			if (pointerType.isConst() == isConst && pointerType.isRestrict() == isRestrict
					&& pointerType.isVolatile() == isVolatile) {
				return type.isSameType(pointerType.getType());
			}

		}
		return false;
	}

	@Override
	public C99PointerType clone() {
		try {
			C99PointerType clone = (C99PointerType) super.clone();
			clone.type = (IType) type.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			assert false;
			return null;
		}

	}
}
