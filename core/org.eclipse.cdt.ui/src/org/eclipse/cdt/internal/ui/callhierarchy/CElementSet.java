/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.viewsupport.WorkingSetFilterUI;

public class CElementSet {
	private Set<ICElement> fSet = new LinkedHashSet<>();
	private int fHashCode;

	CElementSet(ICElement[] elements) {
		fSet.addAll(Arrays.asList(elements));
		fHashCode = 0;
		for (int i = 0; i < elements.length; i++) {
			fHashCode = 31 * fHashCode + elements[i].hashCode();
		}
	}

	@Override
	public int hashCode() {
		return fHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CElementSet other = (CElementSet) obj;
		if (fHashCode != other.fHashCode) {
			return false;
		}
		if (fSet == null) {
			if (other.fSet != null) {
				return false;
			}
		} else {
			if (fSet.size() != other.fSet.size()) {
				return false;
			}
			for (Iterator<ICElement> iter = fSet.iterator(); iter.hasNext();) {
				if (!other.fSet.contains(iter.next())) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isEmpty() {
		return fSet.isEmpty();
	}

	public ICElement[] getElements(WorkingSetFilterUI filter) {
		ArrayList<ICElement> result = new ArrayList<>(fSet.size());
		for (Iterator<ICElement> iter = fSet.iterator(); iter.hasNext();) {
			ICElement element = iter.next();
			if (filter == null || filter.isPartOfWorkingSet(element)) {
				result.add(element);
			}
		}
		return result.toArray(new ICElement[result.size()]);
	}
}