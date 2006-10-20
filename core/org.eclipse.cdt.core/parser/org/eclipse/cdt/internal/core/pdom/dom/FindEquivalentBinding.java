/*******************************************************************************
 * Copyright (c) 2006 Symbian Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.bid.CLocalBindingIdentityComparator;
import org.eclipse.cdt.internal.core.dom.bid.ILocalBindingIdentity;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

public class FindEquivalentBinding implements IBTreeVisitor, IPDOMVisitor {

	PDOMBinding result;
	PDOMLinkage linkage;
	ILocalBindingIdentity targetBID;
	CLocalBindingIdentityComparator cmp;
	
	public FindEquivalentBinding(PDOMLinkage linkage, ILocalBindingIdentity target) throws CoreException {
		this.linkage = linkage;
		this.targetBID = target;
		this.cmp = new CLocalBindingIdentityComparator(linkage);
	}
	
	public FindEquivalentBinding(PDOMLinkage linkage, IBinding target) throws CoreException {
		this.linkage = linkage;
		this.targetBID = linkage.getLocalBindingIdentity(target);
		this.cmp = new CLocalBindingIdentityComparator(linkage);
	}
	
	public boolean visit(int record) throws CoreException {
		if(record!=0) {
			PDOMNode node = linkage.getNode(record);
			if(cmp.compare(targetBID, (IBinding) node)==0) {
				result = (PDOMBinding) node;
				return false;
			}
		}
		return true;
	}
	
	public int compare(int record) throws CoreException {
		PDOMNode node = linkage.getNode(record);
		return cmp.compare((IBinding) node, targetBID);
	}

	public boolean visit(IPDOMNode node) throws CoreException {
		if(node!=null && (node instanceof IBinding)) {
			if(cmp.compare(targetBID, (IBinding) node)==0) {
				result = (PDOMBinding) node;
				// aftodo - there is probably no performance reason not
				// to just return false here
				throw new CoreException(Status.OK_STATUS); 
			}
		}
		return true;
	}
	
	public void leave(IPDOMNode node) throws CoreException {/*NO-OP*/}

	public PDOMBinding getResult() {
		return result;
	}
}
