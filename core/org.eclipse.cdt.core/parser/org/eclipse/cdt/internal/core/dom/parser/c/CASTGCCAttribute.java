/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.ASTGCCAttribute;

/**
 * C-specific attribute.
 */
public class CASTGCCAttribute extends ASTGCCAttribute {
	
    public CASTGCCAttribute() {
    	super();
	}

	public CASTGCCAttribute(IASTName name) {
		super(name);
	}
	
	@Override
	public CASTGCCAttribute copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTGCCAttribute copy(CopyStyle style) {
		return copy(new CASTGCCAttribute(), style);
	}
}
