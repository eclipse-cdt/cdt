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

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.internal.core.model.Util;

/**
 * This is our first implementation of the IParser interface, serving as a parser for
 * ANSI C and C++.
 * 
 * From time to time we will make reference to the ANSI ISO specifications.
 * 
 * @author jcamelon
 */
public class Parser implements IParser {

	private static int DEFAULT_OFFSET = -1;			// sentinel initial value for offsets  
	private int firstErrorOffset = DEFAULT_OFFSET;	// offset where the first parse error occurred
	private IParserCallback callback;				// the parser callback that was registered with us
	private boolean quickParse = false;				// are we doing the high-level parse, or an in depth parse? 
	private boolean parsePassed = true;				// did the parse pass?
	private boolean cppNature = true;				// true for C++, false for C
	private ISourceElementRequestor requestor = null; // new callback mechanism 
	private IASTFactory astFactory = null;				// ast factory 
	
	/**
	 * This is the single entry point for setting parsePassed to 
	 * false, and also making note what token offset we failed upon. 
	 * 
	 * @throws EndOfFile
	 */
	protected void failParse() throws EndOfFile
	{
		if( firstErrorOffset == DEFAULT_OFFSET )
			firstErrorOffset = LA(1).offset;
		parsePassed = false;
	}
	
	
	/**
	 * This is the standard cosntructor that we expect the Parser to be instantiated 
	 * with.  
	 * 
	 * @param s				IScanner instance that has been initialized to the code input 
	 * @param c				IParserCallback instance that will receive callbacks as we parse
	 * @param quick			Are we asking for a high level parse or not? 
	 */
	public Parser(IScanner s, IParserCallback c, boolean quick) {
		callback = c;
		scanner = s;
		if( c instanceof ISourceElementRequestor )
			setRequestor( (ISourceElementRequestor)c );
		quickParse = quick;
		astFactory = ParserFactory.createASTFactory( quick );
		scanner.setQuickScan(quick);
		scanner.setCallback(c);
		scanner.setASTFactory( astFactory );
	}

	
	/**
	 * An additional constructor provided for ease of use and tezting.  
	 * 
	 * @param s				IScanner instance that has been initialized to the code input 
	 * @param c				IParserCallback instance that will receive callbacks as we parse
	 */
	public Parser(IScanner s, IParserCallback c) {
		this(s, c, false);
	}

	/**
	 * An additional constructor provided for ease of use and tezting.  
	 * 
	 * @param s				IScanner instance that has been initialized to the code input 
	 */	
	public Parser( IScanner s) {
		this(s, new NullParserCallback(), false);
	}
	
	
	/**
	 * An additional constructor provided for ease of use and tezting.
	 *
	 * * @param code	The code that we wish to parse
	 */
	public Parser(String code) {
		this(new Scanner().initialize( new StringReader( code ), null
));
	}

	/**
	 * An additional constructor provided for ease of use and tezting.
	 * 
	 * @param code		The code that we wish to parse
	 * @param c			IParserCallback instance that will receive callbacks as we parse
	 */
	public Parser(String code, IParserCallback c) {
		this(new Scanner().initialize( new StringReader( code ), null
), c, false);
	}


	/**
	 * An additional constructor provided for ease of use and tezting.
	 * 
	 * @param code			The code that we wish to parse
	 * @param c				IParserCallback instance that will receive callbacks as we parse
	 * @param quickParse	Are we asking for a high level parse or not?
	 */
	public Parser(String code, IParserCallback c, boolean quickParse ) {
		this(new Scanner().initialize( new StringReader( code ), null
), c, quickParse);
	}


	/**
	 * An additional constructor provided for ease of use and tezting.
	 * 
	 * @param stream		An InputStream represnting the code that we wish to parse
	 * @param c				IParserCallback instance that will receive callbacks as we parse
	 * @param quickParse	Are we asking for a high level parse or not?
	 */
	public Parser(InputStream stream, IParserCallback c, boolean quickParse) {
		this(new Scanner().initialize( new InputStreamReader(stream), null ), 
c, quickParse);
	}
	
	private static int parseCount = 0;		// counter that keeps track of the number of times Parser.parse() is called  
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParser#parse()
	 */
	public boolean parse()  {
		long startTime = System.currentTimeMillis();
		translationUnit();
		// For the debuglog to take place, you have to call
		// Util.setDebugging(true);
		// Or set debug to true in the core plugin preference 
		Util.debugLog( "Parse " + (++parseCount) + ": "
			+ ( System.currentTimeMillis() - startTime ) + "ms"
			+ ( parsePassed ? "" : " - parse failure" ));
			
		return parsePassed;
	}
	
	/**
	 * This is the top-level entry point into the ANSI C++ grammar.  
	 * 
	 * translationUnit  : (declaration)*
	 */
	protected void translationUnit()  {
		try { callback.setParser( this ); } catch( Exception e) {}
		Object translationUnit = null;
		try{ translationUnit = callback.translationUnitBegin();} catch( Exception e ) {}		
		
		IASTCompilationUnit compilationUnit = astFactory.createCompilationUnit();
		requestor.enterCompilationUnit( compilationUnit );
		
		Token lastBacktrack = null;
		Token checkToken;
		while (true) {
			try {
				checkToken = LA(1);
				declaration( translationUnit, compilationUnit );
				if( LA(1) == checkToken )
					errorHandling();
			} catch (EndOfFile e) {
				// Good
				break;
			} catch (Backtrack b) {
				
				try {
					// Mark as failure and try to reach a recovery point
					failParse(); 

					if (lastBacktrack != null && lastBacktrack == LA(1)) {
						// we haven't progressed from the last backtrack
						// try and find tne next definition
						errorHandling();
					} else {
						// start again from here
						lastBacktrack = LA(1);
					}
				} catch (EndOfFile e){
					break;
				}
			}
			catch( Exception e )
			{
				// we've done the best we can
			}
		}
		try{ callback.translationUnitEnd(translationUnit);} catch( Exception e ) {}
		requestor.exitCompilationUnit( compilationUnit );
	}




	/**
	 * This function is called whenever we encounter and error that we cannot backtrack out of and we 
	 * still wish to try and continue on with the parse to do a best-effort parse for our client. 
	 * 
	 * @throws EndOfFile  	We can potentially hit EndOfFile here as we are skipping ahead.  
	 */
	protected void errorHandling() throws EndOfFile  {
		failParse();
		consume();
		int depth = 0; 
		while ( ! ( (LT(1) == Token.tSEMI && depth == 0 ) || ( LT(1) == Token.tRBRACE && depth == 1 ) ) ){
			switch( LT(1))
			{
				case Token.tLBRACE:
					++depth;
					break;
				case Token.tRBRACE:
					--depth;
					break;
			}
			
			consume();
		}
		// eat the SEMI/RBRACE as well
		consume();
	}
	
