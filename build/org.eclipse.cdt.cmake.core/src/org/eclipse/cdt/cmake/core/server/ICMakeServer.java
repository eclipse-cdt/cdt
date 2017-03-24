/*******************************************************************************
 * Copyright (c) 2017 IAR Systems AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jesper Eskilson (IAR Systems AB) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.server;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Java interface on top of the functionality provided by cmake-server, allowing
 * clients to obtain information from cmake-server using plain Java method calls
 * instead of JSON objects.
 * <p>
 * Methods which return data in their reply will return a corresponding object.
 * <p>
 * Methods which do not have any data on their reply apart from the cmake-server
 * protocol bookkeeping, will typically have a void return value, but they will
 * not return until the reply from the server is received.
 * <p>
 * Methods will typically throw {@link CMakeServerException} if the server
 * response is "error".
 * <p>
 * See https://cmake.org/cmake/help/latest/manual/cmake-server.7.html for more
 * details on the semantics of the method calls.
 * 
 * Example:
 * 
 * <pre>
 * ICMakeServer server = CMakeServerFactory.createServer();
 * server.addListener(new StdoutCMakeServerListener());
 * server.startServer(Optional.of("/path/to/cmake"), true);
 * server.handshake("/path/to/my/sources", "/tmp/build", "Ninja");
 * server.configure();
 * server.compute();
 * server.build(pb -> pb.inheritIO());
 * </pre>
 *
 * @since 1.1
 */
public interface ICMakeServer extends AutoCloseable {

	/**
	 * Adds a listener to receive messages/signals from the server.
	 * 
	 * @param listener
	 */
	void addListener(ICMakeServerListener listener);

	/**
	 * Removes a listener.
	 * 
	 * @param listener
	 */
	void removeListener(ICMakeServerListener listener);

	/**
	 * Starts the server using the specified backend. When this method returns,
	 * the "hello" message has been received, and the next method to use should
	 * be one of the "handshake" methods.
	 * 
	 * @param backend
	 *            The server backend to use. This controls the low-level access
	 *            to the cmake-server process. See {@link ICMakeServerBackend}.
	 * @throws CMakeServerException
	 */
	void startServer(ICMakeServerBackend backend) throws CMakeServerException;

	/**
	 * Returns the list of supported protocols.
	 * 
	 * @return
	 * @throws CMakeServerException
	 */
	List<CMakeProtocol> getSupportedProtocolVersions() throws CMakeServerException;

	/**
	 * Performs a handshake. Corresponds to the "handshake" command. This method
	 * must be the first call performed after the {@link #startServer()} call.
	 */
	void handshake(File sourceDirectory, File buildDirectory, String generator, String extraGenerator, String platform,
			String toolset) throws CMakeServerException;

	/**
	 * Performs a handshake, using only the required arguments.
	 * 
	 * @throws CMakeServerException
	 */
	void handshake(File sourceDirectory, File buildDirectory, String generator) throws CMakeServerException;

	/**
	 * Return the global settings. Corresponds to the "globalSettings" command.
	 * 
	 * @throws CMakeServerException
	 */
	CMakeGlobalSettings getGlobalSettings() throws CMakeServerException;

	/**
	 * Writes a global setting. Corresponds to the "setGlobalSettings" command.
	 * All of the global settings which are writable are booleans.
	 * 
	 * @param attr
	 * @param value
	 * @throws CMakeServerException
	 */
	void setGlobalSetting(String attr, boolean value) throws CMakeServerException;

	/**
	 * Corresponds to the "configure" command.
	 * 
	 * @throws CMakeServerException
	 */
	void configure() throws CMakeServerException;

	void configure(Map<String, String> cacheArguments) throws CMakeServerException;

	/**
	 * Corresponds to the "compute" command;
	 * 
	 * @throws CMakeServerException
	 */
	void compute() throws CMakeServerException;

	/**
	 * Corresponds to the "codemodel" command.
	 *
	 * @return
	 * @throws CMakeServerException
	 */
	CMakeCodeModel getCodeModel() throws CMakeServerException;

	/**
	 * Corresponds to the "cmakeInputs" command.
	 * 
	 * @throws CMakeServerException
	 */
	CMakeInputs getCMakeInputs() throws CMakeServerException;

	/**
	 * Corresponds to the "cache" command.
	 *
	 * @return
	 * @throws CMakeServerException
	 */
	CMakeCache getCMakeCache() throws CMakeServerException;

	/**
	 * Corresponds to the "fileSystemWatchers" command.
	 * 
	 * @return
	 * @throws CMakeServerException
	 */
	CMakeFileSystemWatchers getFileSystemWatchers() throws CMakeServerException;

}
