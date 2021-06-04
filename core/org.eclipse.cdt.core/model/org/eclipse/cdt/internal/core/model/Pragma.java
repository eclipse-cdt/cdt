/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IPragma;

public class Pragma extends SourceManipulation implements IPragma {

	public Pragma(ICElement parent, String name) {
		super(parent, name, ICElement.C_PRAGMA);
	}

	@Override
	public String getIdentifierList() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getTokenSequence() {
		return ""; //$NON-NLS-1$
	}

	@Override
	protected CElementInfo createElementInfo() {
		return new SourceManipulationInfo(this);
	}
}
