/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * The code in this source file is derived from the Open C++ parser
 * which has the following copyright:
 * 
 * Copyright (C) 1997-2001 Shigeru Chiba, Tokyo Institute of Technology.
 * Permission to use, copy, distribute and modify this software and
 * its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appear in all copies and that 
 * both that copyright notice and this permission notice appear in 
 * supporting documentation.
 * 
 * Shigeru Chiba makes no representations about the suitability of this
 * software for any purpose.  It is provided "as is" without express or
 * implied warranty.
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.core.newparser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

public class Parser {

	public Parser(Scanner s, IParserCallback c, boolean quick) throws Exception {
		callback = c;
		scanner = s;
		quickParse = quick;
		scanner.setQuickScan(quick);
		scanner.setCallback(c);
		//fetchToken();
	}

	public Parser(Scanner s, IParserCallback c) throws Exception {
		this(s, c, false);
	}
	
	public Parser(Scanner s) throws Exception {
		this(s, new NullParserCallback(), false);
	}
	
	public Parser(String code) throws Exception {
		this(new Scanner(new StringReader(code)));
	}

	public Parser(String code, IParserCallback c) throws Exception {
		this(new Scanner(new StringReader(code)), c, false);
	}

	public Parser(InputStream stream, IParserCallback c, boolean quick) throws Exception {
		this(new Scanner(new InputStreamReader(stream)), c, quick);
	}
	
	private static int parseCount = 0;
	
	private boolean parsePassed = true;
	
	public boolean parse() throws Exception {
		long startTime = System.currentTimeMillis();
		rTranslationUnit();
		System.out.println("Parse " + (++parseCount) + ": "
			+ ( System.currentTimeMillis() - startTime ) + "ms"
			+ ( parsePassed ? "" : " - parse failure" ));
			
		return parsePassed;
	}

	// The callback
	protected IParserCallback callback;

	// For quick parse, skip over stuff
	protected boolean quickParse = false;
	
	// The backtrack exception
	static public class Backtrack extends Exception {
	}
	
	private Backtrack backtrack = new Backtrack();
	
	// translationUnit
	// : ( definition )*
	public void rTranslationUnit() throws Exception {
		Token lastBacktrack = null;
		callback.translationUnitBegin();
		while (LT(1) != Token.tEOF) {
			try {
				rDefinition();
			} catch (Backtrack b) {
				// Mark as failure and try to reach a recovery point
				parsePassed = false;
				
				if (lastBacktrack == null) {
					// First backtrack, see if we can continue from here
					lastBacktrack = LA(1);
				} else if (lastBacktrack == LA(1)) {
					// we haven't progressed from the last backtrack
					// try and find tne next definition
					for (int t = LT(1); t != Token.tEOF; t = LT(1)) {
						consume();
						// TO DO: we should really check for matching braces too
						if (t == Token.tSEMI)
							break;
					}
				} else {
					// else we've progressed, start again from here
					lastBacktrack = LA(1);
				}
			}
		}
		callback.translationUnitEnd();
		return;
	}
	
	// definition
	// : nullDeclaration
	// | typedef
	// | templateDecl
	// | linkageSpec
	// | externTemplateDecl
	// | namespaceSpec
	// | using
	// | declaration
	public void rDefinition() throws Exception {
		switch (LT(1)) {
			case Token.tSEMI:
				rNullDeclaration();
				return;
			case Token.t_typedef:
				rTypedef();
				return;
			case Token.t_template:
				rTemplateDecl();
				return;
			case Token.t_extern:
				switch (LT(2)) {
					case Token.tSTRING:
						rLinkageSpec();
						return;
					case Token.t_template:
						rExternTemplateDecl();
						return;
				}
				break;
			case Token.t_namespace:
				rNamespaceSpec();
				return;
			case Token.t_using:
				rUsing();
				return;
		}
		
		rDeclaration();
	}
	
	// nullDeclaration
	// : ";"
	public void rNullDeclaration() throws Exception {
		consume(Token.tSEMI);
	}
	
	// typedef
	// : typeSpecifier declarators ";"
	public void rTypedef() throws Exception {
		consume(Token.t_typedef);
		rTypeSpecifier();
		rDeclarators(true, false);
		consume(Token.tSEMI);
	}
	
	// typeSpecifier
	// : (cvQualify)? (integralTypeOrClassSpec | name) (cvQualify)?
	public void rTypeSpecifier() throws Exception {
		optCvQualify();
		
		if (!optIntegralTypeOrClassSpec())
			rName();

		optCvQualify();
	}
	
	public boolean isTypeSpecifier() throws Exception { return false; }
	
	public void rLinkageSpec() throws Exception {
		consume(Token.t_extern);
		consume(Token.tSTRING);		
			
		if (LT(1) == Token.tLBRACE) {
			rLinkageBody();
		} else {
			rDefinition();
		}
	}
	
	// namespaceSpec
	// : "namespace" identifier definition
	// | "namespace" (identifier)? linkageBody
	public void rNamespaceSpec() throws Exception {
		consume(Token.t_namespace);
		
		if (LT(1) == Token.tIDENTIFIER)
			consume();
		else if (LT(1) != Token.tLBRACE)
			throw new ParserException(LA(1));

		if (LT(1) == Token.tLBRACE)
			rLinkageBody();
		else
			rDefinition();
	}
	
	public void rUsing() throws Exception {
		consume(Token.t_using);
			
		if (LT(1) == Token.t_namespace)
			consume();
		
		rName();
		consume(Token.tSEMI);		
	}
	
	public void rLinkageBody() throws Exception {
		consume(Token.tLBRACE);
		while (LT(1) != Token.tRBRACE) {
			rDefinition();
		}
		consume();
	}
	
