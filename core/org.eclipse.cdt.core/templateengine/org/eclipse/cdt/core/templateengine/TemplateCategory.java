/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
