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
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.Token;
import org.eclipse.cdt.internal.core.parser.token.TokenDuple;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets.Key;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;

/**
 * @author jcamelon
 */
public class ExpressionParser implements IExpressionParser {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected final IParserLogService log;
	private static int FIRST_ERROR_OFFSET_UNSET = -1;
	protected int firstErrorOffset = FIRST_ERROR_OFFSET_UNSET;
	protected boolean parsePassed = true;
	protected ParserLanguage language = ParserLanguage.CPP;
	protected IASTFactory astFactory = null;
	
	private Stack templateIdScopes = null;


	/**
	 * @param scanner2
	 * @param callback
	 * @param language2
	 * @param log2
	 */
	public ExpressionParser(IScanner scanner, ParserLanguage language, IParserLogService log) {
		this.scanner = scanner;
		this.language = language; 
		this.log = log;
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

	protected List templateArgumentList( IASTScope scope ) throws EndOfFileException, BacktrackException
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
        		IASTTypeId typeId = typeId( scope, false, CompletionKind.TYPE_REFERENCE );
        		
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
	        		expression = assignmentExpression( scope, CompletionKind.VARIABLE_TYPE ); 
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
	        		ITokenDuple nameDuple = name( scope, null );
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
	    ITokenDuple duple = name(scope, kind );
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
	 * @throws BacktrackException	request a backtrack
	 */
	protected ITokenDuple name(IASTScope scope, IASTCompletionNode.CompletionKind kind ) throws BacktrackException, EndOfFileException {
		
	    IToken first = LA(1);
	    IToken last = null;
	    IToken mark = mark();
	    
	    List argumentList = new LinkedList();
	    boolean hasTemplateId = false;
	    boolean startsWithColonColon = false;
        
        if (LT(1) == IToken.tCOLONCOLON){
        	argumentList.add( null );
            last = consume( IToken.tCOLONCOLON );
            setCompletionValues( scope, kind, Key.EMPTY, getCompliationUnit() );
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
            		setCompletionValues(scope, kind, first, prev );
            	else
            		setCompletionValues(scope, kind );
            	
                last = consumeTemplateArguments(scope, last, argumentList);
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
            setCompletionValues( scope, kind, first, prev );
            
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
                    setCompletionValues( scope, kind, first, prev );
		            last = consumeTemplateArguments(scope, last, argumentList);
		            if( last.getType() == IToken.tGT )
		            	hasTemplateId = true;
            }
        }

        return new TokenDuple(first, last, ( hasTemplateId ? argumentList : null ) );

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
	protected void setCompletionValues(IASTScope scope, CompletionKind kind, Key key, IASTNode node) throws EndOfFileException
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
	protected IToken consumeTemplateArguments(IASTScope scope, IToken last, List argumentList) throws EndOfFileException, BacktrackException {
		if( LT(1) == IToken.tLT ){
			IToken secondMark = mark();
			consume( IToken.tLT );
		    try
		    {
		    	List list = templateArgumentList( scope );
		    	argumentList.add( list );
		    	last = consume( IToken.tGT );
		    } catch( BacktrackException bt )
		    {
		    	argumentList.add( null );
		    	backup( secondMark );
		    }
		} else {
			argumentList.add( null );
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
	        	else 
	        		throw backtrack;
	        default :
	            
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
	            exp = constantExpression(scope, CompletionKind.SINGLE_NAME_REFERENCE);
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

	protected void operatorId(Declarator d, IToken originalToken, List templateArgs) throws BacktrackException, EndOfFileException {
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
	    
	    List args = ( templateArgs != null ) ? templateArgs : new LinkedList();
	    boolean hasTemplateId = ( templateArgs != null );

	    toSend = consumeTemplateArguments( d.getDeclarationWrapper().getScope(), toSend, args );
	    if( toSend.getType() == IToken.tGT ){
	    	hasTemplateId = true;
	    }
	    
	    ITokenDuple duple =
	        new TokenDuple( originalToken == null ? operatorToken : originalToken, toSend, (hasTemplateId ? args : null ) );
	
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
		            nameDuple = name(d.getScope(), CompletionKind.USER_SPECIFIED_NAME );
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
	        backup(mark);
	        return result;
		}
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression constantExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    return conditionalExpression(scope,kind);
	}

	public IASTExpression expression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression assignmentExpression = assignmentExpression(scope,kind);
	    while (LT(1) == IToken.tCOMMA)
	    {
	        consume();
	        IASTExpression secondExpression = assignmentExpression(scope,kind);
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
	            throw backtrack;
	        }
	    }
	    return assignmentExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression assignmentExpression(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
		setCompletionValues(scope, kind, Key.EXPRESSION );
		if (LT(1) == IToken.t_throw) {
			return throwExpression(scope);
		}
		IASTExpression conditionalExpression = conditionalExpression(scope,kind);
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
					conditionalExpression, kind);
			case IToken.tSTARASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_MULT,
					conditionalExpression, kind);
			case IToken.tDIVASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_DIV,
					conditionalExpression, kind);
			case IToken.tMODASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_MOD,
					conditionalExpression, kind);
			case IToken.tPLUSASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_PLUS,
					conditionalExpression, kind);
			case IToken.tMINUSASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_MINUS,
					conditionalExpression, kind);
			case IToken.tSHIFTRASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_RSHIFT,
					conditionalExpression, kind);
			case IToken.tSHIFTLASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_LSHIFT,
					conditionalExpression, kind);
			case IToken.tAMPERASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_AND,
					conditionalExpression, kind);
			case IToken.tXORASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_XOR,
					conditionalExpression, kind);
			case IToken.tBITORASSIGN :
				return assignmentOperatorExpression(
					scope,
					IASTExpression.Kind.ASSIGNMENTEXPRESSION_OR,
					conditionalExpression, kind);
		}
		return conditionalExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression throwExpression(IASTScope scope) throws EndOfFileException, BacktrackException {
	    consume(IToken.t_throw);
	    setCompletionValues( scope, CompletionKind.SINGLE_NAME_REFERENCE, Key.EXPRESSION );
	    IASTExpression throwExpression = null;
	    try
	    {
	        throwExpression = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE);
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
	        throw backtrack;
	    }
	}

	/**
	 * @param expression
	 * @return
	 * @throws BacktrackException
	 */
	protected IASTExpression conditionalExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = logicalOrExpression(scope,kind);
	    if (LT(1) == IToken.tQUESTION)
	    {
	        consume();
	        IASTExpression secondExpression = expression(scope,kind);
	        consume(IToken.tCOLON);
	        IASTExpression thirdExpression = assignmentExpression(scope,kind);
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
	protected IASTExpression logicalOrExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = logicalAndExpression(scope,kind);
	    while (LT(1) == IToken.tOR)
	    {
	        consume();
	        IASTExpression secondExpression = logicalAndExpression(scope,kind);
	
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
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression logicalAndExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = inclusiveOrExpression( scope,kind );
	    while (LT(1) == IToken.tAND)
	    {
	        consume();
	        IASTExpression secondExpression = inclusiveOrExpression( scope,kind );
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
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression inclusiveOrExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = exclusiveOrExpression(scope,kind);
	    while (LT(1) == IToken.tBITOR)
	    {
	        consume();
	        IASTExpression secondExpression = exclusiveOrExpression(scope,kind);
	
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
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression exclusiveOrExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = andExpression( scope,kind );
	    while (LT(1) == IToken.tXOR)
	    {
	        consume();
	        
	        IASTExpression secondExpression = andExpression( scope,kind );
	
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
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression andExpression(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
	    IASTExpression firstExpression = equalityExpression(scope,kind);
	    while (LT(1) == IToken.tAMPER)
	    {
	        consume();
	        IASTExpression secondExpression = equalityExpression(scope,kind);
	
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
	            throw backtrack;
	        }
	    }
	    return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression equalityExpression(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
	    IASTExpression firstExpression = relationalExpression(scope,kind);
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tEQUAL :
	            case IToken.tNOTEQUAL :
	                IToken t = consume();
	                IASTExpression secondExpression =
	                    relationalExpression(scope,kind);
	
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
	protected IASTExpression relationalExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = shiftExpression(scope,kind);
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
	                IToken t = consume();
	                IToken next = LA(1);
	                IASTExpression secondExpression =
	                    shiftExpression(scope,kind);
	                if (next == LA(1))
	                {
	                    // we did not consume anything
	                    // this is most likely an error
	                    backup(mark);
	                    return firstExpression;
	                }
	                else
	                {
	                    IASTExpression.Kind expressionKind = null;
	                    switch (t.getType())
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
	protected IASTExpression shiftExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = additiveExpression(scope,kind);
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tSHIFTL :
	            case IToken.tSHIFTR :
	                IToken t = consume();
	                IASTExpression secondExpression =
	                    additiveExpression(scope,kind);
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
	protected IASTExpression additiveExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = multiplicativeExpression( scope, kind );
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tPLUS :
	            case IToken.tMINUS :
	                IToken t = consume();
	                IASTExpression secondExpression =
	                    multiplicativeExpression(scope,kind);
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
	protected IASTExpression multiplicativeExpression(IASTScope scope, CompletionKind kind) throws BacktrackException, EndOfFileException {
	    IASTExpression firstExpression = pmExpression(scope,kind);
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tSTAR :
	            case IToken.tDIV :
	            case IToken.tMOD :
	                IToken t = consume();
	                IASTExpression secondExpression = pmExpression(scope,kind);
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
	protected IASTExpression pmExpression(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
	    IASTExpression firstExpression = castExpression(scope,kind);
	    for (;;)
	    {
	        switch (LT(1))
	        {
	            case IToken.tDOTSTAR :
	            case IToken.tARROWSTAR :
	                IToken t = consume();
	                IASTExpression secondExpression =
	                    castExpression(scope,kind);
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
	protected IASTExpression castExpression(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
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
	            typeId = typeId(scope, false, CompletionKind.TYPE_REFERENCE);
	            consume(IToken.tRPAREN);
	            if( templateIdScopes != null ){ templateIdScopes.pop();	popped = true;}
	            IASTExpression castExpression = castExpression(scope,kind);
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
	                throw backtrack;
	            }
	        }
	        catch (BacktrackException b)
	        {
	            backup(mark);
	            if( templateIdScopes != null && !popped ){ templateIdScopes.pop();	}
	        }
	    }
	    return unaryExpression(scope,kind);
	    
	}

	/**
	 * @param completionKind TODO
	 * @throws BacktrackException
	 */
	protected IASTTypeId typeId(IASTScope scope, boolean skipArrayModifiers, CompletionKind completionKind) throws EndOfFileException, BacktrackException {
		IToken mark = mark();
		IToken start = mark;
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
	            name  = name(scope, completionKind );
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
	                    name = name(scope, completionKind );
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
	            	name = name(scope, completionKind );
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
			String signature = "";
			if( start != null && lastToken != null )
				signature = new TokenDuple( start, lastToken ).toString();
	        return astFactory.createTypeId( scope, kind, isConst, isVolatile, isShort, isLong, isSigned, isUnsigned, isTypename, name, id.getPointerOperators(), id.getArrayModifiers(), signature);
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
	protected IASTExpression deleteExpression(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
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
	    IASTExpression castExpression = castExpression(scope,kind);
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
	protected IASTExpression newExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
		setCompletionValues(scope, CompletionKind.NEW_TYPE_REFERENCE, Key.EMPTY);
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
				newPlacementExpressions.add(expression(scope, CompletionKind.SINGLE_NAME_REFERENCE));
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
								setCompletionValues(scope, CompletionKind.NO_SUCH_KIND, Key.EMPTY);
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
	        
			newTypeIdExpressions.add(assignmentExpression(scope, CompletionKind.SINGLE_NAME_REFERENCE));
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
	        	newInitializerExpressions.add(expression(scope, CompletionKind.CONSTRUCTOR_REFERENCE));
	        
	        setCurrentFunctionName( EMPTY_STRING ); 
	        consume(IToken.tRPAREN);
	        if( templateIdScopes != null ){	templateIdScopes.pop(); }
	    }
	    setCompletionValues(scope, CompletionKind.NO_SUCH_KIND, Key.EMPTY);
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
	protected IASTExpression unaryExpression(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
	    switch (LT(1))
	    {
	        case IToken.tSTAR :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION,kind);
	        case IToken.tAMPER :
				consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION,kind);
	        case IToken.tPLUS :
				consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION,kind);
	        case IToken.tMINUS :
				consume();        
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION,kind);
	        case IToken.tNOT :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION,kind);
	        case IToken.tCOMPL :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION,kind);
	        case IToken.tINCR :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_INCREMENT,kind);
	        case IToken.tDECR :
	        	consume();
	            return unaryOperatorCastExpression(scope,
	                IASTExpression.Kind.UNARY_DECREMENT,kind);
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
	                    d = typeId(scope, false, CompletionKind.TYPE_REFERENCE);
	                    consume(IToken.tRPAREN);
	                }
	                catch (BacktrackException bt)
	                {
	                    backup(mark);
	                    unaryExpression = unaryExpression(scope,kind);
	                }
	            }
	            else
	            {
	                unaryExpression = unaryExpression(scope,kind);
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
	                        null, EMPTY_STRING, null); 
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
	                        null, EMPTY_STRING, null); 
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
	            return deleteExpression(scope,kind);
	        case IToken.tCOLONCOLON :
	        	if( queryLookaheadCapability(2))
	        	{
		            switch (LT(2))
		            {
		                case IToken.t_new :
		                    return newExpression(scope);
		                case IToken.t_delete :
		                    return deleteExpression(scope,kind);
		                default :
		                    return postfixExpression(scope,kind);
		            }
	        	}
	        default :
	            return postfixExpression(scope,kind);
	    }
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression postfixExpression(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
	    IASTExpression firstExpression = null;
	    boolean isTemplate = false;
	    
	    setCompletionValues( scope, kind, Key.EXPRESSION );
	    switch (LT(1))
	    {
	        case IToken.t_typename :
	            consume(IToken.t_typename);
	            ITokenDuple nestedName = name(scope, CompletionKind.TYPE_REFERENCE);
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
					templateId = new TokenDuple( current, templateId(scope, CompletionKind.SINGLE_NAME_REFERENCE ) ); 
				}
				catch( BacktrackException bt )
				{
					if( templateTokenConsumed )
						throw bt;
					backup( current );
				}
	            consume( IToken.tLPAREN ); 
	            if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	}
	            IASTExpression expressionList = expression( scope, CompletionKind.TYPE_REFERENCE ); 
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
	                lhs = expression(scope, CompletionKind.TYPE_REFERENCE);
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
	                throw backtrack;
	            }
	            break;
	        default :
	            firstExpression = primaryExpression(scope, kind);
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
	                secondExpression = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE);
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
	            			context = astFactory.expressionToASTNode( scope, firstExpression.getLHSExpression() );
	            		}
	            	}
	            	
	            	if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	}
	            	setCompletionValues(scope, CompletionKind.FUNCTION_REFERENCE, context );
	                secondExpression = expression(scope, CompletionKind.FUNCTION_REFERENCE);
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
	                															
	                secondExpression = primaryExpression(scope, CompletionKind.MEMBER_REFERENCE);
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
	                            null, EMPTY_STRING, null); 
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
	                															
	                secondExpression = primaryExpression(scope, CompletionKind.MEMBER_REFERENCE);
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
	                            null, EMPTY_STRING, null); 
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

	protected IASTExpression simpleTypeConstructorExpression(IASTScope scope, Kind type ) throws EndOfFileException, BacktrackException {
	    String typeName = consume().getImage();
	    consume(IToken.tLPAREN);
	    setCurrentFunctionName( typeName );
	    IASTExpression inside = expression(scope, CompletionKind.CONSTRUCTOR_REFERENCE);
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
	        throw backtrack;
	    }
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression primaryExpression(IASTScope scope, CompletionKind kind) throws EndOfFileException, BacktrackException {
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
	                    null, EMPTY_STRING, null); 
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
	        	if( templateIdScopes != null ){ templateIdScopes.push( new Integer( IToken.tLPAREN ) );	}
	            IASTExpression lhs = expression(scope, kind);
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
	                throw backtrack;
	            }
	        case IToken.tIDENTIFIER :
	        case IToken.tCOLONCOLON :
	        case IToken.t_operator : 
	            ITokenDuple duple = null; 
	            
	            IToken mark = mark();
	            try
	            {
					duple = name(scope, kind);
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
							operatorId(d, start, null);
						else
						{
						   backup(mark);
						   throw backtrack;
						}
					 }
					 else if( LT(1) == IToken.t_operator )
					 	 operatorId( d, null, null);
					 
					 duple = d.getNameDuple();
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
	                    duple, EMPTY_STRING, null); 
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
							null, EMPTY_STRING, null); 
				} catch (ASTSemanticException e9) {
					// TODO Auto-generated catch block
					e9.printStackTrace();
					
				}
				return empty;
	    }
		
	}

	protected static BacktrackException backtrack = new BacktrackException();
	protected IScanner scanner;
	protected IToken currToken;
	protected IToken lastToken;
	private boolean limitReached = false;

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

	/**
	 * Look Ahead in the token list to see what is coming.  
	 * 
	 * @param i		How far ahead do you wish to peek?
	 * @return		the token you wish to observe
	 * @throws EndOfFileException	if looking ahead encounters EOF, throw EndOfFile 
	 */
	protected IToken LA(int i) throws EndOfFileException {
		
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
	protected int LT(int i) throws EndOfFileException {
	    return LA(i).getType();
	}

	/**
	 * Consume the next token available, regardless of the type.  
	 * 
	 * @return				The token that was consumed and removed from our buffer.  
	 * @throws EndOfFileException	If there is no token to consume.  
	 */
	protected IToken consume() throws EndOfFileException {
		
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
	protected IToken consume(int type) throws EndOfFileException, BacktrackException {
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
	protected IToken mark() throws EndOfFileException {
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
	protected void backup(IToken mark) {
	    currToken = (Token)mark;
	    lastToken = null; // this is not entirely right ... 
	}

	protected IASTExpression assignmentOperatorExpression(IASTScope scope, IASTExpression.Kind kind, IASTExpression lhs, CompletionKind completionKind) throws EndOfFileException, BacktrackException {
	    consume();
	    IASTExpression assignmentExpression = assignmentExpression(scope,completionKind);
	
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
	        throw backtrack;
	    }
	}

	protected void setCompletionValues(IASTScope scope, IASTCompletionNode.CompletionKind kind, KeywordSets.Key key) throws EndOfFileException {	
	}

	protected void setCompletionValues(IASTScope scope, IASTCompletionNode.CompletionKind kind, KeywordSets.Key key, IASTNode node, String prefix) throws EndOfFileException {	
	}

	protected void setCompletionValues(IASTScope scope, CompletionKind kind, Key key, IASTExpression firstExpression, boolean isTemplate) throws EndOfFileException {
	}
	
	protected void setCompletionValues( IASTScope scope, CompletionKind kind, IToken first, IToken last ) throws EndOfFileException {
	}


	protected IASTExpression unaryOperatorCastExpression(IASTScope scope, IASTExpression.Kind kind, CompletionKind completionKind) throws EndOfFileException, BacktrackException {
	    IASTExpression castExpression = castExpression(scope,completionKind);
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
	        throw backtrack;
	    }
	}

	protected IASTExpression specialCastExpression(IASTScope scope, IASTExpression.Kind kind) throws EndOfFileException, BacktrackException {
	    consume();
	    consume(IToken.tLT);
	    IASTTypeId duple = typeId(scope, false, CompletionKind.TYPE_REFERENCE);
	    consume(IToken.tGT);
	    consume(IToken.tLPAREN);
	    IASTExpression lhs = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE);
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
	        throw backtrack;
	    }
	}

	public char[] getCurrentFilename() {
		return scanner.getCurrentFilename();
	}
	
	protected boolean parserTimeout(){
		return false;
	}
}
