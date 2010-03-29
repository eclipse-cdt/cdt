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
import java.util.Iterator;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IConnectorNode;

/**
 * TODO: add description
 */
public class ConnectorNode extends AbstractSingleOutgoingNode implements
		IConnectorNode {
	ArrayList<IBasicBlock> incoming = new ArrayList<IBasicBlock>(2);

	/**
	 * @param next
	 */
	public ConnectorNode() {
		super(null);
	}

	public void addIncoming(IBasicBlock node) {
		incoming.add(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock#
	 * getIncomingIterator()
	 */
	public Iterator<IBasicBlock> getIncomingIterator() {
		return incoming.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock#getIncomingSize
	 * ()
	 */
	public int getIncomingSize() {
		// TODO Auto-generated method stub
		return incoming.size();
	}
}
