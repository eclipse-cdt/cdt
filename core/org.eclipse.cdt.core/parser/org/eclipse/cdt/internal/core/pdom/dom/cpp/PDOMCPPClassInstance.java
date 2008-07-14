/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPClassInstance extends PDOMCPPInstance implements
		ICPPClassType, ICPPClassSpecializationScope, IPDOMMemberOwner, IIndexType, IIndexScope {

	private static final int MEMBERLIST = PDOMCPPInstance.RECORD_SIZE + 0;
	
	/**
	 * The size in bytes of a PDOMCPPClassInstance record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPInstance.RECORD_SIZE + 4;
	
	public PDOMCPPClassInstance(PDOM pdom, PDOMNode parent, ICPPClassType classType, PDOMBinding instantiated)
			throws CoreException {
		super(pdom, parent, (ICPPTemplateInstance) classType, instantiated);
	}
	
	public PDOMCPPClassInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_INSTANCE;
	}
	
	public ICPPClassType getOriginalClassType() {
		return (ICPPClassType) getSpecializedBinding();
	}
	
	public ICPPBase[] getBases() throws DOMException {		
		return CPPTemplates.getBases(this);
	}
	
	private static class ConstructorCollector implements IPDOMVisitor {
		private List<IPDOMNode> fConstructors = new ArrayList<IPDOMNode>();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPConstructor)
				fConstructors.add(node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPConstructor[] getConstructors() {
			return fConstructors.toArray(new ICPPConstructor[fConstructors.size()]);
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
        if (type instanceof ITypedef)
            return ((ITypedef)type).isSameType(this);

		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}
        
        if (type instanceof ICPPTemplateInstance) {
        	ICPPClassType ct1= (ICPPClassType) getSpecializedBinding();
        	ICPPClassType ct2= (ICPPClassType) ((ICPPTemplateInstance)type).getTemplateDefinition();
        	if (!ct1.isSameType(ct2))
        		return false;
        	
        	ObjectMap m1 = getArgumentMap();
        	ObjectMap m2 = ((ICPPTemplateInstance) type).getArgumentMap();
        	if (m1 == null || m2 == null || m1.size() != m2.size())
        		return false;
        	for (int i = 0; i < m1.size(); i++) {
        		IType t1 = (IType) m1.getAt(i);
        		IType t2 = (IType) m2.getAt(i);
        		if (!CPPTemplates.isSameTemplateArgument(t1, t2))
        			return false;
        	}
        	return true;
        }

        return false;
	}
	
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		ICPPClassType specialized = (ICPPClassType) getSpecializedBinding();
		ICPPMethod[] bindings = specialized.getDeclaredMethods();
		SpecializationFinder visitor= new SpecializationFinder(bindings);
		try {
			accept(visitor);
			return ArrayUtil.convert(ICPPMethod.class, visitor.getSpecializations());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}
	
	@Override
	public boolean isGloballyQualified() throws DOMException {
		return ((ICPPBinding) getSpecializedBinding()).isGloballyQualified();
	}

	//ICPPClassType unimplemented
	public IField findField(String name) throws DOMException { fail(); return null; }
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException { fail(); return null; }
	public ICPPField[] getDeclaredFields() throws DOMException { fail(); return null; }
	public IField[] getFields() throws DOMException { fail(); return null; }
	public IBinding[] getFriends() throws DOMException { fail(); return null; }
	public ICPPClassType[] getNestedClasses() throws DOMException { fail(); return null; }
	@Override
	public Object clone() {fail();return null;}

	public ICPPMethod[] getMethods() throws DOMException { 
		return CPPClassType.getMethods(this);
	}

	public ICPPClassType getClassType() {
		return this;
	}
	
	private class SpecializationFinder implements IPDOMVisitor {
		private HashMap<IBinding,ICPPSpecialization> origToSpecialization;
		public SpecializationFinder(IBinding[] specialized) {
			origToSpecialization = new HashMap<IBinding,ICPPSpecialization>(specialized.length);
			for (IBinding element : specialized) {
				origToSpecialization.put(element, null);
			}
		}
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPSpecialization) {
				final ICPPSpecialization specialization = (ICPPSpecialization) node;
				IBinding orig = specialization.getSpecializedBinding();
				if (origToSpecialization.containsKey(orig)) {
					origToSpecialization.put(orig, specialization);
				}
			}
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPSpecialization[] getSpecializations() {
			Iterator<Map.Entry<IBinding,ICPPSpecialization>> it= origToSpecialization.entrySet().iterator();
			while (it.hasNext()) {
				Entry<IBinding, ICPPSpecialization> entry= it.next();
				if (entry.getValue() == null) {
					ICPPSpecialization specialization= CPPTemplates.createSpecialization(
							PDOMCPPClassInstance.this, entry.getKey(), getArgumentMap());
					if (specialization == null) {
						it.remove();
					}
					else {
						entry.setValue(specialization);
					}
				}
			}
			return origToSpecialization.values().toArray(new ICPPSpecialization[origToSpecialization.size()]);
		}
	}
	
	public IBinding[] find(String name) throws DOMException {
		return CPPSemantics.findBindings(this, name, false);
	}
	
	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet)
			throws DOMException {
		try {			
		    if (getDBName().equals(name.toCharArray())) {
		        if (!CPPClassScope.isConstructorReference(name)) {
		        	//9.2 ... The class-name is also inserted into the scope of the class itself
		        	return this;
		        }
		    }

		    IScope scope = ((ICPPClassType) getTemplateDefinition()).getCompositeScope();
			IBinding[] specialized = scope.getBindings(name, resolve, false);			
			SpecializationFinder visitor = new SpecializationFinder(specialized);
			accept(visitor);
			return CPPSemantics.resolveAmbiguities(name, visitor.getSpecializations());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet)
			throws DOMException {
		IBinding[] result = null;
		try {
			if ((!prefixLookup && getDBName().compare(name.toCharArray(), true) == 0)
					|| (prefixLookup && getDBName().comparePrefix(name.toCharArray(), false) == 0)) {
					// 9.2 ... The class-name is also inserted into the scope of
					// the class itself
					result = (IBinding[]) ArrayUtil.append(IBinding.class, result, this);
			}

		    IScope scope = ((ICPPClassType) getTemplateDefinition()).getCompositeScope();
			IBinding[] specialized = scope.getBindings(name, resolve, prefixLookup);
			SpecializationFinder visitor = new SpecializationFinder(specialized);
			accept(visitor);
			result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, visitor.getSpecializations());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}
	
	public IBinding getInstance(IBinding original) {
		SpecializationFinder visitor = new SpecializationFinder(new IBinding[] {original});
		try {
			accept(visitor);
			return visitor.getSpecializations()[0];
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return original;
	}

	
	//ICPPClassScope unimplemented
	public ICPPMethod[] getImplicitMethods() { fail(); return null; }

	public IIndexBinding getScopeBinding() {
		return this;
	}

	@Override
	public void addChild(PDOMNode member) throws CoreException {
		addMember(member);
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.addMember(member);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.accept(visitor);
	}
	
	
	@Override
	public final IIndexScope getScope() {
		try {
			IScope scope= getSpecializedBinding().getScope();
			if(scope instanceof IIndexScope) {
				return (IIndexScope) scope;
			}
		} catch(DOMException de) {
			CCorePlugin.log(de);
		}
		return null;
	}
}
