/**********************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTArrayModifier;
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
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceAlias;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableElement;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTReference;
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
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier.ClassNameType;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.ast.ASTCompletionNode;
import org.eclipse.cdt.internal.core.parser.ast.complete.CompleteParseASTFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.pst.ISymbolOwner;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.OffsetDuple;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;

/**
 * This is our first implementation of the IParser interface, serving as a
 * parser for ANSI C and C++.
 * 
 * From time to time we will make reference to the ANSI ISO specifications.
 * 
 * @author jcamelon
 */
public class Parser implements IParserData, IParser 
{
	protected final ParserMode mode;
	protected static final char[] EMPTY_STRING = "".toCharArray(); //$NON-NLS-1$
	private static int FIRST_ERROR_UNSET = -1;
	protected boolean parsePassed = true;
	protected int firstErrorOffset = FIRST_ERROR_UNSET;
	protected int firstErrorLine = FIRST_ERROR_UNSET;
	private BacktrackException backtrack = new BacktrackException();
	private int backtrackCount = 0;
	private char[] parserStartFilename = null;

	protected final void throwBacktrack( IProblem problem ) throws BacktrackException {
		++backtrackCount;
		backtrack.initialize( problem );
		throw backtrack;
	}

	protected final void throwBacktrack( int startingOffset, int endingOffset, int lineNumber, char[] f ) throws BacktrackException {
		++backtrackCount;		
		backtrack.initialize( startingOffset, ( endingOffset == 0 ) ? startingOffset + 1 : endingOffset, lineNumber, f );
		throw backtrack;
	}

	protected final IParserExtension extension;

	//TODO this stuff needs to be encapsulated by IParserData
	protected final IParserLogService log;
	protected ParserLanguage language = ParserLanguage.CPP;
	protected IASTFactory astFactory = null;
	protected IScanner scanner;
	protected IToken currToken;
	protected IToken lastToken;
	private boolean limitReached = false;
	private ScopeStack templateIdScopes = new ScopeStack();
	private TypeId typeIdInstance = new TypeId();

	private static class ScopeStack {
	    private int [] stack;
	    private int index = -1;
	    
	    public ScopeStack(){
	        stack = new int [8];
	    }
	    
	    private void grow(){
	        int [] newStack = new int[ stack.length << 1 ];
	        System.arraycopy( stack, 0, newStack, 0, stack.length );
	        stack = newStack;
	    }
	    
	    final public void push( int i ){
	        if( ++index == stack.length )
	            grow();
	        stack[index] = i;
	    }
	    final public int pop(){
	        if( index >= 0 )
	            return stack[index--];
	        return -1;
	    }
	    final public int peek(){
	        if( index >= 0 )
	            return stack[index];
	        return -1;
	    }
	    final public int size(){
	        return index + 1;
	    }
	}
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

		if (isCancelled) {
			throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
		}

