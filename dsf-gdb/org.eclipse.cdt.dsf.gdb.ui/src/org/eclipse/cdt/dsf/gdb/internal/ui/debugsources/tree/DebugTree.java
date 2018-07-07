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
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.tree;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A basic tree used to display source files in the debug view
 *
 * @param <T>
 */
public class DebugTree<T extends Comparable<?>> {

	public enum FileExist {
		YES,
		NO,
		UNKNOWN;
	}

	// preserve insertion order with LinkedHashSet
	private final Set<DebugTree<T>> children = new LinkedHashSet<DebugTree<T>>();
	private final T data;
	private T leafData;
	private DebugTree<T> parent;
	private FileExist exists;

	/**
	 * 
	 * @param data
	 * @param exists
	 */
	public DebugTree(T data, FileExist exists) {
		this.data = data;
		this.exists = exists;
	}

	/**
	 * If node is a leaf, add the leafData as well
	 * 
	 * @param data
	 * @param leafData
	 */
	private DebugTree(T data, T leafData, FileExist exist) {
		this.data = data;
		this.leafData = leafData;
		this.exists = exist;
	}

	/**
	 * 
	 * @param data of node
	 * @param exists 
	 * @return a new node if not already existing, existing node otherwise
	 */
	public DebugTree<T> addNode(T data, FileExist exists) {
		for (DebugTree<T> child : children) {
			if (child.data.equals(data)) {
				if (exists == FileExist.YES) {
					child.exists = FileExist.YES;
				}
				return child;
			}
		}
		return addChild(new DebugTree<T>(data, exists));
	}

	/**
	 * 
	 * @param data of leaf node
	 * @param leafData of leaf
	 * @return a new leaf if not already existing, existing leaf otherwise
	 */
	public DebugTree<T> addLeaf(T data, T leafData, FileExist exists) {
		for (DebugTree<T> child : children) {
			if (child.data.equals(data)) {
				return child;
			}
		}
		return addChild(new DebugTree<T>(data, leafData, exists));
	}

	private DebugTree<T> addChild(DebugTree<T> child) {
		children.add(child);
		return child;
	}

	/**
	 * 
	 * @return list of children of the node
	 */
	public Set<DebugTree<T>> getChildren() {
		return children;
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
	 * @return node node
	 */
	public T getData() {
		return data;
	}

	/**
	 * 
	 * @return leaf data
	 */
	public T getLeafData() {
		return leafData;
	}

	/**
	 * Return true for leaf data that really exists on disk.
	 * 
	 * This can be used to display differently.
	 */
	public boolean getExists() {
		switch (exists) {
		case YES:
			return true;
		case NO:
			return false;
		case UNKNOWN:
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @return node parent
	 */
	public DebugTree<?> getParent() {
		return parent;
	}

	/**
	 * Set parent of node
	 * 
	 * @param parent
	 */
	public void setParent(DebugTree<T> parent) {
		this.parent = parent;
	}


	public void setExist(boolean exist) {
		exists = exist? FileExist.YES : FileExist.NO;
	}

	@Override
	public String toString() {
		return Objects.toString(getData());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((exists == null) ? 0 : exists.hashCode());
		result = prime * result + ((leafData == null) ? 0 : leafData.hashCode());
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
		DebugTree<?> other = (DebugTree<?>) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (exists != other.exists)
			return false;
		if (leafData == null) {
			if (other.leafData != null)
				return false;
		} else if (!leafData.equals(other.leafData))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

}
