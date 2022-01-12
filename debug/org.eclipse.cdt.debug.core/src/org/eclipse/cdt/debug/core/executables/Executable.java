/*******************************************************************************
 * Copyright (c) 2008, 2016 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.debug.internal.core.Trace;
import org.eclipse.cdt.internal.core.Util;
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

	/**
	 * Poorly named. This does not determine if the the file is an executable
	 * but rather a binary. Use {@link #isBinaryFile(IPath)} instead.
	 *
	 * @deprecated use {@link #isBinaryFile(IPath)}
	 */
	@Deprecated
	static public boolean isExecutableFile(IPath path) {
		return isBinaryFile(path);
	}

	/**
	 * Determines if the given file is a binary file. For our purposes, an
	 * "executable" is a runnable program (an .exe file on Windows, e.g.,) or a
	 * shared library. A binary can be an executable but it can also be an
	 * instruction-containing artifact of a build, which typically is linked to
	 * make an executable (.e.,g .o and .obj files)
	 *
	 * @param path
	 * @return
	 * @since 7.1
	 */
	static public boolean isBinaryFile(IPath path) {
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
		if (arg0 instanceof Executable) {
			Executable exe = (Executable) arg0;
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
	/** see {@link #setRefreshSourceFiles(boolean)} */
	private boolean refreshSourceFiles;
	/** see {@link #setRemapSourceFiles(boolean) */
	private boolean remapSourceFiles;
	private ISourceFileRemapping[] remappers;
	/** The (unmapped) file specifications found in the executable */
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
		remappedPaths = new HashMap<>();
		sourceFiles = new ArrayList<>();
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IResource.class))
			if (getResource() != null)
				return (T) getResource();
			else
				return (T) this.getProject();
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
		if (Trace.DEBUG_EXECUTABLES)
			Trace.getTrace().traceEntry(null);

		if (!refreshSourceFiles && !remapSourceFiles) {
			if (Trace.DEBUG_EXECUTABLES)
				Trace.getTrace().trace(null, "returning cached result"); //$NON-NLS-1$
			return sourceFiles.toArray(new TranslationUnit[sourceFiles.size()]);
		}

		// Try to get the list of source files used to build the binary from the
		// symbol information.

		remappedPaths.clear();

		sourceFiles.clear();

		CModelManager factory = CModelManager.getDefault();

		ICProject cproject = factory.create(project);

		if (refreshSourceFiles) {
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
				// breakpoints won't be resolved, etc. Make sure to do this only
				// for files that are specified as an absolute path at this
				// point. Paths that are not absolute can't be trusted to
				// java.io.File to canonicalize since that class will
				// arbitrarily give the specification a local context, and we
				// don't want that. A source file that continues to be
				// non-absolute after having been run through source lookups
				// (done in remapSourceFile() above) is effectively ambiguous
				// and we should leave it that way. Users will need to configure
				// a source lookup to give the file a local context if in fact
				// the file is available on his machine.
				boolean fileExists = false;
				boolean isNativeAbsPath = Util.isNativeAbsolutePath(filename);
				if (isNativeAbsPath) {
					try {
						File file = new File(filename);
						fileExists = file.exists();
						if (fileExists) {
							filename = file.getCanonicalPath();
						}
					} catch (IOException e) { // Do nothing.
					}
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
						IFile[] filesInWP = ResourcesPlugin.getWorkspace().getRoot()
								.findFilesForLocationURI(URIUtil.toURI(sourcePath));
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
						URI uri = (isNativeAbsPath && sourcePath.toFile().exists()) ? URIUtil.toURI(sourcePath)
								: URIUtil.toURI(filename, true);
						tu = new ExternalTranslationUnit(cproject, uri, id);
					}

					if (!sourceFiles.contains(tu)) {
						sourceFiles.add(tu);
					}

					if (!orgPath.equals(filename)) {
						remappedPaths.put(tu, orgPath);
					}
				}
			}
		}

		refreshSourceFiles = false;
		remapSourceFiles = false;
		return sourceFiles.toArray(new TranslationUnit[sourceFiles.size()]);
	}

	/**
	 * Call this to force a subsequent call to
	 * {@link #getSourceFiles(IProgressMonitor)} to re-fetch the list of
	 * source files referenced in the executable. Digging into the binary for
	 * that list can be expensive, so we cache the results. However, if the
	 * executable is rebuilt, the cache can no longer be trusted.
	 *
	 * Note that calling this also invalidates any mappings we have cached, so
	 * there is no need to call both this method and
	 * {@link #setRemapSourceFiles(boolean)}. That latter is automatic.
	 *
	 * @since 6.0
	 */
	public void setRefreshSourceFiles(boolean refreshSourceFiles) {
		if (Trace.DEBUG_EXECUTABLES)
			Trace.getTrace().traceEntry(null, refreshSourceFiles);

		this.refreshSourceFiles = refreshSourceFiles;
	}

	public synchronized String getOriginalLocation(ITranslationUnit tu) {
		String orgLocation = remappedPaths.get(tu);
		if (orgLocation == null)
			orgLocation = tu.getLocation().toOSString();
		return orgLocation;
	}

	/**
	 * Call this to force a subsequent call to
	 * {@link #getSourceFiles(IProgressMonitor)} to remap the source files
	 * referenced in the binary. Mapping a source file means running the file
	 * specification found in the executable through any applicable source
	 * locators. Source locators are used to convert a file specification found
	 * in an executable to a usable path on the local machine. E.g., a user
	 * debugging an executable built by someone else likely needs to configure
	 * source locator(s) to point Eclipse to his local copy of the sources.
	 *
	 * <p>
	 * Remapping source paths is expensive, so we cache the results. However, if
	 * applicable source locators have been added, removed or changed, then the
	 * cache can no longer be trusted.
	 *
	 * <p>
	 * Note that we separately cache the (unmapped) file specifications
	 * referenced in the executable, as that is also expensive to fetch. Calling
	 * this method does not invalidate that cache. However, that cache can be
	 * invalidated via {@link #setRefreshSourceFiles(boolean)}, which also ends
	 * up invalidating any mappings we have cached.
	 *
	 * @since 7.0
	 */
	public void setRemapSourceFiles(boolean remapSourceFiles) {
		if (Trace.DEBUG_EXECUTABLES)
			Trace.getTrace().traceEntry(null, remapSourceFiles);
		this.remapSourceFiles = remapSourceFiles;
	}

}
