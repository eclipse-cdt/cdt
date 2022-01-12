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
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

/**
 * @author Emanuel Graf IFS
 */
public abstract class CRefactoringContribution extends RefactoringContribution {

	public CRefactoringContribution() {
		super();
	}

	@Override
	public Map<String, String> retrieveArgumentMap(RefactoringDescriptor descriptor) {
		if (descriptor instanceof CRefactoringDescriptor) {
			CRefactoringDescriptor refDesc = (CRefactoringDescriptor) descriptor;
			return refDesc.getParameterMap();
		}
		return super.retrieveArgumentMap(descriptor);
	}
}