package org.eclipse.cdt.internal.core.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This is an attempt at a copyright clean parser.  The grammar is based
 * on the ISO C++ standard
 */
public class Parser {

	private IParserCallback callback;
	private boolean quickParse = false;
	private boolean parsePassed = true;
	
	// TO DO: convert to a real symbol table
	private Map currRegion = new HashMap();
	
	public Parser(IScanner s, IParserCallback c, boolean quick) throws Exception {
		callback = c;
		scanner = s;
		quickParse = quick;
		scanner.setQuickScan(quick);
		scanner.setCallback(c);
		//fetchToken();
	}

	public Parser(IScanner s, IParserCallback c) throws Exception {
		this(s, c, false);
	}
	
	public Parser( IScanner s) throws Exception {
		this(s, new NullParserCallback(), false);
	}
	
	public Parser(String code) throws Exception {
		this(new Scanner().initialize( new StringReader( code ), null
));
	}

	public Parser(String code, IParserCallback c) throws Exception {
		this(new Scanner().initialize( new StringReader( code ), null
), c, false);
	}

	public Parser(InputStream stream, IParserCallback c, boolean quick) throws Exception {
		this(new Scanner().initialize( new InputStreamReader(stream), null ), 
c, quick);
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
	public void translationUnit() throws Exception {
		Object translationUnit = callback.translationUnitBegin();
		Token lastBacktrack = null;
		while (LT(1) != Token.tEOF) {
			try {
				declaration( translationUnit );
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
		callback.translationUnitEnd(translationUnit);
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
	public void declaration( Object container ) throws Exception {
		switch (LT(1)) {
			case Token.t_asm:
				// asmDefinition( );
				consume();
				return; 
			case Token.t_namespace:
				// namespaceDefinition();
				consume();
				return; 
			case Token.t_using:
				// usingDeclaration();
				consume();
				return; 
			case Token.t_export:
			case Token.t_template:
				// templateDeclaration();
				consume();
				return; 
			case Token.t_extern:
				if (LT(2) == Token.tSTRING)
				{
					// linkageSpecification();
					consume();
					return; 
				}
					
				// else drop through
			default:
				simpleDeclaration( container ); 
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
	public void simpleDeclaration( Object container ) throws Exception {
		Object simpleDecl = callback.simpleDeclarationBegin( container);
		declSpecifierSeq(simpleDecl);

		if (LT(1) != Token.tSEMI)
			try {
				initDeclarator(simpleDecl);
				
				while (LT(1) == Token.tCOMMA) {
					consume();
					
					try {
						initDeclarator(simpleDecl);
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
				callback.functionBodyBegin();
				if (quickParse) {
					// speed up the parser by skiping the body
					// simply look for matching brace and return
					consume();
					int depth = 1;
					while (depth > 0) {
						switch (consume().getType()) {
							case Token.tRBRACE:
								--depth;
								break;
							case Token.tLBRACE:
								++depth;
								break;
							case Token.tEOF:
								// Oops, no match
								throw backtrack;
						}
					}
				} else {
					functionBody();
				}
				callback.functionBodyEnd();
				break;
			default:
				break;
		}
		
		callback.simpleDeclarationEnd(simpleDecl);
	}
	
	
	public void parameterDeclaration( Object containerObject ) throws Exception
	{
		Object parameterDecl = callback.parameterDeclarationBegin( containerObject );
		declSpecifierSeq( parameterDecl );
		
		if (LT(1) != Token.tSEMI)
			try {
				initDeclarator(parameterDecl);
				
			} catch (Backtrack b) {
				// allowed to be empty
			}
 		 
		callback.parameterDeclarationEnd( parameterDecl );
		 
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
	public void declSpecifierSeq( Object decl ) throws Exception {
		boolean encounteredTypename = false;
		boolean encounteredRawType = false;
		declSpecifiers:		
		for (;;) {
			switch (LT(1)) {
				case Token.t_auto:
				case Token.t_register:
				case Token.t_static:
				case Token.t_extern:
				case Token.t_mutable:
				case Token.t_inline:
				case Token.t_virtual:
				case Token.t_explicit:
				case Token.t_typedef:
				case Token.t_friend:
				case Token.t_const:
				case Token.t_volatile:
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
					encounteredRawType = true;
					callback.simpleDeclSpecifier(decl, consume());
					break;
				case Token.t_typename:
					consume();
					name();
					break;
				case Token.tCOLONCOLON:
					consume();
					// handle nested later:
				case Token.tIDENTIFIER:
					if( ! encounteredRawType )
					{
						// handle nested later:
						if( ! encounteredTypename )
						{
							callback.simpleDeclSpecifier(decl,consume());
							encounteredTypename = true; 
							break;
						}
						else
							return;
					}
					else
						return;
					
				case Token.t_class:
				case Token.t_struct:
				case Token.t_union:
					classSpecifier(decl);
					return;
				case Token.t_enum:
					// enumSpecifier();
					break;
				default:
					break declSpecifiers;
			}
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
	public boolean name() throws Exception {
		Token last = null;
		
		callback.nameBegin(LA(1));
		
		if (LT(1) == Token.tCOLONCOLON)
			last = consume();

		last = consume(Token.tIDENTIFIER);

		while (LT(1) == Token.tCOLONCOLON) {
			last = consume();
			
			last = consume(Token.tIDENTIFIER);
		}

		callback.nameEnd(last);

		return true;
	}

	/**
	 * cvQualifier
	 * : "const" | "volatile"
	 */
	public Object cvQualifier() throws Exception {
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
	public void initDeclarator( Object owner ) throws Exception {
		declarator( owner );
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
	public void declarator( Object container ) throws Exception {
		
		boolean aborted; 
		do
		{
			aborted = false;
			Object declarator = callback.declaratorBegin( container );
			
			for (;;) {
				try {
					ptrOperator();
				} catch (Backtrack b) {
					break;
				}
			}
			
			if (LT(1) == Token.tLPAREN) {
				consume();
				declarator(declarator);
				consume(Token.tRPAREN);
				return;
			}
			
			name();
			callback.declaratorId(declarator);
			
			for (;;) {
				switch (LT(1)) {
					case Token.tLPAREN:
						// parameterDeclarationClause
						Object clause = callback.argumentsBegin(declarator);
						consume();
						parameterDeclarationLoop:
						for (;;) {
							switch (LT(1)) {
								case Token.tRPAREN:
									consume();
									break parameterDeclarationLoop;
								case Token.tELIPSE:
									consume();
									break;
								case Token.tCOMMA:
									consume();
									break;
								default:
									parameterDeclaration( clause );  
							}
						}
						callback.argumentsEnd(clause);
						break;
					case Token.tLBRACKET:
						consume();
						// constantExpression();
						consume(Token.tRBRACKET);
						continue;
				}
				break;
			}
			
			if( LA(1).getType() == Token.tIDENTIFIER )
			{
				callback.declaratorAbort( container, declarator );
				declarator = null;
				aborted = true; 
			}
			else
				callback.declaratorEnd(declarator);
		} while( aborted );
	}
	
	/**
	 * ptrOperator
	 * : "*" (cvQualifier)*
	 * | "&"
	 * | name "*" (cvQualifier)*
	 */
	public Object ptrOperator() throws Exception {
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
	public void classSpecifier( Object owner ) throws Exception {
		Token classKey = null;
		
		// class key
		switch (LT(1)) {
			case Token.t_class:
			case Token.t_struct:
			case Token.t_union:
				classKey = consume();
				break;
			default:
				throw backtrack;
		}

		Object classSpec = callback.classSpecifierBegin( owner, classKey);
		
		// class name
		if (LT(1) == Token.tIDENTIFIER) {
			name();
			callback.classSpecifierName(classSpec);			
		}

		//currRegion.put(name.getImage(), classKey);
		
		// base clause
		if (LT(1) == Token.tCOLON) {
			consume();
			baseSpecifier( classSpec );
		}
		
		// If we don't get a "{", assume elaborated type
		if (LT(1) == Token.tLBRACE) {
			consume();
			
			memberDeclarationLoop:
			while (LT(1) != Token.tRBRACE) {
				switch (LT(1)) {
					case Token.t_public:
						consume();
						consume(Token.tCOLON);
						break;
					case Token.t_protected:
						consume();
						consume(Token.tCOLON);
						break;
					case Token.t_private:
						consume();
						consume(Token.tCOLON);
						break;
					case Token.tRBRACE:
						consume(Token.tRBRACE);
						break memberDeclarationLoop;
					default:
						declaration(classSpec);
				}
			}
			// consume the }
			consume();
		}
		
		callback.classSpecifierEnd(classSpec);
	}

	public void baseSpecifier( Object classSpecOwner ) throws Exception {

		Object baseSpecifier = callback.baseSpecifierBegin( classSpecOwner ); 		
		
		baseSpecifierLoop:
		for (;;) {
			switch (LT(1)) {
				case Token.t_virtual:
					callback.baseSpecifierVirtual( baseSpecifier, true ); 
					consume();
					break;
				case Token.t_public:
				case Token.t_protected:
				case Token.t_private:
					callback.baseSpecifierVisibility( baseSpecifier, currToken );
					consume();					
					break;
				case Token.tCOLONCOLON:
				case Token.tIDENTIFIER:
					name();
					callback.baseSpecifierName( baseSpecifier ); 
					break;
				case Token.tCOMMA:
					callback.baseSpecifierEnd( baseSpecifier ); 
					baseSpecifier = callback.baseSpecifierBegin( classSpecOwner );
					consume(); 
					continue baseSpecifierLoop;
				default:
					break baseSpecifierLoop;
			}
		}
		callback.baseSpecifierEnd( baseSpecifier ); 
	}
	
	public void functionBody() throws Exception {
		compoundStatement();
	}
	
	// Statements
	public void statement() throws Exception {
		switch (LT(1)) {
			case Token.t_case:
				consume();
				constantExpression();
				consume(Token.tCOLON);
				statement();
				return;
			case Token.t_default:
				consume();
				consume(Token.tCOLON);
				statement();
				return;
			case Token.tLBRACE:
				compoundStatement();
				return;
			case Token.t_if:
				consume();
				consume(Token.tLPAREN);
				condition();
				consume(Token.tRPAREN);
				statement();
				if (LT(1) == Token.t_else) {
					consume();
					statement();
				}
				return;
			case Token.t_switch:
				consume();
				consume(Token.tLPAREN);
				condition();
				consume(Token.tRPAREN);
				statement();
				return;
			case Token.t_while:
				consume();
				consume(Token.tLPAREN);
				condition();
				consume(Token.tRPAREN);
				statement();
				return;
			case Token.t_do:
				consume();
				statement();
				consume(Token.t_while);
				consume(Token.tLPAREN);
				condition();
				consume(Token.tRPAREN);
				return;
			case Token.t_for:
				consume();
				consume(Token.tLPAREN);
				forInitStatement();
				if (LT(1) != Token.tSEMI)
					condition();
				consume(Token.tSEMI);
				if (LT(1) != Token.tRPAREN)
					expression();
				consume(Token.tRPAREN);
				statement();
				return;
			case Token.t_break:
				consume();
				consume(Token.tSEMI);
				return;
			case Token.t_continue:
				consume();
				consume(Token.tSEMI);
				return;
			case Token.t_return:
				consume();
				if (LT(1) != Token.tSEMI)
					expression();
				consume(Token.tSEMI);
				return;
			case Token.t_goto:
				consume();
				consume(Token.tIDENTIFIER);
				consume(Token.tSEMI);
				return;
			case Token.t_try:
				consume();
				compoundStatement();
				while (LT(1) == Token.t_catch) {
					consume();
					consume(Token.tLPAREN);
					declaration(null); // was exceptionDeclaration
					consume(Token.tRPAREN);
					compoundStatement();
				}
				return;
			case Token.tSEMI:
				consume();
				return;
			default:
				// can be many things:
				// label
				if (LT(1) == Token.tIDENTIFIER && LT(2) == Token.tCOLON) {
					consume();
					consume();
					statement();
					return;
				}
				
				// expressionStatement
				// Note: the function style cast ambiguity is handled in expression
				// Since it only happens when we are in a statement
				try {
					expression();
					consume(Token.tSEMI);
					return;
				} catch (Backtrack b) {
				}
				
				// declarationStatement
				declaration(null);
		}
	}
	
	public void condition() throws Exception {
		// TO DO
	}
	
	public void forInitStatement() throws Exception {
		// TO DO
	}
	
	public void compoundStatement() throws Exception {
		consume(Token.tLBRACE);
		while (LT(1) != Token.tRBRACE)
			statement();
		consume();
	}
	
	// Expressions
	public void constantExpression() throws Exception {
		conditionalExpression();
	}
	
	public void expression() throws Exception {
		assignmentExpression();
		
		while (LT(1) == Token.tCOMMA) {
			Token t = consume();
			assignmentExpression();
			callback.expressionOperator(t);
		}
	}
	
	public void assignmentExpression() throws Exception {
		if (LT(1) == Token.t_throw) {
			throwExpression();
			return;
		}
		
		// if the condition not taken, try assignment operators
		if (!conditionalExpression()) {
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
					conditionalExpression();
					callback.expressionOperator(t);
					break;
			}
		}
	}
	
	public void throwExpression() throws Exception {
		consume(Token.t_throw);
		
		try {
			expression();
		} catch (Backtrack b) {
		}
	}
	
	public boolean conditionalExpression() throws Exception {
		logicalOrExpression();
		
		if (LT(1) == Token.tQUESTION) {
			consume();
			expression();
			consume(Token.tCOLON);
			assignmentExpression();
			return true;
		} else
			return false;
	}
	
	public void logicalOrExpression() throws Exception {
		logicalAndExpression();
		
		while (LT(1) == Token.tOR) {
			Token t = consume();
			logicalAndExpression();
			callback.expressionOperator(t);
		}
	}
	
	public void logicalAndExpression() throws Exception {
		inclusiveOrExpression();
		
		while (LT(1) == Token.tAND) {
			Token t = consume();
			inclusiveOrExpression();
			callback.expressionOperator(t);
		}
	}
	
	public void inclusiveOrExpression() throws Exception {
		exclusiveOrExpression();
		
		while (LT(1) == Token.tBITOR) {
			Token t = consume();
			exclusiveOrExpression();
			callback.expressionOperator(t);
		}
	}
	
	public void exclusiveOrExpression() throws Exception {
		andExpression();
		
		while (LT(1) == Token.tXOR) {
			Token t = consume();
			andExpression();
			callback.expressionOperator(t);
		}
	}
	
	public void andExpression() throws Exception {
		equalityExpression();
		
		while (LT(1) == Token.tAMPER) {
			Token t = consume();
			equalityExpression();
			callback.expressionOperator(t);
		}
	}
	
	public void equalityExpression() throws Exception {
		relationalExpression();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tEQUAL:
				case Token.tNOTEQUAL:
					Token t = consume();
					relationalExpression();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void relationalExpression() throws Exception {
		shiftExpression();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tGT:
					// For template args, the GT means end of args
					//if (templateArgs)
					//	return;
				case Token.tLT:
				case Token.tLTEQUAL:
				case Token.tGTEQUAL:
					Token t = consume();
					shiftExpression();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void shiftExpression() throws Exception {
		additiveExpression();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tSHIFTL:
				case Token.tSHIFTR:
					Token t = consume();
					additiveExpression();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void additiveExpression() throws Exception {
		multiplicativeExpression();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tPLUS:
				case Token.tMINUS:
					Token t = consume();
					multiplicativeExpression();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void multiplicativeExpression() throws Exception {
		pmExpression();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tSTAR:
				case Token.tDIV:
				case Token.tMOD:
					Token t = consume();
					pmExpression();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	public void pmExpression() throws Exception {
		castExpression();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tDOTSTAR:
				case Token.tARROWSTAR:
					Token t = consume();
					castExpression();
					callback.expressionOperator(t);
					break;
				default:
					return;
			}
		}
	}
	
	/**
	 * castExpression
	 * : unaryExpression
	 * | "(" typeId ")" castExpression
	 */
	public void castExpression() throws Exception {
		// TO DO: we need proper symbol checkint to ensure type name
		if (false && LT(1) == Token.tLPAREN) {
			Token mark = mark();
			consume();
			
			// If this isn't a type name, then we shouldn't be here
			try {
				typeId();
				consume(Token.tRPAREN);
				castExpression();
				return;
			} catch (Backtrack b) {
				backup(mark);
			}
		}

		unaryExpression();
	}
	
	public void typeId() throws Exception {
		try {
			name();
			return;
		} catch (Backtrack b) {
		}
	}
	
	public void deleteExpression() throws Exception {
		if (LT(1) == Token.tCOLONCOLON) {
			// global scope
			consume();
		}
		
		consume(Token.t_delete);
		
		if (LT(1) == Token.tLBRACKET) {
			// array delete
			consume();
			consume(Token.tRBRACKET);
		}
		
		castExpression();
	}
	
	public void newExpression() throws Exception {
		if (LT(1) == Token.tCOLONCOLON) {
			// global scope
			consume();
		}
		
		consume (Token.t_new);
		
		// TO DO: finish this horrible mess...
	}
	
	public void unaryExpression() throws Exception {
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
				castExpression();
				callback.expressionOperator(t);
				return;
			case Token.t_sizeof:
				if (LT(1) == Token.tLPAREN) {
					consume();
					typeId();
					consume(Token.tRPAREN);
				} else {
					unaryExpression();
				}
				return;
			case Token.t_new:
				newExpression();
				return;
			case Token.t_delete:
				deleteExpression();
				return;
			case Token.tCOLONCOLON:
				switch (LT(2)) {
					case Token.t_new:
						newExpression();
						return;
					case Token.t_delete:
						deleteExpression();
						return;
					default:
						postfixExpression();
						return;			
				}
			default:
				postfixExpression();
				return;
		}
	}

	public void postfixExpression() throws Exception {
		switch (LT(1)) {
			case Token.t_typename:
				consume();
				// TO DO: this
				break;
			case Token.t_dynamic_cast:
			case Token.t_static_cast:
			case Token.t_reinterpret_cast:
			case Token.t_const_cast:
				consume();
				consume(Token.tLT);
				typeId();
				consume(Token.tGT);
				consume(Token.tLPAREN);
				expression();
				consume(Token.tRPAREN);
				break;
			case Token.t_typeid:
				consume();
				consume(Token.tLPAREN);
				try {
					typeId();
				} catch (Backtrack b) {
					expression();
				}
				consume(Token.tRPAREN);
				break;
			default:
				// TO DO: try simpleTypeSpecifier "(" expressionList ")"
				primaryExpression();
		}
		
		for (;;) {
			switch (LT(1)) {
				case Token.tLBRACKET:
					// array access
					consume();
					expression();
					consume(Token.tRBRACKET);
					break;
				case Token.tLPAREN:
					// function call
					consume();
					// Note: since expressionList and expression are the same...
					expression();
					consume(Token.tRPAREN);
					break;
				case Token.tINCR:
				case Token.tDECR:
					// post incr/decr
					consume();
					break;
				case Token.tDOT:
				case Token.tARROW:
					// member access
					consume();
					// TO DO: handle this
					//varName();
					break;
				default:
					return;
			}
		}
	}
	
	public void primaryExpression() throws Exception {
		switch (LT(1)) {
			// TO DO: we need more literals...
			case Token.tINTEGER:
				callback.expressionTerminal(consume());
				return;
			case Token.tSTRING:
				callback.expressionTerminal(consume());
				return;
			case Token.t_this:
				consume();
				return;
			case Token.tLPAREN:
				consume();
				expression();
				consume(Token.tRPAREN);
				return;
			default:
				// TO DO: idExpression which yeilds a variable
				//idExpression();
				return;
		}
	}
	
	public void varName() throws Exception {
		if (LT(1) == Token.tCOLONCOLON)
			consume();
		
		for (;;) {
			switch (LT(1)) {
				case Token.tIDENTIFIER:
					consume();
					//if (isTemplateArgs()) {
					//	rTemplateArgs();
					//}
					
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
					//rOperatorName();
					return;
				default:
					throw backtrack;
			}
		}
	}
	
	// Backtracking
	private static class Backtrack extends Exception {
	}
	
	private static Backtrack backtrack = new Backtrack();
	
	// Token management
	private IScanner scanner;
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
	
	protected Token consume(int type) throws Exception {
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
