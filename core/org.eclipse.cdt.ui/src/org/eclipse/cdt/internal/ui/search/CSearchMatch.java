/*******************************************************************************
 * Copyright (c) 2006, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.search.ui.text.Match;

/**
 * Base class for search matches found by various index searches.
 */
public class CSearchMatch extends Match {
	private boolean fIsPolymorphicCall;
	private boolean fIsWriteAccess;

	public CSearchMatch(CSearchElement elem, int offset, int length) {
		super(elem, offset, length);
	}

	IIndexFileLocation getLocation() {
		return ((CSearchElement) getElement()).getLocation();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof CSearchMatch))
			return false;
		CSearchMatch other = (CSearchMatch) obj;
		return getElement().equals(other.getElement()) && getOffset() == other.getOffset()
				&& getLength() == other.getLength();
	}

	public void setIsPolymorphicCall() {
		fIsPolymorphicCall = true;
	}

	public boolean isPolymorphicCall() {
		return fIsPolymorphicCall;
	}

	public void setIsWriteAccess() {
		fIsWriteAccess = true;
	}

	boolean isWriteAccess() {
		return fIsWriteAccess;
	}
}
