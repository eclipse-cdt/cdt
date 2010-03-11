/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * Output stream adapter saving build output in a file.
 *
 */
public class BuildOutputLogger extends OutputStream {

	private static final String SAVE_CONSOLE_FILE_ID =
		CCorePlugin.PLUGIN_ID +	"." + "saveBuildOutputToFile"; //$NON-NLS-1$ //$NON-NLS-2$

	private static final String SAVE_CONSOLE_STATE_ID =
		CCorePlugin.PLUGIN_ID +	"." +  "isSavingBuildOutput"; //$NON-NLS-1$ //$NON-NLS-2$

	private URI fileUri;
	private BufferedOutputStream log;
	private OutputStream outputStream;


	public BuildOutputLogger(IProject project, OutputStream stream) {
		super();
		this.outputStream = stream;
		SaveBuildOutputPreferences bp = readSaveBuildOutputPreferences(project);
		if ( ! bp.isSaving || log != null ) return;

		try {
			fileUri = URIUtil.toURI(bp.fileName);
			IFileStore fs = EFS.getStore(fileUri);
			OutputStream out = fs.openOutputStream(EFS.NONE, null);
			log = new BufferedOutputStream(out);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			log = null;
		}
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);
		if ( log != null ) {
			log.write(b);
		}
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b) throws IOException {
		outputStream.write(b);
		if ( log != null ) {
			log.write(b);
		}
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		outputStream.write(b, off, len);
		if ( log != null ) {
			log.write(b, off, len);
		}
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		outputStream.flush();
		outputStream.close();
		if ( log != null ) {
			try {
				log.flush();
				log.close();
				log = null;
				IFile[] files =	ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(fileUri);
				for (IFile file : files) {
					try {
						file.refreshLocal(IResource.DEPTH_ONE, null);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			} catch (IOException e) {
				CCorePlugin.log(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		outputStream.flush();
		if ( log != null ) {
			log.flush();
		}
	}

	public static class SaveBuildOutputPreferences {
		public String fileName;
		public boolean isSaving;

		@Override
		public String toString() { return fileName + " " + isSaving; } //$NON-NLS-1$
	}

	/** Read SaveBuildOutput preferences for active configuration */
	public static SaveBuildOutputPreferences readSaveBuildOutputPreferences(IProject project) {
		ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project);
		if ( projDesc == null ) return new SaveBuildOutputPreferences();
		ICConfigurationDescription configDesc = projDesc.getActiveConfiguration();
		return readSaveBuildOutputPreferences(project, configDesc.getName());
	}

	/** Read SaveBuildOutput preferences for configuration, specified by name */
	public static SaveBuildOutputPreferences readSaveBuildOutputPreferences(IProject project, String configurationName) {
		SaveBuildOutputPreferences bp = new SaveBuildOutputPreferences();
		String key;
		IEclipsePreferences pref = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);

		key = getFileNameKey(project, configurationName);
		bp.fileName = pref.get(key, null);

		key = getIsSavingKey(project, configurationName);
		bp.isSaving = pref.getBoolean(key,false);

		if ( bp.fileName == null || ! canWriteToFile(bp.fileName)) {
			bp.isSaving = false;
		}
		return bp;
	}

	/** Write SaveBuildOutput preferences for active configuration */
	public static void writeSaveBuildOutputPreferences(IProject project, SaveBuildOutputPreferences bp) {
		ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project);
		if ( projDesc == null ) return;
		ICConfigurationDescription configDesc = projDesc.getActiveConfiguration();
		writeSaveBuildOutputPreferences(project, configDesc.getName(), bp);
	}

	/** Write SaveBuildOutput preferences for configuration, specified by name */
	public static void writeSaveBuildOutputPreferences(IProject project, String configurationName,
			SaveBuildOutputPreferences bp) {
		try {
			String key = getFileNameKey(project,configurationName);
			IEclipsePreferences preferences = new InstanceScope().getNode(CCorePlugin.PLUGIN_ID);
			if ( bp.fileName == null || "".equals(bp.fileName)) {  //$NON-NLS-1$
				preferences.remove(key);
			} else {
				preferences.put(key, bp.fileName);
			}
			key = getIsSavingKey(project,configurationName);
			preferences.putBoolean(key, bp.isSaving);
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
	}

	public static boolean canWriteToFile(String fileName) {
		if ( fileName != null && fileName.length() > 0 ) {
			// Check path exists in filesystem
			File f = new File(fileName);
			if ( 	f.getParentFile() != null &&
					f.getParentFile().exists() &&
					f.getParentFile().isDirectory() &&
					( !f.exists() || (f.exists() && f.canWrite()) ) ) {
				// File can be written
				return true;
			}
		}
		return false;
	}

	public static String getFileNameKey(IProject project, String cfgName) {
		// Make this preference key to be per project
		return SAVE_CONSOLE_FILE_ID + "." + project.getName() + "." + cfgName; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getIsSavingKey(IProject project, String cfgName) {
		// Make this preference key to be per project
		return SAVE_CONSOLE_STATE_ID + "." + project.getName() + "." + cfgName; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
