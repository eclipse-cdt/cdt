package org.eclipse.cdt.internal.core.model;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IUsing;

public class Using extends SourceManipulation implements IUsing {

	boolean directive;

	public Using(ICElement parent, String name, boolean isDirective) {
		super(parent, name, CElement.C_USING);
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
