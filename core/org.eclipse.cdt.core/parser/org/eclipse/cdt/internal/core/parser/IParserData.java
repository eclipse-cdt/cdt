/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser;
import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
/**
 * @author jcamelon
 *
 */
public interface IParserData  {
	
	public IASTExpression expression(IASTScope scope, CompletionKind kind,
			KeywordSetKey key) throws BacktrackException, EndOfFileException;
			
	/**
	 * @return Returns the astFactory.
	 */
	public abstract IASTFactory getAstFactory();
	/**
	 * @return Returns the log.
	 */
	public abstract IParserLogService getLog();
	/**
	 * Look Ahead in the token list to see what is coming.  
	 * 
	 * @param i		How far ahead do you wish to peek?
	 * @return		the token you wish to observe
	 * @throws EndOfFileException	if looking ahead encounters EOF, throw EndOfFile 
	 */
	public abstract IToken LA(int i) throws EndOfFileException;
	/**
	 * Look ahead in the token list and return the token type.  
	 * 
	 * @param i				How far ahead do you wish to peek?
	 * @return				The type of that token
	 * @throws EndOfFileException	if looking ahead encounters EOF, throw EndOfFile
	 */
	public abstract int LT(int i) throws EndOfFileException;
	/**
	 * Consume the next token available, regardless of the type.  
	 * 
	 * @return				The token that was consumed and removed from our buffer.  
	 * @throws EndOfFileException	If there is no token to consume.  
	 */
	public abstract IToken consume() throws EndOfFileException;
	/**
	 * Consume the next token available only if the type is as specified.  
	 * 
	 * @param type			The type of token that you are expecting.  	
	 * @return				the token that was consumed and removed from our buffer. 
	 * @throws BacktrackException	If LT(1) != type 
	 */
	public abstract IToken consume(int type) throws EndOfFileException,
			BacktrackException;
	/**
	 * Mark our place in the buffer so that we could return to it should we have to.  
	 * 
	 * @return				The current token. 
	 * @throws EndOfFileException	If there are no more tokens.
	 */
	public abstract IToken mark() throws EndOfFileException;
	/**
	 * Rollback to a previous point, reseting the queue of tokens.  
	 * 
	 * @param mark		The point that we wish to restore to.  
	 *  
	 */
	public abstract void backup(IToken mark);
	/**
	 * @param string
	 * @param e
	 */
	public abstract void logException(String string, Exception e);
	/**
	 * @param scope
	 * @param b
	 * @param kind
	 * @return
	 */
	public IASTTypeId typeId(IASTScope scope, boolean b, CompletionKind kind) throws EndOfFileException, BacktrackException;
	/**
	 * @param scope
	 * @param kind
	 * @param key TODO
	 * @return
	 */
	public IASTExpression unaryExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException, BacktrackException;
	/**
	 * @return
	 */
	public IToken getLastToken();
	/**
	 * @return
	 */
	public abstract ParserLanguage getParserLanguage();
	/**
	 * @param scope
	 * @param kind
	 * @param key TODO
	 * @return
	 */
	public IASTExpression shiftExpression(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws BacktrackException, EndOfFileException;
	
	public IToken identifier() throws EndOfFileException, BacktrackException;
}