		if (i < 1) // can't go backwards
			return null;
		if (currToken == null)
			currToken = fetchToken();
		IToken retToken = currToken;
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
		handleNewToken(lastToken);
		return lastToken;
	}

	/**
	 * Consume the next token available only if the type is as specified.  
	 * 
	 * @param type			The type of token that you are expecting.  	
	 * @return				the token that was consumed and removed from our buffer. 
	 * @throws BacktrackException	If LT(1) != type 
	 */
	public IToken consume(int type) throws EndOfFileException,
			BacktrackException {
		if (LT(1) == type)
			return consume();
		IToken la = LA(1);
		throwBacktrack(la.getOffset(), la.getEndOffset(), la.getLineNumber(), la.getFilename());
		return null;
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
	 * This is the single entry point for setting parsePassed to 
	 * false, and also making note what token offset we failed upon. 
	 * 
	 * @throws EndOfFileException
	 */
	protected void failParse() {
		try {
			if (firstErrorOffset == FIRST_ERROR_UNSET){
				firstErrorOffset = LA(1).getOffset();
				firstErrorLine = LA(1).getLineNumber();
			}
		} catch (EndOfFileException eof) {
			// do nothing
		} finally {
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
	protected IToken consumeTemplateParameters(IToken previousLast)
			throws EndOfFileException, BacktrackException {
		if (language != ParserLanguage.CPP)
			return previousLast;
		int startingOffset = previousLast == null ? lastToken.getOffset() : previousLast.getOffset();
		IToken last = previousLast;
		if (LT(1) == IToken.tLT) {
			last = consume(IToken.tLT);
			// until we get all the names sorted out
			ScopeStack scopes = new ScopeStack();
			scopes.push(IToken.tLT);

			while (scopes.size() > 0) {
				int top;
				last = consume();

				switch (last.getType()) {
					case IToken.tGT :
						if (scopes.peek() == IToken.tLT) {
							scopes.pop();
						}
						break;
					case IToken.tRBRACKET :
						do {
							top = scopes.pop();
						} while (scopes.size() > 0
								&& (top == IToken.tGT || top == IToken.tLT));
						if (top != IToken.tLBRACKET)
							throwBacktrack(startingOffset, last.getEndOffset(), last.getLineNumber(), last.getFilename());

						break;
					case IToken.tRPAREN :
						do {
							top = scopes.pop();
						} while (scopes.size() > 0
								&& (top == IToken.tGT || top == IToken.tLT));
						if (top != IToken.tLPAREN)
							throwBacktrack(startingOffset, last.getEndOffset(), last.getLineNumber(), last.getFilename());

						break;
					case IToken.tLT :
					case IToken.tLBRACKET :
					case IToken.tLPAREN :
						scopes.push(last.getType());
						break;
				}
			}
		}
		return last;
	}

	protected List templateArgumentList(IASTScope scope,
			IASTCompletionNode.CompletionKind kind) throws EndOfFileException,
			BacktrackException {
		IToken start = LA(1);
		int startingOffset = start.getOffset();
		int startingLineNumber = start.getOffset();
		char [] fn = start.getFilename();
		start = null;
		IASTExpression expression = null;
		List list = new ArrayList();

		boolean completedArg = false;
		boolean failed = false;

		templateIdScopes.push( IToken.tLT );

		while (LT(1) != IToken.tGT) {
			completedArg = false;

			IToken mark = mark();

			try {
				IASTTypeId typeId = typeId(scope, false, kind);

				expression = astFactory.createExpression(scope,
						IASTExpression.Kind.POSTFIX_TYPEID_TYPEID, null, null,
						null, typeId, null, EMPTY_STRING, null, (ITokenDuple)start);
				list.add(expression);
				completedArg = true;
			} catch (BacktrackException e) {
				backup(mark);
			} catch (ASTSemanticException e) {
				backup(mark);
			}

			if (!completedArg) {
				try {
					IToken la = LA(1);
					int so = la.getOffset();
					int ln= la.getLineNumber();
					expression = assignmentExpression(scope,
							CompletionKind.VARIABLE_TYPE,
							KeywordSetKey.EXPRESSION);
					 
					if (expression.getExpressionKind() == IASTExpression.Kind.PRIMARY_EMPTY) {
						throwBacktrack(so, ( lastToken != null ) ? lastToken.getEndOffset() : 0, ln, fn );
					}
					list.add(expression);
					completedArg = true;
				} catch (BacktrackException e) {
					backup(mark);
				}
			}
			if (!completedArg) {
				try {
					ITokenDuple nameDuple = name(scope, null,
							KeywordSetKey.EMPTY);
					expression = astFactory.createExpression(scope,
							IASTExpression.Kind.ID_EXPRESSION, null, null,
							null, null, nameDuple, EMPTY_STRING, null, (ITokenDuple)start);
					list.add(expression);
					continue;
				} catch (ASTSemanticException e) {
					failed = true;
					break;
				} catch (BacktrackException e) {
					failed = true;
					break;
				} catch (Exception e) {
					logException("templateArgumentList::createExpression()", e); //$NON-NLS-1$
					failed = true;
					break;
				}
			}

			if (LT(1) == IToken.tCOMMA) {
				consume();
			} else if (LT(1) != IToken.tGT) {
				failed = true;
				break;
			}
		}

		templateIdScopes.pop();

		if (failed) {
			if (expression != null)
				expression.freeReferences();
			throwBacktrack(startingOffset, 0, startingLineNumber, fn );
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
	protected IToken templateId(IASTScope scope, CompletionKind kind)
			throws EndOfFileException, BacktrackException {
		ITokenDuple duple = name(scope, kind, KeywordSetKey.EMPTY);
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
	protected ITokenDuple name(IASTScope scope,
			IASTCompletionNode.CompletionKind kind, KeywordSetKey key)
			throws BacktrackException, EndOfFileException {

		TemplateParameterManager argumentList = TemplateParameterManager
				.getInstance();

		try {
			IToken first = LA(1);
			IToken last = null;
			IToken mark = mark();

			boolean hasTemplateId = false;
			boolean startsWithColonColon = false;

			if (LT(1) == IToken.tCOLONCOLON) {
				argumentList.addSegment(null);
				last = consume(IToken.tCOLONCOLON);
				setCompletionValues(scope, kind, KeywordSetKey.EMPTY,
						getCompliationUnit());
				startsWithColonColon = true;
			}

			if (LT(1) == IToken.tCOMPL)
				consume();

			switch (LT(1)) {
				case IToken.tIDENTIFIER :
					IToken prev = last;
					last = consume(IToken.tIDENTIFIER);
					if (startsWithColonColon)
						setCompletionValues(scope, kind, getCompliationUnit());
					else if (prev != null)
						setCompletionValues(scope, kind, first, prev,
								argumentList.getTemplateArgumentsList(), KeywordSetKey.EMPTY);
					else
						setCompletionValuesNoContext(scope, kind, key);

					last = consumeTemplateArguments(scope, last, argumentList, kind);
					if (last.getType() == IToken.tGT)
						hasTemplateId = true;
					break;

				default :
					IToken l = LA(1);
					backup(mark);
					throwBacktrack(first.getOffset(), l.getEndOffset(), first.getLineNumber(), l.getFilename());
			}

			while (LT(1) == IToken.tCOLONCOLON) {
				IToken prev = last;
				last = consume(IToken.tCOLONCOLON);
				setCompletionValues(scope, kind, first, prev, 
						argumentList.getTemplateArgumentsList(), KeywordSetKey.EMPTY);

				if (queryLookaheadCapability() && LT(1) == IToken.t_template)
					consume();

				if (queryLookaheadCapability() && LT(1) == IToken.tCOMPL)
					consume();

				switch (LT(1)) {
					case IToken.t_operator :
						IToken l = LA(1);
						backup(mark);
						throwBacktrack(first.getOffset(), l.getEndOffset(), first.getLineNumber(), l.getFilename());
					case IToken.tIDENTIFIER :
						prev = last;
						last = consume();
						setCompletionValues(scope, kind, first, prev, 
								argumentList.getTemplateArgumentsList(), KeywordSetKey.EMPTY);
						last = consumeTemplateArguments(scope, last,
								argumentList, kind);
						if (last.getType() == IToken.tGT)
							hasTemplateId = true;
				}
			}

			ITokenDuple tokenDuple = TokenFactory.createTokenDuple(first, last,
					(hasTemplateId
							? argumentList.getTemplateArgumentsList()
							: null));
			setGreaterNameContext(tokenDuple);
			return tokenDuple;
		} finally {
			TemplateParameterManager.returnInstance(argumentList);
		}

	}

	/**
	 * @param scope
	 * @param last
	 * @param argumentList
	 * @return
	 * @throws EndOfFileException
	 * @throws BacktrackException
	 */
	protected IToken consumeTemplateArguments(IASTScope scope, IToken last,
			TemplateParameterManager argumentList,
			IASTCompletionNode.CompletionKind completionKind)
			throws EndOfFileException, BacktrackException {
		if (language != ParserLanguage.CPP)
			return last;
		if (LT(1) == IToken.tLT) {
			IToken secondMark = mark();
			consume(IToken.tLT);
			try {
				List list = templateArgumentList(scope, completionKind);
				argumentList.addSegment(list);
				last = consume(IToken.tGT);
			} catch (BacktrackException bt) {
				argumentList.addSegment(null);
				backup(secondMark);
			}
		} else {
			argumentList.addSegment(null);
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
	protected IToken cvQualifier(IDeclarator declarator)
			throws EndOfFileException, BacktrackException {
		IToken result = null;
		int startingOffset = LA(1).getOffset();
		switch (LT(1)) {
			case IToken.t_const :
				result = consume(IToken.t_const);
				declarator.addPointerOperator(ASTPointerOperator.CONST_POINTER);
				break;
			case IToken.t_volatile :
				result = consume(IToken.t_volatile);
				declarator
						.addPointerOperator(ASTPointerOperator.VOLATILE_POINTER);
				break;
			case IToken.t_restrict :
				if (language == ParserLanguage.C) {
					result = consume(IToken.t_restrict);
					declarator
							.addPointerOperator(ASTPointerOperator.RESTRICT_POINTER);
					break;
				}
				if (extension.isValidCVModifier(language, IToken.t_restrict)) {
					result = consume(IToken.t_restrict);
					declarator.addPointerOperator(extension.getPointerOperator(
							language, IToken.t_restrict));
					break;
				}
				IToken la = LA(1);
				throwBacktrack(startingOffset, la.getEndOffset(), la.getLineNumber(), la.getFilename());

			default :
				if (extension.isValidCVModifier(language, LT(1))) {
					result = consume();
					declarator.addPointerOperator(extension.getPointerOperator(
							language, result.getType()));
				}
		}
		return result;
	}

	protected IToken consumeArrayModifiers(IDeclarator d, IASTScope scope)
			throws EndOfFileException, BacktrackException {
		int startingOffset = LA(1).getOffset();
		IToken last = null;
		while (LT(1) == IToken.tLBRACKET) {
			consume(IToken.tLBRACKET); // eat the '['

			IASTExpression exp = null;
			if (LT(1) != IToken.tRBRACKET) {
				exp = constantExpression(scope,
						CompletionKind.SINGLE_NAME_REFERENCE,
						KeywordSetKey.EXPRESSION);
			}
			last = consume(IToken.tRBRACKET);
			IASTArrayModifier arrayMod = null;
			try {
				arrayMod = astFactory.createArrayModifier(exp);
			} catch (Exception e) {
				logException("consumeArrayModifiers::createArrayModifier()", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, last.getEndOffset(), last.getLineNumber(), last.getFilename());
			}
			d.addArrayModifier(arrayMod);
		}
		return last;
	}

	protected void operatorId(Declarator d, IToken originalToken,
			TemplateParameterManager templateArgs,
			IASTCompletionNode.CompletionKind completionKind)
			throws BacktrackException, EndOfFileException {
		// we know this is an operator
		IToken operatorToken = consume(IToken.t_operator);
		IToken toSend = null;
		if (LA(1).isOperator() || LT(1) == IToken.tLPAREN
				|| LT(1) == IToken.tLBRACKET) {
			if ((LT(1) == IToken.t_new || LT(1) == IToken.t_delete)
					&& LT(2) == IToken.tLBRACKET && LT(3) == IToken.tRBRACKET) {
				consume();
				consume(IToken.tLBRACKET);
				toSend = consume(IToken.tRBRACKET);
				// vector new and delete operators
			} else if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tRPAREN) {
				// operator ()
				consume(IToken.tLPAREN);
				toSend = consume(IToken.tRPAREN);
			} else if (LT(1) == IToken.tLBRACKET && LT(2) == IToken.tRBRACKET) {
				consume(IToken.tLBRACKET);
				toSend = consume(IToken.tRBRACKET);
			} else if (LA(1).isOperator())
				toSend = consume();
			else
				throwBacktrack(operatorToken.getOffset(), toSend != null ? toSend.getEndOffset() : 0, operatorToken.getLineNumber(), operatorToken.getFilename() );
		} else {
			// must be a conversion function
			typeId(d.getDeclarationWrapper().getScope(), true,
					CompletionKind.TYPE_REFERENCE);
			toSend = lastToken;
		}

		boolean hasTemplateId = (templateArgs != null);
		boolean grabbedNewInstance = false;
		if (templateArgs == null) {
			templateArgs = TemplateParameterManager.getInstance();
			grabbedNewInstance = true;
		}

		try {
			toSend = consumeTemplateArguments(d.getDeclarationWrapper()
					.getScope(), toSend, templateArgs, completionKind);
			if (toSend.getType() == IToken.tGT) {
				hasTemplateId = true;
			}

			ITokenDuple duple = TokenFactory.createTokenDuple(
					originalToken == null ? operatorToken : originalToken,
					toSend, (hasTemplateId ? templateArgs
							.getTemplateArgumentsList() : null));

			d.setName(duple);
		} finally {
			if (grabbedNewInstance)
				TemplateParameterManager.returnInstance(templateArgs);
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
	protected IToken consumePointerOperators(IDeclarator d)
			throws EndOfFileException, BacktrackException {
		IToken result = null;
		for (;;) {
			if (LT(1) == IToken.tAMPER) {
				result = consume(IToken.tAMPER);
				d.addPointerOperator(ASTPointerOperator.REFERENCE);
				return result;

			}
			IToken mark = mark();

			ITokenDuple nameDuple = null;
			if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON) {
				try {
					nameDuple = name(d.getScope(),
								CompletionKind.SINGLE_NAME_REFERENCE,
								KeywordSetKey.EMPTY);
				} catch (BacktrackException bt) {
					backup(mark);
					return null;
				}
			}
			if (LT(1) == IToken.tSTAR) {
				result = consume(IToken.tSTAR);

				d.setPointerOperatorName(nameDuple);

				IToken successful = null;
				for (;;) {
					IToken newSuccess = cvQualifier(d);
					if (newSuccess != null)
						successful = newSuccess;
					else
						break;

				}

				if (successful == null) {
					d.addPointerOperator(ASTPointerOperator.POINTER);
				}
				continue;
			}
			if (nameDuple != null)
				nameDuple.freeReferences();
			backup(mark);
			return result;
		}
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression constantExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws BacktrackException,
			EndOfFileException {
		return conditionalExpression(scope, kind, key);
	}

	public IASTExpression expression(IASTScope scope, CompletionKind kind,
			KeywordSetKey key) throws BacktrackException, EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int ln = la.getLineNumber();
		char [] fn = la.getFilename();

		//moved to primary expression
//		IASTExpression resultExpression = null;
//		if( la.getType() == IToken.tLPAREN && LT(2) == IToken.tLBRACE && extension.supportsStatementsInExpressions() )
//		{
//			resultExpression = compoundStatementExpression(scope, la, resultExpression);
//		}
//		
//		if( resultExpression != null )
//			return resultExpression;
		
		IASTExpression assignmentExpression = assignmentExpression(scope, kind,
				key);
		while (LT(1) == IToken.tCOMMA) {
			consume(IToken.tCOMMA);
			setParameterListExpression(assignmentExpression);
			IASTExpression secondExpression = assignmentExpression(scope, kind,
					key);
			setParameterListExpression(null);
			int endOffset = lastToken != null ? lastToken.getEndOffset() : 0 ;
			try {
				assignmentExpression = astFactory.createExpression(scope,
						IASTExpression.Kind.EXPRESSIONLIST,
						assignmentExpression, secondExpression, null, null,
						null, EMPTY_STRING, null, (ITokenDuple)la);
			} catch (ASTSemanticException e) {
				throwBacktrack(e.getProblem());
			} catch (Exception e) {
				logException("expression::createExpression()", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, endOffset, ln, fn);
			}
		}
		return assignmentExpression;
	}

	/**
	 * @param scope
	 * @param la
	 * @param resultExpression
	 * @return
	 * @throws EndOfFileException
	 * @throws BacktrackException
	 */
	private IASTExpression compoundStatementExpression(IASTScope scope, IToken la, IASTExpression resultExpression) throws EndOfFileException, BacktrackException {
		int startingOffset = la.getOffset();
		int ln = la.getLineNumber();
		char [] fn = la.getFilename();
		consume( IToken.tLPAREN );
		try
		{
			if( mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE  )
				skipOverCompoundStatement();
			else if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
			{
				if( scanner.isOnTopContext() )
					compoundStatement(scope, true);
				else
					skipOverCompoundStatement();
			}
			else if( mode == ParserMode.COMPLETE_PARSE )
				compoundStatement(scope, true);

			consume( IToken.tRPAREN );
			try
			{
				resultExpression = astFactory.createExpression( scope, extension.getExpressionKindForStatement(), null, null, null, null, null,EMPTY_STRING, null, (ITokenDuple)la );
			}
			catch (ASTSemanticException e) {
				throwBacktrack(e.getProblem());
			} catch (Exception e) {
				logException("expression::createExpression()", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, lastToken != null ? lastToken.getEndOffset() : 0 , ln, fn);
			}
		}
		catch( BacktrackException bte )
		{
			backup( la );
		}
		return resultExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression assignmentExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws EndOfFileException,
			BacktrackException {
		setCompletionValues(scope, kind, key);
		if (LT(1) == IToken.t_throw) {
			return throwExpression(scope, key);
		}
		
		IASTExpression conditionalExpression = conditionalExpression(scope,
				kind, key);
		// if the condition not taken, try assignment operators
		if (conditionalExpression != null
				&& conditionalExpression.getExpressionKind() == IASTExpression.Kind.CONDITIONALEXPRESSION)
			return conditionalExpression;
		switch (LT(1)) {
			case IToken.tASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_NORMAL,
						conditionalExpression, kind, key);
			case IToken.tSTARASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_MULT,
						conditionalExpression, kind, key);
			case IToken.tDIVASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_DIV,
						conditionalExpression, kind, key);
			case IToken.tMODASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_MOD,
						conditionalExpression, kind, key);
			case IToken.tPLUSASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_PLUS,
						conditionalExpression, kind, key);
			case IToken.tMINUSASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_MINUS,
						conditionalExpression, kind, key);
			case IToken.tSHIFTRASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_RSHIFT,
						conditionalExpression, kind, key);
			case IToken.tSHIFTLASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_LSHIFT,
						conditionalExpression, kind, key);
			case IToken.tAMPERASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_AND,
						conditionalExpression, kind, key);
			case IToken.tXORASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_XOR,
						conditionalExpression, kind, key);
			case IToken.tBITORASSIGN :
				return assignmentOperatorExpression(scope,
						IASTExpression.Kind.ASSIGNMENTEXPRESSION_OR,
						conditionalExpression, kind, key);
		}
		return conditionalExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression throwExpression(IASTScope scope, KeywordSetKey key)
			throws EndOfFileException, BacktrackException {
		IToken throwToken = consume(IToken.t_throw);
		setCompletionValues(scope, CompletionKind.SINGLE_NAME_REFERENCE,
				KeywordSetKey.EXPRESSION);
		IASTExpression throwExpression = null;
		try {
			throwExpression = expression(scope,
					CompletionKind.SINGLE_NAME_REFERENCE, key);
		} catch (BacktrackException b) {
		}
		int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
		try {
			return astFactory.createExpression(scope,
					IASTExpression.Kind.THROWEXPRESSION, throwExpression, null,
					null, null, null, EMPTY_STRING, null, (ITokenDuple)throwToken);
		} catch (ASTSemanticException e) {
			throwBacktrack(e.getProblem());
		} catch (Exception e) {
			logException("throwExpression::createExpression()", e); //$NON-NLS-1$
			throwBacktrack(throwToken.getOffset(), endOffset, throwToken.getLineNumber(), throwToken.getFilename() );

		}
		return null;
	}

	/**
	 * @param expression
	 * @return
	 * @throws BacktrackException
	 */
	protected IASTExpression conditionalExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws BacktrackException,
			EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int ln = la.getLineNumber();
		char [] fn = la.getFilename();		

		IASTExpression firstExpression = logicalOrExpression(scope, kind, key);
		if (LT(1) == IToken.tQUESTION) {
			consume(IToken.tQUESTION);
			IASTExpression secondExpression = expression(scope, kind, key);
			consume(IToken.tCOLON);
			IASTExpression thirdExpression = assignmentExpression(scope, kind,
					key);
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
			try {
				return astFactory.createExpression(scope,
						IASTExpression.Kind.CONDITIONALEXPRESSION,
						firstExpression, secondExpression, thirdExpression,
						null, null, EMPTY_STRING, null, (ITokenDuple)la);
			} catch (ASTSemanticException e) {
				throwBacktrack(e.getProblem());
			} catch (Exception e) {
				logException("conditionalExpression::createExpression()", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, endOffset, ln, fn);
			}
		}
		return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression logicalOrExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws BacktrackException,
			EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		IASTExpression firstExpression = logicalAndExpression(scope, kind, key);
		while (LT(1) == IToken.tOR) {
			consume(IToken.tOR);
			IASTExpression secondExpression = logicalAndExpression(scope, kind,
					key);
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
			try {
				firstExpression = astFactory.createExpression(scope,
						IASTExpression.Kind.LOGICALOREXPRESSION,
						firstExpression, secondExpression, null, null, null,
						EMPTY_STRING, null, (ITokenDuple)la);
			} catch (ASTSemanticException e) {
				throwBacktrack(e.getProblem());
			} catch (Exception e) {
				logException("logicalOrExpression::createExpression()", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, endOffset, line, fn);
			}
		}
		return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression logicalAndExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws BacktrackException,
			EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		IASTExpression firstExpression = inclusiveOrExpression(scope, kind, key);
		while (LT(1) == IToken.tAND) {
			consume(IToken.tAND);
			IASTExpression secondExpression = inclusiveOrExpression(scope,
					kind, key);
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
			try {
				firstExpression = astFactory.createExpression(scope,
						IASTExpression.Kind.LOGICALANDEXPRESSION,
						firstExpression, secondExpression, null, null, null,
						EMPTY_STRING, null, (ITokenDuple)la);
			} catch (ASTSemanticException e) {
				throwBacktrack(e.getProblem());
			} catch (Exception e) {
				logException("logicalAndExpression::createExpression()", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, endOffset, line, fn);
			}
		}
		return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression inclusiveOrExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws BacktrackException,
			EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();

		IASTExpression firstExpression = exclusiveOrExpression(scope, kind, key);
		while (LT(1) == IToken.tBITOR) {
			consume();
			IASTExpression secondExpression = exclusiveOrExpression(scope,
					kind, key);
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
			try {
				firstExpression = astFactory.createExpression(scope,
						IASTExpression.Kind.INCLUSIVEOREXPRESSION,
						firstExpression, secondExpression, null, null, null,
						EMPTY_STRING, null, (ITokenDuple)la);
			} catch (ASTSemanticException e) {
				throwBacktrack(e.getProblem());
			} catch (Exception e) {
				logException("inclusiveOrExpression::createExpression()", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, endOffset, line, fn);
			}
		}
		return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression exclusiveOrExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws BacktrackException,
			EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();

		IASTExpression firstExpression = andExpression(scope, kind, key);
		while (LT(1) == IToken.tXOR) {
			consume();

			IASTExpression secondExpression = andExpression(scope, kind, key);
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
			try {
				firstExpression = astFactory.createExpression(scope,
						IASTExpression.Kind.EXCLUSIVEOREXPRESSION,
						firstExpression, secondExpression, null, null, null,
						EMPTY_STRING, null, (ITokenDuple)la);
			} catch (ASTSemanticException e) {
				throwBacktrack(e.getProblem());
			} catch (Exception e) {
				logException("exclusiveORExpression::createExpression()", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, endOffset, line, fn);
			}
		}
		return firstExpression;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression andExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws EndOfFileException,
			BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();

		IASTExpression firstExpression = equalityExpression(scope, kind, key);
		while (LT(1) == IToken.tAMPER) {
			consume();
			IASTExpression secondExpression = equalityExpression(scope, kind,
					key);
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
			try {
				firstExpression = astFactory.createExpression(scope,
						IASTExpression.Kind.ANDEXPRESSION, firstExpression,
						secondExpression, null, null, null, EMPTY_STRING, null, (ITokenDuple)la);
			} catch (ASTSemanticException e) {
				throwBacktrack(e.getProblem());
			} catch (Exception e) {
				logException("andExpression::createExpression()", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, endOffset, line, fn);
			}
		}
		return firstExpression;
	}

	/**
	 * @param methodName
	 * @param e
	 */
	public void logException(String methodName, Exception e) {
		if (!(e instanceof EndOfFileException) && e != null && log.isTracing()) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Parser: Unexpected exception in "); //$NON-NLS-1$
			buffer.append(methodName);
			buffer.append(":"); //$NON-NLS-1$
			buffer.append(e.getClass().getName());
			buffer.append("::"); //$NON-NLS-1$
			buffer.append(e.getMessage());
			buffer.append(". w/"); //$NON-NLS-1$
			buffer.append(scanner.toString());
			log.traceLog(buffer.toString());
//			log.errorLog(buffer.toString());
		}
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression equalityExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws EndOfFileException,
			BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();

		IASTExpression firstExpression = relationalExpression(scope, kind, key);
		for (;;) {
			switch (LT(1)) {
				case IToken.tEQUAL :
				case IToken.tNOTEQUAL :
					IToken t = consume();
					IASTExpression secondExpression = relationalExpression(
							scope, kind, key);
					int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
					try {
						firstExpression = astFactory.createExpression(scope, (t
								.getType() == IToken.tEQUAL)
								? IASTExpression.Kind.EQUALITY_EQUALS
								: IASTExpression.Kind.EQUALITY_NOTEQUALS,
								firstExpression, secondExpression, null, null,
								null, EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e) {
						throwBacktrack(e.getProblem());
					} catch (Exception e) {
						logException(
								"equalityExpression::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
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
	protected IASTExpression relationalExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws BacktrackException,
			EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();

		IASTExpression firstExpression = shiftExpression(scope, kind, key);
		for (;;) {
			switch (LT(1)) {
				case IToken.tGT :
					if (templateIdScopes.size() > 0	&&  templateIdScopes.peek() == IToken.tLT) {
						return firstExpression;
					}
				case IToken.tLT :
				case IToken.tLTEQUAL :
				case IToken.tGTEQUAL :
					IToken mark = mark();
					int t = consume().getType();
					IASTExpression secondExpression = shiftExpression(scope,
							kind, key);
					if (LA(1) == mark.getNext()) {
						// we did not consume anything
						// this is most likely an error
						backup(mark);
						return firstExpression;
					}
					IASTExpression.Kind expressionKind = null;
					switch (t) {
						case IToken.tGT :
							expressionKind = IASTExpression.Kind.RELATIONAL_GREATERTHAN;
							break;
						case IToken.tLT :
							expressionKind = IASTExpression.Kind.RELATIONAL_LESSTHAN;
							break;
						case IToken.tLTEQUAL :
							expressionKind = IASTExpression.Kind.RELATIONAL_LESSTHANEQUALTO;
							break;
						case IToken.tGTEQUAL :
							expressionKind = IASTExpression.Kind.RELATIONAL_GREATERTHANEQUALTO;
							break;
					}
					int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
					try {
						firstExpression = astFactory.createExpression(scope,
								expressionKind, firstExpression,
								secondExpression, null, null, null,
								EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e) {
						throwBacktrack(e.getProblem());
					} catch (Exception e) {
						logException(
								"relationalExpression::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
					}
					break;
				default :
					if (extension.isValidRelationalExpressionStart(language,
							LT(1))) {
						IASTExpression extensionExpression = extension
								.parseRelationalExpression(scope, this, kind,
										key, firstExpression);
						if (extensionExpression != null)
							return extensionExpression;
					}
					return firstExpression;
			}
		}
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	public IASTExpression shiftExpression(IASTScope scope, CompletionKind kind,
			KeywordSetKey key) throws BacktrackException, EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		IASTExpression firstExpression = additiveExpression(scope, kind, key);
		for (;;) {
			switch (LT(1)) {
				case IToken.tSHIFTL :
				case IToken.tSHIFTR :
					IToken t = consume();
					IASTExpression secondExpression = additiveExpression(scope,
							kind, key);
					int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
					try {
						firstExpression = astFactory.createExpression(scope,
								((t.getType() == IToken.tSHIFTL)
										? IASTExpression.Kind.SHIFT_LEFT
										: IASTExpression.Kind.SHIFT_RIGHT),
								firstExpression, secondExpression, null, null,
								null, EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e) {
						throwBacktrack(e.getProblem());
					} catch (Exception e) {
						logException("shiftExpression::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
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
	protected IASTExpression additiveExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws BacktrackException,
			EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		IASTExpression firstExpression = multiplicativeExpression(scope, kind,
				key);
		for (;;) {
			switch (LT(1)) {
				case IToken.tPLUS :
				case IToken.tMINUS :
					IToken t = consume();
					IASTExpression secondExpression = multiplicativeExpression(
							scope, kind, key);
					int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
					try {
						firstExpression = astFactory.createExpression(scope,
								((t.getType() == IToken.tPLUS)
										? IASTExpression.Kind.ADDITIVE_PLUS
										: IASTExpression.Kind.ADDITIVE_MINUS),
								firstExpression, secondExpression, null, null,
								null, EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e) {
						throwBacktrack(e.getProblem());
					} catch (Exception e) {
						logException(
								"additiveExpression::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
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
	protected IASTExpression multiplicativeExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws BacktrackException,
			EndOfFileException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		IASTExpression firstExpression = pmExpression(scope, kind, key);
		for (;;) {
			switch (LT(1)) {
				case IToken.tSTAR :
				case IToken.tDIV :
				case IToken.tMOD :
					IToken t = consume();
					IASTExpression secondExpression = pmExpression(scope, kind,
							key);
					IASTExpression.Kind expressionKind = null;
					switch (t.getType()) {
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
					int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
					try {
						firstExpression = astFactory.createExpression(scope,
								expressionKind, firstExpression,
								secondExpression, null, null, null,
								EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e) {
						firstExpression.freeReferences();
						throwBacktrack(e.getProblem());
					} catch (Exception e) {
						logException(
								"multiplicativeExpression::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
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
	protected IASTExpression pmExpression(IASTScope scope, CompletionKind kind,
			KeywordSetKey key) throws EndOfFileException, BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();

		IASTExpression firstExpression = castExpression(scope, kind, key);
		for (;;) {
			switch (LT(1)) {
				case IToken.tDOTSTAR :
				case IToken.tARROWSTAR :
					IToken t = consume();
					IASTExpression secondExpression = castExpression(scope,
							kind, key);
					int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
					try {
						firstExpression = astFactory.createExpression(scope,
								((t.getType() == IToken.tDOTSTAR)
										? IASTExpression.Kind.PM_DOTSTAR
										: IASTExpression.Kind.PM_ARROWSTAR),
								firstExpression, secondExpression, null, null,
								null, EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e) {
						throwBacktrack(e.getProblem());
					} catch (Exception e) {
						logException("pmExpression::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
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
	protected IASTExpression castExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws EndOfFileException,
			BacktrackException {
		// TO DO: we need proper symbol checkint to ensure type name
		if (LT(1) == IToken.tLPAREN) {
			IToken la = LA(1);
			int startingOffset = la.getOffset();
			int line = la.getLineNumber();
			char [] fn = la.getFilename();
			IToken mark = mark();
			consume();
			if (templateIdScopes.size() > 0) {
				templateIdScopes.push( IToken.tLPAREN );
			}
			boolean popped = false;
			IASTTypeId typeId = null;
			// If this isn't a type name, then we shouldn't be here
			try {
				try {
					typeId = typeId(scope, false, getCastExpressionKind(kind));
					consume(IToken.tRPAREN);
				} catch (BacktrackException bte) {
					backup(mark);
					if (typeId != null)
						typeId.freeReferences();
					throw bte;
				}
				
				if (templateIdScopes.size() > 0) {
					templateIdScopes.pop();
					popped = true;
				}
				IASTExpression castExpression = castExpression(scope, kind, key);
				if( castExpression != null && castExpression.getExpressionKind() == IASTExpression.Kind.PRIMARY_EMPTY )
				{
					backup( mark );
					if (typeId != null)
						typeId.freeReferences();
					return unaryExpression(scope, kind, key);
				}
				int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
				mark = null; // clean up mark so that we can garbage collect
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.CASTEXPRESSION, castExpression,
							null, null, typeId, null, EMPTY_STRING, null, (ITokenDuple)la);
				} catch (ASTSemanticException e) {
					throwBacktrack(e.getProblem());
				} catch (Exception e) {
					logException("castExpression::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(startingOffset, endOffset, line, fn);
				}
			} catch (BacktrackException b) {
				if (templateIdScopes.size() > 0 && !popped) {
					templateIdScopes.pop();
				}
			}
		}
		return unaryExpression(scope, kind, key);

	}

	/**
	 * @param kind
	 * @return
	 */
	private CompletionKind getCastExpressionKind(CompletionKind kind) {
		return ((kind == CompletionKind.SINGLE_NAME_REFERENCE || kind == CompletionKind.FUNCTION_REFERENCE)
				? kind
				: CompletionKind.TYPE_REFERENCE);
	}

	/**
	 * @param completionKind TODO
	 * @throws BacktrackException
	 */
	public IASTTypeId typeId(IASTScope scope, boolean skipArrayModifiers,
			CompletionKind completionKind) throws EndOfFileException,
			BacktrackException {
		IToken mark = mark();
		ITokenDuple name = null;
		boolean isConst = false, isVolatile = false;
		boolean isSigned = false, isUnsigned = false;
		boolean isShort = false, isLong = false;
		boolean isTypename = false;

		IASTSimpleTypeSpecifier.Type kind = null;
		do {
			try {
				name = name(scope, completionKind,
						KeywordSetKey.DECL_SPECIFIER_SEQUENCE);
				kind = IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
				break;
			} catch (BacktrackException b) {
				// do nothing
			}

			boolean encounteredType = false;
			simpleMods : for (;;) {
				switch (LT(1)) {
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
						if (encounteredType)
							break simpleMods;
						encounteredType = true;
						name = name(scope, completionKind, KeywordSetKey.EMPTY);
						kind = IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
						break;

					case IToken.t_int :
						if (encounteredType)
							break simpleMods;
						encounteredType = true;
						kind = IASTSimpleTypeSpecifier.Type.INT;
						consume();
						break;

					case IToken.t_char :
						if (encounteredType)
							break simpleMods;
						encounteredType = true;
						kind = IASTSimpleTypeSpecifier.Type.CHAR;
						consume();
						break;

					case IToken.t_bool :
						if (encounteredType)
							break simpleMods;
						encounteredType = true;
						kind = IASTSimpleTypeSpecifier.Type.BOOL;
						consume();
						break;

					case IToken.t_double :
						if (encounteredType)
							break simpleMods;
						encounteredType = true;
						kind = IASTSimpleTypeSpecifier.Type.DOUBLE;
						consume();
						break;

					case IToken.t_float :
						if (encounteredType)
							break simpleMods;
						encounteredType = true;
						kind = IASTSimpleTypeSpecifier.Type.FLOAT;
						consume();
						break;

					case IToken.t_wchar_t :
						if (encounteredType)
							break simpleMods;
						encounteredType = true;
						kind = IASTSimpleTypeSpecifier.Type.WCHAR_T;
						consume();
						break;

					case IToken.t_void :
						if (encounteredType)
							break simpleMods;
						encounteredType = true;
						kind = IASTSimpleTypeSpecifier.Type.VOID;
						consume();
						break;

					case IToken.t__Bool :
						if (encounteredType)
							break simpleMods;
						encounteredType = true;
						kind = IASTSimpleTypeSpecifier.Type._BOOL;
						consume();
						break;

					default :
						break simpleMods;
				}
			}

			if (kind != null)
				break;

			if (isShort || isLong || isUnsigned || isSigned) {
				kind = IASTSimpleTypeSpecifier.Type.INT;
				break;
			}

			if (LT(1) == IToken.t_typename || LT(1) == IToken.t_struct
					|| LT(1) == IToken.t_class || LT(1) == IToken.t_enum
					|| LT(1) == IToken.t_union) {
				consume();
				try {
					name = name(scope, completionKind, KeywordSetKey.EMPTY);
					kind = IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
				} catch (BacktrackException b) {
					backup(mark);
					throwBacktrack(b);
				}
			}

		} while (false);

		int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
		if (kind == null)
			throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(), mark.getFilename());

		TypeId id = getTypeIdInstance(scope);
		IToken last = lastToken;
		IToken temp = last;

		//template parameters are consumed as part of name
		//lastToken = consumeTemplateParameters( last );
		//if( lastToken == null ) lastToken = last;

		temp = consumePointerOperators(id);
		if (temp != null)
			last = temp;

		if (!skipArrayModifiers) {
			temp = consumeArrayModifiers(id, scope);
			if (temp != null)
				last = temp;
		}

		endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
		try {
			char[] signature = EMPTY_STRING;
			if (last != null)
			{
				if( lastToken == null )
					lastToken = last;
				signature = TokenFactory.createCharArrayRepresentation(mark, last);
			}
			return astFactory.createTypeId(scope, kind, isConst, isVolatile,
					isShort, isLong, isSigned, isUnsigned, isTypename, name, id
							.getPointerOperators(), id.getArrayModifiers(),
					signature);
		} catch (ASTSemanticException e) {
			backup(mark);
			throwBacktrack(e.getProblem());
		} catch (Exception e) {
			logException("typeId::createTypeId()", e); //$NON-NLS-1$
			throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(), mark.getFilename());
		}
		return null;
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
	protected IASTExpression deleteExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws EndOfFileException,
			BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		if (LT(1) == IToken.tCOLONCOLON) {
			// global scope
			consume(IToken.tCOLONCOLON);
		}

		consume(IToken.t_delete);

		boolean vectored = false;
		if (LT(1) == IToken.tLBRACKET) {
			// array delete
			consume();
			consume(IToken.tRBRACKET);
			vectored = true;
		}
		IASTExpression castExpression = castExpression(scope, kind, key);
		int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
		try {
			return astFactory.createExpression(scope, (vectored
					? IASTExpression.Kind.DELETE_VECTORCASTEXPRESSION
					: IASTExpression.Kind.DELETE_CASTEXPRESSION),
					castExpression, null, null, null, null, EMPTY_STRING, null, (ITokenDuple)la);
		} catch (ASTSemanticException e) {
			throwBacktrack(e.getProblem());
		} catch (Exception e) {
			logException("deleteExpression::createExpression()", e); //$NON-NLS-1$
			throwBacktrack(startingOffset, endOffset, line, fn);
		}
		return null;
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
	protected IASTExpression newExpression(IASTScope scope, KeywordSetKey key)
			throws BacktrackException, EndOfFileException {
		setCompletionValues(scope, CompletionKind.NEW_TYPE_REFERENCE,
				KeywordSetKey.EMPTY);
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		if (LT(1) == IToken.tCOLONCOLON) {
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

		if (LT(1) == IToken.tLPAREN) {
			consume(IToken.tLPAREN);
			if (templateIdScopes.size() > 0) {
				templateIdScopes.push(IToken.tLPAREN);
			}
			try {
				// Try to consume placement list
				// Note: since expressionList and expression are the same...
				backtrackMarker = mark();
				newPlacementExpressions.add(expression(scope,
						CompletionKind.SINGLE_NAME_REFERENCE, key));
				consume(IToken.tRPAREN);
				if (templateIdScopes.size() > 0) {
					templateIdScopes.pop();
				} //pop 1st Parent
				placementParseFailure = false;
				if (LT(1) == IToken.tLPAREN) {
					beforeSecondParen = mark();
					consume(IToken.tLPAREN);
					if (templateIdScopes.size() > 0) {
						templateIdScopes.push(IToken.tLPAREN);
					} //push 2nd Paren
					typeIdInParen = true;
				}
			} catch (BacktrackException e) {
				backup(backtrackMarker);
			}
			if (placementParseFailure) {
				// CASE: new (typeid-not-looking-as-placement) ...
				// the first expression in () is not a placement
				// - then it has to be typeId
				typeId = typeId(scope, true, CompletionKind.NEW_TYPE_REFERENCE);
				consume(IToken.tRPAREN);
				if (templateIdScopes.size() > 0) {
					templateIdScopes.pop();
				} //pop 1st Paren
			} else {
				if (!typeIdInParen) {
					if (LT(1) == IToken.tLBRACKET) {
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
						try {
							backtrackMarker = mark();
							typeId = typeId(scope, true,
									CompletionKind.NEW_TYPE_REFERENCE);
						} catch (BacktrackException e) {
							// Hmmm, so it wasn't typeId after all... Then it is
							// CASE: new (typeid-looking-as-placement)
							backup(backtrackMarker);
							// TODO fix this
							return null;
						}
					}
				} else {
					// Tricky cases: first expression in () is parsed as a placement,
					// and the next expression starts with '('.
					// The problem is, the first expression might as well be a typeid
					try {
						typeId = typeId(scope, true,
								CompletionKind.NEW_TYPE_REFERENCE);
						consume(IToken.tRPAREN);
						if (templateIdScopes.size() > 0) {
							templateIdScopes.pop();
						} //popping the 2nd Paren

						if (LT(1) == IToken.tLPAREN
								|| LT(1) == IToken.tLBRACKET) {
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
							int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
							try {
								setCompletionValues(scope,
										CompletionKind.NO_SUCH_KIND,
										KeywordSetKey.EMPTY);
								return astFactory.createExpression(scope,
										IASTExpression.Kind.NEW_TYPEID, null,
										null, null, typeId, null, EMPTY_STRING,
										astFactory.createNewDescriptor(
												newPlacementExpressions,
												newTypeIdExpressions,
												newInitializerExpressions), (ITokenDuple)la);
							} catch (ASTSemanticException e) {
								throwBacktrack(e.getProblem());
							} catch (Exception e) {
								logException(
										"newExpression_1::createExpression()", e); //$NON-NLS-1$
								throwBacktrack(startingOffset, endOffset, line, fn);
							}
						}
					} catch (BacktrackException e) {
						// CASE: new (typeid-looking-as-placement)(initializer-not-looking-as-typeid)
						// Fallback to initializer processing
						backup(beforeSecondParen);
						if (templateIdScopes.size() > 0) {
							templateIdScopes.pop();
						}//pop that 2nd paren
					}
				}
			}
		} else {
			// CASE: new typeid ...
			// new parameters do not start with '('
			// i.e it has to be a plain typeId
			typeId = typeId(scope, true, CompletionKind.NEW_TYPE_REFERENCE);
		}
		while (LT(1) == IToken.tLBRACKET) {
			// array new
			consume();

			if (templateIdScopes.size() > 0) {
				templateIdScopes.push(IToken.tLBRACKET);
			}

			newTypeIdExpressions.add(assignmentExpression(scope,
					CompletionKind.SINGLE_NAME_REFERENCE, key));
			consume(IToken.tRBRACKET);

			if (templateIdScopes.size() > 0) {
				templateIdScopes.pop();
			}
		}
		// newinitializer
		if (LT(1) == IToken.tLPAREN) {
			consume(IToken.tLPAREN);
			setCurrentFunctionName(((typeId != null) ? typeId
					.getFullSignatureCharArray() : EMPTY_STRING));
			setCompletionValues(scope, CompletionKind.CONSTRUCTOR_REFERENCE);
			if (templateIdScopes.size() > 0) {
				templateIdScopes.push(IToken.tLPAREN);
			}

			//we want to know the difference between no newInitializer and an empty new Initializer
			//if the next token is the RPAREN, then we have an Empty expression in our list.
			newInitializerExpressions.add(expression(scope,
					CompletionKind.CONSTRUCTOR_REFERENCE, key));

			setCurrentFunctionName(EMPTY_STRING);
			consume(IToken.tRPAREN);
			if (templateIdScopes.size() > 0) {
				templateIdScopes.pop();
			}
		}
		setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,
				KeywordSetKey.EMPTY);
		int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
		try {
			return astFactory.createExpression(scope,
					IASTExpression.Kind.NEW_TYPEID, null, null, null, typeId,
					null, EMPTY_STRING, astFactory.createNewDescriptor(
							newPlacementExpressions, newTypeIdExpressions,
							newInitializerExpressions), (ITokenDuple)la);
		} catch (ASTSemanticException e) {
			throwBacktrack(e.getProblem());
			return null;
		} catch (Exception e) {
			logException("newExpression_2::createExpression()", e); //$NON-NLS-1$
			throwBacktrack(startingOffset, endOffset, line, fn);
		}
		return null;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	public IASTExpression unaryExpression(IASTScope scope, CompletionKind kind,
			KeywordSetKey key) throws EndOfFileException, BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		switch (LT(1)) {
			case IToken.tSTAR :
				consume();
				return unaryOperatorCastExpression(scope,
						IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION, kind,
						key);
			case IToken.tAMPER :
				consume();
				return unaryOperatorCastExpression(scope,
						IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION, kind,
						key);
			case IToken.tPLUS :
				consume();
				return unaryOperatorCastExpression(scope,
						IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION, kind,
						key);
			case IToken.tMINUS :
				consume();
				return unaryOperatorCastExpression(scope,
						IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION, kind,
						key);
			case IToken.tNOT :
				consume();
				return unaryOperatorCastExpression(scope,
						IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION, kind, key);
			case IToken.tCOMPL :
				consume();
				return unaryOperatorCastExpression(scope,
						IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION, kind,
						key);
			case IToken.tINCR :
				consume();
				return unaryOperatorCastExpression(scope,
						IASTExpression.Kind.UNARY_INCREMENT, kind, key);
			case IToken.tDECR :
				consume();
				return unaryOperatorCastExpression(scope,
						IASTExpression.Kind.UNARY_DECREMENT, kind, key);
			case IToken.t_sizeof :
				consume(IToken.t_sizeof);
				IToken mark = LA(1);
				IASTTypeId d = null;
				IASTExpression unaryExpression = null;
				if (LT(1) == IToken.tLPAREN) {
					try {
						consume(IToken.tLPAREN);
						d = typeId(scope, false,
								CompletionKind.SINGLE_NAME_REFERENCE);
						consume(IToken.tRPAREN);
					} catch (BacktrackException bt) {
						backup(mark);
						unaryExpression = unaryExpression(scope, kind, key);
					}
				} else {
					unaryExpression = unaryExpression(scope, kind, key);
				}
				int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
				if (unaryExpression == null)
					try {
						return astFactory.createExpression(scope,
								IASTExpression.Kind.UNARY_SIZEOF_TYPEID, null,
								null, null, d, null, EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e) {
						throwBacktrack(e.getProblem());
					} catch (Exception e) {
						logException("unaryExpression_1::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
					}
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.UNARY_SIZEOF_UNARYEXPRESSION,
							unaryExpression, null, null, null, null,
							EMPTY_STRING, null, (ITokenDuple)la);
				} catch (ASTSemanticException e1) {
					throwBacktrack(e1.getProblem());
				} catch (Exception e) {
					logException("unaryExpression_1::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(startingOffset, endOffset, line, fn);
				}
			case IToken.t_new :
				return newExpression(scope, key);
			case IToken.t_delete :
				return deleteExpression(scope, kind, key);
			case IToken.tCOLONCOLON :
				if (queryLookaheadCapability(2)) {
					switch (LT(2)) {
						case IToken.t_new :
							return newExpression(scope, key);
						case IToken.t_delete :
							return deleteExpression(scope, kind, key);
						default :
							return postfixExpression(scope, kind, key);
					}
				}
			default :
				if (extension.isValidUnaryExpressionStart(LT(1))) {
					IASTExpression extensionExpression = extension
							.parseUnaryExpression(scope, this, kind, key);
					if (extensionExpression != null)
						return extensionExpression;
				}
				return postfixExpression(scope, kind, key);
		}
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression postfixExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws EndOfFileException,
			BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		IASTExpression firstExpression = null;
		boolean isTemplate = false;

		setCompletionValues(scope, kind, key);
		switch (LT(1)) {
			case IToken.t_typename :
				consume(IToken.t_typename);

				boolean templateTokenConsumed = false;
				if (LT(1) == IToken.t_template) {
					consume(IToken.t_template);
					templateTokenConsumed = true;
				}
				ITokenDuple nestedName = name(scope,
						CompletionKind.TYPE_REFERENCE, KeywordSetKey.EMPTY);

				consume(IToken.tLPAREN);
				if (templateIdScopes.size() > 0) {
					templateIdScopes.push(IToken.tLPAREN);
				}
				IASTExpression expressionList = expression(scope,
						CompletionKind.TYPE_REFERENCE, key);
				int endOffset = consume(IToken.tRPAREN).getEndOffset();
				if (templateIdScopes.size() > 0) {
					templateIdScopes.pop();
				}
				
				try {
					firstExpression = astFactory
							.createExpression(
									scope,
									(templateTokenConsumed
											? IASTExpression.Kind.POSTFIX_TYPENAME_TEMPLATEID
											: IASTExpression.Kind.POSTFIX_TYPENAME_IDENTIFIER),
									expressionList, null, null, null,
									nestedName, EMPTY_STRING, null, (ITokenDuple)la);
				} catch (ASTSemanticException ase) {
					throwBacktrack(ase.getProblem());
				} catch (Exception e) {
					logException("postfixExpression_1::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(startingOffset, endOffset, line, fn);
				}
				break;
			// simple-type-specifier ( assignment-expression , .. )
			case IToken.t_char :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_CHAR, key);
				break;
			case IToken.t_wchar_t :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_WCHART, key);
				break;
			case IToken.t_bool :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_BOOL, key);
				break;
			case IToken.t_short :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_SHORT, key);
				break;
			case IToken.t_int :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_INT, key);
				break;
			case IToken.t_long :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_LONG, key);
				break;
			case IToken.t_signed :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_SIGNED, key);
				break;
			case IToken.t_unsigned :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_UNSIGNED, key);
				break;
			case IToken.t_float :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_FLOAT, key);
				break;
			case IToken.t_double :
				firstExpression = simpleTypeConstructorExpression(scope,
						IASTExpression.Kind.POSTFIX_SIMPLETYPE_DOUBLE, key);
				break;
			case IToken.t_dynamic_cast :
				firstExpression = specialCastExpression(scope,
						IASTExpression.Kind.POSTFIX_DYNAMIC_CAST, key);
				break;
			case IToken.t_static_cast :
				firstExpression = specialCastExpression(scope,
						IASTExpression.Kind.POSTFIX_STATIC_CAST, key);
				break;
			case IToken.t_reinterpret_cast :
				firstExpression = specialCastExpression(scope,
						IASTExpression.Kind.POSTFIX_REINTERPRET_CAST, key);
				break;
			case IToken.t_const_cast :
				firstExpression = specialCastExpression(scope,
						IASTExpression.Kind.POSTFIX_CONST_CAST, key);
				break;
			case IToken.t_typeid :
				consume();
				consume(IToken.tLPAREN);
				if (templateIdScopes.size() > 0) {
					templateIdScopes.push(IToken.tLPAREN);
				}
				boolean isTypeId = true;
				IASTExpression lhs = null;
				IASTTypeId typeId = null;
				try {
					typeId = typeId(scope, false, CompletionKind.TYPE_REFERENCE);
				} catch (BacktrackException b) {
					isTypeId = false;
					lhs = expression(scope, CompletionKind.TYPE_REFERENCE, key);
				}
				endOffset = consume(IToken.tRPAREN).getEndOffset();
				if (templateIdScopes.size() > 0) {
					templateIdScopes.pop();
				}
				try {
					firstExpression = astFactory
							.createExpression(
									scope,
									(isTypeId
											? IASTExpression.Kind.POSTFIX_TYPEID_TYPEID
											: IASTExpression.Kind.POSTFIX_TYPEID_EXPRESSION),
									lhs, null, null, typeId, null,
									EMPTY_STRING, null, (ITokenDuple)la);
				} catch (ASTSemanticException e6) {
					throwBacktrack(e6.getProblem());
				} catch (Exception e) {
					logException("postfixExpression_2::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(startingOffset, endOffset, line, fn);
				}
				break;
			default :
				firstExpression = primaryExpression(scope, kind, key);
		}
		IASTExpression secondExpression = null;
		for (;;) {
			switch (LT(1)) {
				case IToken.tLBRACKET :
					// array access
					consume(IToken.tLBRACKET);
					if (templateIdScopes.size() > 0) {
						templateIdScopes.push(IToken.tLBRACKET);
					}
					secondExpression = expression(scope,
							CompletionKind.SINGLE_NAME_REFERENCE, key);
					int endOffset = consume(IToken.tRBRACKET).getEndOffset();
					if (templateIdScopes.size() > 0) {
						templateIdScopes.pop();
					}
					try {
						firstExpression = astFactory.createExpression(scope,
								IASTExpression.Kind.POSTFIX_SUBSCRIPT,
								firstExpression, secondExpression, null, null,
								null, EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e2) {
						throwBacktrack(e2.getProblem());
					} catch (Exception e) {
						logException(
								"postfixExpression_3::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
					}
					break;
				case IToken.tLPAREN :
					// function call
					consume(IToken.tLPAREN);
					IASTNode cntext = null;
					if (firstExpression != null) {
						if (firstExpression.getExpressionKind() == IASTExpression.Kind.ID_EXPRESSION)
							setCurrentFunctionName(firstExpression
									.getIdExpressionCharArray());
						else if (firstExpression.getRHSExpression() != null
								&& firstExpression.getRHSExpression()
										.getIdExpressionCharArray() != null) {
							setCurrentFunctionName(firstExpression
									.getRHSExpression().getIdExpressionCharArray());
							cntext = astFactory
									.expressionToMostPreciseASTNode(scope,
											firstExpression.getLHSExpression());
						}
					}

					if (templateIdScopes.size() > 0) {
						templateIdScopes.push(IToken.tLPAREN);
					}
					setCompletionValues(scope,
							CompletionKind.FUNCTION_REFERENCE, cntext);
					secondExpression = expression(scope,
							CompletionKind.FUNCTION_REFERENCE, key);
					setCurrentFunctionName(EMPTY_STRING);
					endOffset = consume(IToken.tRPAREN).getEndOffset();
					if (templateIdScopes.size() > 0) {
						templateIdScopes.pop();
					}
					try {
						firstExpression = astFactory.createExpression(scope,
								IASTExpression.Kind.POSTFIX_FUNCTIONCALL,
								firstExpression, secondExpression, null, null,
								null, EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e3) {
						throwBacktrack(e3.getProblem());
					} catch (Exception e) {
						logException(
								"postfixExpression_4::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
					}
					break;
				case IToken.tINCR :
					endOffset = consume(IToken.tINCR).getEndOffset();
					try {
						firstExpression = astFactory.createExpression(scope,
								IASTExpression.Kind.POSTFIX_INCREMENT,
								firstExpression, null, null, null, null,
								EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e1) {
						throwBacktrack(e1.getProblem());
					} catch (Exception e) {
						logException(
								"postfixExpression_5::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
					}
					break;
				case IToken.tDECR :
					endOffset = consume().getEndOffset();
					try {
						firstExpression = astFactory.createExpression(scope,
								IASTExpression.Kind.POSTFIX_DECREMENT,
								firstExpression, null, null, null, null,
								EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e4) {
						throwBacktrack(e4.getProblem());
					} catch (Exception e) {
						logException(
								"postfixExpression_6::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
					}
					break;
				case IToken.tDOT :
					// member access
					consume(IToken.tDOT);

					if (queryLookaheadCapability())
						if (LT(1) == IToken.t_template) {
							consume(IToken.t_template);
							isTemplate = true;
						}

					Kind memberCompletionKind = (isTemplate
							? IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS
							: IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION);

					setCompletionValues(scope, CompletionKind.MEMBER_REFERENCE,
							KeywordSetKey.EMPTY, firstExpression,
							memberCompletionKind);
					secondExpression = primaryExpression(scope,
							CompletionKind.MEMBER_REFERENCE, key);
					endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
					if (secondExpression != null
							&& secondExpression.getExpressionKind() == Kind.ID_EXPRESSION
							&& CharArrayUtils.indexOf( '~', secondExpression.getIdExpressionCharArray() ) != -1)
						memberCompletionKind = Kind.POSTFIX_DOT_DESTRUCTOR;

					try {
						firstExpression = astFactory.createExpression(scope,
								memberCompletionKind, firstExpression,
								secondExpression, null, null, null,
								EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e5) {
						throwBacktrack(e5.getProblem());
					} catch (Exception e) {
						logException(
								"postfixExpression_7::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
					}
					break;
				case IToken.tARROW :
					// member access
					consume(IToken.tARROW);

					if (queryLookaheadCapability())
						if (LT(1) == IToken.t_template) {
							consume(IToken.t_template);
							isTemplate = true;
						}

					Kind arrowCompletionKind = (isTemplate
							? IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP
							: IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION);

					setCompletionValues(scope, CompletionKind.MEMBER_REFERENCE,
							KeywordSetKey.EMPTY, firstExpression,
							arrowCompletionKind);
					secondExpression = primaryExpression(scope,
							CompletionKind.MEMBER_REFERENCE, key);
					endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
					if (secondExpression != null
							&& secondExpression.getExpressionKind() == Kind.ID_EXPRESSION
							&& CharArrayUtils.indexOf( '~', secondExpression.getIdExpressionCharArray() ) != -1)
						arrowCompletionKind = Kind.POSTFIX_ARROW_DESTRUCTOR;
					try {
						firstExpression = astFactory.createExpression(scope,
								arrowCompletionKind, firstExpression,
								secondExpression, null, null, null,
								EMPTY_STRING, null, (ITokenDuple)la);
					} catch (ASTSemanticException e) {
						throwBacktrack(e.getProblem());
					} catch (Exception e) {
						logException(
								"postfixExpression_8::createExpression()", e); //$NON-NLS-1$
						throwBacktrack(startingOffset, endOffset, line, fn);
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
	protected boolean queryLookaheadCapability(int count)
			throws EndOfFileException {
		//make sure we can look ahead one before doing this
		boolean result = true;
		try {
			LA(count);
		} catch (EndOfFileException olre) {
			result = false;
		}
		return result;
	}

	protected boolean queryLookaheadCapability() throws EndOfFileException {
		return queryLookaheadCapability(1);
	}

	protected void checkEndOfFile() throws EndOfFileException {
		if( mode != ParserMode.SELECTION_PARSE )
			LA(1);
	}

	protected IASTExpression simpleTypeConstructorExpression(IASTScope scope,
			Kind type, KeywordSetKey key) throws EndOfFileException,
			BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		char[] typeName = consume().getCharImage();
		consume(IToken.tLPAREN);
		setCurrentFunctionName(typeName);
		IASTExpression inside = expression(scope,
				CompletionKind.CONSTRUCTOR_REFERENCE, key);
		setCurrentFunctionName(EMPTY_STRING);
		int endOffset = consume(IToken.tRPAREN).getEndOffset();
		try {
			return astFactory.createExpression(scope, type, inside, null, null,
					null, null, EMPTY_STRING, null, (ITokenDuple)la);
		} catch (ASTSemanticException e) {
			throwBacktrack(e.getProblem());
		} catch (Exception e) {
			logException(
					"simpleTypeConstructorExpression::createExpression()", e); //$NON-NLS-1$
			throwBacktrack(startingOffset, endOffset, line, fn);
		}
		return null;
	}

	/**
	 * @param expression
	 * @throws BacktrackException
	 */
	protected IASTExpression primaryExpression(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws EndOfFileException,
			BacktrackException {
		IToken t = null;
		switch (LT(1)) {
			// TO DO: we need more literals...
			case IToken.tINTEGER :
				t = consume();
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.PRIMARY_INTEGER_LITERAL, null,
							null, null, null, null, t.getCharImage(), null, (ITokenDuple)t);
				} catch (ASTSemanticException e1) {
					throwBacktrack(e1.getProblem());
				} catch (Exception e) {
					logException("primaryExpression_1::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(t.getOffset(), t.getEndOffset(), t.getLineNumber(), t.getFilename());
				}
			case IToken.tFLOATINGPT :
				t = consume();
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.PRIMARY_FLOAT_LITERAL, null,
							null, null, null, null, t.getCharImage(), null, (ITokenDuple)t);
				} catch (ASTSemanticException e2) {
					throwBacktrack(e2.getProblem());
				} catch (Exception e) {
					logException("primaryExpression_2::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(t.getOffset(), t.getEndOffset(), t.getLineNumber(), t.getFilename());
				}
			case IToken.tSTRING :
			case IToken.tLSTRING :
				t = consume();
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.PRIMARY_STRING_LITERAL, null,
							null, null, null, null, t.getCharImage(), null, (ITokenDuple)t);
				} catch (ASTSemanticException e5) {
					throwBacktrack(e5.getProblem());
				} catch (Exception e) {
					logException("primaryExpression_3::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(t.getOffset(), t.getEndOffset(), t.getLineNumber(), t.getFilename());
				}

			case IToken.t_false :
			case IToken.t_true :
				t = consume();
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.PRIMARY_BOOLEAN_LITERAL, null,
							null, null, null, null, t.getCharImage(), null, (ITokenDuple)t);
				} catch (ASTSemanticException e3) {
					throwBacktrack(e3.getProblem());
				} catch (Exception e) {
					logException("primaryExpression_4::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(t.getOffset(), t.getEndOffset(), t.getLineNumber(), t.getFilename() );
				}

			case IToken.tCHAR :
			case IToken.tLCHAR :

				t = consume();
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.PRIMARY_CHAR_LITERAL, null,
							null, null, null, null, t.getCharImage(), null, (ITokenDuple)t);
				} catch (ASTSemanticException e4) {
					throwBacktrack(e4.getProblem());
				} catch (Exception e) {
					logException("primaryExpression_5::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(t.getOffset(), t.getEndOffset(), t.getLineNumber(), t.getFilename());
				}

			case IToken.t_this :
				t = consume(IToken.t_this);
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.PRIMARY_THIS, null, null, null,
							null, null, EMPTY_STRING, null, (ITokenDuple)t);
				} catch (ASTSemanticException e7) {
					throwBacktrack(e7.getProblem());
				} catch (Exception e) {
					logException("primaryExpression_6::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(t.getOffset(), t.getEndOffset(), t.getLineNumber(), t.getFilename());
				}
			case IToken.tLPAREN :
			    if( LT(2) == IToken.tLBRACE && extension.supportsStatementsInExpressions() )
				{
					IASTExpression resultExpression = compoundStatementExpression(scope, LA(1), null);
					if( resultExpression != null )
					    return resultExpression;
				}
				t = consume();
				if (templateIdScopes.size() > 0) {
					templateIdScopes.push(IToken.tLPAREN);
				}
				IASTExpression lhs = expression(scope, kind, key);
				int endOffset = consume(IToken.tRPAREN).getEndOffset();
				if (templateIdScopes.size() > 0) {
					templateIdScopes.pop();
				}
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.PRIMARY_BRACKETED_EXPRESSION,
							lhs, null, null, null, null, EMPTY_STRING, null, (ITokenDuple)t);
				} catch (ASTSemanticException e6) {
					throwBacktrack(e6.getProblem());
				} catch (Exception e) {
					logException("primaryExpression_7::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(t.getOffset(), endOffset, t.getLineNumber(), t.getFilename() );
				}
			case IToken.tIDENTIFIER :
			case IToken.tCOLONCOLON :
			case IToken.t_operator :
			case IToken.tCOMPL :
				ITokenDuple duple = null;
				int startingOffset = LA(1).getOffset();
				int line = LA(1).getLineNumber();
				try {
					duple = name(scope, kind, key);
				} catch (BacktrackException bt) {
					IToken mark = mark();
					Declarator d = new Declarator(new DeclarationWrapper(scope,
							mark.getOffset(), mark.getLineNumber(), null, mark.getFilename()));

					if (LT(1) == IToken.tCOLONCOLON
							|| LT(1) == IToken.tIDENTIFIER) {
						IToken start = consume();
						IToken end = null;
						if (start.getType() == IToken.tIDENTIFIER)
							end = consumeTemplateParameters(end);
						while (LT(1) == IToken.tCOLONCOLON
								|| LT(1) == IToken.tIDENTIFIER) {
							end = consume();
							if (end.getType() == IToken.tIDENTIFIER)
								end = consumeTemplateParameters(end);
						}
						if (LT(1) == IToken.t_operator)
							operatorId(d, start, null, kind);
						else {
							backup(mark);
							throwBacktrack(startingOffset, end.getEndOffset(), end.getLineNumber(), t.getFilename());
						}
					} else if (LT(1) == IToken.t_operator)
						operatorId(d, null, null, kind);

					duple = d.getNameDuple();
				}

				endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
				try {
					return astFactory.createExpression(scope,
							IASTExpression.Kind.ID_EXPRESSION, null, null,
							null, null, duple, EMPTY_STRING, null, duple);
				} catch (ASTSemanticException e8) {
					throwBacktrack(e8.getProblem());
				} catch (Exception e) {
					logException("primaryExpression_8::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(startingOffset, endOffset, line, duple.getFilename());
				}
			default :
				IToken la= LA(1);
				startingOffset = la.getOffset();
				line = la.getLineNumber();
				char [] fn = la.getFilename();
				if (!queryLookaheadCapability(2)) {
					if (LA(1).canBeAPrefix()) {
						consume();
						checkEndOfFile();
					}
				}
				IASTExpression empty = null;
				try {
					empty = astFactory.createExpression(scope,
							IASTExpression.Kind.PRIMARY_EMPTY, null, null,
							null, null, null, EMPTY_STRING, null, (ITokenDuple)la);
				} catch (ASTSemanticException e9) {
					throwBacktrack( e9.getProblem() );
					return null;
				} catch (Exception e) {
					logException("primaryExpression_9::createExpression()", e); //$NON-NLS-1$
					throwBacktrack(startingOffset, 0, line, fn);
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
		if (limitReached)
			throw new EndOfFileException();

		try {
			IToken value = scanner.nextToken();
			return value;
		} catch (OffsetLimitReachedException olre) {
			limitReached = true;
			handleOffsetLimitException(olre);
			return null;
		} 
	}

	protected IASTExpression assignmentOperatorExpression(IASTScope scope,
			IASTExpression.Kind kind, IASTExpression lhs,
			CompletionKind completionKind, KeywordSetKey key)
			throws EndOfFileException, BacktrackException {
		IToken t = consume();
		IASTExpression assignmentExpression = assignmentExpression(scope,
				completionKind, key);
		int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
		try {
			return astFactory.createExpression(scope, kind, lhs,
					assignmentExpression, null, null, null, EMPTY_STRING, null, (ITokenDuple)t);
		} catch (ASTSemanticException e) {
			throwBacktrack(e.getProblem());
		} catch (Exception e) {
			logException("assignmentOperatorExpression::createExpression()", e); //$NON-NLS-1$
			throwBacktrack(t.getOffset(), endOffset, t.getLineNumber(), t.getFilename());
		}
		return null;
	}


	protected IASTExpression unaryOperatorCastExpression(IASTScope scope,
			IASTExpression.Kind kind, CompletionKind completionKind,
			KeywordSetKey key) throws EndOfFileException, BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		IASTExpression castExpression = castExpression(scope, completionKind,
				key);
		int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
		try {
			return astFactory.createExpression(scope, kind, castExpression,
					null, null, null, null, EMPTY_STRING, null, (ITokenDuple)la);
		} catch (ASTSemanticException e) {
			throwBacktrack(e.getProblem());
		} catch (Exception e) {
			logException("unaryOperatorCastExpression::createExpression()", e); //$NON-NLS-1$
			throwBacktrack(startingOffset, endOffset, line, fn);
		}
		return null;
	}

	protected IASTExpression specialCastExpression(IASTScope scope,
			IASTExpression.Kind kind, KeywordSetKey key)
			throws EndOfFileException, BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		
		consume();
		consume(IToken.tLT);
		IASTTypeId duple = typeId(scope, false, CompletionKind.TYPE_REFERENCE);
		consume(IToken.tGT);
		consume(IToken.tLPAREN);
		IASTExpression lhs = expression(scope,
				CompletionKind.SINGLE_NAME_REFERENCE, key);
		int endOffset = consume(IToken.tRPAREN).getEndOffset();
		try {
			return astFactory.createExpression(scope, kind, lhs, null, null,
					duple, null, EMPTY_STRING, null, (ITokenDuple)la);
		} catch (ASTSemanticException e) {
			throwBacktrack(e.getProblem());
		} catch (Exception e) {
			logException("specialCastExpression::createExpression()", e); //$NON-NLS-1$
			throwBacktrack(startingOffset, endOffset, line, fn );
		}
		return null;
	}


	protected boolean isCancelled = false;

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
	    if( first instanceof ITokenDuple ) setGreaterNameContext((ITokenDuple) first);
	    return first;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return scanner.toString(); //$NON-NLS-1$
	}

	/**
	 * @return Returns the backtrackCount.
	 */
	public final int getBacktrackCount() {
		return backtrackCount;
	}

	/**
	 * @param bt
	 */
	protected void throwBacktrack(BacktrackException bt) throws BacktrackException {
		throw bt;
	}

	/**
	 * @throws EndOfFileException
	 */
	protected void errorHandling() throws EndOfFileException {
		int depth = ( LT(1) == IToken.tLBRACE ) ? 1 : 0;
	    int type = consume().getType();
	    if( type == IToken.tSEMI ) return;
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

    private static final int DEFAULT_DESIGNATOR_LIST_SIZE = 4;
	protected ISourceElementRequestor requestor = null;
    private IProblemFactory problemFactory = new ParserProblemFactory();
    /**
	 * This is the standard cosntructor that we expect the Parser to be
	 * instantiated with.
     * @param mode TODO
	 *  
	 */
    public Parser(
        IScanner scanner,
        ParserMode mode,
        ISourceElementRequestor callback,
        ParserLanguage language, IParserLogService log, IParserExtension extension )
    {
    	this.parserStartFilename = scanner.getMainFilename();
		this.scanner = scanner;
		this.language = language;
		this.log = log;
		this.extension = extension;
		this.mode = mode;
		setupASTFactory(scanner, language);
    	requestor = callback;
		if( this.mode == ParserMode.QUICK_PARSE ) 
			constructInitializersInDeclarations = false;
    }
    
    
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#failParse()
	 */
	protected void failParse( BacktrackException bt ) {
		if( bt.getProblem() == null )
		{
			IProblem problem = problemFactory.createProblem( 
					IProblem.SYNTAX_ERROR, 
					bt.getStartingOffset(), 
					bt.getEndOffset(), 
					bt.getLineNumber(), 
					bt.getFilename(), 
					EMPTY_STRING, 
					false, 
					true );
			requestor.acceptProblem( problem );
		}
		else
		{
			requestor.acceptProblem( bt.getProblem() );
		}
		failParse();
	}
	
	protected void failParse( IProblem problem ){
		if( problem != null ){
			requestor.acceptProblem( problem );
		}
		failParse();
	}

    // counter that keeps track of the number of times Parser.parse() is called
    private static int parseCount = 0;
    
    /*
	 * (non-Javadoc)
	 * 
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
	 * translationUnit : (declaration)*
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

		compilationUnit.enterScope( requestor );
		try {
			setCompletionValues(compilationUnit, CompletionKind.VARIABLE_TYPE, KeywordSetKey.DECLARATION );
		} catch (EndOfFileException e1) {
			compilationUnit.exitScope( requestor );
			return;
		}
		
//        int lastBacktrack = -1;
        
        while (true)
        {
            try
            {
                int checkOffset = LA(1).hashCode();
                declaration(compilationUnit, null, null, KeywordSetKey.DECLARATION);
                if (LA(1).hashCode() == checkOffset)
                    failParseWithErrorHandling();
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
                    failParse(b);
                    errorHandling();
//                    if (lastBacktrack != -1 && lastBacktrack == LA(1).hashCode())
//                    {
//                        // we haven't progressed from the last backtrack
//                        // try and find tne next definition
//                        failParseWithErrorHandling();
//                    }
//                    else
//                    {
//                        // start again from here
//                        lastBacktrack = LA(1).hashCode();
//                    }
                }
                catch (EndOfFileException e)
                {
                    break;
                }
            }
            catch( OutOfMemoryError oome )
			{
            	logThrowable( "translationUnit", oome ); //$NON-NLS-1$
            	throw oome;
			}
            catch( Exception e )
			{
            	logException( "translationUnit", e ); //$NON-NLS-1$
            	try {
					failParseWithErrorHandling();
				} catch (EndOfFileException e3) {
				}
			}
            catch( ParseError perr )
			{
            	throw perr;
			}
            catch (Throwable e)
            {
            	logThrowable( "translationUnit", e ); //$NON-NLS-1$
				try {
					failParseWithErrorHandling();
				} catch (EndOfFileException e3) {
				}
            }
        }
        compilationUnit.exitScope( requestor );
    }
    /**
	 * @param string
	 * @param e
	 */
	private void logThrowable(String methodName, Throwable e) {
		if( e != null && log.isTracing())
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
			log.traceLog( buffer.toString() );
//			log.errorLog( buffer.toString() );
		}
	}



	/**
	 * This function is called whenever we encounter and error that we cannot
	 * backtrack out of and we still wish to try and continue on with the parse
	 * to do a best-effort parse for our client.
	 * 
	 * @throws EndOfFileException
	 *             We can potentially hit EndOfFile here as we are skipping
	 *             ahead.
	 */
    protected void failParseWithErrorHandling() throws EndOfFileException
    {
        failParse();
        errorHandling();
    }
    /**
	 * The merger of using-declaration and using-directive in ANSI C++ grammar.
	 * 
	 * using-declaration: using typename? ::? nested-name-specifier
	 * unqualified-id ; using :: unqualified-id ; using-directive: using
	 * namespace ::? nested-name-specifier? namespace-name ;
	 * 
	 * @param container
	 *            Callback object representing the scope these definitions fall
	 *            into.
	 * @return TODO
	 * @throws BacktrackException
	 *             request for a backtrack
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
            int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
            if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON)
                duple = name(scope, CompletionKind.NAMESPACE_REFERENCE, KeywordSetKey.EMPTY);
            else
                throwBacktrack(firstToken.getOffset(), endOffset, firstToken.getLineNumber(), firstToken.getFilename());
            if (LT(1) == IToken.tSEMI)
            {
                IToken last = consume(IToken.tSEMI);
                IASTUsingDirective astUD = null; 
                
                try
                {
                    astUD = astFactory.createUsingDirective(scope, duple, firstToken.getOffset(), firstToken.getLineNumber(), last.getEndOffset(), last.getLineNumber());
                }
                catch( ASTSemanticException ase )
				{
                	backup( last );
                	throwBacktrack( ase.getProblem() );
				}
                catch (Exception e1)
                {
                	logException( "usingClause:createUsingDirective", e1 ); //$NON-NLS-1$
                    throwBacktrack(firstToken.getOffset(), last.getEndOffset(), firstToken.getLineNumber(), last.getFilename());
                }
                astUD.acceptElement(requestor );
                return astUD;
            }
            endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
            throwBacktrack(firstToken.getOffset(), endOffset, firstToken.getLineNumber(), firstToken.getFilename());
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
            throwBacktrack(firstToken.getOffset(), ( lastToken != null ) ? lastToken.getEndOffset() : 0, firstToken.getLineNumber(), firstToken.getFilename());
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
            	if( e1 instanceof ASTSemanticException && ((ASTSemanticException)e1).getProblem() != null )
            	    throwBacktrack(((ASTSemanticException)e1).getProblem());
            	else
            	    throwBacktrack(firstToken.getOffset(), last.getEndOffset(), firstToken.getLineNumber(), firstToken.getFilename());
            }
            declaration.acceptElement( requestor );
            setCompletionValues(scope, getCompletionKindForDeclaration(scope, null), KeywordSetKey.DECLARATION );
            return declaration;
        }
        int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
        throwBacktrack(firstToken.getOffset(), endOffset, firstToken.getLineNumber(), firstToken.getFilename());
        return null;
    }
    /**
	 * Implements Linkage specification in the ANSI C++ grammar.
	 * 
	 * linkageSpecification : extern "string literal" declaration | extern
	 * "string literal" { declaration-seq }
	 * 
	 * @param container
	 *            Callback object representing the scope these definitions fall
	 *            into.
	 * @return TODO
	 * @throws BacktrackException
	 *             request for a backtrack
	 */
    protected IASTDeclaration linkageSpecification(IASTScope scope)
        throws EndOfFileException, BacktrackException
    {
        IToken firstToken = consume(IToken.t_extern);
        if (LT(1) != IToken.tSTRING)
            throwBacktrack(firstToken.getOffset(), firstToken.getEndOffset(), firstToken.getLineNumber(), firstToken.getFilename());
        IToken spec = consume(IToken.tSTRING);
  
        if (LT(1) == IToken.tLBRACE)
        {
            IToken lbrace = consume(IToken.tLBRACE);
            IASTLinkageSpecification linkage = null;
            try
            {
                linkage =
                    astFactory.createLinkageSpecification(
                        scope,
                        spec.getCharImage(),
                        firstToken.getOffset(), firstToken.getLineNumber(), firstToken.getFilename());
            }
            catch (Exception e)
            {
            	logException( "linkageSpecification_1:createLinkageSpecification", e ); //$NON-NLS-1$
                throwBacktrack(firstToken.getOffset(), lbrace.getEndOffset(), lbrace.getLineNumber(), lbrace.getFilename());
            }
            
            linkage.enterScope( requestor );
            try
			{
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
	                            failParse(bt);
	                            if (checkToken == LA(1).hashCode())
	                                failParseWithErrorHandling();
	                        }
	                }
	                if (checkToken == LA(1).hashCode())
	                    failParseWithErrorHandling();
	            }
	            // consume the }
	            IToken lastTokenConsumed = consume();
	            linkage.setEndingOffsetAndLineNumber(lastTokenConsumed.getEndOffset(), lastTokenConsumed.getLineNumber());
			}
            finally
			{
            	linkage.exitScope( requestor );
			}
            return linkage;
        }
        // single declaration

        int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0;
        IASTLinkageSpecification linkage;
        try
        {
            linkage =
                astFactory.createLinkageSpecification(
                    scope,
                    spec.getCharImage(),
                    firstToken.getOffset(), firstToken.getLineNumber(), firstToken.getFilename());
        }
        catch (Exception e)
        {
        	logException( "linkageSpecification_2:createLinkageSpecification", e ); //$NON-NLS-1$
            throwBacktrack(firstToken.getOffset(), endOffset, firstToken.getLineNumber(), firstToken.getFilename());
            return null;
        }
		linkage.enterScope( requestor );
		try
		{
			declaration(linkage, null, null, KeywordSetKey.DECLARATION);
		}
		finally
		{
			linkage.exitScope( requestor );
		}
		return linkage;

    }
    /**
	 * 
	 * Represents the emalgamation of template declarations, template
	 * instantiations and specializations in the ANSI C++ grammar.
	 * 
	 * template-declaration: export? template < template-parameter-list >
	 * declaration explicit-instantiation: template declaration
	 * explicit-specialization: template <>declaration
	 * 
	 * @param container
	 *            Callback object representing the scope these definitions fall
	 *            into.
	 * @return TODO
	 * @throws BacktrackException
	 *             request for a backtrack
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
        {
        	if( extension.supportsExtendedTemplateInstantiationSyntax() && extension.isValidModifierForInstantiation(LA(1)))
        	{
        		firstToken = consume(); // consume the modifier
        		consume( IToken.t_template );
        	}
        	else
        		firstToken = consume(IToken.t_template);
        }
        if (LT(1) != IToken.tLT)
        {
            // explicit-instantiation
            IASTTemplateInstantiation templateInstantiation;
            try
            {
                templateInstantiation =
                    astFactory.createTemplateInstantiation(
                        scope,
                        firstToken.getOffset(), firstToken.getLineNumber(), firstToken.getFilename());
            }
            catch (Exception e)
            {
            	logException( "templateDeclaration:createTemplateInstantiation", e ); //$NON-NLS-1$
            	backup( mark );
                throwBacktrack(firstToken.getOffset(), firstToken.getEndOffset(), firstToken.getLineNumber(), firstToken.getFilename());
                return null;
            }
            templateInstantiation.enterScope( requestor );
            try
			{
            	declaration(templateInstantiation, templateInstantiation, null, KeywordSetKey.DECLARATION);
            	templateInstantiation.setEndingOffsetAndLineNumber(lastToken.getEndOffset(), lastToken.getLineNumber());
			} finally
			{
				templateInstantiation.exitScope( requestor );
			}
 
            return templateInstantiation;
        }
        consume(IToken.tLT);
        if (LT(1) == IToken.tGT)
        {
            IToken gt = consume(IToken.tGT);
            // explicit-specialization
            
            IASTTemplateSpecialization templateSpecialization;
            try
            {
                templateSpecialization =
                    astFactory.createTemplateSpecialization(
                        scope,
                        firstToken.getOffset(), firstToken.getLineNumber(), firstToken.getFilename());
            }
            catch (Exception e)
            {
            	logException( "templateDeclaration:createTemplateSpecialization", e ); //$NON-NLS-1$
            	backup( mark );
                throwBacktrack(firstToken.getOffset(), gt.getEndOffset(), gt.getLineNumber(), gt.getFilename());
                return null;
            }
			templateSpecialization.enterScope(requestor);
			try
			{
				declaration(templateSpecialization, templateSpecialization, null, KeywordSetKey.DECLARATION);
				templateSpecialization.setEndingOffsetAndLineNumber(
						lastToken.getEndOffset(), lastToken.getLineNumber());
			}
			finally
			{
				templateSpecialization.exitScope(requestor);
			}
            return templateSpecialization;
        }

        
        try
        {
            List parms = templateParameterList(scope);
            IToken gt = consume(IToken.tGT);
            IASTTemplateDeclaration templateDecl;
            try
            {
                templateDecl =
                    astFactory.createTemplateDeclaration(
                        scope,
                        parms,
                        exported,
                        firstToken.getOffset(), firstToken.getLineNumber(), firstToken.getFilename());
            }
            catch (Exception e)
            {
            	logException( "templateDeclaration:createTemplateDeclaration", e ); //$NON-NLS-1$
                throwBacktrack(firstToken.getOffset(), gt.getEndOffset(), gt.getLineNumber(), gt.getFilename());
                return null;
            }
            templateDecl.enterScope( requestor );
            try{
            	declaration(templateDecl, templateDecl, null, KeywordSetKey.DECLARATION );
            	templateDecl.setEndingOffsetAndLineNumber( lastToken.getEndOffset(), lastToken.getLineNumber() );
            } finally
			{
    			templateDecl.exitScope( requestor );
            }
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
	 * template-parameter-list: template-parameter template-parameter-list ,
	 * template-parameter template-parameter: type-parameter
	 * parameter-declaration type-parameter: class identifier? class identifier? =
	 * type-id typename identifier? typename identifier? = type-id template <
	 * template-parameter-list > class identifier? template <
	 * template-parameter-list > class identifier? = id-expression template-id:
	 * template-name < template-argument-list?> template-name: identifier
	 * template-argument-list: template-argument template-argument-list ,
	 * template-argument template-argument: assignment-expression type-id
	 * id-expression
	 * 
	 * @param templateDeclaration
	 *            Callback's templateDeclaration which serves as a scope to this
	 *            list.
	 * @throws BacktrackException
	 *             request for a backtrack
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
        
        IToken la = LA(1);
		int startingOffset = la.getOffset();
        int lnum = la.getLineNumber();
        char [] fn = la.getFilename();
        
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
				    int nameStart = (id != null) ? id.getOffset() : 0;
				    int nameEnd = (id != null) ? id.getEndOffset() : 0;
				    int nameLine = (id != null) ? id.getLineNumber() : 0;
                    returnValue.add(
                    	astFactory.createTemplateParameter(
                    		kind,
                    		( id == null )? EMPTY_STRING : id.getCharImage(), //$NON-NLS-1$
                    		typeId,
                    		null,
                    		null,
							( parameterScope instanceof IASTCodeScope ) ? (IASTCodeScope) parameterScope : null,
							startingToken.getOffset(), startingToken.getLineNumber(), 
							nameStart, nameEnd, nameLine, 
							(lastToken != null ) ? lastToken.getEndOffset() : nameEnd, 
							(lastToken != null ) ? lastToken.getLineNumber() : nameLine, startingToken.getFilename() ));
                }
				catch( ASTSemanticException ase )
				{
					throwBacktrack(ase.getProblem());
				}
                catch (Exception e)
                {
                	logException( "templateParameterList_1:createTemplateParameter", e ); //$NON-NLS-1$
                    throwBacktrack(startingOffset, ( lastToken != null ) ? lastToken.getEndOffset() : 0, lnum, fn);
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
                            ( optionalId == null )? EMPTY_STRING : optionalId.getCharImage(), //$NON-NLS-1$
                            optionalTypeId,
                            null,
                            subResult, 
							( parameterScope instanceof IASTCodeScope ) ? (IASTCodeScope) parameterScope : null,
							startingToken.getOffset(), startingToken.getLineNumber(), 
							(optionalId != null) ? optionalId.getOffset() : 0, 
							(optionalId != null) ? optionalId.getEndOffset() : 0, 
							(optionalId != null) ? optionalId.getLineNumber() : 0,
							lastToken.getEndOffset(), lastToken.getLineNumber(), lastToken.getFilename() ));
                }
				catch( ASTSemanticException ase )
				{
					throwBacktrack(ase.getProblem());
				}
                catch (Exception e)
                {
                	int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
                	logException( "templateParameterList_2:createTemplateParameter", e ); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, lnum, fn);
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
								wrapper.getEndOffset(), wrapper.getEndLine(), fn ),
                            null, 
							( parameterScope instanceof IASTCodeScope ) ? (IASTCodeScope) parameterScope : null,
							wrapper.getStartingOffset(), wrapper.getStartingLine(), 
							declarator.getNameStartOffset(), declarator.getNameEndOffset(), declarator.getNameLine(), 
							wrapper.getEndOffset(), wrapper.getEndLine(), fn ));
                }
				catch( ASTSemanticException ase )
				{
					throwBacktrack(ase.getProblem());
				}
                catch (Exception e)
                {
                	int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
                	logException( "templateParameterList:createParameterDeclaration", e ); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, lnum, fn);
                }
            }
        }
    }
    /**
	 * The most abstract construct within a translationUnit : a declaration.
	 * 
	 * declaration : {"asm"} asmDefinition | {"namespace"} namespaceDefinition |
	 * {"using"} usingDeclaration | {"export"|"template"} templateDeclaration |
	 * {"extern"} linkageSpecification | simpleDeclaration
	 * 
	 * Notes: - folded in blockDeclaration - merged alternatives that required
	 * same LA - functionDefinition into simpleDeclaration -
	 * namespaceAliasDefinition into namespaceDefinition - usingDirective into
	 * usingDeclaration - explicitInstantiation and explicitSpecialization into
	 * templateDeclaration
	 * 
	 * @param overideKey
	 *            TODO
	 * @param container
	 *            IParserCallback object which serves as the owner scope for
	 *            this declaration.
	 * 
	 * @throws BacktrackException
	 *             request a backtrack
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
                char[] assembly = consume(IToken.tSTRING).getCharImage();
                consume(IToken.tRPAREN);
                IToken last = consume(IToken.tSEMI);
                
                try
                {
                    resultDeclaration  =
                        astFactory.createASMDefinition(
                            scope,
                            assembly,
                            first.getOffset(),
                            first.getLineNumber(), last.getEndOffset(), last.getLineNumber(), last.getFilename());
                }
                catch (Exception e)
                {
                	logException( "declaration:createASMDefinition", e ); //$NON-NLS-1$
                    throwBacktrack(first.getOffset(), last.getEndOffset(), first.getLineNumber(), first.getFilename());
                }
                // if we made it this far, then we have all we need
                // do the callback
 				resultDeclaration.acceptElement(requestor);
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
            	if( extension.supportsExtendedTemplateInstantiationSyntax() && extension.isValidModifierForInstantiation(LA(1)) && LT(2) == IToken.t_template )
            		resultDeclaration = templateDeclaration(scope);
            	else
            		resultDeclaration = simpleDeclarationStrategyUnion(scope, ownerTemplate, overideKind, overideKey);
        }
    	setCompletionValues(scope, kind, KeywordSetKey.DECLARATION );
    	endDeclaration( resultDeclaration );
    }
    
	
	protected IASTDeclaration simpleDeclarationStrategyUnion(
        IASTScope scope,
        IASTTemplate ownerTemplate, CompletionKind overrideKind, KeywordSetKey overrideKey)
        throws EndOfFileException, BacktrackException
    {
        simpleDeclarationMark = mark();
        IProblem firstFailure = null;
        IProblem secondFailure = null;
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
        		throwBacktrack( bt );
        	firstFailure = bt.getProblem();
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
            	{
            		if( firstFailure != null  && (bt2.getProblem() == null ))
            			throwBacktrack(firstFailure);
            		else
            			throwBacktrack(bt2);
            	}
            	
            	secondFailure = bt2.getProblem();
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
					backup( simpleDeclarationMark ); //TODO - necessary?
					
					if( firstFailure != null )
						throwBacktrack( firstFailure );
					else if( secondFailure != null )
						throwBacktrack( secondFailure );
					else
						throwBacktrack( b3 );
					return null;
				}
            }
            
        }
    }
    /**
	 * Serves as the namespace declaration portion of the ANSI C++ grammar.
	 * 
	 * namespace-definition: namespace identifier { namespace-body } | namespace {
	 * namespace-body } namespace-body: declaration-seq?
	 * 
	 * @param container
	 *            IParserCallback object which serves as the owner scope for
	 *            this declaration.
	 * @return TODO
	 * @throws BacktrackException
	 *             request a backtrack
	 *  
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
            IToken lbrace = consume();
            IASTNamespaceDefinition namespaceDefinition = null;
            try
            {
                namespaceDefinition = 
                    astFactory.createNamespaceDefinition(
                        scope,
                        (identifier == null ? EMPTY_STRING: identifier.getCharImage()), //$NON-NLS-1$
                        first.getOffset(),
                        first.getLineNumber(), 
                        (identifier == null ? first.getOffset() : identifier.getOffset()), 
						(identifier == null ? first.getEndOffset() : identifier.getEndOffset() ),  
						(identifier == null ? first.getLineNumber() : identifier.getLineNumber() ), first.getFilename());
            }
            catch (Exception e1)
            {
            	
            	logException( "namespaceDefinition:createNamespaceDefinition", e1 ); //$NON-NLS-1$
                throwBacktrack(first.getOffset(), lbrace.getEndOffset(), first.getLineNumber(), first.getFilename());
                return null;
            }
            namespaceDefinition.enterScope( requestor );
            try
			{
	            setCompletionValues(scope,CompletionKind.VARIABLE_TYPE, KeywordSetKey.DECLARATION );
	            endDeclaration( namespaceDefinition );
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
	                            failParse(bt);
	                            if (checkToken == LA(1).hashCode())
	                                failParseWithErrorHandling();
	                        }
	                }
	                if (checkToken == LA(1).hashCode())
	                    failParseWithErrorHandling();
	            }
	            setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY );
	            // consume the }
	            IToken last = consume(IToken.tRBRACE);
	 
	            namespaceDefinition.setEndingOffsetAndLineNumber(
	                last.getOffset() + last.getLength(), last.getLineNumber());
	            setCompletionValues(scope, kind, KeywordSetKey.DECLARATION );
			} 
            finally
			{
            	namespaceDefinition.exitScope( requestor );
			}
            return namespaceDefinition;
        }
        else if( LT(1) == IToken.tASSIGN )
        {
        	setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY);
        	IToken assign = consume( IToken.tASSIGN );
        	
			if( identifier == null )
			{
				throwBacktrack(first.getOffset(), assign.getEndOffset(), first.getLineNumber(), first.getFilename());
				return null;
			}

        	ITokenDuple duple = name(scope, CompletionKind.NAMESPACE_REFERENCE, KeywordSetKey.EMPTY);
        	IToken semi = consume( IToken.tSEMI );
        	setCompletionValues(scope, kind, KeywordSetKey.DECLARATION );
        	IASTNamespaceAlias alias = null;
        	try
            {
                alias = astFactory.createNamespaceAlias( 
                	scope, identifier.getCharImage(), duple, first.getOffset(), 
                	first.getLineNumber(), identifier.getOffset(), identifier.getEndOffset(), identifier.getLineNumber(), duple.getLastToken().getEndOffset(), duple.getLastToken().getLineNumber() );
            }
            catch (Exception e1)
            {
            	logException( "namespaceDefinition:createNamespaceAlias", e1 ); //$NON-NLS-1$
                throwBacktrack(first.getOffset(), semi.getEndOffset(), first.getLineNumber(), first.getFilename());
                return null;
            }
            return alias;
        }
        else
        {
        	int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
            throwBacktrack(first.getOffset(), endOffset, first.getLineNumber(), first.getFilename());
            return null;
        }
    }
    /**
	 * Serves as the catch-all for all complicated declarations, including
	 * function-definitions.
	 * 
	 * simpleDeclaration : (declSpecifier)* (initDeclarator (","
	 * initDeclarator)*)? (";" | { functionBody }
	 * 
	 * Notes: - append functionDefinition stuff to end of this rule
	 * 
	 * To do: - work in functionTryBlock
	 * 
	 * @param container
	 *            IParserCallback object which serves as the owner scope for
	 *            this declaration.
	 * @param tryConstructor
	 *            true == take strategy1 (constructor ) : false == take strategy
	 *            2 ( pointer to function)
	 * @return TODO
	 * @throws BacktrackException
	 *             request a backtrack
	 */
    protected IASTDeclaration simpleDeclaration(
        SimpleDeclarationStrategy strategy,
        IASTScope scope,
        IASTTemplate ownerTemplate, CompletionKind overideKind, boolean fromCatchHandler, KeywordSetKey overrideKey)
        throws BacktrackException, EndOfFileException
    {
    	IToken firstToken = LA(1);
    	int firstOffset = firstToken.getOffset();
    	int firstLine = firstToken.getLineNumber();
    	char [] fn = firstToken.getFilename();
    	if( firstToken.getType()  == IToken.tLBRACE ) throwBacktrack(firstToken.getOffset(), firstToken.getEndOffset(), firstToken.getLineNumber(), firstToken.getFilename());
        DeclarationWrapper sdw =
            new DeclarationWrapper(scope, firstToken.getOffset(), firstToken.getLineNumber(), ownerTemplate, fn);
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
            	int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
            	logException( "simpleDeclaration:createSimpleTypeSpecifier", e1 ); //$NON-NLS-1$
            	if( e1 instanceof ASTSemanticException && ((ASTSemanticException)e1).getProblem() != null )
            	    throwBacktrack(((ASTSemanticException)e1).getProblem());
            	else
            	    throwBacktrack(firstOffset, endOffset, firstLine, fn);
            }
        
        try {
			Declarator declarator = null;
			if (LT(1) != IToken.tSEMI)
			{
			    declarator = initDeclarator(sdw, strategy, completionKindForDeclaration, constructInitializersInDeclarations);
			        
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
			    		throwBacktrack(firstOffset, LA(1).getEndOffset(), LA(1).getLineNumber(), fn);
			    	break;
			    default: 
			    	throwBacktrack(firstOffset, LA(1).getEndOffset(), LA(1).getLineNumber(), fn);
			}
			
			if( ! consumedSemi )
			{        
			    if( LT(1) == IToken.tLBRACE )
			    {
			        declarator.setHasFunctionBody(true);
			        hasFunctionBody = true;
			    }
			    			    
			    if( hasFunctionTryBlock && ! hasFunctionBody )
			    	throwBacktrack(firstOffset, LA(1).getEndOffset(), LA(1).getLineNumber(), fn);
			}
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
			List l = null; 
			try
			{
			    l = sdw.createASTNodes(astFactory);
			}
			catch (ASTSemanticException e)
			{
				if( e.getProblem() == null )
				{
					IProblem p = problemFactory.createProblem( IProblem.SYNTAX_ERROR, 
							                                   sdw.getStartingOffset(), 
							                                   lastToken != null ? lastToken.getEndOffset() : 0, 
							                                   sdw.getStartingLine(),
							                                   fn,
							                                   EMPTY_STRING, false, true );
					throwBacktrack( p );
				} else { 
					throwBacktrack(e.getProblem());
				}
			}
			catch( Exception e )
			{
				logException( "simpleDecl", e ); //$NON-NLS-1$
				throwBacktrack(firstOffset, endOffset, firstLine, fn);
			}
			
			if (hasFunctionBody && l.size() != 1)
			{
			    throwBacktrack(firstOffset, endOffset, firstLine, fn); //TODO Should be an IProblem
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
			            declaration.acceptElement( requestor );
			        }
			        return declaration;
			    }
			    IASTDeclaration declaration = (IASTDeclaration)l.get(0);
			    endDeclaration( declaration );
			    declaration.enterScope( requestor );
			    try
				{	
					if ( !( declaration instanceof IASTScope ) ) 
						throwBacktrack(firstOffset, endOffset, firstLine, fn);
	 
					handleFunctionBody((IASTScope)declaration );
					((IASTOffsetableElement)declaration).setEndingOffsetAndLineNumber(
						lastToken.getEndOffset(), lastToken.getLineNumber());
				}
			    finally
				{
			    	declaration.exitScope( requestor );
				}
					
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
							sdw.isFriend(), lastToken.getFilename());
					declaration.acceptElement(requestor);
					return declaration;
				}
			}
			catch (Exception e1)
			{
				logException( "simpleDeclaration:createTypeSpecDeclaration", e1 ); //$NON-NLS-1$
			    throwBacktrack(firstOffset, endOffset, firstLine, fn);
			}

			return null;
		} catch( BacktrackException be ) 
		{
			throwBacktrack(be);
			return null;
		}
		catch( EndOfFileException eof )
		{
			throw eof;			
		}
    }


	protected void handleFunctionBody(IASTScope scope) throws BacktrackException, EndOfFileException
	{
		if( mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE  )
			skipOverCompoundStatement();
		else if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			if( scanner.isOnTopContext() )
				functionBody(scope);
			else
				skipOverCompoundStatement();
		}
		else if( mode == ParserMode.COMPLETE_PARSE )
			functionBody(scope);
			

	}

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
	 * This method parses a constructor chain ctorinitializer: :
	 * meminitializerlist meminitializerlist: meminitializer | meminitializer ,
	 * meminitializerlist meminitializer: meminitializerid | ( expressionlist? )
	 * meminitializerid: ::? nestednamespecifier? classname identifier
	 * 
	 * @param declarator
	 *            IParserCallback object that represents the declarator
	 *            (constructor) that owns this initializer
	 * @throws BacktrackException
	 *             request a backtrack
	 */
    protected void ctorInitializer(Declarator d )
        throws EndOfFileException, BacktrackException
    {
        int startingOffset = consume(IToken.tCOLON).getOffset();
        IASTScope scope = d.getDeclarationWrapper().getScope();
        scope = astFactory.getDeclaratorScope(scope, d.getNameDuple());
        for (;;)
        {
            if (LT(1) == IToken.tLBRACE)
                break;

            
            ITokenDuple duple = name(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EMPTY );

            consume(IToken.tLPAREN);
            IASTExpression expressionList = null;

            if( LT(1) != IToken.tRPAREN )
            	expressionList = expression(scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION);

            IToken rparen = consume(IToken.tRPAREN);

            try
            {
                d.addConstructorMemberInitializer(
                    astFactory.createConstructorMemberInitializer(scope, duple, expressionList) );
            }
            catch (Exception e1)
            {
            	logException( "ctorInitializer:addConstructorMemberInitializer", e1 ); //$NON-NLS-1$
                throwBacktrack(startingOffset, rparen.getEndOffset(), rparen.getLineNumber(), rparen.getFilename());
            }
            if (LT(1) == IToken.tLBRACE)
                break;
            consume(IToken.tCOMMA);
        }
        
    }
    
    protected boolean constructInitializersInParameters = true;
    protected boolean constructInitializersInDeclarations = true;
    /**
	 * This routine parses a parameter declaration
	 * 
	 * @param containerObject
	 *            The IParserCallback object representing the
	 *            parameterDeclarationClause owning the parm.
	 * @throws BacktrackException
	 *             request a backtrack
	 */
    protected void parameterDeclaration(
        IParameterCollection collection, IASTScope scope)
        throws BacktrackException, EndOfFileException
    {
        IToken current = LA(1);
        
        DeclarationWrapper sdw =
            new DeclarationWrapper(scope, current.getOffset(), current.getLineNumber(), null, current.getFilename());
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
                throwBacktrack(e.getProblem());
            }
            catch (Exception e)
            {
            	int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
            	logException( "parameterDeclaration:createSimpleTypeSpecifier", e ); //$NON-NLS-1$
                throwBacktrack(current.getOffset(), endOffset, current.getLineNumber(), current.getFilename());
            }
        
        setCompletionValues(scope,CompletionKind.SINGLE_NAME_REFERENCE,KeywordSetKey.EMPTY );     
        if (LT(1) != IToken.tSEMI)
           initDeclarator(sdw, SimpleDeclarationStrategy.TRY_FUNCTION, CompletionKind.VARIABLE_TYPE, constructInitializersInParameters );
 
 		if( lastToken != null )
 			sdw.setEndingOffsetAndLineNumber( lastToken.getEndOffset(), lastToken.getLineNumber() );
 			
        if (current == LA(1))
        {
        	int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
            throwBacktrack(current.getOffset(), endOffset, current.getLineNumber(), current.getFilename());
        }
        collection.addParameter(sdw);
    }
    /**
	 * This class represents the state and strategy for parsing
	 * declarationSpecifierSequences
	 */
    public static class Flags
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
		 * @return true if we have encountered a simple type up to this point,
		 *         false otherwise
		 */
        public boolean haveEncounteredRawType()
        {
            return encounteredRawType;
        }
        /**
		 * @return true if we have encountered a typename up to this point,
		 *         false otherwise
		 */
        public boolean haveEncounteredTypename()
        {
            return encounteredTypename;
        }
        /**
		 * @param b -
		 *            set to true if we encounter a raw type (int, short, etc.)
		 */
        public void setEncounteredRawType(boolean b)
        {
            encounteredRawType = b;
        }
        /**
		 * @param b -
		 *            set to true if we encounter a typename
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
		 * @return whether or not we are attempting the constructor strategy or
		 *         not
		 */
        public boolean isForConstructor()
        {
            return constructor;
        }
    }
    /**
	 * @param flags
	 *            input flags that are used to make our decision
	 * @return whether or not this looks like a constructor (true or false)
	 * @throws EndOfFileException
	 *             we could encounter EOF while looking ahead
	 */
	private boolean lookAheadForConstructorOrConversion(Flags flags,
			DeclarationWrapper sdw, CompletionKind kind)
			throws EndOfFileException {
		if (flags.isForParameterDeclaration())
			return false;
		if (queryLookaheadCapability(2) && LT(2) == IToken.tLPAREN
				&& flags.isForConstructor())
			return true;

		IToken mark = mark();
		Declarator d = new Declarator(sdw);
		try {
			try {
				consumeTemplatedOperatorName(d, kind);
			} catch (BacktrackException e) {
				backup(mark);
				return false;
			} catch (EndOfFileException eof) {
				backup(mark);
				return false;
			}

			ITokenDuple duple = d.getNameDuple();
			if (duple == null) {
				backup(mark);
				return false;
			}

			int lastColon = duple.findLastTokenType(IToken.tCOLON);
			if (lastColon == -1) {
				int lt1 = LT(1);
				backup(mark);
				return flags.isForConstructor() && (lt1 == IToken.tLPAREN);
			}

			IToken className = null;
			int index = lastColon - 1;
			if (duple.getToken(index).getType() == IToken.tGT) {
				int depth = -1;
				while (depth == -1) {
					if (duple.getToken(--index).getType() == IToken.tLT)
						++depth;
				}
				className = duple.getToken(index);
			}

			boolean result = CharArrayUtils.equals( className.getCharImage(), duple.getLastToken().getCharImage() );
			backup(mark);
			return result;
		} finally {
			if (d.getNameDuple() != null
					&& d.getNameDuple().getTemplateIdArgLists() != null) {
				List[] arrayOfLists = d.getNameDuple().getTemplateIdArgLists();
				for (int i = 0; i < arrayOfLists.length; ++i) {
					if (arrayOfLists[i] == null)
						continue;
					for (int j = 0; j < arrayOfLists[i].size(); ++j) {
						IASTExpression e = (IASTExpression) arrayOfLists[i]
								.get(j);
						e.freeReferences();

					}
				}
			}
		}
	}
	/**
	 * @param flags
	 *            input flags that are used to make our decision
	 * @return whether or not this looks like a a declarator follows
	 * @throws EndOfFileException
	 *             we could encounter EOF while looking ahead
	 */
	private boolean lookAheadForDeclarator(Flags flags)
			throws EndOfFileException {
		return flags.haveEncounteredTypename()
				&& ((LT(2) != IToken.tIDENTIFIER || (LT(3) != IToken.tLPAREN && LT(3) != IToken.tASSIGN)) && !LA(
						2).isPointer());
	}
	/**
	 * This function parses a declaration specifier sequence, as according to
	 * the ANSI C++ spec.
	 * 
	 * declSpecifier : "auto" | "register" | "static" | "extern" | "mutable" |
	 * "inline" | "virtual" | "explicit" | "char" | "wchar_t" | "bool" | "short" |
	 * "int" | "long" | "signed" | "unsigned" | "float" | "double" | "void" |
	 * "const" | "volatile" | "friend" | "typedef" | ("typename")? name |
	 * {"class"|"struct"|"union"} classSpecifier | {"enum"} enumSpecifier
	 * 
	 * Notes: - folded in storageClassSpecifier, typeSpecifier,
	 * functionSpecifier - folded elaboratedTypeSpecifier into classSpecifier
	 * and enumSpecifier - find template names in name
	 * 
	 * @param decl
	 *            IParserCallback object representing the declaration that owns
	 *            this specifier sequence
	 * @param parm
	 *            Is this for a parameter declaration (true) or simple
	 *            declaration (false)
	 * @param tryConstructor
	 *            true for constructor, false for pointer to function strategy
	 * @throws BacktrackException
	 *             request a backtrack
	 */
	protected void declSpecifierSeq(DeclarationWrapper sdw, boolean parm,
			boolean tryConstructor, CompletionKind kind, KeywordSetKey key)
			throws BacktrackException, EndOfFileException {
		Flags flags = new Flags(parm, tryConstructor);
		IToken typeNameBegin = null;
		IToken typeNameEnd = null;
		declSpecifiers : for (;;) {
			switch (LT(1)) {
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
					if (typeNameBegin == null)
						typeNameBegin = LA(1);
					typeNameEnd = LA(1);
					consume(IToken.t__Complex);
					sdw.setComplex(true);
					break;
				case IToken.t__Imaginary :
					if (typeNameBegin == null)
						typeNameBegin = LA(1);
					typeNameEnd = LA(1);
					consume(IToken.t__Imaginary);
					sdw.setImaginary(true);
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
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.WCHAR_T);
					break;
				case IToken.t_bool :
					if (typeNameBegin == null)
						typeNameBegin = LA(1);
					typeNameEnd = LA(1);
					flags.setEncounteredRawType(true);
					consume();
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.BOOL);
					break;
				case IToken.t__Bool :
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
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.DOUBLE);
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
					consume(IToken.t_typename);
					ITokenDuple duple = name(sdw.getScope(), CompletionKind.TYPE_REFERENCE,
										KeywordSetKey.EMPTY);
					sdw.setTypeName(duple);
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME);
					flags.setEncounteredTypename(true);
					break;
				case IToken.tCOLONCOLON :
					sdw.setGloballyQualified(true);
					consume(IToken.tCOLONCOLON);
					break;
				case IToken.tIDENTIFIER :
					// TODO - Kludgy way to handle constructors/destructors
					if (flags.haveEncounteredRawType()) {
						setTypeName(sdw, typeNameBegin, typeNameEnd);
						return;
					}
					if (parm && flags.haveEncounteredTypename()) {
						setTypeName(sdw, typeNameBegin, typeNameEnd);
						return;
					}
					if (lookAheadForConstructorOrConversion(flags, sdw, kind)) {
						setTypeName(sdw, typeNameBegin, typeNameEnd);
						return;
					}
					if (lookAheadForDeclarator(flags)) {
						setTypeName(sdw, typeNameBegin, typeNameEnd);
						return;
					}
					setCompletionValues(sdw.getScope(), kind, key);
					ITokenDuple d = name(sdw.getScope(), kind, key);
					sdw.setTypeName(d);
					sdw.setSimpleType(IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME);
					flags.setEncounteredTypename(true);
					break;
				case IToken.t_class :
				case IToken.t_struct :
				case IToken.t_union :
					try {
						classSpecifier(sdw);
						flags.setEncounteredTypename(true);
						break;
					} catch (BacktrackException bt) {
						elaboratedTypeSpecifier(sdw);
						flags.setEncounteredTypename(true);
						break;
					}
				case IToken.t_enum :
					try {
						enumSpecifier(sdw);
						flags.setEncounteredTypename(true);
						break;
					} catch (BacktrackException bt) {
						// this is an elaborated class specifier
						elaboratedTypeSpecifier(sdw);
						flags.setEncounteredTypename(true);
						break;
					}
				default :
					if (extension.canHandleDeclSpecifierSequence(LT(1))) {
						IParserExtension.IDeclSpecifierExtensionResult declSpecExtResult = extension
								.parseDeclSpecifierSequence(this, flags, sdw,
										kind, key);
						if (declSpecExtResult != null) {
							flags = declSpecExtResult.getFlags();
							if (typeNameBegin == null)
								typeNameBegin = declSpecExtResult
										.getFirstToken();
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
	private void setTypeName(DeclarationWrapper sdw, IToken typeNameBegin,
			IToken typeNameEnd) {
		if (typeNameBegin != null)
			sdw.setTypeName(TokenFactory.createTokenDuple(typeNameBegin,
					typeNameEnd));
	}

	/**
	 * Parse an elaborated type specifier.
	 * 
	 * @param decl
	 *            Declaration which owns the elaborated type
	 * @throws BacktrackException
	 *             request a backtrack
	 */
	protected void elaboratedTypeSpecifier(DeclarationWrapper sdw)
			throws BacktrackException, EndOfFileException {
		// this is an elaborated class specifier
		IToken t = consume();
		ASTClassKind eck = null;
		CompletionKind completionKind = null;

		switch (t.getType()) {
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
				backup(t);
				throwBacktrack(t.getOffset(), t.getEndOffset(), t.getLineNumber(), t.getFilename());
		}

		ITokenDuple d = name(sdw.getScope(), completionKind,
				KeywordSetKey.EMPTY);
		IASTTypeSpecifier elaboratedTypeSpec = null;
		final boolean isForewardDecl = (LT(1) == IToken.tSEMI);

		try {
			elaboratedTypeSpec = astFactory.createElaboratedTypeSpecifier(sdw
					.getScope(), eck, d, t.getOffset(), t.getLineNumber(), d
					.getLastToken().getEndOffset(), d.getLastToken()
					.getLineNumber(), isForewardDecl, sdw.isFriend());
		} catch (ASTSemanticException e) {
			throwBacktrack(e.getProblem());
		} catch (Exception e) {
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
			logException(
					"elaboratedTypeSpecifier:createElaboratedTypeSpecifier", e); //$NON-NLS-1$
			throwBacktrack(t.getOffset(), endOffset, t.getLineNumber(), t.getFilename());
		}
		sdw.setTypeSpecifier(elaboratedTypeSpec);

		if (isForewardDecl)
		{
			((IASTElaboratedTypeSpecifier) elaboratedTypeSpec).acceptElement(
					requestor);
		}
	}
	/**
	 * Parses the initDeclarator construct of the ANSI C++ spec.
	 * 
	 * initDeclarator : declarator ("=" initializerClause | "(" expressionList
	 * ")")?
	 * 
	 * @param constructInitializers
	 *            TODO
	 * @param owner
	 *            IParserCallback object that represents the owner declaration
	 *            object.
	 * @return declarator that this parsing produced.
	 * @throws BacktrackException
	 *             request a backtrack
	 */
	protected Declarator initDeclarator(DeclarationWrapper sdw,
			SimpleDeclarationStrategy strategy, CompletionKind kind,
			boolean constructInitializers) throws EndOfFileException,
			BacktrackException {
		Declarator d = declarator(sdw, sdw.getScope(), strategy, kind);

		try {
			astFactory.constructExpressions(constructInitializers);
			if (language == ParserLanguage.CPP)
				optionalCPPInitializer(d, constructInitializers);
			else if (language == ParserLanguage.C)
				optionalCInitializer(d, constructInitializers);
			sdw.addDeclarator(d);
			return d;
		} finally {
			astFactory.constructExpressions(true);
		}
	}

	protected void optionalCPPInitializer(Declarator d,
			boolean constructInitializers) throws EndOfFileException,
			BacktrackException {
		// handle initializer
		final IASTScope scope = d.getDeclarationWrapper().getScope();
		setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,
				KeywordSetKey.EMPTY);
		if (LT(1) == IToken.tASSIGN) {
			consume(IToken.tASSIGN);
			setCompletionValues(scope, CompletionKind.SINGLE_NAME_REFERENCE,
					KeywordSetKey.EMPTY);
			throwAwayMarksForInitializerClause(d);
			try
			{
				IASTInitializerClause clause = initializerClause(scope,
						constructInitializers);
				d.setInitializerClause(clause);
			}
			catch( EndOfFileException eof )
			{
				failParse();
				throw eof;
			}
			setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,
					KeywordSetKey.EMPTY);
		} else if (LT(1) == IToken.tLPAREN) {
			// initializer in constructor
			consume(IToken.tLPAREN); // EAT IT!
			setCompletionValues(scope, CompletionKind.SINGLE_NAME_REFERENCE,
					KeywordSetKey.EMPTY);
			IASTExpression astExpression = null;
			astExpression = expression(scope,
					CompletionKind.SINGLE_NAME_REFERENCE,
					KeywordSetKey.EXPRESSION);
			setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,
					KeywordSetKey.EMPTY);
			consume(IToken.tRPAREN);
			d.setConstructorExpression(astExpression);
		}
	}

	/**
	 * @param d
	 */
	protected void throwAwayMarksForInitializerClause(Declarator d) {
		simpleDeclarationMark = null;
		if (d.getNameDuple() != null)
			d.getNameDuple().getLastToken().setNext(null);
		if (d.getPointerOperatorNameDuple() != null)
			d.getPointerOperatorNameDuple().getLastToken().setNext(null);
	}

	protected void optionalCInitializer(Declarator d,
			boolean constructInitializers) throws EndOfFileException,
			BacktrackException {
		final IASTScope scope = d.getDeclarationWrapper().getScope();
		setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,
				KeywordSetKey.EMPTY);
		if (LT(1) == IToken.tASSIGN) {
			consume(IToken.tASSIGN);
			throwAwayMarksForInitializerClause(d);
			setCompletionValues(scope, CompletionKind.SINGLE_NAME_REFERENCE,
					KeywordSetKey.EMPTY);
			d.setInitializerClause(cInitializerClause(scope,
					Collections.EMPTY_LIST, constructInitializers));
			setCompletionValues(scope, CompletionKind.NO_SUCH_KIND,
					KeywordSetKey.EMPTY);
		}
	}
	/**
	 * @param scope
	 * @return
	 */
	protected IASTInitializerClause cInitializerClause(IASTScope scope,
			List designators, boolean constructInitializers)
			throws EndOfFileException, BacktrackException {
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		la = null;
		if (LT(1) == IToken.tLBRACE) {
			consume(IToken.tLBRACE);
			List initializerList = new ArrayList();
			for (;;) {
				int checkHashcode = LA(1).hashCode();
				// required at least one initializer list
				// get designator list
				List newDesignators = designatorList(scope);
				if (newDesignators.size() != 0)
					if (LT(1) == IToken.tASSIGN)
						consume(IToken.tASSIGN);
				IASTInitializerClause initializer = cInitializerClause(scope,
						newDesignators, constructInitializers);
				initializerList.add(initializer);
				// can end with just a '}'
				if (LT(1) == IToken.tRBRACE)
					break;
				// can end with ", }"
				if (LT(1) == IToken.tCOMMA)
					consume(IToken.tCOMMA);
				if (LT(1) == IToken.tRBRACE)
					break;
				if (checkHashcode == LA(1).hashCode()) {
					IToken l2 = LA(1);
					throwBacktrack(startingOffset, l2.getEndOffset(), l2.getLineNumber(), l2.getFilename());
					return null;
				}

				// otherwise, its another initializer in the list
			}
			// consume the closing brace
			consume(IToken.tRBRACE);
			return createInitializerClause(scope, ((designators.size() == 0)
					? IASTInitializerClause.Kind.INITIALIZER_LIST
					: IASTInitializerClause.Kind.DESIGNATED_INITIALIZER_LIST),
					null, initializerList, designators, constructInitializers);
		}
		// if we get this far, it means that we have not yet succeeded
		// try this now instead
		// assignmentExpression
		try {
			IASTExpression assignmentExpression = assignmentExpression(scope,
					CompletionKind.SINGLE_NAME_REFERENCE,
					KeywordSetKey.EXPRESSION);
			try {
				return createInitializerClause(
						scope,
						((designators.size() == 0)
								? IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION
								: IASTInitializerClause.Kind.DESIGNATED_ASSIGNMENT_EXPRESSION),
						assignmentExpression, null, designators,
						constructInitializers);
			} catch (Exception e) {
				int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
				logException("cInitializerClause:createInitializerClause", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, endOffset, line, fn);
			}
		} catch (BacktrackException b) {
			// do nothing
		}
		int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
		throwBacktrack(startingOffset, endOffset, line, fn);
		return null;
	}
	/**
	 *  
	 */
	protected IASTInitializerClause initializerClause(IASTScope scope,
			boolean constructInitializers) throws EndOfFileException,
			BacktrackException {
		if (LT(1) == IToken.tLBRACE) {
			IToken t = consume(IToken.tLBRACE);
			IToken last = null;
			if (LT(1) == (IToken.tRBRACE)) {
				last = consume(IToken.tRBRACE);
				try {
					return createInitializerClause(scope,
							IASTInitializerClause.Kind.EMPTY, null, null,
							Collections.EMPTY_LIST, constructInitializers);
				} catch (Exception e) {
					logException(
							"initializerClause_1:createInitializerClause", e); //$NON-NLS-1$
					throwBacktrack(t.getOffset(), last.getEndOffset(), t.getLineNumber(), last.getFilename());
					return null;
				}
			}

			// otherwise it is a list of initializer clauses
			List initializerClauses = null;
			int startingOffset = LA(1).getOffset();
			for (;;) {
				IASTInitializerClause clause = initializerClause(scope,
						constructInitializers);
				if (clause != null) {
					if (initializerClauses == null)
						initializerClauses = new ArrayList();
					initializerClauses.add(clause);
				}
				if (LT(1) == IToken.tRBRACE)
					break;
				consume(IToken.tCOMMA);
			}
			last = consume(IToken.tRBRACE);
			try {
				return createInitializerClause(scope,
						IASTInitializerClause.Kind.INITIALIZER_LIST, null,
						initializerClauses == null
								? Collections.EMPTY_LIST
								: initializerClauses, Collections.EMPTY_LIST,
						constructInitializers);
			} catch (Exception e) {
				logException("initializerClause_2:createInitializerClause", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, last.getEndOffset(), last.getLineNumber(), last.getFilename());
				return null;
			}
		}

		// if we get this far, it means that we did not
		// try this now instead
		// assignmentExpression
		IToken la = LA(1);
		char [] fn = la.getFilename();
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		la = null;
		
		IASTExpression assignmentExpression = assignmentExpression(scope,
				CompletionKind.SINGLE_NAME_REFERENCE,
				KeywordSetKey.EXPRESSION);
		int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
		try {
			return createInitializerClause(scope,
					IASTInitializerClause.Kind.ASSIGNMENT_EXPRESSION,
					assignmentExpression, null, Collections.EMPTY_LIST,
					constructInitializers);
		} catch (Exception e) {
			logException("initializerClause_3:createInitializerClause", e); //$NON-NLS-1$
		}
		throwBacktrack(startingOffset, endOffset, line, fn);
		return null;
	}

	protected IASTInitializerClause createInitializerClause(IASTScope scope,
			IASTInitializerClause.Kind kind, IASTExpression expression,
			List initializerClauses, List designators,
			boolean constructInitializer) {
		if (!constructInitializer)
			return null;
		return astFactory.createInitializerClause(scope, kind, expression,
				initializerClauses, designators);
	}

	protected List designatorList(IASTScope scope) throws EndOfFileException,
			BacktrackException {
		List designatorList = Collections.EMPTY_LIST;
		// designated initializers for C

		if (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {

			while (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {
				IToken id = null;
				IASTExpression constantExpression = null;
				IASTDesignator.DesignatorKind kind = null;

				if (LT(1) == IToken.tDOT) {
					consume(IToken.tDOT);
					id = identifier();
					kind = IASTDesignator.DesignatorKind.FIELD;
				} else if (LT(1) == IToken.tLBRACKET) {
					IToken mark = consume(IToken.tLBRACKET);
					constantExpression = expression(scope,
							CompletionKind.SINGLE_NAME_REFERENCE,
							KeywordSetKey.EXPRESSION);
					if (LT(1) != IToken.tRBRACKET) {
						backup(mark);
						if (extension.canHandleCDesignatorInitializer(LT(1))) {
							IASTDesignator d = extension.parseDesignator(this,
									scope);
							if (d != null) {
								if (designatorList == Collections.EMPTY_LIST)
									designatorList = new ArrayList(
											DEFAULT_DESIGNATOR_LIST_SIZE);
								designatorList.add(d);
							}
							break;
						}
					}
					consume(IToken.tRBRACKET);
					kind = IASTDesignator.DesignatorKind.SUBSCRIPT;
				}

				IASTDesignator d = astFactory.createDesignator(kind,
						constantExpression, id, null);
				if (designatorList == Collections.EMPTY_LIST)
					designatorList = new ArrayList(DEFAULT_DESIGNATOR_LIST_SIZE);
				designatorList.add(d);

			}
		} else {
			if (extension.canHandleCDesignatorInitializer(LT(1))) {
				IASTDesignator d = extension.parseDesignator(this, scope);
				if (d != null) {
					if (designatorList == Collections.EMPTY_LIST)
						designatorList = new ArrayList(
								DEFAULT_DESIGNATOR_LIST_SIZE);
					designatorList.add(d);
				}
			}
		}
		return designatorList;
	}
	/**
	 * Parse a declarator, as according to the ANSI C++ specification.
	 * 
	 * declarator : (ptrOperator)* directDeclarator
	 * 
	 * directDeclarator : declaratorId | directDeclarator "("
	 * parameterDeclarationClause ")" (cvQualifier)* (exceptionSpecification)* |
	 * directDeclarator "[" (constantExpression)? "]" | "(" declarator")" |
	 * directDeclarator "(" parameterDeclarationClause ")"
	 * (oldKRParameterDeclaration)*
	 * 
	 * declaratorId : name
	 * 
	 * @param container
	 *            IParserCallback object that represents the owner declaration.
	 * @return declarator that this parsing produced.
	 * @throws BacktrackException
	 *             request a backtrack
	 */
	protected Declarator declarator(IDeclaratorOwner owner, IASTScope scope,
			SimpleDeclarationStrategy strategy, CompletionKind kind)
			throws EndOfFileException, BacktrackException {
		Declarator d = null;
		DeclarationWrapper sdw = owner.getDeclarationWrapper();
		IToken la = LA(1);
		int startingOffset = la.getOffset();
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		la = null;
		overallLoop : do {
			d = new Declarator(owner);

			consumePointerOperators(d);

			if (LT(1) == IToken.tLPAREN) {
				consume();
				declarator(d, scope, strategy, kind);
				consume(IToken.tRPAREN);
			} else
				consumeTemplatedOperatorName(d, kind);

			for (;;) {
				switch (LT(1)) {
					case IToken.tLPAREN :

						boolean failed = false;
						IASTScope parameterScope = astFactory
								.getDeclaratorScope(scope, d.getNameDuple());
						// temporary fix for initializer/function declaration
						// ambiguity
						if (queryLookaheadCapability(2)
								&& !LA(2).looksLikeExpression()
								&& strategy != SimpleDeclarationStrategy.TRY_VARIABLE) {
							if (LT(2) == IToken.tIDENTIFIER) {
								IToken newMark = mark();
								consume(IToken.tLPAREN);
								ITokenDuple queryName = null;
								try {
									try {
										queryName = name(parameterScope,
												CompletionKind.TYPE_REFERENCE,
												KeywordSetKey.EMPTY);
										if (!astFactory.queryIsTypeName(
												parameterScope, queryName))
											failed = true;
									} catch (Exception e) {
										int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
										logException(
												"declarator:queryIsTypeName", e); //$NON-NLS-1$
										throwBacktrack(startingOffset, endOffset, line, newMark.getFilename());
									}
								} catch (BacktrackException b) {
									failed = true;
								}

								if (queryName != null)
									queryName.freeReferences();
								backup(newMark);
							}
						}
						if ((queryLookaheadCapability(2)
								&& !LA(2).looksLikeExpression()
								&& strategy != SimpleDeclarationStrategy.TRY_VARIABLE && !failed)
								|| !queryLookaheadCapability(3)) {
							// parameterDeclarationClause
							d.setIsFunction(true);
							// TODO need to create a temporary scope object here
							consume(IToken.tLPAREN);
							setCompletionValues(scope,
									CompletionKind.ARGUMENT_TYPE,
									KeywordSetKey.DECL_SPECIFIER_SEQUENCE);
							boolean seenParameter = false;
							parameterDeclarationLoop : for (;;) {
								switch (LT(1)) {
									case IToken.tRPAREN :
										consume();
										setCompletionValues(parameterScope,
												CompletionKind.NO_SUCH_KIND,
												KeywordSetKey.FUNCTION_MODIFIER);
										break parameterDeclarationLoop;
									case IToken.tELLIPSIS :
										consume();
										d.setIsVarArgs(true);
										break;
									case IToken.tCOMMA :
										consume();
										setCompletionValues(
												parameterScope,
												CompletionKind.ARGUMENT_TYPE,
												KeywordSetKey.DECL_SPECIFIER_SEQUENCE);
										seenParameter = false;
										break;
									default :
										int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
										if (seenParameter)
											throwBacktrack(startingOffset, endOffset, line, fn);
										parameterDeclaration(d, parameterScope);
										seenParameter = true;
								}
							}
						}

						if (LT(1) == IToken.tCOLON || LT(1) == IToken.t_try)
							break overallLoop;

						IToken beforeCVModifier = mark();
						IToken[] cvModifiers = new IToken[2];
						int numCVModifiers = 0;
						IToken afterCVModifier = beforeCVModifier;
						// const-volatile
						// 2 options: either this is a marker for the method,
						// or it might be the beginning of old K&R style
						// parameter declaration, see
						//      void getenv(name) const char * name; {}
						// This will be determined further below
						while ((LT(1) == IToken.t_const || LT(1) == IToken.t_volatile)
								&& numCVModifiers < 2) {
							cvModifiers[numCVModifiers++] = consume();
							afterCVModifier = mark();
						}
						//check for throws clause here
						List exceptionSpecIds = null;
						if (LT(1) == IToken.t_throw) {
							exceptionSpecIds = new ArrayList();
							consume(); // throw
							consume(IToken.tLPAREN); // (
							boolean done = false;
							IASTTypeId exceptionTypeId = null;
							while (!done) {
								switch (LT(1)) {
									case IToken.tRPAREN :
										consume();
										done = true;
										break;
									case IToken.tCOMMA :
										consume();
										break;
									default :
										try {
											exceptionTypeId = typeId(
													scope,
													false,
													CompletionKind.EXCEPTION_REFERENCE);
											exceptionSpecIds
													.add(exceptionTypeId);
											exceptionTypeId
													.acceptElement(
															requestor);
										} catch (BacktrackException e) {
											failParse(e);
											consume();
											// eat this token anyway
											continue;
										}
										break;
								}
							}
							if (exceptionSpecIds != null)
								try {
									d.setExceptionSpecification(astFactory
											.createExceptionSpecification(d
													.getDeclarationWrapper()
													.getScope(),
													exceptionSpecIds));
								} catch (ASTSemanticException e) {
									throwBacktrack(e.getProblem());
								} catch (Exception e) {
									int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
									logException(
											"declarator:createExceptionSpecification", e); //$NON-NLS-1$
									throwBacktrack(startingOffset, endOffset, line, fn);
								}
						}
						// check for optional pure virtual
						if (LT(1) == IToken.tASSIGN && LT(2) == IToken.tINTEGER  )
						{
						    char[] image = LA(2).getCharImage();
						    if( image.length == 1 && image[0] == '0' ){
								consume(IToken.tASSIGN);
								consume(IToken.tINTEGER);
								d.setPureVirtual(true);
						    }
						}
						if (afterCVModifier != LA(1) || LT(1) == IToken.tSEMI) {
							// There were C++-specific clauses after
							// const/volatile modifier
							// Then it is a marker for the method
							if (numCVModifiers > 0) {
								for (int i = 0; i < numCVModifiers; i++) {
									if (cvModifiers[i].getType() == IToken.t_const)
										d.setConst(true);
									if (cvModifiers[i].getType() == IToken.t_volatile)
										d.setVolatile(true);
								}
							}
							afterCVModifier = mark();
							// In this case (method) we can't expect K&R
							// parameter declarations,
							// but we'll check anyway, for errorhandling
						}
						break;
					case IToken.tLBRACKET :
						consumeArrayModifiers(d, sdw.getScope());
						continue;
					case IToken.tCOLON :
						consume(IToken.tCOLON);
						IASTExpression exp = constantExpression(scope,
								CompletionKind.SINGLE_NAME_REFERENCE,
								KeywordSetKey.EXPRESSION);
						d.setBitFieldExpression(exp);
					default :
						break;
				}
				break;
			}
			if (LA(1).getType() != IToken.tIDENTIFIER)
				break;

		} while (true);
		if (d.getOwner() instanceof IDeclarator)
			((Declarator) d.getOwner()).setOwnedDeclarator(d);
		return d;
	}

	protected void consumeTemplatedOperatorName(Declarator d,
			CompletionKind kind) throws EndOfFileException, BacktrackException {
		TemplateParameterManager argumentList = TemplateParameterManager
				.getInstance();
		try {
			if (LT(1) == IToken.t_operator)
				operatorId(d, null, null, kind);
			else {
				try {
					ITokenDuple duple = name(d.getDeclarationWrapper()
							.getScope(), kind, KeywordSetKey.EMPTY);
					d.setName(duple);

				} catch (BacktrackException bt) {
					Declarator d1 = d;
					Declarator d11 = d1;
					IToken start = null;

					boolean hasTemplateId = false;

					IToken mark = mark();
					if (LT(1) == IToken.tCOLONCOLON
							|| LT(1) == IToken.tIDENTIFIER) {
						start = consume();
						IToken end = null;

						if (start.getType() == IToken.tIDENTIFIER) {
							end = consumeTemplateArguments(d
									.getDeclarationWrapper().getScope(), end,
									argumentList, kind);
							if (end != null && end.getType() == IToken.tGT)
								hasTemplateId = true;
						}

						while (LT(1) == IToken.tCOLONCOLON
								|| LT(1) == IToken.tIDENTIFIER) {
							end = consume();
							if (end.getType() == IToken.tIDENTIFIER) {
								end = consumeTemplateArguments(d
										.getDeclarationWrapper().getScope(),
										end, argumentList, kind);
								if (end.getType() == IToken.tGT)
									hasTemplateId = true;
							}
						}
						if (LT(1) == IToken.t_operator)
							operatorId(d11, start, (hasTemplateId
									? argumentList
									: null), kind);
						else {
							int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
							backup(mark);
							throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(), mark.getFilename());
						}
					}
				}
			}
		} finally {
			TemplateParameterManager.returnInstance(argumentList);
		}
	}
	/**
	 * Parse an enumeration specifier, as according to the ANSI specs in C &
	 * C++.
	 * 
	 * enumSpecifier: "enum" (name)? "{" (enumerator-list) "}" enumerator-list:
	 * enumerator-definition enumerator-list , enumerator-definition
	 * enumerator-definition: enumerator enumerator = constant-expression
	 * enumerator: identifier
	 * 
	 * @param owner
	 *            IParserCallback object that represents the declaration that
	 *            owns this type specifier.
	 * @throws BacktrackException
	 *             request a backtrack
	 */
	protected void enumSpecifier(DeclarationWrapper sdw)
			throws BacktrackException, EndOfFileException {
		IToken mark = mark();
		IToken identifier = null;
		consume(IToken.t_enum);
		setCompletionValues(sdw.getScope(), CompletionKind.ENUM_REFERENCE);
		if (LT(1) == IToken.tIDENTIFIER) {
			identifier = identifier();
			setCompletionValues(sdw.getScope(), CompletionKind.ENUM_REFERENCE);
		}
		if (LT(1) == IToken.tLBRACE) {
			IASTEnumerationSpecifier enumeration = null;
			try {
				enumeration = astFactory.createEnumerationSpecifier(sdw
						.getScope(), ((identifier == null)
						? EMPTY_STRING : identifier.getCharImage()), //$NON-NLS-1$
						mark.getOffset(), mark.getLineNumber(),
						((identifier == null) ? mark.getOffset() : identifier
								.getOffset()), ((identifier == null) ? mark
								.getEndOffset() : identifier.getEndOffset()),
						((identifier == null)
								? mark.getLineNumber()
								: identifier.getLineNumber()), mark.getFilename());
			} catch (ASTSemanticException e) {
				throwBacktrack(e.getProblem());
			} catch (Exception e) {
				int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
				logException("enumSpecifier:createEnumerationSpecifier", e); //$NON-NLS-1$
				throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(), mark.getFilename());
			}
			handleEnumeration( enumeration );
			consume(IToken.tLBRACE);
			while (LT(1) != IToken.tRBRACE) {
				IToken enumeratorIdentifier = null;
				if (LT(1) == IToken.tIDENTIFIER) {
					enumeratorIdentifier = identifier();
				} else {
					IToken la = LA(1);
					throwBacktrack(la.getOffset(), la.getEndOffset(), la.getLineNumber(), la.getFilename());
				}
				IASTExpression initialValue = null;
				if (LT(1) == IToken.tASSIGN) {
					consume(IToken.tASSIGN);
					initialValue = constantExpression(sdw.getScope(),
							CompletionKind.SINGLE_NAME_REFERENCE,
							KeywordSetKey.EXPRESSION);
				}
				IASTEnumerator enumerator = null;
				if (LT(1) == IToken.tRBRACE) {
					try {
						enumerator = astFactory.addEnumerator(enumeration,
								enumeratorIdentifier.getCharImage(),
								enumeratorIdentifier.getOffset(),
								enumeratorIdentifier.getLineNumber(),
								enumeratorIdentifier.getOffset(),
								enumeratorIdentifier.getEndOffset(),
								enumeratorIdentifier.getLineNumber(), lastToken
										.getEndOffset(), lastToken
										.getLineNumber(), initialValue, lastToken.getFilename());
						endEnumerator(enumerator);
					} catch (ASTSemanticException e1) {
						throwBacktrack(e1.getProblem());
					} catch (Exception e) {
						int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
						logException("enumSpecifier:addEnumerator", e); //$NON-NLS-1$
						throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(), mark.getFilename());
					}
					break;
				}
				if (LT(1) != IToken.tCOMMA) {
					enumeration
							.freeReferences();
					if (enumerator != null)
						enumerator.freeReferences();
					int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
					throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(), mark.getFilename());
				}
				try {
					enumerator = astFactory.addEnumerator(enumeration,
							enumeratorIdentifier.getCharImage(),
							enumeratorIdentifier.getOffset(),
							enumeratorIdentifier.getLineNumber(),
							enumeratorIdentifier.getOffset(),
							enumeratorIdentifier.getEndOffset(),
							enumeratorIdentifier.getLineNumber(), lastToken
									.getEndOffset(), lastToken.getLineNumber(),
							initialValue, lastToken.getFilename());
					endEnumerator(enumerator);
				} catch (ASTSemanticException e1) {
					throwBacktrack(e1.getProblem());
				} catch (Exception e) {
					int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
					logException("enumSpecifier:addEnumerator", e); //$NON-NLS-1$
					throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(), mark.getFilename());
				}
				consume(IToken.tCOMMA);
			}
			IToken t = consume(IToken.tRBRACE);
			enumeration.setEndingOffsetAndLineNumber(t.getEndOffset(), t
					.getLineNumber());
			enumeration.acceptElement(requestor);
			sdw.setTypeSpecifier(enumeration);
		} else {
			// enumSpecifierAbort
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
			backup(mark);
			throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(), mark.getFilename());
		}
	}
	/**
	 * Parse a class/struct/union definition.
	 * 
	 * classSpecifier : classKey name (baseClause)? "{" (memberSpecification)*
	 * "}"
	 * 
	 * @param owner
	 *            IParserCallback object that represents the declaration that
	 *            owns this classSpecifier
	 * @throws BacktrackException
	 *             request a backtrack
	 */
	protected void classSpecifier(DeclarationWrapper sdw)
			throws BacktrackException, EndOfFileException {
		ClassNameType nameType = ClassNameType.IDENTIFIER;
		ASTClassKind classKind = null;
		CompletionKind completionKind = null;
		ASTAccessVisibility access = ASTAccessVisibility.PUBLIC;
		IToken classKey = null;
		IToken mark = mark();

		// class key
		switch (LT(1)) {
			case IToken.t_class :
				classKey = consume();
				classKind = ASTClassKind.CLASS;
				access = ASTAccessVisibility.PRIVATE;
				completionKind = CompletionKind.CLASS_REFERENCE;
				break;
			case IToken.t_struct :
				classKey = consume();
				classKind = ASTClassKind.STRUCT;
				completionKind = CompletionKind.STRUCT_REFERENCE;
				break;
			case IToken.t_union :
				classKey = consume();
				classKind = ASTClassKind.UNION;
				completionKind = CompletionKind.UNION_REFERENCE;
				break;
			default :
				throwBacktrack(mark.getOffset(), mark.getEndOffset(), mark.getLineNumber(), mark.getFilename());
		}

		ITokenDuple duple = null;

		setCompletionValues(sdw.getScope(), completionKind, KeywordSetKey.EMPTY);
		// class name
		if (LT(1) == IToken.tIDENTIFIER)
			duple = name(sdw.getScope(), completionKind, KeywordSetKey.EMPTY);
		if (duple != null && !duple.isIdentifier())
			nameType = ClassNameType.TEMPLATE;
		if (LT(1) != IToken.tCOLON && LT(1) != IToken.tLBRACE) {
			IToken errorPoint = LA(1);
			backup(mark);
			throwBacktrack(errorPoint.getOffset(), errorPoint.getEndOffset(), errorPoint.getLineNumber(), errorPoint.getFilename());
		}
		IASTClassSpecifier astClassSpecifier = null;

		try {
			astClassSpecifier = astFactory.createClassSpecifier(sdw.getScope(),
					duple, classKind, nameType, access, classKey.getOffset(),
					classKey.getLineNumber(), duple == null ? classKey
							.getOffset() : duple.getFirstToken().getOffset(),
					duple == null ? classKey.getEndOffset() : duple
							.getFirstToken().getEndOffset(), duple == null
							? classKey.getLineNumber()
							: duple.getFirstToken().getLineNumber(), classKey.getFilename());
		} catch (ASTSemanticException e) {
			throwBacktrack(e.getProblem());
		} catch (Exception e) {
			int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
			logException("classSpecifier:createClassSpecifier", e); //$NON-NLS-1$
			throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(), mark.getFilename());
		}
		sdw.setTypeSpecifier(astClassSpecifier);
		// base clause
		if (LT(1) == IToken.tCOLON) {
			baseSpecifier(astClassSpecifier);
		}

		if (LT(1) == IToken.tLBRACE) {
			consume(IToken.tLBRACE);
			setCompletionValues(astClassSpecifier, CompletionKind.FIELD_TYPE,
					KeywordSetKey.MEMBER);
			astClassSpecifier.enterScope(requestor);
			
			try
			{
				handleClassSpecifier(astClassSpecifier);
				memberDeclarationLoop : while (LT(1) != IToken.tRBRACE) 
				{
					int checkToken = LA(1).hashCode();
					switch (LT(1)) {
						case IToken.t_public :
							consume();
							consume(IToken.tCOLON);
							astClassSpecifier
									.setCurrentVisibility(ASTAccessVisibility.PUBLIC);
							break;
						case IToken.t_protected :
							consume();
							consume(IToken.tCOLON);
							astClassSpecifier
									.setCurrentVisibility(ASTAccessVisibility.PROTECTED);
							break;
	
						case IToken.t_private :
							consume();
							consume(IToken.tCOLON);
							astClassSpecifier
									.setCurrentVisibility(ASTAccessVisibility.PRIVATE);
							break;
						case IToken.tRBRACE :
							consume(IToken.tRBRACE);
							break memberDeclarationLoop;
						default :
							try {
								declaration(astClassSpecifier, null, null,
										KeywordSetKey.MEMBER);
							} catch (BacktrackException bt) {
								if (checkToken == LA(1).hashCode())
									failParseWithErrorHandling();
							}
					}
					if (checkToken == LA(1).hashCode())
						failParseWithErrorHandling();
				}
				// consume the }
				IToken lt = consume(IToken.tRBRACE);
				astClassSpecifier.setEndingOffsetAndLineNumber(lt.getEndOffset(),
						lt.getLineNumber());
				try {
					astFactory.signalEndOfClassSpecifier(astClassSpecifier);
				} catch (Exception e1) {
					logException("classSpecifier:signalEndOfClassSpecifier", e1); //$NON-NLS-1$
					throwBacktrack(lt.getOffset(), lt.getEndOffset(), lt.getLineNumber(), lt.getFilename());
				}

			}
			finally
			{
				astClassSpecifier.exitScope(requestor);				
			}



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
	   		IToken la = LA(1);
	   		char [] fn = la.getFilename();
	        int startingOffset = la.getOffset();
	        int line = la.getLineNumber();
	        la = null;
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
	                		bases = new ArrayList(4);
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
	            		try {
							astFactory.addBaseSpecifier( astClassSpec, 
									                     ((Boolean)data[0]).booleanValue(),
							                             (ASTAccessVisibility) data[1], 
														 (ITokenDuple)data[2] );
						} catch (ASTSemanticException e1) {
							failParse( e1.getProblem() );
						}
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
				failParse( e.getProblem() );
	        } catch (Exception e)
	        {
	        	int endOffset = ( lastToken != null ) ? lastToken.getEndOffset() : 0 ;
	        	logException( "baseSpecifier_2::addBaseSpecifier", e ); //$NON-NLS-1$
	            throwBacktrack( startingOffset, endOffset, line, fn );
	        }
	    }

	/**
	 * Parses a function body.
	 * 
	 * @throws BacktrackException
	 *             request a backtrack
	 */
	protected void functionBody(IASTScope scope) throws EndOfFileException,
			BacktrackException {
		compoundStatement(scope, false);
	}
	/**
	 * Parses a statement.
	 * 
	 * @throws BacktrackException
	 *             request a backtrack
	 */
	protected void statement(IASTCodeScope scope) throws EndOfFileException,
			BacktrackException {

		setCompletionValues(scope, CompletionKind.SINGLE_NAME_REFERENCE,
				KeywordSetKey.STATEMENT);

		switch (LT(1)) {
			case IToken.t_case :
				consume(IToken.t_case);
				IASTExpression constant_expression = constantExpression(scope,
						CompletionKind.SINGLE_NAME_REFERENCE,
						KeywordSetKey.EXPRESSION);
				constant_expression.acceptElement(requestor);
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
			    if_loop: while( true ){
					consume(IToken.t_if);
					consume(IToken.tLPAREN);
					IToken start = LA(1);
					boolean passedCondition = true;
					try {
						condition(scope);
						consume(IToken.tRPAREN);
					} catch (BacktrackException b) {
					    //if the problem has no offset info, make a new one that does
					    if( b.getProblem() != null && b.getProblem().getSourceLineNumber() == -1 ){
					        IProblem p = b.getProblem();
					        IProblem p2 = problemFactory.createProblem( p.getID(), start.getOffset(), 
	                                		   lastToken != null ? lastToken.getEndOffset() : start.getEndOffset(), 
			                                   start.getLineNumber(), p.getOriginatingFileName(),
			                                   p.getArguments() != null ? p.getArguments().toCharArray() : null,
			                                   p.isWarning(), p.isError() );
					        b.initialize( p2 );
					    }
						failParse(b);
						failParseWithErrorHandling();
						passedCondition = false;
					}
					
					if( passedCondition ){
						if (LT(1) != IToken.tLBRACE)
							singleStatementScope(scope);
						else
							statement(scope);
					}
					
					if (LT(1) == IToken.t_else) {
						consume(IToken.t_else);
						if (LT(1) == IToken.t_if) {
							//an else if, don't recurse, just loop and do another if
							cleanupLastToken();
							continue if_loop;
						} else if (LT(1) != IToken.tLBRACE)
							singleStatementScope(scope);
						else
							statement(scope);
					}
					break if_loop;
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
				if (LT(1) != IToken.tLBRACE)
					singleStatementScope(scope);
				else
					statement(scope);
				cleanupLastToken();
				return;
			case IToken.t_do :
				consume(IToken.t_do);
				if (LT(1) != IToken.tLBRACE)
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
				if (LT(1) != IToken.tRPAREN) {
					IASTExpression finalExpression = expression(scope,
							CompletionKind.SINGLE_NAME_REFERENCE,
							KeywordSetKey.DECLARATION);
					finalExpression.acceptElement(requestor);
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
				if (LT(1) != IToken.tSEMI) {
					IASTExpression retVal = expression(scope,
							CompletionKind.SINGLE_NAME_REFERENCE,
							KeywordSetKey.EXPRESSION);
					retVal.acceptElement(requestor);
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
				compoundStatement(scope, true);
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

				if (queryLookaheadCapability(2) && LT(1) == IToken.tIDENTIFIER
						&& LT(2) == IToken.tCOLON) {
					consume(IToken.tIDENTIFIER);
					consume(IToken.tCOLON);
					statement(scope);
					cleanupLastToken();
					return;
				}
				// expressionStatement
				// Note: the function style cast ambiguity is handled in
				// expression
				// Since it only happens when we are in a statement
				IToken mark = mark();
				IASTExpression expressionStatement = null;
				try {
					expressionStatement = expression(scope,
							CompletionKind.SINGLE_NAME_REFERENCE,
							KeywordSetKey.STATEMENT);
					consume(IToken.tSEMI);
					expressionStatement.acceptElement(requestor);
					endExpression(expressionStatement);
					return;
				} catch (BacktrackException b) {
					backup(mark);
					if (expressionStatement != null)
						expressionStatement.freeReferences();
				}

				// declarationStatement
				declaration(scope, null, null, KeywordSetKey.STATEMENT);
		}

	}
	protected void catchHandlerSequence(IASTScope scope)
	throws EndOfFileException, BacktrackException {
		if( LT(1) != IToken.t_catch )
		{
			IToken la = LA(1);
			throwBacktrack(la.getOffset(), la.getEndOffset(), la.getLineNumber(), la.getFilename()); // error, need at least one of these
		}
		while (LT(1) == IToken.t_catch)
		{
			consume(IToken.t_catch);
			setCompletionValues(scope,CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY );
			consume(IToken.tLPAREN);
			setCompletionValues(scope,CompletionKind.EXCEPTION_REFERENCE,KeywordSetKey.DECL_SPECIFIER_SEQUENCE);
			try
			{
				if( LT(1) == IToken.tELLIPSIS )
					consume( IToken.tELLIPSIS );
				else 
					simpleDeclaration( SimpleDeclarationStrategy.TRY_VARIABLE, scope, null, CompletionKind.EXCEPTION_REFERENCE, true, KeywordSetKey.DECLARATION); 
				consume(IToken.tRPAREN);
			
				catchBlockCompoundStatement(scope);
			}
			catch( BacktrackException bte )
			{
				failParse( bte );
				failParseWithErrorHandling();
			}
		}
	}

	protected void catchBlockCompoundStatement(IASTScope scope)
			throws BacktrackException, EndOfFileException
	{
		if( mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE  )
			skipOverCompoundStatement();
		else if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			if( scanner.isOnTopContext() )
				compoundStatement(scope, true);
			else
				skipOverCompoundStatement();
		}
		else if( mode == ParserMode.COMPLETE_PARSE )
			compoundStatement(scope, true);
	}

	protected void singleStatementScope(IASTScope scope)
			throws EndOfFileException, BacktrackException {
		IASTCodeScope newScope;
		try {
			newScope = astFactory.createNewCodeBlock(scope);
		} catch (Exception e) {
			logException("singleStatementScope:createNewCodeBlock", e); //$NON-NLS-1$
			IToken la = LA(1);
			throwBacktrack(la.getOffset(), la.getEndOffset(), la.getLineNumber(), la.getFilename());
			return;
		}
		newScope.enterScope(requestor);
		try {
			statement(newScope);
		} finally {
			newScope.exitScope(requestor);
		}
	}

	/**
	 * @throws BacktrackException
	 */
	protected void condition(IASTScope scope) throws BacktrackException,
			EndOfFileException {
		IASTExpression someExpression = expression(scope,
				CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION);
		someExpression.acceptElement(requestor);

		endExpression(someExpression);
	}

	/**
	 * @throws BacktrackException
	 */
	protected void forInitStatement(IASTScope scope) throws BacktrackException,
			EndOfFileException {
		IToken mark = mark();
		try {
			IASTExpression e = expression(scope,
					CompletionKind.SINGLE_NAME_REFERENCE,
					KeywordSetKey.DECLARATION);
			consume(IToken.tSEMI);
			e.acceptElement(requestor);

		} catch (BacktrackException bt) {
			backup(mark);
			try {
				simpleDeclarationStrategyUnion(scope, null, null, null);
			} catch (BacktrackException b) {
				failParse(b);
				throwBacktrack(b);
			}
		}

	}
	/**
	 * @throws BacktrackException
	 */
	protected void compoundStatement(IASTScope scope, boolean createNewScope)
			throws EndOfFileException, BacktrackException {
		IToken la = LA(1);
		int line = la.getLineNumber();
		char [] fn = la.getFilename();
		int startingOffset = consume(IToken.tLBRACE).getOffset();

		IASTCodeScope newScope = null;
		if (createNewScope) {
			try {
				newScope = astFactory.createNewCodeBlock(scope);
			} catch (Exception e) {
				int endOffset = ( lastToken == null ) ? 0 : lastToken.getEndOffset();
				logException("compoundStatement:createNewCodeBlock", e); //$NON-NLS-1$
				throwBacktrack(startingOffset, endOffset, line, fn);
			}
			newScope.enterScope(requestor);
		}

		try
		{
			setCompletionValues((createNewScope ? newScope : scope),
					CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.STATEMENT);
	
			while (LT(1) != IToken.tRBRACE) {
				int checkToken = LA(1).hashCode();
				try {
					statement((IASTCodeScope) (createNewScope ? newScope : scope));
				} catch (BacktrackException b) {
					failParse(b);
					if (LA(1).hashCode() == checkToken)
						failParseWithErrorHandling();
				}
				setCompletionValues(((createNewScope ? newScope : scope)),
						CompletionKind.SINGLE_NAME_REFERENCE,
						KeywordSetKey.STATEMENT);
			}
	
			consume(IToken.tRBRACE);
		}
		finally
		{	
			if (createNewScope)
				newScope.exitScope(requestor);
		}
	}

	protected IASTCompilationUnit compilationUnit;
	protected IToken simpleDeclarationMark;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser.IParser#getLanguage()
	 */
	public ParserLanguage getLanguage() {
		return language;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser.IParser#setLanguage(Language)
	 */
	public void setLanguage(ParserLanguage l) {
		language = l;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser.IParser#getLastErrorOffset()
	 */
	public int getLastErrorOffset() {
		return firstErrorOffset;
	}
	public int getLastErrorLine() {
	    return firstErrorLine;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public ISelectionParseResult parse(int startingOffset, int endingOffset)
			throws ParseError {
		if( mode != ParserMode.SELECTION_PARSE )
			throw new ParseError(ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED);
		offsetRange = new OffsetDuple( startingOffset, endingOffset );
		translationUnit();
		return reconcileTokenDuple();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) throws ParseError {
		if( mode != ParserMode.COMPLETION_PARSE )
			throw new ParseError(ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED);
		scanner.setOffsetBoundary(offset);
		//long startTime = System.currentTimeMillis();
		translationUnit();
		//long stopTime = System.currentTimeMillis();
		//System.out.println("Completion Parse time: " + (stopTime - startTime) + "ms");
		return new ASTCompletionNode( getCompletionKind(), getCompletionScope(), getCompletionContext(), getCompletionPrefix(), reconcileKeywords( getKeywordSet(), getCompletionPrefix() ), String.valueOf(getCompletionFunctionName()), getParameterListExpression() );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setupASTFactory(org.eclipse.cdt.core.parser.IScanner,
	 *      org.eclipse.cdt.core.parser.ParserLanguage)
	 */
	protected void setupASTFactory(IScanner scanner, ParserLanguage language) {
		astFactory = ParserFactory.createASTFactory( mode, language);
		scanner.setASTFactory(astFactory);
		astFactory.setLogger(log);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#getCompliationUnit()
	 */
	protected IASTNode getCompliationUnit() {
		return compilationUnit;
	}

	protected void endDeclaration(IASTDeclaration declaration)
			throws EndOfFileException {
		if( mode != ParserMode.SELECTION_PARSE || ! tokenDupleCompleted() )
			cleanupLastToken();
		else
		{
			contextNode = declaration;
			throw new EndOfFileException();
		}
	}

	/**
	 *  
	 */
	protected void cleanupLastToken() {
		if (lastToken != null)
			lastToken.setNext(null);
		simpleDeclarationMark = null;
	}

	/**

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#cancel()
	 */
	public synchronized void cancel() {
		isCancelled = true;
		scanner.cancel();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#validateCaches()
	 */
	public boolean validateCaches() {
		if( astFactory instanceof CompleteParseASTFactory)
			return ((CompleteParseASTFactory)astFactory).validateCaches();
		return true;
	}

	protected IASTScope contextualScope;
	protected CompletionKind cKind;
	protected IASTNode context;
	protected IToken finalToken;
	protected Set keywordSet;
	protected char[] functionOrConstructorName = EMPTY_STRING;
	protected char[] currentFunctionName = EMPTY_STRING;
	protected IASTExpression parameterListExpression;

	/**
	 * @return
	 */
	protected IASTScope getCompletionScope() {
		return contextualScope;
	}

	

	/**
	 * @return
	 */
	protected IASTCompletionNode.CompletionKind getCompletionKind() {
		return cKind;
	}

	/**
	 * @return
	 */
	protected String getCompletionPrefix() {
		return ( finalToken == null ? String.valueOf(EMPTY_STRING) : finalToken.getImage() ); 
	}

	/**
	 * @return
	 */
	protected IASTNode getCompletionContext() {
		return context;
	}

	protected void setCompletionContext(IASTNode node) {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
			this.context = node;
	}

	protected void setCompletionKind(IASTCompletionNode.CompletionKind kind) {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
			this.cKind = kind;
	}

	/**
	 * @param compilationUnit
	 * @param kind2
	 * @param set
	 * @param object
	 * @param string
	 */
	protected void setCompletionValues(CompletionKind kind, Set keywordSet, String prefix) {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			setCompletionScope(compilationUnit);
			this.keywordSet = keywordSet;
			setCompletionKind(kind);
			setCompletionContext(null);
			setCompletionFunctionName( );
			setCompletionToken( TokenFactory.createStandAloneToken( IToken.tIDENTIFIER, prefix ) );
		}
	}

	/**
	 */
	protected void setCompletionFunctionName() {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
			functionOrConstructorName = currentFunctionName;
	}
	
	

	protected void setCompletionKeywords(KeywordSetKey key) {
		this.keywordSet = KeywordSets.getKeywords( key, language );
	}

	protected void setCompletionToken(IToken token) {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
			finalToken = token;
	}

	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key, IASTNode node, String prefix) throws EndOfFileException {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			setCompletionToken( TokenFactory.createStandAloneToken( IToken.tIDENTIFIER, prefix ) );
			setCompletionValues(scope, kind, key, node );
		}
	}

	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
			setCompletionValues(scope, kind, key, null );
	}

	
	
	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key, IASTNode node) throws EndOfFileException {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			setCompletionScope(scope);
			setCompletionKeywords(key);
			setCompletionKind(kind);
			setCompletionContext(node);
			setCompletionFunctionName( );
			checkEndOfFile();
		}
	}

	
	protected void setCompletionValues( IASTScope scope, CompletionKind kind, IToken first, IToken last, List arguments, KeywordSetKey key ) throws EndOfFileException{
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			setCompletionScope( scope );
			setCompletionKind( kind );
			setCompletionKeywords(key);
			ITokenDuple duple = TokenFactory.createTokenDuple( first, last, arguments );
			try {
				setCompletionContext( astFactory.lookupSymbolInContext( scope, duple, null ) );
			} catch (ASTNotImplementedException e) {
			}
			setCompletionFunctionName();
		}
	}

	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key, IASTExpression firstExpression, Kind expressionKind) throws EndOfFileException {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			IASTNode node = astFactory.expressionToMostPreciseASTNode( scope, firstExpression );
			if( kind == CompletionKind.MEMBER_REFERENCE )
			{
				if( ! validMemberOperation( node, expressionKind ))
					node =null;
			}
			setCompletionValues(scope,kind,key, node  );
		}
	}

	/**
	 * @param node
	 * @param expressionKind
	 * @return
	 */
	private boolean validMemberOperation(IASTNode node, Kind expressionKind) {
		if( expressionKind == Kind.POSTFIX_ARROW_IDEXPRESSION || expressionKind == Kind.POSTFIX_ARROW_TEMPL_IDEXP )
			return astFactory.validateIndirectMemberOperation( node );
		else if( expressionKind == Kind.POSTFIX_DOT_IDEXPRESSION || expressionKind == Kind.POSTFIX_DOT_TEMPL_IDEXPRESS )
			return astFactory.validateDirectMemberOperation( node );
		return false;
	}	

	protected void setCompletionScope(IASTScope scope) {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
			this.contextualScope = scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setCompletionValues(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind)
	 */
	protected void setCompletionValues(IASTScope scope, CompletionKind kind) throws EndOfFileException {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			setCompletionScope(scope);
			setCompletionKind(kind);
			setCompletionFunctionName( );
			checkEndOfFile();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setCompletionValues(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind, org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	protected void setCompletionValues(IASTScope scope, CompletionKind kind,
			IASTNode context) throws EndOfFileException {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			setCompletionScope(scope);
			setCompletionKind(kind);
			setCompletionContext(context);
			setCompletionFunctionName( );
			checkEndOfFile();	
		}
	}

	/**
	 * @return
	 */
	protected char[] getCompletionFunctionName() {
		return functionOrConstructorName;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setCurrentFunctionName(java.lang.String)
	 */
	protected void setCurrentFunctionName(char[] functionName) {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
			currentFunctionName = functionName;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setCompletionValuesNoContext(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind, org.eclipse.cdt.internal.core.parser.token.KeywordSets.Key)
	 */
	protected void setCompletionValuesNoContext(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws EndOfFileException {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
		{
			setCompletionScope(scope);
			setCompletionKeywords(key);
			setCompletionKind(kind);
			checkEndOfFile();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setParameterListExpression(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	protected void setParameterListExpression(
			IASTExpression assignmentExpression) {
		if( mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE )
			parameterListExpression = assignmentExpression;
	}
	/**
	 * @return Returns the parameterListExpression.
	 */
	public final IASTExpression getParameterListExpression() {
		return parameterListExpression;
	}

	private OffsetDuple offsetRange;
	private IToken firstTokenOfDuple = null, lastTokenOfDuple = null;
	private IASTScope ourScope = null;
	private IASTCompletionNode.CompletionKind ourKind = null;
	private IASTNode ourContext = null;
	private ITokenDuple greaterContextDuple = null;
	private boolean pastPointOfSelection = false;
	private IASTNode contextNode = null;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleNewToken(org.eclipse.cdt.core.parser.IToken)
	 */
	protected void handleNewToken(IToken value) {
		if( mode != ParserMode.SELECTION_PARSE ) return;
		if( value != null && CharArrayUtils.equals(value.getFilename(), parserStartFilename))
		{
			TraceUtil.outputTrace(log, "IToken provided w/offsets ", null, value.getOffset(), " & ", value.getEndOffset() ); //$NON-NLS-1$ //$NON-NLS-2$
			boolean change = false;
			if( value.getOffset() == offsetRange.getFloorOffset() )
			{
				TraceUtil.outputTrace(log, "Offset Floor Hit w/token \"", null, value.getCharImage(), "\"", null ); //$NON-NLS-1$ //$NON-NLS-2$
				firstTokenOfDuple = value;
				change = true;
			}
			if( value.getEndOffset() == offsetRange.getCeilingOffset() )
			{
				TraceUtil.outputTrace(log, "Offset Ceiling Hit w/token \"", null, value.getCharImage(), "\"", null ); //$NON-NLS-1$ //$NON-NLS-2$
				change = true;
				lastTokenOfDuple = value;
			}
			if( change && tokenDupleCompleted() )
			{
				if ( ourScope == null )
					ourScope = getCompletionScope();
				if( ourContext == null )
					ourContext = getCompletionContext();
				if( ourKind == null )
					ourKind = getCompletionKind();
			}
		}
	}

	
	
	
	/**
	 * @return
	 */
	protected boolean tokenDupleCompleted() {
		return lastTokenOfDuple != null && lastTokenOfDuple.getEndOffset() >= offsetRange.getCeilingOffset();
	}


	/**
	 * 
	 */
	protected ISelectionParseResult reconcileTokenDuple() throws ParseError {
		if( firstTokenOfDuple == null || lastTokenOfDuple == null )
			throw new ParseError( ParseError.ParseErrorKind.OFFSET_RANGE_NOT_NAME );
		
		if( getCompletionKind() == IASTCompletionNode.CompletionKind.UNREACHABLE_CODE )
			throw new ParseError( ParseError.ParseErrorKind.OFFSETDUPLE_UNREACHABLE );
		
		ITokenDuple duple = TokenFactory.createTokenDuple( firstTokenOfDuple, lastTokenOfDuple );
		
		if( ! duple.syntaxOfName() )
			throw new ParseError( ParseError.ParseErrorKind.OFFSET_RANGE_NOT_NAME );
		
		return provideSelectionNode(duple);
	}

	/**
	 * @param duple
	 * @return
	 */
	protected ISelectionParseResult provideSelectionNode(ITokenDuple duple) {
		
		ITokenDuple finalDuple = null;
		// reconcile the name to look up first
		if( ! duple.equals( greaterContextDuple ))
		{
			// 3 cases			
			// duple is prefix of greaterContextDuple
			// or duple is suffix of greaterContextDuple
			// duple is a sub-duple of greaterContextDuple
			if( greaterContextDuple == null || duple.getFirstToken().equals( greaterContextDuple.getFirstToken() ))
				finalDuple = duple; //	=> do not use greaterContextDuple
			else if( duple.getLastSegment().getFirstToken().equals( greaterContextDuple.getLastSegment().getFirstToken() ))
				finalDuple = greaterContextDuple; //  => use greaterContextDuple
			else
				throw new ParseError( ParseError.ParseErrorKind.OFFSET_RANGE_NOT_NAME );
		}
		else
			finalDuple = greaterContextDuple;
		
		IASTNode node = lookupNode(finalDuple);
		if( node == null ) return null;
		if( !(node instanceof IASTOffsetableNamedElement )) return null;
		return new SelectionParseResult( (IASTOffsetableNamedElement) node, new String( ((IASTOffsetableElement)node).getFilename() )); 
	}




	/**
	 * @param finalDuple
	 * @return
	 */
	protected IASTNode lookupNode(ITokenDuple finalDuple) {
		if( contextNode == null ) return null;
		if( contextNode instanceof IASTDeclaration )
		{
			if( contextNode instanceof IASTOffsetableNamedElement && !(contextNode instanceof IASTUsingDirective) && !(contextNode instanceof IASTUsingDeclaration))
			{
				if( contextNode instanceof IASTFunction ) {
					Iterator i = ((IASTFunction)contextNode).getParameters();
					while (i.hasNext()) {
						IASTParameterDeclaration parm = (IASTParameterDeclaration)i.next();
						if (parm.getName().equals(finalDuple.toString()) && parm.getNameOffset() == finalDuple.getStartOffset() && parm.getStartingLine() == finalDuple.getLineNumber() && parm instanceof IASTNode) {
							return (IASTNode) parm;
						}
					}
				}

				if (contextNode instanceof IASTMethod) {
					Iterator parms = ((IASTMethod)contextNode).getParameters();
					while (parms.hasNext()) {
						Object parm = parms.next();
						if (parm instanceof IASTParameterDeclaration && ((IASTParameterDeclaration)parm).getName().equals(finalDuple.toString()) && ((IASTParameterDeclaration)parm).getNameOffset() == finalDuple.getStartOffset()) {
							return (IASTNode)parm;
						}
					}
				}
				
				if( ((IASTOffsetableNamedElement)contextNode).getName().equals( finalDuple.toString() ) && ((IASTOffsetableNamedElement)contextNode).getNameOffset() == finalDuple.getStartOffset())  // 75731 needs to include offset for equality as well...
					return contextNode;
			}
			if( contextNode instanceof IASTQualifiedNameElement )
			{
				String [] elementQualifiedName = ((IASTQualifiedNameElement)contextNode).getFullyQualifiedName();
				if( Arrays.equals( elementQualifiedName, finalDuple.toQualifiedName() ) ){
				    IASTNode declNode = null;
					if( contextNode instanceof ISymbolOwner ){
					    ISymbolOwner owner = (ISymbolOwner) contextNode;
					    if( owner.getSymbol() != null && owner.getSymbol().getASTExtension() != null ){
					        declNode = owner.getSymbol().getASTExtension().getPrimaryDeclaration();
					    }
					}
					return (declNode != null) ? declNode : contextNode;
				}
			}
			try {
				if( ourKind == IASTCompletionNode.CompletionKind.NEW_TYPE_REFERENCE )
				{
					if( contextNode instanceof IASTVariable )
					{
						IASTInitializerClause initializer = ((IASTVariable)contextNode).getInitializerClause();
						if( initializer != null )
						{
							IASTExpression ownerExpression = initializer.findExpressionForDuple( finalDuple );
							return astFactory.lookupSymbolInContext( ourScope, finalDuple, ownerExpression );
						}
					}
				}
					
				return astFactory.lookupSymbolInContext( ourScope, finalDuple, null );
			} catch (ASTNotImplementedException e) {
				return null;
			}
		}
		else if( contextNode instanceof IASTExpression )
		{
			try {
				return astFactory.lookupSymbolInContext( ourScope, finalDuple, contextNode );
			} catch (ASTNotImplementedException e) {
				return null;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setGreaterNameContext(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	protected void setGreaterNameContext(ITokenDuple tokenDuple) {
		if( mode != ParserMode.SELECTION_PARSE ) return;
		if( pastPointOfSelection ) return;
		if( greaterContextDuple == null && lastTokenOfDuple != null && firstTokenOfDuple != null && CharArrayUtils.equals(tokenDuple.getFilename(), parserStartFilename))
		{
			if( tokenDuple.getStartOffset() > lastTokenOfDuple.getEndOffset() )
			{
				pastPointOfSelection = true;
				return;
			}
			int tokensFound = 0;

			for( IToken token = tokenDuple.getFirstToken(); token != null; token = token.getNext() )
			{
				if( token == firstTokenOfDuple ) ++tokensFound;
				if( token == lastTokenOfDuple ) ++tokensFound;
				if( token == tokenDuple.getLastToken() )
					break;
			}
			if( tokensFound == 2 )
			{
				greaterContextDuple = tokenDuple;
				pastPointOfSelection = true;
			}
			
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#endExpressionStatement(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	protected void endExpression(IASTExpression expression)
			throws EndOfFileException {
		if( mode != ParserMode.SELECTION_PARSE || ! tokenDupleCompleted() )
			cleanupLastToken();
		else
		{
			contextNode = expression;
			throw new EndOfFileException();
		}
		
	}

	
	public static class SelectionParseResult implements ISelectionParseResult 
	{

		public SelectionParseResult( IASTOffsetableNamedElement node, String fileName )
		{
			this.node = node;
			this.fileName = fileName;
		}
		
		private final String fileName;
		private final IASTOffsetableNamedElement node;

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.IParser.ISelectionParseResult#getNode()
		 */
		public IASTOffsetableNamedElement getOffsetableNamedElement() {
			return node;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.IParser.ISelectionParseResult#getFilename()
		 */
		public String getFilename() {
			return fileName;
		}
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#endEnumerator(org.eclipse.cdt.core.parser.ast.IASTEnumerator)
	 */
	protected void endEnumerator(IASTEnumerator enumerator) throws EndOfFileException {
		if( mode != ParserMode.SELECTION_PARSE || ! tokenDupleCompleted() )
			cleanupLastToken();
		else
		{
			contextNode = enumerator;
			throw new EndOfFileException();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#endClassSpecifier(org.eclipse.cdt.core.parser.ast.IASTClassSpecifier)
	 */
	protected void handleClassSpecifier(IASTClassSpecifier classSpecifier)
			throws EndOfFileException {
		if( mode != ParserMode.SELECTION_PARSE ||  ! tokenDupleCompleted() )
			cleanupLastToken();
		else
		{
			contextNode = classSpecifier;
			throw new EndOfFileException();
		}
	}
	
	protected void handleEnumeration(IASTEnumerationSpecifier enumeration) throws EndOfFileException {
		if( mode != ParserMode.SELECTION_PARSE || ! tokenDupleCompleted() )
			cleanupLastToken();
		else
		{
			contextNode = enumeration;
			throw new EndOfFileException();
		}
	}

	/**
	 * @param set
	 * @param string
	 * @return
	 */
	private Set reconcileKeywords(Set keywords, String prefix) {
		if( keywords == null ) return null;
		if( prefix.equals( "")) return keywords; //$NON-NLS-1$
		Set resultSet = new TreeSet(); 
		Iterator i = keywords.iterator(); 
		while( i.hasNext() )
		{
			String value = (String) i.next();
			if( value.startsWith( prefix ) ) 
				resultSet.add( value );
			else if( value.compareTo( prefix ) > 0 ) 
				break;
		}
		return resultSet;
	}


	/**
	 * @return
	 */
	protected Set getKeywordSet() {
		return keywordSet;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleOffsetLimitException()
	 */
	protected void handleOffsetLimitException(OffsetLimitReachedException exception) throws EndOfFileException {
		if( mode != ParserMode.COMPLETION_PARSE )
			throw new EndOfFileException();

		if( exception.getCompletionNode() == null )
		{	
			setCompletionToken( exception.getFinalToken() );
			if( (finalToken!= null )&& (!finalToken.canBeAPrefix() ))
				setCompletionToken(null);
		}
		else
		{
			ASTCompletionNode node = (ASTCompletionNode) exception.getCompletionNode();
			setCompletionValues( node.getCompletionKind(), node.getKeywordSet(), node.getCompletionPrefix() );
		}
	
		throw exception;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#getCompletionKindForDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	protected CompletionKind getCompletionKindForDeclaration(IASTScope scope, CompletionKind overide) {
		if( mode != ParserMode.COMPLETION_PARSE ) return null;
		IASTCompletionNode.CompletionKind kind = null;
		if( overide != null )
			kind = overide;
		else if( scope instanceof IASTClassSpecifier )
			kind = CompletionKind.FIELD_TYPE;
		else if (scope instanceof IASTCodeScope)
			kind = CompletionKind.SINGLE_NAME_REFERENCE;
		else
			kind = CompletionKind.VARIABLE_TYPE;
		return kind;
	}	
	
	
	protected IToken getCompletionToken()
	{ 
		if( mode != ParserMode.COMPLETION_PARSE )
			return null;
		return finalToken;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTReferenceStore#processReferences()
	 */
	public static void processReferences(List references, ISourceElementRequestor requestor )
	{
		if( references == null || references.isEmpty() )
			return;
	    
	    for( int i = 0; i < references.size(); ++i )
	    {
	    	IASTReference reference = ((IASTReference)references.get(i));
			reference.acceptElement(requestor );
	    }
	    
	    references.clear();
	}

}
