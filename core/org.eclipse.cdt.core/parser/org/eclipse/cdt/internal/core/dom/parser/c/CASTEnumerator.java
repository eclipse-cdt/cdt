/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.ASTEnumerator;

/**
 * C-specific enumerator
 */
public class CASTEnumerator extends ASTEnumerator {
	
    public CASTEnumerator() {
    	super();
	}

	public CASTEnumerator(IASTName name, IASTExpression value) {
		super(name, value);
	}
	
	@Override
	public CASTEnumerator copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTEnumerator copy(CopyStyle style) {
		CASTEnumerator copy = new CASTEnumerator();
		copyAbstractEnumerator(copy, style);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
}
