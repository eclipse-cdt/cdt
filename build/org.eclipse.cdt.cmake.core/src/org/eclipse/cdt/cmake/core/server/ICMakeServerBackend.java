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

import java.io.IOException;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

/**
 * Low-level interface towards underlying CMake server JSON protocol.
 */
public interface ICMakeServerBackend extends AutoCloseable {

	/**
	 * Start the cmake server process.
	 * 
	 * @throws IOException
	 */
	void startServer() throws IOException;

	/**
	 * Sends a JSON request to the server backend.
	 * 
	 * @param json
	 * @throws IOException
	 * @throws CMakeServerException
	 */
	void sendRequest(JsonObject json) throws IOException, CMakeServerException;

	/**
	 * The reply-consumer is a callback which is passed all the replies coming
	 * in from the server object.
	 * 
	 * @param jsonobj
	 */
	void setReplyConsumer(Consumer<JsonObject> jsonobj);

}
