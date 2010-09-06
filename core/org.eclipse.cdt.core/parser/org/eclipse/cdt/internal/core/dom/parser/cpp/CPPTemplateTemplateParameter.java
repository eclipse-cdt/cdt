/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * A template template parameter.
 */
public class CPPTemplateTemplateParameter extends CPPTemplateParameter implements
		ICPPTemplateTemplateParameter, ICPPInternalTemplate, ICPPUnknownBinding,
		ICPPUnknownType {

	private ICPPTemplateParameter[] templateParameters;
	private ObjectMap instances;
	private ICPPScope unknownScope;
	private final boolean fIsParameterPack;

	public CPPTemplateTemplateParameter(IASTName name, boolean isPack) {
		super(name);
		fIsParameterPack= isPack;
	}

	public final boolean isParameterPack() {
		return fIsParameterPack;
	}

	public ICPPScope asScope() {
	    if (unknownScope == null) {
	    	IASTName n = null;
	    	IASTNode[] nodes = getDeclarations();
	    	if (nodes != null && nodes.length > 0)
	    		n = (IASTName) nodes[0];
	        unknownScope = new CPPUnknownScope(this, n);
	    }
	    return unknownScope;
	}
	
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (templateParameters == null) {
			ICPPASTTemplatedTypeTemplateParameter template = (ICPPASTTemplatedTypeTemplateParameter) getPrimaryDeclaration().getParent();
			ICPPASTTemplateParameter[] params = template.getTemplateParameters();
			ICPPTemplateParameter[] result = null;
			for (ICPPASTTemplateParameter param : params) {
				IBinding binding = CPPTemplates.getTemplateParameterName(param).resolvePreBinding();
				if (binding instanceof ICPPTemplateParameter) {
					result = (ICPPTemplateParameter[]) ArrayUtil.append(ICPPTemplateParameter.class, result, binding);
				}
			}
			templateParameters = (ICPPTemplateParameter[]) ArrayUtil.trim(ICPPTemplateParameter.class, result);
		}
		return templateParameters;
	}

	public IBinding resolveTemplateParameter(ICPPTemplateParameter templateParameter) {
		return templateParameter;
	}

	public ICPPClassTemplatePartialSpecialization[] getTemplateSpecializations() throws DOMException {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	public IType getDefault() {
		IASTName[] nds = getDeclarations();
		if (nds == null || nds.length == 0)
		    return null;
		for (IASTName nd : nds) {
			if (nd != null) {
				IASTNode parent = nd.getParent();
				assert parent instanceof ICPPASTTemplatedTypeTemplateParameter;
				if (parent instanceof ICPPASTTemplatedTypeTemplateParameter) {
					ICPPASTTemplatedTypeTemplateParameter param = (ICPPASTTemplatedTypeTemplateParameter) parent;
					IASTExpression value = param.getDefaultValue();
					if (value instanceof IASTIdExpression) {
						IASTName name= ((IASTIdExpression) value).getName();
						IBinding b= name.resolveBinding();
						if (b instanceof IType) {
							return (IType) b;
						}
					}
				}
			}
		}
		return null;
	}
	
	public ICPPTemplateArgument getDefaultValue() {
		IType d= getDefault();
		if (d == null)
			return null;
		
		return new CPPTemplateArgument(d);
	}

	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}
	public IField[] getFields() {
		return IField.EMPTY_FIELD_ARRAY;
	}
	public IField findField(String name) {
		return null;
	}
	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}
	public ICPPMethod[] getMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}
	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}
	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}
	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}
	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	public int getKey() {
		return 0;
	}

	public IScope getCompositeScope() {
		return null;
	}

    public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);
		if (!(type instanceof ICPPTemplateTemplateParameter))
			return false;

		return getParameterID() == ((ICPPTemplateParameter) type).getParameterID();
	}

	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() throws DOMException {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	public final void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		if (instances == null)
			instances = new ObjectMap(2);
		String key= ASTTypeUtil.getArgumentListString(arguments, true);
		instances.put(key, instance);
	}

	public final ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		if (instances != null) {
			String key= ASTTypeUtil.getArgumentListString(arguments, true);
			return (ICPPTemplateInstance) instances.get(key);
		}
		return null;
	}

	public ICPPTemplateInstance[] getAllInstances() {
		if (instances != null) {
			ICPPTemplateInstance[] result= new ICPPTemplateInstance[instances.size()];
			for (int i=0; i < instances.size(); i++) {
				result[i]= (ICPPTemplateInstance) instances.getAt(i);
			}
			return result;
		}
		return ICPPTemplateInstance.EMPTY_TEMPLATE_INSTANCE_ARRAY;
	}
	
	public IASTName getUnknownName() {
		return new CPPASTName(getNameCharArray());
	}

	public boolean isAnonymous() {
		return false;
	}
	
	public ICPPDeferredClassInstance asDeferredInstance() {
		return null;
	}
}
