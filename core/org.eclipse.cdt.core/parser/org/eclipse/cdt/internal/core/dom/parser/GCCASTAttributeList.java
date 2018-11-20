/*******************************************************************************
 * Copyright (c) 2014, 2015 Institute for Software, HSR Hochschule fuer Technik
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
