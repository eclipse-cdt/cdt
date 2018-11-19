/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains the name for a new variable and holds a list of used names.
 */
public class VariableNameInformation {
	private String name = "";	 //$NON-NLS-1$
	private final ArrayList<String> usedNames = new ArrayList<String>();

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
	
	public void addNamesToUsedNames(Collection<String> names) {
		usedNames.addAll(names);
	}
}
