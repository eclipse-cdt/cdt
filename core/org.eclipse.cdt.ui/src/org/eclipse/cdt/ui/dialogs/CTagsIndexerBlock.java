/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.internal.core.index.ctagsindexer.CTagsIndexer;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsIndexerBlock extends AbstractIndexerPage {
	
	protected boolean internalTagsFile = true;
    protected boolean externalTagsFile = false;
    protected Button internalCTagsFile;
    protected Button externalCTagsFile;
	protected Button browseButton;
	protected Text cTagsFile;
	
	private String storedInternalExternal;
	private String storedTagFile;
	
	public final static String PREF_INTOREXT_CTAGS = CUIPlugin.PLUGIN_ID + ".intorextctags"; //$NON-NLS-1$
	public final static String PREF_CTAGSLOCATION_CTAGS = CUIPlugin.PLUGIN_ID + ".ctagslocation"; //$NON-NLS-1$
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.index.AbstractIndexerPage#initialize(org.eclipse.core.resources.IProject)
     */
    public void initialize(IProject project) {
		
		this.currentProject = project;
		try {
			loadPersistedValues(project);
		} catch (CoreException e) {}
		
    }

	public void performApply(IProgressMonitor monitor) throws CoreException {
		
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}

			monitor.beginTask(CUIMessages.getString("IndexerOptiosn.task.savingAttributes "), 1);  //$NON-NLS-1$
			ICOptionContainer container = getContainer();
			IProject proj = null;
	        String internalExternalCTagsString = internalTagsFile ? CTagsIndexer.CTAGS_INTERNAL  : CTagsIndexer.CTAGS_EXTERNAL;
			String cTagsFileLocation = cTagsFile.getText();

			//if external has been chosen, ensure that there is a cTagsFileLocation selected; otherwise default
			//to internal file
			if (internalExternalCTagsString.equals(CTagsIndexer.CTAGS_EXTERNAL) && cTagsFileLocation.equals("")) //$NON-NLS-1$
				internalExternalCTagsString=CTagsIndexer.CTAGS_INTERNAL;
			
			if (container != null){
				proj = container.getProject();
			}
			else{
				proj = currentProject;
			}
			
			if (proj != null) {
				ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(proj, false);
				ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
				if (cext.length > 0) {
					for (int i = 0; i < cext.length; i++) {
						String id = cext[i].getID();
						String orig = cext[i].getExtensionData("ctagfiletype"); //$NON-NLS-1$
						if (orig == null || !orig.equals(internalExternalCTagsString)) {
							cext[i].setExtensionData("ctagfiletype", internalExternalCTagsString); //$NON-NLS-1$
						}
						orig = cext[i].getExtensionData("ctagfilelocation"); //$NON-NLS-1$
						if (orig == null || !orig.equals(cTagsFileLocation)) {
							cext[i].setExtensionData("ctagfilelocation", cTagsFileLocation); //$NON-NLS-1$
						}
					}
				}
			} else {
				if (prefStore != null) {
					prefStore.setValue(PREF_INTOREXT_CTAGS, internalExternalCTagsString);
					prefStore.setValue(PREF_CTAGSLOCATION_CTAGS,cTagsFileLocation);
				}
			}
		}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
     */
    public void performDefaults() {
		internalTagsFile=true;
		externalTagsFile=false;
		internalCTagsFile.setSelection(true);
		externalCTagsFile.setSelection(false);
		cTagsFile.setText(""); //$NON-NLS-1$
		browseButton.setEnabled(false);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite page = ControlFactory.createComposite(parent, 1);	
		
		Group group = ControlFactory.createGroup(page, CUIMessages.getString("CTagsIndexerBlock.blockName"),3); //$NON-NLS-1$
        
        GridData gd = (GridData) group.getLayoutData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;

		
		SelectionListener cListener =  new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {				
				internalTagsFile = internalCTagsFile.getSelection();
				externalTagsFile = externalCTagsFile.getSelection();
				
				if (externalTagsFile){
					setButtonState(CTagsIndexer.CTAGS_EXTERNAL);
				}
				if (internalTagsFile){
					setButtonState(CTagsIndexer.CTAGS_INTERNAL);
				}
			}
		};
		
		internalCTagsFile = ControlFactory.createRadioButton(group,CUIMessages.getString("CTagsIndexerBlock.radioButtonInternal"),"Internal",cListener);//$NON-NLS-1$ //$NON-NLS-2$
		((GridData)internalCTagsFile.getLayoutData()).horizontalSpan = 3;
		((GridData)internalCTagsFile.getLayoutData()).grabExcessHorizontalSpace = true;
		internalCTagsFile.setSelection(internalTagsFile);
		
		externalCTagsFile = ControlFactory.createRadioButton(group,CUIMessages.getString("CTagsIndexerBlock.radioButtonExternal"),"External",cListener);//$NON-NLS-1$ //$NON-NLS-2$
		((GridData)externalCTagsFile.getLayoutData()).horizontalSpan = 3;
		((GridData)externalCTagsFile.getLayoutData()).grabExcessHorizontalSpace = true;
		
		cTagsFile = ControlFactory.createTextField(group);
		((GridData)cTagsFile.getLayoutData()).horizontalSpan = 2;
		((GridData)cTagsFile.getLayoutData()).grabExcessHorizontalSpace = true;
		
		browseButton = ControlFactory.createPushButton(group,CUIMessages.getString("CTagsIndexerBlock.browseButton")); //$NON-NLS-1$
		((GridData)browseButton.getLayoutData()).widthHint = SWTUtil.getButtonWidthHint(browseButton);
		browseButton.setEnabled(false);
		browseButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
				handleBrowseButtonSelected();
            }

            private void handleBrowseButtonSelected() {
                FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
                dialog.setText(CUIMessages.getString("CTagsIndexerBlock.fileBrowser"));  //$NON-NLS-1$
				String fileName = dialog.open();
                if (fileName == null) {
                    return;
                }
				cTagsFile.setText(fileName);
            }
        });
		
		setControl(page);
    }
    
	public void loadPersistedValues(IProject project) throws CoreException {
		
		ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
		ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
		if (cext.length > 0) {
			for (int i = 0; i < cext.length; i++) {
				String id = cext[i].getID();
				String orig = cext[i].getExtensionData("ctagfiletype"); //$NON-NLS-1$
				if (orig != null){
					storedInternalExternal=orig;
					setButtonState(orig);
				}

				orig = cext[i].getExtensionData("ctagfilelocation"); //$NON-NLS-1$
				if (orig != null){
					storedTagFile=orig;
					cTagsFile.setText(orig);
				}
				
			}
		}
	
	}
	
	private void setButtonState(String orig){
		if (orig.equals(CTagsIndexer.CTAGS_INTERNAL)){ 
			internalTagsFile=true;
			externalTagsFile=false;
			internalCTagsFile.setSelection(true);
			externalCTagsFile.setSelection(false);
			browseButton.setEnabled(false);
		} else if (orig.equals(CTagsIndexer.CTAGS_EXTERNAL)){
			externalTagsFile=true;
			internalTagsFile=false;
			externalCTagsFile.setSelection(true);
			internalCTagsFile.setSelection(false);
			browseButton.setEnabled(true);
		}
	}

	public void loadPreferences() {
		String indexerId=prefStore.getString(PREF_INTOREXT_CTAGS);
		if (!indexerId.equals("")) { //$NON-NLS-1$
		  setButtonState(indexerId);
		}
		
		indexerId=prefStore.getString(PREF_CTAGSLOCATION_CTAGS);
		if (!indexerId.equals("")) { //$NON-NLS-1$
			storedTagFile=indexerId;
			cTagsFile.setText(indexerId);
		}
	}

	public void removePreferences() {
		prefStore.setToDefault(PREF_CTAGSLOCATION_CTAGS);
		prefStore.setToDefault(PREF_INTOREXT_CTAGS);
	}

}
