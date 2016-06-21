/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSwitcher;

/**
 * A GDB Console view.  This view shows the full GDB console (GdbCliConsole) for each debug session
 * that supports such a console.  This is only supported with GDB >= 7.12 and if 
 * IGDBBackend.isFullGdbConsoleSupported() returns true.
 * For cases where the GdbCliConsole is not supported, a standard console is shown in the 
 * platform's standard Console view.
 */
public class GdbConsoleView extends PageBookView implements IConsoleListener, IDebugContextListener, IPropertyChangeListener {

	public static final String GDB_CONSOLE_VIEW_ID = "org.eclipse.cdt.dsf.gdb.ui.console.gdbconsoleview"; //$NON-NLS-1$
	
	/** The console being displayed, or <code>null</code> if none */
	private IConsole fActiveConsole;

	/** Map of consoles to dummy console parts (used to close pages) */
	private Map<IConsole, GdbConsoleWorkbenchPart> fConsoleToPart = new HashMap<>();

	/** Map of parts to consoles */
	private Map<GdbConsoleWorkbenchPart, IConsole> fPartToConsole = new HashMap<>();

	private GdbConsoleDropDownAction fDisplayConsoleAction;

	public GdbConsoleView() {
		super();
	}

	@Override
	public void dispose() {
		super.dispose();
		getGdbConsoleManager().removeConsoleListener(this);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).removeDebugContextListener(this);

		if (fDisplayConsoleAction != null) {
			fDisplayConsoleAction.dispose();
			fDisplayConsoleAction = null;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// This is important to update the title of a console when it terminates
		Object source = event.getSource();
		if (source instanceof IConsole && event.getProperty().equals(IBasicPropertyConstants.P_TEXT)) {
			if (source.equals(getCurrentConsole())) {
				updateTitle();
			}
		}

	}

	private boolean isAvailable() {
		return getPageBook() != null && !getPageBook().isDisposed();
	}

	/**
	 * Returns the currently displayed console.
	 * 
	 * @return the currently displayed console
	 */
	public IConsole getCurrentConsole() {
		return fActiveConsole;
	}

	@Override
	protected void showPageRec(PageRec pageRec) {
		IConsole recConsole = fPartToConsole.get(pageRec.part);
		if (recConsole != null && recConsole.equals(getCurrentConsole())) {
			return;
		}

		super.showPageRec(pageRec);
		fActiveConsole = recConsole;

		updateTitle();
	}

	/**
	 * Returns a set of consoles known by the view.
	 *
	 * @return a set of consoles known by the view.
	 */
	protected Set<IConsole> getConsoles() {
		return fConsoleToPart.keySet();
	}

	/**
	 * Updates the view title based on the active console
	 */
	protected void updateTitle() {
		IConsole console = getCurrentConsole();
		if (console == null) {
			setContentDescription(ConsoleMessages.ConsoleMessages_no_console);
		} else {
			String newName = console.getName();
			String oldName = getContentDescription();
			if (newName != null && !newName.equals(oldName)) {
				setContentDescription(newName);
			}
		}
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.page.dispose();
		pageRecord.dispose();

		IConsole console = fPartToConsole.remove(part);
		fConsoleToPart.remove(console);
		console.removePropertyChangeListener(this);
		
        if (fPartToConsole.isEmpty()) {
            fActiveConsole = null;
        }
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart dummyPart) {
		GdbConsoleWorkbenchPart part = (GdbConsoleWorkbenchPart)dummyPart;
		IConsole console = fPartToConsole.get(part);
		if (console instanceof GdbCliConsole) {
			final IPageBookViewPage page = ((GdbCliConsole)console).createPage(this);
			initPage(page);
			page.createControl(getPageBook());
			console.addPropertyChangeListener(this);

			return new PageRec(dummyPart, page);
		}
		assert false;
		return null;
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof GdbConsoleWorkbenchPart;
	}

