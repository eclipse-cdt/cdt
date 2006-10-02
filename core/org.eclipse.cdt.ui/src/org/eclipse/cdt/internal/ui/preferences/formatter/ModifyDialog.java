/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.cdt.internal.ui.util.Messages;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.Profile;

public class ModifyDialog extends StatusDialog {
    
    /**
     * The keys to retrieve the preferred area from the dialog settings.
     */
    private static final String DS_KEY_PREFERRED_WIDTH= CUIPlugin.PLUGIN_ID + "formatter_page.modify_dialog.preferred_width"; //$NON-NLS-1$
    private static final String DS_KEY_PREFERRED_HEIGHT= CUIPlugin.PLUGIN_ID + "formatter_page.modify_dialog.preferred_height"; //$NON-NLS-1$
    private static final String DS_KEY_PREFERRED_X= CUIPlugin.PLUGIN_ID + "formatter_page.modify_dialog.preferred_x"; //$NON-NLS-1$
    private static final String DS_KEY_PREFERRED_Y= CUIPlugin.PLUGIN_ID + "formatter_page.modify_dialog.preferred_y"; //$NON-NLS-1$
    
    /**
     * The key to store the number (beginning at 0) of the tab page which had the 
     * focus last time.
     */
    private static final String DS_KEY_LAST_FOCUS= CUIPlugin.PLUGIN_ID + "formatter_page.modify_dialog.last_focus"; //$NON-NLS-1$ 

	private final String fTitle;
	
	private final boolean fNewProfile;

	private Profile fProfile;
	private final Map fWorkingValues;
	
	private IStatus fStandardStatus;
	
	protected final List fTabPages;
	
	final IDialogSettings fDialogSettings;
//	private TabFolder fTabFolder;
	private ProfileManager fProfileManager;
//	private Button fApplyButton;
		
	protected ModifyDialog(Shell parentShell, Profile profile, ProfileManager profileManager, boolean newProfile) {
		super(parentShell);
		fProfileManager= profileManager;
		fNewProfile= newProfile;
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX );
				
