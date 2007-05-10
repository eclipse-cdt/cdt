/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine;

import java.util.Set;



/**
 * TemplateInfo class contains the template information like wizard ID, pattern, path and project type.
 */
public class TemplateInfo {
	private String projectTypeId;
	private String filterPattern;
	private String usageDescription;
	private String templatePath;
	private String pluginId;
	private Set toolChainIdSet;
	private String pagesProvider;
	private boolean isCategory;
	private String icon;
	private String templateId;
	
	public TemplateInfo(String templateId, String projectTypeId, String filterPattern, String templatePath, 
			String pluginId, Set toolChainIdSet, String usageDescription, 
			String pagesProvider, boolean isCategory) {
		this.templateId = templateId;
		this.filterPattern = filterPattern;
		this.templatePath = templatePath;
		this.pluginId = pluginId;
		this.projectTypeId = projectTypeId;
		this.toolChainIdSet = toolChainIdSet;
		this.usageDescription = usageDescription != null ? usageDescription : ""; //$NON-NLS-1$
		this.pagesProvider = pagesProvider;
		this.isCategory = isCategory;
	}

	/**
	 * Returns the Plugin ID
	 * @return   String contains the plugin id.
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * Returns the Template ID
	 * @return   String contains the template id.
	 */
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * Returns the Template path as String.
	 * @return   String containing the path.
	 */
	public String getTemplatePath() {
		return templatePath;
	}

	/**
	 * Returns the Filter Pattern.
	 * @return   String containing the Filter Pattern.
	 */
	public String getFilterPattern() {
		return filterPattern;
	}

	/**
	 * @return   the usageDescription
	 */
	public String getUsageDescription() {
		return usageDescription;
	}
	
	public String getExtraPagesProvider() {
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
		return (String[]) toolChainIdSet.toArray(new String[toolChainIdSet.size()]);
	}

	public void setToolChainSet(Set toolChainIdSet) {
		this.toolChainIdSet = toolChainIdSet;
	}
	
	/**
	 * @return the isCategory
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

	/**
	 * Return the hashcode of the object.
	 */
	public int hashCode() {
		return projectTypeId.hashCode() | templatePath.hashCode() | pluginId.hashCode();
	}

}
