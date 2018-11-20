/*******************************************************************************
 * Copyright (c) 2014, 2015 Institute for Software, HSR Hochschule fuer Technik and others
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAttributeList;
import org.eclipse.cdt.internal.core.dom.parser.ASTAttributeList;

/**
 * Represents a C++ attribute list, containing attributes.
 */
public class CPPASTAttributeList extends ASTAttributeList implements ICPPASTAttributeList {
	@Override
	public CPPASTAttributeList copy(CopyStyle style) {
		return copy(new CPPASTAttributeList(), style);
	}

	@Override
	public CPPASTAttributeList copy() {
		return copy(CopyStyle.withoutLocations);
	}
}
