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

import org.eclipse.cdt.codan.internal.core.cfg.DecisionNode;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * TODO: add description
 */
public class CxxDecisionNode extends DecisionNode {
	/**
	 * @param node
	 *        the node to set
	 */
	public void setNode(IASTNode node) {
		setData(node);
	}

	/**
	 * @return the node
	 */
	public IASTNode getNode() {
		return (IASTNode) getData();
	}

	/**
	 * @return
	 */
	@Override
	public String toStringData() {
		if (getNode() == null)
			return ""; //$NON-NLS-1$
		return getNode().getRawSignature();
	}
}
