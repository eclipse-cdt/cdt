/*******************************************************************************
 * Copyright (c) 2015 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
