/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.core.scannerconfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.w3c.dom.Element;

public abstract class AbstractLanguageSettingsOutputScanner extends LanguageSettingsSerializable implements
		ILanguageSettingsOutputScanner {

	protected static final String ATTR_EXPAND_RELATIVE_PATHS = "expand-relative-paths"; //$NON-NLS-1$

	protected ICConfigurationDescription currentCfgDescription = null;
	protected IProject currentProject = null;
	protected IResource currentResource = null;
	protected String currentLanguageId = null;

	protected ErrorParserManager errorParserManager = null;
	protected String parsedResourceName = null;
	protected boolean isResolvingPaths = true;

	protected static abstract class AbstractOptionParser {
		protected final Pattern pattern;
		protected final String patternStr;
		protected String nameExpression;
		protected String valueExpression;
		protected int extraFlag = 0;
		protected int kind = 0;
		private String parsedName;
		private String parsedValue;

		public AbstractOptionParser(int kind, String pattern, String nameExpression, String valueExpression, int extraFlag) {
			this.kind = kind;
			this.patternStr = pattern;
			this.nameExpression = nameExpression;
			this.valueExpression = valueExpression;
			this.extraFlag = extraFlag;

			this.pattern = Pattern.compile(pattern);
		}

		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return (ICLanguageSettingEntry) CDataUtil.createEntry(kind, name, value, null, flag | extraFlag);
		}

		/**
		 * TODO: explain
		 */
		protected String extractOption(String input) {
			@SuppressWarnings("nls")
			String option = input.replaceFirst("(" + patternStr + ").*", "$1");
			return option;
		}

		protected String parseStr(Matcher matcher, String str) {
			if (str != null)
				return matcher.replaceAll(str);
			return null;
		}

		protected boolean isPathKind() {
			return kind == ICSettingEntry.INCLUDE_PATH || kind == ICSettingEntry.INCLUDE_FILE
					|| kind == ICSettingEntry.MACRO_FILE || kind == ICSettingEntry.LIBRARY_PATH;
		}

		public boolean parseOption(String option) {
			String opt = extractOption(option);
			Matcher matcher = pattern.matcher(opt);
			boolean isMatch = matcher.matches();
			if (isMatch) {
				parsedName = parseStr(matcher, nameExpression);
				parsedValue = parseStr(matcher, valueExpression);
			}
			return isMatch;
		}

	}

	protected static class IncludePathOptionParser extends AbstractOptionParser {
		public IncludePathOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.INCLUDE_PATH, pattern, nameExpression, nameExpression, 0);
		}
		public IncludePathOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.INCLUDE_PATH, pattern, nameExpression, nameExpression, extraFlag);
		}
	}

	protected static class IncludeFileOptionParser extends AbstractOptionParser {
		public IncludeFileOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.INCLUDE_FILE, pattern, nameExpression, nameExpression, 0);
		}
		public IncludeFileOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.INCLUDE_FILE, pattern, nameExpression, nameExpression, extraFlag);
		}
	}

	protected static class MacroOptionParser extends AbstractOptionParser {
		public MacroOptionParser(String pattern, String nameExpression, String valueExpression) {
			super(ICLanguageSettingEntry.MACRO, pattern, nameExpression, valueExpression, 0);
		}
		public MacroOptionParser(String pattern, String nameExpression, String valueExpression, int extraFlag) {
			super(ICLanguageSettingEntry.MACRO, pattern, nameExpression, valueExpression, extraFlag);
		}
		public MacroOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.MACRO, pattern, nameExpression, null, extraFlag);
		}
	}

	protected static class MacroFileOptionParser extends AbstractOptionParser {
		public MacroFileOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.MACRO_FILE, pattern, nameExpression, nameExpression, 0);
		}
		public MacroFileOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.MACRO_FILE, pattern, nameExpression, nameExpression, extraFlag);
		}
	}

	protected static class LibraryPathOptionParser extends AbstractOptionParser {
		public LibraryPathOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.LIBRARY_PATH, pattern, nameExpression, nameExpression, 0);
		}
		public LibraryPathOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.LIBRARY_PATH, pattern, nameExpression, nameExpression, extraFlag);
		}
	}

	protected static class LibraryFileOptionParser extends AbstractOptionParser {
		public LibraryFileOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.LIBRARY_FILE, pattern, nameExpression, nameExpression, 0);
		}
		public LibraryFileOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.LIBRARY_FILE, pattern, nameExpression, nameExpression, extraFlag);
		}
	}

	/**
	 * Parse the line returning the resource name as appears in the output.
	 * This is the resource where {@link ICLanguageSettingEntry} list is being added.
	 * 
	 * @param line - one input line from the output stripped from end of line characters.
	 * @return the resource name as appears in the output or {@code null}.
	 *    Note that {@code null} can have different semantics and can mean "no resource found"
	 *    or "applicable to any resource". By default "no resource found" is used in this
	 *    abstract class but extenders can handle otherwise.
	 */
	protected abstract String parseForResourceName(String line);

	/**
	 * Parse the line returning the list of substrings to be treated each as input to
	 * the option parsers. It is assumed that each substring presents one
	 * {@link ICLanguageSettingEntry} (for example compiler options {@code -I/path} or
	 * {@code -DMACRO=1}.
	 * 
	 * @param line - one input line from the output stripped from end of line characters.
	 * @return list of substrings representing language settings entries.
	 */
	protected abstract List<String> parseForOptions(String line);

	/**
	 * @return array of option parsers defining how to parse a string to
	 * {@link ICLanguageSettingEntry}.
	 * See {@link AbstractOptionParser} and its specific extenders.
	 */
	protected abstract AbstractOptionParser[] getOptionParsers();

	public boolean isResolvingPaths() {
		return isResolvingPaths;
	}

	public void setResolvingPaths(boolean resolvePaths) {
		this.isResolvingPaths = resolvePaths;
	}


	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		currentCfgDescription = cfgDescription;
		currentProject = cfgDescription != null ? cfgDescription.getProjectDescription().getProject() : null;
	}

	public boolean processLine(String line) {
		return processLine(line, null);
	}

	public void shutdown() {
	}

	public boolean processLine(String line, ErrorParserManager epm) {
		errorParserManager = epm;
		parsedResourceName = parseForResourceName(line);
		
		currentLanguageId = determineLanguage(parsedResourceName);
		if (!isLanguageInScope(currentLanguageId))
			return false;

		currentResource = findResource(parsedResourceName);

		/**
		 * Where source tree starts if mapped. This kind of mapping is useful for example in cases when
		 * the absolute path to the source file on the remote system is simulated inside a project in the
		 * workspace.
		 */
		URI mappedRootURI = null;
		URI buildDirURI = null;

		if (isResolvingPaths) {
			if (currentResource!=null) {
				mappedRootURI = getMappedRootURI(currentResource, parsedResourceName);
			}
			buildDirURI = getBuildDirURI(mappedRootURI);
		}

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		
		List<String> options = parseForOptions(line);
		if (options!=null) {
			for (String option : options) {
				for (AbstractOptionParser optionParser : getOptionParsers()) {
					try {
						if (optionParser.parseOption(option)) {
							ICLanguageSettingEntry entry = null;
							if (isResolvingPaths && optionParser.isPathKind()) {
								URI baseURI = new Path(optionParser.parsedName).isAbsolute() ? mappedRootURI : buildDirURI;
								entry = createResolvedPathEntry(optionParser, optionParser.parsedName, 0, baseURI);
							} else {
								entry = optionParser.createEntry(optionParser.parsedName, optionParser.parsedValue, 0);
							}
	
							if (entry != null && !entries.contains(entry)) {
								entries.add(entry);
								break;
							}
						}
					} catch (Throwable e) {
						// protect from rogue parsers extending this class
						MakeCorePlugin.log(e);
					}
				}
			}
			if (entries.size() > 0) {
				setSettingEntries(entries);
			} else {
				setSettingEntries(null);
			}
		}
		return false;
	}

	protected void setSettingEntries(List<ICLanguageSettingEntry> entries) {
		setSettingEntries(currentCfgDescription, currentResource, currentLanguageId, entries);
		
		// TODO - for debugging only, eventually remove
		IStatus status = new Status(IStatus.INFO, MakeCorePlugin.PLUGIN_ID, getClass().getSimpleName()
				+ " collected " + (entries!=null ? ("" + entries.size()) : "null") + " entries for " + currentResource);
		MakeCorePlugin.log(status);
	}

	protected String determineLanguage(String parsedResourceName) {
		if (parsedResourceName==null)
			return null;
	
		String fileName = new Path(parsedResourceName).lastSegment().toString();
		IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType contentType = manager.findContentTypeFor(fileName);
		if (contentType==null)
			return null;
		
		ILanguage lang = LanguageManager.getInstance().getLanguage(contentType);
		if (lang==null)
			return null;
		
		return lang.getId();
	}

	protected boolean isLanguageInScope(String languageId) {
		List<String> languageIds = getLanguageScope();
		return languageIds == null || languageIds.contains(languageId);
	}

	protected String getPatternFileExtensions() {
		IContentTypeManager manager = Platform.getContentTypeManager();
	
		Set<String> fileExts = new HashSet<String>();
	
		IContentType contentTypeCpp = manager.getContentType("org.eclipse.cdt.core.cxxSource"); //$NON-NLS-1$
		fileExts.addAll(Arrays.asList(contentTypeCpp.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));
	
		IContentType contentTypeC = manager.getContentType("org.eclipse.cdt.core.cSource"); //$NON-NLS-1$
		fileExts.addAll(Arrays.asList(contentTypeC.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));
	
		String pattern = expressionLogicalOr(fileExts);
	
		return pattern;
	}

	private ICLanguageSettingEntry createResolvedPathEntry(AbstractOptionParser optionParser,
			String parsedPath, int flag, URI baseURI) {
		
		ICLanguageSettingEntry entry;
		String resolvedPath = null;
		
		URI uri = determineURI(parsedPath, baseURI);
		IResource rc = null;
		if (uri != null && uri.isAbsolute()) {
			rc = findResourceForLocationURI(uri, optionParser.kind, currentProject);
		}
		if (rc != null) {
			IPath path = rc.getFullPath();
			resolvedPath = path.toString();
			flag = flag | ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED;
		} else {
			IPath path = getFilesystemLocation(uri);
			if (path != null && new File(path.toString()).exists()) {
				resolvedPath = path.toString();
			}
			if (resolvedPath == null) {
				Set<String> referencedProjectsNames = new LinkedHashSet<String>();
				if (currentCfgDescription!=null) {
					Map<String,String> refs = currentCfgDescription.getReferenceInfo();
					referencedProjectsNames.addAll(refs.keySet());
				}
				IResource resource = resolveResourceInWorkspace(parsedPath, currentProject, referencedProjectsNames);
				if (resource != null) {
					path = resource.getFullPath();
					resolvedPath = path.toString();
					flag = flag | ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED;
				}
			}
			if (resolvedPath==null && path!=null) {
				resolvedPath = path.toString();
			}
		}
		
		if (resolvedPath==null) {
			resolvedPath = parsedPath;
		}

		entry = optionParser.createEntry(resolvedPath, resolvedPath, flag);
		return entry;
	}

	private IResource findResource(String parsedResourceName) {
		if (parsedResourceName==null)
			return null;
		
		IResource sourceFile = null;
		
		// try ErrorParserManager
		if (errorParserManager != null) {
			sourceFile = errorParserManager.findFileName(parsedResourceName);
		}
		// try to find absolute path in the workspace
		if (sourceFile == null && new Path(parsedResourceName).isAbsolute()) {
			URI uri = org.eclipse.core.filesystem.URIUtil.toURI(parsedResourceName);
			sourceFile = findFileForLocationURI(uri, currentProject);
		}
		// try path relative to build dir from configuration
		if (sourceFile == null && currentCfgDescription != null) {
			IPath builderCWD = currentCfgDescription.getBuildSetting().getBuilderCWD();
			if (builderCWD!=null) {
				IPath path = builderCWD.append(parsedResourceName);
				URI uri = org.eclipse.core.filesystem.URIUtil.toURI(path);
				sourceFile = findFileForLocationURI(uri, currentProject);
			}
		}
		// try path relative to the project
		if (sourceFile == null && currentProject != null) {
			sourceFile = currentProject.findMember(parsedResourceName);
		}
		return sourceFile;
	}

	private URI getBuildDirURI(URI mappedRootURI) {
		URI buildDirURI = null;
		
		URI cwdURI = null;
		if (currentResource!=null && parsedResourceName!=null && !new Path(parsedResourceName).isAbsolute()) {
			cwdURI = findBaseLocationURI(currentResource.getLocationURI(), parsedResourceName);
		}
		if (cwdURI == null && errorParserManager != null) {
			cwdURI = errorParserManager.getWorkingDirectoryURI();
		}
	
		String cwdPath = cwdURI != null ? EFSExtensionManager.getDefault().getPathFromURI(cwdURI) : null;
		if (cwdPath != null && mappedRootURI != null) {
			buildDirURI = EFSExtensionManager.getDefault().append(mappedRootURI, cwdPath);
		} else {
			buildDirURI = cwdURI;
		}
	
		if (buildDirURI == null && currentCfgDescription != null) {
			IPath builderCWD = currentCfgDescription.getBuildSetting().getBuilderCWD();
			buildDirURI = org.eclipse.core.filesystem.URIUtil.toURI(builderCWD);
		}
		
		if (buildDirURI == null && currentProject != null) {
			buildDirURI = currentProject.getLocationURI();
		}
		
		if (buildDirURI == null && currentResource != null) {
			IContainer container;
			if (currentResource instanceof IContainer) {
				container = (IContainer) currentResource;
			} else {
				container = currentResource.getParent();
			}
			buildDirURI = container.getLocationURI();
		}
		return buildDirURI;
	}

	/**
	 * Determine URI appending to baseURI when possible.
	 * 
	 * @param pathStr - path to the resource, can be absolute or relative
	 * @param baseURI - base {@link URI} where path to the resource is rooted
	 * @return {@link URI} of the resource
	 */
	private static URI determineURI(String pathStr, URI baseURI) {
		URI uri = null;
	
		if (baseURI==null) {
			if (new Path(pathStr).isAbsolute()) {
				uri = resolvePathFromBaseLocation(pathStr, Path.ROOT);
			}
		} else if (baseURI.getScheme().equals(EFS.SCHEME_FILE)) {
			// location on the local filesystem
			IPath baseLocation = org.eclipse.core.filesystem.URIUtil.toPath(baseURI);
			// careful not to use Path here but 'pathStr' as String as we want to properly navigate symlinks
			uri = resolvePathFromBaseLocation(pathStr, baseLocation);
		} else {
			// use canonicalized path here, in particular replace all '\' with '/' for Windows paths
			Path path = new Path(pathStr);
			uri = EFSExtensionManager.getDefault().append(baseURI, path.toString());
		}
	
		if (uri == null) {
			// if everything fails just wrap string to URI
			uri = org.eclipse.core.filesystem.URIUtil.toURI(pathStr);
		}
		return uri;
	}

	private static IResource resolveResourceInWorkspace(String parsedName, IProject preferredProject, Set<String> referencedProjectsNames) {
		IPath path = new Path(parsedName);
		if (path.equals(new Path(".")) || path.equals(new Path(".."))) { //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	
		// prefer current project
		if (preferredProject!=null) {
			List<IResource> result = findPathInFolder(path, preferredProject);
			int size = result.size();
			if (size==1) { // found the one
				return result.get(0);
			} else if (size>1) { // ambiguous
				return null;
			}
		}
	
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		// then prefer referenced projects
		if (referencedProjectsNames.size() > 0) {
			IResource rc = null;
			for (String prjName : referencedProjectsNames) {
				IProject prj = root.getProject(prjName);
				if (prj.isOpen()) {
					List<IResource> result = findPathInFolder(path, prj);
					int size = result.size();
					if (size==1 && rc==null) {
						rc = result.get(0);
					} else if (size > 0) {
						// ambiguous
						rc = null;
						break;
					}
				}
			}
			if (rc!=null) {
				return rc;
			}
		}
	
		// then check all other projects in workspace
		IProject[] projects = root.getProjects();
		if (projects.length > 0) {
			IResource rc = null;
			for (IProject prj : projects) {
				if (!prj.equals(preferredProject) && !referencedProjectsNames.contains(prj.getName()) && prj.isOpen()) {
					List<IResource> result = findPathInFolder(path, prj);
					int size = result.size();
					if (size==1 && rc==null) {
						rc = result.get(0);
					} else if (size > 0) {
						// ambiguous
						rc = null;
						break;
					}
				}
			}
			if (rc!=null) {
				return rc;
			}
		}
		
		// not found or ambiguous
		return null;
	}

	private static List<IResource> findPathInFolder(IPath path, IContainer folder) {
		List<IResource> paths = new ArrayList<IResource>();
		IResource resource = folder.findMember(path);
		if (resource != null) {
			paths.add(resource);
		}
	
		try {
			for (IResource res : folder.members()) {
				if (res instanceof IContainer) {
					paths.addAll(findPathInFolder(path, (IContainer) res));
				}
			}
		} catch (CoreException e) {
			// ignore
		}
	
		return paths;
	}

	private static IResource findFileForLocationURI(URI uri, IProject preferredProject) {
		IResource sourceFile;
		IResource result = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource[] resources = root.findFilesForLocationURI(uri);
		if (resources.length > 0) {
			result = resources[0];
			if (preferredProject!=null) {
				for (IResource rc : resources) {
					if (rc.getProject().equals(preferredProject)) {
						result = rc;
						break;
					}
				}
			}
		}
		sourceFile = result;
		return sourceFile;
	}

	private static URI findBaseLocationURI(URI fileURI, String relativeFileName) {
		URI cwdURI = null;
		String path = fileURI.getPath();
	
		String[] segments = relativeFileName.split("[/\\\\]"); //$NON-NLS-1$
	
		// start removing segments from the end of the path
		for (int i = segments.length - 1; i >= 0; i--) {
			String lastSegment = segments[i];
			if (lastSegment.length() > 0 && !lastSegment.equals(".")) { //$NON-NLS-1$
				if (lastSegment.equals("..")) { //$NON-NLS-1$
					// navigating ".." in the other direction is ambiguous, bailing out
					return null;
				} else {
					if (path.endsWith("/" + lastSegment)) { //$NON-NLS-1$
						int pos = path.lastIndexOf(lastSegment);
						path = path.substring(0, pos);
						continue;
					} else {
						// ouch, relativeFileName does not match fileURI, bailing out
						return null;
					}
				}
			}
		}
	
		try {
			cwdURI = new URI(fileURI.getScheme(), fileURI.getUserInfo(), fileURI.getHost(),
					fileURI.getPort(), path, fileURI.getQuery(), fileURI.getFragment());
		} catch (URISyntaxException e) {
			// It should be valid URI here or something is wrong
			MakeCorePlugin.log(e);
		}
	
		return cwdURI;
	}

	/**
	 * In case when absolute path is mapped to the source tree in a project
	 * this function will try to figure mapping and return "mapped root",
	 * i.e URI where the root path would be mapped. The mapped root will be
	 * used to prepend to other "absolute" paths where appropriate.
	 * 
	 * @param sourceFile - a resource referred by parsed path
	 * @param parsedResourceName - path as appears in the output
	 * @return mapped path as URI
	 */
	private static URI getMappedRootURI(IResource sourceFile, String parsedResourceName) {
		URI fileURI = sourceFile.getLocationURI();
		String mappedRoot = "/"; //$NON-NLS-1$
		
		if (parsedResourceName!=null) {
			IPath parsedSrcPath = new Path(parsedResourceName);
			if (parsedSrcPath.isAbsolute()) {
				IPath absPath = sourceFile.getLocation();
				int absSegmentsCount = absPath.segmentCount();
				int relSegmentsCount = parsedSrcPath.segmentCount();
				if (absSegmentsCount >= relSegmentsCount) {
					IPath ending = absPath.removeFirstSegments(absSegmentsCount - relSegmentsCount);
					ending = ending.setDevice(parsedSrcPath.getDevice()).makeAbsolute();
					if (ending.equals(parsedSrcPath.makeAbsolute())) {
						mappedRoot = absPath.removeLastSegments(relSegmentsCount).toString();
					}
				}
			}
		}
		URI uri = EFSExtensionManager.getDefault().createNewURIFromPath(fileURI, mappedRoot);
		return uri;
	}

	/**
	 * The manipulations here are done to resolve "../" navigation for symbolic links where "link/.." cannot
	 * be collapsed as it must follow the real filesystem path. {@link java.io.File#getCanonicalPath()} deals
	 * with that correctly but {@link Path} or {@link URI} try to normalize the path which would be incorrect
	 * here.
	 */
	private static URI resolvePathFromBaseLocation(String name, IPath baseLocation) {
		String pathName = name;
		if (baseLocation != null && !baseLocation.isEmpty()) {
			String device = new Path(pathName).getDevice();
			if (device != null && device.length() > 0) {
				pathName = pathName.substring(device.length());
			}
			pathName = pathName.replace(File.separatorChar, '/');
	
			baseLocation = baseLocation.addTrailingSeparator();
			if (pathName.startsWith("/")) { //$NON-NLS-1$
				pathName = pathName.substring(1);
			}
			pathName = baseLocation.toString() + pathName;
		}
	
		try {
			File file = new File(pathName);
			file = file.getCanonicalFile();
			return file.toURI();
		} catch (IOException e) {
			// if error just leave it as is
		}
	
		URI uri = org.eclipse.core.filesystem.URIUtil.toURI(pathName);
		return uri;
	}

	private static IResource findResourceForLocationURI(URI uri, int kind, IProject preferredProject) {
		if (uri==null)
			return null;
		
		IResource resource = null;
	
		switch (kind) {
		case ICSettingEntry.INCLUDE_PATH:
		case ICSettingEntry.LIBRARY_PATH:
			resource = findContainerForLocationURI(uri, preferredProject);
			break;
		case ICSettingEntry.INCLUDE_FILE:
		case ICSettingEntry.MACRO_FILE:
			resource = findFileForLocationURI(uri, preferredProject);
			break;
		}
	
		return resource;
	}

	private static IResource findContainerForLocationURI(URI uri, IProject preferredProject) {
		IResource resource = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource[] resources = root.findContainersForLocationURI(uri);
		if (resources.length > 0) {
			for (IResource rc : resources) {
				if ((rc instanceof IProject || rc instanceof IFolder)) { // treat IWorkspaceRoot as non-workspace path
					IProject prj = rc instanceof IProject ? (IProject)rc : rc.getProject();
					if (prj.equals(preferredProject)) {
						resource = rc;
						break;
					}
					if (resource==null) {
						resource=rc; // to be deterministic the first qualified resource has preference
					}
				}
			}
		}
		return resource;
	}

	private static IPath getFilesystemLocation(URI uri) {
		if (uri==null)
			return null;
		
		// EFSExtensionManager mapping
		String pathStr = EFSExtensionManager.getDefault().getMappedPath(uri);
		uri = org.eclipse.core.filesystem.URIUtil.toURI(pathStr);
	
		try {
			File file = new java.io.File(uri);
			String canonicalPathStr = file.getCanonicalPath();
			return new Path(canonicalPathStr);
		} catch (Exception e) {
			MakeCorePlugin.log(e);
		}
		return null;
	}

	@SuppressWarnings("nls")
	private static String expressionLogicalOr(Set<String> fileExts) {
		String pattern = "(";
		for (String ext : fileExts) {
			if (pattern.length() != 1)
				pattern += "|";
			pattern += "(" + Pattern.quote(ext) + ")";
			ext = ext.toUpperCase();
			if (!fileExts.contains(ext)) {
				pattern += "|(" + Pattern.quote(ext) + ")";
			}
		}
		pattern += ")";
		return pattern;
	}
	
	protected static int countGroups(String str) {
		@SuppressWarnings("nls")
		int count = str.replaceAll("[^\\(]", "").length();
		return count;
	}

	@Override
	public Element serialize(Element parentElement) {
		Element elementProvider = super.serialize(parentElement);
		elementProvider.setAttribute(ATTR_EXPAND_RELATIVE_PATHS, Boolean.toString(isResolvingPaths));
		return elementProvider;
	}
	
	@Override
	public void load(Element providerNode) {
		super.load(providerNode);
		
		String expandRelativePathsValue = XmlUtil.determineAttributeValue(providerNode, ATTR_EXPAND_RELATIVE_PATHS);
		if (expandRelativePathsValue!=null)
			isResolvingPaths = Boolean.parseBoolean(expandRelativePathsValue);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isResolvingPaths ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractLanguageSettingsOutputScanner other = (AbstractLanguageSettingsOutputScanner) obj;
		if (isResolvingPaths != other.isResolvingPaths)
			return false;
		return true;
	}

}
