/*******************************************************************************
 * Copyright (c) 2010, 2014 Alena Laskavaia and others.
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
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;

/**
 * Plain node has one prev one jump
 */
public class ExitNode extends AbstractSingleIncomingNode implements IExitNode {
	private IStartNode start;

	protected ExitNode() {
		super();
	}

	@Override
	public IBasicBlock[] getOutgoingNodes() {
		return EMPTY_LIST;
	}

	@Override
	public int getOutgoingSize() {
		return 0;
	}

	@Override
	public IStartNode getStartNode() {
		return start;
	}

	public void setStartNode(IStartNode start) {
		this.start = start;
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		throw new UnsupportedOperationException();
	}
}
