/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Andrew Niefer (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * The result of instantiating a class template or an explicit specialization of a class template.
 * The {@link #isExplicitSpecialization()} method is used to distinguish between the two cases.
 */
public class CPPClassInstance extends CPPClassSpecialization implements ICPPTemplateInstance {
	private final ICPPTemplateArgument[] arguments;

	public CPPClassInstance(ICPPClassType orig, IBinding owner, ICPPTemplateParameterMap argMap,
			ICPPTemplateArgument[] args) {
		super(orig, owner, argMap);
		this.arguments = args;
	}

	@Override
	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}

	@Override
	public ICPPTemplateArgument[] getTemplateArguments() {
		return arguments;
	}

	@Override
	protected ICPPClassSpecializationScope getSpecializationScope() {
		// An instance with a definition has no specialization scope.
		checkForDefinition();
		if (getDefinition() != null)
			return null;

		return super.getSpecializationScope();
	}

	@Override
	public boolean isExplicitSpecialization() {
		// An instance with a declaration is an explicit specialization.
		checkForDefinition();
		if (getDefinition() != null)
			return true;
		final IASTNode[] decls = getDeclarations();
		if (decls != null && decls.length > 0 && decls[0] != null)
			return true;

		return false;
	}

	/**
	 * For debugging only.
	 */
	@Override
	public String toString() {
		return getName() + " " + ASTTypeUtil.getArgumentListString(arguments, true); //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ICPPClassType && isSameType((ICPPClassType) obj);
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);

		return isSameClassInstance(this, type);
	}

	public static boolean isSameClassInstance(ICPPClassSpecialization classInstance, IType type) {
		assert classInstance instanceof ICPPTemplateInstance;

		// Require a class instance.
		if (!(type instanceof ICPPClassSpecialization) || !(type instanceof ICPPTemplateInstance)
				|| type instanceof IProblemBinding) {
			return false;
		}

		final ICPPClassSpecialization classSpec2 = (ICPPClassSpecialization) type;
		final ICPPClassType orig1 = classInstance.getSpecializedBinding();
		final ICPPClassType orig2 = classSpec2.getSpecializedBinding();
		if (!orig1.isSameType(orig2))
			return false;

		return CPPTemplates.haveSameArguments((ICPPTemplateInstance) classInstance, (ICPPTemplateInstance) type);
	}
}