	public void rTemplateDecl() throws Exception {
		rTemplateDecl2();
		rDeclaration();
	}
	
	public void rTemplateDecl2() throws Exception {
		consume(Token.t_template);
		
		if (LT(1) == Token.tLT) {
			consume();
			rTempArgList();
			consume(Token.tGT);
				
			// Nested templates???
			while (LT(1) == Token.t_template) {
				consume();
				if (LT(1) != Token.tLT)
					break;
				consume();
				rTempArgList();
				consume(Token.tGT);
			}
		}
	}
	
	public void rTempArgList() throws Exception {
		if (LT(1) == Token.tGT) {
			return;
		}
		
		rTempArgDeclaration();
		
		while (LT(1) == Token.tCOMMA) {
			consume();
			rTempArgDeclaration();
		}
	}
	
	public void rTempArgDeclaration() throws Exception {
		if (LT(1) == Token.t_class && LT(2) == Token.tIDENTIFIER) {
			consume();
			consume();
			
			if (LT(1) == Token.tASSIGN) {
				consume();
				rTypeName();
			}
		} else if (LT(1) == Token.t_template) {
			rTemplateDecl2();
			
			consume(Token.t_class);
			consume(Token.tIDENTIFIER);
				
			if (LT(1) == Token.tASSIGN) {
				consume();
				rTypeName();
			}
		} else {
			rTypeSpecifier();
			rDeclarator(kArgDeclarator, false, true, false);
			
			if (LT(1) == Token.tASSIGN) {
				consume();
				rAdditiveExpr();
			}
		}
	}
	
	public void rExternTemplateDecl() throws Exception { throw backtrack; }
	
	// declaration
	// : integralDeclaration
	// | constDeclaration
	// | otherDeclaration
	//
	// declHead
	// : (memberSpec)? (storageSpec)? (memberSpec)? (cvQualify)?
	//
	// integralDeclaration
	// : integralDeclHead declarators (";" | functionBody)
	// | integralDeclHead ";"
	// | integralDeclHead ":" expression ";"
	//
	// integralDeclHead
	// : declHead integralOrClassSpec (cvQualify)?
	//
	// otherDeclaration
	// : declHead name (cvQualifer)? declarators (";" | functionBody)
	// | delcHead name constructorDecl (";" | functionBody)
	// | "friend" name ";"
	//
	// constDeclaration
	// : cvQualify ("*")? identifier "=" expression ("," declarators) ";"
	public void rDeclaration() throws Exception {
		callback.simpleDeclarationBegin(LA(1));
		boolean memberSpec = optMemberSpec();
		optStorageSpec();
		if (!memberSpec)
			optMemberSpec();
		boolean cv = optCvQualify();
		if (optIntegralTypeOrClassSpec()) {
			rIntegralDeclaration();
		} else {
			if (cv && ((LT(1) == Token.tIDENTIFIER && LT(2) == Token.tASSIGN)
				|| LT(1) == Token.tSTAR))
				rConstDeclaration();
			else
				rOtherDeclaration();
		}
		callback.simpleDeclarationEnd(LA(0));
	}
	
	public void rIntegralDeclaration() throws Exception {
		optCvQualify();
		
		switch (LT(1)) {
			case Token.tSEMI:
				consume();
				return;
			case Token.tCOLON:
				consume();
				rExpression();
				consume(Token.tSEMI);
				return;
			default:
				rDeclarators(true, false);
				
				if (LT(1) == Token.tSEMI) {
					consume();
					return;
				} else
					rFunctionBody();
		}
	}
	
	public void rConstDeclaration() throws Exception { throw backtrack; }
	
	public void rOtherDeclaration() throws Exception {
		callback.declaratorBegin();
		Token id = LA(1);
		rName();
		callback.declaratorId(id);

		if (isConstructorDecl()) {
			rConstructorDecl();
		} else if (LT(1) == Token.tSEMI) {
			// "friend" name ";"
			// TO DO: check for "friend"
			consume();
		} else {
			optCvQualify();
			rDeclarators(false, false);
		}
		
		if (LT(1) == Token.tSEMI) {
			consume();
		} else {
			rFunctionBody();
		}
		
		callback.declaratorEnd();
	}
	
	public boolean isConstructorDecl() throws Exception {
		if (LT(1) != Token.tLPAREN)
			return false;
		else {
			int t = LT(2);
			if (t == Token.tSTAR || t == Token.tAMPER || t == Token.tLPAREN )
				return false;
			else if (t == Token.t_const || t == Token.t_volatile)
				return true;
			else if (isPtrToMember(2))
				return false;
			else
				return true;
		}
	}
	
	public boolean isPtrToMember(int i) throws Exception {
		int t0 = LT(i++);
		
		if (t0 == Token.tCOLONCOLON)
			t0 = LT(i++);
			
		while (t0 == Token.tIDENTIFIER) {
			int t = LT(i++);
			if (t == Token.tLT) {
				int n = 1;
				while (n > 0) {
					int u = LT(i++);
					if (u == Token.tLT)
						++n;
					else if (u == Token.tGT)
						--n;
					else if (u == Token.tLPAREN) {
						int m = 1;
						while (m > 0) {
							int v = LT(i++);
							if (v == Token.tLPAREN)
								++m;
							else if (v == Token.tRPAREN)
								--m;
							else if (v == Token.tEOF || v == Token.tSEMI || v == Token.tRBRACE )
								return false;
						}
					} else if (u == Token.tEOF || u == Token.tSEMI || u == Token.tRBRACE )
						return false;
				}
				
				t = LT(i++);
			}
			
			if (t != Token.tCOLONCOLON)
				return false;
			
			t0 = LT(i++);
			if (t0 == Token.tSTAR)
				return true;
		}
		
		return false;
	}
	
