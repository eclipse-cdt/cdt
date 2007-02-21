/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;

/**
 * It is a parent for all standard property tabs 
 * in new CDT model.
 * 
 * Although it's enougth for new tabs to implement
 * ICPropertyTab interface only, it would be better 
 * to extend them from this class.
 *
 * In this case, we'll able to use:
 * - a lot of utility methods via "provider" link.
 *   In particular, it allows to get current project,
 *   configuration etc. See ICPropertyProvider interface. 
 * - a standard way to create buttons (ins/edit/del etc)
 *   and to handle their events (see buttonPressed(int))
 * - several utility methods to create widgets in the
 *   uniform manner (setupLabel(), setupText() etc). 
 * - means to handle control messages which are the main
 *   communication way for new CDT model pages and tabs.   
 */
public abstract class AbstractCPropertyTab implements ICPropertyTab {
	public static final int BUTTON_WIDTH = 100; // used as hint for all push buttons

	// commonly used button names
	public static final String EMPTY_STR = ""; //$NON-NLS-1$
	public static final String ADD_STR = NewUIMessages.getResourceString("FileListControl.add"); //$NON-NLS-1$
	public static final String DEL_STR = NewUIMessages.getResourceString("FileListControl.delete"); //$NON-NLS-1$
	public static final String EDIT_STR = NewUIMessages.getResourceString("FileListControl.edit"); //$NON-NLS-1$
	public static final String MOVEUP_STR = NewUIMessages.getResourceString("FileListControl.moveup"); //$NON-NLS-1$
	public static final String MOVEDOWN_STR = NewUIMessages.getResourceString("FileListControl.movedown"); //$NON-NLS-1$
	public static final String WORKSPACEBUTTON_NAME = NewUIMessages.getResourceString("FileListControl.button.workspace"); //$NON-NLS-1$
	public static final String FILESYSTEMBUTTON_NAME = NewUIMessages.getResourceString("FileListControl.button.fs"); //$NON-NLS-1$
	public static final String VARIABLESBUTTON_NAME = "Variables..."; //$NON-NLS-1$ 
	public static final String FILESYSTEM_DIR_DIALOG_MSG = NewUIMessages.getResourceString("BrowseEntryDialog.fs.dir.dlg.msg");	//$NON-NLS-1$
	public static final String FILESYSTEM_FILE_DIALOG_TITLE = ""; //$NON-NLS-1$	
	public static final String WORKSPACE_DIR_DIALOG_TITLE = NewUIMessages.getResourceString("BrowseEntryDialog.wsp.dir.dlg.title");	//$NON-NLS-1$
	public static final String WORKSPACE_FILE_DIALOG_TITLE = NewUIMessages.getResourceString("BrowseEntryDialog.wsp.file.dlg.title");	//$NON-NLS-1$
	public static final String WORKSPACE_DIR_DIALOG_MSG = NewUIMessages.getResourceString("BrowseEntryDialog.wsp.dir.dlg.msg");	//$NON-NLS-1$
	public static final String WORKSPACE_FILE_DIALOG_MSG = NewUIMessages.getResourceString("BrowseEntryDialog.wsp.file.dlg.msg");	//$NON-NLS-1$
	public static final String WORKSPACE_FILE_DIALOG_ERR = NewUIMessages.getResourceString("BrowseEntryDialog.wsp.file.dlg.err");	//$NON-NLS-1$

	protected Composite usercomp; // space where user can create widgets 
	protected Composite buttoncomp; // space for buttons on the right
	private Button[] buttons;     // buttons in buttoncomp
	public ICPropertyProvider page;
	protected Image icon = null; 
	
	protected boolean visible;

	public void createControls(Composite _parent, ICPropertyProvider _provider) {
		page = _provider;
		createControls(_parent);
	}

	/**
	 * Creates basic widgets for property tab.
	 * Descendants should, normally, override
	 * this method but call super.createControls(). 
	 * 
	 * @param parent
	 */
	protected void createControls(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		usercomp = new Composite(parent, SWT.NONE);
		usercomp.setLayoutData(new GridData(GridData.FILL_BOTH));
		buttoncomp = new Composite(parent, SWT.NONE);
		GridData d = new GridData(GridData.END);
		d.widthHint = 1;
		buttoncomp.setLayoutData(d);
	}
	
