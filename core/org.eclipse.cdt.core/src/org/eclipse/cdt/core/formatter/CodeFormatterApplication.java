/*******************************************************************************
 *  Copyright (c) 2005, 2017 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Mustafa Yuecel - initial implementation - adapted from org.eclipse.jdt.core
 *******************************************************************************/
package org.eclipse.cdt.core.formatter;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ToolFactory;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * Implements an Eclipse Application for org.eclipse.cdt.core.CodeFormatter.
 *
 * <p>On MacOS, when invoked using the Eclipse executable, the "user.dir" property is set to the folder
 * in which the eclipse.ini file is located. This makes it harder to use relative paths to point to the
 * files to be formatted or the configuration file to use to set the code formatter's options.</p>
 *
 * <p>There are a couple improvements that could be made: 1. Make a list of all the
 * files first so that a file does not get formatted twice. 2. Use a text based
 * progress monitor for output.</p>
 *
 * @author Ben Konrath <bkonrath@redhat.com>
 * @since 6.4
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CodeFormatterApplication implements IApplication {

	private static final String ARG_CONFIG = "-config"; //$NON-NLS-1$

	private static final String ARG_HELP = "-help"; //$NON-NLS-1$

	private static final String ARG_QUIET = "-quiet"; //$NON-NLS-1$

	private static final String ARG_VERBOSE = "-verbose"; //$NON-NLS-1$

	private String configName;

	@SuppressWarnings("rawtypes")
	private Map options = null;

	private boolean quiet = false;

	private boolean verbose = false;

	/**
	 * Display the command line usage message.
	 */
	private void displayHelp() {
		System.out.println(Messages.CommandLineUsage);
	}

	private void displayHelp(String message) {
		System.err.println(message);
		System.out.println();
		displayHelp();
	}

	/**
	 * Recursively format the C/C++ source code that is contained in the
	 * directory rooted at dir.
	 */
	private void formatDirTree(File dir, CodeFormatter codeFormatter) {

		File[] files = dir.listFiles();
		if (files == null)
			return;

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				formatDirTree(file, codeFormatter);
			} else if (hasSuitableFileExtension(file)) {
				formatFile(file, codeFormatter);
			}
		}
	}

	/**
	 * Format the given C/C++ source file.
	 */
	private void formatFile(File file, CodeFormatter codeFormatter) {
		IDocument doc = new Document();
		try {
			// read the file
			if (this.verbose) {
				System.out.println(Messages.bind(Messages.CommandLineFormatting, file.getAbsolutePath()));
			}
			String contents = new String(Files.readAllBytes(file.toPath())); // compatible only with Java 7+
			// format the file (the meat and potatoes)
			doc.set(contents);
			TextEdit edit = codeFormatter.format(CodeFormatter.K_TRANSLATION_UNIT, contents, 0, contents.length(), 0,
					null);
			if (edit != null) {
				edit.apply(doc);
			} else {
				System.err.println(Messages.bind(Messages.FormatProblem, file.getAbsolutePath()));
				return;
			}

			// write the file
			final BufferedWriter out = new BufferedWriter(new FileWriter(file));
			try {
				out.write(doc.get());
				out.flush();
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					/* ignore */
				}
			}
		} catch (IOException e) {
			String errorMessage = Messages.bind(Messages.CaughtException, "IOException", e.getLocalizedMessage()); //$NON-NLS-1$
			System.err.println(Messages.bind(Messages.ExceptionSkip, errorMessage));
		} catch (BadLocationException e) {
			String errorMessage = Messages.bind(Messages.CaughtException, "BadLocationException", //$NON-NLS-1$
					e.getLocalizedMessage());
			System.err.println(Messages.bind(Messages.ExceptionSkip, errorMessage));
		}
	}

	private File[] processCommandLine(String[] argsArray) {

		ArrayList<String> args = new ArrayList<>();
		for (int i = 0, max = argsArray.length; i < max; i++) {
			args.add(argsArray[i]);
		}
		int index = 0;
		final int argCount = argsArray.length;

		final int DEFAULT_MODE = 0;
		final int CONFIG_MODE = 1;

		int mode = DEFAULT_MODE;

		ArrayList<File> filesToFormat = new ArrayList<>();

		loop: while (index < argCount) {
			String currentArg = argsArray[index++];

			switch (mode) {
			case DEFAULT_MODE:
				if (ARG_HELP.equals(currentArg)) {
					displayHelp();
					return null;
				}
				if (ARG_VERBOSE.equals(currentArg)) {
					this.verbose = true;
					continue loop;
				}
				if (ARG_QUIET.equals(currentArg)) {
					this.quiet = true;
					continue loop;
				}
				if (ARG_CONFIG.equals(currentArg)) {
					mode = CONFIG_MODE;
					continue loop;
				}
				// the current arg should be a file or a directory name
				File file = new File(currentArg);
				if (file.exists()) {
					filesToFormat.add(file);
				} else {
					String canonicalPath;
					try {
						canonicalPath = file.getCanonicalPath();
					} catch (IOException e2) {
						canonicalPath = file.getAbsolutePath();
					}
					String errorMsg = file.isAbsolute() ? Messages.bind(Messages.CommandLineErrorFile, canonicalPath)
							: Messages.bind(Messages.CommandLineErrorFileTryFullPath, canonicalPath);
					displayHelp(errorMsg);
					return null;
				}
				break;
			case CONFIG_MODE:
				this.configName = currentArg;
				this.options = readConfig(currentArg);
				boolean validConfig = false;
				if (this.options != null && !this.options.isEmpty()) {
					@SuppressWarnings("unchecked")
					Iterator<String> it = this.options.keySet().iterator();
					// at least 1 property key starts with org.eclipse.cdt.core.formatter
					while (it.hasNext()) {
						if (it.next().startsWith(this.getClass().getPackage().getName())) {
							validConfig = true;
							break;
						}
					}
				}
				if (!validConfig) {
					displayHelp(Messages.bind(Messages.CommandLineErrorConfig, currentArg));
					return null;
				}
				mode = DEFAULT_MODE;
				continue loop;
			}
		}

		if (this.quiet && this.verbose) {
			displayHelp(Messages.bind(Messages.CommandLineErrorQuietVerbose, new String[] { ARG_QUIET, ARG_VERBOSE }));
			return null;
		}
		if (filesToFormat.isEmpty()) {
			displayHelp(Messages.CommandLineErrorFileDir);
			return null;
		}
		return filesToFormat.toArray(new File[0]);
	}

	/**
	 * Return a Properties file representing the options that are in the
	 * specified configuration file.
	 */
	private Properties readConfig(String filename) {
		File configFile = new File(filename);
		try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(configFile))) {
			final Properties formatterOptions = new Properties();
			formatterOptions.load(stream);
			return formatterOptions;
		} catch (IOException e) {
			String canonicalPath = null;
			try {
				canonicalPath = configFile.getCanonicalPath();
			} catch (IOException e2) {
				canonicalPath = configFile.getAbsolutePath();
			}
			String errorMessage;
			if (!configFile.exists() && !configFile.isAbsolute()) {
				errorMessage = Messages.bind(Messages.ConfigFileNotFoundErrorTryFullPath,
						new Object[] { canonicalPath, System.getProperty("user.dir") //$NON-NLS-1$
						});

			} else {
				errorMessage = Messages.bind(Messages.ConfigFileReadingError, canonicalPath);
			}
			System.err.println(errorMessage);
		}
		return null;
	}

	/**
	 * Checks if given file name ends with e.g. .c .cpp or .h
	 */
	private boolean hasSuitableFileExtension(File file) {
		IContentType ct = CCorePlugin.getContentType(null, file.getName());
		if (ct != null) {
			String id = ct.getId();
			if (CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id) || CCorePlugin.CONTENT_TYPE_CHEADER.equals(id)
					|| CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(id) || CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Runs the code formatter application
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		File[] filesToFormat = processCommandLine(
				(String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS));

		if (filesToFormat == null) {
			return IApplication.EXIT_OK;
		}

		if (!this.quiet) {
			if (this.configName != null) {
				System.out.println(Messages.bind(Messages.CommandLineConfigFile, this.configName));
			}
			System.out.println(Messages.CommandLineStart);
		}

		@SuppressWarnings("unchecked")
		final CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(this.options);
		// format the list of files and/or directories
		for (int i = 0, max = filesToFormat.length; i < max; i++) {
			final File file = filesToFormat[i];
			if (file.isDirectory()) {
				formatDirTree(file, codeFormatter);
			} else {
				formatFile(file, codeFormatter);
			}
		}
		if (!this.quiet) {
			System.out.println(Messages.CommandLineDone);
		}

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// do nothing
	}
}
