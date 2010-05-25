/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class ConnectorNode extends AbstractSingleOutgoingNode implements
		IConnectorNode {
	protected ArrayList<IBasicBlock> incoming = new ArrayList<IBasicBlock>(2);

	protected ConnectorNode() {
		super();
	}

	@Override
	public void addIncoming(IBasicBlock node) {
		incoming.add(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock#
	 * getIncomingIterator()
	 */
	public IBasicBlock[] getIncomingNodes() {
		return incoming.toArray(new IBasicBlock[incoming.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.cfg.IBasicBlock#getIncomingSize ()
	 */
	public int getIncomingSize() {
		return incoming.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IConnectorNode#
	 * hasBackwardIncoming()
	 */
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