	// memberSpec
	// : ("friend" | "inline" | "virtual")+
	public boolean optMemberSpec() throws Exception {
		boolean rc = false;
		int la = LT(1);
		while (la == Token.t_friend
			| la == Token.t_inline
			| la == Token.t_virtual ) {
				rc = true;
				consume();
				la = LT(1);
			}
		return rc;
	}
	
	// storageSpec
	// : "static" | "extern" | "auto" | "register" | "mutable"
	public boolean optStorageSpec() throws Exception {
		switch (LT(1)) {
			case Token.t_static:
			case Token.t_extern:
			case Token.t_auto:
			case Token.t_register:
			case Token.t_mutable:
				consume();
				return true;
			default:
				return false;
		}
	}
	
	// cvQualify
	// : ("const" | "valatile")+
	public boolean optCvQualify() throws Exception {
		boolean rc = false;
		int la = LT(1);
		while (la == Token.t_const || la == Token.t_volatile) {
			rc = true;
			consume();
			la = LT(1);
		}
		return rc;
	}
	
	// integralTypeOrClassSpec
	// : ("char" | "int" | "short" | "long" | "signed" | "unsigned" | "float" | "double"
	//   | "void" | "bool")+
	// | classSpec
	// | enumSpec
	public boolean optIntegralTypeOrClassSpec() throws Exception {
		boolean rc = false;
		
		integral:
		for (;;) {
			switch (LT(1)) {
				case Token.t_char:
				case Token.t_int:
				case Token.t_short:
				case Token.t_long:
				case Token.t_signed:
				case Token.t_unsigned:
				case Token.t_float:
				case Token.t_double:
				case Token.t_void:
				case Token.t_bool:
				case Token.t_wchar_t:
					Token t = consume();
					callback.declSpecifier(t);
					rc = true;
					break;
				default:
					break integral;
			}
		}
		
		if (rc)
			return true;
			
		try {
			rClassSpec();
			return true;
		} catch (Backtrack b) {
		}
		
		try {
			rEnumSpec();
			return true;
		} catch (Backtrack b) {
		}
		
		return false;
	}
	
	public void rConstructorDecl() throws Exception {
		callback.argumentsBegin();
		consume(Token.tLPAREN);
		
		if (LT(1) != Token.tRPAREN)
			rArgDeclList();
		
		consume(Token.tRPAREN);
		callback.argumentsEnd();
		
		optCvQualify();
		optThrowDecl();
		
		if (LT(1) == Token.tCOLON)
			rMemberInitializers();
		
		if (LT(1) == Token.tASSIGN) {
			consume();
			consume(Token.tINTEGER); // should be 0
		}
	}
	
	public boolean optThrowDecl() throws Exception { return false; }
	
	public void rDeclarators(boolean shouldBeDeclarator,
							  boolean isStatement) throws Exception
	{
		for (;;) {
			rDeclaratorWithInit(shouldBeDeclarator, isStatement);
			
			if (LT(1) == Token.tCOMMA)
				consume();
			else
				return;
		}
	}
	
	protected static final int kDeclarator = 0;
	protected static final int kArgDeclarator = 1;
	protected static final int kCastDeclarator = 2;
	
	public void rDeclaratorWithInit(boolean shouldBeDeclarator,
									 boolean isStatement) throws Exception
	{
		callback.declaratorBegin();
		if (LT(1) == Token.tCOLON) {
			consume();
			rExpression();
		} else {
			rDeclarator(kDeclarator, false, shouldBeDeclarator, isStatement);
			
			if (LT(1) == Token.tASSIGN) {
				consume();
				rInitializeExpr();
			} else if (LT(1) == Token.tCOLON) {
				consume();
				rExpression();
			}
		}
		callback.declaratorEnd();
	}
	
	public void rDeclarator(int kind,
							 boolean recursive,
							 boolean shouldBeDeclarator,
							 boolean isStatement) throws Exception
	{
		boolean ptrOperator = optPtrOperator();
		
		if (LT(1) == Token.tLPAREN) {
			consume();
			rDeclarator(kind, true, true, false);
			consume(Token.tRPAREN);
			
			if (!shouldBeDeclarator)
				if (kind == kDeclarator && !ptrOperator)
					if (LT(1) != Token.tLBRACKET && LT(1) != Token.tLPAREN)
						throw backtrack;
		} else if (kind != kCastDeclarator
				&& (kind == kDeclarator || LT(1) == Token.tIDENTIFIER || LT(1) == Token.tCOLONCOLON)) {
			Token id = LA(1);
			rName();
			callback.declaratorId(id);
		}
		
		for (;;) {
			if (LT(1) == Token.tLPAREN) { // function
				callback.argumentsBegin();
				consume();
				boolean args = false;
				if (LT(1) != Token.tRPAREN)
					args = rArgDeclListOrInit(isStatement);

				consume(Token.tRPAREN);
				callback.argumentsEnd();
				
				optCvQualify();
				optThrowDecl();
				
				if (LT(1) == Token.tCOLON)
					rMemberInitializers();
				return;
			} else if (LT(1) == Token.tLBRACKET) { // array
				consume();
				if (LT(1) != Token.tRBRACKET)
					rCommaExpression();
				consume(Token.tRBRACKET);
			} else
				return;
		}
	}
	
	// ptrOperator
	// : (("*" | "&" | ptrToMember) (cvQualify)?)+
	public boolean optPtrOperator() throws Exception {
		boolean rc = false;
		for (;;) {
			int t = LT(1);
			if (t != Token.tSTAR && t != Token.tAMPER && !isPtrToMember(1)) {
				return rc;
			} else {
				rc = true;
				if (t == Token.tSTAR || t == Token.tAMPER)
					consume();
				else
					try {
						rPtrToMember();
					} catch (Backtrack b) {
						return false;
					}
				optCvQualify();
			}
		}
	}
	