	/**
	 * Returns the gdb console manager.
	 *
	 * @return the gdb console manager
	 */
	private GdbConsoleManager getGdbConsoleManager() {
		return GdbUIPlugin.getGdbConsoleManager();
	}

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		page.createControl(getPageBook());
		initPage(page);
		return page;
	}

	@Override
	public void consolesAdded(IConsole[] consoles) {
		if (isAvailable()) {
			asyncExec(() -> {
				for (IConsole console : consoles) {
					if (isAvailable()) {
						// ensure it's still registered since this is done asynchronously
						IConsole[] allConsoles = getGdbConsoleManager().getCliConsoles();
						for (IConsole registered : allConsoles) {
							if (registered.equals(console)) {
								GdbConsoleWorkbenchPart part = new GdbConsoleWorkbenchPart(console, getSite());
								fConsoleToPart.put(console, part);
								fPartToConsole.put(part, console);
								partActivated(part);
								break;
							}
						}

					}
				}
			});
		}
	}

	@Override
	public void consolesRemoved(IConsole[] consoles) {
		if (isAvailable()) {
			asyncExec(() -> {
				for (IConsole console : consoles) {
					if (isAvailable()) {
						GdbConsoleWorkbenchPart part = fConsoleToPart.get(console);
						if (part != null) {
							// partClosed() will also cleanup our maps
							partClosed(part);
						}
						if (getCurrentConsole() == null) {
							// When a part is closed, the page that is shown becomes
							// the default page, which does not have a console
							// We want to select a page wit ha console instead.
							IConsole[] available = getGdbConsoleManager().getCliConsoles();
							if (available.length > 0) {
								display(available[available.length - 1]);
							}
						}
					}
				}
			});
		}
	}

	protected void createActions() {
		fDisplayConsoleAction = new GdbConsoleDropDownAction(this);
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.add(fDisplayConsoleAction);
	}

	/**
	 * Show the specified console in this view.
	 * @param console The console to display.  Cannot be null.
	 */
	public void display(IConsole console) {
		if (console.equals(getCurrentConsole())) {
			// Already displayed
			return;
		}
		
		GdbConsoleWorkbenchPart part = fConsoleToPart.get(console);
		if (part != null) {
			partActivated(part);
		}
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		return null;
	}

	/**
	 * Registers the given runnable with the display associated with this view's
	 * control, if any.
	 *
	 * @param r the runnable
	 * @see org.eclipse.swt.widgets.Display#asyncExec(java.lang.Runnable)
	 */
	public void asyncExec(Runnable r) {
		if (isAvailable()) {
			getPageBook().getDisplay().asyncExec(r);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		createActions();
		configureToolBar(getViewSite().getActionBars().getToolBarManager());
		updateForExistingConsoles();
		getViewSite().getActionBars().updateActionBars();
		initPageSwitcher();
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
	}

	/**
	 * Initialize for existing consoles
	 */
	private void updateForExistingConsoles() {
		GdbConsoleManager manager = getGdbConsoleManager();
		// create pages for consoles
		IConsole[] consoles = manager.getCliConsoles();
		consolesAdded(consoles);
		// add as a listener
		manager.addConsoleListener(this);
	}

	/**
	 * Initialize the PageSwitcher.
	 * The page switcher is triggered using a keyboard shortcut
	 * configured in the user's eclipse and allows to switch 
	 * pages using a popup.
	 */
	private void initPageSwitcher() {
		new PageSwitcher(this) {
			@Override
			public void activatePage(Object page) {
				getGdbConsoleManager().showGdbConsoleView((IConsole)page);
			}

			@Override
			public ImageDescriptor getImageDescriptor(Object page) {
				return ((GdbCliConsole) page).getImageDescriptor();
			}

			@Override
			public String getName(Object page) {
				return ((GdbCliConsole) page).getName();
			}

			@Override
			public Object[] getPages() {
				return getGdbConsoleManager().getCliConsoles();
			}

			@Override
			public int getCurrentPageIndex() {
				IConsole currentConsole = getCurrentConsole();
				IConsole[] consoles = getGdbConsoleManager().getCliConsoles();
				for (int i = 0; i < consoles.length; i++) {
					if (consoles[i].equals(currentConsole)) {
						return i;
					}
				}
				return super.getCurrentPageIndex();
			}
		};
	}
	
	private void displayConsoleForLaunch(ILaunch launch) {
		if (launch != null) {
			for (IConsole console : getConsoles()) {
				if (console instanceof GdbCliConsole) {
					if (launch.equals(((GdbCliConsole)console).getLaunch())) {
						display(console);
						break;
					}
				} else {
					assert false;
				}
			}
		}
	}
	
	/**
	 * @return the launch to which the current selection belongs.
	 */
	protected ILaunch getCurrentLaunch() {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context != null) {
			return context.getAdapter(ILaunch.class);
		}
		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			// Show the GDB console that matches with the currently
			// selected debug session
			displayConsoleForLaunch(getCurrentLaunch());
		}
	}
}
