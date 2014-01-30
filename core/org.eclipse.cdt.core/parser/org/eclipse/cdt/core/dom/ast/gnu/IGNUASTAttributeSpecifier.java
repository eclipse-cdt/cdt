/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;

/**
 * Represents a GNU attribute specifier, introduced by __attribute__.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.7
 */
public interface IGNUASTAttributeSpecifier extends IASTAttributeSpecifier {
	@Override
	public IGNUASTAttributeSpecifier copy();

	@Override
	public IGNUASTAttributeSpecifier copy(CopyStyle style);
}