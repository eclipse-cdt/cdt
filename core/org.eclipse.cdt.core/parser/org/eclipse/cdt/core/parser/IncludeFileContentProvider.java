/*******************************************************************************
 * Copyright (c) 2009, 2014 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.internal.core.parser.EmptyFilesProvider;
import org.eclipse.cdt.internal.core.parser.SavedFilesProvider;

/**
 * A file content provider is used to create file content objects for include
 * directives.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 5.2
 */
public abstract class IncludeFileContentProvider {
	/**
	 * Returns a provider that pretends that every include file is empty.
	 */
	public static IncludeFileContentProvider getEmptyFilesProvider() {
		return EmptyFilesProvider.getInstance();
	}

	/**
	 * Returns a provider for the content as saved in the file-system,
	 * without using a cache.
	 */
	public static IncludeFileContentProvider getSavedFilesProvider() {
		return new SavedFilesProvider();
	}

	/**
	 * @deprecated Provided to achieve backwards compatibility.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static IncludeFileContentProvider adapt(org.eclipse.cdt.core.dom.ICodeReaderFactory factory) {
		return org.eclipse.cdt.internal.core.parser.FileContentProviderAdapter.adapt(factory);
	}
}
