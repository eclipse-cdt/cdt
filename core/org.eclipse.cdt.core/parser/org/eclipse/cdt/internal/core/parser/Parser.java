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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableElement;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplate;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.Token;
import org.eclipse.cdt.internal.core.parser.token.TokenDuple;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets.Key;

/**
 * This is our first implementation of the IParser interface, serving as a parser for
 * ANSI C and C++.
 * 
 * From time to time we will make reference to the ANSI ISO specifications.
 * 
 * @author jcamelon
 */
public abstract class Parser implements IParser
{
    protected final IParserLogService log;
	private static final List EMPTY_LIST = new ArrayList();
    private static int FIRST_ERROR_OFFSET_UNSET = -1;
    // sentinel initial value for offsets 
    protected int firstErrorOffset = FIRST_ERROR_OFFSET_UNSET;
    // offset where the first parse error occurred
   
    // are we doing the high-level parse, or an in depth parse?
    protected boolean parsePassed = true; // did the parse pass?
    protected ParserLanguage language = ParserLanguage.CPP; // C or CPP
    protected ISourceElementRequestor requestor = null;
    // new callback mechanism
    protected IASTFactory astFactory = null; // ast factory
    /**
     * This is the single entry point for setting parsePassed to 
     * false, and also making note what token offset we failed upon. 
     * 
     * @throws EndOfFileException
     */
    protected void failParse()
    {
    	try
    	{
	        if (firstErrorOffset == FIRST_ERROR_OFFSET_UNSET )
	            firstErrorOffset = LA(1).getOffset();
    	} catch( EndOfFileException eof )
    	{
    		// do nothing
    	}
    	finally
    	{
	        parsePassed = false;
    	}
    }
    /**
     * This is the standard cosntructor that we expect the Parser to be instantiated 
     * with.  
     * 
     * @param s				IScanner instance that has been initialized to the code input 
     * @param c				IParserCallback instance that will receive callbacks as we parse
     * @param quick			Are we asking for a high level parse or not? 
     */
    public Parser(
        IScanner scanner,
        ISourceElementRequestor callback,
        ParserLanguage language,
        IParserLogService log )
    {
        this.scanner = scanner;
        this.requestor = callback;
        this.language = language;
        this.log = log;

    }
    
