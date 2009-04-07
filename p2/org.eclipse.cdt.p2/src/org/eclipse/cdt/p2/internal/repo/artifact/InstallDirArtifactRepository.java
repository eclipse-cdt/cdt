package org.eclipse.cdt.p2.internal.repo.artifact;

import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactDescriptor;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRequest;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;
import org.eclipse.equinox.internal.provisional.spi.p2.artifact.repository.AbstractArtifactRepository;

public class InstallDirArtifactRepository extends AbstractArtifactRepository {

	public static String type = InstallDirArtifactRepository.class.getName();
	private static String version = "1.0.0";
	private static String description = "Artifact repository managing installed contents";
	private static String provider = "Eclipse";
	
	@SuppressWarnings("unchecked")
	public InstallDirArtifactRepository(String name, URI location, Map properties) {
		super(name, type, version, location, description, provider, properties);
	}

	@Override
	public boolean contains(IArtifactDescriptor descriptor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(IArtifactKey key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IStatus getArtifact(IArtifactDescriptor descriptor,
			OutputStream destination, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IArtifactDescriptor[] getArtifactDescriptors(IArtifactKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IArtifactKey[] getArtifactKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IStatus getArtifacts(IArtifactRequest[] requests,
			IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getOutputStream(IArtifactDescriptor descriptor)
			throws ProvisionException {
		// TODO Auto-generated method stub
		return null;
	}

	public IStatus getRawArtifact(IArtifactDescriptor descriptor,
			OutputStream destination, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

}
