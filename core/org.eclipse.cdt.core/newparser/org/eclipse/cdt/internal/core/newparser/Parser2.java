package org.eclipse.cdt.internal.core.newparser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.eclipse.cdt.internal.core.dom.*;

/**
 * This is an attempt at a copyright clean parser.  The grammar is based
 * on the ISO C++ standard
 */
public class Parser2 {

	private IParserCallback callback;
	private boolean quickParse = false;
	private boolean parsePassed = true;
	private DeclarativeRegion currRegion = new DeclarativeRegion();
	
	public Parser2(Scanner s, IParserCallback c, boolean quick) throws Exception {
		callback = c;
		scanner = s;
		quickParse = quick;
		scanner.setQuickScan(quick);
		scanner.setCallback(c);
		//fetchToken();
	}

	public Parser2(Scanner s, IParserCallback c) throws Exception {
		this(s, c, false);
	}
	
	public Parser2(Scanner s) throws Exception {
		this(s, new NullParserCallback(), false);
	}
	
	public Parser2(String code) throws Exception {
		this(new Scanner(new StringReader(code)));
	}

	public Parser2(String code, IParserCallback c) throws Exception {
		this(new Scanner(new StringReader(code)), c, false);
	}

	public Parser2(InputStream stream, IParserCallback c, boolean quick) throws Exception {
		this(new Scanner(new InputStreamReader(stream)), c, quick);
	}
	
	private static int parseCount = 0;
	
	public boolean parse() throws Exception {
		long startTime = System.currentTimeMillis();
		translationUnit();
		System.out.println("Parse " + (++parseCount) + ": "
			+ ( System.currentTimeMillis() - startTime ) + "ms"
			+ ( parsePassed ? "" : " - parse failure" ));
			
		return parsePassed;
	}
	
	/**
	 * translationUnit
	 * : (declaration)*
	 * 
	 */
	public TranslationUnit translationUnit() {
		TranslationUnit translationUnit = new TranslationUnit();
		Token lastBacktrack = null;
		while (LT(1) != Token.tEOF) {
			try {
				Declaration declaration = declaration();
				if (declaration != null)
					// To do: once finished, this should never be null
					translationUnit.addDeclaration(declaration);
			} catch (Backtrack b) {
				// Mark as failure and try to reach a recovery point
				parsePassed = false;
				
				if (lastBacktrack != null && lastBacktrack == LA(1)) {
					// we haven't progressed from the last backtrack
					// try and find tne next definition
					for (int t = LT(1); t != Token.tEOF; t = LT(1)) {
						consume();
						// TO DO: we should really check for matching braces too
						if (t == Token.tSEMI)
							break;
					}
				} else {
					// start again from here
					lastBacktrack = LA(1);
				}
			}
		}
		
		return translationUnit;
	}
	
	/**
	 * declaration
	 * : {"asm"} asmDefinition
	 * | {"namespace"} namespaceDefinition
	 * | {"using"} usingDeclaration
	 * | {"export"|"template"} templateDeclaration
	 * | {"extern"} linkageSpecification
	 * | simpleDeclaration
	 * 
	 * Notes:
	 * - folded in blockDeclaration
	 * - merged alternatives that required same LA
	 *   - functionDefinition into simpleDeclaration
	 *   - namespaceAliasDefinition into namespaceDefinition
	 *   - usingDirective into usingDeclaration
	 *   - explicitInstantiation and explicitSpecialization into
	 *       templateDeclaration
	 */
	public Declaration declaration() throws Backtrack {
		switch (LT(1)) {
			case Token.t_asm:
				return null; // asmDefinition();
			case Token.t_namespace:
				return null; // namespaceDefinition();
			case Token.t_using:
				return null; // usingDeclaration();
			case Token.t_export:
			case Token.t_template:
				return null; // templateDeclaration();
			case Token.t_extern:
				return null; // linkageSpecification();
			default:
				return simpleDeclaration(); 
		}
	}
	
	/**
	 * simpleDeclaration
	 * : (declSpecifier)* (initDeclarator ("," initDeclarator)*)? 
	 *     (";" | {"{"} functionBody)
	 * 
	 * Notes:
	 * - append functionDefinition stuff to end of this rule
	 * 
	 * To do:
	 * - work in ctorInitializer and functionTryBlock
	 */
	public SimpleDeclaration simpleDeclaration() throws Backtrack {
		SimpleDeclaration simpleDeclaration = new SimpleDeclaration();
		
		DeclSpecifierSeq declSpecifierSeq = declSpecifierSeq();

		try {
			initDeclarator();
			
			while (LT(1) == Token.tCOMMA) {
				consume();
				
				try {
					initDeclarator();
				} catch (Backtrack b) {
					throw b;
				}
			}
		} catch (Backtrack b) {
			// allowed to be empty
		}
		
		switch (LT(1)) {
			case Token.tSEMI:
				consume();
				break;
			case Token.tLBRACE:
				// functionBody();
				break;
			default:
				throw backtrack;
		}
		
		return simpleDeclaration;
	}
	
