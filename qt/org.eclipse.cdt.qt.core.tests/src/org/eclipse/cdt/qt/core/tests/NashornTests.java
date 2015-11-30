package org.eclipse.cdt.qt.core.tests;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.core.qml.QMLAnalyzer;
import org.junit.Test;

@SuppressWarnings("nls")
public class NashornTests {

	private static Object load(ScriptEngine engine, String path) throws ScriptException, IOException {
		URL scriptURL = Activator.getBundle().getEntry(path);
		return engine.eval(new BufferedReader(new InputStreamReader(scriptURL.openStream())));
	}

	@Test
	public void test1() throws Throwable {
		System.out.println("Loading...");
		long start = System.currentTimeMillis();
		QMLAnalyzer analyzer = new QMLAnalyzer();
		analyzer.load();
		long stop = System.currentTimeMillis();
		System.out.println("Loaded: " + (stop - start) + " ms.");

		ScriptEngine engine = analyzer.getEngine();
		Invocable invoke = (Invocable) engine;

		Object ecma5defs = load(engine, "scripts/ecma5-defs.js");

		Bindings qmlPlugin = engine.createBindings();
		Bindings plugins = engine.createBindings();
		plugins.put("qml", qmlPlugin);

		Bindings options = engine.createBindings();
		options.put("ecmaVersion", 5);
		options.put("plugins", plugins);
		options.put("defs", invoke.invokeMethod(engine.get("Java"), "from", Arrays.asList(ecma5defs)));

		Bindings server = analyzer.createTernServer(options);
		assertNotNull(server);

		invoke.invokeMethod(server, "addFile", "test1.qml", "Window {\n\ttest: \n}");
		Bindings query = engine.createBindings();
		query.put("type", "completions");
		query.put("file", "test1.qml");
		query.put("pos", 15);
		query.put("types", true);
		query.put("docs", false);
		query.put("urls", false);
		query.put("origins", true);
		query.put("caseInsensitive", true);
		query.put("lineCharPositions", true);
		query.put("expandWordForward", false);
		query.put("guess", false);
		Bindings request = engine.createBindings();
		request.put("query", query);

		Object result = invoke.invokeMethod(server, "request", request);
		invoke.invokeFunction("print", invoke.invokeMethod(engine.get("JSON"), "stringify", result));

		long end = System.currentTimeMillis();
		System.out.println("Last bit: " + (end - stop) + " ms.");
	}

}
