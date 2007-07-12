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
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.IndexTypeInfo;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;

/**
 * Element class used to group matches.
 *  
 * @author Doug Schaefer
 */
public class PDOMSearchElement {

	private final ITypeInfo typeInfo;
	private final String filename;
	
	public PDOMSearchElement(IIndex index, IIndexName name, IIndexBinding binding) throws CoreException {
		this.typeInfo= IndexTypeInfo.create(index, binding);
		filename = new Path(name.getFileLocation().getFileName()).toOSString();
	}
	
	public int hashCode() {
		return (typeInfo.getCElementType() *31 + typeInfo.getName().hashCode())*31 + filename.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof PDOMSearchElement))
			return false;
		if (this == obj)
			return true;
		PDOMSearchElement other = (PDOMSearchElement)obj;
		return typeInfo.getCElementType() == other.typeInfo.getCElementType()
			&& typeInfo.getName().equals(other.typeInfo.getName())
			&& filename.equals(other.filename);
	}

	public ITypeInfo getTypeInfo() {
		return typeInfo;
	}
	
	public String getFileName() {
		return filename;
	}
}
