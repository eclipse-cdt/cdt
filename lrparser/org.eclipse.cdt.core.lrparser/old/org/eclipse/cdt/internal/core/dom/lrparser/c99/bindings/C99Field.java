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

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;

public class C99Field extends C99Variable implements IC99Binding, IField, ITypeable {

	private ICompositeType compositeTypeOwner;

	
	public C99Field() {
	}
	
	public C99Field(String name) {
		super(name);
	}

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