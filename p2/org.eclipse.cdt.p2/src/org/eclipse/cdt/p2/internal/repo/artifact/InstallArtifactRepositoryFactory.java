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

package org.eclipse.cdt.p2.internal.repo.artifact;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.eclipse.cdt.p2.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.spi.p2.artifact.repository.ArtifactRepositoryFactory;
import org.eclipse.osgi.util.NLS;

/**
 * @author DSchaefe
 *
 */
public class InstallArtifactRepositoryFactory extends ArtifactRepositoryFactory {

	public IArtifactRepository create(URI location, String name, String type, Map properties) throws ProvisionException {
		return new InstallArtifactRepository(location, name, properties);
	}
	
	public IArtifactRepository load(URI location, IProgressMonitor monitor) throws ProvisionException {
		File localFile = null;
		boolean local = false;
		try {
			localFile = new File(InstallArtifactRepository.getActualLocation(location).getPath());
			InputStream descriptorStream = null;
			try {
				descriptorStream = new BufferedInputStream(new FileInputStream(localFile));
				InstallArtifactRepositoryIO io = new InstallArtifactRepositoryIO();
				return io.read(localFile.toURI(), descriptorStream);
			} finally {
				if (descriptorStream != null)
					descriptorStream.close();
			}
		} catch (FileNotFoundException e) {
			String msg = NLS.bind(Messages.io_failedRead, location);
			throw new ProvisionException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ProvisionException.REPOSITORY_NOT_FOUND, msg, e));
		} catch (IOException e) {
			String msg = NLS.bind(Messages.io_failedRead, location);
			throw new ProvisionException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ProvisionException.REPOSITORY_FAILED_READ, msg, e));
		} finally {
			// TODO why is this here?
			if (!local && localFile != null && !localFile.delete())
				localFile.deleteOnExit();
		}
	}

}