		fProfile= profile;
		if (fProfile.isBuiltInProfile()) {
		    fStandardStatus= new Status(IStatus.INFO, CUIPlugin.getPluginId(), IStatus.OK, FormatterMessages.ModifyDialog_dialog_show_warning_builtin, null); 
		    fTitle= Messages.format(FormatterMessages.ModifyDialog_dialog_show_title, profile.getName()); 
		} else {
		    fStandardStatus= new Status(IStatus.OK, CUIPlugin.getPluginId(), IStatus.OK, "", null); //$NON-NLS-1$
		    fTitle= Messages.format(FormatterMessages.ModifyDialog_dialog_title, profile.getName()); 
		}
		fWorkingValues= new HashMap(fProfile.getSettings());
		updateStatus(fStandardStatus);
		setStatusLineAboveButtons(false);
		fTabPages= new ArrayList();
		fDialogSettings= CUIPlugin.getDefault().getDialogSettings();	
	}
	
	public void create() {
		super.create();
		int lastFocusNr= 0;
		try {
			lastFocusNr= fDialogSettings.getInt(DS_KEY_LAST_FOCUS);
			if (lastFocusNr < 0) lastFocusNr= 0;
			if (lastFocusNr > fTabPages.size() - 1) lastFocusNr= fTabPages.size() - 1;
		} catch (NumberFormatException x) {
			lastFocusNr= 0;
		}
		
		if (!fNewProfile) {
//			fTabFolder.setSelection(lastFocusNr);
//			((ModifyDialogTabPage)fTabFolder.getSelection()[0].getData()).setInitialFocus();
		}
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(fTitle);
	}

	protected Control createDialogArea(Composite parent) {
		
		final Composite composite= (Composite)super.createDialogArea(parent);

		ModifyDialogTabPage tabPage = new IndentationTabPage(this, fWorkingValues);
		tabPage.createContents(composite);
//		fTabFolder = new TabFolder(composite, SWT.NONE);
//		fTabFolder.setFont(composite.getFont());
//		fTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
//
//		addTabPage(fTabFolder, FormatterMessages.ModifyDialog_tabpage_indentation_title, new IndentationTabPage(this, fWorkingValues)); 
//		addTabPage(fTabFolder, FormatterMessages.ModifyDialog_tabpage_braces_title, new BracesTabPage(this, fWorkingValues)); 
//		addTabPage(fTabFolder, FormatterMessages.ModifyDialog_tabpage_whitespace_title, new WhiteSpaceTabPage(this, fWorkingValues)); 
//		addTabPage(fTabFolder, FormatterMessages.ModifyDialog_tabpage_blank_lines_title, new BlankLinesTabPage(this, fWorkingValues)); 
//		addTabPage(fTabFolder, FormatterMessages.ModifyDialog_tabpage_new_lines_title, new NewLinesTabPage(this, fWorkingValues)); 
//		addTabPage(fTabFolder, FormatterMessages.ModifyDialog_tabpage_control_statements_title, new ControlStatementsTabPage(this, fWorkingValues)); 
//		addTabPage(fTabFolder, FormatterMessages.ModifyDialog_tabpage_line_wrapping_title, new LineWrappingTabPage(this, fWorkingValues)); 
//		addTabPage(fTabFolder, FormatterMessages.ModifyDialog_tabpage_comments_title, new CommentsTabPage(this, fWorkingValues)); 
		
		applyDialogFont(composite);
		
//		fTabFolder.addSelectionListener(new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {}
//			public void widgetSelected(SelectionEvent e) {
//				final TabItem tabItem= (TabItem)e.item;
//				final ModifyDialogTabPage page= (ModifyDialogTabPage)tabItem.getData();
////				page.fSashForm.setWeights();
//				fDialogSettings.put(DS_KEY_LAST_FOCUS, fTabPages.indexOf(page));
//				page.makeVisible();
//			}
//		});
		return composite;
	}
	
	public void updateStatus(IStatus status) {
	    super.updateStatus(status != null ? status : fStandardStatus);    
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
    	Point initialSize= super.getInitialSize();
        try {
        	int lastWidth= fDialogSettings.getInt(DS_KEY_PREFERRED_WIDTH);
        	if (initialSize.x > lastWidth)
        		lastWidth= initialSize.x;
        	int lastHeight= fDialogSettings.getInt(DS_KEY_PREFERRED_HEIGHT);
        	if (initialSize.y > lastHeight)
        		lastHeight= initialSize.x;
       		return new Point(lastWidth, lastHeight);
        } catch (NumberFormatException ex) {
        }
        return initialSize;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
        try {
        	return new Point(fDialogSettings.getInt(DS_KEY_PREFERRED_X), fDialogSettings.getInt(DS_KEY_PREFERRED_Y));
        } catch (NumberFormatException ex) {
        	return super.getInitialLocation(initialSize);
        }
	}
	    
	public boolean close() {
		final Rectangle shell= getShell().getBounds();
		
		fDialogSettings.put(DS_KEY_PREFERRED_WIDTH, shell.width);
		fDialogSettings.put(DS_KEY_PREFERRED_HEIGHT, shell.height);
		fDialogSettings.put(DS_KEY_PREFERRED_X, shell.x);
		fDialogSettings.put(DS_KEY_PREFERRED_Y, shell.y);
		
		return super.close();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		applyPressed();
		super.okPressed();
	}
	
    protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CLIENT_ID) {
			applyPressed();
		} else {
			super.buttonPressed(buttonId);
		}
    }
	
	private void applyPressed() {
		 if (fProfile.isBuiltInProfile() || fProfile.isSharedProfile()) {
		 	RenameProfileDialog dialog= new RenameProfileDialog(getShell(), fProfile, fProfileManager);
		 	if (dialog.open() != Window.OK) {
		 		return;
		 	}

			fProfile= dialog.getRenamedProfile();
			
		    fStandardStatus= new Status(IStatus.OK, CUIPlugin.getPluginId(), IStatus.OK, "", null); //$NON-NLS-1$
			updateStatus(fStandardStatus);
		 }
		 fProfile.setSettings(new HashMap(fWorkingValues));
//		 fApplyButton.setEnabled(false);
	}
    
    protected void createButtonsForButtonBar(Composite parent) {
//	    fApplyButton= createButton(parent, IDialogConstants.CLIENT_ID, FormatterMessages.ModifyDialog_apply_button, false); 
//		fApplyButton.setEnabled(false);
		
		GridLayout layout= (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth= false;
		Label label= new Label(parent, SWT.NONE);
		GridData data= new GridData();
		data.widthHint= layout.horizontalSpacing;
		label.setLayoutData(data);
		super.createButtonsForButtonBar(parent);
    }
    
	
//	private final void addTabPage(TabFolder tabFolder, String title, ModifyDialogTabPage tabPage) {
//		final TabItem tabItem= new TabItem(tabFolder, SWT.NONE);
//		applyDialogFont(tabItem.getControl());
//		tabItem.setText(title);
//		tabItem.setData(tabPage);
//		tabItem.setControl(tabPage.createContents(tabFolder));
//		fTabPages.add(tabPage);
//	}

	public void valuesModified() {
//		if (fApplyButton != null && !fApplyButton.isDisposed()) {
//			fApplyButton.setEnabled(hasChanges());
//		}
	}

//	private boolean hasChanges() {
//		Iterator iter= fProfile.getSettings().entrySet().iterator();
//		for (;iter.hasNext();) {
//			Map.Entry curr= (Map.Entry) iter.next();
//			if (!fWorkingValues.get(curr.getKey()).equals(curr.getValue())) {
//				return true;
//			}
//		}
//		return false;
//	}
}
