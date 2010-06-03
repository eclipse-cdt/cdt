/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;

/**
 * Factory for creating CElement handles. These are a minimal implementation
 * of the ICElement interface and can be used for displaying information about
 * the index.
 * @since 4.0
 */
public class CElementHandleFactory {
	private CElementHandleFactory() {}

	public static ICElementHandle create(ITranslationUnit tu, IBinding binding, boolean isDefinition,
			IRegion region, long timestamp) throws CoreException {
		try {
			return internalCreate(tu, binding, isDefinition, region, timestamp);
		}
		catch (DOMException e) {
			return null;
		}
	}

	public static ICElementHandle create(ITranslationUnit tu, IIndexMacro macro, 
			IRegion region, long timestamp) throws CoreException {
		CElementHandle element= new MacroHandle(tu, macro);
		if (region != null) {
			element.setRangeOfID(region, timestamp);
		}
		return element;
	}

	
	public static ICElementHandle internalCreate(ITranslationUnit tu, IBinding binding, boolean definition,
			IRegion region, long timestamp) throws CoreException, DOMException {	

		ICElement parentElement= createParent(tu, binding);
		if (parentElement == null) {
			return null;
		}
		
		CElementHandle element= null;
		if (binding instanceof ICPPMethod) {
			element= definition 
					? new MethodHandle(parentElement, (ICPPMethod) binding)
					: new MethodDeclarationHandle(parentElement, (ICPPMethod) binding);
		} else if (binding instanceof IFunction) {
			if (binding instanceof ICPPTemplateInstance) {
				element= definition 
				? new FunctionTemplateHandle(parentElement, (ICPPTemplateInstance) binding)
				: new FunctionTemplateDeclarationHandle(parentElement, (ICPPTemplateInstance) binding);
			} else if (binding instanceof ICPPFunctionTemplate) {
				element= definition 
				? new FunctionTemplateHandle(parentElement, (ICPPFunctionTemplate) binding)
				: new FunctionTemplateDeclarationHandle(parentElement, (ICPPFunctionTemplate) binding);
			} else {
				element= definition 
				? new FunctionHandle(parentElement, (IFunction) binding)
				: new FunctionDeclarationHandle(parentElement, (IFunction) binding);
			}
		}
		else if (binding instanceof IField) {
			element= new FieldHandle(parentElement, (IField) binding);
		}
		else if (binding instanceof IVariable) {
			if (binding instanceof IParameter) {
				return null;
			}
			element= new VariableHandle(parentElement, (IVariable) binding);
		}
		else if (binding instanceof IEnumeration) {
			element= new EnumerationHandle(parentElement, (IEnumeration) binding);
		}
		else if (binding instanceof IEnumerator) {
			element= new EnumeratorHandle(parentElement, (IEnumerator) binding);
		}
		else if (binding instanceof ICompositeType) {
			element= createHandleForComposite(parentElement, (ICompositeType) binding);
		}
		else if (binding instanceof ICPPNamespace) {
			element= new NamespaceHandle(parentElement, (ICPPNamespace) binding);
		}
		else if (binding instanceof ITypedef) {
			element= new TypedefHandle(parentElement, (ITypedef) binding);
		}
		if (element != null && region != null) {
			element.setRangeOfID(region, timestamp);
		}
		return element;
	}

	private static ICElement createParent(ITranslationUnit tu, IBinding binding) throws DOMException {
		IBinding parentBinding= binding.getOwner();
		if (parentBinding == null) {
			IScope scope= binding.getScope();
			if (scope != null && scope.getKind() == EScopeKind.eLocal) {
				return null;
			}
			return tu;
		}
		
		if (parentBinding instanceof IEnumeration) {
			ICElement grandParent= createParent(tu, parentBinding);
			if (parentBinding instanceof ICPPEnumeration && parentBinding.getNameCharArray().length > 0) {
				if (grandParent != null) {
					return new EnumerationHandle(grandParent, (ICPPEnumeration) parentBinding);
				}
			} else {
				return grandParent;
			}
		}

		if (parentBinding instanceof ICPPNamespace) {
			char[] scopeName= parentBinding.getNameCharArray();
			// skip unnamed namespace
			if (scopeName.length == 0) {
				return createParent(tu, parentBinding);
			} 
			ICElement grandParent= createParent(tu, parentBinding);
			if (grandParent == null) 
				return null;
			return new NamespaceHandle(grandParent, (ICPPNamespace) parentBinding);
		} 
		
		if (parentBinding instanceof ICompositeType) {
			ICElement grandParent= createParent(tu, parentBinding);
			if (grandParent != null) {
				return createHandleForComposite(grandParent, (ICompositeType) parentBinding);
			}
		}
		return null;
	}

	private static CElementHandle createHandleForComposite(ICElement parent, ICompositeType classBinding)
			throws DOMException {
		if (classBinding instanceof ICPPClassTemplatePartialSpecialization) {
			return new StructureTemplateHandle(parent, (ICPPClassTemplatePartialSpecialization) classBinding);
		}
		if (classBinding instanceof ICPPClassTemplate) {
			return new StructureTemplateHandle(parent, (ICPPClassTemplate) classBinding);
		}
		if (classBinding instanceof ICPPClassSpecialization) {
			ICPPClassSpecialization spec= (ICPPClassSpecialization) classBinding;
			ICPPClassType orig= spec.getSpecializedBinding();
			if (orig instanceof ICPPClassTemplate) {
				return new StructureTemplateHandle(parent, (ICPPClassSpecialization) classBinding, (ICPPClassTemplate) orig);
			}
		}
		return new StructureHandle(parent, classBinding);
	}
}
