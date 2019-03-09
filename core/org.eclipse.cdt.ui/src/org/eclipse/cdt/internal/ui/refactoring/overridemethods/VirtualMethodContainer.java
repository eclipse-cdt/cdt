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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * Container for virtual methods collected by {@link VirtualMethodsASTVisitor}.
 * Also serves as content provider for <code>CheckBoxTree</code> in wizard.
 */
public class VirtualMethodContainer implements ITreeContentProvider {
	private Map<ICPPClassType, List<Method>> fData = new HashMap<>();
	final private OverrideOptions fOptions;

	public VirtualMethodContainer(OverrideOptions options) {
		fOptions = options;
	}

	/**
	 * Returns all parents (ICPPClassTypes).
	 * @param inputElement root element.
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof Map<?, ?>)) {
			return null;
		} else {
			return fData.keySet().toArray(new ICPPClassType[fData.keySet().size()]);
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return fData.get(parentElement).toArray();
	}

	/**
	 * Returns all virtual methods for given ICPPClassType.
	 * @param classType
	 */
	public List<Method> getMethods(ICPPClassType classType) {
		return fData.get(classType);
	}

	@Override
	public Object getParent(Object element) {
		for (Entry<ICPPClassType, List<Method>> entry : fData.entrySet()) {
			if (entry.getValue().contains(element)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		List<Method> list = fData.get(element);
		if (list == null) {
			return false;
		} else {
			return !list.isEmpty();
		}
	}

	public List<Method> getAllMethods() {
		List<Method> allMethods = new ArrayList<>();

		for (Entry<ICPPClassType, List<Method>> entry : fData.entrySet()) {
			allMethods.addAll(entry.getValue());
		}

		return allMethods;
	}

	/**
	 * This method is called to populate the tree viewer input, thats why
	 * it is not named "getData".
	 */
	public Map<ICPPClassType, List<Method>> getInitialInput() {
		return fData;
	}

	/**
	 * Checks if given method is already contained in fData.
	 * @param method
	 * @return
	 */
	private boolean isDuplicate(Method method) {
		for (Entry<ICPPClassType, List<Method>> entry : fData.entrySet()) {
			if (entry.getValue().contains(method)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds one method to a specified ICPPClassType.
	 * @param classType
	 * @param method
	 */
	private void addMethodToClass(ICPPClassType classType, Method method) {
		if (!isDuplicate(method)) {
			List<Method> methods = fData.get(classType);
			if (methods == null) {
				methods = new ArrayList<>();
				fData.put(classType, methods);
			}
			methods.add(method);
		}
	}

	public void addMethodsToClass(ICPPClassType classType, ICPPMethod[] methods, IASTDeclSpecifier declSpecifier) {
		for (ICPPMethod icppMethod : methods) {
			addMethodToClass(classType, new Method(icppMethod, declSpecifier, fOptions));
		}
	}

	public boolean isEmpty() {
		return fData.isEmpty();
	}

	public void remove(ICPPMethod method) {
		// Search through all saved methods.
		for (Map.Entry<ICPPClassType, List<Method>> entry : fData.entrySet()) {
			List<Method> methods = entry.getValue();

			if (methods.remove(new Method(method, fOptions))) {
				// Check if classType (parent) is empty ie. if there are no
				// methods to display.
				if (methods.isEmpty()) {
					fData.remove(entry.getKey());
				}
				break;
			}
		}
	}
}
