/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Marc-Andre Laperle - Partially copied to CDT from Trace Compass
 *******************************************************************************/


package org.eclipse.cdt.debug.application.tests;

import static org.junit.Assert.fail;

import java.util.TimeZone;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * SWTBot Helper functions
 *
 * @author Matthew Khouzam
 */
public final class SWTBotUtils {

    private static boolean fPrintedEnvironment = false;

    private SWTBotUtils() {
    }

    /**
     * Sleeps current thread for a given time.
     *
     * @param waitTimeMillis
     *            time in milliseconds to wait
     */
    public static void delay(final long waitTimeMillis) {
        try {
            Thread.sleep(waitTimeMillis);
        } catch (final InterruptedException e) {
            // Ignored
        }
    }

    /**
     * Focus on the main window
     *
     * @param shellBots
     *            swtbotshells for all the shells
     */
    public static void focusMainWindow(SWTBotShell[] shellBots) {
        for (SWTBotShell shellBot : shellBots) {
            if (shellBot.getText().toLowerCase().contains("eclipse")) {
                shellBot.activate();
            }
        }
    }
    /**
     * Initialize the environment for SWTBot
     */
    public static void initialize() {
        failIfUIThread();

        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        UIThreadRunnable.syncExec(() -> {
            printEnvironment();

            // There seems to be problems on some system where the main shell is
            // not in focus initially. This was seen using Xvfb and Xephyr on some occasions.
            focusMainWindow(bot.shells());

            Shell shell = bot.activeShell().widget;

            // Only adjust shell if it appears to be the top-most
            if (shell.getParent() == null) {
                makeShellFullyVisible(shell);
            }
        });
    }

    private static void printEnvironment() {
        if (fPrintedEnvironment) {
            return;
        }

        // Print some information about the environment that could affect test outcome
        Rectangle bounds = Display.getDefault().getBounds();
        System.out.println("Display size: " + bounds.width + "x" + bounds.height);

        String osVersion = System.getProperty("os.version");
        if (osVersion != null) {
            System.out.println("OS version=" + osVersion);
        }
        String gtkVersion = System.getProperty("org.eclipse.swt.internal.gtk.version");
        if (gtkVersion != null) {
            System.out.println("GTK version=" + gtkVersion);
            String overlayScrollbar = System.getenv("LIBOVERLAY_SCROLLBAR");
            if (overlayScrollbar != null) {
                System.out.println("LIBOVERLAY_SCROLLBAR=" + overlayScrollbar);
            }
            String ubuntuMenuProxy = System.getenv("UBUNTU_MENUPROXY");
            if (ubuntuMenuProxy != null) {
                System.out.println("UBUNTU_MENUPROXY=" + ubuntuMenuProxy);
            }
        }

        System.out.println("Time zone: " + TimeZone.getDefault().getDisplayName());

        fPrintedEnvironment = true;
    }

    /**
     * If the test is running in the UI thread then fail
     */
    private static void failIfUIThread() {
        if (Display.getCurrent() != null && Display.getCurrent().getThread() == Thread.currentThread()) {
            fail("SWTBot test needs to run in a non-UI thread. Make sure that \"Run in UI thread\" is unchecked in your launch configuration or"
                    + " that useUIThread is set to false in the pom.xml");
        }
    }

    /**
     * Try to make the shell fully visible in the display. If the shell cannot
     * fit the display, it will be positioned so that top-left corner is at
     * <code>(0, 0)</code> in display-relative coordinates.
     *
     * @param shell
     *            the shell to make fully visible
     */
    private static void makeShellFullyVisible(Shell shell) {
        Rectangle displayBounds = shell.getDisplay().getBounds();
        Point absCoord = shell.toDisplay(0, 0);
        Point shellSize = shell.getSize();

        Point newLocation = new Point(absCoord.x, absCoord.y);
        newLocation.x = Math.max(0, Math.min(absCoord.x, displayBounds.width - shellSize.x));
        newLocation.y = Math.max(0, Math.min(absCoord.y, displayBounds.height - shellSize.y));
        if (!newLocation.equals(absCoord)) {
            shell.setLocation(newLocation);
        }
    }
}
