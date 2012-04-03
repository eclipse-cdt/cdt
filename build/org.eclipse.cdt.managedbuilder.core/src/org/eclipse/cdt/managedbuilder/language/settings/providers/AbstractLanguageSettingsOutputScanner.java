/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.language.settings.providers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsLogger;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
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
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Element;

/**
 * Abstract class for providers capable to parse build output.
 *
 * @since 8.1
 */
public abstract class AbstractLanguageSettingsOutputScanner extends LanguageSettingsSerializableProvider implements ICBuildOutputParser {
	protected static final String ATTR_KEEP_RELATIVE_PATHS = "keep-relative-paths"; //$NON-NLS-1$

	protected ICConfigurationDescription currentCfgDescription = null;
	protected IWorkingDirectoryTracker cwdTracker = null;
	protected IProject currentProject = null;
	protected IResource currentResource = null;
	protected String currentLanguageId = null;

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
	protected abstract String parseResourceName(String line);

	/**
	 * Parse the line returning the list of substrings to be treated each as input to
	 * the option parsers. It is assumed that each substring presents one
	 * {@link ICLanguageSettingEntry} (for example compiler options {@code -I/path} or
	 * {@code -DMACRO=1}).
	 *
	 * @param line - one input line from the output stripped from end of line characters.
	 * @return list of substrings representing language settings entries.
	 */
	protected abstract List<String> parseOptions(String line);

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


