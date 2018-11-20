/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Utility class to search for siblings of an ast node
 */
public class ASTNodeSearch extends ASTGenericVisitor {
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	private int fMode;
	private IASTNode fLeft;
	private IASTNode fRight;
	private final IASTNode fNode;
	private final IASTNode fParent;

	public ASTNodeSearch(IASTNode node) {
		super(true);
		fNode = node;
		fParent = node.getParent();
	}

	public IASTNode findLeftSibling() {
		if (fParent == null)
			return null;

		fMode = LEFT;
		fLeft = fRight = null;
		fParent.accept(this);
		return fLeft;
	}

	public IASTNode findRightSibling() {
		if (fParent == null)
			return null;

		fMode = RIGHT;
		fLeft = fRight = null;
		fParent.accept(this);
		return fRight;
	}

	@Override
	protected int genericVisit(IASTNode node) {
		if (node == fParent)
			return PROCESS_CONTINUE;

		switch (fMode) {
		case LEFT:
			if (node == fNode)
				return PROCESS_ABORT;
			fLeft = node;
			return PROCESS_SKIP;
		case RIGHT:
			if (node == fNode) {
				fLeft = fNode;
			} else if (fLeft != null) {
				fRight = node;
				return PROCESS_ABORT;
			}
			return PROCESS_SKIP;
		}
		return PROCESS_SKIP;
	}
}
