/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite;

import org.eclipse.cdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * @see org.eclipse.cdt.core.dom.rewrite.ITrackedNodePosition
 */
public class TrackedNodePosition implements ITrackedNodePosition {
	private final TextEditGroup group;
	private final ASTNode node;

	public TrackedNodePosition(TextEditGroup group, ASTNode node) {
		this.group = group;
		this.node = node;
	}

	@Override
	public int getStartPosition() {
		if (this.group.isEmpty()) {
			return this.node.getOffset();
		}
		IRegion coverage = TextEdit.getCoverage(this.group.getTextEdits());
		if (coverage == null) {
			return this.node.getOffset();
		}
		return coverage.getOffset();
	}

	@Override
	public int getLength() {
		if (this.group.isEmpty()) {
			return this.node.getLength();
		}
		IRegion coverage = TextEdit.getCoverage(this.group.getTextEdits());
		if (coverage == null) {
			return this.node.getLength();
		}
		return coverage.getLength();
	}
}
