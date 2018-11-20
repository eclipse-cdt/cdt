/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Listens to changes in language mappings.
 *
 * @author crecoskie
 * @since 4.0
 */
public interface ILanguageMappingChangeListener {

	/**
	 * Indicates that language mappings have been changed.
	 * @param event
	 */
	public void handleLanguageMappingChangeEvent(ILanguageMappingChangeEvent event);

}
