/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPDeferredVariableInstance;
import org.eclipse.core.runtime.CoreException;

/**
 * AST implementation of ICPPDeferredVariableInstance.
 */
public class CPPDeferredVariableInstance extends CPPUnknownBinding
		implements ICPPDeferredVariableInstance, ISerializableType {
	private final ICPPVariableTemplate fTemplate;
	private final ICPPTemplateArgument[] fArguments;

	public CPPDeferredVariableInstance(ICPPVariableTemplate template, ICPPTemplateArgument[] arguments) {
		super(template.getNameCharArray());
		fTemplate = template;
		fArguments = arguments;
	}

	@Override
	public IBinding getOwner() {
		return fTemplate.getOwner();
	}

	@Override
	public ICPPVariableTemplate getSpecializedBinding() {
		return fTemplate;
	}

	@Override
	public ICPPTemplateParameterMap getTemplateParameterMap() {
		ICPPTemplateParameter[] params = fTemplate.getTemplateParameters();
		int size = Math.min(fArguments.length, params.length);
		CPPTemplateParameterMap map = new CPPTemplateParameterMap(size);
		for (int i = 0; i < size; i++) {
			map.put(params[i], fArguments[i]);
		}
		return map;
	}

	@Override
	public IType getType() {
		// The type cannot vary in partial specializations, so it's safe to use the primary template's type.
		InstantiationContext context = new InstantiationContext(getTemplateParameterMap(), null);
		return CPPTemplates.instantiateType(fTemplate.getType(), context);
	}

	@Override
	public IValue getInitialValue() {
		// Partial specializations can have different initial values, so we can't compute the initial value
		// until we have concrete template arguments and can select a partial specialization or the
		// primary template.
		return IntegralValue.UNKNOWN;
	}

	@Override
	public boolean isStatic() {
		return fTemplate.isStatic();
	}

	@Override
	public boolean isExtern() {
		return fTemplate.isExtern();
	}

	@Override
	public boolean isAuto() {
		return fTemplate.isAuto();
	}

	@Override
	public boolean isRegister() {
		return fTemplate.isRegister();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean isConstexpr() {
		return fTemplate.isConstexpr();
	}

	@Override
	public boolean isExternC() {
		return fTemplate.isExternC();
	}

	@Override
	public ICPPVariableTemplate getTemplateDefinition() {
		return fTemplate;
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return fArguments;
	}

	@Override
	public boolean isExplicitSpecialization() {
		return false;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.DEFERRED_VARIABLE_INSTANCE;
		buffer.putShort(firstBytes);
		buffer.marshalBinding(fTemplate);
		buffer.putInt(fArguments.length);
		for (ICPPTemplateArgument arg : fArguments) {
			buffer.marshalTemplateArgument(arg);
		}
	}

	public static ICPPDeferredVariableInstance unmarshal(IIndexFragment fragment, short firstBytes,
			ITypeMarshalBuffer buffer) throws CoreException {
		IBinding template = buffer.unmarshalBinding();
		int argcount = buffer.getInt();
		ICPPTemplateArgument[] args = new ICPPTemplateArgument[argcount];
		for (int i = 0; i < argcount; i++) {
			args[i] = buffer.unmarshalTemplateArgument();
		}
		return new PDOMCPPDeferredVariableInstance(fragment, (ICPPVariableTemplate) template, args);
	}
}
