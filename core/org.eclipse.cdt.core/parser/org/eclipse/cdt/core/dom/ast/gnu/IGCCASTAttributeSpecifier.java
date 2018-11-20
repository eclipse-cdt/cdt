/*******************************************************************************
 * Copyright (c) 2014, 2015 Institute for Software, HSR Hochschule fuer Technik and others
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
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.parser.util.InstanceOfPredicate;

/**
 * @since 5.7
 * @deprecated Use IGCCASTAttributeList instead.
 * @noreference This interface is not intended to be referenced by clients.
 */
@Deprecated
public interface IGCCASTAttributeSpecifier extends IASTAttributeSpecifier {
	public static InstanceOfPredicate<IASTAttributeSpecifier> TYPE_FILTER = new InstanceOfPredicate<>(
			IGCCASTAttributeSpecifier.class);

	@Override
	public IGCCASTAttributeSpecifier copy();

	@Override
	public IGCCASTAttributeSpecifier copy(CopyStyle style);
}