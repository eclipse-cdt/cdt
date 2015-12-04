package org.eclipse.cdt.qt.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.qt.core.QMLAnalyzer;
import org.eclipse.cdt.qt.core.QMLTernCompletion;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class NashornTests {

	protected static QMLAnalyzer analyzer;

	@BeforeClass
	public static void loadAnalyzer() {
		analyzer = Activator.getService(QMLAnalyzer.class);
	}

	protected void getCompletions(String code, QMLTernCompletion... expected) throws Throwable {
		int pos = code.indexOf('|');
		code = code.substring(0, pos) + code.substring(pos + 1);

		Collection<QMLTernCompletion> QMLTernCompletions = analyzer.getCompletions("test1.qml", code, pos);

		Map<String, QMLTernCompletion> set = new HashMap<>();
		Set<String> unexpected = new HashSet<>();
		for (QMLTernCompletion QMLTernCompletion : expected) {
			set.put(QMLTernCompletion.getName(), QMLTernCompletion);
		}
		for (QMLTernCompletion QMLTernCompletion : QMLTernCompletions) {
			String name = QMLTernCompletion.getName();
			QMLTernCompletion c = set.get(name);
			if (c != null) {
				assertEquals(c.getType(), QMLTernCompletion.getType());
				assertEquals(c.getOrigin(), QMLTernCompletion.getOrigin());
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
	}

	@Test
	public void test1() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\tproperty int height\n\the|\n}",
				new QMLTernCompletion("height", "number", "test1.qml"));
	}

	@Test
	public void test2() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\tproperty int height\n\tproperty int width\n\tproperty string text\n\t|\n}",
				new QMLTernCompletion("height", "number", "test1.qml"),
				new QMLTernCompletion("text", "string", "test1.qml"),
				new QMLTernCompletion("width", "number", "test1.qml"));
	}

	@Test
	public void test3() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\tproperty int height\n\tObject {\n\t\t|\n\t}\n}");
	}

	@Test
	public void test4() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\tproperty var test: |\n}",
				new QMLTernCompletion("decodeURI", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("decodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("encodeURI", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("encodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("eval", "fn(code: string)", "ecma5"),
				new QMLTernCompletion("isFinite", "fn(value: number) -> bool", "ecma5"),
				new QMLTernCompletion("isNaN", "fn(value: number) -> bool", "ecma5"),
				new QMLTernCompletion("parseFloat", "fn(string: string) -> number", "ecma5"),
				new QMLTernCompletion("parseInt", "fn(string: string, radix?: number) -> number", "ecma5"),
				new QMLTernCompletion("test", "?", "test1.qml"), new QMLTernCompletion("undefined", "?", "ecma5"),
				new QMLTernCompletion("Array", "fn(size: number)", "ecma5"),
				new QMLTernCompletion("Boolean", "fn(value: ?) -> bool", "ecma5"),
				new QMLTernCompletion("Date", "fn(ms: number)", "ecma5"),
				new QMLTernCompletion("Error", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("EvalError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("Function", "fn(body: string) -> fn()", "ecma5"),
				new QMLTernCompletion("Infinity", "number", "ecma5"), new QMLTernCompletion("JSON", "JSON", "ecma5"),
				new QMLTernCompletion("Math", "Math", "ecma5"), new QMLTernCompletion("NaN", "number", "ecma5"),
				new QMLTernCompletion("Number", "fn(value: ?) -> number", "ecma5"),
				new QMLTernCompletion("Object", "fn()", "ecma5"),
				new QMLTernCompletion("RangeError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("ReferenceError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("RegExp", "fn(source: string, flags?: string)", "ecma5"),
				new QMLTernCompletion("String", "fn(value: ?) -> string", "ecma5"),
				new QMLTernCompletion("SyntaxError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("TypeError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("URIError", "fn(message: string)", "ecma5"));
	}

	@Test
	public void test5() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\ttest: |\n}",
				new QMLTernCompletion("decodeURI", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("decodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("encodeURI", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("encodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("eval", "fn(code: string)", "ecma5"),
				new QMLTernCompletion("isFinite", "fn(value: number) -> bool", "ecma5"),
				new QMLTernCompletion("isNaN", "fn(value: number) -> bool", "ecma5"),
				new QMLTernCompletion("parseFloat", "fn(string: string) -> number", "ecma5"),
				new QMLTernCompletion("parseInt", "fn(string: string, radix?: number) -> number", "ecma5"),
				new QMLTernCompletion("undefined", "?", "ecma5"),
				new QMLTernCompletion("Array", "fn(size: number)", "ecma5"),
				new QMLTernCompletion("Boolean", "fn(value: ?) -> bool", "ecma5"),
				new QMLTernCompletion("Date", "fn(ms: number)", "ecma5"),
				new QMLTernCompletion("Error", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("EvalError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("Function", "fn(body: string) -> fn()", "ecma5"),
				new QMLTernCompletion("Infinity", "number", "ecma5"), new QMLTernCompletion("JSON", "JSON", "ecma5"),
				new QMLTernCompletion("Math", "Math", "ecma5"), new QMLTernCompletion("NaN", "number", "ecma5"),
				new QMLTernCompletion("Number", "fn(value: ?) -> number", "ecma5"),
				new QMLTernCompletion("Object", "fn()", "ecma5"),
				new QMLTernCompletion("RangeError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("ReferenceError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("RegExp", "fn(source: string, flags?: string)", "ecma5"),
				new QMLTernCompletion("String", "fn(value: ?) -> string", "ecma5"),
				new QMLTernCompletion("SyntaxError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("TypeError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("URIError", "fn(message: string)", "ecma5"));
	}

	@Test
	public void test6() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\ttest: {\n\t\t|\n\t}\n}",
				new QMLTernCompletion("decodeURI", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("decodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("encodeURI", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("encodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("eval", "fn(code: string)", "ecma5"),
				new QMLTernCompletion("isFinite", "fn(value: number) -> bool", "ecma5"),
				new QMLTernCompletion("isNaN", "fn(value: number) -> bool", "ecma5"),
				new QMLTernCompletion("parseFloat", "fn(string: string) -> number", "ecma5"),
				new QMLTernCompletion("parseInt", "fn(string: string, radix?: number) -> number", "ecma5"),
				new QMLTernCompletion("undefined", "?", "ecma5"),
				new QMLTernCompletion("Array", "fn(size: number)", "ecma5"),
				new QMLTernCompletion("Boolean", "fn(value: ?) -> bool", "ecma5"),
				new QMLTernCompletion("Date", "fn(ms: number)", "ecma5"),
				new QMLTernCompletion("Error", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("EvalError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("Function", "fn(body: string) -> fn()", "ecma5"),
				new QMLTernCompletion("Infinity", "number", "ecma5"), new QMLTernCompletion("JSON", "JSON", "ecma5"),
				new QMLTernCompletion("Math", "Math", "ecma5"), new QMLTernCompletion("NaN", "number", "ecma5"),
				new QMLTernCompletion("Number", "fn(value: ?) -> number", "ecma5"),
				new QMLTernCompletion("Object", "fn()", "ecma5"),
				new QMLTernCompletion("RangeError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("ReferenceError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("RegExp", "fn(source: string, flags?: string)", "ecma5"),
				new QMLTernCompletion("String", "fn(value: ?) -> string", "ecma5"),
				new QMLTernCompletion("SyntaxError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("TypeError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("URIError", "fn(message: string)", "ecma5"));
	}

	@Test
	public void test7() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\tfunction test() {\n\t\t|\n\t}\n}",
				new QMLTernCompletion("decodeURI", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("decodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("encodeURI", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("encodeURIComponent", "fn(uri: string) -> string", "ecma5"),
				new QMLTernCompletion("eval", "fn(code: string)", "ecma5"),
				new QMLTernCompletion("isFinite", "fn(value: number) -> bool", "ecma5"),
				new QMLTernCompletion("isNaN", "fn(value: number) -> bool", "ecma5"),
				new QMLTernCompletion("parseFloat", "fn(string: string) -> number", "ecma5"),
				new QMLTernCompletion("parseInt", "fn(string: string, radix?: number) -> number", "ecma5"),
				new QMLTernCompletion("test", "fn()", "test1.qml"), new QMLTernCompletion("undefined", "?", "ecma5"),
				new QMLTernCompletion("Array", "fn(size: number)", "ecma5"),
				new QMLTernCompletion("Boolean", "fn(value: ?) -> bool", "ecma5"),
				new QMLTernCompletion("Date", "fn(ms: number)", "ecma5"),
				new QMLTernCompletion("Error", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("EvalError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("Function", "fn(body: string) -> fn()", "ecma5"),
				new QMLTernCompletion("Infinity", "number", "ecma5"), new QMLTernCompletion("JSON", "JSON", "ecma5"),
				new QMLTernCompletion("Math", "Math", "ecma5"), new QMLTernCompletion("NaN", "number", "ecma5"),
				new QMLTernCompletion("Number", "fn(value: ?) -> number", "ecma5"),
				new QMLTernCompletion("Object", "fn()", "ecma5"),
				new QMLTernCompletion("RangeError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("ReferenceError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("RegExp", "fn(source: string, flags?: string)", "ecma5"),
				new QMLTernCompletion("String", "fn(value: ?) -> string", "ecma5"),
				new QMLTernCompletion("SyntaxError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("TypeError", "fn(message: string)", "ecma5"),
				new QMLTernCompletion("URIError", "fn(message: string)", "ecma5"));
	}
}
