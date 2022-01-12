/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.indexer;

import org.eclipse.cdt.core.model.ILanguage;

/**
 * This mapper can be used for determining the ILanguage for a particular file.
 *
 * A mapper is needed for standalone indexing when the ILanguage for a file is unknown.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 *
 * @since 4.0
 */
public interface ILanguageMapper {

	/**
	 * Returns the language of a file.
	 * @param file - path of the file
	 * @return the ILanguage of the file
	 */
	ILanguage getLanguage(String file);
}
