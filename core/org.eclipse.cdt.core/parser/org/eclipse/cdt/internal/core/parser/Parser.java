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

	public Parser(String code, IParserCallback c, boolean quickParse ) throws Exception {
		this(new Scanner().initialize( new StringReader( code ), null
), c, quickParse);
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
		Object translationUnit = null;
		try{ translationUnit = callback.translationUnitBegin();} catch( Exception e ) {}
		Token lastBacktrack = null;
		Token checkToken;
		while (true) {
			try {
				checkToken = LA(1);
				declaration( translationUnit );
				if( LA(1) == checkToken )
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
		try{ callback.translationUnitEnd(translationUnit);} catch( Exception e ) {}
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
			Object directive = null; 
			try{ directive = callback.usingDirectiveBegin( container );} catch( Exception e ) {}
			// using-directive
			consume( Token.t_namespace );
			
			// optional :: and nested classes handled in name	
			if( LT(1) == Token.tIDENTIFIER || LT(1) == Token.tCOLONCOLON )
			{
				name();
				try{ directive = callback.usingDirectiveNamespaceId( directive );} catch( Exception e ) {}
			}
			else
			{
				try{ callback.usingDirectiveAbort(directive);} catch( Exception e ) {}
				throw backtrack;
			}
			
			if( LT(1) == Token.tSEMI )
			{
				consume( Token.tSEMI );
				try{ callback.usingDirectiveEnd( directive );} catch( Exception e ) {}
				return;
			}
			else
			{
				try{ callback.usingDirectiveAbort(directive);} catch( Exception e ) {}
				throw backtrack;				
			}
		}
		else
		{
			Object usingDeclaration = null;
			try{ usingDeclaration = callback.usingDeclarationBegin( container );} catch( Exception e ) {}
			
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
				try{ usingDeclaration = callback.usingDeclarationMapping( usingDeclaration, typeName ); } catch( Exception e ) {}
			}
			else
			{
				try{ callback.usingDeclarationAbort( usingDeclaration );} catch( Exception e ) {}
				throw backtrack;
			}
		
			if( LT(1) == Token.tSEMI )
			{
				consume( Token.tSEMI );
				try{ callback.usingDeclarationEnd( usingDeclaration );} catch( Exception e ) {}
			}
			else
			{
				try{ callback.usingDeclarationAbort( usingDeclaration );} catch( Exception e ) {}
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

		Object linkageSpec = null; 
		try{ linkageSpec = callback.linkageSpecificationBegin( container, consume( Token.tSTRING ).getImage() );} catch( Exception e ) {}

		if( LT(1) == Token.tLBRACE )
		{
			consume(Token.tLBRACE); 
			linkageDeclarationLoop:
			while (LT(1) != Token.tRBRACE) {
				Token checkToken = LA(1);
				switch (LT(1)) {
					case Token.tRBRACE:
						consume(Token.tRBRACE);
						break linkageDeclarationLoop;
					default:
						declaration(linkageSpec);
				}
				if (checkToken == LA(1))
					consumeToNextSemicolon();
			}
			// consume the }
			consume();
			try{ callback.linkageSpecificationEnd( linkageSpec );} catch( Exception e ) {}
		}
		else // single declaration
		{
			declaration( linkageSpec );
			try{ callback.linkageSpecificationEnd( linkageSpec );} catch( Exception e ) {}
		}
	}
	
	/*
	 * 
	 * template-declaration:	export? template <template-parameter-list> declaration
	 * explicit-instantiation:	template declaration
	 * explicit-specialization:	template <> declaration
	 * 
	 * template-parameter-list:	template-parameter
	 *							template-parameter-list , template-parameter
	 * template-parameter:		type-parameter
	 *							parameter-declaration
	 * type-parameter:			class identifier?
	 *							class identifier? = type-id
	 * 							typename identifier?
	 * 							typename identifier? = type-id
	 *							template < template-parameter-list > class identifier?
	 *							template < template-parameter-list > class identifier? = id-expression
	 * template-id:				template-name < template-argument-list?>
	 * template-name:			identifier
	 * template-argument-list:	template-argument
	 *							template-argument-list , template-argument
	 * template-argument:		assignment-expression
	 *							type-id
	 *							id-expression
	 * 
	 * @param container
	 */
	protected void templateDeclaration( Object container ) throws Backtrack
	{
		Token firstToken = null; 
		if( LT(1) == Token.t_export )
		{
			firstToken = consume( Token.t_export );
			consume( Token.t_template ); 
		}
		else
			firstToken = consume( Token.t_template );
		
		
		if( LT(1) != Token.tLT )
		{
			// explicit-instantiation
			Object instantiation = null; 
			try { instantiation = callback.explicitInstantiationBegin( container ); } catch( Exception e ) { }
			declaration( instantiation );
			try { callback.explicitInstantiationEnd( instantiation ); } catch( Exception e ) { }
			return;
		}
		else
		{
			consume( Token.tLT );
			if( LT(1) == Token.tGT )
			{
				consume( Token.tGT ); 
				// explicit-specialization
				Object specialization = null;
				try{ specialization = callback.explicitSpecializationBegin( container ); } catch( Exception e ) { }
				declaration( specialization ); 
				try{ callback.explicitSpecializationEnd( specialization ); } catch( Exception e ) { }
				return;
			}
		}
		
		Object templateDeclaration = null;
		try
		{
			try{ templateDeclaration = callback.templateDeclarationBegin( container, firstToken ); } catch ( Exception e ) {}
			templateParameterList( templateDeclaration );
			consume( Token.tGT );
			declaration( templateDeclaration ); 
			try{ callback.templateDeclarationEnd( templateDeclaration, lastToken ); } catch( Exception e ) {}
			
		} catch( Backtrack bt )
		{
			try { callback.templateDeclarationAbort( templateDeclaration ); } catch( Exception e ) {}
		}
	}

	protected void templateParameterList( Object templateDeclaration ) throws EndOfFile, Backtrack {
		// if we have gotten this far then we have a true template-declaration
		// iterate through the template parameter list
		
		Object templateParameterList = null;
		
		try { templateParameterList = callback.templateParameterListBegin( templateDeclaration ); } catch( Exception e ) {}
		
		for ( ; ; )
		{
			if( LT(1) == Token.tGT ) return; 
			if( LT(1) == Token.t_class || LT(1) == Token.t_typename )
			{
				Object currentTemplateParm = null;
				try
				{
					try{ 
						currentTemplateParm = callback.templateTypeParameterBegin( 
						templateParameterList, consume() );
					} catch( Exception e ) {} 
					if( LT(1) == Token.tIDENTIFIER ) // optional identifier
					{
						identifier(); 
						try { currentTemplateParm = callback.templateTypeParameterName( currentTemplateParm );} catch( Exception e ) {}
						if( LT(1) == Token.tASSIGN ) // optional = type-id
						{
							consume( Token.tASSIGN );
							identifier(); // type-id
							try{ currentTemplateParm = callback.templateTypeParameterInitialTypeId( currentTemplateParm ); }catch( Exception e ) {}
						}
					}
					try{ callback.templateTypeParameterEnd( currentTemplateParm );	} catch( Exception e ) {}
					
				} catch( Backtrack bt )
				{
					try{ callback.templateTypeParameterAbort( currentTemplateParm ); }catch( Exception e ) {}
					throw bt;
				}
			}
			else if( LT(1) == Token.t_template )
			{
				Token kind = consume( Token.t_template );
				consume( Token.tLT );
				Object newTemplateParm = null;
				try{ newTemplateParm = callback.templateTypeParameterBegin(templateParameterList,kind ); } catch( Exception e ) {}
				templateParameterList( newTemplateParm );
				consume( Token.tGT );						 
				consume( Token.t_class );
				if( LT(1) == Token.tIDENTIFIER ) // optional identifier
				{
					identifier();
					try{ newTemplateParm = callback.templateTypeParameterName( newTemplateParm );} catch( Exception e ) {} 
					if( LT(1) == Token.tASSIGN ) // optional = type-id
					{
						consume( Token.tASSIGN );
						name(); 
						try{ newTemplateParm = callback.templateTypeParameterInitialTypeId( newTemplateParm );} catch( Exception e ) {}
					}
				}
				try{ callback.templateTypeParameterEnd( newTemplateParm );} catch( Exception e ) {}
			}
			else if( LT(1) == Token.tCOMMA )
			{
				consume( Token.tCOMMA );
				continue;
			}
			else
			{
				parameterDeclaration( templateParameterList );
			}
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
				consume( Token.t_asm );
				consume( Token.tLPAREN );
				String assembly = consume( Token.tSTRING ).getImage();
				consume( Token.tRPAREN ); 
				consume( Token.tSEMI );
				// if we made it this far, then we have all we need 
				// do the callback
				try{ callback.asmDefinition( container, assembly );} catch( Exception e ) {}
				return; 
			case Token.t_namespace:
				namespaceDefinition( container );
				return; 
			case Token.t_using:
				usingClause( container );
				return; 
			case Token.t_export:
			case Token.t_template:
				templateDeclaration( container );
				return; 
			case Token.t_extern:
				if( LT(2) == Token.tSTRING )
				{
					linkageSpecification( container ); 
					return;
				}
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
		Object namespace = null;
		try{ namespace = callback.namespaceDefinitionBegin( container, consume( Token.t_namespace) );} catch( Exception e ) {}

		// optional name 		
		if( LT(1) == Token.tIDENTIFIER )
		{
			name();
			try{ namespace = callback.namespaceDefinitionId( namespace );} catch( Exception e ) {}
		}
	
		if( LT(1) == Token.tLBRACE )
		{
			consume(); 
			namepsaceDeclarationLoop:
			while (LT(1) != Token.tRBRACE) {
				Token checkToken = LA(1);
				switch (LT(1)) {
					case Token.tRBRACE:
						consume(Token.tRBRACE);
						break namepsaceDeclarationLoop;
					default:
						declaration(namespace);
				}
				if (checkToken == LA(1))
					consumeToNextSemicolon();
			}
			// consume the }
			
			try{ callback.namespaceDefinitionEnd( namespace, consume( Token.tRBRACE ));} catch( Exception e ) {}
		}
		else
		{
			try{ callback.namespaceDefinitionAbort( namespace );} catch( Exception e ) {}
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
		Object simpleDecl = null; 
		try{ simpleDecl = callback.simpleDeclarationBegin( container, LA(1));} catch( Exception e ) {}
		declSpecifierSeq(simpleDecl, false);
		Object declarator = null; 

		if (LT(1) != Token.tSEMI)
			try {
				declarator = initDeclarator(simpleDecl);
				
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
				consume(Token.tSEMI);
				break;
			case Token.tCOLON:
				ctorInitializer(declarator);					
				// Falling through on purpose
			case Token.tLBRACE:
				Object function = null; 
				try{ function = callback.functionBodyBegin(simpleDecl ); } catch( Exception e ) {}
				if (quickParse) {
					// speed up the parser by skiping the body
					// simply look for matching brace and return
					consume(Token.tLBRACE);
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
				try{ callback.functionBodyEnd(function );} catch( Exception e ) {}
				break;
			default:
				break;
		}
		
		try{ callback.simpleDeclarationEnd(simpleDecl, lastToken);} catch( Exception e ) {}
	}

	protected void ctorInitializer(Object declarator) throws Backtrack  {
		consume( Token.tCOLON );
		
		Object constructorChain = null; 
		try { constructorChain = callback.constructorChainBegin(declarator);} catch( Exception e ) {}
		
		try	{
			for( ; ; ) {
				if( LT(1) == Token.tLBRACE ) break;
				
				Object constructorChainElement = null; 
				try{ constructorChainElement = callback.constructorChainElementBegin( constructorChain );} catch( Exception e ) {}
				name(); 
				try{ constructorChainElement = callback.constructorChainElementId(constructorChainElement);} catch( Exception e) {} 
			
				consume( Token.tLPAREN );
				
				while( LT(1) != Token.tRPAREN )
				{
					//handle expression list here
					Object item = null; 
					try{ item = callback.constructorChainElementExpressionListElementBegin(constructorChainElement );} catch( Exception e ) {}
					Object expression = null; 
					try{ expression = callback.expressionBegin( item );} catch( Exception e) {}
					assignmentExpression( expression );
					try{ callback.expressionEnd( item );} catch( Exception e) {}
					try{ callback.constructorChainElementExpressionListElementEnd( item );} catch( Exception e) {}
					if( LT(1) == Token.tRPAREN ) break;
					consume( Token.tCOMMA );
				}

				consume( Token.tRPAREN );					
				try{ callback.constructorChainElementEnd(constructorChainElement );} catch( Exception e) {}
				
				if( LT(1) == Token.tLBRACE ) break;
				if( LT(1) == Token.tCOMMA ) consume( Token.tCOMMA );
			}
		}
		catch( Backtrack bt )
		{
			try { callback.constructorChainAbort( constructorChain );} catch( Exception e ) {} 
			if( ! quickParse )
				throw backtrack;  
		}
		
		try { callback.constructorChainEnd( constructorChain ); }  catch( Exception e ) {}
	}
	
	
	protected void parameterDeclaration( Object containerObject ) throws Backtrack
	{
		Object parameterDecl = null;
		try{ parameterDecl = callback.parameterDeclarationBegin( containerObject );} catch( Exception e ) {}
		declSpecifierSeq( parameterDecl, true );
		
		if (LT(1) != Token.tSEMI)
			try {
				Object declarator = initDeclarator(parameterDecl);
				
			} catch (Backtrack b) {
				// allowed to be empty
			}
 		 
		try{ callback.parameterDeclarationEnd( parameterDecl );} catch( Exception e ) {}
		 
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
					try{ decl = callback.simpleDeclSpecifier(decl, consume());} catch( Exception e ) {}
					break;
				case Token.t_signed:
				case Token.t_unsigned:					
				case Token.t_short:					
				case Token.t_char:
				case Token.t_wchar_t:
				case Token.t_bool:
				case Token.t_int:
				case Token.t_long:
				case Token.t_float:
				case Token.t_double:
				case Token.t_void:
					encounteredRawType = true;
					try{ decl = callback.simpleDeclSpecifier(decl, consume());} catch( Exception e ) {}
					break;
				case Token.t_typename:
					consume( Token.t_typename );
					name();
					break;
				case Token.tCOLONCOLON:
					consume( Token.tCOLONCOLON );
					// handle nested later:
				case Token.tIDENTIFIER:
					// TODO - Kludgy way to handle constructors/destructors
					// handle nested later:
					if ((parm && !encounteredRawType) || (!encounteredRawType && LT(2) != Token.tCOLONCOLON && LT(2) != Token.tLPAREN))
					{
						if( ! encounteredTypename )
						{
							try{ decl = callback.simpleDeclSpecifier(decl,LA(1));} catch( Exception e ) {}
							name(); 
							try{ decl = callback.simpleDeclSpecifierName( decl );} catch( Exception e ) {}
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
						Object elab = null; 
						try{ elab = callback.elaboratedTypeSpecifierBegin( decl, consume() );} catch( Exception e ) {} 
						name(); 
						try{ elab = callback.elaboratedTypeSpecifierName( elab ); } catch( Exception e ) {}
						try{ callback.elaboratedTypeSpecifierEnd( elab );} catch( Exception e ) {}
						encounteredTypename = true;
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
						Object elab = null; 
						try{ elab = callback.elaboratedTypeSpecifierBegin( decl, consume() ); } catch( Exception e ) {} 
						name(); 
						try{ elab = callback.elaboratedTypeSpecifierName( elab );} catch( Exception e ) {} 
						try{ callback.elaboratedTypeSpecifierEnd( elab );} catch( Exception e ) {}
					}
					break;
				default:
					break declSpecifiers;
			}
		}
	}


	protected void identifier() throws Backtrack {
		Token first = consume(Token.tIDENTIFIER); // throws backtrack if its not that
		try
		{ 
			callback.nameBegin(first);
			callback.nameEnd(first);
		} catch( Exception e ) {}
	}
	
	
	/* class-name: identifier | template-id
	 * template-id: template-name < template-argument-list opt >
	 * template-name : identifier
	 */
	protected void className() throws Backtrack
	{	
		Token first = LA(1);
		if( LT(1)  == Token.tIDENTIFIER)
		{
			if( LT(2) == Token.tLT )
			{
				consume( Token.tIDENTIFIER );
				consume( Token.tLT );
				
				// until we get all the names sorted out
				int depth = 1;
				Token last = null; 

				while (depth > 0) {
					last = consume();
					switch ( last.getType()) {
						case Token.tGT:
							--depth;
							break;
						case Token.tLT:
							++depth;
						break;
					}
				}
				
				callback.nameBegin( first );
				callback.nameEnd( last );
			}
			else
			{
				identifier();
				return;
			} 
		}
		else
			throw backtrack;
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
		
		Token mark = mark();
		try{ callback.nameBegin(first); } catch( Exception e ) {}
		
		if (LT(1) == Token.tCOLONCOLON)
			last = consume();

		// TODO - whacky way to deal with destructors, please revisit
		if (LT(1) == Token.tCOMPL)
			consume();
				
		switch (LT(1)) {
			case Token.tIDENTIFIER:
				last = consume();
				if( LT(1) == Token.tLT )
				{
					consume( Token.tLT );
				
					// until we get all the names sorted out
					int depth = 1; 

					while (depth > 0) {
						last = consume();
						switch ( last.getType()) {
							case Token.tGT:
								--depth;
								break;
							case Token.tLT:
								++depth;
							break;
						}
					}
				}
				break;
			default:
				backup( mark );
				throw backtrack;
		}

		while (LT(1) == Token.tCOLONCOLON) {
			last = consume();
			
			if (LT(1) == Token.tCOMPL)
				consume();
				
			switch (LT(1)) {
				case Token.t_operator:
					backup( mark );
					throw backtrack;
				case Token.tIDENTIFIER:
					last = consume();
					if( LT(1) == Token.tLT )
					{
						consume( Token.tLT );
				
						// until we get all the names sorted out
						int depth = 1; 

						while (depth > 0) {
							last = consume();
							switch ( last.getType()) {
								case Token.tGT:
									--depth;
									break;
								case Token.tLT:
									++depth;
								break;
							}
						}
					}
				
			}
		}

		try{ callback.nameEnd(last);} catch( Exception e ) {}

	}

	/**
	 * cvQualifier
	 * : "const" | "volatile"
	 */
	protected Object cvQualifier( Object ptrOp ) throws Backtrack {
		switch (LT(1)) {
			case Token.t_const:
			case Token.t_volatile:
				try{ ptrOp = callback.pointerOperatorCVModifier( ptrOp, consume() ); } catch( Exception e ) {}
				return ptrOp;
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
	protected Object initDeclarator( Object owner ) throws Backtrack {
		Object declarator = declarator( owner );
			
		// handle = initializerClause
		if (LT(1) == Token.tASSIGN) {
			consume(); 
			
			// assignmentExpression || { initializerList , } || { }
			Object expression = null; 
			try
			{
				try{ expression = callback.expressionBegin( declarator ); } catch( Exception e ) {}
				assignmentExpression( expression );
				try{ callback.expressionEnd( expression );} catch( Exception e ) {}   
			}
			catch( Backtrack b )
			{
				if( expression != null )
					try{ callback.expressionAbort( expression );} catch( Exception e ) {} 
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
			consume(Token.tLPAREN);  // EAT IT!
			
			Object expression = null; 
			try
			{
				try{ expression = callback.expressionBegin( declarator ); } catch( Exception e ) {}
				constantExpression( expression );
				try{ callback.expressionEnd( expression );   } catch( Exception e ) {}
			}
			catch( Backtrack b )
			{
				if( expression != null )
					try{ callback.expressionAbort( expression );} catch( Exception e ) {} 
			}
			
			if( LT(1) == Token.tRPAREN )
				consume();
		
		}
		
		try{ callback.declaratorEnd( declarator );} catch( Exception e ) {}
		return declarator;
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
		
		boolean anonymous = false;
		do
		{
			Object declarator = null;
			try{ declarator = callback.declaratorBegin( container );} catch( Exception e ) {}
			
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
			
			if( LT(1) == Token.t_operator )
			{
				// we know this is an operator
				Token operatorToken = consume( Token.t_operator );
				Token toSend = null;
				if( LA(1).isOperator() || LT(1) == Token.tLPAREN || LT(1) == Token.tLBRACKET )
				{
					if( (LT(1) == Token.t_new || LT(1) == Token.t_delete ) && 
							LT(2) == Token.tLBRACKET && LT(3) == Token.tRBRACKET )
					{
						consume(); 
						consume( Token.tLBRACKET );
						toSend = consume( Token.tRBRACKET );
						// vector new and delete operators
					}
					else if ( LT(1) == Token.tLPAREN && LT(2) == Token.tRPAREN )
					{
						// operator ()
						consume( Token.tLPAREN );
						toSend = consume( Token.tRPAREN );
					}
					else if ( LT(1) == Token.tLBRACKET && LT(2) == Token.tRBRACKET )
					{
						consume( Token.tLBRACKET );
						toSend = consume( Token.tRBRACKET ); 
					}
					else if( LA(1).isOperator() )
						toSend = consume();
					else 
						throw backtrack;
													
				}
				else
				{
					// temporary 
					while( LT(1) != Token.tLPAREN )
					{
						toSend = consume(); 
					}
				}
					
				try{ 
					callback.nameBegin( operatorToken );
					callback.nameEnd( toSend );
				} catch( Exception e ) {}

				try{ declarator = callback.declaratorId(declarator);} catch( Exception e ) {}				
			}
			else
			{
				try
				{
					name();
					try{ declarator = callback.declaratorId(declarator);} catch( Exception e ) {}
				}
				catch( Backtrack bt )
				{
					if( LT(1) == Token.tCOLONCOLON || LT(1) == Token.tIDENTIFIER )
					{
						Token start = consume();
						Token end = null;  
						while( LT(1) == Token.tCOLONCOLON || LT(1) == Token.tIDENTIFIER )
						{
							end = consume(); 
						}

						if( LT(1) == Token.t_operator )
						{
							if( LA(1).isOperator() || LT(1) == Token.tLPAREN || LT(1) == Token.tLBRACKET )
							{
								if( (LT(1) == Token.t_new || LT(1) == Token.t_delete ) && 
										LT(2) == Token.tLBRACKET && LT(3) == Token.tRBRACKET )
								{
									consume(); 
									consume( Token.tLBRACKET );
									end = consume( Token.tRBRACKET );
									// vector new and delete operators
								}
								else if ( LT(1) == Token.tLPAREN && LT(2) == Token.tRPAREN )
								{
									// operator ()
									consume( Token.tLPAREN );
									end = consume( Token.tRPAREN );
								}
								else if ( LT(1) == Token.tLBRACKET && LT(2) == Token.tRBRACKET )
								{
									consume( Token.tLBRACKET );
									end = consume( Token.tRBRACKET ); 
								}
								else if( LA(1).isOperator() )
									end = consume();
								else 
									throw backtrack;
													
							}
							else
							{
								// temporary 
								while( LT(1) != Token.tLPAREN )
								{
									end = consume(); 
								}
							}
					
							try{ 
								callback.nameBegin( start );
								callback.nameEnd( end );
							} catch( Exception e ) {}

							try{ declarator = callback.declaratorId(declarator);} catch( Exception e ) {}
						}				
					}
					else
					{
						// anonymous is good 
						anonymous = true;
					}
				}
			}

			
			for (;;) {
				switch (LT(1)) {
					case Token.tLPAREN:
						// temporary fix for initializer/function declaration ambiguity
						if( ! LA(2).looksLikeExpression() )
						{
							// parameterDeclarationClause
							Object clause = null; 
							try { clause = callback.argumentsBegin(declarator);} catch( Exception e ) {}
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
							try{ callback.argumentsEnd(clause);} catch( Exception e ) {}
							
							if( LT(1) == Token.tCOLON ) 
							{
								// this is most likely the definition of the constructor 
								return declarator; 
							}
														
							// const-volatile marker on the method
							if( LT(1) == Token.t_const || LT(1) == Token.t_volatile )
							{
								try{ declarator = callback.declaratorCVModifier( declarator, consume() );} catch( Exception e ) {}
							}
							
							//check for throws clause here 
							if( LT(1) == Token.t_throw )
							{
								try{ declarator = callback.declaratorThrowsException( declarator );} catch( Exception e ) {}
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
											try{ declarator = callback.declaratorThrowExceptionName( declarator );} catch( Exception e ) {}
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

							// check for optional pure virtual							
							if( LT(1) == Token.tASSIGN && LT(2) == Token.tINTEGER && LA(2).getImage().equals( "0") )
							{
								consume( Token.tASSIGN);
								consume( Token.tINTEGER);
								try{ declarator = callback.declaratorPureVirtual( declarator ); } catch( Exception e ) { }
							}

						}
						break;
					case Token.tLBRACKET:
						while( LT(1) == Token.tLBRACKET )
						{
							consume(); // eat the '['
							Object array = null; 
							try{ array = callback.arrayDeclaratorBegin( declarator ); } catch( Exception e ) {}
							if( LT(1) != Token.tRBRACKET )
							{
								Object expression = null; 
								try{ expression = callback.expressionBegin( array );} catch( Exception e ) {} 
								constantExpression(expression);
								try{ callback.expressionEnd( expression ); } catch( Exception e ) {}
							}
							consume(Token.tRBRACKET);
							try{ callback.arrayDeclaratorEnd( array );} catch( Exception e ) {}
						}
						continue;
					default:
						break;
				}
				break;				
			}
			
			if( LA(1).getType() == Token.tIDENTIFIER )
			{
				try{ callback.declaratorAbort( container, declarator ); } catch( Exception e ) {}
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
		Object ptrOp = null;
		try{ ptrOp = callback.pointerOperatorBegin( owner );} catch( Exception e ) {} 
		
		if (t == Token.tAMPER) {
			try{ ptrOp = callback.pointerOperatorType( ptrOp, consume(Token.tAMPER) ); } catch( Exception e ) {}
			try{ callback.pointerOperatorEnd( ptrOp );} catch( Exception e ) {}
			return;
		}
		
		Token mark = mark();
		
		boolean hasName = false; 
		if (t == Token.tIDENTIFIER || t == Token.tCOLONCOLON)
		{
			name();
			hasName = true; 
		}

		if (t == Token.tSTAR) {
			if( hasName )
				try{ ptrOp = callback.pointerOperatorName( ptrOp );} catch( Exception e ) {}
				
			try{ ptrOp = callback.pointerOperatorType( ptrOp, consume());} catch( Exception e ) {}

			for (;;) {
				try {
					ptrOp = cvQualifier( ptrOp );
				} catch (Backtrack b) {
					// expected at some point
					break;
				}
			}			
			
			try{ callback.pointerOperatorEnd( ptrOp );} catch( Exception e ) {}
			return;
		}
		
		backup(mark);
		try{ callback.pointerOperatorAbort( ptrOp ); } catch( Exception e ) { }
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
		Object enumSpecifier = null;
		try{ enumSpecifier = callback.enumSpecifierBegin( owner, consume( Token.t_enum ) );} catch( Exception e ) {}

		if( LT(1) == Token.tIDENTIFIER )
		{ 
			identifier();
			try{ enumSpecifier = callback.enumSpecifierId( enumSpecifier );} catch( Exception e ) {}
		} 
		
		if( LT(1) == Token.tLBRACE )
		{
			consume( Token.tLBRACE );
			
			while( LT(1) != Token.tRBRACE )
			{
				Object defn;
				if( LT(1) == Token.tIDENTIFIER )
				{
					defn = null; 
					try{ defn = callback.enumeratorBegin( enumSpecifier );} catch( Exception e ) {}
					identifier();
					try{ defn = callback.enumeratorId( defn ); } catch( Exception e ) {}
				}
				else
				{
					try{ callback.enumSpecifierAbort( enumSpecifier );} catch( Exception e ) {}
					throw backtrack; 
				}
				
				if( LT(1) == Token.tASSIGN )
				{
					consume( Token.tASSIGN );
					Object expression = null; 
					try{ expression = callback.expressionBegin( defn );} catch( Exception e ) {}
					constantExpression( expression ); 
					try{ callback.expressionEnd( expression );} catch( Exception e ) {}
				}
				

				try{ callback.enumeratorEnd( defn, lastToken );} catch( Exception e ) {}				
				if( LT(1) == Token.tRBRACE )
					break;
			
				
				if( LT(1) != Token.tCOMMA )
				{
					try{ callback.enumSpecifierAbort( enumSpecifier );} catch( Exception e ) {}
					throw backtrack; 					
				}
				consume(Token.tCOMMA);
			}
			try{ callback.enumSpecifierEnd( enumSpecifier, consume( Token.tRBRACE ) );} catch( Exception e ) {}
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

		Object classSpec = null;
		try{ classSpec = callback.classSpecifierBegin( owner, classKey);} catch( Exception e ){}
		
		// class name
		if (LT(1) == Token.tIDENTIFIER) {
			className();
			try{ classSpec = callback.classSpecifierName(classSpec);} catch( Exception e ){}			
		}
		
		if( LT(1) != Token.tCOLON && LT(1) != Token.tLBRACE )
		{
			// this is not a classSpecification
			try{ callback.classSpecifierAbort( classSpec );} catch( Exception e ){}
			classSpec = null; 	
			backup( mark ); 
			throw backtrack; 
		}
		else
			try{ classSpec = callback.classSpecifierSafe( classSpec ); } catch( Exception e ){}
		
		// base clause
		if (LT(1) == Token.tCOLON) {
			consume();
			baseSpecifier( classSpec );
		}
		
		if (LT(1) == Token.tLBRACE) {
			consume(Token.tLBRACE);
			
			memberDeclarationLoop:
			while (LT(1) != Token.tRBRACE) {
				Token checkToken = LA(1);
			
				switch (LT(1)) {
					case Token.t_public:
					case Token.t_protected:
					case Token.t_private:
						try{ classSpec = callback.classMemberVisibility( classSpec, consume() );} catch( Exception e ){}
						consume(Token.tCOLON);
						break;
					case Token.tRBRACE:
						consume(Token.tRBRACE);
						break memberDeclarationLoop;
					default:
						declaration(classSpec);
				}
				if (checkToken == LA(1))
					consumeToNextSemicolon();
			}
			// consume the }
			try{ callback.classSpecifierEnd(classSpec, consume( Token.tRBRACE )); } catch( Exception e ) {}
		}
		

	}

	protected void baseSpecifier( Object classSpecOwner ) throws Backtrack {

		Object baseSpecifier = null; 
		
		try { baseSpecifier = callback.baseSpecifierBegin( classSpecOwner ); 	} catch( Exception e )	{}	
		
		baseSpecifierLoop:
		for (;;) {
			switch (LT(1)) {
				case Token.t_virtual:
					consume(Token.t_virtual);
					try{ baseSpecifier = callback.baseSpecifierVirtual( baseSpecifier, true ); } catch( Exception e ){}
					break;
				case Token.t_public:
				case Token.t_protected:
				case Token.t_private:
					try { baseSpecifier = callback.baseSpecifierVisibility( baseSpecifier, consume() );} catch( Exception e ){}
					break;
				case Token.tCOLONCOLON:
				case Token.tIDENTIFIER:
					name();
					try { baseSpecifier = callback.baseSpecifierName( baseSpecifier ); } catch( Exception e ){}
					break;
				case Token.tCOMMA:
					try { 
						callback.baseSpecifierEnd( baseSpecifier ); 
						baseSpecifier = callback.baseSpecifierBegin( classSpecOwner );
					} catch( Exception e ){}
					consume(); 
					continue baseSpecifierLoop;
				default:
					break baseSpecifierLoop;
			}
		}
		try { callback.baseSpecifierEnd( baseSpecifier ); } catch( Exception e ){}
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
				// TODO regarding this null
				try{ expression = callback.expressionBegin( null ); } catch( Exception e ) {}
				constantExpression(expression);
				try{ callback.expressionEnd( expression ); } catch( Exception e ) {}
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
					try{ expression = callback.expressionBegin( null ); } catch( Exception e ) {}
					//TODO get rid of NULL  
					expression(expression);
					try{ callback.expressionEnd( expression );} catch( Exception e ) {}
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
					try{ expression = callback.expressionBegin( null );} catch( Exception e ) {} 
					//TODO get rid of NULL  
					expression(expression);
					try{ callback.expressionEnd( expression );} catch( Exception e ) {}
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
					try{ expression = callback.expressionBegin( null ); } catch( Exception e ) {} 
					//TODO get rid of NULL  
					expression(expression);
					try{ callback.expressionEnd( expression );} catch( Exception e ) {}
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
			try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
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
					try	{ callback.expressionOperator(expression, t); } catch( Exception e )	{}
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
			try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
		}
	}
	
	protected void logicalAndExpression( Object expression ) throws Backtrack {
		inclusiveOrExpression( expression );
		
		while (LT(1) == Token.tAND) {
			Token t = consume();
			inclusiveOrExpression(expression );
			try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
		}
	}
	
	protected void inclusiveOrExpression( Object expression ) throws Backtrack {
		exclusiveOrExpression(expression);
		
		while (LT(1) == Token.tBITOR) {
			Token t = consume();
			exclusiveOrExpression(expression);
			try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
		}
	}
	
	protected void exclusiveOrExpression( Object expression ) throws Backtrack {
		andExpression( expression );
		
		while (LT(1) == Token.tXOR) {
			Token t = consume();
			andExpression(expression);
			try { callback.expressionOperator(expression, t);} catch( Exception e ) {}
			
		}
	}
	
	protected void andExpression( Object expression ) throws Backtrack {
		equalityExpression(expression);
		
		while (LT(1) == Token.tAMPER) {
			Token t = consume();
			equalityExpression(expression);

			try{ callback.expressionOperator(expression, t); }	catch( Exception e ) {}

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
					try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
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
					try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
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
					try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
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
					try	{ callback.expressionOperator(expression, t); }	catch( Exception e ) {}
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
					try{ callback.expressionOperator(expression , t);} catch( Exception e ) {}
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
					try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
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
		if (LT(1) == Token.tLPAREN) {
			Token mark = mark();
			consume();
			
			// If this isn't a type name, then we shouldn't be here
			try {
				if( LT(1) == Token.t_const ) consume(); 
				typeId();
				while( LT(1) == Token.tSTAR ) 
				{
					consume( Token.tSTAR ); 
					if( LT(1) == Token.t_const || LT(1) == Token.t_volatile ) consume();
				}
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
			boolean encountered = false;
			simpleMods:
			for( ; ; )
			{
				switch( LT(1) )
				{
					case Token.t_short:
					case Token.t_unsigned:
					case Token.t_long:
						encountered = true;
						consume(); 
						break;
					case Token.t_int:
					case Token.t_char:
					case Token.t_bool:
					case Token.t_double:
					case Token.t_float:
					case Token.t_wchar_t:
					case Token.t_void: 
						encountered = true;
						consume(); 
					default:
						break simpleMods;
				}
			}
			if( encountered )
				return;
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
				try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
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
					primaryExpression(expression);
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
			case Token.tSTRING:
			case Token.t_false: 
			case Token.t_true:			
				try{ callback.expressionTerminal(expression, consume());} catch( Exception e ) {}
				return;
			
			case Token.tIDENTIFIER:
				try{ callback.expressionTerminal(expression, consume());} catch( Exception e ) {}
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
	private Token currToken, lastToken;
	
	private Token fetchToken() throws EndOfFile {
		try {
			return scanner.nextToken();
		} catch (EndOfFile e) {
			throw e;
		} catch (ScannerException e) {
			e.printStackTrace();
			return fetchToken();  
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

		lastToken = currToken;
		currToken = currToken.getNext();
		return lastToken;
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
		lastToken = null; 
	}

}
