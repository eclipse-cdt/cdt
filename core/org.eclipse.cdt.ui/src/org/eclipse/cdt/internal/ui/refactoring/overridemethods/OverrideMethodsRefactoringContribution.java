/*******************************************************************************
 * Copyright (c) 2017 Pavel Marek
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Pavel Marek - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import java.util.Map;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

public class OverrideMethodsRefactoringContribution extends CRefactoringContribution {

	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment,
			Map<String, String> arguments, int flags) throws IllegalArgumentException {
		return null;
	}

}
