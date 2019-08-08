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

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;

public class C99Field extends C99Variable implements IField {

	private ICompositeType compositeTypeOwner;

	public C99Field() {
	}

	public C99Field(String name) {
		super(name);
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		return compositeTypeOwner;
	}

	public void setCompositeTypeOwner(ICompositeType compositeTypeOwner) {
		this.compositeTypeOwner = compositeTypeOwner;
	}

	@Override
	public IScope getScope() {
		return compositeTypeOwner.getCompositeScope();
	}

}