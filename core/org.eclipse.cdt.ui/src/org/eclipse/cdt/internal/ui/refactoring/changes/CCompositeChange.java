/*******************************************************************************
 * Copyright (c) 2009, 2014 Institute for Software, HSR Hochschule fuer Technik
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
package org.eclipse.cdt.internal.ui.refactoring.changes;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;

/**
 * @author Emanuel Graf IFS
 */
public class CCompositeChange extends CompositeChange {
	private RefactoringChangeDescriptor desc;

	public CCompositeChange(String name, Change[] children) {
		super(name, children);
	}

	public CCompositeChange(String name) {
		super(name);
	}

	public void setDescription(RefactoringChangeDescriptor descriptor) {
		desc = descriptor;
	}

	@Override
	public ChangeDescriptor getDescriptor() {
		if (desc != null) {
			return desc;
		}
		return super.getDescriptor();
	}
}
