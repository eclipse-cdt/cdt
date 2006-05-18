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
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.indexer.ctags.CtagsIndexer;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
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
	
	protected CtagsIndexer ctagsIndexer;
	
	protected boolean internalTagsFile = true;
    protected Button internalCTagsFile;
    protected Button externalCTagsFile;
	protected Button browseButton;
	protected Text cTagsFile;
	
	protected boolean useDefaultCTags = true;
	protected Button useCTagsPath;
	protected Button useCTagsExecutable;
	protected Button browseButtonCTagsExec;
	protected Text cTagsExecutable;
	
	public final static String PREF_INTOREXT_CTAGS = CUIPlugin.PLUGIN_ID + ".intorextctags"; //$NON-NLS-1$
	public final static String PREF_CTAGS_FILE_LOCATION_CTAGS = CUIPlugin.PLUGIN_ID + ".ctagsfilelocation"; //$NON-NLS-1$
	public final static String PREF_CTAGS_INDEXINCLUDEFILES = CUIPlugin.PLUGIN_ID + ".ctagsindexincludes"; //$NON-NLS-1$
	public final static String PREF_CTAGS_LOCATION_TYPE = CUIPlugin.PLUGIN_ID + ".ctagslocationtype"; //$NON-NLS-1$
	public final static String PREF_CTAGS_LOCATION = CUIPlugin.PLUGIN_ID + ".ctagslocation"; //$NON-NLS-1$
	
    public void initialize(ICProject project) {
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
		if (ctagsIndexer != null) {
			ctagsIndexer.setPreferences(
					useDefaultCTags,
					cTagsExecutable.getText(),
					internalTagsFile,
					cTagsFile.getText());
		} else {
			CtagsIndexer.setDefaultPreferences(
					useDefaultCTags,
					cTagsExecutable.getText(),
					internalTagsFile,
					cTagsFile.getText());
		}
	}

	public void performDefaults() {
    	//ctag file options
		internalTagsFile=true;
		internalCTagsFile.setSelection(true);
		externalCTagsFile.setSelection(false);
		cTagsFile.setText(""); //$NON-NLS-1$
		browseButton.setEnabled(false);
		//ctag path options
		useDefaultCTags=true;
		useCTagsPath.setSelection(true);
		useCTagsExecutable.setSelection(false);
		cTagsExecutable.setText(""); //$NON-NLS-1$
		browseButtonCTagsExec.setEnabled(false);
    }

	public void createControl(Composite parent) {
        Composite page = ControlFactory.createComposite(parent, 1);	
		
        Group cTagsExecutableGroup = ControlFactory.createGroup(page,CUIMessages.getString("CTagsIndexerBlock.ctagsLocation"),3); //$NON-NLS-1$
        GridData gd3 = (GridData) cTagsExecutableGroup.getLayoutData();
        gd3.grabExcessHorizontalSpace = true;
        gd3.horizontalAlignment = GridData.FILL;
        
    	SelectionListener cTagsListener =  new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {				
				useDefaultCTags  = useCTagsPath.getSelection();
				if (useDefaultCTags){
					setCommandState();
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
		
		Group group = ControlFactory.createGroup(page, CUIMessages.getString("CTagsIndexerBlock.blockName"),3); //$NON-NLS-1$
        
        GridData gd = (GridData) group.getLayoutData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;

		
		SelectionListener cListener =  new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {				
				internalTagsFile = internalCTagsFile.getSelection();
				if (internalTagsFile){
					setFilenameState();
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
    
	public void loadPersistedValues(ICProject project) throws CoreException {
		IPDOMIndexer indexer = CCorePlugin.getPDOMManager().getIndexer(project);
		if (!(indexer instanceof CtagsIndexer))
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, "Wrong indexer", null));
		ctagsIndexer = (CtagsIndexer)indexer;

		useDefaultCTags = ctagsIndexer.useCtagsOnPath();
		cTagsExecutable.setText(ctagsIndexer.getCtagsCommand());
		setCommandState();
		
		internalTagsFile = ctagsIndexer.useInternalCtagsFile();
		cTagsFile.setText(ctagsIndexer.getCtagsFileName());
		setFilenameState();
	}
	
	private void setCommandState() {
		useCTagsPath.setSelection(useDefaultCTags);
		useCTagsExecutable.setSelection(!useDefaultCTags);
		browseButtonCTagsExec.setEnabled(!useDefaultCTags);
	}
	
	private void setFilenameState() {
		internalCTagsFile.setSelection(internalTagsFile);
		externalCTagsFile.setSelection(!internalTagsFile);
		browseButton.setEnabled(!internalTagsFile);
	}
	
	public void loadPreferences() {
		useDefaultCTags = CtagsIndexer.getDefaultUseCtagsOnPath();
		cTagsExecutable.setText(CtagsIndexer.getDefaultCtagsCommand());
		setCommandState();
		
		internalTagsFile = CtagsIndexer.getDefaultUseInternalCtagsFile();
		cTagsFile.setText(CtagsIndexer.getDefaultCtagsFileName());
		setFilenameState();
	}

	public void removePreferences() {
		prefStore.setToDefault(PREF_CTAGS_FILE_LOCATION_CTAGS);
		prefStore.setToDefault(PREF_INTOREXT_CTAGS);
	}

}
