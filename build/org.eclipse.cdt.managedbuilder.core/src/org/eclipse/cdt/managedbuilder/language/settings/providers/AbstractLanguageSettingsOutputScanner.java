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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.XmlUtil;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.w3c.dom.Element;

/**
 * Abstract class for language settings providers capable to parse build output.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class interface is not stable yet as
 * it is not currently (CDT 8.1, Juno) clear how it may need to be used in future.
 * There is no guarantee that this API will work or that it will remain the same.
 * Please do not use this API without consulting with the CDT team.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
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

	/**
	 * Abstract class defining common functionality for option parsers.
	 * The purpose of this parser is to parse a portion of string representing
	 * a single option and create a language settings entry out of it.
	 *
	 * See {@link GCCBuildCommandParser} for an example how to define the parsers.
	 */
	protected static abstract class AbstractOptionParser {
		private final int kind;
		private final String patternStr;
		private final Pattern pattern;
		private final String nameExpression;
		private final String valueExpression;
		private final int extraFlag;

		private String parsedName;
		private String parsedValue;

		/**
		 * Constructor.
		 *
		 * @param kind - kind of language settings entries being parsed by the parser.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 * @param valueExpression - capturing group expression defining value of an entry.
		 * @param extraFlag - extra-flag to add while creating language settings entry.
		 */
		public AbstractOptionParser(int kind, String pattern, String nameExpression, String valueExpression, int extraFlag) {
			this.kind = kind;
			this.patternStr = pattern;
			this.nameExpression = nameExpression;
			this.valueExpression = valueExpression;
			this.extraFlag = extraFlag;

			this.pattern = Pattern.compile(pattern);
		}

		/**
		 * Create language settings entry of appropriate kind and considering extra-flag passed in constructor.
		 *
		 * @param name - name of language settings entry.
		 * @param value - value of language settings entry.
		 * @param flag - flag to set. Note that the flag will be amended with the extra-flag defined in constructor.
		 * @return new language settings entry.
		 */
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return (ICLanguageSettingEntry) CDataUtil.createEntry(kind, name, value, null, flag | extraFlag);
		}

		/**
		 * Check if the king of option parsed by parser is "file".
		 *
		 * @return {@code true} if the kind is file, {@code false} otherwise.
		 */
		public boolean isForFile() {
			return kind == ICSettingEntry.INCLUDE_FILE || kind == ICSettingEntry.MACRO_FILE;
		}

		/**
		 * Check if the king of option parsed by parser is "folder".
		 *
		 * @return {@code true} if the kind is folder, {@code false} otherwise.
		 */
		public boolean isForFolder() {
			return kind == ICSettingEntry.INCLUDE_PATH || kind == ICSettingEntry.LIBRARY_PATH;
		}

		/**
		 * Return value represented by the capturing group expression.
		 */
		private String parseStr(Matcher matcher, String str) {
			if (str != null)
				return matcher.replaceAll(str);
			return null;
		}

		/**
		 * Test for a match and parse a portion of input string representing a single option
		 * to retrieve name and value.
		 *
		 * @param optionString - an option to test and parse, possibly with an argument.
		 * @return {@code true} if the option is a match to parser's regular expression
		 *    or {@code false} otherwise.
		 */
		public boolean parseOption(String optionString) {
			// get rid of extra text at the end (for example file name could be confused for an argument)
			@SuppressWarnings("nls")
			String option = optionString.replaceFirst("(" + patternStr + ").*", "$1");

			Matcher matcher = pattern.matcher(option);
			boolean isMatch = matcher.matches();
			if (isMatch) {
				parsedName = parseStr(matcher, nameExpression);
				parsedValue = parseStr(matcher, valueExpression);
			}
			return isMatch;
		}
	}

	/**
	 * Implementation of {@link AbstractOptionParser} for include path options parsing.
	 */
	protected static class IncludePathOptionParser extends AbstractOptionParser {
		public IncludePathOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.INCLUDE_PATH, pattern, nameExpression, nameExpression, 0);
		}
		public IncludePathOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.INCLUDE_PATH, pattern, nameExpression, nameExpression, extraFlag);
		}
	}

	/**
	 * Implementation of {@link AbstractOptionParser} for include file options parsing.
	 */
	protected static class IncludeFileOptionParser extends AbstractOptionParser {
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 */
		public IncludeFileOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.INCLUDE_FILE, pattern, nameExpression, nameExpression, 0);
		}
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 * @param extraFlag - extra-flag to add while creating language settings entry.
		 */
		public IncludeFileOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.INCLUDE_FILE, pattern, nameExpression, nameExpression, extraFlag);
		}
	}

	/**
	 * Implementation of {@link AbstractOptionParser} for macro options parsing.
	 */
	protected static class MacroOptionParser extends AbstractOptionParser {
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 * @param valueExpression - capturing group expression defining value of an entry.
		 */
		public MacroOptionParser(String pattern, String nameExpression, String valueExpression) {
			super(ICLanguageSettingEntry.MACRO, pattern, nameExpression, valueExpression, 0);
		}
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 * @param valueExpression - capturing group expression defining value of an entry.
		 * @param extraFlag - extra-flag to add while creating language settings entry.
		 */
		public MacroOptionParser(String pattern, String nameExpression, String valueExpression, int extraFlag) {
			super(ICLanguageSettingEntry.MACRO, pattern, nameExpression, valueExpression, extraFlag);
		}
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 * @param extraFlag - extra-flag to add while creating language settings entry.
		 */
		public MacroOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.MACRO, pattern, nameExpression, null, extraFlag);
		}
	}

	/**
	 * Implementation of {@link AbstractOptionParser} for macro file options parsing.
	 */
	protected static class MacroFileOptionParser extends AbstractOptionParser {
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 */
		public MacroFileOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.MACRO_FILE, pattern, nameExpression, nameExpression, 0);
		}
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 * @param extraFlag - extra-flag to add while creating language settings entry.
		 */
		public MacroFileOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.MACRO_FILE, pattern, nameExpression, nameExpression, extraFlag);
		}
	}

	/**
	 * Implementation of {@link AbstractOptionParser} for library path options parsing.
	 */
	protected static class LibraryPathOptionParser extends AbstractOptionParser {
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 */
		public LibraryPathOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.LIBRARY_PATH, pattern, nameExpression, nameExpression, 0);
		}
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 * @param extraFlag - extra-flag to add while creating language settings entry.
		 */
		public LibraryPathOptionParser(String pattern, String nameExpression, int extraFlag) {
			super(ICLanguageSettingEntry.LIBRARY_PATH, pattern, nameExpression, nameExpression, extraFlag);
		}
	}

	/**
	 * Implementation of {@link AbstractOptionParser} for library file options parsing.
	 */
	protected static class LibraryFileOptionParser extends AbstractOptionParser {
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 */
		public LibraryFileOptionParser(String pattern, String nameExpression) {
			super(ICLanguageSettingEntry.LIBRARY_FILE, pattern, nameExpression, nameExpression, 0);
		}
		/**
		 * Constructor.
		 * @param pattern - regular expression pattern being parsed by the parser.
		 * @param nameExpression - capturing group expression defining name of an entry.
		 * @param extraFlag - extra-flag to add while creating language settings entry.
		 */
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

	/**
	 * @return {@code true} when the provider tries to resolve relative or remote paths
	 * to the existing paths in the workspace or local file-system using certain heuristics.
	 */
	public boolean isResolvingPaths() {
		return isResolvingPaths;
	}

	/**
	 * Enable or disable resolving  relative or remote paths to the existing paths
	 * in the workspace or local file-system.
	 *
	 * @param resolvePaths - set {@code true} to enable or {@code false} to disable
	 *    resolving paths. When this parameter is set to {@code false} the paths will
	 *    be kept as they appear in the build output.
	 */
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
			AbstractOptionParser[] optionParsers = getOptionParsers();
			for (String option : options) {
				for (AbstractOptionParser optionParser : optionParsers) {
					try {
						if (optionParser.parseOption(option)) {
							ICLanguageSettingEntry entry = null;
							if (isResolvingPaths && (optionParser.isForFile() || optionParser.isForFolder())) {
								URI baseURI = mappedRootURI;
								if (buildDirURI != null && !new Path(optionParser.parsedName).isAbsolute()) {
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
						@SuppressWarnings("nls")
						String msg = "Exception trying to parse option [" + option + "], class " + getClass().getSimpleName();
						ManagedBuilderCorePlugin.log(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, msg, e));
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

		// try IWorkingDirectoryTracker
		if (buildDirURI == null && cwdTracker != null) {
			buildDirURI = cwdTracker.getWorkingDirectoryURI();
		}

		// try builder working directory
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

		// try directory of the current project
		if (buildDirURI == null && currentProject != null) {
			buildDirURI = currentProject.getLocationURI();
		}

		// try parent folder of the resource
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
	 * Sets language settings entries for current configuration description, current resource
	 * and current language ID.
	 *
	 * @param entries - language settings entries to set.
	 */
	protected void setSettingEntries(List<ICLanguageSettingEntry> entries) {
		setSettingEntries(currentCfgDescription, currentResource, currentLanguageId, entries);
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
			// It should be valid URI here or something is really wrong
			ManagedBuilderCorePlugin.log(e);
		}

		return cwdURI;
	}

	/**
	 * The manipulations here are done to resolve problems such as "../" navigation for symbolic links where
	 * "link/.." cannot be collapsed as it must follow the real file-system path. {@link java.io.File#getCanonicalPath()}
	 * deals with that correctly but {@link Path} or {@link URI} try to normalize the path which would be incorrect here.
	 * Another issue being resolved here is fixing drive letters in URI syntax.
	 */
	private static URI resolvePathFromBaseLocation(String pathStr0, IPath baseLocation) {
		String pathStr = pathStr0;
		if (baseLocation != null && !baseLocation.isEmpty()) {
			pathStr = pathStr.replace(File.separatorChar, '/');
			String device = new Path(pathStr).getDevice();
			if (device == null || device.equals(baseLocation.getDevice())) {
				if (device != null && device.length() > 0) {
					pathStr = pathStr.substring(device.length());
				}

				baseLocation = baseLocation.addTrailingSeparator();
				if (pathStr.startsWith("/")) { //$NON-NLS-1$
					pathStr = pathStr.substring(1);
				}
				pathStr = baseLocation.toString() + pathStr;
			}
		}

		try {
			File file = new File(pathStr);
			file = file.getCanonicalFile();
			URI uri = file.toURI();
			if (file.exists()) {
				return uri;
			}

			IPath path0 = new Path(pathStr0);
			if (!path0.isAbsolute()) {
				return uri;
			}

			String device = path0.getDevice();
			if (device == null || device.isEmpty()) {
				// Avoid spurious adding of drive letters on Windows
				pathStr = path0.setDevice(null).toString();
			} else {
				// On Windows "C:/folder/" -> "/C:/folder/"
				if (pathStr.charAt(0) != IPath.SEPARATOR) {
					pathStr = IPath.SEPARATOR + pathStr;
				}
			}

			return new URI(uri.getScheme(), uri.getAuthority(), pathStr, uri.getQuery(), uri.getFragment());

		} catch (Exception e) {
			// if error will leave it as is
			ManagedBuilderCorePlugin.log(e);
		}

		return org.eclipse.core.filesystem.URIUtil.toURI(pathStr);
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
			if (rc != null) {
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
	 * Get location on the local file-system considering possible mapping by {@link EFSExtensionManager}.
	 */
	private static IPath getFilesystemLocation(URI uri) {
		if (uri == null)
			return null;

		String pathStr = EFSExtensionManager.getDefault().getMappedPath(uri);
		uri = org.eclipse.core.filesystem.URIUtil.toURI(pathStr);

		if (uri != null && uri.isAbsolute()) {
			try {
				File file = new java.io.File(uri);
				String canonicalPathStr = file.getCanonicalPath();
				if (new Path(pathStr).getDevice() == null) {
					return new Path(canonicalPathStr).setDevice(null);
				}
				return new Path(canonicalPathStr);
			} catch (Exception e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
		return null;
	}

	/**
	 * Resolve and create language settings path entry.
	 */
	private ICLanguageSettingEntry createResolvedPathEntry(AbstractOptionParser optionParser,
			String parsedPath, int flag, URI baseURI) {

		String resolvedPath = null;
		int resolvedFlag = 0;

		URI uri = determineMappedURI(parsedPath, baseURI);
		IResource rc = null;

		// Try to resolve in the workspace
		if (uri != null && uri.isAbsolute()) {
			if (optionParser.isForFolder()) {
				rc = findContainerForLocationURI(uri, currentProject);
			} else if (optionParser.isForFile()) {
				rc = findFileForLocationURI(uri, currentProject);
			}
			if (rc != null) {
				resolvedPath = rc.getFullPath().toString();
				resolvedFlag = flag | ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED;
			}
		}

		// Try to resolve on the file-system
		if (resolvedPath == null) {
			IPath path = getFilesystemLocation(uri);
			if (path != null && new File(path.toString()).exists()) {
				resolvedPath = path.toString();
				resolvedFlag = flag;
			}
			if (resolvedPath == null) {
				Set<String> referencedProjectsNames = new LinkedHashSet<String>();
				if (currentCfgDescription!=null) {
					Map<String,String> refs = currentCfgDescription.getReferenceInfo();
					referencedProjectsNames.addAll(refs.keySet());
				}
				IResource resource = resolveResourceInWorkspace(parsedPath, currentProject, referencedProjectsNames);
				if (resource != null) {
					resolvedPath = resource.getFullPath().toString();
					resolvedFlag = flag | ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED;
				}
			}
			if (resolvedPath == null && path != null) {
				resolvedPath = path.toString();
				resolvedFlag = flag;
			}
		}

		// if cannot resolve keep parsed path
		if (resolvedPath == null) {
			resolvedPath = parsedPath;
			resolvedFlag = flag;
		}

		return optionParser.createEntry(resolvedPath, resolvedPath, resolvedFlag);
	}

	/**
	 * Count how many groups are present in regular expression.
	 * The implementation is simplistic but should be sufficient for the cause.
	 *
	 * @param str - regular expression to count the groups.
	 * @return number of the groups (groups are enclosed in round brackets) present.
	 */
	protected static int countGroups(String str) {
		@SuppressWarnings("nls")
		int count = str.replaceAll("[^\\(]", "").length();
		return count;
	}

	/**
	 * Helper method to construct logical "or" to be used inside regular expressions.
	 */
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

	/**
	 * Construct regular expression to find any file extension for C or C++.
	 * Returns expression shaped in form of "((cpp)|(c++)|(c))".
	 *
	 * @return regular expression for searching C/C++ file extensions.
	 */
	protected String getPatternFileExtensions() {
		IContentTypeManager manager = Platform.getContentTypeManager();

		Set<String> fileExts = new HashSet<String>();

		IContentType contentTypeCpp = manager.getContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE);
		fileExts.addAll(Arrays.asList(contentTypeCpp.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));

		IContentType contentTypeC = manager.getContentType(CCorePlugin.CONTENT_TYPE_CSOURCE);
		fileExts.addAll(Arrays.asList(contentTypeC.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));

		String pattern = expressionLogicalOr(fileExts);

		return pattern;
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
