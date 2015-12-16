/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.cdt.qt.core.IQMLAnalyzer;
import org.eclipse.cdt.qt.core.QMLTernCompletion;
import org.eclipse.cdt.qt.core.qmljs.IQmlASTNode;

@SuppressWarnings("nls")
public class QMLAnalyzer implements IQMLAnalyzer {

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

		ResolveDirectory resolveDirectory = (file, pathString) -> {
			String filename = (String) file.get("name");
			String fileDirectory = new File(filename).getParent();
			if (fileDirectory == null) {
				fileDirectory = "";
			}
			if (pathString == null) {
				return fixPathString(fileDirectory);
			}
			Path fileDirectoryPath = Paths.get(fileDirectory);
			Path path = Paths.get(pathString);
			if (!path.isAbsolute()) {
				path = fileDirectoryPath.toAbsolutePath().resolve(path);
			}
			return fixPathString(path.normalize().toString());
		};
		options.put("resolveDirectory", invoke.invokeFunction("resolveDirectory", resolveDirectory));

		synchronized (this) {
			tern = invoke.invokeFunction("newTernServer", options);
			notifyAll();
		}
	}

	@FunctionalInterface
	public interface ResolveDirectory {
		public String resolveDirectory(Bindings file, String path);
	}

	private Object load(String file) throws ScriptException, IOException {
		URL scriptURL = Activator.getDefault().getBundle().getEntry(file);
		if (scriptURL == null) {
			throw new FileNotFoundException(file);
		}
		engine.getContext().setAttribute(ScriptEngine.FILENAME, file, ScriptContext.ENGINE_SCOPE);
		return engine.eval(new BufferedReader(new InputStreamReader(scriptURL.openStream(), StandardCharsets.UTF_8)));
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

	@FunctionalInterface
	public interface RequestCallback {
		void callback(Object err, Object data);
	}

	private String fixPathString(String fileName) {
		fileName = fileName.replaceAll("\\\\", "/");
		if (fileName.startsWith("/")) {
			fileName = fileName.substring(1);
		}
		return fileName;
	}

	@Override
	public void addFile(String fileName, String code) throws NoSuchMethodException, ScriptException {
		waitUntilLoaded();
		invoke.invokeMethod(tern, "addFile", fixPathString(fileName), code);
	}

	@Override
	public void deleteFile(String fileName) throws NoSuchMethodException, ScriptException {
		waitUntilLoaded();
		invoke.invokeMethod(tern, "delFile", fixPathString(fileName));
	}

	private static class ASTCallback implements RequestCallback {
		private IQmlASTNode ast;

		@Override
		public void callback(Object err, Object data) {
			if (err != null) {
				throw new RuntimeException(err.toString());
			} else {
				try {
					ast = QmlASTNodeHandler.createQmlASTProxy((Bindings) ((Bindings) data).get("ast"));
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		}

		public IQmlASTNode getAST() {
			return ast;
		}
	}

	@Override
	public IQmlASTNode parseFile(String fileName, String text) throws NoSuchMethodException, ScriptException {
		waitUntilLoaded();
		fileName = fixPathString(fileName);

		Bindings query = engine.createBindings();
		query.put("type", "parseFile");
		query.put("file", fileName);
		Bindings request = engine.createBindings();
		request.put("query", query);

		if (text != null) {
			Bindings file = engine.createBindings();
			file.put("type", "full");
			file.put("name", fileName);
			file.put("text", text);
			Bindings files = (Bindings) engine.eval("new Array()");
			invoke.invokeMethod(files, "push", file);
			request.put("files", files);
		}

		ASTCallback callback = new ASTCallback();
		invoke.invokeMethod(tern, "request", request, invoke.invokeFunction("requestCallback", callback));
		return callback.getAST();
	}

	@Override
	public IQmlASTNode parseString(String text) throws NoSuchMethodException, ScriptException {
		return parseString(text, "qml", false, false);
	}

	@Override
	public IQmlASTNode parseString(String text, String mode, boolean locations, boolean ranges)
			throws NoSuchMethodException, ScriptException {
		waitUntilLoaded();
		Bindings query = engine.createBindings();
		query.put("type", "parseString");
		query.put("text", text);
		Bindings options = engine.createBindings();
		options.put("mode", mode);
		options.put("locations", locations);
		options.put("ranges", ranges);
		query.put("options", options);
		Bindings request = engine.createBindings();
		request.put("query", query);

		ASTCallback callback = new ASTCallback();
		invoke.invokeMethod(tern, "request", request, invoke.invokeFunction("requestCallback", callback));
		return callback.getAST();
	}

	protected <T> T[] toJavaArray(Bindings binding, Class<T[]> clazz) throws NoSuchMethodException, ScriptException {
		return clazz.cast(invoke.invokeMethod(engine.get("Java"), "to", binding,
				clazz.getCanonicalName() + (clazz.isArray() ? "" : "[]")));
	}

	@Override
	public Collection<QMLTernCompletion> getCompletions(String fileName, String text, int pos)
			throws NoSuchMethodException, ScriptException {
		return getCompletions(fileName, text, pos, true);
	}

	@Override
	public Collection<QMLTernCompletion> getCompletions(String fileName, String text, int pos, boolean includeKeywords)
			throws NoSuchMethodException, ScriptException {
		waitUntilLoaded();
		fileName = fixPathString(fileName);

		Bindings query = engine.createBindings();
		query.put("type", "completions");
		query.put("lineCharPositions", true);
		query.put("file", fileName);
		query.put("end", pos);
		query.put("types", true);
		query.put("docs", false);
		query.put("urls", false);
		query.put("origins", true);
		query.put("filter", true);
		query.put("caseInsensitive", true);
		query.put("guess", false);
		query.put("sort", true);
		query.put("expandWordForward", false);
		query.put("includeKeywords", includeKeywords);

		Bindings request = engine.createBindings();
		request.put("query", query);
		if (text != null) {
			Bindings file = engine.createBindings();
			file.put("type", "full");
			file.put("name", fileName);
			file.put("text", text);
			Bindings files = (Bindings) engine.eval("new Array()");
			invoke.invokeMethod(files, "push", file);
			request.put("files", files);
		}

		List<QMLTernCompletion> completions = new ArrayList<>();

		RequestCallback callback = (err, data) -> {
			if (err != null) {
				throw new RuntimeException(err.toString());
			} else {
				try {
					Bindings comps = (Bindings) ((Bindings) data).get("completions");
					for (Bindings completion : toJavaArray(comps, Bindings[].class)) {
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

	@Override
	public List<Bindings> getDefinition(String identifier, String fileName, String text, int pos)
			throws NoSuchMethodException, ScriptException {
		waitUntilLoaded();
		fileName = fixPathString(fileName);

		Bindings query = engine.createBindings();
		query.put("type", "definition");
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
		if (text != null) {
			Bindings file = engine.createBindings();
			file.put("type", "full");
			file.put("name", fileName);
			file.put("text", text);
			Bindings files = (Bindings) engine.eval("new Array()");
			invoke.invokeMethod(files, "push", file);
			request.put("files", files);
		}

		List<Bindings> definitions = new ArrayList<>();

		RequestCallback callback = (err, data) -> {
			if (err != null) {
				throw new RuntimeException(err.toString());
			} else {
				definitions.add((Bindings) data);
			}
		};

		invoke.invokeMethod(tern, "request", request, invoke.invokeFunction("requestCallback", callback));
		return definitions;
	}

}
