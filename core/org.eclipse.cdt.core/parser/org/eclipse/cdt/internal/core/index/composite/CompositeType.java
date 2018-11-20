/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * Represents an index-contexting carrying type
 */
public abstract class CompositeType implements IIndexType {
	protected final IType type;
	protected final ICompositesFactory cf;

	protected CompositeType(IType rtype, ICompositesFactory cf) {
		this.type = rtype;
		this.cf = cf;
	}

	@Override
	public boolean isSameType(IType other) {
		return type.isSameType(other);
	}

	@Override
	public Object clone() {
		fail();
		return null;
	}

	public final void setType(IType type) {
		fail();
	}

	protected void fail() {
		throw new CompositingNotImplementedError("Compositing feature (for IType) not implemented"); //$NON-NLS-1$
	}
}
