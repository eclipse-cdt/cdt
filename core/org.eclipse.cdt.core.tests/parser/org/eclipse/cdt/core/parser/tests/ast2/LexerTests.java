/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer;
import org.eclipse.cdt.internal.core.parser.scanner.Token;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;


public class LexerTests extends BaseTestCase {
	static String TRIGRAPH_REPLACES_CHARS= "#^[]|{}~\\";
	static String TRIGRAPH_CHARS= "='()!<>-/";

	public static TestSuite suite() {
		return suite(LexerTests.class);
	}

	private Lexer fLexer;
	private TestLexerLog fLog= new TestLexerLog();
	private int fLastEndOffset;

	public LexerTests() {
		super();
	}

	public LexerTests(String name) {
		super(name);
	}

	private void init(String input) throws Exception {
		fLog.clear();
		fLexer= new Lexer(input.toCharArray(), new LexerOptions(), fLog);
		fLog.setInput(input);
		fLexer.nextToken();
		fLastEndOffset= 0;
	}

	private void init(String input, boolean dollar, boolean minmax) throws Exception {
		fLog.clear();
		final LexerOptions lexerOptions = new LexerOptions();
		lexerOptions.fSupportDollarInitializers= dollar;
		lexerOptions.fSupportMinAndMax= minmax;
		fLexer= new Lexer(input.toCharArray(), lexerOptions, fLog);
		fLexer.nextToken();
		fLastEndOffset= 0;
	}

	private void token(int tokenType) throws Exception {
		token(tokenType, null);
	}
	
	private void token(int tokenType, String image) throws Exception {
		Token t= fLexer.currentToken();
		assertEquals(tokenType, t.getType());
		assertEquals(fLastEndOffset, t.getOffset());
		fLastEndOffset= t.getEndOffset();
		if (image != null) {
			assertEquals(image, new String(t.getCharImage()));
		}
		fLexer.nextToken();
	}
	
	private void integer(String expectedImage) throws Exception {
		token(IToken.tINTEGER, expectedImage);
	}

	private void floating(String expectedImage) throws Exception {
		token(IToken.tFLOATINGPT, expectedImage);
	}

	private void id(String expectedImage) throws Exception {
		token(IToken.tIDENTIFIER, expectedImage);
	}

	private void str(String expectedImage) throws Exception {
		token(IToken.tSTRING, "\"" + expectedImage + "\"");
	}

	private void wstr(String expectedImage) throws Exception {
		token(IToken.tLSTRING, "L\"" + expectedImage + "\"");
	}

	private void ch(String expectedImage) throws Exception {
		token(IToken.tCHAR, expectedImage);
	}

	private void wch(String expectedImage) throws Exception {
		token(IToken.tLCHAR, expectedImage);
	}

	private void eof() throws Exception {
		IToken t= fLexer.nextToken();
		assertEquals("superfluous token " + t, Lexer.tEND_OF_INPUT, t.getType());
		assertEquals(0, fLog.getProblemCount());
		assertEquals(0, fLog.getCommentCount());
	}
	
	private void nl() throws Exception {
		token(Lexer.tNEWLINE);
	}

	private void ws() throws Exception {
		int offset= fLexer.currentToken().getOffset();
		assertTrue(offset > fLastEndOffset);
		fLastEndOffset= offset;
	}

	private void problem(int kind, String img) throws Exception {
		assertEquals(fLog.createString(kind, img), fLog.removeFirstProblem());
	}

	private void comment(String img) throws Exception {
		ws();
		assertEquals(img, fLog.removeFirstComment());
	}

	public void testTrigraphSequences() throws Exception {
		init("\"??=??/??'??(??)??!??<??>??-\"");
		str("#\\^[]|{}~");
		eof();
		
		init("??=??'??(??)??!??<??>??-");
		token(IToken.tPOUND);
		token(IToken.tXOR);
		token(IToken.tLBRACKET);
		token(IToken.tRBRACKET);
		token(IToken.tBITOR);
		token(IToken.tLBRACE);
		token(IToken.tRBRACE);
		token(IToken.tCOMPL);
		eof();
		
		init("a??/\nb");
		id("ab");
		eof();
	}
	
