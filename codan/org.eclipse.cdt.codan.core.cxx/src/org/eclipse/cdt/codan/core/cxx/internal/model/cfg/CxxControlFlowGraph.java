/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model.cfg;

import java.util.Collection;

import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.IStartNode;
import org.eclipse.cdt.codan.internal.core.cfg.ControlFlowGraph;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

/**
 * TODO: add description
 */
public class CxxControlFlowGraph extends ControlFlowGraph {
	/**
	 * @param start
	 * @param exitNodes
	 */
	public CxxControlFlowGraph(IStartNode start, Collection<IExitNode> exitNodes) {
		super(start, exitNodes);
	}

	public static CxxControlFlowGraph build(IASTFunctionDefinition def) {
		return new ControlFlowGraphBuilder().build(def);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}
