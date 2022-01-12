/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * Extension of IChecker that works with C-Index of a file (but not AST)
 * Default implementation {@link AbstractCIndexChecker}
 *
 * Client may implement this interface.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same.
 * </p>
 */
public interface ICIndexChecker extends IChecker {
	/**
	 * Run checker on translation unit
	 *
	 * @param unit - translation unit
	 */
	void processUnit(ITranslationUnit unit);
}
