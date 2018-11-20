/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.internal.core.dom.rewrite;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.TextFileChange;

/**
 * Factory to create CTextFileChanges. Allows for creating ui-dependent objects in the core plugin.
 * @since 5.0
 */
public interface ICTextFileChangeFactory {

	/**
	 * Creates a text file change for the given file.
	 */
	TextFileChange createCTextFileChange(IFile file);
}
