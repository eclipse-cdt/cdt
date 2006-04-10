/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.resources;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


/**
 * @author mjberger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AddToArchiveForm extends CombineForm 
{

	protected Button savePathInfoCheckBox;
	protected Combo relativeToCombo;
	protected String[] _relativePathList = null;
	protected String relativePath = "";
	protected boolean saveFullPathInfo = false;

	public AddToArchiveForm(
		ISystemMessageLine msgLine,
		Object caller,
		boolean fileMode) 
	{
		super(msgLine, caller, fileMode);
	}

	public AddToArchiveForm(
		ISystemMessageLine msgLine,
		Object caller,
		boolean fileMode,
		boolean prePopSelection) 
	{
		super(msgLine, caller, fileMode, prePopSelection);
	}
	
	public AddToArchiveForm(
	ISystemMessageLine msgLine,
	Object caller,
	boolean fileMode,
	boolean prePopSelection,
	String[] relativePaths) 
{
	super(msgLine, caller, fileMode, prePopSelection);
	_relativePathList = relativePaths;
	enableRelativePathFeature(relativePaths != null);
}

	public void setRelativePathList(String[] relativePathList)
	{
		if (relativePathList != null)
		{
			_relativePathList = relativePathList;
			if (relativeToCombo != null) relativeToCombo.setItems(relativePathList);
			enableRelativePathFeature(true);
		}
		else
		{
			enableRelativePathFeature(false);
		}
	}

	protected void enableRelativePathFeature(boolean enable)
	{
		if (relativeToCombo != null) relativeToCombo.setEnabled(enable);
		if (savePathInfoCheckBox != null) savePathInfoCheckBox.setEnabled(enable);
	}

	/**
	 * In this method, we populate the given SWT container with widgets and return the container
	 *  to the caller. 
	 * @param parent The parent composite
	 */
	public Control createContents(Shell shell, Composite parent)	
	{
		Control control = super.createContents(shell, parent);
		
		Composite composite = SystemWidgetHelpers.createComposite(parent, 1);
	   
	   	savePathInfoCheckBox = SystemWidgetHelpers.createCheckBox(composite, null, FileResources.RESID_ADDTOARCHIVE_SAVEPATH_LABEL, FileResources.RESID_ADDTOARCHIVE_SAVEPATH_TOOLTIP);
	   	relativeToCombo = SystemWidgetHelpers.createLabeledReadonlyCombo(composite, null, FileResources.RESID_ADDTOARCHIVE_RELATIVETO_LABEL, FileResources.RESID_ADDTOARCHIVE_RELATIVETO_LABEL);
	   	
	   	enableRelativePathFeature(_relativePathList != null);
	   	
		if (_relativePathList != null) 
		{
			relativeToCombo.setItems(_relativePathList);
			relativeToCombo.setEnabled(false);
		}
		
		savePathInfoCheckBox.addSelectionListener(new SelectionListener() {
		
		 public void widgetDefaultSelected(SelectionEvent e) {
				setPageComplete();
				relativeToCombo.setEnabled(savePathInfoCheckBox.getSelection());
				if (!savePathInfoCheckBox.getSelection()) relativePath = "";
				setSaveFullPathInfo();
			}
		 public void widgetSelected(SelectionEvent e) {
				setPageComplete();
				relativeToCombo.setEnabled(savePathInfoCheckBox.getSelection());
			if (!savePathInfoCheckBox.getSelection()) relativePath = "";
				setSaveFullPathInfo();
			}
		});
		
		relativeToCombo.addModifyListener(new ModifyListener() {
		
		 public void modifyText(ModifyEvent e) {
				 relativePath = relativeToCombo.getText();
				 setPageComplete();
			 }
		 });
					
		return control;
	}
	
	public boolean isPageComplete()
	{
		boolean pathInfoChecked = (savePathInfoCheckBox != null && savePathInfoCheckBox.getSelection());
		boolean relPathSelectionMade = relativePath != null && !relativePath.equals("");
		boolean relPathComplete = savePathInfoCheckBox == null || !savePathInfoCheckBox.getEnabled();
		if (!relPathComplete)
		{
			if (pathInfoChecked)
			{
				relPathComplete = relPathSelectionMade;
			}
			else relPathComplete = true;
		}
		return super.isPageComplete() && relPathComplete;
	}

	protected void setSaveFullPathInfo()
	{
		saveFullPathInfo = savePathInfoCheckBox != null && 
							savePathInfoCheckBox.getEnabled() && 
							savePathInfoCheckBox.getSelection();
	}
	
	public String getRelativePath()
	{
		return relativePath;
	}
	
	public boolean getSaveFullPathInfo()
	{
		return saveFullPathInfo;
	}
}