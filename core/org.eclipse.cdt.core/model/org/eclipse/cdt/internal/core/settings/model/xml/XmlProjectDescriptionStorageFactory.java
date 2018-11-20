/*******************************************************************************
 * Copyright (c) 2008, 2011 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model.xml;

import org.eclipse.cdt.internal.core.settings.model.AbstractCProjectDescriptionStorage;
import org.eclipse.cdt.internal.core.settings.model.ICProjectDescriptionStorageType;
import org.eclipse.core.resources.IProject;
import org.osgi.framework.Version;

/**
 * Concrete implementation of ICProjectDescriptionStorageType
 * for instantiating XmlProjectDescriptionStorage
 */
public class XmlProjectDescriptionStorageFactory implements ICProjectDescriptionStorageType {

	@Override
	public AbstractCProjectDescriptionStorage getProjectDescriptionStorage(CProjectDescriptionStorageTypeProxy type,
			IProject project, Version version) {
		return new XmlProjectDescriptionStorage(type, project, version);
	}

	@Override
	public boolean createsCProjectXMLFile() {
		return true;
	}
}
