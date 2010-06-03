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

import org.eclipse.cdt.codan.internal.core.cfg.BranchNode;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * TODO: add description
 */
public class CxxBranchNode extends BranchNode {
	private IASTNode labelData;


    CxxBranchNode(IASTNode label) {
		super(label.getRawSignature());
		this.labelData = label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.internal.core.cfg.DecisionArc#toString()
	 */
	@Override
	public String toString() {
		return labelData.getRawSignature()+":"; //$NON-NLS-1$
	}
}
