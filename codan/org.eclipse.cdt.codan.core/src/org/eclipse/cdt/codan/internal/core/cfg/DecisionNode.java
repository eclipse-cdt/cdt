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
package org.eclipse.cdt.codan.internal.core.cfg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.core.model.cfg.IDecisionNode;

/**
 * @see {@link IDecisionNode}
 */
public class DecisionNode extends AbstractSingleIncomingNode implements
		IDecisionNode {
	private List<IBasicBlock> next = new ArrayList<IBasicBlock>(2);
	private IConnectorNode conn;

	/**
	 * @param prev
	 */
	protected DecisionNode() {
		super();
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		IBranchNode cnode = (IBranchNode) node; // cast to throw CCE
		next.add(cnode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock#
	 * getOutgoingIterator()
	 */
	public IBasicBlock[] getOutgoingNodes() {
		return next.toArray(new IBasicBlock[next.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.cfg.IBasicBlock#getOutgoingSize ()
	 */
	public int getOutgoingSize() {
		return next.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IDecisionNode#
	 * getConnectionNode()
	 */
	public IConnectorNode getMergeNode() {
		return conn;
	}

	public void setMergeNode(IConnectorNode conn) {
		this.conn = conn;
	}
}
