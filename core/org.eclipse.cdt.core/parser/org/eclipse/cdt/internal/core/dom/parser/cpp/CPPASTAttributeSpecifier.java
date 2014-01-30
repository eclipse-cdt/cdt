/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.parser.cpp.ICPPASTAttributeSpecifier;

/**
 * Represents a C++ attribute specifier, containing attributes.
 */
public class CPPASTAttributeSpecifier extends ASTAttributeSpecifier implements ICPPASTAttributeSpecifier {
	@Override
	public CPPASTAttributeSpecifier copy(CopyStyle style) {
		return copy(new CPPASTAttributeSpecifier(), style);
	}

	@Override
	public CPPASTAttributeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}
}
