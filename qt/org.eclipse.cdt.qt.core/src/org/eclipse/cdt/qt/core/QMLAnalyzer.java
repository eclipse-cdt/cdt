/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.core.Activator;

@SuppressWarnings("nls")
public class QMLAnalyzer {

	private ScriptEngine engine;
	private Invocable invoke;
	private Object tern;

	public void load() throws ScriptException, IOException, NoSuchMethodException {
		engine = new ScriptEngineManager().getEngineByName("nashorn");
		invoke = (Invocable) engine;

		load("/tern-qml/node_modules/acorn/dist/acorn.js");
		load("/tern-qml/node_modules/acorn/dist/acorn_loose.js");
		load("/tern-qml/node_modules/acorn/dist/walk.js");
		load("/tern-qml/node_modules/acorn-qml/inject.js");
		load("/tern-qml/node_modules/acorn-qml/index.js");
		load("/tern-qml/node_modules/acorn-qml/loose/inject.js");
		load("/tern-qml/node_modules/acorn-qml/loose/index.js");
		load("/tern-qml/node_modules/acorn-qml/walk/index.js");

		load("/tern-qml/node_modules/tern/lib/signal.js");
		load("/tern-qml/node_modules/tern/lib/tern.js");
		load("/tern-qml/node_modules/tern/lib/def.js");
		load("/tern-qml/node_modules/tern/lib/comment.js");
		load("/tern-qml/node_modules/tern/lib/infer.js");

		load("/tern-qml/qml.js");
		load("/tern-qml/qml-nsh.js");
		Bindings options = (Bindings) engine.eval("new Object()");
		options.put("ecmaVersion", 5);

		Bindings plugins = (Bindings) engine.eval("new Object()");
		plugins.put("qml", true);
		options.put("plugins", plugins);

		Bindings defs = (Bindings) engine.eval("new Array()");
		load("/tern-qml/ecma5-defs.js");
		invoke.invokeMethod(defs, "push", engine.get("ecma5defs"));
		options.put("defs", defs);

		synchronized (this) {
			tern = invoke.invokeFunction("newTernServer", options);
			notifyAll();
		}
	}

	private Object load(String file) throws ScriptException, IOException {
		URL scriptURL = Activator.getDefault().getBundle().getEntry(file);
		if (scriptURL == null) {
			throw new FileNotFoundException(file);
		}
		engine.getContext().setAttribute(ScriptEngine.FILENAME, file, ScriptContext.ENGINE_SCOPE);
		return engine.eval(new BufferedReader(new InputStreamReader(scriptURL.openStream())));
	}

	private void waitUntilLoaded() {
		synchronized (this) {
			while (tern == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					Activator.log(e);
					return;
				}
			}
		}
	}

	public interface RequestCallback {
		void callback(Object err, Object data);
	}

	public void addFile(String fileName, String code) throws NoSuchMethodException, ScriptException {
		waitUntilLoaded();
		invoke.invokeMethod(tern, "addFile", fileName, code);
	}

	public Collection<QMLTernCompletion> getCompletions(String fileName, int pos)
			throws NoSuchMethodException, ScriptException {
		waitUntilLoaded();
		Bindings query = engine.createBindings();
		query.put("type", "completions");
		query.put("file", fileName);
		query.put("end", pos);
		query.put("types", true);
		query.put("docs", false);
		query.put("urls", false);
		query.put("origins", true);
		query.put("caseInsensitive", true);
		query.put("lineCharPositions", true);
		query.put("expandWordForward", false);
		query.put("includeKeywords", true);
		query.put("guess", false);
		Bindings request = engine.createBindings();
		request.put("query", query);

		List<QMLTernCompletion> completions = new ArrayList<>();

		RequestCallback callback = (err, data) -> {
			if (err != null) {
				throw new RuntimeException(err.toString());
			} else {
				try {
					for (Bindings completion : (Bindings[]) invoke.invokeMethod(engine.get("Java"), "to",
							((Bindings) data).get("completions"), "javax.script.Bindings[]")) {
						completions.add(new QMLTernCompletion((String) completion.get("name"),
								(String) completion.get("type"), (String) completion.get("origin")));
					}
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		};

		invoke.invokeMethod(tern, "request", request, invoke.invokeFunction("requestCallback", callback));

		return completions;
	}

}
