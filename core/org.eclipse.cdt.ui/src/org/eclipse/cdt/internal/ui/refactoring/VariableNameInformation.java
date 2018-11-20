/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the name for a new variable and holds a list of used names.
 */
public class VariableNameInformation {
	private String name = ""; //$NON-NLS-1$
	private final ArrayList<String> usedNames = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getUsedNames() {
		return usedNames;
	}

	public void addNameToUsedNames(String name) {
		usedNames.add(name);
	}

	public void addNamesToUsedNames(List<String> names) {
		usedNames.addAll(names);
	}
}
