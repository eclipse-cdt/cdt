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
package org.eclipse.cdt.internal.core.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.internal.core.parser.IParserCallback;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.cdt.internal.core.parser.util.AccessSpecifier;
import org.eclipse.cdt.internal.core.parser.util.DeclSpecifier;
import org.eclipse.cdt.internal.core.parser.util.Name;

public class NewModelBuilder implements IParserCallback {

	private TranslationUnitWrapper translationUnit = new TranslationUnitWrapper();
	
	
	public NewModelBuilder(TranslationUnit tu) {
		translationUnit.setElement( tu );
	}
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginTranslationUnit()
	 */
	public Object translationUnitBegin() {
		return translationUnit;
	}
	
	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginClass(String, String)
	 */
	public Object classSpecifierBegin(Object container, Token classKey) {
		if( container instanceof SimpleDeclarationWrapper )
		{
			SimpleDeclarationWrapper c = (SimpleDeclarationWrapper)container; 
				
			SimpleDeclarationWrapper wrapper = new SimpleDeclarationWrapper();
			wrapper.setClassKind( classKey );
			switch( classKey.getType() )
			{
				case Token.t_class:
					wrapper.setCurrentVisibility( AccessSpecifier.v_private );
					break;
				case Token.t_struct:
				case Token.t_union:
					wrapper.setCurrentVisibility( AccessSpecifier.v_public );
					break;
			}
			
			
			wrapper.setParent( c.getParent() );
			
			return wrapper;
		}
		else
			return null;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classSpecifierName() 
	 */
	public void classSpecifierName(Object classSpecifier) 
	{
		if( classSpecifier instanceof SimpleDeclarationWrapper )
		{
			SimpleDeclarationWrapper wrapper = (SimpleDeclarationWrapper)classSpecifier;
			wrapper.setName( currName );
		}
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endClass()
	 */
	public void classSpecifierEnd(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#beginDeclarator()
	 */
	public Object declaratorBegin(Object container) {
		DeclSpecifier.Container declSpec = (DeclSpecifier.Container)container;
		List declarators = declSpec.getDeclarators();
		Declarator declarator =new Declarator();
		declarators.add( declarator );  
		return declarator; 
	}


	private int startIdPos;
	private int idLength;

	private CElement elem;
		
	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#endDeclarator()
	 */
	public void declaratorEnd( Object declarator) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginFunctionBody()
	 */
	public Object functionBodyBegin(Object declaration) {
		SimpleDeclarationWrapper wrapper = (SimpleDeclarationWrapper)declaration; 
		wrapper.setFunctionDefinition(true);
		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.newparser.IParserCallback#macro(String)
	 */
	public void macro(String macroName, int offset) {
		Macro elem = new Macro((TranslationUnit)translationUnit.getElement(), macroName);
		elem.setIdPos(offset, macroName.length());
		elem.setPos(offset, macroName.length());

		((TranslationUnit)translationUnit.getElement()).addChild(elem);
	}

	private int startPos;

	/**
	 * @see 
org.eclipse.cdt.internal.core.newparser.IParserCallback#beginSimpleDeclaration(Token)
	 */
	public Object simpleDeclarationBegin(Object container) {
		ICElementWrapper wrapper = (ICElementWrapper)container;
		// Assuming that the parent is the container's element 
		IParent parent = (IParent)wrapper.getElement();
		SimpleDeclarationWrapper result = new SimpleDeclarationWrapper();
		result.setParent( parent );
		// A special case to transfere the visibility
		if( wrapper instanceof SimpleDeclarationWrapper ){
			result.setCurrentVisibility(((SimpleDeclarationWrapper)wrapper).getCurrentVisibility());
		}
		return result;  	
	}
	
	

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#beginInclusion(String)
	 */
	public void inclusionBegin(String includeFile, int offset) {
		Include elem = new Include(((TranslationUnit)translationUnit.getElement()), includeFile);
		((TranslationUnit)translationUnit.getElement()).addChild(elem);
		elem.setIdPos(offset, includeFile.length());
		elem.setPos(offset, includeFile.length());
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#endInclusion()
	 */
	public void inclusionEnd() {
	}
	
	private Name currName;
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(Token firstToken) {
		currName = new Name(firstToken);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(Token lastToken) {
		currName.setEnd(lastToken);
	}


	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(java.lang.Object)
	 */
	public void simpleDeclarationEnd(Object declaration) {
		SimpleDeclarationWrapper wrapper = (SimpleDeclarationWrapper)declaration; 
		wrapper.createElements();
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclSpecifier(java.lang.Object, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclSpecifier(Object declSpec, Token specifier) { 
		DeclSpecifier declSpecifier = (DeclSpecifier)declSpec;  
		declSpecifier.setType( specifier ); 				
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(java.lang.Object)
	 */
	public void declaratorId(Object declarator) {
		Declarator decl = (Declarator)declarator; 
		decl.setName( currName ); 
	}


	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsBegin(java.lang.Object)
	 */
	public Object argumentsBegin(Object declarator) {
		Declarator decl = (Declarator)declarator;
		List parameterDeclarationClause = new LinkedList();
		decl.setParameterDeclarationClause( parameterDeclarationClause );
		return parameterDeclarationClause;  
		
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierBegin(java.lang.Object)
	 */
	public Object baseSpecifierBegin(Object containingClassSpec) {
		return null;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierEnd(java.lang.Object)
	 */
	public void baseSpecifierEnd(Object baseSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierName(java.lang.Object)
	 */
	public void baseSpecifierName(Object baseSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierVirtual(java.lang.Object, boolean)
	 */
	public void baseSpecifierVirtual(Object baseSpecifier, boolean virtual) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#baseSpecifierVisibility(java.lang.Object, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void baseSpecifierVisibility(
		Object baseSpecifier,
		Token visibility) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionOperator(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionOperator(Object expression, Token operator) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionTerminal(Object expression, Token terminal) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyEnd()
	 */
	public void functionBodyEnd(Object functionBody) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#parameterDeclarationBegin(java.lang.Object, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object parameterDeclarationBegin(
		Object container ) {
		List parameterDeclarationClause = (List)container; 
		Parameter p = new Parameter();
		parameterDeclarationClause.add( p );
		return p; 
		
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#parameterDeclarationEnd(java.lang.Object)
	 */
	public void parameterDeclarationEnd(Object declaration) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitEnd(java.lang.Object)
	 */
	public void translationUnitEnd(Object unit) {
	}
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsEnd()
	 */
	public void argumentsEnd(Object parameterDeclarationClause) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorAbort(java.lang.Object, java.lang.Object)
	 */
	public void declaratorAbort(Object container, Object declarator) {
		DeclSpecifier.Container declSpec = (DeclSpecifier.Container)container;
		Declarator toBeRemoved =(Declarator)declarator; 
		declSpec.removeDeclarator( toBeRemoved ); 
		toBeRemoved = null; 
		currName = null;   		
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionBegin(java.lang.Object)
	 */
	public Object expressionBegin(Object container) {
		return null;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionEnd(java.lang.Object)
	 */
	public void expressionEnd(Object expression) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierAbort(java.lang.Object)
	 */
	public void classSpecifierAbort(Object classSpecifier) {
		classSpecifier = null; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classSpecifierSafe(java.lang.Object)
	 */
	public void classSpecifierSafe(Object classSpecifier) {
		SimpleDeclarationWrapper wrapper = (SimpleDeclarationWrapper)classSpecifier;
		int kind;
		
		switch( wrapper.getClassKind().getType() )
		{
			case Token.t_class:
				kind = ICElement.C_CLASS;
				break;
			case Token.t_struct:
				kind = ICElement.C_STRUCT;	
				break;	
			default:
				kind = ICElement.C_UNION;
				break;
		}
		Structure elem = new Structure( (CElement)wrapper.getParent(), kind, null );
		wrapper.setElement( elem );
		((Parent)wrapper.getParent()).addChild(elem);
		
		String elementName = ( wrapper.getName() == null ) ? "" : wrapper.getName().toString();
		elem.setElementName( elementName );
		if( wrapper.getName() != null )
		{ 	
			elem.setTypeName( wrapper.getClassKind().getImage() );
			elem.setIdPos(wrapper.getName().getStartOffset(), elementName.length());
			elem.setPos(wrapper.getName().getStartOffset(), elementName.length());
		}
		else
		{
			elem.setTypeName( wrapper.getClassKind().getImage() );
			elem.setIdPos(wrapper.getClassKind().getOffset(), wrapper.getClassKind().getLength());
			elem.setPos(wrapper.getClassKind().getOffset(), wrapper.getClassKind().getLength());		
		}
	}
	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierBegin(java.lang.Object)
	 */
	public Object elaboratedTypeSpecifierBegin(Object container, Token classKey) {
		if( container instanceof SimpleDeclarationWrapper )
		{
			SimpleDeclarationWrapper c = (SimpleDeclarationWrapper)container; 
											
			SimpleDeclarationWrapper wrapper = new SimpleDeclarationWrapper();
			wrapper.setClassKind( classKey );
			wrapper.setParent( c.getParent() );
					
			return wrapper;
		}
		else
			return null;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierEnd(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierEnd(Object elab) {
		SimpleDeclarationWrapper wrapper = (SimpleDeclarationWrapper)elab;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#elaboratedTypeSpecifierName(java.lang.Object)
	 */
	public void elaboratedTypeSpecifierName(Object elab) {
		if( elab instanceof SimpleDeclarationWrapper )
		{
			SimpleDeclarationWrapper wrapper = (SimpleDeclarationWrapper)elab;
			wrapper.setName( currName );
		}
	}



	/**
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#simpleDeclSpecifierName(java.lang.Object)
	 */
	public void simpleDeclSpecifierName(Object declaration) {
		DeclSpecifier declSpecifier = (DeclSpecifier)declaration;  
		declSpecifier.setName( currName ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#expressionAbort(java.lang.Object)
	 */
	public void expressionAbort(Object expression) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#classMemberVisibility(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void classMemberVisibility(Object classSpecifier, Token visibility) {
		SimpleDeclarationWrapper spec = (SimpleDeclarationWrapper)classSpecifier;
		switch( visibility.getType() )
		{
			case Token.t_public:
				spec.setCurrentVisibility( AccessSpecifier.v_public );
				break;
			case Token.t_protected:
				spec.setCurrentVisibility( AccessSpecifier.v_protected );
				break;
			case Token.t_private:
				spec.setCurrentVisibility( AccessSpecifier.v_private );
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object pointerOperatorBegin(Object container) {
		Declarator d = (Declarator)container;
		PointerOperator po = new PointerOperator(d); 
		return po;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorEnd(java.lang.Object)
	 */
	public void pointerOperatorEnd(Object ptrOperator) {
		PointerOperator po = (PointerOperator)ptrOperator;
		po.getOwnerDeclarator().addPointerOperator( po );
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorName(java.lang.Object)
	 */
	public void pointerOperatorName(Object ptrOperator) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorType(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorType(Object ptrOperator, Token type) {
		PointerOperator po=(PointerOperator)ptrOperator;
		switch( type.getType() )
		{
			case Token.tSTAR: 
				po.setKind( PointerOperator.k_pointer );
				break;
			case Token.tAMPER:
				po.setKind( PointerOperator.k_reference );
				break;
			default: 
		}	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void pointerOperatorCVModifier(Object ptrOperator, Token modifier) {
		PointerOperator po=(PointerOperator)ptrOperator;
		switch( modifier.getType() )
		{
			case Token.t_const: 
				po.setConst( true );
				break;
			case Token.t_volatile:
				po.setVolatile( true );
				break;
			default: 
		}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorCVModifier(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public void declaratorCVModifier(Object declarator, Token modifier) {
		Declarator d = (Declarator)declarator;
		switch( modifier.getType() )
		{
			case Token.t_const:
				d.setConst( true );
				break;
			case Token.t_volatile:
				d.setVolatile(true);
				break;
			default:
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayBegin(java.lang.Object)
	 */
	public Object arrayDeclaratorBegin(Object declarator) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#arrayEnd(java.lang.Object)
	 */
	public void arrayDeclaratorEnd(Object arrayQualifier ) {
		// TODO Auto-generated method stub
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#exceptionSpecificationTypename(java.lang.Object)
	 */
	public void declaratorThrowExceptionName(Object declarator) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorThrowsException(java.lang.Object)
	 */
	public void declaratorThrowsException(Object declarator) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationBegin(java.lang.Object)
	 */
	public Object namespaceDefinitionBegin(Object container) {

		ICElementWrapper c = (ICElementWrapper)container; 
		NamespaceWrapper wrapper = new NamespaceWrapper((IParent)c.getElement());		
		return wrapper;  	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationId(java.lang.Object)
	 */
	public void namespaceDefinitionId(Object namespace) {
		// set wrapper name to current name
		NamespaceWrapper wrapper = (NamespaceWrapper)namespace;
		wrapper.setName( currName );
		
		// create the new element
		String namespaceName = wrapper.getName().toString(); 
		Parent realParent = (Parent)wrapper.getParent();
		Namespace newNameSpace = new Namespace( (ICElement)realParent, namespaceName );
		wrapper.setElement(newNameSpace);
		realParent.addChild( newNameSpace );

		// set the positions
		newNameSpace.setIdPos(wrapper.getName().getStartOffset(), namespaceName.length());
		newNameSpace.setPos(wrapper.getName().getStartOffset(), namespaceName.length());		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationAbort(java.lang.Object)
	 */
	public void namespaceDefinitionAbort(Object namespace) {
		namespace = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#namespaceDeclarationEnd(java.lang.Object)
	 */
	public void namespaceDefinitionEnd(Object namespace) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationBegin(java.lang.Object, java.lang.String)
	 */
	public Object linkageSpecificationBegin(Object container, String literal) {
		// until linkageSpecs are part of the code model (do they need to be?) just return the container object
		return container; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#linkageSpecificationEnd(java.lang.Object)
	 */
	public void linkageSpecificationEnd(Object linkageSpec) {
		// do not implement anything unless linkageSpecificationBegin does more than just return its container
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveBegin(java.lang.Object)
	 */
	public Object usingDirectiveBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveNamespaceId(java.lang.Object)
	 */
	public void usingDirectiveNamespaceId(Object container) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveEnd(java.lang.Object)
	 */
	public void usingDirectiveEnd(Object directive) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationBegin(java.lang.Object)
	 */
	public Object usingDeclarationBegin(Object container) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationMapping(java.lang.Object)
	 */
	public void usingDeclarationMapping(Object container, boolean isTypename) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationEnd(java.lang.Object)
	 */
	public void usingDeclarationEnd(Object directive) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDirectiveAbort(java.lang.Object)
	 */
	public void usingDirectiveAbort(Object directive) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#usingDeclarationAbort(java.lang.Object)
	 */
	public void usingDeclarationAbort(Object declaration) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierBegin(java.lang.Object)
	 */
	public Object enumSpecifierBegin(Object container, Token enumKey) {
		SimpleDeclarationWrapper c = (SimpleDeclarationWrapper)container;
		EnumerationWrapper wrapper = new EnumerationWrapper(c.getParent(), enumKey );
		return wrapper;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierId(java.lang.Object)
	 */
	public void enumSpecifierId(Object enumSpec) {
		((EnumerationWrapper)enumSpec).setName( currName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierAbort(java.lang.Object)
	 */
	public void enumSpecifierAbort(Object enumSpec) {
		enumSpec = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumSpecifierEnd(java.lang.Object)
	 */
	public void enumSpecifierEnd(Object enumSpec) {
		EnumerationWrapper wrapper = (EnumerationWrapper)enumSpec;
		 
		List enumerators = wrapper.getEnumerators();
		
		Parent realParent = (Parent)wrapper.getParent();
		String enumName = ( wrapper.getName() == null ) ? "" : wrapper.getName().toString();
		Enumeration enumeration = new Enumeration( (ICElement)realParent, enumName );
		enumeration.setTypeName( "enum" ); 
		realParent.addChild( enumeration );
		
		// create the list 
		Iterator i = enumerators.iterator(); 
		while( i.hasNext())
		{
			EnumeratorWrapper subwrapper = (EnumeratorWrapper)i.next(); 
			Enumerator enumerator = new Enumerator( enumeration, subwrapper.getName().toString() );
			String enumeratorName = subwrapper.getName().toString();

			enumerator.setIdPos(subwrapper.getName().getStartOffset(), enumeratorName.length());
			enumerator.setPos(subwrapper.getName().getStartOffset(), enumeratorName.length());
			 
			enumeration.addChild( enumerator );
		}
		
		// do the offsets
		if( wrapper.getName() != null )
		{ 	
			enumeration.setIdPos(wrapper.getName().getStartOffset(), enumName.length());
			enumeration.setPos(wrapper.getName().getStartOffset(), enumName.length());
		}
		else
		{
			enumeration.setIdPos(wrapper.getClassKind().getOffset(), wrapper.getClassKind().getLength());
			enumeration.setPos(wrapper.getClassKind().getOffset(), wrapper.getClassKind().getLength());		
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionBegin(java.lang.Object)
	 */
	public Object enumDefinitionBegin(Object enumSpec) {
		EnumerationWrapper wrapper = (EnumerationWrapper)enumSpec; 
		EnumeratorWrapper result = new EnumeratorWrapper(wrapper); 
		return result; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionId(java.lang.Object)
	 */
	public void enumDefinitionId(Object enumDefn) {
		EnumeratorWrapper wrapper = (EnumeratorWrapper)enumDefn; 
		wrapper.setName( currName ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#enumDefinitionEnd(java.lang.Object)
	 */
	public void enumDefinitionEnd(Object enumDefn) {
		EnumeratorWrapper wrapper = (EnumeratorWrapper)enumDefn;
		wrapper.getParent().addEnumerator( wrapper );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#asmDefinition(java.lang.String)
	 */
	public void asmDefinition(Object container, String assemblyCode) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainBegin(java.lang.Object)
	 */
	public Object constructorChainBegin(Object declarator) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainAbort(java.lang.Object)
	 */
	public void constructorChainAbort(Object ctor) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainEnd(java.lang.Object)
	 */
	public void constructorChainEnd(Object ctor) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementBegin(Object ctor) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementEnd(java.lang.Object)
	 */
	public void constructorChainElementEnd(Object element) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainId(java.lang.Object)
	 */
	public void constructorChainElementId(Object ctor) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementExpressionListElementBegin(java.lang.Object)
	 */
	public Object constructorChainElementExpressionListElementBegin(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#constructorChainElementExpressionListElementEnd(java.lang.Object)
	 */
	public void constructorChainElementExpressionListElementEnd(Object expression) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationBegin(java.lang.Object)
	 */
	public Object explicitInstantiationBegin(Object container) {
		// until explicit-instantiations are part of the code model just return the container object
		return container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitInstantiationEnd(java.lang.Object)
	 */
	public void explicitInstantiationEnd(Object instantiation) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationBegin(java.lang.Object)
	 */
	public Object explicitSpecializationBegin(Object container) {
		//	until explicit-specializations are part of the code model just return the container object
		return container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#explicitSpecializationEnd(java.lang.Object)
	 */
	public void explicitSpecializationEnd(Object instantiation) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#declaratorPureVirtual(java.lang.Object)
	 */
	public void declaratorPureVirtual(Object declarator) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationBegin(java.lang.Object, boolean)
	 */
	public Object templateDeclarationBegin(Object container, boolean exported) {
		// until linkageSpecs are part of the code model just return the container object
		return container;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationAbort(java.lang.Object)
	 */
	public void templateDeclarationAbort(Object templateDecl) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateDeclarationEnd(java.lang.Object)
	 */
	public void templateDeclarationEnd(Object templateDecl) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterBegin(java.lang.Object, org.eclipse.cdt.internal.core.parser.Token)
	 */
	public Object templateTypeParameterBegin(Object templDecl, Token kind) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterName(java.lang.Object)
	 */
	public void templateTypeParameterName(Object typeParm) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeInitialTypeId(java.lang.Object)
	 */
	public void templateTypeParameterInitialTypeId(Object typeParm) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterEnd(java.lang.Object)
	 */
	public void templateTypeParameterEnd(Object typeParm) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#templateTypeParameterAbort(java.lang.Object)
	 */
	public void templateTypeParameterAbort(Object typeParm) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.IParserCallback#pointerOperatorAbort(java.lang.Object)
	 */
	public void pointerOperatorAbort(Object ptrOperator) {
		ptrOperator = null; 
	}

}
