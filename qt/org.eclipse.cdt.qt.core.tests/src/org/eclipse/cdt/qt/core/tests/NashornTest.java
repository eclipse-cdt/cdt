/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.qt.core.tests;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.qt.core.IQMLAnalyzer;
import org.eclipse.cdt.qt.core.QMLTernCompletion;
import org.eclipse.cdt.qt.core.qmljs.IJSBinaryExpression;
import org.eclipse.cdt.qt.core.qmljs.IJSBinaryExpression.BinaryOperator;
import org.eclipse.cdt.qt.core.qmljs.IQmlASTNode;
import org.eclipse.cdt.qt.core.qmljs.IQmlHeaderItem;
import org.eclipse.cdt.qt.core.qmljs.IQmlImport;
import org.eclipse.cdt.qt.core.qmljs.IQmlObjectMember;
import org.eclipse.cdt.qt.core.qmljs.IQmlProgram;
import org.eclipse.cdt.qt.core.qmljs.IQmlPropertyBinding;
import org.eclipse.cdt.qt.core.qmljs.IQmlRootObject;
import org.eclipse.cdt.qt.core.qmljs.IQmlScriptBinding;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class NashornTests {

	protected static IQMLAnalyzer analyzer;

	@BeforeClass
	public static void loadAnalyzer() {
		analyzer = Activator.getService(IQMLAnalyzer.class);
	}

	protected void getCompletions(String code, QMLTernCompletion... expected) throws Throwable {
		int pos = code.indexOf('|');
		code = code.substring(0, pos) + code.substring(pos + 1);

		Collection<QMLTernCompletion> QMLTernCompletions = analyzer.getCompletions("test1.qml", code, pos, false);

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
	public void testParseFile1() throws Throwable {
		IQmlASTNode node = analyzer.parseFile("main.qml", "");
		assertEquals("Unexpected program node type", "QMLProgram", node.getType());
	}

	@Test
	public void testParseFile2() throws Throwable {
		IQmlASTNode node = analyzer.parseFile("main.qml", "import QtQuick 2.2");
		assertThat(node, instanceOf(IQmlProgram.class));
		IQmlProgram program = (IQmlProgram) node;
		List<IQmlHeaderItem> headerItems = program.getHeaderItemList().getItems();
		assertEquals("Unexpected number of header items", 1, headerItems.size());
		assertThat(headerItems.get(0), instanceOf(IQmlImport.class));
		IQmlImport imp = (IQmlImport) headerItems.get(0);
		assertEquals("Unexpected module identifier", "QtQuick", imp.getModule().getIdentifier().getName());
		assertEquals("Unexpected module raw version", "2.2", imp.getModule().getVersion().getRaw());
		assertEquals("Unexpected module version", 2.2, imp.getModule().getVersion().getValue(), 0.0001d);
	}

	@Test
	public void testParseString1() throws Throwable {
		IQmlASTNode node = analyzer.parseString("", "qml", true, true);
		assertEquals("Unexpected program node type", "QMLProgram", node.getType());
	}

	@Test
	public void testParseString2() throws Throwable {
		IQmlASTNode node = analyzer.parseString("import QtQuick 2.2", "qml", true, true);
		assertThat(node, instanceOf(IQmlProgram.class));
		IQmlProgram program = (IQmlProgram) node;
		List<IQmlHeaderItem> headerItems = program.getHeaderItemList().getItems();
		assertEquals("Unexpected number of header items", 1, headerItems.size());
		assertThat(headerItems.get(0), instanceOf(IQmlImport.class));
		IQmlImport imp = (IQmlImport) headerItems.get(0);
		assertEquals("Unexpected module identifier", "QtQuick", imp.getModule().getIdentifier().getName());
		assertEquals("Unexpected module raw version", "2.2", imp.getModule().getVersion().getRaw());
		assertEquals("Unexpected module version", 2.2, imp.getModule().getVersion().getValue(), 0.0001d);
	}

	@Test
	public void testParseString3() throws Throwable {
		IQmlASTNode node = analyzer.parseString("import QtQuick 2.2", "qml", true, true);
		assertThat(node, instanceOf(IQmlProgram.class));
		IQmlProgram program = (IQmlProgram) node;
		List<IQmlHeaderItem> headerItems = program.getHeaderItemList().getItems();
		assertEquals("Unexpected number of header items", 1, headerItems.size());
		assertThat(headerItems.get(0), instanceOf(IQmlImport.class));
		IQmlImport imp = (IQmlImport) headerItems.get(0);
		assertEquals("Unexpected start range", 0, imp.getRange()[0]);
		assertEquals("Unexpected end range", 18, imp.getRange()[1]);
	}

	@Test
	public void testParseString4() throws Throwable {
		IQmlASTNode node = analyzer.parseString("import QtQuick 2.2", "qml", true, true);
		assertThat(node, instanceOf(IQmlProgram.class));
		IQmlProgram program = (IQmlProgram) node;
		List<IQmlHeaderItem> headerItems = program.getHeaderItemList().getItems();
		assertEquals("Unexpected number of header items", 1, headerItems.size());
		assertThat(headerItems.get(0), instanceOf(IQmlImport.class));
		IQmlImport imp = (IQmlImport) headerItems.get(0);
		assertEquals("Unexpected start line", 1, imp.getLocation().getStart().getLine());
		assertEquals("Unexpected start column", 0, imp.getLocation().getStart().getColumn());
		assertEquals("Unexpected start line", 1, imp.getLocation().getEnd().getLine());
		assertEquals("Unexpected start column", 18, imp.getLocation().getEnd().getColumn());
	}

	@Test
	public void testParseString5() throws Throwable {
		IQmlASTNode node = analyzer.parseString("QtObject {}", "qml", true, true);
		assertThat(node, instanceOf(IQmlProgram.class));
		IQmlProgram program = (IQmlProgram) node;
		List<IQmlHeaderItem> headerItems = program.getHeaderItemList().getItems();
		assertEquals("Unexpected number of header items", 0, headerItems.size());
		assertNotNull("Root object was null", program.getRootObject());
		IQmlRootObject root = program.getRootObject();
		assertEquals("Unexpected root object type", "QMLObjectDefinition", root.getType());
		assertEquals("Unexpected root object identifier", "QtObject", root.getIdentifier().getName());
	}

	@Test
	public void testParseString6() throws Throwable {
		IQmlASTNode node = analyzer.parseString("QtObject {s: 3 + 3}", "qml", true, true);
		assertThat(node, instanceOf(IQmlProgram.class));
		IQmlProgram program = (IQmlProgram) node;
		assertNotNull("Root object was null", program.getRootObject());
		IQmlRootObject root = program.getRootObject();
		List<IQmlObjectMember> members = root.getBody().getMembers();
		assertEquals("Unexpected number of root object members", 1, members.size());
		assertThat(members.get(0), instanceOf(IQmlPropertyBinding.class));
		IQmlPropertyBinding bind = (IQmlPropertyBinding) members.get(0);
		assertThat(bind.getBinding(), instanceOf(IQmlScriptBinding.class));
		IQmlScriptBinding scriptBinding = (IQmlScriptBinding) bind.getBinding();
		assertFalse("Script binding was not a JavaScript expression", scriptBinding.isBlock());
		assertThat(scriptBinding.getScript(), instanceOf(IJSBinaryExpression.class));
		assertEquals("Unexpected expression type", "BinaryExpression", scriptBinding.getScript().getType());
		IJSBinaryExpression expr = (IJSBinaryExpression) scriptBinding.getScript();
		assertEquals("Unexpected binary operator", BinaryOperator.Add, expr.getOperator());
	}

	@Test
	public void testCompletions1() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\tproperty int height\n\the|\n}",
				new QMLTernCompletion("height", "number", "test1.qml"));
	}

	@Test
	public void testCompletions2() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\tproperty int height\n\tproperty int width\n\tproperty string text\n\t|\n}",
				new QMLTernCompletion("height", "number", "test1.qml"),
				new QMLTernCompletion("text", "string", "test1.qml"),
				new QMLTernCompletion("width", "number", "test1.qml"));
	}

	@Test
	public void testCompletions3() throws Throwable {
		if (analyzer == null) {
			return;
		}
		getCompletions("Window {\n\tproperty int height\n\tObject {\n\t\t|\n\t}\n}");
	}

	@Test
	public void testCompletions4() throws Throwable {
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
	public void testCompletions5() throws Throwable {
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
	public void testCompletions6() throws Throwable {
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
	public void testCompletions7() throws Throwable {
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
