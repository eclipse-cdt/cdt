/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.tests;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.internal.qt.core.location.Position;
import org.eclipse.cdt.qt.core.location.IPosition;
import org.eclipse.cdt.qt.core.qmldir.IQDirAST;
import org.eclipse.cdt.qt.core.qmldir.IQDirASTNode;
import org.eclipse.cdt.qt.core.qmldir.IQDirClassnameCommand;
import org.eclipse.cdt.qt.core.qmldir.IQDirCommentCommand;
import org.eclipse.cdt.qt.core.qmldir.IQDirDependsCommand;
import org.eclipse.cdt.qt.core.qmldir.IQDirDesignerSupportedCommand;
import org.eclipse.cdt.qt.core.qmldir.IQDirInternalCommand;
import org.eclipse.cdt.qt.core.qmldir.IQDirModuleCommand;
import org.eclipse.cdt.qt.core.qmldir.IQDirPluginCommand;
import org.eclipse.cdt.qt.core.qmldir.IQDirResourceCommand;
import org.eclipse.cdt.qt.core.qmldir.IQDirSingletonCommand;
import org.eclipse.cdt.qt.core.qmldir.IQDirSyntaxError;
import org.eclipse.cdt.qt.core.qmldir.IQDirTypeInfoCommand;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryParser;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryParser.SyntaxError;
import org.junit.Test;

@SuppressWarnings("nls")
public class QMLDirectoryParserTests {

	public void assertLocation(int start, int end, IPosition locStart, IPosition locEnd, IQDirASTNode node) {
		// Check position offsets
		assertEquals("Unexpected start position", start, node.getStart());
		assertEquals("Unexpected end position", end, node.getEnd());

		// Check SourceLocation start
		assertEquals("Unexpected location start line", locStart.getLine(), node.getLocation().getStart().getLine());
		assertEquals("Unexpected location start column", locStart.getColumn(),
				node.getLocation().getStart().getColumn());
	}

	private InputStream createInputStream(String s) {
		return new ByteArrayInputStream(s.getBytes());
	}

	@Test
	public void testModuleCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("module QtQuick.Controls\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirModuleCommand.class));
		IQDirModuleCommand mod = (IQDirModuleCommand) ast.getCommands().get(0);
		assertEquals("Unexpected qualified ID", "QtQuick.Controls", mod.getModuleIdentifier().getText());
		assertLocation(0, 24, new Position(1, 0), new Position(1, 24), mod);
	}

	@Test
	public void testModuleNoIdentifier() {
		try {
			QMLDirectoryParser parser = new QMLDirectoryParser();
			parser.parse(createInputStream("module\n"), false);
			fail("Parser did not throw SyntaxError");
		} catch (SyntaxError e) {
			assertEquals("Unexpected token '\\n' (1:6)", e.getMessage());
		}
	}

	@Test
	public void testSingletonCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("singleton Singleton 2.3 Singleton.qml\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirSingletonCommand.class));
		IQDirSingletonCommand singleton = (IQDirSingletonCommand) ast.getCommands().get(0);
		assertEquals("Unexpected type name", "Singleton", singleton.getTypeName().getText());
		assertEquals("Unexpected initial version", "2.3", singleton.getInitialVersion().getVersionString());
		assertEquals("Unexpected file name", "Singleton.qml", singleton.getFile().getText());
		assertLocation(0, 38, new Position(1, 0), new Position(1, 38), singleton);
	}

	@Test
	public void testInvalidVersionNumber() {
		try {
			QMLDirectoryParser parser = new QMLDirectoryParser();
			parser.parse(createInputStream("singleton Singleton 2 Singleton.qml\n"), false);
			fail("Parser did not throw SyntaxError");
		} catch (SyntaxError e) {
			assertEquals("Unexpected token '2' (1:20)", e.getMessage());
		}
	}

	@Test
	public void testInternalCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("internal MyPrivateType MyPrivateType.qml\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirInternalCommand.class));
		IQDirInternalCommand internal = (IQDirInternalCommand) ast.getCommands().get(0);
		assertEquals("Unexpected type name", "MyPrivateType", internal.getTypeName().getText());
		assertEquals("Unexpected file name", "MyPrivateType.qml", internal.getFile().getText());
		assertLocation(0, 41, new Position(1, 0), new Position(1, 41), internal);
	}

