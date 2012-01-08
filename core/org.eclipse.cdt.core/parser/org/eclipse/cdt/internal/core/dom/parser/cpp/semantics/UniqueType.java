/*
 * UniqueType.java
 * Created on 04.10.2010
 *
 * Copyright 2010 Wind River Systems, Inc. All rights reserved.
 */

package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Used for computing the partial ordering of function templates.
 */
class UniqueType implements IType {

	private boolean fForParameterPack;

	public UniqueType(boolean forParameterPack) {
		fForParameterPack= forParameterPack;
	}

	@Override
	public boolean isSameType(IType type) {
		return type == this;
	}

	public boolean isForParameterPack() {
		return fForParameterPack;
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}
}
