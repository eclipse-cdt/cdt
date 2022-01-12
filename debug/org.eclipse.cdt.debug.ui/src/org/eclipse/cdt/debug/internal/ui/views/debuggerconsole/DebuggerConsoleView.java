/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.debuggerconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleManager;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleView;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSwitcher;

/**
 * The Debugger console view shows different {@link IDebuggerConsole}.
 *
 * This class extends {@link IConsoleView} to allow it to easily display
 * consoles of type {@link IOConsole}.
 *
 * @see {@link IDebuggerConsoleManager}
 */
public class DebuggerConsoleView extends PageBookView
		implements IConsoleView, IDebuggerConsoleView, IConsoleListener, IPropertyChangeListener, IPartListener2 {

	public static final String DEBUGGER_CONSOLE_VIEW_ID = "org.eclipse.cdt.debug.ui.debuggerConsoleView"; //$NON-NLS-1$
	public static final String DROP_DOWN_ACTION_ID = DEBUGGER_CONSOLE_VIEW_ID + ".DebuggerConsoleDropDownAction"; //$NON-NLS-1$

	/**
	 * Stack of consoles in MRU order
	 */
	private List<IDebuggerConsole> fStack = new ArrayList<>();

	/** The console being displayed, or <code>null</code> if none */
	private IDebuggerConsole fActiveConsole;

	/** Map of consoles to dummy console parts (used to close pages) */
	private Map<IDebuggerConsole, DebuggerConsoleWorkbenchPart> fConsoleToPart = new HashMap<>();

	/** Map of parts to consoles */
	private Map<DebuggerConsoleWorkbenchPart, IDebuggerConsole> fPartToConsole = new HashMap<>();

	private DebuggerConsoleDropDownAction fDisplayConsoleAction;

	// Code for page participants borrowed from
	// org.eclipse.ui.internal.console.ConsoleView
	/**
	 * Map of consoles to array of page participants
	 */
	private Map<IDebuggerConsole, ListenerList<IConsolePageParticipant>> fConsoleToPageParticipants = new HashMap<>();

	/**
	 * Whether this view is active
	 */
	private boolean fActive = false;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		createActions();
		configureToolBar(getViewSite().getActionBars().getToolBarManager());

		// create pages for existing consoles
		IConsole[] consoles = getConsoleManager().getConsoles();
		consolesAdded(consoles);

		// add as a listener for new consoles
		getConsoleManager().addConsoleListener(this);
		// register for part events
		getViewSite().getPage().addPartListener((IPartListener2) this);
		getViewSite().getActionBars().updateActionBars();
		initPageSwitcher();
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart dummyPart) {
		DebuggerConsoleWorkbenchPart part = (DebuggerConsoleWorkbenchPart) dummyPart;
		IDebuggerConsole console = fPartToConsole.get(part);
		IPageBookViewPage page = console.createDebuggerPage(this);
		initPage(page);
		page.createControl(getPageBook());
		console.addPropertyChangeListener(this);

		// initialize page participants
		IConsolePageParticipant[] consoleParticipants = ((DebuggerConsoleManager) getConsoleManager())
				.getPageParticipants(console);
		final ListenerList<IConsolePageParticipant> participants = new ListenerList<>();
		for (int i = 0; i < consoleParticipants.length; i++) {
			participants.add(consoleParticipants[i]);
		}
		fConsoleToPageParticipants.put(console, participants);
		for (IConsolePageParticipant participant : participants) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					participant.init(page, console);
				}

				@Override
				public void handleException(Throwable exception) {
					CDebugUIPlugin.log(exception);
					participants.remove(participant);
				}
			});
		}

		return new PageRec(dummyPart, page);
	}

	protected void createActions() {
		fDisplayConsoleAction = new DebuggerConsoleDropDownAction(this);
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.add(new Separator(IConsoleConstants.LAUNCH_GROUP));
		mgr.add(new Separator(IConsoleConstants.OUTPUT_GROUP));
		mgr.add(new Separator("fixedGroup")); //$NON-NLS-1$
		mgr.add(fDisplayConsoleAction);
	}

	@Override
	public void dispose() {
		super.dispose();
		getConsoleManager().removeConsoleListener(this);
		getViewSite().getPage().removePartListener((IPartListener2) this);
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
	 */
	@Override
	public IDebuggerConsole getCurrentConsole() {
		return fActiveConsole;
	}

	@Override
	protected void showPageRec(PageRec pageRec) {
		IDebuggerConsole recConsole = fPartToConsole.get(pageRec.part);
		if (recConsole != null && recConsole.equals(getCurrentConsole())) {
			return;
		}

		super.showPageRec(pageRec);
		fActiveConsole = recConsole;

		IDebuggerConsole topOfStack = null;
		if (!fStack.isEmpty()) {
			topOfStack = fStack.get(0);
		}

		if (topOfStack != null && !topOfStack.equals(fActiveConsole) && fActive) {
			deactivateParticipants(topOfStack);
		}
		if (fActiveConsole != null && !fActiveConsole.equals(topOfStack)) {
			fStack.remove(fActiveConsole);
			fStack.add(0, fActiveConsole);
			activateParticipants(fActiveConsole);
		}

		updateTitle();
	}

	/**
	 * Activates the participants for the given console, if any.
	 *
	 * @param console the console
	 */
	private void activateParticipants(IDebuggerConsole console) {
		// activate
		if (console != null && fActive) {
			final ListenerList<IConsolePageParticipant> listeners = getParticipants(console);
			if (listeners != null) {
				for (IConsolePageParticipant participant : listeners) {
					SafeRunner.run(new ISafeRunnable() {
						@Override
						public void run() throws Exception {
							participant.activated();
						}

						@Override
						public void handleException(Throwable exception) {
							CDebugUIPlugin.log(exception);
							listeners.remove(participant);
						}
					});
				}
			}
		}
	}

	/**
	 * Returns the page participants registered for the given console, or
	 * <code>null</code>
	 *
	 * @param console
	 *            the console
	 * @return registered page participants or <code>null</code>
	 */
	private ListenerList<IConsolePageParticipant> getParticipants(IDebuggerConsole console) {
		return fConsoleToPageParticipants.get(console);
	}

	/**
	 * Returns a set of consoles known by the view.
	 */
	protected Set<IDebuggerConsole> getConsoles() {
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
		IDebuggerConsole console = fPartToConsole.remove(part);

		// dispose page participants
		ListenerList<IConsolePageParticipant> listeners = fConsoleToPageParticipants.remove(console);
		if (listeners != null) {
			for (IConsolePageParticipant participant : listeners) {
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void run() throws Exception {
						participant.dispose();
					}

					@Override
					public void handleException(Throwable exception) {
						CDebugUIPlugin.log(exception);
					}
				});
			}
		}

		pageRecord.page.dispose();
		pageRecord.dispose();

		fConsoleToPart.remove(console);
		console.removePropertyChangeListener(this);

		if (fPartToConsole.isEmpty()) {
			fActiveConsole = null;
		}
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof DebuggerConsoleWorkbenchPart;
	}

	private IDebuggerConsoleManager getConsoleManager() {
		return CDebugUIPlugin.getDebuggerConsoleManager();
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
						// Ensure console is still registered since this is done asynchronously
						IDebuggerConsole[] allConsoles = getConsoleManager().getConsoles();
						for (IDebuggerConsole registered : allConsoles) {
							if (registered.equals(console)) {
								DebuggerConsoleWorkbenchPart part = new DebuggerConsoleWorkbenchPart(registered,
										getSite());
								fConsoleToPart.put(registered, part);
								fPartToConsole.put(part, registered);
								// Must call partActivated() to create the page
								// However, this will also show the page, which is not
								// what we want.  Therefore, let's force the previous
								// page to be shown again right after.
								IDebuggerConsole previouslyShown = getCurrentConsole();
								partActivated(part);
								if (previouslyShown != null) {
									display(previouslyShown);
								}
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
						fStack.remove(console);
						DebuggerConsoleWorkbenchPart part = fConsoleToPart.get(console);
						if (part != null) {
							// partClosed() will also cleanup our maps
							partClosed(part);
						}
						if (getCurrentConsole() == null) {
							// When a part is closed, the page that is shown becomes
							// the default page, which does not have a console.
							// We want to select a page with a console instead.
							IDebuggerConsole[] available = getConsoleManager().getConsoles();
							if (available.length > 0) {
								display(available[available.length - 1]);
							}
						}
					}
				}
			});
		}
	}

	@Override
	public void display(IDebuggerConsole console) {
		if (console.equals(getCurrentConsole())) {
			// Already displayed
			return;
		}

		DebuggerConsoleWorkbenchPart part = fConsoleToPart.get(console);
		if (part != null) {
			partActivated(part);
			// let the console know it's being activated
			fActiveConsole.consoleSelected();
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
	private void asyncExec(Runnable r) {
		if (isAvailable()) {
			getPageBook().getDisplay().asyncExec(r);
		}
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
				display((IDebuggerConsole) page);
			}

			@Override
			public ImageDescriptor getImageDescriptor(Object page) {
				return ((IDebuggerConsole) page).getImageDescriptor();
			}

			@Override
			public String getName(Object page) {
				return ((IDebuggerConsole) page).getName();
			}

			@Override
			public Object[] getPages() {
				return getConsoleManager().getConsoles();
			}

			@Override
			public int getCurrentPageIndex() {
				IConsole currentConsole = getCurrentConsole();
				IConsole[] consoles = getConsoleManager().getConsoles();
				for (int i = 0; i < consoles.length; i++) {
					if (consoles[i].equals(currentConsole)) {
						return i;
					}
				}
				return super.getCurrentPageIndex();
			}
		};
	}

	@Override
	public void setAutoScrollLock(boolean scrollLock) {
	}

	@Override
	public boolean getAutoScrollLock() {
		return false;
	}

	@Override
	public void display(IConsole console) {
		if (console instanceof IDebuggerConsole) {
			display((IDebuggerConsole) console);
		}
	}

	@Override
	public void setPinned(boolean pin) {
	}

	@Override
	public void pin(IConsole console) {
	}

	@Override
	public boolean isPinned() {
		return false;
	}

	@Override
	public IConsole getConsole() {
		return getCurrentConsole();
	}

	@Override
	public void warnOfContentChange(IConsole console) {
		assert false;
	}

	@Override
	public void setScrollLock(boolean scrollLock) {
	}

	@Override
	public boolean getScrollLock() {
		return false;
	}

	@Override
	public void setWordWrap(boolean wordWrap) {
	}

	@Override
	public boolean getWordWrap() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> key) {
		Object adapter = super.getAdapter(key);
		if (adapter == null) {
			IDebuggerConsole console = getCurrentConsole();
			if (console != null) {
				ListenerList<IConsolePageParticipant> participants = getParticipants(console);
				// an adapter can be asked for before the participants are created
				if (participants != null) {
					for (IConsolePageParticipant participant : participants) {
						adapter = participant.getAdapter(key);
						if (adapter != null) {
							return (T) adapter;
						}
					}
				}
			}
		}
		return (T) adapter;
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			fActive = true;
			activateParticipants(fActiveConsole);
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// Do nothing
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		// Do nothing
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		if (isThisPart(partRef)) {
			fActive = false;
			deactivateParticipants(fActiveConsole);
		}
	}

	/**
	 * Returns if the specified part reference is to this view part (if the part
	 * reference is the console view or not)
	 *
	 * @param partRef
	 *            the workbench part reference
	 * @return true if the specified part reference is the console view
	 */
	protected boolean isThisPart(IWorkbenchPartReference partRef) {
		if (partRef instanceof IViewReference) {
			IViewReference viewRef = (IViewReference) partRef;
			if (getViewSite() != null && viewRef.getId().equals(getViewSite().getId())) {
				String secId = viewRef.getSecondaryId();
				String mySec = null;
				if (getSite() instanceof IViewSite) {
					mySec = ((IViewSite) getSite()).getSecondaryId();
				}
				if (mySec == null) {
					return secId == null;
				}
				return mySec.equals(secId);
			}
		}
		return false;
	}

	/**
	 * Deactivates participants for the given console, if any.
	 *
	 * @param console
	 *            console to deactivate
	 */
	private void deactivateParticipants(IDebuggerConsole console) {
		// deactivate
		if (console != null) {
			final ListenerList<IConsolePageParticipant> listeners = getParticipants(console);
			if (listeners != null) {
				for (IConsolePageParticipant participant : listeners) {
					SafeRunner.run(new ISafeRunnable() {
						@Override
						public void run() throws Exception {
							participant.deactivated();
						}

						@Override
						public void handleException(Throwable exception) {
							CDebugUIPlugin.log(exception);
							listeners.remove(participant);
						}
					});
				}
			}
		}
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		// Do nothing
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// Do nothing
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// Do nothing
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// Do nothing
	}
}
