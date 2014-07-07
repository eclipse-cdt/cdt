/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.inGroup;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForWidget;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.widgetIsEnabled;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;
import org.junit.After;

public abstract class AbstractTest {
    protected static SWTWorkbenchBot bot;
    protected static String projectName;
    protected static SWTBotShell mainShell;
    protected static SWTBotView projectExplorer;

    public static void init(String projectName) throws Exception {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        bot = new SWTWorkbenchBot();
        mainShell = null;
        for (int i = 0, attempts = 100; i < attempts; i++) {
            for (SWTBotShell shell : bot.shells()) {
                if (shell.getText().contains("Eclipse Platform")) {
                    mainShell = shell;
                    shell.setFocus();
                    break;
                }
            }
        }
        assertNotNull(mainShell);
        // Close the Welcome view if it exists
        try {
            bot.viewByTitle("Welcome").close();
        } catch (Exception e) {
            // do nothing
        }
        // Turn off automatic building by default
        clickMainMenu("Window", "Preferences");
        SWTBotShell shell = bot.shell("Preferences");
        shell.activate();
        bot.text().setText("Workspace");
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "General",
                "Workspace"));
        SWTBotCheckBox buildAuto = bot.checkBox("Build automatically");
        if (buildAuto != null && buildAuto.isChecked()) {
            buildAuto.click();
        }
        bot.button("Apply").click();
        // Ensure that the C/C++ perspective is chosen automatically
        // and doesn't require user intervention
        bot.text().setText("Perspectives");
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "General",
                "Perspectives"));
        clickRadioButtonInGroup("Always open",
                "Open the associated perspective when creating a new project");
        bot.button("OK").click();

        AbstractTest.projectName = projectName;
        clickMainMenu("File", "New", "Project...");
        shell = bot.shell("New Project");
        shell.activate();
        bot.text().setText("C Project");
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), "C/C++",
                "C Project"));
        bot.button("Next >").click();

        bot.textWithLabel("Project name:").setText(projectName);
        bot.tree().expandNode("GNU Autotools")
        .select("Hello World ANSI C Autotools Project");
        bot.button("Finish").click();
        bot.waitUntil(Conditions.shellCloses(shell));

        IProjectNature nature = checkProject().getNature(
                "org.eclipse.cdt.autotools.core.autotoolsNatureV2");
        assertTrue(nature != null);

        projectExplorer = bot.viewByTitle("Project Explorer");
    }

    public static class NodeAvailableAndSelect extends DefaultCondition {

        private SWTBotTree tree;
        private String parent;
        private String node;

        /**
         * Wait for a tree node (with a known parent) to become visible, and
         * select it when it does. Note that this wait condition should only be
         * used after having made an attempt to reveal the node.
         *
         * @param tree
         *            The SWTBotTree that contains the node to select.
         * @param parent
         *            The text of the parent node that contains the node to
         *            select.
         * @param node
         *            The text of the node to select.
         */
        public NodeAvailableAndSelect(SWTBotTree tree, String parent,
                String node) {
            this.tree = tree;
            this.node = node;
            this.parent = parent;
        }

        @Override
        public boolean test() {
            try {
                SWTBotTreeItem parentNode = tree.getTreeItem(parent);
                parentNode.getNode(node).select();
                return true;
            } catch (WidgetNotFoundException e) {
                return false;
            }
        }

        @Override
        public String getFailureMessage() {
            return "Timed out waiting for " + node; //$NON-NLS-1$
        }
    }

    /**
     * Enter the project folder so as to avoid expanding trees later
     */
    public static void enterProjectFolder() {
        projectExplorer.setFocus();
        projectExplorer.bot().tree().select(projectName).contextMenu("Go Into")
        .click();
        bot.waitUntil(waitForWidget(WidgetMatcherFactory.withText(projectName),
                projectExplorer.getWidget()));
    }

    /**
     * Exit from the project tree.
     */
    public static void exitProjectFolder() {
        projectExplorer.setFocus();
        SWTBotToolbarButton forwardButton;
        try {
            forwardButton = projectExplorer.toolbarPushButton("Forward");
        } catch (WidgetNotFoundException e) {
            // If the "Forward" button is not found, already at the top level.
            return;
        }
        SWTBotToolbarButton backButton = projectExplorer
                .toolbarPushButton("Back to Workspace");
        if (backButton.isEnabled()) {
            backButton.click();
            bot.waitUntil(widgetIsEnabled(forwardButton));
        }
    }

    /**
     * Selects a radio button with the given <code>mnemonicText</code> in a
     * group with the given label <inGroup>, while also deselecting whatever
     * other radio button in the group that is already selected. Workaround for
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=344484
     */
    public static void clickRadioButtonInGroup(String mnemonicText,
            final String inGroup) {
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                @SuppressWarnings("unchecked")
                Matcher<Widget> matcher = allOf(inGroup(inGroup),
                        widgetOfType(Button.class),
                        withStyle(SWT.RADIO, "SWT.RADIO"));
                int i = 0;
                while (true) {
                    Button b;
                    try {
                        b = (Button) bot.widget(matcher, i++);
                    } catch (IndexOutOfBoundsException e) {
                        return;
                    }
                    if (b.getSelection()) {
                        b.setSelection(false);
                        return;
                    }
                }
            }
        });
        bot.radioInGroup(mnemonicText, inGroup).click();
    }

    public static void clickContextMenu(AbstractSWTBot<? extends Control> bot,
            String... texts) {
        new SWTBotMenu(ContextMenuHelper.contextMenu(bot, texts)).click();
    }

    public static void clickVolatileContextMenu(
            AbstractSWTBot<? extends Control> bot, String... texts) {
        int tries = 0;
        final int maxTries = 2;
        while (true) {
            try {
                clickContextMenu(bot, texts);
                return;
            } catch (Exception e) {
                if (++tries > maxTries) {
                    throw e;
                }
            }
        }
    }

    public static void clickProjectContextMenu(String... texts) {
        clickVolatileContextMenu(bot.viewByTitle("Project Explorer").bot()
                .tree().select(projectName), texts);
    }

    /**
     * Click an item from the main Eclipse menu, with a guarantee that the main
     * shell will be in focus.
     *
     * @param items
     */
    public static void clickMainMenu(String... items) {
        if (items.length == 0) {
            return;
        }
        mainShell.setFocus();
        SWTBotMenu menu = bot.menu(items[0]);
        for (int i = 1; i < items.length; i++) {
            menu = menu.menu(items[i]);
        }
        menu.click();
    }

    public static SWTBotShell openProperties(String parentCategory,
            String category) {
        clickContextMenu(projectExplorer.bot().tree().select(projectName),
                "Properties");
        SWTBotShell shell = bot.shell("Properties for " + projectName);
        shell.activate();
        bot.text().setText(category);
        bot.waitUntil(new NodeAvailableAndSelect(bot.tree(), parentCategory,
                category));
        shell.activate();
        return shell;
    }

    public static IProject checkProject() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        assertTrue(workspace != null);
        IWorkspaceRoot root = workspace.getRoot();
        assertTrue(root != null);
        IProject project = root.getProject(projectName);
        assertTrue(project != null);
        return project;
    }

    public static SWTBotView viewConsole(String consoleType) {
        SWTBotView view = bot.viewByPartName("Console");
        view.setFocus();
        SWTBotToolbarDropDownButton b = view
                .toolbarDropDownButton("Display Selected Console");
        org.hamcrest.Matcher<MenuItem> withRegex = withRegex(".*" + consoleType
                + ".*");
        bot.shell("C/C++ - Eclipse Platform").activate();
        b.menuItem(withRegex).click();
        try {
            b.pressShortcut(KeyStroke.getInstance("ESC"));
        } catch (ParseException e) {
        }
        view.setFocus();
        return view;
    }

    @After
    public void cleanUp() {
        exitProjectFolder();
        SWTBotShell[] shells = bot.shells();
        for (final SWTBotShell shell : shells) {
            if (!shell.equals(mainShell)) {
                String shellTitle = shell.getText();
                if (shellTitle.length() > 0
                        && !shellTitle.startsWith("Quick Access")) {
                    UIThreadRunnable.syncExec(new VoidResult() {
                        @Override
                        public void run() {
                            if (shell.widget.getParent() != null
                                    && !shell.isOpen()) {
                                shell.close();
                            }
                        }
                    });
                }
            }
        }
        bot.closeAllEditors();
        mainShell.activate();
    }
}
