/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
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
