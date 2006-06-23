/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IUsing#isDirective()
	 */
	public boolean isDirective() {
		return directive;
	}

}