	/**
	 * The common way to create buttons cluster
	 * on the right of tab workspace.
	 * @param names : array of button names
	 * null instead of name means "skip place" 
	 */
	protected void  initButtons(String[] names) {
		initButtons(buttoncomp, names, 80);
	}
	protected void  initButtons(String[] names, int width) {
		initButtons(buttoncomp, names, width);
	}
	
	/**
	 * Ability to create standard button on any composite.
	 * @param c
	 * @param names
	 */
	protected void initButtons(Composite c, String[] names) {
		initButtons(c, names, 80);
	}
	protected void initButtons(Composite c, String[] names, int width) {
		if (names == null || names.length == 0) return;
		c.setLayoutData(new GridData(GridData.FILL_VERTICAL));
	    c.setLayout(new GridLayout(1, false));
		buttons = new Button[names.length];
		for (int i=0; i<names.length; i++) {
			buttons[i] = new Button(c, SWT.PUSH);
			GridData gdb = new GridData(GridData.VERTICAL_ALIGN_CENTER);
			gdb.grabExcessHorizontalSpace = false;
			gdb.horizontalAlignment = SWT.FILL;
			gdb.widthHint = width;
			
			if (names[i] != null)
				buttons[i].setText(names[i]);
			else { // no button, but placeholder ! 
				buttons[i].setVisible(false);
				buttons[i].setEnabled(false);
				gdb.heightHint = 10;
			}
			
			buttons[i].setLayoutData(gdb);
			buttons[i].addSelectionListener(new SelectionAdapter() {
		        public void widgetSelected(SelectionEvent event) {
		        	buttonPressed(event);
		        }});
		}
	}
	
	/**
	 * Called when user changes 
	 * @param cfg - selected configuration
	 */
	private void configChanged(ICResourceDescription cfg) {
		if (visible) updateData(cfg);
	}
	
    /**
     * Disposes the SWT resources allocated by this
     * dialog page.
     */
    public void dispose() {}

    /**
     * Sets the visibility of this property tab.
     *
     * @param _visible <code>true</code> to make this tab visible,
     *  and <code>false</code> to hide it
     */
    public void setVisible(boolean _visible) {
    	visible = _visible;
    	if (visible) updateData(page.getResDesc());
    }
	
    /**
     * Descendant tabs should implement this method so 
     * that it copies it's data from one description 
     * to another. Only data affected by given tab
     * should be copied.
     * 
     * @param src
     * @param dst
     */
	protected abstract void performApply(ICResourceDescription src, ICResourceDescription dst);
	protected abstract void performDefaults();
	protected abstract void updateData(ICResourceDescription cfg);
	protected void performCancel() {}
	protected void performOK() {}

	/**
	 * 
	 * @param e - event to be handled
	 */
	private void buttonPressed(SelectionEvent e) {
		for (int i=0; i<buttons.length; i++) {
			if (buttons[i].equals(e.widget)) {
				buttonPressed(i);
				return;
			}
		}
	}
	
	/**
	 * Method should be rewritten to handle button presses 
	 * @param i : number of button pressed
	 * 
	 * Does nothing by default. 
	 * May (but not must) be overridden.
	 */
	protected void buttonPressed(int i) {}
	
	protected boolean buttonIsEnabled(int i) {
		if (buttons == null || buttons.length <= i ) 
			return false;
		else
			return buttons[i].isEnabled();
	}
	
	protected void buttonSetEnabled(int i, boolean state) {
		if (buttons == null || buttons.length <= i ) return;
		buttons[i].setEnabled(state);
	}

	/**********************************************
	 * Utility methods for unified widget creation
	 **********************************************/
	protected Label setupLabel(Composite c, String name, int span, int mode) {
		Label l = new Label(c, SWT.NONE);
		l.setText(name);
		setupControl(l, span, mode);
		return l;
	}

