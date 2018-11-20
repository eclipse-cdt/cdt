/*******************************************************************************
 * Copyright (c) 2009, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software (IFS)- initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.hidemethod;

import java.util.Map;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

/**
 * @author Emanuel Graf IFS
 */
public class HideMethodRefactoringContribution extends CRefactoringContribution {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment,
			Map arguments, int flags) throws IllegalArgumentException {
		if (id.equals(HideMethodRefactoring.ID)) {
			return new HideMethodRefactoringDescriptor(project, description, comment, arguments);
		}
		return null;
	}
}
