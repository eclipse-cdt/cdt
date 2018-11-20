/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.cfg;

import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.core.model.cfg.IDecisionNode;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.IJumpNode;
import org.eclipse.cdt.codan.core.model.cfg.INodeFactory;
import org.eclipse.cdt.codan.core.model.cfg.IPlainNode;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;

/**
 * Factory that creates cfg nodes
 */
public class NodeFactory implements INodeFactory {
	public NodeFactory() {
	}

	@Override
	public IPlainNode createPlainNode() {
		return new PlainNode();
	}

	@Override
	public IJumpNode createJumpNode() {
		return new JumpNode();
	}

	@Override
	public IDecisionNode createDecisionNode() {
		return new DecisionNode();
	}

	@Override
	public IConnectorNode createConnectorNode() {
		return new ConnectorNode();
	}

	@Override
	public IStartNode createStartNode() {
		return new StartNode();
	}

	@Override
	public IExitNode createExitNode() {
		return new ExitNode();
	}

	@Override
	public IBranchNode createBranchNode(String label) {
		return new BranchNode(label);
	}
}
