/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
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
