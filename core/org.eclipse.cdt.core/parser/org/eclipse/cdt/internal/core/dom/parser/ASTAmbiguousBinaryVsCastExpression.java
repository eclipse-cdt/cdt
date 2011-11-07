/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;

/**
 * Handles the ambiguity between a binary- and a cast-expression. (type)+var versus (var)+var.
 * It also handles the impact on the grouping of the sub-expressions.
 */
public abstract class ASTAmbiguousBinaryVsCastExpression extends ASTAmbiguousNode implements IASTAmbiguousExpression {

	private final IASTBinaryExpression fBinaryExpression;
    private final IASTCastExpression fCastExpression;

    /**
     * The binary expression must have one of the following operators: +,-,&,*. The left hand side of the binary expression
     * must end with an expression in parenthesis (which could be read as the beginning of the cast-expression.
     * The cast-expression must contain the type-id (corresponding to the last expression in parenthesis on the left side of the
     * binary expression. The operand of the castExpression can be <code>null</code>, it will be computed from the binary expression.
     */
    public ASTAmbiguousBinaryVsCastExpression(IASTBinaryExpression binaryExpression, IASTCastExpression castExpression) {
    	fBinaryExpression= binaryExpression;
    	fCastExpression= castExpression;
    }
        
    @Override
	public final IASTExpression copy() {
    	throw new UnsupportedOperationException();
    }
    
	@Override
	public final IASTExpression copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

    @Override
	public final void addExpression(IASTExpression e) {
    	throw new UnsupportedOperationException();
    }
    
	@Override
	public final IASTNode[] getNodes() {
		return getExpressions();
	}

	@Override
	public IASTExpression[] getExpressions() {
		return new IASTExpression[] {fBinaryExpression, fCastExpression};
	}

	@Override
	protected final IASTNode doResolveAmbiguity(ASTVisitor visitor) {
		final IASTAmbiguityParent owner= (IASTAmbiguityParent) getParent();
		IASTNode nodeToReplace= this;

		// handle nested ambiguities first
		owner.replace(nodeToReplace, fCastExpression);
		nodeToReplace= fCastExpression;
		fCastExpression.getTypeId().accept(visitor);

		owner.replace(nodeToReplace, fBinaryExpression);
		nodeToReplace= fBinaryExpression;
		fBinaryExpression.accept(visitor);
		

		// find nested names
		final NameCollector nameCollector= new NameCollector();
		fCastExpression.getTypeId().accept(nameCollector);
		final IASTName[] names= nameCollector.getNames();

		// resolve names 
		boolean hasIssue= false;
		for (IASTName name : names) {
			try {
				IBinding b = name.resolveBinding();
				if (b instanceof IProblemBinding) {
					hasIssue= true;
					break;
				}
			} catch (Exception t) {
				hasIssue= true;
				break;
			}
		}
		if (hasIssue) {
			return nodeToReplace;
		}
		
		final IASTExpression left = fBinaryExpression.getOperand1();
		final IASTExpression right = fBinaryExpression.getOperand2();
		left.setParent(null);
		right.setParent(null);
		IASTUnaryExpression primaryInParenthesis= findTrailingBracketedPrimaryExpression(left);
		IASTExpression leadingCastExpression= findLeadingCastExpression(right);
		IASTExpression castedUnary= fCastExpression.getOperand();
		if (primaryInParenthesis != null && leadingCastExpression != null && castedUnary instanceof IASTUnaryExpression) {
			IASTExpression lp= (IASTExpression) primaryInParenthesis.getParent();
			IASTBinaryExpression rp= (IASTBinaryExpression) leadingCastExpression.getParent();
			((IASTUnaryExpression)castedUnary).setOperand(leadingCastExpression);
			setEnd(castedUnary, leadingCastExpression);
			setRange(fCastExpression, primaryInParenthesis, leadingCastExpression);
			IASTExpression root= joinExpressions(lp, fCastExpression, rp);
			if (root != null) {
				owner.replace(nodeToReplace, root);
				return root;
			}
		}
		return nodeToReplace;
	}

	private void setEnd(IASTNode node, IASTNode end) {
		final ASTNode target= (ASTNode) node;
		final ASTNode e= (ASTNode) end;
		target.setLength(e.getOffset() + e.getLength() - target.getOffset());
	}

	private void setStart(IASTNode node, IASTNode start) {
		final ASTNode target= (ASTNode) node;
		final int offset = ((ASTNode) start).getOffset();
		target.setOffsetAndLength(offset, target.getOffset() + target.getLength() - offset);
	}

	private void setRange(IASTNode node, IASTNode from, IASTNode to) {
		final int offset = ((ASTNode) from).getOffset();
		final ASTNode t= (ASTNode) to;
		((ASTNode) node).setOffsetAndLength(offset, t.getOffset()+t.getLength()-offset);
	}


