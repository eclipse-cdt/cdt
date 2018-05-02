/*******************************************************************************
 * Copyright (c) 2018, 2019 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Baha El-Kassaby - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources;

import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class DebugSourcesTreeContentProvider implements ITreeContentProvider {
	private boolean flattenFoldersWithNoFiles = true;
	private boolean showExistingFilesOnly = true;

	public DebugSourcesTreeContentProvider() {
	}

	public void setFlattenFoldersWithNoFiles(boolean flattenFoldersWithNoFiles) {
		this.flattenFoldersWithNoFiles = flattenFoldersWithNoFiles;
	}

	public boolean isFlattenFoldersWithNoFiles() {
		return flattenFoldersWithNoFiles;
	}

	public void setShowExistingFilesOnly(boolean showExistingFilesOnly) {
		this.showExistingFilesOnly = showExistingFilesOnly;
	}

	public boolean isShowExistingFilesOnly() {
		return showExistingFilesOnly;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof DebugSourcesTreeElement) {
			DebugSourcesTreeElement tree = (DebugSourcesTreeElement) inputElement;
			Set<DebugSourcesTreeElement> children = tree.getChildren(showExistingFilesOnly);
			return children.toArray();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DebugSourcesTreeElement) {
			DebugSourcesTreeElement tree = (DebugSourcesTreeElement) parentElement;
			Set<DebugSourcesTreeElement> children = tree.getChildren(showExistingFilesOnly);

			if (flattenFoldersWithNoFiles) {
				if (children.size() == 1) {
					DebugSourcesTreeElement child = children.iterator().next();
					if (child.getFullPath() == null) {
						return getChildren(child);
					}
				}
			}
			return children.toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element == null)
			return null;
		if (element instanceof DebugSourcesTreeElement) {
			DebugSourcesTreeElement node = (DebugSourcesTreeElement) element;
			DebugSourcesTreeElement parent = node.getParent();
			if (parent == null) {
				return null;
			}
			if (flattenFoldersWithNoFiles) {
				DebugSourcesTreeElement grandParent = parent.getParent();
				if (grandParent != null) {
					Set<DebugSourcesTreeElement> children = grandParent.getChildren(showExistingFilesOnly);
					if (children.size() == 1) {
						return getParent(parent);
					}
				}
			}
			return parent;
		}
		return null;

	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof DebugSourcesTreeElement) {
			return getChildren(element).length > 0;
		}
		return false;
	}

}
