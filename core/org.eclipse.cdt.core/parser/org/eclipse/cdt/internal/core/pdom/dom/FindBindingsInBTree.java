/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

public final class FindBindingsInBTree implements IBTreeVisitor {
	private final PDOMLinkage linkage;
	private final char[] name;
	private final boolean prefixLookup;
	private IndexFilter filter;
	
	private List bindings = new ArrayList();

	/**
	 * Collects all bindings with given name.
	 */
	public FindBindingsInBTree(PDOMLinkage linkage, char[] name) {
		this(linkage, name, null, false);
	}
		
	/**
	 * Collects all bindings with given name, passing the filter. If prefixLookup is set to
	 * <code>true</code> a binding is considered if its name starts with the given prefix.
	 */
	public FindBindingsInBTree(PDOMLinkage linkage, char[] name, IndexFilter filter, boolean prefixLookup) {
		this.name = name;
		this.linkage= linkage;
		this.filter= filter;
		this.prefixLookup = prefixLookup;
	}
	
	public int compare(int record) throws CoreException {
		PDOMNamedNode node = ((PDOMNamedNode)linkage.getNode(record));
		if (prefixLookup) {
			return node.getDBName().comparePrefix(name);
		}
		return node.getDBName().compare(name);
	}
	
	public boolean visit(int record) throws CoreException {
		if (record == 0)
			return true;
		
		PDOMBinding tBinding = linkage.getPDOM().getBinding(record);
		if (filter == null || filter.acceptBinding(tBinding)) {
			bindings.add(tBinding);
		} 
		return true; // look for more
	}
	
	public IBinding[] getBindings() {
		return (IBinding[])bindings.toArray(new IBinding[bindings.size()]);
	}
}
