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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestMakeTargets extends AbstractTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        AbstractTest.init("GnuProject3");
    }

    @Test
    // Verify we can build and run the info MakeTarget tool
    public void canBuildAndAccessInfoTarget() throws Exception {
        clickProjectContextMenu("Build Project");

        // Wait until the project is built
        SWTBotShell shell = bot.shell("Build Project");
        bot.waitUntil(Conditions.shellCloses(shell), 120000);

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        assertTrue(workspace != null);
        IWorkspaceRoot root = workspace.getRoot();
        assertTrue(root != null);
        IProject project = root.getProject(projectName);
        assertTrue(project != null);
        IPath path = project.getLocation();
        path = path.append("config.status");
        File f = new File(path.toOSString());
        assertTrue(f.exists());
        f = new File(path.toOSString());
        assertTrue(f.exists());

        projectExplorer.bot().tree().getTreeItem(projectName).select();
        clickContextMenu(projectExplorer.bot().tree().select(projectName),
                "Make Targets", "Build...");
        shell = bot.shell("Make Targets");
        shell.activate();
        bot.table().getTableItem("info").select();
        bot.button("Build").click();
        bot.sleep(3000);

        SWTBotView consoleView = viewConsole("CDT Build Console");
        String output = consoleView.bot().styledText().getText();
        Pattern p = Pattern.compile(".*make info.*", Pattern.DOTALL);
        Matcher m = p.matcher(output);
        assertTrue(m.matches());

        // Make Targets using right-click on project.
        clickProjectContextMenu("Make Targets", "Build...");
        shell = bot.shell("Make Targets");
        shell.activate();
        bot.table().getTableItem("check").select();
        bot.button("Build").click();
        bot.sleep(3000);
        consoleView = bot.viewByPartName("Console");
        consoleView.setFocus();
        output = consoleView.bot().styledText().getText();
        p = Pattern.compile(".*make check.*Making check in src.*",
                Pattern.DOTALL);
        m = p.matcher(output);
        assertTrue(m.matches());
    }

}
