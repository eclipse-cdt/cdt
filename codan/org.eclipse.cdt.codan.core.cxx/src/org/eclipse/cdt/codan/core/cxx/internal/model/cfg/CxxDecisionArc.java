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
package org.eclipse.cdt.codan.core.cxx.internal.model.cfg;

import org.eclipse.cdt.codan.internal.core.cfg.DecisionArc;
import org.eclipse.cdt.codan.internal.core.cfg.DecisionNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * TODO: add description
 */
public class CxxDecisionArc extends DecisionArc {
	private IASTNode label;

	/**
	 * @param decisionNode
	 * @param i
	 * @param node
	 */
	public CxxDecisionArc(DecisionNode decisionNode, int i, IBasicBlock node,
			IASTNode label) {
		super(decisionNode, i, node);
		this.label = label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.internal.core.cfg.DecisionArc#toString()
	 */
	@Override
	public String toString() {
		return label.getRawSignature();
	}
}
