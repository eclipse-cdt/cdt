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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITemplate;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.ArrayQualifier;
import org.eclipse.cdt.internal.core.dom.ClassKey;
import org.eclipse.cdt.internal.core.dom.ClassSpecifier;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.DOMFactory;
import org.eclipse.cdt.internal.core.dom.DeclSpecifier;
import org.eclipse.cdt.internal.core.dom.Declaration;
import org.eclipse.cdt.internal.core.dom.Declarator;
import org.eclipse.cdt.internal.core.dom.ElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.EnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.EnumeratorDefinition;
import org.eclipse.cdt.internal.core.dom.IOffsetable;
import org.eclipse.cdt.internal.core.dom.ITemplateParameterListOwner;
import org.eclipse.cdt.internal.core.dom.Inclusion;
import org.eclipse.cdt.internal.core.dom.Macro;
import org.eclipse.cdt.internal.core.dom.NamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.ParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.ParameterDeclarationClause;
import org.eclipse.cdt.internal.core.dom.PointerOperator;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.TemplateParameter;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.dom.TypeSpecifier;
import org.eclipse.cdt.internal.core.parser.IParser;
import org.eclipse.cdt.internal.core.parser.Name;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.core.resources.IProject;

public class CModelBuilder {
	
	protected org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit;
	protected Map newElements;
	
	public CModelBuilder(org.eclipse.cdt.internal.core.model.TranslationUnit tu) {
		this.translationUnit = tu ;
		this.newElements = new HashMap();
	}

	public Map parse(boolean requiresLineNumbers) throws Exception {
		// Note - if a CModel client wishes to have a CModel with valid line numbers
		// DOMFactory.createDOMBuilder should be given the parameter 'true'
		DOMBuilder domBuilder = DOMFactory.createDOMBuilder( requiresLineNumbers ); 
		String code = translationUnit.getBuffer().getContents();
		IParser parser = new Parser(code, domBuilder, true);
		parser.mapLineNumbers(requiresLineNumbers);
		if( translationUnit.getCProject() != null )
		{
			IProject currentProject = translationUnit.getCProject().getProject();
			boolean hasCppNature = CoreModel.getDefault().hasCCNature(currentProject);
			parser.setCppNature(hasCppNature);
		}
		try
		{
			parser.parse();
		}
		catch( Exception e )
		{
			System.out.println( "Parse Exception in Outline View" ); 
			e.printStackTrace();
		}
		long startTime = System.currentTimeMillis();
		try
		{ 
			generateModelElements(domBuilder.getTranslationUnit());
		}
		catch( NullPointerException npe )
		{
			System.out.println( "NullPointer exception generating CModel");
			npe.printStackTrace();
		}
		 
		// For the debuglog to take place, you have to call
		// Util.setDebugging(true);
		// Or set debug to true in the core plugin preference 
		Util.debugLog("CModel build: "+ ( System.currentTimeMillis() - startTime ) + "ms");
		return this.newElements;
	}
	
