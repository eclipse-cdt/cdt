/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ast.IASTExpression.IASTNewExpressionDescriptor;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;


/**
 * This is a utility class to help convert AST elements to Strings.
 */

public class ASTUtil {
	public static String[] getTemplateParameters(IASTTemplateDeclaration templateDeclaration){
		// add the parameters
		Iterator i = templateDeclaration.getTemplateParameters();
		return getTemplateParameters(i);
	}
	public static String[] getTemplateParameters(Iterator templateParams){
		List paramList = new ArrayList();
		while (templateParams.hasNext()){
			StringBuffer paramType = new StringBuffer();
			IASTTemplateParameter parameter = (IASTTemplateParameter)templateParams.next();
			IASTTemplateParameter.ParamKind kind = parameter.getTemplateParameterKind();
			if(kind == IASTTemplateParameter.ParamKind.CLASS){
				if((parameter.getIdentifier() != null) && (parameter.getIdentifier().length() != 0))
				{
					paramType.append(parameter.getIdentifier().toString());
				}else {
					paramType.append("class"); //$NON-NLS-1$
				}
			}
			if(kind == IASTTemplateParameter.ParamKind.TYPENAME){
				if((parameter.getIdentifier() != null) && (parameter.getIdentifier().length() != 0))
				{
					paramType.append(parameter.getIdentifier().toString());
				}else {
					paramType.append("typename"); //$NON-NLS-1$
				}
			}
			if(kind == IASTTemplateParameter.ParamKind.TEMPLATE_LIST){
				paramType.append("template<"); //$NON-NLS-1$
				String[] subParams = getTemplateParameters(parameter.getTemplateParameters());
				int p = 0; 
				if ( subParams.length > 0)
					paramType.append(subParams[p++]);
				while( p < subParams.length){
					paramType.append(", "); //$NON-NLS-1$
					paramType.append(subParams[p++]);							
				}
				paramType.append(">"); //$NON-NLS-1$
			}
			if(kind == IASTTemplateParameter.ParamKind.PARAMETER){
				paramType.append(getType(parameter.getParameterDeclaration()));				
			}
			paramList.add(paramType.toString());
		}// end while
		String[] parameterTypes = new String[paramList.size()];
		for(int j=0; j<paramList.size(); ++j){
			parameterTypes[j] = (String) paramList.get(j);			
		}
		return parameterTypes;		
		
	}	
		
	public static String getType(IASTAbstractDeclaration declaration)
	{
		StringBuffer type = new StringBuffer();
			
		// get type from declaration
		type.append(getDeclarationType(declaration));
		type.append(getPointerOperation(declaration));
		type.append(getArrayQualifiers(declaration));
		
		type.append(getPointerToFunctionType(declaration));
		if (declaration instanceof IASTParameterDeclaration)
			type.append(getInitializerClause((IASTParameterDeclaration)declaration));
		return type.toString();
	}
	    
