/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

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
	
	public boolean parse() throws Backtrack {
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
	protected void translationUnit() throws Backtrack {
		Object translationUnit = callback.translationUnitBegin();
		Token lastBacktrack = null;
		Token lastToken;
		while (true) {
			try {
				lastToken = LA(1);
				declaration( translationUnit );
				if( LA(1) == lastToken )
					consumeToNextSemicolon();
			} catch (EndOfFile e) {
				// Good
				break;
			} catch (Backtrack b) {
				// Mark as failure and try to reach a recovery point
				parsePassed = false;
				
				if (lastBacktrack != null && lastBacktrack == LA(1)) {
					// we haven't progressed from the last backtrack
					// try and find tne next definition
					consumeToNextSemicolon();
				} else {
					// start again from here
					lastBacktrack = LA(1);
				}
			}
		}
		callback.translationUnitEnd(translationUnit);
	}

	protected void consumeToNextSemicolon() throws EndOfFile {
		parsePassed = false;
		consume();
		// TODO - we should really check for matching braces too
		while (LT(1) != Token.tSEMI) {
			consume();
		}
	}
	
	/**
	 * 
	 * The merger of using-declaration and using-directive.  
	 * 
	 * using-declaration:
	 *	using typename? ::? nested-name-specifier unqualified-id ;
	 *	using :: unqualified-id ;
	 * using-directive:
	 *  using namespace ::? nested-name-specifier? namespace-name ;
	 * 
	 * @param container
	 * @throws Backtrack
	 */
	protected void usingClause( Object container ) throws Backtrack
	{
		consume( Token.t_using );
		
		if( LT(1) == Token.t_namespace )
		{
			Object directive = callback.usingDirectiveBegin( container );
			// using-directive
			consume( Token.t_namespace );
			
			// optional :: and nested classes handled in name	
			if( LT(1) == Token.tIDENTIFIER || LT(1) == Token.tCOLONCOLON )
			{
				name();
				callback.usingDirectiveNamespaceId( directive );
			}
			else
			{
				callback.usingDirectiveAbort(directive);
				throw backtrack;
			}
			
			if( LT(1) == Token.tSEMI )
			{
				consume( Token.tSEMI );
				callback.usingDirectiveEnd( directive );
				return;
			}
			else
			{
				callback.usingDirectiveAbort(directive);
				throw backtrack;				
			}
		}
		else
		{
			Object usingDeclaration = callback.usingDeclarationBegin( container );
			
			boolean typeName = false;
			if( LT(1) == Token.t_typename )
			{
				typeName = true;
				consume( Token.t_typename );
			}
			
			if( LT(1) == Token.tIDENTIFIER || LT(1) == Token.tCOLONCOLON )
			{
				//	optional :: and nested classes handled in name
				name();
				callback.usingDeclarationMapping( usingDeclaration, typeName ); 
			}
			else
			{
				callback.usingDeclarationAbort( usingDeclaration );
				throw backtrack;
			}
		
			if( LT(1) == Token.tSEMI )
			{
				consume( Token.tSEMI );
				callback.usingDeclarationEnd( usingDeclaration );
			}
			else
			{
				callback.usingDeclarationAbort( usingDeclaration );
				throw backtrack;
			}

					
		}
		
		
		
	}
	
	/**
	 * linkageSpecification
	 * : extern "string literal" declaration
	 * | extern "string literal" { declaration-seq } 
	 * @param container
	 * @throws Exception
	 */
	protected void linkageSpecification( Object container ) throws Backtrack
	{
		consume( Token.t_extern );

		if( LT(1) != Token.tSTRING )
			throw backtrack;

		Object linkageSpec = callback.linkageSpecificationBegin( container, consume( Token.tSTRING ).getImage() );

		if( LT(1) == Token.tLBRACE )
		{
			consume(Token.tLBRACE); 
			linkageDeclarationLoop:
			while (LT(1) != Token.tRBRACE) {
				Token lastToken = LA(1);
				switch (LT(1)) {
					case Token.tRBRACE:
						consume(Token.tRBRACE);
						break linkageDeclarationLoop;
					default:
						declaration(linkageSpec);
				}
				if (lastToken == LA(1))
					consumeToNextSemicolon();
			}
			// consume the }
			consume();
			callback.linkageSpecificationEnd( linkageSpec );
		}
		else // single declaration
		{
			declaration( linkageSpec );
			callback.linkageSpecificationEnd( linkageSpec );
		}
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
	protected void declaration( Object container ) throws Backtrack {
		switch (LT(1)) {
			case Token.t_asm:
				// asmDefinition( );
				consume();
				return; 
			case Token.t_namespace:
				namespaceDefinition( container );
				return; 
			case Token.t_using:
				usingClause( container );
				return; 
			case Token.t_export:
			case Token.t_template:
				// templateDeclaration();
				consume();
				return; 
			case Token.t_extern:
				linkageSpecification( container ); 
				return;
			default:
				simpleDeclaration( container ); 
		}
	}
	
	/**
	 *  namespaceDefinition()
	 * 
	 * 	namespace-definition:
	 *		namespace identifier { namespace-body } | namespace { namespace-body }
	 *	 namespace-body:
	 *		declaration-seq?
	 * 
	 */
	
	protected void namespaceDefinition( Object container ) throws Backtrack
	{
		consume( Token.t_namespace);
		Object namespace = callback.namespaceDefinitionBegin( container );

		// optional name 		
		if( LT(1) == Token.tIDENTIFIER )
		{
			name();
			callback.namespaceDefinitionId( namespace );
		}
	
		if( LT(1) == Token.tLBRACE )
		{
			consume(); 
			namepsaceDeclarationLoop:
			while (LT(1) != Token.tRBRACE) {
				Token lastToken = LA(1);
				switch (LT(1)) {
					case Token.tRBRACE:
						consume(Token.tRBRACE);
						break namepsaceDeclarationLoop;
					default:
						declaration(namespace);
				}
				if (lastToken == LA(1))
					consumeToNextSemicolon();
			}
			// consume the }
			consume();
			callback.namespaceDefinitionEnd( namespace );
		}
		else
		{
			callback.namespaceDefinitionAbort( namespace );
			throw backtrack;
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
	protected void simpleDeclaration( Object container ) throws Backtrack {
		Object simpleDecl = callback.simpleDeclarationBegin( container);
		declSpecifierSeq(simpleDecl, false);

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
			case Token.tCOLON:
				// TODO - Initializer for constructor, for now consume
				// and look for the left brace;
				consume();
				while (LT(1) != Token.tLBRACE) {
					consume();
				}
				// Falling through on purpose
			case Token.tLBRACE:
				Object function = callback.functionBodyBegin(simpleDecl );
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
						}
					}
				} else {
					functionBody();
				}
				callback.functionBodyEnd(function);
				break;
			default:
				break;
		}
		
		callback.simpleDeclarationEnd(simpleDecl);
	}
	
	
	protected void parameterDeclaration( Object containerObject ) throws Backtrack
	{
		Object parameterDecl = callback.parameterDeclarationBegin( containerObject );
		declSpecifierSeq( parameterDecl, true );
		
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
	protected void declSpecifierSeq( Object decl, boolean parm ) throws Backtrack {
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
				case Token.t_signed:
				case Token.t_unsigned:
				case Token.t_short:
					callback.simpleDeclSpecifier(decl, consume());
					break;
				case Token.t_char:
				case Token.t_wchar_t:
				case Token.t_bool:
				case Token.t_int:
				case Token.t_long:
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
					// TODO - Kludgy way to handle constructors/destructors
					// handle nested later:
					if ((parm && !encounteredRawType) || (!encounteredRawType && LT(2) != Token.tCOLONCOLON && LT(2) != Token.tLPAREN))
					{
						if( ! encounteredTypename )
						{
							callback.simpleDeclSpecifier(decl,LA(1));
							name(); 
							callback.simpleDeclSpecifierName( decl );
							encounteredTypename = true; 
							break;
						}
					}
					return;
				case Token.t_class:
				case Token.t_struct:
				case Token.t_union:
					try
					{
						classSpecifier(decl);
						return;
					}
					catch( Backtrack bt )
					{
						// this is an elaborated class specifier
						Object elab = callback.elaboratedTypeSpecifierBegin( decl, consume() ); 
						name(); 
						callback.elaboratedTypeSpecifierName( elab ); 
						callback.elaboratedTypeSpecifierEnd( elab );
						break;
					}
				case Token.t_enum:
					try
					{
						enumSpecifier(decl);
						return;
					}
					catch( Backtrack bt )
					{
						// this is an elaborated class specifier
						Object elab = callback.elaboratedTypeSpecifierBegin( decl, consume() ); 
						name(); 
						callback.elaboratedTypeSpecifierName( elab ); 
						callback.elaboratedTypeSpecifierEnd( elab );
					}
					break;
				default:
					break declSpecifiers;
			}
		}
	}


	protected void identifier() throws Backtrack {
		Token first = consume(Token.tIDENTIFIER); // throws backtrack if its not that
		callback.nameBegin(first);
		callback.nameEnd(first);
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
	protected void name() throws Backtrack {
		Token first = LA(1);
		Token last = null;
		
		callback.nameBegin(first);
		
		if (LT(1) == Token.tCOLONCOLON)
			last = consume();

		// TODO - whacky way to deal with destructors, please revisit
		if (LT(1) == Token.tCOMPL)
			consume();
				
		switch (LT(1)) {
			case Token.tIDENTIFIER:
				last = consume();
				break;
			default:
				throw backtrack;
		}

		while (LT(1) == Token.tCOLONCOLON) {
			last = consume();
			
			if (LT(1) == Token.tCOMPL)
				consume();
				
			switch (LT(1)) {
				case Token.tIDENTIFIER:
					last = consume();
			}
		}

		callback.nameEnd(last);

	}

	/**
	 * cvQualifier
	 * : "const" | "volatile"
	 */
	protected void cvQualifier( Object ptrOp ) throws Backtrack {
		switch (LT(1)) {
			case Token.t_const:
			case Token.t_volatile:
				callback.pointerOperatorCVModifier( ptrOp, consume() ); 
				return;
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
	protected void initDeclarator( Object owner ) throws Backtrack {
		Object declarator = declarator( owner );
		
		// handle = initializerClause
		if (LT(1) == Token.tASSIGN) {
			consume(); 
			
			// assignmentExpression || { initializerList , } || { }
			Object expression = null; 
			try
			{
				expression = callback.expressionBegin( declarator ); 
				assignmentExpression( expression );
				callback.expressionEnd( expression );   
			}
			catch( Backtrack b )
			{
				if( expression != null )
					callback.expressionAbort( expression ); 
			}
			
			if (LT(1) == Token.tLBRACE) {
				// for now, just consume to matching brace
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
					}
				}
			}
		}
		else if( LT(1) == Token.tLPAREN )
		{
			consume();  // EAT IT!
			
			Object expression = null; 
			try
			{
				expression = callback.expressionBegin( declarator ); 
				constantExpression( expression );
				callback.expressionEnd( expression );   
			}
			catch( Backtrack b )
			{
				if( expression != null )
					callback.expressionAbort( expression ); 
			}
			
			if( LT(1) == Token.tRPAREN )
				consume();
		
		}
		
		callback.declaratorEnd( declarator );
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
	protected Object declarator( Object container ) throws Backtrack {
		
		do
		{
			Object declarator = callback.declaratorBegin( container );
			
			for (;;) {
				try {
					ptrOperator(declarator);
				} catch (Backtrack b) {
					break;
				}
			}
			
			if (LT(1) == Token.tLPAREN) {
				consume();
				declarator(declarator);
				consume(Token.tRPAREN);
				return declarator;
			}
			
			name();
			callback.declaratorId(declarator);
			
			for (;;) {
				switch (LT(1)) {
					case Token.tLPAREN:
						// temporary fix for initializer/function declaration ambiguity
						if( LT(2) != Token.tINTEGER )
						{
							// parameterDeclarationClause
							Object clause = callback.argumentsBegin(declarator);
							consume();
							boolean seenParameter = false;
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
										seenParameter = false;
										break;
									default:
										if (seenParameter)
											throw backtrack;
										parameterDeclaration( clause );
										seenParameter = true;
								}
							}
							callback.argumentsEnd(clause);
							
							// const-volatile marker on the method
							if( LT(1) == Token.t_const || LT(1) == Token.t_volatile )
							{
								callback.declaratorCVModifier( declarator, consume() );
							}
							
							//check for throws clause here 
							if( LT(1) == Token.t_throw )
							{
								callback.declaratorThrowsException( declarator );
								consume(); // throw
								consume( Token.tLPAREN );// (
								boolean done = false; 
								while( ! done )
								{	
									switch( LT(1) )
									{
										case Token.tRPAREN:
											consume();  
											done = true; 
											break; 
										case Token.tIDENTIFIER: 
											//TODO this is not exactly right - should be type-id rather than just a name
											name(); 
											callback.declaratorThrowExceptionName( declarator );
											break;
										case Token.tCOMMA: 
											consume(); 
											break;
										default: 
											System.out.println( "Unexpected Token =" + LA(1).getImage() ); 
											consumeToNextSemicolon(); 
											continue;
									}
								}
							}
						}
						break;
					case Token.tLBRACKET:
						while( LT(1) == Token.tLBRACKET )
						{
							consume(); // eat the '['
							Object array = callback.arrayDeclaratorBegin( declarator ); 
							if( LT(1) != Token.tRBRACKET )
							{
								Object expression = callback.expressionBegin( array ); 
								constantExpression(expression);
								callback.expressionEnd( expression ); 
							}
							consume(Token.tRBRACKET);
							callback.arrayDeclaratorEnd( array );
						}
						continue;
				}
				break;
			}
			
			if( LA(1).getType() == Token.tIDENTIFIER )
			{
				callback.declaratorAbort( container, declarator );
				declarator = null;
			}
			else
				return declarator; 
		} while( true );
	}
	
	/**
	 * ptrOperator
	 * : "*" (cvQualifier)*
	 * | "&"
	 * | name "*" (cvQualifier)*
	 */
	protected void ptrOperator(Object owner) throws Backtrack {
		int t = LT(1);
		Object ptrOp = callback.pointerOperatorBegin( owner ); 
		
		if (t == Token.tAMPER) {
			callback.pointerOperatorType( ptrOp, consume() ); 
			callback.pointerOperatorEnd( ptrOp );
			return;
		}
		
		Token mark = mark();
		if (t == Token.tIDENTIFIER || t == Token.tCOLONCOLON)
		{
			name();
			callback.pointerOperatorName( ptrOp );
		}

		if (t == Token.tSTAR) {
			callback.pointerOperatorType( ptrOp, consume());

			for (;;) {
				try {
					cvQualifier( ptrOp );
				} catch (Backtrack b) {
					// expected at some point
					break;
				}
			}			
			
			callback.pointerOperatorEnd( ptrOp );
			return;
		}
		
		backup(mark);
		throw backtrack;
	}


	/**
	 * enumSpecifier:
	 * 		"enum" (name)? "{" (enumerator-list) "}"
	 * enumerator-list:
	 * 	enumerator-definition
	 *	enumerator-list , enumerator-definition
	 * enumerator-definition:
	 * 	enumerator
	 *  enumerator = constant-expression
	 * enumerator: identifier 
	 */
	protected void enumSpecifier( Object owner ) throws Backtrack
	{
		consume( Token.t_enum );

		Object enumSpecifier = callback.enumSpecifierBegin( owner );

		if( LT(1) == Token.tIDENTIFIER )
		{ 
			identifier();
			callback.enumSpecifierId( enumSpecifier );
		} 
		
		if( LT(1) == Token.tLBRACE )
		{
			consume( Token.tLBRACE );
			
			while( LT(1) != Token.tRBRACE )
			{
				Object defn;
				if( LT(1) == Token.tIDENTIFIER )
				{
					defn = callback.enumDefinitionBegin( enumSpecifier );
					identifier();
					callback.enumDefinitionId( defn ); 
				}
				else
				{
					callback.enumSpecifierAbort( enumSpecifier );
					throw backtrack; 
				}
				
				if( LT(1) == Token.tASSIGN )
				{
					consume( Token.tASSIGN );
					Object expression = callback.expressionBegin( defn );
					constantExpression( expression ); 
					callback.expressionEnd( expression );
				}
				
				callback.enumDefinitionEnd( defn );
				
				if( LT(1) == Token.tRBRACE )
					break;
				
				if( LT(1) != Token.tCOMMA )
				{
					callback.enumSpecifierAbort( enumSpecifier );
					throw backtrack; 					
				}
				consume(Token.tCOMMA); // if we made it this far 
			}
			consume( Token.tRBRACE );
			
			callback.enumSpecifierEnd( enumSpecifier );
		}
		else
		{
			// enumSpecifierAbort
			throw backtrack; 
		}

	}

	/**
	 * classSpecifier
	 * : classKey name (baseClause)? "{" (memberSpecification)* "}"
	 */
	protected void classSpecifier( Object owner ) throws Backtrack {
		Token classKey = null;
		
		Token mark = mark();
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
		
		if( LT(1) != Token.tCOLON && LT(1) != Token.tLBRACE )
		{
			// this is not a classSpecification
			callback.classSpecifierAbort( classSpec );
			classSpec = null; 	
			backup( mark ); 
			throw backtrack; 
		}
		else
			callback.classSpecifierSafe( classSpec );
		
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
				Token lastToken = LA(1);
			
				switch (LT(1)) {
					case Token.t_public:
					case Token.t_protected:
					case Token.t_private:
						callback.classMemberVisibility( classSpec, consume() );
						consume(Token.tCOLON);
						break;
					case Token.tRBRACE:
						consume(Token.tRBRACE);
						break memberDeclarationLoop;
					default:
						declaration(classSpec);
				}
				if (lastToken == LA(1))
					consumeToNextSemicolon();
			}
			// consume the }
			consume();
		}
		
		callback.classSpecifierEnd(classSpec);
	}

	protected void baseSpecifier( Object classSpecOwner ) throws Backtrack {

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
					callback.baseSpecifierVisibility( baseSpecifier, consume() );
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
	
	protected void functionBody() throws Backtrack {
		compoundStatement();
	}
	
	// Statements
	protected void statement() throws Backtrack {
		Object expression = null; 
		switch (LT(1)) {
			case Token.t_case:
				consume();
				expression = callback.expressionBegin( null ); //TODO regarding this null
				constantExpression(expression);
				callback.expressionEnd( expression );
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
				{
					expression = callback.expressionBegin( null ); //TODO get rid of NULL  
					expression(expression);
					callback.expressionEnd( expression );
				}
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
				{
					expression = callback.expressionBegin( null ); //TODO get rid of NULL  
					expression(expression);
					callback.expressionEnd( expression );
				}
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
					expression = callback.expressionBegin( null ); //TODO get rid of NULL  
					expression(expression);
					callback.expressionEnd( expression );
					consume(Token.tSEMI);
					return;
				} catch (Backtrack b) {
				}
				
				// declarationStatement
				declaration(null);
		}
	}
	
	protected void condition() throws Backtrack {
		// TO DO
	}
	
	protected void forInitStatement() throws Backtrack {
		// TO DO
	}
	
	protected void compoundStatement() throws Backtrack {
		consume(Token.tLBRACE);
		while (LT(1) != Token.tRBRACE)
			statement();
		consume();
	}
	
	// Expressions
	protected void constantExpression( Object expression ) throws Backtrack {
		conditionalExpression( expression );
	}
	
	public void expression( Object expression ) throws Backtrack {
		assignmentExpression( expression );
		
		while (LT(1) == Token.tCOMMA) {
			Token t = consume();
			assignmentExpression( expression );
			callback.expressionOperator(expression, t);
		}
	}
	
	protected void assignmentExpression( Object expression ) throws Backtrack {
		if (LT(1) == Token.t_throw) {
			throwExpression(expression);
			return;
		}
		
		// if the condition not taken, try assignment operators
		if (!conditionalExpression(expression)) {
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
					conditionalExpression(expression);
					callback.expressionOperator(expression, t);
					break;
			}
		}
	}
	
	protected void throwExpression( Object expression ) throws Backtrack {
		consume(Token.t_throw);
		
		try {
			expression(expression);
		} catch (Backtrack b) {
		}
	}
	
	protected boolean conditionalExpression( Object expression ) throws Backtrack {
		logicalOrExpression( expression );
		
		if (LT(1) == Token.tQUESTION) {
			consume();
			expression(expression);
			consume(Token.tCOLON);
			assignmentExpression(expression);
			return true;
		} else
			return false;
	}
	
	protected void logicalOrExpression( Object expression ) throws Backtrack {
		logicalAndExpression( expression );
		
		while (LT(1) == Token.tOR) {
			Token t = consume();
			logicalAndExpression( expression );
			callback.expressionOperator(expression, t);
		}
	}
	
	protected void logicalAndExpression( Object expression ) throws Backtrack {
		inclusiveOrExpression( expression );
		
		while (LT(1) == Token.tAND) {
			Token t = consume();
			inclusiveOrExpression(expression );
			callback.expressionOperator(expression, t);
		}
	}
	
	protected void inclusiveOrExpression( Object expression ) throws Backtrack {
		exclusiveOrExpression(expression);
		
		while (LT(1) == Token.tBITOR) {
			Token t = consume();
			exclusiveOrExpression(expression);
			callback.expressionOperator(expression, t);
		}
	}
	
	protected void exclusiveOrExpression( Object expression ) throws Backtrack {
		andExpression( expression );
		
		while (LT(1) == Token.tXOR) {
			Token t = consume();
			andExpression(expression);
			callback.expressionOperator(expression, t);
		}
	}
	
	protected void andExpression( Object expression ) throws Backtrack {
		equalityExpression(expression);
		
		while (LT(1) == Token.tAMPER) {
			Token t = consume();
			equalityExpression(expression);
			callback.expressionOperator(expression, t);
		}
	}
	
	protected void equalityExpression(Object expression) throws Backtrack {
		relationalExpression(expression);
		
		for (;;) {
			switch (LT(1)) {
				case Token.tEQUAL:
				case Token.tNOTEQUAL:
					Token t = consume();
					relationalExpression(expression);
					callback.expressionOperator(expression, t);
					break;
				default:
					return;
			}
		}
	}
	
	protected void relationalExpression(Object expression) throws Backtrack {
		shiftExpression(expression);
		
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
					shiftExpression(expression);
					callback.expressionOperator(expression, t);
					break;
				default:
					return;
			}
		}
	}
	
	protected void shiftExpression( Object expression ) throws Backtrack {
		additiveExpression(expression);
		
		for (;;) {
			switch (LT(1)) {
				case Token.tSHIFTL:
				case Token.tSHIFTR:
					Token t = consume();
					additiveExpression(expression);
					callback.expressionOperator(expression, t);
					break;
				default:
					return;
			}
		}
	}
	
	protected void additiveExpression( Object expression ) throws Backtrack {
		multiplicativeExpression(expression);
		
		for (;;) {
			switch (LT(1)) {
				case Token.tPLUS:
				case Token.tMINUS:
					Token t = consume();
					multiplicativeExpression(expression);
					callback.expressionOperator(expression, t);
					break;
				default:
					return;
			}
		}
	}
	
	protected void multiplicativeExpression( Object expression ) throws Backtrack {
		pmExpression( expression );
		
		for (;;) {
			switch (LT(1)) {
				case Token.tSTAR:
				case Token.tDIV:
				case Token.tMOD:
					Token t = consume();
					pmExpression(expression );
					callback.expressionOperator(expression , t);
					break;
				default:
					return;
			}
		}
	}
	
	protected void pmExpression( Object expression ) throws Backtrack {
		castExpression( expression );
		
		for (;;) {
			switch (LT(1)) {
				case Token.tDOTSTAR:
				case Token.tARROWSTAR:
					Token t = consume();
					castExpression( expression );
					callback.expressionOperator(expression, t);
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
	protected void castExpression( Object expression ) throws Backtrack {
		// TO DO: we need proper symbol checkint to ensure type name
		if (false && LT(1) == Token.tLPAREN) {
			Token mark = mark();
			consume();
			
			// If this isn't a type name, then we shouldn't be here
			try {
				typeId();
				consume(Token.tRPAREN);
				castExpression( expression );
				return;
			} catch (Backtrack b) {
				backup(mark);
			}
		}

		unaryExpression(expression);
	}
	
	protected void typeId() throws Backtrack {
		try {
			name();
			return;
		} catch (Backtrack b) {
		}
	}
	
	protected void deleteExpression( Object expression ) throws Backtrack {
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
		
		castExpression( expression );
	}
	
	protected void newExpression( Object expression ) throws Backtrack {
		if (LT(1) == Token.tCOLONCOLON) {
			// global scope
			consume();
		}
		
		consume (Token.t_new);
		
		//TODO: finish this horrible mess...
	}
	
	protected void unaryExpression( Object expression ) throws Backtrack {
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
				castExpression(expression);
				callback.expressionOperator(expression, t);
				return;
			case Token.t_sizeof:
				if (LT(1) == Token.tLPAREN) {
					consume();
					typeId();
					consume(Token.tRPAREN);
				} else {
					unaryExpression( expression );
				}
				return;
			case Token.t_new:
				newExpression( expression );
				return;
			case Token.t_delete:
				deleteExpression( expression );
				return;
			case Token.tCOLONCOLON:
				switch (LT(2)) {
					case Token.t_new:
						newExpression(expression);
						return;
					case Token.t_delete:
						deleteExpression(expression);
						return;
					default:
						postfixExpression(expression);
						return;			
				}
			default:
				postfixExpression(expression);
				return;
		}
	}

	protected void postfixExpression( Object expression) throws Backtrack {
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
				expression(expression);
				consume(Token.tRPAREN);
				break;
			case Token.t_typeid:
				consume();
				consume(Token.tLPAREN);
				try {
					typeId();
				} catch (Backtrack b) {
					expression(expression);
				}
				consume(Token.tRPAREN);
				break;
			default:
				// TO DO: try simpleTypeSpecifier "(" expressionList ")"
				primaryExpression(expression);
		}
		
		for (;;) {
			switch (LT(1)) {
				case Token.tLBRACKET:
					// array access
					consume();
					expression(expression);
					consume(Token.tRBRACKET);
					break;
				case Token.tLPAREN:
					// function call
					consume();
					// Note: since expressionList and expression are the same...
					expression(expression);
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
	
	protected void primaryExpression( Object expression ) throws Backtrack {
		int type = LT(1);
		switch (type) {
			// TO DO: we need more literals...
			case Token.tINTEGER:
				callback.expressionTerminal(expression, consume());
				return;
			case Token.tSTRING:
				callback.expressionTerminal(expression, consume());
				return;
			case Token.tIDENTIFIER:
				callback.expressionTerminal(expression, consume());
				return;
			case Token.t_this:
				consume();
				return;
			case Token.tLPAREN:
				consume();
				expression(expression);
				consume(Token.tRPAREN);
				return;
			default:
				// TO DO: idExpression which yeilds a variable
				//idExpression();
				return;
		}
	}
	
	protected void varName() throws Exception {
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
	public static class Backtrack extends Exception {
	}
	
	private static Backtrack backtrack = new Backtrack();
	
	// End of file generally causes backtracking
	public static class EndOfFile extends Backtrack {
	}
	
	public static EndOfFile endOfFile = new EndOfFile();
	
	// Token management
	private IScanner scanner;
	private Token currToken;
	
	private Token fetchToken() throws EndOfFile {
		try {
			return scanner.nextToken();
		} catch (EndOfFile e) {
			throw e;
		} catch (Exception e) {
			return null;
		}
	}

	protected Token LA(int i) throws EndOfFile {
		if (i < 1)
			// can't go backwards
			return null;

		if (currToken == null)
			currToken = fetchToken();
		
		Token retToken = currToken;
		 
		for (; i > 1; --i) {
			retToken = retToken.getNext();
			if (retToken == null)
				retToken = fetchToken();
		}
		
		return retToken;
	}

	protected int LT(int i) throws EndOfFile {
		return LA(i).type;
	}
	
	protected Token consume() throws EndOfFile {
		if (currToken == null)
			currToken = fetchToken();

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
	
	protected Token mark() throws EndOfFile {
		if (currToken == null)
			currToken = fetchToken();
		return currToken;
	}
	
	protected void backup(Token mark) {
		currToken = mark;
	}

	// Utility routines that require a knowledge of the grammar
	protected static String generateName(Token startToken) throws ParserException {
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
