/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.IndexTypeInfo;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;

import org.eclipse.cdt.internal.core.browser.ASTTypeInfo;

/**
 * Represents a a c/c++-entity in a search.
 */
public class TypeInfoSearchElement extends CSearchElement {
	private final ITypeInfo typeInfo;

	public TypeInfoSearchElement(IIndex index, IIndexName name, IIndexBinding binding) throws CoreException {
		super(name.getFile().getLocation());
		this.typeInfo= IndexTypeInfo.create(index, binding);
	}

	public TypeInfoSearchElement(ASTTypeInfo typeInfo) {
		super(typeInfo.getIFL());
		this.typeInfo= typeInfo;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + typeInfo.hashCode()*31;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TypeInfoSearchElement))
			return false;
		TypeInfoSearchElement other= (TypeInfoSearchElement)obj;
		return super.equals(other) && typeInfo.equals(other.typeInfo);
	}

	public final ITypeInfo getTypeInfo() {
		return typeInfo;
	}
}
