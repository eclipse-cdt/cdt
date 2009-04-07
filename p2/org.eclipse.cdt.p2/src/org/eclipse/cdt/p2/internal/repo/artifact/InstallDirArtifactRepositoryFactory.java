package org.eclipse.cdt.p2.internal.repo.artifact;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.spi.p2.artifact.repository.ArtifactRepositoryFactory;

public class InstallDirArtifactRepositoryFactory extends
		ArtifactRepositoryFactory {

	@SuppressWarnings("unchecked")
	@Override
	public IArtifactRepository create(URI location, String name, String type, Map properties) throws ProvisionException {
		if (InstallDirArtifactRepository.type.equals(type))
			return new InstallDirArtifactRepository(name, location, properties);
		else 
			return null;
	}

	@Override
	public IArtifactRepository load(URI location, int flags, IProgressMonitor monitor) throws ProvisionException {
		return null;
	}

}
