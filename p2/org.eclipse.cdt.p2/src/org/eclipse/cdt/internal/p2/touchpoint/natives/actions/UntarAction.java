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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.eclipse.cdt.internal.p2.Activator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.engine.Profile;
import org.eclipse.equinox.internal.p2.touchpoint.natives.Messages;
import org.eclipse.equinox.internal.p2.touchpoint.natives.Util;
import org.eclipse.equinox.internal.p2.touchpoint.natives.actions.ActionConstants;
import org.eclipse.equinox.internal.p2.touchpoint.natives.actions.UnzipAction;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;
import org.eclipse.osgi.util.NLS;

/**
 * Untar the artifact with a choice of compression
 * 
 * syntax: untar(source:@artifact, target:${installFolder}/<subdir>, compression:[gz|bz2])
 * 
 * @author DSchaefe
 *
 */
public class UntarAction extends ProvisioningAction {

	private static final String ACTION_NAME = "untar";
	private static final String PARM_COMPRESSION = "compression";
	private static final String VALUE_GZ = "gz";
	private static final String VALUE_BZ2 = "bz2";
	
	private enum Compression {
		none,
		gz,
		bz2
	}
	
	@Override
	public IStatus execute(Map parameters) {
		try {
			return untar(parameters);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public IStatus undo(Map parameters) {
		try {
			return CleanupUntarAction.cleanup(parameters);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
	}

	public static IStatus untar(Map parameters) throws Exception {
		String source = (String)parameters.get(ActionConstants.PARM_SOURCE);
		if (source == null)
			return Util.createError(NLS.bind(Messages.param_not_set, ActionConstants.PARM_SOURCE, ACTION_NAME));

		String originalSource = source;
		String target = (String)parameters.get(ActionConstants.PARM_TARGET);
		if (target == null)
			return Util.createError(NLS.bind(Messages.param_not_set, ActionConstants.PARM_TARGET, ACTION_NAME));

		String compressionStr = (String)parameters.get(PARM_COMPRESSION);
		Compression compression;
		if (compressionStr == null)
			return Util.createError(NLS.bind(Messages.param_not_set, PARM_COMPRESSION, ACTION_NAME));
		else if (compressionStr.equals(VALUE_GZ))
			compression = Compression.gz;
		else if (compressionStr.equals(VALUE_BZ2))
			compression = Compression.bz2;
		else 
			// TODO Should put out a log if compression is unknown
			compression = Compression.none;
		
		IInstallableUnit iu = (IInstallableUnit) parameters.get(ActionConstants.PARM_IU);
		Profile profile = (Profile) parameters.get(ActionConstants.PARM_PROFILE);
		IProvisioningAgent agent = (IProvisioningAgent) parameters.get(ActionConstants.PARM_AGENT);

		if (source.equals(ActionConstants.PARM_AT_ARTIFACT)) {
			//TODO: fix wherever this occurs -- investigate as this is probably not desired
			if (iu.getArtifacts() == null || iu.getArtifacts().size() == 0)
				return Status.OK_STATUS;

			IArtifactKey artifactKey = iu.getArtifacts().iterator().next();

			IFileArtifactRepository downloadCache;
			try {
				downloadCache = Util.getDownloadCacheRepo(agent);
			} catch (ProvisionException e) {
				return e.getStatus();
			}
			File fileLocation = downloadCache.getArtifactFile(artifactKey);
			if ((fileLocation == null) || !fileLocation.exists())
				return Util.createError(NLS.bind(Messages.artifact_not_available, artifactKey));
			source = fileLocation.getAbsolutePath();
		}

		File[] unzippedFiles = untar(source, target, compression);
		StringBuffer unzippedFileNameBuffer = new StringBuffer();
		for (int i = 0; i < unzippedFiles.length; i++)
			unzippedFileNameBuffer.append(unzippedFiles[i].getAbsolutePath()).append(ActionConstants.PIPE);

		profile.setInstallableUnitProperty(iu, "unzipped" + ActionConstants.PIPE + originalSource + ActionConstants.PIPE + target, unzippedFileNameBuffer.toString()); //$NON-NLS-1$
		return Status.OK_STATUS;
	}
	
	private static File[] untar(String source, String destination, Compression compression) throws Exception {
		File zipFile = new File(source);
		if (!zipFile.exists()) {
			Util.log(UnzipAction.class.getName() + " the files to be unzipped is not here"); //$NON-NLS-1$
		}

		File target = new File(destination);
		
		FileInputStream fileIn = new FileInputStream(zipFile);
		InputStream compIn = fileIn;
		if (compression.equals(Compression.gz))
			compIn = new GZIPInputStream(fileIn);
		else if (compression.equals(Compression.bz2)) {
			// Skip the magic bytes first
			fileIn.read(new byte[2]);
			compIn = new CBZip2InputStream(fileIn);
		}

		ArrayList<File> fileList = new ArrayList<File>();
		TarInputStream tarIn = new TarInputStream(compIn);
		for (TarEntry tarEntry = tarIn.getNextEntry(); tarEntry != null; tarEntry = tarIn.getNextEntry()) {
			File outFile = new File(target, tarEntry.getName());
			if (tarEntry.isDirectory()) {
				outFile.mkdirs();
			} else {
				if (outFile.exists())
					outFile.delete();
				else
					outFile.getParentFile().mkdirs();
				FileOutputStream outStream = new FileOutputStream(outFile);
				tarIn.copyEntryContents(outStream);
				outStream.close();
					
				// Set last modified time from the tar entry
				long lastModified = tarEntry.getModTime().getTime();
				outFile.setLastModified(lastModified);
					
				// Set the executable bits from the tar entry
				// we let the umask determine the r/w
				int mode = tarEntry.getMode();
				boolean exec = (mode & 0x111) != 0;
				boolean execOwner = (mode & 0x11) == 0;
//				outFile.setExecutable(exec, execOwner);
					
				fileList.add(outFile);
			}
		}
		tarIn.close();
		return fileList.toArray(new File[fileList.size()]);
	}
	
}
