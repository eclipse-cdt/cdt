/*******************************************************************************
 * Copyright (c) 2006 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Symbian - Initial implementation
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
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Dump the contents of the PDOM index to stdout (for when you need
 * a lo-fidelity debugging tool)
 */
public class PDOMPrettyPrinter implements IPDOMVisitor {
	StringBuffer indent = new StringBuffer();
	final String step = "   "; //$NON-NLS-1$

	public void leave(IPDOMNode node) throws CoreException {
		if(indent.length()>=step.length())
			indent.setLength(indent.length()-step.length());
	}

	public boolean visit(IPDOMNode node) throws CoreException {
		indent.append(step);
		System.out.println(indent+""+node);
		return true;
	}

	/**
	 * Dumps the contents of the specified linkage for all primary fragments of the specified index
	 * to standard out, including file local scopes.
	 * @param index
	 * @param linkageID
	 */
	public static void dumpLinkage(IIndex index, final String linkageID) {
		final IPDOMVisitor v= new PDOMPrettyPrinter();
		IIndexFragment[] frg= ((CIndex)index).getPrimaryFragments();
		for(int i=0; i<frg.length; i++) {
			final PDOM pdom = (PDOM) frg[i];
			try {
				pdom.getLinkage(linkageID).getIndex().accept(
						new IBTreeVisitor() {
							public int compare(int record) throws CoreException {
								return 0;
							}
							public boolean visit(int record) throws CoreException {
								if(record==0) return false;
								PDOMNode node= pdom.getLinkage(linkageID).getNode(record);
								if(v.visit(node))
									node.accept(v);
								v.leave(node);
								return true;
							}
						});
			} catch(CoreException ce) {
				CCorePlugin.log(ce);
			}
		}
	}
}
