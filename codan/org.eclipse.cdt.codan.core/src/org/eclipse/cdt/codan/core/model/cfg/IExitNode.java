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
 * Exit node of the graph. Usually return from the function, can also be throw
 * or abort, such at exit(0) call.
 * 
 */
public interface IExitNode extends IBasicBlock, ISingleIncoming {
	/**
	 * @return reference to a start node a graph
	 */
	IStartNode getStartNode();
}
