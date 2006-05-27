/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;

/**
 * Element class used to group matches.
 *  
 * @author Doug Schaefer
 */
public class PDOMSearchElement {

	private final PDOMBinding binding;
	private final String name;
	private final String filename;
	
	public PDOMSearchElement(PDOMName name) throws CoreException {
		binding = name.getPDOMBinding();
		this.name = binding.getName();
		filename = name.getFile().getFileName().getString();
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

	public PDOMBinding getBinding() {
		return binding;
	}
	
}
