/*******************************************************************************
 * Copyright (c) 2014, 2015 Institute for Software, HSR Hochschule fuer Technik
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

import org.eclipse.cdt.core.dom.ast.gnu.IGCCASTAttributeList;

/**
 * Represents a GCC attribute list, containing attributes.
 */
public class GCCASTAttributeList extends ASTAttributeList implements IGCCASTAttributeList {
	@Override
	public GCCASTAttributeList copy(CopyStyle style) {
		return copy(new GCCASTAttributeList(), style);
	}

	@Override
	public GCCASTAttributeList copy() {
		return copy(CopyStyle.withoutLocations);
	}
}
