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
package org.eclipse.cdt.codan.internal.core.cfg;

import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IDecisionArc;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IDecisionNode;

public class DecisionArc implements IDecisionArc {
	private final DecisionNode decisionNode;
	protected int index;
	protected IBasicBlock node;

	public DecisionArc(DecisionNode decisionNode, int i, IBasicBlock node) {
		this.decisionNode = decisionNode;
		this.index = i;
		this.node = node;
	}

	public int getIndex() {
		return index;
	}

	public IBasicBlock getOutgoing() {
		return node;
	}

	public IDecisionNode getDecisionNode() {
		return this.decisionNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return index + "";
	}
}