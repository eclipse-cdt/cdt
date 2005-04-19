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
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

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
					if( simpleDecl.getDeclSpecifier() instanceof ICPPASTCompositeTypeSpecifier ){
						name = ((ICPPASTCompositeTypeSpecifier)simpleDecl.getDeclSpecifier()).getName();
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
		}
		return  (binding instanceof ICPPTemplateDefinition) ? (ICPPTemplateDefinition) binding : null;
	}
	public static IBinding createBinding( ICPPASTTemplateParameter templateParameter ){
		ICPPTemplateDefinition template = getContainingTemplate( templateParameter );
		
		IBinding binding = null;
		if( template instanceof CPPTemplateDefinition ){
			binding = ((CPPTemplateDefinition)template).resolveTemplateParameter( templateParameter );
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
		if( parent instanceof ICPPASTCompositeTypeSpecifier ){
			return createClassPartialSpecialization( (ICPPASTCompositeTypeSpecifier) parent );
		}
		IASTName templateName = id.getTemplateName();
		IBinding template = templateName.resolveBinding();
		if( template != null && template instanceof ICPPTemplateDefinition ){
			return ((CPPTemplateDefinition)template).instantiate( id );
		}
		return template;
	}

	protected static IBinding createClassPartialSpecialization( ICPPASTCompositeTypeSpecifier compSpec ){
		ICPPASTTemplateId id = (ICPPASTTemplateId) compSpec.getName();
		
		IBinding binding = id.getTemplateName().resolveBinding();
		if( !(binding instanceof ICPPClassTemplate) ) 
			return null;  //TODO: problem?
		
		CPPClassTemplate template = (CPPClassTemplate) binding;
		ICPPTemplateSpecialization [] specializations = template.getSpecializations();
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
	public static IBinding createInstance( IASTName id, ICPPScope scope, IBinding decl, ObjectMap argMap) {
		ICPPTemplateInstance instance = null;
		if( decl instanceof ICPPClassType ){
			instance = new CPPClassInstance( id, scope, decl, argMap );
		} else if( decl instanceof ICPPField ){
			instance = new CPPFieldInstance( scope, decl, argMap );
		} else if( decl instanceof ICPPMethod ) {
			instance = new CPPMethodInstance( scope, decl, argMap );
		} else if( decl instanceof ICPPFunction ) {
			instance = new CPPFunctionInstance( scope, decl, argMap );
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
			}
		} else if( type instanceof ICPPTemplateParameter && argMap.containsKey( type ) ){
			newType = (IType) argMap.get( type );
		}
	
		return newType;
	}

	public static ICPPASTTemplateDeclaration getTemplateDeclaration( IASTName name ){
		if( name == null ) return null;
		
		IASTNode parent = name.getParent();
		while( !(parent instanceof ICPPASTTemplateDeclaration) )
			parent = parent.getParent();
		
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
						if( i <= idx )
							return templates[ i - 1 ];
						break;
					}
				}
			} else {
				return templates[0];
			}
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
		ICPPTemplateParameter [] defParams = definition.getTemplateParameters();
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
			IType type = CPPVisitor.createType( (IASTDeclarator) parent );
			try {
				IType ftype = ((ICPPFunction)definition).getType();
				if( ftype.isSameType( type ) )
					result = true;
			} catch (DOMException e) {
			}
		} else if( parent instanceof IASTDeclSpecifier ){
			if( name instanceof ICPPASTTemplateId ){
				if( definition instanceof ICPPTemplateSpecialization ){
					ICPPTemplateSpecialization spec = (ICPPTemplateSpecialization) definition;
					IASTNode [] args = ((ICPPASTTemplateId)name).getTemplateArguments();
					if( args.length == spec.getArguments().length ){
						int i = 0;
						for (; i < args.length; i++) {
							IType t1 = CPPVisitor.createType( spec.getArguments()[i] );
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
	
	static protected IFunction[] selectTemplateFunctions( ObjectSet templates, Object[] functionArguments, IASTName name ) {//IASTNode[] templateArguments ){
		IFunction [] instances = null;
		if( name.getParent() instanceof ICPPASTTemplateId )
			name = (IASTName) name.getParent();
		if( name instanceof ICPPASTTemplateId ){
			Object [] keys = templates.keyArray();
			for (int i = 0; i < keys.length; i++) {
				CPPTemplateDefinition templateDef = (CPPTemplateDefinition) keys[i];
				ICPPTemplateInstance temp = (ICPPTemplateInstance) templateDef.instantiate( (ICPPASTTemplateId) name );
				if( temp != null )
					instances = (IFunction[]) ArrayUtil.append( IFunction.class, instances, temp );
			}
		}
		return instances;
		//TODO, instead of the above, do proper argument checking, deduction
	}
}
