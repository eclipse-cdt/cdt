/*******************************************************************************
 * Copyright (c) 2009, 2014 Alena Laskavaia
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

	@Override
	public String toString() {
		return super.toString();
	}
}
