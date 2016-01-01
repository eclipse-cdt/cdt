/*******************************************************************************
 * Copyright (c) 2008, 2011 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public AbstractCProjectDescriptionStorage getProjectDescriptionStorage(CProjectDescriptionStorageTypeProxy type, IProject project, Version version) {
		return new XmlProjectDescriptionStorage(type, project, version);
	}

	@Override
	public boolean createsCProjectXMLFile() {
		return true;
	}
}
