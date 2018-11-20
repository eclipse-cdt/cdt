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
package org.eclipse.cdt.codan.internal.core.cfg;

import org.eclipse.cdt.codan.core.model.cfg.IBranchNode;

/**
 * Branch node is a node with on incoming arc, one outgoing arc and a "string"
 * label. Can be used to represent branches of if, switch and labelled
 * statements.
 */
public class BranchNode extends PlainNode implements IBranchNode {
	protected String label;

	protected BranchNode(String label) {
		super();
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String toStringData() {
		return label;
	}
}
