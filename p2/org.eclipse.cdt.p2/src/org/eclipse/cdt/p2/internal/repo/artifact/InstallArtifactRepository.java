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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.p2.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.core.helpers.URLUtil;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.ArtifactDescriptor;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactDescriptor;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRequest;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.core.location.AgentLocation;
import org.eclipse.equinox.internal.provisional.p2.core.repository.IRepository;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;
import org.eclipse.equinox.internal.provisional.spi.p2.artifact.repository.AbstractArtifactRepository;

/**
 * @author DSchaefe
 *
 */
public class InstallArtifactRepository extends AbstractArtifactRepository {

	// Install directory property
	public static final String INSTALL_DIR = "installDir"; //$NON-NLS-1$
	public static final String SUB_DIR = "subdir"; //$NON-NLS-1$
	public static final String FILENAME = "installArtifact.xml"; //$NON-NLS-1$
	
	public static final String COMPRESSION = "compression";
	public static final String GZIP_COMPRESSION = "tar.gz";
	public static final String BZIP2_COMPRESSION = "tar.bz2";
	public static final String ZIP_COMPRESSION = "zip";
	
	private static final String VERSION = "2.0.0"; //$NON-NLS-1$
	private static final String DESCRIPTION = "Wind River Metadata Repository"; //$NON-NLS-1$
	private static final String PROVIDER = "Wind River"; //$NON-NLS-1$
	
	// Map from artifact id to artifact descriptor. We only allow one version of each artifact
	// to be installed at a time.
	private Map<String, IArtifactDescriptor> artifacts = new HashMap<String, IArtifactDescriptor>();
	
	public InstallArtifactRepository(URL aLocation, String aName, Map aProperties) {
		super(aName, InstallArtifactRepository.class.getName(), VERSION, aLocation, DESCRIPTION, PROVIDER, aProperties);
		save();
	}
	
	/**
	 * Constructor for reading in from file.
	 * 
	 * @param _name
	 * @param _type
	 * @param _version
	 * @param _description
	 * @param _provider
	 * @param _artifacts
	 * @param mappingRules
	 * @param _properties
	 */
	InstallArtifactRepository(String _name, String _type, String _version, URL _location, String _description, String _provider, Set<ArtifactDescriptor> _artifacts, Map _properties) {
		super(_name, _type, _version, _location, _description, _provider, _properties);
		for (IArtifactDescriptor descriptor : _artifacts)
			artifacts.put(descriptor.getArtifactKey().getId(), descriptor);
	}

