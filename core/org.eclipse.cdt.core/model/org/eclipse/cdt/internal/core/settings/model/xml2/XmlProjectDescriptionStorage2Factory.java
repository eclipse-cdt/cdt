/*******************************************************************************
 * Copyright (c) 2008, 2009 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.settings.model.xml2;

import org.eclipse.cdt.internal.core.settings.model.AbstractCProjectDescriptionStorage;
import org.eclipse.cdt.internal.core.settings.model.ICProjectDescriptionStorageType;
import org.eclipse.core.resources.IProject;
import org.osgi.framework.Version;

/**
 * Concrete implementation of ICProjectDescriptionStorageType
 * for instantiating XmlProjectDescriptionStorage2
 */
public class XmlProjectDescriptionStorage2Factory implements ICProjectDescriptionStorageType {

	public AbstractCProjectDescriptionStorage getProjectDescriptionStorage(CProjectDescriptionStorageTypeProxy type, IProject project, Version version) {
		return new XmlProjectDescriptionStorage2(type, project, version);
	}

	public boolean createsCProjectXMLFile() {
		return true;
	}

}
