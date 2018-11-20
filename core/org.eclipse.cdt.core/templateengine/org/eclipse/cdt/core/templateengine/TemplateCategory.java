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

/**
 * @author Doug Schaefer
 * @since 5.4
 */
public class TemplateCategory {

	private final String id;
	private String label;
	private List<String> parentCategoryIds;

	public TemplateCategory(String id, String label, List<String> parentCategoryIds) {
		this.id = id;
		this.label = label;
		this.parentCategoryIds = parentCategoryIds;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public List<String> getParentCategoryIds() {
		return parentCategoryIds;
	}

}
