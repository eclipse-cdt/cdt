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

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser2;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;

public abstract class AbstractBuildCommandParser extends AbstractLanguageSettingsOutputScanner implements
		ILanguageSettingsBuildOutputScanner {

	private static final Pattern OPTIONS_PATTERN = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s][^\\s]+)))?"); //$NON-NLS-1$
	private static final int OPTION_GROUP = 0;
	
	/**
	 * Note: design patterns to keep file group the same and matching {@link #FILE_GROUP}
	 */
	@SuppressWarnings("nls")
	private static final String[] PATTERN_TEMPLATES = {
		"\\s*\"?${COMPILER_PATTERN}\"?.*\\s" + "()([^'\"\\s]*\\.${EXTENSIONS_PATTERN})(\\s.*)?[\r\n]*", // compiling unquoted file
		"\\s*\"?${COMPILER_PATTERN}\"?.*\\s" + "(['\"])(.*\\.${EXTENSIONS_PATTERN})\\${COMPILER_GROUPS+1}(\\s.*)?[\r\n]*" // compiling quoted file
	};
	private static final int FILE_GROUP = 2;
	
	
	@SuppressWarnings("nls")
	private String getCompilerCommandPattern() {
		String parameter = getCustomParameter();
		return "(" + parameter + ")";
	}

	private int adjustFileGroup() {
		return countGroups(getCompilerCommandPattern()) + FILE_GROUP;
	}

	private String makePattern(String template) {
		@SuppressWarnings("nls")
		String pattern = template
				.replace("${COMPILER_PATTERN}", getCompilerCommandPattern())
				.replace("${EXTENSIONS_PATTERN}", getPatternFileExtensions())
				.replace("${COMPILER_GROUPS+1}", new Integer(countGroups(getCompilerCommandPattern()) + 1).toString());
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

	// This is redundant but let us keep it here to navigate in java code easier
	@Override
	public boolean processLine(String line, ErrorParserManager epm) {
		return super.processLine(line, epm);
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
			AbstractBuildCommandParser buildCommandParser = (AbstractBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(buildCommandParserId);
			for (String template : PATTERN_TEMPLATES) {
				String pattern = buildCommandParser.makePattern(template);
				String fileExpr = "$"+buildCommandParser.adjustFileGroup(); //$NON-NLS-1$
				String descExpr = "$0"; //$NON-NLS-1$
				addPattern(new RegexErrorPattern(pattern, fileExpr, null, descExpr, null, IMarkerGenerator.SEVERITY_WARNING, true));
			}
		}

		public int getProcessLineBehaviour() {
			return KEEP_LONGLINES;
		}
	}


}
