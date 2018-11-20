/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IProblemBinding;

/**
 * Interface for problem bindings created to avoid infinite recursion.
 */
public interface IRecursionResolvingBinding extends IProblemBinding {
}
