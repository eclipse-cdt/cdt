/*******************************************************************************
 * Copyright (c) 2007, 2014 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Applies the specified visitor to the node being visited, and recursively to
 * any nodes which act as containers
 */
public class ApplyVisitor implements IBTreeVisitor, IPDOMVisitor {
	protected PDOMLinkage linkage;
	protected IPDOMVisitor visitor;

	public ApplyVisitor(PDOMLinkage linkage, IPDOMVisitor visitor) {
		this.linkage = linkage;
		this.visitor = visitor;
	}

	@Override
	public int compare(long record) throws CoreException {
		return 0; // visit all nodes in a b-tree
	}

	@Override
	public boolean visit(IPDOMNode node) throws CoreException {
		if (node instanceof PDOMBinding) {
			((PDOMBinding) node).accept(visitor);
			((PDOMBinding) node).accept(this);
		}
		return false; // don't visit children of the node
	}

	@Override
	public boolean visit(long record) throws CoreException {
		if (record == 0)
			return true;
		PDOMNode node = PDOMNode.load(linkage.getPDOM(), record);
		if (node instanceof PDOMBinding) {
			((PDOMBinding) node).accept(visitor);
			((PDOMBinding) node).accept(this);
		}
		return true;
	}

	@Override
	public void leave(IPDOMNode node) throws CoreException {
	}
}
