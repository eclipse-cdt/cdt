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
import java.util.Stack;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
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

/**
 * @author jcamelon
 */
public class ExpressionParser implements IExpressionParser {

	protected final IParserLogService log;
	private static int FIRST_ERROR_OFFSET_UNSET = -1;
	protected int firstErrorOffset = FIRST_ERROR_OFFSET_UNSET;
	protected boolean parsePassed = true;
	protected ParserLanguage language = ParserLanguage.CPP;
	protected IASTFactory astFactory = null;

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
	 * : template-id
	 * 
	 * @throws BacktrackException	request a backtrack
	 */
	protected TokenDuple name(IASTScope scope, IASTCompletionNode.CompletionKind kind) throws BacktrackException, EndOfFileException {
		
	    IToken first = LA(1);
	    IToken last = null;
	    IToken mark = mark();
	    
        if (LT(1) == IToken.tCOLONCOLON)
            last = consume( IToken.tCOLONCOLON );

        if (LT(1) == IToken.tCOMPL)
            consume();
        
        switch (LT(1))
        {
            case IToken.tIDENTIFIER :
                last = consume(IToken.tIDENTIFIER);
                IToken secondMark = null;
                
                secondMark = mark();
                
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

	protected void operatorId(Declarator d, IToken originalToken) throws BacktrackException, EndOfFileException {
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
		            nameDuple = name(d.getScope(), CompletionKind.NO_SUCH_KIND );
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
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression constantExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
	    return conditionalExpression(scope);
	}

	public IASTExpression expression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                    null, "", null); //$NON-NLS-1$
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
	protected IASTExpression assignmentExpression(IASTScope scope) throws EndOfFileException, BacktrackException {
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

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression throwExpression(IASTScope scope) throws EndOfFileException, BacktrackException {
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
	            null, "", null); //$NON-NLS-1$
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
	protected IASTExpression conditionalExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                null, "", null); //$NON-NLS-1$
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
	protected IASTExpression logicalOrExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                    null, "", null); //$NON-NLS-1$
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
	protected IASTExpression logicalAndExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                    null, "", null); //$NON-NLS-1$
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
	protected IASTExpression inclusiveOrExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                    null, "", null); //$NON-NLS-1$
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
	protected IASTExpression exclusiveOrExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                    null, "", null); //$NON-NLS-1$
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
	protected IASTExpression andExpression(IASTScope scope) throws EndOfFileException, BacktrackException {
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
	                    null, "", null); //$NON-NLS-1$
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
	protected IASTExpression equalityExpression(IASTScope scope) throws EndOfFileException, BacktrackException {
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
	                            null, "", null); //$NON-NLS-1$
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
	protected IASTExpression relationalExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                                null, "", null); //$NON-NLS-1$
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
	protected IASTExpression shiftExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                            null, "", null); //$NON-NLS-1$
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
	protected IASTExpression additiveExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                            null, "", null); //$NON-NLS-1$
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
	protected IASTExpression multiplicativeExpression(IASTScope scope) throws BacktrackException, EndOfFileException {
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
	                            null, "", null); //$NON-NLS-1$
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
	protected IASTExpression pmExpression(IASTScope scope) throws EndOfFileException, BacktrackException {
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
	                            null, "", null); //$NON-NLS-1$
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
	protected IASTExpression castExpression(IASTScope scope) throws EndOfFileException, BacktrackException {
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
	                    null, "", null); //$NON-NLS-1$
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
	protected IASTTypeId typeId(IASTScope scope, boolean skipArrayModifiers) throws EndOfFileException, BacktrackException {
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
	            name  = name(scope, CompletionKind.TYPE_REFERENCE );
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
	                    name = name(scope, CompletionKind.TYPE_REFERENCE);
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
	            	name = name(scope, CompletionKind.TYPE_REFERENCE );
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
	protected IASTExpression deleteExpression(IASTScope scope) throws EndOfFileException, BacktrackException {    	
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
	            null, "", null); //$NON-NLS-1$
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
									"", astFactory.createNewDescriptor(newPlacementExpressions, newTypeIdExpressions, newInitializerExpressions)); //$NON-NLS-1$
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
			"", astFactory.createNewDescriptor(newPlacementExpressions, newTypeIdExpressions, newInitializerExpressions)); //$NON-NLS-1$
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
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression unaryExpression(IASTScope scope) throws EndOfFileException, BacktrackException {
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
	                        null, "", null); //$NON-NLS-1$
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
	                        null, "", null); //$NON-NLS-1$
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
	protected IASTExpression postfixExpression(IASTScope scope) throws EndOfFileException, BacktrackException {
	    IASTExpression firstExpression = null;
	    boolean isTemplate = false;
	    checkEndOfFile();
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
					templateId = new TokenDuple( current, templateId(scope, CompletionKind.SINGLE_NAME_REFERENCE
							) ); 
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
													"",  //$NON-NLS-1$
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
	                        null, "", null); //$NON-NLS-1$
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
	            firstExpression = primaryExpression(scope, CompletionKind.SINGLE_NAME_REFERENCE);
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
	                            null, "", null); //$NON-NLS-1$
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
	                            null, "", null); //$NON-NLS-1$
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
	                            null, "", null); //$NON-NLS-1$
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
	                            null, "", null); //$NON-NLS-1$
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
	                            null, "", null); //$NON-NLS-1$
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
	                            null, "", null); //$NON-NLS-1$
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

	protected IASTExpression simpleTypeConstructorExpression(IASTScope scope, Kind type) throws EndOfFileException, BacktrackException {
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
	            null, "", null); //$NON-NLS-1$
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
	                    null, "", null); //$NON-NLS-1$
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
	                    null, "", null); //$NON-NLS-1$
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
	                    duple, "", null); //$NON-NLS-1$
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
							null, "", null); //$NON-NLS-1$
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
	        log.traceLog( "ScannerException thrown : " + e.getProblem().getMessage() ); //$NON-NLS-1$
			log.errorLog( "Scanner Exception: " + e.getProblem().getMessage()); //$NON-NLS-1$h
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

	protected IASTExpression assignmentOperatorExpression(IASTScope scope, IASTExpression.Kind kind, IASTExpression lhs) throws EndOfFileException, BacktrackException {
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
	            null, "", null); //$NON-NLS-1$
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

	protected IASTExpression unaryOperatorCastExpression(IASTScope scope, IASTExpression.Kind kind) throws EndOfFileException, BacktrackException {
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
	            null, "", null); //$NON-NLS-1$
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
	            null, "", null); //$NON-NLS-1$
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

}
