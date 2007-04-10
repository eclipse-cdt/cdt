/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

public class TeamPDOMExportOperation implements IWorkspaceRunnable {

	private ICProject fProject; 
	private String fTargetLocation;
	private File fTargetLocationFile;
	private MessageDigest fMessageDigest;

	public TeamPDOMExportOperation(ICProject project) {
		fProject= project;
	}

	public void setTargetLocation(String location) {
		fTargetLocation= location;
	}

	public void setOptions(int options) {
	}
	
	public void setAlgorithm(MessageDigest md) {
		fMessageDigest= md;
	}

	public void run(IProgressMonitor monitor) throws CoreException {
		getMessageDigest();
		getTargetLocation();

		File tmpPDOM= null;
		File tmpChecksums= null;
		try {
			tmpPDOM = File.createTempFile("tmp", ".pdom");  //$NON-NLS-1$//$NON-NLS-2$
			tmpChecksums= File.createTempFile("checksums", ".dat"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.TeamPDOMExportOperation_errorCreatingTempFile, e));
		}

		try {
			PDOMManager pdomManager= CCoreInternals.getPDOMManager();

			// wait for indexer
			monitor.beginTask("", 100); //$NON-NLS-1$
			pdomManager.joinIndexer(Integer.MAX_VALUE, subMonitor(monitor, 1));
			checkMonitor(monitor);

			// create index
			IIndexLocationConverter converter= new ResourceContainerRelativeLocationConverter(ResourcesPlugin.getWorkspace().getRoot());
			pdomManager.exportProjectPDOM(fProject, tmpPDOM, converter);
			checkMonitor(monitor);
			monitor.worked(5);
			
			// create checksums
			PDOM pdom= new PDOM(tmpPDOM, converter, LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
			try {
				monitor.setTaskName(Messages.Checksums_taskComputeChecksums);
				createChecksums(fProject, pdom, tmpChecksums, subMonitor(monitor, 94));
			}
			finally {
				pdom.close();
			}
			
			// create archive
			createArchive(tmpPDOM, tmpChecksums);
			
			// store preferences
			monitor.setTaskName(Messages.TeamPDOMExportOperation_taskExportIndex);
			IndexerPreferences.setIndexImportLocation(fProject.getProject(), fTargetLocation.toString());
		}
		finally {
			if (tmpPDOM != null) {
				tmpPDOM.delete();
			}
			if (tmpChecksums != null) {
				tmpChecksums.delete();
			}
		}
	}

	private void getTargetLocation() throws CoreException {
		fTargetLocationFile= TeamPDOMImportOperation.expandLocation(fProject.getProject(), fTargetLocation);
	}

	private void getMessageDigest() throws CoreException {
		if (fMessageDigest == null) {
			try {
				fMessageDigest= Checksums.getDefaultAlgorithm();
			}
			catch (NoSuchAlgorithmException e) {
				throw new CoreException(CCorePlugin.createStatus(e.getMessage(), e));
			}
		}
	}

	private void createChecksums(ICProject cproject, PDOM pdom, File target, IProgressMonitor monitor) throws CoreException {
		List locs;
		try {
			pdom.acquireReadLock();
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
		try {
			locs = pdom.getAllFileLocations();
		}
		finally {
			pdom.releaseReadLock();
		}
		int i=0;
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IFile[] files= new IFile[locs.size()];
		for (Iterator iterator = locs.iterator(); iterator.hasNext();) {
			IndexFileLocation floc = (IndexFileLocation) iterator.next();
			String fullPath= floc.getFullPath();
			if (fullPath != null) {
				files[i++]= root.getFile(new Path(fullPath));
			}
 		}
		Map map= Checksums.createChecksumMap(files, fMessageDigest, monitor);
		writeChecksums(map, target);
	}

	private void writeChecksums(Map map, File target) throws CoreException {
		ObjectOutputStream out= null;
		try {
			out= new ObjectOutputStream(new FileOutputStream(target));
			out.writeObject(map);
		} catch (IOException e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.TeamPDOMExportOperation_errorWriteTempFile, e));
		}
		finally {
			close(out);
		}
	}

	private void close(InputStream in) {
		try {
			if (in != null) { 
				in.close();
			}
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
	} 

	private void close(OutputStream out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
	} 

	private void createArchive(File tmpPDOM, File tmpChecksums) throws CoreException {
		fTargetLocationFile.delete();
		ZipOutputStream out= null;
		try {
			fTargetLocationFile.getParentFile().mkdirs();
			out= new ZipOutputStream(new FileOutputStream(fTargetLocationFile));
			out.setLevel(Deflater.BEST_COMPRESSION);
			writeEntry(out, TeamPDOMImportOperation.INDEX_NAME, tmpPDOM);
			writeEntry(out, TeamPDOMImportOperation.CHECKSUMS_NAME, tmpChecksums);
		}
		catch (IOException e) {
			throw new CoreException(CCorePlugin.createStatus(Messages.TeamPDOMExportOperation_errorCreateArchive, e));
		}
		finally {
			close(out);
		}
		IFile[] wsResource= ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(fTargetLocationFile.getAbsolutePath()));
		for (int i = 0; i < wsResource.length; i++) {
			IFile file = wsResource[i];
			file.refreshLocal(0, new NullProgressMonitor());
		}
	}

	private void writeEntry(ZipOutputStream out, String name, File input) throws IOException {
		ZipEntry e= new ZipEntry(name);
		out.putNextEntry(e);
		int read= 0;
		byte[] buffer= new byte[4096];
		InputStream in= new FileInputStream(input);
		try {
			while ((read= in.read(buffer)) >= 0) {
				out.write(buffer, 0, read);
			}
			out.closeEntry();
		}
		finally {
			close(in);
		}
	}

	private SubProgressMonitor subMonitor(IProgressMonitor monitor, int ticks) {
		return new SubProgressMonitor(monitor, ticks);
	}

	private void checkMonitor(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}
}
