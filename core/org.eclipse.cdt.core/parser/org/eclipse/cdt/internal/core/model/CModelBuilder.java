package org.eclipse.cdt.internal.core.model;


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

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.ClassSpecifier;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.Declaration;
import org.eclipse.cdt.internal.core.dom.Declarator;
import org.eclipse.cdt.internal.core.dom.ElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.EnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.EnumeratorDefinition;
import org.eclipse.cdt.internal.core.dom.IOffsetable;
import org.eclipse.cdt.internal.core.dom.Inclusion;
import org.eclipse.cdt.internal.core.dom.Macro;
import org.eclipse.cdt.internal.core.dom.NamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.ParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.ParameterDeclarationClause;
import org.eclipse.cdt.internal.core.dom.PointerOperator;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.dom.TypeSpecifier;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser.util.ClassKey;
import org.eclipse.cdt.internal.core.parser.util.DeclSpecifier;

public class CModelBuilder {
	
	org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit;
	public CModelBuilder(org.eclipse.cdt.internal.core.model.TranslationUnit tu) {
		this.translationUnit = tu ;
	}

	public TranslationUnit parse() throws Exception {
		DOMBuilder domBuilder = new DOMBuilder();
		String code = translationUnit.getBuffer().getContents();
		Parser parser = new Parser(code, domBuilder, true);
		if( ! parser.parse() ) throw new ParserException( "Parse failure" ); 
		generateModelElements(domBuilder.getTranslationUnit());
		return domBuilder.getTranslationUnit();
	}
	
	protected void generateModelElements(TranslationUnit tu){
		List offsetables = tu.getOffsetables();
		Iterator i = offsetables.iterator();
		while (i.hasNext()){
			IOffsetable offsetable = (IOffsetable)i.next();
			if(offsetable instanceof Inclusion){
				createInclusion(translationUnit, (Inclusion) offsetable); 		
			}
			else if(offsetable instanceof Macro){
				createMacro(translationUnit, (Macro) offsetable);				
			}else if(offsetable instanceof Declaration){
				generateModelElements (translationUnit, (Declaration) offsetable);
			}
		} 
	}	
	
	protected void generateModelElements (Parent parent, Declaration declaration){
		// Namespace
		if(declaration instanceof NamespaceDefinition){
			NamespaceDefinition nsDef = (NamespaceDefinition) declaration;
			IParent namespace = createNamespace(parent, nsDef);
			List nsDeclarations = nsDef.getDeclarations();
			Iterator nsDecls = 	nsDeclarations.iterator();
			while (nsDecls.hasNext()){
				Declaration subNsDeclaration = (Declaration) nsDecls.next();
				generateModelElements((Parent)namespace, subNsDeclaration);			
			}
		}

		if(declaration instanceof SimpleDeclaration){
			SimpleDeclaration simpleDeclaration = (SimpleDeclaration) declaration;

			/*-------------------------------------------
			 * Checking the type if it is a composite one
			 *-------------------------------------------*/
			TypeSpecifier typeSpec = simpleDeclaration.getTypeSpecifier();
			// Enumeration
			if (typeSpec instanceof EnumerationSpecifier){
				EnumerationSpecifier enumSpecifier = (EnumerationSpecifier) typeSpec;
				IParent enumElement = createEnumeration (parent, enumSpecifier);
			}
			// Structure
			else if (typeSpec instanceof ClassSpecifier){
				ClassSpecifier classSpecifier = (ClassSpecifier) typeSpec;
				IParent classElement = createClass (parent, simpleDeclaration, classSpecifier);
				// create the sub declarations 
				List declarations = classSpecifier.getDeclarations();
				Iterator j = declarations.iterator();
				while (j.hasNext()){
					Declaration subDeclaration = (Declaration)j.next();
					generateModelElements((Parent)classElement, subDeclaration);					
				}
				// TODO: create the declarators too here				
			}

			/*-----------------------------------------
			 * Create declarators of simple declaration
			 * ----------------------------------------*/

			List declarators  = simpleDeclaration.getDeclarators();
			Iterator d = declarators.iterator();
			while (d.hasNext()){ 		
				Declarator declarator = (Declarator)d.next();
				// typedef
				if(simpleDeclaration.getDeclSpecifier().isTypedef()){
					createTypeDef(parent, declarator, simpleDeclaration);
				} else {
					ParameterDeclarationClause pdc = declarator.getParms();
					if (pdc == null){
						createVariableSpecification(parent, simpleDeclaration, declarator); 
					}
					else{
						createFunctionSpecification(parent, simpleDeclaration, declarator, pdc);
					}
				}				
			} // end while
		} // end if SimpleDeclaration		 
	}
	
	
	
