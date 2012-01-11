/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.search.ui.text.Match;

import org.eclipse.cdt.core.index.IIndexFileLocation;

/**
 * Base class for search matches found by various index searches. 
 */
public class CSearchMatch extends Match {
	private boolean fIsPolymorphicCall;

	public CSearchMatch(CSearchElement elem, int offset, int length) {
		super(elem, offset, length);
	}

	IIndexFileLocation getLocation() {
		return ((CSearchElement)getElement()).getLocation();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof CSearchMatch))
			return false;
		CSearchMatch other = (CSearchMatch)obj;
		return getElement().equals(other.getElement())
			&& getOffset() == other.getOffset()
			&& getLength() == other.getLength();
	}

	public void setIsPolymorphicCall() {
		fIsPolymorphicCall= true;
	}
	
	public boolean isPolymorphicCall() {
		return fIsPolymorphicCall;
	}
}
