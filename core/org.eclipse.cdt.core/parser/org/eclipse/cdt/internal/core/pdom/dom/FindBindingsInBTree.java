/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
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
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

public final class FindBindingsInBTree implements IBTreeVisitor {
	protected final PDOMLinkage linkage;
	protected final char[] name;
	protected final boolean prefixLookup;
	
	
	public int compare(int record) throws CoreException {
		PDOMNamedNode node = ((PDOMNamedNode)linkage.getNode(record));
		IString n = node.getDBName();
		if (prefixLookup && n.getString().startsWith(new String(name))) {
			return 0;
		}
		return n.compare(name);
	}
	
	private List bindings = new ArrayList();
	private final int[] desiredType;

	/**
	 * Matches all types.
	 * 
	 * @param pdom
	 * @param name
	 */
	public FindBindingsInBTree(PDOMLinkage linkage, char[] name) {
		this(linkage, name, null);
	}
	
	/**
	 * Match a specific type.
	 * 
	 * @param pdom
	 * @param name
	 * @param desiredType
	 */
	public FindBindingsInBTree(PDOMLinkage linkage, char[] name, int desiredType) {
		this(linkage, name, new int[] { desiredType });
	}
	
	/**
	 * Match a collection of types.
	 * 
	 * @param pdom
	 * @param name
	 * @param desiredType
	 */
	public FindBindingsInBTree(PDOMLinkage linkage, char[] name, int[] desiredType) {
		this(linkage, name, desiredType, false);
	}
	
	/**
	 * Match a collection of types.
	 * 
	 * @param pdom
	 * @param name
	 * @param desiredType
	 * @param prefixLookup
	 */
	public FindBindingsInBTree(PDOMLinkage linkage, char[] name, int[] desiredType, boolean prefixLookup) {
		this.name = name;
		this.desiredType = desiredType;
		this.linkage= linkage;
		this.prefixLookup = prefixLookup;
	}
	
	public boolean visit(int record) throws CoreException {
		if (record == 0)
			return true;
		
		PDOMBinding tBinding = linkage.getPDOM().getBinding(record);
		if ((!prefixLookup && !tBinding.hasName(name))
				|| (prefixLookup && !CharArrayUtils.equals(
						tBinding.getNameCharArray(),
						0, name.length, name, false)))
			return false;
		
		if (desiredType == null) {
			bindings.add(tBinding);
			return true; // look for more
		} else {
			int nodeType = tBinding.getNodeType();
			for (int i = 0; i < desiredType.length; ++i)
				if (nodeType == desiredType[i]) {
					bindings.add(tBinding);
					return true; // look for more
				}
		}
		
		// wrong type, try again
		return true;
	}
	
	public IBinding[] getBinding() {
		return (IBinding[])bindings.toArray(new IBinding[bindings.size()]);
	}
}
