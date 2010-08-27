/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public ICompositeType getCompositeType() {
		return struct;
	}
	
	public IBinding getBinding(@SuppressWarnings("unused") char[] name) {
		throw new UnsupportedOperationException();
	}
}
