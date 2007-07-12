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
import org.eclipse.search.ui.text.Match;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchMatch extends Match {

	public PDOMSearchMatch(IIndex index, IIndexBinding binding, IIndexName name, int offset, int length) throws CoreException {
		super(new PDOMSearchElement(index, name, binding), offset, length);
	}

	IIndexFileLocation getLocation() {
		return ((PDOMSearchElement)getElement()).getLocation();
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof PDOMSearchMatch))
			return false;
		PDOMSearchMatch other = (PDOMSearchMatch)obj;
		return getElement().equals(other.getElement())
			&& getOffset() == other.getOffset()
			&& getLength() == other.getLength();
	}
	
}
