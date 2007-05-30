/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBase;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPClassSpecialization extends PDOMCPPSpecialization implements
		ICPPClassType, ICPPClassScope, IPDOMMemberOwner, IIndexType, IIndexScope {

	private static final int FIRSTBASE = PDOMCPPSpecialization.RECORD_SIZE + 0;
	private static final int MEMBERLIST = PDOMCPPSpecialization.RECORD_SIZE + 4;
	
	/**
	 * The size in bytes of a PDOMCPPClassSpecialization record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 8;
	
	public PDOMCPPClassSpecialization(PDOM pdom, PDOMNode parent, ICPPClassType classType, PDOMBinding specialized)
			throws CoreException {
		super(pdom, parent, (ICPPSpecialization) classType, specialized);
		if (specialized instanceof PDOMCPPClassTemplate) {
			((PDOMCPPClassTemplate)specialized).addMember(this);
		} else if (specialized instanceof PDOMCPPClassTemplateSpecialization) {
			((PDOMCPPClassTemplateSpecialization)specialized).addMember(this);
		}
	}

	public PDOMCPPClassSpecialization(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_CLASS_SPECIALIZATION;
	}

	public PDOMCPPBase getFirstBase() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRSTBASE);
		return rec != 0 ? new PDOMCPPBase(pdom, rec) : null;
	}

	private void setFirstBase(PDOMCPPBase base) throws CoreException {
		int rec = base != null ? base.getRecord() : 0;
		pdom.getDB().putInt(record + FIRSTBASE, rec);
	}
	
	public void addBase(PDOMCPPBase base) throws CoreException {
		PDOMCPPBase firstBase = getFirstBase();
		base.setNextBase(firstBase);
		setFirstBase(base);
	}
	
	public void removeBase(PDOMName pdomName) throws CoreException {
		PDOMCPPBase base= getFirstBase();
		PDOMCPPBase predecessor= null;
		int nameRec= pdomName.getRecord();
		while (base != null) {
			PDOMName name = base.getBaseClassSpecifierNameImpl();
			if (name != null && name.getRecord() == nameRec) {
				break;
			}
			predecessor= base;
			base= base.getNextBase();
		}
		if (base != null) {
			if (predecessor != null) {
				predecessor.setNextBase(base.getNextBase());
			}
			else {
				setFirstBase(base.getNextBase());
			}
			base.delete();
		}
	}
	
	public IField findField(String name) throws DOMException { fail(); return null; }
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException { fail(); return null; }

	public ICPPBase[] getBases() throws DOMException {
		if (!(this instanceof ICPPTemplateDefinition) && 
				getSpecializedBinding() instanceof ICPPTemplateDefinition) {
			//this is an explicit specialization
			try {
				List list = new ArrayList();
				for (PDOMCPPBase base = getFirstBase(); base != null; base = base.getNextBase())
					list.add(base);
				Collections.reverse(list);
				ICPPBase[] bases = (ICPPBase[])list.toArray(new ICPPBase[list.size()]);
				return bases;
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		} else {
			//this is an implicit specialization
			ICPPBase[] pdomBases = ((ICPPClassType) getSpecializedBinding()).getBases();

			if (pdomBases != null) {
				ICPPBase[] result = null;
				
				for (int i = 0; i < pdomBases.length; i++) {
					ICPPBase origBase = pdomBases[i];
					ICPPBase specBase = (ICPPBase) ((ICPPInternalBase)origBase).clone();
					IBinding origClass = origBase.getBaseClass();
					if (origClass instanceof IType) {
						IType specClass = CPPTemplates.instantiateType((IType) origClass, getArgumentMap());
						specClass = CPPSemantics.getUltimateType(specClass, true);
						if (specClass instanceof IBinding) {
							((ICPPInternalBase)specBase).setBaseClass((IBinding) specClass);
						}
						result = (ICPPBase[]) ArrayUtil.append(ICPPBase.class, result, specBase);
					}
				}
				
				return (ICPPBase[]) ArrayUtil.trim(ICPPBase.class, result);
			}
		}
		
		return new ICPPBase[0];
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		try {
			PDOMClassUtil.ConstructorCollector visitor= new PDOMClassUtil.ConstructorCollector();
			accept(visitor);
			return visitor.getConstructors();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		}
	}
	
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		try {
			PDOMClassUtil.MethodCollector methods = new PDOMClassUtil.MethodCollector(false);
			accept(methods);
			return methods.getMethods();
		} catch (CoreException e) {
			return new ICPPMethod[0];
		}
	}
	
	public ICPPField[] getDeclaredFields() throws DOMException {
		try {
			PDOMClassUtil.FieldCollector visitor = new PDOMClassUtil.FieldCollector();
			accept(visitor);
			return visitor.getFields();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ICPPField[0];
		}
	}
	
	//ICPPClassType unimplemented	
	public IField[] getFields() throws DOMException { fail(); return null; }
	public IBinding[] getFriends() throws DOMException { fail(); return null; }
	public ICPPMethod[] getMethods() throws DOMException { fail(); return null; }
	public ICPPClassType[] getNestedClasses() throws DOMException { fail(); return null; }

	public IScope getCompositeScope() throws DOMException {
		return this;
	}

	public int getKey() throws DOMException {
		return ((ICPPClassType)getSpecializedBinding()).getKey();
	}

	public boolean isSameType(IType type) {
		if( type == this )
			return true;

		if( type instanceof ITypedef )
			return type.isSameType( this );

		if( type instanceof PDOMNode ) {
			PDOMNode node = (PDOMNode) type;
			if (node.getPDOM() == getPDOM() && node.getRecord() == getRecord()) {
				return true;
			}
		}

		if( type instanceof ICPPSpecialization ){
			ICPPClassType myCT= (ICPPClassType) getSpecializedBinding();
			ICPPClassType otherCT= (ICPPClassType) ((ICPPSpecialization)type).getSpecializedBinding(); 
			if(!myCT.isSameType(otherCT)) {
				return false;
			}

			ObjectMap m1 = getArgumentMap(), m2 = ((ICPPSpecialization)type).getArgumentMap();
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
	
	public Object clone() {fail();return null;}

	public ICPPClassType getClassType() {
		return this;
	}

	private class SpecializationFinder implements IPDOMVisitor {
		private ObjectMap specMap;
		public SpecializationFinder(IBinding[] specialized) {
			specMap = new ObjectMap(specialized.length);
			for (int i = 0; i < specialized.length; i++) {
				specMap.put(specialized[i], null);
			}
		}
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPSpecialization) {
				IBinding specialized = ((ICPPSpecialization)node).getSpecializedBinding();
				if (specMap.containsKey(specialized)) {
					ICPPSpecialization specialization = (ICPPSpecialization) specMap.get(node);
					if (specialization == null) {
						specMap.remove(specialized);
						specMap.put(specialized, node);
					}
				}
			}
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPSpecialization[] getSpecializations() {
			ICPPSpecialization[] result = new ICPPSpecialization[specMap.size()];
			for (int i = 0; i < specMap.size(); i++) {
				ICPPSpecialization specialization = (ICPPSpecialization) specMap.getAt(i);
				if (specialization != null) {
					result[i] = specialization;
				} else {
					result[i] = CPPTemplates.createSpecialization(
							PDOMCPPClassSpecialization.this, (IBinding) specMap
									.keyAt(i), getArgumentMap());
				}
			}
			return result;
		}
	}
	
	public IBinding[] find(String name) throws DOMException {
		return CPPSemantics.findBindings( this, name, false );
	}
	
	public IBinding getBinding(IASTName name, boolean resolve)
			throws DOMException {
		if (!(this instanceof ICPPTemplateDefinition) && 
				getSpecializedBinding() instanceof ICPPTemplateDefinition) {
			//this is an explicit specialization
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
		} else {
			//this is an implicit specialization
			try {			
			    if (getDBName().equals(name.toCharArray())) {
			        if (!CPPClassScope.isConstructorReference(name)){
			        	//9.2 ... The class-name is also inserted into the scope of the class itself
			        	return this;
			        }
			    }
				
				IBinding[] specialized = ((ICPPClassType) getSpecializedBinding())
						.getCompositeScope().getBindings(name, resolve, false);			
				SpecializationFinder visitor = new SpecializationFinder(specialized);
				accept(visitor);
				return CPPSemantics.resolveAmbiguities(name, visitor.getSpecializations());
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		
		return null;
	}
	
	public ICPPMethod[] getImplicitMethods() {
		try {
			PDOMClassUtil.MethodCollector methods = new PDOMClassUtil.MethodCollector(true, false);
			accept(methods);
			return methods.getMethods();
		} catch (CoreException e) {
			return new ICPPMethod[0];
		}
	}

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
	
	public String toString() {
		String result= super.toString();
		ObjectMap map= getArgumentMap();
		for(int i=0; i<map.size(); i++)
			result+=" <"+map.keyAt(i)+"=>"+getArgumentMap().getAt(i)+">";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) throws DOMException {
		IBinding[] result = null;
		if (!(this instanceof ICPPTemplateDefinition)
				&& getSpecializedBinding() instanceof ICPPTemplateDefinition) {
			// this is an explicit specialization
			try {
				if ((!prefixLookup && getDBName().compare(name.toCharArray(), true) == 0)
						|| (prefixLookup && getDBName().comparePrefix(name.toCharArray(), false) == 0)) {
					// 9.2 ... The class-name is also inserted into the scope of
					// the class itself
					result = (IBinding[]) ArrayUtil.append(IBinding.class, result, this);
				}
				BindingCollector visitor = new BindingCollector(getLinkageImpl(), name.toCharArray(), null, prefixLookup, !prefixLookup);
				accept(visitor);
				result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, visitor.getBindings());
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		} else {
			// this is an implicit specialization
			try {
				if ((!prefixLookup && getDBName().compare(name.toCharArray(), true) == 0)
						|| (prefixLookup && getDBName().comparePrefix(name.toCharArray(), false) == 0)) {
					// 9.2 ... The class-name is also inserted into the
					// scope of the class itself
					result = (IBinding[]) ArrayUtil.append(IBinding.class, result, this);
				}

				IBinding[] specialized = ((ICPPClassType) getSpecializedBinding())
						.getCompositeScope().getBindings(name, resolve, prefixLookup);
				SpecializationFinder visitor = new SpecializationFinder(specialized);
				accept(visitor);
				result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, visitor.getSpecializations());
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}

}
