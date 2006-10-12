/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
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

import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;

/**
 * Element class used to group matches.
 *  
 * @author Doug Schaefer
 */
public class PDOMSearchElement {

	private final IIndexBinding binding;
	private final String name;
	private final String filename;
	
	public PDOMSearchElement(IIndexName name, IIndexBinding binding) throws CoreException {
		this.binding= binding;
		this.name = binding.getName();
		filename = name.getFileName();
	}
	
	public int hashCode() {
		return name.hashCode() + filename.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof PDOMSearchElement))
			return false;
		if (this == obj)
			return true;
		PDOMSearchElement other = (PDOMSearchElement)obj;
		return name.equals(other.name)
			&& filename.equals(other.filename);
	}

	public String getFileName() {
		return filename;
	}

	public IIndexBinding getBinding() {
		return binding;
	}
	
}
