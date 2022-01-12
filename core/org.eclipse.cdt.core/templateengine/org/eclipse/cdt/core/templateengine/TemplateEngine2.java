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
		return (TemplateEngine2) TemplateEngine.getDefault();
	}

	public TemplateCategory getCategory(String id) {
		return categoryMap.get(id);
	}

}
