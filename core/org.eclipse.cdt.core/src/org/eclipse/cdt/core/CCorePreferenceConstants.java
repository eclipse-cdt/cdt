/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;


public class CCorePreferenceConstants {

	/**
	 * Possible configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String TRANSLATION_TASK_TAGS = CCorePlugin.PLUGIN_ID + ".translation.taskTags"; //$NON-NLS-1$

	/**
	 * Default task tag
	 */
	public static final String DEFAULT_TASK_TAG = "TODO"; //$NON-NLS-1$
	
	/**
	 * Default task priority
	 */
	public static final String DEFAULT_TASK_PRIORITY = CCorePlugin.TRANSLATION_TASK_PRIORITY_NORMAL;
	/**
	 * Possible configurable option ID.
	 * @see #getDefaultOptions
	 */
	public static final String TRANSLATION_TASK_PRIORITIES = CCorePlugin.PLUGIN_ID + ".translation.taskPriorities"; //$NON-NLS-1$

	/**
	 * Active code formatter ID.
	 * @see #getDefaultOptions
	 */
	public static final String CODE_FORMATTER = CCorePlugin.PLUGIN_ID + ".code_formatter"; //$NON-NLS-1$
	
	/**
	 * Default code formatter
	 */
	public static final String DEFAULT_CODE_FORMATTER = CCorePlugin.PLUGIN_ID + ".defaultCodeFormatter"; //$NON-NLS-1$
	
}
