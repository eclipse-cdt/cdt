/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceAlias;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableElement;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
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
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;

/**
 * This is our first implementation of the IParser interface, serving as a parser for
 * ANSI C and C++.
 * 
 * From time to time we will make reference to the ANSI ISO specifications.
 * 
 * @author jcamelon
 */
public abstract class Parser extends ExpressionParser implements IParser 
{
    private static final int DEFAULT_DESIGNATOR_LIST_SIZE = 4;
	protected ISourceElementRequestor requestor = null;
    private IProblemFactory problemFactory = new ParserProblemFactory();
    /**
     * This is the standard cosntructor that we expect the Parser to be instantiated 
     * with.  
     * 
     */
    public Parser(
        IScanner scanner,
        ISourceElementRequestor callback,
        ParserLanguage language,
        IParserLogService log, IParserExtension extension )
    {
    	super( scanner, language, log, extension );
    	requestor = callback;
    }
    
    
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#failParse()
	 */
	protected void failParse() {
		IToken referenceToken = null;
		if( lastToken != null )
			referenceToken = lastToken;
		else
			try
			{
				referenceToken = LA(1);
			}
			catch( EndOfFileException eof )
			{
				return;
			}
		IProblem problem = problemFactory.createProblem( 
				IProblem.SYNTAX_ERROR, 
				referenceToken.getOffset(), 
				referenceToken.getOffset(), 
				referenceToken.getLineNumber(), 
				scanner.getCurrentFilename(), 
				EMPTY_STRING, 
				false, 
				true );
		requestor.acceptProblem( problem );
		super.failParse();
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
            "Parse " //$NON-NLS-1$
                + (++parseCount)
                + ": " //$NON-NLS-1$
                + (System.currentTimeMillis() - startTime)
                + "ms" //$NON-NLS-1$
                + (parsePassed ? "" : " - parse failure") ); //$NON-NLS-1$ //$NON-NLS-2$
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
        	logException( "translationUnit::createCompilationUnit()", e2 ); //$NON-NLS-1$
            return;
        }

		compilationUnit.enterScope( requestor, astFactory.getReferenceManager() );
		try {
			setCompletionValues(compilationUnit, CompletionKind.VARIABLE_TYPE, KeywordSetKey.DECLARATION );
		} catch (EndOfFileException e1) {
			compilationUnit.exitScope( requestor, astFactory.getReferenceManager() );
			return;
		}
		
        int lastBacktrack = -1;
        
        while (true)
        {
            try
            {
                int checkOffset = LA(1).hashCode();
                declaration(compilationUnit, null, null, KeywordSetKey.DECLARATION);
                if (LA(1).hashCode() == checkOffset)
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
                    if (lastBacktrack != -1 && lastBacktrack == LA(1).hashCode())
                    {
                        // we haven't progressed from the last backtrack
                        // try and find tne next definition
                        errorHandling();
                    }
                    else
                    {
                        // start again from here
                        lastBacktrack = LA(1).hashCode();
                    }
                }
                catch (EndOfFileException e)
                {
                    break;
                }
            }
            catch( Exception e )
			{
            	logException( "translationUnit", e ); //$NON-NLS-1$
            	failParse();
			}
            catch( ParseError perr )
			{
            	throw perr;
			}
            catch (Throwable e)
            {
            	logThrowable( "translationUnit", e ); //$NON-NLS-1$
				failParse();
            }
        }
        compilationUnit.exitScope( requestor, astFactory.getReferenceManager() );
    }
    /**
	 * @param string
	 * @param e
	 */
	private void logThrowable(String methodName, Throwable e) {
		if( e != null )
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append( "Parser: Unexpected throwable in "); //$NON-NLS-1$
			buffer.append( methodName );
			buffer.append( ":"); //$NON-NLS-1$
			buffer.append( e.getClass().getName() );
			buffer.append( "::"); //$NON-NLS-1$
			buffer.append( e.getMessage() );
			buffer.append( ". w/"); //$NON-NLS-1$
			buffer.append( scanner.toString() );
			if( log.isTracing() )
				log.traceLog( buffer.toString() );
			log.errorLog( buffer.toString() );
		}
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
        int depth = ( LT(1) == IToken.tLBRACE ) ? 1 : 0;
        consume();    
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
            if( depth < 0 )
            	return;
            
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
     * @return TODO
     * @throws BacktrackException	request for a backtrack
     */
    protected IASTDeclaration usingClause(IASTScope scope)
        throws EndOfFileException, BacktrackException
    {
        IToken firstToken = consume(IToken.t_using);
        setCompletionValues(scope, CompletionKind.TYPE_REFERENCE, KeywordSetKey.POST_USING );
        
        if (LT(1) == IToken.t_namespace)
        {
            // using-directive
            consume(IToken.t_namespace);
            
            setCompletionValues(scope, CompletionKind.NAMESPACE_REFERENCE, KeywordSetKey.EMPTY );
            // optional :: and nested classes handled in name
            ITokenDuple duple = null;
            if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON)
                duple = name(scope, CompletionKind.NAMESPACE_REFERENCE, KeywordSetKey.EMPTY);
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
                	logException( "usingClause:createUsingDirective", e1 ); //$NON-NLS-1$
                    throw backtrack;
                }
                astUD.acceptElement(requestor, astFactory.getReferenceManager());
                return astUD;
            }
            throw backtrack;
        }
        boolean typeName = false;
        setCompletionValues(scope, CompletionKind.TYPE_REFERENCE, KeywordSetKey.POST_USING );
        
        if (LT(1) == IToken.t_typename)
        {
            typeName = true;
            consume(IToken.t_typename);
        }

        setCompletionValues(scope, CompletionKind.TYPE_REFERENCE, KeywordSetKey.NAMESPACE_ONLY );
        ITokenDuple name = null;
        if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON)
        {
            //	optional :: and nested classes handled in name
            name = name(scope, CompletionKind.TYPE_REFERENCE, KeywordSetKey.POST_USING);
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
            	logException( "usingClause:createUsingDeclaration", e1 ); //$NON-NLS-1$
                throw backtrack;
            }
            declaration.acceptElement( requestor, astFactory.getReferenceManager() );
            setCompletionValues(scope, getCompletionKindForDeclaration(scope, null), KeywordSetKey.DECLARATION );
            return declaration;
        }
            throw backtrack;
    
    }
    /**
     * Implements Linkage specification in the ANSI C++ grammar. 
     * 
     * linkageSpecification
     * : extern "string literal" declaration
     * | extern "string literal" { declaration-seq } 
     * 
     * @param container Callback object representing the scope these definitions fall into.
     * @return TODO
     * @throws BacktrackException	request for a backtrack
     */
    protected IASTDeclaration linkageSpecification(IASTScope scope)
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
            	logException( "linkageSpecification_1:createLinkageSpecification", e ); //$NON-NLS-1$
                throw backtrack;
            }
            
            linkage.enterScope( requestor, astFactory.getReferenceManager() );    
            linkageDeclarationLoop : while (LT(1) != IToken.tRBRACE)
            {
                int checkToken = LA(1).hashCode();
                switch (LT(1))
                {
                    case IToken.tRBRACE :
                        consume(IToken.tRBRACE);
                        break linkageDeclarationLoop;
                    default :
                        try
                        {
                            declaration(linkage, null, null, KeywordSetKey.DECLARATION);
                        }
                        catch (BacktrackException bt)
                        {
                            failParse();
                            if (checkToken == LA(1).hashCode())
                                errorHandling();
                        }
                }
                if (checkToken == LA(1).hashCode())
                    errorHandling();
            }
            // consume the }
            IToken lastTokenConsumed = consume();
            linkage.setEndingOffsetAndLineNumber(lastTokenConsumed.getEndOffset(), lastTokenConsumed.getLineNumber());
            linkage.exitScope( requestor, astFactory.getReferenceManager() );
            return linkage;
        }
        // single declaration

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
        	logException( "linkageSpecification_2:createLinkageSpecification", e ); //$NON-NLS-1$
            throw backtrack;
        }
		linkage.enterScope( requestor, astFactory.getReferenceManager() );
        declaration(linkage, null, null, KeywordSetKey.DECLARATION);
		linkage.exitScope( requestor, astFactory.getReferenceManager() );
		return linkage;

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
     * @return TODO
     * @throws BacktrackException		request for a backtrack
     */
    protected IASTDeclaration templateDeclaration(IASTScope scope)
        throws EndOfFileException, BacktrackException
    {
    	IToken mark = mark();
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
            	logException( "templateDeclaration:createTemplateInstantiation", e ); //$NON-NLS-1$
            	backup( mark );
                throw backtrack;
            }
            templateInstantiation.enterScope( requestor, astFactory.getReferenceManager() );
            declaration(templateInstantiation, templateInstantiation, null, KeywordSetKey.DECLARATION);
            templateInstantiation.setEndingOffsetAndLineNumber(lastToken.getEndOffset(), lastToken.getLineNumber());
			templateInstantiation.exitScope( requestor, astFactory.getReferenceManager() );
 
            return templateInstantiation;
        }
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
            	logException( "templateDeclaration:createTemplateSpecialization", e ); //$NON-NLS-1$
            	backup( mark );
                throw backtrack;
            }
			templateSpecialization.enterScope(requestor, astFactory.getReferenceManager());
            declaration(templateSpecialization, templateSpecialization, null, KeywordSetKey.DECLARATION);
            templateSpecialization.setEndingOffsetAndLineNumber(
                lastToken.getEndOffset(), lastToken.getLineNumber());
            templateSpecialization.exitScope(requestor, astFactory.getReferenceManager());
            return templateSpecialization;
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
            	logException( "templateDeclaration:createTemplateDeclaration", e ); //$NON-NLS-1$
                throw backtrack;
            }
            templateDecl.enterScope( requestor, astFactory.getReferenceManager() );
            try{
            	declaration(templateDecl, templateDecl, null, KeywordSetKey.DECLARATION );
            } catch( EndOfFileException e ){
            	templateDecl.setEndingOffsetAndLineNumber( lastToken.getEndOffset(), lastToken.getLineNumber() );
    			templateDecl.exitScope( requestor, astFactory.getReferenceManager() );
    			throw e;
            }
            templateDecl.setEndingOffsetAndLineNumber( lastToken.getEndOffset(), lastToken.getLineNumber() );
			templateDecl.exitScope( requestor, astFactory.getReferenceManager() );
			return templateDecl;
        }
        catch (BacktrackException bt)
        {
        	backup( mark );
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
 
        IASTScope parameterScope = astFactory.createNewCodeBlock( scope );
        if( parameterScope == null )
        	parameterScope = scope;
        
        for (;;)
        {
            if (LT(1) == IToken.tGT)
                return returnValue;
            if (LT(1) == IToken.t_class || LT(1) == IToken.t_typename)
            {
                IASTTemplateParameter.ParamKind kind = (consume().getType() == IToken.t_class)
                                                       ? IASTTemplateParameter.ParamKind.CLASS
                                                       : IASTTemplateParameter.ParamKind.TYPENAME;
                IToken startingToken = lastToken;				
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
                            typeId = typeId(parameterScope, false, CompletionKind.TYPE_REFERENCE); // type-id
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
                    		( id == null )? "" : id.getImage(), //$NON-NLS-1$
                    		typeId,
                    		null,
                    		null,
							( parameterScope instanceof IASTCodeScope ) ? (IASTCodeScope) parameterScope : null,
							startingToken.getOffset(), startingToken.getLineNumber(), 
							(id != null) ? id.getOffset() : 0, 
							(id != null) ? id.getEndOffset() : 0, 
							(id != null) ? id.getLineNumber() : 0,
							lastToken.getEndOffset(), lastToken.getLineNumber() ));
                }
                catch (Exception e)
                {
                	logException( "templateParameterList_1:createTemplateParameter", e ); //$NON-NLS-1$
                    throw backtrack;
                }

            }
            else if (LT(1) == IToken.t_template)
            {
                consume(IToken.t_template);
                IToken startingToken = lastToken;
                consume(IToken.tLT);

                List subResult = templateParameterList(parameterScope);
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
                        optionalTypeId = typeId(parameterScope, false, CompletionKind.TYPE_REFERENCE);
    
                    }
                }
 
                try
                {
                    returnValue.add(
                        astFactory.createTemplateParameter(
                            IASTTemplateParameter.ParamKind.TEMPLATE_LIST,
                            ( optionalId == null )? "" : optionalId.getImage(), //$NON-NLS-1$
                            optionalTypeId,
                            null,
                            subResult, 
							( parameterScope instanceof IASTCodeScope ) ? (IASTCodeScope) parameterScope : null,
							startingToken.getOffset(), startingToken.getLineNumber(), 
							(optionalId != null) ? optionalId.getOffset() : 0, 
							(optionalId != null) ? optionalId.getEndOffset() : 0, 
							(optionalId != null) ? optionalId.getLineNumber() : 0,
							lastToken.getEndOffset(), lastToken.getLineNumber() ));
                }
                catch (Exception e)
                {
                	logException( "templateParameterList_2:createTemplateParameter", e ); //$NON-NLS-1$
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
                parameterDeclaration(c, parameterScope);
                DeclarationWrapper wrapper = (DeclarationWrapper)c.getParameters().get(0);
                Declarator declarator = (Declarator)wrapper.getDeclarators().next();
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
                                null, null, 
								declarator.getName(), 
                                declarator.getInitializerClause(), 
								wrapper.getStartingOffset(), wrapper.getStartingLine(), 
								declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), 
								wrapper.getEndOffset(), wrapper.getEndLine()),
                            null, 
							( parameterScope instanceof IASTCodeScope ) ? (IASTCodeScope) parameterScope : null,
							wrapper.getStartingOffset(), wrapper.getStartingLine(), 
							declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), 
							wrapper.getEndOffset(), wrapper.getEndLine() ));
                }
                catch (Exception e)
                {
                	logException( "templateParameterList:createParameterDeclaration", e ); //$NON-NLS-1$
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
     * @param overideKey TODO
     * @param container		IParserCallback object which serves as the owner scope for this declaration.  
     * 
     * @throws BacktrackException	request a backtrack
     */
    protected void declaration(
        IASTScope scope,
        IASTTemplate ownerTemplate, CompletionKind overideKind, KeywordSetKey overideKey)
        throws EndOfFileException, BacktrackException
    {
    	
    	IASTCompletionNode.CompletionKind kind = getCompletionKindForDeclaration(scope, overideKind);
    	setCompletionValues(scope, kind, overideKey);
    	IASTDeclaration resultDeclaration = null;
    	switch (LT(1))
        {
            case IToken.t_asm :
                IToken first = consume(IToken.t_asm);
                setCompletionValues( scope, CompletionKind.NO_SUCH_KIND, KeywordSetKey.EMPTY );
                consume(IToken.tLPAREN);
                String assembly = consume(IToken.tSTRING).getImage();
                consume(IToken.tRPAREN);
                IToken last = consume(IToken.tSEMI);
                
                try
                {
                    resultDeclaration  =
                        astFactory.createASMDefinition(
                            scope,
                            assembly,
                            first.getOffset(),
                            first.getLineNumber(), last.getEndOffset(), last.getLineNumber());
                }
                catch (Exception e)
                {
                	logException( "declaration:createASMDefinition", e ); //$NON-NLS-1$
                    throw backtrack;
                }
                // if we made it this far, then we have all we need 
                // do the callback
 				resultDeclaration.acceptElement(requestor, astFactory.getReferenceManager());
 				setCompletionValues(scope, kind, KeywordSetKey.DECLARATION );
                break;
            case IToken.t_namespace :
                resultDeclaration = namespaceDefinition(scope);
                break;
            case IToken.t_using :
                resultDeclaration = usingClause(scope);
            	break;
            case IToken.t_export :
            case IToken.t_template :
                resultDeclaration = templateDeclaration(scope);
                break;
            case IToken.t_extern :
                if (LT(2) == IToken.tSTRING)
                {
                    resultDeclaration = linkageSpecification(scope);
                    break;
                }
            default :
                resultDeclaration = simpleDeclarationStrategyUnion(scope, ownerTemplate, overideKind, overideKey);
        }
    	setCompletionValues(scope, kind, KeywordSetKey.DECLARATION );
    	endDeclaration( resultDeclaration );
    }
    
    /**
	 * @param scope
	 * @return
	 */
	protected IASTCompletionNode.CompletionKind getCompletionKindForDeclaration(IASTScope scope, CompletionKind overide) {
		return null;
	}
	
	protected IASTDeclaration simpleDeclarationStrategyUnion(
        IASTScope scope,
        IASTTemplate ownerTemplate, CompletionKind overrideKind, KeywordSetKey overrideKey)
        throws EndOfFileException, BacktrackException
    {
        simpleDeclarationMark = mark();
		try
        {
            return simpleDeclaration(
                SimpleDeclarationStrategy.TRY_CONSTRUCTOR,
                scope,
                ownerTemplate, overrideKind, false, overrideKey);
            // try it first with the original strategy
        }
        catch (BacktrackException bt)
        {
        	if( simpleDeclarationMark == null )
        		throw backtrack;
            // did not work 
            backup(simpleDeclarationMark);
            
            try
            {  
            	return simpleDeclaration(
                	SimpleDeclarationStrategy.TRY_FUNCTION,
	                scope,
    	            ownerTemplate, overrideKind, false, overrideKey);
            }
            catch( BacktrackException bt2 )
            {
            	if( simpleDeclarationMark == null )
            		throw backtrack;

            	backup( simpleDeclarationMark ); 

				try
				{
					return simpleDeclaration(
						SimpleDeclarationStrategy.TRY_VARIABLE,
						scope,
						ownerTemplate, overrideKind, false, overrideKey);
				}
				catch( BacktrackException b3 )
				{
					backup( simpleDeclarationMark );
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
     * @return TODO
     * @throws BacktrackException	request a backtrack
    
     */
    protected IASTDeclaration namespaceDefinition(IASTScope scope)
        throws BacktrackException, EndOfFileException
    {
        IToken first = consume(IToken.t_namespace);
 
        IASTCompletionNode.CompletionKind kind = getCompletionKindForDeclaration(scope, null);
        
        setCompletionValues(scope,CompletionKind.NAMESPACE_REFERENCE, KeywordSetKey.EMPTY );
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
                        (identifier == null ? "" : identifier.getImage()), //$NON-NLS-1$
                        first.getOffset(),
                        first.getLineNumber(), 
                        (identifier == null ? first.getOffset() : identifier.getOffset()), 
						(identifier == null ? first.getEndOffset() : identifier.getEndOffset() ),  
						(identifier == null ? first.getLineNumber() : identifier.getLineNumber() ));
            }
            catch (Exception e1)
            {
            	logException( "namespaceDefinition:createNamespaceDefinition", e1 ); //$NON-NLS-1$
                throw backtrack;
            }
            namespaceDefinition.enterScope( requestor, astFactory.getReferenceManager() );
            setCompletionValues(scope,CompletionKind.VARIABLE_TYPE, KeywordSetKey.DECLARATION );
            namespaceDeclarationLoop : while (LT(1) != IToken.tRBRACE)
            {
                int checkToken = LA(1).hashCode();
                switch (LT(1))
                {
                    case IToken.tRBRACE :
                        //consume(Token.tRBRACE);
                        break namespaceDeclarationLoop;
                    default :
                        try
                        {
                            declaration(namespaceDefinition, null, null, KeywordSetKey.DECLARATION);
                        }
                        catch (BacktrackException bt)
                        {
                            failParse();
                            if (checkToken == LA(1).hashCode())
                                errorHandling();
                        }
                }
                if (checkToken == LA(1).hashCode())
                    errorHandling();
            }
            setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY );
            // consume the }
            IToken last = consume(IToken.tRBRACE);
 
            namespaceDefinition.setEndingOffsetAndLineNumber(
                last.getOffset() + last.getLength(), last.getLineNumber());
            setCompletionValues(scope, kind, KeywordSetKey.DECLARATION );
            namespaceDefinition.exitScope( requestor, astFactory.getReferenceManager() );
            return namespaceDefinition;
        }
        else if( LT(1) == IToken.tASSIGN )
        {
        	setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY);
        	consume( IToken.tASSIGN );
        	
			if( identifier == null )
				throw backtrack;

        	ITokenDuple duple = name(scope, CompletionKind.NAMESPACE_REFERENCE, KeywordSetKey.EMPTY);
        	consume( IToken.tSEMI );
        	setCompletionValues(scope, kind, KeywordSetKey.DECLARATION );
        	IASTNamespaceAlias alias = null;
        	try
            {
                alias = astFactory.createNamespaceAlias( 
                	scope, identifier.getImage(), duple, first.getOffset(), 
                	first.getLineNumber(), identifier.getOffset(), identifier.getEndOffset(), identifier.getLineNumber(), duple.getLastToken().getEndOffset(), duple.getLastToken().getLineNumber() );
            }
            catch (Exception e1)
            {
            	logException( "namespaceDefinition:createNamespaceAlias", e1 ); //$NON-NLS-1$
                throw backtrack;
            }
            return alias;
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
     * @return TODO
     * @throws BacktrackException		request a backtrack
     */
    protected IASTDeclaration simpleDeclaration(
        SimpleDeclarationStrategy strategy,
        IASTScope scope,
        IASTTemplate ownerTemplate, CompletionKind overideKind, boolean fromCatchHandler, KeywordSetKey overrideKey)
        throws BacktrackException, EndOfFileException
    {
    	IToken firstToken = LA(1);
    	if( firstToken.getType()  == IToken.tLBRACE ) throw backtrack;
        DeclarationWrapper sdw =
            new DeclarationWrapper(scope, firstToken.getOffset(), firstToken.getLineNumber(), ownerTemplate);
        firstToken = null; // necessary for scalability

        CompletionKind completionKindForDeclaration = getCompletionKindForDeclaration(scope, overideKind);
		setCompletionValues( scope, completionKindForDeclaration, KeywordSetKey.DECL_SPECIFIER_SEQUENCE );
        declSpecifierSeq(sdw, false, strategy == SimpleDeclarationStrategy.TRY_CONSTRUCTOR, completionKindForDeclaration, overrideKey );
        IASTSimpleTypeSpecifier simpleTypeSpecifier = null;
        if (sdw.getTypeSpecifier() == null && sdw.getSimpleType() != IASTSimpleTypeSpecifier.Type.UNSPECIFIED )
            try
            {
                simpleTypeSpecifier = astFactory.createSimpleTypeSpecifier(
                        scope,
                        sdw.getSimpleType(),
                        sdw.getName(),
                        sdw.isShort(),
                        sdw.isLong(),
                        sdw.isSigned(),
                        sdw.isUnsigned(), 
						sdw.isTypeNamed(), 
						sdw.isComplex(), 
						sdw.isImaginary(),
						sdw.isGloballyQualified(), sdw.getExtensionParameters());
				sdw.setTypeSpecifier(
                    simpleTypeSpecifier);
                sdw.setTypeName( null );
            }
            catch (Exception e1)
            {
            	logException( "simpleDeclaration:createSimpleTypeSpecifier", e1 ); //$NON-NLS-1$
                throw backtrack;
            }
        
        try {
			Declarator declarator = null;
			if (LT(1) != IToken.tSEMI)
			{
			    declarator = initDeclarator(sdw, strategy, completionKindForDeclaration, constructInitializersInDeclarations
			    		);
			        
			    while (LT(1) == IToken.tCOMMA)
			    {
			        consume();
			        initDeclarator(sdw, strategy, completionKindForDeclaration, constructInitializersInDeclarations );
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
			        declarator.setHasFunctionBody(true);
			        hasFunctionBody = true;
			    }
			    			    
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
			catch( Exception e )
			{
				logException( "simpleDecl", e ); //$NON-NLS-1$
				throw backtrack;
			}
			
			if (hasFunctionBody && l.size() != 1)
			{
			    throw backtrack; //TODO Should be an IProblem
			}
			if (!l.isEmpty()) // no need to do this unless we have a declarator
			{
			    if (!hasFunctionBody || fromCatchHandler)
			    {
			    	IASTDeclaration declaration = null;
			        for( int i = 0; i < l.size(); ++i )
			        {
			            declaration = (IASTDeclaration)l.get(i);
			            ((IASTOffsetableElement)declaration).setEndingOffsetAndLineNumber(
			                lastToken.getEndOffset(), lastToken.getLineNumber());
			            declaration.acceptElement( requestor, astFactory.getReferenceManager() );
			            if( sdw.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier )
			            	((IASTSimpleTypeSpecifier)sdw.getTypeSpecifier()).releaseReferences( astFactory.getReferenceManager() );

			        }
			        return declaration;
			    }
			    IASTDeclaration declaration = (IASTDeclaration)l.get(0);
			    endDeclaration( declaration );
			    declaration.enterScope( requestor, astFactory.getReferenceManager() );
			    if( sdw.getTypeSpecifier() instanceof IASTSimpleTypeSpecifier )
			    	((IASTSimpleTypeSpecifier)sdw.getTypeSpecifier()).releaseReferences( astFactory.getReferenceManager() );

				if ( !( declaration instanceof IASTScope ) ) 
					throw backtrack;
 
				handleFunctionBody((IASTScope)declaration );
				((IASTOffsetableElement)declaration).setEndingOffsetAndLineNumber(
					lastToken.getEndOffset(), lastToken.getLineNumber());
  
				declaration.exitScope( requestor, astFactory.getReferenceManager() );
					
				if( hasFunctionTryBlock )
					catchHandlerSequence( scope );
					
				return declaration;
					
			}
				
			try
			{
				if( sdw.getTypeSpecifier() != null )
				{
			   		IASTAbstractTypeSpecifierDeclaration declaration = astFactory.createTypeSpecDeclaration(
			                sdw.getScope(),
			                sdw.getTypeSpecifier(),
			                ownerTemplate,
			                sdw.getStartingOffset(),
			                sdw.getStartingLine(), lastToken.getEndOffset(), lastToken.getLineNumber(),
							sdw.isFriend());
					declaration.acceptElement(requestor, astFactory.getReferenceManager());
					return declaration;
				}
			}
			catch (Exception e1)
			{
				logException( "simpleDeclaration:createTypeSpecDeclaration", e1 ); //$NON-NLS-1$
			    throw backtrack;
			}

			return null;
		} catch( BacktrackException be ) 
		{
			if( simpleTypeSpecifier != null )
				simpleTypeSpecifier.releaseReferences(astFactory.getReferenceManager());
			throw be;
		}
		catch( EndOfFileException eof )
		{
			if( simpleTypeSpecifier != null )
				simpleTypeSpecifier.releaseReferences(astFactory.getReferenceManager());
			throw eof;			
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

                
                ITokenDuple duple = name(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EMPTY );

                consume(IToken.tLPAREN);
                IASTExpression expressionList = null;

                expressionList = expression(d.getDeclarationWrapper().getScope(), CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION);

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
                	logException( "ctorInitializer:addConstructorMemberInitializer", e1 ); //$NON-NLS-1$
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
    
    protected boolean constructInitializersInParameters = true;
    protected boolean constructInitializersInDeclarations = true;
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
        declSpecifierSeq(sdw, true, false, CompletionKind.ARGUMENT_TYPE, KeywordSetKey.DECL_SPECIFIER_SEQUENCE );
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
                        sdw.isUnsigned(), 
						sdw.isTypeNamed(), 
						sdw.isComplex(), 
						sdw.isImaginary(),
						sdw.isGloballyQualified(), null));
            }
            catch (ASTSemanticException e)
            {
                throw backtrack;
            }
            catch (Exception e)
            {
            	logException( "parameterDeclaration:createSimpleTypeSpecifier", e ); //$NON-NLS-1$
                throw backtrack;
            }
        
        setCompletionValues(scope,CompletionKind.SINGLE_NAME_REFERENCE,KeywordSetKey.EMPTY );     
        if (LT(1) != IToken.tSEMI)
           initDeclarator(sdw, SimpleDeclarationStrategy.TRY_FUNCTION, CompletionKind.VARIABLE_TYPE, constructInitializersInParameters );
 
 		if( lastToken != null )
 			sdw.setEndingOffsetAndLineNumber( lastToken.getEndOffset(), lastToken.getLineNumber() );
 			
        if (current == LA(1))
            throw backtrack;
        collection.addParameter(sdw);
    }
    /**
     * This class represents the state and strategy for parsing declarationSpecifierSequences
     */
    public class Flags
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
     * @return                 whether or not this  looks like a constructor (true or false)
     * @throws EndOfFileException       we could encounter EOF while looking ahead
     */
    private boolean lookAheadForConstructorOrConversion(Flags flags, DeclarationWrapper sdw, CompletionKind kind )
        throws EndOfFileException
    {
        if (flags.isForParameterDeclaration())
            return false;
        if (queryLookaheadCapability(2) && LT(2) == IToken.tLPAREN && flags.isForConstructor())
            return true;
        
        IToken mark = mark(); 
        Declarator d = new Declarator( sdw );
        try
		{
	        try
	        {
	            consumeTemplatedOperatorName( d, kind );
	        }
	        catch (BacktrackException e)
	        {
	            backup( mark ); 
	            return false;
	        }
	        catch ( EndOfFileException eof )
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
        finally
		{
        	if( d.getNameDuple() != null && d.getNameDuple().getTemplateIdArgLists() != null )
        	{
        		List [] arrayOfLists  = d.getNameDuple().getTemplateIdArgLists();
        		for( int i = 0; i < arrayOfLists.length; ++i )
        		{
        			if( arrayOfLists[i] == null ) continue;
        			for( int j = 0; j < arrayOfLists[i].size(); ++j )
        			{
        				IASTExpression e = (IASTExpression) arrayOfLists[i].get(j);
        				e.freeReferences( astFactory.getReferenceManager());
        				
        			}
        		}
        	}
		}
    }
    /**
     * @param flags			input flags that are used to make our decision 
     * @return				whether or not this looks like a a declarator follows
     * @throws EndOfFileException	we could encounter EOF while looking ahead
     */
    private boolean lookAheadForDeclarator(Flags flags) throws EndOfFileException
    {
        return flags.haveEncounteredTypename()
            && ( (LT(2) != IToken.tIDENTIFIER
                || (LT(3) != IToken.tLPAREN && LT(3) != IToken.tASSIGN))
                && !LA(2).isPointer());
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
        boolean tryConstructor, CompletionKind kind, KeywordSetKey key )
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
                    flags.setEncounteredRawType(true);
					consume();
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.INT);
                    break;
                case IToken.t_unsigned :
                    sdw.setUnsigned(true);
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    flags.setEncounteredRawType(true);
					consume();
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.INT);
                    break;
                case IToken.t_short :
                    sdw.setShort(true);
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    flags.setEncounteredRawType(true);
					consume();
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.INT);
                    break;
                case IToken.t_long :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    flags.setEncounteredRawType(true);
					consume();
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
                    flags.setEncounteredRawType(true);
					consume();
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.CHAR);
                    break;
                case IToken.t_wchar_t :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    flags.setEncounteredRawType(true);
					consume();
                    sdw.setSimpleType(
                        IASTSimpleTypeSpecifier.Type.WCHAR_T);
                    break;
                case IToken.t_bool :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    flags.setEncounteredRawType(true);
					consume();
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.BOOL);
                    break;
                case IToken.t__Bool: 
					if (typeNameBegin == null)
						typeNameBegin = LA(1);
					typeNameEnd = LA(1);
					flags.setEncounteredRawType(true);
					consume();
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type._BOOL);
					break;                
                case IToken.t_int :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    flags.setEncounteredRawType(true);
					consume();
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.INT);
                    break;					
                case IToken.t_float :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    flags.setEncounteredRawType(true);
					consume();
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.FLOAT);
                    break;
                case IToken.t_double :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    flags.setEncounteredRawType(true);
					consume();
                    sdw.setSimpleType(
                        IASTSimpleTypeSpecifier.Type.DOUBLE);
                    break;
                case IToken.t_void :
                    if (typeNameBegin == null)
                        typeNameBegin = LA(1);
                    typeNameEnd = LA(1);
                    flags.setEncounteredRawType(true);
					consume();
                    sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.VOID);
                    break;
                case IToken.t_typename :
                    sdw.setTypenamed(true);
                    consume(IToken.t_typename ); 
                    IToken first = LA(1);
                    IToken last = null;
                    last = name(sdw.getScope(), CompletionKind.TYPE_REFERENCE, KeywordSetKey.EMPTY).getLastToken();
                    if (LT(1) == IToken.t_template)
                    {
                        consume(IToken.t_template);
                        last = templateId(sdw.getScope(), CompletionKind.SINGLE_NAME_REFERENCE );
                    }
                    if( sdw.getName() != null )
                    	first = sdw.getName().getFirstToken();
                    ITokenDuple duple = TokenFactory.createTokenDuple(first, last);
                    sdw.setTypeName(duple);
					flags.setEncounteredTypename(true);
                    break;
                case IToken.tCOLONCOLON :
                	sdw.setGloballyQualified( true );
                	consume( IToken.tCOLONCOLON );
                	break;
                case IToken.tIDENTIFIER :
                    // TODO - Kludgy way to handle constructors/destructors
                    if (flags.haveEncounteredRawType())
                    {
                        setTypeName(sdw, typeNameBegin, typeNameEnd);
                        return;
                    }
                    if (parm && flags.haveEncounteredTypename())
                    {
                        setTypeName(sdw, typeNameBegin, typeNameEnd);
                        return;
                    }
                    if (lookAheadForConstructorOrConversion(flags, sdw, kind))
                    {
                        setTypeName(sdw, typeNameBegin, typeNameEnd);
                        return;
                    }
                    if (lookAheadForDeclarator(flags))
                    {
                        setTypeName(sdw, typeNameBegin, typeNameEnd);
                        return;
                    }
                    setCompletionValues(sdw.getScope(), kind, key );
                    ITokenDuple d = name(sdw.getScope(), kind, key );
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
                	if( extension.canHandleDeclSpecifierSequence( LT(1)))
                	{
                		IParserExtension.IDeclSpecifierExtensionResult declSpecExtResult = extension.parseDeclSpecifierSequence( this, flags, sdw, kind, key );
                		if( declSpecExtResult != null )
                		{
                			flags = declSpecExtResult.getFlags();
                			if( typeNameBegin == null )
                				typeNameBegin = declSpecExtResult.getFirstToken();
                			typeNameEnd = declSpecExtResult.getLastToken();
                			break;
                		}
                		break declSpecifiers;
                	}
                    break declSpecifiers;
            }
        }
        setTypeName(sdw, typeNameBegin, typeNameEnd);
        return;
    }



	/**
	 * @param sdw
	 * @param typeNameBegin
	 * @param typeNameEnd
	 */
	private void setTypeName(DeclarationWrapper sdw, IToken typeNameBegin, IToken typeNameEnd) {
		if (typeNameBegin != null)
		    sdw.setTypeName(
		    		TokenFactory.createTokenDuple(typeNameBegin, typeNameEnd));
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
        CompletionKind completionKind = null;
        
        switch (t.getType())
        {
            case IToken.t_class :
                eck = ASTClassKind.CLASS;
            	completionKind = CompletionKind.CLASS_REFERENCE;
                break;
            case IToken.t_struct :
                eck = ASTClassKind.STRUCT;
            	completionKind = CompletionKind.STRUCT_REFERENCE;
                break;
            case IToken.t_union :
                eck = ASTClassKind.UNION;
            	completionKind = CompletionKind.UNION_REFERENCE;
                break;
            case IToken.t_enum :
                eck = ASTClassKind.ENUM;
            	completionKind = CompletionKind.ENUM_REFERENCE;
                break;
            default :
                backup( t );
            	throw backtrack;
        }
 
        ITokenDuple d = name(sdw.getScope(), completionKind, KeywordSetKey.EMPTY);
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
        	logException( "elaboratedTypeSpecifier:createElaboratedTypeSpecifier", e ); //$NON-NLS-1$
            throw backtrack;
        }
        sdw.setTypeSpecifier(elaboratedTypeSpec);
        
        if( isForewardDecl )
        	((IASTElaboratedTypeSpecifier)elaboratedTypeSpec).acceptElement( requestor, astFactory.getReferenceManager() );
    }
    /**
     * Parses the initDeclarator construct of the ANSI C++ spec.
     * 
     * initDeclarator
     * : declarator ("=" initializerClause | "(" expressionList ")")?
     * @param constructInitializers TODO
     * @param owner			IParserCallback object that represents the owner declaration object.  
     * @return				declarator that this parsing produced.  
     * @throws BacktrackException	request a backtrack
     */
    protected Declarator initDeclarator(
        DeclarationWrapper sdw, SimpleDeclarationStrategy strategy, CompletionKind kind, boolean constructInitializers )
        throws EndOfFileException, BacktrackException
    {
        Declarator d = declarator(sdw, sdw.getScope(), strategy, kind );
        
        try
		{
        	astFactory.constructExpressions(constructInitializers);
	        if( language == ParserLanguage.CPP )
	        	optionalCPPInitializer(d, constructInitializers);
	        else if( language == ParserLanguage.C )
	        	optionalCInitializer(d, constructInitializers);
	        sdw.addDeclarator(d);
	        return d;
		} 
        finally
		{
        	astFactory.constructExpressions( true );
		}
    }
    
    protected void optionalCPPInitializer(Declarator d, boolean constructInitializers)
        throws EndOfFileException, BacktrackException
    {
        // handle initializer
        final IASTScope scope = d.getDeclarationWrapper().getScope();
        setCompletionValues(scope,CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY);
		if (LT(1) == IToken.tASSIGN)
        {
            consume(IToken.tASSIGN);
            setCompletionValues(scope,CompletionKind.SINGLE_NAME_REFERENCE,KeywordSetKey.EMPTY);
            throwAwayMarksForInitializerClause(d);
            IASTInitializerClause clause = initializerClause(scope,constructInitializers);
			d.setInitializerClause(clause);
			setCompletionValues(scope,CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY);
        }
        else if (LT(1) == IToken.tLPAREN )
        {
            // initializer in constructor
            consume(IToken.tLPAREN); // EAT IT!
            setCompletionValues(scope,CompletionKind.SINGLE_NAME_REFERENCE,KeywordSetKey.EMPTY);
            IASTExpression astExpression = null;
            astExpression = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION);
            setCompletionValues(scope,CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY);
            consume(IToken.tRPAREN);
            d.setConstructorExpression(astExpression);
        }
    }
    
    /**
	 * @param d
	 */
	protected void throwAwayMarksForInitializerClause(Declarator d) {
		simpleDeclarationMark = null; 
		if( d.getNameDuple() != null )
			d.getNameDuple().getLastToken().setNext( null );
		if( d.getPointerOperatorNameDuple() != null )
			d.getPointerOperatorNameDuple().getLastToken().setNext( null );
	}


	protected void optionalCInitializer( Declarator d, boolean constructInitializers ) throws EndOfFileException, BacktrackException
    {
    	final IASTScope scope = d.getDeclarationWrapper().getScope();
    	setCompletionValues(scope,CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY);
    	if( LT(1) == IToken.tASSIGN )
    	{
    		consume( IToken.tASSIGN );
    		throwAwayMarksForInitializerClause(d);
    		setCompletionValues(scope,CompletionKind.SINGLE_NAME_REFERENCE,KeywordSetKey.EMPTY);
			d.setInitializerClause( cInitializerClause(scope, Collections.EMPTY_LIST, constructInitializers ) );
			setCompletionValues(scope,CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY);
    	}
    }
    /**
     * @param scope
     * @return
     */
    protected IASTInitializerClause cInitializerClause(
        IASTScope scope,
        List designators, boolean constructInitializers)
        throws EndOfFileException, BacktrackException
    {    	
        if (LT(1) == IToken.tLBRACE)
        {
        	consume(IToken.tLBRACE);
            List initializerList = new ArrayList();
            for (;;)
            {
            	int checkOffset = LA(1).hashCode();
                // required at least one initializer list
                // get designator list
                List newDesignators = designatorList(scope);
                if( newDesignators.size() != 0 )
                	if( LT(1) == IToken.tASSIGN )
                		consume( IToken.tASSIGN );
                IASTInitializerClause initializer =
                    cInitializerClause(scope, newDesignators, constructInitializers );
                initializerList.add(initializer);
                // can end with just a '}'
                if (LT(1) == IToken.tRBRACE)
                    break;
                // can end with ", }"
                if (LT(1) == IToken.tCOMMA)
                    consume(IToken.tCOMMA);
                if (LT(1) == IToken.tRBRACE)
                    break;
                if( checkOffset == LA(1).hashCode())
                	throw backtrack;
                
                // otherwise, its another initializer in the list
            }
            // consume the closing brace
            consume(IToken.tRBRACE);
            return createInitializerClause(
                scope,
                (
				( designators.size() == 0 ) ? 
					IASTInitializerClause.Kind.INITIALIZER_LIST : 
					IASTInitializerClause.Kind.DESIGNATED_INITIALIZER_LIST ),
                null, initializerList, designators, constructInitializers );
        }
        // if we get this far, it means that we have not yet succeeded
        // try this now instead
        // assignmentExpression 
        try
        {
            IASTExpression assignmentExpression = assignmentExpression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION);
            try
            {
                return createInitializerClause(
                    scope,
                    (
				( designators.size() == 0 ) ? 
					IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION : 
					IASTInitializerClause.Kind.DESIGNATED_ASSIGNMENT_EXPRESSION ),
                    assignmentExpression, null, designators, constructInitializers );
            }
            catch (Exception e)
            {
            	logException( "cInitializerClause:createInitializerClause", e ); //$NON-NLS-1$
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
    protected IASTInitializerClause initializerClause(IASTScope scope, boolean constructInitializers)
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
                    return createInitializerClause(
                        scope,
                        IASTInitializerClause.Kind.EMPTY,
                        null, null, Collections.EMPTY_LIST, constructInitializers );
                }
                catch (Exception e)
                {
                	logException( "initializerClause_1:createInitializerClause", e ); //$NON-NLS-1$
                    throw backtrack;
                }
            }
            
            // otherwise it is a list of initializer clauses
            List initializerClauses = null;
            for (;;)
            {
                IASTInitializerClause clause = initializerClause(scope, constructInitializers);
                if( clause != null )
                {
                	if( initializerClauses == null )
                		initializerClauses = new ArrayList();
                	initializerClauses.add(clause);
                }
                if (LT(1) == IToken.tRBRACE)
                    break;
                consume(IToken.tCOMMA);
            }
            consume(IToken.tRBRACE);
            try
            {
                return createInitializerClause(
                    scope,
                    IASTInitializerClause.Kind.INITIALIZER_LIST,
                    null, initializerClauses == null ? Collections.EMPTY_LIST : initializerClauses, Collections.EMPTY_LIST, constructInitializers );
            }
            catch (Exception e)
            {
            	logException( "initializerClause_2:createInitializerClause", e ); //$NON-NLS-1$
                throw backtrack;
            }
        }
        
        // if we get this far, it means that we did not 
        // try this now instead
        // assignmentExpression 
        try
        {
            IASTExpression assignmentExpression =
                assignmentExpression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION);
   
            try
            {
                return createInitializerClause(
                    scope,
                    IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION,
                    assignmentExpression, null, Collections.EMPTY_LIST, constructInitializers );
            }
            catch (Exception e)
            {
            	logException( "initializerClause_3:createInitializerClause", e ); //$NON-NLS-1$
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
    
    
    protected IASTInitializerClause createInitializerClause( IASTScope scope, IASTInitializerClause.Kind kind, IASTExpression expression,
    		List initializerClauses, List designators, boolean constructInitializer )
	{
    	if( ! constructInitializer ) return null;
    	return astFactory.createInitializerClause(
                scope,
                kind,
                expression, initializerClauses, designators );
	}
    
    protected List designatorList(IASTScope scope) throws EndOfFileException, BacktrackException
    {
        List designatorList = Collections.EMPTY_LIST;
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
    				IToken mark = consume( IToken.tLBRACKET );
    				constantExpression = expression( scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION );
    				if( LT(1) != IToken.tRBRACKET )
    				{
    					backup( mark );
    			   		if( extension.canHandleCDesignatorInitializer( LT(1)))
    		    		{
    		    			IASTDesignator d = extension.parseDesignator( this, scope );
    		    			if( d != null )
    		    			{
    		    				if( designatorList == Collections.EMPTY_LIST )
    		    					designatorList = new ArrayList( DEFAULT_DESIGNATOR_LIST_SIZE );
    		    				designatorList.add( d );
    		    			}
    		    			break;
    		    		}
    				}
    				consume( IToken.tRBRACKET );
					kind = IASTDesignator.DesignatorKind.SUBSCRIPT; 	
    			}
    			
    			IASTDesignator d = 
    				astFactory.createDesignator( kind, constantExpression, id, null );
    			if( designatorList == Collections.EMPTY_LIST )
    				designatorList = new ArrayList( DEFAULT_DESIGNATOR_LIST_SIZE );
    			designatorList.add( d );
    				
    		}
    	}
    	else
    	{
    		if( extension.canHandleCDesignatorInitializer( LT(1)))
    		{
    			IASTDesignator d = extension.parseDesignator( this, scope );
    			if( d != null )
    			{
    				if( designatorList == Collections.EMPTY_LIST )
    					designatorList = new ArrayList( DEFAULT_DESIGNATOR_LIST_SIZE );
    				designatorList.add( d );
    			}
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
        IDeclaratorOwner owner, IASTScope scope, SimpleDeclarationStrategy strategy, CompletionKind kind )
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
                declarator(d, scope, strategy, kind );
                consume(IToken.tRPAREN);
            }
            else
	            consumeTemplatedOperatorName(d, kind);
            
            for (;;)
            {
                switch (LT(1))
                {
                    case IToken.tLPAREN :
                    	
                    	boolean failed = false;
                    	IASTScope parameterScope = astFactory.getDeclaratorScope( scope, d.getNameDuple() );
                        // temporary fix for initializer/function declaration ambiguity
                        if ( queryLookaheadCapability(2) && !LA(2).looksLikeExpression() && strategy != SimpleDeclarationStrategy.TRY_VARIABLE  )
                        {
                        	if(  LT(2) == IToken.tIDENTIFIER )
                        	{
								IToken newMark = mark();
								consume( IToken.tLPAREN );
								ITokenDuple queryName = null;
	                        	try
	                        	{
	                        		try
                                    {
                                        queryName = name(parameterScope, CompletionKind.TYPE_REFERENCE, KeywordSetKey.EMPTY );
										if( ! astFactory.queryIsTypeName( parameterScope, queryName ) )
                                        	failed = true;
                                    }
                                    catch (Exception e)
                                    {
                                    	logException( "declarator:queryIsTypeName", e ); //$NON-NLS-1$
                                        throw backtrack;
                                    }
	                        	} catch( BacktrackException b )
	                        	{ 
	                        		failed = true; 
	                        	}
	                        	
	                        	if( queryName != null )
	                        		queryName.freeReferences(astFactory.getReferenceManager());
								backup( newMark );
                        	}
                        }
						if( ( queryLookaheadCapability(2) && !LA(2).looksLikeExpression() && strategy != SimpleDeclarationStrategy.TRY_VARIABLE && !failed) || ! queryLookaheadCapability(3) )
						{  									
                            // parameterDeclarationClause
                            d.setIsFunction(true);
							// TODO need to create a temporary scope object here 
                            consume(IToken.tLPAREN);
                            setCompletionValues( scope, CompletionKind.ARGUMENT_TYPE, KeywordSetKey.DECL_SPECIFIER_SEQUENCE );
                            boolean seenParameter = false;
                            parameterDeclarationLoop : for (;;)
                            {
                                switch (LT(1))
                                {
                                    case IToken.tRPAREN :
                                        consume();
                                        setCompletionValues( parameterScope, CompletionKind.NO_SUCH_KIND, KeywordSetKey.FUNCTION_MODIFIER );
                                        break parameterDeclarationLoop;
                                    case IToken.tELLIPSIS :
                                        consume();
                                        d.setIsVarArgs( true );
                                        break;
                                    case IToken.tCOMMA :
                                        consume();
                                        setCompletionValues( parameterScope, CompletionKind.ARGUMENT_TYPE, KeywordSetKey.DECL_SPECIFIER_SEQUENCE );            
                                        seenParameter = false;
                                        break;
                                    default :
                                        if (seenParameter)
                                            throw backtrack;
                                        parameterDeclaration(d, parameterScope);
                                        seenParameter = true;
                                }
                            }
						}

                        if (LT(1) == IToken.tCOLON || LT(1) == IToken.t_try )
                            break overallLoop;
                        
                        IToken beforeCVModifier = mark();
                        IToken [] cvModifiers = new IToken[2];
                        int numCVModifiers = 0;
                        IToken afterCVModifier = beforeCVModifier;
                        // const-volatile
                        // 2 options: either this is a marker for the method,
                        // or it might be the beginning of old K&R style parameter declaration, see
                        //      void getenv(name) const char * name; {}
                        // This will be determined further below
                        while( (LT(1) == IToken.t_const || LT(1) == IToken.t_volatile) && numCVModifiers < 2 )
                        {
                            cvModifiers[numCVModifiers++] = consume();
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
                            IASTTypeId exceptionTypeId = null;
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
                                        try
                                        {
                                            exceptionTypeId = typeId(scope, false, CompletionKind.EXCEPTION_REFERENCE );
                                            exceptionSpecIds.add(exceptionTypeId);
                                            exceptionTypeId.acceptElement( requestor, astFactory.getReferenceManager() );
                                        }
                                        catch (BacktrackException e)
                                        {
                                            failParse();
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
                                	logException( "declarator:createExceptionSpecification", e ); //$NON-NLS-1$
                                    throw backtrack;
                                }
                        }
                        // check for optional pure virtual							
                        if (LT(1) == IToken.tASSIGN
                            && LT(2) == IToken.tINTEGER
                            && LA(2).getImage().equals("0")) //$NON-NLS-1$
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
                            if ( numCVModifiers > 0 )
                            {
                            	for( int i = 0; i < numCVModifiers; i++ ){
	                            	if( cvModifiers[i].getType() == IToken.t_const )
	                                    d.setConst(true);
	                                if( cvModifiers[i].getType() == IToken.t_volatile )
                                    	d.setVolatile(true);
                            	}
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
                        IASTExpression exp = constantExpression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION );
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
    
    protected void consumeTemplatedOperatorName(Declarator d, CompletionKind kind)
        throws EndOfFileException, BacktrackException
    {
    	TemplateParameterManager argumentList = TemplateParameterManager.getInstance();
    	try
		{
	        if (LT(1) == IToken.t_operator)
	            operatorId(d, null, null, kind);
	        else
	        {
	            try
	            {
	                ITokenDuple duple = name(d.getDeclarationWrapper().getScope(), kind, KeywordSetKey.EMPTY );
	                d.setName(duple);
	        
	            }
	            catch (BacktrackException bt)
	            {
	                Declarator d1 = d;
	                Declarator d11 = d1;
	                IToken start = null;
	                
	                boolean hasTemplateId = false;
	                
	                IToken mark = mark();
	                if (LT(1) == IToken.tCOLONCOLON
	                    || LT(1) == IToken.tIDENTIFIER)
	                {
	                    start = consume();
	                    IToken end = null;
	                    
	                    if (start.getType() == IToken.tIDENTIFIER){
	                      	end = consumeTemplateArguments(d.getDeclarationWrapper().getScope(), end, argumentList, kind);
	                    	if( end != null && end.getType() == IToken.tGT )
	                    		hasTemplateId = true;
	                    }
	                    
	                    while (LT(1) == IToken.tCOLONCOLON
	                        || LT(1) == IToken.tIDENTIFIER)
	                    {
	                        end = consume();
	                        if (end.getType() == IToken.tIDENTIFIER){
	                          	end = consumeTemplateArguments(d.getDeclarationWrapper().getScope(), end, argumentList, kind);
	                        	if( end.getType() == IToken.tGT )
	                        		hasTemplateId = true;
	                        }
	                    }
	                    if (LT(1) == IToken.t_operator)
	                        operatorId(d11, start, ( hasTemplateId ? argumentList : null ), kind );
	                    else
	                    {
	
	                        backup(mark);
	                        throw backtrack;
	                    }
	                }
	            }
	        }
		} finally
		{
			TemplateParameterManager.returnInstance(argumentList );
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
        setCompletionValues( sdw.getScope(), CompletionKind.ENUM_REFERENCE );
        if (LT(1) == IToken.tIDENTIFIER)
        {
            identifier = identifier();
            setCompletionValues( sdw.getScope(), CompletionKind.ENUM_REFERENCE );
        }
        if (LT(1) == IToken.tLBRACE)
        {
            IASTEnumerationSpecifier enumeration = null;
            try
            {
                enumeration = astFactory.createEnumerationSpecifier(
                        sdw.getScope(),
                        ((identifier == null) ? "" : identifier.getImage()), //$NON-NLS-1$
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
            	logException( "enumSpecifier:createEnumerationSpecifier", e ); //$NON-NLS-1$
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
                    initialValue = constantExpression(sdw.getScope(), CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION);
                }
                IASTEnumerator enumerator = null;
                if (LT(1) == IToken.tRBRACE)
                {
                    try
                    {
                        enumerator = astFactory.addEnumerator(
                            enumeration,
                            enumeratorIdentifier.getImage(),
                            enumeratorIdentifier.getOffset(),
							enumeratorIdentifier.getLineNumber(),
                            enumeratorIdentifier.getOffset(), enumeratorIdentifier.getEndOffset(), 
                            enumeratorIdentifier.getLineNumber(), lastToken.getEndOffset(), lastToken.getLineNumber(), initialValue);
                        endEnumerator(enumerator);
                    }
                    catch (ASTSemanticException e1)
                    {
						failParse();
						throw backtrack;                   
                    } catch (Exception e)
                    {
                    	logException( "enumSpecifier:addEnumerator", e ); //$NON-NLS-1$
                        throw backtrack;
                    }
                    break;
                }
                if (LT(1) != IToken.tCOMMA)
                {
                	enumeration.freeReferences( astFactory.getReferenceManager() );
                	if( enumerator != null )
                		enumerator.freeReferences( astFactory.getReferenceManager() );
                    throw backtrack;
                }
                try
                {
                    enumerator = astFactory.addEnumerator(
                        enumeration,
                        enumeratorIdentifier.getImage(),
                        enumeratorIdentifier.getOffset(),
						enumeratorIdentifier.getLineNumber(),
						enumeratorIdentifier.getOffset(), enumeratorIdentifier.getEndOffset(), enumeratorIdentifier.getLineNumber(), lastToken.getEndOffset(), lastToken.getLineNumber(), initialValue);
                    endEnumerator(enumerator);
                }
                catch (ASTSemanticException e1)
                {
					failParse();
					throw backtrack; 
                } catch (Exception e)
                {
                	logException( "enumSpecifier:addEnumerator", e ); //$NON-NLS-1$
                    throw backtrack;
                }
                consume(IToken.tCOMMA);
            }
            IToken t = consume(IToken.tRBRACE);
            enumeration.setEndingOffsetAndLineNumber(t.getEndOffset(), t.getLineNumber());
            enumeration.acceptElement( requestor, astFactory.getReferenceManager() );
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
        CompletionKind completionKind  = null;
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
                completionKind = CompletionKind.CLASS_REFERENCE;
                break;
            case IToken.t_struct :
                classKey = consume();
                classKind = ASTClassKind.STRUCT;
                completionKind  = CompletionKind.STRUCT_REFERENCE;
                break;
            case IToken.t_union :
                classKey = consume();
                classKind = ASTClassKind.UNION;
                completionKind = CompletionKind.UNION_REFERENCE;
                break;
            default :
                throw backtrack;
        }

        
        ITokenDuple duple = null;
        
        setCompletionValues(sdw.getScope(), completionKind, KeywordSetKey.EMPTY );
        // class name
        if (LT(1) == IToken.tIDENTIFIER)
            duple = name( sdw.getScope(), completionKind, KeywordSetKey.EMPTY );
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
        	logException( "classSpecifier:createClassSpecifier", e ); //$NON-NLS-1$
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
            setCompletionValues(astClassSpecifier, CompletionKind.FIELD_TYPE, KeywordSetKey.MEMBER );
            astClassSpecifier.enterScope( requestor, astFactory.getReferenceManager() );
            handleClassSpecifier( astClassSpecifier );
            memberDeclarationLoop : while (LT(1) != IToken.tRBRACE)
            {
                int checkToken = LA(1).hashCode();
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
                            declaration(astClassSpecifier, null, null, KeywordSetKey.MEMBER);
                        }
                        catch (BacktrackException bt)
                        {
                            failParse();
                            if (checkToken == LA(1).hashCode())
                                errorHandling();
                        }
                }
                if (checkToken == LA(1).hashCode())
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
            	logException( "classSpecifier:signalEndOfClassSpecifier", e1 ); //$NON-NLS-1$
                throw backtrack;
            }
            
            astClassSpecifier.exitScope( requestor, astFactory.getReferenceManager() );
            
            
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
        
        setCompletionValues(astClassSpec.getOwnerScope(), CompletionKind.CLASS_REFERENCE, KeywordSetKey.BASE_SPECIFIER );
        boolean isVirtual = false;
        ASTAccessVisibility visibility = ASTAccessVisibility.PUBLIC;
        ITokenDuple nameDuple = null;
        
        ArrayList bases = null;
        
        baseSpecifierLoop : for (;;)
        {
            switch (LT(1))
            {
                case IToken.t_virtual :
                    consume(IToken.t_virtual);
                	setCompletionValues(astClassSpec.getOwnerScope(), CompletionKind.CLASS_REFERENCE, KeywordSetKey.EMPTY );
                    isVirtual = true;
                    break;
                case IToken.t_public :
                	consume();
                	setCompletionValues(astClassSpec.getOwnerScope(), CompletionKind.CLASS_REFERENCE, KeywordSetKey.EMPTY );
                    break;
                case IToken.t_protected :
					consume();
				    visibility = ASTAccessVisibility.PROTECTED;
				    setCompletionValues(astClassSpec.getOwnerScope(), CompletionKind.CLASS_REFERENCE, KeywordSetKey.EMPTY );
                    break;
                case IToken.t_private :
                    visibility = ASTAccessVisibility.PRIVATE;
					consume();
					setCompletionValues(astClassSpec.getOwnerScope(), CompletionKind.CLASS_REFERENCE, KeywordSetKey.EMPTY );
           			break;
                case IToken.tCOLONCOLON :
                case IToken.tIDENTIFIER :
                	//to get templates right we need to use the class as the scope
                    nameDuple = name(astClassSpec, CompletionKind.CLASS_REFERENCE, KeywordSetKey.BASE_SPECIFIER );
                    break;
                case IToken.tCOMMA :
                	//because we are using the class as the scope to get the name, we need to postpone adding the base 
                	//specifiers until after we have all the nameDuples
                	if( bases == null ){
                		bases = new ArrayList(5);
                	}
                	bases.add( new Object[] { isVirtual ? Boolean.TRUE : Boolean.FALSE, visibility, nameDuple } );                    	

                    isVirtual = false;
                    visibility = ASTAccessVisibility.PUBLIC;
                    nameDuple = null;                        
                    consume();
                    setCompletionValues(astClassSpec.getOwnerScope(), CompletionKind.CLASS_REFERENCE, KeywordSetKey.BASE_SPECIFIER );
                    continue baseSpecifierLoop;
                default :
                    break baseSpecifierLoop;
            }
        }

        try
        {
            if( bases != null ){
            	int size = bases.size();
            	for( int i = 0; i < size; i++ ){
            		Object [] data = (Object[]) bases.get( i );
            		astFactory.addBaseSpecifier( astClassSpec, 
            				                     ((Boolean)data[0]).booleanValue(),
            		                             (ASTAccessVisibility) data[1], 
												 (ITokenDuple)data[2] );
            	}
            }
            
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
        	logException( "baseSpecifier_2::addBaseSpecifier", e ); //$NON-NLS-1$
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
    protected void statement(IASTCodeScope scope) throws EndOfFileException, BacktrackException
    {
  
    	setCompletionValues(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.STATEMENT);
    	
        switch (LT(1))
        {
            case IToken.t_case :
                consume(IToken.t_case);
                IASTExpression constant_expression = constantExpression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION );
				constant_expression.acceptElement(requestor, astFactory.getReferenceManager());
				endExpression(constant_expression);
                consume(IToken.tCOLON);
                statement(scope);
                cleanupLastToken();
                return;
            case IToken.t_default :
                consume(IToken.t_default);
                consume(IToken.tCOLON);
                statement(scope);
                cleanupLastToken();
                return;
            case IToken.tLBRACE :
                compoundStatement(scope, true);
            	cleanupLastToken();
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
                    if( LT(1) == IToken.t_if ){
                    	//an else if, return and get the rest of the else if as the next statement instead of recursing
                    	cleanupLastToken();
                    	return;
                    } else if( LT(1) != IToken.tLBRACE )
						singleStatementScope(scope);
                    else
                    	statement( scope );
                }
                cleanupLastToken();
                return;
            case IToken.t_switch :
                consume();
                consume(IToken.tLPAREN);
                condition(scope);
                consume(IToken.tRPAREN);
                statement(scope);
                cleanupLastToken();
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
                cleanupLastToken();
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
                cleanupLastToken();
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
                    IASTExpression finalExpression = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.DECLARATION);
                    finalExpression.acceptElement(requestor, astFactory.getReferenceManager());
                    endExpression(finalExpression);
                }
                consume(IToken.tRPAREN);
                statement(scope);
                cleanupLastToken();
                return;
            case IToken.t_break :
                consume();
                consume(IToken.tSEMI);
                cleanupLastToken();
                return;
            case IToken.t_continue :
                consume();
                consume(IToken.tSEMI);
                cleanupLastToken();
                return;
            case IToken.t_return :
                consume();
                if (LT(1) != IToken.tSEMI)
                {
                    IASTExpression retVal = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION);
                    retVal.acceptElement(requestor, astFactory.getReferenceManager());
                    endExpression(retVal);
                }
                consume(IToken.tSEMI);
                cleanupLastToken();
                return;
            case IToken.t_goto :
                consume();
                consume(IToken.tIDENTIFIER);
                consume(IToken.tSEMI);
                cleanupLastToken();
                return;
            case IToken.t_try :
                consume();
                compoundStatement(scope,true);
                catchHandlerSequence(scope);
                cleanupLastToken();
                return;
            case IToken.tSEMI :
                consume();
            	cleanupLastToken();
                return;
            default :
                // can be many things:
                // label
            	
                if (queryLookaheadCapability(2) && LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON)
                {
                    consume(IToken.tIDENTIFIER);
                    consume(IToken.tCOLON);
                    statement(scope);
                    cleanupLastToken();
                    return;
                }
                // expressionStatement
                // Note: the function style cast ambiguity is handled in expression
                // Since it only happens when we are in a statement
                IToken mark = mark();
                IASTExpression expressionStatement = null;
                try
                {
                    expressionStatement = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.STATEMENT);
                   	consume(IToken.tSEMI);
                   	expressionStatement.acceptElement( requestor, astFactory.getReferenceManager() );
                   	endExpression(expressionStatement);
                    return;
                }
                catch (BacktrackException b)
                {
                	backup( mark );
                	if( expressionStatement != null )
                		expressionStatement.freeReferences(astFactory.getReferenceManager());
                }

                // declarationStatement
                declaration(scope, null, null, KeywordSetKey.STATEMENT);
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
    			simpleDeclaration( SimpleDeclarationStrategy.TRY_VARIABLE, scope, null, CompletionKind.EXCEPTION_REFERENCE, true, KeywordSetKey.DECL_SPECIFIER_SEQUENCE ); 
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
        	logException( "singleStatementScope:createNewCodeBlock", e ); //$NON-NLS-1$
            throw backtrack;
        }
        newScope.enterScope( requestor, astFactory.getReferenceManager() );
        try
        {
			statement( newScope );
        }
        finally
        {
			newScope.exitScope( requestor, astFactory.getReferenceManager() );
        }
    }

    /**
     * @throws BacktrackException
     */
    protected void condition( IASTScope scope ) throws BacktrackException, EndOfFileException
    {
        IASTExpression someExpression = expression( scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION );
        someExpression.acceptElement(requestor, astFactory.getReferenceManager());
        
        endExpression(someExpression); 
    }
    
    /**
     * @throws BacktrackException
     */
    protected void forInitStatement( IASTScope scope ) throws BacktrackException, EndOfFileException
    {
    	IToken mark = mark();
    	try
    	{
    		IASTExpression e = expression( scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.DECLARATION );
    		consume( IToken.tSEMI );
			e.acceptElement(requestor, astFactory.getReferenceManager());
			
			
    	}
    	catch( BacktrackException bt )
    	{
    		backup( mark );
    		try
    		{
    			simpleDeclarationStrategyUnion(scope,null, null,null);
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
            	logException( "compoundStatement:createNewCodeBlock", e ); //$NON-NLS-1$
                throw backtrack;
            }        
        	newScope.enterScope( requestor, astFactory.getReferenceManager() );
        }
        
        setCompletionValues( 
        		(createNewScope ? newScope : scope ), 
				CompletionKind.SINGLE_NAME_REFERENCE, 
        		KeywordSetKey.STATEMENT );
        
        while (LT(1) != IToken.tRBRACE)
        {
        	int checkToken = LA(1).hashCode();
        	try
        	{
            	statement((IASTCodeScope) (createNewScope ? newScope : scope) );
        	}
        	catch( BacktrackException b )
        	{
        		failParse(); 
        		if( LA(1).hashCode() == checkToken )
        			errorHandling();
        	}
        	setCompletionValues(((createNewScope ? newScope : scope )), CompletionKind.SINGLE_NAME_REFERENCE, 
        			KeywordSetKey.STATEMENT );
        }
        
        consume(IToken.tRBRACE);
        
        if( createNewScope )
        	newScope.exitScope( requestor, astFactory.getReferenceManager() );
    }
    
    protected IASTCompilationUnit compilationUnit;
	protected IToken simpleDeclarationMark;
    
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
    
    protected void setCompletionToken( IToken token )
	{
    	// do nothing!
    }
    
    protected IToken getCompletionToken()
	{
    	return null;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public ISelectionParseResult parse(int startingOffset, int endingOffset)
		throws ParseError {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) throws ParseError {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setupASTFactory(org.eclipse.cdt.core.parser.IScanner, org.eclipse.cdt.core.parser.ParserLanguage)
	 */
	protected void setupASTFactory(IScanner scanner, ParserLanguage language) {
		// do nothing as of yet
		// subclasses will need to implement this method
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#parserTimeout()
	 */
	protected final boolean parserTimeout() {
		return requestor.parserTimeout();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#getCompliationUnit()
	 */
	protected IASTNode getCompliationUnit() {
		return compilationUnit;
	}
	
	protected void endDeclaration( IASTDeclaration declaration ) throws EndOfFileException
	{
		cleanupLastToken();
		if( declaration instanceof IASTOffsetableNamedElement )
			handleOffsetableNamedElement( (IASTOffsetableNamedElement) declaration );
	}
	
	protected void endEnumerator( IASTEnumerator enumerator  ) throws EndOfFileException
	{
		cleanupLastToken();
		handleOffsetableNamedElement(enumerator);
	}
	
	/**
	 * 
	 */
	protected void cleanupLastToken() {
		if( lastToken != null )
			lastToken.setNext( null );
		simpleDeclarationMark = null;
	}


	protected void endExpression( IASTExpression expression ) throws EndOfFileException
	{
		cleanupLastToken();
	}
	
	protected void handleClassSpecifier( IASTClassSpecifier classSpecifier ) throws EndOfFileException
	{
		cleanupLastToken();
		handleOffsetableNamedElement( classSpecifier );
	}


	/**
	 * @param expression
	 */
	protected void handleOffsetableNamedElement(IASTOffsetableNamedElement node ) {
	}
	
}