	public void rMemberInitializers() throws Exception { throw backtrack; }
	
	public void rMemberInit() throws Exception { throw backtrack; }
	
	// name
	// : ("::")? name2 ("::" name2)*
	//
	// name2
	// : identifier (templateArgs)?
	// | "~" identifier
	// | "operator" operatorName (templateArgs)?
	// returns list of names
	public void rName() throws Exception {
		if (LT(1) == Token.tCOLONCOLON) {
			consume();
		}

		for (;;) {
			int t = LT(1);
			
			if (t == Token.tIDENTIFIER) {
				Token tk = consume();
				t = LT(1);
				if (LT(1) == Token.tLT) {
					rTemplateArgs();
					t = LT(1);
				}
				if (t == Token.tCOLONCOLON)
					consume();
				else
					return;
			} else if (t == Token.tCOMPL) {
				consume();
				if (LT(1) == Token.tIDENTIFIER) {
					Token tk = consume();
					return;
				} else
					throw backtrack;
			} else if (t == Token.t_operator) {
				// TO DO: make a name here
				consume();
				rOperatorName();
				if (LT(1) == Token.tLT)
					rTemplateArgs();
				return;
			} else
				throw backtrack;
		}
	}
	
	public void rOperatorName() throws Exception {
		int t = LT(1);
		switch (t) {
			case Token.tPLUS:
			case Token.tMINUS:
			case Token.tSTAR:
			case Token.tDIV:
			case Token.tMOD:
			case Token.tXOR:
			case Token.tAMPER:
			case Token.tBITOR:
			case Token.tCOMPL:
			case Token.tNOT:
			case Token.tASSIGN:
			case Token.tLT:
			case Token.tGT:
			case Token.tPLUSASSIGN:
			case Token.tMINUSASSIGN:
			case Token.tSTARASSIGN:
			case Token.tDIVASSIGN:
			case Token.tMODASSIGN:
			case Token.tXORASSIGN:
			case Token.tAMPERASSIGN:
			case Token.tBITORASSIGN:
			case Token.tSHIFTL:
			case Token.tSHIFTR:
			case Token.tSHIFTLASSIGN:
			case Token.tSHIFTRASSIGN:
			case Token.tEQUAL:
			case Token.tNOTEQUAL:
			case Token.tLTEQUAL:
			case Token.tGTEQUAL:
			case Token.tAND:
			case Token.tOR:
			case Token.tINCR:
			case Token.tDECR:
			case Token.tCOMMA:
			case Token.tARROWSTAR:
			case Token.tARROW:
				consume();
				return;
			case Token.t_new:
			case Token.t_delete:
				consume();
				if (LT(1) == Token.tLBRACKET) {
					consume();
					consume(Token.tRBRACKET);
				}
				return;
			case Token.tLPAREN:
				consume();
				consume(Token.tRPAREN);
				return;
			case Token.tLBRACKET:
				consume();
				consume(Token.tRBRACKET);
				return;
			default:
				rCastOperatorName();
		}
	}
	
	public void rCastOperatorName() throws Exception { throw backtrack; }
	
	public void rPtrToMember() throws Exception { throw backtrack; }
	
	public void rTemplateArgs() throws Exception {
		consume(Token.tLT);
		
		if (LT(1) == Token.tGT) {
			consume();
			return;
		}
		
		for (;;) {
			Token mark = mark();
			try {
				rTypeName();
			} catch (Backtrack b) {
				backup(mark);
				rLogicalOrExpr(true);
			}
			
			switch (LT(1)) {
				case Token.tGT:
					consume();
					return;
				case Token.tCOMMA:
					consume();
					break;
				case Token.tSHIFTR:
					// Need some magic here to split >> into > >
					currToken.type = Token.tGT;
					return;
				default:
					throw backtrack;
			}
		}
	}
	
	// argDeclListOrInit
	// : argDeclList
	// | functionArguments
	public boolean rArgDeclListOrInit(boolean maybeInit) throws Exception {
		Token mark = mark();
		
		if (maybeInit) {
			try {
				rFunctionArguments();
				if (LT(1) == Token.tLPAREN) {
					return false;
				}
			} catch (Backtrack b) {
				backup(mark);
			}

			rArgDeclList();
			return true;
		} else {
			try {
				rArgDeclList();
				return true;
			} catch (Backtrack b) {
				backup(mark);
			}
			
			rFunctionArguments();
			return false;
		}
	}
	
	public void rArgDeclList() throws Exception {
		for (;;) {
			int t = LT(1);
			if (t == Token.tRPAREN) {
				break;
			} else if (t == Token.tELIPSE) {
				consume();
				break;
			} else {
				rArgDeclaration();
				t = LT(1);
				if (t == Token.tCOMMA)
					consume();
				else if (t != Token.tRPAREN && t != Token.tELIPSE)
					throw backtrack;
			}
		}
	}
	
	// argDeclaration
	// : ("register")? typeSpecifier argDeclarator ("=" expression)?
	public void rArgDeclaration() throws Exception {
		if (LT(1) == Token.t_register)
			consume();
		
		rTypeSpecifier();
		rDeclarator(kArgDeclarator, false, true, false);
		
		if (LT(1) == Token.tASSIGN) {
			consume();
			rInitializeExpr();
		}
	}
	
	public void rInitializeExpr() throws Exception {
		if (LT(1) != Token.tLBRACE)
			rExpression();
		else {
			consume();
			for (;;) {
				rInitializeExpr();
				
				switch (LT(1)) {
					case Token.tCOMMA:
						consume();
						break;
					case Token.tRBRACE:
						consume();
						return;
					default:
						throw new ParserException(LA(1));
				}
			}
		}
	}
	
