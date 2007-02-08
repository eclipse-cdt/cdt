/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Visitor to find named nodes in a BTree or below a PDOMNode. Nested nodes are not visited.
 * @since 4.0
 */
public class NamedNodeCollector implements IBTreeVisitor, IPDOMVisitor {
	private final PDOMLinkage linkage;
	private final char[] name;
	private final boolean prefixLookup;
	
	private List nodes = new ArrayList();

	/**
	 * Collects all nodes with given name.
	 */
	public NamedNodeCollector(PDOMLinkage linkage, char[] name) {
		this(linkage, name, false);
	}
		
	/**
	 * Collects all nodes with given name, passing the filter. If prefixLookup is set to
	 * <code>true</code> a binding is considered if its name starts with the given prefix.
	 */
	public NamedNodeCollector(PDOMLinkage linkage, char[] name, boolean prefixLookup) {
		this.name = name;
		this.linkage= linkage;
		this.prefixLookup = prefixLookup;
	}
	
	final public int compare(int record) throws CoreException {
		PDOMNamedNode node = ((PDOMNamedNode)linkage.getNode(record));
		return compare(node);
	}

	private int compare(PDOMNamedNode node) throws CoreException {
		if (prefixLookup) {
			return node.getDBName().comparePrefix(name);
		}
		return node.getDBName().compare(name);
	}
	
	final public boolean visit(int record) throws CoreException {
		if (record == 0)
			return true;
		
		PDOMNode node= linkage.getNode(record);
		if (node instanceof PDOMNamedNode) {
			return addNode((PDOMNamedNode) node);
		}
		return true; // look for more
	}

	/**
	 * Return true to continue the visit.
	 */
	protected boolean addNode(PDOMNamedNode node) {
		nodes.add(node);
		return true; // look for more
	}
	
	final protected List getNodeList() {
		return nodes;
	}
	
	final public PDOMNamedNode[] getNodes() {
		return (PDOMNamedNode[])nodes.toArray(new PDOMNamedNode[nodes.size()]);
	}

	final public boolean visit(IPDOMNode node) throws CoreException {
		if (node instanceof PDOMNamedNode) {
			PDOMNamedNode pb= (PDOMNamedNode) node;
			if (compare(pb) == 0) {
				addNode(pb);
			}
		}
		return false;	// don't visit children
	}

	final public void leave(IPDOMNode node) throws CoreException {
	}
}
