/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.newparser;

import java.util.Stack;

public class ExpressionEvaluator extends NullParserCallback {

	public class ExpressionException extends Exception {
		public ExpressionException(String msg) {
			super(msg);
		}
	}
	
	private Stack stack = new Stack();
	
	private int popInt() {
		return ((Integer)stack.pop()).intValue();
	}
	
	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#expressionOperator(Token)
	 */
	public void expressionOperator(Token operator) throws Exception {
		
		int second = popInt(); 
		int first; 
		switch (operator.getType()) {
			
			case Token.tPLUS:
				first = popInt(); 
				stack.push(new Integer(first + second));
				break;
			case Token.tMINUS:
				first = popInt(); 
				stack.push(new Integer(first - second));
				break;
			case Token.tSTAR:
				first = popInt(); 			
				stack.push(new Integer(first * second));
				break;
			case Token.tDIV:
				first = popInt(); 
				stack.push(new Integer(first / second));
				break;
			case Token.tLT:
				first = popInt(); 			
				stack.push(new Integer(first < second ? 1 : 0));
				break;
			case Token.tLTEQUAL:
				first = popInt(); 			
				stack.push(new Integer(first <= second ? 1 : 0));
				break;
			case Token.tGT:
				first = popInt(); 			
				stack.push(new Integer(first > second  ? 1 : 0));
				break;
			case Token.tGTEQUAL:
				first = popInt(); 			
				stack.push(new Integer(first >= second  ? 1 : 0));
				break;
			case Token.tEQUAL:
				first = popInt(); 			
				stack.push(new Integer(first == second  ? 1 : 0));
				break;
			case Token.tNOTEQUAL:
				first = popInt(); 			
				stack.push(new Integer(first != second  ? 1 : 0));
				break;
			case Token.tAND:
				first = popInt(); 			
				stack.push( new Integer( ( ( first != 0 ) && ( second != 0 ) ) ? 1 : 0  ) ); 
				break; 
			case Token.tOR:
				first = popInt(); 			
				stack.push( new Integer( ( ( first != 0 ) || ( second != 0 ) ) ? 1 : 0  ) ); 
				break;
			case Token.tNOT: 
				stack.push( new Integer( ( second == 0 ) ? 1 : 0 ) ); 
				break;
			default:
				throw new ExpressionException("Unhandled operator: " + operator );
		}
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#expressionTerminal(Token)
	 */
	public void expressionTerminal(Token terminal) throws Exception {
		switch (terminal.getType()) {
			case Token.tINTEGER:
				stack.push(new Integer(terminal.getImage()));
				break;
			default:
				throw new ExpressionException("Unhandled terminal: " + terminal.getImage());
		}
	}
	
	public Object getResult() {
		return stack.peek();
	}

}