	/**
	 * declSpecifier
	 * : "auto" | "register" | "static" | "extern" | "mutable"
	 * | "inline" | "virtual" | "explicit"
	 * | "char" | "wchar_t" | "bool" | "short" | "int" | "long"
	 * | "signed" | "unsigned" | "float" | "double" | "void"
	 * | "const" | "volatile"
	 * | "friend" | "typedef"
	 * | ("typename")? name
	 * | {"class"|"struct"|"union"} classSpecifier
	 * | {"enum"} enumSpecifier
	 * 
	 * Notes:
	 * - folded in storageClassSpecifier, typeSpecifier, functionSpecifier
	 * - folded elaboratedTypeSpecifier into classSpecifier and enumSpecifier
	 * - find template names in name
	 */
	public DeclSpecifierSeq declSpecifierSeq() throws Backtrack {
		DeclSpecifierSeq declSpecifierSeq = new DeclSpecifierSeq();
		
		boolean done = false;
		while (!done) {
			switch (LT(1)) {
				case Token.t_auto:
					consume();
					declSpecifierSeq.setAuto(true);
					break;
				case Token.t_register:
					consume();
					declSpecifierSeq.setRegister(true);
					break;
				case Token.t_static:
					consume();
					declSpecifierSeq.setStatic(true);
					break;
				case Token.t_extern:
					consume();
					declSpecifierSeq.setExtern(true);
					break;
				case Token.t_mutable:
					consume();
					declSpecifierSeq.setMutable(true);
					break;
				case Token.t_inline:
					consume();
					declSpecifierSeq.setInline(true);
					break;
				case Token.t_virtual:
					consume();
					declSpecifierSeq.setVirtual(true);
					break;
				case Token.t_explicit:
					consume();
					declSpecifierSeq.setExplicit(true);
					break;
				case Token.t_typedef:
					consume();
					declSpecifierSeq.setTypedef(true);
					break;
				case Token.t_friend:
					consume();
					declSpecifierSeq.setFriend(true);
					break;
				case Token.t_const:
					consume();
					declSpecifierSeq.setConst(true);
					break;
				case Token.t_volatile:
					consume();
					declSpecifierSeq.setVolatile(true);
					break;
				case Token.t_char:
					consume();
					declSpecifierSeq.setType(DeclSpecifierSeq.t_char);
					break;
				case Token.t_wchar_t:
					consume();
					declSpecifierSeq.setType(DeclSpecifierSeq.t_wchar_t);
					break;
				case Token.t_bool:
					consume();
					declSpecifierSeq.setType(DeclSpecifierSeq.t_bool);
					break;
				case Token.t_short:
					consume();
					declSpecifierSeq.setShort(true);
					break;
				case Token.t_int:
					consume();
					declSpecifierSeq.setType(DeclSpecifierSeq.t_int);
					break;
				case Token.t_long:
					consume();
					declSpecifierSeq.setLong(true);
					break;
				case Token.t_signed:
					consume();
					declSpecifierSeq.setUnsigned(false);
					break;
				case Token.t_unsigned:
					consume();
					declSpecifierSeq.setUnsigned(true);
					break;
				case Token.t_float:
					consume();
					declSpecifierSeq.setType(DeclSpecifierSeq.t_float);
					break;
				case Token.t_double:
					consume();
					declSpecifierSeq.setType(DeclSpecifierSeq.t_double);
					break;
				case Token.t_void:
					consume();
					declSpecifierSeq.setType(DeclSpecifierSeq.t_void);
					break;
				case Token.t_typename:
					consume();
					name();
					break;
				case Token.tCOLONCOLON:
					consume();
					// handle nested later:
				case Token.tIDENTIFIER:
					// handle nested later:
					if (currRegion.getDeclaration(LA(1).getImage()) != null) {
						consume();
						break;
					}
					else
						done = true;
				case Token.t_class:
				case Token.t_struct:
				case Token.t_union:
					classSpecifier();
					break;
				case Token.t_enum:
					// enumSpecifier();
					break;
				default:
					done = true;
			}
		}
		return declSpecifierSeq;
	}
	
