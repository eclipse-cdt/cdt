/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.templateengine;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateDescriptor;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
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

import junit.framework.Assert;

/**
 * All supporting functions which are not part of Testing class.
 *
 * @since 4.0
*/
public class TemplateEngineTestsHelper {
	public static final String LOGGER_FILE_NAME = "TemplateEngineTests"; //$NON-NLS-1$

	/**
	 * Returns the url of a xml template, by passing the xml file name.
	 * @param templateName
	 * @return URL
	 */
	public static URL getTemplateURL(String templateName) {
		Bundle bundle = Platform.getBundle(CTestPlugin.PLUGIN_ID);
		URL url = FileLocator.find(bundle, new Path("resources/templateengine/" + templateName), null); //$NON-NLS-1$
		if (url != null) {
			try {
				url = FileLocator.toFileURL(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return url;
	}

	public static TemplateCore[] getTestTemplates() {
		TemplateCore[] templates = TemplateEngine.getDefault().getTemplates();
		List<TemplateCore> testTemplates = new ArrayList<>();
		for (int i = 0; i < templates.length; i++) {
			if (templates[i].getTemplateType().equals("TestTemplate")) {
				testTemplates.add(templates[i]);
			}
		}
		return testTemplates.toArray(new TemplateCore[testTemplates.size()]);
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
				Assert.fail(status.getMessage());
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
}
