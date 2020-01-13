/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tools.templates.core;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface used by the Template Wizard to call on the generator to generate. Also provides other
 * utility methods as necessary, but that should be limited.
 */
public interface IGenerator {

	/**
	 * Generate.
	 *
	 * @param model
	 * @param monitor
	 * @throws CoreException
	 * @deprecated The generator should manage it's own model.
	 */
	@Deprecated
	default void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		if (model.isEmpty()) {
			generate(monitor);
		}
	}

	/**
	 * Generate.
	 *
	 * @param monitor
	 * @throws CoreException
	 */
	default void generate(IProgressMonitor monitor) throws CoreException {
	}

	/**
	 * Return which files should be opened in the workbench when the generation is complete.
	 *
	 * @return files to open
	 */
	default IFile[] getFilesToOpen() {
		return new IFile[0];
	}

}
