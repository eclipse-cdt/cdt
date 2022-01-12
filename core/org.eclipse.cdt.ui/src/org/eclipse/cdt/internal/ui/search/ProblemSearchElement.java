/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.index.IIndexFileLocation;

/**
 * Represents a problem in a search.
 */
public class ProblemSearchElement extends CSearchElement {

	private final int fProblemID;
	private final String fDetail;

	public ProblemSearchElement(int problemID, String detail, IIndexFileLocation floc) {
		super(floc);
		fProblemID = problemID;
		fDetail = detail;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fDetail == null) ? 0 : fDetail.hashCode());
		result = prime * result + fProblemID;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProblemSearchElement other = (ProblemSearchElement) obj;
		if (fDetail == null) {
			if (other.fDetail != null)
				return false;
		} else if (!fDetail.equals(other.fDetail))
			return false;
		if (fProblemID != other.fProblemID)
			return false;
		return true;
	}

	public final int getProblemID() {
		return fProblemID;
	}

	public final String getDetail() {
		return fDetail;
	}
}
