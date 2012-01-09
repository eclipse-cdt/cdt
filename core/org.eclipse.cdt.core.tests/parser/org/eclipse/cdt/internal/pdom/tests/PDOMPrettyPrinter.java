/*******************************************************************************
 * Copyright (c) 2006, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Symbian - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Dump the contents of the PDOM index to stdout (for when you need
 * a lo-fidelity debugging tool)
 */
public class PDOMPrettyPrinter implements IPDOMVisitor {
	StringBuilder indent = new StringBuilder();
	final String step = "   "; //$NON-NLS-1$

	@Override
	public void leave(IPDOMNode node) throws CoreException {
//		if (node instanceof PDOMCPPClassTemplate) {
//			((PDOMCPPClassTemplate) node).specializationsAccept(this);
//		}
		if (indent.length() >= step.length())
			indent.setLength(indent.length() - step.length());
	}

	@Override
	public boolean visit(IPDOMNode node) throws CoreException {
		indent.append(step);
		StringBuilder sb= new StringBuilder();
		sb.append(indent);
		sb.append(node);
		if (node instanceof PDOMBinding) {
			sb.append("  ");
			PDOMBinding binding= (PDOMBinding) node;
			sb.append(" " + binding.getRecord());
		}
		System.out.println(sb);
		return true;
	}

	/**
	 * Dumps the contents of the specified linkage for all primary fragments of the specified index
	 * to standard out, including file local scopes.
	 * @param index
	 * @param linkageID
	 */
	public static void dumpLinkage(IIndex index, final int linkageID) {
		final IPDOMVisitor v= new PDOMPrettyPrinter();
		IIndexFragment[] frg= ((CIndex) index).getPrimaryFragments();
		for (IIndexFragment element : frg) {
			final PDOM pdom = (PDOM) element;
			dumpLinkage(pdom, linkageID, v);
		}
	}

	public static void dumpLinkage(PDOM pdom, final int linkageID) {
		final IPDOMVisitor v= new PDOMPrettyPrinter();
		dumpLinkage(pdom, linkageID, v);
	}

	private static void dumpLinkage(final PDOM pdom, final int linkageID, final IPDOMVisitor v) {
		try {
			final PDOMLinkage linkage = pdom.getLinkage(linkageID);
			if (linkage != null) {
				linkage.getIndex().accept(new IBTreeVisitor() {
					@Override
					public int compare(long record) throws CoreException {
						return 0;
					}

					@Override
					public boolean visit(long record) throws CoreException {
						if (record == 0)
							return false;
						PDOMNode node = linkage.getNode(record);
						if (v.visit(node))
							node.accept(v);
						v.leave(node);
						return true;
					}
				});
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
}
