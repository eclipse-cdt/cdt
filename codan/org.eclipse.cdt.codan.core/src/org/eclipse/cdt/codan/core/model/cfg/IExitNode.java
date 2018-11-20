/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia
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
package org.eclipse.cdt.codan.core.model.cfg;

/**
 * Exit node of the graph. Usually return from the function, can also be throw
 * or abort, such at exit(0) call.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IExitNode extends IBasicBlock, ISingleIncoming {
	/**
	 * @return reference to a start node a graph
	 */
	IStartNode getStartNode();
}
