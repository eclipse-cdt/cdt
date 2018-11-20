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

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;

public class C99CompositeTypeScope extends C99Scope implements ICCompositeTypeScope {

	private ICompositeType struct;

	public C99CompositeTypeScope(ICompositeType struct) {
		super(EScopeKind.eClassType);
		this.struct = struct;
	}

	@Override
	public ICompositeType getCompositeType() {
		return struct;
	}

	@Override
	public IBinding getBinding(@SuppressWarnings("unused") char[] name) {
		throw new UnsupportedOperationException();
	}
}
