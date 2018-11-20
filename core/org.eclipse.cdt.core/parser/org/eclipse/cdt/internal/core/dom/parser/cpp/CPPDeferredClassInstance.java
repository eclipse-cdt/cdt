/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPDeferredClassInstance;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a instantiation that cannot be performed because of dependent arguments or an unknown template.
 */
public class CPPDeferredClassInstance extends CPPUnknownBinding
		implements ICPPDeferredClassInstance, ISerializableType {
	private final ICPPTemplateArgument[] fArguments;
	private final ICPPClassTemplate fClassTemplate;
	private final ICPPScope fLookupScope;

	public CPPDeferredClassInstance(ICPPClassTemplate template, ICPPTemplateArgument[] arguments,
			ICPPScope lookupScope) {
		// With template template parameters the owner must not be calculated, it'd lead to an infinite loop.
		// Rather than that we override getOwner().
		super(template.getNameCharArray());
		fArguments = arguments;
		fClassTemplate = template;
		fLookupScope = lookupScope;
	}

	public CPPDeferredClassInstance(ICPPClassTemplate template, ICPPTemplateArgument[] arguments) {
		this(template, arguments, null);
	}

	@Override
	public IBinding getOwner() {
		return fClassTemplate.getOwner();
	}

	@Override
	public ICPPClassTemplate getClassTemplate() {
		return (ICPPClassTemplate) getSpecializedBinding();
	}

	@Override
	public boolean isExplicitSpecialization() {
		return false;
	}

	@Override
	public CPPDeferredClassInstance clone() {
		return (CPPDeferredClassInstance) super.clone();
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;

		if (type instanceof ITypedef)
			return type.isSameType(this);

		// allow some fuzziness here.
		ICPPClassTemplate classTemplate = getClassTemplate();
		if (type instanceof ICPPDeferredClassInstance) {
			final ICPPDeferredClassInstance rhs = (ICPPDeferredClassInstance) type;
			if (!classTemplate.isSameType((IType) rhs.getSpecializedBinding()))
				return false;

			return CPPTemplates.haveSameArguments(this, rhs);
		}
		return false;
	}

	@Override
	public int getKey() {
		return getClassTemplate().getKey();
	}

	@Override
	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	@Override
	public IField[] getFields() {
		return IField.EMPTY_FIELD_ARRAY;
	}

	@Override
	public IField findField(String name) {
		return null;
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	@Override
	public ICPPMethod[] getMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	@Override
	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public final IScope getCompositeScope() {
		return asScope();
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		return ICPPUsingDeclaration.EMPTY_USING_DECL_ARRAY;
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return fArguments;
	}

	@Override
	public ICPPTemplateDefinition getTemplateDefinition() {
		return fClassTemplate;
	}

	@Override
	public CPPTemplateParameterMap getTemplateParameterMap() {
		ICPPTemplateParameter[] params = fClassTemplate.getTemplateParameters();
		int size = Math.min(fArguments.length, params.length);
		CPPTemplateParameterMap map = new CPPTemplateParameterMap(size);
		for (int i = 0; i < size; i++) {
			map.put(params[i], fArguments[i]);
		}
		return map;
	}

	@Override
	public IBinding getSpecializedBinding() {
		return getTemplateDefinition();
	}

	@Override
	public IScope getScope() throws DOMException {
		return fClassTemplate.getScope();
	}

	@Override
	public ICPPScope asScope() {
		if (fLookupScope != null)
			return fLookupScope;

		return super.asScope();
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this, true);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.DEFERRED_CLASS_INSTANCE;
		buffer.putShort(firstBytes);
		buffer.marshalBinding(fClassTemplate);
		buffer.putInt(fArguments.length);
		for (ICPPTemplateArgument arg : fArguments) {
			buffer.marshalTemplateArgument(arg);
		}
	}

	public static ICPPDeferredClassInstance unmarshal(IIndexFragment fragment, short firstBytes,
			ITypeMarshalBuffer buffer) throws CoreException {
		IBinding template = buffer.unmarshalBinding();
		int argcount = buffer.getInt();
		ICPPTemplateArgument[] args = new ICPPTemplateArgument[argcount];
		for (int i = 0; i < argcount; i++) {
			args[i] = buffer.unmarshalTemplateArgument();
		}
		return new PDOMCPPDeferredClassInstance(fragment, (ICPPClassTemplate) template, args);
	}

	@Override
	public int getVisibility(IBinding member) {
		throw new IllegalArgumentException(member.getName() + " is not a member of " + getName()); //$NON-NLS-1$
	}
}
