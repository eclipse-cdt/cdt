/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring.code.flow;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

/**
 * Index of local variables inside a function. Each variable is assigned an integer index in normal
 * code reading order. A variable with a smaller index is declared before a variable with a larger
 * one.
 */
public class LocalVariableIndex {
	private final Map<IVariable, Integer> variableMap = new HashMap<IVariable, Integer>();

	public LocalVariableIndex(IASTFunctionDefinition functionDefinition) {
		functionDefinition.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name instanceof ICPPASTQualifiedName || name.isQualified() || !name.isDeclaration()) {
					return PROCESS_CONTINUE;
				}

				IBinding binding = name.resolveBinding();
				if (binding instanceof IVariable && !(binding instanceof IField)) {
					IVariable variable = (IVariable) binding;
					if (!variableMap.containsKey(variable)) {
						variableMap.put(variable, variableMap.size());
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	/**
	 * Returns the number of local variables in the index.
	 */
	public int getNumLocalVariables() {
		return variableMap.size();
	}

	/**
	 * Returns the index for the given local variable.
	 * @param variable the local variable
	 * @return the index of the variable, or -1 if the variable in not contained in the index.
	 */
	public int getIndexFromLocal(IVariable variable) {
		Integer index = variableMap.get(variable);
		return index != null ? index.intValue() : -1;
	}
}
