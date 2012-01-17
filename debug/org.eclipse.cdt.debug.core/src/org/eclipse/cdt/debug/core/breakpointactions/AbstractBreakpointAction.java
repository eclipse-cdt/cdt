/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
