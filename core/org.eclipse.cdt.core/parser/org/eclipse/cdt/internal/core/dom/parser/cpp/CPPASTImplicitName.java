/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mike Kucera (IBM) - Initial API and implementation
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
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;


public class CPPASTImplicitName extends CPPASTName implements IASTImplicitName {
	private boolean alternate;
	private boolean isOperator;
	private boolean isDefinition= false;

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
		if ((!alternate && action.shouldVisitImplicitNames) ||
				(alternate && action.shouldVisitImplicitNameAlternates)) {
            switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
            }

            switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
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
		isDefinition= val;
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
			if ((first.getNext() == null && oo != null) ||
					Arrays.equals(first.getCharImage(), Keywords.cDELETE) ||
					Arrays.equals(first.getCharImage(), Keywords.cNEW)) {
				int length = first.getLength();
				setOffsetAndLength(offset, length);
			} else {
				setOffsetAndLength(offset, 0);
			}
		} catch (ExpansionOverlapsBoundaryException e) {
			ASTNode parent = (ASTNode) getParent();
			setOffsetAndLength(parent.getOffset() + parent.getLength(), 0);
		}
    }

	public void setOperator(boolean isOperator) {
		this.isOperator = isOperator;
	}

	@Override
	public boolean isOperator() {
		return isOperator;
	}
}
