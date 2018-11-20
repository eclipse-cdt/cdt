/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia
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

import java.util.ArrayList;

import org.eclipse.cdt.codan.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.core.model.cfg.IJumpNode;

/**
 * TODO: add description
 */
public class ConnectorNode extends AbstractSingleOutgoingNode implements IConnectorNode {
	protected ArrayList<IBasicBlock> incoming = new ArrayList<>(2);

	protected ConnectorNode() {
		super();
	}

	@Override
	public void addIncoming(IBasicBlock node) {
		incoming.add(node);
	}

	@Override
	public IBasicBlock[] getIncomingNodes() {
		return incoming.toArray(new IBasicBlock[incoming.size()]);
	}

	@Override
	public int getIncomingSize() {
		return incoming.size();
	}

	@Override
	public boolean hasBackwardIncoming() {
		for (IBasicBlock node : incoming) {
			if (node instanceof IJumpNode) {
				if (((IJumpNode) node).isBackwardArc())
					return true;
			}
		}
		return false;
	}
}