	public void testWindowsLineEnding() throws Exception {
		init("\n\n");
		nl(); nl(); eof();
		init("\r\n\r\n");
		nl(); nl(); eof();
	}
	
	public void testLineSplicingTrigraph() throws Exception {
		// a trigraph cannot be spliced
		init("??\\\n="); 
		token(IToken.tQUESTION);
		token(IToken.tQUESTION);
		token(IToken.tASSIGN);
		eof();

		init("??\\\r\n="); 
		token(IToken.tQUESTION);
		token(IToken.tQUESTION);
		token(IToken.tASSIGN);
		eof();
		
		// trigraph can be used to splice a line
		init("a??/\nb");
		id("ab");
		eof();
	}
		
	public void testLineSplicingStringLiteral() throws Exception {
		// splicing in string literal
		init("\"a\\\nb\""); 
		str("ab"); 
		eof();

		init("\"a\\\r\nb\"");
		str("ab"); 
		eof();
	}

	public void testLineSplicingCharLiteral() throws Exception {
		init("'a\\\nb'"); 
		ch("'ab'"); 
		eof();

		init("'a\\\r\nb'");
		ch("'ab'"); 
		eof();
	}

	public void testLineSplicingHeaderName() throws Exception {
		init("p\"a\\\nb\""); 
		fLexer.setInsideIncludeDirective();
		id("p");
		token(Lexer.tQUOTE_HEADER_NAME, "\"ab\""); 
		eof();

		init("p\"a\\\r\nb\"");
		fLexer.setInsideIncludeDirective();
		id("p");
		token(Lexer.tQUOTE_HEADER_NAME, "\"ab\""); 
		eof();

		init("p<a\\\nb>"); 
		fLexer.setInsideIncludeDirective();
		id("p");
		token(Lexer.tSYSTEM_HEADER_NAME, "<ab>"); 
		eof();

		init("p<a\\\r\nb>");
		fLexer.setInsideIncludeDirective();
		id("p");
		token(Lexer.tSYSTEM_HEADER_NAME, "<ab>"); 
		eof();
	}

	public void testLineSplicingComment() throws Exception {
		init("// a\\\nb\n");
		comment("// a\\\nb");
		nl();
		eof();

		init("// a\\\nb\n");
		comment("// a\\\nb");
		nl();
		eof();

		init("/\\\n\\\n/ ab\n");
		comment("/\\\n\\\n/ ab");
		nl();
		eof();

		init("/\\\n* a\\\nb*\\\n/");
		comment("/\\\n* a\\\nb*\\\n/");
		eof();
	}

	public void testLineSplicingIdentifier() throws Exception {
		init("a\\\nb");
		id("ab");
		eof();

		init("a\\\r\nb");
		id("ab");
		eof();
	}

	public void testLineSplicingNumber() throws Exception {
		init(".\\\n1");
		floating(".1");
		eof();

		init(".\\\r\n1");
		floating(".1");
		eof();
	}

	public void testComments() throws Exception {
		init("// /*\na");
		comment("// /*");
		nl();
		id("a");
		eof();
		
		init("/* // /* \n xxx*/a");
		comment("/* // /* \n xxx*/");
		id("a");
		eof();
	}
	
	public void testHeaderName() throws Exception {
		init("p\"'/*//\\\"");
		fLexer.setInsideIncludeDirective();
		id("p");
		token(Lexer.tQUOTE_HEADER_NAME, "\"'/*//\\\"");
		eof();

		init("p<'\"/*//>");
		fLexer.setInsideIncludeDirective();
		id("p");
		token(Lexer.tSYSTEM_HEADER_NAME, "<'\"/*//>");
		eof();
	}
	
	public void testIdentifier() throws Exception {
		final String ident= "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_$\\u1234\\U123456780123456789";
		int unc1= ident.indexOf('\\');
		for (int i = 0; i <= unc1; i++) {
			String id= ident.substring(i);
			init(id); 
			id(id);
			eof();
		}
		String id= ident.substring(ident.indexOf('\\', unc1+1));
		init(id); 
		id(id);
		eof();
		
		for (int i= 0; i <10; i++) {
			String nonid= ident.substring(ident.length()-i-1);
			init(nonid);
			integer(nonid);
			eof();
		}
		
		init(ident, false, true); 
		final int idxDollar = ident.indexOf('$');
		id(ident.substring(0, idxDollar));
		problem(IProblem.SCANNER_BAD_CHARACTER, "$");
		ws();
		id(ident.substring(idxDollar+1));
	}
	
