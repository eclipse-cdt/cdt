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
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;

/**
 * Start node has no incoming, one outgoing and it is connect to function exits
 * 
 */
public class StartNode extends AbstractSingleOutgoingNode implements IStartNode {
	protected StartNode() {
		super();
	}

	@Override
	public IBasicBlock[] getIncomingNodes() {
		return EMPTY_LIST;
	}

	@Override
	public int getIncomingSize() {
		return 0;
	}

	@Override
	public void addOutgoing(IBasicBlock node) {
		setOutgoing(node);
	}

	@Override
	public void addIncoming(IBasicBlock node) {
		throw new UnsupportedOperationException();
	}
}
