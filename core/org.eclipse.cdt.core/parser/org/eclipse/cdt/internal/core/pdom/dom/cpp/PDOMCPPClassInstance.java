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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPClassInstance extends PDOMCPPInstance implements
		ICPPClassType, ICPPClassScope, IPDOMMemberOwner, IIndexType, IIndexScope {

	private static final int MEMBERLIST = PDOMCPPInstance.RECORD_SIZE + 0;
	
	/**
	 * The size in bytes of a PDOMCPPClassInstance record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPInstance.RECORD_SIZE + 4;
	
	public PDOMCPPClassInstance(PDOM pdom, PDOMNode parent, ICPPClassType classType, PDOMBinding instantiated)
			throws CoreException {
		super(pdom, parent, (ICPPTemplateInstance) classType, instantiated);
	}
	
	public PDOMCPPClassInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_CLASS_INSTANCE;
	}

	public ICPPBase[] getBases() throws DOMException {
		//TODO Get bases
		return ICPPBase.EMPTY_BASE_ARRAY;
	}
	
	private static class ConstructorCollector implements IPDOMVisitor {
		private List fConstructors = new ArrayList();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPConstructor)
				fConstructors.add(node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPConstructor[] getConstructors() {
			return (ICPPConstructor[])fConstructors.toArray(new ICPPConstructor[fConstructors.size()]);
		}
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		ConstructorCollector visitor= new ConstructorCollector();
		try {
			accept(visitor);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return visitor.getConstructors();
	}
	
	public int getKey() throws DOMException {
		return ((ICPPClassType)getSpecializedBinding()).getKey();
	}
	
	public IScope getCompositeScope() throws DOMException {
		return this;
	}
	
	public boolean isSameType(IType type) {
        if( type instanceof PDOMNode ) {
			PDOMNode node = (PDOMNode) type;
			if (node.getPDOM() == getPDOM() && node.getRecord() == getRecord()) {
				return true;
			}
        }
        if( type instanceof ITypedef )
            return ((ITypedef)type).isSameType( this );
        if( type instanceof ICPPDeferredTemplateInstance && type instanceof ICPPClassType )
        	return type.isSameType( this );  //the CPPDeferredClassInstance has some fuzziness
        
        if( type instanceof ICPPTemplateInstance ){
        	if( getSpecializedBinding() != ((ICPPTemplateInstance)type).getTemplateDefinition() )
        		return false;
        	
        	ObjectMap m1 = getArgumentMap(), m2 = ((ICPPTemplateInstance)type).getArgumentMap();
        	if( m1 == null || m2 == null || m1.size() != m2.size())
        		return false;
        	for( int i = 0; i < m1.size(); i++ ){
        		IType t1 = (IType) m1.getAt( i );
        		IType t2 = (IType) m2.getAt( i );
        		if( t1 == null || ! t1.isSameType( t2 ) )
        			return false;
        	}
        	return true;
        }

        return false;
	}
	
	//ICPPClassType unimplemented
	public IField findField(String name) throws DOMException { fail(); return null; }
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException { fail(); return null; }
	public ICPPField[] getDeclaredFields() throws DOMException { fail(); return null; }
	public ICPPMethod[] getDeclaredMethods() throws DOMException { fail(); return null; }
	public IField[] getFields() throws DOMException { fail(); return null; }
	public IBinding[] getFriends() throws DOMException { fail(); return null; }
	public ICPPMethod[] getMethods() throws DOMException { fail(); return null; }
	public ICPPClassType[] getNestedClasses() throws DOMException { fail(); return null; }
	public Object clone() {fail();return null;}

	public ICPPClassType getClassType() {
		return this;
	}
	
	public IBinding[] find(String name) throws DOMException {
		return find(name, false);
	}

	public IBinding[] find(String name, boolean prefixLookup)
			throws DOMException {
		try {
			BindingCollector visitor = new BindingCollector(getLinkageImpl(), name.toCharArray(), null, prefixLookup, !prefixLookup);
			accept(visitor);
			return visitor.getBindings();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public IBinding getBinding(IASTName name, boolean resolve)
			throws DOMException {
		try {
		    if (getDBName().equals(name.toCharArray())) {
		        if (CPPClassScope.isConstructorReference(name)){
		            return CPPSemantics.resolveAmbiguities(name, getConstructors());
		        }
	            //9.2 ... The class-name is also inserted into the scope of the class itself
	            return this;
		    }
			
		    BindingCollector visitor = new BindingCollector(getLinkageImpl(), name.toCharArray());
			accept(visitor);
			return CPPSemantics.resolveAmbiguities(name, visitor.getBindings());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	//ICPPClassScope unimplemented
	public ICPPMethod[] getImplicitMethods() { fail(); return null; }

	public IIndexBinding getScopeBinding() {
		return this;
	}

	public void addChild(PDOMNode member) throws CoreException {
		addMember(member);
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.addMember(member);
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.accept(visitor);
	}
}
