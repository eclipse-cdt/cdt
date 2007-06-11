/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David Dykstal (IBM) - [191130] log exception instead of printing, do not log if project is not available
 ********************************************************************************/

package org.eclipse.rse.internal.ui;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rse.core.ISystemResourceListener;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.core.RemoteSystemsProject;
import org.eclipse.rse.internal.core.model.SystemModelChangeEvent;
import org.eclipse.rse.internal.ui.view.SystemPerspectiveHelpers;
import org.eclipse.rse.internal.ui.view.SystemPerspectiveLayout;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.internal.ui.view.SystemViewPart;
import org.eclipse.rse.internal.ui.view.team.SystemTeamViewPart;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


/**
 * This class manages listening for resource changes within our project.
 * Normally, we do not care about such changes. However, after a team synchronize,
 * we do.
 */
public class SystemResourceListener implements ISystemResourceListener, Runnable
{
    private boolean debug = true;
    private boolean fullDebug = debug && true;
    private static SystemResourceListener inst = null;
    private IProject remoteSystemsProject = null;
    private boolean ignoreEvents = true;
    private boolean ensureEventsOnMode = false;
    private boolean sawSyncEvent = false;
    private boolean changesPending = false;
    private int addRmvListenerCount;
    private Vector listeners;
    private IWorkbenchPage primaryRSEPerspective = null;
    private IViewPart[] primaryRSEViews = null;

    private int runAction = 0;
    private static final int CLOSE_PERSPECTIVES = 1;
    private static final int CLOSE_EDITORS = 2;
    private static final int CLOSE_VIEWS = 3;
    private static final int RSE_RESTART = 4;
    private static final int OPEN_VIEWS = 5;
    private static final int FIRE_EVENT = 99;
    
    SystemResourceListener(IProject remoteSystemsProject)
    {
        this.remoteSystemsProject = remoteSystemsProject;
    }

    /**
     * Return singleton
     */
    public static SystemResourceListener getListener(IProject remoteSystemsProject)
    {
        if (inst == null)
            inst = new SystemResourceListener(remoteSystemsProject);
        return inst;
    }

    /**
     * Register a listener for resource change events on objects in our remote system project.
     * No attempt is made to filter the events, they are simply passed on and the listener can
     * decide if the event applies to them or not.
     * <p>
     * However, the event will only be fired if a change is made to that resource outside of the
     * normal activities of the Remote Systems Framework, and only for resources within the
     * Remote Systems Connection project.
     */
    public void addResourceChangeListener(IResourceChangeListener l)
    {
    	if (listeners == null)
    	{
    		listeners = new Vector();
    		listeners.add(l);
    	}
        else if (!listeners.contains(l))
            listeners.add(l);
    }
    /**
     * Remove a listener for resource change events on object in our remote system project.
     */
    public void removeResourceChangeListener(IResourceChangeListener l)
    {
        if ((listeners!=null) && listeners.contains(l))
            listeners.remove(l);
    }

    /**
     * Turn off event listening. Please call this before do anything that modifies resources and
     * turn it on again after.
     */
    public void turnOffResourceEventListening()
    {
        // Always turn off, even though may already be off, if nested block

        ignoreEvents = true;

        //remoteSystemsProject.getWorkspace().removeResourceChangeListener(listener);

        --addRmvListenerCount;

        // May have nested blocks of turnOff/ turnOn calls,
        // so don't want to issue this warning.

        //if (addRmvListenerCount != 0)

        // Safeguard:  Dont expect many levels of nesting.  Diagnose.

        //  1 nested level for now??

        if (addRmvListenerCount < -1)
            SystemBasePlugin.logWarning("LISTENER TURNED OFF OUT-OF-SEQUENCE ERROR: " + addRmvListenerCount); //$NON-NLS-1$
    }

    /**
     * Turn off event listening. Please call this after modifying resources.
     */
    public void turnOnResourceEventListening()
    {

        // May have nested blocks of turnOff/ turnOn calls.
        // Normally, expect (addRmvListenerCount) to equal 1 before it is turned Off,
        // then equal 0 when off.  But may go below zero if nested off/on's occur.

        ++addRmvListenerCount;

        if (addRmvListenerCount > 0)
        {

            ignoreEvents = false;
            sawSyncEvent = false;
            //remoteSystemsProject.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.POST_AUTO_BUILD);    	

            if (addRmvListenerCount != 1)
                SystemBasePlugin.logWarning("LISTENER TURNED ON OUT-OF-SEQUENCE ERROR: " + addRmvListenerCount); //$NON-NLS-1$
        }

    }

