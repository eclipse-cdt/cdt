/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.model.IMacro;

public class Macro extends SourceManipulation implements IMacro {
	
	public Macro(ICElement parent, String name) {
		super(parent, name, ICElement.C_MACRO);
	}

	public String getIdentifierList() {
		return ""; //$NON-NLS-1$
	}

	public String getTokenSequence() {
		return ""; //$NON-NLS-1$
	}

	protected CElementInfo createElementInfo () {
		return new SourceManipulationInfo(this);
	}
}
