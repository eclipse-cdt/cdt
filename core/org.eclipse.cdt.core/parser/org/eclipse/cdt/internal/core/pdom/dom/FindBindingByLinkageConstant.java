/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.bid.CLocalBindingIdentityComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

public class FindBindingByLinkageConstant implements IBTreeVisitor, IPDOMVisitor {
	protected final char[] name;
	protected final int constant;
	protected final PDOMLinkage linkage;
	protected final CLocalBindingIdentityComparator bic;

	protected PDOMBinding result;
	
	public FindBindingByLinkageConstant(PDOMLinkage linkage, char[] name, int constant) {
		this.name = name;
		this.constant = constant;
		this.linkage = linkage;
		this.bic = new CLocalBindingIdentityComparator(linkage);
	}

	public int compare(int record) throws CoreException {
		PDOMNode node = linkage.getNode(record);
		return CharArrayUtils.compare(
				((PDOMBinding)node).getNameCharArray(),
				name); 
	}

	public boolean visit(int record) throws CoreException {
		if(record!=0) {
			PDOMNode node = linkage.getNode(record);
			if(bic.compareNameAndConstOnly((PDOMBinding)node, name, constant)==0) {
				result = (PDOMBinding) node;
				return false;
			}
		}
		return true;
	}
	
	public boolean visit(IPDOMNode node) throws CoreException {
		if(node!=null) {
			if(bic.compareNameAndConstOnly((PDOMBinding)node, name, constant)==0) {
				result = (PDOMBinding) node;
				throw new CoreException(Status.OK_STATUS); // TODO - why not just return false?
			}
		}
		return true;
	}
	
	public void leave(IPDOMNode node) throws CoreException {/*NO-OP*/}
	
	public PDOMBinding getResult() {
		return result;
	}
}
