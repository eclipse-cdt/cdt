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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.core.pdom.indexer.Messages;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.osgi.util.NLS;

public class TeamPDOMImportOperation implements IWorkspaceRunnable {
	static final String CHECKSUMS_NAME = "checksums.dat"; //$NON-NLS-1$
	static final String INDEX_NAME = "cdt-index.pdom"; //$NON-NLS-1$
	private static final Pattern PROJECT_VAR_PATTERN= Pattern.compile("\\$\\{(project_[a-zA-Z0-9]*)\\}"); //$NON-NLS-1$
	private static final String PROJECT_VAR_REPLACEMENT_BEGIN = "\\${$1:"; //$NON-NLS-1$
	private static final String PROJECT_VAR_REPLACEMENT_END = "}"; //$NON-NLS-1$
	private static final String DOLLAR_OR_BACKSLASH_REPLACEMENT = "\\\\$0"; //$NON-NLS-1$
	private static final Pattern DOLLAR_OR_BACKSLASH_PATTERN= Pattern.compile("[\\$\\\\]"); //$NON-NLS-1$

	private static final class FileAndChecksum {
		public ITranslationUnit fFile;
		public byte[] fChecksum;
		public FileAndChecksum(ITranslationUnit tu, byte[] checksum) {
			fFile= tu;
			fChecksum= checksum;
		}
	}
	
	private ICProject fProject;
	private boolean fSuccess;
	private boolean fShowActivity;

	public TeamPDOMImportOperation(ICProject project) {
		fProject= project;
		fShowActivity= PDOMIndexerTask.checkDebugOption(IPDOMIndexerTask.TRACE_ACTIVITY, "true"); //$NON-NLS-1$
	}

	public void run(IProgressMonitor pm) {
		if (fShowActivity) {
			System.out.println("Indexer: PDOMImporter start"); //$NON-NLS-1$
		}
		fSuccess= false;
		Exception ex= null;
		try {
			File importFile= getImportLocation();
			if (importFile.exists()) {
				doImportIndex(importFile, pm);
				fSuccess= true;
			}
		}
		catch (InterruptedException e) {
			throw new OperationCanceledException();
		} 
		catch (ZipException e) {
			ex= e;
		} 
		catch (IOException e) {
			ex= e;
		} 
		catch (CoreException e) {
			ex= e;
		} 
		
		if (ex != null) {
			CCorePlugin.log(ex);
		}
		if (fShowActivity) {
			System.out.println("Indexer: PDOMImporter completed, ok=" + fSuccess); //$NON-NLS-1$
		}
	}

	public boolean wasSuccessful() {
		return fSuccess;
	}
		
	private File getImportLocation() throws CoreException {
		IProject project= fProject.getProject();
		String locationString= IndexerPreferences.getIndexImportLocation(project);
		return expandLocation(project, locationString);
	}

	static File expandLocation(IProject project, String loc) throws CoreException {
		String replacement= PROJECT_VAR_REPLACEMENT_BEGIN
		 	+ DOLLAR_OR_BACKSLASH_PATTERN.matcher(project.getName()).replaceAll(DOLLAR_OR_BACKSLASH_REPLACEMENT) 
		 	+ PROJECT_VAR_REPLACEMENT_END; 
		
		loc= PROJECT_VAR_PATTERN.matcher(loc).replaceAll(replacement);
		IStringVariableManager varManager= VariablesPlugin.getDefault().getStringVariableManager();
		IPath location= new Path(varManager.performStringSubstitution(loc));
		if (!location.isAbsolute()) {
			location= project.getLocation().append(location);
		}
		return location.toFile();
	}

	private void doImportIndex(File importFile, IProgressMonitor monitor) throws CoreException, InterruptedException, IOException {
		ZipFile zip= new ZipFile(importFile);
		Map checksums= null;
		try {
			importIndex(zip, monitor);
			checksums= getChecksums(zip);
		}
		finally {
			try {
				zip.close();
			} catch (IOException e) {
				CCorePlugin.log(e);
			}
		}
		
		checkIndex(checksums, monitor);
	}

	private void importIndex(ZipFile zip, IProgressMonitor monitor) throws CoreException, IOException {
		ZipEntry indexEntry= zip.getEntry(INDEX_NAME);
		if (indexEntry == null) {
			throw new CoreException(CCorePlugin.createStatus(
					NLS.bind(Messages.PDOMImportTask_errorInvalidArchive, zip.getName())));
		}
		InputStream stream= zip.getInputStream(indexEntry);
		CCoreInternals.getPDOMManager().importProjectPDOM(fProject, stream);
	}

