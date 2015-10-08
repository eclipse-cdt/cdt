package org.eclipse.cdt.qt.core.qml.tests;

import static org.eclipse.cdt.qt.core.qml.tests.util.QMLParseTreeUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLLexer;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLListener;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlObjectLiteralContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlPragmaDeclarationContext;
import org.eclipse.cdt.internal.qt.core.qml.parser.QMLParser.QmlProgramContext;
import org.eclipse.cdt.qt.core.qml.tests.util.ExpectedQmlAttribute;
import org.eclipse.cdt.qt.core.qml.tests.util.ExpectedQmlImportDeclaration;
import org.eclipse.cdt.qt.core.qml.tests.util.ExpectedQmlObjectLiteral;
import org.eclipse.cdt.qt.core.qml.tests.util.ExpectedQmlProgram;
import org.eclipse.cdt.qt.core.qml.tests.util.ExpectedQmlPropertyDeclaration;
import org.eclipse.cdt.qt.core.qml.tests.util.ExpectedQmlRootObjectLiteral;
import org.eclipse.cdt.qt.core.qml.tests.util.QMLParseTreeUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class QMLParserTest extends AbstractParserTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	public void runParser(CharSequence code, QMLListener listener) throws Exception {
		ANTLRInputStream input = new ANTLRInputStream(code.toString());

		// Create the lexer
		QMLLexer lexer = new QMLLexer(input);
		lexer.addErrorListener(createANTLRErrorListener());
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		// Create and run the parser
		QMLParser parser = new QMLParser(tokens);
		parser.addParseListener(listener);
		parser.addErrorListener(createANTLRErrorListener());
		parser.qmlProgram();
	}

	// import Namespace 2.0;
	@Test
	public void test_ImportStatement() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(
								new ExpectedQmlImportDeclaration("Namespace", "2.0", null))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// import Namespace 2.0 as SingletonTypeIdentifier;
	@Test
	public void test_ImportStatement_AsIdentifier() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(
								new ExpectedQmlImportDeclaration("Namespace", "2.0", "SingletonTypeIdentifier"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		});
	}

	// import QtQuick 2.0;
	// import MyModule 7.3 as ModuleName;
	@Test
	public void test_ImportStatement_MultipleImports() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(
								new ExpectedQmlImportDeclaration("QtQuick", "2.0", null), //$NON-NLS-1$ //$NON-NLS-2$
								new ExpectedQmlImportDeclaration("MyModule", "7.3", "ModuleName"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		});
	}

	// import QtQuick 2.0
	@Test
	public void test_ImportStatement_NoSemi() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(
								new ExpectedQmlImportDeclaration("QtQuick", "2.0", null))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// import "javascript.js"
	@Test
	public void test_ImportStatement_StringLiteral() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(
								new ExpectedQmlImportDeclaration("\"javascript.js\"", null, null))); //$NON-NLS-1$
			}
		});
	}

	// import "javascript.js" 1.0 as JavaScriptFile
	@Test
	public void test_ImportStatement_StringLiteralAsIdentifier() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(
								new ExpectedQmlImportDeclaration("\"javascript.js\"", "1.0", "JavaScriptFile"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		});
	}

	// pragma library
	@Test
	public void test_PragmaStatement() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlPragmaDeclaration(QmlPragmaDeclarationContext ctx) {
				assertEquals("library", ctx.Identifier().getText()); //$NON-NLS-1$
			}
		});
	}

	// Object {
	// }
	@Test
	public void test_QMLObjectLiteral() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(new ExpectedQmlRootObjectLiteral("Object"))); //$NON-NLS-1$
			}
		});
	}

	// Object {
	//     property var test
	// }
	@Test
	public void test_QMLPropertyDeclaration() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "var", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property var test : c
	// }
	@Test
	public void test_QMLPropertyDeclaration_WithAssignment() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "var", "c")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		});
	}

	// Object {
	//     readonly property var test
	// }
	@Test
	public void test_QMLPropertyDeclaration_ReadOnly() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(true, "test", "var", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property var test;
	// }
	@Test
	public void test_QMLPropertyDeclaration_WithSemi() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "var", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property var color;
	// }
	@Test
	public void test_QMLPropertyIdentifier_ColorAsIdentifier() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "color", "var", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property var list;
	// }
	@Test
	public void test_QMLPropertyIdentifier_ListAsIdentifier() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "list", "var", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property var url;
	// }
	@Test
	public void test_QMLPropertyIdentifier_UrlAsIdentifier() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "url", "var", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property var real;
	// }
	@Test
	public void test_QMLPropertyIdentifier_RealAsIdentifier() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "real", "var", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property var string;
	// }
	@Test
	public void test_QMLPropertyIdentifier_StringAsIdentifier() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "string", "var", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property boolean test;
	// }
	@Test
	public void test_QMLPropertyType_Boolean() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "boolean", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property double test;
	// }
	@Test
	public void test_QMLPropertyType_Double() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "double", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property int test;
	// }
	@Test
	public void test_QMLPropertyType_Int() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "int", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property list test;
	// }
	@Test
	public void test_QMLPropertyType_List() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "list", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property color test;
	// }
	@Test
	public void test_QMLPropertyType_Color() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "color", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property real test;
	// }
	@Test
	public void test_QMLPropertyType_Real() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "real", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property string test;
	// }
	@Test
	public void test_QMLPropertyType_String() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "string", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property url test;
	// }
	@Test
	public void test_QMLPropertyType_URL() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QmlObjectLiteralContext rootObject = getRootObjectLiteral(ctx);
				QMLParseTreeUtil.assertEquals(rootObject.qmlMembers().qmlMember(0),
						new ExpectedQmlPropertyDeclaration(false, "test", "url", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// Object {
	//     property error test;
	// }
	@Test
	public void test_QMLPropertyType_InvalidType() throws Exception {
		expectedException.expectMessage(containsString("at 'error'")); //$NON-NLS-1$
		runParser(getComment(), new AbstractQMLListener() {
			// Ignore. We expect this test to fail at the 'error' token.
		});
	}

	// import QtQuick 2.0
	//
	// Rectangle {
	//    width: 200
	//    height: 200
	//    color: "red"
	//
	//    Text {
	//        anchors.centerIn: parent
	//        text: "Hello, QML!"
	//    }
	// }
	@Test
	public void test_RectangleExample() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(
								new ExpectedQmlImportDeclaration("QtQuick", "2.0", null), //$NON-NLS-1$ //$NON-NLS-2$
								new ExpectedQmlRootObjectLiteral("Rectangle", //$NON-NLS-1$
										new ExpectedQmlAttribute("width", "200"), //$NON-NLS-1$ //$NON-NLS-2$
										new ExpectedQmlAttribute("height", "200"), //$NON-NLS-1$ //$NON-NLS-2$
										new ExpectedQmlAttribute("color", "\"red\""), //$NON-NLS-1$ //$NON-NLS-2$
										new ExpectedQmlObjectLiteral("Text", //$NON-NLS-1$
												new ExpectedQmlAttribute("anchors.centerIn", "parent"), //$NON-NLS-1$ //$NON-NLS-2$
												new ExpectedQmlAttribute("text", "\"Hello, QML!\""))))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// import QtQuick 2.0
	//
	// Rectangle {
	//     y: 200; width: 80; height: 80
	//     rotation: 90
	//     gradient: Gradient {
	//         GradientStop { position: 0.0; color: "lightsteelblue" }
	//         GradientStop { position: 1.0; color: "blue" }
	//     }
	// }
	@Test
	public void test_GradientExample() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(
								new ExpectedQmlImportDeclaration("QtQuick", "2.0", null), //$NON-NLS-1$ //$NON-NLS-2$
								new ExpectedQmlRootObjectLiteral("Rectangle", //$NON-NLS-1$
										new ExpectedQmlAttribute("y", "200"), //$NON-NLS-1$ //$NON-NLS-2$
										new ExpectedQmlAttribute("width", "80"), //$NON-NLS-1$ //$NON-NLS-2$
										new ExpectedQmlAttribute("height", "80"), //$NON-NLS-1$ //$NON-NLS-2$
										new ExpectedQmlAttribute("rotation", "90"), //$NON-NLS-1$ //$NON-NLS-2$
										new ExpectedQmlAttribute("gradient", //$NON-NLS-1$
												new ExpectedQmlObjectLiteral("Gradient", //$NON-NLS-1$
														new ExpectedQmlObjectLiteral("GradientStop", //$NON-NLS-1$
																new ExpectedQmlAttribute("position", "0.0"), //$NON-NLS-1$ //$NON-NLS-2$
																new ExpectedQmlAttribute("color", "\"lightsteelblue\"")), //$NON-NLS-1$ //$NON-NLS-2$
														new ExpectedQmlObjectLiteral("GradientStop", //$NON-NLS-1$
																new ExpectedQmlAttribute("position", "1.0"), //$NON-NLS-1$ //$NON-NLS-2$
																new ExpectedQmlAttribute("color", "\"blue\""))))))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	// import QtQuick 2.3
	// import QtQuick.Window 2.2
	//
	// Window {
	//     visible: true
	//
	//     MouseArea {
	//         anchors.fill: parent
	//         onClicked: {
	//             Qt.quit();
	//         }
	//     }
	//
	//     Text {
	//         text: qsTr("Hello World")
	//         anchors.centerIn: parent
	//     }
	// }
	@Test
	public void test_HelloWorld() throws Exception {
		runParser(getComment(), new AbstractQMLListener() {

			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {
				QMLParseTreeUtil.assertEquals(ctx,
						new ExpectedQmlProgram(
								new ExpectedQmlImportDeclaration("QtQuick", "2.3", null), //$NON-NLS-1$ //$NON-NLS-2$
								new ExpectedQmlImportDeclaration("QtQuick.Window", "2.2", null), //$NON-NLS-1$//$NON-NLS-2$
								new ExpectedQmlRootObjectLiteral("Window", //$NON-NLS-1$
										new ExpectedQmlAttribute("visible", "true"), //$NON-NLS-1$ //$NON-NLS-2$
										new ExpectedQmlObjectLiteral("MouseArea", //$NON-NLS-1$
												new ExpectedQmlAttribute("anchors.fill", "parent"), //$NON-NLS-1$ //$NON-NLS-2$
												new ExpectedQmlAttribute("onClicked", "{Qt.quit();}")), //$NON-NLS-1$ //$NON-NLS-2$
										new ExpectedQmlObjectLiteral("Text", //$NON-NLS-1$
												new ExpectedQmlAttribute("text", "qsTr(\"Hello World\")"), //$NON-NLS-1$ //$NON-NLS-2$
												new ExpectedQmlAttribute("anchors.centerIn", "parent"))))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}
}
