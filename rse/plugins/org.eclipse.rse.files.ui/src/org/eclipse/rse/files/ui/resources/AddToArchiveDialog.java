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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.files.ui.widgets.SystemSelectRemoteFileOrFolderForm;
import org.eclipse.swt.widgets.Shell;


/**
 * @author mjberger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AddToArchiveDialog extends CombineDialog {

	private String[] _relativePaths;

	public AddToArchiveDialog(Shell shell) 
	{
		super(shell);
		setHelp(SystemPlugin.HELPPREFIX + "atad0000");
	}

	public AddToArchiveDialog(Shell shell, String title) {
		super(shell, title);
		setHelp(SystemPlugin.HELPPREFIX + "atad0000");
	}
	
	public AddToArchiveDialog(Shell shell, String title, String[] relativePaths) 
	{
		super(shell, title);
		setHelp(SystemPlugin.HELPPREFIX + "atad0000");
		_relativePaths = relativePaths;
		((AddToArchiveForm)form).setRelativePathList(_relativePaths);
	}

	public AddToArchiveDialog(
		Shell shell,
		String title,
		boolean prePopSelection) 
	{
		super(shell, title, prePopSelection);
		setHelp(SystemPlugin.HELPPREFIX + "atad0000");
	}
	
	public AddToArchiveDialog(
		Shell shell,
		String title,
		boolean prePopSelection,
		String[] relativePaths) 
	{
		super(shell, title, prePopSelection);
		setHelp(SystemPlugin.HELPPREFIX + "atad0000");
		_relativePaths = relativePaths;
		((AddToArchiveForm)form).setRelativePathList(_relativePaths);
	}


	protected SystemSelectRemoteFileOrFolderForm getForm(boolean fileMode)
	{
		super.getForm(fileMode);
		form = new AddToArchiveForm(getMessageLine(), this, fileMode, prePop, _relativePaths);
		return form;
	}
	
	public boolean getSaveFullPathInfo()
	{
		return ((AddToArchiveForm)form).getSaveFullPathInfo();
	}
	
	public String getRelativePath()
	{
		return ((AddToArchiveForm)form).getRelativePath();
	}
}