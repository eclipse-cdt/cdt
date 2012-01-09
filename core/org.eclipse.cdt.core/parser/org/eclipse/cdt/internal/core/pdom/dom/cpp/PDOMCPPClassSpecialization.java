/*******************************************************************************
 * Copyright (c) 2007, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPClassSpecializationScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPClassSpecialization extends PDOMCPPSpecialization implements
		ICPPClassSpecialization, IPDOMMemberOwner, IPDOMCPPClassType {

	private static final int FIRSTBASE = PDOMCPPSpecialization.RECORD_SIZE + 0;
	private static final int MEMBERLIST = PDOMCPPSpecialization.RECORD_SIZE + 4;
	
	/**
	 * The size in bytes of a PDOMCPPClassSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 8;
	
	private volatile ICPPClassScope fScope;
	private ObjectMap specializationMap= null; // Obtained from the synchronized PDOM cache
	
	public PDOMCPPClassSpecialization(PDOMLinkage linkage, PDOMNode parent, ICPPClassType classType,
			PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) classType, specialized);
	}

	public PDOMCPPClassSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_SPECIALIZATION;
	}

	@Override
	public ICPPClassType getSpecializedBinding() {
		return (ICPPClassType) super.getSpecializedBinding();
	}
	
	@Override
	public IBinding specializeMember(IBinding original) {	
		if (specializationMap == null) {
			final Long key= record+PDOMCPPLinkage.CACHE_INSTANCE_SCOPE;
			Object cached= getPDOM().getCachedResult(key);
			if (cached != null) {
				specializationMap= (ObjectMap) cached;
			} else {
				final ObjectMap newMap= new ObjectMap(2);
				try {
					PDOMClassUtil.NestedClassCollector visitor = new PDOMClassUtil.NestedClassCollector();
					PDOMCPPClassScope.acceptViaCache(this, visitor, false);
					final ICPPClassType[] nested= visitor.getNestedClasses();
					for (ICPPClassType classType : nested) {
						if (classType instanceof ICPPSpecialization) {
							newMap.put(((ICPPSpecialization) classType).getSpecializedBinding(), classType);
						}
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
				specializationMap= (ObjectMap) getPDOM().putCachedResult(key, newMap, false);
			}
		}
		synchronized (specializationMap) {
			IBinding result= (IBinding) specializationMap.get(original);
			if (result != null) 
				return result;
		}
		IBinding newSpec= CPPTemplates.createSpecialization(this, original);
		synchronized (specializationMap) {
			IBinding oldSpec= (IBinding) specializationMap.put(original, newSpec);
			if (oldSpec != null) {
				specializationMap.put(original, oldSpec);
				return oldSpec;
			}
		}
		return newSpec;
	}

	@Override
	public ICPPClassScope getCompositeScope() {
		if (fScope == null) {
			try {
				if (hasOwnScope()) {
					fScope= new PDOMCPPClassScope(this);
					return fScope;
				} 
			} catch (CoreException e) {
			}
			fScope= new PDOMCPPClassSpecializationScope(this);
		}
		return fScope;
	}

	protected boolean hasOwnScope() throws CoreException {
		return hasDefinition();
	}

	public PDOMCPPBase getFirstBase() throws CoreException {
		long rec = getDB().getRecPtr(record + FIRSTBASE);
		return rec != 0 ? new PDOMCPPBase(getLinkage(), rec) : null;
	}

	private void setFirstBase(PDOMCPPBase base) throws CoreException {
		long rec = base != null ? base.getRecord() : 0;
		getDB().putRecPtr(record + FIRSTBASE, rec);
	}
	
	public void addBase(PDOMCPPBase base) throws CoreException {
		getPDOM().removeCachedResult(record+PDOMCPPLinkage.CACHE_BASES);
		PDOMCPPBase firstBase = getFirstBase();
		base.setNextBase(firstBase);
		setFirstBase(base);
	}
	
	public void removeBase(PDOMName pdomName) throws CoreException {
		getPDOM().removeCachedResult(record+PDOMCPPLinkage.CACHE_BASES);
		PDOMCPPBase base= getFirstBase();
		PDOMCPPBase predecessor= null;
		long nameRec= pdomName.getRecord();
		while (base != null) {
			PDOMName name = base.getBaseClassSpecifierName();
			if (name != null && name.getRecord() == nameRec) {
				break;
			}
			predecessor= base;
			base= base.getNextBase();
		}
		if (base != null) {
			if (predecessor != null) {
				predecessor.setNextBase(base.getNextBase());
			} else {
				setFirstBase(base.getNextBase());
			}
			base.delete();
		}
	}
	
	// implementation of class type
	@Override
	public ICPPBase[] getBases() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getBases();
		} 
		
		// this is an explicit specialization
		Long key= record + PDOMCPPLinkage.CACHE_BASES;
		ICPPBase[] bases= (ICPPBase[]) getPDOM().getCachedResult(key);
		if (bases != null) 
			return bases;

		try {
			List<PDOMCPPBase> list = new ArrayList<PDOMCPPBase>();
			for (PDOMCPPBase base = getFirstBase(); base != null; base = base.getNextBase())
				list.add(base);
			Collections.reverse(list);
			bases = list.toArray(new ICPPBase[list.size()]);
			getPDOM().putCachedResult(key, bases);
			return bases;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return ICPPBase.EMPTY_BASE_ARRAY;
	}
	
	@Override
	public ICPPConstructor[] getConstructors() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getConstructors();
		}
		try {
			PDOMClassUtil.ConstructorCollector visitor= new PDOMClassUtil.ConstructorCollector();
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			return visitor.getConstructors();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		}
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredMethods();
		}
		try {
			PDOMClassUtil.MethodCollector methods = new PDOMClassUtil.MethodCollector(false);
			PDOMCPPClassScope.acceptViaCache(this, methods, false);
			return methods.getMethods();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
		}
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getDeclaredFields();
		} 
		try {
			PDOMClassUtil.FieldCollector visitor = new PDOMClassUtil.FieldCollector();
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			return visitor.getFields();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPField.EMPTY_CPPFIELD_ARRAY;
		}
	}
	
	@Override
	public ICPPClassType[] getNestedClasses() {
		IScope scope= getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getNestedClasses();
		} 
		try {
			PDOMClassUtil.NestedClassCollector visitor = new PDOMClassUtil.NestedClassCollector();
			PDOMCPPClassScope.acceptViaCache(this, visitor, false);
			return visitor.getNestedClasses();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPClassType.EMPTY_CLASS_ARRAY;
		}
	}

	@Override
	public IBinding[] getFriends() {
		// not yet supported.
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public ICPPMethod[] getMethods() { 
		return ClassTypeHelper.getMethods(this);
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}
	
	@Override
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}
	
	@Override
	public IField findField(String name) {
		return ClassTypeHelper.findField(this, name);
	}

	@Override
	public int getKey() {
		return getSpecializedBinding().getKey();
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;

		if (type instanceof ITypedef)
			return type.isSameType(this);

		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}

		// require a class specialization
		if (!(type instanceof ICPPClassSpecialization))
			return false;

		return CPPClassSpecialization.isSameClassSpecialization(this, (ICPPClassSpecialization) type);
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

	@Override
	public void addChild(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.addMember(member);
	}

	@Override
	public void acceptUncached(IPDOMVisitor visitor) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.accept(visitor);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMCPPClassScope.acceptViaCache(this, visitor, false);
	}
	
	@Override
	public boolean isAnonymous() {
		return false;
	}
}
