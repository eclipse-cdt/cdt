/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IUsing;

public class Using extends SourceManipulation implements IUsing {

	boolean directive;

	public Using(ICElement parent, String name, boolean isDirective) {
		super(parent, name, ICElement.C_USING);
		directive = isDirective;
	}

	@Override
	protected CElementInfo createElementInfo() {
		return new SourceManipulationInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IUsing#isDirective()
	 */
	@Override
	public boolean isDirective() {
		return directive;
	}

}