	/**
	 * name
	 * : ("::")? name2 ("::" name2)*
	 * 
	 * name2
	 * : IDENTIFER
	 * 
	 * To Do:
	 * - Handle template ids
	 * - Handle unqualifiedId
	 */
	public boolean name() throws Backtrack {
		if (LT(1) == Token.tCOLONCOLON)
			consume();

		consume(Token.tIDENTIFIER);

		while (LT(1) == Token.tCOLONCOLON) {
			consume();
			
			consume(Token.tIDENTIFIER);
		}
		
		return true;
	}

	/**
	 * cvQualifier
	 * : "const" | "volatile"
	 */
	public Object cvQualifier() throws Backtrack {
		switch (LT(1)) {
			case Token.t_const:
			case Token.t_volatile:
				consume();
				return null;
			default:
				throw backtrack;
		}
	}
	
	/**
	 * initDeclarator
	 * : declarator ("=" initializerClause | "(" expressionList ")")?
	 * 
	 * To Do:
	 * - handle initializers
	 */
	public Object initDeclarator() throws Backtrack {
		declarator();
			
		return null;
	}
	
	/**
	 * declarator
	 * : (ptrOperator)* directDeclarator
	 * 
	 * directDeclarator
	 * : declaratorId
	 * | directDeclarator "(" parameterDeclarationClause ")" (cvQualifier)*
	 *     (exceptionSpecification)*
	 * | directDeclarator "[" (constantExpression)? "]"
	 * | "(" declarator")"
	 * 
	 * declaratorId
	 * : name
	 */
	public Declarator declarator() throws Backtrack {
		
		for (;;) {
			try {
				ptrOperator();
			} catch (Backtrack b) {
				break;
			}
		}
		
		if (LT(1) == Token.tLPAREN) {
			consume();
			declarator();
			consume(Token.tRPAREN);
			return null;
		}
		
		name();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tLPAREN:
					consume();
					// parameterDeclarationClause();
					consume(Token.tRPAREN);
					continue;
				case Token.tLBRACKET:
					consume();
					// constantExpression();
					consume(Token.tRBRACKET);
					continue;
			}
			break;
		}
		
		return null;
	}
	
	/**
	 * ptrOperator
	 * : "*" (cvQualifier)*
	 * | "&"
	 * | name "*" (cvQualifier)*
	 */
	public Object ptrOperator() throws Backtrack {
		int t = LT(1);
		
		if (t == Token.tAMPER) {					
			consume();
			return null;
		}
		
		Token mark = mark();
		if (t == Token.tIDENTIFIER || t == Token.tCOLONCOLON)
		name();

		if (t == Token.tSTAR) {
			consume();

			for (;;) {
				try {
					cvQualifier();
				} catch (Backtrack b) {
					break;
				}
			}			
			
			return null;
		}
		
		backup(mark);
		throw backtrack;
	}

	/**
	 * classSpecifier
	 * : classKey name (baseClause)? "{" (memberSpecification)* "}"
	 */
	public ClassSpecifier classSpecifier() throws Backtrack {
		switch (LT(1)) {
			case Token.t_class:
			case Token.t_struct:
			case Token.t_union:
				consume();
				break;
			default:
				throw backtrack;
		}
		
		ClassSpecifier classSpecifier = new ClassSpecifier();
		
		// Right now, just handle the case where the class name is
		// being declared
		if (LT(1) == Token.tIDENTIFIER) {
			currRegion.addDeclaration(consume().getImage(), classSpecifier);
		} // else it's an anonymous class
		
		// If we don't get a "{", assume elaborated type
		if (LT(1) == Token.tLBRACE) {
			consume();
			consume(Token.tRBRACE);
		}
		
		return classSpecifier;
	}
	
	// Backtracking
	private static class Backtrack extends Exception {
	}
	
	private static Backtrack backtrack = new Backtrack();
	
	// Token management
	private Scanner scanner;
	private Token currToken;
	
	private Token fetchToken() {
		try {
			return scanner.nextToken();
		} catch (Exception e) {
			return null;
		}
	}

	protected Token LA(int i) {
		if (i < 1)
			// can't go backwards
			return null;

		if (currToken == null)
			currToken = fetchToken();
		
		Token retToken = currToken;
		 
		for (; i > 1; --i) {
			if (retToken.getNext() == null)
				fetchToken();
			retToken = retToken.getNext();
		}
		
		return retToken;
	}

	protected int LT(int i) {
		return LA(i).type;
	}
	
	protected Token consume() {
		if (currToken.getNext() == null)
			fetchToken();
		Token retToken = currToken;
		currToken = currToken.getNext();
		return retToken;
	}
	
	protected Token consume(int type) throws Backtrack {
		if (LT(1) == type)
			return consume();
		else
			throw backtrack;
	}
	
	protected Token mark() {
		return currToken;
	}
	
	protected void backup(Token mark) {
		currToken = mark;
	}
	

}
