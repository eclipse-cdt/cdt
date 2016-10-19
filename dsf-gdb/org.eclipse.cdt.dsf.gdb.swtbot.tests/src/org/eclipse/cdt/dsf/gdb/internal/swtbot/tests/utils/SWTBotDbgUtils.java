package org.eclipse.cdt.dsf.gdb.internal.swtbot.tests.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuHelper;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.IWaitCondition;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IPageLayout;
import org.osgi.framework.Bundle;

public class SWTBotDbgUtils {
    private static final String ROOT_FOLDER = "stubs";
    /**
     * @param bot
     * @param stubFolderName
     * @param projectLocation
     * @param monitor
     * @return
     */
    public static void importStubProject(SWTWorkbenchBot bot, String stubFolderName, URI projectLocation, IProgressMonitor monitor) {
        Bundle bundle = Platform.getBundle(SWTBotDbgConstants.BUNDLE);

        URL url = null;
        try {
            url = FileLocator.resolve(bundle.getEntry(ROOT_FOLDER));
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("Unable to resolve stubs folder", false);
        }

        assertNotNull(url);
        String path = url.getPath();
        bot.menu("File").menu("Import...").click();

        bot.shell("Import").activate();

        bot.tree().getTreeItem("General").expand();
        bot.tree().getTreeItem("General").getNode("Existing Projects into Workspace").select();
        bot.button("Next >").click();
        bot.radio("Select root directory:").click();
        bot.comboBox().setText(path + stubFolderName);

        // Make sure we import a copy of the stub project into the work space
        SWTBotCheckBox copyProjectSel = bot.checkBox("Copy projects into workspace");
        if (!copyProjectSel.isChecked()) {
            copyProjectSel.select();
        }

        bot.button("Refresh").click();
        bot.button("Finish").click();
    }


    /**
     * @param bot
     * @param projName
     * @param monitor
     */
    public static void launchProject(SWTWorkbenchBot bot, String projName, IProgressMonitor monitor) {
        // select project
        SWTBotUtils.openView(SWTBotDbgConstants.PROJECT_EXPLORER_VIEW);
        bot.tree().getTreeItem(projName).select();

        String menuName = waitForMenuDebugAsLocal(bot);
        if (menuName != null) {
            bot.menu("Run").menu("Debug As").menu(menuName).click();
        }
    }

   /**
 * @param bot
 * @return
 */
public static String waitForMenuDebugAsLocal(SWTWorkbenchBot bot) {
       WaitMenuDebugCAppCondition debugAsCAppCond = new SWTBotDbgUtils().new WaitMenuDebugCAppCondition(bot);

       WaitUtils.waitUntil(debugAsCAppCond);
       String itemName = debugAsCAppCond.getMatchedItemName();
       if (itemName == null) {
           System.out.println("Error, unable to find Debug As Local C/C++ Application menu");
       }
       return itemName;
   }

   class WaitMenuDebugCAppCondition implements IWaitCondition {
       private SWTWorkbenchBot fBot;
       private String localCApp = "Local C/C++ Application";
       String fMatchedItem = null;

       WaitMenuDebugCAppCondition(SWTWorkbenchBot bot) {
           fBot = bot;
       }
       @Override
       public boolean test() throws Exception {
           // Launch a debugging session of the selected project
           List<String> menuitems = fBot.menu("Run").menu("Debug As").menuItems();
           for (String tItem: menuitems) {
               if (tItem.contains(localCApp)) {
                   fMatchedItem = tItem;
                   break;
               }
           }

           return (fMatchedItem != null);

       }

       public String getMatchedItemName() {
           return fMatchedItem;
       }

       @Override
       public String getFailureMessage() {
           WaitUtils.printJobs();
           return "Timed out waiting for menu : " + localCApp;
       }
   }


    /**
     * @param prjName
     * @return
     */
    public static IProject getProject(String prjName) {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();

        IProject project = root.getProject(prjName);
        assertNotNull(project);
        return project;
    }

    public static void buildAndlauchtProject(SWTWorkbenchBot bot, String prjName) {
//        bot.viewByTitle("Package Explorer").show();
        SWTBotUtils.openView(SWTBotDbgConstants.PROJECT_EXPLORER_VIEW);
        bot.tree().getTreeItem(prjName).select();

        // Buld project and wait for it
//        new SWTBotMenu(ContextMenuHelper.contextMenu(bot.tree(), "Build Project")).click();
//        WaitUtils.waitForJobs();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Launch Project
        new SWTBotMenu(ContextMenuHelper.contextMenu(bot.tree(), "Debug As", "1 Local C/C++ Application")).click();
//        WaitUtils.waitForJobs();

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        bot.button("OK").click();

//        bot.waitUntil(shellIsActive("Confirm Perspective Switch"), 120000);

//        SWTBotShell confirmPerspectiveShell = bot.shell("Confirm Perspective Switch");
//        confirmPerspectiveShell.activate();
    }


