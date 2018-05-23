/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNodeSearch;

/**
 * An implicit name is used to resolve uses of implicit bindings, such as overloaded operators.
 *
 * @see IASTImplicitName
 */
public class CPPASTImplicitName extends CPPASTName implements IASTImplicitName {
	private boolean alternate;
	private boolean isOperator;
	private boolean isDefinition;

	public CPPASTImplicitName(IASTNode parent) {
		this(CharArrayUtils.EMPTY, parent);
	}

	public CPPASTImplicitName(char[] name, IASTNode parent) {
		super(name);
		setParent(parent);
		setPropertyInParent(IASTImplicitNameOwner.IMPLICIT_NAME);
	}

	public CPPASTImplicitName(OverloadableOperator op, IASTNode parent) {
		this(op.toCharArray(), parent);
		isOperator = true;
	}

	@Override
	public CPPASTImplicitName copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CPPASTImplicitName copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAlternate() {
		return alternate;
	}

	public void setAlternate(boolean alternate) {
		this.alternate = alternate;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if ((!alternate && action.shouldVisitImplicitNames)
				|| (alternate && action.shouldVisitImplicitNameAlternates)) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			}

			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			}
		}
		return true;
	}

	@Override
	public boolean isDeclaration() {
		return false;
	}

	@Override
	public boolean isDefinition() {
		return isDefinition;
	}

	@Override
	public boolean isReference() {
		return !isDefinition;
	}

	public void setIsDefinition(boolean val) {
		isDefinition = val;
	}

	/**
	 * Utility method for setting offsets using operator syntax.
	 *
	 * @param trailing true for trailing syntax, false for leading syntax
	 */
	public void computeOperatorOffsets(IASTNode relativeNode, boolean trailing) {
		if (relativeNode == null)
			return;

		IToken first;
		try {
			first = trailing ? relativeNode.getTrailingSyntax() : relativeNode.getLeadingSyntax();

			int offset = ((ASTNode) relativeNode).getOffset() + first.getOffset();
			if (trailing)
				offset += ((ASTNode) relativeNode).getLength();

			OverloadableOperator oo = OverloadableOperator.valueOf(first);
			if ((first.getNext() == null && oo != null) || Arrays.equals(first.getCharImage(), Keywords.cDELETE)
					|| Arrays.equals(first.getCharImage(), Keywords.cNEW)) {
				int length = first.getLength();
				setOffsetAndLength(offset, length);
			} else {
				setOffsetAndLength(offset, 0);
			}
		} catch (ExpansionOverlapsBoundaryException e) {
			if (!computeOperatorOffsetsFallback(relativeNode, trailing)) {
				// Fall-back for the fall-back
				ASTNode parent = (ASTNode) getParent();
				setOffsetAndLength(parent.getOffset() + parent.getLength(), 0);
			}
		}
	}

	// Fallback algorithm to use in computeOperatorOffsets() when the operator is
	// in a macro expansion.
	private boolean computeOperatorOffsetsFallback(IASTNode relativeNode, boolean trailing) {
		if (!(relativeNode instanceof ASTNode)) {
			return false;
		}
		ASTNode relative = (ASTNode) relativeNode;

		// Find the sequence numbers denoting the bounds of the leading or
		// trailing syntax, much as IASTNode.getLeadingSyntax() or
		// getTrailingSyntax() would. The code here follows the
		// implementation of those functions closely.
		ASTNodeSearch visitor = new ASTNodeSearch(relativeNode);
		IASTNode sibling = trailing ? visitor.findRightSibling() : visitor.findLeftSibling();
		IASTNode parent = sibling == null ? relativeNode.getParent() : null;
		if (!((sibling == null || sibling instanceof ASTNode) && (parent == null || parent instanceof ASTNode))) {
			return false;
		}
		ASTNode sib = (ASTNode) sibling;
		ASTNode par = (ASTNode) parent;
		@SuppressWarnings("null")
		int start = trailing ? relative.getOffset() + relative.getLength()
				: sib != null ? sib.getOffset() + sib.getLength() : par.getOffset();
		@SuppressWarnings("null")
		int end = trailing ? sib != null ? sib.getOffset() : par.getOffset() + par.getLength() : relative.getOffset();

		// If there is only one token within the bounds, it must be the
		// operator token, and we have our answer.
		if (end == start + 1) {
			setOffsetAndLength(start, 1);
			return true;
		}

		// Otherwise, give up.
		return false;
	}

	public void setOperator(boolean isOperator) {
		this.isOperator = isOperator;
	}

	@Override
	public boolean isOperator() {
		return isOperator;
	}
}
