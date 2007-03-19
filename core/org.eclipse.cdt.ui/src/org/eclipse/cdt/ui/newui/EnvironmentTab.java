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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;

import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.internal.core.envvar.UserDefinedEnvironmentSupplier;


public class EnvironmentTab extends AbstractCPropertyTab {
	private static final String PREFIX = "EnvironmentBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String VALUE_UNDEF = LABEL + ".value.undef";	//$NON-NLS-1$
	private static final String SEMI = ";"; //$NON-NLS-1$
	private static final String LBR = " ["; //$NON-NLS-1$
	private static final String RBR = "]"; //$NON-NLS-1$
	private static final UserDefinedEnvironmentSupplier fUserSupplier = EnvironmentVariableManager.fUserSupplier;
	

	private Table table;
	private TableViewer tv;
	private ArrayList data = new ArrayList();
	private Button b1, b2;
	
	private static final IContributedEnvironment ce = CCorePlugin.getDefault().getBuildEnvironmentManager().getContributedEnvironment();
	private ICConfigurationDescription cfgd = null;
	private StorableEnvironment vars = null;

	private class TabData {
		IEnvironmentVariable var;
		boolean changed;
		TabData(IEnvironmentVariable _var, boolean _changed) {
			var = _var;
			changed = _changed;
		}
	}
	
	private class EnvironmentLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider , ITableFontProvider, IColorProvider{
		public EnvironmentLabelProvider(boolean user){
		}
		public Image getImage(Object element) {
			return null; // JavaPluginImages.get(JavaPluginImages.IMG_OBJS_REFACTORING_INFO);
		}
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			TabData td = (TabData)element;
			switch(columnIndex){
			case 0:
				return td.var.getName();
			case 1:
				if(td.var.getOperation() == IEnvironmentVariable.ENVVAR_REMOVE)
					return NewUIMessages.getResourceString(VALUE_UNDEF);
				return td.var.getValue();
			}
			return EMPTY_STR;
		}

		public Font getFont(Object element) {
			return getFont(element, 0);
		}

		public Font getFont(Object element, int columnIndex) {
			TabData td = (TabData)element;
			Font f = null;
			if (cfgd == null || ce.isUserVariable(cfgd, td.var))
				f = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);

