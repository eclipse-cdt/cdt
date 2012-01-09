/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;
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
	private final char[] matchChars;
	private final boolean prefixLookup;
	private final IContentAssistMatcher contentAssistMatcher;
	private final boolean caseSensitive;
	private IProgressMonitor monitor= null;
	private int monitorCheckCounter= 0;
	
	private List<PDOMNamedNode> nodes = new ArrayList<PDOMNamedNode>();

	/**
	 * Collects all nodes with given name.
	 */
	public NamedNodeCollector(PDOMLinkage linkage, char[] name) {
		this(linkage, name, false, false, true);
	}

	/**
	 * Collects all nodes with given name, passing the filter.
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
	public NamedNodeCollector(PDOMLinkage linkage, char[] name, boolean prefixLookup,
			boolean contentAssistLookup, boolean caseSensitive) {
		this.linkage= linkage;
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
		if ((contentAssistMatcher == null) || contentAssistMatcher.match(node.getDBName().getChars())) {
			nodes.add(node);
		}
		return true; // look for more
	}
	
	final protected List<PDOMNamedNode> getNodeList() {
		return nodes;
	}
	
	final public PDOMNamedNode[] getNodes() {
		return nodes.toArray(new PDOMNamedNode[nodes.size()]);
	}

	@Override
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

	@Override
	final public void leave(IPDOMNode node) throws CoreException {
	}
}
