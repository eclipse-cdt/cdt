/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.templateengine.TemplateInfo;
import org.eclipse.cdt.core.templateengine.TemplateInitializationException;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * TemplateEngine is implemented as a Singleton. TemplateEngine is responsible for 
 * creating SharedDefaults and initializing the SharedDefaults. Template instances
 * are obtained from TemplateEngine.
 * 
 * @since 4.0
 */
public class TemplateEngineUI {

	/**
	 * static reference to the Singleton TemplateEngine instance.
	 */
	private static final TemplateEngineUI TEMPLATE_ENGINE_UI = new TemplateEngineUI();

	private TemplateEngineUI() {
	}

	public static TemplateEngineUI getDefault() {
		return TEMPLATE_ENGINE_UI;
	}

	public Template[] getTemplates(String projectType, String toolChain, String usageFilter) {
		TemplateInfo[] templateInfoArray = TemplateEngine.getDefault().getTemplateInfos(projectType, toolChain, usageFilter);
		List<Template> templatesList = new ArrayList<Template>();
		for (int i=0; i<templateInfoArray.length; i++) {
			TemplateInfo info = templateInfoArray[i];
			try {
				templatesList.add(new Template(info));
			} catch (TemplateInitializationException tie) {
				CUIPlugin.log(tie);
			}
		}
		return templatesList.toArray(new Template[templatesList.size()]);
	}

	public Template[] getTemplates(String projectType, String toolChain) {
		return getTemplates(projectType, toolChain, null);
	}

	public Template[] getTemplates(String projectType) {
		return getTemplates(projectType, null, null);
	}

	/**
	 * Returns all the templates, no filtering is done.
	 */
	public Template[] getTemplates() {
		TemplateInfo[] templateInfoArray = TemplateEngine.getDefault().getTemplateInfos();
		List<Template> templatesList = new ArrayList<Template>();
		for (int i=0; i<templateInfoArray.length; i++) {
			try {
				templatesList.add(new Template(templateInfoArray[i]));
			} catch (TemplateInitializationException tie) {
				CUIPlugin.log(tie);
			}
		}
		return templatesList.toArray(new Template[templatesList.size()]);
	}

	public Template getTemplateById(String templateId) {
		Template[] templates = getTemplates();

		for(int i=0; i<templates.length; i++) {
			Template template = templates[i];
			if (template.getTemplateId().equalsIgnoreCase(templateId)) {
				return template;
			}
		}
		return null;
	}

}
