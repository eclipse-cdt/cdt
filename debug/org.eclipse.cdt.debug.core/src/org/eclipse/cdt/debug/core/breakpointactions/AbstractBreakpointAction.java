/*******************************************************************************
 * Copyright (c) 2007, 2012 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.breakpointactions;

import org.eclipse.core.runtime.PlatformObject;

public abstract class AbstractBreakpointAction extends PlatformObject implements IBreakpointAction {

	private String actionName;

	@Override
	public String getName() {
		return actionName;
	}

	@Override
	public void setName(String name) {
		actionName = name;
	}

}
