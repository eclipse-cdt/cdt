/*******************************************************************************
 * Copyright (c) 2009, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software (IFS)- initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.movetype;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContribution;

public class MoveTypeRefactoringContribution extends CRefactoringContribution {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description,
			String comment, Map arguments, int flags) throws IllegalArgumentException {
		if (id.equals(MoveTypeRefactoring.ID)) {
			return new MoveTypeRefactoringDescriptor(project, description, comment, arguments);
		}
		return null;
	}
}
