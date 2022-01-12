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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

public class CPPVariableTemplate extends CPPTemplateDefinition
		implements ICPPVariableTemplate, ICPPInternalDeclaredVariable {
	private IType fType;
	private IValue fInitialValue = IntegralValue.NOT_INITIALIZED;
	private boolean fAllResolved;
	private ICPPPartialSpecialization[] partialSpecializations = ICPPPartialSpecialization.EMPTY_ARRAY;

	public CPPVariableTemplate(IASTName name) {
		super(name);
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean isConstexpr() {
		return VariableHelpers.isConstexpr((IASTName) getDefinition());
	}

	@Override
	public boolean isExternC() {
		return CPPVisitor.isExternC(getDefinition(), getDeclarations());
	}

	@Override
	public IType getType() {
		if (fType != null) {
			return fType;
		}

		boolean allResolved = fAllResolved;
		fAllResolved = true;
		fType = VariableHelpers.createType(this, definition, declarations, allResolved);

		return fType;
	}

	@Override
	public IValue getInitialValue() {
		if (fInitialValue == IntegralValue.NOT_INITIALIZED) {
			fInitialValue = computeInitialValue();
		}
		return fInitialValue;
	}

	private IValue computeInitialValue() {
		return VariableHelpers.getInitialValue((IASTName) getDefinition(), (IASTName[]) getDeclarations(), getType());
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

	@Override
	public ICPPPartialSpecialization[] getPartialSpecializations() {
		partialSpecializations = ArrayUtil.trim(partialSpecializations);
		return partialSpecializations;
	}

	public void addPartialSpecialization(ICPPPartialSpecialization partSpec) {
		partialSpecializations = ArrayUtil.append(partialSpecializations, partSpec);
	}

	@Override
	public void allDeclarationsDefinitionsAdded() {
		fAllResolved = true;
	}
}
