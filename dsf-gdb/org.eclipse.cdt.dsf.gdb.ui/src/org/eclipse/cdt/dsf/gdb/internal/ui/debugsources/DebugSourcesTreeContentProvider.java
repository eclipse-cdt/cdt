/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Baha El-Kassaby - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.tree.DebugTree;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class DebugSourcesTreeContentProvider implements ITreeContentProvider {
	private boolean flattenFoldersWithNoFiles;
	private boolean showExistingFilesOnly;

	public DebugSourcesTreeContentProvider(boolean flattenFoldersWithNoFiles, boolean showExistingFilesOnly) {
		this.flattenFoldersWithNoFiles = flattenFoldersWithNoFiles;
		this.showExistingFilesOnly = showExistingFilesOnly;
	}

	public void setFlattenFoldersWithNoFiles(boolean flattenFoldersWithNoFiles) {
		this.flattenFoldersWithNoFiles = flattenFoldersWithNoFiles;
	}

	public void setShowExistingFilesOnly(boolean showExistingFilesOnly) {
		this.showExistingFilesOnly = showExistingFilesOnly;
	}

	public boolean isFlattenFoldersWithNoFiles() {
		return flattenFoldersWithNoFiles;
	}

	public boolean isShowExistingFilesOnly() {
		return showExistingFilesOnly;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof DebugTree) {
			@SuppressWarnings("unchecked")
			DebugTree<Comparable<?>> tree = (DebugTree<Comparable<?>>) inputElement;
			Set<DebugTree<Comparable<?>>> children = tree.getChildren();
			if (showExistingFilesOnly) {
				children = children.stream().filter(c -> c.getExists()).collect(Collectors.toSet());
			}
			return children.toArray();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DebugTree) {
			@SuppressWarnings("unchecked")
			DebugTree<Comparable<?>> tree = (DebugTree<Comparable<?>>) parentElement;
			Set<DebugTree<Comparable<?>>> children = tree.getChildren();
			if (showExistingFilesOnly) {
				children = children.stream().filter(c -> c.getExists()).collect(Collectors.toSet());
			}
			if (flattenFoldersWithNoFiles) {
				if (children.size() == 1) {
					DebugTree<Comparable<?>> child = children.iterator().next();
					if (child.getLeafData() == null) {
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
		if (element instanceof DebugTree) {
			@SuppressWarnings("unchecked")
			DebugTree<Comparable<?>> node = (DebugTree<Comparable<?>>) element;
			DebugTree<?> parent = node.getParent();
			if (parent == null) {
				return null;
			}
			if (flattenFoldersWithNoFiles) {
				DebugTree<?> grandParent = parent.getParent();
				if (grandParent != null && grandParent.getChildren().size() == 1) {
					return getParent(parent);
				}
			}
			return parent;
		}
		return null;

	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof DebugTree) {
			return getChildren(element).length > 0;
		}
		return false;
	}

}
