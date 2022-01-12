/*******************************************************************************
 * Copyright (c) 2015, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lukas Wegmann (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableInstance;

/**
 * An instance of a variable template.
 */
public class CPPVariableInstance extends CPPSpecialization implements ICPPVariableInstance, ICPPInternalVariable {
	private ICPPTemplateArgument[] templateArguments;
	private IType type;
	private IValue initialValue;

	public CPPVariableInstance(IBinding specialized, IBinding owner, ICPPTemplateParameterMap argumentMap,
			ICPPTemplateArgument[] args, IType tpe, IValue value) {
		super(specialized, owner, argumentMap);
		templateArguments = args;
		type = tpe;
		initialValue = value;
	}

	@Override
	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return templateArguments;
	}

	@Override
	public boolean isExplicitSpecialization() {
		if (getDefinition() != null)
			return true;
		IASTNode[] decls = getDeclarations();
		if (decls != null) {
			for (IASTNode decl : decls) {
				if (decl != null)
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean isConstexpr() {
		return ((ICPPVariable) getSpecializedBinding()).isConstexpr();
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public IValue getInitialValue() {
		return initialValue;
	}

	@Override
	public boolean isStatic() {
		return hasStorageClass(IASTDeclSpecifier.sc_static);
	}

	@Override
	public boolean isExtern() {
		return hasStorageClass(IASTDeclSpecifier.sc_extern);
	}

	@Override
	public boolean isAuto() {
		return hasStorageClass(IASTDeclSpecifier.sc_auto);
	}

	@Override
	public boolean isRegister() {
		return hasStorageClass(IASTDeclSpecifier.sc_register);
	}

	public boolean hasStorageClass(int storage) {
		IASTName name = (IASTName) getDefinition();
		IASTNode[] ns = getDeclarations();

		return VariableHelpers.hasStorageClass(name, ns, storage);
	}
}
