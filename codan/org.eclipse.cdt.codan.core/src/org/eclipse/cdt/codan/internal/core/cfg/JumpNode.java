/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.cfg;

import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.core.model.cfg.IJumpNode;

/**
 * Jump node is node that connects unusual control pass, such as goto, break and
 * continue
 *
 */
public class JumpNode extends AbstractSingleIncomingNode implements IJumpNode {
	private IConnectorNode jump;
	private boolean backward;

	protected JumpNode() {
		super();
	}

	@Override
	public IBasicBlock[] getOutgoingNodes() {
		return new IBasicBlock[] { jump };
	}

	@Override
	public int getOutgoingSize() {
		return 1;
	}

	@Override
	public IConnectorNode getJumpNode() {
		return jump;
	}

	@Override
	public IBasicBlock getOutgoing() {
		return jump;
	}

	@Override
	public boolean isBackwardArc() {
		return backward;
	}

	public void setJump(IConnectorNode jump, boolean backward) {
		if (this.jump != null && this.jump != jump)
			throw new IllegalArgumentException("Cannot modify exiting connector"); //$NON-NLS-1$
		this.jump = jump;
		this.backward = backward;
	}

	public void setBackward(boolean backward) {
		this.backward = backward;
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		setJump((IConnectorNode) node, backward);
	}
}
