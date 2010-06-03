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
 * Connector node has multiple incoming branches and single outgoing.
 * Incoming nodes are usually instance of {@link IJumpNode}
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IConnectorNode extends IBasicBlock, ISingleOutgoing {
	/**
	 * @return true if one of the incoming arcs is backward arc
	 */
	boolean hasBackwardIncoming();
}