	public void testNumber() throws Exception {
		final String number= ".0123456789.abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_" +
			"\\uaaaa\\Uaaaaaaaae+e-E+E-";
		for (int i = 0; i < 11; i++) {
			String n= number.substring(i);
			init(n); 
			floating(n);
			eof();
		}
		int idxPlus= number.indexOf('+');
		for (int i = 11; i < number.length(); i++) {
			String n= number.substring(i);
			init(n);
			int startString= 0;
			if (i==11) {token(IToken.tDOT); startString=1;}
			if (i<idxPlus) id(n.substring(startString, idxPlus-i));
			if (i<idxPlus+1) token(IToken.tPLUS);
			if (i<idxPlus+2) id("e");
			if (i<idxPlus+3) token(IToken.tMINUS);
			if (i<idxPlus+4) id("E");
			if (i<idxPlus+5) token(IToken.tPLUS);
			if (i<idxPlus+6) id("E");
			token(IToken.tMINUS);
			eof();
		}
	}
	
	public void testCharLiteral() throws Exception {
		String lit= "'abc0123\\'\".:; \\\\'";
		init(lit);
		ch(lit);
		eof();

		lit= 'L'+lit;
		init(lit);
		wch(lit);
		eof();

		lit= "'ut\n";
		init(lit);
		problem(IProblem.SCANNER_BAD_CHARACTER, "'ut");
		ch("'ut");
		nl();
		eof();

		lit= 'L'+lit;
		init(lit);
		problem(IProblem.SCANNER_BAD_CHARACTER, "L'ut");
		wch("L'ut");
		nl();
		eof();
		
		lit= "'ut\\'";
		init(lit);
		problem(IProblem.SCANNER_BAD_CHARACTER, lit);
		ch("'ut\\'");
		eof();

		lit= 'L'+lit;
		init(lit);
		problem(IProblem.SCANNER_BAD_CHARACTER, lit);
		wch("L'ut\\'");
		eof();
	}

	public void testStringLiteral() throws Exception {
		String lit= "abc0123\\\"'.:; \\\\";
		init('"' + lit + '"');
		str(lit);
		eof();

		init("L\"" + lit + '"');
		wstr(lit);
		eof();

		lit= "ut\n";
		init('"' + lit);
		problem(IProblem.SCANNER_UNBOUNDED_STRING, "\"ut");
		token(IToken.tSTRING, "\"ut");
		nl();
		eof();

		init("L\"" + lit);
		problem(IProblem.SCANNER_UNBOUNDED_STRING, "L\"ut");
		token(IToken.tLSTRING, "L\"ut");
		nl();
		eof();
		
		lit= "\"ut\\\"";
		init(lit);
		problem(IProblem.SCANNER_UNBOUNDED_STRING, lit);
		token(IToken.tSTRING, "\"ut\\\"");
		eof();

		lit= 'L'+lit;
		init(lit);
		problem(IProblem.SCANNER_UNBOUNDED_STRING, lit);
		token(IToken.tLSTRING, "L\"ut\\\"");
		eof();
	}

