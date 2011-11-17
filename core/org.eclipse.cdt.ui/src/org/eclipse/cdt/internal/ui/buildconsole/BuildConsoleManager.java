/*******************************************************************************
 * Copyright (c) 2002, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Red Hat Inc. - multiple build console support
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *     Alex Collins (Broadcom Corp.) - Global build console
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.osgi.service.prefs.Preferences;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleEvent;
import org.eclipse.cdt.ui.IBuildConsoleListener;
import org.eclipse.cdt.ui.IBuildConsoleManager;

import org.eclipse.cdt.internal.core.LocalProjectScope;

import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;

public class BuildConsoleManager implements IBuildConsoleManager, IResourceChangeListener, IPropertyChangeListener {
	/*package*/ static final String PREF_QUALIFIER = CUIPlugin.PLUGIN_ID;
	public static final String KEY_KEEP_LOG = "keepLog"; //$NON-NLS-1$
	public static final String KEY_LOG_LOCATION = "logLocation"; //$NON-NLS-1$
	public static final boolean CONSOLE_KEEP_LOG_DEFAULT = true;

	private static final String BUILD_CONSOLE_NODE = "buildConsole"; //$NON-NLS-1$
	private static final String PROJECT_LOG_EXT = ".build.log"; //$NON-NLS-1$

	private ListenerList listeners = new ListenerList();
	/** UI console object in which per-project consoles are shown */
	private BuildConsole fConsole;
	private Map<IProject, BuildConsolePartitioner> fConsoleMap = new HashMap<IProject, BuildConsolePartitioner>();
	private Color infoColor;
	private Color outputColor;
	private Color errorColor;
	private Color backgroundColor;
	private Color problemHighlightedColor;
	private Color problemErrorBackgroundColor;
	private Color problemInfoBackgroundColor;
	private Color problemWarningBackgroundColor;

	public Color getProblemHighlightedColor() {
		return problemHighlightedColor;
	}

	/**
	 * This function returns background color for errors only now
	 */
	public Color getProblemBackgroundColor() {
		return problemErrorBackgroundColor;
	}

	public Color getWarningBackgroundColor() {
		return problemWarningBackgroundColor;
	}

	public Color getInfoBackgroundColor() {
		return problemInfoBackgroundColor;
	}

	private BuildConsoleStreamDecorator infoStream;
	private BuildConsoleStreamDecorator outputStream;
	private BuildConsoleStreamDecorator errorStream;

	private String fName;
	private String fContextMenuId;

	static public final int BUILD_STREAM_TYPE_INFO = 0;
	static public final int BUILD_STREAM_TYPE_OUTPUT = 1;
	static public final int BUILD_STREAM_TYPE_ERROR = 2;

	static public final String DEFAULT_CONTEXT_MENU_ID = CUIPlugin.PLUGIN_ID + ".CDTBuildConsole"; //$NON-NLS-1$

	private IProject fLastProject;

	/**
	 * Default constructor.
	 */
	public BuildConsoleManager() {
	}

	/**
	 * Notifies the console manager that console activity has started on the
	 * project The manager will open the console if the preference is set to
	 * show the console, and notify listeners
	 */
	protected void startConsoleActivity(IProject project) {
		Object[] list = listeners.getListeners();
		if (list.length > 0) {
			for (Object element : list) {
				IBuildConsoleListener listener = (IBuildConsoleListener)element;
				ConsoleEvent event = new ConsoleEvent(BuildConsoleManager.this, project, IBuildConsoleEvent.CONSOLE_START);
				listener.consoleChange(event);
			}
		}
		showConsole();
	}

	/**
	 * Opens the console view. If the view is already open, it is brought to the
	 * front. The console that is shown is the console that was last on top.
	 */
	protected void showConsole() {
		IWorkbenchWindow window = CUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IViewPart consoleView = page.findView(IConsoleConstants.ID_CONSOLE_VIEW);
				if (consoleView == null && BuildConsolePreferencePage.isAutoOpenConsole()) {
					IWorkbenchPart activePart = page.getActivePart();
					try {
						consoleView = page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
					} catch (PartInitException pie) {
						CUIPlugin.log(pie);
					}
					//restore focus stolen by the creation of the
					// console
					page.activate(activePart);
				} else {
					boolean bringToTop = shouldBringToTop(consoleView);
					if (bringToTop) {
						page.bringToTop(consoleView);
					}
				}
				if (consoleView instanceof IConsoleView) {
					if (BuildConsole.getCurrentPage() == null)
						((IConsoleView)consoleView).display(fConsole);
					else
						((IConsoleView)consoleView).display(BuildConsole.getCurrentPage().getConsole());
				}
			}
		}
	}

	boolean shouldBringToTop(IViewPart consoleView) {
		boolean bringToTop = false;
		if (consoleView instanceof IConsoleView) {
			IConsoleView cView = (IConsoleView)consoleView;
			return !cView.isPinned() && BuildConsolePreferencePage.isConsoleOnTop();
		}
		return bringToTop;
	}

	/**
	 * Traverses the delta looking for added/removed/changed launch
	 * configuration files.
	 *
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResource resource = event.getResource();
		if (resource != null && resource.getType() == IResource.PROJECT) {
			if (event.getType() == IResourceChangeEvent.PRE_DELETE || event.getType() == IResourceChangeEvent.PRE_CLOSE) {
				IDocumentPartitioner partioner = fConsoleMap.remove(resource);
				if (partioner != null) {
					partioner.disconnect();
					Object[] list = listeners.getListeners();
					if (list.length > 0) {
						for (Object element : list) {
							IBuildConsoleListener listener = (IBuildConsoleListener)element;
							ConsoleEvent consoleEvent = new ConsoleEvent(this, (IProject)resource, IBuildConsoleEvent.CONSOLE_CLOSE);
							listener.consoleChange(consoleEvent);
						}
					}
				}
			}
		}
	}

	/**
	 * Release resources allocated on {@link #startup(String, String, URL)}.
	 */
	public void shutdown() {
		if (infoColor != null) {
			infoColor.dispose();
			outputColor.dispose();
			errorColor.dispose();
			backgroundColor.dispose();
			problemErrorBackgroundColor.dispose();
			problemWarningBackgroundColor.dispose();
			problemInfoBackgroundColor.dispose();
			problemHighlightedColor.dispose();
		}
		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new org.eclipse.ui.console.IConsole[]{fConsole});
		CUIPlugin.getWorkspace().removeResourceChangeListener(this);
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	private void runUI(Runnable run) {
		Display display;
		display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
			display.asyncExec(run);
		} else {
			run.run();
		}
	}

	/**
	 * Create new build console. Subclasses may override to create a specialized
	 * console.
	 *
	 * @param name - name of console to appear in the list of consoles in context menu
	 *    in the Console view.
	 * @param contextId - context menu id in the Console view.
	 * @param iconUrl - a {@link URL} of the icon for the context menu of the Console
	 *    view. The url is expected to point to an image in eclipse OSGi bundle.
	 *    {@code iconUrl} can be <b>null</b>, in that case the default image is used.
	 * @return newly created build console.
	 */
	protected BuildConsole createBuildConsole(String name, String contextId, final URL iconUrl) {
		return new BuildConsole(this, name, contextId, iconUrl);
	}

	/**
	 * Start console activities. This will create a new console in the Console view,
	 * create streams, color resources, register listeners etc.
	 * Most work is done in UI thread.
	 *
	 * Use {@link #shutdown()} after the console activity ends.
	 *
	 * @param name - name of the console to appear in the Console view.
	 * @param contextId - context menu id in the Console view.
	 * @param iconUrl - icon to show in the context menu.
	 */
	public void startup(String name, String contextId, final URL iconUrl) {
		// Ensure global console is initialized before any other build console
		if (!(this instanceof GlobalBuildConsoleManager))
			GlobalBuildConsoleManager.startup();

		infoStream = new BuildConsoleStreamDecorator();
		outputStream = new BuildConsoleStreamDecorator();
		errorStream = new BuildConsoleStreamDecorator();
		fName = name;
		fContextMenuId = contextId;

		runUI(new Runnable() {

			/*
			 * (non-Javadoc)
			 *
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				// add console to the Console view
				fConsole = createBuildConsole(fName, fContextMenuId, iconUrl);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new org.eclipse.ui.console.IConsole[]{fConsole});

				infoStream.setConsole(fConsole);
				infoColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_INFO_COLOR);
				infoStream.setColor(infoColor);
				outputStream.setConsole(fConsole);
				outputColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_OUTPUT_COLOR);
				outputStream.setColor(outputColor);
				errorStream.setConsole(fConsole);
				errorColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_ERROR_COLOR);
				errorStream.setColor(errorColor);
				backgroundColor = createBackgroundColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_BACKGROUND_COLOR);
				fConsole.setBackground(backgroundColor);
				problemHighlightedColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR);
				problemErrorBackgroundColor = createBackgroundColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR);
				problemWarningBackgroundColor = createBackgroundColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR);
				problemInfoBackgroundColor = createBackgroundColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR);
			}
		});
		CUIPlugin.getWorkspace().addResourceChangeListener(this);
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		// colors
		if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_INFO_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_INFO_COLOR);
			infoStream.setColor(newColor);
			infoColor.dispose();
			infoColor = newColor;
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_OUTPUT_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_OUTPUT_COLOR);
			outputStream.setColor(newColor);
			outputColor.dispose();
			outputColor = newColor;
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_ERROR_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_ERROR_COLOR);
			errorStream.setColor(newColor);
			errorColor.dispose();
			errorColor = newColor;
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_BACKGROUND_COLOR)) {
			Color newColor = createBackgroundColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_BACKGROUND_COLOR);
			fConsole.setBackground(newColor);
			backgroundColor.dispose();
			backgroundColor = newColor;
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR)) {
			Color newColor = createColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR);
			problemHighlightedColor.dispose();
			problemHighlightedColor = newColor;
			redrawTextViewer();
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR)) {
			Color newColor = createBackgroundColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR);
			problemErrorBackgroundColor.dispose();
			problemErrorBackgroundColor = newColor;
			redrawTextViewer();
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR)) {
			Color newColor = createBackgroundColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR);
			problemWarningBackgroundColor.dispose();
			problemWarningBackgroundColor = newColor;
			redrawTextViewer();
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR)) {
			Color newColor = createBackgroundColor(CUIPlugin.getStandardDisplay(), BuildConsolePreferencePage.PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR);
			problemInfoBackgroundColor.dispose();
			problemInfoBackgroundColor = newColor;
			redrawTextViewer();
		}
	}

	private void redrawTextViewer() {
		final BuildConsolePage p = BuildConsole.getCurrentPage();
		if ( p == null ) return;
		final BuildConsoleViewer v = p.getViewer();
		if ( v  == null ) return;
		Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				v.getTextWidget().redraw();
			}
		});
	}

	public BuildConsoleStreamDecorator getStreamDecorator(int type) throws CoreException {
		switch (type) {
			case BUILD_STREAM_TYPE_ERROR :
				return errorStream;
			case BUILD_STREAM_TYPE_INFO :
				return infoStream;
			case BUILD_STREAM_TYPE_OUTPUT :
				return outputStream;
		}
		throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, -1, "No Such Console", null)); //$NON-NLS-1$
	}

	/**
	 * Returns a color instance based on data from a preference field.
	 */
	private Color createColor(Display display, String preference) {
		RGB rgb = PreferenceConverter.getColor(CUIPlugin.getDefault().getPreferenceStore(), preference);
		return new Color(display, rgb);
	}

	/**
	 * Returns a background color instance based on data from a preference field.
	 * This is a workaround for black console bug 320723.
	 */
	private Color createBackgroundColor(Display display, String preference) {
		IPreferenceStore preferenceStore = CUIPlugin.getDefault().getPreferenceStore();
		RGB rgb;
		if (preferenceStore.contains(preference)) {
			rgb = PreferenceConverter.getColor(preferenceStore, preference);
		} else {
			rgb = new RGB(200, 200, 200); // gray background
		}
		return new Color(display, rgb);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IConsole getConsole(IProject project) {
		return new MultiBuildConsoleAdapter(getProjectConsole(project), GlobalBuildConsoleManager.getGlobalConsole());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the console for the specified project. Returns {@code null}
	 *   if project is {@code null} or not accessible.
	 */
	@Override
	public IConsole getProjectConsole(IProject project) {
		if (project==null || !project.isAccessible())
			return null;

		fLastProject = project;
		return getProjectConsolePartioner(project).getConsole();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.ui.IBuildConsoleManager#getLastBuiltProject()
	 */
	@Override
	public IProject getLastBuiltProject() {
		return fLastProject;
	}

	/**
	 * @return the partitioner for the specified projects build console
	 */
	private BuildConsolePartitioner getProjectConsolePartioner(IProject project) {
		BuildConsolePartitioner partitioner = fConsoleMap.get(project);
		if (partitioner == null) {
			partitioner = new BuildConsolePartitioner(project, this);
			fConsoleMap.put(project, partitioner);
		}
		return partitioner;
	}

	/**
	 * @return the document backing the build console for the specified project
	 */
	@Override
	public IDocument getConsoleDocument(IProject project) {
		Assert.isNotNull(project);
		return getProjectConsolePartioner(project).getDocument();
	}

	@Override
	public void addConsoleListener(IBuildConsoleListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeConsoleListener(IBuildConsoleListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @return logging preferences for a given project.
	 * @param project to get logging preferences for, can't be {@code null}.
	 */
	public Preferences getBuildLogPreferences(IProject project) {
		return new LocalProjectScope(project).getNode(PREF_QUALIFIER).node(BUILD_CONSOLE_NODE);
	}

	/**
	 * @return default location of logs for a project.
	 * @param project to get default log location for, can't be {@code null}.
	 */
	public String getDefaultConsoleLogLocation(IProject project) {
		IPath defaultLogLocation = CUIPlugin.getDefault().getStateLocation().append(project.getName() + PROJECT_LOG_EXT);
		return defaultLogLocation.toOSString();
	}

	/**
	 * Where the log for per-project console is kept.
	 *
	 * @param project - the project. Cannot be {@code null}.
	 * @return {@link URI} of build log or {@code null} if not available.
	 */
	public URI getLogURI(IProject project) {
		Assert.isNotNull(project);

		if (fContextMenuId!=DEFAULT_CONTEXT_MENU_ID)
			return null;

		URI logURI = null;

		Preferences prefs = getBuildLogPreferences(project);
		boolean keepLog = prefs.getBoolean(KEY_KEEP_LOG, CONSOLE_KEEP_LOG_DEFAULT);
		if (keepLog) {
			String strLocation;
			strLocation = prefs.get(KEY_LOG_LOCATION, getDefaultConsoleLogLocation(project));
			if (strLocation.trim().length()>0) {
				logURI = URIUtil.toURI(strLocation);
			}
			if (logURI==null) {
				IStatus status= new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,"Can't determine URI for location=["+strLocation+"]");  //$NON-NLS-1$ //$NON-NLS-2$
				CUIPlugin.log(status);
			}
		}
		return logURI;
	}

}
