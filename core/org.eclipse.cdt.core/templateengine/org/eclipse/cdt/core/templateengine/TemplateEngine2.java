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

/**
 * @author Doug Schaefer
 * @since 5.4
 */
public class TemplateEngine2 extends TemplateEngine {

	/**
	 * Project type for new templates. Default if not set in extension.
	 */
	public static final String NEW_TEMPLATE = "newTemplate"; //$NON-NLS-1$
	
	public static TemplateEngine2 getDefault() {
		return (TemplateEngine2)TemplateEngine.getDefault();
	}

	public TemplateCategory getCategory(String id) {
		return categoryMap.get(id);
	}
	
}
