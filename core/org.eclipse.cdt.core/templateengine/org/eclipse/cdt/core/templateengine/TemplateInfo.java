/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine;

import java.util.List;
import java.util.Set;

/**
 * TemplateInfo class contains the template information like wizard ID, pattern, path and project type.
 */
public class TemplateInfo {
	private String projectTypeId;
	private String filterPattern;
	private String templatePath;
	private String pluginId;
	private Set<String> toolChainIdSet;
	private Object pagesProvider; /*IPagesAfterTemplateSelectionProvider*/
	private boolean isCategory;
	private String icon;
	private String templateId;
	private List<?> configs; /*IConfiguration This seems to be used for storing build-system specific configurations*/
	
	/**
	 * 
	 * @param templateId
	 * @param projectTypeId
	 * @param filterPattern
	 * @param templatePath
	 * @param pluginId
	 * @param toolChainIdSet
	 * @param extraPagesProvider an IPagesAfterTemplateSelectionProvider or null
	 * @param isCategory
	 */
	public TemplateInfo(String templateId, String projectTypeId, String filterPattern, String templatePath, 
			String pluginId, Set<String> toolChainIdSet, 
			Object extraPagesProvider, boolean isCategory) {
		this.templateId = templateId;
		this.filterPattern = filterPattern;
		this.templatePath = templatePath;
		this.pluginId = pluginId;
		this.projectTypeId = projectTypeId;
		this.toolChainIdSet = toolChainIdSet;
		this.pagesProvider = extraPagesProvider;
		this.isCategory = isCategory;
		this.configs = null;
	}

	/**
	 * @return the plug-in id
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * @return the template id.
	 */
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * @return the template path
	 */
	public String getTemplatePath() {
		return templatePath;
	}

	/**
	 * @return the filter Pattern.
	 */
	public String getFilterPattern() {
		return filterPattern;
	}
	
	/**
	 * @return an IPagesAfterTemplateSelectionProvider or null
	 */
	public Object getExtraPagesProvider() {
		return pagesProvider;
	}
	
	/**
	 * @return the projectTypeIds
	 */
	public String getProjectType() {
		return projectTypeId;
	}

	/**
	 * @return the toolChainIds
	 */
	public String[] getToolChainIds() {
		return toolChainIdSet.toArray(new String[toolChainIdSet.size()]);
	}

	public void setToolChainSet(Set<String> toolChainIdSet) {
		this.toolChainIdSet = toolChainIdSet;
	}
	
	public List<?/*IConfiguration*/> getConfigurations() {
		return configs;
	}
	
	public void setConfigurations(List<?/*IConfiguration*/> configs) {
		this.configs = configs;
	}
	
	/**
	 * @return whether this template is a category
	 */
	public boolean isCategory() {
		return isCategory;
	}

	/**
	 * @return the icon image file name
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Checks whether two TemplateInfo object are equal. 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TemplateInfo) {
			TemplateInfo info = (TemplateInfo) obj;
			return projectTypeId.equals(info.projectTypeId) && templatePath.equals(info.templatePath) && pluginId.equals(info.pluginId)
				&& (((filterPattern == null || info.filterPattern == null) && filterPattern == info.filterPattern)
						|| filterPattern.equals(info.filterPattern))
				&& ((toolChainIdSet.equals(info.toolChainIdSet)))
				&& ((isCategory == info.isCategory));
		}
		return false;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return projectTypeId.hashCode() | templatePath.hashCode() | pluginId.hashCode();
	}
}