	/**
	 * @param l unary, cast or binary expression to the left, all parents are unary, cast or binary expressions.
	 * @param middle initially a cast-expression, always suitable to put into l or r
	 * @param r a binary expression to the right, all parents are binary expressions.
	 */
	private IASTExpression joinExpressions(IASTExpression l, IASTExpression middle, IASTBinaryExpression r) {
		while (true) {
			if (l == null) {
				if (r == null) {
					return middle;
				}
				r.setOperand1(middle);	
				setStart(r, middle);
				middle= r;
				r= (IASTBinaryExpression) r.getParent();
			} else if (l instanceof IASTCastExpression) { 
				// cast binds stronger than binary operator
				((IASTCastExpression) l).setOperand(middle); 
				setEnd(l, middle);
				middle= l;				// middle becomes cast-expr, can be put into r (a binary-expr)
				l= (IASTExpression) l.getParent(); 
			} else if (l instanceof IASTUnaryExpression) { //
				// unary operator binds stronger than binary operator
				((IASTUnaryExpression) l).setOperand(middle); 
				setEnd(l, middle);
				middle= l;				// middle becomes unary-expr, can be put into r (a binary-expr)
				l= (IASTExpression) l.getParent(); 
			} else {
				if (r== null || getPrecendence((IASTBinaryExpression) l) >= getPrecendence(r)) {
					((IASTBinaryExpression)l).setOperand2(middle);
					setEnd(l, middle);
					middle= l;			// middle becomes binary, can be put into r because precedence is greater or equal.
					l= (IASTExpression) l.getParent();
				} else {
					r.setOperand1(middle);
					setStart(r, middle);
					middle= r;			// middle becomes binary, can be put into r because precedence is greater.
					r= (IASTBinaryExpression) r.getParent();
				}
			}
		}
	}

	private int getPrecendence(IASTBinaryExpression r) {
		switch(r.getOperator()) {
		case IASTBinaryExpression.op_ellipses:
		case IASTBinaryExpression.op_assign:
		case IASTBinaryExpression.op_binaryAndAssign:
		case IASTBinaryExpression.op_binaryOrAssign:
		case IASTBinaryExpression.op_binaryXorAssign:
		case IASTBinaryExpression.op_divideAssign:
		case IASTBinaryExpression.op_minusAssign: 
		case IASTBinaryExpression.op_moduloAssign: 
		case IASTBinaryExpression.op_multiplyAssign:
		case IASTBinaryExpression.op_plusAssign: 
		case IASTBinaryExpression.op_shiftLeftAssign: 
		case IASTBinaryExpression.op_shiftRightAssign: 
			return 0;
		case IASTBinaryExpression.op_logicalOr:
			return 1;
		case IASTBinaryExpression.op_logicalAnd:
			return 2;
		case IASTBinaryExpression.op_binaryOr:
			return 3;
		case IASTBinaryExpression.op_binaryXor:
			return 4;
		case IASTBinaryExpression.op_binaryAnd:
			return 5;
		case IASTBinaryExpression.op_equals:
		case IASTBinaryExpression.op_notequals:
			return 6;
		case IASTBinaryExpression.op_greaterThan:
		case IASTBinaryExpression.op_greaterEqual:
		case IASTBinaryExpression.op_lessThan:
		case IASTBinaryExpression.op_lessEqual:
		case IASTBinaryExpression.op_max:
		case IASTBinaryExpression.op_min:
			return 7;
		case IASTBinaryExpression.op_shiftLeft:
		case IASTBinaryExpression.op_shiftRight:
			return 8;
		case IASTBinaryExpression.op_plus:
		case IASTBinaryExpression.op_minus:
			return 9;
		case IASTBinaryExpression.op_multiply:
		case IASTBinaryExpression.op_divide:
		case IASTBinaryExpression.op_modulo:
			return 10;
		case IASTBinaryExpression.op_pmarrow: 
		case IASTBinaryExpression.op_pmdot: 
			return 11;
		}
		assert false;
		return 0;
	}

	private IASTUnaryExpression findTrailingBracketedPrimaryExpression(IASTExpression expr) {
		while(true) {
			if (expr instanceof IASTBinaryExpression) {
				expr= ((IASTBinaryExpression) expr).getOperand2(); 
			} else if (expr instanceof IASTCastExpression) {
				expr= ((IASTCastExpression)expr).getOperand();
			} else if (expr instanceof IASTUnaryExpression) {
				IASTUnaryExpression u= (IASTUnaryExpression) expr;
				if (u.getOperator() == IASTUnaryExpression.op_bracketedPrimary) 
					return u;
				expr= u.getOperand();
			} else {
				return null;
			}
		}
	}
	
	private IASTExpression findLeadingCastExpression(IASTExpression expr) {
		while (expr instanceof IASTBinaryExpression) {
			expr= ((IASTBinaryExpression) expr).getOperand1(); 
		}
		return expr;
	}
}