    /**
     * Ensure event listening is on. Called at start of team synch action to be safe.
     */
    public void ensureOnResourceEventListening()
    {
        SystemBasePlugin.logInfo("INSIDE ENSUREONRESOURCEEVENTLISTENING"); //$NON-NLS-1$
        if (ignoreEvents)
        {
            SystemBasePlugin.logWarning("RESOURCE LISTENER WAS TURNED OFF. IT WAS FORCED ON"); //$NON-NLS-1$
            turnOnResourceEventListening();
        }
        ensureEventsOnMode = true; // we can add debug statements conditioned by this so we don't stop until team synch action is run
    }

    /**
     * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event)
    {
        Object source = event.getSource();
        if (ignoreEvents || !(event.getSource() instanceof IWorkspace))
        {
            return;
        }

        if (fullDebug)
        {
            int type = event.getType();
            String sType = getTypeString(type);
            SystemBasePlugin.logInfo("RESOURCE CHANGED EVENT: eventType=" + sType + ", eventSource=" + event.getSource()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (ensureEventsOnMode)
        {
            ensureEventsOnMode = true; // set breakpoint here
        }

        IResource resource = event.getResource();
        IResourceDelta delta = event.getDelta();
        if ((resource == null) && (delta != null))
            resource = delta.getResource();

        // ATTEMPT TO FILTER OUT UNWANTED EVENTS...
        if (resource != null)
        {
            if (resource.getProject() != null)
            {
                if (!resource.getProject().getName().equals(remoteSystemsProject.getName()))
                {
                    if (fullDebug)
                        SystemBasePlugin.logInfo("EVENT FILTERED OUT BY PROJECT NAME"); //$NON-NLS-1$
                    return;
                }
            }
            else if (resource.getType() == IResource.PROJECT)
            {
                if (!resource.getName().equals(remoteSystemsProject.getName()))
                {
                    if (fullDebug)
                        SystemBasePlugin.logInfo("EVENT FILTERED OUT BY PROJECT NAME"); //$NON-NLS-1$
                    return;
                }
                try
                {
                    if (!(((IProject) resource).hasNature(RemoteSystemsProject.ID)))
                    {
                        if (fullDebug)
                            SystemBasePlugin.logInfo("EVENT FILTERED OUT BY PROJECT NATURE"); //$NON-NLS-1$
                        return;
                    }
                }
                catch (Exception exc)
                {
                }
            }
            //else
            //  RSEUIPlugin.logWarning("IN RESOURCeListener, RESOURCE AND PROJECT ARE NULL, UNABLE TO FILTER THE EVENT");
        }
        else
        {
            //if (resource == null)
            //  RSEUIPlugin.logWarning("IN RESOURCeListener, RESOURCE IS NULL, UNABLE TO FILTER THE EVENT");
        }

        if (delta != null)
        {
            if (preScanForIgnore(delta))
            {
                if (fullDebug)
                    SystemBasePlugin.logInfo("EVENT FILTERED OUT IN PRESCAN"); //$NON-NLS-1$
                return;
            }
        }

        if (fullDebug)
        {
            SystemBasePlugin.logInfo("*** RESOURCE CHANGE EVENT ALLOWED IN ***"); //$NON-NLS-1$
        	//(new Exception("Stack Trace")).fillInStackTrace().printStackTrace();
        }

        /*
        Exception exc = new Exception("Stack Trace");
        exc.fillInStackTrace();
        exc.printStackTrace();
         */

