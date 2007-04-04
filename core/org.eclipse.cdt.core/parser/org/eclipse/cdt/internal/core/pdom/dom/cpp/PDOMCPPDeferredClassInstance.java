/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPDeferredClassInstance extends PDOMCPPInstance implements
		ICPPClassType, IIndexType, ICPPDeferredTemplateInstance, ICPPInternalDeferredClassInstance {

	/**
	 * The size in bytes of a PDOMCPPDeferredClassInstance record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPInstance.RECORD_SIZE + 0;
	
	public PDOMCPPDeferredClassInstance(PDOM pdom, PDOMNode parent, ICPPClassType classType, PDOMBinding instantiated)
			throws CoreException {
		super(pdom, parent, (ICPPTemplateInstance) classType, instantiated);
	}

	public PDOMCPPDeferredClassInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_DEFERRED_CLASS_INSTANCE;
	}

	public ICPPBase[] getBases() throws DOMException {
		return ((ICPPClassType) getSpecializedBinding()).getBases();
	}
	
	public IScope getCompositeScope() throws DOMException {
		return ((ICPPClassType) getSpecializedBinding()).getCompositeScope();
	}
	
	public int getKey() throws DOMException {
		return ((ICPPClassType) getSpecializedBinding()).getKey();
	}
	
	public boolean isSameType(IType type) {
        if( type instanceof PDOMNode ) {
			PDOMNode node = (PDOMNode) type;
			if (node.getPDOM() == getPDOM() && node.getRecord() == getRecord()) {
				return true;
			}
        }
		
		ICPPClassTemplate classTemplate = (ICPPClassTemplate) getTemplateDefinition();
		
		//allow some fuzziness here.
		if( type instanceof ICPPDeferredTemplateInstance && type instanceof ICPPClassType ){
			ICPPClassTemplate typeClass = (ICPPClassTemplate) ((ICPPDeferredTemplateInstance)type).getSpecializedBinding();
			return (typeClass == classTemplate );
		} else if( type instanceof ICPPClassTemplate && classTemplate == type ){
			return true;
		} else if( type instanceof ICPPTemplateInstance && ((ICPPTemplateInstance)type).getTemplateDefinition() == classTemplate ){
			return true;
		}
		return false;
	}
	
	public ICPPConstructor[] getConstructors() throws DOMException {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}
	
	//Unimplemented
	public IField findField(String name) throws DOMException { fail(); return null; }
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException { fail(); return null; }
	public ICPPField[] getDeclaredFields() throws DOMException { fail(); return null; }
	public ICPPMethod[] getDeclaredMethods() throws DOMException { fail(); return null; }
	public IField[] getFields() throws DOMException { fail(); return null; }
	public IBinding[] getFriends() throws DOMException { fail(); return null; }
	public ICPPMethod[] getMethods() throws DOMException { fail(); return null; }
	public ICPPClassType[] getNestedClasses() throws DOMException { fail(); return null; }
	public Object clone() {fail();return null;}
	
	/**
	 * @param argMap
	 * @return
	 */
	public IType instantiate(ObjectMap argMap) {
		IType[] arguments = getArguments();
		
		IType [] newArgs = new IType[ arguments.length ];
		int size = arguments.length;
		for( int i = 0; i < size; i++ ){
			newArgs[i] = CPPTemplates.instantiateType( arguments[i], argMap );
		}
		
		return (IType) ((ICPPInternalTemplateInstantiator)getTemplateDefinition()).instantiate( newArgs );
	}
}
