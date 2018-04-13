/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hansruedi Patzen (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ms.IMSASTDeclspecList;

/**
 * Represents a GCC attribute list, containing attributes.
 */
public class MSASTDeclspecList extends ASTAttributeList implements IMSASTDeclspecList {
	@Override
	public MSASTDeclspecList copy(CopyStyle style) {
		return copy(new MSASTDeclspecList(), style);
	}

	@Override
	public MSASTDeclspecList copy() {
		return copy(CopyStyle.withoutLocations);
	}
}