	@Test
	public void testResourceCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("MyScript 1.0 MyScript.qml\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirResourceCommand.class));
		IQDirResourceCommand resource = (IQDirResourceCommand) ast.getCommands().get(0);
		assertEquals("Unexpected type name", "MyScript", resource.getResourceIdentifier().getText());
		assertEquals("Unexpected initial version", "1.0", resource.getInitialVersion().getVersionString());
		assertEquals("Unexpected file name", "MyScript.qml", resource.getFile().getText());
		assertLocation(0, 26, new Position(1, 0), new Position(1, 26), resource);
	}

	@Test
	public void testPluginCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("plugin MyPluginLibrary\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirPluginCommand.class));
		IQDirPluginCommand plugin = (IQDirPluginCommand) ast.getCommands().get(0);
		assertEquals("Unexpected identifier", "MyPluginLibrary", plugin.getName().getText());
		assertEquals("Unexpected path", null, plugin.getPath());
		assertLocation(0, 23, new Position(1, 0), new Position(1, 23), plugin);
	}

	@Test
	public void testPluginCommandWithPath() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("plugin MyPluginLibrary ./lib/\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirPluginCommand.class));
		IQDirPluginCommand plugin = (IQDirPluginCommand) ast.getCommands().get(0);
		assertEquals("Unexpected identifier", "MyPluginLibrary", plugin.getName().getText());
		assertEquals("Unexpected path", "./lib/", plugin.getPath().getText());
		assertLocation(0, 30, new Position(1, 0), new Position(1, 30), plugin);
	}

	@Test
	public void testClassnameCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("classname MyClass\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirClassnameCommand.class));
		IQDirClassnameCommand classname = (IQDirClassnameCommand) ast.getCommands().get(0);
		assertEquals("Unexpected class name", "MyClass", classname.getIdentifier().getText());
		assertLocation(0, 18, new Position(1, 0), new Position(1, 18), classname);
	}

	@Test
	public void testTypeInfoCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("typeinfo mymodule.qmltypes\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirTypeInfoCommand.class));
		IQDirTypeInfoCommand typeinfo = (IQDirTypeInfoCommand) ast.getCommands().get(0);
		assertEquals("Unexpected file name", "mymodule.qmltypes", typeinfo.getFile().getText());
		assertLocation(0, 27, new Position(1, 0), new Position(1, 27), typeinfo);
	}

	@Test
	public void testDependsCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("depends MyOtherModule 1.0\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirDependsCommand.class));
		IQDirDependsCommand depends = (IQDirDependsCommand) ast.getCommands().get(0);
		assertEquals("Unexpected module identifier", "MyOtherModule", depends.getModuleIdentifier().getText());
		assertEquals("Unexpected initial version", "1.0", depends.getInitialVersion().getVersionString());
		assertLocation(0, 26, new Position(1, 0), new Position(1, 26), depends);
	}

	@Test
	public void testDesignerSupportedCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("designersupported\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirDesignerSupportedCommand.class));
		assertLocation(0, 18, new Position(1, 0), new Position(1, 18), ast.getCommands().get(0));
	}

	@Test
	public void testCommentCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("# This is a comment command\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirCommentCommand.class));
		IQDirCommentCommand comment = (IQDirCommentCommand) ast.getCommands().get(0);
		assertEquals("Unexpected text", "# This is a comment command", comment.getText());
	}

