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

	@Override
	public Iterator<IExitNode> getExitNodeIterator() {
		return exitNodes.iterator();
	}

	@Override
	public int getExitNodeSize() {
		return exitNodes.size();
	}

	public void setExitNodes(Collection<IExitNode> exitNodes) {
		if (this.exitNodes != null)
			throw new IllegalArgumentException("Cannot modify already exiting connector"); //$NON-NLS-1$
		this.exitNodes = Collections.unmodifiableList(new ArrayList<IExitNode>(exitNodes));
	}

	public void setUnconnectedNodes(Collection<IBasicBlock> nodes) {
		this.deadNodes = Collections.unmodifiableList(new ArrayList<IBasicBlock>(nodes));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IControlFlowGraph#
	 * getStartNode()
	 */
	@Override
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
	@Override
	public Iterator<IBasicBlock> getUnconnectedNodeIterator() {
		return deadNodes.iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IControlFlowGraph#
	 * getUnconnectedNodeSize()
	 */
	@Override
	public int getUnconnectedNodeSize() {
		return deadNodes.size();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.codan.core.model.cfg.IControlFlowGraph#getNodes ()
	 */
	@Override
	public Collection<IBasicBlock> getNodes() {
		Collection<IBasicBlock> result = new LinkedHashSet<IBasicBlock>();
		getNodes(getStartNode(), result);
		getDeadNodes(result);
		return result;
	}

	private void getNodes(IBasicBlock start, Collection<IBasicBlock> result) {
		if (start == null)
			return; // huh
		if (result.contains(start))
			return;
		result.add(start);
		for (IBasicBlock bb : start.getOutgoingNodes()) {
			getNodes(bb, result);
		}
		if (start instanceof IConnectorNode) {
			// special case where connect can have some incoming branch nodes not in the graph
			for (IBasicBlock bb : start.getIncomingNodes()) {
				getNodes(bb, result);
			}
		}
	}

	public Collection<IBasicBlock> getDeadNodes() {
		Collection<IBasicBlock> result = new LinkedHashSet<IBasicBlock>();
		getDeadNodes(result);
		return result;
	}

	private void getDeadNodes(Collection<IBasicBlock> result) {
		Collection<IBasicBlock> liveNodes = new LinkedHashSet<IBasicBlock>();
		getNodes(getStartNode(), liveNodes);

		for (Iterator<IBasicBlock> iterator = deadNodes.iterator(); iterator.hasNext();) {
			IBasicBlock d = iterator.next();
			getDeadNodes(d, result, liveNodes);
		}
	}

	public void getDeadNodes(IBasicBlock start, Collection<IBasicBlock> result, Collection<IBasicBlock> liveNodes) {
		if (start == null)
			return; // huh
		if (result.contains(start))
			return;
		// A connector node is only dead if none of its incoming edges are live nodes.
		if (start instanceof IConnectorNode) {

			for (IBasicBlock bb : start.getIncomingNodes()) {
				if (liveNodes.contains(bb)) {
					// skip node
					return;
				} else {
					// If the incoming edge is not live, mark it as dead.
					// This is necessary because in some situations (branch node for dead label)
					// we will never reach it otherwise from a dead starting node.
					result.add(bb);
				}
			}
		}
		result.add(start);
		for (IBasicBlock bb : start.getOutgoingNodes()) {
			getDeadNodes(bb, result, liveNodes);
		}
	}
}
