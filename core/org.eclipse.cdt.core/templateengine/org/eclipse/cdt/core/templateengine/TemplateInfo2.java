/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.templateengine;

import java.util.List;
import java.util.Set;

/**
 * Template info extended to include new stuff for new new project wizard UI.
 *
 * @author Doug Schaefer
 * @since 5.4
 */
public class TemplateInfo2 extends TemplateInfo {

	private List<String> parentCategoryIds;

	public TemplateInfo2(String templateId, String projectTypeId, String filterPattern, String templatePath,
			String pluginId, Set<String> toolChainIdSet, Object extraPagesProvider, boolean isCategory,
			List<String> parentCategoryIds) {
		super(templateId, projectTypeId, filterPattern, templatePath, pluginId, toolChainIdSet, extraPagesProvider,
				isCategory);
		this.parentCategoryIds = parentCategoryIds;
	}

	public List<String> getParentCategoryIds() {
		return parentCategoryIds;
	}

}
