package org.eclipse.cdt.qt.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.core.qml.QMLAnalyzer;
import org.eclipse.cdt.internal.qt.core.qml.QMLAnalyzer.RequestCallback;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class NashornTests {

	protected static QMLAnalyzer analyzer;
	protected static ScriptEngine engine;
	protected static Invocable invoke;

	private static Object load(ScriptEngine engine, String file) throws Throwable {
		URL scriptURL = Activator.getBundle().getEntry(file);
		if (scriptURL == null) {
			throw new FileNotFoundException(file);
		}
		engine.getContext().setAttribute(ScriptEngine.FILENAME, file, ScriptContext.ENGINE_SCOPE);
		return engine.eval(new BufferedReader(new InputStreamReader(scriptURL.openStream())));
	}

	@BeforeClass
	public static void loadAnalyzer() {
		System.out.println("Loading QML Analyzer...");
		long start = System.currentTimeMillis();

		analyzer = new QMLAnalyzer();
		try {
			analyzer.load();
		} catch (ScriptException | IOException e) {
			e.printStackTrace();
			return;
		}

		engine = analyzer.getEngine();
		invoke = analyzer.getInvocable();
		long stop = System.currentTimeMillis();
		System.out.println("Loaded: " + (stop - start) + " ms.");
	}

	protected Bindings newObject() throws Throwable {
		return (Bindings) engine.eval("new Object()");
	}

	protected Bindings newArray() throws Throwable {
		return (Bindings) engine.eval("new Array()");
	}

	protected Object createTernServer() throws Throwable {
		Bindings options = newObject();
		options.put("ecmaVersion", 5);

		Bindings plugins = newObject();
		plugins.put("qml", true);
		options.put("plugins", plugins);

		Bindings defs = newArray();
		load(engine, "scripts/ecma5-defs.js");
		invoke.invokeMethod(defs, "push", engine.get("ecma5defs"));
		options.put("defs", defs);

		return analyzer.createTernServer(options);
	}

	private static class Completion {
		final String name;
		final String type;
		final String origin;

		public Completion(String name, String type, String origin) {
			this.name = name;
			this.type = type;
			this.origin = origin;
		}
	}

	protected void getCompletions(Object server, String code, Completion... expected) throws Throwable {
		int pos = code.indexOf('|');
		code = code.substring(0, pos) + code.substring(pos + 1);

		invoke.invokeMethod(server, "addFile", "test1.qml", code);

		Bindings query = engine.createBindings();
		query.put("type", "completions");
		query.put("file", "test1.qml");
		query.put("end", pos);
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

		RequestCallback callback = (err, data) -> {
			if (err != null) {
				fail(err.toString());
			} else {
				try {
					Bindings[] completions = (Bindings[]) invoke.invokeMethod(engine.get("Java"), "to",
							((Bindings) data).get("completions"), "javax.script.Bindings[]");

					Map<String, Completion> set = new HashMap<>();
					Set<String> unexpected = new HashSet<>();
					for (Completion completion : expected) {
						set.put(completion.name, completion);
					}
					for (Bindings completion : completions) {
						String name = (String) completion.get("name");
						Completion c = set.get(name);
						if (c != null) {
							assertEquals(c.type, completion.get("type"));
							assertEquals(c.origin, completion.get("origin"));
							set.remove(name);
						} else {
							unexpected.add(name);
						}
					}
					if (!set.isEmpty()) {
						fail("Missing names: " + String.join(", ", set.keySet()));
					}
					if (!unexpected.isEmpty()) {
						fail("Unexpected names: " + String.join(", ", unexpected));
					}
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
		};

		invoke.invokeMethod(server, "request", request, invoke.invokeFunction("requestCallback", callback));
	}

	@Test
	public void test1() throws Throwable {
		if (engine == null) {
			return;
		}
		getCompletions(createTernServer(), "Window {\n\tproperty int height\n\the|\n}",
				new Completion("height", "number", "test1.qml"));
	}

	@Test
	public void test2() throws Throwable {
		if (engine == null) {
			return;
		}
		getCompletions(createTernServer(),
				"Window {\n\tproperty int height\n\tproperty int width\n\tproperty string text\n\t|\n}",
				new Completion("height", "number", "test1.qml"), new Completion("text", "string", "test1.qml"),
				new Completion("width", "number", "test1.qml"));
	}

	@Test
	public void test3() throws Throwable {
		if (engine == null) {
			return;
		}
		getCompletions(createTernServer(), "Window {\n\tproperty int height\n\tObject {\n\t\t|\n\t}\n}");
	}

	@Test
	public void test4() throws Throwable {
		if (engine == null) {
			return;
		}
		getCompletions(createTernServer(), "Window {\n\tproperty var test: |\n}",
				new Completion("decodeURI", "fn(uri: string) -> string", "ecma5"),
				new Completion("decodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new Completion("encodeURI", "fn(uri: string) -> string", "ecma5"),
				new Completion("encodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new Completion("eval", "fn(code: string)", "ecma5"),
				new Completion("isFinite", "fn(value: number) -> bool", "ecma5"),
				new Completion("isNaN", "fn(value: number) -> bool", "ecma5"),
				new Completion("parseFloat", "fn(string: string) -> number", "ecma5"),
				new Completion("parseInt", "fn(string: string, radix?: number) -> number", "ecma5"),
				new Completion("test", "?", "test1.qml"), new Completion("undefined", "?", "ecma5"),
				new Completion("Array", "fn(size: number)", "ecma5"),
				new Completion("Boolean", "fn(value: ?) -> bool", "ecma5"),
				new Completion("Date", "fn(ms: number)", "ecma5"),
				new Completion("Error", "fn(message: string)", "ecma5"),
				new Completion("EvalError", "fn(message: string)", "ecma5"),
				new Completion("Function", "fn(body: string) -> fn()", "ecma5"),
				new Completion("Infinity", "number", "ecma5"), new Completion("JSON", "JSON", "ecma5"),
				new Completion("Math", "Math", "ecma5"), new Completion("NaN", "number", "ecma5"),
				new Completion("Number", "fn(value: ?) -> number", "ecma5"), new Completion("Object", "fn()", "ecma5"),
				new Completion("RangeError", "fn(message: string)", "ecma5"),
				new Completion("ReferenceError", "fn(message: string)", "ecma5"),
				new Completion("RegExp", "fn(source: string, flags?: string)", "ecma5"),
				new Completion("String", "fn(value: ?) -> string", "ecma5"),
				new Completion("SyntaxError", "fn(message: string)", "ecma5"),
				new Completion("TypeError", "fn(message: string)", "ecma5"),
				new Completion("URIError", "fn(message: string)", "ecma5"));
	}

	@Test
	public void test5() throws Throwable {
		if (engine == null) {
			return;
		}
		getCompletions(createTernServer(), "Window {\n\ttest: |\n}",
				new Completion("decodeURI", "fn(uri: string) -> string", "ecma5"),
				new Completion("decodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new Completion("encodeURI", "fn(uri: string) -> string", "ecma5"),
				new Completion("encodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new Completion("eval", "fn(code: string)", "ecma5"),
				new Completion("isFinite", "fn(value: number) -> bool", "ecma5"),
				new Completion("isNaN", "fn(value: number) -> bool", "ecma5"),
				new Completion("parseFloat", "fn(string: string) -> number", "ecma5"),
				new Completion("parseInt", "fn(string: string, radix?: number) -> number", "ecma5"),
				new Completion("undefined", "?", "ecma5"), new Completion("Array", "fn(size: number)", "ecma5"),
				new Completion("Boolean", "fn(value: ?) -> bool", "ecma5"),
				new Completion("Date", "fn(ms: number)", "ecma5"),
				new Completion("Error", "fn(message: string)", "ecma5"),
				new Completion("EvalError", "fn(message: string)", "ecma5"),
				new Completion("Function", "fn(body: string) -> fn()", "ecma5"),
				new Completion("Infinity", "number", "ecma5"), new Completion("JSON", "JSON", "ecma5"),
				new Completion("Math", "Math", "ecma5"), new Completion("NaN", "number", "ecma5"),
				new Completion("Number", "fn(value: ?) -> number", "ecma5"), new Completion("Object", "fn()", "ecma5"),
				new Completion("RangeError", "fn(message: string)", "ecma5"),
				new Completion("ReferenceError", "fn(message: string)", "ecma5"),
				new Completion("RegExp", "fn(source: string, flags?: string)", "ecma5"),
				new Completion("String", "fn(value: ?) -> string", "ecma5"),
				new Completion("SyntaxError", "fn(message: string)", "ecma5"),
				new Completion("TypeError", "fn(message: string)", "ecma5"),
				new Completion("URIError", "fn(message: string)", "ecma5"));
	}

	@Test
	public void test6() throws Throwable {
		if (engine == null) {
			return;
		}
		getCompletions(createTernServer(), "Window {\n\ttest: {\n\t\t|\n\t}\n}",
				new Completion("decodeURI", "fn(uri: string) -> string", "ecma5"),
				new Completion("decodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new Completion("encodeURI", "fn(uri: string) -> string", "ecma5"),
				new Completion("encodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new Completion("eval", "fn(code: string)", "ecma5"),
				new Completion("isFinite", "fn(value: number) -> bool", "ecma5"),
				new Completion("isNaN", "fn(value: number) -> bool", "ecma5"),
				new Completion("parseFloat", "fn(string: string) -> number", "ecma5"),
				new Completion("parseInt", "fn(string: string, radix?: number) -> number", "ecma5"),
				new Completion("undefined", "?", "ecma5"), new Completion("Array", "fn(size: number)", "ecma5"),
				new Completion("Boolean", "fn(value: ?) -> bool", "ecma5"),
				new Completion("Date", "fn(ms: number)", "ecma5"),
				new Completion("Error", "fn(message: string)", "ecma5"),
				new Completion("EvalError", "fn(message: string)", "ecma5"),
				new Completion("Function", "fn(body: string) -> fn()", "ecma5"),
				new Completion("Infinity", "number", "ecma5"), new Completion("JSON", "JSON", "ecma5"),
				new Completion("Math", "Math", "ecma5"), new Completion("NaN", "number", "ecma5"),
				new Completion("Number", "fn(value: ?) -> number", "ecma5"), new Completion("Object", "fn()", "ecma5"),
				new Completion("RangeError", "fn(message: string)", "ecma5"),
				new Completion("ReferenceError", "fn(message: string)", "ecma5"),
				new Completion("RegExp", "fn(source: string, flags?: string)", "ecma5"),
				new Completion("String", "fn(value: ?) -> string", "ecma5"),
				new Completion("SyntaxError", "fn(message: string)", "ecma5"),
				new Completion("TypeError", "fn(message: string)", "ecma5"),
				new Completion("URIError", "fn(message: string)", "ecma5"));
	}

	@Test
	public void test7() throws Throwable {
		if (engine == null) {
			return;
		}
		getCompletions(createTernServer(), "Window {\n\tfunction test() {\n\t\t|\n\t}\n}",
				new Completion("decodeURI", "fn(uri: string) -> string", "ecma5"),
				new Completion("decodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new Completion("encodeURI", "fn(uri: string) -> string", "ecma5"),
				new Completion("encodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new Completion("eval", "fn(code: string)", "ecma5"),
				new Completion("isFinite", "fn(value: number) -> bool", "ecma5"),
				new Completion("isNaN", "fn(value: number) -> bool", "ecma5"),
				new Completion("parseFloat", "fn(string: string) -> number", "ecma5"),
				new Completion("parseInt", "fn(string: string, radix?: number) -> number", "ecma5"),
				new Completion("test", "fn()", "test1.qml"), new Completion("undefined", "?", "ecma5"),
				new Completion("Array", "fn(size: number)", "ecma5"),
				new Completion("Boolean", "fn(value: ?) -> bool", "ecma5"),
				new Completion("Date", "fn(ms: number)", "ecma5"),
				new Completion("Error", "fn(message: string)", "ecma5"),
				new Completion("EvalError", "fn(message: string)", "ecma5"),
				new Completion("Function", "fn(body: string) -> fn()", "ecma5"),
				new Completion("Infinity", "number", "ecma5"), new Completion("JSON", "JSON", "ecma5"),
				new Completion("Math", "Math", "ecma5"), new Completion("NaN", "number", "ecma5"),
				new Completion("Number", "fn(value: ?) -> number", "ecma5"), new Completion("Object", "fn()", "ecma5"),
				new Completion("RangeError", "fn(message: string)", "ecma5"),
				new Completion("ReferenceError", "fn(message: string)", "ecma5"),
				new Completion("RegExp", "fn(source: string, flags?: string)", "ecma5"),
				new Completion("String", "fn(value: ?) -> string", "ecma5"),
				new Completion("SyntaxError", "fn(message: string)", "ecma5"),
				new Completion("TypeError", "fn(message: string)", "ecma5"),
				new Completion("URIError", "fn(message: string)", "ecma5"));
	}
}