	public void rFunctionArguments() throws Exception {
		if (LT(1) == Token.tRPAREN) {
			return;
		}
		
		for (;;) {
			rExpression();
			
			if (LT(1) == Token.tCOMMA)
				consume();
			else {
				return;
			}
		}
	}
	
	// enumSpec
	// : "enum" identifier
	// | "enum" (identifier)? "{" (enumBody)? "}"
	public void rEnumSpec() throws Exception {
		if (LT(1) == Token.t_enum)
			consume();
		else
			throw backtrack;
		
		if (LT(1) == Token.tIDENTIFIER) {
			consume();
			
			if (LT(1) != Token.tLBRACE)
				return;
		}
		
		consume(Token.tLBRACE);
		
		if (LT(1) != Token.tRBRACE)
			rEnumBody();

		consume(Token.tRBRACE);	
	}
	
	public void rEnumBody() throws Exception {
		for (;;) {
			if (LT(1) == Token.tRBRACE)
				return;

			consume(Token.tIDENTIFIER);			
			
			if (LT(1) == Token.tASSIGN) {
				consume();
				rExpression();
			}
			
			if (LT(1) == Token.tCOMMA)
				consume();
			else
				return;
		}
	}
	
	// classSpec
	// : classKey classBody
	// | classKey name (classBody)?
	// | classKey name ":" baseSpecifiers classBody
	static int anonymousName = 0;
	public void rClassSpec() throws Exception {
		int t = LT(1);
		String classKey = null;
		switch (t) {
			case Token.t_class:
				classKey = "class";
				break;
			case Token.t_struct:
				classKey = "struct";
				break;
			case Token.t_union:
				classKey = "union";
				break;
			default:
				throw backtrack;
		}
		
		consume();
	
		if (LT(1) != Token.tLBRACE) {
			Token nameToken = LA(1);
			rName();
			callback.classBegin(classKey, nameToken);
			callback.declSpecifier(nameToken);
			
			if (LT(1) == Token.tCOLON)
				rBaseSpecifiers();
			else if (LT(1) != Token.tLBRACE) {
				callback.classEnd();
				return;
			}
		} else {
			// anonymous class
			callback.classBegin(classKey, null);
			callback.declSpecifier(null);
		}
		
		rClassBody();
		callback.classEnd();
	}
	
	public void rBaseSpecifiers() throws Exception {
		consume(Token.tCOLON);
		
		for (;;) {
			int t = LT(1);
			
			if (t == Token.t_virtual) {
				consume();
				t = LT(1);
			}
			
			if (t == Token.t_public
				|| t == Token.t_protected
				|| t == Token.t_private) {
				consume();
				t = LT(1);
			}
			
			if (t == Token.t_virtual)
				consume();
			
			rName();
			
			if (LT(1) == Token.tCOMMA)
				consume();
			else
				return;
		}
	}
	
	public void rClassBody() throws Exception {
		consume(Token.tLBRACE);
		while (LT(1) != Token.tRBRACE) {
			rClassMember();
		}
		consume();
	}
	
	public void rClassMember() throws Exception {
		int t = LT(1);
		if (t == Token.t_public
			|| t == Token.t_protected
			|| t == Token.t_private)
		{
			consume();
			consume(Token.tCOLON);
		} else if (t == Token.tSEMI) {
			rNullDeclaration();
		} else if (t == Token.t_typedef) {
			rTypedef();
		} else if (t == Token.t_template) {
			rTemplateDecl();
		} else if (t == Token.t_using) {
			rUsing();
		} else {
			rDeclaration();
		}
	}
	
	public void rAccessDecl() throws Exception { throw backtrack; }
	
	public void rUserAccessSpec() throws Exception { throw backtrack; }
	
	public void rCommaExpression() throws Exception {
		rExpression();
		
		while (LT(1) == Token.tCOMMA) {
			Token t = consume();
			rExpression();
			callback.expressionOperator(t);
		}
	}
	
	public void rExpression() throws Exception {
		rConditionalExpr();
		
		switch (LT(1)) {
			case Token.tASSIGN:
			case Token.tSTARASSIGN:
			case Token.tDIVASSIGN:
			case Token.tMODASSIGN:
			case Token.tPLUSASSIGN:
			case Token.tMINUSASSIGN:
			case Token.tSHIFTRASSIGN:
			case Token.tSHIFTLASSIGN:
			case Token.tAMPERASSIGN:
			case Token.tXORASSIGN:
			case Token.tBITORASSIGN:
				Token t = consume();
				rExpression();
				callback.expressionOperator(t);
		}
	}
	
	public void rConditionalExpr() throws Exception {
		rLogicalOrExpr(false);
		
		if (LT(1) == Token.tQUESTION) {
			Token t = consume();
			rCommaExpression();
			consume(Token.tCOLON);
			rConditionalExpr();
			callback.expressionOperator(t);
		}
	}
	
	public void rLogicalOrExpr(boolean templateArgs) throws Exception {
		rLogicalAndExpr(templateArgs);
		
		while (LT(1) == Token.tOR) {
			Token t = consume();
			rLogicalAndExpr(templateArgs);
			callback.expressionOperator(t);
		}
	}
	
	public void rLogicalAndExpr(boolean templateArgs) throws Exception {
		rInclusiveOrExpr(templateArgs);
		
		while (LT(1) == Token.tAND) {
			Token t = consume();
			rInclusiveOrExpr(templateArgs);
			callback.expressionOperator(t);
		}
	}
	
	public void rInclusiveOrExpr(boolean templateArgs) throws Exception {
		rExclusiveOrExpr(templateArgs);
		
		while (LT(1) == Token.tBITOR) {
			Token t = consume();
			rExclusiveOrExpr(templateArgs);
			callback.expressionOperator(t);
		}
	}
	
