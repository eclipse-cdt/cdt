/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.core.launch;

import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;

/**
 * 
 * @since 8.3
 */
public class GenericTargetTypeProvider implements ILaunchTargetProvider {

	public static final String TYPE_ID = "org.eclipse.cdt.launchTargetType.generic"; //$NON-NLS-1$

	@Override
	public void init(ILaunchTargetManager targetManager) {
		// Nothing to do
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		// Always OK
		return TargetStatus.OK_STATUS;
	}

}
