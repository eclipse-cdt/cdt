/*******************************************************************************
 * Copyright (c) 2010, 2016 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.templateengine.tests;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.templateengine.TemplateDescriptor;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import junit.framework.TestCase;

/**
 *
 * All supporting functions which are not part of Testing class.
 *
 * @since 4.0
*/

public class TemplateEngineTestsHelper {

	public static final String LOGGER_FILE_NAME = "TemplateEngineTests"; //$NON-NLS-1$
	private static List<IProjectType> projectTypes = new ArrayList<>();
	private static List<String> projectTypeNames = new ArrayList<>();

	/**
	 * get the url of a xml template, by passing the xml file name.
	 * @param templateName
	 * @return URL
	 */
	public static URL getTemplateURL(String templateName) {
		Bundle bundle = Platform.getBundle("org.eclipse.cdt.managedbuilder.core.tests"); //$NON-NLS-1$
		URL url = FileLocator.find(bundle, new Path("testdata/" + templateName), null); //$NON-NLS-1$
		if (url != null) {
			try {
				url = FileLocator.toFileURL(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return url;
	}

	public static int getChildCount(TemplateDescriptor templateDescriptor, String propertyGroupID) {
		List<Element> list = templateDescriptor.getPropertyGroupList();
		for (int i = 0, l = list.size(); i < l; i++) {
			Element element = list.get(i);
			NamedNodeMap attributes = element.getAttributes();
			for (int j = 0, l1 = attributes.getLength(); j < l1; j++) {
				String value = attributes.item(j).getNodeValue();
				if (value.equals(propertyGroupID)) {
					return TemplateEngine.getChildrenOfElement(element).size();
				}
			}
		}
		return 0;
	}

	public static boolean failIfErrorStatus(IStatus[] statuses) {
		for (int i = 0; i < statuses.length; i++) {
			IStatus status = statuses[i];
			if (status.getCode() == IStatus.ERROR) {
				TestCase.fail(status.getMessage());
				return true;
			}
			IStatus[] children = status.getChildren();
			if (children != null) {
				if (failIfErrorStatus(children)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void turnOffAutoBuild() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		workspaceDesc.setAutoBuilding(false);
		workspace.setDescription(workspaceDesc);
	}

	public static List<IProjectType> getProjectTypes() {
		populateProjectTypes();
		return projectTypes;
	}

	public static List<String> getProjectTypeNames() {
		populateProjectTypes();
		return projectTypeNames;
	}

	/* (non-Javadoc)
	 * Collects all the valid project types for the platform Eclipse is running on
	 * Note: This method is a copy of populateTypes() from org.eclipse.cdt.managedbuilder.ui.wizards.CProjectPlatformPage class.
	 */
	private static void populateProjectTypes() {
		IProjectType[] allProjectTypes = ManagedBuildManager.getDefinedProjectTypes();
		String os = Platform.getOS();
		String arch = Platform.getOSArch();

		for (int index = 0; index < allProjectTypes.length; ++index) {
			IProjectType type = allProjectTypes[index];
			if (!type.isAbstract() && !type.isTestProjectType()) {

				if (!type.getConvertToId().isEmpty())
					continue;

				if (type.isSupported()) {
					IConfiguration[] configs = type.getConfigurations();
					for (int j = 0; j < configs.length; ++j) {
						IToolChain tc = configs[j].getToolChain();
						List<String> osList = Arrays.asList(tc.getOSList());
						if (osList.contains("all") || osList.contains(os)) { //$NON-NLS-1$
							List<String> archList = Arrays.asList(tc.getArchList());
							if (archList.contains("all") || archList.contains(arch)) { //$NON-NLS-1$
								projectTypes.add(type);
								break;
							}
						}
					}
				}
			}
		}

		for (Iterator<IProjectType> iter = projectTypes.iterator(); iter.hasNext();) {
			projectTypeNames.add(iter.next().getName());
		}
	}

}
