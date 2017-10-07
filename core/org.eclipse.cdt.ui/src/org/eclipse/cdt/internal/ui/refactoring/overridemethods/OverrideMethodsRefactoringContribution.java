/******************************************************************************* 
 * Copyright (c) 2017 Pavel Marek 
 * All rights reserved. This program and the accompanying materials  
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at  
 * http://www.eclipse.org/legal/epl-v10.html   
 *  
 * Contributors:  
 *      Pavel Marek - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContribution;

public class OverrideMethodsRefactoringContribution extends CRefactoringContribution {

	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description,
			String comment, Map<String, String> arguments, int flags) throws IllegalArgumentException {
		return null;
	}

}
