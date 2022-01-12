/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.cdt.ui.refactoring.CTextFileChange;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

/**
 * @author Emanuel Graf
 */
public class ChangeTreeSet {

	private static final class ChangePositionComparator implements Comparator<CTextFileChange> {
		@Override
		public int compare(CTextFileChange o1, CTextFileChange o2) {
			if (o1.getFile().equals(o2.getFile())) {
				return o2.getEdit().getOffset() - o1.getEdit().getOffset();
			}
			return o2.getFile().hashCode() - o1.getFile().hashCode();
		}
	}

	private final TreeSet<CTextFileChange> changes = new TreeSet<>(new ChangePositionComparator());

	public void add(CTextFileChange change) {
		changes.add(change);
	}

	public CompositeChange getCompositeChange(String name) {
		CompositeChange allChanges = new CompositeChange(name);

		for (Change change : changes) {
			allChanges.add(change);
		}
		return allChanges;
	}

	@Override
	public String toString() {
		return changes.toString();
	}
}
