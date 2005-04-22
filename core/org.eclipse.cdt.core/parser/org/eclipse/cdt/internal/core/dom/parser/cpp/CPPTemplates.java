/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
/*
 * Created on Mar 11, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics.Cost;

/**
 * @author aniefer
 */
public class CPPTemplates {

	public static IASTName getTemplateParameterName( ICPPASTTemplateParameter param ){
		if( param instanceof ICPPASTSimpleTypeTemplateParameter )
			return ((ICPPASTSimpleTypeTemplateParameter)param).getName();
		else if( param instanceof ICPPASTTemplatedTypeTemplateParameter )
			return ((ICPPASTTemplatedTypeTemplateParameter)param).getName();
		else if( param instanceof ICPPASTParameterDeclaration )
			return ((ICPPASTParameterDeclaration)param).getDeclarator().getName();
		return null;
	}
	
	private static ICPPTemplateDefinition getContainingTemplate( ICPPASTTemplateParameter param ){
		IASTNode parent = param.getParent();
		IBinding binding = null;
		if( parent instanceof ICPPASTTemplateDeclaration ){
			ICPPASTTemplateDeclaration [] templates = new ICPPASTTemplateDeclaration [] { (ICPPASTTemplateDeclaration) parent };
			
			while( parent.getParent() instanceof ICPPASTTemplateDeclaration ){
				parent = parent.getParent();
				templates = (ICPPASTTemplateDeclaration[]) ArrayUtil.append( ICPPASTTemplateDeclaration.class, templates, parent );
			}
			templates = (ICPPASTTemplateDeclaration[]) ArrayUtil.trim( ICPPASTTemplateDeclaration.class, templates );
			
			ICPPASTTemplateDeclaration templateDeclaration = templates[0];
			IASTDeclaration decl = templateDeclaration.getDeclaration();
			while( decl instanceof ICPPASTTemplateDeclaration )
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
			
			IASTName name = null;
			if( decl instanceof IASTSimpleDeclaration ){
				IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) decl;
				IASTDeclarator [] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
				if( dtors.length == 0 ){
					IASTDeclSpecifier spec = simpleDecl.getDeclSpecifier();
					if( spec instanceof ICPPASTCompositeTypeSpecifier ){
						name = ((ICPPASTCompositeTypeSpecifier)spec).getName();
					} else if( spec instanceof ICPPASTElaboratedTypeSpecifier ){
						name = ((ICPPASTElaboratedTypeSpecifier)spec).getName();
					}
				} else {
					IASTDeclarator dtor = dtors[0];
					while( dtor.getNestedDeclarator() != null )
						dtor = dtor.getNestedDeclarator();
					name = dtor.getName();
				}
			} else if( decl instanceof IASTFunctionDefinition ){
				IASTDeclarator dtor = ((IASTFunctionDefinition)decl).getDeclarator();
				while( dtor.getNestedDeclarator() != null )
					dtor = dtor.getNestedDeclarator();
				name = dtor.getName();
			}
			if( name == null )
				return null;
				
			if( name instanceof ICPPASTQualifiedName ){
				int idx = templates.length;
				int i = 0;
				IASTName [] ns = ((ICPPASTQualifiedName) name).getNames();
				for (int j = 0; j < ns.length; j++) {
					if( ns[j] instanceof ICPPASTTemplateId ){
						++i;
						if( i == idx ){
							binding = ((ICPPASTTemplateId)ns[j]).getTemplateName().resolveBinding();
							break;
						}
					}
				}
				if( binding == null )
					binding = ns[ ns.length - 1 ].resolveBinding();
			} else {
				binding = name.resolveBinding();
			}
		} else if( parent instanceof ICPPASTTemplatedTypeTemplateParameter ){
			ICPPASTTemplatedTypeTemplateParameter templatedParam = (ICPPASTTemplatedTypeTemplateParameter) parent;
			binding = templatedParam.getName().resolveBinding();
		}
		return  (binding instanceof ICPPTemplateDefinition) ? (ICPPTemplateDefinition) binding : null;
	}
	public static IBinding createBinding( ICPPASTTemplateParameter templateParameter ){
		ICPPTemplateDefinition template = getContainingTemplate( templateParameter );
		
		IBinding binding = null;
		if( template instanceof CPPTemplateDefinition ){
			binding = ((CPPTemplateDefinition)template).resolveTemplateParameter( templateParameter );
		} else if( template instanceof CPPTemplateTemplateParameter ){
			binding = ((CPPTemplateTemplateParameter)template).resolveTemplateParameter( templateParameter );
		}
		        
	    return binding;
	}
	static public ICPPScope getContainingScope( IASTNode node ){
		while( node != null ){
			if( node instanceof ICPPASTTemplateParameter ){
				IASTNode parent = node.getParent();
				if( parent instanceof ICPPASTTemplateDeclaration ){
					return ((ICPPASTTemplateDeclaration)parent).getScope();
				}
			}
			node = node.getParent();
		}
		
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public static IBinding createBinding(ICPPASTTemplateId id) {
		IASTNode parent = id.getParent();
		int segment = -1;
		if( parent instanceof ICPPASTQualifiedName ){
			IASTName [] ns = ((ICPPASTQualifiedName)parent).getNames();
			segment = ( ns[ ns.length - 1 ] == id ) ? 1 : 0;
			parent = parent.getParent();
		}

		if( parent instanceof ICPPASTCompositeTypeSpecifier && segment == 1 ){
			return createClassPartialSpecialization( (ICPPASTCompositeTypeSpecifier) parent );
		} else if( parent instanceof ICPPASTFunctionDeclarator && segment == 1 ){
			return createFunctionSpecialization( id );
		}
		
		//a reference: class or function template?
		IBinding template = null;
		if( parent instanceof ICPPASTNamedTypeSpecifier || segment == 0 ){
			//class template
			IASTName templateName = id.getTemplateName();
			template = templateName.resolveBinding();
			if( template instanceof ICPPTemplateSpecialization ){
				//specializations are selected during the instantiation, start with the primary template
				try {
					template = ((ICPPTemplateSpecialization)template).getPrimaryTemplateDefinition();
				} catch (DOMException e) {
					return e.getProblem();
				}
			}
			
			if( template != null && template instanceof ICPPTemplateDefinition ){
				if( template instanceof CPPTemplateDefinition )
					if( template instanceof ICPPTemplateTemplateParameter ){
						return template;//todo
					} else if( template instanceof CPPTemplateDefinition )
						return ((CPPTemplateDefinition)template).instantiate( id );
			}
		} else {
			//functions are instatiated as part of the resolution process
			template = CPPVisitor.createBinding( id );
			if( template instanceof ICPPTemplateInstance ){
				IASTName templateName = id.getTemplateName();
				templateName.setBinding( ((ICPPTemplateInstance)template).getOriginalBinding() );
			}
		}
		
		return template;
	}

	protected static IBinding createClassPartialSpecialization( ICPPASTCompositeTypeSpecifier compSpec ){
		IASTName name = compSpec.getName();
		if( name instanceof ICPPASTQualifiedName ){
			IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
			name = ns[ ns.length - 1 ];
		}
		ICPPASTTemplateId id = (ICPPASTTemplateId) name;
		
		IBinding binding = id.getTemplateName().resolveBinding();
		if( !(binding instanceof ICPPClassTemplate) ) 
			return null;  //TODO: problem?
		
		CPPClassTemplate template = (CPPClassTemplate) binding;
		ICPPTemplateSpecialization [] specializations = template.getTemplateSpecializations();
		ICPPTemplateSpecialization spec = null;
		for (int i = 0; i < specializations.length; i++) {
			if( isSameTemplate( specializations[i], id ) ){
				spec = specializations[i];
				break;
			}
		}
		
		if( spec != null ){
			((ICPPInternalBinding)spec).addDefinition( id );
			return spec;
		}
		
		spec = new CPPClassTemplateSpecialization( id );
		template.addSpecialization( spec );
		return spec;
	}
	
	protected static IBinding createFunctionSpecialization( IASTName name ){
		CPPSemantics.LookupData data = new CPPSemantics.LookupData( name );
		data.forceQualified = true;
		IScope scope = CPPVisitor.getContainingScope( name );
		if( name.getPropertyInParent() != ICPPASTQualifiedName.SEGMENT_NAME ){
			try {
				scope = scope.getParent();
			} catch (DOMException e) {
			}
		}
		try {
			CPPSemantics.lookup( data, scope );
		} catch (DOMException e) {
			return e.getProblem();
		}

		ICPPFunctionTemplate function = resolveTemplateFunctions( (Object[]) data.foundItems, name );
		if( function == null )
			return new ProblemBinding( name, IProblemBinding.SEMANTIC_NAME_NOT_FOUND, name.toCharArray() );
		if( function instanceof IProblemBinding )
			return function;
		
		if( name instanceof ICPPASTTemplateId ){
			((ICPPASTTemplateId)name).getTemplateName().setBinding( function );
		}
		IASTNode parent = name.getParent();
		while( parent instanceof IASTName )
			parent = parent.getParent();
		
		ICPPTemplateSpecialization spec = new CPPFunctionTemplateSpecialization( (ICPPASTFunctionDeclarator)parent, function );
		((CPPTemplateDefinition)function).addSpecialization( spec );
		return spec;
	}
	
	static protected ICPPFunctionTemplate resolveTemplateFunctions( Object [] items, IASTName name ) {
		if( items == null )
			return null;
		ICPPFunctionTemplate [] templates = null;
		IBinding temp = null;
		for( int i = 0; i < items.length; i++ ){
			Object o = items[i];
	        
	        if( o instanceof IASTName ){
	            temp = ((IASTName) o).resolveBinding();
	            if( temp == null )
	                continue;
	        } else if( o instanceof IBinding ){
	            temp = (IBinding) o;
	        } else
	            continue;
			
			if( temp instanceof ICPPTemplateInstance )
				temp = ((ICPPTemplateInstance)temp).getOriginalBinding();
			if( temp instanceof ICPPFunctionTemplate )
				templates = (ICPPFunctionTemplate[]) ArrayUtil.append( ICPPFunctionTemplate.class, templates, temp );
		}
		
		if( templates == null )
			return null;
		
		IType [] templateArguments = null;
		
		if( name instanceof ICPPASTTemplateId ){
			templateArguments = createTypeArray( ((ICPPASTTemplateId)name).getTemplateArguments() );
		}
		int numArgs = ( templateArguments != null ) ? templateArguments.length : 0;
		
		
		if( name.getParent() instanceof IASTName )
			name = (IASTName) name.getParent();
		ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) name.getParent();
		IType [] functionParameters = createTypeArray( fdtor.getParameters() );
		
		ICPPFunctionTemplate result = null;
		outer: for( int i = 0; i < templates.length && templates[i] != null; i++ ){
			ICPPFunctionTemplate tmpl = templates[i];
			
			ObjectMap map = ObjectMap.EMPTY_MAP;
			try {
				map = deduceTemplateArguments( tmpl, functionParameters );
			} catch (DOMException e) {
			}
			
			if( map == null )
				continue;			 
			ICPPTemplateParameter [] params = null;
			try {
				params = tmpl.getTemplateParameters();
			} catch (DOMException e) {
				continue;
			}
			
			int numParams = params.length;
			IType arg = null;
			for( int j = 0; j < numParams; j++ ){
				ICPPTemplateParameter param = params[j];
				if( j < numArgs ){
					arg = templateArguments[j];
				}
				if( map.containsKey( param ) ) {
					IType t = (IType) map.get( param );
					if( arg == null ) 
						arg = t;
					else if( !t.isSameType( arg ) ){
						continue outer;
					}
				} else if( arg == null || !matchTemplateParameterAndArgument( param, arg )){
					continue outer;
				}
			}
			//made it this far, its a match
			if( result != null ){
				return new CPPFunctionTemplate.CPPFunctionTemplateProblem(name, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, name.toCharArray() );
			} 
			result = tmpl;
		}
		
		return result;
	}
	
	static protected IType[] deduceTemplateFunctionArguments( ICPPTemplateSpecialization specialization, IASTNode [] specArgs ) throws DOMException
	{
		if( !(specialization instanceof ICPPFunction ) )
			return null;
		
		ICPPFunctionTemplate primaryTemplate = (ICPPFunctionTemplate) specialization.getPrimaryTemplateDefinition();
		ICPPTemplateParameter [] templateParameters = primaryTemplate.getTemplateParameters();
		IType [] arguments = createTypeArray( specArgs );
		
		IType [] result = new IType[ templateParameters.length ];
		ObjectMap map = null;
		
		for( int i = 0; i < templateParameters.length; i++ ){
			ICPPTemplateParameter param = templateParameters[i];
			IType arg = null;
			if( i < arguments.length ){
				arg = arguments[i];
			} else {
				if( map == null ){
					IParameter [] functionParameters = ((ICPPFunction)specialization).getParameters();
					IType [] paramTypes = createTypeArray( functionParameters );
					map = deduceTemplateArguments( primaryTemplate, paramTypes );
					if(map == null )
						return null;
				}
				if( map.containsKey( param ) ){
					arg = (IType) map.get( param );
				}
			}
			
			if( arg == null || !matchTemplateParameterAndArgument( param, arg ) )
				return null;
			
			result[i] = arg;
		}
		
		return result;
	}
	/**
	 * @param scope
	 * @return
	 */
	public static ICPPTemplateDefinition getTemplateDefinition(ICPPTemplateScope scope) {
		if( scope != null ) {}
		return null;
	}

	/**
	 * @param decl
	 * @param arguments
	 * @return
	 */
	public static IBinding createInstance( ICPPScope scope, IBinding decl, ObjectMap argMap, IType [] args ) {
		ICPPTemplateInstance instance = null;
		if( decl instanceof ICPPClassType ){
			instance = new CPPClassInstance( scope, decl, argMap, args );
		} else if( decl instanceof ICPPField ){
			instance = new CPPFieldInstance( scope, decl, argMap, args );
		} else if( decl instanceof ICPPMethod ) {
			instance = new CPPMethodInstance( scope, decl, argMap, args );
		} else if( decl instanceof ICPPFunction ) {
			instance = new CPPFunctionInstance( scope, decl, argMap, args );
		}
		return instance;
	}
	

	/**
	 * @param type
	 * @param arguments
	 */
	public static IType instantiateType(IType type, ObjectMap argMap) {
		if( argMap == null )
			return type;
		
		IType newType = type;
		IType temp = null;
		if( type instanceof IFunctionType ){
			IType ret = null;
			IType [] params = null;
			try {
				ret = instantiateType( ((IFunctionType) type).getReturnType(), argMap );
				IType [] ps = ((IFunctionType) type).getParameterTypes();
				params = new IType[ ps.length ];
				for (int i = 0; i < params.length; i++) {
					temp = instantiateType( ps[i], argMap );
					params[i] = temp;
				}
			} catch (DOMException e) {
			}
			newType = new CPPFunctionType( ret, params );
		} else if( type instanceof ITypeContainer ){
			try {
				temp = ((ITypeContainer) type).getType();
			} catch (DOMException e) {
				return type;
			}
			newType = instantiateType( temp, argMap );
			if( newType != temp ){
				temp = (IType) type.clone();
				((ITypeContainer)temp).setType( newType );
				newType = temp;
			} else {
				newType = type;
			}
		} else if( type instanceof ICPPTemplateParameter && argMap.containsKey( type ) ){
			newType = (IType) argMap.get( type );
		} else if( type instanceof CPPDeferredClassInstance ){
			newType = ((CPPDeferredClassInstance)type).instantiate( argMap );
		}
	
		return newType;
	}

	public static ICPPASTTemplateDeclaration getTemplateDeclaration( IASTName name ){
		if( name == null ) return null;
		
		IASTNode parent = name.getParent();
		while( parent != null && !(parent instanceof ICPPASTTemplateDeclaration) &&
			   !(parent instanceof ICPPASTTemplatedTypeTemplateParameter) )
		{
			parent = parent.getParent();
		}
		
		if( parent == null ) return null;
		
		if( parent instanceof ICPPASTTemplateDeclaration ){
			ICPPASTTemplateDeclaration [] templates = new ICPPASTTemplateDeclaration [] { (ICPPASTTemplateDeclaration) parent };
			
			while( parent.getParent() instanceof ICPPASTTemplateDeclaration ){
				parent = parent.getParent();
				templates = (ICPPASTTemplateDeclaration[]) ArrayUtil.append( ICPPASTTemplateDeclaration.class, templates, parent );
			}
			templates = (ICPPASTTemplateDeclaration[]) ArrayUtil.trim( ICPPASTTemplateDeclaration.class, templates );
			
			if( name == null )
				return null;
				
			if( name.getParent() instanceof ICPPASTQualifiedName ){
				int idx = templates.length;
				int i = 0;
				IASTName [] ns = ((ICPPASTQualifiedName) name.getParent()).getNames();
				for (int j = 0; j < ns.length; j++) {
					if( ns[j] instanceof ICPPASTTemplateId ){
						++i;
					}
					if( ns[j] == name ){
						if( i < idx )
							return templates[ i ];
						return null;
					}
				}
			} else {
				return templates[0];
			}
		} else if( parent instanceof ICPPASTTemplatedTypeTemplateParameter ){
			
		}
		return  null;

	}
	
	private static class ClearBindingAction extends CPPASTVisitor {
		public ObjectSet bindings = null;
		public ClearBindingAction( ObjectSet bindings ) {
			shouldVisitNames = true;
			shouldVisitStatements = true;
			this.bindings = bindings;
		}
		public int visit(IASTName name) {
			if( name.getBinding() != null && bindings.containsKey( name.getBinding() ) )
				name.setBinding( null );
			return PROCESS_CONTINUE;
		}
		public int visit(IASTStatement statement) {
			return PROCESS_SKIP;
		}
	}
	/**
	 * @param definition
	 * @param declarator
	 * @return
	 */
	public static boolean isSameTemplate(ICPPTemplateDefinition definition, IASTName name) {
		ICPPTemplateParameter [] defParams = null;
		try {
			defParams = definition.getTemplateParameters();
		} catch (DOMException e1) {
			return false;
		}
		ICPPASTTemplateDeclaration templateDecl = getTemplateDeclaration( name );
		ICPPASTTemplateParameter [] templateParams = templateDecl.getTemplateParameters();
		if( defParams.length != templateParams.length )
			return false;
		
		ObjectSet bindingsToClear = null;
		for (int i = 0; i < templateParams.length; i++) {
			IASTName tn = getTemplateParameterName( templateParams[i] );
			if( tn.getBinding() != null )
				return ( tn.getBinding() == defParams[i] );
			if( bindingsToClear == null )
				bindingsToClear = new ObjectSet( templateParams.length );
			tn.setBinding( defParams[i] );
			bindingsToClear.put( defParams[i] );
		}
		
		boolean result = false;
		IASTNode parent = name.getParent();
		if( parent instanceof ICPPASTFunctionDeclarator ){
			try {
				IASTParameterDeclaration [] params = ((ICPPASTFunctionDeclarator)parent).getParameters();
				IParameter [] ps = ((ICPPFunction)definition).getParameters();
				if( ps.length == params.length ) {
					int i = 0;
					for(; i < ps.length; i++) {
						IType t1 = CPPVisitor.createType( params[i].getDeclarator() );
						IType t2 = ps[0].getType();
						if( ! t1.isSameType( t2 ) ){
							break;
						}
					}
					if( i == ps.length )
						result = true;
				}
			} catch (DOMException e) {
			}
		} else if( parent instanceof IASTDeclSpecifier ){
			if( name instanceof ICPPASTTemplateId ){
				if( definition instanceof ICPPTemplateSpecialization ){
					ICPPTemplateSpecialization spec = (ICPPTemplateSpecialization) definition;
					IASTNode [] args = ((ICPPASTTemplateId)name).getTemplateArguments();
					IType [] specArgs = null;
					try {
						specArgs = spec.getArguments();
					} catch (DOMException e) {
						result = false;
					}
					if( specArgs != null && args.length == specArgs.length ){
						int i = 0;
						for (; i < args.length; i++) {
							IType t1 = specArgs[i];
							IType t2 = CPPVisitor.createType( args[i] );
							if( t1 != null && t2 != null && t1.isSameType( t2 ) )
								continue;
							break;
						}
						result = ( i == args.length );
					}
				}
			} else {
				result = CharArrayUtils.equals( definition.getNameCharArray(), name.toCharArray() );
			}
		}
		
		if( bindingsToClear != null ){
			ClearBindingAction action = new ClearBindingAction( bindingsToClear );
			templateDecl.accept( action );
		}
		
		return result;
	}
	
	static public IType [] createTypeArray( Object [] params ){
		if( params instanceof IType[] )
			return (IType[]) params;
		
		IType [] result = new IType[ params.length ];
		for( int i = 0; i < params.length; i++ ) {
		    if( params[i] instanceof IASTNode ){
				result[i] = CPPVisitor.createType( (IASTNode) params[ i ] );
			}
		}
		return result;
	}
	
	static protected IFunction[] selectTemplateFunctions( ObjectSet templates, Object[] functionArguments, IASTName name ) {//IASTNode[] templateArguments ){
		if( templates == null || templates.size() == 0 )
			return null;
		
		IFunction [] instances = null;
		
		int size = templates.size();
		
		int numTemplateArgs = 0;
		IASTNode [] templateArguments = null;
		if( name instanceof ICPPASTTemplateId )	{
			templateArguments = ((ICPPASTTemplateId)name).getTemplateArguments();
			numTemplateArgs = templateArguments.length;
		}
		
		IType [] fnArgs = createTypeArray( functionArguments );
		
		outer: for( int idx = 0; idx < size; idx++ ){
			CPPFunctionTemplate template = (CPPFunctionTemplate) templates.keyAt( idx );
			
			ObjectMap map = null;
			try {
				map = deduceTemplateArguments( template, fnArgs );
			} catch (DOMException e) {
				continue;
			}
			if( map == null )
				continue;
			
			ICPPTemplateParameter [] templateParams = template.getTemplateParameters();
			int numTemplateParams = templateParams.length;
			
			IType [] instanceArgs = null;
			for( int i = 0; i < numTemplateParams; i++ ){
				IType arg = ( i < numTemplateArgs ) ? CPPVisitor.createType( templateArguments[i] ) : null;
				IType mapped = (IType) map.get( templateParams[i] );
				
				if( arg != null && mapped != null )
					if( arg.isSameType( mapped ) )
						instanceArgs = (IType[]) ArrayUtil.append( IType.class, instanceArgs, arg );
					else
						continue outer;
				else if( arg == null && mapped == null ) {
				    IType def = null;
				    try {
					    if( templateParams[i] instanceof ICPPTemplateTypeParameter ){
	                        def = ((ICPPTemplateTypeParameter)templateParams[i]).getDefault();
					    } else if( templateParams[i] instanceof ICPPTemplateTemplateParameter ){
					        def = ((ICPPTemplateTemplateParameter)templateParams[i]).getDefault();
					    } else if( templateParams[i] instanceof ICPPTemplateNonTypeParameter ){
					        def = CPPVisitor.getExpressionType( ((ICPPTemplateNonTypeParameter)templateParams[i]).getDefault() );
					    }
				    } catch ( DOMException e ) {
				        continue outer;
				    }
				    if( def != null ){
				        if( def instanceof ICPPTemplateParameter ){
				            for ( int j = 0; j < i; j++ ) {
                                if( templateParams[j] == def ) {
                                    def = instanceArgs[j];
                                }
                            }
				        }
				        instanceArgs = (IType[]) ArrayUtil.append( IType.class, instanceArgs, def );
				    } else 
				        continue outer;
				} else
					instanceArgs = (IType[]) ArrayUtil.append( IType.class, instanceArgs, (arg != null) ? arg : mapped );
			}
			instanceArgs  = (IType[]) ArrayUtil.trim( IType.class, instanceArgs );
			ICPPTemplateInstance temp = (ICPPTemplateInstance) template.instantiate( instanceArgs );
			if( temp != null )
				instances = (IFunction[]) ArrayUtil.append( IFunction.class, instances, temp );
		}
		
		return (IFunction[]) ArrayUtil.trim( IFunction.class, instances );
	}
	
	/**
	 * 
	 * @param template
	 * @param args
	 * @return
	 * 
	 * A type that is specified in terms of template parameters (P) is compared with an actual 
	 * type (A), and an attempt is made to find template argument vaules that will make P, 
	 * after substitution of the deduced values, compatible with A.
	 * @throws DOMException
	 */
	static private ObjectMap deduceTemplateArguments( ICPPFunctionTemplate template, IType[] arguments ) throws DOMException{
		ICPPFunction function = (ICPPFunction) template;
		IParameter [] functionParameters = null;
		try {
			functionParameters = function.getParameters();
		} catch (DOMException e) {
			return null;
		}
		if( arguments == null /*|| functionParameters.length != arguments.length*/ ){
			return null;
		}
		
		int numParams = functionParameters.length;
		int numArgs = arguments.length;
		ObjectMap map = new ObjectMap(numParams);
		for( int i = 0; i < numArgs && i < numParams; i++ ){
			if( !deduceTemplateArgument( map, functionParameters[i].getType(), arguments[i] ) ){
				return null;
			}
		}
		
		return map;			
	}
	
	/**
	 * 14.8.2.1-2 If P is a cv-qualified type, the top level cv-qualifiers of P's type are ignored for type
	 * deduction.  If P is a reference type, the type referred to by P is used for Type deduction.
	 * @param pSymbol
	 * @return
	 */	
	static private IType getParameterTypeForDeduction( IType pType ){
		IType result = pType;
		try {
			if( pType instanceof IQualifierType ){
				result = ((IQualifierType)pType).getType();
			} else if( pType instanceof ICPPReferenceType ){
				result = ((ICPPReferenceType)pType).getType();
			} else if( pType instanceof  CPPPointerType ){
				result = ((CPPPointerType)pType).stripQualifiers();
			}
		} catch ( DOMException e ){
			result = e.getProblem();
		}
		return result;
	}
	
	/**
	 * 14.8.2.1-2
	 * if P is not a reference type
	 * - If A is an array type, the pointer type produced by the array-to-pointer conversion is used instead
	 * - If A is a function type, the pointer type produced by the function-to-pointer conversion is used instead
	 * - If A is a cv-qualified type, the top level cv-qualifiers are ignored for type deduction 
	 * @param aInfo
	 * @return
	 */
	static private IType getArgumentTypeForDeduction( IType aType, boolean pIsAReferenceType ) {
		IType result = aType;
		if( !pIsAReferenceType ){
			try {
				if( aType instanceof IArrayType ){
					result = new CPPPointerType( ((IArrayType)aType).getType() );
				} else if( aType instanceof IFunctionType ){
					result = new CPPPointerType( aType );
				} else if( aType instanceof IQualifierType ){
					result = ((IQualifierType)aType).getType();
				} else if( aType instanceof CPPPointerType ){
					result = ((CPPPointerType)aType).stripQualifiers();
				}
			} catch( DOMException e ){
				result = e.getProblem();
			}
		}
			
		return result;
	}

	static private boolean expressionsEquivalent( IASTExpression p, IASTExpression a ){
		if( p == null )
			return true;
		
		if( p instanceof IASTLiteralExpression && a instanceof IASTLiteralExpression ){
			return p.toString().equals( a.toString () );
		}
		return false;
	}
	static protected boolean deduceTemplateArgument( ObjectMap map, IType p, IType a ) throws DOMException {
		boolean pIsAReferenceType = ( p instanceof ICPPReferenceType );
		p = getParameterTypeForDeduction( p );
		a = getArgumentTypeForDeduction( a, pIsAReferenceType );
		
		if( p instanceof IBasicType ) {
			if( p.isSameType( a ) && a instanceof IBasicType ) {
				return expressionsEquivalent( ((IBasicType)p).getValue(), ((IBasicType)a).getValue() );
			}
		} else {
			while( p != null ){
				if( p instanceof IBasicType ){
					return p.isSameType( a );
				} else if( p instanceof ICPPPointerToMemberType ){
					if( !(a instanceof ICPPPointerToMemberType ) )
						return false;
					
					if( !deduceTemplateArgument( map, ((ICPPPointerToMemberType)p).getMemberOfClass(), ((ICPPPointerToMemberType)a).getMemberOfClass() ) )
						return false;
					
					p = ((ICPPPointerToMemberType)p).getType();
					p = ((ICPPPointerToMemberType)a).getType();
				} else if( p instanceof IPointerType ){
					if( !(a instanceof IPointerType) ){
						return false;
					}
					p = ((IPointerType) p).getType();
					a = ((IPointerType) a).getType();
				} else if ( p instanceof IQualifierType ){
					if( !(a instanceof IQualifierType) )
						return false;
					a = ((IQualifierType)a).getType(); //TODO a = strip qualifiers from p out of a
					p = ((IQualifierType)p).getType();
				} else if( p instanceof IFunctionType ){
					if( !(a instanceof IFunctionType ) )
						return false;
					if( !deduceTemplateArgument( map, ((IFunctionType)p).getReturnType(), ((IFunctionType)a).getReturnType() ) )
						return false;
					IType [] pParams = ((IFunctionType)p).getParameterTypes();
					IType [] aParams = ((IFunctionType)a).getParameterTypes();
					if( pParams.length != aParams.length )
						return false;
					for (int i = 0; i < pParams.length; i++) {
						if( !deduceTemplateArgument( map, pParams[i], aParams[i] ) ) 
							return false;
					}
					return true;
				} else if( p instanceof ICPPTemplateParameter ){
					if( map.containsKey( p ) ){
						IType current = (IType)map.get( p );
						return current.isSameType( a );
					} 
					if( a == null )
						return false;
					map.put( p, a );
					return true;
				} else if( p instanceof ICPPTemplateInstance ){
					if( !(a instanceof ICPPTemplateInstance ) )
						return false;
					ICPPTemplateInstance pInst = (ICPPTemplateInstance) p;
					ICPPTemplateInstance aInst = (ICPPTemplateInstance) a;

					IType [] pArgs = createTypeArray( pInst.getArguments() );
					IType [] aArgs = createTypeArray( aInst.getArguments() );
					if( pArgs.length != aArgs.length )
						return false;
					for (int i = 0; i < pArgs.length; i++) {
						if( !deduceTemplateArgument( map, pArgs[i], aArgs[i] ) ) 
							return false;
					}
					return true;
				} else {
					return p.isSameType( a );
				}
			}
		}

		return false;
	}
	
	/**
	 * transform a function template for use in partial ordering, as described in the
	 * spec 14.5.5.2-3 
	 * @param template
	 * @return
	 * -for each type template parameter, synthesize a unique type and substitute that for each
	 * occurence of that parameter in the function parameter list
	 * -for each non-type template parameter, synthesize a unique value of the appropriate type and
	 * susbstitute that for each occurence of that parameter in the function parameter list
	 * for each template template parameter, synthesize a unique class template and substitute that
	 * for each occurence of that parameter in the function parameter list
	 * @throws DOMException
	 */

	static private IType [] createArgsForFunctionTemplateOrdering( ICPPFunctionTemplate template ) throws DOMException{
		ICPPTemplateParameter [] paramList = template.getTemplateParameters();
		int size = paramList.length;
		IType [] args = new IType [ size ];
		for( int i = 0; i < size; i++ ){
			ICPPTemplateParameter param = paramList[i];
			if( param instanceof ICPPTemplateNonTypeParameter ){
				IType t = ((ICPPTemplateNonTypeParameter)param).getType();
				if( t instanceof CPPBasicType ){
					CPPASTLiteralExpression exp = new CPPASTLiteralExpression();
					exp.setValue( String.valueOf( i ) );
					CPPBasicType temp = (CPPBasicType) t.clone();
					temp.setValue( exp );
					args[i] = temp;
				}
			} else {
				args[i] = new CPPBasicType( -1, 0 );
			}
		}
		return args;
	}
	
	static protected int orderTemplateFunctions( ICPPFunctionTemplate f1, ICPPFunctionTemplate f2 ) throws DOMException {
		//Using the transformed parameter list, perform argument deduction against the other
		//function template
		IType [] args = createArgsForFunctionTemplateOrdering( f1 );
		ICPPFunction function = (ICPPFunction) ((CPPTemplateDefinition)f1).instantiate( args );
		
		ObjectMap m1 = deduceTemplateArguments( f2, function.getType().getParameterTypes() );
		
		args = createArgsForFunctionTemplateOrdering( f2 );
		function = (ICPPFunction) ((CPPTemplateDefinition)f2).instantiate( args );
		
		ObjectMap m2 = deduceTemplateArguments( f1, function.getType().getParameterTypes() );
		
		//The transformed  template is at least as specialized as the other iff the deduction
		//succeeds and the deduced parameter types are an exact match
		//A template is more specialized than another iff it is at least as specialized as the
		//other template and that template is not at least as specialized as the first.
		boolean d1 = ( m1 != null );
		boolean d2 = ( m2 != null );
		
		if( d1 && d2 || !d1 && !d2 )
			return 0;
		else if( d1 && !d2 )
			return 1;
		else 
			return -1;
	}
	
	static protected ICPPTemplateDefinition matchTemplatePartialSpecialization( ICPPTemplateDefinition template, IType[] args ) throws DOMException{
		if( template == null ){
			return null;
		}
		
		ICPPTemplateSpecialization[] specializations = template.getTemplateSpecializations();
		int size = ( specializations != null ) ? specializations.length : 0;
		if( size == 0 ){
			return template;
		}
		
		ICPPTemplateSpecialization bestMatch = null, spec = null;
		boolean bestMatchIsBest = true;
		IType[] specArgs = null;
		for( int i = 0; i < size; i++ ){
			spec = specializations[i];
			specArgs = spec.getArguments();
			if( specArgs == null || specArgs.length != args.length ){
				continue;
			}
			
			int specArgsSize = specArgs.length;			
			ObjectMap map = new ObjectMap(specArgsSize);
			IType t1 = null, t2 = null;

			boolean match = true;
			for( int j = 0; j < specArgsSize; j++ ){
				t1 = specArgs[j];
				t2 = args[j];
				
				if( !deduceTemplateArgument( map, t1, t2 ) ){
					match = false;
					break;
				}
			}
			if( match ){
				int compare = orderSpecializations( bestMatch, spec );
				if( compare == 0 ){
					bestMatchIsBest = false; 
				} else if( compare < 0 ) {
					bestMatch = spec;
					bestMatchIsBest = true;
				}
			}
		}
		
		//14.5.4.1 If none of the specializations is more specialized than all the other matchnig
		//specializations, then the use of the class template is ambiguous and the program is ill-formed.
		if( !bestMatchIsBest ){
			//TODO problem
			return new CPPTemplateDefinition.CPPTemplateProblem( null, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, null);
		}
		
		return bestMatch;
	}
	
	/**
	 * Compare spec1 to spec2.  Return > 0 if spec1 is more specialized, < 0 if spec2
	 * is more specialized, = 0 otherwise.
	 * @param spec1
	 * @param spec2
	 * @return
	 * @throws DOMException
	 */
	static private int orderSpecializations( ICPPTemplateSpecialization spec1, ICPPTemplateSpecialization spec2 ) throws DOMException {
		if( spec1 == null ){
			return -1;	
		}
		
		//to order class template specializations, we need to transform them into function templates
		ICPPFunctionTemplate template1 = null, template2 = null;
		
		if( spec1 instanceof ICPPClassType ) {
			template1 = classTemplateSpecializationToFunctionTemplate( spec1 );
			template2 = classTemplateSpecializationToFunctionTemplate( spec2 );	
		} else if( spec1 instanceof ICPPFunction ) {
			template1 = (ICPPFunctionTemplate) spec1;
			template2 = (ICPPFunctionTemplate) spec2;
		}
		
		return orderTemplateFunctions( template1, template2);
	}
	
	public static final class CPPImplicitFunctionTemplate extends CPPFunctionTemplate {
		IParameter [] functionParameters = null;
		ICPPTemplateParameter [] templateParameters = null;
		/**
		 * @param name
		 */
		public CPPImplicitFunctionTemplate( ICPPTemplateParameter [] templateParameters, IParameter [] functionParameters) {
			super( null );
			this.functionParameters = functionParameters;
			this.templateParameters = templateParameters;
		}
		public IParameter[] getParameters() {
			return functionParameters; 
		}
		public ICPPTemplateParameter[] getTemplateParameters() {
			return templateParameters;
		}
		public IScope getScope() {
			return null;
		}
		public IFunctionType getType() {
			if( type == null ){
				type = CPPVisitor.createImplicitFunctionType( new CPPBasicType(IBasicType.t_void, 0 ), functionParameters );
			}
			return type;
		}
	}
	/**
	 * transform the class template to a function template as described in the spec
	 * 14.5.4.2-1
	 * @param template
	 * @return IParameterizedSymbol
	 * the function template has the same template parameters as the partial specialization and
	 * has a single function parameter whose type is a class template specialization with the template 
	 * arguments of the partial specialization
	 */
	static private ICPPFunctionTemplate classTemplateSpecializationToFunctionTemplate( ICPPTemplateSpecialization specialization ) {
		if( !(specialization instanceof ICPPClassType) )
			return null;

		ICPPTemplateDefinition template = specialization;
		IType [] args = null;
		try {
			args = specialization.getArguments();
		} catch (DOMException e1) {
			return null;
		}
		
		IType paramType = (IType) ((CPPTemplateDefinition)template).instantiate( args );
		IParameter [] functionParameters = new IParameter[] { new CPPParameter( paramType ) };
		
		try {
			return new CPPImplicitFunctionTemplate( specialization.getTemplateParameters(), functionParameters );
		} catch (DOMException e) {
			return null;
		}
	}
	
	static private boolean isValidArgument(ICPPTemplateParameter param, IType argument) {
		//TODO
		return true;	
	}
	static protected boolean matchTemplateParameterAndArgument( ICPPTemplateParameter param, IType argument ){
		if( !isValidArgument(param, argument) ){
			return false;
		}
		if( param instanceof ICPPTemplateTypeParameter )
			return true;
		else if( param instanceof ICPPTemplateTemplateParameter ){
			if( !( argument instanceof ICPPTemplateDefinition ) )
				return false;
			
			ICPPTemplateParameter [] pParams = null, aParams = null;
			try {
				pParams = ((ICPPTemplateTemplateParameter)param).getTemplateParameters();
				aParams = ((ICPPTemplateDefinition)argument).getTemplateParameters();
			} catch ( DOMException e ) {
				return false;
			}
			

			int size = pParams.length;
			if( aParams.length != size){
				return false;
			}
			
			for( int i = 0; i < size; i++){
				if( (pParams[i] instanceof ICPPTemplateTypeParameter && !(aParams[i] instanceof ICPPTemplateTypeParameter)) ||
					(pParams[i] instanceof ICPPTemplateTemplateParameter && !(aParams[i] instanceof ICPPTemplateTemplateParameter)) ||
					(pParams[i] instanceof ICPPTemplateNonTypeParameter && !(aParams[i] instanceof ICPPTemplateNonTypeParameter)) )
				{
					return false;
				}
			}
			
			return true;
		} else {
			try {
				IType pType = ((ICPPTemplateNonTypeParameter)param).getType();
				Cost cost = CPPSemantics.checkStandardConversionSequence( argument, pType );
					
				if( cost == null || cost.rank == Cost.NO_MATCH_RANK ){
					return false;
				}
			} catch( DOMException e ){
				return false;
			}
		}
		return true;
	}

	public static IBinding instantiateWithinClassTemplate( ICPPClassTemplate template ) throws DOMException {
		IType [] args = null;
		if( template instanceof ICPPTemplateSpecialization ){
			args = ((ICPPTemplateSpecialization)template).getArguments();
		} else {
			ICPPTemplateParameter [] templateParameters = template.getTemplateParameters();
			args = new IType [ templateParameters.length ];
			for (int i = 0; i < templateParameters.length; i++) {
				if( templateParameters[i] instanceof IType ){
					args[i] = (IType) templateParameters[i];
				} else if( templateParameters[i] instanceof ICPPTemplateNonTypeParameter ){
					args[i] = ((ICPPTemplateNonTypeParameter)templateParameters[i]).getType();
				}
			}
		}
		
		if( template instanceof CPPClassTemplate ){
			return ((CPPClassTemplate)template).instantiate( args );
		}
		return template;
	}
}
