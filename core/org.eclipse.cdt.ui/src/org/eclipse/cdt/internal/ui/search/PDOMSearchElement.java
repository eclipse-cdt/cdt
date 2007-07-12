/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.IndexTypeInfo;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;

/**
 * Element class used to group matches.
 *  
 * @author Doug Schaefer
 */
public class PDOMSearchElement {

	private final ITypeInfo typeInfo;
	private final IIndexFileLocation location;
	
	public PDOMSearchElement(IIndex index, IIndexName name, IIndexBinding binding) throws CoreException {
		this.typeInfo= IndexTypeInfo.create(index, binding);
		this.location= name.getFile().getLocation();
	}
	
	public int hashCode() {
		return (typeInfo.getCElementType() *31 + typeInfo.getName().hashCode())*31 + location.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof PDOMSearchElement))
			return false;
		if (this == obj)
			return true;
		PDOMSearchElement other = (PDOMSearchElement)obj;
		return typeInfo.getCElementType() == other.typeInfo.getCElementType()
			&& typeInfo.getName().equals(other.typeInfo.getName())
			&& location.equals(other.location);
	}

	public ITypeInfo getTypeInfo() {
		return typeInfo;
	}
	
	IIndexFileLocation getLocation() {
		return location;
	}
}
