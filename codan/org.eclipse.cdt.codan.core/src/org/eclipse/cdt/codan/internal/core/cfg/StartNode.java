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
