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
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.runtime.CoreException;

/**
 * Element class used to group matches.
 *  
 * @author Doug Schaefer
 */
public class PDOMSearchElement {

	private final PDOMBinding binding;
	private final PDOMFile file;
	
	public PDOMSearchElement(PDOMName name) throws CoreException {
		binding = name.getPDOMBinding();
		file = name.getFile();
	}
	
	public int hashCode() {
		return binding.getRecord() + file.getRecord();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof PDOMSearchElement))
			return false;
		if (this == obj)
			return true;
		PDOMSearchElement other = (PDOMSearchElement)obj;
		return binding.equals(other.binding)
			&& file.equals(other.file);
	}

	public PDOMFile getFile() {
		return file;
	}
	
	public PDOMBinding getBinding() {
		return binding;
	}
	
}