        if (debug)
        {
            int type = event.getType();
            String sType = getTypeString(type);
            if (!fullDebug)
                SystemBasePlugin.logInfo("RESOURCE CHANGED EVENT: eventType=" + sType + ", eventSource=" + source); //$NON-NLS-1$ //$NON-NLS-2$
            //RSEUIPlugin.logInfo("RESOURCE DELTA:"); //$NON-NLS-1$
        }
        boolean ignored = false;
        if (delta != null)
        {
            //previousResource = null;
            //ignored = !processDelta("", delta); //$NON-NLS-1$		

            // OK, LET'S DO IT!
            if (sawSyncEvent)
            {
                sawSyncEvent = false;
                // code to reload moved to reloadRSE() method. Must be called explicitly now!
            }
            changesPending = true;
        }
        if (!ignored)
        {
        	fireResourceChangeEvent(event);
        }
    }
    
    /**
     * Fire resource change events to interested listeners.
     * This is simply a propogation of the eclipse resource event, once we know it applies to us.
     */
    protected void fireResourceChangeEvent(IResourceChangeEvent event)
    {
         if (listeners == null)
           return;
         // inform all listeners...
         for (int idx = 0; idx < listeners.size(); idx++)
         {
                IResourceChangeListener l = (IResourceChangeListener) listeners.elementAt(idx);
                l.resourceChanged(event);
         }
    }

    /**
     * Prescan for unrelated events.
     * @return true if event filtered out
     */
    protected boolean preScanForIgnore(IResourceDelta delta)
    {
        if (delta == null)
            return true; // not sure when we'd get this
        IResourceDelta[] subdeltas = delta.getAffectedChildren();
        if (subdeltas.length > 0)
        {
            IResource resource = subdeltas[0].getResource();
            int resType = 0;
            if (resource != null)
              resType = resource.getType();
            int flags = subdeltas[0].getFlags();
            if (debug)
            {
              if (debug)
                SystemBasePlugin.logInfo("...In preScanForIgnore. Kind = "+getKindString(delta.getKind())); //$NON-NLS-1$
              if (resource == null)
                SystemBasePlugin.logInfo("......resource is null"); //$NON-NLS-1$
              else
                SystemBasePlugin.logInfo("......resource is: "+resource.getName() + ", type is: " + getResourceTypeString(resType)); //$NON-NLS-1$ //$NON-NLS-2$
              if (flags == IResourceDelta.SYNC)
                SystemBasePlugin.logInfo("......flags == SYNC"); //$NON-NLS-1$
              else
                SystemBasePlugin.logInfo("......flags == "+flags); //$NON-NLS-1$
            }

            if (flags == IResourceDelta.SYNC) // apparently we no longer get this in 2.0!
            {
                sawSyncEvent = true;
                return true;
            }
            if ((resource !=null) && (resType == IResource.ROOT))
              return true; // someone created a new project?
            if ((resource != null) && (resType == IResource.PROJECT))
            {
            	if (!resource.getName().equals(remoteSystemsProject.getName()))
            	  return true;
                // [191130] the event can be ignored if the project cannot be accessed
            	if (!resource.isAccessible()) return true;
                try
                {
                    if (!(((IProject) resource).hasNature(RemoteSystemsProject.ID)))
                        return true;
					}
                catch (CoreException exc)
                {
                	RSECorePlugin.getDefault().getLogger().logError("Exception trying to test the natures of the project that fired a resource change event", exc); //$NON-NLS-1$
				}
			}
        }
        return false;
    }
    
    private String getResourceTypeString(int type)
    {
    	switch (type)
    	{
    		case IResource.ROOT : return "root"; //$NON-NLS-1$
    		case IResource.PROJECT : return "project"; //$NON-NLS-1$
    		case IResource.FOLDER : return "folder"; //$NON-NLS-1$
    		case IResource.FILE : return "file"; //$NON-NLS-1$
    	}
    	return "unknown: "+Integer.toString(type); //$NON-NLS-1$
    }

    /**
     * Process all deltas.
     * Currently not used.
     * WILL FLESH OUT IN A FUTURE RELEASE AND TRY TO AVOID THE ATOM BOMB APPROACH WE TAKE NOW
     */
    protected boolean processDelta(String indent, IResourceDelta delta)
    {
        int kind = delta.getKind();
        int flags = delta.getFlags();
        if ((kind == IResourceDelta.CHANGED) && (flags == IResourceDelta.SYNC))
            return false;
        boolean processKids = true;
        IResource resource = delta.getResource();
        String kindString = null;
        String pre = null;
        if (debug)
        {
            kindString = getKindString(kind);
            pre = kindString + ": " + indent; //$NON-NLS-1$
            SystemBasePlugin.logInfo(pre + delta + ": flags: " + getKindString(flags)); //$NON-NLS-1$
        }
        if (resource == null)
            return true;
        int resourceType = resource.getType();
        switch (kind)
        {
            case IResourceDelta.ADDED :
                break;
            case IResourceDelta.CHANGED :
                if (debug)
                    SystemBasePlugin.logInfo(pre + "resource type: " + resourceType); //$NON-NLS-1$
                if (resourceType == IResource.PROJECT)
                {
                    try
                    {
                        if (!(((IProject) resource).hasNature(RemoteSystemsProject.ID)))
                        {
                            if (debug)
                                SystemBasePlugin.logInfo("EVENT DELTA FILTERED OUT BY PROJECT NATURE"); //$NON-NLS-1$
                            return false;
                        }
                    }
                    catch (Exception exc)
                    {
                    }
                }
                break;
            case IResourceDelta.CONTENT :
            case IResourceDelta.REPLACED :
                break;
            case IResourceDelta.REMOVED :
                break;
            default :
                if (debug)
                    SystemBasePlugin.logInfo(kindString + " DELTA IGNORED"); //$NON-NLS-1$
        }
        boolean stop = false;
        if (processKids)
        {
            IResourceDelta[] subdeltas = delta.getAffectedChildren();
            for (int i = 0; !stop && (i < subdeltas.length); i++)
                stop = !processDelta(indent + "   ", subdeltas[i]); //$NON-NLS-1$
        }
        return !stop;
    }

    public static String getKindString(int kind)
    {
        String kindString = "Unknown: " + Integer.toString(kind); //$NON-NLS-1$
        switch (kind)
        {
            case IResourceDelta.ADDED :
                kindString = "ADDED"; //$NON-NLS-1$
                break;
            case IResourceDelta.ADDED_PHANTOM :
                kindString = "ADDED_PHANTOM"; //$NON-NLS-1$
                break;
            case IResourceDelta.ALL_WITH_PHANTOMS :
                kindString = "ALL_WITH_PHANTOMS"; //$NON-NLS-1$
                break;
            case IResourceDelta.CHANGED :
                kindString = "CHANGED"; //$NON-NLS-1$
                break;
            case IResourceDelta.CONTENT :
                kindString = "CONTENT"; //$NON-NLS-1$
                break;
            case IResourceDelta.DESCRIPTION :
                kindString = "DESCRIPTION"; //$NON-NLS-1$
                break;
            case IResourceDelta.MARKERS :
                kindString = "ADDED"; //$NON-NLS-1$
                break;
            case IResourceDelta.MOVED_FROM :
                kindString = "MOVED_FROM"; //$NON-NLS-1$
                break;
            case IResourceDelta.MOVED_TO :
                kindString = "MOVED_TO"; //$NON-NLS-1$
                break;
            case IResourceDelta.NO_CHANGE :
                kindString = "NO_CHANGE"; //$NON-NLS-1$
                break;
            case IResourceDelta.OPEN :
                kindString = "OPEN"; //$NON-NLS-1$
                break;
            case IResourceDelta.REMOVED :
                kindString = "REMOVED"; //$NON-NLS-1$
                break;
            case IResourceDelta.REMOVED_PHANTOM :
                kindString = "REMOVED_PHANTOM"; //$NON-NLS-1$
                break;
            case IResourceDelta.REPLACED :
                kindString = "REPLACED"; //$NON-NLS-1$
                break;
            case IResourceDelta.SYNC :
                kindString = "SYNC"; //$NON-NLS-1$
                break;
            case IResourceDelta.TYPE :
                kindString = "TYPE"; //$NON-NLS-1$
                break;
        }
        return kindString;
    }

    public static String getTypeString(int type)
    {
        String typeString = "Unknown: " + Integer.toString(type); //$NON-NLS-1$
        switch (type)
        {
            case IResourceChangeEvent.POST_CHANGE :
                typeString = "POST_CHANGE"; //$NON-NLS-1$
                break;
            case IResourceChangeEvent.POST_BUILD:
                typeString = "POST_BUILD"; //$NON-NLS-1$
                break;
            case IResourceChangeEvent.PRE_DELETE :
                typeString = "PRE_DELETE"; //$NON-NLS-1$
                break;
            case IResourceChangeEvent.PRE_CLOSE :
                typeString = "PRE_CLOSE"; //$NON-NLS-1$
                break;
            case IResourceChangeEvent.PRE_BUILD :
                typeString = "PRE_BUILD"; //$NON-NLS-1$
                break;
        }
        return typeString;
    }

    /**
     * Close all non-primary system view perspectives
     */
    private void closeRSEPerspectives()
    {
        primaryRSEPerspective = null;
        primaryRSEViews = null;
        IWorkbenchPage[] rsePages = getRSEPerspectives();
        IWorkbench wb = PlatformUI.getWorkbench();
        if (wb == null)
            SystemBasePlugin.logInfo("PlatformUI.getWorkbench() returned null!!"); //$NON-NLS-1$
        else
        {
            IWorkbenchWindow wbw = wb.getActiveWorkbenchWindow();
            if (wbw == null)
                SystemBasePlugin.logInfo("Active workbench window is null"); //$NON-NLS-1$
            if ((primaryRSEPerspective != null) && (wbw != null) && (wbw.getActivePage() != primaryRSEPerspective))
                wbw.setActivePage(primaryRSEPerspective);
        }
        // close non-primary rse perspectives...
        if ((rsePages != null) && (rsePages.length > 0))
        {
            for (int idx = 0; idx < rsePages.length; idx++)
            {
                IWorkbenchPage rsePage = rsePages[idx];
                rsePage.close();
            }
        }
        // for primary rse perspective...
        if (primaryRSEPerspective != null)
        {
            Vector v = new Vector();
            IViewReference[] views = primaryRSEPerspective.getViewReferences();
            // scan for, and record, all non SystemView views
            if (views != null)
            {
                for (int idx = 0; idx < views.length; idx++)
                    if (!(views[idx].getView(false) instanceof SystemViewPart) &&
                        !(views[idx].getView(false) instanceof SystemTeamViewPart))
                        v.addElement(views[idx].getView(false));
            }
            primaryRSEViews = new IViewPart[v.size()];
            for (int idx = 0; idx < v.size(); idx++)
                primaryRSEViews[idx] = (IViewPart) v.elementAt(idx);
            // ok, now we have those secondary views... what to do with them??
        }
    }

    /**
     * Return primary RSE View
     */
    private SystemView getRSEView()
    {
    	return SystemPerspectiveHelpers.findRSEView();
    	/*
        SystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow[] wbWindows = wb.getWorkbenchWindows();
        for (int idx = 0; idx < wbWindows.length; idx++)
        {
            IWorkbenchPage[] pages = wbWindows[idx].getPages();
            if (pages != null)
                for (int jdx = 0; jdx < pages.length; jdx++)
                    if ((pages[jdx].getPerspective().getId().equals(SystemPerspectiveLayout.ID)))
                    {
                        SystemView sv = getSystemView(pages[jdx]);
                        if ((sv != null) && (sv.getInput() == sr))
                            return sv;
                    }
        }
        return null;
        */
    }

    /**
     * Find all Remote System Explorer perspectives
     * @return an array of all non-primary RSE perspectives, plus primaryRSEPerpsective set for the primary one.
     */
    private IWorkbenchPage[] getRSEPerspectives()
    {
        ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow[] wbWindows = wb.getWorkbenchWindows();
        Vector v = new Vector();
        for (int idx = 0; idx < wbWindows.length; idx++)
        {
            IWorkbenchPage[] pages = wbWindows[idx].getPages();
            if (pages != null)
                for (int jdx = 0; jdx < pages.length; jdx++)
                    if ((pages[jdx].getPerspective().getId().equals(SystemPerspectiveLayout.ID)))
                    {
                        SystemView sv = getSystemView(pages[jdx]);
                        if ((sv != null) && !(sv.getInput() == sr)) // not the primary perspective
                            v.addElement(pages[jdx]);
                        else if (sv != null)
                            primaryRSEPerspective = pages[jdx];
                    }
        }
        IWorkbenchPage[] ourPages = new IWorkbenchPage[v.size()];
        for (int idx = 0; idx < ourPages.length; idx++)
            ourPages[idx] = (IWorkbenchPage) v.elementAt(idx);
        return ourPages;
    }

    /**
     * Get RSE view for a given perspective page
     */
    private SystemView getSystemView(IWorkbenchPage page)
    {
        IViewReference[] views = page.getViewReferences();
        if (views != null)
            for (int idx = 0; idx < views.length; idx++)
                if (views[idx].getView(false) instanceof SystemViewPart)
                    return ((SystemViewPart) views[idx].getView(false)).getSystemView();
        return null;
    }

    /**
     * IRunnable run method
     */
    public void run()
    {
        if (runAction == CLOSE_PERSPECTIVES)
        {
            closeRSEPerspectives();
        }
        else if (runAction == CLOSE_EDITORS)
        {
            closeEditors();
        }
        else if (runAction == CLOSE_VIEWS)
        {
            closeViews();
        }
        else if (runAction == RSE_RESTART)
        {
            RSEUIPlugin.getDefault().restart();
        }
        else if (runAction == OPEN_VIEWS)
        {
            openViews();
        }
        else if (runAction == FIRE_EVENT)
        {
            RSECorePlugin.getTheSystemRegistry().fireEvent(
              new SystemModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ALL_RELOADED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_ALL, "dummy")); //$NON-NLS-1$
        }

    }

    /**
     * Close active editors in RSE views. You may prompt the user to save or discard pending changes, but
     *  you cannot cancel the operation... the editor must be closed one way or the other.
     */
    protected void closeEditors()
    {
        // todo
    }

    /**
     * Close active view in RSE perspective. You may either close the view or set its input to null, whichever is
     *  appropriate. This is called prior to refreshing from disk. If you close the view, sets its array entry to null.
     */
    protected void closeViews()
    {
        // close views in primary RSE perspective
        if (primaryRSEViews != null)
        {
            for (int idx = 0; idx < primaryRSEViews.length; idx++)
            {
            }
        }
    }
    /**
     * Open views in RSE perspective. You may either close the view or set its input to null, whichever is
     *  appropriate. This is called prior to refreshing from disk.
     */
    protected void openViews()
    {
        // re-open views in primary RSE perspective
        if (primaryRSEViews != null)
        {
            for (int idx = 0; idx < primaryRSEViews.length; idx++)
            {
                if (primaryRSEViews[idx] != null)
                {
                }
            }
        }

    }
    
    /**
     * Return true if changes are pending and hence a reloadRSE is in order
     */
    public static boolean changesPending()
    {
    	 SystemResourceListener us = null;
    	 if (inst == null)
    	    us = getListener(SystemResourceManager.getRemoteSystemsProject(false));
    	 else
    	    us = inst;

    	 return us.changesPending;
    }
    
    /**
     * Re-load the whole RSE from the workspace. This is to be called after a team synchronization.
     */
    public static void reloadRSE()
    {
    	 SystemResourceListener us = null;
    	 if (inst == null)
    	    us = getListener(SystemResourceManager.getRemoteSystemsProject(false));
    	 else
    	    us = inst;
    	    
         Display d = Display.getCurrent();
         if (d == null)
           d = Display.getDefault();
         if (d == null)
         {
           SystemBasePlugin.logInfo("Hmm, can't get the display"); //$NON-NLS-1$
           SystemView sv = us.getRSEView();
           if (sv != null)
             d = sv.getShell().getDisplay();
           else
             SystemBasePlugin.logInfo("Hmm, really can't get the display"); //$NON-NLS-1$
         }
         
         // here is the idea:
         //  0. Close all open editors. Currently a no-op
         //  1. Close all non-primary RSE perspectives
         //     -- For the primary (last one found with SystemRegistry as the input) RSE perspective, leave it open
         //  2. Close all non-primary views from the primary RSE perspective. Currently, a no-op
         //  3. Reload the model.
         //  4. Re-open non-primary views in primary RSE perspective. Currently a no-op
         //  5. Give model change listeners (eg, views) an opportunity to re-load themselves if they need to

         us.runAction = CLOSE_EDITORS;
         if (d != null)
           d.syncExec(us);
         else
           us.run();

         us.runAction = CLOSE_PERSPECTIVES;
         if (d != null)
           d.syncExec(us);
         else
           us.run();

         us.runAction = CLOSE_VIEWS;
         if (d != null)
           d.syncExec(us);
         else
           us.run();

         us.runAction = RSE_RESTART;
         if (d != null)
           d.syncExec(us);
         else
           us.run();

         us.runAction = OPEN_VIEWS;
         if (d != null)
           d.syncExec(us);
         else
           us.run();
           
         us.changesPending = false;
         
         us.runAction = FIRE_EVENT;         
         if (d != null)
           d.syncExec(us);
         else
           us.run();
    }

}