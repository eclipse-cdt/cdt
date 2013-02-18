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

package org.eclipse.cdt.managedbuilder.language.settings.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.IErrorParser2;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Abstract class for providers parsing compiler option from build command when present in build output.
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
public abstract class AbstractBuildCommandParser extends AbstractLanguageSettingsOutputScanner {
	public static final Object JOB_FAMILY_BUILD_COMMAND_PARSER = "org.eclipse.cdt.managedbuilder.AbstractBuildCommandParser"; //$NON-NLS-1$

	private static final String ATTR_PARAMETER = "parameter"; //$NON-NLS-1$
	private static final String ATTR_RESOURCE_SCOPE = "resource-scope"; //$NON-NLS-1$
	private static final String VALUE_FILE_SCOPE = "per-file"; //$NON-NLS-1$
	private static final String VALUE_FOLDER_SCOPE = "per-folder"; //$NON-NLS-1$
	private static final String VALUE_PROJECT_SCOPE = "per-project"; //$NON-NLS-1$


	private static final String LEADING_PATH_PATTERN = "\\S+[/\\\\]"; //$NON-NLS-1$
	private static final Pattern OPTIONS_PATTERN = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s][^\\s]+)))?"); //$NON-NLS-1$
	private static final int OPTION_GROUP = 0;

	public enum ResourceScope {
		FILE,
		FOLDER,
		PROJECT,
	}

	/**
	 * Note: design patterns to keep file group the same and matching {@link #FILE_GROUP}
	 */
	@SuppressWarnings("nls")
	private static final String[] COMPILER_COMMAND_PATTERN_TEMPLATES = {
		"${COMPILER_PATTERN}.*\\s" + "()([^'\"\\s]*\\.${EXTENSIONS_PATTERN})(\\s.*)?[\r\n]*", // compiling unquoted file
		"${COMPILER_PATTERN}.*\\s" + "(['\"])(.*\\.${EXTENSIONS_PATTERN})\\${COMPILER_GROUPS+1}(\\s.*)?[\r\n]*" // compiling quoted file
	};
	private static final int FILE_GROUP = 2;

	// cached value from properties, do not need to use in equals() and hashCode()
	private ResourceScope resourceScope = null;

	/**
	 * The compiler command pattern without specifying compiler options.
	 * The options are intended to be handled with option parsers,
	 * see {@link #getOptionParsers()}.
	 * This is regular expression pattern.
	 *
	 * @return the compiler command pattern.
	 */
	public String getCompilerPattern() {
		return getProperty(ATTR_PARAMETER);
	}

	/**
	 * Set compiler command pattern for the provider. See {@link #getCompilerPattern()}.
	 * @param commandPattern - value of the command pattern to set.
	 *    This is regular expression pattern.
	 */
	public void setCompilerPattern(String commandPattern) {
		setProperty(ATTR_PARAMETER, commandPattern);
	}

	/**
	 * Sub-expression for compiler command pattern accounting for spaces, quotes etc.
	 */
	@SuppressWarnings("nls")
	private String getCompilerPatternExtended() {
		String compilerPattern = getCompilerPattern();
		return "\\s*\"?("+LEADING_PATH_PATTERN+")?(" + compilerPattern + ")\"?";
	}

	/**
	 * @return resource scope of the entries, i.e. level in resource hierarchy where language settings entries
	 *    will be applied by the provider. Resource scope can be one of the following:
	 *    <br>- {@code AbstractBuildCommandParser.ResourceScope.FILE} - apply entries to the file being parsed.
	 *    <br>- {@code AbstractBuildCommandParser.ResourceScope.FOLDER} - apply entries to the enclosing folder.
	 *    <br>- {@code AbstractBuildCommandParser.ResourceScope.PROJECT} - apply entries to the project level.
	 */
	public ResourceScope getResourceScope() {
		if (resourceScope == null) {
			String scopeStr = getProperty(ATTR_RESOURCE_SCOPE);
			if (scopeStr.equals(VALUE_FILE_SCOPE)) {
				resourceScope = ResourceScope.FILE;
			} else if (scopeStr.equals(VALUE_FOLDER_SCOPE)) {
				resourceScope = ResourceScope.FOLDER;
			} else if (scopeStr.equals(VALUE_PROJECT_SCOPE)) {
				resourceScope = ResourceScope.PROJECT;
			} else {
				resourceScope = ResourceScope.FILE;
			}
		}
		return resourceScope;
	}

	/**
	 * Set resource scope of the entries, i.e. level in resource hierarchy where language settings entries
	 * will be applied by the provider.
	 *
	 * @param rcScope - resource scope can be one of the following:
	 *    <br>- {@code AbstractBuildCommandParser.ResourceScope.FILE} - apply entries to the file being parsed.
	 *    <br>- {@code AbstractBuildCommandParser.ResourceScope.FOLDER} - apply entries to the enclosing folder.
	 *    <br>- {@code AbstractBuildCommandParser.ResourceScope.PROJECT} - apply entries to the project level.
	 */
	public void setResourceScope(ResourceScope rcScope) {
		resourceScope = rcScope;
		switch (rcScope) {
		case FILE:
			setProperty(ATTR_RESOURCE_SCOPE, VALUE_FILE_SCOPE);
			break;
		case FOLDER:
			setProperty(ATTR_RESOURCE_SCOPE, VALUE_FOLDER_SCOPE);
			break;
		case PROJECT:
			setProperty(ATTR_RESOURCE_SCOPE, VALUE_PROJECT_SCOPE);
			break;
		default:
			setProperty(ATTR_RESOURCE_SCOPE, VALUE_FILE_SCOPE);
			break;
		}
	}

	@Override
	protected void setSettingEntries(List<ICLanguageSettingEntry> entries) {
		IResource rc = null;
		switch (getResourceScope()) {
		case FILE:
			rc = currentResource;
			break;
		case FOLDER:
			if (currentResource instanceof IFile) {
				rc = currentResource.getParent();
			}
			break;
		case PROJECT:
			if (currentResource != null) {
				rc = currentResource.getProject();
			}
			break;
		default:
			break;

		}

		setSettingEntries(currentCfgDescription, rc, currentLanguageId, entries);
	}

	/**
	 * Adjust count for file group taking into consideration extra groups added by {@link #getCompilerPatternExtended()}.
	 */
	private int adjustFileGroup() {
		return countGroups(getCompilerPatternExtended()) + FILE_GROUP;
	}

	/**
	 * Make search pattern for compiler command based on template.
	 */
	private String makePattern(String template) {
		@SuppressWarnings("nls")
		String pattern = template
				.replace("${COMPILER_PATTERN}", getCompilerPatternExtended())
				.replace("${EXTENSIONS_PATTERN}", getPatternFileExtensions())
				.replace("${COMPILER_GROUPS+1}", new Integer(countGroups(getCompilerPatternExtended()) + 1).toString());
		return pattern;
	}

	@Override
	protected String parseResourceName(String line) {
		if (line == null) {
			return null;
		}

		for (String template : COMPILER_COMMAND_PATTERN_TEMPLATES) {
			String pattern = makePattern(template);
			Matcher fileMatcher = Pattern.compile(pattern).matcher(line);
			if (fileMatcher.matches()) {
				int fileGroup = adjustFileGroup();
				String sourceFileName = fileMatcher.group(fileGroup);
				return sourceFileName;
			}
		}
		return null;
	}

	@Override
	protected List<String> parseOptions(String line) {
		if (line == null || currentResource == null) {
			return null;
		}

		List<String> options = new ArrayList<String>();
		Matcher optionMatcher = OPTIONS_PATTERN.matcher(line);
		while (optionMatcher.find()) {
			String option = optionMatcher.group(OPTION_GROUP);
			if (option!=null) {
				options.add(option);
			}
		}
		return options;
	}

	private void serializeLanguageSettingsInBackground() {
		ILanguageSettingsProvider wspProvider = LanguageSettingsManager.getWorkspaceProvider(getId());
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
		if (rawProvider == this) {
			// this is workspace provider
			serializeLanguageSettingsInBackground(null);
		} else {
			serializeLanguageSettingsInBackground(currentCfgDescription);
		}
	}

	@Override
	public void shutdown() {
		serializeLanguageSettingsInBackground();
		super.shutdown();
	}

	/**
	 * Trivial Error Parser which allows highlighting of output lines matching the patterns
	 * of this parser. Intended for better troubleshooting experience.
	 * Implementers are supposed to add the error parser via extension point {@code org.eclipse.cdt.core.ErrorParser}.
	 */
	protected static abstract class AbstractBuildCommandPatternHighlighter extends RegexErrorParser implements IErrorParser2 {
		/**
		 * Constructor.
		 * @param parserId - build command parser ID specified in the extension {@code org.eclipse.cdt.core.LanguageSettingsProvider}.
		 */
		public AbstractBuildCommandPatternHighlighter(String parserId) {
			init(parserId);
		}

		/**
		 * Initialize the error parser.
		 * @param parserId - language settings provider (the build command parser) ID.
		 */
		protected void init(String parserId) {
			AbstractBuildCommandParser buildCommandParser = (AbstractBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(parserId, false);
			if (buildCommandParser != null) {
				for (String template : COMPILER_COMMAND_PATTERN_TEMPLATES) {
					String pattern = buildCommandParser.makePattern(template);
					String fileExpr = "$"+buildCommandParser.adjustFileGroup(); //$NON-NLS-1$
					String descExpr = "$0"; //$NON-NLS-1$
					addPattern(new RegexErrorPattern(pattern, fileExpr, null, descExpr, null, IMarkerGenerator.SEVERITY_WARNING, true));
				}
			}
		}

		@Override
		public int getProcessLineBehaviour() {
			return KEEP_LONGLINES;
		}
	}


}
