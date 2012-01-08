/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn (Wind River Systems)
 *    Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;
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
	private final char[] matchChars;
	private final boolean prefixLookup;
	private final IContentAssistMatcher contentAssistMatcher;
	private final boolean caseSensitive;
	private IProgressMonitor monitor= null;
	private int monitorCheckCounter= 0;
	
	private List<PDOMMacroContainer> macros = new ArrayList<PDOMMacroContainer>();


	/**
	 * Collects all nodes with given name, passing the filter. If prefixLookup is set to
	 * <code>true</code> a binding is considered if its name starts with the given prefix.
	 * 
	 * @param linkage
	 * @param name
	 * @param prefixLookup
	 *            If set to <code>true</code> a binding is considered if its name starts with the given prefix
	 *            Otherwise, the binding will only be considered if its name matches exactly. This parameter
	 *            is ignored if <code>contentAssistLookup</code> is true.
	 * @param contentAssistLookup
	 *            If set to <code>true</code> a binding is considered if its names matches according to the
	 *            current content assist matching rules.
	 * @param caseSensitive
	 *            Ignored if <code>contentAssistLookup</code> is true.
	 */
	public MacroContainerCollector(PDOMLinkage linkage, char[] name, boolean prefixLookup,
			boolean contentAssistLookup, boolean caseSensitive) {
		if (contentAssistLookup) {
			IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(name); 
			this.contentAssistMatcher =  matcher.matchRequiredAfterBinarySearch() ? matcher : null;
			this.matchChars = matcher.getPrefixForBinarySearch();
			this.prefixLookup= true;
			this.caseSensitive= false;
		} else {
			this.contentAssistMatcher = null;
			this.matchChars = name;
			this.prefixLookup= prefixLookup;
			this.caseSensitive= caseSensitive;
		}
		this.linkage= linkage;
	}
	
	/**
	 * Allows to cancel a visit. If set a visit may throw an OperationCancelledException.
	 * @since 4.0
	 */
	public void setMonitor(IProgressMonitor pm) {
		monitor= pm;
	}
	
	@Override
	final public int compare(long record) throws CoreException {
		if (monitor != null)
			checkCancelled();
		IString rhsName= PDOMNamedNode.getDBName(linkage.getDB(), record);
		return compare(rhsName);
	}

	private int compare(IString rhsName) throws CoreException {
		int cmp;
		if (prefixLookup) {
			cmp= rhsName.comparePrefix(matchChars, false);
			if(caseSensitive) {
				cmp= cmp==0 ? rhsName.comparePrefix(matchChars, true) : cmp;
			}
		} else {
			if(caseSensitive) {
				cmp= rhsName.compareCompatibleWithIgnoreCase(matchChars);
			}
			else {
				cmp= rhsName.compare(matchChars, false);
			}
		}
		return cmp;
	}
	
	@Override
	final public boolean visit(long record) throws CoreException {
		if (monitor != null)
			checkCancelled();

		if (record == 0)
			return true;

		if (contentAssistMatcher != null) {
			char[] nodeName = PDOMNamedNode.getDBName(linkage.getDB(), record).getChars();
			if (contentAssistMatcher.match(nodeName)) {
				macros.add(new PDOMMacroContainer(linkage, record));
			}
		} else { 
			macros.add(new PDOMMacroContainer(linkage, record));
		}

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