	private void createInclusion(Parent parent, Inclusion inclusion){
		// create element
		Include element = new Include((CElement)parent, inclusion.getName());
		// add to parent
		parent.addChild((CElement) element);
		// set position
		element.setIdPos(inclusion.getNameOffset(), inclusion.getNameLength());
		element.setPos(inclusion.getStartingOffset(), inclusion.getTotalLength());
	}
	
	private void createMacro(Parent parent, Macro macro){
		// create element
		org.eclipse.cdt.internal.core.model.Macro element = new  org.eclipse.cdt.internal.core.model.Macro(parent, macro.getName());
		// add to parent
		parent.addChild((CElement) element);		
		// set position
		element.setIdPos(macro.getNameOffset(), macro.getNameLength());
		element.setPos(macro.getStartingOffset(), macro.getTotalLength());
		
	}
	
	private IParent createNamespace(Parent parent, NamespaceDefinition nsDef){
		// create element
		String nsName = (nsDef.getName() == null ) ? "" : nsDef.getName().toString();
		Namespace element = new Namespace ((ICElement)parent, nsName );
		// add to parent
		parent.addChild((ICElement)element);
		// set element position
		if(nsDef.getName() != null){
			element.setIdPos(nsDef.getName().getStartOffset(), nsDef.getName().length());
		}else{
			element.setIdPos(nsDef.getStartToken().getOffset(), nsDef.getStartToken().getLength());
		}
		element.setPos(nsDef.getStartingOffset(), nsDef.getTotalLength());
		element.setTypeName(nsDef.getStartToken().getImage());
		
		return (IParent)element;
	}

	private IParent createEnumeration(Parent parent, EnumerationSpecifier enumSpecifier){
		// create element
		String enumName = (enumSpecifier.getName() == null ) ? "" : enumSpecifier.getName().toString();
		Enumeration enum = new Enumeration ((ICElement)parent, enumName );
		// add to parent
		parent.addChild((ICElement)enum);
		List enumItems = enumSpecifier.getEnumeratorDefinitions();
		Iterator i = enumItems.iterator();
		while (i.hasNext()){
			// create sub element
			EnumeratorDefinition enumDef = (EnumeratorDefinition) i.next();
			Enumerator element = new Enumerator (enum, enumDef.getName().toString());
			// add to parent
			enum.addChild(element);
			// set enumerator position
			element.setIdPos(enumDef.getName().getStartOffset(), enumDef.getName().length());
			element.setPos(enumDef.getStartingOffset(), enumDef.getTotalLength());
		}
		
		// set enumeration position
		if(enumSpecifier.getName() != null ){
			enum.setIdPos(enumSpecifier.getName().getStartOffset(), enumSpecifier.getName().length());
		}else {
			enum.setIdPos(enumSpecifier.getStartToken().getOffset(), enumSpecifier.getStartToken().getLength());				
		}
		enum.setPos(enumSpecifier.getStartingOffset(), enumSpecifier.getTotalLength());
		enum.setTypeName(enumSpecifier.getStartToken().getImage());
		 
		return (IParent)enum;
	}
	
