/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.runtime.MultiStatus;

/**
 *
 */
public class BuildMultiStatus extends MultiStatus {

	public BuildMultiStatus(String message, Throwable exception) {
		super(ManagedBuilderCorePlugin.getUniqueIdentifier(), 0, message, exception);
	}

}
