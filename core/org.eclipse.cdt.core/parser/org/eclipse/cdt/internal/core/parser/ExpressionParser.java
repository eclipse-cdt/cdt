/*******************************************************************************
 * Copyright (c) 2000 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;

/**
 * @author jcamelon
 */
public class ExpressionParser implements IExpressionParser, IParserData {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static int FIRST_ERROR_OFFSET_UNSET = -1;
	protected boolean parsePassed = true;
	protected int firstErrorOffset = FIRST_ERROR_OFFSET_UNSET;	
	protected static BacktrackException backtrack = new BacktrackException();
	protected final IParserExtension extension;
	
	//TODO this stuff needs to be encapsulated by IParserData
	protected final IParserLogService log;
	protected ParserLanguage language = ParserLanguage.CPP;
	protected IASTFactory astFactory = null;
	protected IScanner scanner;
	protected IToken currToken;
	protected IToken lastToken;
	private boolean limitReached = false;
	private Stack templateIdScopes = null;
	private TypeId typeIdInstance = new TypeId();
	
	/**
	 * @return Returns the astFactory.
	 */
	public IASTFactory getAstFactory() {
		return astFactory;
	}
	/**
	 * @return Returns the log.
	 */
	public IParserLogService getLog() {
		return log;
	}

	
	/**
	 * Look Ahead in the token list to see what is coming.  
	 * 
	 * @param i		How far ahead do you wish to peek?
	 * @return		the token you wish to observe
	 * @throws EndOfFileException	if looking ahead encounters EOF, throw EndOfFile 
	 */
	public IToken LA(int i) throws EndOfFileException {
		
		if (parserTimeout()){
			throw new ParseError( ParseError.ParseErrorKind.TIMEOUT );
    	}
		
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
	public int LT(int i) throws EndOfFileException {
	    return LA(i).getType();
	}

	/**
	 * Consume the next token available, regardless of the type.  
	 * 
	 * @return				The token that was consumed and removed from our buffer.  
	 * @throws EndOfFileException	If there is no token to consume.  
	 */
	public IToken consume() throws EndOfFileException {
		
	    if (currToken == null)
	        currToken = fetchToken();
	    if (currToken != null)
	        lastToken = currToken;
	    currToken = currToken.getNext();
    	handleNewToken( lastToken );
	    return lastToken;
	}

	/**
	 * Consume the next token available only if the type is as specified.  
	 * 
	 * @param type			The type of token that you are expecting.  	
	 * @return				the token that was consumed and removed from our buffer. 
	 * @throws BacktrackException	If LT(1) != type 
	 */
	public IToken consume(int type) throws EndOfFileException, BacktrackException {
	    if (LT(1) == type)
	        return consume();
	    throw backtrack;
	}

	/**
	 * Mark our place in the buffer so that we could return to it should we have to.  
	 * 
	 * @return				The current token. 
	 * @throws EndOfFileException	If there are no more tokens.
	 */
	public IToken mark() throws EndOfFileException {
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
	public void backup(IToken mark) {
	    currToken = mark;
	    lastToken = null; // this is not entirely right ... 
	}
	
	


	/**
	 * @param extension TODO
	 * @param scanner2
	 * @param callback
	 * @param language2
	 * @param log2
	 */
	public ExpressionParser(IScanner scanner, ParserLanguage language, IParserLogService log, IParserExtension extension) {
		this.scanner = scanner;
		this.language = language; 
		this.log = log;
		this.extension = extension;
		setupASTFactory(scanner, language);
	}

	/**
	 * @param scanner
	 * @param language
	 */
	protected void setupASTFactory(IScanner scanner, ParserLanguage language) {
		astFactory = ParserFactory.createASTFactory( this, ParserMode.EXPRESSION_PARSE, language);
		scanner.setASTFactory(astFactory);
		astFactory.setLogger(log);
	}

	/**
	 * This is the single entry point for setting parsePassed to 
	 * false, and also making note what token offset we failed upon. 
	 * 
	 * @throws EndOfFileException
	 */
	protected void failParse() {
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
	 * Consumes template parameters.  
	 *
	 * @param previousLast	Previous "last" token (returned if nothing was consumed)
	 * @return				Last consumed token, or <code>previousLast</code> if nothing was consumed
	 * @throws BacktrackException	request a backtrack
	 */
	protected IToken consumeTemplateParameters(IToken previousLast) throws EndOfFileException, BacktrackException {
		if( language != ParserLanguage.CPP ) return previousLast;
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

	protected List templateArgumentList( IASTScope scope, IASTCompletionNode.CompletionKind kind ) throws EndOfFileException, BacktrackException
	{
        IASTExpression expression = null;
        List list = new LinkedList();
        
        boolean completedArg = false;
        boolean failed = false;
        
        if( templateIdScopes == null ){
        	templateIdScopes = new Stack();
        }
        templateIdScopes.push( new Integer( IToken.tLT ) );
        
        while( LT(1) != IToken.tGT ){
        	completedArg = false;
        	
        	IToken mark = mark();
        	
        	try{
        		IASTTypeId typeId = typeId( scope, false, kind );
        		
        		expression = astFactory.createExpression( scope, IASTExpression.Kind.POSTFIX_TYPEID_TYPEID,
                                                          null, null, null, typeId, null, EMPTY_STRING, null); 
        		list.add( expression );
        		completedArg = true;
        	} catch( BacktrackException e ){
        		backup( mark );
        	} catch (ASTSemanticException e) {
        		backup( mark );
			}

        	if( ! completedArg ){
	        	try{
	        		expression = assignmentExpression( scope, CompletionKind.VARIABLE_TYPE, KeywordSetKey.EXPRESSION ); 
	        		if( expression.getExpressionKind() == IASTExpression.Kind.PRIMARY_EMPTY ){
	        			throw backtrack;
	        		}
	        		list.add( expression );
	        		completedArg = true;
	        	} catch( BacktrackException e ){
	        		backup( mark );
	        	}
        	}
        	if( !completedArg ){
	        	try{
	        		ITokenDuple nameDuple = name( scope, null, KeywordSetKey.EMPTY );
	        		expression = astFactory.createExpression( scope, IASTExpression.Kind.ID_EXPRESSION, 
	        				                                  null, null, null, null, nameDuple, EMPTY_STRING, null); 
	        		list.add( expression );
	        		continue;
	        	} catch( ASTSemanticException e ){
	        		failed = true;
	        		break;
	        	}catch( BacktrackException e ){
	        		failed = true;
	        		break;
	        	}catch( Exception e ){
	        		logException( "templateArgumentList::createExpression()", e ); //$NON-NLS-1$
	        		failed = true;
	        		break;
	        	}
        	}
        	
        	if( LT(1) == IToken.tCOMMA ){
        		consume();
        	} else if( LT(1) != IToken.tGT ){
        		failed = true;
        		break;
        	}
        }
	       
        templateIdScopes.pop();
    	if( templateIdScopes.size() == 0 ){
    		templateIdScopes = null;
    	}
    	
        if( failed ) {
        	if( expression != null )
        		expression.freeReferences(astFactory.getReferenceManager());
        	throw backtrack;
        }
        
	    return list;
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
	protected IToken templateId(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
	    ITokenDuple duple = name(scope, kind, KeywordSetKey.EMPTY );
	    //IToken last = consumeTemplateParameters(duple.getLastToken());
	    return duple.getLastToken();//last;
	}

	/**
	 * Parse a name.
	 * 
	 * name
	 * : ("::")? name2 ("::" name2)*
	 * 
	 * name2
	 * : IDENTIFER
	 * : template-id
	 * 
	 * @param key TODO
	 * @throws BacktrackException	request a backtrack
	 */
	protected ITokenDuple name(IASTScope scope, IASTCompletionNode.CompletionKind kind, KeywordSetKey key ) throws BacktrackException, EndOfFileException {
		
		TemplateParameterManager argumentList = TemplateParameterManager.getInstance();
		
		try
		{
		    IToken first = LA(1);
		    IToken last = null;
		    IToken mark = mark();
		    
		    
		    boolean hasTemplateId = false;
		    boolean startsWithColonColon = false;
	        
	        if (LT(1) == IToken.tCOLONCOLON){
	        	argumentList.addSegment( null );
	            last = consume( IToken.tCOLONCOLON );
	            setCompletionValues( scope, kind, KeywordSetKey.EMPTY, getCompliationUnit() );
	            startsWithColonColon = true;
	        }
	
	        if (LT(1) == IToken.tCOMPL)
	            consume();
	        
	        switch (LT(1))
	        {
	            case IToken.tIDENTIFIER :
	            	IToken prev = last;
	            	last = consume(IToken.tIDENTIFIER);
	            	if( startsWithColonColon )
	            		setCompletionValues( scope, kind, getCompliationUnit() );
	            	else if( prev != null )
	            		setCompletionValues(scope, kind, first, prev, KeywordSetKey.EMPTY );
	            	else
	            		setCompletionValuesNoContext(scope, kind, key );
	            	
	                last = consumeTemplateArguments(scope, last, argumentList, kind);
	                if( last.getType() == IToken.tGT )
	                	hasTemplateId = true;
	                break;
	        
	            default :
	                backup(mark);
	                throw backtrack;
	        }
	
	        while (LT(1) == IToken.tCOLONCOLON)
	        {
	        	IToken prev = last;
	            last = consume(IToken.tCOLONCOLON);
	            setCompletionValues( scope, kind, first, prev, KeywordSetKey.EMPTY );
	            
	            if (queryLookaheadCapability() && LT(1) == IToken.t_template)
	                consume();
	            
	            if (queryLookaheadCapability() && LT(1) == IToken.tCOMPL)
	                consume();
	
	            switch (LT(1))
	            {
	                case IToken.t_operator :
	                    backup(mark);
	                    throw backtrack;
	                case IToken.tIDENTIFIER :
	                	prev = last;
	                    last = consume();
	                    setCompletionValues( scope, kind, first, prev, KeywordSetKey.EMPTY );
			            last = consumeTemplateArguments(scope, last, argumentList, kind);
			            if( last.getType() == IToken.tGT )
			            	hasTemplateId = true;
	            }
	        }
	
	        ITokenDuple tokenDuple = TokenFactory.createTokenDuple(first, last, ( hasTemplateId ? argumentList.getTemplateArgumentsList() : null ) );
	        setGreaterNameContext( tokenDuple );
			return tokenDuple;
		}
		finally
		{
			TemplateParameterManager.returnInstance( argumentList );
		}

	}

	/**
	 * @param scope
	 * @param kind
	 * @param key
	 */
	protected void setCompletionValuesNoContext(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException {
	}
	/**
	 * @param tokenDuple
	 */
	protected void setGreaterNameContext(ITokenDuple tokenDuple) {
		//do nothing in this implementation
	}

	/**
	 * @param scope
	 * @param kind
	 */
	protected void setCompletionValues(IASTScope scope, CompletionKind kind, IASTNode context ) throws EndOfFileException{
	}
	
	/**
	 * @param scope
	 * @param kind
	 */
	protected void setCompletionValues(IASTScope scope, CompletionKind kind) throws EndOfFileException{
	}

	/**
	 * @return
	 */
	protected IASTNode getCompliationUnit() {
		return null;
	}

	/**
	 * @param scope
	 * @param kind
	 * @param key
	 * @param node
	 */
	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key, IASTNode node) throws EndOfFileException
	{
	}

	/**
	 * @param scope
	 * @param last
	 * @param argumentList
	 * @return
	 * @throws EndOfFileException
	 * @throws BacktrackException
	 */
	protected IToken consumeTemplateArguments(IASTScope scope, IToken last, TemplateParameterManager argumentList, IASTCompletionNode.CompletionKind completionKind) throws EndOfFileException, BacktrackException {
		if( language != ParserLanguage.CPP ) return last;
		if( LT(1) == IToken.tLT ){
			IToken secondMark = mark();
			consume( IToken.tLT );
		    try
		    {
		    	List list = templateArgumentList( scope, completionKind );
		    	argumentList.addSegment( list );
		    	last = consume( IToken.tGT );
		    } catch( BacktrackException bt )
		    {
		    	argumentList.addSegment( null );
		    	backup( secondMark );
		    }
		} else {
			argumentList.addSegment( null );
		}
		return last;
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
	protected IToken cvQualifier(IDeclarator declarator) throws EndOfFileException, BacktrackException {
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
        		if( extension.isValidCVModifier( language, IToken.t_restrict ))
        		{
	        		result = consume( IToken.t_restrict );
					declarator.addPointerOperator( extension.getPointerOperator(language, IToken.t_restrict ));
					break;	        			
        		}
        		throw backtrack;

	        	
	        default :
	            if( extension.isValidCVModifier( language, LT(1)))
	            {
	            	result = consume();
	            	declarator.addPointerOperator( extension.getPointerOperator( language, result.getType() ));
	            }
	    }
	    return result;
	}

	protected void consumeArrayModifiers(IDeclarator d, IASTScope scope) throws EndOfFileException, BacktrackException {
	    while (LT(1) == IToken.tLBRACKET)
	    {
	        consume( IToken.tLBRACKET ); // eat the '['
	    
	        IASTExpression exp = null;
	        if (LT(1) != IToken.tRBRACKET)
	        {
	            exp = constantExpression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION );
	        }
	        consume(IToken.tRBRACKET);
	        IASTArrayModifier arrayMod;
	        try
	        {
	            arrayMod = astFactory.createArrayModifier(exp);
	        }
	        catch (Exception e)
	        {
	        	logException( "consumeArrayModifiers::createArrayModifier()", e ); //$NON-NLS-1$
	            throw backtrack;
	        }
	        d.addArrayModifier(arrayMod);
	    }
	}

	protected void operatorId(Declarator d, IToken originalToken, TemplateParameterManager templateArgs, IASTCompletionNode.CompletionKind completionKind ) throws BacktrackException, EndOfFileException {
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
	        typeId(d.getDeclarationWrapper().getScope(), true, CompletionKind.TYPE_REFERENCE );
	        toSend = lastToken;
	    }
	    
	    boolean hasTemplateId = ( templateArgs != null );
	    boolean grabbedNewInstance = false;
	    if( templateArgs == null ) 
	    {
	    	templateArgs = TemplateParameterManager.getInstance();
	    	grabbedNewInstance = true;
	    }
	    
	    try
		{
		    toSend = consumeTemplateArguments( d.getDeclarationWrapper().getScope(), toSend, templateArgs, completionKind );
		    if( toSend.getType() == IToken.tGT ){
		    	hasTemplateId = true;
		    }
		    
		    ITokenDuple duple =
		    	TokenFactory.createTokenDuple( originalToken == null ? operatorToken : originalToken, toSend, (hasTemplateId ? templateArgs.getTemplateArgumentsList() : null ) );
		
		    d.setName(duple);
		}
	    finally
		{
	    	if( grabbedNewInstance )
	    		TemplateParameterManager.returnInstance( templateArgs );
		}
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
	protected IToken consumePointerOperators(IDeclarator d) throws EndOfFileException, BacktrackException {
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
	        		try
					{
	        			nameDuple = name(d.getScope(), CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EMPTY );
					}
	        		catch( OffsetLimitReachedException olre )
					{
	        			backup( mark );
	        			return null;
					}
	        	}
	        	catch( BacktrackException bt )
	        	{
	        		backup( mark ); 
	        		return null;
	        	}
	        }
	        if ( LT(1) == IToken.tSTAR)
	        {
	            result = consume(IToken.tSTAR); 
	
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
	        if( nameDuple != null )
	        	nameDuple.freeReferences( astFactory.getReferenceManager() );
	        backup(mark);
	        return result;
		}
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression constantExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    return conditionalExpression(scope,kind,key);
	}

	public IASTExpression expression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression assignmentExpression = assignmentExpression(scope,kind,key);
	    while (LT(1) == IToken.tCOMMA)
	    {
	        consume(IToken.tCOMMA);
	        setParameterListExpression( assignmentExpression );
	        IASTExpression secondExpression = assignmentExpression(scope,kind,key);
	        setParameterListExpression( null );
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
	                    null, EMPTY_STRING, null); 
	        }
	        catch (ASTSemanticException e)
	        {
	            throw backtrack;
	        } catch (Exception e)
	        {
	        	logException( "expression::createExpression()", e ); //$NON-NLS-1$
	            throw backtrack;
	        }
	    }
	    return assignmentExpression;
	}

	/**
	 * @param assignmentExpression
	 */
	protected void setParameterListExpression(IASTExpression assignmentExpression) {
	}
	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression assignmentExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
		setCompletionValues(scope, kind, key );
		if (LT(1) == IToken.t_throw) {
			return throwExpression(scope,key);
		}
		IASTExpression conditionalExpression = conditionalExpression(scope,kind,key);
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
					conditionalExpression, kind,key);
			case IToken.tSTARASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_MULT,
					conditionalExpression, kind,key);
			case IToken.tDIVASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_DIV,
					conditionalExpression, kind,key);
			case IToken.tMODASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_MOD,
					conditionalExpression, kind,key);
			case IToken.tPLUSASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_PLUS,
					conditionalExpression, kind,key);
			case IToken.tMINUSASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_MINUS,
					conditionalExpression, kind,key);
			case IToken.tSHIFTRASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_RSHIFT,
					conditionalExpression, kind,key);
			case IToken.tSHIFTLASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_LSHIFT,
					conditionalExpression, kind,key);
			case IToken.tAMPERASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_AND,
					conditionalExpression, kind,key);
			case IToken.tXORASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_XOR,
					conditionalExpression, kind,key);
			case IToken.tBITORASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_OR,
					conditionalExpression, kind,key);
		}
		return conditionalExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression throwExpression(IASTScope scope, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    consume(IToken.t_throw);
	    setCompletionValues( scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION );
	    IASTExpression throwExpression = null;
	    try
	    {
	        throwExpression = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE, key);
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
	            null, EMPTY_STRING, null); 
	    }
	    catch (ASTSemanticException e)
	    {
	        throw backtrack;
	    } catch (Exception e)
	    {
	    	logException( "throwExpression::createExpression()", e ); //$NON-NLS-1$
	        throw backtrack;
	    }
	}

	/**
	 * @param expression
	 * @return
	 * @throws BacktrackException
	 */
	protected IASTExpression conditionalExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = logicalOrExpression(scope,kind,key);
	    if (LT(1) == IToken.tQUESTION)
	    {
	        consume();
	        IASTExpression secondExpression = expression(scope,kind, key);
	        consume(IToken.tCOLON);
	        IASTExpression thirdExpression = assignmentExpression(scope,kind,key);
	        try
	        {
	            return astFactory.createExpression(
	                scope,
	                IASTExpression.Kind.CONDITIONALEXPRESSION,
	                firstExpression,
	                secondExpression,
	                thirdExpression,
	                null,
	                null, EMPTY_STRING, null); 
	        }
	        catch (ASTSemanticException e)
	        {
	            throw backtrack;
	        } catch (Exception e)
	        {
	        	logException( "conditionalExpression::createExpression()", e ); //$NON-NLS-1$
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression logicalOrExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = logicalAndExpression(scope,kind,key);
	    while (LT(1) == IToken.tOR)
	    {
	        consume();
	        IASTExpression secondExpression = logicalAndExpression(scope,kind,key);
	
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
	                    null, EMPTY_STRING, null); 
	        }
	        catch (ASTSemanticException e)
	        {
	            throw backtrack;
	        } catch (Exception e)
	        {
	        	logException( "logicalOrExpression::createExpression()", e ); //$NON-NLS-1$
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression logicalAndExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = inclusiveOrExpression( scope,kind,key );
	    while (LT(1) == IToken.tAND)
	    {
	        consume();
	        IASTExpression secondExpression = inclusiveOrExpression( scope,kind,key );
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
	                    null, EMPTY_STRING, null); 
	        }
	        catch (ASTSemanticException e)
	        {
	            throw backtrack;
	        } catch (Exception e)
	        {
	        	logException( "logicalAndExpression::createExpression()", e ); //$NON-NLS-1$
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression inclusiveOrExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = exclusiveOrExpression(scope,kind,key);
	    while (LT(1) == IToken.tBITOR)
	    {
	        consume();
	        IASTExpression secondExpression = exclusiveOrExpression(scope,kind,key);
	
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
	                    null, EMPTY_STRING, null); 
	        }
	        catch (ASTSemanticException e)
	        {
	            throw backtrack;
	        } catch (Exception e)
	        {
	        	logException( "inclusiveOrExpression::createExpression()", e ); //$NON-NLS-1$
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression exclusiveOrExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = andExpression( scope,kind, key );
	    while (LT(1) == IToken.tXOR)
	    {
	        consume();
	        
	        IASTExpression secondExpression = andExpression( scope,kind, key );
	
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
	                    null, EMPTY_STRING, null); 
	        }
	        catch (ASTSemanticException e)
	        {
	            throw backtrack;
	        } catch (Exception e)
	        {
	        	logException( "exclusiveORExpression::createExpression()", e ); //$NON-NLS-1$
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression andExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    IASTExpression firstExpression = equalityExpression(scope,kind, key);
	    while (LT(1) == IToken.tAMPER)
	    {
	        consume();
	        IASTExpression secondExpression = equalityExpression(scope,kind, key);
	
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
	                    null, EMPTY_STRING, null); 
	        }
	        catch (ASTSemanticException e)
	        {
	            throw backtrack;
	        } catch (Exception e)
	        {
	        	logException("andExpression::createExpression()", e); //$NON-NLS-1$
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param methodName
	 * @param e
	 */
	public void logException(String methodName, Exception e) {
		if( !(e instanceof EndOfFileException ) && e != null )
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append( "Parser: Unexpected exception in "); //$NON-NLS-1$
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
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression equalityExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    IASTExpression firstExpression = relationalExpression(scope,kind,key);
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tEQUAL :
	            case IToken.tNOTEQUAL :
	                IToken t = consume();
	                IASTExpression secondExpression =
	                    relationalExpression(scope,kind,key);
	
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
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e)
	                {
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "equalityExpression::createExpression()", e ); //$NON-NLS-1$
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
	protected IASTExpression relationalExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = shiftExpression(scope,kind, key);
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tGT :
	            	if( templateIdScopes != null && ((Integer)templateIdScopes.peek()).intValue() == IToken.tLT ){
	            		return firstExpression;
	            	}
	            case IToken.tLT :
	            case IToken.tLTEQUAL :
	            case IToken.tGTEQUAL :
	                IToken mark = mark();
	                int t = consume().getType();
	                IASTExpression secondExpression = shiftExpression(scope,kind, key);
	                if (LA(1) == mark.getNext() )
	                {
	                    // we did not consume anything
	                    // this is most likely an error
	                    backup(mark);
	                    return firstExpression;
	                }
                    IASTExpression.Kind expressionKind = null;
                    switch (t)
                    {
                        case IToken.tGT :
                            expressionKind =
                                IASTExpression.Kind.RELATIONAL_GREATERTHAN;
                            break;
                        case IToken.tLT :
                            expressionKind = IASTExpression.Kind.RELATIONAL_LESSTHAN;
                            break;
                        case IToken.tLTEQUAL :
                            expressionKind =
                                IASTExpression
                                    .Kind
                                    .RELATIONAL_LESSTHANEQUALTO;
                            break;
                        case IToken.tGTEQUAL :
                            expressionKind =
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
                                expressionKind,
                                firstExpression,
                                secondExpression,
                                null,
                                null,
                                null, EMPTY_STRING, null); 
                    }
                    catch (ASTSemanticException e)
                    {
                        throw backtrack;
                    } catch (Exception e)
                    {
                    	logException( "relationalExpression::createExpression()", e ); //$NON-NLS-1$
                        throw backtrack;
                    }
	                break;
	            default :
	            	if( extension.isValidRelationalExpressionStart(language, LT(1)))
	            	{
	            		IASTExpression extensionExpression = extension.parseRelationalExpression(scope, this, kind, key, firstExpression );
	            		if( extensionExpression != null ) return extensionExpression;
	            	}
	                return firstExpression;
	        }
	    }
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	public IASTExpression shiftExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = additiveExpression(scope,kind,key);
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tSHIFTL :
	            case IToken.tSHIFTR :
	                IToken t = consume();
	                IASTExpression secondExpression =
	                    additiveExpression(scope,kind,key);
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
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e)
	                {
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "shiftExpression::createExpression()", e ); //$NON-NLS-1$
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
	protected IASTExpression additiveExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = multiplicativeExpression( scope, kind, key );
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tPLUS :
	            case IToken.tMINUS :
	                IToken t = consume();
	                IASTExpression secondExpression =
	                    multiplicativeExpression(scope,kind,key);
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
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e)
	                {
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "additiveExpression::createExpression()", e ); //$NON-NLS-1$
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
	protected IASTExpression multiplicativeExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = pmExpression(scope,kind,key);
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tSTAR :
	            case IToken.tDIV :
	            case IToken.tMOD :
	                IToken t = consume();
	                IASTExpression secondExpression = pmExpression(scope,kind,key);
	                IASTExpression.Kind expressionKind = null;
	                switch (t.getType())
	                {
	                    case IToken.tSTAR :
	                        expressionKind = IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY;
	                        break;
	                    case IToken.tDIV :
	                        expressionKind = IASTExpression.Kind.MULTIPLICATIVE_DIVIDE;
	                        break;
	                    case IToken.tMOD :
	                        expressionKind = IASTExpression.Kind.MULTIPLICATIVE_MODULUS;
	                        break;
	                }
	                try
	                {
	                    firstExpression =
	                        astFactory.createExpression(
	                            scope,
	                            expressionKind,
	                            firstExpression,
	                            secondExpression,
	                            null,
	                            null,
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e)
	                {
	                	firstExpression.freeReferences(astFactory.getReferenceManager());
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "multiplicativeExpression::createExpression()", e ); //$NON-NLS-1$
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
	protected IASTExpression pmExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    IASTExpression firstExpression = castExpression(scope,kind,key);
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tDOTSTAR :
	            case IToken.tARROWSTAR :
	                IToken t = consume();
	                IASTExpression secondExpression =
	                    castExpression(scope,kind,key);
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
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e)
	                {
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "pmExpression::createExpression()", e ); //$NON-NLS-1$
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
	protected IASTExpression castExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    // TO DO: we need proper symbol checkint to ensure type name
	    if (LT(1) == IToken.tLPAREN)
	    {
	        IToken mark = mark();
	        consume();
	        if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	}
	        boolean popped = false;
	        IASTTypeId typeId = null;
	        // If this isn't a type name, then we shouldn't be here
	        try
	        {
	        	try
				{
	        		typeId = typeId(scope, false, getCastExpressionKind(kind));
		            consume(IToken.tRPAREN);
				}
	        	catch( BacktrackException bte )
				{
	        		backup( mark );
	        		if( typeId != null )
	        			typeId.freeReferences(astFactory.getReferenceManager());
	        		throw bte;
				}
	        	mark = null; // clean up mark so that we can garbage collect
	            if( templateIdScopes != null ){ templateIdScopes.pop();	popped = true;}
	            IASTExpression castExpression = castExpression(scope,kind,key);
	            try
	            {
	                return astFactory.createExpression(
	                    scope,
	                    IASTExpression.Kind.CASTEXPRESSION,
	                    castExpression,
	                    null,
	                    null,
	                    typeId,
	                    null, EMPTY_STRING, null); 
	            }
	            catch (ASTSemanticException e)
	            {
	                throw backtrack;
	            } catch (Exception e)
	            {
	            	logException( "castExpression::createExpression()", e ); //$NON-NLS-1$
	                throw backtrack;
	            }
	        }
	        catch (BacktrackException b)
	        {
	            if( templateIdScopes != null && !popped ){ templateIdScopes.pop();	}
	        }
	    }
	    return unaryExpression(scope,kind, key);
	    
	}

	/**
	 * @param kind
	 * @return
	 */
	private CompletionKind getCastExpressionKind(CompletionKind kind) {
		return ((kind == CompletionKind.SINGLE_NAME_REFERENCE || kind == CompletionKind.FUNCTION_REFERENCE)? kind : CompletionKind.TYPE_REFERENCE);
	}
	

	
	/**
	 * @param completionKind TODO
	 * @throws BacktrackException
	 */
	public IASTTypeId typeId(IASTScope scope, boolean skipArrayModifiers, CompletionKind completionKind) throws EndOfFileException, BacktrackException {
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
	            name  = name(scope, completionKind, KeywordSetKey.DECL_SPECIFIER_SEQUENCE );
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
	                    name = name(scope, completionKind, KeywordSetKey.EMPTY );
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
	            	name = name(scope, completionKind, KeywordSetKey.EMPTY );
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
		
		TypeId id = getTypeIdInstance(scope); 
		IToken last = lastToken;
		
		//template parameters are consumed as part of name
		//lastToken = consumeTemplateParameters( last );
		//if( lastToken == null ) lastToken = last;
		
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
			String signature = "";//$NON-NLS-1$
			if( lastToken != null )
				signature = TokenFactory.createStringRepresentation(mark, lastToken);
	        return astFactory.createTypeId( scope, kind, isConst, isVolatile, isShort, isLong, isSigned, isUnsigned, isTypename, name, id.getPointerOperators(), id.getArrayModifiers(), signature);
	    }
	    catch (ASTSemanticException e)
	    {
	        backup( mark );
	        throw backtrack;
	    } catch (Exception e)
	    {
	    	logException( "typeId::createTypeId()", e ); //$NON-NLS-1$
	        throw backtrack;
	    }
	}

	/**
	 * @param scope
	 * @return
	 */
	private TypeId getTypeIdInstance(IASTScope scope) {
		typeIdInstance.reset(scope);
		return typeIdInstance;
	}
	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression deleteExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
		if (LT(1) == IToken.tCOLONCOLON)
	    {
	        // global scope
	        consume(IToken.tCOLONCOLON);
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
	    IASTExpression castExpression = castExpression(scope,kind,key);
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
	            null, EMPTY_STRING, null); 
	    }
	    catch (ASTSemanticException e)
	    {
	        throw backtrack;
	    } catch (Exception e)
	    {
	    	logException( "deleteExpression::createExpression()", e ); //$NON-NLS-1$
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
	protected IASTExpression newExpression(IASTScope scope, KeywordSetKey key) throws BacktrackException, EndOfFileException {
		setCompletionValues(scope, CompletionKind.NEW_TYPE_REFERENCE, KeywordSetKey.EMPTY);
	    if (LT(1) == IToken.tCOLONCOLON)
	    {
	        // global scope
	        consume(IToken.tCOLONCOLON);
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
	    	if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	}
	        try
	        {
	            // Try to consume placement list
	            // Note: since expressionList and expression are the same...
	            backtrackMarker = mark();
				newPlacementExpressions.add(expression(scope, CompletionKind.SINGLE_NAME_REFERENCE, key));
	            consume(IToken.tRPAREN);
	            if( templateIdScopes != null ){ templateIdScopes.pop(); } //pop 1st Parent
	            placementParseFailure = false;
	            if (LT(1) == IToken.tLPAREN)
	            {
	                beforeSecondParen = mark();
	                consume(IToken.tLPAREN);
	                if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	} //push 2nd Paren
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
	            typeId = typeId(scope, true, CompletionKind.NEW_TYPE_REFERENCE );
	            consume(IToken.tRPAREN);
	            if( templateIdScopes != null ){	templateIdScopes.pop(); } //pop 1st Paren
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
	                        typeId = typeId(scope, true, CompletionKind.NEW_TYPE_REFERENCE);
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
	                    typeId = typeId(scope, true, CompletionKind.NEW_TYPE_REFERENCE);
	                    consume(IToken.tRPAREN);
	                    if( templateIdScopes != null ){	templateIdScopes.pop(); } //popping the 2nd Paren
	                    
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
								setCompletionValues(scope, CompletionKind.NO_SUCH_KIND, KeywordSetKey.EMPTY);
								return astFactory.createExpression(
									scope, IASTExpression.Kind.NEW_TYPEID, 
									null, null, null, typeId, null, 
									EMPTY_STRING, astFactory.createNewDescriptor(newPlacementExpressions, newTypeIdExpressions, newInitializerExpressions)); 
							}
							catch (ASTSemanticException e)
							{
								throw backtrack;
							} catch (Exception e)
	                        {
								logException( "newExpression_1::createExpression()", e ); //$NON-NLS-1$
	                            throw backtrack;
	                        }
	                    }
	                }
	                catch (BacktrackException e)
	                {
	                    // CASE: new (typeid-looking-as-placement)(initializer-not-looking-as-typeid)
	                    // Fallback to initializer processing
	                    backup(beforeSecondParen);
	                    if( templateIdScopes != null ){	templateIdScopes.pop(); }//pop that 2nd paren
	                }
	            }
	        }
	    }
	    else
	    {
	        // CASE: new typeid ...
	        // new parameters do not start with '('
	        // i.e it has to be a plain typeId
	        typeId = typeId(scope, true, CompletionKind.NEW_TYPE_REFERENCE);
	    }
	    while (LT(1) == IToken.tLBRACKET)
	    {
	        // array new
	        consume();
    	    
	        if( templateIdScopes != null ){	templateIdScopes.push( new Integer( IToken.tLBRACKET ) ); }
	        
			newTypeIdExpressions.add(assignmentExpression(scope, CompletionKind.SINGLE_NAME_REFERENCE,key));
	        consume(IToken.tRBRACKET);
	        
            if( templateIdScopes != null ){	templateIdScopes.pop(); }
	    }
	    // newinitializer
	    if (LT(1) == IToken.tLPAREN)
	    {
	        consume(IToken.tLPAREN);
	        setCurrentFunctionName( (( typeId != null ) ? typeId.getFullSignature() : EMPTY_STRING));
	        setCompletionValues( scope, CompletionKind.CONSTRUCTOR_REFERENCE  );
	        if( templateIdScopes != null ){	templateIdScopes.push( new Integer( IToken.tLPAREN ) ); }
	        
	        if ( queryLookaheadCapability() && (LT(1) != IToken.tRPAREN))
	        	newInitializerExpressions.add(expression(scope, CompletionKind.CONSTRUCTOR_REFERENCE, key));
	        
	        setCurrentFunctionName( EMPTY_STRING ); 
	        consume(IToken.tRPAREN);
	        if( templateIdScopes != null ){	templateIdScopes.pop(); }
	    }
	    setCompletionValues(scope, CompletionKind.NO_SUCH_KIND, KeywordSetKey.EMPTY);
		try
		{
	    return astFactory.createExpression(
	    	scope, IASTExpression.Kind.NEW_TYPEID, 
			null, null, null, typeId, null, 
			EMPTY_STRING, astFactory.createNewDescriptor(newPlacementExpressions, newTypeIdExpressions, newInitializerExpressions)); 
		}
		catch (ASTSemanticException e)
		{
			return null;
		} catch (Exception e)
	    {
			logException( "newExpression_2::createExpression()", e ); //$NON-NLS-1$
	        throw backtrack;
	    }
		
	}

	/**
	 * @param functionName 
	 */
	protected void setCurrentFunctionName(String functionName ) {
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	public IASTExpression unaryExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    switch (LT(1))
	    {
	        case IToken.tSTAR :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION,kind,key);
	        case IToken.tAMPER :
				consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION,kind,key);
	        case IToken.tPLUS :
				consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION,kind,key);
	        case IToken.tMINUS :
				consume();        
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION,kind,key);
	        case IToken.tNOT :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION,kind,key);
	        case IToken.tCOMPL :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION,kind,key);
	        case IToken.tINCR :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_INCREMENT,kind,key);
	        case IToken.tDECR :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_DECREMENT,kind,key);
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
	                    d = typeId(scope, false, CompletionKind.SINGLE_NAME_REFERENCE);
	                    consume(IToken.tRPAREN);
	                }
	                catch (BacktrackException bt)
	                {
	                    backup(mark);
	                    unaryExpression = unaryExpression(scope,kind, key);
	                }
	            }
	            else
	            {
	                unaryExpression = unaryExpression(scope,kind, key);
	            }
	            if (unaryExpression == null)
	                try
	                {
	                    return astFactory.createExpression(
	                        scope,
	                        IASTExpression.Kind.UNARY_SIZEOF_TYPEID,
	                        null,
	                        null,
	                        null,
	                        d,
	                        null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e)
	                {
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "unaryExpression_1::createExpression()", e ); //$NON-NLS-1$
	                    throw backtrack;
	                }
                try
                {
                    return astFactory.createExpression(
                        scope,
                        IASTExpression.Kind.UNARY_SIZEOF_UNARYEXPRESSION,
                        unaryExpression,
                        null,
                        null,
                        null,
                        null, EMPTY_STRING, null); 
                }
                catch (ASTSemanticException e1)
                {
                    throw backtrack;
                } catch (Exception e)
                {
                	logException( "unaryExpression_1::createExpression()", e ); //$NON-NLS-1$
                    throw backtrack;
                }
	        case IToken.t_new :
	            return newExpression(scope,key);
	        case IToken.t_delete :
	            return deleteExpression(scope,kind,key);
	        case IToken.tCOLONCOLON :
	        	if( queryLookaheadCapability(2))
	        	{
		            switch (LT(2))
		            {
		                case IToken.t_new :
		                    return newExpression(scope,key);
		                case IToken.t_delete :
		                    return deleteExpression(scope,kind,key);
		                default :
		                    return postfixExpression(scope,kind,key);
		            }
	        	}
	        default :
	        	if( extension.isValidUnaryExpressionStart( LT(1)))
	        	{
	        		IASTExpression extensionExpression = extension.parseUnaryExpression(scope,this,kind, key);
	        		if( extensionExpression != null ) return extensionExpression;
	        	}
	            return postfixExpression(scope,kind,key);
	    }
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression postfixExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    IASTExpression firstExpression = null;
	    boolean isTemplate = false;
	    
	    setCompletionValues( scope, kind, key );
	    switch (LT(1))
	    {
	        case IToken.t_typename :
	            consume(IToken.t_typename);
	            ITokenDuple nestedName = name(scope, CompletionKind.TYPE_REFERENCE, KeywordSetKey.EMPTY);
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
					templateId = TokenFactory.createTokenDuple( current, templateId(scope, CompletionKind.SINGLE_NAME_REFERENCE ) ); 
				}
				catch( BacktrackException bt )
				{
					if( templateTokenConsumed )
						throw bt;
					backup( current );
				}
				current = null;
	            consume( IToken.tLPAREN ); 
	            if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	}
	            IASTExpression expressionList = expression( scope, CompletionKind.TYPE_REFERENCE, key ); 
	            consume( IToken.tRPAREN );
	            if( templateIdScopes != null ){ templateIdScopes.pop();	}
	            try {
					firstExpression = 
						astFactory.createExpression( scope, 
													(( templateId != null )? IASTExpression.Kind.POSTFIX_TYPENAME_TEMPLATEID : IASTExpression.Kind.POSTFIX_TYPENAME_IDENTIFIER ), 
													expressionList, 
													null, 
													null, 
													null, 
													nestedName,
													EMPTY_STRING,  
													null );
				} catch (ASTSemanticException ase ) {
					throw backtrack;
				} catch (Exception e)
	            {
					logException( "postfixExpression_1::createExpression()", e ); //$NON-NLS-1$
	                throw backtrack;
	            }
	            break;                
	            // simple-type-specifier ( assignment-expression , .. )
	        case IToken.t_char :
	            firstExpression =
	                simpleTypeConstructorExpression(scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_CHAR,key);
	            break;
	        case IToken.t_wchar_t :
	            firstExpression =
	                simpleTypeConstructorExpression(scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_WCHART,key);
	            break;
	        case IToken.t_bool :
	            firstExpression =
	                simpleTypeConstructorExpression(scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_BOOL,key);
	            break;
	        case IToken.t_short :
	            firstExpression =
	                simpleTypeConstructorExpression(scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_SHORT,key);
	            break;
	        case IToken.t_int :
	            firstExpression =
	                simpleTypeConstructorExpression(scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_INT,key);
	            break;
	        case IToken.t_long :
	            firstExpression =
	                simpleTypeConstructorExpression(scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_LONG,key);
	            break;
	        case IToken.t_signed :
	            firstExpression =
	                simpleTypeConstructorExpression(scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_SIGNED,key);
	            break;
	        case IToken.t_unsigned :
	            firstExpression =
	                simpleTypeConstructorExpression(scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_UNSIGNED,key);
	            break;
	        case IToken.t_float :
	            firstExpression =
	                simpleTypeConstructorExpression(scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_FLOAT,key);
	            break;
	        case IToken.t_double :
	            firstExpression =
	                simpleTypeConstructorExpression( scope,
	                    IASTExpression.Kind.POSTFIX_SIMPLETYPE_DOUBLE,key);
	            break;
	        case IToken.t_dynamic_cast :
	            firstExpression =
	                specialCastExpression(scope,
	                    IASTExpression.Kind.POSTFIX_DYNAMIC_CAST,key);
	            break;
	        case IToken.t_static_cast :
	            firstExpression =
	                specialCastExpression(scope,
	                    IASTExpression.Kind.POSTFIX_STATIC_CAST,key);
	            break;
	        case IToken.t_reinterpret_cast :
	            firstExpression =
	                specialCastExpression(scope,
	                    IASTExpression.Kind.POSTFIX_REINTERPRET_CAST,key);
	            break;
	        case IToken.t_const_cast :
	            firstExpression =
	                specialCastExpression(scope,
	                    IASTExpression.Kind.POSTFIX_CONST_CAST,key);
	            break;
	        case IToken.t_typeid :
	            consume();
	            consume(IToken.tLPAREN);
	            if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	}
	            boolean isTypeId = true;
	            IASTExpression lhs = null;
	            IASTTypeId typeId = null;
	            try
	            {
	                typeId = typeId(scope, false, CompletionKind.TYPE_REFERENCE);
	            }
	            catch (BacktrackException b)
	            {
	                isTypeId = false;
	                lhs = expression(scope, CompletionKind.TYPE_REFERENCE, key);
	            }
	            consume(IToken.tRPAREN);
	            if( templateIdScopes != null ){ templateIdScopes.pop();	}
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
	                        null, EMPTY_STRING, null); 
	            }
	            catch (ASTSemanticException e6)
	            {
	                failParse();
	                throw backtrack;
	            } catch (Exception e)
	            {
	            	logException( "postfixExpression_2::createExpression()", e ); //$NON-NLS-1$
	                throw backtrack;
	            }
	            break;
	        default :
	            firstExpression = primaryExpression(scope, kind, key);
	    }
	    IASTExpression secondExpression = null;
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tLBRACKET :
	                // array access
	                consume(IToken.tLBRACKET);
	            	if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLBRACKET ) );	}
	                secondExpression = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE, key);
	                consume(IToken.tRBRACKET);
	                if( templateIdScopes != null ){ templateIdScopes.pop();	}
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
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e2)
	                {
	                    failParse();
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "postfixExpression_3::createExpression()", e ); //$NON-NLS-1$
	                    throw backtrack;
	                }
	                break;
	            case IToken.tLPAREN :
	                // function call
	                consume(IToken.tLPAREN);
	            	IASTNode context = null;
	            	if( firstExpression != null )
	            	{
	            		if( firstExpression.getExpressionKind() == IASTExpression.Kind.ID_EXPRESSION )
	            			setCurrentFunctionName( firstExpression.getIdExpression() );
	            		else if( firstExpression.getRHSExpression() != null && 
	            				 firstExpression.getRHSExpression().getIdExpression() != null )
	            		{
	            			setCurrentFunctionName( firstExpression.getRHSExpression().getIdExpression() );
	            			context = astFactory.expressionToMostPreciseASTNode( scope, firstExpression.getLHSExpression() );
	            		}
	            	}
	            	
	            	if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	}
	            	setCompletionValues(scope, CompletionKind.FUNCTION_REFERENCE, context );
	                secondExpression = expression(scope, CompletionKind.FUNCTION_REFERENCE, key);
	                setCurrentFunctionName( EMPTY_STRING ); 
	                consume(IToken.tRPAREN);
	                if( templateIdScopes != null ){ templateIdScopes.pop();	}
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
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e3)
	                {
	                    failParse();
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "postfixExpression_4::createExpression()", e ); //$NON-NLS-1$
	                    throw backtrack;
	                }
	                break;
	            case IToken.tINCR :
	                consume(IToken.tINCR);
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
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e1)
	                {
	                    failParse();
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "postfixExpression_5::createExpression()", e ); //$NON-NLS-1$
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
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e4)
	                {
	                    failParse();
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "postfixExpression_6::createExpression()", e ); //$NON-NLS-1$
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

	                Kind memberCompletionKind = (isTemplate
                            ? IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS
                            : IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION);
		                															
		            setCompletionValues(scope, CompletionKind.MEMBER_REFERENCE,	KeywordSetKey.EMPTY, firstExpression, memberCompletionKind  );
	                secondExpression = primaryExpression(scope, CompletionKind.MEMBER_REFERENCE, key);
	                if( secondExpression != null && secondExpression.getExpressionKind() == Kind.ID_EXPRESSION && secondExpression.getIdExpression().indexOf( '~') != -1 )
	                	memberCompletionKind = Kind.POSTFIX_DOT_DESTRUCTOR;
	                
	                try
	                {
						firstExpression =
	                        astFactory.createExpression(
	                            scope,
	                            memberCompletionKind,
	                            firstExpression,
	                            secondExpression,
	                            null,
	                            null,
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e5)
	                {
	                    failParse();
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "postfixExpression_7::createExpression()", e ); //$NON-NLS-1$
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

                    Kind arrowCompletionKind = (isTemplate
                         ? IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP
                         : IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION);

		            setCompletionValues(scope, CompletionKind.MEMBER_REFERENCE,	KeywordSetKey.EMPTY, firstExpression, arrowCompletionKind );	                															
	                secondExpression = primaryExpression(scope, CompletionKind.MEMBER_REFERENCE, key);                 

	                if( secondExpression != null && secondExpression.getExpressionKind() == Kind.ID_EXPRESSION && secondExpression.getIdExpression().indexOf( '~') != -1 )
	                	arrowCompletionKind = Kind.POSTFIX_ARROW_DESTRUCTOR;
	                try
	                {
						firstExpression =
	                        astFactory.createExpression(
	                            scope,
	                            arrowCompletionKind,
	                            firstExpression,
	                            secondExpression,
	                            null,
	                            null,
	                            null, EMPTY_STRING, null); 
	                }
	                catch (ASTSemanticException e)
	                {
	                    failParse();
	                    throw backtrack;
	                } catch (Exception e)
	                {
	                	logException( "postfixExpression_8::createExpression()", e ); //$NON-NLS-1$
	                    throw backtrack;
	                }
	                break;
	            default :
	                return firstExpression;
	        }
	    }
	}

	/**
	 * @return
	 * @throws EndOfFileException
	 */
	protected boolean queryLookaheadCapability(int count) throws EndOfFileException {
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

	protected void checkEndOfFile() throws EndOfFileException {
		LA(1);
	}

	protected IASTExpression simpleTypeConstructorExpression(IASTScope scope, Kind type, KeywordSetKey key ) throws EndOfFileException, BacktrackException {
	    String typeName = consume().getImage();
	    consume(IToken.tLPAREN);
	    setCurrentFunctionName( typeName );
	    IASTExpression inside = expression(scope, CompletionKind.CONSTRUCTOR_REFERENCE, key);
	    setCurrentFunctionName( EMPTY_STRING );
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
	            null, EMPTY_STRING, null); 
	    }
	    catch (ASTSemanticException e)
	    {
	        failParse();
	        throw backtrack;
	    } catch (Exception e)
	    {
	    	logException( "simpleTypeConstructorExpression::createExpression()", e ); //$NON-NLS-1$
	        throw backtrack;
	    }
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression primaryExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    IToken t = null;
	    switch (LT(1))
	    {
	        // TO DO: we need more literals...
	        case IToken.tINTEGER :
	            t = consume();
	            try
	            {
            		return astFactory.createExpression( scope, IASTExpression.Kind.PRIMARY_INTEGER_LITERAL, null, null, null, null, null, t.getImage(), null ); 
	            }
	            catch (ASTSemanticException e1)
	            {
	                throw backtrack;
	            } catch (Exception e)
	            {
	            	logException( "primaryExpression_1::createExpression()", e ); //$NON-NLS-1$
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
	            	logException( "primaryExpression_2::createExpression()", e ); //$NON-NLS-1$
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
	            	logException( "primaryExpression_3::createExpression()", e ); //$NON-NLS-1$
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
	            	logException( "primaryExpression_4::createExpression()", e ); //$NON-NLS-1$
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
	            	logException( "primaryExpression_5::createExpression()", e ); //$NON-NLS-1$
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
	                    null, EMPTY_STRING, null); 
	            }
	            catch (ASTSemanticException e7)
	            {
	                throw backtrack;
	            } catch (Exception e)
	            {
	            	logException( "primaryExpression_6::createExpression()", e ); //$NON-NLS-1$
	                throw backtrack;
	            }
	        case IToken.tLPAREN :
	            consume();
	        	if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	}
	            IASTExpression lhs = expression(scope, kind, key);
	            consume(IToken.tRPAREN);
	            if( templateIdScopes != null ){ templateIdScopes.pop();	}
	            try
	            {
	                return astFactory.createExpression(
	                    scope,
	                    IASTExpression.Kind.PRIMARY_BRACKETED_EXPRESSION,
	                    lhs,
	                    null,
	                    null,
	                    null,
	                    null, EMPTY_STRING, null); 
	            }
	            catch (ASTSemanticException e6)
	            {
	                throw backtrack;
	            } catch (Exception e)
	            {
	            	logException( "primaryExpression_7::createExpression()", e ); //$NON-NLS-1$
	                throw backtrack;
	            }
	        case IToken.tIDENTIFIER :
	        case IToken.tCOLONCOLON :
	        case IToken.t_operator :
	        case IToken.tCOMPL:
	            ITokenDuple duple = null; 
	            
	            
	            try
	            {
					duple = name(scope, kind, key);
	            }
	            catch( BacktrackException bt )
	            {
	            	IToken mark = mark();
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
							operatorId(d, start, null, kind);
						else
						{
						   backup(mark);
						   throw backtrack;
						}
					 }
					 else if( LT(1) == IToken.t_operator )
					 	 operatorId( d, null, null, kind);
					 
					 duple = d.getNameDuple();
	            }
	            
	            try
	            {
	                return astFactory.createExpression(
	                    scope,
	                    IASTExpression.Kind.ID_EXPRESSION,
	                    null,
	                    null,
	                	null,
						null,
	                    duple, EMPTY_STRING, null); 
	            }
	            catch (ASTSemanticException e8)
	            {
	                throw backtrack;
	            } catch (Exception e)
	            {
	            	logException( "primaryExpression_8::createExpression()", e ); //$NON-NLS-1$
	                throw backtrack;
	            }
	        default :
	        	if( !queryLookaheadCapability(2) )
	        	{
	        		if( LA(1).canBeAPrefix() )
	        		{
	        			consume();
	        			checkEndOfFile();
	        		}
	        	}
				IASTExpression empty = null;
		        try {
					empty = astFactory.createExpression(
							scope,
							IASTExpression.Kind.PRIMARY_EMPTY,
							null,
							null,
							null,
							null,
							null, EMPTY_STRING, null); 
				} catch (ASTSemanticException e9) {
					// TODO Auto-generated catch block
					e9.printStackTrace();
					
				} catch( Exception e )
				{
					logException( "primaryExpression_9::createExpression()", e ); //$NON-NLS-1$
					throw backtrack;
				}
				return empty;
	    }
		
	}

	/**
	 * Fetches a token from the scanner. 
	 * 
	 * @return				the next token from the scanner
	 * @throws EndOfFileException	thrown when the scanner.nextToken() yields no tokens
	 */
	protected IToken fetchToken() throws EndOfFileException {
		if(limitReached) throw new EndOfFileException();
		
	    try
	    {
	    	IToken value = scanner.nextToken();
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
	    	TraceUtil.outputTrace(log, "ScannerException thrown : ", e.getProblem(), null, null, null); //$NON-NLS-1$
			log.errorLog( "Scanner Exception: " + e.getProblem().getMessage()); //$NON-NLS-1$
	        failParse(); 
	        return fetchToken();
	    }
	}

	/**
	 * @param value
	 */
	protected void handleNewToken(IToken value) {
	}

	protected void handleOffsetLimitException(OffsetLimitReachedException exception) throws EndOfFileException {
		// unexpected, throw EOF instead (equivalent)
		throw new EndOfFileException();
	}

	protected IASTExpression assignmentOperatorExpression(IASTScope scope, IASTExpression.Kind kind, IASTExpression lhs, CompletionKind completionKind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    consume();
	    IASTExpression assignmentExpression = assignmentExpression(scope,completionKind, key);
	
	    try
	    {
	        return astFactory.createExpression(
	            scope,
	            kind,
	            lhs,
				assignmentExpression,
	            null,
	            null,
	            null, EMPTY_STRING, null); 
	    }
	    catch (ASTSemanticException e)
	    {
	        throw backtrack;
	    } catch (Exception e)
	    {
	    	logException( "assignmentOperatorExpression::createExpression()", e ); //$NON-NLS-1$
	        throw backtrack;
	    }
	}

	protected void setCompletionValues(IASTScope scope, IASTCompletionNode.CompletionKind kind, KeywordSetKey key) throws EndOfFileException {	
	}

	protected void setCompletionValues(IASTScope scope, IASTCompletionNode.CompletionKind kind, KeywordSetKey key, IASTNode node, String prefix) throws EndOfFileException {	
	}

	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key, IASTExpression firstExpression, Kind expressionKind) throws EndOfFileException {
	}
	
	protected void setCompletionValues( IASTScope scope, CompletionKind kind, IToken first, IToken last, KeywordSetKey key ) throws EndOfFileException {
	}


	protected IASTExpression unaryOperatorCastExpression(IASTScope scope, IASTExpression.Kind kind, CompletionKind completionKind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    IASTExpression castExpression = castExpression(scope,completionKind,key);
	    try
	    {
	        return astFactory.createExpression(
	            scope,
	            kind,
	            castExpression,
	            null,
	            null,
	            null,
	            null, EMPTY_STRING, null); 
	    }
	    catch (ASTSemanticException e)
	    {
	        throw backtrack;
	    } catch (Exception e)
	    {
	    	logException( "unaryOperatorCastExpression::createExpression()", e ); //$NON-NLS-1$
	        throw backtrack;
	    }
	}

	protected IASTExpression specialCastExpression(IASTScope scope, IASTExpression.Kind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException {
	    consume();
	    consume(IToken.tLT);
	    IASTTypeId duple = typeId(scope, false, CompletionKind.TYPE_REFERENCE);
	    consume(IToken.tGT);
	    consume(IToken.tLPAREN);
	    IASTExpression lhs = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE, key);
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
	            null, EMPTY_STRING, null); 
	    }
	    catch (ASTSemanticException e)
	    {
	        throw backtrack;
	    } catch (Exception e)
	    {
	    	logException( "specialCastExpression::createExpression()", e ); //$NON-NLS-1$
	        throw backtrack;
	    }
	}

	public char[] getCurrentFilename() {
		return scanner.getCurrentFilename();
	}
	
	protected boolean parserTimeout(){
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserData#getLastToken()
	 */
	public IToken getLastToken() {
		return lastToken;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserData#getParserLanguage()
	 */
	public final ParserLanguage getParserLanguage() {
		return language;
	}
	/**
	 * Parse an identifier.  
	 * 
	 * @throws BacktrackException	request a backtrack
	 */
	public IToken identifier() throws EndOfFileException, BacktrackException {
	    IToken first = consume(IToken.tIDENTIFIER); // throws backtrack if its not that
	    return first;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IFilenameProvider#getCurrentFileIndex()
	 */
	public int getCurrentFileIndex() {
		return scanner.getCurrentFileIndex();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IFilenameProvider#getFilenameForIndex(int)
	 */
	public String getFilenameForIndex(int index) {
		return scanner.getFilenameForIndex(index);
	}
	
	public boolean validateCaches()
	{
		return true;
	}
		
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return scanner.toString(); //$NON-NLS-1$
	}
}
