/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia and others.
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
package org.eclipse.cdt.codan.core.model.cfg;

/**
 *
 * Interface for decision node. This node represent condition node in the graph,
 * it has one incoming arc and many outgoing, each of outgoing node should be
 * IBranchNode
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IDecisionNode extends IBasicBlock, ISingleIncoming {
	/**
	 * Node where branches of decision node merge
	 *
	 * @return the "merge" node
	 */
	IConnectorNode getMergeNode();
}
