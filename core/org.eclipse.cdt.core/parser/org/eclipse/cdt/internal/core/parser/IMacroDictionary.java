/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.ISignificantMacros;

/**
 * Interface for accessing the macro dictionary of the preprocessor.
 */
public interface IMacroDictionary {

	boolean satisfies(ISignificantMacros significantMacros);
}
