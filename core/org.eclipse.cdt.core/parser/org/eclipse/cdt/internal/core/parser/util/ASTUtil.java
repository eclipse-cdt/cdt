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
package org.eclipse.cdt.internal.core.parser.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateParameter;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;

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
			if((parameter.getIdentifier() != null) && (parameter.getIdentifier().length() != 0))
			{
				paramList.add(parameter.getIdentifier().toString());
			}
			else
			{				
				IASTTemplateParameter.ParamKind kind = parameter.getTemplateParameterKind();
				if(kind == IASTTemplateParameter.ParamKind.CLASS){
					paramType.append("class"); //$NON-NLS-1$
				}
				if(kind == IASTTemplateParameter.ParamKind.TYPENAME){
					paramType.append("typename"); //$NON-NLS-1$
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
			} // end else
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
					String literal = (expression.getLiteralString().length() > 0 
									? expression.getLiteralString() 
									: expression.getIdExpression() );
					if(literal.length() > 0){
						initializer.append("="); //$NON-NLS-1$
						initializer.append(literal);
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
		IASTTypeSpecifier typeSpecifier = declaration.getTypeSpecifier();
		if(typeSpecifier instanceof IASTElaboratedTypeSpecifier){
			IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier) typeSpecifier;
			type.append(getElaboratedTypeSignature(elab));
		}else if(typeSpecifier instanceof IASTSimpleTypeSpecifier){		
			IASTSimpleTypeSpecifier simpleSpecifier = (IASTSimpleTypeSpecifier) typeSpecifier;		
			type.append(simpleSpecifier.getTypename());
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

}