    // counter that keeps track of the number of times Parser.parse() is called
    private static int parseCount = 0;
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IParser#parse()
     */
    public boolean parse()
    {
        long startTime = System.currentTimeMillis();
        translationUnit();
        // For the debuglog to take place, you have to call
        // Util.setDebugging(true);
        // Or set debug to true in the core plugin preference 
        log.traceLog(
            "Parse "
                + (++parseCount)
                + ": "
                + (System.currentTimeMillis() - startTime)
                + "ms"
                + (parsePassed ? "" : " - parse failure") );
        return parsePassed;
    }
        
    
    /**
     * This is the top-level entry point into the ANSI C++ grammar.  
     * 
     * translationUnit  : (declaration)*
     */
    protected void translationUnit()
    {
        try
        {
            compilationUnit = astFactory.createCompilationUnit();
        }
        catch (Exception e2)
        {
            return;
        }

		compilationUnit.enterScope( requestor );     
        IToken lastBacktrack = null;
        IToken checkToken = null;
        while (true)
        {
            try
            {
                checkToken = LA(1);
                declaration(compilationUnit, null, null);
                if (LA(1) == checkToken)
                    errorHandling();
            }
            catch (EndOfFileException e)
            {
                // Good
                break;
            }
            catch (BacktrackException b)
            {
                try
                {
                    // Mark as failure and try to reach a recovery point
                    failParse();
                    if (lastBacktrack != null && lastBacktrack == LA(1))
                    {
                        // we haven't progressed from the last backtrack
                        // try and find tne next definition
                        errorHandling();
                    }
                    else
                    {
                        // start again from here
                        lastBacktrack = LA(1);
                    }
                }
                catch (EndOfFileException e)
                {
                    break;
                }
            }
            catch (Exception e)
            {
				failParse();
                break;
            }
        }
        compilationUnit.exitScope( requestor );
    }
    /**
     * This function is called whenever we encounter and error that we cannot backtrack out of and we 
     * still wish to try and continue on with the parse to do a best-effort parse for our client. 
     * 
     * @throws EndOfFileException  	We can potentially hit EndOfFile here as we are skipping ahead.  
     */
    protected void errorHandling() throws EndOfFileException
    {
        failParse();
        consume();
        int depth = 0;
        while (!((LT(1) == IToken.tSEMI && depth == 0)
            || (LT(1) == IToken.tRBRACE && depth == 1)))
        {
            switch (LT(1))
            {
                case IToken.tLBRACE :
                    ++depth;
                    break;
                case IToken.tRBRACE :
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
     * @throws BacktrackException	request for a backtrack
     */
    protected void usingClause(IASTScope scope)
        throws EndOfFileException, BacktrackException
    {
        IToken firstToken = consume(IToken.t_using);
        setCompletionValues(scope, CompletionKind.TYPE_REFERENCE, Key.POST_USING );
        
        if (LT(1) == IToken.t_namespace)
        {
            // using-directive
            consume(IToken.t_namespace);
            
            setCompletionValues(scope, CompletionKind.NAMESPACE_REFERENCE, Key.EMPTY );
            // optional :: and nested classes handled in name
            TokenDuple duple = null;
            if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON)
                duple = name(scope);
            else
                throw backtrack;
            if (LT(1) == IToken.tSEMI)
            {
                IToken last = consume(IToken.tSEMI);
                IASTUsingDirective astUD = null; 
                
                try
                {
                    astUD = astFactory.createUsingDirective(scope, duple, firstToken.getOffset(), firstToken.getLineNumber(), last.getEndOffset(), last.getLineNumber());
                }
                catch (Exception e1)
                {
                    throw backtrack;
                }
                astUD.acceptElement(requestor);
                return;
            }
            else
            {
                throw backtrack;
            }
        }
        else
        {
            boolean typeName = false;
            setCompletionValues(scope, CompletionKind.TYPE_REFERENCE, Key.POST_USING );
            
            if (LT(1) == IToken.t_typename)
            {
                typeName = true;
                consume(IToken.t_typename);
                
            }

            setCompletionValues(scope, CompletionKind.TYPE_REFERENCE, Key.NAMESPACE_ONLY );
            TokenDuple name = null;
            if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON)
            {
                //	optional :: and nested classes handled in name
                name = name(scope);
            }
            else
            {
                throw backtrack;
            }
            if (LT(1) == IToken.tSEMI)
            {
                IToken last = consume(IToken.tSEMI);
                IASTUsingDeclaration declaration = null;
                try
                {
                    declaration =
                        astFactory.createUsingDeclaration(
                            scope,
                            typeName,
                            name,
                            firstToken.getOffset(),
                            firstToken.getLineNumber(), last.getEndOffset(), last.getLineNumber());
                }
                catch (Exception e1)
                {
                    throw backtrack;
                }
                declaration.acceptElement( requestor );
                setCompletionValues(scope, getCompletionKindForDeclaration(scope, null), Key.DECLARATION );
            }
            else
            {
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
     * @throws BacktrackException	request for a backtrack
     */
    protected void linkageSpecification(IASTScope scope)
        throws EndOfFileException, BacktrackException
    {
        IToken firstToken = consume(IToken.t_extern);
        if (LT(1) != IToken.tSTRING)
            throw backtrack;
        IToken spec = consume(IToken.tSTRING);
  
        if (LT(1) == IToken.tLBRACE)
        {
            consume(IToken.tLBRACE);
            IASTLinkageSpecification linkage;
            try
            {
                linkage =
                    astFactory.createLinkageSpecification(
                        scope,
                        spec.getImage(),
                        firstToken.getOffset(), firstToken.getLineNumber());
            }
            catch (Exception e)
            {
                throw backtrack;
            }
            
            linkage.enterScope( requestor );    
            linkageDeclarationLoop : while (LT(1) != IToken.tRBRACE)
            {
                IToken checkToken = LA(1);
                switch (LT(1))
                {
                    case IToken.tRBRACE :
                        consume(IToken.tRBRACE);
                        break linkageDeclarationLoop;
                    default :
                        try
                        {
                            declaration(linkage, null, null);
                        }
                        catch (BacktrackException bt)
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
            IToken lastToken = consume();
            linkage.setEndingOffsetAndLineNumber(lastToken.getEndOffset(), lastToken.getLineNumber());
            linkage.exitScope( requestor );
        }
        else // single declaration
            {
            IASTLinkageSpecification linkage;
            try
            {
                linkage =
                    astFactory.createLinkageSpecification(
                        scope,
                        spec.getImage(),
                        firstToken.getOffset(), firstToken.getLineNumber());
            }
            catch (Exception e)
            {
                throw backtrack;
            }
			linkage.enterScope( requestor );
            declaration(linkage, null, null);
			linkage.exitScope( requestor );
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
     * @throws BacktrackException		request for a backtrack
     */
    protected void templateDeclaration(IASTScope scope)
        throws EndOfFileException, BacktrackException
    {
        IToken firstToken = null;
        boolean exported = false; 
        if (LT(1) == IToken.t_export)
        {
        	exported = true;
            firstToken = consume(IToken.t_export);
            consume(IToken.t_template);
        }
        else
            firstToken = consume(IToken.t_template);
        if (LT(1) != IToken.tLT)
        {
            // explicit-instantiation
            IASTTemplateInstantiation templateInstantiation;
            try
            {
                templateInstantiation =
                    astFactory.createTemplateInstantiation(
                        scope,
                        firstToken.getOffset(), firstToken.getLineNumber());
            }
            catch (Exception e)
            {
                throw backtrack;
            }
            templateInstantiation.enterScope( requestor );
            declaration(scope, templateInstantiation, null);
            templateInstantiation.setEndingOffsetAndLineNumber(lastToken.getEndOffset(), lastToken.getLineNumber());
			templateInstantiation.exitScope( requestor );
 
            return;
        }
        else
        {
            consume(IToken.tLT);
            if (LT(1) == IToken.tGT)
            {
                consume(IToken.tGT);
                // explicit-specialization
                
                IASTTemplateSpecialization templateSpecialization;
                try
                {
                    templateSpecialization =
                        astFactory.createTemplateSpecialization(
                            scope,
                            firstToken.getOffset(), firstToken.getLineNumber());
                }
                catch (Exception e)
                {
                    throw backtrack;
                }
				templateSpecialization.enterScope(requestor);
                declaration(scope, templateSpecialization, null);
                templateSpecialization.setEndingOffsetAndLineNumber(
                    lastToken.getEndOffset(), lastToken.getLineNumber());
                templateSpecialization.exitScope(requestor);
                return;
            }
        }
        
        try
        {
            List parms = templateParameterList(scope);
            consume(IToken.tGT);
            IASTTemplateDeclaration templateDecl;
            try
            {
                templateDecl =
                    astFactory.createTemplateDeclaration(
                        scope,
                        parms,
                        exported,
                        firstToken.getOffset(), firstToken.getLineNumber());
            }
            catch (Exception e)
            {
                throw backtrack;
            }
            templateDecl.enterScope( requestor );
            declaration(scope, templateDecl, null );
			templateDecl.setEndingOffsetAndLineNumber(
				lastToken.getEndOffset(), lastToken.getLineNumber() );
			templateDecl.exitScope( requestor );
            
        }
        catch (BacktrackException bt)
        {
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
     * @throws BacktrackException				request for a backtrack
     */
    protected List templateParameterList(IASTScope scope)
        throws BacktrackException, EndOfFileException
    {
        // if we have gotten this far then we have a true template-declaration
        // iterate through the template parameter list
        List returnValue = new ArrayList();
 
        for (;;)
        {
            if (LT(1) == IToken.tGT)
                return returnValue;
            if (LT(1) == IToken.t_class || LT(1) == IToken.t_typename)
            {
                IASTTemplateParameter.ParamKind kind =
                    (consume().getType() == IToken.t_class)
                        ? IASTTemplateParameter.ParamKind.CLASS
                        : IASTTemplateParameter.ParamKind.TYPENAME;
				
				IToken id = null;
				IASTTypeId typeId = null;
                try
                {
                    if (LT(1) == IToken.tIDENTIFIER) // optional identifier
                    {
                        id = identifier();
                        
                        if (LT(1) == IToken.tASSIGN) // optional = type-id
                        {
                            consume(IToken.tASSIGN);
                            typeId = typeId(scope, false); // type-id
                        }
                    }

                }
                catch (BacktrackException bt)
                {
                    throw bt;
                }
				try
                {
                    returnValue.add(
                    	astFactory.createTemplateParameter(
                    		kind,
                    		( id == null )? "" : id.getImage(),
                    		(typeId == null) ? null : typeId.getTypeOrClassName(),
                    		null,
                    		null));
                }
                catch (Exception e)
                {
                    throw backtrack;
                }

            }
            else if (LT(1) == IToken.t_template)
            {
                consume(IToken.t_template);
                consume(IToken.tLT);

                List subResult = templateParameterList(scope);
                consume(IToken.tGT);
                consume(IToken.t_class);
                IToken optionalId = null;
                IASTTypeId optionalTypeId = null;
                if (LT(1) == IToken.tIDENTIFIER) // optional identifier
                {
                    optionalId = identifier();
   
                    if (LT(1) == IToken.tASSIGN) // optional = type-id
                    {
                        consume(IToken.tASSIGN);
                        optionalTypeId = typeId(scope, false);
    
                    }
                }
 
                try
                {
                    returnValue.add(
                        astFactory.createTemplateParameter(
                            IASTTemplateParameter.ParamKind.TEMPLATE_LIST,
                            ( optionalId == null )? "" : optionalId.getImage(),
                            ( optionalTypeId == null )  ? "" : optionalTypeId.toString(),
                            null,
                            subResult));
                }
                catch (Exception e)
                {
                    throw backtrack;
                }
            }
            else if (LT(1) == IToken.tCOMMA)
            {
                consume(IToken.tCOMMA);
                continue;
            }
            else
            {
                ParameterCollection c = new ParameterCollection();
                parameterDeclaration(c, scope);
                DeclarationWrapper wrapper =
                    (DeclarationWrapper)c.getParameters().get(0);
                Declarator declarator =
                    (Declarator)wrapper.getDeclarators().next();
                try
                {
                    returnValue.add(
                        astFactory.createTemplateParameter(
                            IASTTemplateParameter.ParamKind.PARAMETER,
                            null,
                            null,
                            astFactory.createParameterDeclaration(
                                wrapper.isConst(),
                                wrapper.isVolatile(),
                                wrapper.getTypeSpecifier(),
                                declarator.getPointerOperators(),
                                declarator.getArrayModifiers(),
                                null, null, declarator.getName() == null
                                                ? ""
                                                : declarator.getName(), declarator.getInitializerClause(), wrapper.getStartingOffset(), wrapper.getStartingLine(), declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), wrapper.getEndOffset(), wrapper.getEndLine()),
                            null));
                }
                catch (Exception e)
                {
                    throw backtrack;
                }
            }
        }
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
     * @throws BacktrackException	request a backtrack
     */
    protected void declaration(
        IASTScope scope,
        IASTTemplate ownerTemplate, CompletionKind overideKind)
        throws EndOfFileException, BacktrackException
    {
    	IASTCompletionNode.CompletionKind kind = getCompletionKindForDeclaration(scope, overideKind);
    	setCompletionValues(scope, kind, Key.DECLARATION );

    	switch (LT(1))
        {
            case IToken.t_asm :
                IToken first = consume(IToken.t_asm);
                setCompletionValues( scope, CompletionKind.NO_SUCH_KIND, Key.EMPTY );
                consume(IToken.tLPAREN);
                String assembly = consume(IToken.tSTRING).getImage();
                consume(IToken.tRPAREN);
                IToken last = consume(IToken.tSEMI);
                
                IASTASMDefinition asmDefinition;
                try
                {
                    asmDefinition =
                        astFactory.createASMDefinition(
                            scope,
                            assembly,
                            first.getOffset(),
                            first.getLineNumber(), last.getEndOffset(), last.getLineNumber());
                }
                catch (Exception e)
                {
                    throw backtrack;
                }
                // if we made it this far, then we have all we need 
                // do the callback
 				asmDefinition.acceptElement(requestor);
 				setCompletionValues(scope, kind, Key.DECLARATION );
                return;
            case IToken.t_namespace :
                namespaceDefinition(scope);
                return;
            case IToken.t_using :
                usingClause(scope);
                return;
            case IToken.t_export :
            case IToken.t_template :
                templateDeclaration(scope);
                return;
            case IToken.t_extern :
                if (LT(2) == IToken.tSTRING)
                {
                    linkageSpecification(scope);
                    return;
                }
            default :
                simpleDeclarationStrategyUnion(scope, ownerTemplate, overideKind);
        }
    	setCompletionValues(scope, kind, Key.DECLARATION );        
    }
    
    /**
	 * @param scope
	 * @return
	 */
	protected IASTCompletionNode.CompletionKind getCompletionKindForDeclaration(IASTScope scope, CompletionKind overide) {
		return null;
	}
	
	protected void simpleDeclarationStrategyUnion(
        IASTScope scope,
        IASTTemplate ownerTemplate, CompletionKind overide)
        throws EndOfFileException, BacktrackException
    {
        IToken mark = mark();
        
        try
        {
            simpleDeclaration(
                SimpleDeclarationStrategy.TRY_CONSTRUCTOR,
                scope,
                ownerTemplate, overide, false);
            // try it first with the original strategy
        }
        catch (BacktrackException bt)
        {
            // did not work 
            backup(mark);
            
            try
            {  
            	simpleDeclaration(
                	SimpleDeclarationStrategy.TRY_FUNCTION,
	                scope,
    	            ownerTemplate, overide, false);
            }
            catch( BacktrackException bt2 )
            {
            	backup( mark ); 

				try
				{
					simpleDeclaration(
						SimpleDeclarationStrategy.TRY_VARIABLE,
						scope,
						ownerTemplate, overide, false);
				}
				catch( BacktrackException b3 )
				{
					backup( mark );
					throw b3;
				}
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
     * @throws BacktrackException	request a backtrack
    
     */
    protected void namespaceDefinition(IASTScope scope)
        throws BacktrackException, EndOfFileException
    {
        IToken first = consume(IToken.t_namespace);
 
        setCompletionValues(scope,CompletionKind.USER_SPECIFIED_NAME, Key.EMPTY );
        IToken identifier = null;
        // optional name 		
        if (LT(1) == IToken.tIDENTIFIER)
            identifier = identifier();
        
        if (LT(1) == IToken.tLBRACE)
        {
            consume();
            IASTNamespaceDefinition namespaceDefinition = null;
            try
            {
                namespaceDefinition = 
                    astFactory.createNamespaceDefinition(
                        scope,
                        (identifier == null ? "" : identifier.getImage()),
                        first.getOffset(),
                        first.getLineNumber(), 
                        (identifier == null ? first.getOffset() : identifier.getOffset()), 
						(identifier == null ? first.getEndOffset() : identifier.getEndOffset() ),  
						(identifier == null ? first.getLineNumber() : identifier.getLineNumber() ));
            }
            catch (Exception e1)
            {
                throw backtrack;
            }
            namespaceDefinition.enterScope( requestor );
            setCompletionValues(scope,CompletionKind.VARIABLE_TYPE, Key.DECLARATION );
            namespaceDeclarationLoop : while (LT(1) != IToken.tRBRACE)
            {
                IToken checkToken = LA(1);
                switch (LT(1))
                {
                    case IToken.tRBRACE :
                        //consume(Token.tRBRACE);
                        break namespaceDeclarationLoop;
                    default :
                        try
                        {
                            declaration(namespaceDefinition, null, null);
                        }
                        catch (BacktrackException bt)
                        {
                            failParse();
                            if (checkToken == LA(1))
                                errorHandling();
                        }
                }
                if (checkToken == LA(1))
                    errorHandling();
            }
            setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,Key.EMPTY);
            // consume the }
            IToken last = consume(IToken.tRBRACE);
 
            namespaceDefinition.setEndingOffsetAndLineNumber(
                last.getOffset() + last.getLength(), last.getLineNumber());
            namespaceDefinition.exitScope( requestor );
        }
        else if( LT(1) == IToken.tASSIGN )
        {
        	setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,Key.EMPTY);
        	consume( IToken.tASSIGN );
        	
			if( identifier == null )
				throw backtrack;

        	ITokenDuple duple = name(scope);
        	consume( IToken.tSEMI );
        	try
            {
                astFactory.createNamespaceAlias( 
                	scope, identifier.getImage(), duple, first.getOffset(), 
                	first.getLineNumber(), identifier.getOffset(), identifier.getEndOffset(), identifier.getLineNumber(), duple.getLastToken().getEndOffset(), duple.getLastToken().getLineNumber() );
            }
            catch (Exception e1)
            {
                throw backtrack;
            }
        }
        else
        {
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
     * @throws BacktrackException		request a backtrack
     */
    protected void simpleDeclaration(
        SimpleDeclarationStrategy strategy,
        IASTScope scope,
        IASTTemplate ownerTemplate, CompletionKind overideKind, boolean fromCatchHandler)
        throws BacktrackException, EndOfFileException
    {
    	IToken firstToken = LA(1);
        DeclarationWrapper sdw =
            new DeclarationWrapper(scope, firstToken.getOffset(), firstToken.getLineNumber(), ownerTemplate);

        setCompletionValues( scope, getCompletionKindForDeclaration(scope, overideKind), Key.DECL_SPECIFIER_SEQUENCE );
        declSpecifierSeq(sdw, false, strategy == SimpleDeclarationStrategy.TRY_CONSTRUCTOR );
        if (sdw.getTypeSpecifier() == null && sdw.getSimpleType() != IASTSimpleTypeSpecifier.Type.UNSPECIFIED )
            try
            {
                sdw.setTypeSpecifier(
                    astFactory.createSimpleTypeSpecifier(
                        scope,
                        sdw.getSimpleType(),
                        sdw.getName(),
                        sdw.isShort(),
                        sdw.isLong(),
                        sdw.isSigned(),
                        sdw.isUnsigned(), sdw.isTypeNamed(), sdw.isComplex(), sdw.isImaginary()));
            }
            catch (Exception e1)
            {
                throw backtrack;
            }
        
        Declarator declarator = null;
        if (LT(1) != IToken.tSEMI)
        {
            declarator = initDeclarator(sdw, strategy);
                
            while (LT(1) == IToken.tCOMMA)
            {
                consume();
                initDeclarator(sdw, strategy);
            }
        }

        boolean hasFunctionBody = false;
        boolean hasFunctionTryBlock = false;
        boolean consumedSemi = false;
        
        switch (LT(1))
        {
            case IToken.tSEMI :
                consume(IToken.tSEMI);
                consumedSemi = true;
                break;
            case IToken.t_try : 
            	consume( IToken.t_try );
            	if( LT(1) == IToken.tCOLON )
            		ctorInitializer( declarator );
        		hasFunctionTryBlock = true;
        		declarator.setFunctionTryBlock( true );    	
            	break;       	
            case IToken.tCOLON :
                ctorInitializer(declarator);
                break;
            case IToken.tLBRACE: 
            	break;
            case IToken.tRPAREN:
            	if( ! fromCatchHandler )
            		throw backtrack;
            	break;
            default: 
            	throw backtrack;
        }
        
        if( ! consumedSemi )
		{        
	        if( LT(1) == IToken.tLBRACE )
	        {
	        	if( firstToken == LA(1) )
					throw backtrack;
	            declarator.setHasFunctionBody(true);
	            hasFunctionBody = true;
	        }
	        
	        if( fromCatchHandler )
	        	return;
	        
	        if( hasFunctionTryBlock && ! hasFunctionBody )
	        	throw backtrack;
		}
		        
        List l = null; 
        try
        {
            l = sdw.createASTNodes(astFactory);
        }
        catch (ASTSemanticException e)
        {
			throw backtrack;
        }
        Iterator i = l.iterator();
        if (hasFunctionBody && l.size() != 1)
        {
            throw backtrack; //TODO Should be an IProblem
        }
        if (i.hasNext()) // no need to do this unless we have a declarator
        {
            if (!hasFunctionBody)
            {
                while (i.hasNext())
                {
                    IASTDeclaration declaration = (IASTDeclaration)i.next();
                    ((IASTOffsetableElement)declaration).setEndingOffsetAndLineNumber(
                        lastToken.getEndOffset(), lastToken.getLineNumber());
                    declaration.acceptElement( requestor );
                }
            }
            else
            {
                IASTDeclaration declaration = (IASTDeclaration)i.next();
                declaration.enterScope( requestor );
   			
   				if ( !( declaration instanceof IASTScope ) ) 
   					throw backtrack;
 
				handleFunctionBody((IASTScope)declaration );
				((IASTOffsetableElement)declaration).setEndingOffsetAndLineNumber(
					lastToken.getEndOffset(), lastToken.getLineNumber());
  
  				declaration.exitScope( requestor );
  				
  				if( hasFunctionTryBlock )
					catchHandlerSequence( scope );
  				
            }
        }
        else
        {
            try
            {
                astFactory
                    .createTypeSpecDeclaration(
                        sdw.getScope(),
                        sdw.getTypeSpecifier(),
                        ownerTemplate,
                        sdw.getStartingOffset(),
                        sdw.getStartingLine(), lastToken.getEndOffset(), lastToken.getLineNumber())
                    .acceptElement(requestor);
            }
            catch (Exception e1)
            {
                throw backtrack;
            }
        }
        
    }
    protected abstract void handleFunctionBody(IASTScope scope) throws BacktrackException, EndOfFileException;

    protected void skipOverCompoundStatement() throws BacktrackException, EndOfFileException
    {
        // speed up the parser by skiping the body
        // simply look for matching brace and return
        consume(IToken.tLBRACE);
        int depth = 1;
        while (depth > 0)
        {
            switch (consume().getType())
            {
                case IToken.tRBRACE :
                    --depth;
                    break;
                case IToken.tLBRACE :
                    ++depth;
                    break;
            }
        }
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
     * @throws BacktrackException	request a backtrack
     */
    protected void ctorInitializer(Declarator d )
        throws EndOfFileException, BacktrackException
    {
        consume(IToken.tCOLON);
        IASTScope scope = d.getDeclarationWrapper().getScope();
        try
        {
            for (;;)
            {
                if (LT(1) == IToken.tLBRACE)
                    break;

                
                ITokenDuple duple = name(scope);

                consume(IToken.tLPAREN);
                IASTExpression expressionList = null;

                expressionList = expression(d.getDeclarationWrapper().getScope());

                consume(IToken.tRPAREN);

                try
                {
                    d.addConstructorMemberInitializer(
                        astFactory.createConstructorMemberInitializer(
                            d.getDeclarationWrapper().getScope(),
                            duple, expressionList));
                }
                catch (Exception e1)
                {
                    throw backtrack;
                }
                if (LT(1) == IToken.tLBRACE)
                    break;
                consume(IToken.tCOMMA);
            }
        }
        catch (BacktrackException bt)
        {
 
            throw backtrack;
        }

    }
    /**
     * This routine parses a parameter declaration 
     * 
     * @param containerObject	The IParserCallback object representing the parameterDeclarationClause owning the parm. 
     * @throws BacktrackException		request a backtrack
     */
    protected void parameterDeclaration(
        IParameterCollection collection, IASTScope scope)
        throws BacktrackException, EndOfFileException
    {
        IToken current = LA(1);
        
        DeclarationWrapper sdw =
            new DeclarationWrapper(scope, current.getOffset(), current.getLineNumber(), null);
        declSpecifierSeq(sdw, true, false);
        if (sdw.getTypeSpecifier() == null
            && sdw.getSimpleType()
                != IASTSimpleTypeSpecifier.Type.UNSPECIFIED)
            try
            {
                sdw.setTypeSpecifier(
                    astFactory.createSimpleTypeSpecifier(
                        scope,
                        sdw.getSimpleType(),
                        sdw.getName(),
                        sdw.isShort(),
                        sdw.isLong(),
                        sdw.isSigned(),
                        sdw.isUnsigned(), sdw.isTypeNamed(), sdw.isComplex(), sdw.isImaginary()));
            }
            catch (ASTSemanticException e)
            {
                throw backtrack;
            }
            catch (Exception e)
            {
                throw backtrack;
            }
        
        setCompletionValues(scope,CompletionKind.USER_SPECIFIED_NAME,Key.EMPTY );     
        if (LT(1) != IToken.tSEMI)
           initDeclarator(sdw, SimpleDeclarationStrategy.TRY_FUNCTION );
 
 		if( lastToken != null )
 			sdw.setEndingOffsetAndLineNumber( lastToken.getEndOffset(), lastToken.getLineNumber() );
 			
        if (current == LA(1))
            throw backtrack;
        collection.addParameter(sdw);
    }
    /**
     * This class represents the state and strategy for parsing declarationSpecifierSequences
     */
    private class Flags
    {
        private boolean encounteredTypename = false;
        // have we encountered a typeName yet?
        private boolean encounteredRawType = false;
        // have we encountered a raw type yet?
        private final boolean parm;
        // is this for a simpleDeclaration or parameterDeclaration?
        private final boolean constructor;
        // are we attempting the constructor strategy?
        public Flags(boolean parm, boolean c)
        {
            this.parm = parm;
            constructor = c;
        }
        /**
         * @return	true if we have encountered a simple type up to this point, false otherwise
         */
        public boolean haveEncounteredRawType()
        {
            return encounteredRawType;
        }
        /**
         * @return  true if we have encountered a typename up to this point, false otherwise
         */
        public boolean haveEncounteredTypename()
        {
            return encounteredTypename;
        }
        /**
         * @param b - set to true if we encounter a raw type (int, short, etc.)
         */
        public void setEncounteredRawType(boolean b)
        {
            encounteredRawType = b;
        }
        /**
         * @param b - set to true if we encounter a typename
         */
        public void setEncounteredTypename(boolean b)
        {
            encounteredTypename = b;
        }
        /**
         * @return true if we are parsing for a ParameterDeclaration
         */
        public boolean isForParameterDeclaration()
        {
            return parm;
        }
        /**
         * @return whether or not we are attempting the constructor strategy or not 
         */
        public boolean isForConstructor()
        {
            return constructor;
        }
    }
    /**
     * @param flags            input flags that are used to make our decision 
     * @return                 whether or not this looks like a constructor (true or false)
     * @throws EndOfFileException       we could encounter EOF while looking ahead
     */
    private boolean lookAheadForConstructorOrConversion(Flags flags, DeclarationWrapper sdw )
        throws EndOfFileException
    {
        if (flags.isForParameterDeclaration())
            return false;
        if (LT(2) == IToken.tLPAREN && flags.isForConstructor())
            return true;
        
        IToken mark = mark(); 
        Declarator d = new Declarator( sdw );
        try
        {
            consumeTemplatedOperatorName( d );
        }
        catch (BacktrackException e)
        {
            backup( mark ); 
            return false;
        }
        
        ITokenDuple duple = d.getNameDuple(); 
       	if( duple == null )
       	{
       		backup( mark ); 
       		return false; 
       	} 
       	
       	int lastColon = duple.findLastTokenType(IToken.tCOLON);
       	if( lastColon == -1  ) 
       	{
       		int lt1 = LT(1);
       		backup( mark );
       		return flags.isForConstructor() && (lt1 == IToken.tLPAREN);
       	} 
       	
       	IToken className = null;
       	int index = lastColon - 1;
        if( duple.getToken( index ).getType() == IToken.tGT )
       	{
       		int depth = -1; 
       		while( depth == -1 )
       		{
       			if( duple.getToken( --index ).getType() == IToken.tLT )
       				++depth;
       		}
       		className = duple.getToken( index );
       	}
       	
       	boolean result = className.getImage().equals( duple.getLastToken());
       	backup( mark );
       	return result;
    }
    /**
     * @param flags			input flags that are used to make our decision 
     * @return				whether or not this looks like a a declarator follows
     * @throws EndOfFileException	we could encounter EOF while looking ahead
     */
    private boolean lookAheadForDeclarator(Flags flags) throws EndOfFileException
    {
        return flags.haveEncounteredTypename()
            && ((LT(2) != IToken.tIDENTIFIER
                || (LT(3) != IToken.tLPAREN && LT(3) != IToken.tASSIGN))
                && !LA(2).isPointer());
    }
    private void callbackSimpleDeclToken(Flags flags) throws EndOfFileException
    {
        flags.setEncounteredRawType(true);
        consume(); 
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
     * @throws BacktrackException		request a backtrack
     */
    protected void declSpecifierSeq(
        DeclarationWrapper sdw,
        boolean parm,
        boolean tryConstructor )
        throws BacktrackException, EndOfFileException
    {
        Flags flags = new Flags(parm, tryConstructor);
        IToken typeNameBegin = null;
        IToken typeNameEnd = null;
        declSpecifiers : for (;;)
        {
            switch (LT(1))
            {
                case IToken.t_inline :
                	consume(); 
                    sdw.setInline(true);
                    break;
                case IToken.t_auto :
					consume(); 
                    sdw.setAuto(true);
                    break;
                case IToken.t_register :
                    sdw.setRegister(true);
					consume(); 
    	            break;
                case IToken.t_static :
                    sdw.setStatic(true);
					consume(); 
    		        break;
                case IToken.t_extern :
                    sdw.setExtern(true);
					consume(); 
                    break;
                case IToken.t_mutable :
                    sdw.setMutable(true);
					consume(); 
                    break;
                case IToken.t_virtual :
                    sdw.setVirtual(true);
					consume(); 
                    break;
                case IToken.t_explicit :
                    sdw.setExplicit(true);
					consume(); 
                    break;
                case IToken.t_typedef :
                    sdw.setTypedef(true);
					consume(); 
                    break;
                case IToken.t_friend :
                    sdw.setFriend(true);
					consume(); 
                    break;
                case IToken.t_const :
                    sdw.setConst(true);
					consume(); 
                    break;
                case IToken.t_volatile :
                    sdw.setVolatile(true);
					consume(); 
                    break;
                case IToken.t_signed :
                    sdw.setSigned(true);
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.INT);
                    break;
                case IToken.t_unsigned :
                    sdw.setUnsigned(true);
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.INT);
                    break;
                case IToken.t_short :
                    sdw.setShort(true);
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.INT);
                    break;
                case IToken.t_long :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.INT);
                    sdw.setLong(true);
                    break;
                case IToken.t__Complex :
					consume( IToken.t__Complex );
					if (typeNameBegin == null)
						typeNameBegin = LA(1);
					typeNameEnd = LA(1);
					sdw.setComplex( true );
					break;
				case IToken.t__Imaginary :
					consume( IToken.t__Imaginary );
					if (typeNameBegin == null)
						typeNameBegin = LA(1);
					typeNameEnd = LA(1);
					sdw.setImaginary( true );
					break;                
                case IToken.t_char :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.CHAR);
                    break;
                case IToken.t_wchar_t :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
                    sdw.setSimpleType(
                        IASTSimpleTypeSpecifier.Type.WCHAR_T);
                    break;
                case IToken.t_bool :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.BOOL);
                    break;
                case IToken.t__Bool: 
					if (typeNameBegin == null)
						typeNameBegin = LA(1);
					typeNameEnd = LA(1);
					callbackSimpleDeclToken(flags);
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type._BOOL);
					break;                
                case IToken.t_int :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.INT);
                    break;					
                case IToken.t_float :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.FLOAT);
                    break;
                case IToken.t_double :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
                    sdw.setSimpleType(
                        IASTSimpleTypeSpecifier.Type.DOUBLE);
                    break;
                case IToken.t_void :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    callbackSimpleDeclToken(flags);
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.VOID);
                    break;
                case IToken.t_typename :
                    sdw.setTypenamed(true);
                    consume(IToken.t_typename ); 
                    IToken first = LA(1);
                    IToken last = null;
                    last = name(sdw.getScope()).getLastToken();
                    if (LT(1) == IToken.t_template)
                    {
                        consume(IToken.t_template);
                        last = templateId(sdw.getScope());
                    }
                    ITokenDuple duple = new TokenDuple(first, last);
                    sdw.setTypeName(duple);
      
                    break;
                case IToken.tCOLONCOLON :
                    consume(IToken.tCOLONCOLON);
                case IToken.tIDENTIFIER :
                    // TODO - Kludgy way to handle constructors/destructors
                    if (flags.haveEncounteredRawType())
                    {
                        if (typeNameBegin != null)
                            sdw.setTypeName(
                                new TokenDuple(typeNameBegin, typeNameEnd));
                        return;
                    }
                    if (parm && flags.haveEncounteredTypename())
                    {
                        if (typeNameBegin != null)
                            sdw.setTypeName(
                                new TokenDuple(typeNameBegin, typeNameEnd));
                        return;
                    }
                    if (lookAheadForConstructorOrConversion(flags, sdw))
                    {
                        if (typeNameBegin != null)
                            sdw.setTypeName(
                                new TokenDuple(typeNameBegin, typeNameEnd));
                        return;
                    }
                    if (lookAheadForDeclarator(flags))
                    {
                        if (typeNameBegin != null)
                            sdw.setTypeName(
                                new TokenDuple(typeNameBegin, typeNameEnd));
 
                        return;
                    }
 
                    ITokenDuple d = name(sdw.getScope());
                    sdw.setTypeName(d);
                    sdw.setSimpleType( IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME ); 
                    flags.setEncounteredTypename(true);
                    break;
                case IToken.t_class :
                case IToken.t_struct :
                case IToken.t_union :
                    try
                    {
                        classSpecifier(sdw);
						flags.setEncounteredTypename(true);
                        break;
                    }
                    catch (BacktrackException bt)
                    {
                        elaboratedTypeSpecifier(sdw);
                        flags.setEncounteredTypename(true);
                        break;
                    }
                case IToken.t_enum :
                    try
                    {
                        enumSpecifier(sdw);
   					    flags.setEncounteredTypename(true);
                        break;
                    }
                    catch (BacktrackException bt)
                    {
                        // this is an elaborated class specifier
                        elaboratedTypeSpecifier(sdw);
                        flags.setEncounteredTypename(true);
                        break;
                    }
                default :
                    break declSpecifiers;
            }
        }
        if (typeNameBegin != null)
            sdw.setTypeName(new TokenDuple(typeNameBegin, typeNameEnd));
    }
    /**
     * Parse an elaborated type specifier.  
     * 
     * @param decl			Declaration which owns the elaborated type 
     * @throws BacktrackException	request a backtrack
     */
    protected void elaboratedTypeSpecifier(DeclarationWrapper sdw)
        throws BacktrackException, EndOfFileException
    {
        // this is an elaborated class specifier
        IToken t = consume();
        ASTClassKind eck = null;
        switch (t.getType())
        {
            case IToken.t_class :
                eck = ASTClassKind.CLASS;
                break;
            case IToken.t_struct :
                eck = ASTClassKind.STRUCT;
                break;
            case IToken.t_union :
                eck = ASTClassKind.UNION;
                break;
            case IToken.t_enum :
                eck = ASTClassKind.ENUM;
                break;
            default :
                break;
        }
 
        ITokenDuple d = name(sdw.getScope());
		IASTTypeSpecifier elaboratedTypeSpec = null;
		final boolean isForewardDecl = ( LT(1) == IToken.tSEMI );
		
        try
        {
            elaboratedTypeSpec =
                astFactory.createElaboratedTypeSpecifier(
                    sdw.getScope(),
                    eck,
                    d,
                    t.getOffset(),
                    t.getLineNumber(), 
                    d.getLastToken().getEndOffset(), d.getLastToken().getLineNumber(), isForewardDecl, sdw.isFriend() );
        }
        catch (ASTSemanticException e)
        {
			failParse();
			throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
        sdw.setTypeSpecifier(elaboratedTypeSpec);
        
        if( isForewardDecl )
        	((IASTElaboratedTypeSpecifier)elaboratedTypeSpec).acceptElement( requestor );
    }
    /**
     * Consumes template parameters.  
     *
     * @param previousLast	Previous "last" token (returned if nothing was consumed)
     * @return				Last consumed token, or <code>previousLast</code> if nothing was consumed
     * @throws BacktrackException	request a backtrack
     */
    protected IToken consumeTemplateParameters(IToken previousLast)
        throws EndOfFileException, BacktrackException
    {
        IToken last = previousLast;
        if (LT(1) == IToken.tLT)
        {
            last = consume(IToken.tLT);
            // until we get all the names sorted out
            Stack scopes = new Stack();
            scopes.push(new Integer(IToken.tLT));
            
            while (!scopes.empty())
            {
				int top;
                last = consume();
                
                switch (last.getType()) {
                    case IToken.tGT :
                        if (((Integer)scopes.peek()).intValue() == IToken.tLT) {
							scopes.pop();
						}
                        break;
					case IToken.tRBRACKET :
						do {
							top = ((Integer)scopes.pop()).intValue();
						} while (!scopes.empty() && (top == IToken.tGT || top == IToken.tLT));
						if (top != IToken.tLBRACKET) throw backtrack;
						
						break;
					case IToken.tRPAREN :
						do {
							top = ((Integer)scopes.pop()).intValue();
						} while (!scopes.empty() && (top == IToken.tGT || top == IToken.tLT));
						if (top != IToken.tLPAREN) throw backtrack;
							
						break;
                    case IToken.tLT :
					case IToken.tLBRACKET:
					case IToken.tLPAREN:
						scopes.push(new Integer(last.getType()));
                        break;
                }
            }
        }
        return last;
    }
    /**
     * Parse an identifier.  
     * 
     * @throws BacktrackException	request a backtrack
     */
    protected IToken identifier() throws EndOfFileException, BacktrackException
    {
        IToken first = consume(IToken.tIDENTIFIER); // throws backtrack if its not that
        return first;
    }
    /**
     * Parses a className.  
     * 
     * class-name: identifier | template-id
     * 
     * @throws BacktrackException
     */
    protected ITokenDuple className(IASTScope scope) throws EndOfFileException, BacktrackException
    {
		ITokenDuple duple = name(scope);
		IToken last = duple.getLastToken(); 
        if (LT(1) == IToken.tLT) {
			last = consumeTemplateParameters(duple.getLastToken());
        }
        
		return new TokenDuple(duple.getFirstToken(), last);
    }
    
    /**
     * Parse a template-id, according to the ANSI C++ spec.  
     * 
     * template-id: template-name < template-argument-list opt >
     * template-name : identifier
     * 
     * @return		the last token that we consumed in a successful parse 
     * 
     * @throws BacktrackException	request a backtrack
     */
    protected IToken templateId(IASTScope scope) throws EndOfFileException, BacktrackException
    {
        ITokenDuple duple = name(scope);
        IToken last = consumeTemplateParameters(duple.getLastToken());
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
     * @throws BacktrackException	request a backtrack
     */
    protected TokenDuple name(IASTScope scope) throws BacktrackException, EndOfFileException
    {
        IToken first = LA(1);
        IToken last = null;
        IToken mark = mark();
 
        try
		{
	        if (LT(1) == IToken.tCOLONCOLON)
	            last = consume( IToken.tCOLONCOLON );
	        // TODO - whacky way to deal with destructors, please revisit
	        if (LT(1) == IToken.tCOMPL)
	            consume();
	        switch (LT(1))
	        {
	            case IToken.tIDENTIFIER :
	                last = consume(IToken.tIDENTIFIER);
	                IToken secondMark = null;
	                if( queryLookaheadCapability() )
	                	secondMark = mark();
	                else
	                	return new TokenDuple(last, last);
	                
	                try
	                {
	                	last = consumeTemplateParameters(last);
	                } catch( BacktrackException bt )
	                {
	                	backup( secondMark );
	                }
	                break;
	            default :
	                backup(mark);
	                throw backtrack;
	        }
	        while (LT(1) == IToken.tCOLONCOLON)
	        {
	            last = consume();
	            if (LT(1) == IToken.t_template)
	                consume();
	            if (LT(1) == IToken.tCOMPL)
	                consume();
	            switch (LT(1))
	            {
	                case IToken.t_operator :
	                    backup(mark);
	                    throw backtrack;
	                case IToken.tIDENTIFIER :
	                    last = consume();
	                    last = consumeTemplateParameters(last);
	            }
	        }
	
	        return new TokenDuple(first, last);
        } catch( OffsetLimitReachedException olre )
		{
        	backup(mark);
        	throw backtrack;
        }
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
     * @throws BacktrackException
     */
    protected IToken cvQualifier(
        IDeclarator declarator)
        throws EndOfFileException, BacktrackException
    {
    	IToken result = null; 
        switch (LT(1))
        {
            case IToken.t_const :
            	result = consume( IToken.t_const ); 
                declarator.addPointerOperator(ASTPointerOperator.CONST_POINTER);
                break;
            case IToken.t_volatile :
            	result = consume( IToken.t_volatile ); 
				declarator.addPointerOperator(ASTPointerOperator.VOLATILE_POINTER);
                break;
            case IToken.t_restrict : 
            	if( language == ParserLanguage.C )
            	{
            		result = consume( IToken.t_restrict );
					declarator.addPointerOperator(ASTPointerOperator.RESTRICT_POINTER);
					break;
            	}
            	else 
            		throw backtrack;
            default :
                
        }
        return result;
    }
    /**
     * Parses the initDeclarator construct of the ANSI C++ spec.
     * 
     * initDeclarator
     * : declarator ("=" initializerClause | "(" expressionList ")")?
     * @param owner			IParserCallback object that represents the owner declaration object.  
     * @return				declarator that this parsing produced.  
     * @throws BacktrackException	request a backtrack
     */
    protected Declarator initDeclarator(
        DeclarationWrapper sdw, SimpleDeclarationStrategy strategy )
        throws EndOfFileException, BacktrackException
    {
        Declarator d = declarator(sdw, sdw.getScope(), strategy );
        if( language == ParserLanguage.CPP )
        	optionalCPPInitializer(d);
        else if( language == ParserLanguage.C )
        	optionalCInitializer(d);
        sdw.addDeclarator(d);
        return d;
    }
    
    protected void optionalCPPInitializer(Declarator d)
        throws EndOfFileException, BacktrackException
    {
        // handle initializer
        if (LT(1) == IToken.tASSIGN)
        {
            consume(IToken.tASSIGN);
            d.setInitializerClause(initializerClause(d.getDeclarationWrapper().getScope()));
        }
        else if (LT(1) == IToken.tLPAREN )
        {
        	IToken mark = mark(); 
            // initializer in constructor
            try
            {
                consume(IToken.tLPAREN); // EAT IT!
                IASTExpression astExpression = null;
                astExpression = expression(d.getDeclarationWrapper().getScope());
                consume(IToken.tRPAREN);
                d.setConstructorExpression(astExpression);
            } catch( BacktrackException bt )
            {
            	backup( mark ); 
            	throw bt;
            }
        }
    }
    
    protected void optionalCInitializer( Declarator d ) throws EndOfFileException, BacktrackException
    {
    	if( LT(1) == IToken.tASSIGN )
    	{
    		consume( IToken.tASSIGN );
    		d.setInitializerClause( cInitializerClause(d.getDeclarationWrapper().getScope(), EMPTY_LIST ) );
    	}
    }
    /**
     * @param scope
     * @return
     */
    protected IASTInitializerClause cInitializerClause(
        IASTScope scope,
        List designators)
        throws EndOfFileException, BacktrackException
    {    	
        if (LT(1) == IToken.tLBRACE)
        {
            consume(IToken.tLBRACE);
            List initializerList = new ArrayList();
            for (;;)
            {
                // required at least one initializer list
                // get designator list
                List newDesignators = designatorList(scope);
                if( newDesignators.size() != 0 )
                	consume( IToken.tASSIGN );
                IASTInitializerClause initializer =
                    cInitializerClause(scope, newDesignators );
                initializerList.add(initializer);
                // can end with just a '}'
                if (LT(1) == IToken.tRBRACE)
                    break;
                // can end with ", }"
                if (LT(1) == IToken.tCOMMA)
                    consume(IToken.tCOMMA);
                if (LT(1) == IToken.tRBRACE)
                    break;
                // otherwise, its another initializer in the list
            }
            // consume the closing brace
            consume(IToken.tRBRACE);
            return astFactory.createInitializerClause(
                scope,
                (
				( designators.size() == 0 ) ? 
					IASTInitializerClause.Kind.INITIALIZER_LIST : 
					IASTInitializerClause.Kind.DESIGNATED_INITIALIZER_LIST ),
                null, initializerList, designators );
        }
        // if we get this far, it means that we have not yet succeeded
        // try this now instead
        // assignmentExpression 
        try
        {
            IASTExpression assignmentExpression = assignmentExpression(scope);
            try
            {
                return astFactory.createInitializerClause(
                    scope,
                    (
				( designators.size() == 0 ) ? 
					IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION : 
					IASTInitializerClause.Kind.DESIGNATED_ASSIGNMENT_EXPRESSION ),
                    assignmentExpression, null, designators );
            }
            catch (Exception e)
            {
                throw backtrack;
            }
        }
        catch (BacktrackException b)
        {
            // do nothing
        }
        throw backtrack;
    }
    /**
     * 
     */
    protected IASTInitializerClause initializerClause(IASTScope scope)
        throws EndOfFileException, BacktrackException
    {
        if (LT(1) == IToken.tLBRACE)
        {
            consume(IToken.tLBRACE);
            if (LT(1) == (IToken.tRBRACE))
            {
                consume(IToken.tRBRACE);
                try
                {
                    return astFactory.createInitializerClause(
                        scope,
                        IASTInitializerClause.Kind.EMPTY,
                        null, null, EMPTY_LIST );
                }
                catch (Exception e)
                {
                    throw backtrack;
                }
            }
            
            // otherwise it is a list of initializer clauses
            List initializerClauses = new ArrayList();
            for (;;)
            {
                IASTInitializerClause clause = initializerClause(scope);
                initializerClauses.add(clause);
                if (LT(1) == IToken.tRBRACE)
                    break;
                consume(IToken.tCOMMA);
            }
            consume(IToken.tRBRACE);
            try
            {
                return astFactory.createInitializerClause(
                    scope,
                    IASTInitializerClause.Kind.INITIALIZER_LIST,
                    null, initializerClauses, EMPTY_LIST );
            }
            catch (Exception e)
            {
                throw backtrack;
            }
        }
        
        // if we get this far, it means that we did not 
        // try this now instead
        // assignmentExpression 
        try
        {
            IASTExpression assignmentExpression =
                assignmentExpression(scope);
   
            try
            {
                return astFactory.createInitializerClause(
                    scope,
                    IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION,
                    assignmentExpression, null, EMPTY_LIST );
            }
            catch (Exception e)
            {
                throw backtrack;
            }
        }
        catch (BacktrackException b)
        {
			// do nothing
        }
        catch ( EndOfFileException eof )
        {

        }
        throw backtrack;
    }
    
    protected List designatorList(IASTScope scope) throws EndOfFileException, BacktrackException
    {
        List designatorList = new ArrayList();
        // designated initializers for C
        
    	if( LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET )
    	{
    
    		while( LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET )
    		{
    			IToken id = null; 
    			IASTExpression constantExpression = null;
    			IASTDesignator.DesignatorKind kind = null;
    			
    			if( LT(1) == IToken.tDOT )
    			{
    				consume( IToken.tDOT );
    				id = identifier();
    				kind = IASTDesignator.DesignatorKind.FIELD;
    			}
    			else if( LT(1) == IToken.tLBRACKET )
    			{
    				consume( IToken.tLBRACKET );
    				constantExpression = expression( scope );
    				consume( IToken.tRBRACKET );
					kind = IASTDesignator.DesignatorKind.SUBSCRIPT; 	
    			}
    			
    			IASTDesignator d = 
    				astFactory.createDesignator( kind, constantExpression, id );
    			designatorList.add( d );
    				
    		}
    	}
		return designatorList;
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
     * | directDeclarator "(" parameterDeclarationClause ")" (oldKRParameterDeclaration)*
     * 
     * declaratorId
     * : name
     * 
    	 * @param container		IParserCallback object that represents the owner declaration.  
     * @return				declarator that this parsing produced.
     * @throws BacktrackException	request a backtrack
     */
    protected Declarator declarator(
        IDeclaratorOwner owner, IASTScope scope, SimpleDeclarationStrategy strategy )
        throws EndOfFileException, BacktrackException
    {
        Declarator d = null;
        DeclarationWrapper sdw = owner.getDeclarationWrapper();
        overallLoop : do
        {
            d = new Declarator(owner);
 
            consumePointerOperators(d);
 
            if (LT(1) == IToken.tLPAREN)
            {
                consume();
                declarator(d, scope, strategy );
                consume(IToken.tRPAREN);
            }
            else
	            consumeTemplatedOperatorName(d);
            
            for (;;)
            {
                switch (LT(1))
                {
                    case IToken.tLPAREN :
                    	
                    	boolean failed = false;
                        // temporary fix for initializer/function declaration ambiguity
                        if ( queryLookaheadCapability(2) && !LA(2).looksLikeExpression() && strategy != SimpleDeclarationStrategy.TRY_VARIABLE  )
                        {
                        	if(  LT(2) == IToken.tIDENTIFIER )
                        	{
								IToken newMark = mark();
								consume( IToken.tLPAREN );

	                        	try
	                        	{
	                        		try
                                    {
                                        if( ! astFactory.queryIsTypeName( scope, name(scope) ) )
                                        	failed = true;
                                    }
                                    catch (Exception e)
                                    {
                                        throw backtrack;
                                    }
	                        	} catch( BacktrackException b )
	                        	{ 
	                        		failed = true; 
	                        	}
	                        	
								backup( newMark );
                        	}
                        }
						if( ( queryLookaheadCapability(2) && !LA(2).looksLikeExpression() && strategy != SimpleDeclarationStrategy.TRY_VARIABLE && !failed) || ! queryLookaheadCapability(3) )
						{  									
                            // parameterDeclarationClause
                            d.setIsFunction(true);
							// TODO need to create a temporary scope object here 
                            consume(IToken.tLPAREN);
                            setCompletionValues( scope, CompletionKind.ARGUMENT_TYPE, Key.DECL_SPECIFIER_SEQUENCE );
                            boolean seenParameter = false;
                            parameterDeclarationLoop : for (;;)
                            {
                                switch (LT(1))
                                {
                                    case IToken.tRPAREN :
                                        consume();
                                        setCompletionValues( scope, CompletionKind.NO_SUCH_KIND, KeywordSets.Key.FUNCTION_MODIFIER );
                                        break parameterDeclarationLoop;
                                    case IToken.tELLIPSIS :
                                        consume();
                                        d.setIsVarArgs( true );
                                        break;
                                    case IToken.tCOMMA :
                                        consume();
                                        setCompletionValues( scope, CompletionKind.ARGUMENT_TYPE, Key.DECL_SPECIFIER_SEQUENCE );            
                                        seenParameter = false;
                                        break;
                                    default :
                                        if (seenParameter)
                                            throw backtrack;
                                        parameterDeclaration(d, scope);
                                        seenParameter = true;
                                }
                            }
						}

                        if (LT(1) == IToken.tCOLON || LT(1) == IToken.t_try )
                            break overallLoop;
                        
                        IToken beforeCVModifier = mark();
                        IToken cvModifier = null;
                        IToken afterCVModifier = beforeCVModifier;
                        // const-volatile
                        // 2 options: either this is a marker for the method,
                        // or it might be the beginning of old K&R style parameter declaration, see
                        //      void getenv(name) const char * name; {}
                        // This will be determined further below
                        if (LT(1) == IToken.t_const
                            || LT(1) == IToken.t_volatile)
                        {
                            cvModifier = consume();
                            afterCVModifier = mark();
                        }
                        //check for throws clause here 
                        List exceptionSpecIds = null;
                        if (LT(1) == IToken.t_throw)
                        {
                            exceptionSpecIds = new ArrayList();
                            consume(); // throw
                            consume(IToken.tLPAREN); // (
                            boolean done = false;
                            IASTTypeId duple = null;
                            while (!done)
                            {
                                switch (LT(1))
                                {
                                    case IToken.tRPAREN :
                                        consume();
                                        done = true;
                                        break;
                                    case IToken.tCOMMA :
                                        consume();
                                        break;
                                    default :
                                        String image = LA(1).getImage();
                                        try
                                        {
                                            duple = typeId(scope, false);
                                            exceptionSpecIds.add(duple);
                                        }
                                        catch (BacktrackException e)
                                        {
                                            failParse();
                                            log.traceLog(
                                                "Unexpected Token ="
                                                    + image );
                                            consume();
                                            // eat this token anyway
                                            continue;
                                        }
                                        break;
                                }
                            }
                            if (exceptionSpecIds != null)
                                try
                                {
                                    d.setExceptionSpecification(
                                        astFactory
                                            .createExceptionSpecification(
                                            d.getDeclarationWrapper().getScope(), exceptionSpecIds));
                                }
                                catch (ASTSemanticException e)
                                {
                                    failParse();
                                    throw backtrack;
                                } catch (Exception e)
                                {
                                    throw backtrack;
                                }
                        }
                        // check for optional pure virtual							
                        if (LT(1) == IToken.tASSIGN
                            && LT(2) == IToken.tINTEGER
                            && LA(2).getImage().equals("0"))
                        {
                            consume(IToken.tASSIGN);
                            consume(IToken.tINTEGER);
                            d.setPureVirtual(true);
                        }
                        if (afterCVModifier != LA(1)
                            || LT(1) == IToken.tSEMI)
                        {
                            // There were C++-specific clauses after const/volatile modifier
                            // Then it is a marker for the method
                            if (cvModifier != null)
                            {
           
                                if (cvModifier.getType() == IToken.t_const)
                                    d.setConst(true);
                                if (cvModifier.getType()
                                    == IToken.t_volatile)
                                    d.setVolatile(true);
                            }
                            afterCVModifier = mark();
                            // In this case (method) we can't expect K&R parameter declarations,
                            // but we'll check anyway, for errorhandling
                        }
                        break;
                    case IToken.tLBRACKET :
                        consumeArrayModifiers(d, sdw.getScope());
                        continue;
                    case IToken.tCOLON :
                        consume(IToken.tCOLON);
                        IASTExpression exp = null;
                        exp = constantExpression(scope);
                        d.setBitFieldExpression(exp);
                    default :
                        break;
                }
                break;
            }
            if (LA(1).getType() != IToken.tIDENTIFIER)
                break;

        }
        while (true);
        if (d.getOwner() instanceof IDeclarator)
             ((Declarator)d.getOwner()).setOwnedDeclarator(d);
        return d;
    }
    protected void consumeTemplatedOperatorName(Declarator d)
        throws EndOfFileException, BacktrackException
    {
        if (LT(1) == IToken.t_operator)
            operatorId(d, null);
        else
        {
            try
            {
                ITokenDuple duple = name(d.getDeclarationWrapper().getScope());
                d.setName(duple);
        
            }
            catch (BacktrackException bt)
            {
                Declarator d1 = d;
                Declarator d11 = d1;
                IToken start = null;
                IToken mark = mark();
                if (LT(1) == IToken.tCOLONCOLON
                    || LT(1) == IToken.tIDENTIFIER)
                {
                    start = consume();
                    IToken end = null;
                    if (start.getType() == IToken.tIDENTIFIER)
                        end = consumeTemplateParameters(end);
                        while (LT(1) == IToken.tCOLONCOLON
                            || LT(1) == IToken.tIDENTIFIER)
                        {
                            end = consume();
                            if (end.getType() == IToken.tIDENTIFIER)
                                end = consumeTemplateParameters(end);
                        }
                    if (LT(1) == IToken.t_operator)
                        operatorId(d11, start);
                    else
                    {
                        backup(mark);
                        throw backtrack;
                    }
                }
            }
        }
    }
    protected void consumeArrayModifiers( IDeclarator d, IASTScope scope )
        throws EndOfFileException, BacktrackException
    {
        while (LT(1) == IToken.tLBRACKET)
        {
            consume( IToken.tLBRACKET ); // eat the '['
        
            IASTExpression exp = null;
            if (LT(1) != IToken.tRBRACKET)
            {
                exp = constantExpression(scope);
            }
            consume(IToken.tRBRACKET);
            IASTArrayModifier arrayMod;
            try
            {
                arrayMod = astFactory.createArrayModifier(exp);
            }
            catch (Exception e)
            {
                throw backtrack;
            }
            d.addArrayModifier(arrayMod);
        }
    }
    
    protected void operatorId(
        Declarator d,
        IToken originalToken)
        throws BacktrackException, EndOfFileException
    {
        // we know this is an operator
        IToken operatorToken = consume(IToken.t_operator);
        IToken toSend = null;
        if (LA(1).isOperator()
            || LT(1) == IToken.tLPAREN
            || LT(1) == IToken.tLBRACKET)
        {
            if ((LT(1) == IToken.t_new || LT(1) == IToken.t_delete)
                && LT(2) == IToken.tLBRACKET
                && LT(3) == IToken.tRBRACKET)
            {
                consume();
                consume(IToken.tLBRACKET);
                toSend = consume(IToken.tRBRACKET);
                // vector new and delete operators
            }
            else if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tRPAREN)
            {
                // operator ()
                consume(IToken.tLPAREN);
                toSend = consume(IToken.tRPAREN);
            }
            else if (LT(1) == IToken.tLBRACKET && LT(2) == IToken.tRBRACKET)
            {
                consume(IToken.tLBRACKET);
                toSend = consume(IToken.tRBRACKET);
            }
            else if (LA(1).isOperator())
                toSend = consume();
            else
                throw backtrack;
        }
        else
        {
            // must be a conversion function
            typeId(d.getDeclarationWrapper().getScope(), true );
            toSend = lastToken;
        }
        ITokenDuple duple =
            new TokenDuple(
                originalToken == null ? operatorToken : originalToken,
                toSend);
   
        d.setName(duple);
    }
    /**
     * Parse a Pointer Operator.   
     * 
     * ptrOperator
     * : "*" (cvQualifier)*
     * | "&"
     * | ::? nestedNameSpecifier "*" (cvQualifier)*
     * 
     * @param owner 		Declarator that this pointer operator corresponds to.  
     * @throws BacktrackException 	request a backtrack
     */
    protected IToken consumePointerOperators(IDeclarator d) throws EndOfFileException, BacktrackException
    {
    	IToken result = null;
    	for( ; ; )
    	{
	        if (LT(1) == IToken.tAMPER)
	        {
	        	result = consume( IToken.tAMPER ); 
	            d.addPointerOperator(ASTPointerOperator.REFERENCE);
	            return result;
	            
	        }
	        IToken mark = mark();

	        ITokenDuple nameDuple = null;
	        if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON)
	        {
	        	try
	        	{
		            nameDuple = name(d.getScope());
	        	}
	        	catch( BacktrackException bt )
	        	{
	        		backup( mark ); 
	        		return null;
	        	}
	        }
	        if ( LT(1) == IToken.tSTAR)
	        {
	            result = consume(IToken.tSTAR); // tokenType = "*"
	
				d.setPointerOperatorName(nameDuple);
	
				IToken successful = null;
	            for (;;)
	            {
                    IToken newSuccess = cvQualifier(d);
                    if( newSuccess != null ) successful = newSuccess; 
                    else break;
                    
	            }
	            
				if( successful == null )
				{
					d.addPointerOperator( ASTPointerOperator.POINTER );
				}
				continue;	            
	        }
	        backup(mark);
	        return result;
    	}
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
     * @throws	BacktrackException	request a backtrack
     */
    protected void enumSpecifier(DeclarationWrapper sdw)
        throws BacktrackException, EndOfFileException
    {
        IToken mark = mark();
        IToken identifier = null;
        consume( IToken.t_enum );
        if (LT(1) == IToken.tIDENTIFIER)
        {
            identifier = identifier();
        }
        if (LT(1) == IToken.tLBRACE)
        {
            IASTEnumerationSpecifier enumeration = null;
            try
            {
                enumeration = astFactory.createEnumerationSpecifier(
                        sdw.getScope(),
                        ((identifier == null) ? "" : identifier.getImage()),
                        mark.getOffset(), 
                        mark.getLineNumber(), 
                         ((identifier == null)
                            ? mark.getOffset()
                            : identifier.getOffset()), 
							((identifier == null)? mark.getEndOffset() : identifier.getEndOffset()), 
							((identifier == null)? mark.getLineNumber() : identifier.getLineNumber())
                );
            }
            catch (ASTSemanticException e)
            {
				failParse();
				throw backtrack;               
            } catch (Exception e)
            {
                throw backtrack;
            }
            consume(IToken.tLBRACE);
            while (LT(1) != IToken.tRBRACE)
            {
                IToken enumeratorIdentifier = null;
                if (LT(1) == IToken.tIDENTIFIER)
                {
                    enumeratorIdentifier = identifier();
                }
                else
                {
                    throw backtrack;
                }
                IASTExpression initialValue = null;
                if (LT(1) == IToken.tASSIGN)
                {
                    consume(IToken.tASSIGN);
                    initialValue = constantExpression(sdw.getScope());
                }
  
                if (LT(1) == IToken.tRBRACE)
                {
                    try
                    {
                        astFactory.addEnumerator(
                            enumeration,
                            enumeratorIdentifier.getImage(),
                            enumeratorIdentifier.getOffset(),
							enumeratorIdentifier.getLineNumber(),
                            enumeratorIdentifier.getOffset(), enumeratorIdentifier.getEndOffset(), 
                            enumeratorIdentifier.getLineNumber(), lastToken.getEndOffset(), lastToken.getLineNumber(), initialValue);
                    }
                    catch (ASTSemanticException e1)
                    {
						failParse();
						throw backtrack;                   
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                }
                if (LT(1) != IToken.tCOMMA)
                {
                    throw backtrack;
                }
                try
                {
                    astFactory.addEnumerator(
                        enumeration,
                        enumeratorIdentifier.getImage(),
                        enumeratorIdentifier.getOffset(),
						enumeratorIdentifier.getLineNumber(),
						enumeratorIdentifier.getOffset(), enumeratorIdentifier.getEndOffset(), enumeratorIdentifier.getLineNumber(), lastToken.getEndOffset(), lastToken.getLineNumber(), initialValue);
                }
                catch (ASTSemanticException e1)
                {
					failParse();
					throw backtrack; 
                } catch (Exception e)
                {
                    throw backtrack;
                }
                consume(IToken.tCOMMA);
            }
            IToken t = consume(IToken.tRBRACE);
            enumeration.setEndingOffsetAndLineNumber(t.getEndOffset(), t.getLineNumber());
            enumeration.acceptElement( requestor );
            sdw.setTypeSpecifier(enumeration);
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
     * @throws	BacktrackException	request a backtrack
     */
    protected void classSpecifier(DeclarationWrapper sdw)
        throws BacktrackException, EndOfFileException
    {
        ClassNameType nameType = ClassNameType.IDENTIFIER;
        ASTClassKind classKind = null;
        ASTAccessVisibility access = ASTAccessVisibility.PUBLIC;
        IToken classKey = null;
        IToken mark = mark();
        
		// class key
        switch (LT(1))
        {
            case IToken.t_class :
                classKey = consume();
                classKind = ASTClassKind.CLASS;
                access = ASTAccessVisibility.PRIVATE;
                break;
            case IToken.t_struct :
                classKey = consume();
                classKind = ASTClassKind.STRUCT;
                break;
            case IToken.t_union :
                classKey = consume();
                classKind = ASTClassKind.UNION;
                break;
            default :
                throw backtrack;
        }

        
        ITokenDuple duple = null;
        
        setCompletionValues(sdw.getScope(), CompletionKind.USER_SPECIFIED_NAME, Key.EMPTY );
        // class name
        if (LT(1) == IToken.tIDENTIFIER)
            duple = className(sdw.getScope());
        if (duple != null && !duple.isIdentifier())
            nameType = ClassNameType.TEMPLATE;
        if (LT(1) != IToken.tCOLON && LT(1) != IToken.tLBRACE)
        {
            backup(mark);
            throw backtrack;
        }
        IASTClassSpecifier astClassSpecifier = null;
        
        try
        {
            astClassSpecifier = 
                astFactory
                    .createClassSpecifier(
                        sdw.getScope(),
                        duple, 
                        classKind,
                        nameType,
                        access,
                        classKey.getOffset(),
            			classKey.getLineNumber(), 
						duple == null ?  classKey.getOffset() : duple.getFirstToken().getOffset(), 
						duple == null ?  classKey.getEndOffset() : duple.getFirstToken().getEndOffset(), 
						duple == null ?  classKey.getLineNumber() : duple.getFirstToken().getLineNumber() );
        }
        catch (ASTSemanticException e)
        {
			failParse();
			throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
        sdw.setTypeSpecifier(astClassSpecifier);
        // base clause
        if (LT(1) == IToken.tCOLON)
        {
            baseSpecifier(astClassSpecifier);
        }
        if (LT(1) == IToken.tLBRACE)
        {
            consume(IToken.tLBRACE);
            setCompletionValues(astClassSpecifier, CompletionKind.FIELD_TYPE, Key.DECLARATION );
            astClassSpecifier.enterScope( requestor );
            memberDeclarationLoop : while (LT(1) != IToken.tRBRACE)
            {
                IToken checkToken = LA(1);
                switch (LT(1))
                {
                    case IToken.t_public :
						consume(); 
						consume(IToken.tCOLON);
						astClassSpecifier.setCurrentVisibility( ASTAccessVisibility.PUBLIC );
						break;                    
                    case IToken.t_protected :
						consume(); 
						consume(IToken.tCOLON);
					astClassSpecifier.setCurrentVisibility( ASTAccessVisibility.PROTECTED);
						break;

                    case IToken.t_private :
                    	consume(); 
                        consume(IToken.tCOLON);
						astClassSpecifier.setCurrentVisibility( ASTAccessVisibility.PRIVATE);
                        break;
                    case IToken.tRBRACE :
                        consume(IToken.tRBRACE);
                        break memberDeclarationLoop;
                    default :
                        try
                        {
                            declaration(astClassSpecifier, null, null);
                        }
                        catch (BacktrackException bt)
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
            IToken lt = consume(IToken.tRBRACE);
            astClassSpecifier.setEndingOffsetAndLineNumber(lt.getEndOffset(), lt.getLineNumber());
            
            try
            {
                astFactory.signalEndOfClassSpecifier( astClassSpecifier );
            }
            catch (Exception e1)
            {
                throw backtrack;
            }
            
            astClassSpecifier.exitScope( requestor );
            
            
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
     * @throws BacktrackException
     */
    protected void baseSpecifier(
        IASTClassSpecifier astClassSpec)
        throws EndOfFileException, BacktrackException
    {
        consume(IToken.tCOLON);
        
        setCompletionValues(astClassSpec.getOwnerScope(), CompletionKind.CLASS_REFERENCE, KeywordSets.Key.BASE_SPECIFIER );
        boolean isVirtual = false;
        ASTAccessVisibility visibility = ASTAccessVisibility.PUBLIC;
        ITokenDuple nameDuple = null;
        
        baseSpecifierLoop : for (;;)
        {
            switch (LT(1))
            {
                case IToken.t_virtual :
                    consume(IToken.t_virtual);
                    isVirtual = true;
                    break;
                case IToken.t_public :
                	consume(); 
                    break;
                case IToken.t_protected :
					consume();
				    visibility = ASTAccessVisibility.PROTECTED;
                    break;
                case IToken.t_private :
                    visibility = ASTAccessVisibility.PRIVATE;
					consume();
           			break;
                case IToken.tCOLONCOLON :
                case IToken.tIDENTIFIER :
                    nameDuple = name(astClassSpec);
                    break;
                case IToken.tCOMMA :
                    try
                    {
                        astFactory.addBaseSpecifier(
                            astClassSpec,
                            isVirtual,
                            visibility,
                            nameDuple );
                    }
                    catch (ASTSemanticException e)
                    {
						failParse();
						throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    isVirtual = false;
                    visibility = ASTAccessVisibility.PUBLIC;
                    nameDuple = null;                        
                    consume();
                    continue baseSpecifierLoop;
                default :
                    break baseSpecifierLoop;
            }
        }

        try
        {
            astFactory.addBaseSpecifier(
                astClassSpec,
                isVirtual,
                visibility,
                nameDuple );
        }
        catch (ASTSemanticException e)
        {
			failParse();
			throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
    }
    /**
     * Parses a function body. 
     * 
     * @throws BacktrackException	request a backtrack
     */
    protected void functionBody( IASTScope scope ) throws EndOfFileException, BacktrackException
    {
        compoundStatement( scope, false );
    }
    /**
     * Parses a statement. 
     * 
     * @throws BacktrackException	request a backtrack
     */
    protected void statement(IASTScope scope) throws EndOfFileException, BacktrackException
    {
    	setCompletionValues(scope, CompletionKind.SINGLE_NAME_REFERENCE, Key.STATEMENT);
    	
        switch (LT(1))
        {
            case IToken.t_case :
                consume(IToken.t_case);
                IASTExpression constant_expression = constantExpression(scope);
				constant_expression.acceptElement(requestor);
                consume(IToken.tCOLON);
                statement(scope);
                return;
            case IToken.t_default :
                consume(IToken.t_default);
                consume(IToken.tCOLON);
                statement(scope);
                return;
            case IToken.tLBRACE :
                compoundStatement(scope, true);
                return;
            case IToken.t_if :
                consume( IToken.t_if );
                consume(IToken.tLPAREN);
                condition( scope );
                consume(IToken.tRPAREN);
                if( LT(1) != IToken.tLBRACE )
                    singleStatementScope(scope);
                else
                	statement( scope );
                if (LT(1) == IToken.t_else)
                {
                    consume( IToken.t_else );
                    if( LT(1) != IToken.tLBRACE )
						singleStatementScope(scope);
                    else
                    	statement( scope );
                }
                return;
            case IToken.t_switch :
                consume();
                consume(IToken.tLPAREN);
                condition(scope);
                consume(IToken.tRPAREN);
                statement(scope);
                return;
            case IToken.t_while :
                consume(IToken.t_while);
                consume(IToken.tLPAREN);
                condition(scope);
                consume(IToken.tRPAREN);
                if( LT(1) != IToken.tLBRACE )
					singleStatementScope(scope);
                else
                	statement(scope);
                return;
            case IToken.t_do :
                consume(IToken.t_do);
				if( LT(1) != IToken.tLBRACE )
					singleStatementScope(scope);
				else
					statement(scope);
                consume(IToken.t_while);
                consume(IToken.tLPAREN);
                condition(scope);
                consume(IToken.tRPAREN);
                return;
            case IToken.t_for :
                consume();
                consume(IToken.tLPAREN);
                forInitStatement(scope);
                if (LT(1) != IToken.tSEMI)
                    condition(scope);
                consume(IToken.tSEMI);
                if (LT(1) != IToken.tRPAREN)
                {  
                    IASTExpression finalExpression = expression(scope);
                    finalExpression.acceptElement(requestor);
                }
                consume(IToken.tRPAREN);
                statement(scope);
                return;
            case IToken.t_break :
                consume();
                consume(IToken.tSEMI);
                return;
            case IToken.t_continue :
                consume();
                consume(IToken.tSEMI);
                return;
            case IToken.t_return :
                consume();
                if (LT(1) != IToken.tSEMI)
                {
                    IASTExpression retVal = expression(scope);
                    retVal.acceptElement(requestor);
                }
                consume(IToken.tSEMI);
                return;
            case IToken.t_goto :
                consume();
                consume(IToken.tIDENTIFIER);
                consume(IToken.tSEMI);
                return;
            case IToken.t_try :
                consume();
                compoundStatement(scope,true);
                catchHandlerSequence(scope);
                return;
            case IToken.tSEMI :
                consume();
                return;
            default :
                // can be many things:
                // label
            	
            	try
				{
	                if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON)
	                {
	                    consume(IToken.tIDENTIFIER);
	                    consume(IToken.tCOLON);
	                    statement(scope);
	                    return;
	                }
            	}catch( OffsetLimitReachedException olre )
				{
            		// ok
            	}
                // expressionStatement
                // Note: the function style cast ambiguity is handled in expression
                // Since it only happens when we are in a statement
                IToken mark = mark();
                try
                {
                    IASTExpression thisExpression = expression(scope);
                   	consume(IToken.tSEMI);
                    thisExpression.acceptElement( requestor );
                    return;
                }
                catch (BacktrackException b)
                {
                	backup( mark );
                }
                catch( OffsetLimitReachedException olre )
				{
                	backup(mark);
                }

                // declarationStatement
                declaration(scope, null, null);
        }        

    }
    protected void catchHandlerSequence(IASTScope scope)
	throws EndOfFileException, BacktrackException {
    	if( LT(1) != IToken.t_catch )
    		throw backtrack; // error, need at least one of these
    	while (LT(1) == IToken.t_catch)
    	{
    		consume(IToken.t_catch);
    		consume(IToken.tLPAREN);
    		if( LT(1) == IToken.tELLIPSIS )
    			consume( IToken.tELLIPSIS );
    		else 
    			simpleDeclaration( SimpleDeclarationStrategy.TRY_VARIABLE, scope, null, CompletionKind.EXCEPTION_REFERENCE, true); // was exceptionDeclaration
    		consume(IToken.tRPAREN);
    		
    		catchBlockCompoundStatement(scope);
    	}
    }
    
    
    protected abstract void catchBlockCompoundStatement(IASTScope scope) throws BacktrackException, EndOfFileException; 
    
	protected void singleStatementScope(IASTScope scope) throws EndOfFileException, BacktrackException
    {
        IASTCodeScope newScope;
        try
        {
            newScope = astFactory.createNewCodeBlock(scope);
        }
        catch (Exception e)
        {
            throw backtrack;
        }
        newScope.enterScope( requestor );
        try
        {
			statement( newScope );
        }
        finally
        {
			newScope.exitScope( requestor );
        }
    }

    /**
     * @throws BacktrackException
     */
    protected void condition( IASTScope scope ) throws BacktrackException, EndOfFileException
    {
        IASTExpression someExpression = expression( scope );
        someExpression.acceptElement(requestor);
        //TODO type-specifier-seq declarator = assignment expression 
    }
    
    /**
     * @throws BacktrackException
     */
    protected void forInitStatement( IASTScope scope ) throws BacktrackException, EndOfFileException
    {
    	try
    	{
        	simpleDeclarationStrategyUnion(scope,null, null);
    	}
    	catch( BacktrackException bt )
    	{
    		try
    		{
    			IASTExpression e = expression( scope );
    			e.acceptElement(requestor);
    		}
    		catch( BacktrackException b )
    		{
    			failParse(); 
    			throw b;
    		}
    	}
        
    }
    /**
     * @throws BacktrackException
     */
    protected void compoundStatement( IASTScope scope, boolean createNewScope ) throws EndOfFileException, BacktrackException
    {
        consume(IToken.tLBRACE);
        
		IASTCodeScope newScope = null;
        if( createNewScope )
        {
        	try
            {
                newScope = astFactory.createNewCodeBlock(scope);
            }
            catch (Exception e)
            {
                throw backtrack;
            }        
        	newScope.enterScope( requestor );
        }
        IToken checkToken = null;
        setCompletionValues( 
        		(createNewScope ? newScope : scope ), 
				CompletionKind.SINGLE_NAME_REFERENCE, 
        		KeywordSets.Key.STATEMENT );
        
        while (LT(1) != IToken.tRBRACE)
        {
        	checkToken = LA(1);
        	try
        	{
            	statement(createNewScope ? newScope : scope );
        	}
        	catch( BacktrackException b )
        	{
        		failParse(); 
        		if( LA(1) == checkToken )
        			errorHandling();
        	}
        	setCompletionValues(((createNewScope ? newScope : scope )), CompletionKind.SINGLE_NAME_REFERENCE, 
        			KeywordSets.Key.STATEMENT );
        }
        
        consume(IToken.tRBRACE);
        
        if( createNewScope )
        	newScope.exitScope( requestor );
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression constantExpression( IASTScope scope )
        throws BacktrackException, EndOfFileException
    {
        return conditionalExpression(scope);
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IParser#expression(java.lang.Object)
     */
    public IASTExpression expression(IASTScope scope) throws BacktrackException, EndOfFileException
    {
        IASTExpression assignmentExpression = assignmentExpression(scope);
        while (LT(1) == IToken.tCOMMA)
        {
            consume();
            IASTExpression secondExpression = assignmentExpression(scope);
            try
            {
                assignmentExpression =
                    astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.EXPRESSIONLIST,
                        assignmentExpression,
                        secondExpression,
                        null,
                        null,
                        null, "", null);
            }
            catch (ASTSemanticException e)
            {
                throw backtrack;
            } catch (Exception e)
            {
                throw backtrack;
            }
        }
        return assignmentExpression;
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
	protected IASTExpression assignmentExpression(IASTScope scope)
		throws EndOfFileException, BacktrackException {
		if (LT(1) == IToken.t_throw) {
			return throwExpression(scope);
		}
		IASTExpression conditionalExpression = conditionalExpression(scope);
		// if the condition not taken, try assignment operators
		if (conditionalExpression != null
			&& conditionalExpression.getExpressionKind()
				== IASTExpression.Kind.CONDITIONALEXPRESSION)
			return conditionalExpression;
		switch (LT(1)) {
			case IToken.tASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_NORMAL,
					conditionalExpression);
			case IToken.tSTARASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_MULT,
					conditionalExpression);
			case IToken.tDIVASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_DIV,
					conditionalExpression);
			case IToken.tMODASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_MOD,
					conditionalExpression);
			case IToken.tPLUSASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_PLUS,
					conditionalExpression);
			case IToken.tMINUSASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_MINUS,
					conditionalExpression);
			case IToken.tSHIFTRASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_RSHIFT,
					conditionalExpression);
			case IToken.tSHIFTLASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_LSHIFT,
					conditionalExpression);
			case IToken.tAMPERASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_AND,
					conditionalExpression);
			case IToken.tXORASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_XOR,
					conditionalExpression);
			case IToken.tBITORASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_OR,
					conditionalExpression);
		}
		return conditionalExpression;
	}
    protected IASTExpression assignmentOperatorExpression(
    	IASTScope scope,
        IASTExpression.Kind kind, IASTExpression lhs )
        throws EndOfFileException, BacktrackException
    {
        consume();
        IASTExpression assignmentExpression = assignmentExpression(scope);
 
        try
        {
            return astFactory.createExpression(
                scope,
                kind,
                lhs,
				assignmentExpression,
                null,
                null,
                null, "", null);
        }
        catch (ASTSemanticException e)
        {
            throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression throwExpression( IASTScope scope )
        throws EndOfFileException, BacktrackException
    {
        consume(IToken.t_throw);
        IASTExpression throwExpression = null;
        try
        {
            throwExpression = expression(scope);
        }
        catch (BacktrackException b)
        {
        }
        try
        {
            return astFactory.createExpression(
                scope,
                IASTExpression.Kind.THROWEXPRESSION,
                throwExpression,
                null,
                null,
                null,
                null, "", null);
        }
        catch (ASTSemanticException e)
        {
            throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
    }
    /**
     * @param expression
     * @return
     * @throws BacktrackException
     */
    protected IASTExpression conditionalExpression( IASTScope scope )
        throws BacktrackException, EndOfFileException
    {
        IASTExpression firstExpression = logicalOrExpression(scope);
        if (LT(1) == IToken.tQUESTION)
        {
            consume();
            IASTExpression secondExpression = expression(scope);
            consume(IToken.tCOLON);
            IASTExpression thirdExpression = assignmentExpression(scope);
            try
            {
                return astFactory.createExpression(
                    scope,
                    IASTExpression.Kind.CONDITIONALEXPRESSION,
                    firstExpression,
                    secondExpression,
                    thirdExpression,
                    null,
                    null, "", null);
            }
            catch (ASTSemanticException e)
            {
                throw backtrack;
            } catch (Exception e)
            {
                throw backtrack;
            }
        }
        else
            return firstExpression;
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression logicalOrExpression(IASTScope scope)
        throws BacktrackException, EndOfFileException
    {
        IASTExpression firstExpression = logicalAndExpression(scope);
        while (LT(1) == IToken.tOR)
        {
            consume();
            IASTExpression secondExpression = logicalAndExpression(scope);

            try
            {
                firstExpression =
                    astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.LOGICALOREXPRESSION,
                        firstExpression,
                        secondExpression,
                        null,
                        null,
                        null, "", null);
            }
            catch (ASTSemanticException e)
            {
                throw backtrack;
            } catch (Exception e)
            {
                throw backtrack;
            }
        }
        return firstExpression;
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression logicalAndExpression( IASTScope scope )
        throws BacktrackException, EndOfFileException
    {
        IASTExpression firstExpression = inclusiveOrExpression( scope );
        while (LT(1) == IToken.tAND)
        {
            consume();
            IASTExpression secondExpression = inclusiveOrExpression( scope );
            try
            {
                firstExpression =
                    astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.LOGICALANDEXPRESSION,
                        firstExpression,
                        secondExpression,
                        null,
                        null,
                        null, "", null);
            }
            catch (ASTSemanticException e)
            {
                throw backtrack;
            } catch (Exception e)
            {
                throw backtrack;
            }
        }
        return firstExpression;
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression inclusiveOrExpression( IASTScope scope )
        throws BacktrackException, EndOfFileException
    {
        IASTExpression firstExpression = exclusiveOrExpression(scope);
        while (LT(1) == IToken.tBITOR)
        {
            consume();
            IASTExpression secondExpression = exclusiveOrExpression(scope);
  
            try
            {
                firstExpression =
                    astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.INCLUSIVEOREXPRESSION,
                        firstExpression,
                        secondExpression,
                        null,
                        null,
                        null, "", null);
            }
            catch (ASTSemanticException e)
            {
                throw backtrack;
            } catch (Exception e)
            {
                throw backtrack;
            }
        }
        return firstExpression;
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression exclusiveOrExpression( IASTScope scope )
        throws BacktrackException, EndOfFileException
    {
        IASTExpression firstExpression = andExpression( scope );
        while (LT(1) == IToken.tXOR)
        {
            consume();
            
            IASTExpression secondExpression = andExpression( scope );

            try
            {
                firstExpression =
                    astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.EXCLUSIVEOREXPRESSION,
                        firstExpression,
                        secondExpression,
                        null,
                        null,
                        null, "", null);
            }
            catch (ASTSemanticException e)
            {
                throw backtrack;
            } catch (Exception e)
            {
                throw backtrack;
            }
        }
        return firstExpression;
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression andExpression(IASTScope scope) throws EndOfFileException, BacktrackException
    {
        IASTExpression firstExpression = equalityExpression(scope);
        while (LT(1) == IToken.tAMPER)
        {
            consume();
            IASTExpression secondExpression = equalityExpression(scope);
 
            try
            {
                firstExpression =
                    astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.ANDEXPRESSION,
                        firstExpression,
                        secondExpression,
                        null,
                        null,
                        null, "", null);
            }
            catch (ASTSemanticException e)
            {
                throw backtrack;
            } catch (Exception e)
            {
                throw backtrack;
            }
        }
        return firstExpression;
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression equalityExpression( IASTScope scope )
        throws EndOfFileException, BacktrackException
    {
        IASTExpression firstExpression = relationalExpression(scope);
        for (;;)
        {
            switch (LT(1))
            {
                case IToken.tEQUAL :
                case IToken.tNOTEQUAL :
                    IToken t = consume();
                    IASTExpression secondExpression =
                        relationalExpression(scope);

                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                (t.getType() == IToken.tEQUAL)
                                    ? IASTExpression.Kind.EQUALITY_EQUALS
                                    : IASTExpression.Kind.EQUALITY_NOTEQUALS,
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e)
                    {
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                default :
                    return firstExpression;
            }
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression relationalExpression(IASTScope scope)
        throws BacktrackException, EndOfFileException
    {
        IASTExpression firstExpression = shiftExpression(scope);
        for (;;)
        {
            switch (LT(1))
            {
                case IToken.tGT :
                case IToken.tLT :
                case IToken.tLTEQUAL :
                case IToken.tGTEQUAL :
                    IToken mark = mark();
                    IToken t = consume();
                    IToken next = LA(1);
                    IASTExpression secondExpression =
                        shiftExpression(scope);
                    if (next == LA(1))
                    {
                        // we did not consume anything
                        // this is most likely an error
                        backup(mark);
                        return firstExpression;
                    }
                    else
                    {
                        IASTExpression.Kind kind = null;
                        switch (t.getType())
                        {
                            case IToken.tGT :
                                kind =
                                    IASTExpression.Kind.RELATIONAL_GREATERTHAN;
                                break;
                            case IToken.tLT :
                                kind = IASTExpression.Kind.RELATIONAL_LESSTHAN;
                                break;
                            case IToken.tLTEQUAL :
                                kind =
                                    IASTExpression
                                        .Kind
                                        .RELATIONAL_LESSTHANEQUALTO;
                                break;
                            case IToken.tGTEQUAL :
                                kind =
                                    IASTExpression
                                        .Kind
                                        .RELATIONAL_GREATERTHANEQUALTO;
                                break;
                        }
                        try
                        {
                            firstExpression =
                                astFactory.createExpression(
                                    scope,
                                    kind,
                                    firstExpression,
                                    secondExpression,
                                    null,
                                    null,
                                    null, "", null);
                        }
                        catch (ASTSemanticException e)
                        {
                            throw backtrack;
                        } catch (Exception e)
                        {
                            throw backtrack;
                        }
                    }
                    break;
                default :
                    return firstExpression;
            }
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression shiftExpression(IASTScope scope)
        throws BacktrackException, EndOfFileException
    {
        IASTExpression firstExpression = additiveExpression(scope);
        for (;;)
        {
            switch (LT(1))
            {
                case IToken.tSHIFTL :
                case IToken.tSHIFTR :
                    IToken t = consume();
                    IASTExpression secondExpression =
                        additiveExpression(scope);
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                ((t.getType() == IToken.tSHIFTL)
                                    ? IASTExpression.Kind.SHIFT_LEFT
                                    : IASTExpression.Kind.SHIFT_RIGHT),
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e)
                    {
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                default :
                    return firstExpression;
            }
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression additiveExpression( IASTScope scope )
        throws BacktrackException, EndOfFileException
    {
        IASTExpression firstExpression = multiplicativeExpression( scope );
        for (;;)
        {
            switch (LT(1))
            {
                case IToken.tPLUS :
                case IToken.tMINUS :
                    IToken t = consume();
                    IASTExpression secondExpression =
                        multiplicativeExpression(scope);
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                ((t.getType() == IToken.tPLUS)
                                    ? IASTExpression.Kind.ADDITIVE_PLUS
                                    : IASTExpression.Kind.ADDITIVE_MINUS),
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e)
                    {
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                default :
                    return firstExpression;
            }
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression multiplicativeExpression( IASTScope scope )
        throws BacktrackException, EndOfFileException
    {
        IASTExpression firstExpression = pmExpression(scope);
        for (;;)
        {
            switch (LT(1))
            {
                case IToken.tSTAR :
                case IToken.tDIV :
                case IToken.tMOD :
                    IToken t = consume();
                    IASTExpression secondExpression = pmExpression(scope);
                    IASTExpression.Kind kind = null;
                    switch (t.getType())
                    {
                        case IToken.tSTAR :
                            kind = IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY;
                            break;
                        case IToken.tDIV :
                            kind = IASTExpression.Kind.MULTIPLICATIVE_DIVIDE;
                            break;
                        case IToken.tMOD :
                            kind = IASTExpression.Kind.MULTIPLICATIVE_MODULUS;
                            break;
                    }
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                kind,
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e)
                    {
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                default :
                    return firstExpression;
            }
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression pmExpression( IASTScope scope ) throws EndOfFileException, BacktrackException
    {
        IASTExpression firstExpression = castExpression(scope);
        for (;;)
        {
            switch (LT(1))
            {
                case IToken.tDOTSTAR :
                case IToken.tARROWSTAR :
                    IToken t = consume();
                    IASTExpression secondExpression =
                        castExpression(scope);
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                ((t.getType() == IToken.tDOTSTAR)
                                    ? IASTExpression.Kind.PM_DOTSTAR
                                    : IASTExpression.Kind.PM_ARROWSTAR),
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e)
                    {
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                default :
                    return firstExpression;
            }
        }
    }
    /**
     * castExpression
     * : unaryExpression
     * | "(" typeId ")" castExpression
     */
    protected IASTExpression castExpression( IASTScope scope ) throws EndOfFileException, BacktrackException
    {
        // TO DO: we need proper symbol checkint to ensure type name
        if (LT(1) == IToken.tLPAREN)
        {
            IToken mark = mark();
            consume();
            IASTTypeId typeId = null;
            // If this isn't a type name, then we shouldn't be here
            try
            {
                typeId = typeId(scope, false);
                consume(IToken.tRPAREN);
                IASTExpression castExpression = castExpression(scope);
                try
                {
                    return astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.CASTEXPRESSION,
                        castExpression,
                        null,
                        null,
                        typeId,
                        null, "", null);
                }
                catch (ASTSemanticException e)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
            }
            catch (BacktrackException b)
            {
                backup(mark);
            }
        }
        return unaryExpression(scope);
        
    }
    
    /**
     * @throws BacktrackException
     */
    protected IASTTypeId typeId(IASTScope scope, boolean skipArrayModifiers ) throws EndOfFileException, BacktrackException
    {
    	IToken mark = mark();
    	ITokenDuple name = null;
    	boolean isConst = false, isVolatile = false; 
    	boolean isSigned = false, isUnsigned = false; 
    	boolean isShort = false, isLong = false;
    	boolean isTypename = false; 
    	
    	IASTSimpleTypeSpecifier.Type kind = null;
    	do
    	{
	        try
	        {
	            name  = name(scope);
	            kind = IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
	            break;
	        }
	        catch (BacktrackException b)
	        {
	        	// do nothing
	        }
	        
	        boolean encounteredType = false;
            simpleMods : for (;;)
            {
                switch (LT(1))
                {
					 case IToken.t_signed :
					 	consume(); 
					 	isSigned = true; 
					 	break;
					 	
					 case IToken.t_unsigned :
					 	consume(); 
					 	isUnsigned = true; 
					 	break;
					 	
					 case IToken.t_short :
					 	consume(); 
					 	isShort = true; 
					 	break;
					 	
					 case IToken.t_long :
					 	consume(); 
					 	isLong = true; 
					 	break;
					 
					 case IToken.t_const :
					 	consume(); 
					 	isConst = true; 
					 	break; 
					 	
					 case IToken.t_volatile :
					 	consume(); 
					 	isVolatile = true;
                        break;
                        
                    case IToken.tIDENTIFIER :
                    	if( encounteredType ) break simpleMods;
                    	encounteredType = true;
                        name = name(scope);
						kind = IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
                        break;
                        
                    case IToken.t_int :
						if( encounteredType ) break simpleMods;
						encounteredType = true;                    
                    	kind = IASTSimpleTypeSpecifier.Type.INT;
						consume();
                    	break;
                    	
                    case IToken.t_char :
						if( encounteredType ) break simpleMods;
						encounteredType = true;                    
						kind = IASTSimpleTypeSpecifier.Type.CHAR;
						consume();
						break;

                    case IToken.t_bool :
						if( encounteredType ) break simpleMods;
						encounteredType = true;                    
						kind = IASTSimpleTypeSpecifier.Type.BOOL;
						consume();
						break;
                    
                    case IToken.t_double :
						if( encounteredType ) break simpleMods;
						encounteredType = true;                    
						kind = IASTSimpleTypeSpecifier.Type.DOUBLE;
						consume();
						break;
                    
                    case IToken.t_float :
						if( encounteredType ) break simpleMods;
						encounteredType = true;                    
						kind = IASTSimpleTypeSpecifier.Type.FLOAT;
						consume();
						break;
                    
                    case IToken.t_wchar_t :
						if( encounteredType ) break simpleMods;
						encounteredType = true;                    
						kind = IASTSimpleTypeSpecifier.Type.WCHAR_T;
						consume();
						break;

                    
                    case IToken.t_void :
						if( encounteredType ) break simpleMods;
						encounteredType = true;                    
						kind = IASTSimpleTypeSpecifier.Type.VOID;
						consume();
						break;

					case IToken.t__Bool :
						if( encounteredType ) break simpleMods;
						encounteredType = true;                    
						kind = IASTSimpleTypeSpecifier.Type._BOOL;
						consume();
						break;
						
                    default :
                        break simpleMods;
                }
            }

			if( kind != null ) break;
			
			if( isShort || isLong || isUnsigned || isSigned )
			{
				kind = IASTSimpleTypeSpecifier.Type.INT;
				break;
			}
			
            if (
                LT(1) == IToken.t_typename
                    || LT(1) == IToken.t_struct
                    || LT(1) == IToken.t_class
                    || LT(1) == IToken.t_enum
                    || LT(1) == IToken.t_union)
            {
                consume();
                try
                {
                	name = name(scope);
                	kind = IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
                } catch( BacktrackException b )
                {
                	backup( mark );
                	throw backtrack; 
                }
            }
        
    	} while( false );
    	
    	if( kind == null )
    		throw backtrack;
    	
    	TypeId id = new TypeId(scope); 
    	IToken last = lastToken;
    	
		lastToken = consumeTemplateParameters( last );
		if( lastToken == null ) lastToken = last;
		
    	consumePointerOperators( id );
    	if( lastToken == null ) lastToken = last;
		
		if( ! skipArrayModifiers  )
		{
			last = lastToken; 
	    	consumeArrayModifiers( id, scope );
			if( lastToken == null ) lastToken = last;
		}
		
    	try
        {
            return astFactory.createTypeId( scope, kind, isConst, isVolatile, isShort, isLong, isSigned, isUnsigned, isTypename, name, id.getPointerOperators(), id.getArrayModifiers());
        }
        catch (ASTSemanticException e)
        {
            backup( mark );
            throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression deleteExpression( IASTScope scope )
        throws EndOfFileException, BacktrackException
    {    	
        if (LT(1) == IToken.tCOLONCOLON)
        {
            // global scope
            consume();
        }
        consume(IToken.t_delete);
        boolean vectored = false;
        if (LT(1) == IToken.tLBRACKET)
        {
            // array delete
            consume();
            consume(IToken.tRBRACKET);
            vectored = true;
        }
        IASTExpression castExpression = castExpression(scope);
        try
        {
            return astFactory.createExpression(
                scope,
                (vectored
                    ? IASTExpression.Kind.DELETE_VECTORCASTEXPRESSION
                    : IASTExpression.Kind.DELETE_CASTEXPRESSION),
                castExpression,
                null,
                null,
                null,
                null, "", null);
        }
        catch (ASTSemanticException e)
        {
            throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
    }
    /**
     * Pazse a new-expression.  
     * 
     * @param expression
     * @throws BacktrackException
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
    protected IASTExpression newExpression( IASTScope scope ) throws BacktrackException, EndOfFileException
    {
    	setCompletionValues(scope, CompletionKind.NEW_TYPE_REFERENCE, Key.EMPTY);
        if (LT(1) == IToken.tCOLONCOLON)
        {
            // global scope
            consume();
        }
        consume(IToken.t_new);
        boolean typeIdInParen = false;
        boolean placementParseFailure = true;
        IToken beforeSecondParen = null;
        IToken backtrackMarker = null;
        IASTTypeId typeId = null;
		ArrayList newPlacementExpressions = new ArrayList();
		ArrayList newTypeIdExpressions = new ArrayList();
		ArrayList newInitializerExpressions = new ArrayList();
				
        if (LT(1) == IToken.tLPAREN)
        {
            consume(IToken.tLPAREN);
            try
            {
                // Try to consume placement list
                // Note: since expressionList and expression are the same...
                backtrackMarker = mark();
				newPlacementExpressions.add(expression(scope));
                consume(IToken.tRPAREN);
                placementParseFailure = false;
                if (LT(1) == IToken.tLPAREN)
                {
                    beforeSecondParen = mark();
                    consume(IToken.tLPAREN);
                    typeIdInParen = true;
                }
            }
            catch (BacktrackException e)
            {
                backup(backtrackMarker);
            }
            if (placementParseFailure)
            {
                // CASE: new (typeid-not-looking-as-placement) ...
                // the first expression in () is not a placement
                // - then it has to be typeId
                typeId = typeId(scope, true );
                consume(IToken.tRPAREN);
            }
            else
            {
                if (!typeIdInParen)
                {
                    if (LT(1) == IToken.tLBRACKET)
                    {
                        // CASE: new (typeid-looking-as-placement) [expr]...
                        // the first expression in () has been parsed as a placement;
                        // however, we assume that it was in fact typeId, and this 
                        // new statement creates an array.
                        // Do nothing, fallback to array/initializer processing
                    }
                    else
                    {
                        // CASE: new (placement) typeid ...
                        // the first expression in () is parsed as a placement,
                        // and the next expression doesn't start with '(' or '['
                        // - then it has to be typeId
                        try
                        {
                            backtrackMarker = mark();
                            typeId = typeId(scope, true);
                        }
                        catch (BacktrackException e)
                        {
                            // Hmmm, so it wasn't typeId after all... Then it is
                            // CASE: new (typeid-looking-as-placement)
                            backup(backtrackMarker);
							// TODO fix this
                            return null; 
                        }
                    }
                }
                else
                {
                    // Tricky cases: first expression in () is parsed as a placement,
                    // and the next expression starts with '('.
                    // The problem is, the first expression might as well be a typeid
                    try
                    {
                        typeId = typeId(scope, true);
                        consume(IToken.tRPAREN);
                        if (LT(1) == IToken.tLPAREN
                            || LT(1) == IToken.tLBRACKET)
                        {
                            // CASE: new (placement)(typeid)(initializer)
                            // CASE: new (placement)(typeid)[] ...
                            // Great, so far all our assumptions have been correct
                            // Do nothing, fallback to array/initializer processing
                        }
                        else
                        {
                            // CASE: new (placement)(typeid)
                            // CASE: new (typeid-looking-as-placement)(initializer-looking-as-typeid)
                            // Worst-case scenario - this cannot be resolved w/o more semantic information.
                            // Luckily, we don't need to know what was that - we only know that 
                            // new-expression ends here.
							try
							{
								setCompletionValues(scope, CompletionKind.NO_SUCH_KIND, Key.EMPTY);
								return astFactory.createExpression(
									scope, IASTExpression.Kind.NEW_TYPEID, 
									null, null, null, typeId, null, 
									"", astFactory.createNewDescriptor(newPlacementExpressions, newTypeIdExpressions, newInitializerExpressions));
							}
							catch (ASTSemanticException e)
							{
								throw backtrack;
							} catch (Exception e)
                            {
                                throw backtrack;
                            }
                        }
                    }
                    catch (BacktrackException e)
                    {
                        // CASE: new (typeid-looking-as-placement)(initializer-not-looking-as-typeid)
                        // Fallback to initializer processing
                        backup(beforeSecondParen);
                    }
                }
            }
        }
        else
        {
            // CASE: new typeid ...
            // new parameters do not start with '('
            // i.e it has to be a plain typeId
            typeId = typeId(scope, true);
        }
        while (LT(1) == IToken.tLBRACKET)
        {
            // array new
            consume();
			newTypeIdExpressions.add(assignmentExpression(scope));
            consume(IToken.tRBRACKET);
        }
        // newinitializer
        if (LT(1) == IToken.tLPAREN)
        {
            consume(IToken.tLPAREN);
            if (LT(1) != IToken.tRPAREN)
			newInitializerExpressions.add(expression(scope));
            consume(IToken.tRPAREN);
        }
        setCompletionValues(scope, CompletionKind.NO_SUCH_KIND, Key.EMPTY);
		try
		{
        return astFactory.createExpression(
        	scope, IASTExpression.Kind.NEW_TYPEID, 
			null, null, null, typeId, null, 
			"", astFactory.createNewDescriptor(newPlacementExpressions, newTypeIdExpressions, newInitializerExpressions));
		}
		catch (ASTSemanticException e)
		{
			return null;
		} catch (Exception e)
        {
            throw backtrack;
        }
		
    }
    protected IASTExpression unaryOperatorCastExpression( IASTScope scope,
        IASTExpression.Kind kind)
        throws EndOfFileException, BacktrackException
    {
        IASTExpression castExpression = castExpression(scope);
        try
        {
            return astFactory.createExpression(
                scope,
                kind,
                castExpression,
                null,
                null,
                null,
                null, "", null);
        }
        catch (ASTSemanticException e)
        {
            throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression unaryExpression( IASTScope scope )
        throws EndOfFileException, BacktrackException
    {
        switch (LT(1))
        {
            case IToken.tSTAR :
            	consume();
                return unaryOperatorCastExpression(scope,
                    IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION);
            case IToken.tAMPER :
				consume();
                return unaryOperatorCastExpression(scope,
                    IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION);
            case IToken.tPLUS :
				consume();
                return unaryOperatorCastExpression(scope,
                    IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION);
            case IToken.tMINUS :
				consume();        
                return unaryOperatorCastExpression(scope,
                    IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION);
            case IToken.tNOT :
            	consume();
                return unaryOperatorCastExpression(scope,
                    IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION);
            case IToken.tCOMPL :
            	consume();
                return unaryOperatorCastExpression(scope,
                    IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION);
            case IToken.tINCR :
            	consume();
                return unaryOperatorCastExpression(scope,
                    IASTExpression.Kind.UNARY_INCREMENT);
            case IToken.tDECR :
            	consume();
                return unaryOperatorCastExpression(scope,
                    IASTExpression.Kind.UNARY_DECREMENT);
            case IToken.t_sizeof :
                consume(IToken.t_sizeof);
                IToken mark = LA(1);
                IASTTypeId d = null;
                IASTExpression unaryExpression = null;
                if (LT(1) == IToken.tLPAREN)
                {
                    try
                    {
                        consume(IToken.tLPAREN);
                        d = typeId(scope, false);
                        consume(IToken.tRPAREN);
                    }
                    catch (BacktrackException bt)
                    {
                        backup(mark);
                        unaryExpression = unaryExpression(scope);
                    }
                }
                else
                {
                    unaryExpression = unaryExpression(scope);
                }
                if (d != null & unaryExpression == null)
                    try
                    {
                        return astFactory.createExpression(
                            scope,
                            IASTExpression.Kind.UNARY_SIZEOF_TYPEID,
                            null,
                            null,
                            null,
                            d,
                            null, "", null);
                    }
                    catch (ASTSemanticException e)
                    {
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                else if (unaryExpression != null && d == null)
                    try
                    {
                        return astFactory.createExpression(
                            scope,
                            IASTExpression.Kind.UNARY_SIZEOF_UNARYEXPRESSION,
                            unaryExpression,
                            null,
                            null,
                            null,
                            null, "", null);
                    }
                    catch (ASTSemanticException e1)
                    {
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                else
                    throw backtrack;
            case IToken.t_new :
                return newExpression(scope);
            case IToken.t_delete :
                return deleteExpression(scope);
            case IToken.tCOLONCOLON :
                switch (LT(2))
                {
                    case IToken.t_new :
                        return newExpression(scope);
                    case IToken.t_delete :
                        return deleteExpression(scope);
                    default :
                        return postfixExpression(scope);
                }
            default :
                return postfixExpression(scope);
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression postfixExpression( IASTScope scope )
        throws EndOfFileException, BacktrackException
    {
        IASTExpression firstExpression = null;
        boolean isTemplate = false;
        checkEndOfFile();
        switch (LT(1))
        {
            case IToken.t_typename :
                consume(IToken.t_typename);
                ITokenDuple nestedName = name(scope);
				boolean templateTokenConsumed = false;
				if( LT(1) == IToken.t_template )
				{
				  consume( IToken.t_template ); 
				  templateTokenConsumed = true;
				}
				IToken current = mark(); 
				ITokenDuple templateId = null;
				try
				{
					templateId = new TokenDuple( current, templateId(scope) ); 
				}
				catch( BacktrackException bt )
				{
					if( templateTokenConsumed )
						throw bt;
					backup( current );
				}
                consume( IToken.tLPAREN ); 
                IASTExpression expressionList = expression( scope ); 
                consume( IToken.tRPAREN );
                try {
					firstExpression = 
						astFactory.createExpression( scope, 
													(( templateId != null )? IASTExpression.Kind.POSTFIX_TYPENAME_TEMPLATEID : IASTExpression.Kind.POSTFIX_TYPENAME_IDENTIFIER ), 
													expressionList, 
													null, 
													null, 
													null, 
													nestedName,
													"", 
													null );
				} catch (ASTSemanticException ase ) {
					throw backtrack;
				} catch (Exception e)
                {
                    throw backtrack;
                }
                break;                
                // simple-type-specifier ( assignment-expression , .. )
            case IToken.t_char :
                firstExpression =
                    simpleTypeConstructorExpression(scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_CHAR);
                break;
            case IToken.t_wchar_t :
                firstExpression =
                    simpleTypeConstructorExpression(scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_WCHART);
                break;
            case IToken.t_bool :
                firstExpression =
                    simpleTypeConstructorExpression(scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_BOOL);
                break;
            case IToken.t_short :
                firstExpression =
                    simpleTypeConstructorExpression(scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_SHORT);
                break;
            case IToken.t_int :
                firstExpression =
                    simpleTypeConstructorExpression(scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_INT);
                break;
            case IToken.t_long :
                firstExpression =
                    simpleTypeConstructorExpression(scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_LONG);
                break;
            case IToken.t_signed :
                firstExpression =
                    simpleTypeConstructorExpression(scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_SIGNED);
                break;
            case IToken.t_unsigned :
                firstExpression =
                    simpleTypeConstructorExpression(scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_UNSIGNED);
                break;
            case IToken.t_float :
                firstExpression =
                    simpleTypeConstructorExpression(scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_FLOAT);
                break;
            case IToken.t_double :
                firstExpression =
                    simpleTypeConstructorExpression( scope,
                        IASTExpression.Kind.POSTFIX_SIMPLETYPE_DOUBLE);
                break;
            case IToken.t_dynamic_cast :
                firstExpression =
                    specialCastExpression(scope,
                        IASTExpression.Kind.POSTFIX_DYNAMIC_CAST);
                break;
            case IToken.t_static_cast :
                firstExpression =
                    specialCastExpression(scope,
                        IASTExpression.Kind.POSTFIX_STATIC_CAST);
                break;
            case IToken.t_reinterpret_cast :
                firstExpression =
                    specialCastExpression(scope,
                        IASTExpression.Kind.POSTFIX_REINTERPRET_CAST);
                break;
            case IToken.t_const_cast :
                firstExpression =
                    specialCastExpression(scope,
                        IASTExpression.Kind.POSTFIX_CONST_CAST);
                break;
            case IToken.t_typeid :
                consume();
                consume(IToken.tLPAREN);
                boolean isTypeId = true;
                IASTExpression lhs = null;
                IASTTypeId typeId = null;
                try
                {
                    typeId = typeId(scope, false);
                }
                catch (BacktrackException b)
                {
                    isTypeId = false;
                    lhs = expression(scope);
                }
                consume(IToken.tRPAREN);
                try
                {
                    firstExpression =
                        astFactory.createExpression(
                            scope,
                            (isTypeId
                                ? IASTExpression.Kind.POSTFIX_TYPEID_TYPEID
                                : IASTExpression.Kind.POSTFIX_TYPEID_EXPRESSION),
                            lhs,
                            null,
                            null,
                            typeId,
                            null, "", null);
                }
                catch (ASTSemanticException e6)
                {
                    failParse();
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
                break;
            default :
                firstExpression = primaryExpression(scope);
        }
        IASTExpression secondExpression = null;
        for (;;)
        {
            switch (LT(1))
            {
                case IToken.tLBRACKET :
                    // array access
                    consume();
                    secondExpression = expression(scope);
                    consume(IToken.tRBRACKET);
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                IASTExpression.Kind.POSTFIX_SUBSCRIPT,
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e2)
                    {
                        failParse();
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                case IToken.tLPAREN :
                    // function call
                    consume(IToken.tLPAREN);
                    secondExpression = expression(scope);
                    consume(IToken.tRPAREN);
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                IASTExpression.Kind.POSTFIX_FUNCTIONCALL,
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e3)
                    {
                        failParse();
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                case IToken.tINCR :
                    consume();
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                IASTExpression.Kind.POSTFIX_INCREMENT,
                                firstExpression,
                                null,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e1)
                    {
                        failParse();
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                case IToken.tDECR :
                    consume();
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                IASTExpression.Kind.POSTFIX_DECREMENT,
                                firstExpression,
                                null,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e4)
                    {
                        failParse();
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                case IToken.tDOT :
                    // member access
                    consume(IToken.tDOT);
                	
                    if( queryLookaheadCapability() )
	                    if (LT(1) == IToken.t_template)
	                    {
	                        consume(IToken.t_template);
	                        isTemplate = true;
	                    }
            
		            setCompletionValues(scope, CompletionKind.MEMBER_REFERENCE,	KeywordSets.Key.EMPTY, firstExpression, isTemplate );
                    															
                    secondExpression = primaryExpression(scope);
                    checkEndOfFile();
                    
                    setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,	KeywordSets.Key.EMPTY );
                    
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                (isTemplate
                                    ? IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS
                                    : IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION),
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e5)
                    {
                        failParse();
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    } 
                    break;
                case IToken.tARROW :
                    // member access
                    consume(IToken.tARROW);
                    
                    if( queryLookaheadCapability() )
                    	if (LT(1) == IToken.t_template)
	                    {
	                        consume(IToken.t_template);
	                        isTemplate = true;
	                    }
                    
		            setCompletionValues(scope, CompletionKind.MEMBER_REFERENCE,	KeywordSets.Key.EMPTY, firstExpression, isTemplate );
                    															
                    secondExpression = primaryExpression(scope);
                    checkEndOfFile();                    

                    setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,	KeywordSets.Key.EMPTY );
                    try
                    {
                        firstExpression =
                            astFactory.createExpression(
                                scope,
                                (isTemplate
                                    ? IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP
                                    : IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION),
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, "", null);
                    }
                    catch (ASTSemanticException e)
                    {
                        failParse();
                        throw backtrack;
                    } catch (Exception e)
                    {
                        throw backtrack;
                    }
                    break;
                default :
                    return firstExpression;
            }
        }
    }
    
	/**
	 * @param scope
	 * @param kind
	 * @param key
	 * @param firstExpression
	 * @param isTemplate
	 */
	protected void setCompletionValues(IASTScope scope, CompletionKind kind, Key key, IASTExpression firstExpression, boolean isTemplate) throws EndOfFileException {
	}
	
	/**
	 * @return
	 * @throws EndOfFileException
	 */
	protected boolean queryLookaheadCapability( int count ) throws EndOfFileException {
		//make sure we can look ahead one before doing this
		boolean result = true;
		try
		{
			LA(count);
		}
		catch( EndOfFileException olre )
		{
			result = false;
		}
		return result;
	}
	
	protected boolean queryLookaheadCapability() throws EndOfFileException {
		return queryLookaheadCapability(1);
	}

	protected void checkEndOfFile() throws EndOfFileException
	{
		LA(1);
	}
		
	
	protected IASTExpression specialCastExpression( IASTScope scope,
        IASTExpression.Kind kind)
        throws EndOfFileException, BacktrackException
    {
        consume();
        consume(IToken.tLT);
        IASTTypeId duple = typeId(scope, false);
        consume(IToken.tGT);
        consume(IToken.tLPAREN);
        IASTExpression lhs = expression(scope);
        consume(IToken.tRPAREN);
        try
        {
            return astFactory.createExpression(
                scope,
                kind,
                lhs,
                null,
                null,
                duple,
                null, "", null);
        }
        catch (ASTSemanticException e)
        {
            throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
    }
    protected IASTExpression simpleTypeConstructorExpression( IASTScope scope,
        Kind type)
        throws EndOfFileException, BacktrackException
    {
        consume();
        consume(IToken.tLPAREN);
        IASTExpression inside = expression(scope);
        consume(IToken.tRPAREN);
        try
        {
            return astFactory.createExpression(
                scope,
                type,
                inside,
                null,
                null,
                null,
                null, "", null);
        }
        catch (ASTSemanticException e)
        {
            failParse();
            throw backtrack;
        } catch (Exception e)
        {
            throw backtrack;
        }
    }
    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression primaryExpression( IASTScope scope )
        throws EndOfFileException, BacktrackException
    {
        IToken t = null;
        switch (LT(1))
        {
            // TO DO: we need more literals...
            case IToken.tINTEGER :
                t = consume();
                try
                {
                    return astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.PRIMARY_INTEGER_LITERAL,
                        null,
                        null,
                        null,
                        null,
                        null, t.getImage(), null);
                }
                catch (ASTSemanticException e1)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
            case IToken.tFLOATINGPT :
                t = consume();
                try
                {
                    return astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.PRIMARY_FLOAT_LITERAL,
                        null,
                        null,
                        null,
                        null,
                        null, t.getImage(), null);
                }
                catch (ASTSemanticException e2)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
            case IToken.tSTRING :
            case IToken.tLSTRING :
				t = consume();
				try
                {
                    return astFactory.createExpression( scope, IASTExpression.Kind.PRIMARY_STRING_LITERAL, null, null, null, null, null, t.getImage(), null );
                }
                catch (ASTSemanticException e5)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
            
            case IToken.t_false :
            case IToken.t_true :
                t = consume();
                try
                {
                    return astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.PRIMARY_BOOLEAN_LITERAL,
                        null,
                        null,
                    	null,
                        null,
                        null, t.getImage(), null);
                }
                catch (ASTSemanticException e3)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
                  
            case IToken.tCHAR :
			case IToken.tLCHAR :

                t = consume();
                try
                {
                    return astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.PRIMARY_CHAR_LITERAL,
                        null,
                        null,
                        null,
                        null,
                        null, t.getImage(), null);
                }
                catch (ASTSemanticException e4)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
                    
            case IToken.t_this :
                consume(IToken.t_this);
                try
                {
                    return astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.PRIMARY_THIS,
                        null,
                        null,
                        null,
                        null,
                        null, "", null);
                }
                catch (ASTSemanticException e7)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
            case IToken.tLPAREN :
                consume();
                IASTExpression lhs = expression(scope);
                consume(IToken.tRPAREN);
                try
                {
                    return astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.PRIMARY_BRACKETED_EXPRESSION,
                        lhs,
                        null,
                        null,
                        null,
                        null, "", null);
                }
                catch (ASTSemanticException e6)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
            case IToken.tIDENTIFIER :
            case IToken.tCOLONCOLON :
            case IToken.t_operator : 
                ITokenDuple duple = null; 
                
                IToken mark = mark();
                try
                {
					duple = name(scope);
                }
                catch( BacktrackException bt )
                {
                	Declarator d = new Declarator( new DeclarationWrapper(scope, mark.getOffset(), mark.getLineNumber(), null) );

					if (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER)
					{
						IToken start = consume();
						IToken end = null;
						if (start.getType() == IToken.tIDENTIFIER)
							 end = consumeTemplateParameters(end);
						while (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER)
						{
						   end = consume();
						   if (end.getType() == IToken.tIDENTIFIER)
						      end = consumeTemplateParameters(end);
						}
						if (LT(1) == IToken.t_operator)
							operatorId(d, start);
						else
						{
						   backup(mark);
						   throw backtrack;
						}
					 }
					 else if( LT(1) == IToken.t_operator )
					 	 operatorId( d, null);
					 
					 duple = d.getNameDuple();
                }
                catch(OffsetLimitReachedException olre )
				{
                	backup(mark);
                	throw backtrack;
                }
                                
                checkEndOfFile();
                try
                {
                    return astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.ID_EXPRESSION,
                        null,
                        null,
                    	null,
						null,
                        duple, "", null);
                }
                catch (ASTSemanticException e8)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                    throw backtrack;
                }
            default :
				IASTExpression empty = null;
		        try {
					empty = astFactory.createExpression(
							scope,
							IASTExpression.Kind.PRIMARY_EMPTY,
							null,
							null,
							null,
							null,
							null, "", null);
				} catch (ASTSemanticException e9) {
					// TODO Auto-generated catch block
					e9.printStackTrace();
					
				}
				return empty;
        }
		
    }
    /**
     * @throws Exception
     */
    protected void varName() throws Exception
    {
        if (LT(1) == IToken.tCOLONCOLON)
            consume();
        for (;;)
        {
            switch (LT(1))
            {
                case IToken.tIDENTIFIER :
                    consume();
                    //if (isTemplateArgs()) {
                    //	rTemplateArgs();
                    //}
                    if (LT(1) == IToken.tCOLONCOLON)
                    {
                        switch (LT(2))
                        {
                            case IToken.tIDENTIFIER :
                            case IToken.tCOMPL :
                            case IToken.t_operator :
                                consume();
                                break;
                            default :
                                return;
                        }
                    }
                    else
                        return;
                    break;
                case IToken.tCOMPL :
                    consume();
                    consume(IToken.tIDENTIFIER);
                    return;
                case IToken.t_operator :
                    consume();
                    //rOperatorName();
                    return;
                default :
                    throw backtrack;
            }
        }
    }

    // the static instance we always use
    protected static BacktrackException backtrack = new BacktrackException();

    // Token management    
    protected IScanner scanner;
    protected IToken currToken, // current token we plan to consume next 
    lastToken; // last token we consumed
	private boolean limitReached = false;
	protected IASTCompilationUnit compilationUnit;
    
    /**
     * Fetches a token from the scanner. 
     * 
     * @return				the next token from the scanner
     * @throws EndOfFileException	thrown when the scanner.nextToken() yields no tokens
     */
    protected IToken fetchToken() throws EndOfFileException
    {
    	if(limitReached) throw new EndOfFileException();
    	
        try
        {
        	IToken value = scanner.nextToken(); 
        	handleNewToken( value );
            return value; 
        }
        catch( OffsetLimitReachedException olre )
        {
        	limitReached = true;
        	handleOffsetLimitException(olre);
        	return null;
        }
        catch (ScannerException e)
        {
            log.traceLog( "ScannerException thrown : " + e.getProblem().getMessage() );
			log.errorLog( "Scanner Exception: " + e.getProblem().getMessage()); //$NON-NLS-1$h
            failParse(); 
            return fetchToken();
        }
    }

    /**
	 * @param value
	 */
	protected void handleNewToken(IToken value) throws EndOfFileException {
	}
	
	protected void handleOffsetLimitException(OffsetLimitReachedException exception) throws EndOfFileException {
		// unexpected, throw EOF instead (equivalent)
		throw new EndOfFileException();
	}
	/**
     * Look Ahead in the token list to see what is coming.  
     * 
     * @param i		How far ahead do you wish to peek?
     * @return		the token you wish to observe
     * @throws EndOfFileException	if looking ahead encounters EOF, throw EndOfFile 
     */
    protected IToken LA(int i) throws EndOfFileException
    {
        if (i < 1) // can't go backwards
            return null;
        if (currToken == null)
            currToken = fetchToken();
        IToken retToken = currToken;
        for (; i > 1; --i)
        {
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
     * @throws EndOfFileException	if looking ahead encounters EOF, throw EndOfFile
     */
    protected int LT(int i) throws EndOfFileException
    {
        return LA(i).getType();
    }
    /**
     * Consume the next token available, regardless of the type.  
     * 
     * @return				The token that was consumed and removed from our buffer.  
     * @throws EndOfFileException	If there is no token to consume.  
     */
    protected IToken consume() throws EndOfFileException
    {
        if (currToken == null)
            currToken = fetchToken();
        if (currToken != null)
            lastToken = currToken;
        currToken = currToken.getNext();
        return lastToken;
    }
    /**
     * Consume the next token available only if the type is as specified.  
     * 
     * @param type			The type of token that you are expecting.  	
     * @return				the token that was consumed and removed from our buffer. 
     * @throws BacktrackException	If LT(1) != type 
     */
    protected IToken consume(int type) throws EndOfFileException, BacktrackException
    {
        if (LT(1) == type)
            return consume();
        else
            throw backtrack;
    }
    /**
     * Mark our place in the buffer so that we could return to it should we have to.  
     * 
     * @return				The current token. 
     * @throws EndOfFileException	If there are no more tokens.
     */
    protected IToken mark() throws EndOfFileException
    {
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
    protected void backup(IToken mark)
    {
        currToken = (Token)mark;
        lastToken = null; // this is not entirely right ... 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IParser#getLanguage()
     */
    public ParserLanguage getLanguage()
    {
        return language;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IParser#setLanguage(Language)
     */
    public void setLanguage( ParserLanguage l )
    {
        language = l;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IParser#getLastErrorOffset()
     */
    public int getLastErrorOffset()
    {
        return firstErrorOffset;
    }
    
    protected void setCompletionValues( IASTScope scope, IASTCompletionNode.CompletionKind kind, KeywordSets.Key key )throws EndOfFileException 
	{	
    }
    
    protected void setCompletionValues( IASTScope scope, IASTCompletionNode.CompletionKind kind, KeywordSets.Key key, IASTNode node, String prefix )throws EndOfFileException 
	{	
    }
    
    protected void setCompletionToken( IToken token )
	{
    }
    
    protected IToken getCompletionToken()
	{
    	return null;
    }
}
