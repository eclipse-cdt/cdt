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
		
		while (declSpecifier());
		
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
	public boolean declSpecifier() throws Backtrack {
		switch (LT(1)) {
			case Token.t_auto:
			case Token.t_register:
			case Token.t_static:
			case Token.t_extern:
			case Token.t_mutable:
			case Token.t_inline:
			case Token.t_virtual:
			case Token.t_explicit:
			case Token.t_char:
			case Token.t_wchar_t:
			case Token.t_bool:
			case Token.t_short:
			case Token.t_int:
			case Token.t_long:
			case Token.t_signed:
			case Token.t_unsigned:
			case Token.t_float:
			case Token.t_double:
			case Token.t_void:
			case Token.t_const:
			case Token.t_volatile:
			case Token.t_friend:
			case Token.t_typedef:
				consume();
				return true;
			case Token.t_typename:
				consume();
				return name();
			case Token.tCOLON:
				// handle nested later:
				return false;
			case Token.tIDENTIFIER:
				// handle nested later:
				if (currRegion.getDeclaration(LA(1).getImage()) != null) {
					consume();
					return true;
				}
				else
					return false;
			case Token.t_class:
			case Token.t_struct:
			case Token.t_union:
				return classSpecifier();
			case Token.t_enum:
				// enumSpecifier();
				return true;
			default:
				return false;
		}
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
	public boolean name() {
		if (LT(1) == Token.tCOLONCOLON)
			consume();

		if (!consume(Token.tIDENTIFIER))
			return false;

		while (LT(1) == Token.tCOLONCOLON) {
			consume();
			
			if (!consume(Token.tIDENTIFIER))
				return false;
		}
		
		return true;
	}

	/**
	 * cvQualifier
	 * : "const" | "volatile"
	 */
	public boolean cvQualifier() {
		switch (LT(1)) {
			case Token.t_const:
			case Token.t_volatile:
				consume();
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * initDeclarator
	 * : declarator ("=" initializerClause | "(" expressionList ")")?
	 * 
	 * To Do:
	 * - handle initializers
	 */
	public boolean initDeclarator() throws Backtrack {
		if (!declarator())
			return false;
			
		return true;
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
	public boolean declarator() {
		while (ptrOperator());
		
		if (LT(1) == Token.tLPAREN) {
			consume();
			if (!declarator())
				return false;
				
			return consume(Token.tRPAREN);
		}
		
		if (!name())
			return false;
		
		for (;;) {
			switch (LT(1)) {
				case Token.tLPAREN:
					consume();
					// parameterDeclarationClause();
					if (!consume(Token.tRPAREN))
						return false;

					continue;
				case Token.tLBRACKET:
					consume();
					// constantExpression();
					if (!consume(Token.tRBRACKET))
						return false;
					
					continue;
			}
			break;
		}
		
		return true;
	}
	
	/**
	 * ptrOperator
	 * : "*" (cvQualifier)*
	 * | "&"
	 * | name "*" (cvQualifier)*
	 */
	public boolean ptrOperator() {
		int t = LT(1);
		
		if (t == Token.tAMPER) {					
			consume();
			return true;
		}
		
		Token mark = mark();
		if (t == Token.tIDENTIFIER || t == Token.tCOLONCOLON)
			if (!name())
				return false;
		
		if (t == Token.tSTAR) {
			consume();
			
			while (cvQualifier());
			
			return true;
		}
		
		backup(mark);
		return false;
	}

	/**
	 * classSpecifier
	 * : classKey name (baseClause)? "{" (memberSpecification)* "}"
	 */
	public boolean classSpecifier() {
		switch (LT(1)) {
			case Token.t_class:
			case Token.t_struct:
			case Token.t_union:
				consume();
				break;
			default:
				return false;
		}
		
		Declaration decl = new Declaration();
		
		// Right now, just handle the case where the class name is
		// being declared
		if (LT(1) == Token.tIDENTIFIER) {
			currRegion.addDeclaration(consume().getImage(), decl);
		} // else it's an anonymous class
		
		// If we don't get a "{", assume elaborated type
		if (LT(1) != Token.tLBRACE)
			return true;
		else
			consume();

		if (!consume(Token.tRBRACE))
			return false;

		return true;
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
	
	protected boolean consume(int type) {
		if (LT(1) == type) {
			consume();
			return true;
		} else
		 return false;
	}
	
	protected Token mark() {
		return currToken;
	}
	
	protected void backup(Token mark) {
		currToken = mark;
	}
	

}