	@Override
	public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker) throws CoreException {
		this.currentCfgDescription = cfgDescription;
		this.currentProject = cfgDescription != null ? cfgDescription.getProjectDescription().getProject() : null;
		this.cwdTracker = cwdTracker;
	}

	@Override
	public void shutdown() {
		// release resources for garbage collector
		// but keep currentCfgDescription for AbstractBuiltinSpecsDetector flow
		parsedResourceName = null;
		currentLanguageId = null;
		currentResource = null;
		cwdTracker = null;
	}

	@Override
	public boolean processLine(String line) {
		parsedResourceName = parseResourceName(line);
		currentResource = findResource(parsedResourceName);

		currentLanguageId = determineLanguage();
		if (!isLanguageInScope(currentLanguageId)) {
			return false;
		}

		/**
		 * URI of directory where the build is happening. This URI could point to a remote file-system
		 * for remote builds. Most often it is the same file-system as for currentResource but
		 * it can be different file-system (and different URI schema).
		 */
		URI buildDirURI = null;

		/**
		 * Where source tree starts if mapped. This kind of mapping is useful for example in cases when
		 * the absolute path to the source file on the remote system is simulated inside a project in the
		 * workspace.
		 * This URI is rooted on the same file-system where currentResource resides. In general this file-system
		 * (or even URI schema) does not have to match that of buildDirURI.
		 */
		URI mappedRootURI = null;

		if (isResolvingPaths) {
			mappedRootURI = getMappedRootURI(currentResource, parsedResourceName);
			buildDirURI = getBuildDirURI(mappedRootURI);
		}

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();

		List<String> options = parseOptions(line);
		if (options != null) {
			for (String option : options) {
				for (AbstractOptionParser optionParser : getOptionParsers()) {
					try {
						if (optionParser.parseOption(option)) {
							ICLanguageSettingEntry entry = null;
							if (isResolvingPaths && optionParser.isPathKind()) {
								URI baseURI = mappedRootURI;
								if (!new Path(optionParser.parsedName).isAbsolute() && buildDirURI != null) {
									baseURI = EFSExtensionManager.getDefault().append(mappedRootURI, buildDirURI.getPath());
								}
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
						ManagedBuilderCorePlugin.log(e);
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

	// TODO - temporary, remove me
	@Deprecated
	protected String getPrefixForLog() {
		String str;
		if (currentCfgDescription!= null) {
			IProject ownerProject = currentCfgDescription.getProjectDescription().getProject();
			str = ownerProject + ":" + currentCfgDescription.getName();
		} else {
			str = "[global]";
		}
		return str + ": ";
	}

	protected void setSettingEntries(List<ICLanguageSettingEntry> entries) {
		setSettingEntries(currentCfgDescription, currentResource, currentLanguageId, entries);

		// AG FIXME - temporary log to remove before CDT Juno release
		LanguageSettingsLogger.logInfo(getPrefixForLog()
				+ getClass().getSimpleName() + " collected " + (entries!=null ? ("" + entries.size()) : "null") + " entries for " + currentResource);
	}

	/**
	 * Determine a language associated with the resource.
	 *
	 * @return language ID for the resource.
	 */
	protected String determineLanguage() {
		IResource rc = currentResource;
		if (rc == null && currentProject != null && parsedResourceName != null) {
			String fileName = new Path(parsedResourceName).lastSegment().toString();
			// use handle; resource does not need to exist
			rc = currentProject.getFile("__" + fileName); //$NON-NLS-1$
		}

		if (rc == null)
			return null;

		List<String> languageIds = LanguageSettingsManager.getLanguages(rc, currentCfgDescription);
		if (languageIds.isEmpty())
			return null;

		return languageIds.get(0);
	}

	/**
	 * Determine if the language is in scope of the provider.
	 *
	 * @param languageId - language ID.
	 * @return {@code true} if the language is in scope, {@code false } otherwise.
	 */
	protected boolean isLanguageInScope(String languageId) {
		List<String> languageIds = getLanguageScope();
		return languageIds == null || languageIds.contains(languageId);
	}

	/**
	 * Resolve and create language settings path entry.
	 */
	private ICLanguageSettingEntry createResolvedPathEntry(AbstractOptionParser optionParser,
			String parsedPath, int flag, URI baseURI) {

		String resolvedPath = null;

		URI uri = determineMappedURI(parsedPath, baseURI);
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

		return optionParser.createEntry(resolvedPath, resolvedPath, flag);
	}

	/**
	 * Determine resource in the workspace corresponding to the parsed resource name.
	 */
	private IResource findResource(String parsedResourceName) {
		if (parsedResourceName == null || parsedResourceName.isEmpty()) {
			return null;
		}

		IResource sourceFile = null;

		// try ErrorParserManager
		if (cwdTracker instanceof ErrorParserManager) {
			sourceFile = ((ErrorParserManager) cwdTracker).findFileName(parsedResourceName);
		}

		// try to find absolute path in the workspace
		if (sourceFile == null && new Path(parsedResourceName).isAbsolute()) {
			URI uri = org.eclipse.core.filesystem.URIUtil.toURI(parsedResourceName);
			sourceFile = findFileForLocationURI(uri, currentProject);
		}

		// try last known current working directory from build output
		if (sourceFile == null && cwdTracker != null) {
			URI cwdURI = cwdTracker.getWorkingDirectoryURI();
			if (cwdURI != null) {
				URI uri = EFSExtensionManager.getDefault().append(cwdURI, parsedResourceName);
				sourceFile = findFileForLocationURI(uri, currentProject);
			}
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

	/**
	 * Determine current build directory considering currentResource (resource being compiled),
	 * parsedResourceName and mappedRootURI.
	 *
	 * @param mappedRootURI - root of the source tree when mapped to remote file-system.
	 * @return {@link URI} of current build directory
	 */
	protected URI getBuildDirURI(URI mappedRootURI) {
		URI buildDirURI = null;

		// try to deduce build directory from full path of currentResource and partial path of parsedResourceName
		URI cwdURI = null;
		if (currentResource != null && parsedResourceName != null && !new Path(parsedResourceName).isAbsolute()) {
			cwdURI = findBaseLocationURI(currentResource.getLocationURI(), parsedResourceName);
		}
		String cwdPath = cwdURI != null ? EFSExtensionManager.getDefault().getPathFromURI(cwdURI) : null;
		if (cwdPath != null && mappedRootURI != null) {
			buildDirURI = EFSExtensionManager.getDefault().append(mappedRootURI, cwdPath);
		} else {
			buildDirURI = cwdURI;
		}

		// if not found - try IWorkingDirectoryTracker
		if (buildDirURI == null && cwdTracker != null) {
			buildDirURI = cwdTracker.getWorkingDirectoryURI();
		}

		// if not found - try builder working directory
		if (buildDirURI == null && currentCfgDescription != null) {
			IPath pathBuilderCWD = currentCfgDescription.getBuildSetting().getBuilderCWD();
			if (pathBuilderCWD != null) {
				String builderCWD = pathBuilderCWD.toString();
				try {
					// here is a hack to overcome ${workspace_loc:/prj-name} returned by builder
					// where "/" is treated as path separator by pathBuilderCWD
					ICdtVariableManager vmanager = CCorePlugin.getDefault().getCdtVariableManager();
					builderCWD = vmanager.resolveValue(builderCWD, "", null, currentCfgDescription); //$NON-NLS-1$
				} catch (CdtVariableException e) {
					ManagedBuilderCorePlugin.log(e);
				}
				if (builderCWD != null && !builderCWD.isEmpty()) {
					buildDirURI = org.eclipse.core.filesystem.URIUtil.toURI(builderCWD);
				}
			}
		}

		// if not found - try directory of the current project
		if (buildDirURI == null && currentProject != null) {
			buildDirURI = currentProject.getLocationURI();
		}

		// if not found - try parent folder of the resource
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
	 * Determine URI on the local file-system considering possible mapping.
	 *
	 * @param pathStr - path to the resource, can be absolute or relative
	 * @param baseURI - base {@link URI} where path to the resource is rooted
	 * @return {@link URI} of the resource
	 */
	private static URI determineMappedURI(String pathStr, URI baseURI) {
		URI uri = null;

		if (baseURI == null) {
			if (new Path(pathStr).isAbsolute()) {
				uri = resolvePathFromBaseLocation(pathStr, Path.ROOT);
			}
		} else if (baseURI.getScheme().equals(EFS.SCHEME_FILE)) {
			// location on the local file-system
			IPath baseLocation = org.eclipse.core.filesystem.URIUtil.toPath(baseURI);
			// careful not to use Path here but 'pathStr' as String as we want to properly navigate symlinks
			uri = resolvePathFromBaseLocation(pathStr, baseLocation);
		} else {
			// location on a remote file-system
			IPath path = new Path(pathStr); // use canonicalized path here, in particular replace all '\' with '/' for Windows paths
			URI remoteUri = EFSExtensionManager.getDefault().append(baseURI, path.toString());
			if (remoteUri != null) {
				String localPath = EFSExtensionManager.getDefault().getMappedPath(remoteUri);
				if (localPath != null) {
					uri = org.eclipse.core.filesystem.URIUtil.toURI(localPath);
				}
			}
		}

		if (uri == null) {
			// if everything fails just wrap string to URI
			uri = org.eclipse.core.filesystem.URIUtil.toURI(pathStr);
		}
		return uri;
	}

	/**
	 * Determine which resource in workspace is the best fit to parsedName passed.
	 */
	private static IResource resolveResourceInWorkspace(String parsedName, IProject preferredProject, Set<String> referencedProjectsNames) {
		IPath path = new Path(parsedName);
		if (path.equals(new Path(".")) || path.equals(new Path(".."))) { //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}

		// prefer current project
		if (preferredProject != null) {
			List<IResource> result = findPathInFolder(path, preferredProject);
			int size = result.size();
			if (size == 1) { // found the one
				return result.get(0);
			} else if (size > 1) { // ambiguous
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
					if (size == 1 && rc == null) {
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
					if (size == 1 && rc == null) {
						rc = result.get(0);
					} else if (size > 0) {
						// ambiguous
						rc = null;
						break;
					}
				}
			}
			if (rc != null) {
				return rc;
			}
		}

		// not found or ambiguous
		return null;
	}

	/**
	 * Find all resources in the folder which might be represented by relative path passed.
	 */
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

	/**
	 * Find resource in the workspace for a given URI with a preference for the resource
	 * to reside in the given project.
	 */
	private static IResource findFileForLocationURI(URI uri, IProject preferredProject) {
		IResource sourceFile = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource[] resources = root.findFilesForLocationURI(uri);
		if (resources.length > 0) {
			sourceFile = resources[0];
			if (preferredProject != null) {
				for (IResource rc : resources) {
					if (rc.getProject().equals(preferredProject)) {
						sourceFile = rc;
						break;
					}
				}
			}
		}
		return sourceFile;
	}

	/**
	 * Find base location of the file, i.e. location of the directory which
	 * results from removing trailing relativeFileName from fileURI or
	 * {@code null} if fileURI doesn't represent relativeFileName.
	 */
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
						int pos = path.lastIndexOf("/" + lastSegment); //$NON-NLS-1$
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
					fileURI.getPort(), path + '/', fileURI.getQuery(), fileURI.getFragment());
		} catch (URISyntaxException e) {
			// It should be valid URI here or something is wrong
			ManagedBuilderCorePlugin.log(e);
		}

		return cwdURI;
	}

	/**
	 * In case when absolute path is mapped to the source tree in a project
	 * this function will try to figure mapping and return "mapped root",
	 * i.e URI where the root path would be mapped. The mapped root will be
	 * used to prepend to other "absolute" paths where appropriate.
	 *
	 * @param resource - a resource referred by parsed path
	 * @param parsedResourceName - path as appears in the output
	 * @return mapped path as URI
	 */
	protected URI getMappedRootURI(IResource resource, String parsedResourceName) {
		if (resource == null) {
			return null;
		}

		URI resourceURI = resource.getLocationURI();
		String mappedRoot = "/"; //$NON-NLS-1$

		if (parsedResourceName != null) {
			IPath parsedSrcPath = new Path(parsedResourceName);
			if (parsedSrcPath.isAbsolute()) {
				IPath absResourcePath = resource.getLocation();
				int absSegmentsCount = absResourcePath.segmentCount();
				int relSegmentsCount = parsedSrcPath.segmentCount();
				if (absSegmentsCount >= relSegmentsCount) {
					IPath ending = absResourcePath.removeFirstSegments(absSegmentsCount - relSegmentsCount);
					ending = ending.setDevice(parsedSrcPath.getDevice()).makeAbsolute();
					if (ending.equals(parsedSrcPath.makeAbsolute())) {
						// mappedRoot here is parsedSrcPath with removed parsedResourceName trailing segments,
						// i.e. if absResourcePath="/path/workspace/project/file.c" and parsedResourceName="project/file.c"
						// then mappedRoot="/path/workspace/"
						mappedRoot = absResourcePath.removeLastSegments(relSegmentsCount).toString();
					}
				}
			}
		}
		// this creates URI with schema and other components from resourceURI but path as mappedRoot
		URI uri = EFSExtensionManager.getDefault().createNewURIFromPath(resourceURI, mappedRoot);
		return uri;
	}

	/**
	 * The manipulations here are done to resolve "../" navigation for symbolic links where "link/.." cannot
	 * be collapsed as it must follow the real file-system path. {@link java.io.File#getCanonicalPath()} deals
	 * with that correctly but {@link Path} or {@link URI} try to normalize the path which would be incorrect
	 * here.
	 */
	private static URI resolvePathFromBaseLocation(String name, IPath baseLocation) {
		String pathName = name;
		if (baseLocation != null && !baseLocation.isEmpty()) {
			pathName = pathName.replace(File.separatorChar, '/');
			String device = new Path(pathName).getDevice();
			if (device==null || device.equals(baseLocation.getDevice())) {
				if (device != null && device.length() > 0) {
					pathName = pathName.substring(device.length());
				}

				baseLocation = baseLocation.addTrailingSeparator();
				if (pathName.startsWith("/")) { //$NON-NLS-1$
					pathName = pathName.substring(1);
				}
				pathName = baseLocation.toString() + pathName;
			}
		}

		try {
			File file = new File(pathName);
			file = file.getCanonicalFile();
			return file.toURI();
		} catch (IOException e) {
			// if error just leave it as is
		}

		return org.eclipse.core.filesystem.URIUtil.toURI(pathName);
	}

	/**
	 * Return a resource in workspace corresponding the given {@link URI} preferable residing in
	 * the provided project.
	 */
	private static IResource findResourceForLocationURI(URI uri, int kind, IProject preferredProject) {
		if (uri == null)
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

	/**
	 * Return a resource in workspace corresponding the given folder {@link URI} preferable residing in
	 * the provided project.
	 */
	private static IResource findContainerForLocationURI(URI uri, IProject preferredProject) {
		IResource resource = null;
		IResource[] resources = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(uri);
		for (IResource rc : resources) {
			if ((rc instanceof IProject || rc instanceof IFolder)) { // treat IWorkspaceRoot as non-workspace path
				if (rc.getProject().equals(preferredProject)) {
					resource = rc;
					break;
				}
				if (resource == null) {
					resource = rc; // to be deterministic the first qualified resource has preference
				}
			}
		}
		return resource;
	}

	/**
	 * Get location on the local file-system considering possible mapping by {@link EFSExtensionManager}.
	 */
	private static IPath getFilesystemLocation(URI uri) {
		if (uri == null)
			return null;

		String pathStr = EFSExtensionManager.getDefault().getMappedPath(uri);
		uri = org.eclipse.core.filesystem.URIUtil.toURI(pathStr);

		try {
			File file = new java.io.File(uri);
			String canonicalPathStr = file.getCanonicalPath();
			return new Path(canonicalPathStr);
		} catch (Exception e) {
			ManagedBuilderCorePlugin.log(e);
		}
		return null;
	}

	protected static int countGroups(String str) {
		@SuppressWarnings("nls")
		int count = str.replaceAll("[^\\(]", "").length();
		return count;
	}

	@Override
	public Element serializeAttributes(Element parentElement) {
		Element elementProvider = super.serializeAttributes(parentElement);
		elementProvider.setAttribute(ATTR_KEEP_RELATIVE_PATHS, Boolean.toString( ! isResolvingPaths ));
		return elementProvider;
	}

	@Override
	public void loadAttributes(Element providerNode) {
		super.loadAttributes(providerNode);

		String expandRelativePathsValue = XmlUtil.determineAttributeValue(providerNode, ATTR_KEEP_RELATIVE_PATHS);
		if (expandRelativePathsValue!=null)
			isResolvingPaths = ! Boolean.parseBoolean(expandRelativePathsValue);
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
