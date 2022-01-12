/*******************************************************************************
 * Copyright (c) 2009, 2014 Zeligsoft Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Represents a workbench object that is able to provide instances of ITranslationUnit.  For
 * example, the CEditor (in the CDT UI plugin) implements this interface in order to provide
 * the IWorkingCopy of the editor's active translation unit.
 *
 * @since 5.7
 */
public interface ITranslationUnitHolder {
	/**
	 * Returns the translation unit that is provided by the receiver or null if there is no
	 * such translation unit.
	 */
	public ITranslationUnit getTranslationUnit();
}
