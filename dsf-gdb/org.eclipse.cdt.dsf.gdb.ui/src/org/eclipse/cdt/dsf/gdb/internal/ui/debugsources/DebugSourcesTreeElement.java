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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A basic tree used to display source files in the debug view
  */
public class DebugSourcesTreeElement {

	public enum FileExist {
		YES, NO, UNKNOWN;
	}

	// preserve insertion order with LinkedHashSet
	private final Set<DebugSourcesTreeElement> children = new LinkedHashSet<>();
	private final String name;
	private final String fullPath;
	private DebugSourcesTreeElement parent;
	private FileExist exists;

	/**
	 *
	 * @param name part of the path
	 * @param exists
	 */
	public DebugSourcesTreeElement(String name, FileExist exists) {
		this.name = name;
		this.fullPath = null;
		this.exists = exists;
	}

	/**
	 * If node is a leaf, add the fullPath as well
	 *
	 * @param name part of the path
	 * @param leafData full path of the file
	 */
	private DebugSourcesTreeElement(String name, String fullPath, FileExist exist) {
		this.name = name;
		this.fullPath = fullPath;
		this.exists = exist;
	}

	/**
	 *
	 * @param name part of the path
	 * @param exists
	 * @return a new node if not already existing, existing node otherwise
	 */
	public DebugSourcesTreeElement addNode(String name, FileExist exists) {
		for (DebugSourcesTreeElement child : children) {
			if (child.name.equals(name)) {
				if (exists == FileExist.YES) {
					child.exists = FileExist.YES;
				}
				return child;
			}
		}
		return addChild(new DebugSourcesTreeElement(name, exists));
	}

	/**
	 *
	 * @param name part of the path
	 * @param fullPath of leaf
	 * @return a new leaf if not already existing, existing leaf otherwise
	 */
	public DebugSourcesTreeElement addLeaf(String name, String fullPath, FileExist exists) {
		for (DebugSourcesTreeElement child : children) {
			if (child.name.equals(name)) {
				return child;
			}
		}
		return addChild(new DebugSourcesTreeElement(name, fullPath, exists));
	}

	private DebugSourcesTreeElement addChild(DebugSourcesTreeElement child) {
		children.add(child);
		return child;
	}

	/**
	 *
	 * @return list of children of the node
	 */
	public Set<DebugSourcesTreeElement> getChildren() {
		return children;
	}

	/**
	 *
	 * @param filesThatMayExistOnly only include files that may exist
	 * @return list of children of the node
	 */
	public Set<DebugSourcesTreeElement> getChildren(boolean filesThatMayExistOnly) {
		if (filesThatMayExistOnly) {
			return children.stream().filter(c -> c.getExists() != FileExist.NO).collect(Collectors.toSet());
		} else {
			return children;
		}
	}

	/**
	 *
	 * @return true if node has children, false otherwise
	 */
	public boolean hasChildren() {
		if (children != null && children.size() > 0)
			return true;
		return false;
	}

	/**
	 *
	 * @return name of the file or folder segment
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @return full path to file (or null if not a file)
	 */
	public String getFullPath() {
		return fullPath;
	}

	/**
	 * Return true for leaf data that really exists on disk.
	 *
	 * This can be used to display differently.
	 */
	public FileExist getExists() {
		return exists;
	}

	/**
	 *
	 * @return node parent
	 */
	public DebugSourcesTreeElement getParent() {
		return parent;
	}

	/**
	 * Set parent of node
	 *
	 * @param parent
	 */
	public void setParent(DebugSourcesTreeElement parent) {
		this.parent = parent;
	}

	public void setExist(FileExist exists) {
		this.exists = exists;
	}

	@Override
	public String toString() {
		return Objects.toString(getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((exists == null) ? 0 : exists.hashCode());
		result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DebugSourcesTreeElement other = (DebugSourcesTreeElement) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (exists != other.exists)
			return false;
		if (fullPath == null) {
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

}
