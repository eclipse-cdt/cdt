/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Abstract Option Page for C/C++ Project Paths page for 3.X projects.
 */
public abstract class CPathBasePage extends AbstractCOptionPage {

	public CPathBasePage(String title) {
		super(title);
	}

	public CPathBasePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected void fixNestingConflicts(List<CPElement> newEntries, List<CPElement> existingList,
			Set<CPElement> modifiedSourceEntries) {
		ArrayList<CPElement> existing = new ArrayList<>(existingList);
		for (int i = 0; i < newEntries.size(); i++) {
			CPElement curr = newEntries.get(i);
			addExclusionPatterns(curr, existing, modifiedSourceEntries);
			// add the entry to the existing list so it can be analyse also.
			existing.add(curr);
		}
	}

	private void addExclusionPatterns(CPElement newEntry, List<CPElement> existing, Set<CPElement> modifiedEntries) {
		IPath entryPath = newEntry.getPath();
		for (int i = 0; i < existing.size(); i++) {
			CPElement curr = existing.get(i);
			if (curr.getEntryKind() == IPathEntry.CDT_SOURCE) {
				IPath currPath = curr.getPath();
				if (currPath.isPrefixOf(entryPath) && !currPath.equals(entryPath)) {
					IPath[] exclusionFilters = (IPath[]) curr.getAttribute(CPElement.EXCLUSION);
					if (!CoreModelUtil.isExcludedPath(entryPath.removeFirstSegments(1), exclusionFilters)) {
						IPath pathToExclude = entryPath.removeFirstSegments(currPath.segmentCount())
								.addTrailingSeparator();
						IPath[] newExclusionFilters = new IPath[exclusionFilters.length + 1];
						System.arraycopy(exclusionFilters, 0, newExclusionFilters, 0, exclusionFilters.length);
						newExclusionFilters[exclusionFilters.length] = pathToExclude;
						curr.setAttribute(CPElement.EXCLUSION, newExclusionFilters);
						modifiedEntries.add(curr);
					}
				}
			}
		}
	}

	public abstract List<?> getSelection();

	public abstract void setSelection(List<?> selection);

	public abstract boolean isEntryKind(int kind);

	protected List<CPElement> filterList(List<CPElement> cpelements) {
		ArrayList<CPElement> filtered = new ArrayList<>();

		for (int i = 0; i < cpelements.size(); i++) {
			CPElement cpe = cpelements.get(i);
			if (isEntryKind(cpe.getEntryKind())) {
				filtered.add(cpe);
			}
		}
		return filtered;
	}

}
