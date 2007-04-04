/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
/*
 * Created on Mar 28, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;

/**
 * @author aniefer
 */
public class CPPClassSpecializationScope implements ICPPClassScope, IASTInternalScope {
	private ObjectMap instanceMap = ObjectMap.EMPTY_MAP;
	final private ICPPSpecialization specialization;
	
	/**
	 * @param instance
	 */
	public CPPClassSpecializationScope( ICPPSpecialization specialization ) {
		this.specialization = specialization;
	}

	private ICPPClassType getOriginalClass(){
		return (ICPPClassType) specialization.getSpecializedBinding();
	}
	
	private IBinding getInstance(IBinding binding) {
		if( instanceMap.containsKey( binding ) ) {
			return (IBinding) instanceMap.get( binding );
		} else if (!(binding instanceof ICPPClassTemplatePartialSpecialization)) {
			IBinding spec = CPPTemplates.createSpecialization( this, binding, specialization.getArgumentMap() );
			if( instanceMap == ObjectMap.EMPTY_MAP )
				instanceMap = new ObjectMap(2);
			instanceMap.put( binding, spec );
			return spec;
		}
		return null;
	}
	
	public IBinding getBinding( IASTName name, boolean forceResolve ) throws DOMException {
		char [] c = name.toCharArray();
		
	    if( CharArrayUtils.equals( c, specialization.getNameCharArray() ) )
	    	if (!CPPClassScope.isConstructorReference( name ))
	    		return specialization;

		ICPPClassType specialized = (ICPPClassType) specialization.getSpecializedBinding();
		IScope classScope = specialized.getCompositeScope();
		IBinding[] bindings = classScope != null ? classScope.find(name.toString()) : null;
		
		if (bindings == null) return null;
    	
		IBinding[] specs = new IBinding[0];
		for (int i = 0; i < bindings.length; i++) {
			specs = (IBinding[]) ArrayUtil.append(IBinding.class, specs, getInstance(bindings[i]));
		}
		specs = (IBinding[]) ArrayUtil.trim(IBinding.class, specs);
    	return CPPSemantics.resolveAmbiguities( name, specs );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope#getClassType()
	 */
	public ICPPClassType getClassType() {
		return (ICPPClassType) specialization;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope#getImplicitMethods()
	 */
	public ICPPMethod[] getImplicitMethods() {
		//implicit methods shouldn't have implicit specializations
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
	 */
	public IName getScopeName() {
		if (specialization instanceof ICPPInternalBinding)
			return (IASTName) ((ICPPInternalBinding)specialization).getDefinition();
		//TODO: get the scope name for non-internal bindings
		return null;
	}

	protected ICPPConstructor [] getConstructors() throws DOMException {
		ICPPClassType specialized = (ICPPClassType) specialization.getSpecializedBinding();
		ICPPConstructor[] bindings = specialized.getConstructors();
		
		if (bindings == null) return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		
    	ICPPConstructor[] specs = new ICPPConstructor[0];
		for (int i = 0; i < bindings.length; i++) {
			specs = (ICPPConstructor[]) ArrayUtil.append(ICPPConstructor.class, specs, getInstance(bindings[i]));
		}
		return (ICPPConstructor[]) ArrayUtil.trim(ICPPConstructor.class, specs);
	}
	
	protected ICPPMethod[] getConversionOperators() {
		ICPPClassType specialized = (ICPPClassType) specialization.getSpecializedBinding();
		
		if (!(specialized instanceof ICPPInternalClassType)) {
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
 		
		ICPPMethod[] bindings = ((ICPPInternalClassType)specialized).getConversionOperators();
		
		if (bindings == null) return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		
    	ICPPMethod[] specs = new ICPPMethod[0];
		for (int i = 0; i < bindings.length; i++) {
			specs = (ICPPMethod[]) ArrayUtil.append(ICPPMethod.class, specs, getInstance(bindings[i]));
		}
		return (ICPPMethod[]) ArrayUtil.trim(ICPPMethod.class, specs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
	 */
	public IScope getParent() throws DOMException {
		ICPPClassType cls = getOriginalClass();
		ICPPClassScope scope = (ICPPClassScope)cls.getCompositeScope();
		if( scope != null )
			return scope.getParent();
		if( cls instanceof ICPPInternalBinding ){
			IASTNode [] nds = ((ICPPInternalBinding)cls).getDeclarations();
			if( nds != null && nds.length > 0 )
				return CPPVisitor.getContainingScope( nds[0] );
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) throws DOMException {
		return find(name, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name, boolean prefixLookup) throws DOMException {		
		ICPPClassType specialized = (ICPPClassType) specialization.getSpecializedBinding();
		IBinding[] bindings = specialized.getCompositeScope().find(name.toString(), prefixLookup);
		
		if (bindings == null) return null;
		
		IBinding[] specs = new IBinding[0];
		for (int i = 0; i < bindings.length; i++) {
			specs = (IBinding[]) ArrayUtil.append(IBinding.class, specs, getInstance(bindings[i]));
		}
    	return (IBinding[]) ArrayUtil.trim(IBinding.class, specs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope#isFullyCached()
	 */
	public boolean isFullyCached() throws DOMException {
		ICPPScope origScope = (ICPPScope) getOriginalClass().getCompositeScope();
		if (!ASTInternal.isFullyCached(origScope)) {
			CPPSemantics.LookupData data = new CPPSemantics.LookupData();
			try {
				CPPSemantics.lookupInScope( data, origScope, null );
			} catch (DOMException e) {
			}
		}
		return true;
	}
	
	//this scope does not cache its own names
	public void setFullyCached(boolean b) {}
	public void flushCache() {}
	public void addName(IASTName name) {}
	public IASTNode getPhysicalNode() {return null;}
	public void removeBinding(IBinding binding) {}
	public void addBinding(IBinding binding) {}
}