	private IParent createClass(Parent parent, SimpleDeclaration simpleDeclaration, ClassSpecifier classSpecifier){
		// create element
		String className = (classSpecifier.getName() == null ) ? "" : classSpecifier.getName().toString();
		int kind;
		switch( classSpecifier.getClassKey() )
		{
			case ClassKey.t_class:
				kind = ICElement.C_CLASS;
				break;
			case ClassKey.t_struct:
				kind = ICElement.C_STRUCT;
				break;	
			default:
				kind = ICElement.C_UNION;
				break;
		}
		
		Structure classElement = new Structure( (CElement)parent, kind, className );

		// add to parent
		parent.addChild((ICElement) classElement);
		String type;
		// set element position 
		if( classSpecifier.getName()  != null )
		{
			type = simpleDeclaration.getDeclSpecifier().getTypeName();
			classElement.setIdPos( classSpecifier.getName().getStartOffset(), classSpecifier.getName().length() );
		}
		else
		{
			type = classSpecifier.getClassKeyToken().getImage();
			classElement.setIdPos(classSpecifier.getClassKeyToken().getOffset(), classSpecifier.getClassKeyToken().getLength());
			
		}
		classElement.setTypeName( type );
		classElement.setPos(classSpecifier.getStartingOffset(), classSpecifier.getTotalLength());
		
		return classElement;
	}
	
	private void createTypeDef(Parent parent, Declarator declarator, SimpleDeclaration simpleDeclaration){
		// create the element
		String declaratorName = declarator.getName().toString();		
		TypeDef typedef = new TypeDef( parent, declaratorName );
		String type = getType(simpleDeclaration, declarator);
		typedef.setTypeName(type);
		
		// add to parent
		parent.addChild((CElement)typedef);

		// set positions
		typedef.setIdPos(declarator.getName().getStartOffset(), declarator.getName().length());	
		typedef.setPos(simpleDeclaration.getStartingOffset(), simpleDeclaration.getTotalLength());	
	}

	private void createVariableSpecification(Parent parent, SimpleDeclaration simpleDeclaration, Declarator declarator){
		
		String declaratorName = declarator.getName().toString();
		DeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
		
		VariableDeclaration element = null;
		if(parent instanceof IStructure){
			// field
			Field newElement = new Field( parent, declaratorName );
			newElement.setMutable(declSpecifier.isMutable());			
			newElement.setVisibility(simpleDeclaration.getAccessSpecifier().getAccess());
			element = newElement;			
		}
		else {
			if(declSpecifier.isExtern()){
				// variableDeclaration
				VariableDeclaration newElement = new VariableDeclaration( parent, declaratorName );
				element = newElement;
			}
			else {
				// variable
				Variable newElement = new Variable( parent, declaratorName );
				element = newElement;				
			}
		}
		element.setTypeName ( getType(simpleDeclaration, declarator) );
		element.setConst(declSpecifier.isConst());
		element.setVolatile(declSpecifier.isVolatile());
		element.setStatic(declSpecifier.isStatic());
		// add to parent
		parent.addChild( element ); 	

		// set position
			// hook up the offsets
		element.setIdPos( declarator.getName().getStartOffset(), declarator.getName().length() );
		element.setPos(simpleDeclaration.getStartingOffset(), simpleDeclaration.getTotalLength());	

	}

	private void createFunctionSpecification(Parent parent, SimpleDeclaration simpleDeclaration, Declarator declarator, ParameterDeclarationClause pdc){
		String declaratorName = declarator.getName().toString();
		DeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
		// getParameterTypes
		List parameterList = pdc.getDeclarations();
		String[] parameterTypes = new String[parameterList.size()];
		FunctionDeclaration element = null;
		for( int j = 0; j< parameterList.size(); ++j )
		{
			ParameterDeclaration param = (ParameterDeclaration )parameterList.get(j);
			Declarator paramDeclarator = (Declarator)param.getDeclarators().get(0);
			parameterTypes[j] = new String(getType(param, paramDeclarator));
		}
		
		if( parent instanceof IStructure )
		{
			if (simpleDeclaration.isFunctionDefinition())
			{
				// method
				Method newElement = new Method( parent, declaratorName );
				newElement.setVisibility(simpleDeclaration.getAccessSpecifier().getAccess());
				element = newElement;				
			}
			else
			{
				// method declaration
				MethodDeclaration newElement = new MethodDeclaration( parent, declaratorName );
				newElement.setVisibility(simpleDeclaration.getAccessSpecifier().getAccess());
				element = newElement;				
			}
		}
		else if(( parent instanceof ITranslationUnit ) 
				|| ( parent instanceof INamespace ))
		{
			if (simpleDeclaration.isFunctionDefinition())
			{
				// if it belongs to a class, then create a method
				// else create a function
				// this will not be known until we have cross reference information
				
				// function
				Function newElement = new Function( parent, declaratorName );
				element = newElement;				
			}
			else
			{
				// functionDeclaration
				FunctionDeclaration newElement = new FunctionDeclaration( parent, declaratorName );
				element = newElement;				
			}
		}						
		element.setParameterTypes(parameterTypes);
		element.setReturnType( getType(simpleDeclaration, declarator) );
		element.setVolatile(declSpecifier.isVolatile());
		element.setStatic(declSpecifier.isStatic());
		element.setConst(declarator.isConst());				

		// add to parent
		parent.addChild( element ); 	

		// hook up the offsets
		element.setIdPos( declarator.getName().getStartOffset(), declarator.getName().length() );		
		element.setPos(simpleDeclaration.getStartingOffset(), simpleDeclaration.getTotalLength());	
		
	}
	
