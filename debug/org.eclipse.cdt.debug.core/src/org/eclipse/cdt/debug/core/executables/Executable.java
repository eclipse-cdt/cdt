/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.executables;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

public class Executable extends PlatformObject {

	static public boolean isExecutableFile(IPath path) {
		// ignore directories
		if (path.toFile().isDirectory()) {
			return false;
		}
		// Only if file has no extension, has an extension that is an integer
		// or is a binary file content type
		String ext = path.getFileExtension();
		if (ext != null) {
			// shared libraries often have a version number
			boolean isNumber = true;
			for (int i = 0; i < ext.length(); ++i)
				if (!Character.isDigit(ext.charAt(i))) {
					isNumber = false;
					break;
				}
			if (!isNumber) {
				boolean isBinary = false;
				final IContentTypeManager ctm = Platform.getContentTypeManager();
				final IContentType ctbin = ctm.getContentType(CCorePlugin.CONTENT_TYPE_BINARYFILE);
				final IContentType[] cts = ctm.findContentTypesFor(path.toFile().getName());
				for (int i = 0; !isBinary && i < cts.length; i++) {
					isBinary = cts[i].isKindOf(ctbin);
				}
				if (!isBinary) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof Executable)
		{
			Executable exe = (Executable)arg0;
			return exe.getPath().equals(this.getPath());
		}
		return super.equals(arg0);
	}

	private final IPath executablePath;
	private final IProject project;
	private final String name;
	private final IResource resource;
	private final Map<ITranslationUnit, String> remappedPaths;
	private final ArrayList<ITranslationUnit> sourceFiles;
	private boolean refreshSourceFiles;
	private boolean remapSourceFiles;
	private ISourceFileRemapping[] remappers;
	private String[] symReaderSources;

	public IPath getPath() {
		return executablePath;
	}

	public IProject getProject() {
		return project;
	}

	/**
	 * @since 7.0
	 */
	public Executable(IPath path, IProject project, IResource resource, ISourceFileRemapping[] sourceFileRemappings) {
		this.executablePath = path;
		this.project = project;
		this.name = new File(path.toOSString()).getName();
		this.resource = resource;
		this.remappers = sourceFileRemappings;		
		remappedPaths = new HashMap<ITranslationUnit, String>();
		sourceFiles = new ArrayList<ITranslationUnit>();
		refreshSourceFiles = true;
		remapSourceFiles = true;
	}

	public IResource getResource() {
		return resource;
	}

	@Override
	public String toString() {
		return executablePath.toString();
	}

	public String getName() {
		return name;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IResource.class))
			if (getResource() != null)
				return getResource();
			else
				return this.getProject();
		return super.getAdapter(adapter);
	}
	
	private String remapSourceFile(String filename) {
		for (ISourceFileRemapping remapper : remappers) {
			String remapped = remapper.remapSourceFile(this.getPath(), filename);
			if (!remapped.equals(filename)) {
				return remapped;
			}
		}
		return filename;
	}
	

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 6.0
	 */
	public synchronized ITranslationUnit[] getSourceFiles(IProgressMonitor monitor) {
		
		if (!refreshSourceFiles && !remapSourceFiles)
			return sourceFiles.toArray(new TranslationUnit[sourceFiles.size()]) ;
		
		// Try to get the list of source files used to build the binary from the
		// symbol information.

		remappedPaths.clear();

		sourceFiles.clear();

		CModelManager factory = CModelManager.getDefault();

		ICProject cproject = factory.create(project);

		if (refreshSourceFiles)
		{
			symReaderSources = ExecutablesManager.getExecutablesManager().getSourceFiles(this, monitor);
		}
		if (symReaderSources != null && symReaderSources.length > 0) {
			for (String filename : symReaderSources) {
				String orgPath = filename;

				filename = remapSourceFile(filename);

				// Sometimes the path in the symbolics will have a different
				// case than the actual file system path. Even if the file
				// system is not case sensitive this will confuse the Path
				// class. So make sure the path is canonical, otherwise
				// breakpoints won't be resolved, etc.. Also check for relative
				// path names and attempt to resolve them relative to the
				// executable.

				boolean fileExists = false;

				try {
					File file = new File(filename);
					fileExists = file.exists();
					if (fileExists) {
						filename = file.getCanonicalPath();
					} else if (filename.startsWith(".")) { //$NON-NLS-1$
						file = new File(executablePath.removeLastSegments(1).toOSString(), filename);
						filename = file.getCanonicalPath();
					}
				} catch (IOException e) { // Do nothing.
				}

				IFile wkspFile = null;
				IFile sourceFile = getProject().getFile(filename);
				IPath sourcePath = new Path(filename);
				if (fileExists) {
					// See if this source file is already in the project.
					// We check this to determine if we should create a
					// TranslationUnit or ExternalTranslationUnit

					if (sourceFile.exists())
						wkspFile = sourceFile;
					else {
						IFile[] filesInWP = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URIUtil.toURI(sourcePath));
						for (IFile fileInWP : filesInWP) {
							if (fileInWP.isAccessible()) {
								wkspFile = fileInWP;
								break;
							}
						}
					}
				}

				// Create a translation unit for this file and add it as
				// a child of the binary
				String id = CoreModel.getRegistedContentTypeId(sourceFile.getProject(), sourceFile.getName());

				if (id != null) { // Don't add files we can't get an
									// ID for.
					TranslationUnit tu;
					if (wkspFile != null)
						tu = new TranslationUnit(cproject, wkspFile, id);
					else {
						// Be careful not to convert a unix path like
						// "/src/home" to "c:\source\home" on Windows. See
						// bugzilla 297781
						URI uri = (sourcePath.toFile().exists()) ? URIUtil.toURI(sourcePath) : URIUtil.toURI(filename);						
						tu = new ExternalTranslationUnit(cproject, uri, id);
					}

					sourceFiles.add(tu);

					if (!orgPath.equals(filename)) {
						remappedPaths.put(tu, orgPath);
					}
				}
			}
		}
		
		refreshSourceFiles = false;
		remapSourceFiles = false;
		return sourceFiles.toArray(new TranslationUnit[sourceFiles.size()]) ;
	}

	/**
	 * @since 6.0
	 */
	public void setRefreshSourceFiles(boolean refreshSourceFiles) {
		this.refreshSourceFiles = refreshSourceFiles;
	}

	public synchronized String getOriginalLocation(ITranslationUnit tu) {
		String orgLocation = remappedPaths.get(tu);
		if (orgLocation == null)
			orgLocation = tu.getLocation().toOSString();
		return orgLocation;
	}

	/**
	 * @since 7.0
	 */
	public void setRemapSourceFiles(boolean remapSourceFiles) {
		this.remapSourceFiles = remapSourceFiles;
	}

}