	public void rExclusiveOrExpr(boolean templateArgs) throws Exception {
		rAndExpr(templateArgs);
		
		while (LT(1) == Token.tXOR) {
			Token t = consume();
			rAndExpr(templateArgs);
			callback.expressionOperator(t);
		}
	}
	
	public void rAndExpr(boolean templateArgs) throws Exception {
		rEqualityExpr(templateArgs);
		
		while (LT(1) == Token.tAMPER) {
			Token t = consume();
			rEqualityExpr(templateArgs);
			callback.expressionOperator(t);
		}
	}
	
	public void rEqualityExpr(boolean templateArgs) throws Exception {
		rRelationalExpr(templateArgs);
		
		for (;;) {
			switch (LT(1)) {
				case Token.tEQUAL:
				case Token.tNOTEQUAL:
					Token t = consume();
					rRelationalExpr(templateArgs);
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void rRelationalExpr(boolean templateArgs) throws Exception {
		rShiftExpr();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tGT:
					// For template args, the GT means end of args
					if (templateArgs)
						return;
				case Token.tLT:
				case Token.tLTEQUAL:
				case Token.tGTEQUAL:
					Token t = consume();
					rShiftExpr();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void rShiftExpr() throws Exception {
		rAdditiveExpr();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tSHIFTL:
				case Token.tSHIFTR:
					Token t = consume();
					rAdditiveExpr();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void rAdditiveExpr() throws Exception {
		rMultiplyExpr();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tPLUS:
				case Token.tMINUS:
					Token t = consume();
					rMultiplyExpr();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void rMultiplyExpr() throws Exception {
		rPmExpr();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tSTAR:
				case Token.tDIV:
				case Token.tMOD:
					Token t = consume();
					rPmExpr();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void rPmExpr() throws Exception {
		rCastExpr();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tDOTSTAR:
				case Token.tARROWSTAR:
					Token t = consume();
					rCastExpr();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
		
	public void rCastExpr() throws Exception {
		// TODO: expression incomplete
		if (LT(1) == Token.tLPAREN) {
			Token mark = mark();
			consume();
			
			try {
				rTypeName();

				if (LT(1) == Token.tRPAREN)
					consume();
				else
					throw backtrack;
				
				rCastExpr();
				return;
			} catch (Backtrack b) {
				backup(mark);
			}
		}

		rUnaryExpr();
	}
	
	public void rTypeName() throws Exception {
		try {
			rTypeSpecifier();
			return;
		} catch (Backtrack b) {
		}
		
		rDeclarator(kCastDeclarator, false, false, false);
	}
	
	// unaryExpr
	// : postfixExpr
	// | ("*" | "&" | "+" | "-" | "!" | "~" | "++" | "--" ) castExpr
	// | sizeofExpr
	// | allocateExpr
	// | throwExpr
	public void rUnaryExpr() throws Exception {
		switch (LT(1)) {
			case Token.tSTAR:
			case Token.tAMPER:
			case Token.tPLUS:
			case Token.tMINUS:
			case Token.tNOT:
			case Token.tCOMPL:
			case Token.tINCR:
			case Token.tDECR:
				Token t = consume();
				rCastExpr();
				callback.expressionOperator(t); 
				return;
			case Token.t_sizeof:
				rSizeofExpr();
				return;
			case Token.t_throw:
				rThrowExpr();
				return;
			default:
				if (isAllocateExpr())
					rAllocateExpr();
				else
					rPostfixExpr();
		}
	}
	
	public void rThrowExpr() throws Exception {
		consume(Token.t_throw);
		
		switch (LT(1)) {
			case Token.tCOLON:
			case Token.tSEMI:
				return;
			default:
				rExpression();
		}
	}
	
	public void rTypeidExpr() throws Exception { throw backtrack; }
	
	public void rSizeofExpr() throws Exception {
		consume(Token.t_sizeof);
		
		if (LT(1) == Token.tLPAREN) {
			Token mark = mark();
			consume();

			try {
				rTypeName();
				
				if (LT(1) == Token.tRPAREN)
					consume();
				else
					throw backtrack;
				
				rCastExpr();
				return;
			} catch (Backtrack b) {
				backup(mark);
			}
		}

		rUnaryExpr();
	}
	
	public boolean isAllocateExpr() throws Exception {
		int t = LT(1);
		
		if (t == Token.tCOLONCOLON)
			t = LT(2);
		
		return t == Token.t_new || t == Token.t_delete;
	}
	
	public void rAllocateExpr() throws Exception {
		int t = LT(1);
		
		if (t == Token.tCOLONCOLON) {
			consume();
			t = LT(1);
		}
		
		if (t == Token.t_delete) {
			consume();
			
			if (LT(1) == Token.tLBRACKET) {
				consume();
				consume(Token.tRBRACKET);
			}
			
			rCastExpr();
			return;
		} else if (t == Token.t_new) {
			consume();
			rAllocateType();
			return;
		} else
			throw new ParserException(LA(1));
	}
	
	// allocateType
	// : ("(" functionArguments ")")? typeSpecifier newDeclarator (allocateInitializer)?
	// | ("(" functionArguments ")")? "(" typeName ")" (allocateInitializer)?
	public void rAllocateType() throws Exception {
		if (LT(1) == Token.tLPAREN) {
			consume();
			
			Token mark = mark();
			try {
				rTypeName();
				
				if (LT(1) == Token.tRPAREN) {
					consume();
					if (LT(1) != Token.tLPAREN) {
						if (isTypeSpecifier())
							return;
						else
							throw backtrack;
					} else {
						rAllocateInitializer();
						
						if (LT(1) != Token.tLPAREN)
							return;
						else
							throw backtrack;
					}
				}
			} catch (Backtrack b) {
				backup(mark);
			}
			
			rFunctionArguments();
			consume(Token.tRPAREN);
		}
		
		if (LT(1) == Token.tLPAREN) {
			consume();
			rTypeName();
			consume(Token.tRPAREN);
		} else {
			rTypeSpecifier();
			rNewDeclarator();
		}
		
		if (LT(1) == Token.tLPAREN)
			rAllocateInitializer();
		
		return;
	}
	
	public void rNewDeclarator() throws Exception {
		if (LT(1) != Token.tLBRACKET) {
			if (!optPtrOperator())
				throw new ParserException(LA(1));
		}
		
		while (LT(1) == Token.tLBRACKET) {
			consume();
			rCommaExpression();
			consume(Token.tRBRACKET);
		}
	}
	
	public void rAllocateInitializer() throws Exception {
		consume(Token.tLPAREN);
		
		if (LT(1) == Token.tRPAREN) {
			consume();
			return;
		}
		
		for (;;) {
			rInitializeExpr();
			
			if (LT(1) == Token.tCOMMA)
				consume();
			else
				break;
		}

		consume(Token.tRPAREN);
	}
	
	public void rPostfixExpr() throws Exception {
		rPrimaryExpr();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tLBRACKET:
					consume();
					rCommaExpression();
					consume(Token.tRBRACKET);
					break;
				case Token.tLPAREN:
					consume();
					rFunctionArguments();
					consume(Token.tRPAREN);
					break;
				case Token.tINCR:
				case Token.tDECR:
					consume();
					break;
				case Token.tDOT:
				case Token.tARROW:
					consume();
					rVarName();
					break;
				default:
					return;
			}
		}
	}
	
	public void rPrimaryExpr() throws Exception {
		switch (LT(1)) {
			case Token.tINTEGER:
			case Token.tSTRING:
			case Token.t_this:
				callback.expressionTerminal(consume());
				return;
			case Token.tLPAREN:
				consume();
				rCommaExpression();
				consume(Token.tRPAREN);
				return;
			case Token.t_typeid:
				rTypeidExpr();
				return;
			default:
				if (optIntegralTypeOrClassSpec()) {
					consume(Token.tLPAREN);
					rFunctionArguments();
					consume(Token.tRPAREN);
					return;
				} else {
					rVarName();
					return;
				}
		}
	}
	
	public void rVarName() throws Exception {
		if (LT(1) == Token.tCOLONCOLON)
			consume();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tIDENTIFIER:
					consume();
					if (isTemplateArgs()) {
						rTemplateArgs();
					}
					
					if (LT(1) == Token.tCOLONCOLON) {
						switch (LT(2)) {
							case Token.tIDENTIFIER:
							case Token.tCOMPL:
							case Token.t_operator:
								consume();
								break;
							default:
								return;
						}
					} else
						return;
					break;
				case Token.tCOMPL:
					consume();
					consume(Token.tIDENTIFIER);
					return;
				case Token.t_operator:
					consume();
					rOperatorName();
					return;
				default:
					throw new ParserException(LA(1));
			}
		}
	}
	
	public boolean isTemplateArgs() throws Exception {
		int i = 1;
		int t = LT(i++);
		if (t == Token.tLT) {
			int n = 1;
			while (n > 0) {
				int u = LT(i++);
				if (u == Token.tLT)
					++n;
				else if (u == Token.tGT)
					--n;
				else if (u == Token.tLPAREN) {
					int m = 1;
					while (m > 0) {
						int v = LT(i++);
						if (v == Token.tLPAREN)
							++m;
						else if (v == Token.tRPAREN)
							--m;
						else if (v == Token.tEOF || v == Token.tSEMI || v == Token.tRBRACE)
							return false;
					}
				}
				else if (u == Token.tEOF || u == Token.tSEMI || u == Token.tRBRACE)
					return false;
			}
			
			t = LT(i);
			return t == Token.tCOLONCOLON || t == Token.tLPAREN;
		}
		
		return false;
	}
	
	public void rFunctionBody() throws Exception {
		callback.functionBodyBegin();
		rCompoundStatement();
		callback.functionBodyEnd();
	}
	
	public void rCompoundStatement() throws Exception {
		consume(Token.tLBRACE);
		
		if (quickParse) {
			// simply look for matching brace and return
			int depth = 1;
			while (depth > 0) {
				switch (LT(1)) {
					case Token.tRBRACE:
						--depth;
						break;
					case Token.tLBRACE:
						++depth;
						break;
					case Token.tEOF:
						// Oops, no match
						throw new ParserException(LA(1));
				}
				consume();
			}
			return;
		}
		
		while (LT(1) != Token.tRBRACE) {
			rStatement();
		}
		consume();
	}
	
	// statement
	// : compoundStatement
	// | typedef
	// | ifStatement
	// | switchStatement
	// | whileStatement
	// | doStatement
	// | forStatement
	// | tryStatement
	// | "break" ";"
	// | "continue" ";"
	// | "return" (commaExpression)? ";"
	// | "goto" identifier ";"
	// | "case" expression ":" statement
	// | "default" ":" statement
	// | identifier ":" statement
	// | exprStatement
	public void rStatement() throws Exception {
		switch (LT(1)) {
			case Token.tLBRACE:
				rCompoundStatement();
				return;
			case Token.t_typedef:
				rTypedef();
				return;
			case Token.t_if:
				rIfStatement();
				return;
			case Token.t_switch:
				rSwitchStatement();
				return;
			case Token.t_while:
				rWhileStatement();
				return;
			case Token.t_do:
				rDoStatement();
				return;
			case Token.t_for:
				rForStatement();
				return;
			case Token.t_try:
				rTryStatement();
				return;
			case Token.t_break:
			case Token.t_continue:
				consume();
				consume(Token.tSEMI);
				return;
			case Token.t_return:
				consume();
				if (LT(1) == Token.tSEMI)
					consume();
				else {
					rCommaExpression();
					consume(Token.tSEMI);
				}
				return;
			case Token.t_goto:
				consume();
				consume(Token.tIDENTIFIER);
				consume(Token.tSEMI);
				return;
			case Token.t_case:
				consume();
				rExpression();
				consume(Token.tCOLON);
				rStatement();
				return;
			case Token.t_default:
				consume();
				consume(Token.tCOLON);
				rStatement();
				return;
			case Token.tIDENTIFIER:
				if (LT(2) == Token.tCOLON) {
					consume();
					consume();
					rStatement();
					return;
				}
				// fall through to default:
			default:
				rExprStatement();
		}
	}
	
	public void rIfStatement() throws Exception {
		consume(Token.t_if);
		consume(Token.tLPAREN);
		rCommaExpression();
		consume(Token.tRPAREN);
		rStatement();
		
		if (LT(1) == Token.t_else) {
			consume();
			rStatement();
		}
	}
	
	public void rSwitchStatement() throws Exception {
		consume(Token.t_switch);
		consume(Token.tLPAREN);
		rCommaExpression();
		consume(Token.tRPAREN);
		rStatement();
	}
	
	public void rWhileStatement() throws Exception {
		consume(Token.t_while);
		consume(Token.tLPAREN);
		rCommaExpression();
		consume(Token.tRPAREN);
		rStatement();
	}
	
	public void rDoStatement() throws Exception {
		consume(Token.t_do);
		rStatement();
		consume(Token.t_while);
		consume(Token.tLPAREN);
		rCommaExpression();
		consume(Token.tRPAREN);
		consume(Token.tSEMI);
	}
	
	public void rForStatement() throws Exception {
		consume(Token.t_for);
		consume(Token.tLPAREN);
		rExprStatement();
		if (LT(1) != Token.tSEMI)
			rCommaExpression();
		consume(Token.tSEMI);
		if (LT(1) != Token.tRPAREN)
			rCommaExpression();
		consume(Token.tRPAREN);
		rStatement();
	}
	
	public void rTryStatement() throws Exception {
		consume(Token.t_try);
		rCompoundStatement();
		do {
			consume(Token.t_catch);
			consume(Token.tLPAREN);
			if (LT(1) == Token.tELIPSE)
				consume();
			else
				rArgDeclaration();
			consume(Token.tRPAREN);
			rCompoundStatement();
		} while (LT(1) == Token.t_catch);
	}
	
	public void rExprStatement() throws Exception {
		if (LT(1) == Token.tSEMI) {
			consume();
			return;
		} else {
			Token mark = mark();
			try {
				rDeclarationStatement();
				return;
			} catch (Backtrack b) {
			}
			
			backup(mark);
			rCommaExpression();
			consume(Token.tSEMI);
		}
	}
	
	public void rDeclarationStatement() throws Exception {
		optStorageSpec();
		boolean cv = optCvQualify();
		
		if (optIntegralTypeOrClassSpec()) {
			rIntegralDeclStatement();
			return;
		} else {
			if (cv && ((LT(1) == Token.tIDENTIFIER && LT(2) == Token.tASSIGN) || LT(1) == Token.tSTAR))
				rConstDeclaration();
			else
				rOtherDeclStatement();
		}
	}
	
	public void rIntegralDeclStatement() throws Exception {
		optCvQualify();
		if (LT(1) == Token.tSEMI) {
			consume();
			return;
		} else {
			rDeclarators(false, true);
			consume(Token.tSEMI);
		}
	}
	
	public void rOtherDeclStatement() throws Exception {
		rName();
		optCvQualify();
		rDeclarators(false, true);
		consume(Token.tSEMI);
	}
	
	
	// Token management
	private Scanner scanner;

	static public final int maxToken = 2000;
	
	private Token currToken;
	
	private void fetchToken() throws Exception {
		scanner.nextToken();
	}

	protected Token LA(int i) throws Exception {
		if (i < 1)
			// can't go backwards
			return null;

		if (currToken == null)
			currToken = scanner.nextToken();
		
		Token retToken = currToken;
		 
		for (; i > 1; --i) {
			if (retToken.getNext() == null)
				scanner.nextToken();
			retToken = retToken.getNext();
		}
		
		return retToken;
	}

	protected int LT(int i) throws Exception {
		return LA(i).type;
	}
	
	protected Token consume() throws Exception {
		if (currToken.getNext() == null)
			scanner.nextToken();
		Token retToken = currToken;
		currToken = currToken.getNext();
		return retToken;
	}
	
	protected Token consume(int type) throws Exception {
		if (LT(1) == type)
			return consume();
		else
			throw backtrack; //throw new SyntaxError();
	}
	
	protected Token mark() {
		return currToken;
	}
	
	protected void backup(Token mark) {
		currToken = mark;
	}
	
	// Utility routines that require a knowledge of the grammar
	public static String generateName(Token startToken) throws Exception {
		Token currToken = startToken.getNext();
		
		if (currToken == null || currToken.getType() != Token.tCOLONCOLON)
			return startToken.getImage();
		
		StringBuffer buff = new StringBuffer(startToken.getImage());
		while (currToken != null && currToken.getType() == Token.tCOLONCOLON) {
			currToken = currToken.getNext();
			if (currToken == null || currToken.getType() != Token.tIDENTIFIER)
				// Not good
				throw new ParserException(startToken);
			buff.append("::");
			buff.append(currToken.getImage());
			currToken = currToken.getNext();
		}
		
		return buff.toString();
	}
	
}
