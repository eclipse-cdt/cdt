/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * Represents a class template.
 */
public class CPPClassTemplate extends CPPTemplateDefinition implements ICPPClassTemplate,
		ICPPInternalClassTemplate, ICPPInternalClassTypeMixinHost {

	private ICPPClassTemplatePartialSpecialization[] partialSpecializations = null;
	private ICPPDeferredClassInstance fDeferredInstance;
	private boolean addedPartialSpecializationsOfIndex;

	public CPPClassTemplate(IASTName name) {
		super(name);
	}

	@Override
	public void checkForDefinition() { 
		// Ambiguity resolution ensures that definitions are resolved.
	}
	
	@Override
	public void addPartialSpecialization(ICPPClassTemplatePartialSpecialization spec) {
		partialSpecializations = ArrayUtil.append(
				ICPPClassTemplatePartialSpecialization.class, partialSpecializations, spec);
	}

	@Override
	public ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier() {
		if (definition != null) {
			IASTNode node = definition.getParent();
			if (node instanceof ICPPASTQualifiedName)
				node = node.getParent();
			if (node instanceof ICPPASTCompositeTypeSpecifier)
				return (ICPPASTCompositeTypeSpecifier) node;
		}
		return null;
	}

	@Override
	public ICPPClassScope getCompositeScope() {
		if (definition == null) {
			checkForDefinition();
		}
		if (definition != null) {
			IASTNode parent = definition.getParent();
			while (parent instanceof IASTName)
				parent = parent.getParent();
			if (parent instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier)parent;
				return compSpec.getScope();
			}
		}
		
		// Forward declarations must be backed up from the index.
		ICPPClassTemplate ib = getIndexBinding();
		if (ib != null) {
			IScope scope = ib.getCompositeScope();
			if (scope instanceof ICPPClassScope)
				return (ICPPClassScope) scope;
		}
		return null;
	}

	@Override
	public int getKey() {
		if (definition != null) {
			ICPPASTCompositeTypeSpecifier cts= getCompositeTypeSpecifier();
			if (cts != null) {
				return cts.getKey();
			}
			IASTNode n= definition.getParent();
			if (n instanceof ICPPASTElaboratedTypeSpecifier) {
				return ((ICPPASTElaboratedTypeSpecifier) n).getKind();
			}
		}

		if (declarations != null && declarations.length > 0) {
			IASTNode n = declarations[0].getParent();
			if (n instanceof ICPPASTElaboratedTypeSpecifier) {
				return ((ICPPASTElaboratedTypeSpecifier) n).getKind();
			}
		}

		return ICPPASTElaboratedTypeSpecifier.k_class;
	}
	
	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		if (!addedPartialSpecializationsOfIndex) {
			addedPartialSpecializationsOfIndex= true;
			ICPPClassTemplate ib = getIndexBinding();
			if (ib != null) {
				IIndexFileSet fs = getTemplateName().getTranslationUnit().getIndexFileSet();
				for (ICPPClassTemplatePartialSpecialization spec : ib.getPartialSpecializations()) {
					if (spec instanceof IIndexBinding && fs.containsDeclaration((IIndexBinding) spec)) {
						addPartialSpecialization(spec);
					}
				}
			}
		}
		partialSpecializations = ArrayUtil.trim(ICPPClassTemplatePartialSpecialization.class, partialSpecializations);
		return partialSpecializations;
	}
	
	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef || type instanceof IIndexBinding)
			return type.isSameType(this);
		return false;
	}
	
	@Override
	public ICPPBase[] getBases() {
		return ClassTypeHelper.getBases(this);
	}

	@Override
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		return ClassTypeHelper.getDeclaredFields(this);
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
	public ICPPMethod[] getDeclaredMethods() {
		return ClassTypeHelper.getDeclaredMethods(this);
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		return ClassTypeHelper.getConstructors(this);
	}

	@Override
	public IBinding[] getFriends() {
		return ClassTypeHelper.getFriends(this);
	}
	
	@Override
	public ICPPClassType[] getNestedClasses() {
		return ClassTypeHelper.getNestedClasses(this);
	}

	@Override
	public IField findField(String name) {
		return ClassTypeHelper.findField(this, name);
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

	/* (non-Javadoc)
	 * For debug purposes only
	 */
	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public final ICPPDeferredClassInstance asDeferredInstance() {
		if (fDeferredInstance == null) {
			fDeferredInstance= CPPTemplates.createDeferredInstance(this);
		}
		return fDeferredInstance;
	}

	@Override
	public ICPPTemplateArgument getDefaultArgFromIndex(int paramPos) throws DOMException {
		ICPPClassTemplate ib = getIndexBinding();
		if (ib != null) {
			ICPPTemplateParameter[] params = ib.getTemplateParameters();
			if (paramPos < params.length) {
				ICPPTemplateParameter param = params[paramPos];
				return param.getDefaultValue();
			}
		}
		return null;
	}
}