	protected Button setupButton(Composite c, String name, int span, int mode) {
		Button b = new Button(c, SWT.PUSH);
		b.setText(name);
		setupControl(b, span, mode);
		GridData g = (GridData)b.getLayoutData();
		g.widthHint = BUTTON_WIDTH;
		g.horizontalAlignment = SWT.RIGHT;
		b.setLayoutData(g);
		return b;
	}
	
	protected Text setupText(Composite c, int span, int mode) {
		Text t = new Text(c, SWT.SINGLE | SWT.BORDER);
		setupControl(t, span, mode);
		return t;
	}
	
	protected Group setupGroup(Composite c, String name, int cols, int mode) {
		Group g = new Group(c, SWT.NONE);
		g.setText(name);
		g.setLayout(new GridLayout(cols, false));
		setupControl(g, 1, mode);
		return g;
	}
	
	protected Button setupCheck(Composite c, String name, int span, int mode) {
		 Button b = new Button(c, SWT.CHECK);
		 b.setText(name);
		 setupControl(b, span, mode);
		 b.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent event) {
		    	checkPressed(event);
		 }});
		 return b;
	}

	/**
	 * Selection handler for checkbox created by method "setupCheck()" 
	 * Descendants should override this method if they use "setupCheck".  
	 * Usually the method body will look like:
	 * { 
	 * 		Button b = (Button)e.widget;
	 *   	if (b.equals(myFirstCheckbox) { ... } 
	 *   	else if (b.equals(mySecondCheckbox) { ... }
	 *   ... } 
	 */
    protected void checkPressed(SelectionEvent e) {}
    
	protected void setupControl(Control c, int span, int mode) {
		// although we use GridLayout usually,
		// exceptions can occur: do nothing. 
		if (span != 0) {
			GridData gd = new GridData(mode);
			gd.horizontalSpan = span;
			c.setLayoutData(gd);
		}
		Composite p = c.getParent();
		if (c != null) {
			c.setFont(p.getFont());
		}
	}
	
	/*
	 * A set of methods providing selection dialogs for files or dirs. 
	 */
	
	public static String getFileSystemDirDialog(Shell shell, String text) {
		DirectoryDialog dialog = new DirectoryDialog(shell,	SWT.OPEN|SWT.APPLICATION_MODAL);
		if(text != null && text.trim().length() != 0) dialog.setFilterPath(text);
		dialog.setMessage(FILESYSTEM_DIR_DIALOG_MSG);
		return dialog.open();
	}

	public static String getFileSystemFileDialog(Shell shell, String text) {
		FileDialog dialog = new FileDialog(shell);
		if(text != null && text.trim().length() != 0) dialog.setFilterPath(text);
		dialog.setFilterExtensions(new String[] {"*.a;*.so;*.dll;*.lib"}); //$NON-NLS-1$
		dialog.setText(FILESYSTEM_FILE_DIALOG_TITLE);
		return dialog.open();
	}

	public static String getVariableDialog(Shell shell, ICConfigurationDescription cfgd) {
		
		ICdtVariableManager vm = CCorePlugin.getDefault().getCdtVariableManager();
		
		ListDialog dialog = new ListDialog(shell);
		dialog.setContentProvider(new IStructuredContentProvider() {
					public Object[] getElements(Object inputElement) { return (Object[])inputElement; }
					public void dispose() {}
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
				});
		dialog.setLabelProvider(new ILabelProvider() {
					public Image getImage(Object element) { return null; }
					public String getText(Object element) {
							if (element instanceof ICdtVariable) 
								return ((ICdtVariable)element).getName();
							return null;
						}
					public void addListener(ILabelProviderListener listener) {}
					public void dispose() {}
					public boolean isLabelProperty(Object element, String property) { return false; }
					public void removeListener(ILabelProviderListener listener) {
				}});
		
		dialog.setInput(vm.getVariables(cfgd));
		dialog.setHeightInChars(10);
		dialog.setTitle(NewUIMessages.getResourceString("AbstractCPropertyTab.0")); //$NON-NLS-1$

		if (dialog.open() == Window.OK) {
			Object[] selected = dialog.getResult();
			if (selected.length > 0) {
				String s = ((ICdtVariable)selected[0]).getName();
				return  "${"+s.trim()+"}";  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return null;
	}

	public static String getWorkspaceDirDialog(Shell shell, String text) {
		return getWorkspaceDialog(shell, text, true, null);
	}
	public static String getWorkspaceFileDialog(Shell shell, String text) {
		return getWorkspaceDialog(shell, text, false, null);
	}
	
	private static String getWorkspaceDialog(Shell shell, String text, boolean dir, IProject prj) {
		String currentPathText;
		IPath path;
		currentPathText = text;
		/* Remove double quotes */
		currentPathText = currentPathText.replaceAll("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
		path = new Path(currentPathText);
		
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(shell,
				new WorkbenchLabelProvider(), new WorkbenchContentProvider());

		if (prj == null)
			dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		else
			dialog.setInput(prj);
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
	
		if (dir)	{
			IResource container = null;
			if(path.isAbsolute()){
				IContainer cs[] = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(path);
				if(cs != null && cs.length > 0)
					container = cs[0];
			}
			dialog.setInitialSelection(container);
			dialog.setTitle(WORKSPACE_DIR_DIALOG_TITLE); 
            dialog.setMessage(WORKSPACE_DIR_DIALOG_MSG); 
		} else {
			IResource resource = null;
			if(path.isAbsolute()){
				IFile fs[] = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
				if(fs != null && fs.length > 0)
					resource = fs[0];
			}
			dialog.setInitialSelection(resource);
			dialog.setValidator(new ISelectionStatusValidator() {
			    public IStatus validate(Object[] selection) {
			    	if (selection != null)
			    		if (selection.length > 0)
			    			if (!(selection[0] instanceof IFile))
			    				return new StatusInfo(IStatus.ERROR, WORKSPACE_FILE_DIALOG_ERR);
			    	return new StatusInfo();
			    }
			});
			dialog.setTitle(WORKSPACE_FILE_DIALOG_TITLE); 
            dialog.setMessage(WORKSPACE_FILE_DIALOG_MSG); 
		}
		if (dialog.open() == Window.OK) {
			IResource resource = (IResource) dialog.getFirstResult();
			if (resource != null) { 
				StringBuffer buf = new StringBuffer();
				return buf.append("${").append("workspace_loc:").append(resource.getFullPath()).append("}").toString(); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
			}
		}
		return null;
	}
	
	// shortcut to frequently-used method
	public ICResourceDescription getResDesc() {
		return page.getResDesc();
	}

	/**
	 * Common event handler: called by parent for each tab
	 */
	public void handleTabEvent (int kind, Object data) {
		switch(kind) {
		case ICPropertyTab.OK:
			if (canBeVisible()) performOK();
			break;
		case ICPropertyTab.APPLY:
			if (canBeVisible()) performApply(getResDesc(), (ICResourceDescription)data);
			break;
		case ICPropertyTab.CANCEL:
			if (canBeVisible()) performCancel();
			break;
		case ICPropertyTab.DEFAULTS:
			if (canBeVisible() && getResDesc() != null) {
				updateData(getResDesc());
				performDefaults();
			}
			break;
		case ICPropertyTab.UPDATE:
			if (canBeVisible()) configChanged((ICResourceDescription)data);
			break;
		case ICPropertyTab.DISPOSE:
			break;
		case ICPropertyTab.VISIBLE:
			if (canBeVisible()) setVisible(data != null);
			break;
		case ICPropertyTab.SET_ICON:
			icon = (Image)data; 
			break;
		default:
			break;
		}
	}
	
    // By default, returns true (no visibility restriction)
    // But several pages should rewrite this functionality.
	public boolean canBeVisible() {
		return true;
	}

	/**
	 * Added to avoid usage PixelConverter class.
	 * @param control
	 * @return FontMetrics
	 */
	public static FontMetrics getFontMetrics(Control control) {
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		FontMetrics fFontMetrics= gc.getFontMetrics();
		gc.dispose();
		return fFontMetrics;
	}


}
