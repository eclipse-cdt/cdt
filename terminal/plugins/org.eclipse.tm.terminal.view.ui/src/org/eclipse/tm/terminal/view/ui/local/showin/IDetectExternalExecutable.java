/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.local.showin;

import java.util.List;
import java.util.Map;

import org.eclipse.tm.terminal.view.ui.interfaces.IExternalExecutablesProperties;

public interface IDetectExternalExecutable {
	/**
	 * Detect if {@link #getEntries(List)} will return a non-empty list of entries, assuming externalEntries is an empty
	 * list. This method is used during critical UI times (such as startup) and should be very fast.
	 */
	boolean hasEntries();

	/**
	 * Detect any additional external executables that can be added to the Show In list.
	 *
	 * This method is sometimes called in the UI thread when displaying context menus, so should
	 * either be very fast, or it should use a flag to not re-run multiple times after the initial detection.
	 *
	 * The same instance of the {@link IDetectExternalExecutable} will be used on each invocation of this method.
	 *
	 * @param externalExecutables is the list of executables already present that can be used to prevent duplicate
	 * entries. This list should not be modified.
	 * @return a list of additional items to add to the external executables list. Each map entry should have keys
	 * that match {@link IExternalExecutablesProperties}. Must not return <code>null</code>, return
	 * an empty list instead.
	 */
	List<Map<String, String>> getEntries(List<Map<String, String>> externalExecutables);

}
