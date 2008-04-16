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
package org.eclipse.cdt.internal.ui.refactoring.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Helps to generate new unsused names.
 * 
 * @author Mirko Stocker
 *
 */
public class PseudoNameGenerator {
	
	private final Set<String> names = new HashSet<String>();
	
	public void addExistingName(String name) {
		names.add(name);
	}
	
	public String generateNewName(String typeName) {
		
		String[] nameParts = typeName.split("::"); //$NON-NLS-1$
		typeName = nameParts[nameParts.length - 1];
		
		String newNameCandidate = null;
		int index = 0;
		
		do {
			index++;
			newNameCandidate = String.format("%s%d", typeName, Integer.valueOf(index)); //$NON-NLS-1$
		} while(names.contains(newNameCandidate));
		
		names.add(newNameCandidate);
		
		return newNameCandidate;
	}
}
