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
package org.eclipse.cdt.core.dom.ast.gnu;

import org.eclipse.cdt.core.dom.ast.IASTAttributeList;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.parser.util.InstanceOfPredicate;

/**
 * Represents a GCC attribute specifier, introduced by __attribute__.
 *
 * @since 6.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
@SuppressWarnings("deprecation")
public interface IGCCASTAttributeList extends IASTAttributeList, IGCCASTAttributeSpecifier {
	public static InstanceOfPredicate<IASTAttributeSpecifier> TYPE_FILTER = new InstanceOfPredicate<>(
			IGCCASTAttributeList.class);
}