	/**
	 * The merger of using-declaration and using-directive in ANSI C++ grammar.  
	 * 
	 * using-declaration:
	 *	using typename? ::? nested-name-specifier unqualified-id ;
	 *	using :: unqualified-id ;
	 * using-directive:
	 *  using namespace ::? nested-name-specifier? namespace-name ;
	 * 
	 * @param container		Callback object representing the scope these definitions fall into. 
	 * @throws Backtrack	request for a backtrack
	 */
	protected void usingClause( Object container, IASTScope scope ) throws Backtrack
	{
		Token firstToken = consume( Token.t_using );
		
		if( LT(1) == Token.t_namespace )
		{
			Object directive = null; 
			try{ directive = callback.usingDirectiveBegin( container);} catch( Exception e ) {}
			// using-directive
			consume( Token.t_namespace );
			
			// optional :: and nested classes handled in name
			TokenDuple duple = null ;	
			if( LT(1) == Token.tIDENTIFIER || LT(1) == Token.tCOLONCOLON )
			{
				duple = name();
				try{ callback.usingDirectiveNamespaceId( directive );} catch( Exception e ) {}
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
				
				IASTUsingDirective astUD = astFactory.createUsingDirective(scope, duple);
				requestor.acceptUsingDirective( astUD );
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
			
			TokenDuple name = null; 
			if( LT(1) == Token.tIDENTIFIER || LT(1) == Token.tCOLONCOLON )
			{
				//	optional :: and nested classes handled in name
				name = name();
				try{ callback.usingDeclarationMapping( usingDeclaration, typeName ); } catch( Exception e ) {}
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
				
				IASTUsingDeclaration declaration = astFactory.createUsingDeclaration( scope, typeName, name );
				requestor.acceptUsingDeclaration(declaration);
			}
			else
			{
				try{ callback.usingDeclarationAbort( usingDeclaration );} catch( Exception e ) {}
				throw backtrack;
			}			
		}
	}


	/**
	 * Implements Linkage specification in the ANSI C++ grammar. 
	 * 
	 * linkageSpecification
	 * : extern "string literal" declaration
	 * | extern "string literal" { declaration-seq } 
	 * 
	 * @param container Callback object representing the scope these definitions fall into.
	 * @throws Backtrack	request for a backtrack
	 */
	protected void linkageSpecification( Object container, IASTScope scope ) throws Backtrack
	{
		consume( Token.t_extern );

		if( LT(1) != Token.tSTRING )
			throw backtrack;

		Object linkageSpec = null;
		Token spec = consume( Token.tSTRING ); 
		try{ linkageSpec = callback.linkageSpecificationBegin( container, spec.getImage() );} catch( Exception e ) {}
		
		if( LT(1) == Token.tLBRACE )
		{	
			consume(Token.tLBRACE);
		
			IASTLinkageSpecification linkage = astFactory.createLinkageSpecification(scope, spec.getImage());
		
			requestor.enterLinkageSpecification( linkage );
			 
			linkageDeclarationLoop:
			while (LT(1) != Token.tRBRACE) {
				Token checkToken = LA(1);
				switch (LT(1)) {
					case Token.tRBRACE:
						consume(Token.tRBRACE);
						break linkageDeclarationLoop;
					default:
						try
						{
							declaration(linkageSpec, linkage);
						}
						catch( Backtrack bt )
						{
							failParse(); 
							if( checkToken == LA(1))
								errorHandling();
						}
				}
				if (checkToken == LA(1))
					errorHandling();
			}
			// consume the }
			consume();
			try{ callback.linkageSpecificationEnd( linkageSpec );} catch( Exception e ) {}
			requestor.exitLinkageSpecification( linkage );
		}
		else // single declaration
		{
			
			IASTLinkageSpecification linkage = astFactory.createLinkageSpecification( scope, spec.getImage() );
		
			requestor.enterLinkageSpecification( linkage );

			declaration( linkageSpec );
			try{ callback.linkageSpecificationEnd( linkageSpec );} catch( Exception e ) {}
			requestor.exitLinkageSpecification( linkage );
		}
	}

	
	/**
	 * 
	 * Represents the emalgamation of template declarations, template instantiations and 
	 * specializations in the ANSI C++ grammar.  
	 * 
	 * template-declaration:	export? template < template-parameter-list > declaration
	 * explicit-instantiation:	template declaration
	 * explicit-specialization:	template <> declaration
	 *  
	 * @param container			Callback object representing the scope these definitions fall into.
	 * @throws Backtrack		request for a backtrack
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
			throw bt;
		}
	}

	/**
	 * 
	 * 
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
	 * @param templateDeclaration		Callback's templateDeclaration which serves as a scope to this list.  
	 * @throws Backtrack				request for a backtrack
	 */
	protected void templateParameterList( Object templateDeclaration ) throws Backtrack {
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
						try { callback.templateTypeParameterName( currentTemplateParm );} catch( Exception e ) {}
						if( LT(1) == Token.tASSIGN ) // optional = type-id
						{
							consume( Token.tASSIGN );
							typeId(); // type-id
							try{ callback.templateTypeParameterInitialTypeId( currentTemplateParm ); }catch( Exception e ) {}
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
					try{ callback.templateTypeParameterName( newTemplateParm );} catch( Exception e ) {} 
					if( LT(1) == Token.tASSIGN ) // optional = type-id
					{
						consume( Token.tASSIGN );
						typeId(); 
						try{ callback.templateTypeParameterInitialTypeId( newTemplateParm );} catch( Exception e ) {}
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
	
	
	protected void declaration( Object container ) throws Backtrack
	{
		declaration( container, null );
	}
	/**
	 * The most abstract construct within a translationUnit : a declaration.  
	 * 
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
	 * 
	 * @param container		IParserCallback object which serves as the owner scope for this declaration.  
	 * @throws Backtrack	request a backtrack
	 */
	protected void declaration( Object container, IASTScope scope ) throws Backtrack {
		switch (LT(1)) {
			case Token.t_asm:
				Token first = consume( Token.t_asm );
				consume( Token.tLPAREN );
				String assembly = consume( Token.tSTRING ).getImage();
				consume( Token.tRPAREN ); 
				Token last = consume( Token.tSEMI );
				
				IASTASMDefinition asmDefinition =
					astFactory.createASMDefinition(scope, assembly, first.getOffset(), last.getEndOffset());
				
				// if we made it this far, then we have all we need 
				// do the callback
				try{ callback.asmDefinition( container, assembly );} catch( Exception e ) {}
				
				requestor.acceptASMDefinition( asmDefinition );
				return;
			case Token.t_namespace:
				namespaceDefinition( container, scope);
				return; 
			case Token.t_using:
				usingClause( container, scope );
				return; 
			case Token.t_export:
			case Token.t_template:
				templateDeclaration( container );
				return; 
			case Token.t_extern:
				if( LT(2) == Token.tSTRING )
				{
					linkageSpecification( container, scope ); 
					return;
				}
			default:
				Token mark = mark(); 
				try
				{
					simpleDeclaration( container, true ); // try it first with the original strategy 
				}
				catch( Backtrack bt)
				{ 
					// did not work 
					backup( mark );
					simpleDeclaration( container, false ); // try it again with the second strategy
				}
		}
	}
	
	/**
	 *  Serves as the namespace declaration portion of the ANSI C++ grammar.  
	 * 
	 * 	namespace-definition:
	 *		namespace identifier { namespace-body } | namespace { namespace-body }
	 *	 namespace-body:
	 *		declaration-seq?
	 * @param container		IParserCallback object which serves as the owner scope for this declaration.  
	 * @throws Backtrack	request a backtrack

	 */	
	protected void namespaceDefinition( Object container, IASTScope scope ) throws Backtrack
	{
		Object namespace = null;
		Token first = consume( Token.t_namespace );
		try{ namespace = callback.namespaceDefinitionBegin( container, first );} catch( Exception e ) {}

		Token identifier = null; 
		// optional name 		
		if( LT(1) == Token.tIDENTIFIER )
		{
			identifier = identifier();
			try{ callback.namespaceDefinitionId( namespace );} catch( Exception e ) {}
		}
	
		if( LT(1) == Token.tLBRACE )
		{
			consume(); 
			
			IASTNamespaceDefinition namespaceDefinition =
				astFactory.createNamespaceDefinition( 
					scope, 
					( identifier == null ? "" : identifier.getImage() ), 
					first.getOffset(), ( identifier == null ? 0 : identifier.getOffset()) );
			
			requestor.enterNamespaceDefinition( namespaceDefinition );
			
			namepsaceDeclarationLoop:
			while (LT(1) != Token.tRBRACE) {
				Token checkToken = LA(1);
				switch (LT(1)) {
					case Token.tRBRACE:
						//consume(Token.tRBRACE);
						break namepsaceDeclarationLoop;
					default:
						try
						{
							declaration(namespace, namespaceDefinition);
						}
						catch( Backtrack bt )
						{
							failParse();
							if (checkToken == LA(1))
								errorHandling();							
						}
				}
				if (checkToken == LA(1))
					errorHandling();
			}
			// consume the }
			
			Token last = consume( Token.tRBRACE ); 
			try{ callback.namespaceDefinitionEnd( namespace, last);} catch( Exception e ) {}
			
			namespaceDefinition.setEndingOffset( last.getOffset() + last.getLength());
			requestor.exitNamespaceDefinition( namespaceDefinition );
		}
		else
		{
			try{ callback.namespaceDefinitionAbort( namespace );} catch( Exception e ) {}
			throw backtrack;
		}
	}


		
	/**
	 * Serves as the catch-all for all complicated declarations, including function-definitions.  
	 * 
	 * simpleDeclaration
	 * : (declSpecifier)* (initDeclarator ("," initDeclarator)*)? 
	 *     (";" | { functionBody }
	 * 
	 * Notes:
	 * - append functionDefinition stuff to end of this rule
	 * 
	 * To do:
	 * - work in functionTryBlock
	 * 
	 * @param container			IParserCallback object which serves as the owner scope for this declaration.
	 * @param tryConstructor	true == take strategy1 (constructor ) : false == take strategy 2 ( pointer to function) 
	 * @throws Backtrack		request a backtrack
	 */
	protected void simpleDeclaration( Object container, boolean tryConstructor ) throws Backtrack {
		Object simpleDecl = null; 
		try{ simpleDecl = callback.simpleDeclarationBegin( container, LA(1));} catch( Exception e ) {}
		declSpecifierSeq(simpleDecl, false, tryConstructor);
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
				throw backtrack;
		}
		
		try{ callback.simpleDeclarationEnd(simpleDecl, lastToken);} catch( Exception e ) {}
	}

	/**
	 * This method parses a constructor chain 
	 * ctorinitializer:	 : meminitializerlist
	 * meminitializerlist: meminitializer | meminitializer , meminitializerlist
	 * meminitializer: meminitializerid | ( expressionlist? ) 
	 * meminitializerid:	::? nestednamespecifier?
	 * 						classname
	 * 						identifier
	 * @param declarator	IParserCallback object that represents the declarator (constructor) that owns this initializer
	 * @throws Backtrack	request a backtrack
	 */
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
				try{ callback.constructorChainElementId(constructorChainElement);} catch( Exception e) {} 
			
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
	
	
	/**
	 * This routine parses a parameter declaration 
	 * 
	 * @param containerObject	The IParserCallback object representing the parameterDeclarationClause owning the parm. 
	 * @throws Backtrack		request a backtrack
	 */
	protected void parameterDeclaration( Object containerObject ) throws Backtrack
	{
		Token current = LA(1);
		Object parameterDecl = null;
		try{ parameterDecl = callback.parameterDeclarationBegin( containerObject );} catch( Exception e ) {}
		declSpecifierSeq( parameterDecl, true, false );
		
		if (LT(1) != Token.tSEMI)
			try {
				Object declarator = initDeclarator(parameterDecl);
				
				
			} catch (Backtrack b) {
				// allowed to be empty
			}
 		
		if( current == LA(1) )
			throw backtrack; 
		try{ callback.parameterDeclarationEnd( parameterDecl );} catch( Exception e ) {}
		 
	}
	
	/**
	 * This class represents the state and strategy for parsing declarationSpecifierSequences
	 */
	private class Flags
	{
		private boolean encounteredTypename = false;	// have we encountered a typeName yet? 
		private boolean encounteredRawType = false;		// have we encountered a raw type yet? 
		private final boolean parm; 					// is this for a simpleDeclaration or parameterDeclaration?
		private final boolean constructor; 				// are we attempting the constructor strategy? 
		
		public Flags( boolean parm, boolean c)
		{
			this.parm = parm;
			constructor = c; 
		}
		

		/**
		 * @return	true if we have encountered a simple type up to this point, false otherwise
		 */
		public boolean haveEncounteredRawType() {
			return encounteredRawType;
		}

		/**
		 * @return  true if we have encountered a typename up to this point, false otherwise
		 */
		public boolean haveEncounteredTypename() {
			return encounteredTypename;
		}
		


		/**
		 * @param b - set to true if we encounter a raw type (int, short, etc.)
		 */
		public void setEncounteredRawType(boolean b) {
			encounteredRawType = b;
		}

		/**
		 * @param b - set to true if we encounter a typename
		 */
		public void setEncounteredTypename(boolean b) {
			encounteredTypename = b;
		}

		/**
		 * @return true if we are parsing for a ParameterDeclaration
		 */
		public boolean isForParameterDeclaration() {
			return parm;
		}

		/**
		 * @return whether or not we are attempting the constructor strategy or not 
		 */
		public boolean isForConstructor() {
			return constructor;
		}

	}
	
	/**
	 * @param flags            input flags that are used to make our decision 
	 * @return                 whether or not this looks like a constructor (true or false)
	 * @throws EndOfFile       we could encounter EOF while looking ahead
	 */
	private boolean lookAheadForConstructorOrConversion( Flags flags ) throws EndOfFile
	{
		if (flags.isForParameterDeclaration()) return false;
		if (LT(2) == Token.tLPAREN && flags.isForConstructor()) return true;
		
		int posTokenAfterTemplateParameters = 2;
		
		if (LT(posTokenAfterTemplateParameters) == Token.tLT) {
			// a case for template constructor, like CFoobar<A,B>::CFoobar
			
			posTokenAfterTemplateParameters++;
		
			// until we get all the names sorted out
			int depth = 1;
		
			while (depth > 0) {
				switch (LT(posTokenAfterTemplateParameters++)) {
					case Token.tGT :
						--depth;
						break;
					case Token.tLT :
						++depth;
						break;
				}
			}
		}

         // for constructors
		 return
		 (
			 ( 
			  (LT(posTokenAfterTemplateParameters) == Token.tCOLONCOLON)
	         && 
			  (
				LA(posTokenAfterTemplateParameters+1).getImage().equals( LA(1).getImage() ) ||  
				LT(posTokenAfterTemplateParameters+1) == Token.tCOMPL
			  )
		 	 )
		 ||
			 (
	           // for conversion operators   
			  (LT(posTokenAfterTemplateParameters) == Token.tCOLONCOLON)
	         && 
			  (
				LT(posTokenAfterTemplateParameters+1) == Token.t_operator
			  )
			 )
		 );
 	}
	
	/**
	 * @param flags			input flags that are used to make our decision 
	 * @return				whether or not this looks like a a declarator follows
	 * @throws EndOfFile	we could encounter EOF while looking ahead
	 */
	private boolean lookAheadForDeclarator( Flags flags ) throws EndOfFile
	{
		return 
				flags.haveEncounteredTypename() && 
				( 
					(
						LT(2) != Token.tIDENTIFIER || 
				  		( 
					  		LT(3) != Token.tLPAREN && 
					  		LT(3) != Token.tASSIGN 
					  	)
					) && 
				  	!LA(2).isPointer() 
				) 
			;
	}
	
	

	/**
	 * This function parses a declaration specifier sequence, as according to the ANSI C++ spec. 
	 * 
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
	 * 
	 * @param decl				IParserCallback object representing the declaration that owns this specifier sequence
	 * @param parm				Is this for a parameter declaration (true) or simple declaration (false)
	 * @param tryConstructor	true for constructor, false for pointer to function strategy
	 * @throws Backtrack		request a backtrack
	 */
	protected void declSpecifierSeq( Object decl, boolean parm, boolean tryConstructor ) throws Backtrack {
		Flags flags = new Flags( parm, tryConstructor ); 
		declSpecifiers:		
		for (;;) {
			switch (LT(1)) {
				case Token.t_inline:
				case Token.t_auto:
				case Token.t_register:
				case Token.t_static:
				case Token.t_extern:
				case Token.t_mutable:
				case Token.t_virtual:
				case Token.t_explicit:
				case Token.t_typedef:
				case Token.t_friend:
				case Token.t_const:
				case Token.t_volatile:
					try{ callback.simpleDeclSpecifier(decl, consume());} catch( Exception e ) {}
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
					flags.setEncounteredRawType(true);
					try{ callback.simpleDeclSpecifier(decl, consume());} catch( Exception e ) {}
					break;
				case Token.t_typename:
					try{ callback.simpleDeclSpecifier(decl, consume( Token.t_typename ));} catch( Exception e ) {}
					Token first = LA(1);
					Token last = null;  
					name();
					if( LT(1) == Token.t_template )
					{
						consume( Token.t_template );
						last = templateId();
						try
						{
							callback.nameBegin( first );
							callback.nameEnd( last );
						}
						catch( Exception e ) { }
						
						  
					}
					
					try{ callback.simpleDeclSpecifierName( decl );} catch( Exception e ) {}
					return;
				case Token.tCOLONCOLON:
					consume( Token.tCOLONCOLON );
					// handle nested later:
				case Token.tIDENTIFIER:
					// TODO - Kludgy way to handle constructors/destructors
					// handle nested later:
					if ( flags.haveEncounteredRawType() )
						return;
					if( parm && flags.haveEncounteredTypename() )
						return;
					if ( lookAheadForConstructorOrConversion( flags )  )
						return;
					if ( lookAheadForDeclarator( flags ) )
						return;
					try{ callback.simpleDeclSpecifier(decl,LA(1));} catch( Exception e ) {}
					name(); 
					try{ callback.simpleDeclSpecifierName( decl );} catch( Exception e ) {}
					flags.setEncounteredTypename(true);

					break;

				case Token.t_class:
				case Token.t_struct:
				case Token.t_union:
					if( !parm )
					{
						try
						{
							classSpecifier(decl);
							return;
						}
						catch( Backtrack bt )
						{
							elaboratedTypeSpecifier(decl);
							flags.setEncounteredTypename(true);
							break;
						}
					}
					else
					{
						elaboratedTypeSpecifier(decl);
						flags.setEncounteredTypename(true);
						break;
					}
				case Token.t_enum:
					if( !parm )
					{
						try
						{
							enumSpecifier(decl);
							break;
						}
						catch( Backtrack bt )
						{
							// this is an elaborated class specifier
							elaboratedTypeSpecifier(decl);
							flags.setEncounteredTypename(true);
							break;
						}
					}
					else
					{
						elaboratedTypeSpecifier(decl);
						flags.setEncounteredTypename(true);
						break;
					}
				default:
					break declSpecifiers;
			}
		}
	}

	/**
	 * Parse an elaborated type specifier.  
	 * 
	 * @param decl			Declaration which owns the elaborated type 
	 * @throws Backtrack	request a backtrack
	 */
	private void elaboratedTypeSpecifier(Object decl) throws Backtrack {
		// this is an elaborated class specifier
		Object elab = null; 
		try{ elab = callback.elaboratedTypeSpecifierBegin( decl, consume() );} catch( Exception e ) {} 
		name(); 
		try{ 
			callback.elaboratedTypeSpecifierName( elab ); 
			callback.elaboratedTypeSpecifierEnd( elab );
		} catch( Exception e ) {}
	}

	
	/**
	 * Consumes template parameters.  
	 *
	 * @param previousLast	Previous "last" token (returned if nothing was consumed)
	 * @return				Last consumed token, or <code>previousLast</code> if nothing was consumed
	 * @throws Backtrack	request a backtrack
	 */
	private Token consumeTemplateParameters(Token previousLast) throws Backtrack {
		Token last = previousLast;

		if (LT(1) == Token.tLT) {
			last = consume(Token.tLT);

			// until we get all the names sorted out
			int depth = 1;

			while (depth > 0) {
				last = consume();
				switch (last.getType()) {
					case Token.tGT :
						--depth;
						break;
					case Token.tLT :
						++depth;
						break;
				}
			}
		}
		
		return last;
	}

	/**
	 * Parse an identifier.  
	 * 
	 * @throws Backtrack	request a backtrack
	 */
	protected Token identifier() throws Backtrack {
		Token first = consume(Token.tIDENTIFIER); // throws backtrack if its not that
		try
		{ 
			callback.nameBegin(first);
			callback.nameEnd(first);
		} catch( Exception e ) {}
		return first;
	}

	/**
	 * Parses a className.  
	 * 
	 * class-name: identifier | template-id
	 * 
	 * @throws Backtrack
	 */
	protected void className() throws Backtrack
	{	
		if( LT(1)  == Token.tIDENTIFIER)
		{
			if( LT(2) == Token.tLT )
			{
				templateId();
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
	 * Parse a template-id, according to the ANSI C++ spec.  
	 * 
	 * template-id: template-name < template-argument-list opt >
	 * template-name : identifier
	 * 
	 * @return		the last token that we consumed in a successful parse 
	 * 
	 * @throws Backtrack	request a backtrack
	 */
	protected Token templateId() throws Backtrack {
		Token first = consume( Token.tIDENTIFIER );
		Token last = consumeTemplateParameters(first);
		
		callback.nameBegin( first );
		callback.nameEnd( last );
		return last; 
	}
	
	/**
	 * Parse a name.
	 * 
	 * name
	 * : ("::")? name2 ("::" name2)*
	 * 
	 * name2
	 * : IDENTIFER
	 * 
	 * @throws Backtrack	request a backtrack
	 */
	protected TokenDuple name() throws Backtrack {
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
				last = consumeTemplateParameters(last);
				break;
			default:
				backup( mark );
				throw backtrack;
		}

		while (LT(1) == Token.tCOLONCOLON) {
			last = consume();
			
			if (LT(1) == Token.t_template )
				consume(); 
				
			if (LT(1) == Token.tCOMPL)
				consume();
				
			switch (LT(1)) {
				case Token.t_operator:
					backup( mark );
					throw backtrack;
				case Token.tIDENTIFIER:
					last = consume();
					last = consumeTemplateParameters(last);				
			}
		}

		try{ callback.nameEnd(last);} catch( Exception e ) {}
		return new TokenDuple( first, last );

	}


	/**
	 * Parse a const-volatile qualifier.  
	 * 
	 * cvQualifier
	 * : "const" | "volatile"
	 * 
	 * TODO: fix this 
	 * @param ptrOp		Pointer Operator that const-volatile applies to. 		  		
	 * @return			Returns the same object sent in.
	 * @throws Backtrack
	 */
	protected Object cvQualifier( Object ptrOp ) throws Backtrack {
		switch (LT(1)) {
			case Token.t_const:
			case Token.t_volatile:
				try{ callback.pointerOperatorCVModifier( ptrOp, consume() ); } catch( Exception e ) {}
				return ptrOp;
			default:
				throw backtrack;
		}
	}
	
	/**
	 * Parses the initDeclarator construct of the ANSI C++ spec.
	 * 
	 * initDeclarator
	 * : declarator ("=" initializerClause | "(" expressionList ")")?
	 * @param owner			IParserCallback object that represents the owner declaration object.  
	 * @return				declarator that this parsing produced.  
	 * @throws Backtrack	request a backtrack
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
				expression( expression );
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
	 * Parse a declarator, as according to the ANSI C++ specification. 
	 * 
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
	 * 
 	 * @param container		IParserCallback object that represents the owner declaration.  
	 * @return				declarator that this parsing produced.
	 * @throws Backtrack	request a backtrack
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
				Object subDeclarator = declarator(declarator);
				consume(Token.tRPAREN);
				try{ callback.declaratorEnd( subDeclarator );} catch( Exception e ) {}
			}
			else if( LT(1) == Token.t_operator )
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
                    // must be a conversion function
                    typeId(); 
                    toSend = lastToken;
                    try {
                    	// this ptrOp doesn't belong to the declarator, 
                    	// it's just a part of the name
                    	ptrOperator(null); 
                    	toSend = lastToken; 
                   	} catch (Backtrack b) {}
                    
					// In case we'll need better error recovery 
					// while( LT(1) != Token.tLPAREN )	{ toSend = consume(); }
				}
					
				try{ 
					callback.nameBegin( operatorToken );
					callback.nameEnd( toSend );
				} catch( Exception e ) {}

				try{ callback.declaratorId(declarator);} catch( Exception e ) {}				
			}
			else
			{
				try
				{
					name();
					try{ callback.declaratorId(declarator);} catch( Exception e ) {}
				}
				catch( Backtrack bt )
				{
					if( LT(1) == Token.tCOLONCOLON || LT(1) == Token.tIDENTIFIER )
					{
						Token start = consume();
						Token end = null;  
						if (start.type == Token.tIDENTIFIER) end = consumeTemplateParameters(end);
						while( LT(1) == Token.tCOLONCOLON || LT(1) == Token.tIDENTIFIER )
						{
							end = consume(); 
							if (end.type == Token.tIDENTIFIER) end = consumeTemplateParameters(end); 
						}

						if( LT(1) == Token.t_operator )
						{
							consume();
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
                                // must be a conversion function
                                typeId();
								end = lastToken;
								try { 
									// this ptrOp doesn't belong to the declarator, 
									// it's just a part of the name
									ptrOperator(null);
									end = lastToken; 
								} catch (Backtrack b) {}

                                // In case we'll need better error recovery 
                                // while( LT(1) != Token.tLPAREN )  { toSend = consume(); }
                            }
					
							try{ 
								callback.nameBegin( start );
								callback.nameEnd( end );
							} catch( Exception e ) {}

							try{ callback.declaratorId(declarator);} catch( Exception e ) {}
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
								try{ callback.declaratorCVModifier( declarator, consume() );} catch( Exception e ) {}
							}
							
							//check for throws clause here 
							if( LT(1) == Token.t_throw )
							{
								try{ callback.declaratorThrowsException( declarator );} catch( Exception e ) {}
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
											try{ callback.declaratorThrowExceptionName( declarator );} catch( Exception e ) {}
											break;
										case Token.tCOMMA: 
											consume(); 
											break;
										default: 
											System.out.println( "Unexpected Token =" + LA(1).getImage() ); 
											errorHandling(); 
											continue;
									}
								}
							}

							// check for optional pure virtual							
							if( LT(1) == Token.tASSIGN && LT(2) == Token.tINTEGER && LA(2).getImage().equals( "0") )
							{
								consume( Token.tASSIGN);
								consume( Token.tINTEGER);
								try{ callback.declaratorPureVirtual( declarator ); } catch( Exception e ) { }
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
					case Token.tCOLON:
						consume( Token.tCOLON );
						Object bitfield = null; 
						try{ bitfield = callback.startBitfield( declarator );} catch( Exception e ) {}
						Object expression = null; 
						try{ expression = callback.expressionBegin( bitfield );} catch( Exception e ) {} 
						constantExpression(expression);
						try{ callback.expressionEnd( expression ); } catch( Exception e ) {}
						try{ callback.endBitfield( bitfield );} catch( Exception e ) {}
						
					default:
						break;
				}
				break;				
			}
			
			if( LA(1).getType() == Token.tIDENTIFIER )
			{
				try{ callback.declaratorAbort( declarator ); } catch( Exception e ) {}
				declarator = null;
			}
			else
				return declarator; 
				
		} while( true );
	}
	
	/**
	 * Parse a Pointer Operator.   
	 * 
	 * ptrOperator
	 * : "*" (cvQualifier)*
	 * | "&"
	 * | name "*" (cvQualifier)*
	 * 
	 * @param owner 		Declarator that this pointer operator corresponds to.  
	 * @throws Backtrack 	request a backtrack
	 */
	protected void ptrOperator(Object owner) throws Backtrack {
		int t = LT(1);
		Object ptrOp = null;
		try{ ptrOp = callback.pointerOperatorBegin( owner );} catch( Exception e ) {} 
		
		if (t == Token.tAMPER) {
			try{ callback.pointerOperatorType( ptrOp, consume(Token.tAMPER) ); } catch( Exception e ) {}
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
				try{ callback.pointerOperatorName( ptrOp );} catch( Exception e ) {}
				
			try{ callback.pointerOperatorType( ptrOp, consume());} catch( Exception e ) {}

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
	 * Parse an enumeration specifier, as according to the ANSI specs in C & C++.  
	 * 
	 * enumSpecifier:
	 * 		"enum" (name)? "{" (enumerator-list) "}"
	 * enumerator-list:
	 * 	enumerator-definition
	 *	enumerator-list , enumerator-definition
	 * enumerator-definition:
	 * 	enumerator
	 *  enumerator = constant-expression
	 * enumerator: identifier 
	 * 
	 * @param	owner		IParserCallback object that represents the declaration that owns this type specifier. 
	 * @throws	Backtrack	request a backtrack
	 */
	protected void enumSpecifier( Object owner ) throws Backtrack
	{
		Object enumSpecifier = null;
		Token mark = mark(); 
		try{ enumSpecifier = callback.enumSpecifierBegin( owner, consume( Token.t_enum ) );} catch( Exception e ) {}

		if( LT(1) == Token.tIDENTIFIER )
		{ 
			identifier();
			try{ callback.enumSpecifierId( enumSpecifier );} catch( Exception e ) {}
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
					try{ callback.enumeratorId( defn ); } catch( Exception e ) {}
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
			backup(mark);
			throw backtrack; 
		}

	}

	/**
	 * Parse a class/struct/union definition. 
	 * 
	 * classSpecifier
	 * : classKey name (baseClause)? "{" (memberSpecification)* "}"
	 * 
	 * @param	owner		IParserCallback object that represents the declaration that owns this classSpecifier
	 * @throws	Backtrack	request a backtrack
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
			try{ callback.classSpecifierName(classSpec);} catch( Exception e ){}			
		}
		
		if( LT(1) != Token.tCOLON && LT(1) != Token.tLBRACE )
		{
			// this is not a classSpecification
			try{ callback.classSpecifierAbort( classSpec );} catch( Exception e ){}
			classSpec = null; 	
			backup( mark ); 
			throw backtrack; 
		}
		
		// base clause
		if (LT(1) == Token.tCOLON) {
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
						try{ callback.classMemberVisibility( classSpec, consume() );} catch( Exception e ){}
						consume(Token.tCOLON);
						break;
					case Token.tRBRACE:
						consume(Token.tRBRACE);
						break memberDeclarationLoop;
					default:
						try
						{
							declaration(classSpec);
						}
						catch( Backtrack bt )
						{
							failParse();
							if (checkToken == LA(1))
								errorHandling();							
						}
				}
				if (checkToken == LA(1))
					errorHandling();
			}
			// consume the }
			try{ callback.classSpecifierEnd(classSpec, consume( Token.tRBRACE )); } catch( Exception e ) {}
		}
		

	}

	/**
	 * Parse the subclass-baseclauses for a class specification.  
	 * 
	 * baseclause:	: basespecifierlist
	 * basespecifierlist: 	basespecifier
	 * 						basespecifierlist, basespecifier
	 * basespecifier:	::? nestednamespecifier? classname
	 * 					virtual accessspecifier? ::? nestednamespecifier? classname
	 * 					accessspecifier virtual? ::? nestednamespecifier? classname
	 * accessspecifier:	private | protected | public
	 * @param classSpecOwner
	 * @throws Backtrack
	 */
	protected void baseSpecifier( Object classSpecOwner ) throws Backtrack {
		consume( Token.tCOLON );
		Object baseSpecifier = null; 
		
		try { baseSpecifier = callback.baseSpecifierBegin( classSpecOwner ); 	} catch( Exception e )	{}	
		
		baseSpecifierLoop:
		for (;;) {
			switch (LT(1)) {
				case Token.t_virtual:
					consume(Token.t_virtual);
					try{ callback.baseSpecifierVirtual( baseSpecifier, true ); } catch( Exception e ){}
					break;
				case Token.t_public:
				case Token.t_protected:
				case Token.t_private:
					try { callback.baseSpecifierVisibility( baseSpecifier, consume() );} catch( Exception e ){}
					break;
				case Token.tCOLONCOLON:
				case Token.tIDENTIFIER:
					name();
					try { callback.baseSpecifierName( baseSpecifier ); } catch( Exception e ){}
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
	
	/**
	 * Parses a function body. 
	 * 
	 * @throws Backtrack	request a backtrack
	 */
	protected void functionBody() throws Backtrack {
		compoundStatement();
	}
	
	
	/**
	 * Parses a statement. 
	 * 
	 * @throws Backtrack	request a backtrack
	 */
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
	
	/**
	 * @throws Backtrack
	 */
	protected void condition() throws Backtrack {
		// TO DO
	}
	
	/**
	 * @throws Backtrack
	 */
	protected void forInitStatement() throws Backtrack {
		// TO DO
	}
	
	/**
	 * @throws Backtrack
	 */
	protected void compoundStatement() throws Backtrack {
		consume(Token.tLBRACE);
		while (LT(1) != Token.tRBRACE)
			statement();
		consume();
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
	protected void constantExpression( Object expression ) throws Backtrack {
		conditionalExpression( expression );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParser#expression(java.lang.Object)
	 */
	public void expression( Object expression ) throws Backtrack {
		assignmentExpression( expression );
		
		while (LT(1) == Token.tCOMMA) {
			Token t = consume();
			assignmentExpression( expression );
			try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
		}
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
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
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
	protected void throwExpression( Object expression ) throws Backtrack {
		consume(Token.t_throw);
		
		try {
			expression(expression);
		} catch (Backtrack b) {
		}
	}
	
	/**
	 * @param expression
	 * @return
	 * @throws Backtrack
	 */
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
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
	protected void logicalOrExpression( Object expression ) throws Backtrack {
		logicalAndExpression( expression );
		
		while (LT(1) == Token.tOR) {
			Token t = consume();
			logicalAndExpression( expression );
			try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
		}
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
	protected void logicalAndExpression( Object expression ) throws Backtrack {
		inclusiveOrExpression( expression );
		
		while (LT(1) == Token.tAND) {
			Token t = consume();
			inclusiveOrExpression(expression );
			try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
		}
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
	protected void inclusiveOrExpression( Object expression ) throws Backtrack {
		exclusiveOrExpression(expression);
		
		while (LT(1) == Token.tBITOR) {
			Token t = consume();
			exclusiveOrExpression(expression);
			try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
		}
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
	protected void exclusiveOrExpression( Object expression ) throws Backtrack {
		andExpression( expression );
		
		while (LT(1) == Token.tXOR) {
			Token t = consume();
			andExpression(expression);
			try { callback.expressionOperator(expression, t);} catch( Exception e ) {}
			
		}
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
	protected void andExpression( Object expression ) throws Backtrack {
		equalityExpression(expression);
		
		while (LT(1) == Token.tAMPER) {
			Token t = consume();
			equalityExpression(expression);

			try{ callback.expressionOperator(expression, t); }	catch( Exception e ) {}

		}
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
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
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
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
					Token mark = mark(); 
					Token t = consume();
					Token next = LA(1); 
					shiftExpression(expression);
					if( next == LA(1) )
					{
						// we did not consume anything
						// this is most likely an error
						backup( mark ); 
						return;
					}
					else
					{
						try{ callback.expressionOperator(expression, t);} catch( Exception e ) {}
					}
					
					break;
				default:
					return;
			}
		}
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
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
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
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
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
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
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
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
	
	/**
	 * @throws Backtrack
	 */
	protected void typeId() throws Backtrack {
		try {
			name();
			return;
		} catch (Backtrack b) {
			Token begin = LA(1); 
			Token end = null;
			simpleMods:
			for( ; ; )
			{
				switch( LT(1) )
				{
					case Token.t_short:
					case Token.t_unsigned:
					case Token.t_long:
					case Token.t_const:
						end = consume(); 
						break;
					case Token.tAMPER:
					case Token.tSTAR:
					case Token.tIDENTIFIER:
						if( end == null )
							throw backtrack;
						end = consume(); 
						break;						
					case Token.t_int:
					case Token.t_char:
					case Token.t_bool:
					case Token.t_double:
					case Token.t_float:
					case Token.t_wchar_t:
					case Token.t_void: 
						end = consume(); 
					default:
						break simpleMods;
				}
			}
			if( end != null )
			{
				try
				{
					callback.nameBegin( begin );
					callback.nameEnd( end );
				} catch( Exception e ) {}
			}
			else if( LT(1) == Token.t_typename )
			{
				consume( Token.t_typename );
				name(); 
			}
			else
				throw backtrack;
		}
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
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
	
	/**
	 * Pazse a new-expression.  
	 * 
	 * @param expression
	 * @throws Backtrack
	 * 
	 * 
	 * newexpression: 	::? new newplacement? newtypeid newinitializer?
	 *					::? new newplacement? ( typeid ) newinitializer?
	 * newplacement:	( expressionlist )
	 * newtypeid:		typespecifierseq newdeclarator?
	 * newdeclarator:	ptroperator newdeclarator? | directnewdeclarator
	 * directnewdeclarator:		[ expression ]
	 *							directnewdeclarator [ constantexpression ]
	 * newinitializer:	( expressionlist? )
	 */
	protected void newExpression( Object expression ) throws Backtrack {
		if (LT(1) == Token.tCOLONCOLON) {
			// global scope
			consume();
		}
		
		consume (Token.t_new);
		
		boolean typeIdInParen = false;
        boolean placementParseFailure = true;
        Token beforeSecondParen = null;
        Token backtrackMarker = null;
        
		if( LT(1) == Token.tLPAREN )
		{
			consume( Token.tLPAREN );
            
            try {
                // Try to consume placement list
                // Note: since expressionList and expression are the same...
                backtrackMarker = mark();
                expression(expression);
                consume( Token.tRPAREN );
                
                placementParseFailure = false;
                
                if( LT(1) == Token.tLPAREN ) {
                    beforeSecondParen = mark();
                    consume( Token.tLPAREN );
                    typeIdInParen = true;
                }
            } catch (Backtrack e) {
                backup(backtrackMarker);
            }
            
            if (placementParseFailure) {
                // CASE: new (typeid-not-looking-as-placement) ...
                // the first expression in () is not a placement
                // - then it has to be typeId
                typeId();
                consume(Token.tRPAREN);
            } else {
                if (!typeIdInParen) {
                    if (LT(1) == Token.tLBRACKET) {
                        // CASE: new (typeid-looking-as-placement) [expr]...
                        // the first expression in () has been parsed as a placement;
                        // however, we assume that it was in fact typeId, and this 
                        // new statement creates an array.
                        // Do nothing, fallback to array/initializer processing
                    } else {
                        // CASE: new (placement) typeid ...
                        // the first expression in () is parsed as a placement,
                        // and the next expression doesn't start with '(' or '['
                        // - then it has to be typeId
                        try { backtrackMarker = mark(); typeId(); } catch (Backtrack e) {
                            // Hmmm, so it wasn't typeId after all... Then it is
                            // CASE: new (typeid-looking-as-placement)
                            backup(backtrackMarker);
                            return;
                        }
                    }
                } else {
                    // Tricky cases: first expression in () is parsed as a placement,
                    // and the next expression starts with '('.
                    // The problem is, the first expression might as well be a typeid
                    try { 
                        typeId();
                        consume(Token.tRPAREN);
                         
                        if (LT(1) == Token.tLPAREN || LT(1) == Token.tLBRACKET) {
                            // CASE: new (placement)(typeid)(initializer)
                            // CASE: new (placement)(typeid)[] ...
                            // Great, so far all our assumptions have been correct
                            // Do nothing, fallback to array/initializer processing
                        } else {
                            // CASE: new (placement)(typeid)
                            // CASE: new (typeid-looking-as-placement)(initializer-looking-as-typeid)
                            // Worst-case scenario - this cannot be resolved w/o more semantic information.
                            // Luckily, we don't need to know what was that - we only know that 
                            // new-expression ends here.
                            return;
                        }
                    } catch (Backtrack e) {
                        // CASE: new (typeid-looking-as-placement)(initializer-not-looking-as-typeid)
                        // Fallback to initializer processing
                        backup(beforeSecondParen);                        
                    }
                }
            }
		} else {
            // CASE: new typeid ...
            // new parameters do not start with '('
            // i.e it has to be a plain typeId
            typeId();
		}
               
        while (LT(1) == Token.tLBRACKET) {
            // array new
            consume();
            assignmentExpression(expression);
            consume(Token.tRBRACKET);
        }
        		
		// newinitializer
		if( LT(1) == Token.tLPAREN ) 
		{
			consume( Token.tLPAREN ); 
			if( LT(1) != Token.tRPAREN )
				expression( expression );
			consume( Token.tRPAREN );
              
		}
	}
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
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
				consume(Token.t_sizeof);
				Token mark = LA(1);
				if (LT(1) == Token.tLPAREN) {
					try
					{
						consume( Token.tLPAREN );	
						typeId();
						consume(Token.tRPAREN);
					}
					catch( Backtrack bt )
					{
						backup( mark );
						unaryExpression( expression );
					}
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

	/**
	 * @param expression
	 * @throws Backtrack
	 */
	protected void postfixExpression( Object expression) throws Backtrack {
		switch (LT(1)) {
			case Token.t_typename:
				consume();
				// TO DO: this
				break;
			// simple-type-specifier ( assignment-expression , .. )
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
				consume(); 
				consume( Token.tLPAREN );
				while( true )
				{
					assignmentExpression( expression );
					if( LT(1) == Token.tRPAREN ) break;
					consume( Token.tCOMMA );
				}
				consume( Token.tRPAREN );
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
	
	/**
	 * @param expression
	 * @throws Backtrack
	 */
	protected void primaryExpression( Object expression ) throws Backtrack {
		int type = LT(1);
		switch (type) {
			// TO DO: we need more literals...
			case Token.tINTEGER:
			case Token.tFLOATINGPT:
			case Token.tSTRING:
			case Token.tLSTRING:
			case Token.t_false: 
			case Token.t_true:	
			case Token.tCHAR:		
				try{ callback.expressionTerminal(expression, consume());} catch( Exception e ) {}
				return;
			
			case Token.tIDENTIFIER:
				name(); 
				try{ callback.expressionName(expression);} catch( Exception e ) {}
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
	
	/**
	 * @throws Exception
	 */
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
	
	/**
	 * Class that represents the a request to backtrack. 
	 */
	public static class Backtrack extends Exception {
	}
	
	// the static instance we always use
	private static Backtrack backtrack = new Backtrack();
	
	/**
	 * Class that represents encountering EOF.  
	 *
	 * End of file generally causes backtracking 
	 */ 
	public static class EndOfFile extends Backtrack {
	}
	
	// the static instance we always use
	public static EndOfFile endOfFile = new EndOfFile();
	
	// Token management
	private IScanner scanner;
	private Token 	currToken,		// current token we plan to consume next 
					lastToken;		// last token we consumed
	
	/**
	 * Fetches a token from the scanner. 
	 * 
	 * @return				the next token from the scanner
	 * @throws EndOfFile	thrown when the scanner.nextToken() yields no tokens
	 */
	private Token fetchToken() throws EndOfFile {
		try {
			return scanner.nextToken();
		} catch (EndOfFile e) {
			throw e;
		} catch (ScannerException e) {
//			e.printStackTrace();
			return fetchToken();  
		}
	}

	/**
	 * Look Ahead in the token list to see what is coming.  
	 * 
	 * @param i		How far ahead do you wish to peek?
	 * @return		the token you wish to observe
	 * @throws EndOfFile	if looking ahead encounters EOF, throw EndOfFile 
	 */
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

	/**
	 * Look ahead in the token list and return the token type.  
	 * 
	 * @param i				How far ahead do you wish to peek?
	 * @return				The type of that token
	 * @throws EndOfFile	if looking ahead encounters EOF, throw EndOfFile
	 */
	protected int LT(int i) throws EndOfFile {
		return LA(i).type;
	}
	
	/**
	 * Consume the next token available, regardless of the type.  
	 * 
	 * @return				The token that was consumed and removed from our buffer.  
	 * @throws EndOfFile	If there is no token to consume.  
	 */
	protected Token consume() throws EndOfFile {
		if (currToken == null)
			currToken = fetchToken();

		if( currToken != null )
			lastToken = currToken;
			
		currToken = currToken.getNext();
		return lastToken;
	}
	
	/**
	 * Consume the next token available only if the type is as specified.  
	 * 
	 * @param type			The type of token that you are expecting.  	
	 * @return				the token that was consumed and removed from our buffer. 
	 * @throws Backtrack	If LT(1) != type 
	 */
	protected Token consume(int type) throws Backtrack {
		if (LT(1) == type)
			return consume();
		else
			throw backtrack;
	}
	
	/**
	 * Mark our place in the buffer so that we could return to it should we have to.  
	 * 
	 * @return				The current token. 
	 * @throws EndOfFile	If there are no more tokens.
	 */
	protected Token mark() throws EndOfFile {
		if (currToken == null)
			currToken = fetchToken();
		return currToken;
	}
	
	/**
	 * Rollback to a previous point, reseting the queue of tokens.  
	 * 
	 * @param mark		The point that we wish to restore to.  
	 *  
	 */
	protected void backup(Token mark) {
		currToken = mark;
		lastToken = null; 	// this is not entirely right ... 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParser#isCppNature()
	 */
	public boolean isCppNature() {
		return cppNature;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParser#setCppNature(boolean)
	 */
	public void setCppNature(boolean b) {
		cppNature = b;
		if( scanner != null )
			scanner.setCppNature( b ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParser#getLineNumberForOffset(int)
	 */
	public int getLineNumberForOffset(int offset) throws NoSuchMethodException
	{
		return scanner.getLineNumberForOffset(offset);
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParser#mapLineNumbers(boolean)
	 */
	public void mapLineNumbers(boolean value) {
		scanner.mapLineNumbers( value );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParser#getLastErrorOffset()
	 */
	public int getLastErrorOffset() {
		return firstErrorOffset;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#setRequestor(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void setRequestor(ISourceElementRequestor r) {
		requestor = r;
		if( scanner != null )
			scanner.setRequestor(r);
	}
}
