/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.p2.touchpoint.natives.actions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.eclipse.cdt.internal.p2.Activator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;

/**
 * Unpack the artifact with a choice of compression
 * 
 * syntax: unpack(source:<url>, targetDir:${installFolder}/<subdir>, compression:[gz|bz2])
 */
public class UnpackAction extends ProvisioningAction {

	private static final String ACTION_NAME = "unpack"; //$NON-NLS-1$
	private static final String PARM_SOURCE = "source"; //$NON-NLS-1$
	private static final String PARM_TARGET_DIR = "targetDir"; //$NON-NLS-1$
	private static final String PARM_FORMAT = "format"; //$NON-NLS-1$

	@Override
	public IStatus execute(Map<String, Object> parameters) {
		try {
			String source = (String)parameters.get(PARM_SOURCE);
			if (source == null) {
				return Activator.getStatus(IStatus.ERROR, String.format(Messages.UnpackAction_ParmNotPresent, PARM_SOURCE, ACTION_NAME));
			}

			String targetDir = (String)parameters.get(PARM_TARGET_DIR);
			if (targetDir == null) {
				return Activator.getStatus(IStatus.ERROR, String.format(Messages.UnpackAction_ParmNotPresent, PARM_TARGET_DIR, ACTION_NAME));
			}

			String format = (String)parameters.get(PARM_FORMAT);
			if (format == null) {
				return Activator.getStatus(IStatus.ERROR, String.format(Messages.UnpackAction_ParmNotPresent, PARM_FORMAT, ACTION_NAME));
			}

			IProfile profile = (IProfile) parameters.get("profile"); //$NON-NLS-1$
			File installFolder = new File(profile.getProperty(IProfile.PROP_INSTALL_FOLDER));
			File destDir = new File(installFolder, targetDir);
			if (destDir.exists()) {
				return Activator.getStatus(IStatus.ERROR, String.format(org.eclipse.cdt.internal.p2.touchpoint.natives.actions.Messages.UnpackAction_TargetDirExists, destDir.getAbsolutePath()));
			}

			URL url = new URL(source);
			InputStream fileIn = new BufferedInputStream(url.openStream());

			switch (format) {
			case "tar.gz": //$NON-NLS-1$
				InputStream gzIn = new GzipCompressorInputStream(fileIn);
				untar(gzIn, destDir);
				break;
			case "tar.bz2": //$NON-NLS-1$
				InputStream bzIn = new BZip2CompressorInputStream(fileIn);
				untar(bzIn, destDir);
				break;
			case "tar.xz": //$NON-NLS-1$
				InputStream xzIn = new XZCompressorInputStream(fileIn);
				untar(xzIn, destDir);
				break;
			case "zip": //$NON-NLS-1$

			}

			return Status.OK_STATUS;
		} catch (Throwable e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
	}

	private void untar(InputStream in, File destDir) throws IOException {
		byte[] buff = new byte[4096];
		try (TarArchiveInputStream tarIn = new TarArchiveInputStream(in)) {
			for (TarArchiveEntry entry = tarIn.getNextTarEntry(); entry != null; entry = tarIn.getNextTarEntry()) {
				String name = entry.getName();
				File destFile = new File(destDir, name);
				if (entry.isSymbolicLink()) {
					Files.createSymbolicLink(destFile.toPath(), Paths.get(name));
				} else {
					try (FileOutputStream out = new FileOutputStream(destFile)) {
						long size = entry.getSize();
						while (size > 0) {
							int n = tarIn.read(buff, 0, (int)Math.min(size, buff.length));
							out.write(buff, 0, n);
							size -= n;
						}
					}
					chmod(destFile, entry.getMode());
				}
			}
		}
	}

	private void chmod(File file, int mode) {
		file.setExecutable((mode & 0111) != 0, (mode & 0110) == 0);
		file.setWritable((mode & 0222) != 0, (mode & 0220) == 0);
		file.setReadable((mode & 0444) != 0, (mode & 0440) == 0);
	}

	@Override
	public IStatus undo(Map<String, Object> parameters) {
		try {
			return CleanupUntarAction.cleanup(parameters);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
	}

}
