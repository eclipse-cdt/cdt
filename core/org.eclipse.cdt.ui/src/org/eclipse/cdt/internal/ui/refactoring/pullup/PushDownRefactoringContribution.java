/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContribution;

public class PushDownRefactoringContribution extends CRefactoringContribution {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description,
			String comment, Map arguments, int flags) throws IllegalArgumentException {
		if (id.equals(PushDownRefactoring.ID)) {
			return new PushDownRefactoringDescriptor(project, description, comment, 
					arguments);
		}
		return null;
	}
}
