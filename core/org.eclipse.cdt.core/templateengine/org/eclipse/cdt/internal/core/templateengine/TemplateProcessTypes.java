/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.templateengine;

import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

public final class TemplateProcessTypes {

	private final String id = "org.eclipse.cdt.core.templateProcessTypes"; //$NON-NLS-1$

	public IExtensionPoint getExtensionPoint() {
		return Platform.getExtensionRegistry().getExtensionPoint(id);
	}

}
