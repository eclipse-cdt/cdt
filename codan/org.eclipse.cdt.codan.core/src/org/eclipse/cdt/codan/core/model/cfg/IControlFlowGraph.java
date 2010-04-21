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
 * TODO: add description
 */
public interface IControlFlowGraph {
	IStartNode getStartNode();

	/**
	 * 
	 * @return
	 */
	Iterator<IExitNode> getExitNodeIterator();

	int getExitNodeSize();

	Iterator<IBasicBlock> getUnconnectedNodeIterator();

	int getUnconnectedNodeSize();

	Collection<IBasicBlock> getNodes();
}
