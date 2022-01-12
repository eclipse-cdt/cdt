/*******************************************************************************
 * Copyright (c) 2017 Pavel Marek
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Pavel Marek - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

/**
 * Virtual method collector invoked from {@link VirtualMethodsASTVisitor}.
 * @author Pavel Marek
 */
public class MethodCollector {
	/**
	 * Ignore virtual destructors.
	 * @param base
	 * @return
	 */
	private ICPPMethod[] virtualMethods(ICPPClassType clas) {
		ArrayList<ICPPMethod> virtualMethods = new ArrayList<>();

		ICPPMethod[] methods = clas.getDeclaredMethods();
		// Traverse all methods and check for virtuality.
		for (ICPPMethod method : methods) {
			if (method.isVirtual() && !method.isDestructor() && !method.isFinal()) {
				virtualMethods.add(method);
			}
		}

		return virtualMethods.toArray(new ICPPMethod[virtualMethods.size()]);
	}

	private List<ICPPClassType> getBaseClasses(ICPPClassType classType) {
		List<ICPPClassType> baseClasses = new ArrayList<>();

		ICPPBase[] bases = classType.getBases();
		for (int i = 0; i < bases.length; i++) {
			IBinding binding = bases[i].getBaseClass();
			if (binding instanceof ICPPClassType) {
				baseClasses.add((ICPPClassType) binding);
			}
		}

		return baseClasses;
	}

	/**
	 * Implemented with recursion.
	 * @param container
	 * @param classType
	 */
	private void fillContainerRecursion(VirtualMethodContainer container, ICPPClassType classType,
			IASTDeclSpecifier declSpecifier) {
		List<ICPPClassType> baseClasses = getBaseClasses(classType);
		// Recursion base (at top base class).
		if (baseClasses.size() == 0) {
			container.addMethodsToClass(classType, virtualMethods(classType), declSpecifier);
		} else {
			for (ICPPClassType baseClass : baseClasses) {
				// Recurse.
				fillContainerRecursion(container, baseClass, declSpecifier);
			}
			// Add also virtual methods of this class.
			container.addMethodsToClass(classType, virtualMethods(classType), declSpecifier);
		}
	}

	/**
	 * Just calls private method - this is to avoid storing virtual methods from
	 * the current class eg. the class that the refactoring was invoked from.
	 * @param container
	 * @param classType
	 */
	public void fillContainer(VirtualMethodContainer container, ICPPClassType classType,
			IASTDeclSpecifier declSpecifier) {
		List<ICPPClassType> baseClasses = getBaseClasses(classType);
		// Check if there are any base classes.
		if (baseClasses.size() != 0) {
			for (ICPPClassType baseClass : baseClasses) {
				// Recurse.
				fillContainerRecursion(container, baseClass, declSpecifier);
			}
		}
	}
}
