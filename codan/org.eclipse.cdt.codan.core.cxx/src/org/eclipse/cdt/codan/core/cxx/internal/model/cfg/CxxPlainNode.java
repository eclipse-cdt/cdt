/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model.cfg;

import org.eclipse.cdt.codan.internal.core.cfg.PlainNode;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * TODO: add description
 */
public class CxxPlainNode extends PlainNode {
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
