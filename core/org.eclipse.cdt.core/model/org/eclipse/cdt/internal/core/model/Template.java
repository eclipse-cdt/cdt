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
import org.eclipse.cdt.core.model.ITemplate;

public class Template extends SourceManipulation implements ITemplate{

	public Template(ICElement parent, String name) {
		super(parent, name, CElement.C_TEMPLATE);
	}

	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IMember#getVisibility()
	 */
	public int getVisibility() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#getAccessControl()
	 */
	public int getAccessControl() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#isConst()
	 */
	public boolean isConst() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#isStatic()
	 */
	public boolean isStatic() {
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IDeclaration#isVolatile()
	 */
	public boolean isVolatile() {
		return false;
	}

}
