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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICConsoleParser;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IErrorParser2;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Abstract class for providers parsing compiler option from build command when it
 * is present in build output.
 *
 * Note: IErrorParser interface is used here to work around {@link ConsoleOutputSniffer} having
 * no access from CDT core to build packages. TODO - elaborate?
 *
 * @since 7.2
 */
public abstract class AbstractBuildCommandParser extends AbstractLanguageSettingsOutputScanner
		implements ICConsoleParser, IErrorParser {
	public static final Object JOB_FAMILY_BUILD_COMMAND_PARSER = "org.eclipse.cdt.make.core.scannerconfig.AbstractBuildCommandParser";
	private static final String ATTR_PARAMETER = "parameter"; //$NON-NLS-1$

	private static final String LEADING_PATH_PATTERN = "\\S+[/\\\\]"; //$NON-NLS-1$
	private static final Pattern OPTIONS_PATTERN = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s][^\\s]+)))?"); //$NON-NLS-1$
	private static final int OPTION_GROUP = 0;

	/**
	 * Note: design patterns to keep file group the same and matching {@link #FILE_GROUP}
	 */
	@SuppressWarnings("nls")
	private static final String[] PATTERN_TEMPLATES = {
		"${COMPILER_PATTERN}.*\\s" + "()([^'\"\\s]*\\.${EXTENSIONS_PATTERN})(\\s.*)?[\r\n]*", // compiling unquoted file
		"${COMPILER_PATTERN}.*\\s" + "(['\"])(.*\\.${EXTENSIONS_PATTERN})\\${COMPILER_GROUPS+1}(\\s.*)?[\r\n]*" // compiling quoted file
	};
	private static final int FILE_GROUP = 2;


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

	@SuppressWarnings("nls")
	private String getCompilerPatternExtended() {
		String compilerPattern = getCompilerPattern();
		return "\\s*\"?("+LEADING_PATH_PATTERN+")?(" + compilerPattern + ")\"?";
	}

	private int adjustFileGroup() {
		return countGroups(getCompilerPatternExtended()) + FILE_GROUP;
	}

	private String makePattern(String template) {
		@SuppressWarnings("nls")
		String pattern = template
				.replace("${COMPILER_PATTERN}", getCompilerPatternExtended())
				.replace("${EXTENSIONS_PATTERN}", getPatternFileExtensions())
				.replace("${COMPILER_GROUPS+1}", new Integer(countGroups(getCompilerPatternExtended()) + 1).toString());
		return pattern;
	}

	@Override
	protected String parseForResourceName(String line) {
		if (line==null) {
			return null;
		}

		for (String template : PATTERN_TEMPLATES) {
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
	protected List<String> parseForOptions(String line) {
		if (line==null || currentResource==null) {
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

	@Override
	public boolean processLine(String line) {
		return processLine(line, null);
	}

	// This is redundant but let us keep it here to navigate in java code easier
	@Override
	public boolean processLine(String line, ErrorParserManager epm) {
		return super.processLine(line, epm);
	}

	@Override
	public void shutdown() {
		scheduleSerializingJob(currentCfgDescription);
		super.shutdown();
	}


	private void scheduleSerializingJob(final ICConfigurationDescription cfgDescription) {
		Job job = new Job("Serialize CDT language settings entries") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return serializeLanguageSettings(cfgDescription);
			}
			@Override
			public boolean belongsTo(Object family) {
				return family == JOB_FAMILY_BUILD_COMMAND_PARSER;
			}
		};

		ISchedulingRule rule = null;
		if (currentProject != null) {
			IFolder settingsFolder = currentProject.getFolder(".settings");
			if (!settingsFolder.exists()) {
				try {
					settingsFolder.create(true, true, null);
					if (settingsFolder.isAccessible())
						rule = currentProject.getFile(".settings/language.settings.xml");
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
		}
		if (rule == null) {
			rule = ResourcesPlugin.getWorkspace().getRoot();
		}
		job.setRule(rule);
		job.schedule();
	}

	/**
	 * Trivial Error Parser which allows highlighting of output lines matching the patterns
	 * of this parser. Intended for better troubleshooting experience.
	 * Implementers are supposed to add the error parser as an extension. Initialize with
	 * build command parser extension ID.
	 */
	protected static abstract class AbstractBuildCommandPatternHighlighter extends RegexErrorParser implements IErrorParser2 {
		public AbstractBuildCommandPatternHighlighter(String buildCommandParserPluginExtension) {
			init(buildCommandParserPluginExtension);
		}

		protected void init(String buildCommandParserId) {
			AbstractBuildCommandParser buildCommandParser = (AbstractBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(buildCommandParserId, false);
			if (buildCommandParser != null) {
				for (String template : PATTERN_TEMPLATES) {
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
