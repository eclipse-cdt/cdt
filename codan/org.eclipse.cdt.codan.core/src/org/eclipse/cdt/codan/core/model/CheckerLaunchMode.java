/*******************************************************************************
 * Copyright (c) 2011, 2012 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Alex Ruiz (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * CheckerLaunchMode - how checker can be run
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same.
 * </p>
 *
 * @since 2.0
 */
public enum CheckerLaunchMode {
	/**
	 * Checker runs when full build is running.
	 */
	RUN_ON_FULL_BUILD,
	/**
	 * Checker runs when incremental build is running.
	 */
	RUN_ON_INC_BUILD,
	/**
	 * Checker runs when a file is opened.
	 * @since 2.1
	 */
	RUN_ON_FILE_OPEN,
	/**
	 * Checker runs when a file is saved. Checker will not run if the file is an editor with unsaved
	 * changes.
	 * @since 2.1
	 */
	RUN_ON_FILE_SAVE,
	/**
	 * Checker runs in editor as you type.
	 */
	RUN_AS_YOU_TYPE,
	/**
	 * Checker runs when explicit command is given.
	 */
	RUN_ON_DEMAND,
}