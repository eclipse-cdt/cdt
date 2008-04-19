/*******************************************************************************
 * Copyright (c) 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Sergey Prigogin
 */
class CompositeCPPUnknownClassType extends CompositeCPPBinding
implements ICPPInternalUnknownClassType, IIndexType {
	public CompositeCPPUnknownClassType(ICompositesFactory cf, ICPPInternalUnknownClassType rbinding) {
		super(cf, rbinding);
	}

	@Override
	public Object clone() {
		fail(); return null;
	}


	public IField findField(String name) throws DOMException {
		IField preResult = ((ICPPClassType) rbinding).findField(name);
		return (IField) cf.getCompositeBinding((IIndexFragmentBinding)preResult);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() {
		return IField.EMPTY_FIELD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public ICPPMethod[] getMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
	 */
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
	 */
	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		ICPPClassType[] result = ((ICPPClassType) rbinding).getNestedClasses();
		for (int i = 0; i < result.length; i++) {
			result[i] = (ICPPClassType) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	public IScope getCompositeScope() throws DOMException {
		return new CompositeCPPClassScope(cf, rbinding);
	}

	public int getKey() throws DOMException {
		return ((ICPPClassType) rbinding).getKey();
	}

	public boolean isSameType(IType type) {
		return ((ICPPClassType) rbinding).isSameType(type);
	}

	public ICPPScope getUnknownScope() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown#resolveUnknown(org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	public IBinding resolveUnknown(ObjectMap argMap) throws DOMException {
		IBinding result = this;
		IType t = null;
		try {
			if(rbinding instanceof PDOMBinding) {
				IIndexFragmentBinding tparentBinding = (IIndexFragmentBinding) ((PDOMBinding) rbinding).getParentBinding();
				IIndexBinding parentBinding= cf.getCompositeBinding(tparentBinding);


				if (parentBinding instanceof ICPPTemplateTypeParameter) {
					t = CPPTemplates.instantiateType((ICPPTemplateTypeParameter) parentBinding, argMap);
				} else if (parentBinding instanceof ICPPInternalUnknownClassType) {
					IBinding binding = ((ICPPInternalUnknownClassType) parentBinding).resolveUnknown(argMap);
					if (binding instanceof IType) {
						t = (IType) binding;
					}
				}
			}

			if (t != null) {
				t = SemanticUtil.getUltimateType(t, false);
				if (t instanceof ICPPClassType) {
					IScope s = ((ICPPClassType) t).getCompositeScope();
					if (s != null && ASTInternal.isFullyCached(s)) {
		            	IBinding[] bindings = s.find(getName());
		            	if (bindings != null && bindings.length > 0) {
		            		result = bindings[0];
		            	}
					}
				} else if (t instanceof ICPPInternalUnknown) {
					result = resolvePartially((ICPPInternalUnknown) t, argMap);
				}
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassType#resolvePartially(org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown, org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	public IBinding resolvePartially(ICPPInternalUnknown parentBinding,	ObjectMap argMap) {
		return ((ICPPInternalUnknownClassType) rbinding).resolvePartially(parentBinding, argMap);
	}

	public void addDeclaration(IASTNode node) {
	}

	public void addDefinition(IASTNode node) {
	}

	public IASTNode[] getDeclarations() {
		return null;
	}

	public IASTNode getDefinition() {
		return null;
	}

	public void removeDeclaration(IASTNode node) {
	}
}
