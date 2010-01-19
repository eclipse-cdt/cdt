/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
