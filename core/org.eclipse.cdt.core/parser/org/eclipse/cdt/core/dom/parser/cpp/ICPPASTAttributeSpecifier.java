/*******************************************************************************
 * Copyright (c) 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.parser.util.InstanceOfPredicate;

/**
 * Represents a C++11 (ISO/IEC 14882:2011 7.6.1 [dcl.attr.grammar]) attribute specifier.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.7
 */
public interface ICPPASTAttributeSpecifier extends IASTAttributeSpecifier {
	public static InstanceOfPredicate<IASTAttributeSpecifier> TYPE_FILTER = new InstanceOfPredicate<>(
			ICPPASTAttributeSpecifier.class);

	@Override
	public ICPPASTAttributeSpecifier copy();

	@Override
	public ICPPASTAttributeSpecifier copy(CopyStyle style);
}