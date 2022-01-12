/*******************************************************************************
 * Copyright (c) 2006, 2010 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceNotFoundElement;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditorInput;

public class CSourceNotFoundEditorInput extends CommonSourceNotFoundEditorInput {

	public CSourceNotFoundEditorInput(Object artifact) {
		super(artifact);
	}

	@Override
	public String getName() {
		Object artifact = getArtifact();
		if (artifact instanceof CSourceNotFoundElement) {
			String description = ((CSourceNotFoundElement) artifact).getDescription();
			if (description != null) {
				return description;
			}
		}
		return super.getName();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CSourceNotFoundEditorInput) {
			return super.equals(other) || (this.getName().equals(((CSourceNotFoundEditorInput) other).getName()));
		}
		return super.equals(other);
	}
}