	public void testOperatorAndPunctuators() throws Exception {
		final String ops= "{}[]###()<::><%%>%:%:%:;:...?.::..*+-*/%^&|~=!<>+=-=*=/=%=" +
		"^=&=|=<<>><<=>>===!=<=>=&&||++--,->*-><?>?\\";
		final int[] tokens= new int[] {
				IToken.tLBRACE, IToken.tRBRACE, IToken.tLBRACKET, IToken.tRBRACKET,	IToken.tPOUNDPOUND, 
				IToken.tPOUND, IToken.tLPAREN, IToken.tRPAREN, IToken.tLBRACKET, IToken.tRBRACKET, 
				IToken.tLBRACE, IToken.tRBRACE, IToken.tPOUNDPOUND, IToken.tPOUND, IToken.tSEMI, 
				IToken.tCOLON, IToken.tELLIPSIS, IToken.tQUESTION, IToken.tDOT, IToken.tCOLONCOLON, IToken.tDOT,
				IToken.tDOTSTAR, IToken.tPLUS, IToken.tMINUS, IToken.tSTAR, IToken.tDIV, IToken.tMOD,
				IToken.tXOR, IToken.tAMPER, IToken.tBITOR, IToken.tCOMPL, IToken.tASSIGN, IToken.tNOT, 
				IToken.tLT, IToken.tGT, IToken.tPLUSASSIGN, IToken.tMINUSASSIGN, IToken.tSTARASSIGN, 
				IToken.tDIVASSIGN, IToken.tMODASSIGN, IToken.tXORASSIGN, IToken.tAMPERASSIGN, 
				IToken.tBITORASSIGN, IToken.tSHIFTL, IToken.tSHIFTR, IToken.tSHIFTLASSIGN, 
				IToken.tSHIFTRASSIGN, IToken.tEQUAL, IToken.tNOTEQUAL, IToken.tLTEQUAL, IToken.tGTEQUAL,
				IToken.tAND, IToken.tOR, IToken.tINCR, IToken.tDECR, IToken.tCOMMA, IToken.tARROWSTAR,
				IToken.tARROW, IGCCToken.tMIN, IGCCToken.tMAX, IToken.tBACKSLASH,
			};
		
		for (int splices=0; splices<9; splices++) {
			for (int trigraphs= 0; trigraphs<6; trigraphs++) {
				StringBuffer buf= new StringBuffer();
				String input= useTrigraphs(ops.toCharArray(), trigraphs);
				init(instertLineSplices(input, splices)); 
				for (int i = 0; i < tokens.length; i++) {
					Token token= fLexer.currentToken();
					buf.append(token.getCharImage());
					token(tokens[i]);
				}
				eof();
				assertEquals(ops, buf.toString()); // check token image

				init(input, true, false); 
				for (int i = 0; i < tokens.length; i++) {
					switch (tokens[i]) {
					case IGCCToken.tMIN:
						token(IToken.tLT);
						token(IToken.tQUESTION);
						break;
					case IGCCToken.tMAX:
						token(IToken.tGT);
						token(IToken.tQUESTION);
						break;
					default:
						token(tokens[i]);
					break;
					}
				}
				eof();
			}
		}
	}

	private String instertLineSplices(String input, int splices) {
		int m1= splices%3;
		int m2= (splices-m1)/3;
		char[] c= input.toCharArray();
		StringBuffer result= new StringBuffer();
		for (int i = 0; i < c.length; i++) {
			result.append(c[i]);
			if (c[i]=='?' && i+2 < c.length && c[i+1] == '?' && TRIGRAPH_CHARS.indexOf(c[i+2]) >= 0) {
				result.append(c[++i]);
				result.append(c[++i]);
			}
			switch(m1) {
			case 1:
				result.append("\\\n");
				break;
			case 2:
				result.append("\\ \n");
				break;
			}
			switch(m2) {
			case 1:
				result.append("\\\r\n");
				break;
			case 2:
				result.append("\\\t\r\n");
				break;
			}
		}
		return result.toString();
	}

	private String useTrigraphs(char[] input, int mode) {
		if (mode == 0) {
			return new String(input);
		}

		boolean yes= mode > 1;
		StringBuffer result= new StringBuffer();
		for (int i = 0; i < input.length; i++) {
			char c = input[i];
			int idx= TRIGRAPH_REPLACES_CHARS.indexOf(c);
			if (idx > 0) {
				if (yes) {
					result.append("??");
					result.append(TRIGRAPH_CHARS.charAt(idx));
				}
				else {
					result.append(c);
				}
				if (mode < 3) {
					yes= !yes;
				}
			}
			else {
				result.append(c);
			}
		}
		return result.toString();
	}
	
	public void testLineSplicingOperator() throws Exception {
		// splicing in operator
		init("|\\\n|");
		token(IToken.tOR);
		eof();
		
		init("|\\\r\n|");
		token(IToken.tOR); 
		eof();
	}
}
