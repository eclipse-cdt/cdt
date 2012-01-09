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
import org.eclipse.cdt.codan.core.model.cfg.ISingleIncoming;

/**
 * Abstract node with one incoming arc (node)
 * 
 */
public abstract class AbstractSingleIncomingNode extends AbstractBasicBlock implements ISingleIncoming {
	private IBasicBlock prev;

	/**
	 * Default constructor
	 */
	public AbstractSingleIncomingNode() {
		super();
	}

	@Override
	public IBasicBlock[] getIncomingNodes() {
		return new IBasicBlock[] { prev };
	}

	@Override
	public int getIncomingSize() {
		return 1;
	}

	@Override
	public IBasicBlock getIncoming() {
		return prev;
	}

	/**
	 * Sets the incoming node
	 * 
	 * @param prev
	 */
	public void setIncoming(IBasicBlock prev) {
		this.prev = prev;
	}

	@Override
	public void addIncoming(IBasicBlock node) {
		setIncoming(node);
	}
}
