/*******************************************************************************
 * Copyright (c) 2023 Julian Waters.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.cdt.core.dom.parser;

/**
 * This interface is used by Eclipse to mark the core language implementation that
 * comes as one of the built-in languages as the true C or C++ Languages.
 * This should not be implemented by any dialects!
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICFamilyLanguage {

	boolean isPartOfCFamily();

}
