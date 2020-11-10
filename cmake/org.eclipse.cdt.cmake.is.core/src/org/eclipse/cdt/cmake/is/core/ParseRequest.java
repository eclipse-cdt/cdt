/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core;

import java.util.Objects;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IFile;

/** Holds the arguments used to create a {@link CompileCommandsJsonParser}.
 *
 * @author weber
 */
public final class ParseRequest {
	private final IFile compileCommandsJson;
	private final IIndexerInfoConsumer indexerInfoConsumer;
	private final ICommandLauncher launcher;
	private final IConsole console;

	/** Creates a new ParseRequest object.
	 *
	 * @param compileCommandsJsonFile  the file to parse
	 * @param indexerInfoConsumer the object that receives the indexer relevant
	 *                            information for each source file
	 * @param launcher the launcher to run the compiler for built-ins detection.
	 *                 Should be capable to run in docker container, if build in
	 *                 container is configured for the project.
	 * @param console  the console to print the compiler output during built-ins
	 *                 detection to or <code>null</code> if no console output is requested.
	 *                 Ignored if workspace preferences indicate that no console output is wanted.
	 */
	public ParseRequest(IFile compileCommandsJsonFile, IIndexerInfoConsumer indexerInfoConsumer,
			ICommandLauncher launcher, IConsole console) {
		this.compileCommandsJson = Objects.requireNonNull(compileCommandsJsonFile, "compileCommandsJsonFile"); //$NON-NLS-1$
		this.indexerInfoConsumer = Objects.requireNonNull(indexerInfoConsumer, "indexerInfoConsumer"); //$NON-NLS-1$
		this.launcher = Objects.requireNonNull(launcher, "launcher"); //$NON-NLS-1$
		this.console = console;
	}

	/** Gets the 'compile_commands.json' file to parse.
	 */
	public IFile getFile() {
		return compileCommandsJson;
	}

	/** Gets the object that receives the indexer relevant
	 *                            information for each source file
	 */
	public IIndexerInfoConsumer getIndexerInfoConsumer() {
		return indexerInfoConsumer;
	}

	/** Gets the launcher to run the compiler for built-ins detection.
	 */
	public ICommandLauncher getLauncher() {
		return launcher;
	}

	/** Gets the console to print the compiler output during built-ins detection to.
	 *
	 * @return the console or <code>null</code> if no console output is requested.
	 */
	public IConsole getConsole() {
		return console;
	}
}