			switch(columnIndex){
			case 0:
				break;
			case 1:
				if(td.var.getOperation() == IEnvironmentVariable.ENVVAR_REMOVE)
					f = JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
				break;
			}
			return f;
		}
		
	    /* (non-Javadoc)
	     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	     */
	    public Color getForeground(Object element){
			return null;
	    }
		public Color getBackground(Object element){
			return null;
	    }
	}
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));
		Label l1 = new Label(usercomp, SWT.LEFT);
		l1.setText(NewUIMessages.getResourceString("EnvironmentTab.0")); //$NON-NLS-1$
		l1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		table = new Table(usercomp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
	    		if (buttonIsEnabled(2) && table.getSelectionIndex() != -1)
    				buttonPressed(2);
			}});
		
		tv = new TableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				if (inputElement != null && inputElement instanceof ArrayList) {
					ArrayList ar = (ArrayList)inputElement;
					return ar.toArray(new TabData[0]);
				}
				return null;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
			});
		tv.setLabelProvider(new EnvironmentLabelProvider(true));
		// add headers
		TableColumn tc = new TableColumn(table, SWT.LEFT);
		tc.setText(NewUIMessages.getResourceString("EnvironmentTab.1")); //$NON-NLS-1$
		tc.setWidth(200);
		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(NewUIMessages.getResourceString("EnvironmentTab.2")); //$NON-NLS-1$
		tc.setWidth(200);
		                      
	    table.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    b1 = new Button(usercomp, SWT.RADIO);
	    b1.setText(NewUIMessages.getResourceString("EnvironmentTab.3")); //$NON-NLS-1$
	    b1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    b1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (cfgd != null)
					ce.setAppendEnvironment(true, cfgd);
				else
					vars.setAppendContributedEnvironment(true);
				updateData();
			}});

	    b2 = new Button(usercomp, SWT.RADIO);
	    b2.setText(NewUIMessages.getResourceString("EnvironmentTab.4")); //$NON-NLS-1$
	    b2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    b2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (cfgd != null)
					ce.setAppendEnvironment(false, cfgd);
				else
					vars.setAppendContributedEnvironment(false);
				updateData();
			}});
	    
	    initButtons(new String[] {NewUIMessages.getResourceString("EnvironmentTab.5"),NewUIMessages.getResourceString("EnvironmentTab.6"),NewUIMessages.getResourceString("EnvironmentTab.7"),NewUIMessages.getResourceString("EnvironmentTab.8"),NewUIMessages.getResourceString("EnvironmentTab.9")}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	public void buttonPressed(int i) {
		IEnvironmentVariable var = null;
		EnvDialog dlg;
		int n = table.getSelectionIndex();
		int[] idx;
		switch (i) {
		case 0:
			dlg = new EnvDialog(usercomp.getShell(), var, NewUIMessages.getResourceString("EnvironmentTab.10"), true, cfgd); //$NON-NLS-1$
			if (dlg.open() == Window.OK) {
				if (dlg.t1.trim().length() > 0) {
					ICConfigurationDescription[] cfgs;
					if (dlg.toAll)
						cfgs = page.getCfgsEditable();
					else 
						cfgs = new ICConfigurationDescription[] {cfgd};
					if (cfgd == null)
						vars.createVariable(dlg.t1.trim(), dlg.t2.trim(), 
								IEnvironmentVariable.ENVVAR_APPEND,	SEMI);
					else	
						for (int x=0; x<cfgs.length; x++) { 
							ce.addVariable(dlg.t1.trim(), dlg.t2.trim(), 
							IEnvironmentVariable.ENVVAR_APPEND, 
							SEMI, cfgs[x]);
					}
					updateData();
				}
			}
			break;
		case 1: // select
			handleEnvSelectButtonSelected();
			updateData();
			break;
		case 2: // edit
			if (n == -1) return;
			var = ((TabData)tv.getElementAt(n)).var;
			dlg = new EnvDialog(usercomp.getShell(), var, NewUIMessages.getResourceString("EnvironmentTab.11"), false, cfgd); //$NON-NLS-1$
			if (dlg.open() == Window.OK) {
				if (cfgd != null)
					ce.addVariable(	dlg.t1.trim(), dlg.t2.trim(), 
						IEnvironmentVariable.ENVVAR_REPLACE, 
						var.getDelimiter(), cfgd);
				else
					vars.createVariable(dlg.t1.trim(), dlg.t2.trim(),
						IEnvironmentVariable.ENVVAR_REPLACE, var.getDelimiter());
				updateData();
			}
			break;
		case 3: // remove
			if (n == -1) return;
			idx = table.getSelectionIndices();
			for (int j=0; j<idx.length; j++) {
				var = ((TabData)tv.getElementAt(idx[j])).var;
				if (cfgd == null) 
					vars.deleteVariable(var.getName());
				else
					ce.removeVariable(var.getName(), cfgd);
			}
			updateData();
			break;
		case 4: // Undefine
			if (n == -1) return;
			idx = table.getSelectionIndices();
			for (int j=0; j<idx.length; j++) {
				var = ((TabData)tv.getElementAt(idx[j])).var;
				if (cfgd == null)
					vars.createVariable(
							var.getName(), 
							null, 
							IEnvironmentVariable.ENVVAR_REMOVE, 
							var.getDelimiter());
				else 
					ce.addVariable(
						var.getName(), 
						null, 
						IEnvironmentVariable.ENVVAR_REMOVE, 
						var.getDelimiter(), cfgd);
			}
			updateData();
			break;
		}
		updateButtons();
	}
	
	public void updateButtons() {
		if (table == null || table.isDisposed()) return;
		
		int pos = table.getSelectionIndex();
		buttonSetEnabled(2, pos != -1);
		buttonSetEnabled(3, pos != -1);
		buttonSetEnabled(4, pos != -1);
	}

	public void updateData(ICResourceDescription _cfgd) {
		// null means preference configuration 
		cfgd = (_cfgd != null) ? _cfgd.getConfiguration() : null;
		if (cfgd == null && vars == null)
			vars = fUserSupplier.getWorkspaceEnvironmentCopy();
		updateData();
	}
	private void updateData() {
		IEnvironmentVariable[] _vars = null;
		if (cfgd != null) {
			b1.setSelection(ce.appendEnvironment(cfgd));
			b2.setSelection(!ce.appendEnvironment(cfgd));
			 _vars = ce.getVariables(cfgd);
		} else {
			b1.setSelection(vars.appendContributedEnvironment());
			b2.setSelection(!vars.appendContributedEnvironment());
			_vars = vars.getVariables() ;
		}
		data.clear();
		if (_vars != null) {
			for (int i=0; i<_vars.length; i++) {
				data.add(new TabData(_vars[i], false));
			}
		}
		tv.setInput(data);
		updateButtons();
	}

	public void performApply(ICResourceDescription _src, ICResourceDescription _dst) {
		ICConfigurationDescription src = _src.getConfiguration();
		ICConfigurationDescription dst = _dst.getConfiguration();
		
		ce.setAppendEnvironment(ce.appendEnvironment(src), dst);
		IEnvironmentVariable[] v = ce.getVariables(dst);
		for (int i=0; i<v.length; i++) ce.removeVariable(v[i].getName(), dst);
		v = ce.getVariables(src);
		for (int i=0; i<v.length; i++) 
			ce.addVariable(v[i].getName(), v[i].getValue(), 
					v[i].getOperation(), v[i].getDelimiter(), dst);
	}

	/**
	 * 
	 */
	private class MyListSelectionDialog extends ListSelectionDialog {
		public boolean toAll = false;
	    public MyListSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider) {
	        super(parentShell, input, contentProvider, new LabelProvider() {}, NewUIMessages.getResourceString("EnvironmentTab.12")); //$NON-NLS-1$
	    }
	    protected Control createDialogArea(Composite parent) {
	    	Composite composite = (Composite) super.createDialogArea(parent);
	    	Button b = new Button(composite, SWT.CHECK);
	    	b.setText(NewUIMessages.getResourceString("EnvironmentTab.13")); //$NON-NLS-1$
	    	b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    	if (cfgd == null)
	    		b.setVisible(false);
	    	else
	    		b.addSelectionListener(new SelectionAdapter() {
	    			public void widgetSelected(SelectionEvent e) {
	    				toAll = ((Button)e.widget).getSelection();
	    			}});
	    	return composite;
	    }
	}
	
	private void handleEnvSelectButtonSelected() {
		// get Environment Variables from the OS
		Map v = EnvironmentReader.getEnvVars();
		MyListSelectionDialog dialog = new MyListSelectionDialog(usercomp.getShell(), v, createSelectionDialogContentProvider());
		
		dialog.setTitle("Select variables"); //$NON-NLS-1$
		if (dialog.open() == Window.OK) {
			Object[] selected = dialog.getResult();
			ICConfigurationDescription[] cfgs;
			if (dialog.toAll)
				cfgs = page.getCfgsEditable();
			else 
				cfgs = new ICConfigurationDescription[] {cfgd};
			
			for (int i = 0; i < selected.length; i++) {
				String name = (String)selected[i];
				String value = EMPTY_STR;
				int x = name.indexOf(LBR);
				if (x >= 0) {
					value = name.substring(x + 2, name.length() - 1);
					name = name.substring(0, x);
				}
				
				if (cfgd == null) 
					vars.createVariable(name, value); 
				else
					for (int y=0; y<cfgs.length; y++) { 
						ce.addVariable(
								name, value, 
								IEnvironmentVariable.ENVVAR_APPEND, 
								SEMI, cfgs[y]);
				}
			}
		}
	}
	
	private IStructuredContentProvider createSelectionDialogContentProvider() {
		return new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				String[] els = null;
				if (inputElement instanceof Map) {
					Map m = (Map)inputElement;
					els = new String[m.size()];  
					int index = 0;
					for (Iterator iterator = m.keySet().iterator(); iterator.hasNext(); index++) {
						String k = (String)iterator.next();
						els[index] = k + LBR + (String)m.get(k) + RBR;  
					}
				}
				return els;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		};
	}
	// This page can be displayed for project only
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}

	protected void performOK() {
		if (vars != null) {
			if (fUserSupplier.setWorkspaceEnvironment(vars))
				if (page instanceof PrefPage_Abstract)
					PrefPage_Abstract.isChanged = true;
		}
		vars = null;
		super.performOK();
	}
	
	protected void performCancel() {
		vars = null;
		super.performCancel();
	}
	
	protected void performDefaults() {
		ce.restoreDefaults(cfgd); // both for proj & prefs
		vars = null;
		updateData();
	}
}
