/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.p2.touchpoint.natives.actions;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.internal.p2.Activator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.engine.Profile;
import org.eclipse.equinox.internal.p2.touchpoint.natives.Messages;
import org.eclipse.equinox.internal.p2.touchpoint.natives.Util;
import org.eclipse.equinox.internal.p2.touchpoint.natives.actions.ActionConstants;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.osgi.util.NLS;

/**
 * Cleanup an untared artifact.
 * 
 * syntax: cleanupuntar(source:@artifact, target:${installFolder}/<subdir>, compression:[gz|bz2])
 * 
 * @author DSchaefe
 *
 */
public class CleanupUntarAction extends ProvisioningAction {

	private static final String ACTION_NAME = "cleanupuntar";

	@Override
	public IStatus execute(Map parameters) {
		try {
			return cleanup(parameters);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public IStatus undo(Map parameters) {
		try {
			return UntarAction.untar(parameters);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
	}

	public static IStatus cleanup(Map parameters) {
		String source = (String)parameters.get(ActionConstants.PARM_SOURCE);
		if (source == null)
			return Util.createError(NLS.bind(Messages.param_not_set, ActionConstants.PARM_SOURCE, ACTION_NAME));

		String target = (String)parameters.get(ActionConstants.PARM_TARGET);
		if (target == null)
			return Util.createError(NLS.bind(Messages.param_not_set, ActionConstants.PARM_TARGET, ACTION_NAME));
		
		IInstallableUnit iu = (IInstallableUnit) parameters.get(ActionConstants.PARM_IU);
		Profile profile = (Profile) parameters.get(ActionConstants.PARM_PROFILE);
		
		String fileList = profile.getInstallableUnitProperty(iu, "unzipped" + ActionConstants.PIPE + source + ActionConstants.PIPE + target);
		StringTokenizer tokenizer = new StringTokenizer(fileList, ActionConstants.PIPE);
		while (tokenizer.hasMoreTokens()) {
			String filename = tokenizer.nextToken();
			for (File file = new File(filename); file.delete(); file = file.getParentFile());
		}
		
		return Status.OK_STATUS;
	}
}
