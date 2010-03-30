/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model.cfg;

import org.eclipse.cdt.codan.internal.core.cfg.DecisionNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * TODO: add description
 */
public class CxxDecisionNode extends DecisionNode {


	/**
	 * @param prev
	 * @param expression
	 */
	public CxxDecisionNode(IBasicBlock prev, IASTNode expression) {
		super(prev);
		this.setNode(expression);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getNode().getRawSignature();
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(IASTNode node) {
		setData(node);
	}

	/**
	 * @return the node
	 */
	public IASTNode getNode() {
		return (IASTNode) getData();
	}
	/**
	 * @return
	 */
	public String toStringData() {
		if (getNode() == null)
			return "";
		return getNode().getRawSignature();
	}
}