	private String getType(Declaration declaration, Declarator declarator){
		String type = "";
		// get type from declaration
		type = getDeclarationType(declaration);
		// add pointerr or reference from declarator if any
		type += getDeclaratorPointerOperation(declarator);
		return type;		
	}
	
	private String getDeclarationType(Declaration declaration){
		String type = "";
		if(declaration instanceof ParameterDeclaration){
			ParameterDeclaration paramDeclaration = (ParameterDeclaration) declaration;
			if(paramDeclaration.getDeclSpecifier().isConst())
				type += "const ";
			if(paramDeclaration.getDeclSpecifier().isVolatile())
				type += "volatile ";
			TypeSpecifier typeSpecifier = paramDeclaration.getTypeSpecifier();
			if(typeSpecifier == null){
				type += paramDeclaration.getDeclSpecifier().getTypeName();
			}
			else if(typeSpecifier instanceof ElaboratedTypeSpecifier){
				ElaboratedTypeSpecifier elab = (ElaboratedTypeSpecifier) typeSpecifier;
				type += getElaboratedTypeSignature(elab);
			}
		}
		
		if(declaration instanceof SimpleDeclaration){
			SimpleDeclaration simpleDeclaration = (SimpleDeclaration) declaration;
			if(simpleDeclaration.getDeclSpecifier().isConst())
				type += "const ";
			if(simpleDeclaration.getDeclSpecifier().isVolatile())
				type += "volatile ";
			TypeSpecifier typeSpecifier = simpleDeclaration.getTypeSpecifier();
			if(typeSpecifier == null){
				type += simpleDeclaration.getDeclSpecifier().getTypeName(); 
			} 
			else if(typeSpecifier instanceof ElaboratedTypeSpecifier){
				ElaboratedTypeSpecifier elab = (ElaboratedTypeSpecifier) typeSpecifier;
				type += getElaboratedTypeSignature(elab);
			}
		}
		
		return type;	
	}
	
	private String getElaboratedTypeSignature(ElaboratedTypeSpecifier elab){
		String type = "";
		int t = elab.getClassKey();
		switch (t){
			case ClassKey.t_class:
				type = "class";
			break;
			case ClassKey.t_struct:
				type = "struct";
			break;
			case ClassKey.t_union:
				type = "union";
			break;
			case ClassKey.t_enum:
				type = "enum";
			break;
		};
		type += " ";
		type += elab.getName().toString();
		return type;
	}
	
	private String getDeclaratorPointerOperation(Declarator declarator){		
		String pointerString = "";
		List pointerOperators = declarator.getPointerOperators();
		if(pointerOperators != null) {
			Iterator i = pointerOperators.iterator();
			while(i.hasNext()){
				PointerOperator po = (PointerOperator) i.next();
				switch (po.getType()){
					case PointerOperator.t_pointer:
						pointerString += "*";
					break;
					case PointerOperator.t_reference:
						pointerString += "&";
					break;									
				}
				
				if(po.isConst())
					pointerString += " const";
				if(po.isVolatile())
					pointerString += " volatile";
			}
		}
		return pointerString;
	}
}
