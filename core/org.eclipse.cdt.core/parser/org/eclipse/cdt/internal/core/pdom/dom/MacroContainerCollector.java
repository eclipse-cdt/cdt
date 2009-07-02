/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Visitor to find macros in a BTree.
 * @since 4.0.2
 */
public final class MacroContainerCollector implements IBTreeVisitor {
	private final PDOMLinkage linkage;
	private final char[] name;
	private final boolean prefixLookup;
	private final boolean caseSensitive;
	private IProgressMonitor monitor= null;
	private int monitorCheckCounter= 0;
	
	private List<PDOMMacroContainer> macros = new ArrayList<PDOMMacroContainer>();

		
	/**
	 * Collects all nodes with given name, passing the filter. If prefixLookup is set to
	 * <code>true</code> a binding is considered if its name starts with the given prefix.
	 */
	public MacroContainerCollector(PDOMLinkage linkage, char[] name, boolean prefixLookup, boolean caseSensitive) {
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
	
	final public int compare(long record) throws CoreException {
		if (monitor != null)
			checkCancelled();
		IString rhsName= PDOMNamedNode.getDBName(linkage.getDB(), record);
		return compare(rhsName);
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
			if(caseSensitive) {
				cmp= rhsName.compareCompatibleWithIgnoreCase(name);
			}
			else {
				cmp= rhsName.compare(name, false);
			}
		}
		return cmp;
	}
	
	final public boolean visit(long record) throws CoreException {
		if (monitor != null)
			checkCancelled();

		if (record == 0)
			return true;
		
		macros.add(new PDOMMacroContainer(linkage, record));
		return true; // look for more
	}
	
	final public List<PDOMMacroContainer> getMacroList() {
		return macros;
	}
	
	private void checkCancelled() {
		if (++monitorCheckCounter % 0x1000 == 0 && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}
}
