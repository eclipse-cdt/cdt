/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.core.internal.target;

import org.eclipse.launchbar.core.internal.Messages;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;

public class LocalLaunchTargetProvider implements ILaunchTargetProvider {

	@Override
	public void init(ILaunchTargetManager targetManager) {
		if (targetManager.getLaunchTarget(ILaunchTargetManager.localLaunchTargetTypeId,
				Messages.LocalTarget_name) == null) {
			targetManager.addLaunchTarget(ILaunchTargetManager.localLaunchTargetTypeId, Messages.LocalTarget_name,
					Messages.LocalTarget_name);
		}
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		return TargetStatus.OK_STATUS;
	}

}
