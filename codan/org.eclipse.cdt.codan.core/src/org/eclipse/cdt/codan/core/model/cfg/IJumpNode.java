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
package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Node that changes the control of the graph, i.e. passes control to non-next
 * statement. Can be used to implement gotos, break, continue, end of branches.
 * Outgoing node is always {@link IConnectorNode}
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IJumpNode extends IBasicBlock, ISingleOutgoing {
	/**
	 * @return true of outgoing arc is backward one, see definition of backward
	 *         arc in a "network" graph
	 */
	boolean isBackwardArc();

	/**
	 * @return reference to a connector node to which this one "jumps" (same as
	 *         outgoing node)
	 */
	IConnectorNode getJumpNode();
}
