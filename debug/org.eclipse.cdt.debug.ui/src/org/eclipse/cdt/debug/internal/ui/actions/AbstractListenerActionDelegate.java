package org.eclipse.cdt.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class AbstractListenerActionDelegate extends AbstractDebugActionDelegate implements IDebugEventSetListener, IPartListener, IPageListener {

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(getView())) {
			dispose();
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().removeDebugEventListener(this);
		getWindow().removePageListener(this);
		if (getView() != null) {
			getView().getViewSite().getPage().removePartListener(this);	
		}
	}
	
	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(final DebugEvent[] events) {
		if (getPage() == null || getAction() == null) {
			return;
		}
		Shell shell= getWindow().getShell();
		if (shell == null || shell.isDisposed()) {
			return;
		}
		Runnable r= new Runnable() {
			public void run() {
				Shell shell= getWindow().getShell();
				if (shell == null || shell.isDisposed()) {
					return;
				}
				for (int i = 0; i < events.length; i++) {
					if (events[i].getSource() != null) {
						doHandleDebugEvent(events[i]);
					}
				}
			}
		};
		
		getPage().getWorkbenchWindow().getShell().getDisplay().asyncExec(r);
	}
	
	/**
	 * Returns the page that this action works in.
	 */
	protected IWorkbenchPage getPage() {
		if (getWindow() != null) {
			return getWindow().getActivePage();
		} else {
			IWorkbenchWindow window=  DebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				return window.getActivePage();
			}
		}
		return null;
	}
	
	/**
	 * Default implementation to update on specific debug events.
	 * Subclasses should override to handle events differently.
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		switch (event.getKind()) {
			case DebugEvent.TERMINATE :
				update(getAction(), getSelection());
				break;
			case DebugEvent.RESUME :
				if (!event.isEvaluation() || !((event.getDetail() & DebugEvent.EVALUATION_IMPLICIT) != 0)) {
					update(getAction(), getSelection());
				}
				break;
			case DebugEvent.SUSPEND :
				// Update on suspend events (even for evaluations), in case the user changed
				// the selection during an implicit evaluation.
				update(getAction(), getSelection());
				break;
		}
	}		

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
		super.init(window);
		DebugPlugin.getDefault().addDebugEventListener(this);
		window.addPageListener(this);
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().addDebugEventListener(this);
		setWindow(view.getViewSite().getWorkbenchWindow());
		if ( getPage() != null )
		{
			getPage().addPartListener(this);
			getPage().getWorkbenchWindow().addPageListener(this);
		}
	}
	
	/**
	 * @see IPageListener#pageActivated(IWorkbenchPage)
	 */
	public void pageActivated(IWorkbenchPage page) {
		if (getAction() != null && getView() != null && getPage() != null && getPage().equals(page)) {
			Runnable r= new Runnable() {
				public void run() {
					if (getPage() != null) {
						IWorkbenchWindow window= getPage().getWorkbenchWindow();
						if (window != null && window.getShell() != null && !window.getShell().isDisposed()) {
							ISelection selection= getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
							update(getAction(), selection);
						}
					}
				}
			};
		
			getPage().getWorkbenchWindow().getShell().getDisplay().asyncExec(r);
		}
	}

	/**
	 * @see IPageListener#pageClosed(IWorkbenchPage)
	 */
	public void pageClosed(IWorkbenchPage page) {
		if (page.equals(getPage())) {
			dispose();
		}
	}

	/**
	 * @see IPageListener#pageOpened(IWorkbenchPage)
	 */
	public void pageOpened(IWorkbenchPage page) {
	}
}
