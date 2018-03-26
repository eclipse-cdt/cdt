/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.inlinelocalvariable;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContribution;

/**
 * @since 6.5
 */
public class InlineLocalVariableRefactoringContribution extends CRefactoringContribution {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment,
			Map arguments, int flags) throws IllegalArgumentException {
		if (id.equals(InlineLocalVariableRefactoring.ID)) {
			return new InlineLocalVariableRefactoringDescriptor(project, description, comment, arguments);
		}
		return null;
	}
}
