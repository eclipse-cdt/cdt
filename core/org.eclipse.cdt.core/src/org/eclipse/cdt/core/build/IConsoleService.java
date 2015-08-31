package org.eclipse.cdt.core.build;

import java.io.IOException;

import org.eclipse.core.resources.IFolder;

/**
 * This may be temporary. It's uses the TextConsole's parsing and hyperlink
 * framework to parse build output for errors.
 * 
 * TODO Should we replace all CDT build consoles with this.
 * 
 * @since 5.12
 */
public interface IConsoleService {

	/**
	 * Display the stdout and stderr of the process in the console. Use the
	 * console parsers to parse that output to mark errors and warnings and
	 * such. The build directory helps to find resources for markers.
	 * 
	 * @param process
	 * @param consoleParsers
	 * @param buildDirectory
	 * @throws IOException
	 */
	void monitor(Process process, CConsoleParser[] consoleParsers, IFolder buildDirectory) throws IOException;

	/**
	 * Write a message on the console stdout.
	 * 
	 * @param msg
	 * @throws IOException
	 */
	void writeOutput(String msg) throws IOException;

	/**
	 * Write a message on the console stderr.
	 * 
	 * @param msg
	 * @throws IOException
	 */
	void writeError(String msg) throws IOException;

}
