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
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTAttributeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ASTAttributeSpecifier;

/**
 * Represents a GNU attribute specifier, containing attributes.
 */
public class GNUASTAttributeSpecifier extends ASTAttributeSpecifier implements IGNUASTAttributeSpecifier {
	@Override
	public GNUASTAttributeSpecifier copy(CopyStyle style) {
		return copy(new GNUASTAttributeSpecifier(), style);
	}

	@Override
	public GNUASTAttributeSpecifier copy() {
		return copy(CopyStyle.withoutLocations);
	}
}
