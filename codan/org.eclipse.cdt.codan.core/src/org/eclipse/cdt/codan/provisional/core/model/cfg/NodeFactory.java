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
package org.eclipse.cdt.codan.provisional.core.model.cfg;

import org.eclipse.cdt.codan.internal.core.cfg.ConnectorNode;
import org.eclipse.cdt.codan.internal.core.cfg.DecisionNode;
import org.eclipse.cdt.codan.internal.core.cfg.ExitNode;
import org.eclipse.cdt.codan.internal.core.cfg.JumpNode;
import org.eclipse.cdt.codan.internal.core.cfg.BranchNode;
import org.eclipse.cdt.codan.internal.core.cfg.PlainNode;
import org.eclipse.cdt.codan.internal.core.cfg.StartNode;

/**
 * TODO: add description
 */
public class NodeFactory implements INodeFactory {
	IControlFlowGraph graph;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.INodeFactory#
	 * getControlFlowGraph()
	 */
	public IControlFlowGraph getControlFlowGraph() {
		return graph;
	}

	public NodeFactory() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.provisional.core.model.cfg.INodeFactory#createPlainNode
	 * ()
	 */
	public IPlainNode createPlainNode() {
		return new PlainNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.provisional.core.model.cfg.INodeFactory#createJumpNode
	 * ()
	 */
	public IJumpNode createJumpNode() {
		return new JumpNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.INodeFactory#
	 * createDecisionNode()
	 */
	public IDecisionNode createDecisionNode() {
		return new DecisionNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.INodeFactory#
	 * createConnectiorNode()
	 */
	public IConnectorNode createConnectorNode() {
		return new ConnectorNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.provisional.core.model.cfg.INodeFactory#createStartNode
	 * ()
	 */
	public IStartNode createStartNode() {
		return new StartNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.provisional.core.model.cfg.INodeFactory#createExitNode
	 * ()
	 */
	public IExitNode createExitNode() {
		return new ExitNode();
	}

	public IBranchNode createBranchNode(String label) {
		return new BranchNode(label);
	}
}
