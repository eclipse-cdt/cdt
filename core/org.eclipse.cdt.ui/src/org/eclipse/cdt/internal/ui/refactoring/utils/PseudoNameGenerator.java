/*******************************************************************************
 * Copyright (c) 2008, 2010 Institute for Software, HSR Hochschule fuer Technik  
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
 * Helps to generate new unused names.
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
		if (typeName.contains("<")) { //$NON-NLS-1$
			typeName = typeName.substring(0, typeName.indexOf('<'));
		}
		if (typeName.length() != 0) {
			typeName = typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
		}

		nameParts = typeName.split("\\s"); //$NON-NLS-1$
		for (int i = 0; i < nameParts.length; i++) {
			if (i <= 0) {
				typeName = nameParts[i];
			} else {
				typeName = typeName.concat(nameParts[i].substring(0,1).toUpperCase());
				if (nameParts[i].length() > 1) {
					typeName = typeName.concat(nameParts[i].substring(1));
				}
			}
		}

		String numberString = ""; //$NON-NLS-1$
		String newNameCandidate;
		int index = 0;
		
		do {
			newNameCandidate = typeName + numberString;
			if (!NameHelper.isValidLocalVariableName(newNameCandidate)) {
				return ""; //$NON-NLS-1$
			}
			index++;
			numberString = Integer.toString(index);
		} while (names.contains(newNameCandidate) || NameHelper.isKeyword(newNameCandidate));
		
		names.add(newNameCandidate);
		return newNameCandidate;
	}
}