	protected void generateModelElements(TranslationUnit tu){
		Iterator i = tu.iterateOffsetableElements();
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
		// Namespace Definition 
		if(declaration instanceof NamespaceDefinition){
			NamespaceDefinition nsDef = (NamespaceDefinition) declaration;
			IParent namespace = createNamespace(parent, nsDef);
			List nsDeclarations = nsDef.getDeclarations();
			Iterator nsDecls = 	nsDeclarations.iterator();
			while (nsDecls.hasNext()){
				Declaration subNsDeclaration = (Declaration) nsDecls.next();
				generateModelElements((Parent)namespace, subNsDeclaration);			
			}
		}// end Namespace Definition

		// Simple Declaration 
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
				IParent classElement = createClass (parent, simpleDeclaration, classSpecifier, false);
				// create the sub declarations 
				List declarations = classSpecifier.getDeclarations();
				Iterator j = declarations.iterator();
				while (j.hasNext()){
					Declaration subDeclaration = (Declaration)j.next();
					generateModelElements((Parent)classElement, subDeclaration);					
				} // end while j
			}
			/*-----------------------------------------
			 * Create declarators of simple declaration
			 * ----------------------------------------*/
			List declarators  = simpleDeclaration.getDeclarators();
			Iterator d = declarators.iterator();
			while (d.hasNext()){ 		
				Declarator declarator = (Declarator)d.next();
				createElement(parent, simpleDeclaration, declarator);
			} // end while d		
		} // end if SimpleDeclaration
		
		// Template Declaration 
		if(declaration instanceof TemplateDeclaration){
			TemplateDeclaration templateDeclaration = (TemplateDeclaration)declaration;
			SimpleDeclaration simpleDeclaration = (SimpleDeclaration)templateDeclaration.getDeclarations().get(0);
			TypeSpecifier typeSpec = simpleDeclaration.getTypeSpecifier();
			if (typeSpec instanceof ClassSpecifier){
				ClassSpecifier classSpecifier = (ClassSpecifier) typeSpec;
				ITemplate classTemplate = (StructureTemplate)createClass(parent, simpleDeclaration, classSpecifier, true);
				CElement element = (CElement) classTemplate;
				// set the element position		
				element.setPos(templateDeclaration.getStartingOffset(), templateDeclaration.getTotalLength());	
				// set the element lines
				element.setLines(templateDeclaration.getTopLine(), templateDeclaration.getBottomLine());
				// set the template parameters				
				String[] parameterTypes = getTemplateParameters(templateDeclaration);
				classTemplate.setTemplateParameterTypes(parameterTypes);				

				// create the sub declarations 
				List declarations = classSpecifier.getDeclarations();
				Iterator j = declarations.iterator();
				while (j.hasNext()){
					Declaration subDeclaration = (Declaration)j.next();
					generateModelElements((Parent)classTemplate, subDeclaration);					
				} // end while j
			}
			List declarators  = simpleDeclaration.getDeclarators();
			Iterator d = declarators.iterator();
			while (d.hasNext()){ 		
				Declarator declarator = (Declarator)d.next();
				createTemplateElement(parent,templateDeclaration, simpleDeclaration, declarator);
			} // end while d		
			
		}// end Template Declaration

	}
		
	protected void createElement(Parent parent, SimpleDeclaration simpleDeclaration, Declarator declarator){
		// typedef
		if(simpleDeclaration.getDeclSpecifier().isTypedef()){
			createTypeDef(parent, declarator, simpleDeclaration);
		} else {
			ParameterDeclarationClause pdc = declarator.getParms();
			// variable or field
			if (pdc == null){	
				createVariableSpecification(parent, simpleDeclaration, declarator, false); 
			}
			else{
				// pointer to function 
				if(declarator.getDeclarator() != null){
					createPointerToFunction(parent, simpleDeclaration, declarator, pdc, false);				
				}else {
				// function or method 
					createFunctionSpecification(parent, simpleDeclaration, declarator, pdc, false);
				}
			}
		}				
	}

	protected void createTemplateElement(Parent parent, TemplateDeclaration templateDeclaration, SimpleDeclaration simpleDeclaration, Declarator declarator){
		ParameterDeclarationClause pdc = declarator.getParms();
		ITemplate template = null;
		if (pdc == null){	
			template = (ITemplate) createVariableSpecification(parent, simpleDeclaration, declarator, true); 
		}
		else{
			// template of function or method
			template = (ITemplate) createFunctionSpecification(parent, simpleDeclaration, declarator, pdc, true);
		}

		if(template != null){
			CElement element = (CElement)template;
			// set the element position		
			element.setPos(templateDeclaration.getStartingOffset(), templateDeclaration.getTotalLength());	
			// set the element lines
			element.setLines(templateDeclaration.getTopLine(), templateDeclaration.getBottomLine());
			// set the template parameters
			String[] parameterTypes = getTemplateParameters(templateDeclaration);	
			template.setTemplateParameterTypes(parameterTypes);				
		}
	}
	protected Include createInclusion(Parent parent, Inclusion inclusion){
		// create element
		Include element = new Include((CElement)parent, inclusion.getName());
		// add to parent
		parent.addChild((CElement) element);
		// set position
		element.setIdPos(inclusion.getNameOffset(), inclusion.getNameLength());
		element.setPos(inclusion.getStartingOffset(), inclusion.getTotalLength());
		// set the element lines
		element.setLines(inclusion.getTopLine(), inclusion.getBottomLine());
		this.newElements.put(element, element.getElementInfo());
		return element;
	}
	
	protected org.eclipse.cdt.internal.core.model.Macro createMacro(Parent parent, Macro macro){
		// create element
		org.eclipse.cdt.internal.core.model.Macro element = new  org.eclipse.cdt.internal.core.model.Macro(parent, macro.getName());
		// add to parent
		parent.addChild((CElement) element);		
		// set position
		element.setIdPos(macro.getNameOffset(), macro.getNameLength());
		element.setPos(macro.getStartingOffset(), macro.getTotalLength());
		// set the element lines
		element.setLines(macro.getTopLine(), macro.getBottomLine());
		this.newElements.put(element, element.getElementInfo());
		return element;
	}
	
	protected Namespace createNamespace(Parent parent, NamespaceDefinition nsDef){
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
		// set the element lines
		element.setLines(nsDef.getTopLine(), nsDef.getBottomLine());
		
		this.newElements.put(element, element.getElementInfo());		
		return element;
	}

	protected Enumeration createEnumeration(Parent parent, EnumerationSpecifier enumSpecifier){
		// create element
		String enumName = (enumSpecifier.getName() == null ) ? "" : enumSpecifier.getName().toString();
		Enumeration element = new Enumeration ((ICElement)parent, enumName );
		// add to parent
		parent.addChild((ICElement)element);
		List enumItems = enumSpecifier.getEnumeratorDefinitions();
		Iterator i = enumItems.iterator();
		while (i.hasNext()){
			// create sub element
			EnumeratorDefinition enumDef = (EnumeratorDefinition) i.next();
			createEnumerator(element, enumDef);
		}
		// set enumeration position
		if(enumSpecifier.getName() != null ){
			element.setIdPos(enumSpecifier.getName().getStartOffset(), enumSpecifier.getName().length());
		}else {
			element.setIdPos(enumSpecifier.getStartToken().getOffset(), enumSpecifier.getStartToken().getLength());				
		}
		element.setPos(enumSpecifier.getStartingOffset(), enumSpecifier.getTotalLength());
		element.setTypeName(enumSpecifier.getStartToken().getImage());
		// set the element lines
		element.setLines(enumSpecifier.getTopLine(), enumSpecifier.getBottomLine());
		 
		this.newElements.put(element, element.getElementInfo());
		return element;
	}
	
	protected Enumerator createEnumerator(Parent enum, EnumeratorDefinition enumDef){
		Enumerator element = new Enumerator (enum, enumDef.getName().toString());
		// add to parent
		enum.addChild(element);
		// set enumerator position
		element.setIdPos(enumDef.getName().getStartOffset(), enumDef.getName().length());
		element.setPos(enumDef.getStartingOffset(), enumDef.getTotalLength());
		// set the element lines
		element.setLines(enumDef.getTopLine(), enumDef.getBottomLine());

		this.newElements.put(element, element.getElementInfo());
		return element;		
	}
	
	protected Structure createClass(Parent parent, SimpleDeclaration simpleDeclaration, ClassSpecifier classSpecifier, boolean isTemplate){
		// create element
		String className = (classSpecifier.getName() == null ) ? "" : classSpecifier.getName().toString();
		int kind;
		switch( classSpecifier.getClassKey() )
		{
			case ClassKey.t_class:
				if(!isTemplate)
					kind = ICElement.C_CLASS;
				else
					kind = ICElement.C_TEMPLATE_CLASS;
				break;
			case ClassKey.t_struct:
				if(!isTemplate)
					kind = ICElement.C_STRUCT;
				else
					kind = ICElement.C_TEMPLATE_STRUCT;
				break;	
			default:
				if(!isTemplate)
					kind = ICElement.C_UNION;
				else
					kind = ICElement.C_TEMPLATE_UNION;
				break;
		}
		
		Structure element;
		if(!isTemplate){		
			Structure classElement = new Structure( (CElement)parent, kind, className );
			element = classElement;
		} else {
			StructureTemplate classTemplate = new StructureTemplate( (CElement)parent, kind, className );
			element = classTemplate;
		}
		

		// add to parent
		parent.addChild((ICElement) element);
		String type;
		// set element position 
		if( classSpecifier.getName()  != null )
		{
			type = simpleDeclaration.getDeclSpecifier().getTypeName();
			element.setIdPos( classSpecifier.getName().getStartOffset(), classSpecifier.getName().length() );
		}
		else
		{
			type = classSpecifier.getClassKeyToken().getImage();
			element.setIdPos(classSpecifier.getClassKeyToken().getOffset(), classSpecifier.getClassKeyToken().getLength());
			
		}
		element.setTypeName( type );
		if(!isTemplate){
			// set the element position
			element.setPos(classSpecifier.getStartingOffset(), classSpecifier.getTotalLength());
			// set the element lines
			element.setLines(classSpecifier.getTopLine(), classSpecifier.getBottomLine());
		}
		
		this.newElements.put(element, element.getElementInfo());
		return element;
	}
	
	protected TypeDef createTypeDef(Parent parent, Declarator declarator, SimpleDeclaration simpleDeclaration){
		// create the element
		Name domName = ( declarator.getDeclarator() != null ) ? declarator.getDeclarator().getName() : 
			declarator.getName(); 
		String declaratorName = domName.toString();		
			
		TypeDef element = new TypeDef( parent, declaratorName );
		String type = getType(simpleDeclaration, declarator);
		element.setTypeName(type);
		
		// add to parent
		parent.addChild((CElement)element);

		// set positions
		element.setIdPos(domName.getStartOffset(), domName.length());	
		element.setPos(simpleDeclaration.getStartingOffset(), simpleDeclaration.getTotalLength());
		// set the element lines
		element.setLines(simpleDeclaration.getTopLine(), simpleDeclaration.getBottomLine());

		this.newElements.put(element, element.getElementInfo());
		return element;	
	}

	protected VariableDeclaration createVariableSpecification(Parent parent, SimpleDeclaration simpleDeclaration, Declarator declarator, boolean isTemplate){
		Name domName = ( declarator.getDeclarator() != null ) ? 
			declarator.getDeclarator().getName() : 
			declarator.getName(); 

		String variableName = domName.toString();  
		DeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
		
		VariableDeclaration element = null;
		if(parent instanceof IStructure){
			// field
			Field newElement = new Field( parent, variableName);
			newElement.setMutable(declSpecifier.isMutable());			
			newElement.setVisibility(simpleDeclaration.getAccessSpecifier().getAccess());
			element = newElement;			
		}
		else {
			if(isTemplate){
				// variable
				VariableTemplate newElement = new VariableTemplate( parent, variableName );
				element = newElement;									
			}else {
				if(declSpecifier.isExtern()){
					// variableDeclaration
					VariableDeclaration newElement = new VariableDeclaration( parent, variableName );
					element = newElement;
				}
				else {
					// variable
					Variable newElement = new Variable( parent, variableName );
					element = newElement;				
				}
			}
		}
		element.setTypeName ( getType(simpleDeclaration, declarator) );
		element.setConst(declSpecifier.isConst());
		element.setVolatile(declSpecifier.isVolatile());
		element.setStatic(declSpecifier.isStatic());
		// add to parent
		parent.addChild( element ); 	

		// set position
		element.setIdPos( domName.getStartOffset(), domName.length() );
		if(!isTemplate){
			// set element position
			element.setPos(simpleDeclaration.getStartingOffset(), simpleDeclaration.getTotalLength());
			// set the element lines
			element.setLines(simpleDeclaration.getTopLine(), simpleDeclaration.getBottomLine());
		}
			
		this.newElements.put(element, element.getElementInfo());
		return element;
	}

	protected FunctionDeclaration createFunctionSpecification(Parent parent, SimpleDeclaration simpleDeclaration, Declarator declarator, ParameterDeclarationClause pdc, boolean isTemplate){
		Name domName = ( declarator.getDeclarator() != null ) ? 
			declarator.getDeclarator().getName() : 
			declarator.getName(); 

		String declaratorName = domName.toString();
		DeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
		// getParameterTypes
		List parameterList = pdc.getDeclarations();
		String[] parameterTypes = new String[parameterList.size()];
		FunctionDeclaration element = null;
		for( int j = 0; j< parameterList.size(); ++j )
		{
			ParameterDeclaration param = (ParameterDeclaration )parameterList.get(j);
			parameterTypes[j] = new String(getType(param, (Declarator)param.getDeclarators().get(0)));
		}
		
		if( parent instanceof IStructure )
		{
			if (simpleDeclaration.isFunctionDefinition())
			{
				// method
				if(!isTemplate){
					Method newElement = new Method( parent, declaratorName );
					newElement.setVisibility(simpleDeclaration.getAccessSpecifier().getAccess());
					element = newElement;				
				}else {
					MethodTemplate newElement = new MethodTemplate(parent, declaratorName);
					newElement.setVisibility(simpleDeclaration.getAccessSpecifier().getAccess());
					element = newElement;				
				}
			}
			else
			{
				// method declaration
				if(!isTemplate){
					MethodDeclaration newElement = new MethodDeclaration( parent, declaratorName );
					newElement.setVisibility(simpleDeclaration.getAccessSpecifier().getAccess());
					element = newElement;				
				}else {
					MethodTemplate newElement = new MethodTemplate(parent, declaratorName);
					newElement.setVisibility(simpleDeclaration.getAccessSpecifier().getAccess());
					element = newElement;				
				}
				
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
				if(!isTemplate){
					Function newElement = new Function( parent, declaratorName );
					element = newElement;				
				} else {
					FunctionTemplate newElement = new FunctionTemplate( parent, declaratorName );
					element = newElement;
				}
			}
			else
			{
				// functionDeclaration
				if(!isTemplate){
					FunctionDeclaration newElement = new FunctionDeclaration( parent, declaratorName );
					element = newElement;				
				} else {
					FunctionTemplate newElement = new FunctionTemplate( parent, declaratorName );
					element = newElement;
				}
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
		element.setIdPos( domName.getStartOffset(), domName.length() );
		if(!isTemplate){
			// set the element position		
			element.setPos(simpleDeclaration.getStartingOffset(), simpleDeclaration.getTotalLength());	
			// set the element lines
			element.setLines(simpleDeclaration.getTopLine(), simpleDeclaration.getBottomLine());
		}

		this.newElements.put(element, element.getElementInfo());
		return element;
	}

	protected VariableDeclaration createPointerToFunction(Parent parent, SimpleDeclaration simpleDeclaration, Declarator declarator, ParameterDeclarationClause pdc, boolean isTemplate){
		String declaratorName = declarator.getDeclarator().getName().toString();
		DeclSpecifier declSpecifier = simpleDeclaration.getDeclSpecifier();
		// getParameterTypes
		List parameterList = pdc.getDeclarations();
		String[] parameterTypes = new String[parameterList.size()];
		VariableDeclaration element = null;
		for( int j = 0; j< parameterList.size(); ++j )
		{
			ParameterDeclaration param = (ParameterDeclaration )parameterList.get(j);
			parameterTypes[j] = new String(getType(param, (Declarator)param.getDeclarators().get(0)));
		}
		if(( parent instanceof ITranslationUnit ) 
			|| ( parent instanceof INamespace ))
		{		
			element = new VariableDeclaration(parent, declaratorName);
		} else if( parent instanceof IStructure){
			Field newElement = new Field(parent, declaratorName);
			newElement.setVisibility(simpleDeclaration.getAccessSpecifier().getAccess());
			element = newElement;
		}
							
		StringBuffer typeName = new StringBuffer();
		typeName.append(getType(simpleDeclaration, declarator));
		if(parameterTypes.length > 0){
			typeName.append("(");
			int i = 0;
			typeName.append(parameterTypes[i++]);
			while (i < parameterTypes.length){
				typeName.append(", ");
				typeName.append(parameterTypes[i++]);
			}
			typeName.append(")");
		}
		else{
			typeName.append("()");
		}
		element.setTypeName( typeName.toString() );
		element.setVolatile(declSpecifier.isVolatile());
		element.setStatic(declSpecifier.isStatic());
		element.setConst(declarator.isConst());				

		// add to parent
		parent.addChild( element ); 	

		// hook up the offsets
		element.setIdPos( declarator.getDeclarator().getName().getStartOffset(), declarator.getDeclarator().getName().length() );
		element.setPos(simpleDeclaration.getStartingOffset(), simpleDeclaration.getTotalLength());	
		// set the element lines
		element.setLines(simpleDeclaration.getTopLine(), simpleDeclaration.getBottomLine());

		this.newElements.put(element, element.getElementInfo());
		return element;
	}	
	
	private String[] getTemplateParameters(ITemplateParameterListOwner templateDeclaration){
		// add the parameters
		List templateParameters = templateDeclaration.getTemplateParms().getDeclarations();
		Iterator i = templateParameters.iterator();
		String[] parameterTypes = new String[templateParameters.size()];
		
		for( int j = 0; j< templateParameters.size(); ++j ){
			StringBuffer paramType = new StringBuffer();
			Declaration decl = (Declaration)templateParameters.get(j);
			if(decl instanceof TemplateParameter){
				TemplateParameter parameter = (TemplateParameter) decl;
				if(parameter.getName() != null){
					paramType.append(parameter.getName().toString());
				}else {
					int kind = parameter.getKind();
					switch (kind){
						case TemplateParameter.k_class:
							paramType.append("class");
						break;						
						case TemplateParameter.k_typename:
							paramType.append("typename");
						break;						
						case TemplateParameter.k_template:
							paramType.append("template<");
							String[] subParams =getTemplateParameters(parameter);
							int p = 0; 
							if ( subParams.length > 0)
								paramType.append(subParams[p++]);
							while( p < subParams.length){
								paramType.append(", ");
								paramType.append(subParams[p++]);							
							}
							paramType.append(">");
						break;						
						default:
						break;
					} // switch
				}
			} else if(decl instanceof ParameterDeclaration){
				ParameterDeclaration parameter = (ParameterDeclaration) decl;
				paramType.append(getType(parameter, (Declarator)parameter.getDeclarators().get(0)));				
			}
			parameterTypes[j] = new String(paramType.toString());
		} // end for
		return parameterTypes;		
	}
	
	private String getType(Declaration declaration, Declarator declarator){
		StringBuffer type = new StringBuffer();
		// get type from declaration
		type.append(getDeclarationType(declaration));
		// add pointerr or reference from declarator if any
		type.append(getDeclaratorPointerOperation(declarator));
		if(declarator.getDeclarator() != null){
			// pointer to function or array of functions
			type.append("(");
			// add pointerr or reference from declarator if any
			type.append(getDeclaratorPointerOperation(declarator.getDeclarator()));
			type.append(")");
		}
		// arrays
		type.append(getDeclaratorArrayQualifiers(declarator));
		return type.toString();		
	}
	
	private String getDeclarationType(Declaration declaration){
		StringBuffer type = new StringBuffer();
		if(declaration instanceof ParameterDeclaration){
			ParameterDeclaration paramDeclaration = (ParameterDeclaration) declaration;
			if(paramDeclaration.getDeclSpecifier().isConst())
				type.append("const ");
			if(paramDeclaration.getDeclSpecifier().isVolatile())
				type.append("volatile ");
			TypeSpecifier typeSpecifier = paramDeclaration.getTypeSpecifier();
			if(typeSpecifier == null){
				type.append(paramDeclaration.getDeclSpecifier().getTypeName());
			}
			else if(typeSpecifier instanceof ElaboratedTypeSpecifier){
				ElaboratedTypeSpecifier elab = (ElaboratedTypeSpecifier) typeSpecifier;
				type.append(getElaboratedTypeSignature(elab));
			}
		}
		
		if(declaration instanceof SimpleDeclaration){
			SimpleDeclaration simpleDeclaration = (SimpleDeclaration) declaration;
			if(simpleDeclaration.getDeclSpecifier().isConst())
				type.append("const ");
			if(simpleDeclaration.getDeclSpecifier().isVolatile())
				type.append("volatile ");
			TypeSpecifier typeSpecifier = simpleDeclaration.getTypeSpecifier();
			if(typeSpecifier == null){
				type.append(simpleDeclaration.getDeclSpecifier().getTypeName()); 
			} 
			else if(typeSpecifier instanceof ElaboratedTypeSpecifier){
				ElaboratedTypeSpecifier elab = (ElaboratedTypeSpecifier) typeSpecifier;
				type.append(getElaboratedTypeSignature(elab));
			}
		}
		
		return type.toString();	
	}
	
	private String getElaboratedTypeSignature(ElaboratedTypeSpecifier elab){
		StringBuffer type = new StringBuffer();
		int t = elab.getClassKey();
		switch (t){
			case ClassKey.t_class:
				type.append("class");
			break;
			case ClassKey.t_struct:
				type.append("struct");
			break;
			case ClassKey.t_union:
				type.append("union");
			break;
			case ClassKey.t_enum:
				type.append("enum");
			break;
		};
		type.append(" ");
		type.append(elab.getName().toString());
		return type.toString();
	}
	
	private String getDeclaratorPointerOperation(Declarator declarator){		
		StringBuffer pointerString = new StringBuffer();
		List pointerOperators = declarator.getPointerOperators();
		if(pointerOperators != null) {
			Iterator i = pointerOperators.iterator();
			while(i.hasNext()){
				PointerOperator po = (PointerOperator) i.next();
				switch (po.getType()){
					case PointerOperator.t_pointer:
						pointerString.append("*");
					break;
					case PointerOperator.t_reference:
						pointerString.append("&");
					break;									
				}
				
				if(po.isConst())
					pointerString.append(" const");
				if(po.isVolatile())
					pointerString.append(" volatile");
			}
		}
		return pointerString.toString();
	}

	private String getDeclaratorArrayQualifiers(Declarator declarator){		
		StringBuffer arrayString = new StringBuffer();
		List arrayQualifiers = declarator.getArrayQualifiers(); 
		if(arrayQualifiers != null){
			Iterator i = arrayQualifiers.iterator();
			while (i.hasNext()){
				ArrayQualifier q = (ArrayQualifier) i.next();
				arrayString.append("[]");				
			}
		}
		return arrayString.toString();
	}
}
