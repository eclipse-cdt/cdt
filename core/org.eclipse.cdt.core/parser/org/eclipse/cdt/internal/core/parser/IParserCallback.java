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
package org.eclipse.cdt.internal.core.parser;

public interface IParserCallback {

	public void setParser( IParser parser );
	
	public Object translationUnitBegin();
	public void translationUnitEnd(Object unit);
	
	public Object inclusionBegin(String includeFile, int nameBeginOffset, int inclusionBeginOffset);
	public void inclusionEnd(Object inclusion);
	public Object macro(String macroName, int macroNameOffset, int macroBeginOffset, int macroEndOffset);
	
	public Object simpleDeclarationBegin(Object Container, Token firstToken);
	public void simpleDeclSpecifier(Object Container, Token specifier);
	public void simpleDeclSpecifierName( Object declaration );
	public void simpleDeclarationEnd(Object declaration, Token lastToken);

	public Object parameterDeclarationBegin( Object Container ); 
	public void  parameterDeclarationEnd( Object declaration ); 
	
	public void nameBegin(Token firstToken);
	public void nameEnd(Token lastToken);
	
	public Object declaratorBegin(Object container);
	public void declaratorId(Object declarator);
	public void declaratorAbort( Object declarator );
	public void declaratorPureVirtual( Object declarator );
	public void declaratorCVModifier( Object declarator, Token modifier );
	public void declaratorThrowsException( Object declarator ); 
	public void declaratorThrowExceptionName( Object declarator );
	public void declaratorEnd(Object declarator);
		
	public Object arrayDeclaratorBegin( Object declarator );
	public void arrayDeclaratorEnd( Object arrayQualifier );
	
	public Object pointerOperatorBegin( Object container );
	public void pointerOperatorType( Object ptrOperator, Token type );
	public void pointerOperatorName( Object ptrOperator );
	public void pointerOperatorCVModifier( Object ptrOperator, Token modifier );
	public void pointerOperatorAbort( Object ptrOperator );
	public void pointerOperatorEnd( Object ptrOperator );
		
	public Object argumentsBegin( Object declarator );
	public void argumentsEnd(Object parameterDeclarationClause);
	
	public Object functionBodyBegin(Object declaration);
	public void functionBodyEnd(Object functionBody);
	
	public Object classSpecifierBegin(Object container, Token classKey);
	public void classSpecifierName(Object classSpecifier);
	public void classSpecifierAbort( Object classSpecifier ); 
	public void classMemberVisibility( Object classSpecifier, Token visibility );
	public void classSpecifierEnd(Object classSpecifier, Token closingBrace );
	
	public Object	baseSpecifierBegin( Object containingClassSpec );
	public void	baseSpecifierName( Object baseSpecifier );
	public void 	baseSpecifierVisibility( Object baseSpecifier, Token visibility );
	public void 	baseSpecifierVirtual( Object baseSpecifier, boolean virtual );
	public void  	baseSpecifierEnd( Object baseSpecifier );
	
	public Object 	expressionBegin( Object container ); 
	public void 	expressionOperator(Object expression, Token operator);
	public void 	expressionTerminal(Object expression, Token terminal);
	public void		expressionName( Object expression );
	public void     expressionAbort( Object expression ); 
	public void 	expressionEnd(Object expression );
	
	public Object	elaboratedTypeSpecifierBegin( Object container, Token classKey ); 
	public void  	elaboratedTypeSpecifierName( Object elab ); 
	public void 	elaboratedTypeSpecifierEnd( Object elab );
	
	public Object	namespaceDefinitionBegin( Object container, Token namespace ); 
	public void		namespaceDefinitionId( Object namespace );
	public void 	namespaceDefinitionAbort( Object namespace );
	public void		namespaceDefinitionEnd( Object namespace, Token closingBrace );

	public Object   linkageSpecificationBegin( Object container, String literal );
	public void     linkageSpecificationEnd( Object linkageSpec );
	
	public Object	usingDirectiveBegin( Object container );
	public void		usingDirectiveNamespaceId( Object directive );
	public void 	usingDirectiveAbort( Object directive ); 
	public void		usingDirectiveEnd( Object directive );
	
	public Object	usingDeclarationBegin( Object container );
	public void		usingDeclarationMapping( Object declaration, boolean isTypeName );
	public void		usingDeclarationAbort( Object declaration );
	public void		usingDeclarationEnd( Object declaration );

	public Object	enumSpecifierBegin( Object container, Token enumKey );
	public void		enumSpecifierId( Object enumSpec );
	public void		enumSpecifierAbort( Object enumSpec );
	public void 	enumSpecifierEnd( Object enumSpec, Token closingBrace );
	
	public Object	enumeratorBegin( Object enumSpec );
	public void		enumeratorId( Object enumDefn );
	public void		enumeratorEnd( Object enumDefn, Token lastToken );
	
	public void		asmDefinition( Object container, String assemblyCode );
	
	public Object	constructorChainBegin( Object declarator );
	public void		constructorChainAbort( Object ctor );
	public void 	constructorChainEnd( Object ctor );
	
	public Object	constructorChainElementBegin( Object ctor );
	public void		constructorChainElementId( Object element );
	public void		constructorChainElementEnd( Object element );
	
	public Object	constructorChainElementExpressionListElementBegin( Object element ); 
	public void		constructorChainElementExpressionListElementEnd( Object expression );
	
	public Object	explicitInstantiationBegin( Object container);
	public void		explicitInstantiationEnd( Object instantiation ); 
	
	public Object	explicitSpecializationBegin( Object container );
	public void		explicitSpecializationEnd( Object instantiation );

	public Object	templateDeclarationBegin( Object container, Token firstToken );
	public void 	templateDeclarationAbort( Object templateDecl );
	public void		templateDeclarationEnd( Object templateDecl, Token lastToken );	
	
	public Object	templateParameterListBegin( Object declaration );
	public void		templateParameterListEnd( Object parameterList );
	
	public Object	templateTypeParameterBegin( Object templDecl, Token kind );
	public void		templateTypeParameterName( Object typeParm );
	public void		templateTypeParameterAbort( Object typeParm );
	public void 	templateTypeParameterInitialTypeId( Object typeParm );
	public void 	templateTypeParameterEnd( Object typeParm );
	
}
