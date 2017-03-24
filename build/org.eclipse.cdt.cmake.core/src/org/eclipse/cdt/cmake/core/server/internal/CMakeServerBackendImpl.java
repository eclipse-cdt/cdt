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
package org.eclipse.cdt.cmake.core.server.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.function.Consumer;

import org.eclipse.cdt.cmake.core.server.CMakeServerException;
import org.eclipse.cdt.cmake.core.server.ICMakeServerBackend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Manages the cmake server process itself: starting/stopping, sending/receiving
 * messages.
 */
public class CMakeServerBackendImpl implements ICMakeServerBackend, AutoCloseable {

	private Process process;
	private Gson gson;
	private OutputStreamWriter writer;
	private Thread responseReader;
	private final String CMAKE_SERVER_MAGIC_START = "[== \"CMake Server\" ==[";
	private final String CMAKE_SERVER_MAGIC_END = "]== \"CMake Server\" ==]";
	private Consumer<JsonObject> consumer;
	private StringBuilder currentMessage = new StringBuilder();
	private ProcessBuilder pb;

	@Override
	public void startServer() throws IOException {
		pb.redirectInput(Redirect.PIPE);
		pb.redirectOutput(Redirect.PIPE);
		process = pb.start();
		writer = new OutputStreamWriter(process.getOutputStream());
		responseReader.start();
	}

	@Override
	public void setReplyConsumer(Consumer<JsonObject> consumer) {
		this.consumer = consumer;
	}

	private void handleLineFromServer(String line) {
		// System.out.println("From server: " + line.trim());

		switch (line) {
		case CMAKE_SERVER_MAGIC_START: {
			currentMessage.setLength(0);
			break;
		}
		case CMAKE_SERVER_MAGIC_END: {
			consumer.accept(gson.toJsonTree(gson.fromJson(currentMessage.toString(), Object.class)).getAsJsonObject());
			break;
		}
		default:
			currentMessage.append(line);
			break;
		}
	}

	/**
	 * Create a server backend using the given process builder which is
	 * configured to refer to a physical CMake executable.
	 * 
	 * @param pb
	 */
	public CMakeServerBackendImpl(ProcessBuilder pb) {
		this.pb = pb;

		gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		responseReader = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				reader.lines().forEach(line -> handleLineFromServer(line));
			} catch (Exception e) {
				// TODO better logging here
				e.printStackTrace();
			} finally {
				// Inform the reply-consumer that the reader thread has
				// terminated. This is to make sure that we don't wait for
				// a timeout if/when the server fails to start.
				JsonObject obj = new JsonObject();
				obj.addProperty("type", "closed");
				consumer.accept(obj);
			}
		});

		responseReader.setName("cmake-server-reader");
		responseReader.setDaemon(true);
	}

	/**
	 * Closes the stdin stream, then wait for the cmake process to terminate.
	 */
	@Override
	public void close() throws InterruptedException, IOException {
		process.getOutputStream().close();
		process.waitFor();
		responseReader.interrupt();
		responseReader.join();
	}

	/**
	 * Sends a request to the server. This is a low-level method which simply
	 * serializes the JSON object into a string, and adds the magic strings
	 * before and after.
	 * 
	 * @param obj
	 *            The request as a JSON object.
	 * @throws IOException
	 * @throws CMakeServerException
	 */
	@Override
	public void sendRequest(JsonObject obj) throws IOException, CMakeServerException {
		if (!responseReader.isAlive()) {
			throw new CMakeServerException("Reader thread is not alive");
		}

		if (!process.isAlive()) {
			throw new CMakeServerException("Server process is not alive");
		}

		String payload = gson.toJson(obj);
		String message = String.format("%s\n%s\n%s\n", CMAKE_SERVER_MAGIC_START, payload, CMAKE_SERVER_MAGIC_END);

		writer.write(message);
		writer.flush();
	}

}
