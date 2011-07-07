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


import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;

public class GCCBuildCommandParser extends AbstractBuildCommandParser implements
		ILanguageSettingsEditableProvider {
	@SuppressWarnings("nls")
	static final AbstractOptionParser[] optionParsers = {
			new IncludePathOptionParser("-I\\s*([\"'])(.*)\\1", "$2"),
			new IncludePathOptionParser("-I\\s*([^\\s\"']*)", "$1"),
			new IncludeFileOptionParser("-include\\s*([\"'])(.*)\\1", "$2"),
			new IncludeFileOptionParser("-include\\s*([^\\s\"']*)", "$1"),
			new MacroOptionParser("-D\\s*([\"'])([^=]*)(=(.*))?\\1", "$2", "$4"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=(\\\\([\"']))(.*?)\\2", "$1", "$3$4$3"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=([\"'])(.*?)\\2", "$1", "$3"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)(=([^\\s\"']*))?", "$1", "$3"),
			new MacroOptionParser("-U\\s*([^\\s=\"']*)", "$1", ICSettingEntry.UNDEFINED),
			new MacroFileOptionParser("-macros\\s*([\"'])(.*)\\1", "$2"),
			new MacroFileOptionParser("-macros\\s*([^\\s\"']*)", "$1"),
			new LibraryPathOptionParser("-L\\s*([\"'])(.*)\\1", "$2"),
			new LibraryPathOptionParser("-L\\s*([^\\s\"']*)", "$1"),
			new LibraryFileOptionParser("-l\\s*([^\\s\"']*)", "lib$1.a"), };

	@Override
	protected AbstractOptionParser[] getOptionParsers() {
		return optionParsers;
	}
	
	@Override
	public GCCBuildCommandParser cloneShallow() throws CloneNotSupportedException {
		return (GCCBuildCommandParser) super.cloneShallow();
	}

	@Override
	public GCCBuildCommandParser clone() throws CloneNotSupportedException {
		return (GCCBuildCommandParser) super.clone();
	}

	public static class GCCBuildCommandPatternHighlighter extends AbstractBuildCommandParser.AbstractBuildCommandPatternHighlighter {
		// ID of the parser taken from the extension point
		private static final String GCC_BUILD_COMMAND_PARSER_EXT = "org.eclipse.cdt.make.core.build.command.parser.gcc"; //$NON-NLS-1$

		public GCCBuildCommandPatternHighlighter() {
			super(GCC_BUILD_COMMAND_PARSER_EXT);
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			GCCBuildCommandPatternHighlighter that = new GCCBuildCommandPatternHighlighter();
			that.setId(getId());
			that.setName(getName());
			for (RegexErrorPattern pattern : getPatterns()) {
				that.addPattern((RegexErrorPattern)pattern.clone());
			}
			return that;
		}
	}

}
