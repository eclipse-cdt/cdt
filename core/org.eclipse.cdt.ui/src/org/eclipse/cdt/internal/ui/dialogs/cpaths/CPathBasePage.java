/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;

public abstract class CPathBasePage extends AbstractCOptionPage {

	public CPathBasePage(String title) {
		super(title);
	}

	public CPathBasePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected void fixNestingConflicts(List newEntries, List existingList, Set modifiedSourceEntries) {
		ArrayList existing = new ArrayList(existingList);
		for (int i = 0; i < newEntries.size(); i++) {
			CPElement curr = (CPElement) newEntries.get(i);
			addExclusionPatterns(curr, existing, modifiedSourceEntries);
			// add the entry to the existing list so it can be analyse also.
			existing.add(curr);
		}
	}

	private void addExclusionPatterns(CPElement newEntry, List existing, Set modifiedEntries) {
		IPath entryPath = newEntry.getPath();
		for (int i = 0; i < existing.size(); i++) {
			CPElement curr = (CPElement) existing.get(i);
			if (curr.getEntryKind() == IPathEntry.CDT_SOURCE) {
				IPath currPath = curr.getPath();
				if (currPath.isPrefixOf(entryPath) && !currPath.equals(entryPath)) {
					IPath[] exclusionFilters = (IPath[]) curr.getAttribute(CPElement.EXCLUSION);
					if (!CoreModelUtil.isExcludedPath(entryPath.removeFirstSegments(1), exclusionFilters)) {
						IPath pathToExclude = entryPath.removeFirstSegments(currPath.segmentCount()).addTrailingSeparator();
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

	public abstract List getSelection();

	public abstract void setSelection(List selection);

	public abstract boolean isEntryKind(int kind);

	protected List filterList(List input) {
		ArrayList filtered = new ArrayList();

		List cpelements = input;
		for (int i = 0; i < cpelements.size(); i++) {
			CPElement cpe = (CPElement) cpelements.get(i);
			if (isEntryKind(cpe.getEntryKind())) {
				filtered.add(cpe);
			}
		}
		return filtered;
	}

}