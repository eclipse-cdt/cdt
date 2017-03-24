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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.cdt.cmake.core.server.CMakeCache;
import org.eclipse.cdt.cmake.core.server.CMakeCodeModel;
import org.eclipse.cdt.cmake.core.server.CMakeFileSystemWatchers;
import org.eclipse.cdt.cmake.core.server.CMakeGlobalSettings;
import org.eclipse.cdt.cmake.core.server.CMakeHandshake;
import org.eclipse.cdt.cmake.core.server.CMakeInputs;
import org.eclipse.cdt.cmake.core.server.CMakeProgress;
import org.eclipse.cdt.cmake.core.server.CMakeProtocol;
import org.eclipse.cdt.cmake.core.server.CMakeServerException;
import org.eclipse.cdt.cmake.core.server.ICMakeServer;
import org.eclipse.cdt.cmake.core.server.ICMakeServerBackend;
import org.eclipse.cdt.cmake.core.server.ICMakeServerListener;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Default implementation of the {@link ICMakeServer} interface. This
 * implementation is not thread-safe. If used from multiple threads without
 * suitable protection, things will not work well.
 */
public class CMakeServerImpl implements ICMakeServer {
	private Gson gson;
	private ICMakeServerBackend backend;
	private List<CMakeProtocol> supportedProtocols = new ArrayList<>();
	private CountDownLatch helloSignal = new CountDownLatch(1);
	private List<ICMakeServerListener> listeners = new CopyOnWriteArrayList<>();
	private Map<String, Reply> replies = new ConcurrentHashMap<>();

	/**
	 * Internal class to track callers waiting for replies.
	 */
	private static class Reply {
		CountDownLatch signal;
		JsonObject reply;

		public Reply(JsonObject reply) {
			super();
			this.signal = new CountDownLatch(1);
			this.reply = reply;
		}
	};

	private void replyConsumer(JsonObject reply) {

		switch (reply.get("type").getAsString()) {

		case "closed": {
			// This is a pseudo message indicating that the
			// cmake-server itself has disappeared. We need
			// this to avoid timing out if something fails
			// during startup
			helloSignal.countDown();
		}

		case "hello": {
			// Store the list of supported protocols reported
			// by the server.
			if (reply.has("supportedProtocolVersions")) {
				JsonElement protocolVersions = reply.get("supportedProtocolVersions");

				for (JsonElement jsonelem : protocolVersions.getAsJsonArray()) {
					supportedProtocols.add(gson.fromJson(jsonelem, CMakeProtocol.class));
				}
			}

			helloSignal.countDown();
			break;
		}

		case "error":
			// In case we are still waiting for the hello
			// signal.
			helloSignal.countDown();
			// Fall-through

		case "reply": {
			// Check if the cookie corresponds to a pending
			// reply.
			String cookie = reply.get("cookie").getAsString();
			Reply r = replies.getOrDefault(cookie, null);
			if (r != null) {
				r.reply = reply;
				r.signal.countDown();
			}
			break;
		}

		case "message": {
			String title = reply.has("title") ? reply.get("title").getAsString() : null;
			String message = reply.get("message").getAsString();
			listeners.forEach(l -> l.onMessage(title, message));
			break;
		}

		case "progress": {
			CMakeProgress progress = gson.fromJson(reply, CMakeProgress.class);
			listeners.forEach(l -> l.onProgress(progress));
			break;
		}

		case "signal": {
			String name = reply.get("name").getAsString();
			switch (name) {
			case "fileChange":
				/*
				 * This signal is the only signal which carries additional data,
				 * and has its own listener method.
				 * 
				 * We do not have a dedicated class for these parameters, so
				 * extract the parameters directly from the JSON object.
				 */
				String path = reply.get("path").getAsString();
				List<String> properties = new ArrayList<>();
				for (JsonElement p : reply.get("properties").getAsJsonArray()) {
					properties.add(p.getAsString());
				}
				listeners.forEach(l -> l.onFileChange(path, properties));
				break;
			default:
				listeners.forEach(l -> l.onSignal(name));
				break;
			}

			break;
		}

		default:
			break;
		}
	};

	@Override
	public void startServer(ICMakeServerBackend backend) throws CMakeServerException {
		this.gson = new Gson();
		this.backend = backend;

		try {
			backend.setReplyConsumer(this::replyConsumer);
			backend.startServer();

			if (!helloSignal.await(5000, TimeUnit.MILLISECONDS)) {
				throw new CMakeServerException("Timeout waiting for initial protocol 'hello' signal.");
			}

			if (supportedProtocols.size() == 0) {
				throw new CMakeServerException("No supported protocols");
			}
		} catch (IOException | InterruptedException e) {
			throw new CMakeServerException(e);
		}
	}

