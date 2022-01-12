/*******************************************************************************
 * Copyright (c) 2011 Google Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * Utility methods related to <code>{@link IChecker}</code>.
 *
 * @since 2.0
 */
public final class Checkers {

	/**
	 * Indicates whether the given checker can "run as you type."
	 * @param checker the checker to verify.
	 * @return {@code true} if the given checker can "run as you type"; {@code false} otherwise.
	 * @see IChecker#runInEditor()
	 */
	public static boolean canCheckerRunAsYouType(IChecker checker) {
		return checker.runInEditor() && checker instanceof IRunnableInEditorChecker;
	}

	private Checkers() {
	}
}
