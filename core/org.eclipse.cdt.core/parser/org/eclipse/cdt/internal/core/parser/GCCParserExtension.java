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

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.Enum;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTSemanticException;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTypeId;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCDesignator;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCExpression;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.internal.core.parser.Parser.Flags;


/**
 * @author jcamelon
 *
 */
public class GCCParserExtension implements IParserExtension {

	private static final char[] EMPTY_STRING = "".toCharArray();//$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#isValidCVModifier(org.eclipse.cdt.core.parser.ParserLanguage, int)
	 */
	public boolean isValidCVModifier(ParserLanguage language, int tokenType) {
		if( tokenType == IToken.t_restrict && language == ParserLanguage.CPP )
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#getPointerOperator(org.eclipse.cdt.core.parser.ParserLanguage, int)
	 */
	public ASTPointerOperator getPointerOperator(ParserLanguage language, int tokenType) {
		if( tokenType == IToken.t_restrict && language == ParserLanguage.CPP )
			return ASTPointerOperator.RESTRICT_POINTER;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#isValidUnaryExpressionStart(int)
	 */
	public boolean isValidUnaryExpressionStart(int tokenType) {
		switch( tokenType )
		{
			case IGCCToken.t___alignof__:
			case IGCCToken.t_typeof:
				return true;
			default:
				return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#parseUnaryExpression(org.eclipse.cdt.internal.core.parser.IParserData)
	 */
	public IASTExpression parseUnaryExpression(IASTScope scope, IParserData data, IASTCompletionNode.CompletionKind kind, KeywordSetKey key) {
		try {
			switch( data.LT(1))
			{
				case IGCCToken.t___alignof__:
					return performUnaryExpression( data, scope, kind, key, UnaryExpressionKind.ALIGNOF );
				case IGCCToken.t_typeof:
					return performUnaryExpression( data, scope, kind, key, UnaryExpressionKind.TYPEOF );
				default:
					break;
			}
		} catch (EndOfFileException e) {
			//TODO
		}
		return null;
	}


	protected static class UnaryExpressionKind extends Enum
	{
		public static final UnaryExpressionKind ALIGNOF = new UnaryExpressionKind( 1 );
		public static final UnaryExpressionKind TYPEOF = new UnaryExpressionKind( 2 );
		/**
		 * @param enumValue
		 */
		protected UnaryExpressionKind(int enumValue) {
			super(enumValue);
		}
		
	}
	
	/**
	 * @param data
	 * @param scope
	 * @param kind
	 * @param key TODO
	 * @param type TODO
	 * @return
	 */
	protected IASTExpression performUnaryExpression(IParserData data, IASTScope scope, CompletionKind kind, KeywordSetKey key, UnaryExpressionKind type) {
		IToken startingPoint = null;
		try
		{
			if( type == UnaryExpressionKind.ALIGNOF )
				startingPoint = data.consume(IGCCToken.t___alignof__);
			else if( type == UnaryExpressionKind.TYPEOF )
				startingPoint = data.consume(IGCCToken.t_typeof );
		} catch( BacktrackException b )
		{
			return null;
		} catch (EndOfFileException e) {
			return null;
		}
		
		try
		{
	        IToken mark = data.mark();
	        IASTTypeId d = null;
	        IASTExpression unaryExpression = null;
	        if (data.LT(1) == IToken.tLPAREN)
	        {
	            try
	            {
	                data.consume(IToken.tLPAREN);
	                d = data.typeId(scope, false, CompletionKind.TYPE_REFERENCE);
	                data.consume(IToken.tRPAREN);
	            }
	            catch (BacktrackException bt)
	            {
	            	data.backup(mark);
	            	d = null;
	                unaryExpression = data.unaryExpression(scope,kind, key);
	            }
	        }
	        else
	        {
	            unaryExpression = data.unaryExpression(scope,kind, key);
	        }
	        if (d != null & unaryExpression == null)
	            try
	            {
	            	IASTExpression.Kind expKind = null;
	            	if( type == UnaryExpressionKind.ALIGNOF )
	            		expKind = IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID;
	            	else if( type == UnaryExpressionKind.TYPEOF )
	            		expKind = IASTGCCExpression.Kind.UNARY_TYPEOF_TYPEID;
	                return data.getAstFactory().createExpression(
	                    scope,
	                    expKind,
	                    null,
	                    null,
	                    null,
	                    d,
	                    null, EMPTY_STRING, null); 
	            }
	            catch (ASTSemanticException e)
	            {
	            	data.backup( startingPoint );
	                return null;
	            } catch (Exception e)
	            {
	            	data.logException( "unaryExpression_1::createExpression()", e ); //$NON-NLS-1$
	               	data.backup( startingPoint );
	                return null;
	            }
	        else if (unaryExpression != null && d == null)
	            try
	            {
	            	IASTExpression.Kind expKind = null;
	            	if( type == UnaryExpressionKind.ALIGNOF )
	            		expKind = IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION;
	            	else if( type == UnaryExpressionKind.TYPEOF )
	            		expKind = IASTGCCExpression.Kind.UNARY_TYPEOF_UNARYEXPRESSION;

	                return data.getAstFactory().createExpression(
	                    scope,
	                    expKind,
	                    unaryExpression,
	                    null,
	                    null,
	                    null,
	                    null, EMPTY_STRING, null); 
	            }
	            catch (ASTSemanticException e1)
	            {
	               	data.backup( startingPoint );
	                return null;
	            } catch (Exception e)
	            {
	            	data.logException( "unaryExpression_1::createExpression()", e ); //$NON-NLS-1$
	               	data.backup( startingPoint );
	                return null;
	            }
	        return null;
		}
		catch( BacktrackException bt )
		{
			data.backup( startingPoint );
			return null;
		}
		catch( EndOfFileException eof )
		{
			data.backup( startingPoint );
			return null;			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#canHandleDeclSpecifierSequence(int)
	 */
	public boolean canHandleDeclSpecifierSequence(int tokenType) {
		switch( tokenType )
		{
			case IGCCToken.t_typeof:
				return true;
			default:
				return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#handleDeclSpecifierSequence(org.eclipse.cdt.internal.core.parser.IParserData, org.eclipse.cdt.core.model.Flags, org.eclipse.cdt.internal.core.parser.DeclarationWrapper)
	 */
	public IDeclSpecifierExtensionResult parseDeclSpecifierSequence(IParserData data, Parser.Flags flags, DeclarationWrapper sdw, CompletionKind kind, KeywordSetKey key) {
		IToken startingPoint = null;
		try
		{
			startingPoint = data.mark();
		} catch( EndOfFileException eof )
		{
			return null;
		}
		
		try
		{
			switch( data.LT(1))
			{
				case IGCCToken.t_typeof:
					IASTExpression typeOfExpression = performUnaryExpression( data, sdw.getScope(), kind, key, UnaryExpressionKind.TYPEOF );
					if( typeOfExpression != null )
					{
						sdw.setSimpleType( IASTGCCSimpleTypeSpecifier.Type.TYPEOF );
						flags.setEncounteredRawType(true);
						Hashtable params = new Hashtable();
						params.put( IASTGCCSimpleTypeSpecifier.TYPEOF_EXRESSION, typeOfExpression );
						sdw.setExtensionParameter( IASTGCCSimpleTypeSpecifier.TYPEOF_EXRESSION, typeOfExpression );
						return new GCCDeclSpecifierExtensionResult( startingPoint, data.getLastToken(), flags, params );
					}
					data.backup( startingPoint );
					return null;
				default:
					data.backup( startingPoint );
					return null;
			}
		} 
		catch( EndOfFileException eof )
		{
			data.backup( startingPoint );
		}
		return null;
	}	

	public class GCCDeclSpecifierExtensionResult implements IDeclSpecifierExtensionResult 
	{
		private final IToken first;
		private final IToken last;
		private final Flags flags;

		/**
		 * @param startingPoint
		 * @param token
		 * @param flags
		 */
		public GCCDeclSpecifierExtensionResult(IToken startingPoint, IToken token, Flags flags, Hashtable params) {
			first = startingPoint;
			last = token;
			this.flags = flags;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.extension.IParserExtension.IDeclSpecifierExtensionResult#getFirstToken()
		 */
		public IToken getFirstToken() {
			return first;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.extension.IParserExtension.IDeclSpecifierExtensionResult#getLastToken()
		 */
		public IToken getLastToken() {
			return last;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.extension.IParserExtension.IDeclSpecifierExtensionResult#getFlags()
		 */
		public Flags getFlags() {
			return flags;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#isValidRelationalExpressionStart(int)
	 */
	public boolean isValidRelationalExpressionStart(ParserLanguage language, int tokenType) {
		switch( tokenType )
		{
			case IGCCToken.tMAX:
			case IGCCToken.tMIN:
				return true;
			default:
				return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#parseRelationalExpression(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.internal.core.parser.IParserData, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind)
	 */
	public IASTExpression parseRelationalExpression(IASTScope scope, IParserData data, CompletionKind kind, KeywordSetKey key, IASTExpression lhsExpression) {
		if( data.getParserLanguage() == ParserLanguage.C ) return null;
		IToken mark = null;
		try {
			mark = data.mark();
		} catch (EndOfFileException e) {
			return null;
		}
		IASTGCCExpression.Kind expressionKind = null;
		try
		{
			switch( data.LT(1) )
			{
				case IGCCToken.tMAX:
					data.consume( IGCCToken.tMAX );
					expressionKind = IASTGCCExpression.Kind.RELATIONAL_MAX;
					break;
				case IGCCToken.tMIN:
					data.consume( IGCCToken.tMIN );
					expressionKind = IASTGCCExpression.Kind.RELATIONAL_MIN;
					break;
				default:
					data.backup( mark );
					return null;
			}
			
            IToken next = data.LA(1);
            IASTExpression secondExpression = data.shiftExpression(scope,kind, key);
            if (next == data.LA(1))
            {
                // we did not consume anything
                // this is most likely an error
                data.backup(mark);
                return null;
            }

            try {
				IASTExpression resultExpression = data.getAstFactory().createExpression( 
						scope, expressionKind, lhsExpression, secondExpression, null, null, null, EMPTY_STRING, null );
				return resultExpression;
			} catch (ASTSemanticException e1) {
				data.backup( mark );
				return null;
			}
            
		} catch( EndOfFileException eof )
		{
			data.backup( mark );
			return null;
		} catch( BacktrackException bt )
		{
			data.backup( mark );
			return null;			
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#canHandleCDesignatorInitializer(int)
	 */
	public boolean canHandleCDesignatorInitializer(int tokenType) {
		switch( tokenType )
		{
			case IToken.tIDENTIFIER:
			case IToken.tLBRACKET:
				return true;
			default:
				return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#parseDesignator(org.eclipse.cdt.internal.core.parser.IParserData)
	 */
	public IASTDesignator parseDesignator(IParserData parserData, IASTScope scope) {
		IToken startingPoint = null;
		try {
			startingPoint = parserData.mark();
		} catch (EndOfFileException e) {
			return null;
		}
		
		try
		{
			if( parserData.LT(1) == IToken.tIDENTIFIER )
			{
				IToken identifier = parserData.identifier();
				parserData.consume( IToken.tCOLON );
				return parserData.getAstFactory().createDesignator( IASTDesignator.DesignatorKind.FIELD, null, identifier, null );
			}
			if( parserData.LT(1) == IToken.tLBRACKET )
			{
				parserData.consume( IToken.tLBRACKET );
				IASTExpression constantExpression1 = parserData.expression( scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION );
				parserData.consume( IToken.tELLIPSIS );
				IASTExpression constantExpression2 = parserData.expression( scope, CompletionKind.SINGLE_NAME_REFERENCE, KeywordSetKey.EXPRESSION );
				parserData.consume(IToken.tRBRACKET );
				Map extensionParms = new Hashtable();
				extensionParms.put( IASTGCCDesignator.SECOND_EXRESSION, constantExpression2 );
				return parserData.getAstFactory().createDesignator( IASTGCCDesignator.DesignatorKind.SUBSCRIPT_RANGE, constantExpression1, null, extensionParms );
			}
		}
		catch( EndOfFileException eof )
		{
		    //nothing
		}
		catch( BacktrackException bt )
		{
		    //nothing
		}
		parserData.backup( startingPoint );
		return null;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#supportsStatementsInExpressions()
	 */
	public boolean supportsStatementsInExpressions() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#getExpressionKindForStatement()
	 */
	public Kind getExpressionKindForStatement() {
		return IASTGCCExpression.Kind.STATEMENT_EXPRESSION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#supportsExtendedTemplateInstantiationSyntax()
	 */
	public boolean supportsExtendedTemplateInstantiationSyntax() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.extension.IParserExtension#isValidModifierForInstantiation(org.eclipse.cdt.core.parser.IToken)
	 */
	public boolean isValidModifierForInstantiation(IToken la) {
		if( la == null ) return false;
		switch( la.getType() )
		{
			case IToken.t_static:
			case IToken.t_inline:
			case IToken.t_extern: 
				return true;
			default:
				return false;
		}
	}

}
