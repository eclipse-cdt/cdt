/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class RefactoringWizardDialog2 extends Dialog implements IWizardContainer {

	private RefactoringWizard fWizard;
	private IWizardPage fCurrentPage;
	private IWizardPage fVisiblePage;
	
	private PageBook fPageContainer;
	private PageBook fStatusContainer;
	private MessageBox fMessageBox;
	private ProgressMonitorPart fProgressMonitorPart;
	private int fActiveRunningOperations;
	private Cursor fWaitCursor;
	private Cursor fArrowCursor;

	private static final int PREVIEW_ID= IDialogConstants.CLIENT_ID + 1;

	private int fPreviewWidth;
	private int fPreviewHeight;
	private IDialogSettings fSettings;
	private static final String DIALOG_SETTINGS= "RefactoringWizard.preview"; //$NON-NLS-1$
	private static final String WIDTH= "width"; //$NON-NLS-1$
	private static final String HEIGHT= "height"; //$NON-NLS-1$
	
	private static final Image INFO= CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_INFO);
	private static final Image WARNING= CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_WARNING);
	private static final Image ERROR= CPluginImages.get(CPluginImages.IMG_OBJS_REFACTORING_ERROR);
	
	private static class MessageBox extends Composite {
		private Label fImage;
		private Label fText;
		public MessageBox(Composite parent, int style) {
			super(parent, style);
			GridLayout layout= new GridLayout();
			layout.numColumns= 2;
			setLayout(layout);
			fImage= new Label(this, SWT.NONE);
			fImage.setImage(INFO);
			Point size= fImage.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			GridData gd= new GridData();
			gd.verticalAlignment= SWT.TOP;			
			gd.widthHint= size.x;
			gd.heightHint= size.y;
			fImage.setLayoutData(gd);
			fImage.setImage(null);
			fText= new Label(this, SWT.WRAP);
			fText.setText(" \n "); //$NON-NLS-1$
			size= fText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			gd= new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint= size.y;
			gd.verticalAlignment= SWT.TOP;			
			fText.setLayoutData(gd);
		}
		public void setMessage(IWizardPage page) {
			String msg= page.getErrorMessage();
			int type= IMessageProvider.ERROR;
			if (msg == null || msg.length() == 0) {
				msg= page.getMessage();
				type= IMessageProvider.NONE;
			if (msg != null && page instanceof IMessageProvider) 
				type = ((IMessageProvider)page).getMessageType();
			}
			Image image= null;
			switch (type) {
				case IMessageProvider.INFORMATION:
					image= INFO;
					break;
				case IMessageProvider.WARNING:
					image= WARNING;
					break;
				case IMessageProvider.ERROR:
					image= ERROR;
					break;
			}
			if (msg == null)
				msg= ""; //$NON-NLS-1$
			fText.setText(msg);
			if (image == null && msg.length() > 0)
				image= INFO;
			fImage.setImage(image);
		}
	}
	
	private static class PageBook extends Composite {
		private StackLayout fLayout;
		public PageBook(Composite parent, int style) {
			super(parent, style);
			fLayout= new StackLayout();
			setLayout(fLayout);
			fLayout.marginWidth= 5; fLayout.marginHeight= 5;
		}
		public void showPage(Control page) {
			fLayout.topControl= page;
			layout();
		}
		public Control getTopPage() {
			return fLayout.topControl;
		}
	}
	
	public RefactoringWizardDialog2(Shell shell, RefactoringWizard wizard) {
		super(shell);
		Assert.isNotNull(wizard);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		wizard.setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		fWizard= wizard; 
		fWizard.setContainer(this);
		fWizard.addPages();
		initSize();
	}
	
	private void initSize() {
		IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings();
		fSettings= settings.getSection(DIALOG_SETTINGS);
		if (fSettings == null) {
			fSettings= new DialogSettings(DIALOG_SETTINGS);
			settings.addSection(fSettings);
			fSettings.put(WIDTH, 600);
			fSettings.put(HEIGHT, 100);
		}
		fPreviewWidth= 600;
		fPreviewHeight= 100;
		try {
			fPreviewWidth= fSettings.getInt(WIDTH);
			fPreviewHeight= fSettings.getInt(HEIGHT);
		} catch (NumberFormatException e) {
		}
	}
	
	private void saveSize() {
		if (fCurrentPage instanceof PreviewWizardPage) {
			Control control= fCurrentPage.getControl().getParent();
			Point size = control.getSize();
			fSettings.put(WIDTH, size.x);
			fSettings.put(HEIGHT, size.y);
		}
	}
	
	//---- IWizardContainer --------------------------------------------

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void showPage(IWizardPage page) {
		fCurrentPage= page;
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void updateButtons() {
		boolean previewPage= isPreviewPageActive();
		boolean ok= fWizard.canFinish();
		boolean canFlip= fCurrentPage.canFlipToNextPage();
		Button previewButton= getButton(PREVIEW_ID);
		Button defaultButton= null;
		if (previewButton != null && !previewButton.isDisposed()) {
			previewButton.setEnabled(!previewPage);
			if (!previewPage)
				previewButton.setEnabled(canFlip);
			if (previewButton.isEnabled())
				defaultButton= previewButton;
		}
		Button okButton= getButton(IDialogConstants.OK_ID);
		if (okButton != null && !okButton.isDisposed()) {
			okButton.setEnabled(ok);
			if (ok)
				defaultButton= okButton;
		}
		if (defaultButton != null) {
			defaultButton.getShell().setDefaultButton(defaultButton);
		}
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void updateMessage() {
		if (fStatusContainer == null || fStatusContainer.isDisposed())
			return;
		fStatusContainer.showPage(fMessageBox);
		fMessageBox.setMessage(fCurrentPage);
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void updateTitleBar() {
		// we don't have a title bar.
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void updateWindowTitle() {
		getShell().setText(fWizard.getWindowTitle());
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public IWizardPage getCurrentPage() {
		return fCurrentPage;
	}
	
	//---- IRunnableContext --------------------------------------------

	/* (non-Javadoc)
	 * Method declared on IRunnableContext
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		if (fProgressMonitorPart == null) {
			ModalContext.run(runnable, false, new NullProgressMonitor(), getShell().getDisplay());
		} else {
			Object state = null;
			if(fActiveRunningOperations == 0)
				state = aboutToStart(fork && cancelable);
		
			fActiveRunningOperations++;
			try {
				ModalContext.run(runnable, fork, fProgressMonitorPart, getShell().getDisplay());
			} finally {
				fActiveRunningOperations--;
				//Stop if this is the last one
				if(state!= null)
					stopped(state);
			}
		}
	}
	
	private Object aboutToStart(boolean cancelable) {
		Map savedState = null;
		Shell shell= getShell();
		if (shell != null) {
			// Save focus control
			Control focusControl = getShell().getDisplay().getFocusControl();
			if (focusControl != null && focusControl.getShell() != getShell())
				focusControl = null;
				
			Button cancelButton= getButton(IDialogConstants.CANCEL_ID);	
			// Set the busy cursor to all shells.
			Display d = getShell().getDisplay();
			fWaitCursor = new Cursor(d, SWT.CURSOR_WAIT);
			setDisplayCursor(d, fWaitCursor);
					
			// Set the arrow cursor to the cancel component.
			fArrowCursor= new Cursor(d, SWT.CURSOR_ARROW);
			cancelButton.setCursor(fArrowCursor);
	
			boolean hasProgressMonitor= fProgressMonitorPart != null;
	
			// Deactivate shell
			savedState= saveUIState(hasProgressMonitor && cancelable);
			if (focusControl != null)
				savedState.put("focus", focusControl); //$NON-NLS-1$
			
			if (hasProgressMonitor) {	
				fProgressMonitorPart.attachToCancelComponent(cancelButton);
				fStatusContainer.showPage(fProgressMonitorPart);
			}
			// Update the status container since we are blocking the event loop right now.
			fStatusContainer.update();
		}
		return savedState;
	}
	
	private Map saveUIState(boolean keepCancelEnabled) {
		Map savedState= new HashMap(10);
		saveEnableStateAndSet(getButton(PREVIEW_ID), savedState, "preview", false); //$NON-NLS-1$
		saveEnableStateAndSet(getButton(IDialogConstants.OK_ID), savedState, "ok", false); //$NON-NLS-1$
		saveEnableStateAndSet(getButton(IDialogConstants.CANCEL_ID), savedState, "cancel", keepCancelEnabled); //$NON-NLS-1$
		savedState.put("page", ControlEnableState.disable(fVisiblePage.getControl())); //$NON-NLS-1$
		return savedState;
	}
	
	private void saveEnableStateAndSet(Control w, Map h, String key, boolean enabled) {
		if (w != null) {
			h.put(key, new Boolean(w.getEnabled()));
			w.setEnabled(enabled);
		}
	}
	
	private void setDisplayCursor(Display d, Cursor c) {
		Shell[] shells= d.getShells();
		for (int i= 0; i < shells.length; i++)
			shells[i].setCursor(c);
	}	

	private void stopped(Object savedState) {
		Shell shell= getShell();
		if (shell != null) {
			Button cancelButton= getButton(IDialogConstants.CANCEL_ID);
			
			if (fProgressMonitorPart != null)
				fProgressMonitorPart.removeFromCancelComponent(cancelButton);
				
			fStatusContainer.showPage(fMessageBox);
			Map state = (Map)savedState;
			restoreUIState(state);
	
			setDisplayCursor(shell.getDisplay(), null);	
			cancelButton.setCursor(null);
			fWaitCursor.dispose();
			fWaitCursor = null;
			fArrowCursor.dispose();
			fArrowCursor = null;
			Control focusControl = (Control)state.get("focus"); //$NON-NLS-1$
			if (focusControl != null)
				focusControl.setFocus();
		}
	}
	
	private void restoreUIState(Map state) {
		restoreEnableState(getButton(PREVIEW_ID), state, "preview");//$NON-NLS-1$
		restoreEnableState(getButton(IDialogConstants.OK_ID), state, "ok");//$NON-NLS-1$
		restoreEnableState(getButton(IDialogConstants.CANCEL_ID), state, "cancel");//$NON-NLS-1$
		ControlEnableState pageState = (ControlEnableState) state.get("page");//$NON-NLS-1$
		pageState.restore();
	}
	
	private void restoreEnableState(Control w, Map h, String key) {
		if (w != null) {
			Boolean b = (Boolean) h.get(key);
			if (b != null)
				w.setEnabled(b.booleanValue());
		}
	}
	
	//---- Dialog -----------------------------------------------------------
	
	public boolean close() {
		fWizard.dispose();
		return super.close();
	}

	protected void cancelPressed() {
		if (fActiveRunningOperations == 0)	{
			if (fWizard.performCancel())	
				super.cancelPressed();
		}
	}

	protected void okPressed() {
		IWizardPage current= fCurrentPage;
		if (fWizard.performFinish()) {
			saveSize();
			super.okPressed();
			return;
		}
		if (fCurrentPage == current)
			return;
		Assert.isTrue(ErrorWizardPage.PAGE_NAME.equals(fCurrentPage.getName()));
		if (showErrorDialog((ErrorWizardPage)fCurrentPage)) {
			if (fWizard.performFinish()) {
				super.okPressed();
				return;
			}
		}
		fCurrentPage= current;
	}
	
	private boolean isPreviewPageActive() {
		return IPreviewWizardPage.PAGE_NAME.equals(fCurrentPage.getName());
	}
	
	private void previewPressed() {
		IWizardPage current= fCurrentPage;
		fCurrentPage= fCurrentPage.getNextPage();
		if (current == fCurrentPage)
			return;
		String pageName= fCurrentPage.getName();
		if (ErrorWizardPage.PAGE_NAME.equals(pageName)) {
			if (showErrorDialog((ErrorWizardPage)fCurrentPage)) {
				fCurrentPage= fCurrentPage.getNextPage();
				pageName= fCurrentPage.getName();
			} else {
				return;
			}
		}
		if (IPreviewWizardPage.PAGE_NAME.equals(pageName)) {
			fCurrentPage.createControl(fPageContainer);
			makeVisible(fCurrentPage);
			updateButtons();
			if (((PreviewWizardPage)fCurrentPage).hasChanges())
				resize();
			else
				getButton(IDialogConstants.OK_ID).setEnabled(false);
		} else {
			fCurrentPage= current;
		}
	}
	
	private boolean showErrorDialog(ErrorWizardPage page) {
		RefactoringStatusDialog dialog= new RefactoringStatusDialog(getShell(), page, true);
		switch (dialog.open()) {
			case IDialogConstants.OK_ID:
				return true;
			case IDialogConstants.BACK_ID:
				fCurrentPage= fCurrentPage.getPreviousPage();
				break;
			case IDialogConstants.CANCEL_ID:
				super.cancelPressed();
		}
		return false;
	}
	
	private void resize() {
		Control control= fPageContainer.getTopPage();
		Point size= control.getSize();
		int dw= Math.max(0, fPreviewWidth - size.x);
		int dh= Math.max(0, fPreviewHeight - size.y);
		int dx = dw / 2;
		int dy= dh / 2;
		Shell shell= getShell();
		Rectangle rect= shell.getBounds();
		Rectangle display= shell.getDisplay().getClientArea();
		rect.x= Math.max(0, rect.x - dx);
		rect.y= Math.max(0, rect.y - dy);
		rect.width= Math.min(rect.width + dw, display.width);
		rect.height= Math.min(rect.height + dh, display.height);
		int xe= rect.x + rect.width;
		if (xe > display.width) {
			rect.x-= xe - display.width; 
		}
		int ye= rect.y + rect.height;
		if (ye > display.height) {
			rect.y-= ye - display.height; 
		}
		shell.setBounds(rect);
	}
	
	//---- UI construction ---------------------------------------------------
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(fWizard.getDefaultPageTitle());
	}
	
	protected Control createContents(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0; layout.marginWidth= 0;
		layout.verticalSpacing= 0; layout.horizontalSpacing= 0;
		result.setLayout(layout);
		result.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// initialize the dialog units
		initializeDialogUnits(result);
	
		fPageContainer= new PageBook(result, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_BOTH);
		fPageContainer.setLayoutData(gd);
		fCurrentPage= fWizard.getStartingPage();
		dialogArea= fPageContainer;
		if (fCurrentPage instanceof PreviewWizardPage) {
			gd.widthHint= fPreviewWidth;
			gd.heightHint= fPreviewHeight;
		}
		
		fStatusContainer= new PageBook(result, SWT.NONE);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(fWizard.getMessageLineWidthInChars());
		fStatusContainer.setLayoutData(gd);
		if (fWizard.needsProgressMonitor())
			createProgressMonitorPart();
		createMessageBox();
		fStatusContainer.showPage(fMessageBox);
		
		buttonBar= createButtonBar(result);
		
		fCurrentPage.createControl(fPageContainer);
		makeVisible(fCurrentPage);
				
		updateMessage();
		updateButtons();
		applyDialogFont(result);		
		return result;
	}
	
	private void createProgressMonitorPart() {
		// Insert a progress monitor 
		GridLayout pmlayout= new GridLayout();
		pmlayout.numColumns= 1;
		pmlayout.marginHeight= 0;
		fProgressMonitorPart= new ProgressMonitorPart(fStatusContainer, pmlayout);
	}
	
	private void createMessageBox() {
		fMessageBox= new MessageBox(fStatusContainer, SWT.NONE);
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		if (! (fCurrentPage instanceof PreviewWizardPage) && fWizard.hasPreviewPage()) {
			Button preview= createButton(parent, PREVIEW_ID, RefactoringMessages.getString("RefactoringWizardDialog2.buttons.preview.label"), false); //$NON-NLS-1$
			preview.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					previewPressed();
				}
			});
		}
		
		String OK_LABEL= IDialogConstants.OK_LABEL;
		String CANCEL_LABEL= IDialogConstants.CANCEL_LABEL;
		if (fWizard.yesNoStyle()) {
			OK_LABEL= IDialogConstants.YES_LABEL;
			CANCEL_LABEL= IDialogConstants.NO_LABEL;
		}
		createButton(
			parent,
			IDialogConstants.OK_ID,
			OK_LABEL,
			true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			CANCEL_LABEL,
			false);
		Button okButton= getButton(IDialogConstants.OK_ID);
		okButton.setFocus();
	}
	
	private void makeVisible(IWizardPage page) {
		if (fVisiblePage == page)
			return;
		if (fVisiblePage != null)	
			fVisiblePage.setVisible(false);
		fVisiblePage= page;
		fPageContainer.showPage(page.getControl());
		fVisiblePage.setVisible(true);
	}	
}
