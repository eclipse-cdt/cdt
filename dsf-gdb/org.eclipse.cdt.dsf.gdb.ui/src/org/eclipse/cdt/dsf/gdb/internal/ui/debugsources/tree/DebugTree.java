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
import java.util.Set;

/**
 * A basic tree
 *
 * @param <T>
 */
public class DebugTree<T extends Comparable<?>> {

	// preserve insertion order with LinkedHashSet
	private final Set<DebugTree<T>> children = new LinkedHashSet<DebugTree<T>>();
	private final T data;
	private T leafData;
	private DebugTree<T> parent;

	/**
	 * 
	 * @param data
	 */
	public DebugTree(T data) {
		this.data = data;
	}

	/**
	 * If node is a leaf, add the leafData as well
	 * 
	 * @param data
	 * @param leafData
	 */
	private DebugTree(T data, T leafData) {
		this.data = data;
		this.leafData = leafData;
	}

	/**
	 * 
	 * @param data of node
	 * @return a new node if not already existing, existing node otherwise
	 */
	public DebugTree<T> addNode(T data) {
		for (DebugTree<T> child : children) {
			if (child.data.equals(data)) {
				return child;
			}
		}
		return addChild(new DebugTree<T>(data));
	}

	/**
	 * 
	 * @param data of leaf node
	 * @param leafData of leaf
	 * @return a new leaf if not already existing, existing leaf otherwise
	 */
	public DebugTree<T> addLeaf(T data, T leafData) {
		for (DebugTree<T> child : children) {
			if (child.data.equals(data)) {
				return child;
			}
		}
		return addChild(new DebugTree<T>(data, leafData));
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

}
