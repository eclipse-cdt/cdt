/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.p2.internal.touchpoint;

import java.util.Map;

import org.eclipse.cdt.p2.internal.repo.artifact.InstallArtifactRepository;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningAction;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;

/**
 * @author DSchaefe
 *
 */
public class UninstallAction extends ProvisioningAction {

	public static final String ACTION_NAME = "uninstall";

	@Override
	public IStatus execute(Map parameters) {
		IProfile profile = (IProfile)parameters.get("profile");
		IArtifactKey artifact = (IArtifactKey)parameters.get("artifact");
		
		try {
			IArtifactRepository repo = InstallArtifactRepository.getRepository(profile);
			repo.removeDescriptor(artifact);
		} catch (ProvisionException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(Map parameters) {
		return Status.OK_STATUS;
	}

}
