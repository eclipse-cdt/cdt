/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.core.model.cfg.IControlFlowGraph;
import org.eclipse.cdt.codan.core.model.cfg.IDecisionNode;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.ISingleOutgoing;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;

/**
 * Implementation of control flow graph
 */
public class ControlFlowGraph implements IControlFlowGraph {
	private List<IExitNode> exitNodes;
	private List<IBasicBlock> deadNodes = new ArrayList<IBasicBlock>();
	private IStartNode start;

	public ControlFlowGraph(IStartNode start, Collection<IExitNode> exitNodes) {
		setExitNodes(exitNodes);
		this.start = start;
	}

	public Iterator<IExitNode> getExitNodeIterator() {
		return exitNodes.iterator();
	}

	public int getExitNodeSize() {
		return exitNodes.size();
	}

	public void setExitNodes(Collection<IExitNode> exitNodes) {
		if (this.exitNodes != null)
			throw new IllegalArgumentException(
					"Cannot modify already exiting connector"); //$NON-NLS-1$
		this.exitNodes = Collections.unmodifiableList(new ArrayList<IExitNode>(
				exitNodes));
	}

	public void setUnconnectedNodes(Collection<IBasicBlock> nodes) {
		this.deadNodes = Collections
				.unmodifiableList(new ArrayList<IBasicBlock>(nodes));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IControlFlowGraph#
	 * getStartNode()
	 */
	public IStartNode getStartNode() {
		return start;
	}

	void setStartNode(IStartNode start) {
		this.start = start;
	}

	public void print(IBasicBlock node) {
		System.out.println(node.getClass().getSimpleName() + ": " //$NON-NLS-1$
				+ ((AbstractBasicBlock) node).toStringData());
		if (node instanceof IDecisionNode) {
			// todo
			IBasicBlock[] branches = ((IDecisionNode) node).getOutgoingNodes();
			for (int i = 0; i < branches.length; i++) {
				IBasicBlock brNode = branches[i];
				System.out.println("{"); //$NON-NLS-1$
				print(brNode);
				System.out.println("}"); //$NON-NLS-1$
			}
			print(((IDecisionNode) node).getMergeNode());
		} else if (node instanceof ISingleOutgoing) {
			IBasicBlock next = ((ISingleOutgoing) node).getOutgoing();
			if (!(next instanceof IConnectorNode && !(next instanceof IBranchNode)))
				print(next);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IControlFlowGraph#
	 * getUnconnectedNodeIterator()
	 */
	public Iterator<IBasicBlock> getUnconnectedNodeIterator() {
		return deadNodes.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IControlFlowGraph#
	 * getUnconnectedNodeSize()
	 */
	public int getUnconnectedNodeSize() {
		return deadNodes.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.cfg.IControlFlowGraph#getNodes ()
	 */
	public Collection<IBasicBlock> getNodes() {
		Collection<IBasicBlock> result = new LinkedHashSet<IBasicBlock>();
		getNodes(getStartNode(), result);
		for (Iterator<IBasicBlock> iterator = deadNodes.iterator(); iterator
				.hasNext();) {
			IBasicBlock d = iterator.next();
			getNodes(d, result);
		}
		return result;
	}

	/**
	 * @param d
	 * @param result
	 */
	private void getNodes(IBasicBlock start, Collection<IBasicBlock> result) {
		if (result.contains(start))
			return;
		result.add(start);
		IBasicBlock[] outgoingNodes = start.getOutgoingNodes();
		for (int i = 0; i < outgoingNodes.length; i++) {
			IBasicBlock b = outgoingNodes[i];
			getNodes(b, result);
		}
	}
}
