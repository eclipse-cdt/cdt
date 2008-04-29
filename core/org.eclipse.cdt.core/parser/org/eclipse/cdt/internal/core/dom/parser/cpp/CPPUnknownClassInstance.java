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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/*
 * Represents a partially instantiated C++ class template, declaration of which is not yet available.
 *
 * @author Sergey Prigogin
 */
public class CPPUnknownClassInstance extends CPPUnknownClass implements ICPPInternalUnknownClassInstance {
	private final IType[] arguments;

	public CPPUnknownClassInstance(ICPPInternalUnknown scopeBinding, IASTName name, IType[] arguments) {
		super(scopeBinding, name);
		this.arguments = arguments;
	}

	public IType[] getArguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown#resolveUnknown(org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	@Override
	public IBinding resolveUnknown(ObjectMap argMap) throws DOMException {
		IBinding result = super.resolveUnknown(argMap);
		if (result instanceof ICPPSpecialization && result instanceof ICPPTemplateDefinition) {
			IType[] newArgs = CPPTemplates.instantiateTypes(arguments, argMap);
			result = CPPTemplates.instantiateTemplate((ICPPTemplateDefinition) result, newArgs, null);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassType#resolvePartially(org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown, org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	@Override
	public IBinding resolvePartially(ICPPInternalUnknown parentBinding, ObjectMap argMap) {
		IType[] newArgs = CPPTemplates.instantiateTypes(arguments, argMap);
		return new CPPUnknownClassInstance(parentBinding, name, newArgs);
	}

	@Override
	public String toString() {
		return getName() + " <" + ASTTypeUtil.getTypeListString(arguments) + ">"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
