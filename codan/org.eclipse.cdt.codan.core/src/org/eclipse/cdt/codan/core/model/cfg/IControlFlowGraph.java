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

import java.util.Collection;
import java.util.Iterator;

/**
 * Represents control flow graph (CFG) object.
 * This is "normalized" control flow graph, with typed nodes:
 * <br>
 * <li> {@link IStartNode} - start node of the cfg (source)
 * <li> {@link IExitNode} - exit node of the cfg (sink)
 * <li> {@link IPlainNode} - has one incoming one outgoing
 * <li> {@link IDecisionNode} - has one incoming and the only node that can have
 * multiple outcoming
 * arcs
 * <li> {@link IConnectorNode} - the only node that can have multiple incoming
 * arcs, and one outgoing
 * <li> {@link IJumpNode} - has one incoming and one outgoing but represent
 * change of control direction
 * <li> {@link IBranchNode} - usually node where decision node connect to,
 * labels represent a way where controls goes to
 */
public interface IControlFlowGraph {
	/**
	 * @return start node of the graph. CFG only has one start node.
	 */
	IStartNode getStartNode();

	/**
	 * @return iterator over exit nodes of control flow graph. Exit nodes
	 *         include return statement,
	 *         and statements with throw and abort/exit functions.
	 */
	Iterator<IExitNode> getExitNodeIterator();

	/**
	 * @return size of exit nodes list
	 */
	int getExitNodeSize();

	/**
	 * @return list of roots of dead code sections, they don't have incoming
	 *         arcs
	 */
	Iterator<IBasicBlock> getUnconnectedNodeIterator();

	/**
	 * @return size of unconnected nodes list
	 */
	int getUnconnectedNodeSize();

	/**
	 * @return collection of all nodes
	 */
	Collection<IBasicBlock> getNodes();
}