	public static URL getActualLocation(URL base) {
		final String name = FILENAME;
		String spec = base.toExternalForm();
		if (spec.endsWith(name))
			return base;
		if (spec.endsWith("/")) //$NON-NLS-1$
			spec += name;
		else
			spec += "/" + name; //$NON-NLS-1$
		try {
			return new URL(spec);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public static IArtifactRepository getRepository(IProfile profile) throws ProvisionException {
		AgentLocation location = Activator.getDefault().getService(AgentLocation.class);
		String profileId = profile.getProfileId();
		profileId = profileId.replaceAll("[:/\\\\]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			URL url = location.getDataArea(Activator.PLUGIN_ID);
			url = new URL(url.toExternalForm() + "installDirRepo/" + profileId + "/" + FILENAME); //$NON-NLS-1$ //$NON-NLS-2$
			IArtifactRepositoryManager repoMgr = Activator.getDefault().getService(IArtifactRepositoryManager.class);
			try {
				return repoMgr.loadRepository(url, null);
			} catch (ProvisionException e) {
				Map<String, String> properties = new HashMap<String, String>();
				properties.put(INSTALL_DIR, profile.getLocalProperty(IProfile.PROP_INSTALL_FOLDER));
				return repoMgr.createRepository(url, profile.getProfileId(), InstallArtifactRepository.class.getName(), properties);
			}
		} catch (MalformedURLException e) {
			Activator.getDefault().log(IStatus.ERROR, "Creating install repo URI", e); //$NON-NLS-1$
			return null;
		}
	}
	
	@Override
	public boolean isModifiable() {
		// We're always modifiable
		// TODO - unless we're a shared install...
		return true;
	}
	
	@Override
	public boolean contains(IArtifactDescriptor descriptor) {
		return contains(descriptor.getArtifactKey());
	}

	@Override
	public synchronized boolean contains(IArtifactKey key) {
		IArtifactDescriptor desc = artifacts.get(key.getId());
		if (desc == null)
			return false;
		return desc.getArtifactKey().equals(key);
	}

	@Override
	public IStatus getArtifact(IArtifactDescriptor descriptor, OutputStream destination, IProgressMonitor monitor) {
		// copying from this repository is not supported, yet...
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized IArtifactDescriptor[] getArtifactDescriptors(IArtifactKey key) {
		// we only have one artifact descriptor per key
		IArtifactDescriptor desc = artifacts.get(key);
		if (desc != null)
			return new IArtifactDescriptor[] { desc };
		return new IArtifactDescriptor[0];
	}

	@Override
	public synchronized IArtifactKey[] getArtifactKeys() {
		Collection<IArtifactDescriptor> descs = artifacts.values();
		IArtifactKey[] keys = new IArtifactKey[descs.size()];
		int i = 0;
		for (IArtifactDescriptor desc : descs)
			keys[i++] = desc.getArtifactKey();
		return keys;
	}

	@Override
	public IStatus getArtifacts(IArtifactRequest[] requests, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return Status.OK_STATUS;
	}

	@Override
	public synchronized void addDescriptor(IArtifactDescriptor descriptor) {
		super.addDescriptor(descriptor);
		artifacts.put(descriptor.getArtifactKey().getId(), descriptor);
		save();
	}

	@Override
	public synchronized void addDescriptors(IArtifactDescriptor[] descriptors) {
		super.addDescriptors(descriptors);
		for (IArtifactDescriptor descriptor : descriptors)
			artifacts.put(descriptor.getArtifactKey().getId(), descriptor);
		save();
	}

	synchronized Collection<IArtifactDescriptor> getDescriptors() {
		return artifacts.values();
	}
	
	private File getFileListFile(String artifact) throws IOException {
		File file;
		try {
			file = new File(URLUtil.toURI(location));
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		if (file.getName().equals(FILENAME))
			file = file.getParentFile();
		return new File(file, artifact + ".txt"); //$NON-NLS-1$
	}
	
	@Override
	public OutputStream getOutputStream(IArtifactDescriptor descriptor)	throws ProvisionException {
		// Do the modifiable check in the superclass
		super.getOutputStream(descriptor);
		
		// Add the descriptor to the list and save it
		IArtifactDescriptor oldDesc = artifacts.get(descriptor.getArtifactKey().getId());
		if (oldDesc != null)
			removeDescriptor(oldDesc);
		addDescriptor(descriptor);
		
		// Start the extractor
		try {
			String installDirName = (String)getProperties().get(INSTALL_DIR);
			if (installDirName == null)
				throw new ProvisionException("Install directory not set"); //$NON-NLS-1$
			File installDir = new File(installDirName);
			String subDir = (String)descriptor.getProperties().get(SUB_DIR);
			if (subDir != null)
				installDir = new File(installDir, subDir);
			PipedOutputStream out = new PipedOutputStream();
			PipedInputStream in = new PipedInputStream(out);
			String compression = descriptor.getProperty(COMPRESSION);
			if (ZIP_COMPRESSION.equals(compression)) {
				ZipExtractor extractor = new ZipExtractor(in, installDir,
						new FileListWriter(getFileListFile(descriptor.getArtifactKey().getId())));
				extractor.start();
			} else {
				TarExtractor extractor = new TarExtractor(in, installDir, 
						new FileListWriter(getFileListFile(descriptor.getArtifactKey().getId())),
						compression);
				extractor.start();
			}
			return out;
		} catch (IOException e) {
			// TODO How could that happen
			throw new ProvisionException(e.getLocalizedMessage());
		}
	}

	private void deleteFiles(String artifact) {
		File fileListFile = null;
		try {
			fileListFile = getFileListFile(artifact);
			FileListReader reader = new FileListReader(fileListFile);
			InstalledFile file;
			while ((file = reader.getNext()) != null) {
				file.uninstall();
			}
			reader.close();
		} catch (IOException e) {
			Activator.getDefault().log(IStatus.WARNING, "deleting file", e); //$NON-NLS-1$
		} finally {
			if (fileListFile != null)
				fileListFile.delete();
		}
	}
	
	@Override
	public synchronized void removeAll() {
		super.removeAll();
		for (String artifact : artifacts.keySet())
			deleteFiles(artifact);
		artifacts.clear();
		save();
	}

	@Override
	public void removeDescriptor(IArtifactDescriptor descriptor) {
		removeDescriptor(descriptor.getArtifactKey());
	}

	@Override
	public synchronized void removeDescriptor(IArtifactKey key) {
		super.removeDescriptor(key);
		deleteFiles(key.getId());
		artifacts.remove(key);
		save();
	}

	private void save() {
		try {
			OutputStream os = null;
			try {
				URL actualLocation = getActualLocation(location);
				File artifactsFile = new File(actualLocation.getPath());
				artifactsFile.getParentFile().mkdirs();
				os = new FileOutputStream(artifactsFile);
				super.setProperty(IRepository.PROP_TIMESTAMP, Long.toString(System.currentTimeMillis()));
				new InstallArtifactRepositoryIO().write(this, os);
			} catch (IOException e) {
				// TODO proper exception handling
				e.printStackTrace();
			} finally {
				if (os != null)
					os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
