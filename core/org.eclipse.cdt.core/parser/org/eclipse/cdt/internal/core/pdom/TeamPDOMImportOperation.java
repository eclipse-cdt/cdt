/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
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
		public IIndexFragmentFile fIFile;
		public byte[] fChecksum;
		public FileAndChecksum(ITranslationUnit tu, IIndexFragmentFile ifile, byte[] checksum) {
			fFile= tu;
			fIFile= ifile;
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

	@Override
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
			
			if(project.getLocation() != null)
				location= project.getLocation().append(location);
		}
		return location.toFile();
	}

	private void doImportIndex(File importFile, IProgressMonitor monitor) throws CoreException, InterruptedException, IOException {
		ZipFile zip= new ZipFile(importFile);
		Map<?, ?> checksums= null;
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

	private Map<?, ?> getChecksums(ZipFile zip) {
		ZipEntry indexEntry= zip.getEntry(CHECKSUMS_NAME);
		if (indexEntry != null) {
			try {
				ObjectInputStream input= new ObjectInputStream(zip.getInputStream(indexEntry));
				try {
					Object obj= input.readObject();
					if (obj instanceof Map<?, ?>) {
						return (Map<?,?>) obj;
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

	private void checkIndex(Map<?, ?> checksums, IProgressMonitor monitor) throws CoreException, InterruptedException {
		IPDOM obj= CCoreInternals.getPDOMManager().getPDOM(fProject);
		if (!(obj instanceof WritablePDOM)) {
			return;
		}
		
		WritablePDOM pdom= (WritablePDOM) obj;
		pdom.acquireReadLock();
		try {
			List<FileAndChecksum> filesToCheck= new ArrayList<FileAndChecksum>();		
			if (!pdom.isSupportedVersion()) {
				throw new CoreException(CCorePlugin.createStatus(					
						NLS.bind(Messages.PDOMImportTask_errorInvalidPDOMVersion, fProject.getElementName())));
			}

			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IIndexFragmentFile[] filesToDelete= pdom.getAllFiles();
			for (int i = 0; i < filesToDelete.length; i++) {
				checkMonitor(monitor);
				IIndexFragmentFile ifile = filesToDelete[i];

				byte[] checksum= null;
				ITranslationUnit tu= null;

				IIndexFileLocation ifl = ifile.getLocation();
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
					filesToCheck.add(new FileAndChecksum(tu, ifile, checksum));
					filesToDelete[i]= null;
				}
			}		
			
			List<FileAndChecksum> updateTimestamps= getUnchangedWithDifferentTimestamp(checksums, filesToCheck, monitor);
			updateIndex(pdom, 1, filesToDelete, updateTimestamps, monitor);
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

	private void updateIndex(WritablePDOM pdom, final int giveupReadlocks, IIndexFragmentFile[] filesToDelete,
			List<FileAndChecksum> updateTimestamps, IProgressMonitor monitor) throws InterruptedException, CoreException {
		pdom.acquireWriteLock(giveupReadlocks);
		try {
			for (IIndexFragmentFile ifile : filesToDelete) {
				if (ifile != null) {
					checkMonitor(monitor);
					pdom.clearFile(ifile);
				}
			}
			for (FileAndChecksum fc : updateTimestamps) {
				checkMonitor(monitor);
				
				IIndexFragmentFile file= fc.fIFile;
				if (file != null) {
					IResource r= fc.fFile.getResource();
					if (r != null) {
						file.setTimestamp(r.getLocalTimeStamp());
					}
				}
			}
		}
		finally {
			pdom.releaseWriteLock(giveupReadlocks, true);
		}
	}
	
	private List<FileAndChecksum> getUnchangedWithDifferentTimestamp(Map<?, ?> checksums, List<FileAndChecksum> filesToCheck, IProgressMonitor monitor) {
        MessageDigest md;
		try {
			md = Checksums.getAlgorithm(checksums);
		} catch (NoSuchAlgorithmException e) {
			CCorePlugin.log(e);
			return Collections.emptyList();
		} 

		List<FileAndChecksum> result= new ArrayList<TeamPDOMImportOperation.FileAndChecksum>();
		for (FileAndChecksum cs : filesToCheck) {
			checkMonitor(monitor);
			
			ITranslationUnit tu= cs.fFile;
			if (tu != null) {
				IPath location= tu.getLocation();
				if (location != null) {
					try {
						final File file = location.toFile();
						if (file.isFile()) {
							IResource res= cs.fFile.getResource();
							if (res == null || res.getLocalTimeStamp() != cs.fIFile.getTimestamp()) {
								byte[] checksum= Checksums.computeChecksum(md, file);
								if (Arrays.equals(checksum, cs.fChecksum)) {
									result.add(cs);
								}
							}
						}
					} catch (IOException e) {
						CCorePlugin.log(e);
						result.add(cs);
					} catch (CoreException e) {
						CCorePlugin.log(e);
						result.add(cs);
					}
				}
			}
		}
		return result;
	}
}