	private Map getChecksums(ZipFile zip) {
		ZipEntry indexEntry= zip.getEntry(CHECKSUMS_NAME);
		if (indexEntry != null) {
			try {
				ObjectInputStream input= new ObjectInputStream(zip.getInputStream(indexEntry));
				try {
					Object obj= input.readObject();
					if (obj instanceof Map) {
						return (Map) obj;
					}
				}
				finally {
					input.close();
				}
			}
			catch (Exception e) {
				CCorePlugin.log(e);
			}
		}
		return Collections.EMPTY_MAP;
	}

	private void checkIndex(Map checksums, IProgressMonitor monitor) throws CoreException, InterruptedException {
		List filesToCheck= new ArrayList();		

		WritablePDOM pdom= (WritablePDOM) CCoreInternals.getPDOMManager().getPDOM(fProject);
		pdom.acquireReadLock();
		try {
			if (pdom.versionMismatch()) {
				throw new CoreException(CCorePlugin.createStatus(					
						NLS.bind(Messages.PDOMImportTask_errorInvalidPDOMVersion, fProject.getElementName())));
			}

			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			List filesToDelete= pdom.getAllFileLocations();
			for (Iterator i = filesToDelete.iterator(); i.hasNext();) {
				checkMonitor(monitor);

				byte[] checksum= null;
				ITranslationUnit tu= null;

				IIndexFileLocation ifl = (IIndexFileLocation) i.next();
				String fullPathStr= ifl.getFullPath();
				if (fullPathStr != null) {
					Path fullPath= new Path(fullPathStr);
					IFile file= root.getFile(fullPath);
					boolean exists= file.exists();
					if (!exists) {
						try {
							file.refreshLocal(0, new NullProgressMonitor());
							exists= file.exists();
						} catch (CoreException e) {
							CCorePlugin.log(e);
						}
					}
					if (exists) {
						tu= (ITranslationUnit) CoreModel.getDefault().create(file);						
						if (tu != null) {
							checksum= Checksums.getChecksum(checksums, file);
						}
					}
				}
				if (checksum != null) {
					filesToCheck.add(new FileAndChecksum(tu, checksum));
					i.remove();
				}
			}			
			try {
				removeOutdatedFiles(checksums, filesToCheck, monitor);
			}
			catch (NoSuchAlgorithmException e) {
				CCorePlugin.log(e);
			}
			deleteFiles(pdom, 1, filesToDelete, filesToCheck, monitor);
		}
		finally {
			pdom.releaseReadLock();
		}
	}

	private void checkMonitor(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	private void deleteFiles(WritablePDOM pdom, final int giveupReadlocks, List filesToDelete,
			List updateTimestamps, IProgressMonitor monitor) throws InterruptedException, CoreException {
		pdom.acquireWriteLock(giveupReadlocks);
		try {
			for (Iterator i = filesToDelete.iterator(); i.hasNext();) {
				checkMonitor(monitor);
				
				IndexFileLocation ifl = (IndexFileLocation) i.next();
				IIndexFragmentFile file= pdom.getFile(ifl);
				pdom.clearFile(file, null);
			}
			for (Iterator i = updateTimestamps.iterator(); i.hasNext();) {
				checkMonitor(monitor);
				
				FileAndChecksum fc = (FileAndChecksum) i.next();
				IIndexFragmentFile file= pdom.getFile(IndexLocationFactory.getIFL(fc.fFile));
				if (file != null) {
					IResource r= fc.fFile.getResource();
					if (r != null) {
						file.setTimestamp(r.getLocalTimeStamp());
						file.setScannerConfigurationHashcode(0);
					}
				}
			}
		}
		finally {
			pdom.releaseWriteLock(giveupReadlocks);
		}
	}
	
	private void removeOutdatedFiles(Map checksums, List filesToCheck, IProgressMonitor monitor) throws NoSuchAlgorithmException {
        MessageDigest md= Checksums.getAlgorithm(checksums); 
		for (Iterator i = filesToCheck.iterator(); i.hasNext();) {
			checkMonitor(monitor);
			
			FileAndChecksum cs= (FileAndChecksum) i.next();
			ITranslationUnit tu= cs.fFile;
			if (tu != null) {
				IPath location= tu.getLocation();
				if (location != null) {
					try {
						byte[] checksum= Checksums.computeChecksum(md, location.toFile());
						if (!Arrays.equals(checksum, cs.fChecksum)) {
							i.remove();
						}
					}
					catch (IOException e) {
						CCorePlugin.log(e);
					}
				}
			}
		}
	}
}
