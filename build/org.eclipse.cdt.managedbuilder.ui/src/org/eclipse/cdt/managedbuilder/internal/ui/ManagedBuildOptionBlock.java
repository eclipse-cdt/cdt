/**********************************************************************
 * Copyright (c) 2002,2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import java.util.Iterator;

import org.eclipse.cdt.ui.dialogs.BinaryParserBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPropertyPage;
import org.eclipse.cdt.managedbuilder.ui.properties.ResourceBuildPropertyPage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.help.WorkbenchHelp;

public class ManagedBuildOptionBlock extends TabFolderOptionBlock {

	private ToolsSettingsBlock toolsSettingsBlock;
	private BuildSettingsBlock buildSettingsBlock;
	private ErrorParserBlock errParserBlock;
	private BinaryParserBlock binaryParserBlock;
	private Object element;
	
	/**
	 * @param parent
	 */
	public ManagedBuildOptionBlock(BuildPropertyPage parent) {
		super(parent, false);
	}
	
	public ManagedBuildOptionBlock(ResourceBuildPropertyPage resParent) {
		super(resParent, false);
	}
	
	public BuildPropertyPage getBuildPropertyPage() {
		return (BuildPropertyPage)fParent;
	}

	public ResourceBuildPropertyPage getResourceBuildPropertyPage() {
		return (ResourceBuildPropertyPage)fParent;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#addTabs()
	 */
	protected void addTabs() {
		
		if (element instanceof IProject) {
			addTab(toolsSettingsBlock = new ToolsSettingsBlock((BuildPropertyPage) fParent, element));
			addTab(buildSettingsBlock = new BuildSettingsBlock((BuildPropertyPage) fParent));
			addTab(errParserBlock = new ErrorParserBlock());
			addTab(binaryParserBlock = new BinaryParserBlock());
		} else if (element instanceof IFile) {
			addTab(toolsSettingsBlock = new ToolsSettingsBlock((ResourceBuildPropertyPage) fParent, element));
		}
	}

	public ToolsSettingsBlock getToolsSettingsBlock() {
		return toolsSettingsBlock;
	}
	
	public BuildSettingsBlock getBuildSettingsBlock() {
		return buildSettingsBlock;
	}

	public BinaryParserBlock getBinaryParserBlock() {
		return binaryParserBlock;
	}
	
	public ErrorParserBlock getErrorParserBlock() {
		return errParserBlock;
	}
	
	public Control createContents(Composite parent, Object element) {
		this.element = element;
		Control control = super.createContents( parent );
		((GridLayout)((Composite)control).getLayout()).marginWidth = 1;
		GridData gd = new GridData(GridData.FILL_BOTH);
		((Composite)control).setLayoutData(gd);

		// TODO
		//if (getToolsSettingsBlock() != null)
		//	WorkbenchHelp.setHelp(getToolsSettingsBlock().getControl(), ManagedBuilderHelpContextIds.MAN_PROJ_ERROR_PARSER);
		if (getErrorParserBlock() != null)
			WorkbenchHelp.setHelp(getErrorParserBlock().getControl(), ManagedBuilderHelpContextIds.MAN_PROJ_ERROR_PARSER);

		return control;
	}	

	protected void initializeValues() {
		if (getToolsSettingsBlock()!= null) {
			getToolsSettingsBlock().initializeValues();
		}
		if (getBuildSettingsBlock()!= null) {
			getBuildSettingsBlock().initializeValues();
		}
		if (getErrorParserBlock()!= null) {
			// TODO
			//getErrorParserBlock().initializeValues();
		}
		if (getBinaryParserBlock()!= null) {
			// TODO
			//getBinaryParserBlock().initializeValues();
		}
	}

	public void updateValues() {
		if (element instanceof IProject) {
			if (getToolsSettingsBlock() != null) {
				getToolsSettingsBlock().updateValues();
			}
			if (getBuildSettingsBlock() != null) {
				getBuildSettingsBlock().updateValues();
			}
			if (getErrorParserBlock() != null) {
				getErrorParserBlock().updateValues();
			}
			if (getBinaryParserBlock() != null) {
				// TODO
				//getBinaryParserBlock().updateValues();
			}
		} else if( element instanceof IFile) {
			if (getToolsSettingsBlock() != null) {
				getToolsSettingsBlock().updateValues();
			}
		}
	}

	public void setValues() {
		if (element instanceof IProject) {
			if (getToolsSettingsBlock() != null) {
				getToolsSettingsBlock().updateValues();
			}
			if (getBuildSettingsBlock() != null) {
				getBuildSettingsBlock().setValues();
			}
			if (getErrorParserBlock() != null) {
				// TODO
				//getErrorParserBlock().setValues();
			}
			if (getBinaryParserBlock() != null) {
				// TODO
				//getBinaryParserBlock().setValues();
			}
		} else  if (element instanceof IFile) {
			if (getToolsSettingsBlock() != null) {
				getToolsSettingsBlock().updateValues();
			}
		}
	}

	public void removeValues(String id) {
		if (element instanceof IProject) {
			if (getToolsSettingsBlock() != null) {
				getToolsSettingsBlock().removeValues(id);
			}
			if (getBuildSettingsBlock() != null) {
				getBuildSettingsBlock().removeValues(id);
			}
			if (getErrorParserBlock() != null) {
				// TODO
				//getErrorParserBlock().removeValues(id);
			}
			if (getBinaryParserBlock() != null) {
				// TODO
				//getBinaryParserBlock().removeValues(id);
			}
		} else  if (element instanceof IFile) {
			if (getToolsSettingsBlock()!= null) {
				getToolsSettingsBlock().removeValues(id);
			}
		}
	}
	
	public IPreferenceStore getPreferenceStore()
	{
		if (element instanceof IProject) {
			if (getCurrentPage() instanceof ToolsSettingsBlock) {
				return toolsSettingsBlock.getPreferenceStore();
			}
			if (getCurrentPage() instanceof BuildSettingsBlock) {
				return buildSettingsBlock.getPreferenceStore();
			}
			if (getCurrentPage() instanceof ErrorParserBlock) {
				return errParserBlock.getPreferenceStore();
			}
			if (getCurrentPage() instanceof BinaryParserBlock) {
				return null;
			}
		} else if( element instanceof IFile) {
			if (getCurrentPage() instanceof ToolsSettingsBlock) {
				return toolsSettingsBlock.getPreferenceStore();
			}
		}
		return null;
	}
	
	public IPreferenceStore getToolSettingsPreferenceStore()
	{
		return toolsSettingsBlock.getPreferenceStore();
	}

	public void update() {
		super.update();
		ICOptionPage tab = getCurrentPage();
		//  Currently, other settings are per-config, while binary parser settings are per-project
		if (tab instanceof BinaryParserBlock) {
			((BuildPropertyPage)fParent).enableConfigSelection(false);
		} else {
			if(element instanceof IProject) {
				((BuildPropertyPage)fParent).enableConfigSelection(true);
			} else if ( element instanceof IFile) {
				((ResourceBuildPropertyPage)fParent).enableConfigSelection(true);
			}
		}
	}

	/**
	 * Sets the dirty state of the contained pages
	 */
	public void setDirty(boolean b) {
		Iterator iter = getOptionPages().iterator();
		while (iter.hasNext()) {
			ICOptionPage tab = (ICOptionPage)iter.next();
			if (tab instanceof BuildSettingsBlock) {
			    ((BuildSettingsBlock)tab).setDirty(b);
			} else if (tab instanceof ToolsSettingsBlock) {
				    ((ToolsSettingsBlock)tab).setDirty(b);
			} else if (tab instanceof ErrorParserBlock) {
			    ((ErrorParserBlock)tab).setDirty(b);
			} else if (tab instanceof BinaryParserBlock) {
			    //TODO  ManagedBuildSystem needs its own binary parser block
			}
		}
	}

	/**
	 * Returns <code> true <code/> if any of the pages are dirty
	 * @return boolean
	 */
	public boolean isDirty() {
		Iterator iter = getOptionPages().iterator();
		while (iter.hasNext()) {
			ICOptionPage tab = (ICOptionPage)iter.next();
			if (tab instanceof BuildSettingsBlock) {
			    if (((BuildSettingsBlock)tab).isDirty()) return true;
			} else if (tab instanceof ToolsSettingsBlock) {
			    if (((ToolsSettingsBlock)tab).isDirty()) return true;
			} else if (tab instanceof ErrorParserBlock) {
			    if (((ErrorParserBlock)tab).isDirty()) return true;
			} else if (tab instanceof BinaryParserBlock) {
			    //TODO  ManagedBuildSystem needs its own binary parser block
			}
			
		}
		return false;
	}
	
}
