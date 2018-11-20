/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.settingswizards;

import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * The exported XML file is divided into 'section' elements, each
 * ISettingsProcessor is responsible for reading and writing
 * a particular section.
 *
 * A section can contain anything, for example include paths or macros.
 * No schema is defined for the XML file, that way additional settings
 * processors can be easily added. In the future there may be an extension
 * point for adding settings processors.
 *
 *
 * @author Mike Kucera
 * @since 5.1
 */
public interface ISettingsProcessor {

	/**
	 * Return the string that will appear in the selection list
	 * on the wizard page.
	 */
	String getDisplayName();

	/**
	 * Return the image that will appear in the selection list
	 * on the wizard page.
	 */
	Image getIcon();

	/**
	 * The name of the section in the XML file.
	 *
	 * This String should be unique, so prefix it with the package
	 * name or something.
	 */
	String getSectionName();

	/**
	 * Outputs a section of the XML file using the given SAX ContentHandler.
	 *
	 * @param projectRoot The folder description for the selected project and configuration
	 * @throws SettingsImportExportException if the section could not be written
	 */
	void writeSectionXML(ICFolderDescription projectRoot, ContentHandler content) throws SettingsImportExportException;

	/**
	 * Passed part of the DOM tree that represents the section
	 * that is processed by this importer.
	 *
	 * @param projectRoot The folder description for the selected project and configuration
	 * @throws SettingsImportExportException if the section could not be read and imported
	 */
	void readSectionXML(ICFolderDescription projectRoot, Element section) throws SettingsImportExportException;
}
