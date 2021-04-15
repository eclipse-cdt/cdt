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
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.projectconverter.UpdateManagedProjectManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IOverwriteQuery;

/**
 * Use {@link #isAvailable()} to determine if references to the optional bundle
 * dependencies are available in this session.
 *
 * @see UpdateManagedProjectManager
 */
public enum UIBundlesAvailable {
	INSTANCE;

	private boolean available;

	private UIBundlesAvailable() {
		try {
			// The classes here are example classes from the required bundles.
			Class.forName(IOverwriteQuery.class.getName());
			Class.forName(Shell.class.getName());
			available = true;
		} catch (Throwable t) {
			available = false;
		}
	}

	public boolean isAvailable() {
		return available;
	}
}