	@Override
	public void close() throws Exception {
		if (backend != null)
			backend.close();

	}

	@Override
	public List<CMakeProtocol> getSupportedProtocolVersions() throws CMakeServerException {
		return supportedProtocols;
	}

	public String toString(File f) {
		/*
		 * cmake-server does not like backslashes being passed in the
		 * source/build directory strings.
		 */
		return f.toString().replace('\\', '/');
	}

	/**
	 * Sends a request to the server and waits for a reply.
	 * 
	 * @param obj
	 * @param type
	 * @param replyType
	 *            The type of the expected reply.
	 * @throws CMakeServerException
	 */
	private <T> T send(Object obj, String type, Class<T> replyType) throws CMakeServerException {
		JsonObject json = gson.toJsonTree(obj).getAsJsonObject();
		String cookie = UUID.randomUUID().toString();
		json.addProperty("cookie", cookie);
		json.addProperty("type", type);
		Reply reply = new Reply(json);
		replies.put(cookie, reply);
		try {
			backend.sendRequest(json);
			reply.signal.await(); // TODO timeout?
			replies.remove(cookie);

			switch (reply.reply.get("type").getAsString()) {
			case "error":
				throw new CMakeServerException(reply.reply.get("errorMessage").getAsString());
			default:
				return gson.fromJson(reply.reply, replyType);
			}
		} catch (IOException | InterruptedException e) {
			throw new CMakeServerException(e);
		}
	}

	/**
	 * Send a request without a payload and no reply type. E.g. "configure".
	 * 
	 * @param type
	 * @throws CMakeServerException
	 */
	private void send(String type) throws CMakeServerException {
		send(new Object(), type, Object.class);
	}

	/**
	 * Send a request with a payload, but no reply type. E.g.
	 * "setGlobalSettings".
	 * 
	 * @param request
	 * @param type
	 * @throws CMakeServerException
	 */
	private void send(Object request, String type) throws CMakeServerException {
		send(request, type, Object.class);
	}

	/**
	 * Sends a request without a payload, but with a reply type.
	 * 
	 * @param type
	 * @param replyType
	 * @return
	 * @throws CMakeServerException
	 */
	private <T> T send(String type, Class<T> replyType) throws CMakeServerException {
		return send(new Object(), type, replyType);
	}

	@Override
	public void handshake(File sourceDirectory, File buildDirectory, String generator, String extraGenerator,
			String platform, String toolset) throws CMakeServerException {

		send(new CMakeHandshake(toString(sourceDirectory), toString(buildDirectory), generator, extraGenerator,
				platform, toolset, supportedProtocols.get(0)), "handshake");
	}

	@Override
	public void handshake(File sourceDirectory, File buildDirectory, String generator) throws CMakeServerException {
		handshake(sourceDirectory, buildDirectory, generator, null, null, null);
	}

	@Override
	public CMakeGlobalSettings getGlobalSettings() throws CMakeServerException {
		return send("globalSettings", CMakeGlobalSettings.class);
	}

	@Override
	public void setGlobalSetting(String attr, boolean value) throws CMakeServerException {
		Map<String, Boolean> obj = new HashMap<>();
		obj.put(attr, value);
		send(obj, "setGlobalSettings");
	}

	@Override
	public void configure() throws CMakeServerException {
		send("configure");
	}

	@Override
	public void configure(Map<String, String> cacheArguments) throws CMakeServerException {

		Map<String, Object> obj = new HashMap<>();
		obj.put("cacheArguments", cacheArguments.entrySet().stream()
				.map(e -> String.format("-D %s=%s", e.getKey(), e.getValue())).collect(Collectors.toList()));
		send(obj, "configure");
	}

	@Override
	public void compute() throws CMakeServerException {
		send("compute");
	}

	@Override
	public CMakeCodeModel getCodeModel() throws CMakeServerException {
		return send("codemodel", CMakeCodeModel.class);
	}

	@Override
	public CMakeInputs getCMakeInputs() throws CMakeServerException {
		return send("cmakeInputs", CMakeInputs.class);
	}

	@Override
	public CMakeCache getCMakeCache() throws CMakeServerException {
		return send("cache", CMakeCache.class);
	}

	@Override
	public CMakeFileSystemWatchers getFileSystemWatchers() throws CMakeServerException {
		return send("fileSystemWatchers", CMakeFileSystemWatchers.class);
	}

	@Override
	public void addListener(ICMakeServerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ICMakeServerListener listener) {
		listeners.remove(listener);
	}
}