	@Test
	public void testSyntaxErrorCommand() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("classname"));
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirSyntaxError.class));
		IQDirSyntaxError err = (IQDirSyntaxError) ast.getCommands().get(0);
		assertEquals("Unexpected message", "Unexpected token 'EOF' (1:9)", err.getSyntaxError().getMessage());
		assertLocation(0, 9, new Position(1, 0), new Position(1, 9), err);
	}

	@Test
	public void testSyntaxErrorCommandIncludesWholeLine() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("classname class extra\n"));
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirSyntaxError.class));
		IQDirSyntaxError err = (IQDirSyntaxError) ast.getCommands().get(0);
		assertEquals("Unexpected message", "Expected token '\\n' or 'EOF', but saw 'extra' (1:16)",
				err.getSyntaxError().getMessage());
		assertLocation(0, 22, new Position(1, 0), new Position(1, 22), err);
	}

	@Test
	public void testExampleQMLDirFile() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("module QtQuick\n" + "plugin qtquick2plugin\n"
				+ "classname QtQuick2Plugin\n" + "typeinfo plugins.qmltypes\n" + "designersupported\n"));

		assertEquals("Unexpected command list size", 5, ast.getCommands().size());
		// Module Command (index 0)
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirModuleCommand.class));
		IQDirModuleCommand mod = (IQDirModuleCommand) ast.getCommands().get(0);
		assertEquals("Unexpected module qualified ID", "QtQuick", mod.getModuleIdentifier().getText());
		// Plugin Command (index 1)
		assertThat("Unexpected command", ast.getCommands().get(1), instanceOf(IQDirPluginCommand.class));
		IQDirPluginCommand plugin = (IQDirPluginCommand) ast.getCommands().get(1);
		assertEquals("Unexpected plugin identifier", "qtquick2plugin", plugin.getName().getText());
		assertEquals("Unexpected plugin path", null, plugin.getPath());
		// Classname Command (index 2)
		assertThat("Unexpected command", ast.getCommands().get(2), instanceOf(IQDirClassnameCommand.class));
		IQDirClassnameCommand classname = (IQDirClassnameCommand) ast.getCommands().get(2);
		assertEquals("Unexpected class name", "QtQuick2Plugin", classname.getIdentifier().getText());
		// Type Info Command (index 3)
		assertThat("Unexpected command", ast.getCommands().get(3), instanceOf(IQDirTypeInfoCommand.class));
		IQDirTypeInfoCommand typeinfo = (IQDirTypeInfoCommand) ast.getCommands().get(3);
		assertEquals("Unexpected type info file name", "plugins.qmltypes", typeinfo.getFile().getText());
		// Designer Supported Command (index 4)
		assertThat("Unexpected command", ast.getCommands().get(4), instanceOf(IQDirDesignerSupportedCommand.class));
	}

	@Test
	public void testExampleQMLDirFileWithError() {
		QMLDirectoryParser parser = new QMLDirectoryParser();
		IQDirAST ast = parser.parse(createInputStream("module QtQuick\n" + "plugin qtquick2plugin\n"
				+ "classnames QtQuick2Plugin\n" + "typeinfo plugins.qmltypes\n" + "designersupported\n"));

		assertEquals("Unexpected command list size", 5, ast.getCommands().size());
		// Module Command (index 0)
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirModuleCommand.class));
		IQDirModuleCommand mod = (IQDirModuleCommand) ast.getCommands().get(0);
		assertEquals("Unexpected module qualified ID", "QtQuick", mod.getModuleIdentifier().getText());
		// Plugin Command (index 1)
		assertThat("Unexpected command", ast.getCommands().get(1), instanceOf(IQDirPluginCommand.class));
		IQDirPluginCommand plugin = (IQDirPluginCommand) ast.getCommands().get(1);
		assertEquals("Unexpected plugin identifier", "qtquick2plugin", plugin.getName().getText());
		assertEquals("Unexpected plugin path", null, plugin.getPath());
		// Syntax Error Command (index 2)
		assertThat("Unexpected command", ast.getCommands().get(2), instanceOf(IQDirSyntaxError.class));
		IQDirSyntaxError err = (IQDirSyntaxError) ast.getCommands().get(2);
		assertEquals("Unexpected error message", "Unexpected token 'QtQuick2Plugin' (3:11)",
				err.getSyntaxError().getMessage());
		// Type Info Command (index 3)
		assertThat("Unexpected command", ast.getCommands().get(3), instanceOf(IQDirTypeInfoCommand.class));
		IQDirTypeInfoCommand typeinfo = (IQDirTypeInfoCommand) ast.getCommands().get(3);
		assertEquals("Unexpected type info file name", "plugins.qmltypes", typeinfo.getFile().getText());
		// Designer Supported Command (index 4)
		assertThat("Unexpected command", ast.getCommands().get(4), instanceOf(IQDirDesignerSupportedCommand.class));
	}

	@Test
	public void testParseTwoDifferentStreams() {
		QMLDirectoryParser parser = new QMLDirectoryParser();

		// Parse module QtQuick.Controls
		IQDirAST ast = parser.parse(createInputStream("module QtQuick.Controls\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirModuleCommand.class));
		IQDirModuleCommand mod = (IQDirModuleCommand) ast.getCommands().get(0);
		assertEquals("Unexpected qualified ID", "QtQuick.Controls", mod.getModuleIdentifier().getText());
		assertLocation(0, 24, new Position(1, 0), new Position(1, 24), mod);

		// Parse a second module MyModule
		ast = parser.parse(createInputStream("module MyModule\n"), false);
		assertEquals("Unexpected command list size", 1, ast.getCommands().size());
		assertThat("Unexpected command", ast.getCommands().get(0), instanceOf(IQDirModuleCommand.class));
		mod = (IQDirModuleCommand) ast.getCommands().get(0);
		assertEquals("Unexpected qualified ID", "MyModule", mod.getModuleIdentifier().getText());
		assertLocation(0, 16, new Position(1, 0), new Position(1, 16), mod);
	}
}
