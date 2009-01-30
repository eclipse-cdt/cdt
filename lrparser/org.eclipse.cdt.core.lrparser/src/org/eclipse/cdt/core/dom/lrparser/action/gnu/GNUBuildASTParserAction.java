/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.gnu;

import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.lrparser.action.AbstractParserAction;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
import org.eclipse.cdt.internal.core.dom.lrparser.gcc.GCCParsersym;

public class GNUBuildASTParserAction extends AbstractParserAction {

	private final INodeFactory nodeFactory;
	
	private final TokenMap tokenMap;
	
	public GNUBuildASTParserAction(IParserActionTokenProvider parser, IASTTranslationUnit tu, ScopedStack<Object> astStack, INodeFactory nodeFactory) {
		super(parser, tu, astStack);
		
		this.nodeFactory = nodeFactory;
		this.tokenMap = new TokenMap(GCCParsersym.orderedTerminalSymbols, parser.getOrderedTerminalSymbols());
	}
	

	@Override
	protected IASTName createName(char[] image) {
		return nodeFactory.newName(image);
	}

	@Override
	protected boolean isCompletionToken(IToken token) {
		return tokenMap.mapKind(token.getKind()) == GCCParsersym.TK_Completion;
	}
	

	
	/**
	 * Add support for GCC extended ASM declaration syntax.
	 * 
	 * 
	 * asm_definition -- same as in C++ but its not in C99 spec so we put it here
     *     ::= 'asm' '(' 'stringlit' ')' ';'
     * 
     * extended_asm_declaration
     *     ::= 'asm' 'volatile' '(' extended_asm_param_seq ')' ';'
     *       | 'asm' '(' extended_asm_param_seq ')' ';'
     *       
	 */
	public void consumeDeclarationASM() {
		List<IToken> tokens = parser.getRuleTokens();
		
		int firstToken = 2; 
		if(tokenMap.mapKind(tokens.get(1).getKind()) == GCCParsersym.TK_volatile)
			firstToken = 3;
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(IToken token : tokens.subList(firstToken, tokens.size()-2)) {
			if(!first)
				sb.append(' ');
			sb.append(token.toString());
			first = false;
		}
		
		IASTASMDeclaration asm = nodeFactory.newASMDeclaration(sb.toString());
		setOffsetAndLength(asm);
		astStack.push(asm);
	}


}
