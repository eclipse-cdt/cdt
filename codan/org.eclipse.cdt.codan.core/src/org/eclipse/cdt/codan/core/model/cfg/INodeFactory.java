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
package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Control Flow Graph Node factory
 * 
 * @noextend This interface is not intended to be extended by clients.
 */
public interface INodeFactory {
	/**
	 * @return new plain node
	 */
	IPlainNode createPlainNode();

	/**
	 * @return new jump node
	 */
	IJumpNode createJumpNode();

	/**
	 * @return new decision node
	 */
	IDecisionNode createDecisionNode();

	/**
	 * @return new connector node
	 */
	IConnectorNode createConnectorNode();

	/**
	 * @param label
	 * @return new branch node
	 */
	IBranchNode createBranchNode(String label);

	/**
	 * @return new start node
	 */
	IStartNode createStartNode();

	/**
	 * @return new exit node
	 */
	IExitNode createExitNode();
}
