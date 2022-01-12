/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public interface ICFolderDescription extends ICResourceDescription {
	ICResourceDescription getNestedResourceDescription(IPath relPath, boolean exactPath);

	ICResourceDescription[] getNestedResourceDescriptions(int kind);

	ICResourceDescription[] getNestedResourceDescriptions();

	ICLanguageSetting[] getLanguageSettings();

	ICLanguageSetting getLanguageSettingForFile(String fileName);

	ICLanguageSetting createLanguageSettingForContentTypes(String languageId, String cTypeIds[]) throws CoreException;

	ICLanguageSetting createLanguageSettingForExtensions(String languageId, String extensions[]) throws CoreException;

	boolean isRoot();
}
