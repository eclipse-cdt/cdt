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

import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.p2.internal.repo.artifact.InstallArtifactRepository;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.artifact.repository.MirrorRequest;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRequest;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.InstallableUnitOperand;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningAction;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;

/**
 * @author DSchaefe
 *
 */
public class CollectAction extends ProvisioningAction {

	public static final String ACTION_NAME = "collect";
	
	@Override
	public IStatus execute(Map parameters) {
		try {
			InstallableUnitOperand operand = (InstallableUnitOperand)parameters.get("operand");
			IInstallableUnit installableUnit = operand.second();
			IProfile profile = (IProfile)parameters.get("profile"); //$NON-NLS-1$
			
			IArtifactRequest[] requests;
			IArtifactKey[] toDownload = installableUnit.getArtifacts();
			if (toDownload == null || toDownload.length == 0)
				requests = new IArtifactRequest[0];
			else {
				IArtifactRepository destination = InstallArtifactRepository.getRepository(profile);
				requests = new IArtifactRequest[toDownload.length];
				for (int i = 0; i < toDownload.length; i++)
					requests[i] = new MirrorRequest(toDownload[i], destination, null, null);
			}
			
			Collection artifactRequests = (Collection)parameters.get("artifactRequests");
			artifactRequests.add(requests);
			return Status.OK_STATUS;
		} catch (ProvisionException e) {
			return e.getStatus();
		}
	}

	@Override
	public IStatus undo(Map parameters) {
		// No undo
		return Status.OK_STATUS;
	}

}
