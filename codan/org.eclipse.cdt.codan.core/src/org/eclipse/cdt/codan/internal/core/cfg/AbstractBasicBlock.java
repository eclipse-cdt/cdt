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
import org.eclipse.cdt.codan.core.model.cfg.ICfgData;

/**
 * Abstract Basic Block for control flow graph.
 */
public abstract class AbstractBasicBlock implements IBasicBlock, ICfgData {
	/**
	 * Empty array of basic blocks
	 */
	public final static IBasicBlock[] EMPTY_LIST = new IBasicBlock[0];
	private Object data;

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Add a node to list of outgoing nodes of this node
	 *
	 * @param node - node to add
	 */
	public abstract void addOutgoing(IBasicBlock node);

	/**
	 * Add a node to list of incoming nodes of this node
	 *
	 * @param node - node to add
	 */
	public abstract void addIncoming(IBasicBlock node);

	/**
	 * @return toString for data object
	 */
	public String toStringData() {
		if (getData() == null)
			return "0x" + Integer.toHexString(System.identityHashCode(this)); //$NON-NLS-1$
		return getData().toString();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + toStringData(); //$NON-NLS-1$
	}
}