    /**
     * @param bot
     * @param projName
     * @param nullProgressMonitor
     */
    public static void stopProject(SWTWorkbenchBot bot, String projName, NullProgressMonitor nullProgressMonitor) {
        // select project
        System.out.println("entering stopProject, opening explorer view");
        SWTBotView explorerView = bot.viewById(SWTBotDbgConstants.PROJECT_EXPLORER_VIEW);
        explorerView.show();
        explorerView.setFocus();

//        SWTBotUtils.openView(SWTBotDbgConstants.PROJECT_EXPLORER_VIEW);
        System.out.println("starting project selection" + projName);
        bot.tree().getTreeItem(projName).select();

        // Launch a debugging session of the selected project
        System.out.println("triggering terminate, click");
        bot.menu("Run").menu("Terminate").click();
    }

    /**
     * @param main
     * @param version
     * @return
     */
    public static String getProgramPath(String main, String version) {
        // See bugzilla 303811 for why we have to append ".exe" on Windows
        boolean isWindows = runningOnWindows();
        String gdbPath = System.getProperty("cdt.tests.dsf.gdb.path");
        String fileExtension = isWindows ? ".exe" : "";
        String versionPostfix = (!version.equals("default")) ? "." + version : "";
        String debugName = main + versionPostfix + fileExtension;
        if (gdbPath != null) {
            debugName = gdbPath + "/" + debugName;
        }
        return debugName;
    }

    /**
     * @return
     */
    public static boolean runningOnWindows() {
        return Platform.getOS().equals(Platform.OS_WIN32);
    }

    /**
     * @param bot
     * @param monitor
     */
    public static void debugConfigGDB(SWTWorkbenchBot bot, IProgressMonitor monitor) {
        String gdbCmd = getProgramPath("gdb", "7.12");
        IEclipsePreferences node = DefaultScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
        node.put(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND, gdbCmd);
    }

    /**
     * @param jobName The name of the expected job
     */
    public static void waitForJob(String jobName) {
        WaitUtils.waitUntil(new IWaitCondition() {
            @Override
            public boolean test() throws Exception {
                Job[] jobs = Job.getJobManager().find(null);
                for (Job job : jobs) {
                    if (jobName.equals(job.getName())) {
                        return true;
                    }
                }

                return false;

            }

            @Override
            public String getFailureMessage() {
                WaitUtils.printJobs();
                return "Timed out waiting for job: " + jobName;
            }
        });
    }

    /**
     * @param jobName The name of the expected job
     */
    public static void waitForJobGone(String jobName) {
        WaitUtils.waitUntil(new IWaitCondition() {
            @Override
            public boolean test() throws Exception {
                System.out.println("entering waitforJobGone test method");
                Job[] jobs = Job.getJobManager().find(null);
                System.out.println("Got jobs array");
                if (jobs == null || jobs.length ==0) {
                    System.out.println("process console job is gone");
                    return true;
                }

                for (Job job : jobs) {
                    if (jobName.equals(job.getName())) {
                        System.out.println("process console job is still alive");
                        return false;
                    }
                }

                System.out.println("loop finished, process console job is gone");
                return true;
            }

            @Override
            public String getFailureMessage() {
                WaitUtils.printJobs();
                return "Timed out waiting for termination of job: " + jobName;
            }
        });
    }

    public static void deleteProject(final String projectName, boolean deleteResources, SWTWorkbenchBot bot) {
        // Wait for any analysis to complete because it might create
        // supplementary files
//        WaitUtils.waitForJobs();
        try {
            ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
        }

//        WaitUtils.waitForJobs();

        SWTBotUtils.closeSecondaryShells(bot);
//        WaitUtils.waitForJobs();

        final SWTBotView projectViewBot = bot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
        projectViewBot.setFocus();

        SWTBotTree treeBot = projectViewBot.bot().tree();
        SWTBotTreeItem treeItem = treeBot.getTreeItem(projectName);
        SWTBotMenu contextMenu = treeItem.contextMenu("Delete");
        contextMenu.click();

        if (deleteResources) {
            bot.shell("Delete Resources").setFocus();
            final SWTBotCheckBox checkBox = bot.checkBox();
            bot.waitUntil(Conditions.widgetIsEnabled(checkBox));
            checkBox.click();
        }

        final SWTBotButton okButton = bot.button("OK");
        bot.waitUntil(Conditions.widgetIsEnabled(okButton));
        okButton.click();

//        WaitUtils.waitForJobs();
    }

}
