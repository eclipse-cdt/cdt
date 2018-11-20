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
import org.eclipse.cdt.codan.core.model.cfg.ISingleOutgoing;

/**
 * Abstract implementation of basic block with single outgoing arc (node)
 *
 */
public abstract class AbstractSingleOutgoingNode extends AbstractBasicBlock implements ISingleOutgoing {
	private IBasicBlock next;

	/**
	 * Default constructor
	 */
	public AbstractSingleOutgoingNode() {
		super();
	}

	@Override
	public IBasicBlock[] getOutgoingNodes() {
		return new IBasicBlock[] { next };
	}

	@Override
	public int getOutgoingSize() {
		return 1;
	}

	@Override
	public IBasicBlock getOutgoing() {
		return next;
	}

	/**
	 * Sets outgoing node
	 *
	 * @param node
	 */
	public void setOutgoing(IBasicBlock node) {
		this.next = node;
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		setOutgoing(node);
	}
}
