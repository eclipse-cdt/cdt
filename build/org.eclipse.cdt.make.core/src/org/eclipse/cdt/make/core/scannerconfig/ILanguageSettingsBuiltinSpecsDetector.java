/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.core.scannerconfig;

import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ILanguageSettingsBuiltinSpecsDetector extends ILanguageSettingsProvider {
	public List<String> getLanguageScope();
	public void run(IProject project, String languageId, IPath workingDirectory, String[] env, IProgressMonitor monitor) throws CoreException, IOException;
	public void run(ICConfigurationDescription cfgDescription, String languageId, IPath workingDirectory, String[] env, IProgressMonitor monitor) throws CoreException, IOException;
}
