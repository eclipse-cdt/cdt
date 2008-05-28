/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * Associate a name with a visibility and holds a list of used names.
 *
 */
public class NameNVisibilityInformation {
	
	private String name = "";	 //$NON-NLS-1$
	private VisibilityEnum visibility = VisibilityEnum.v_public;
	private final ArrayList<String> usedNames = new ArrayList<String>();

	public String getName() {
		return name; 
	}

	public void setName(String name) {
		this.name = name;
	}

	public VisibilityEnum getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityEnum visibility) {
		this.visibility = visibility;
	}
	
	public ArrayList<String> getUsedNames(){
		return usedNames;
	}
	
	public void addNameToUsedNames(String name) {
		usedNames.add(name);
	}
	
	public void addNamesToUsedNames(ArrayList<String> names) {
		usedNames.addAll(names);
	}
	
}
