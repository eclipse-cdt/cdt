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
import org.eclipse.cdt.codan.core.model.cfg.ISingleOutgoing;

/**
 * Abstract implementation of basic block with single outgoing arc (node)
 * 
 */
public abstract class AbstractSingleOutgoingNode extends AbstractBasicBlock
		implements ISingleOutgoing {
	private IBasicBlock next;

	/**
	 * Default constructor
	 */
	public AbstractSingleOutgoingNode() {
		super();
	}

	public IBasicBlock[] getOutgoingNodes() {
		return new IBasicBlock[] { next };
	}

	public int getOutgoingSize() {
		return 1;
	}

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
