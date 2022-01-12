/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;

/**
 * A template context for C/C++ code and comment.
 *
 * @since 5.0
 */
public class CodeTemplateContext extends FileTemplateContext {
	private ICProject fProject;

	public CodeTemplateContext(String contextTypeId, ICProject project, String lineDelimiter) {
		super(contextTypeId, lineDelimiter);
		fProject = project;
	}

	public ICProject getCProject() {
		return fProject;
	}

	public void setTranslationUnitVariables(ITranslationUnit tu) {
		IFile file = (IFile) tu.getResource();
		if (file != null) {
			super.setResourceVariables(file);
		}
	}
}
