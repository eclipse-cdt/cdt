package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.PageSwitcher;

public class GdbConsoleView extends PageBookView implements IConsoleListener{

	/**
	 * Stack of consoles in MRU order
	 */
	private List<IConsole> fStack = new ArrayList<>();

	/**
	 * The console being displayed, or <code>null</code> if none
	 */
	private IConsole fActiveConsole = null;

	/**
	 * Map of consoles to dummy console parts (used to close pages)
	 */
	private Map<IConsole, GdbConsoleWorkbenchPart> fConsoleToPart;

	/**
	 * Map of parts to consoles
	 */
	private Map<GdbConsoleWorkbenchPart, IConsole> fPartToConsole;

	/**
	 * Whether this view is active
	 */
	private boolean fActive = false;

	private IContextActivation fActivatedContext;

	// actions
	private GdbConsoleDropDownAction fDisplayConsoleAction = null;

	private boolean isAvailable() {
		return getPageBook() != null && !getPageBook().isDisposed();
	}

	public IConsole getConsole() {
		return fActiveConsole;
	}

	@Override
	protected void showPageRec(PageRec pageRec) {
		IConsole recConsole = fPartToConsole.get(pageRec.part);
		if (recConsole!=null && recConsole.equals(fActiveConsole)) {
			return;
		}

		super.showPageRec(pageRec);
		fActiveConsole = recConsole;
		IConsole tos = null;
		if (!fStack.isEmpty()) {
			tos = fStack.get(0);
		}

		if (fActiveConsole != null && !fActiveConsole.equals(tos)) {
			fStack.remove(fActiveConsole);
			fStack.add(0,fActiveConsole);
		}
		updateTitle();
	}

	/**
	 * Returns a stack of consoles in the view in MRU order.
	 *
	 * @return a stack of consoles in the view in MRU order
	 */
	protected List<IConsole> getConsoleStack() {
		return fStack;
	}

	/**
	 * Updates the view title based on the active console
	 */
	protected void updateTitle() {
		IConsole console = getConsole();
		if (console == null) {
			setContentDescription("No consoles to display at this time.");
		} else {
			String newName = console.getName();
			String oldName = getContentDescription();
			if (newName!=null && !(newName.equals(oldName))) {
				setContentDescription(console.getName());
			}
		}
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		IConsole console = fPartToConsole.get(part);

		IPage page = pageRecord.page;
		page.dispose();
		pageRecord.dispose();

		// empty cross-reference cache
		fPartToConsole.remove(part);
		fConsoleToPart.remove(console);
		if (fPartToConsole.isEmpty()) {
			fActiveConsole = null;
		}
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart dummyPart) {
		GdbConsoleWorkbenchPart part = (GdbConsoleWorkbenchPart)dummyPart;
		final GdbCliConsole console = (GdbCliConsole)part.getConsole();
		final IPageBookViewPage page = console.createPage(this);
		initPage(page);
		page.createControl(getPageBook());

		PageRec rec = new PageRec(dummyPart, page);
		return rec;
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof GdbConsoleWorkbenchPart;
	}

	@Override
	public void dispose() {
		IViewSite site = getViewSite();
		if(site != null) {
			site.getPage().removePartListener((IPartListener2)this);
		}
		super.dispose();
//		ConsoleManager consoleManager = (ConsoleManager) ConsolePlugin.getDefault().getConsoleManager();
//		consoleManager.removeConsoleListener(this);
//		consoleManager.unregisterConsoleView(this);
		if (fDisplayConsoleAction != null) {
			fDisplayConsoleAction.dispose();
			fDisplayConsoleAction = null;
		}
	}

	/**
	 * Returns the console manager.
	 *
	 * @return the console manager
	 */
	private GdbConsoleManager getConsoleManager() {
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
	public void consolesAdded(final IConsole[] consoles) {
		if (isAvailable()) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < consoles.length; i++) {
						if (isAvailable()) {
							IConsole console = consoles[i];
							// ensure it's still registered since this is done asynchronously
							IConsole[] allConsoles = getConsoleManager().getConsoles();
							for (int j = 0; j < allConsoles.length; j++) {
								IConsole registered = allConsoles[j];
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
				}
			};
			asyncExec(r);
		}
	}

	@Override
	public void consolesRemoved(final IConsole[] consoles) {
		if (isAvailable()) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < consoles.length; i++) {
						if (isAvailable()) {
							IConsole console = consoles[i];
							fStack.remove(console);
							GdbConsoleWorkbenchPart part = fConsoleToPart.get(console);
							if (part != null) {
								partClosed(part);
							}
							if (getConsole() == null) {
								GdbCliConsole[] available = getConsoleManager().getConsoles();
								if (available.length > 0) {
									display(available[available.length - 1]);
								}
							}
						}
					}
				}
			};
			asyncExec(r);
		}
	}

	/**
	 * Constructs a console view
	 */
	public GdbConsoleView() {
		super();
		fConsoleToPart = new HashMap<>();
		fPartToConsole = new HashMap<>();
		getConsoleManager().addConsoleListener(this);
	}

	protected void createActions() {
		fDisplayConsoleAction = new GdbConsoleDropDownAction(this);
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.add(new Separator(IConsoleConstants.LAUNCH_GROUP));
		mgr.add(new Separator(IConsoleConstants.OUTPUT_GROUP));
		mgr.add(new Separator("fixedGroup")); //$NON-NLS-1$
		mgr.add(fDisplayConsoleAction);
	}

	public void display(IConsole console) {
		if (console.equals(fActiveConsole)) {
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
		IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
		configureToolBar(tbm);
		updateForExistingConsoles();
		getViewSite().getActionBars().updateActionBars();
		initPageSwitcher();
	}

	/**
	 * Initialize the PageSwitcher.
	 */
	private void initPageSwitcher() {
		new PageSwitcher(this) {
			@Override
			public void activatePage(Object page) {
				getConsoleManager().showConsoleView((GdbCliConsole)page);
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
				return getConsoleManager().getConsoles();
			}

			@Override
			public int getCurrentPageIndex() {
				IConsole currentConsole= getConsole();
				IConsole[] consoles= getConsoleManager().getConsoles();
				for (int i= 0; i < consoles.length; i++) {
					if (consoles[i].equals(currentConsole)) {
						return i;
					}
				}
				return super.getCurrentPageIndex();
			}
		};
	}

	/**
	 * Initialize for existing consoles
	 */
	private void updateForExistingConsoles() {
		GdbConsoleManager manager = getConsoleManager();
		// create pages for consoles
		IConsole[] consoles = manager.getConsoles();
		consolesAdded(consoles);
		// add as a listener
//		manager.addConsoleListener(this);
	}
}
