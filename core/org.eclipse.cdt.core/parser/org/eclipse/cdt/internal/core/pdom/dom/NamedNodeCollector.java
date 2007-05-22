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
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Visitor to find named nodes in a BTree or below a PDOMNode. Nested nodes are not visited.
 * @since 4.0
 */
public class NamedNodeCollector implements IBTreeVisitor, IPDOMVisitor {
	private final PDOMLinkage linkage;
	private final char[] name;
	private final boolean prefixLookup;
	private final boolean caseSensitive;
	private IProgressMonitor monitor= null;
	private int monitorCheckCounter= 0;
	
	private List nodes = new ArrayList();

	/**
	 * Collects all nodes with given name.
	 */
	public NamedNodeCollector(PDOMLinkage linkage, char[] name) {
		this(linkage, name, false, true);
	}
		
	/**
	 * Collects all nodes with given name, passing the filter. If prefixLookup is set to
	 * <code>true</code> a binding is considered if its name starts with the given prefix.
	 */
	public NamedNodeCollector(PDOMLinkage linkage, char[] name, boolean prefixLookup, boolean caseSensitive) {
		this.name= name;
		this.linkage= linkage;
		this.prefixLookup= prefixLookup;
		this.caseSensitive= caseSensitive;
	}
	
	/**
	 * Allows to cancel a visit. If set a visit may throw an OperationCancelledException.
	 * @since 4.0
	 */
	public void setMonitor(IProgressMonitor pm) {
		monitor= pm;
	}
	
	final public int compare(int record) throws CoreException {
		IString name= PDOMNamedNode.getDBName(linkage.getPDOM(), record);
		return compare(name);
	}

	private int compare(IString rhsName) throws CoreException {
		int cmp;
		if (prefixLookup) {
			cmp= rhsName.comparePrefix(name, false);
			if(caseSensitive) {
				cmp= cmp==0 ? rhsName.comparePrefix(name, true) : cmp;
			}
			return cmp;
		} else {
			cmp= rhsName.compare(name, false);
			if(caseSensitive) {
				cmp= cmp==0 ? rhsName.compare(name, true) : cmp;
			}
		}
		return cmp;
	}
	
	final public boolean visit(int record) throws CoreException {
		if (monitor != null)
			checkCancelled();

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
	 * @throws CoreException 
	 */
	protected boolean addNode(PDOMNamedNode node) throws CoreException {
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
		if (monitor != null)
			checkCancelled();
		
		if (node instanceof PDOMNamedNode) {
			PDOMNamedNode pb= (PDOMNamedNode) node;
			if (compare(pb.getDBName()) == 0) {
				addNode(pb);
			}
		}
		return false;	// don't visit children
	}

	private void checkCancelled() {
		if (++monitorCheckCounter % 0x1000 == 0 && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	final public void leave(IPDOMNode node) throws CoreException {
	}
}
