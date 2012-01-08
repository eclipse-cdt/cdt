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
import org.eclipse.cdt.codan.core.model.cfg.IPlainNode;

/**
 * Plain node has one incoming arc and one outgoing arc
 * 
 */
public class PlainNode extends AbstractSingleIncomingNode implements IPlainNode {
	protected IBasicBlock next;

	protected PlainNode() {
		super();
	}

	@Override
	public IBasicBlock[] getOutgoingNodes() {
		return new IBasicBlock[] { next };
	}

	@Override
	public int getOutgoingSize() {
		if (next == null)
			return 0;
		return 1;
	}

	@Override
	public IBasicBlock getOutgoing() {
		return next;
	}

	public void setOutgoing(IBasicBlock exit) {
		this.next = exit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock#addOutgoing
	 * (org.eclipse.cdt.codan.core.model.cfg.IBasicBlock)
	 */
	@Override
	public void addOutgoing(IBasicBlock node) {
		setOutgoing(node);
	}
}
