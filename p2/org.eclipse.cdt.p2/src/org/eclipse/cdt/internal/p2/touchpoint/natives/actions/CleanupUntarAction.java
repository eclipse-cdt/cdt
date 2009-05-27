/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.p2.touchpoint.natives.actions;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.touchpoint.natives.actions.CleanupzipAction;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningAction;

/**
 * Cleanup an untared artifact.
 * 
 * syntax: cleanupuntar(source:@artifact, target:${installFolder}/<subdir>, compression:[gz|bz2])
 * 
 * @author DSchaefe
 *
 */
public class CleanupUntarAction extends ProvisioningAction {

	@Override
	public IStatus execute(Map parameters) {
		return cleanup(parameters);
	}

	@Override
	public IStatus undo(Map parameters) {
		return UntarAction.untar(parameters);
	}

	public static IStatus cleanup(Map parameters) {
		return CleanupzipAction.cleanupzip(parameters, false);
	}
}
