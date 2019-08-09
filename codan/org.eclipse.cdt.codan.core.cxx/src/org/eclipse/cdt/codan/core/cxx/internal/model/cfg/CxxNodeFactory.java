/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia
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

import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.core.model.cfg.ICfgData;
import org.eclipse.cdt.codan.core.model.cfg.IDecisionNode;
import org.eclipse.cdt.codan.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.core.model.cfg.IPlainNode;
import org.eclipse.cdt.codan.internal.core.cfg.NodeFactory;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * TODO: add description
 */
public class CxxNodeFactory extends NodeFactory {
	public CxxNodeFactory() {
		super();
	}

	@Override
	public IPlainNode createPlainNode() {
		return new CxxPlainNode();
	}

	@Override
	public IDecisionNode createDecisionNode() {
		return new CxxDecisionNode();
	}

	@Override
	public IExitNode createExitNode() {
		return new CxxExitNode();
	}

	public CxxPlainNode createPlainNode(IASTNode ast) {
		IPlainNode node = createPlainNode();
		((ICfgData) node).setData(ast);
		return (CxxPlainNode) node;
	}

	public CxxDecisionNode createDecisionNode(IASTNode ast) {
		IDecisionNode node = createDecisionNode();
		((ICfgData) node).setData(ast);
		return (CxxDecisionNode) node;
	}

	public CxxExitNode createExitNode(IASTNode ast) {
		IExitNode node = createExitNode();
		((ICfgData) node).setData(ast);
		return (CxxExitNode) node;
	}

	/**
	 * @param caseSt
	 * @return
	 */
	public IBranchNode createBranchNode(IASTNode caseSt) {
		IBranchNode node = createBranchNode(caseSt.getRawSignature());
		((ICfgData) node).setData(caseSt);
		return node;
	}
}
