/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPASTAttributeSpecifier;

/**
 * A C++ alignment-specifier.
 *
 * In the C++ grammar, an alignment-specifier is an attribute-specifier.
 *
 * @since 6.0
 */
public interface ICPPASTAlignmentSpecifier extends IASTAlignmentSpecifier, ICPPASTAttributeSpecifier {
	@Override
	public ICPPASTAlignmentSpecifier copy();

	@Override
	public ICPPASTAlignmentSpecifier copy(CopyStyle style);

}
