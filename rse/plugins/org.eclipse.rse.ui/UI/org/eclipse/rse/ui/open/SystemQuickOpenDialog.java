/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.open;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class SystemQuickOpenDialog extends Dialog implements ISystemQuickOpenPageContainer, IRunnableContext {
	
	// the tab folder layout
	private class TabFolderLayout extends Layout {
		
		/**
		 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
		 */
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}

			int x = 0; 
			int y = 0;				
			
			Control[] children = composite.getChildren();
			
			for (int i= 0; i < children.length; i++) {
				Point size = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				x = Math.max(x, size.x);
				y = Math.max(y, size.y);
			}
			
			Point minSize = getMinSize();
			x = Math.max(x, minSize.x);
			y = Math.max(y, minSize.y);
			
			if (wHint != SWT.DEFAULT) {
				x = wHint;
			}
			
			if (hHint != SWT.DEFAULT) {
				y = hHint;
			}
			
			return new Point(x, y);		
		}
		
		
		/**
		 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
		 */
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle rect = composite.getClientArea();
			
			Control[] children = composite.getChildren();
			
			for (int i = 0; i < children.length; i++) {
				children[i].setBounds(rect);
			}
		}
	}
	
	// contents and buttons
	private Control contents;
	private Button cancelButton;
	private Button openButton;
	
	private String performActionLabel = JFaceResources.getString("finish");
	
	// the number of long running operations being executed from the dialog	
	private long activeRunningOperations;
	
	// cursors during operation
	private Cursor waitCursor;
	private Cursor arrowCursor;

	// progress monitor
	private ProgressMonitorPart progressMonitorPart;
	
	// window closing dialog
	private MessageDialog windowClosingDialog;
	
	// minimum size for tab folder
	private Point minSize;
	
	private ISelection selection;
	private String initialPageId;
	private ISystemQuickOpenPage currentPage;
	private int currentIndex;
	private List descriptors;

	/**
	 * The constructor for the quick open dialog.
	 * @param shell the shell.
	 * @param selection the current selection.
	 * @param pageId the initial page id.
	 */
	public SystemQuickOpenDialog(Shell shell, ISelection selection, String pageId) {
		super(shell);
		this.selection = selection;
		this.initialPageId = pageId;
		this.descriptors = SystemQuickOpenUtil.getInstance().getQuickOpenPageDescriptors(initialPageId);
	}
	
	
	// ------------------------------- UI creation and handling ---------------------------------------
	
	/**
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		
		if (currentPage != null) {
			currentPage.setVisible(true);
		}
	}
	
	/**
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Open");
		// TODO: add image and F1 help
	}
	
	/**
	 * Creates a page area, a progress monitor and a separator.
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		
		// call super to get a standard composite
		Composite composite = (Composite)(super.createDialogArea(parent));
		
		// create a grid layout
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		
		// set layout for composite
		composite.setLayout(layout);
		
		// set layout data for composite
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// create the page area
		contents = createPageArea(composite);
		
		// create a progress monitor and make it invisible initially
		GridLayout pmlayout = new GridLayout();
		pmlayout.numColumns = 1;
		progressMonitorPart = new ProgressMonitorPart(composite, pmlayout, SWT.DEFAULT);
		progressMonitorPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		progressMonitorPart.setVisible(false);

		// add a separator
		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// apply dialog font
		applyDialogFont(composite);
		
		return composite;
	}
	
	/**
	 * Creates the page area.
	 * @param parent the parent composite.
	 * @return the page area control.
	 */
	protected Control createPageArea(Composite parent) {
		
		int numPages = descriptors.size();
		
		// if number of pages is 0, then just show a label
		if (numPages == 0) {
			Label label = new Label(parent, SWT.CENTER | SWT.WRAP);
			// TODO: set text
			//label.setText(SearchMessages.getString("SearchDialog.noSearchExtension")); //$NON-NLS-1$
			return label;
		}
		
		// get the preferred index, which is the index of the page with the initial page id, or depends on
		// the current selection
		currentIndex = getPreferredPageIndex();

		// get the current page from the index
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				currentPage = getDescriptorAt(currentIndex).createObject();
			}
		});
		
		// set the current page container
		currentPage.setContainer(this);

		// if number of pages is 1, simple get the control representing the page and return it
		if (numPages == 1) {
			return getControl(currentPage, parent);
		}
		// if number of pages is more than 1, then we create a tab folder
		else {
			
			// create a border composite
			Composite border = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginWidth= 7;
			layout.marginHeight= 7;
			border.setLayout(layout);
			
			// create a tab folder
			TabFolder folder = new TabFolder(border, SWT.NONE);
			folder.setLayoutData(new GridData(GridData.FILL_BOTH));
			folder.setLayout(new TabFolderLayout());

			// go through all descriptors
			for (int i = 0; i < numPages; i++) {			
				SystemQuickOpenPageDescriptor descriptor = (SystemQuickOpenPageDescriptor)(descriptors.get(i));

				// create a tab item for each descriptor
				final TabItem item = new TabItem(folder, SWT.NONE);
				
				// set the text of the tab item to the label of the descriptor
				item.setText(descriptor.getLabel());
				
				// add a dispose listener which destroys the image
				item.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						item.setData(null);
								
						if (item.getImage() != null) {
							item.getImage().dispose();
						}
					}
				});
				
				// get the image descriptor from the page descriptor
				ImageDescriptor imageDesc = descriptor.getImage();
				
				// if image descriptor exists, create image and set it for the tab item
				if (imageDesc != null) {
					item.setImage(imageDesc.createImage());
				}
				
				// set item data to the descriptor
				item.setData(descriptor);
				
				// now if index is the current index (i.e. the preferred index)
				if (i == currentIndex) {
					
					// get control corresponding to current page with folder as the parent
					item.setControl(getControl(currentPage, folder));
					
					// set the data to the actual page
					item.setData(currentPage);
				}
			}
			
			// add a selection listener to the folder
			folder.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					turnToPage(event);
				}
			});
		
			// set the selection to the current index
			folder.setSelection(currentIndex);
			
			// finally, return the border
			return border;
		}
	}
	
	/**
	 * Returns the index of the page to be displayed. If a particular page id was requested, then the index
	 * of the page that has that id is returned. Otherwise the index depends on the page most appropriate for 
	 * the current selection.
	 * @return the index of the page to be displayed.
	 */
	private int getPreferredPageIndex() {
		
		// TODO: calculate the most appropriate page depending on the selection
		int result = 0;
		
		int size = descriptors.size();
		
		for (int i = 0; i < size; i++) {
			
			SystemQuickOpenPageDescriptor descriptor = (SystemQuickOpenPageDescriptor)(descriptors.get(i));
			
			// if we have an initial page id then we must return the index
			if (initialPageId != null && initialPageId.equals(descriptor.getId())) {
				return i;
			}
			
			// TODO: find out the most appropriate page and return its index
		}
		
		return result;
	}
	
	/**
	 * Gets the page descriptor at the specified index.
	 * @param index the index.
	 * @return the page descriptor at the specified index.
	 */
	private SystemQuickOpenPageDescriptor getDescriptorAt(int index) {
		return (SystemQuickOpenPageDescriptor)(descriptors.get(index));
	}
	
	/**
	 * Returns the control representing the given page.
	 * If the control for the page hasn't been created yet, it is created.
	 * The parent of the page control is returned, i.e. we have a wrapper for a page and that is what is returned.
	 * @param page the quick open page.
	 * @param parent the parent in which to create the page wrapper where the page control will be created.
	 * @return the parent of the page control, i.e. a wrapper for the page. The wrapper's parent is the given parent.
	 */
	private Control getControl(ISystemQuickOpenPage page, Composite parent) {

		// if the page control is null, create it
		if (page.getControl() == null) {
			
			// create a wrapper for the page
			Composite pageWrapper = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			pageWrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			pageWrapper.setLayout(layout);
			
			// create the page in the wrapper
			page.createControl(pageWrapper);
		}
		
		// returns the wrapper
		return page.getControl().getParent();
	}
	
	/**
	 * Turns to the page which has been selected.
	 * @param event the selection event.
	 */
	private void turnToPage(SelectionEvent event) {
		final TabItem item = (TabItem)(event.item);
		
		// if control for tab item hasn't been created yet
		if (item.getControl() == null) {
			
			// get the data which is the descriptor
			final SystemQuickOpenPageDescriptor descriptor = (SystemQuickOpenPageDescriptor)(item.getData());
			
			// set the data to be the actual quick open page
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				public void run() {
					item.setData(descriptor.createObject());
				}
			});
			
			// now get the data, which is the quick open page
			ISystemQuickOpenPage page = (ISystemQuickOpenPage)(item.getData());
			
			// set the container of the page
			page.setContainer(this);
			
			// get the control represeting the page
			// note that the widget for the event is the tab folder
			Control newControl = getControl(page, (Composite)(event.widget));
			
			// set the item control
			item.setControl(newControl);
		}
		
		// get the item data and check whether it is an instance of quick open page
		if (item.getData() instanceof ISystemQuickOpenPage) {
			
			// the item data is the new current page
			currentPage = (ISystemQuickOpenPage)(item.getData());
			
			// the current index is the selection index of the item parent (i.e. the tab folder)
			currentIndex = item.getParent().getSelectionIndex();
			
			// resize dialog if needed and pass in the page wrapper
			// that method will test if the control in the page is smaller than the wrapper (i.e. its parent)
			resizeDialogIfNeeded(item.getControl());
			
			// make the current page visible
			currentPage.setVisible(true);
		}
	}
	
	/**
	 * Resizes dialog if needed. Tests the given control size with the size of the current page control.
	 * If the current page control is smaller, then resize.
	 * @param newControl the control whose size we want to test against the size of the page control.
	 */
	private void resizeDialogIfNeeded(Control newControl) {
		Point currentSize = currentPage.getControl().getSize();
		Point newSize = newControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		// if we must resize, then compute size of shell again, and set it
		if (mustResize(currentSize, newSize)) {
			Shell shell = getShell();
			shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		}
	}
	
	/**
	 * Returns whether we must resize.
	 * @param currentSize the current size.
	 * @param newSize the new size.
	 * @return <code>true</code> if current size is smaller than new size, <code>false</code> otherwise.
	 */
	private boolean mustResize(Point currentSize, Point newSize) {
		return currentSize.x < newSize.x || currentSize.y < newSize.y;
	}
	
	/**
	 * Gets the minimum size for the tab folder.
	 * @return
	 */
	private Point getMinSize() {
		
		if (minSize != null) {
			return minSize;
		}
			
		int x = 0;
		int y = 0;
		int length = descriptors.size();
		
		for (int i = 0; i < length; i++) {
			Point size = getDescriptorAt(i).getPreferredSize();
			
			if (size.x != SWT.DEFAULT) {
				x = Math.max(x, size.x);
			}
			if (size.y != SWT.DEFAULT) {
				y = Math.max(y, size.y);
			}
		}
		
		minSize = new Point(x, y);
		return minSize;	
	}

	/**
	 * Calls the super class method if there are no running operations.
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		
		if (activeRunningOperations == 0) {
			super.cancelPressed();	
		}
	}

	/**
	 * Calls <code>performAction</code>. If the result of calling this method is <code>true</code>
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		boolean result = performAction();
		
		if (result) {
			super.okPressed();
		}
	}
	
	/**
	 * Returns whether ok to close. Asks the current page, if any, whether it is ok to close.
	 * @return <code>true</code> if the dialog can be closed, <code>false</code> otherwise.
	 */
	protected boolean performAction() {
		
		if (currentPage == null) {
			return true;
		}
		
		return currentPage.performAction();
	}


	// ----------------------------------------- Interface methods ----------------------------------
	
	/**
	 * @see org.eclipse.rse.ui.open.ISystemQuickOpenPageContainer#getRunnableContext()
	 */
	public IRunnableContext getRunnableContext() {
		return this;
	}

	/**
	 * @see org.eclipse.rse.ui.open.ISystemQuickOpenPageContainer#getSelection()
	 */
	public ISelection getSelection() {
		return selection;
	}

	/**
	 * @see org.eclipse.rse.ui.open.ISystemQuickOpenPageContainer#setPerformActionEnabled(boolean)
	 */
	public void setPerformActionEnabled(boolean state) {
		
		if (openButton != null) {
			openButton.setEnabled(state);
		}
	}
	
	
	// ----------------------------- Operation related methods --------------------------
	
	/**
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
	
		// The operation can only be canceled if it is executed in a separate thread.
		// Otherwise the UI is blocked anyway.
		HashMap state = null;
		
		try {
			activeRunningOperations++;
			state = aboutToStart(fork && cancelable);
			ModalContext.run(runnable, fork, getProgressMonitor(), getShell().getDisplay());
		}
		finally {
			
			if (state != null) {
				stopped(state);
			}
			
			activeRunningOperations--;
		}
	}
	
	/**
	 * Returns the progress monitor. If the dialog doesn't
	 * have a progress monitor, <code>null</code> is returned.
	 */
	protected IProgressMonitor getProgressMonitor() {
		return progressMonitorPart;
	}
	
	/**
	 * About to start a long running operation tiggered through the dialog.
	 * Shows the progress monitor and disables the dialog.
	 * @param enableCancelButton <code>true</code> if cancel button should be enabled, <code>false</code> otherwise.
	 * @return the saved UI state.
	 * @see #stopped(HashMap);
	 */
	protected synchronized HashMap aboutToStart(boolean enableCancelButton) {
		HashMap savedState = null;
		
		Shell shell = getShell();
		
		if (shell != null) {
			Display d = shell.getDisplay();
			
			// get focus control
			Control focusControl = d.getFocusControl();
			
			if (focusControl != null && focusControl.getShell() != shell) {
				focusControl = null;
			}
				
			// set the busy cursor to all shells
			waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
			setDisplayCursor(d, waitCursor);
					
			// set the arrow cursor to the cancel component
			arrowCursor = new Cursor(d, SWT.CURSOR_ARROW);
			cancelButton.setCursor(arrowCursor);
	
			// deactivate shell
			savedState = saveUIState(enableCancelButton);
			
			// save focus control
			if (focusControl != null) {
				savedState.put("focusControl", focusControl);
			}
				
			// attach the progress monitor part to the cancel button and make it visible
			progressMonitorPart.attachToCancelComponent(cancelButton);
			progressMonitorPart.setVisible(true);
		}
		
		return savedState;
	}
	
	/**
	 * A long running operation triggered through the wizard
	 * was stopped either by user input or by normal end.
	 * @param savedState The saveState returned by <code>aboutToStart</code>.
	 * @see #aboutToStart(boolean)
	 */
	protected synchronized void stopped(HashMap state) {
		
		Shell shell = getShell();
		
		if (shell != null) {
	
			progressMonitorPart.setVisible(false);	
			progressMonitorPart.removeFromCancelComponent(cancelButton);
			
			restoreUIState(state);
	
			setDisplayCursor(shell.getDisplay(), null);	
			cancelButton.setCursor(null);
			waitCursor.dispose();
			waitCursor = null;
			arrowCursor.dispose();
			arrowCursor = null;
			Control focusControl = (Control)(state.get("focusControl"));
			
			if (focusControl != null && ! focusControl.isDisposed()) {
				focusControl.setFocus();
			}
		}
	}
	
	/**
	 * Sets a cursor for all shells in a display.
	 * @param d the display.
	 * @param c the cursor.
	 */
	private void setDisplayCursor(Display d, Cursor c) {
		
		Shell[] shells = d.getShells();
		
		for (int i= 0; i < shells.length; i++) {
			shells[i].setCursor(c);
		}
	}	


	//------------------------ UI state save and restoring -------------------------------------
	
	/**
	 * Restores the enable state of the UI, i.e. all dialog contents.
	 * @param state the hashmap that contains the enable state of the UI.
	 */
	private void restoreUIState(HashMap state) {
		restoreEnableState(cancelButton, state, "cancel");
		restoreEnableState(openButton, state, "open");
		ControlEnableState pageState = (ControlEnableState)state.get("tabForm");
		pageState.restore();
	}
	
	/**
	 * Restores the enable state of a control.
	 * @param w the control whose state needs to be restored.
	 * @param h the hashmap containing the enable state of the control.
	 * @param key the key to use to retrieve the enable state.
	 */
	protected void restoreEnableState(Control w, HashMap h, String key) {
		
		if (!w.isDisposed()) {
			Boolean b = (Boolean)h.get(key);
			
			if (b != null) {
				w.setEnabled(b.booleanValue());
			}
		}
	}
	
	/**
	 * Disables all dialog contents, except maybe the cancel button, depending on the given boolean.
	 * @param keepCancelEnabled <code>true</code> if cancel button is enabled, <code>false</code> otherwise.
	 * @return the saved state.
	 */
	private HashMap saveUIState(boolean keepCancelEnabled) {
		HashMap savedState = new HashMap();
		
		saveEnableStateAndSet(cancelButton, savedState, "cancel", keepCancelEnabled);
		saveEnableStateAndSet(openButton, savedState, "open", false);
		savedState.put("tabForm", ControlEnableState.disable(contents));
		
		return savedState;
	}
	
	/**
	 * Saves the enable state of a control and sets it as well.
	 * @param w the control whose enable state we want to set and save.
	 * @param h the hashmap where the enable state of the control will be saved.
	 * @param key the key with which to save the enable state.
	 * @param enabled <code>true</code> if control is to be enabled, <code>false</code> otherwise.
	 */
	private void saveEnableStateAndSet(Control w, HashMap h, String key, boolean enabled) {
		
		if (!w.isDisposed()) {
			h.put(key, new Boolean(w.isEnabled()));
			w.setEnabled(enabled);
		}	
	}	


	// ------------------------------- Handle shell closing ------------------------------
	
	/**
	 * Checks to see if there are any long running operations. If there are, a dialog is shown 
	 * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
	 */
	protected void handleShellCloseEvent() {
		
		if (okToClose()) {
			super.handleShellCloseEvent();
		}
	}

	/**
	 * Checks if any operations are running. If so, shows a message dialog alerting the user, and returns <code>false</code>
	 * indicating the dialog should not be closed.
	 * @param <code>true</code> if it is ok to close the dialog, <code>false</code> otherwise.
	 */
	public boolean okToClose() {
		
		if (activeRunningOperations > 0) {
			
			// get closing dialog
			synchronized (this) {
				windowClosingDialog = createClosingDialog();
			}	
			
			// open it
			windowClosingDialog.open();
			
			// make it null
			synchronized (this) {
				windowClosingDialog = null;
			}
			
			// indicate that operations are running, so not ok to close
			return false;
		}
		
		return true;
	}
	
	/**
	 * Creates a dialog with the message that the quick open dialog is closing.
	 * @return the message dialog.
	 */
	private MessageDialog createClosingDialog() {
		MessageDialog result = new MessageDialog(getShell(), JFaceResources.getString("WizardClosingDialog.title"),
												 null, JFaceResources.getString("WizardClosingDialog.message"),
												 MessageDialog.QUESTION, new String[] {IDialogConstants.OK_LABEL}, 0); 
		return result;		
	}
}