/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.c99;

import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_identifier;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_int;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import lpg.lpgjavaruntime.IToken;
import lpg.lpgjavaruntime.Token;

import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Variable;

public class ResolverActionTests extends TestCase {

	/**
	 * We are testing the parser actions in isolation without running
	 * an actual parser, therefore we need to mock out the parser object.
	 */
	private static class MockParser implements IParserActionTokenProvider {

		public List<IToken> ruleTokens;
		
		public MockParser(Object ... tokenTypes) {
			this.ruleTokens = tokens(tokenTypes);
		}
		public List<IToken> getCommentTokens() { 
			return null; 
		}
		public IToken getEOFToken() { 
			return null; 
		}
		public IToken getLeftIToken() {
			return ruleTokens.get(0);
		}
		public IToken getRightIToken() {
			return ruleTokens.get(ruleTokens.size()-1);
		}
		public List<IToken> getRuleTokens() {
			return ruleTokens;
		}
		public void setRuleTokens(Object ... tokenTypes) {
			this.ruleTokens = tokens(tokenTypes);
		}
		static List<IToken> tokens(Object[] tokenTypes) {
			List<IToken> tokens = new ArrayList<IToken>();
			if(tokenTypes == null)
				return tokens;
			
			for(final Object o : tokenTypes) {
				IToken token;
				if(o instanceof Integer)
					token = new Token(0, 0, ((Integer)o).intValue());
				else if(o instanceof String)
					token = new Token(0, 0, TK_identifier) {
						@Override public String toString() {
							return o.toString();
						}
					};
				else
					throw new AssertionFailedError();
				
				tokens.add(token);
			}
			return tokens;
		}
	}
	
	
	/**
	 * Parsing: int x;, then undo, then parse again
	 */
	@SuppressWarnings("deprecation")
	public void testResolverActions1() {
		MockParser mockParser = new MockParser();
		C99ResolveParserAction action = new C99ResolveParserAction(mockParser);
		
		mockParser.setRuleTokens(TK_int);
		action.openDeclarationScope();
		action.consumeDeclSpecToken();
		mockParser.setRuleTokens("x");
		action.consumeDirectDeclaratorIdentifier();
		action.consumeDeclaratorComplete();
		action.closeDeclarationScope();
		
		C99SymbolTable symbolTable;
		symbolTable = action.getSymbolTable();
		assertEquals(1, symbolTable.size());
		C99Variable binding = (C99Variable) symbolTable.lookup(CNamespace.IDENTIFIER, "x");
		assertEquals("x", binding.getName());
		
		// cool, now undo!
		assertEquals(5, action.undoStackSize());
		action.undo(5);
		assertEquals(0, action.undoStackSize());
		assertEquals(0, action.getDeclarationStack().size());
		symbolTable = action.getSymbolTable();
		assertTrue(symbolTable.isEmpty());
		
		// rerun
		mockParser.setRuleTokens(TK_int);
		action.openDeclarationScope();
		action.consumeDeclSpecToken();
		mockParser.setRuleTokens("x");
		action.consumeDirectDeclaratorIdentifier();
		action.consumeDeclaratorComplete();
		action.closeDeclarationScope();
		
		symbolTable = action.getSymbolTable();
		assertEquals(1, symbolTable.size());
		binding = (C99Variable) symbolTable.lookup(CNamespace.IDENTIFIER, "x");
		assertEquals("x", binding.getName());
	}
}


