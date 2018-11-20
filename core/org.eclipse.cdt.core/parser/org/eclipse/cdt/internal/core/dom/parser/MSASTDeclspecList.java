/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
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
 *     Hansruedi Patzen (IFS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ms.IMSASTDeclspecList;

/**
 * Represents a __declspec list.
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
