/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@RunWith(SWTBotJunit4ClassRunner.class)
public class SetConfigurationParameter {
	
	private static SWTWorkbenchBot	bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		bot = new SWTWorkbenchBot();
		// Close the Welcome view if it exists
		try {
		bot.viewByTitle("Welcome").close();
		// Turn off automatic building by default
		} catch (Exception e) {
			// do nothing
		}
		// Turn off automatic building by default
		bot.menu("Window").menu("Preferences").click();
		SWTBotShell shell = bot.shell("Preferences");
		shell.activate();
		bot.tree().expandNode("General").select("Workspace");
		SWTBotCheckBox buildAuto = bot.checkBox("Build automatically");
		if (buildAuto != null && buildAuto.isChecked())
			buildAuto.click();
		bot.button("Apply").click();
		// Ensure that the C/C++ perspective is chosen automatically
		// and doesn't require user intervention
		bot.tree().expandNode("General").select("Perspectives");
		SWTBotRadio radio = bot.radio("Always open");
		if (radio != null && !radio.isSelected())
			radio.click();
		bot.button("OK").click();
	}
 
	@Test
	// Verify we can create a sample Autotools project using the New C Project UI
	public void canCreateANewAutotoolsProject() throws Exception {
		bot.menu("File").menu("New").menu("Project...").click();
 
		SWTBotShell shell = bot.shell("New Project");
		shell.activate();
		bot.tree().expandNode("C/C++").select("C Project");
		bot.button("Next >").click();
 
		bot.textWithLabel("Project name:").setText("GnuProject0");
		bot.tree().expandNode("GNU Autotools").select("Hello World ANSI C Autotools Project");
 
		bot.button("Finish").click();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject0");
		assertTrue(project != null);
		IProjectNature nature = project.getNature("org.eclipse.cdt.autotools.core.autotoolsNatureV2");
		assertTrue(nature != null);
	}
	
	@Test
	// Verify we can set a configuration parameter and that it is recorded in
	// the .autotools file for the project
	public void canSetConfigParm() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Properties").click();
		SWTBotShell shell = bot.shell("Properties for GnuProject0");
		shell.activate();
		bot.tree().expandNode("Autotools").select("Configure Settings");
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		// Set the configure parameters to be --enable-jeff via user-defined options
		SWTBotText text = bot.textWithLabel("Additional command-line options");
		text.typeText("--enable-jeff");
		bot.button("OK").click();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject0");
		assertTrue(project != null);
		IPath path = project.getLocation();
		path = path.append(".autotools");
		File f = new File(path.toOSString());
		assertTrue(f.exists());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document d = db.parse(f);
		Element e = d.getDocumentElement();
		// Get the stored configuration data
		NodeList cfgs = e.getElementsByTagName("configuration"); // $NON-NLS-1$
		assertEquals(1, cfgs.getLength());
		Node n = cfgs.item(0);
		NodeList l = n.getChildNodes();
		// Verify user field in .autotools file is set to --enable-jeff
		boolean foundUser = false;
		for (int y = 0; y < l.getLength(); ++y) {
			Node child = l.item(y);
			if (child.getNodeName().equals("option")) { // $NON-NLS-1$
				NamedNodeMap optionAttrs = child.getAttributes();
				Node idNode = optionAttrs.getNamedItem("id"); // $NON-NLS-1$
				Node valueNode = optionAttrs.getNamedItem("value"); // $NON-NLS-1$
				assertTrue(idNode != null);
				assertTrue(valueNode != null);
				String id = idNode.getNodeValue();
				String value = valueNode.getNodeValue();
				if (id.equals("user")) {
					foundUser = true;
					assertEquals(value, "--enable-jeff");
				}
			}
		}
		assertTrue(foundUser);
	}

	@Test
	// Verify we can build the project with a configuration parameter and that
	// the configuration parameter can be found in the config.status file.
	public void canBuildWithConfigParm() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Build Project").click();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject0");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// We need to wait until the a.out file is created so
		// sleep a bit and look for it...give up after 120 seconds
		for (int i = 0; i < 240; ++i) {
			bot.sleep(500);
			File f = new File(path.append("src/a.out").toOSString());
			if (f.exists())
				break;
		}
		File f = new File(path.append("config.status").toOSString());
		assertTrue(f.exists());
		BufferedReader r = new BufferedReader(new FileReader(f));
		int ch;
		boolean optionFound = false;
		// Read config.status and look for the string --enable-jeff
		// which is a simple verification that the option was used in the 
		// configure step.
		while ((ch = r.read()) != -1) {
			if (ch == '-') {
				char[] buf = new char[12];
				r.mark(100);
				int count = r.read(buf);
				if (count < 12)
					break;
				String s = new String(buf);
				if (s.equals("-enable-jeff")) {
					optionFound = true;
					break;
				} else {
					r.reset();
				}
			}
		}
		assertTrue(optionFound);
		view = bot.viewByTitle("Console");
		view.setFocus();
		SWTBotToolbarDropDownButton b = bot.toolbarDropDownButtonWithTooltip("Display Selected Console");
		org.hamcrest.Matcher<MenuItem> withRegex = withRegex(".*Configure.*");
		b.menuItem(withRegex).click();
		b.pressShortcut(KeyStroke.getInstance("ESC"));
		String output = view.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*WARNING:.*unrecognized.*--enable-jeff.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// Verify that if we build again, we don't reconfigure.
		// Verifies fix for bug: #308261
		long oldDate = f.lastModified();
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Build Project").click();
		path = project.getLocation();
		// We need to wait until the a.out file is created so
		// sleep a bit and look for it...give up after 120 seconds
		for (int i = 0; i < 240; ++i) {
			bot.sleep(500);
			File tmp = new File(path.append("src/a.out").toOSString());
			if (tmp.exists())
				break;
		}
		f = new File(path.append("config.status").toOSString());
		assertTrue(f.exists());
		long newDate = f.lastModified();
		assertEquals(newDate, oldDate);
	}

	@Test
	// Verify a new configuration will copy the configuration parameters
	// of its base configuration.
	public void newConfigCopiesParms() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Build Configurations").menu("Manage...").click();
		SWTBotShell shell = bot.shell("GnuProject0: Manage Configurations");
		shell.activate();
		bot.button("New...").click();
		shell = bot.shell("Create New Configuration");
		shell.activate();
		SWTBotText t = bot.textWithLabel("Name:");
		t.typeText("debug");
		SWTBotRadio radio = bot.radio("Existing configuration");
		if (!radio.isSelected())
			radio.click();
		bot.button("OK").click();
		shell = bot.shell("GnuProject0: Manage Configurations");
		shell.activate();
		bot.button("New...").click();
		shell = bot.shell("Create New Configuration");
		shell.activate();
		t = bot.textWithLabel("Name:");
		t.typeText("default");
		radio = bot.radio("Default configuration");
		if (!radio.isSelected())
			radio.click();
		bot.button("OK").click();
		shell = bot.shell("GnuProject0: Manage Configurations");
		shell.activate();
		SWTBotTable table = bot.table();
		assertTrue(table.containsItem("debug"));
		table.getTableItem("debug").select();
		bot.button("Set Active").click();
		bot.button("OK").click();
		// Verify the debug configuration is active and has a user parameter: --enable-jeff
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Properties").click();
		shell = bot.shell("Properties for GnuProject0");
		shell.activate();
		bot.tree().expandNode("Autotools").select("Configure Settings");
		SWTBotCombo configs = bot.comboBoxWithLabel(UIMessages.getString("AbstractPage.6"));
		configs.setFocus();
		String[] items = configs.items();
		for (int i = 0; i < items.length; ++i) {
			if (items[i].contains("debug") && items[i].contains("Active"))
				configs.setSelection(i);
		}
		assertTrue(configs.getText().contains("debug"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		SWTBotText text = bot.textWithLabel("Additional command-line options");
		String val = text.getText();
		assertEquals(val, "--enable-jeff");
		// Verify that the build directory for the new configuration has been
		// switched to build-debug
		bot.tree().expandNode("C/C++ Build").select();
		String buildDir = bot.textWithLabel("Build directory:").getText();
		assertTrue(buildDir.endsWith("build-debug"));
		// Verify the default configuration has no user setting
		bot.tree().expandNode("Autotools").select("Configure Settings");
		configs = bot.comboBoxWithLabel(UIMessages.getString("AbstractPage.6"));
		configs.setSelection("default");
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		val = text.getText();
		assertEquals(val, "");
		bot.button("OK").click();
		// Build the project again and verify we get a build-debug directory
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Build Project").click();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject0");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			File f = new File(path.append("build-debug/src/a.out").toOSString());
			if (f.exists())
				break;
		}
		File f = new File(path.append("build-debug/config.status").toOSString());
		assertTrue(f.exists());
		BufferedReader r = new BufferedReader(new FileReader(f));
		int ch;
		boolean optionFound = false;
		// Read config.status and look for the string --enable-jeff
		// which is a simple verification that the option was used in the 
		// configure step.
		while ((ch = r.read()) != -1) {
			if (ch == '-') {
				char[] buf = new char[12];
				r.mark(100);
				int count = r.read(buf);
				if (count < 12)
					break;
				String s = new String(buf);
				if (s.equals("-enable-jeff")) {
					optionFound = true;
					break;
				} else {
					r.reset();
				}
			}
		}
		assertTrue(optionFound);
		// Verify we cleaned out the top-level build directory (i.e. that there
		// is no config.status there anymore).
		path = project.getLocation().append("config.status");
		f = new File(path.toOSString());
		assertTrue(!f.exists());
		path = project.getLocation().append(".autotools");
		f = new File(path.toOSString());
		assertTrue(f.exists());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document d = db.parse(f);
		Element e = d.getDocumentElement();
		// Get the stored configuration data
		NodeList cfgs = e.getElementsByTagName("configuration"); // $NON-NLS-1$
		assertEquals(3, cfgs.getLength());
		int foundUser = 0;
		for (int x = 0; x < cfgs.getLength(); ++x) {
			Node n = cfgs.item(x);
			NodeList l = n.getChildNodes();
			// Verify two of the user fields in .autotools file are set to --enable-jeff
			for (int y = 0; y < l.getLength(); ++y) {
				Node child = l.item(y);
				if (child.getNodeName().equals("option")) { // $NON-NLS-1$
					NamedNodeMap optionAttrs = child.getAttributes();
					Node idNode = optionAttrs.getNamedItem("id"); // $NON-NLS-1$
					Node valueNode = optionAttrs.getNamedItem("value"); // $NON-NLS-1$
					assertTrue(idNode != null);
					assertTrue(valueNode != null);
					String id = idNode.getNodeValue();
					String value = valueNode.getNodeValue();
					if (id.equals("user")) {
						if (value.equals("--enable-jeff"))
							++foundUser;
					}
				}
			}
		}
		assertEquals(foundUser, 2);
	}

	@Test
	// Verify we can do a double rename of configurations, renaming one configuration to
	// another and then cancel without changing configuration settings.
	public void doubleRenameCancel() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Properties").click();
		SWTBotShell shell = bot.shell("Properties for GnuProject0");
		shell.activate();
		bot.tree().expandNode("Autotools").select("Configure Settings");
		SWTBotCombo configs = bot.comboBoxWithLabel(UIMessages.getString("AbstractPage.6"));
		assertTrue(configs.getText().contains("debug"));
		bot.button("Manage Configurations...").click();
		shell = bot.shell("GnuProject0: Manage Configurations");
		shell.activate();
		bot.table().select("debug");
		// Rename "debug" to "release" and rename "default" to "debug".
		// The settings should follow the rename operation.
		bot.button("Rename...").click();
		shell = bot.shell("Rename Configuration");
		shell.activate();
		SWTBotText text = bot.textWithLabel("Name:");
		text.setText("");
		text.typeText("release");
		bot.button("OK").click();
		shell = bot.shell("GnuProject0: Manage Configurations");
		shell.activate();
		bot.table().select("default");
		bot.button("Rename...").click();
		shell = bot.shell("Rename Configuration");
		shell.activate();
		text = bot.textWithLabel("Name:");
		text.setText("");
		text.typeText("debug");
		bot.button("OK").click();
		shell = bot.shell("GnuProject0: Manage Configurations");
		shell.activate();
		bot.button("OK").click();
		// Verify that "release" has --enable-jeff set and that
		// the new "debug" configuration has no user setting.
		shell = bot.shell("Properties for GnuProject0");
		shell.activate();
		configs = bot.comboBoxWithLabel(UIMessages.getString("AbstractPage.6"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		String setting = text.getText();
		assertEquals("--enable-jeff", setting);
		configs.setFocus();
		configs.setSelection("debug");
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		setting = text.getText();
		assertEquals("", setting);
		bot.button("Cancel").click();
		// Cancel and then verify that "debug" is back to normal with
		// --enable-jeff set and that "default" is back to normal with
		// no user setting.
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Properties").click();
		shell = bot.shell("Properties for GnuProject0");
		shell.activate();
		bot.tree().expandNode("Autotools").select("Configure Settings");
		configs = bot.comboBoxWithLabel(UIMessages.getString("AbstractPage.6"));
		assertTrue(configs.getText().contains("debug"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		setting = text.getText();
		assertEquals("--enable-jeff", setting);
		configs.setFocus();
		configs.setSelection("default");
		assertTrue(configs.getText().contains("default"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		setting = text.getText();
		assertEquals("", setting);
		bot.button("OK").click();
	}

	@Test
	// Verify we can do a double rename of configurations, renaming one configuration to
	// another and inheriting the settings properly.
	public void doubleRenameOk() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Properties").click();
		SWTBotShell shell = bot.shell("Properties for GnuProject0");
		shell.activate();
		bot.tree().expandNode("Autotools").select("Configure Settings");
		SWTBotCombo configs = bot.comboBoxWithLabel(UIMessages.getString("AbstractPage.6"));
		assertTrue(configs.getText().contains("debug"));
		bot.button("Manage Configurations...").click();
		shell = bot.shell("GnuProject0: Manage Configurations");
		shell.activate();
		bot.table().select("debug");
		// Rename "debug" to "release" and rename "default" to "debug".
		// The settings should follow the rename operation.
		bot.button("Rename...").click();
		shell = bot.shell("Rename Configuration");
		shell.activate();
		SWTBotText text = bot.textWithLabel("Name:");
		text.setText("");
		text.typeText("release");
		bot.button("OK").click();
		shell = bot.shell("GnuProject0: Manage Configurations");
		shell.activate();
		bot.table().select("default");
		bot.button("Rename...").click();
		shell = bot.shell("Rename Configuration");
		shell.activate();
		text = bot.textWithLabel("Name:");
		text.setText("");
		text.typeText("debug");
		bot.button("OK").click();
		shell = bot.shell("GnuProject0: Manage Configurations");
		shell.activate();
		bot.button("OK").click();
		shell = bot.shell("Properties for GnuProject0");
		shell.activate();
		bot.button("OK").click();
		// Verify changes have taken effect permanently
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject0");
		bot.menu("Project", 1).menu("Properties").click();
		shell = bot.shell("Properties for GnuProject0");
		shell.activate();
		bot.tree().expandNode("Autotools").select("Configure Settings");
		configs = bot.comboBoxWithLabel(UIMessages.getString("AbstractPage.6"));
		assertTrue(configs.getText().contains("release"));
		assertTrue(configs.getText().contains("Active"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		String setting = text.getText();
		assertEquals("--enable-jeff", setting);
		configs.setFocus();
		configs.setSelection("debug");
		assertTrue(configs.getText().contains("debug"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		setting = text.getText();
		assertEquals("", setting);
		bot.button("OK").click();
	}

	@AfterClass
	public static void sleep() {
		bot.sleep(4000);
	}

}