	public static String getInitializerClause(IASTParameterDeclaration declaration){
		StringBuffer initializer = new StringBuffer();
		if(declaration != null){
			IASTInitializerClause clause = declaration.getDefaultValue();
			if(clause != null){
				IASTExpression expression = clause.getAssigmentExpression();
				if(expression != null){
					String init = getExpressionString( expression );
					if(init.length() > 0){
						initializer.append("="); //$NON-NLS-1$
						initializer.append(init);
					}
				}
			}
		}
		return initializer.toString();
	}
	public static String getPointerToFunctionType(IASTAbstractDeclaration declaration){
		StringBuffer type = new StringBuffer();
		ASTPointerOperator po = declaration.getPointerToFunctionOperator();
		if(po != null){
			type.append("("); //$NON-NLS-1$
			type.append(getPointerOperator(po));
			type.append(")"); //$NON-NLS-1$
			String[] parameters =getParameterTypes(declaration.getParameters(), false /* replace with takeVarArgs() later*/); 
			type.append(getParametersString(parameters));
		}
		return type.toString();
	}
	public static String getDeclarationType(IASTAbstractDeclaration declaration){
		StringBuffer type = new StringBuffer();
		
		if(declaration.isConst())
			type.append("const "); //$NON-NLS-1$
		if(declaration.isVolatile())
			type.append("volatile "); //$NON-NLS-1$
		IASTTypeSpecifier typeSpecifier = declaration.getTypeSpecifier();
		if(typeSpecifier instanceof IASTElaboratedTypeSpecifier){
			IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier) typeSpecifier;
			type.append(getElaboratedTypeSignature(elab));
		}else if(typeSpecifier instanceof IASTSimpleTypeSpecifier){		
			IASTSimpleTypeSpecifier simpleSpecifier = (IASTSimpleTypeSpecifier) typeSpecifier;		
			type.append(simpleSpecifier.getTypename());
		}else if(typeSpecifier instanceof IASTClassSpecifier){		
			IASTClassSpecifier classSpecifier = (IASTClassSpecifier) typeSpecifier;		
			type.append(classSpecifier.getName());
		}else if(typeSpecifier instanceof IASTEnumerationSpecifier){		
			IASTEnumerationSpecifier enumSpecifier = (IASTEnumerationSpecifier) typeSpecifier;		
			type.append(enumSpecifier.getName());
		}
		return type.toString();	
	}
	
	public static String getElaboratedTypeSignature(IASTElaboratedTypeSpecifier elab){
		StringBuffer type = new StringBuffer();
		ASTClassKind t = elab.getClassKind();
		if( t == ASTClassKind.CLASS){
			type.append("class"); //$NON-NLS-1$
		} 
		else if( t == ASTClassKind.STRUCT){
			type.append("struct"); //$NON-NLS-1$
		}
		else if( t == ASTClassKind.UNION){
			type.append("union"); //$NON-NLS-1$
		}
		else if( t == ASTClassKind.STRUCT){
			type.append("enum"); //$NON-NLS-1$
		}
		type.append(" "); //$NON-NLS-1$
		type.append(elab.getName().toString());
		return type.toString();
	}
	
	public static String getPointerOperation(IASTAbstractDeclaration declaration){		
		StringBuffer pointerString = new StringBuffer();
		Iterator i = declaration.getPointerOperators();
		while(i.hasNext()){
			ASTPointerOperator po = (ASTPointerOperator) i.next();
			pointerString.append(getPointerOperator(po));
		}
		return pointerString.toString();
	}
	
	public static String getPointerOperator(ASTPointerOperator po){
		String pointerString =""; //$NON-NLS-1$
		if(po == ASTPointerOperator.POINTER)
			pointerString = ("*"); //$NON-NLS-1$

		if(po == ASTPointerOperator.REFERENCE)
			pointerString =("&"); //$NON-NLS-1$

		if(po == ASTPointerOperator.CONST_POINTER)
			pointerString =("* const"); //$NON-NLS-1$

		if(po == ASTPointerOperator.VOLATILE_POINTER)
			pointerString =("* volatile"); //$NON-NLS-1$
			
		if(po == ASTPointerOperator.RESTRICT_POINTER)
			pointerString =("* restrict"); //$NON-NLS-1$
			
		return pointerString;						
	}
	
	public static String getArrayQualifiers(IASTAbstractDeclaration declaration){		
		StringBuffer arrayString = new StringBuffer();
		Iterator i  = declaration.getArrayModifiers(); 
		while (i.hasNext()){
			i.next();
			arrayString.append("[]");				 //$NON-NLS-1$
		}
		return arrayString.toString();
	}
	
	public static String[] getFunctionParameterTypes(IASTFunction functionDeclaration)
	{
		Iterator parameters = functionDeclaration.getParameters();
		return getParameterTypes(parameters, functionDeclaration.takesVarArgs());
	}

	public static String[] getParameterTypes(Iterator parameters, boolean takesVarArgs){
		List paramList = new ArrayList();
		while (parameters.hasNext()){
			IASTParameterDeclaration param = (IASTParameterDeclaration)parameters.next();
			paramList.add(getType(param));
		}
		int paramListSize = paramList.size();
		if(takesVarArgs)
			paramListSize++;
		String[] parameterTypes = new String[paramListSize];
		for(int i=0; i<paramList.size(); ++i){
			parameterTypes[i] = (String)paramList.get(i); 
		}
		// add the ellipse to the parameter type list
		if(takesVarArgs)
			parameterTypes[paramListSize-1] = "..."; //$NON-NLS-1$
		return parameterTypes;			
	}
	public static String getParametersString(String[] parameterTypes) 
	{
		StringBuffer parameters = new StringBuffer(""); //$NON-NLS-1$
		
		if ((parameterTypes != null) && (parameterTypes.length > 0)) {
			parameters.append("("); //$NON-NLS-1$
			int i = 0;
			parameters.append(parameterTypes[i++]);
			while (i < parameterTypes.length) {
				parameters.append(", "); //$NON-NLS-1$
				parameters.append(parameterTypes[i++]);
			}
			parameters.append(")"); //$NON-NLS-1$
		} else {
			if (parameterTypes != null) parameters.append("()"); //$NON-NLS-1$
		}
		
		return parameters.toString();
	}	    

	public static String getTypeId( IASTTypeId id ){
		StringBuffer type = new StringBuffer();

		if( id.isTypename() ){
			type.append( Keywords.TYPENAME );
			type.append( ' ' );
		}
		type.append( id.getFullSignature() );
		
//		Iterator i = id.getPointerOperators();
//		while(i.hasNext()){
//			ASTPointerOperator po = (ASTPointerOperator) i.next();
//			type.append(getPointerOperator(po));
//		}
//		
//		i  = id.getArrayModifiers(); 
//		while (i.hasNext()){
//			i.next();
//			type.append("[]");				 //$NON-NLS-1$
//		}
		
		return type.toString();
	}
	
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	/**
	 * Return a string for the given expression.  Expressions having an extension kind should
	 * provide their own toString method which will be called by this.
	 * @param expression
	 * @return
	 */
	public static String getExpressionString( IASTExpression expression ){
		
		if( expression.getExpressionKind().isExtensionKind() )
			return expression.toString();
		
		String literal = expression.getLiteralString();
		String idExpression = expression.getIdExpression();
		
		IASTExpression lhs = expression.getLHSExpression();
		IASTExpression rhs = expression.getRHSExpression();
		IASTExpression third = expression.getThirdExpression();
		IASTNewExpressionDescriptor descriptor = expression.getNewExpressionDescriptor();
		IASTTypeId typeId = expression.getTypeId();

		if( literal != null && !literal.equals( EMPTY_STRING ) && ( idExpression == null || idExpression.equals( EMPTY_STRING ) ) )
			return getLiteralExpression( expression );
			
		if( idExpression != null && !idExpression.equals( EMPTY_STRING ) && lhs == null )
			return getIdExpression( expression );
		
		if( third != null )
			return getConditionalExpression( expression );
		
		if( descriptor != null  )
			return getNewExpression( expression );
		
		if( lhs != null && rhs != null )
			return getBinaryExpression( expression );
		
		if( lhs != null && typeId != null )
			return getUnaryTypeIdExpression( expression );
		
		if( lhs != null && ( idExpression != null && !idExpression.equals( EMPTY_STRING ) ) )
			return getUnaryIdExpression( expression );
		
		if( lhs != null )
			return getUnaryExpression( expression );
		
		if( typeId != null )
			return getTypeIdExpression( expression );

		return getEmptyExpression( expression );
	}
	
	private static String getEmptyExpression( IASTExpression expression ){
		if( expression.getExpressionKind() == Kind.PRIMARY_THIS )
			return Keywords.THIS;
		
		return EMPTY_STRING;
	}
	
	private static String getLiteralExpression( IASTExpression expression ){
		Kind kind = expression.getExpressionKind();
		
		if( kind != Kind.PRIMARY_CHAR_LITERAL && kind != Kind.PRIMARY_STRING_LITERAL )
			return expression.getLiteralString();
		
		StringBuffer buffer = new StringBuffer();
		boolean quoted = false;
		String literalString = expression.getLiteralString();
		if( kind == Kind.PRIMARY_CHAR_LITERAL ){
			quoted = ( literalString.charAt(0) == literalString.charAt(literalString.length() - 1 ) && 
					   literalString.charAt(0) == '\'' ); 
			if( !quoted )
				buffer.append( '\'' );
			buffer.append( literalString );
			if( !quoted )
				buffer.append( '\'' );
		} else if( kind == Kind.PRIMARY_STRING_LITERAL ) {
			quoted = ( literalString.charAt(0) == literalString.charAt(literalString.length() - 1 ) && 
					   literalString.charAt(0) == '\"' ); 
			if( !quoted )
				buffer.append( '"' );
			buffer.append( expression.getLiteralString() );
			if( !quoted )
				buffer.append( '"' );
		}
		return buffer.toString();
	}
	
	private static String getIdExpression( IASTExpression expression ){
		return expression.getIdExpression();
	}
	private static String getConditionalExpression( IASTExpression expression ){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append( getExpressionString( expression.getLHSExpression() ) );
		buffer.append( " ? " ); //$NON-NLS-1$
		buffer.append( getExpressionString( expression.getRHSExpression() ) );
		buffer.append( " : " ); //$NON-NLS-1$
		buffer.append( getExpressionString( expression.getThirdExpression() ) );
		
		return buffer.toString();
	}
	private static String getNewExpression( IASTExpression expression ){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append( Keywords.NEW );
		buffer.append( ' ' );
		
		IASTNewExpressionDescriptor descriptor = expression.getNewExpressionDescriptor();
		Iterator iter = descriptor.getNewPlacementExpressions();
		if( iter.hasNext() ){
			buffer.append( '(' );
			buffer.append( getExpressionString( (IASTExpression) iter.next() ) );
			buffer.append( ") " ); //$NON-NLS-1$
		}
		
		iter = descriptor.getNewTypeIdExpressions();
		if( iter.hasNext() ){
			buffer.append( getExpressionString( (IASTExpression) iter.next() ) );
			buffer.append( ' ' ); 
		}
		
		if( expression.getTypeId() != null ){
			buffer.append( getTypeId( expression.getTypeId() ) );
		}
		
		iter = descriptor.getNewInitializerExpressions();
		if( iter.hasNext() ){
			buffer.append( '(' );
			buffer.append( getExpressionString( (IASTExpression) iter.next() ) );
			buffer.append( ')' ); 
		}
		
		return buffer.toString();
	}
	private static String getBinaryExpression( IASTExpression expression ){
		Kind kind = expression.getExpressionKind();
		StringBuffer buffer = new StringBuffer();
		buffer.append( getExpressionString( expression.getLHSExpression() ) );
		
		boolean appendSpace = false;
		if( kind != Kind.EXPRESSIONLIST && 
			kind != Kind.PM_DOTSTAR &&
			kind != Kind.PM_ARROWSTAR &&
			kind != Kind.POSTFIX_SUBSCRIPT &&
			kind != Kind.POSTFIX_FUNCTIONCALL &&
			kind != Kind.POSTFIX_DOT_TEMPL_IDEXPRESS &&
            kind != Kind.POSTFIX_DOT_IDEXPRESSION &&
			kind != Kind.POSTFIX_DOT_DESTRUCTOR &&
			kind != Kind.POSTFIX_ARROW_TEMPL_IDEXP &&
            kind != Kind.POSTFIX_ARROW_IDEXPRESSION &&
			kind != Kind.POSTFIX_ARROW_DESTRUCTOR)
		{
			appendSpace = true;
			buffer.append( ' ' );
		}
		
		if( kind == Kind.ANDEXPRESSION ||
			kind == Kind.EXPRESSIONLIST ||
			kind == Kind.EXCLUSIVEOREXPRESSION ||
			kind == Kind.PM_DOTSTAR ||
			kind == Kind.PM_ARROWSTAR ||
			kind == Kind.LOGICALANDEXPRESSION ||
			kind == Kind.LOGICALOREXPRESSION ||
			kind == Kind.RELATIONAL_GREATERTHAN ||
			kind == Kind.RELATIONAL_LESSTHAN || 
			kind == Kind.RELATIONAL_LESSTHANEQUALTO ||
			kind == Kind.RELATIONAL_GREATERTHANEQUALTO ||
			kind == Kind.EQUALITY_EQUALS ||
            kind == Kind.EQUALITY_NOTEQUALS ||
            kind == Kind.ADDITIVE_PLUS ||
            kind == Kind.ADDITIVE_MINUS ||
			kind == Kind.INCLUSIVEOREXPRESSION ||
			kind == Kind.MULTIPLICATIVE_MULTIPLY ||
        	kind == Kind.MULTIPLICATIVE_DIVIDE ||
			kind == Kind.MULTIPLICATIVE_MODULUS ||
			kind == Kind.POSTFIX_DOT_TEMPL_IDEXPRESS ||
            kind == Kind.POSTFIX_DOT_IDEXPRESSION ||
            kind == Kind.POSTFIX_DOT_DESTRUCTOR ||
			kind == Kind.POSTFIX_ARROW_TEMPL_IDEXP ||
            kind == Kind.POSTFIX_ARROW_IDEXPRESSION ||
			kind == Kind.POSTFIX_ARROW_DESTRUCTOR ||
			kind == Kind.ASSIGNMENTEXPRESSION_NORMAL ||
			kind == Kind.ASSIGNMENTEXPRESSION_MULT ||
			kind == Kind.ASSIGNMENTEXPRESSION_DIV ||
			kind == Kind.ASSIGNMENTEXPRESSION_MOD ||
			kind == Kind.ASSIGNMENTEXPRESSION_PLUS ||
			kind == Kind.ASSIGNMENTEXPRESSION_MINUS ||
			kind == Kind.ASSIGNMENTEXPRESSION_RSHIFT ||
			kind == Kind.ASSIGNMENTEXPRESSION_LSHIFT ||
			kind == Kind.ASSIGNMENTEXPRESSION_AND ||
			kind == Kind.ASSIGNMENTEXPRESSION_XOR  ||
			kind == Kind.ASSIGNMENTEXPRESSION_OR ||
			kind == Kind.SHIFT_LEFT ||
			kind == Kind.SHIFT_RIGHT)
		{
			buffer.append( ASTUtil.getStringForKind( kind ) );
		} else if( kind == Kind.POSTFIX_SUBSCRIPT )
			buffer.append( '[' );
		else if( kind == Kind.POSTFIX_FUNCTIONCALL )
			buffer.append( '(' );
		
		if( kind == Kind.POSTFIX_DOT_TEMPL_IDEXPRESS ||
			kind == IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP)
		{
			buffer.append( ' ' );
			buffer.append( Keywords.TEMPLATE );
			buffer.append( ' ' );
		}
			
		if( appendSpace || kind == Kind.EXPRESSIONLIST )
			buffer.append( ' ' );
		
		buffer.append( getExpressionString( expression.getRHSExpression() ) );
		
		if( kind == Kind.POSTFIX_SUBSCRIPT )
			buffer.append( ']' );
		else if( kind == Kind.POSTFIX_FUNCTIONCALL )
			buffer.append( ')' );
		
		return buffer.toString();
	}

	private static String getUnaryTypeIdExpression( IASTExpression expression ){
		StringBuffer buffer = new StringBuffer();
		
		Kind kind = expression.getExpressionKind();
		if( kind == Kind.CASTEXPRESSION ){
			buffer.append( '(' );
			buffer.append( getTypeId( expression.getTypeId() ) );
			buffer.append( ')' );
			buffer.append( getExpressionString( expression.getLHSExpression() ) );
			
		} else if ( kind == Kind.POSTFIX_DYNAMIC_CAST || 
				    kind == Kind.POSTFIX_STATIC_CAST ||
					kind == Kind.POSTFIX_REINTERPRET_CAST ||
					kind == Kind.POSTFIX_CONST_CAST )
		{
			buffer.append( ASTUtil.getStringForKind( kind ) );
			buffer.append( '<' );
			buffer.append( getTypeId( expression.getTypeId() ) );
			buffer.append( ">(" ); //$NON-NLS-1$
			buffer.append( getExpressionString( expression.getLHSExpression() ) );
			buffer.append( ')' );
		}			
		
		return buffer.toString();
	}
	
	private static String getUnaryIdExpression( IASTExpression expression ){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append( Keywords.TYPENAME );
		buffer.append( ' ' );
		if( expression.getExpressionKind() == Kind.POSTFIX_TYPENAME_TEMPLATEID ){
			buffer.append( Keywords.TEMPLATE );
			buffer.append( ' ' );
		}
		buffer.append( expression.getIdExpression() );
		buffer.append( '(' );
		buffer.append( getExpressionString( expression.getLHSExpression() ) );
		buffer.append( ')' );
		
		return buffer.toString();
	}
	private static String getUnaryExpression( IASTExpression expression ){
		StringBuffer buffer = new StringBuffer();
		Kind kind = expression.getExpressionKind();
		
		boolean bracketsAroundExpression = ( kind == Kind.PRIMARY_BRACKETED_EXPRESSION );
		
		if ( kind == Kind.UNARY_SIZEOF_UNARYEXPRESSION ){
			buffer.append( Keywords.SIZEOF );
			buffer.append( ' ' );
		}
		else if ( kind == Kind.UNARY_STAR_CASTEXPRESSION ||
				  kind == Kind.UNARY_AMPSND_CASTEXPRESSION ||
				  kind == Kind.UNARY_PLUS_CASTEXPRESSION ||
				  kind == Kind.UNARY_MINUS_CASTEXPRESSION ||
				  kind == Kind.UNARY_NOT_CASTEXPRESSION ||
				  kind == Kind.UNARY_TILDE_CASTEXPRESSION ||
				  kind == Kind.UNARY_DECREMENT ||
				  kind == Kind.THROWEXPRESSION
				)
		{
			buffer.append( ASTUtil.getStringForKind( kind ) );
		}
		else if ( kind == Kind.UNARY_INCREMENT )
			buffer.append( "++" );  //$NON-NLS-1$
		else if( kind == Kind.DELETE_VECTORCASTEXPRESSION || kind == Kind.DELETE_CASTEXPRESSION ){
			buffer.append( Keywords.DELETE );
			buffer.append(' ');
			if( kind == Kind.DELETE_VECTORCASTEXPRESSION )
				buffer.append( "[ ] " ); //$NON-NLS-1$
		} else if( kind == Kind.POSTFIX_SIMPLETYPE_CHAR || 
				   kind == Kind.POSTFIX_SIMPLETYPE_WCHART ||
				   kind == Kind.POSTFIX_SIMPLETYPE_BOOL ||
				   kind == Kind.POSTFIX_SIMPLETYPE_SHORT ||
				   kind == Kind.POSTFIX_SIMPLETYPE_INT ||
				   kind == Kind.POSTFIX_SIMPLETYPE_LONG ||
				   kind == Kind.POSTFIX_SIMPLETYPE_SIGNED ||
				   kind == Kind.POSTFIX_SIMPLETYPE_UNSIGNED ||
				   kind == Kind.POSTFIX_SIMPLETYPE_FLOAT ||
				   kind == Kind.POSTFIX_SIMPLETYPE_DOUBLE
				 )
		{
			buffer.append( ASTUtil.getStringForKind( kind ) );
			bracketsAroundExpression = true;
		}  else if( kind == Kind.POSTFIX_TYPEID_EXPRESSION )
		{
			buffer.append( Keywords.TYPEID );
			bracketsAroundExpression = true;
		}
		
		if( bracketsAroundExpression )
			buffer.append( '(' );
		
		buffer.append( getExpressionString( expression.getLHSExpression() ) );
		
		if( bracketsAroundExpression )
			buffer.append( ')' );
		
		if( kind == Kind.POSTFIX_INCREMENT ||
			kind == Kind.POSTFIX_DECREMENT )
		{
			buffer.append( ASTUtil.getStringForKind( kind ) );
		}

		return buffer.toString();
	}
	private static String getTypeIdExpression( IASTExpression expression ){
		StringBuffer buffer = new StringBuffer();
		
		Kind kind = expression.getExpressionKind();
		
		boolean addBrackets = false;
		if( kind == Kind.UNARY_SIZEOF_TYPEID ){
			buffer.append( Keywords.SIZEOF );
			buffer.append( ' ' );
			addBrackets = true;
		} else if( kind == Kind.POSTFIX_TYPEID_TYPEID ){
			buffer.append( Keywords.TYPEID );
			addBrackets = true;
		}
		
		if( addBrackets )
			buffer.append( '(' );
		buffer.append( ASTUtil.getTypeId( expression.getTypeId() ) );
		if( addBrackets )
			buffer.append( ')' );
		
		return buffer.toString();
	}
	
	private static final Map expressionKindStringMap = new HashMap();
	static {
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_DYNAMIC_CAST,  		Keywords.DYNAMIC_CAST );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_STATIC_CAST,  			Keywords.STATIC_CAST );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_REINTERPRET_CAST, 		Keywords.REINTERPRET_CAST );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_CONST_CAST, 			Keywords.CONST_CAST );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_CHAR, 		Keywords.CHAR );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_WCHART, 	Keywords.WCHAR_T );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_BOOL, 		Keywords.BOOL );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_SHORT, 		Keywords.SHORT );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_INT, 		Keywords.INT );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_LONG, 		Keywords.LONG );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_SIGNED, 	Keywords.SIGNED );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_UNSIGNED, 	Keywords.UNSIGNED );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_FLOAT, 		Keywords.FLOAT );
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_SIMPLETYPE_DOUBLE, 	Keywords.DOUBLE );
		expressionKindStringMap.put( IASTExpression.Kind.THROWEXPRESSION, 				Keywords.THROW );
        expressionKindStringMap.put( IASTExpression.Kind.ANDEXPRESSION, 				"&" ); //$NON-NLS-1$
        expressionKindStringMap.put( IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION, 	"&" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.EXPRESSIONLIST, 				"," ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.EXCLUSIVEOREXPRESSION, 		"^" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.PM_DOTSTAR, 					".*" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.PM_ARROWSTAR, 					"->*" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.LOGICALANDEXPRESSION, 			"&&" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.LOGICALOREXPRESSION, 			"||" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.RELATIONAL_GREATERTHAN, 		">" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.RELATIONAL_LESSTHAN, 			"<" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.RELATIONAL_LESSTHANEQUALTO, 	"<=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.RELATIONAL_GREATERTHANEQUALTO,	">=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.EQUALITY_EQUALS, 				"==" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.EQUALITY_NOTEQUALS, 			"!=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION, 	"*" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY, 		"*" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION, 	"+" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ADDITIVE_PLUS, 				"+" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION, 	"-" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ADDITIVE_MINUS, 				"-" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION, 		"!" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION, 	"~" );  //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.UNARY_DECREMENT, 				"--" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_DECREMENT, 			"--" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.UNARY_INCREMENT, 				"++" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_INCREMENT, 			"++" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.INCLUSIVEOREXPRESSION, 		"|" ); //$NON-NLS-1$		
    	expressionKindStringMap.put( IASTExpression.Kind.MULTIPLICATIVE_DIVIDE, 		"/" ); //$NON-NLS-1$
    	expressionKindStringMap.put( IASTExpression.Kind.MULTIPLICATIVE_MODULUS, 		"%" ); //$NON-NLS-1$
    	expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS, 	"." ); //$NON-NLS-1$	
    	expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION, 		"." ); //$NON-NLS-1$   |
		expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_DOT_DESTRUCTOR, 		"." ); //$NON-NLS-1$
    	expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP, 	"->" ); //$NON-NLS-1$
    	expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_ARROW_DESTRUCTOR, 		"->" ); //$NON-NLS-1$
    	expressionKindStringMap.put( IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION, 	"->" ); //$NON-NLS-1$
    	expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_NORMAL,	"=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_MULT,		"*=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_DIV,		"/=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_MOD,		"%=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_PLUS,		"+=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_MINUS,	"-=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_RSHIFT,	">>=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_LSHIFT,	"<<=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_AND,		"&=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_XOR,		"^=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.ASSIGNMENTEXPRESSION_OR,		"|=" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.SHIFT_LEFT, 					"<<" ); //$NON-NLS-1$
		expressionKindStringMap.put( IASTExpression.Kind.SHIFT_RIGHT, 					">>" ); //$NON-NLS-1$

	}
	private static String getStringForKind( IASTExpression.Kind kind ){
        return (String) expressionKindStringMap.get( kind );
	}
}
