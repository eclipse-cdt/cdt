/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
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
    protected Button indexIncludePaths;
	protected Button browseButton;
	protected Text cTagsFile;
	
	protected boolean useDefaultCTags = true;
    protected boolean useSpecifiedCTagsExecutable = false;
	protected Button useCTagsPath;
	protected Button useCTagsExecutable;
	protected Button browseButtonCTagsExec;
	protected Text cTagsExecutable;
	
	public final static String PREF_INTOREXT_CTAGS = CUIPlugin.PLUGIN_ID + ".intorextctags"; //$NON-NLS-1$
	public final static String PREF_CTAGS_FILE_LOCATION_CTAGS = CUIPlugin.PLUGIN_ID + ".ctagsfilelocation"; //$NON-NLS-1$
	public final static String PREF_CTAGS_INDEXINCLUDEFILES = CUIPlugin.PLUGIN_ID + ".ctagsindexincludes"; //$NON-NLS-1$
	public final static String PREF_CTAGS_LOCATION_TYPE = CUIPlugin.PLUGIN_ID + ".ctagslocationtype"; //$NON-NLS-1$
	public final static String PREF_CTAGS_LOCATION = CUIPlugin.PLUGIN_ID + ".ctagslocation"; //$NON-NLS-1$
	
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
			String cTagsFileLocation = ""; //$NON-NLS-1$
			if (!internalTagsFile)
				cTagsFileLocation = cTagsFile.getText();
			
			String indexIncludeFiles = new Boolean(indexIncludePaths.getSelection()).toString();
			
			String cTagsLocationType = useDefaultCTags ? CTagsIndexer.CTAGS_PATH_DEFAULT : CTagsIndexer.CTAGS_PATH_SPECIFIED;
			String cTagsLocation = ""; //$NON-NLS-1$
			if (!useDefaultCTags)
				cTagsLocation=cTagsExecutable.getText();
				
			//if external has been chosen, ensure that there is a cTagsFileLocation selected; otherwise default
			//to internal file
			if (internalExternalCTagsString.equals(CTagsIndexer.CTAGS_EXTERNAL) && cTagsFileLocation.equals("")) //$NON-NLS-1$
				internalExternalCTagsString=CTagsIndexer.CTAGS_INTERNAL;
			
			//if an external CPaths has been selected but no path has been provided, switch back to default setting
			if (cTagsLocationType.equals(CTagsIndexer.CTAGS_PATH_SPECIFIED) && cTagsLocation.equals("")) //$NON-NLS-1$
				cTagsLocationType=CTagsIndexer.CTAGS_PATH_DEFAULT;
			
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
						String orig = cext[i].getExtensionData("ctagfiletype"); //$NON-NLS-1$
						if (orig == null || !orig.equals(internalExternalCTagsString)) {
							cext[i].setExtensionData("ctagfiletype", internalExternalCTagsString); //$NON-NLS-1$
						}
						orig = cext[i].getExtensionData("ctagfilelocation"); //$NON-NLS-1$
						if (orig == null || !orig.equals(cTagsFileLocation)) {
							cext[i].setExtensionData("ctagfilelocation", cTagsFileLocation); //$NON-NLS-1$
						}
						orig = cext[i].getExtensionData("ctagsindexincludes"); //$NON-NLS-1$
						if (orig == null || !orig.equals(indexIncludeFiles)) {
							cext[i].setExtensionData("ctagsindexincludes", indexIncludeFiles); //$NON-NLS-1$
						}
						orig = cext[i].getExtensionData("ctagslocationtype"); //$NON-NLS-1$
						if (orig == null || !orig.equals(cTagsLocationType)) {
							cext[i].setExtensionData("ctagslocationtype", cTagsLocationType); //$NON-NLS-1$
						}
						orig = cext[i].getExtensionData("ctagslocation"); //$NON-NLS-1$
						if (orig == null || !orig.equals(cTagsLocation)) {
							cext[i].setExtensionData("ctagslocation", cTagsLocation); //$NON-NLS-1$
						}
					}
				}
			} else {
				if (prefStore != null) {
					prefStore.setValue(PREF_INTOREXT_CTAGS, internalExternalCTagsString);
					prefStore.setValue(PREF_CTAGS_FILE_LOCATION_CTAGS,cTagsFileLocation);
					prefStore.setValue(PREF_CTAGS_INDEXINCLUDEFILES,indexIncludeFiles);
					prefStore.setValue(PREF_CTAGS_LOCATION_TYPE,cTagsLocationType);
					prefStore.setValue(PREF_CTAGS_LOCATION,cTagsLocation);
				}
			}
		}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
     */
    public void performDefaults() {
    	//ctag file options
		internalTagsFile=true;
		externalTagsFile=false;
		internalCTagsFile.setSelection(true);
		externalCTagsFile.setSelection(false);
		cTagsFile.setText(""); //$NON-NLS-1$
		browseButton.setEnabled(false);
		//ctag path options
		useDefaultCTags=true;
		useSpecifiedCTagsExecutable=false;
		useCTagsPath.setSelection(true);
		useCTagsExecutable.setSelection(false);
		cTagsExecutable.setText(""); //$NON-NLS-1$
		browseButtonCTagsExec.setEnabled(false);
		//index include paths
		indexIncludePaths.setSelection(false);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite page = ControlFactory.createComposite(parent, 1);	
		
        Group cTagsExecutableGroup = ControlFactory.createGroup(page,CUIMessages.getString("CTagsIndexerBlock.ctagsLocation"),3); //$NON-NLS-1$
        GridData gd3 = (GridData) cTagsExecutableGroup.getLayoutData();
        gd3.grabExcessHorizontalSpace = true;
        gd3.horizontalAlignment = GridData.FILL;
        
    	SelectionListener cTagsListener =  new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {				
				useDefaultCTags  = useCTagsPath.getSelection();
				useSpecifiedCTagsExecutable = useCTagsExecutable.getSelection();
				
				if (useDefaultCTags){
					setButtonState(CTagsIndexer.CTAGS_PATH_DEFAULT);
				}
				if (useSpecifiedCTagsExecutable){
					setButtonState(CTagsIndexer.CTAGS_PATH_SPECIFIED);
				}
			}
		};
		
        useCTagsPath = ControlFactory.createRadioButton(cTagsExecutableGroup,CUIMessages.getString("CTagsIndexerBlock.radioButtonCTagsDefault"),"CTagsDefault",cTagsListener);//$NON-NLS-1$ //$NON-NLS-2$
		((GridData)useCTagsPath.getLayoutData()).horizontalSpan = 3;
		((GridData)useCTagsPath.getLayoutData()).grabExcessHorizontalSpace = true;
		useCTagsPath.setSelection(internalTagsFile);
		
		useCTagsExecutable = ControlFactory.createRadioButton(cTagsExecutableGroup,CUIMessages.getString("CTagsIndexerBlock.radioButtonCTagsSpecified"),"CTafsSpecified",cTagsListener);//$NON-NLS-1$ //$NON-NLS-2$
		((GridData)useCTagsExecutable.getLayoutData()).horizontalSpan = 3;
		((GridData)useCTagsExecutable.getLayoutData()).grabExcessHorizontalSpace = true;
		
		cTagsExecutable = ControlFactory.createTextField(cTagsExecutableGroup);
		((GridData)cTagsExecutable.getLayoutData()).horizontalSpan = 2;
		((GridData)cTagsExecutable.getLayoutData()).grabExcessHorizontalSpace = true;;
		
		browseButtonCTagsExec = ControlFactory.createPushButton(cTagsExecutableGroup,CUIMessages.getString("CTagsIndexerBlock.browseButton")); //$NON-NLS-1$
		((GridData)browseButtonCTagsExec.getLayoutData()).widthHint = SWTUtil.getButtonWidthHint(browseButtonCTagsExec);
		browseButtonCTagsExec.setEnabled(false);
		browseButtonCTagsExec.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {
				handleBrowseButtonSelected();
            }

            void handleBrowseButtonSelected() {
				cTagsExecutable.setText(openFileBrowser());
            }
        });
        //
		
        Group includeGroup = ControlFactory.createGroup(page,CUIMessages.getString("CTagsIndexerBlock.includeGroup"),1); //$NON-NLS-1$
        GridData gd2 = (GridData) includeGroup.getLayoutData();
        gd2.grabExcessHorizontalSpace = true;
        gd2.horizontalAlignment = GridData.FILL;
       
        indexIncludePaths = ControlFactory.createCheckBox(includeGroup,CUIMessages.getString("CTagsIndexerBlock.indexIncludes"));//$NON-NLS-1$ //$NON-NLS-2$
		((GridData)indexIncludePaths.getLayoutData()).horizontalSpan =1;
		((GridData)indexIncludePaths.getLayoutData()).grabExcessHorizontalSpace = true;
    
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
				cTagsFile.setText(openFileBrowser());
            }
        });
		
		setControl(page);
    }
    
    String openFileBrowser(){
    	FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
        dialog.setText(CUIMessages.getString("CTagsIndexerBlock.fileBrowser"));  //$NON-NLS-1$
		String fileName = dialog.open();
        if (fileName == null) {
            return ""; //$NON-NLS-1$
        }
        
        return fileName;
    }
    
	public void loadPersistedValues(IProject project) throws CoreException {
		
		ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
		ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
		if (cext.length > 0) {
			for (int i = 0; i < cext.length; i++) {
				String orig = cext[i].getExtensionData("ctagfiletype"); //$NON-NLS-1$
				if (orig != null){
					setButtonState(orig);
				}

				orig = cext[i].getExtensionData("ctagfilelocation"); //$NON-NLS-1$
				if (orig != null){
					cTagsFile.setText(orig);
				}
				
				orig = cext[i].getExtensionData("ctagsindexincludes"); //$NON-NLS-1$
				if (orig != null){
					if (new Boolean(orig).booleanValue()){
						indexIncludePaths.setSelection(true);
					} else {
						indexIncludePaths.setSelection(false);
					}
				}
				
				orig = cext[i].getExtensionData("ctagslocationtype"); //$NON-NLS-1$
				if (orig != null){
					setButtonState(orig);
				}
				
				orig = cext[i].getExtensionData("ctagslocation"); //$NON-NLS-1$
				if (orig != null){
					cTagsExecutable.setText(orig);
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
		} else if(orig.equals(CTagsIndexer.CTAGS_PATH_DEFAULT)){
			useDefaultCTags=true;
			useSpecifiedCTagsExecutable=false;
			useCTagsPath.setSelection(true);
			useCTagsExecutable.setSelection(false);
			browseButtonCTagsExec.setEnabled(false);
		} else if(orig.equals(CTagsIndexer.CTAGS_PATH_SPECIFIED)){
			useDefaultCTags=false;
			useSpecifiedCTagsExecutable=true;
			useCTagsPath.setSelection(false);
			useCTagsExecutable.setSelection(true);
			browseButtonCTagsExec.setEnabled(true);
		}
	}

	public void loadPreferences() {
		String indexerId=prefStore.getString(PREF_INTOREXT_CTAGS);
		if (!indexerId.equals("")) { //$NON-NLS-1$
		  setButtonState(indexerId);
		}
		
		indexerId=prefStore.getString(PREF_CTAGS_FILE_LOCATION_CTAGS);
		if (!indexerId.equals("")) { //$NON-NLS-1$
			cTagsFile.setText(indexerId);
		}
	}

	public void removePreferences() {
		prefStore.setToDefault(PREF_CTAGS_FILE_LOCATION_CTAGS);
		prefStore.setToDefault(PREF_INTOREXT_CTAGS);
	}

}
