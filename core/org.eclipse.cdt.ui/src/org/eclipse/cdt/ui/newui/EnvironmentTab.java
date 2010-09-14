/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;

import org.eclipse.cdt.internal.core.envvar.EnvVarDescriptor;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.internal.core.envvar.UserDefinedEnvironmentSupplier;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class EnvironmentTab extends AbstractCPropertyTab {
	private static final String SEPARATOR = System.getProperty("path.separator", ";"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String LBR = " ["; //$NON-NLS-1$
	private static final String RBR = "]"; //$NON-NLS-1$
	private static final UserDefinedEnvironmentSupplier fUserSupplier = EnvironmentVariableManager.fUserSupplier;
	
	private final MultiCfgContributedEnvironment ce = new MultiCfgContributedEnvironment();

	private Table table;
	private TableViewer tv;
	private ArrayList<TabData> data = new ArrayList<TabData>();
	private Button b1, b2;
	private Label  lb1, lb2;
	
	private ICConfigurationDescription cfgd = null;
	private StorableEnvironment vars = null;

	private class TabData implements Comparable<TabData> {
		IEnvironmentVariable var;
		TabData(IEnvironmentVariable _var) {
			var = _var;
		}
		public int compareTo(TabData a) {
			String s = var.getName();
			if (a != null && s != null && a.var != null)
					return (s.compareTo(a.var.getName())); 
			return 0;
		}
	}
	
	private class EnvironmentLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider , ITableFontProvider, IColorProvider{
		public EnvironmentLabelProvider(boolean user){
		}
		@Override
		public Image getImage(Object element) {
			return null; // JavaPluginImages.get(JavaPluginImages.IMG_OBJS_REFACTORING_INFO);
		}
		@Override
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
					return UIMessages.getString("EnvironmentTab.20"); //$NON-NLS-1$
				return td.var.getValue();
			case 2:
				return ce.getOrigin(td.var);
			}
			return EMPTY_STR;
		}

		public Font getFont(Object element) {
			return getFont(element, 0);
		}

		public Font getFont(Object element, int columnIndex) {
			TabData td = (TabData)element;
			switch(columnIndex){
			case 0:
				if (isUsers(td.var))
					return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
				break;
			default:
				break;
			}
			return null;
		}
		
	    public Color getForeground(Object element){
			return null;
	    }

	    public Color getBackground(Object element){
			TabData td = (TabData)element;
			if (isUsers(td.var))
				return BACKGROUND_FOR_USER_VAR;
			return null; 
	    }
	}
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(3, true));
		Label l1 = new Label(usercomp, SWT.LEFT);
		l1.setText(UIMessages.getString("EnvironmentTab.0")); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		l1.setLayoutData(gd);
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
				if (inputElement != null && inputElement instanceof ArrayList<?>) {
					@SuppressWarnings("unchecked")
					ArrayList<TabData> ar = (ArrayList<TabData>)inputElement;
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
		tc.setText(UIMessages.getString("EnvironmentTab.1")); //$NON-NLS-1$
		tc.setWidth(150);
		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(UIMessages.getString("EnvironmentTab.2")); //$NON-NLS-1$
		tc.setWidth(150);
		if (this.getResDesc() != null) {
			tc = new TableColumn(table, SWT.LEFT);
			tc.setText(UIMessages.getString("EnvironmentTab.16"));  //$NON-NLS-1$
			tc.setWidth(100);
		}
		                    
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
	    table.setLayoutData(gd);
	    
	    b1 = new Button(usercomp, SWT.RADIO);
	    b1.setText(UIMessages.getString("EnvironmentTab.3")); //$NON-NLS-1$
	    b1.setToolTipText(UIMessages.getString("EnvironmentTab.3")); //$NON-NLS-1$
	    gd = new GridData(GridData.FILL_HORIZONTAL);
	    gd.horizontalSpan = 3; 
	    b1.setLayoutData(gd);
	    b1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (cfgd != null)
					ce.setAppendEnvironment(true, cfgd);
				else
					vars.setAppendContributedEnvironment(true);
				updateData();
			}});

	    b2 = new Button(usercomp, SWT.RADIO);
	    b2.setText(UIMessages.getString("EnvironmentTab.4")); //$NON-NLS-1$
	    b2.setToolTipText(UIMessages.getString("EnvironmentTab.4")); //$NON-NLS-1$
	    gd = new GridData(GridData.FILL_HORIZONTAL);
	    gd.horizontalSpan = 3; 
	    b2.setLayoutData(gd);
	    b2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (cfgd != null)
					ce.setAppendEnvironment(false, cfgd);
				else
					vars.setAppendContributedEnvironment(false);
				updateData();
			}});

	    if (!page.isForPrefs()) {
	    	// dummy placeholder
	    	new Label(usercomp, SWT.NONE).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    	
	    	lb1 = new Label(usercomp, SWT.BORDER | SWT.CENTER);
	    	lb1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    	lb1.setToolTipText(UIMessages.getString("EnvironmentTab.15")); //$NON-NLS-1$
	    	lb1.addMouseListener(new MouseAdapter() {
	    		@Override
	    		public void mouseDoubleClick(MouseEvent e) {
	    			CDTPrefUtil.spinDMode();
	    			updateData();
	    		}});

	    	lb2 = new Label(usercomp, SWT.BORDER | SWT.CENTER);
	    	lb2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    	lb2.setToolTipText(UIMessages.getString("EnvironmentTab.23")); //$NON-NLS-1$
	    	lb2.addMouseListener(new MouseAdapter() {
	    		@Override
	    		public void mouseDoubleClick(MouseEvent e) {
	    			CDTPrefUtil.spinWMode();
	    			updateLbs(null, lb2);
	    		}});
	    }
	    initButtons(new String[] {UIMessages.getString("EnvironmentTab.5"),UIMessages.getString("EnvironmentTab.6"),UIMessages.getString("EnvironmentTab.7"),UIMessages.getString("EnvironmentTab.8"),UIMessages.getString("EnvironmentTab.9")}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
	
	@Override
	public void buttonPressed(int i) {
		switch (i) {
		case 0:
			handleEnvAddButtonSelected();
			break;
		case 1: // select
			handleEnvSelectButtonSelected();
			break;
		case 2: // edit
			handleEnvEditButtonSelected(table.getSelectionIndex());
			break;
		case 3: // remove
			handleEnvDelButtonSelected(table.getSelectionIndex());
			break;
		case 4: // Undefine
			handleEnvUndefButtonSelected(table.getSelectionIndex());
			break;
		}
		table.setFocus();
	}
	
	@Override
	protected void updateButtons() {
		if (table == null || table.isDisposed()) return;
		
		boolean canEdit = table.getSelectionCount() == 1;
		boolean canDel  = false;
		boolean canUndef = table.getSelectionCount() >= 1;
		if (canUndef) {
			for (int i : table.getSelectionIndices()) {
				IEnvironmentVariable var = ((TabData)tv.getElementAt(i)).var;
				if (isUsers(var)) {
				//	if (cfgd == null || !wse.getVariable(var.))
					canDel = true;
					break;
				}
			}
		}
		buttonSetEnabled(2, canEdit); // edit
		buttonSetEnabled(3, canDel); // delete
		buttonSetEnabled(4, canUndef); // undefine
	}

	@Override
	protected void updateData(ICResourceDescription _cfgd) {
		// null means preference configuration 
		cfgd = (_cfgd != null) ? _cfgd.getConfiguration() : null;
		if (cfgd == null && vars == null)
			vars = fUserSupplier.getWorkspaceEnvironmentCopy();
		else
			ce.setMulti(page.isMultiCfg());
		updateData();
	}

	private void updateData() {
		IEnvironmentVariable[] _vars = null;
		if (cfgd != null) {
			b1.setSelection(ce.appendEnvironment(cfgd));
			b2.setSelection(!ce.appendEnvironment(cfgd));
			 _vars = ce.getVariables(cfgd);
		} else {
			if (vars == null)
				vars = fUserSupplier.getWorkspaceEnvironmentCopy();
			b1.setSelection(vars.appendContributedEnvironment());
			b2.setSelection(!vars.appendContributedEnvironment());
			_vars = vars.getVariables() ;
		}
		
		data.clear();
		if (_vars != null) {
			for (IEnvironmentVariable _var : _vars) {
				data.add(new TabData(_var));
			}
		}
		Collections.sort(data);
		tv.setInput(data);
		
		updateLbs(lb1, lb2);
		updateButtons();
	}

	@Override
	protected void performApply(ICResourceDescription _src, ICResourceDescription _dst) {
		ICConfigurationDescription src = _src.getConfiguration();
		ICConfigurationDescription dst = _dst.getConfiguration();
		
		ce.setAppendEnvironment(ce.appendEnvironment(src), dst);
		IEnvironmentVariable[] v = ce.getVariables(dst);
		for (IEnvironmentVariable element : v)
			ce.removeVariable(element.getName(), dst);
		v = ce.getVariables(src);
		for (IEnvironmentVariable element : v) {
			if (ce.isUserVariable(src, element))
					ce.addVariable(element.getName(), element.getValue(), 
							element.getOperation(), element.getDelimiter(), dst);
		}
	}

	/**
	 * 
	 */
	private class MyListSelectionDialog extends ListSelectionDialog {
		public boolean toAll = false;
	    public MyListSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider) {
	        super(parentShell, input, contentProvider, new LabelProvider() {}, UIMessages.getString("EnvironmentTab.12")); //$NON-NLS-1$
	    }
	    @Override
		protected Control createDialogArea(Composite parent) {
	    	Composite composite = (Composite) super.createDialogArea(parent);
	    	Button b = new Button(composite, SWT.CHECK);
	    	b.setText(UIMessages.getString("EnvironmentTab.13")); //$NON-NLS-1$
	    	b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    	if (cfgd == null)
	    		b.setVisible(false);
	    	else
	    		b.addSelectionListener(new SelectionAdapter() {
	    			@Override
					public void widgetSelected(SelectionEvent e) {
	    				toAll = ((Button)e.widget).getSelection();
	    			}});
	    	return composite;
	    }
	}
	
	private void handleEnvEditButtonSelected(int n) {
		if (n == -1)
			return;
		IEnvironmentVariable var = ((TabData)tv.getElementAt(n)).var;
		EnvDialog dlg = new EnvDialog(usercomp.getShell(), 
				var, 
				UIMessages.getString("EnvironmentTab.11"),  //$NON-NLS-1$ 
				false,
				page.isMultiCfg(),
				cfgd);
		if (dlg.open() == Window.OK) {
			if (cfgd != null)
				ce.addVariable(	var.getName(), dlg.t2.trim(), 
						IEnvironmentVariable.ENVVAR_REPLACE, 
						var.getDelimiter(), cfgd);
			else
				vars.createVariable(dlg.t1.trim(), dlg.t2.trim(),
						IEnvironmentVariable.ENVVAR_REPLACE, var.getDelimiter());
			updateData();
			table.setSelection(n);
			updateButtons();
		}
	}
	
	private void handleEnvUndefButtonSelected(int n) {
		if (n == -1) 
			return;
		for (int i : table.getSelectionIndices()) {
			IEnvironmentVariable var = ((TabData)tv.getElementAt(i)).var;
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
		table.setSelection(n);
		updateButtons();
	}
	
	private void handleEnvDelButtonSelected(int n) {
		if (n == -1) 
			return;
		for (int i : table.getSelectionIndices()) {
			IEnvironmentVariable var = ((TabData)tv.getElementAt(i)).var;
			if (cfgd == null) 
				vars.deleteVariable(var.getName());
			else
				ce.removeVariable(var.getName(), cfgd);
		}
		updateData();
		int x = table.getItemCount() - 1;
		if (x >= 0) {
			table.setSelection(Math.min(x, n));
			updateButtons();
		}
	}
	
	private void handleEnvAddButtonSelected() {
		IEnvironmentVariable var = null;
		EnvDialog dlg = new EnvDialog(usercomp.getShell(), 
				var, 
				UIMessages.getString("EnvironmentTab.10"), //$NON-NLS-1$ 
				true,
				page.isMultiCfg(),
				cfgd);
		if (dlg.open() == Window.OK) {
			String name = dlg.t1.trim();
			if (name.length() > 0) {
				ICConfigurationDescription[] cfgs;
				if (dlg.toAll)
					cfgs = page.getCfgsEditable();
				else 
					cfgs = new ICConfigurationDescription[] {cfgd};
				if (cfgd == null)
					vars.createVariable(name, dlg.t2.trim(), 
							IEnvironmentVariable.ENVVAR_APPEND, SEPARATOR);
				else
					for (ICConfigurationDescription cfg : cfgs) { 
						ce.addVariable(name, dlg.t2.trim(), 
								IEnvironmentVariable.ENVVAR_APPEND, 
								SEPARATOR, cfg);
					}
				updateData();
				setPos(name);
			}
		}
	}

	private void setPos(String name) {
		if (name == null || name.length() == 0)
			return;
		for (int i=0; i<table.getItemCount(); i++) {
			if (name.equals(table.getItem(i).getText())) {
				table.setSelection(i);
				updateButtons();
				break;
			}
		}
	}
	
	private void handleEnvSelectButtonSelected() {
		// get Environment Variables from the OS
		Map<?,?> v = EnvironmentReader.getEnvVars();
		MyListSelectionDialog dialog = new MyListSelectionDialog(usercomp.getShell(), v, createSelectionDialogContentProvider());
		
		dialog.setTitle(UIMessages.getString("EnvironmentTab.14")); //$NON-NLS-1$
		if (dialog.open() == Window.OK) {
			Object[] selected = dialog.getResult();
			ICConfigurationDescription[] cfgs;
			if (dialog.toAll)
				cfgs = page.getCfgsEditable();
			else 
				cfgs = new ICConfigurationDescription[] {cfgd};
			
			String name = null;
			for (Object element : selected) {
				name = (String)element;
				String value = EMPTY_STR;
				int x = name.indexOf(LBR);
				if (x >= 0) {
					value = name.substring(x + 2, name.length() - 1);
					name = name.substring(0, x);
				}
				
				if (cfgd == null) 
					vars.createVariable(name, value);
				else
					for (ICConfigurationDescription cfg : cfgs) { 
						ce.addVariable(
								name, value, 
								IEnvironmentVariable.ENVVAR_APPEND, 
								SEPARATOR, cfg);
				}
			}
			updateData();
			setPos(name);
		}
	}
	
	private IStructuredContentProvider createSelectionDialogContentProvider() {
		return new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				String[] els = null;
				if (inputElement instanceof Map<?, ?>) {
					@SuppressWarnings("unchecked")
					Map<String, String> m = (Map<String, String>)inputElement;
					els = new String[m.size()];  
					int index = 0;
					for (Iterator<String> iterator = m.keySet().iterator(); iterator.hasNext(); index++) {
						String k = iterator.next();
						els[index] = TextProcessor.process(k + LBR + m.get(k) + RBR);  
					}
				}
				Arrays.sort(els, CDTListComparator.getInstance());
				return els;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		};
	}
	// This page can be displayed for project only
	@Override
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}

	@Override
	protected void performOK() {
		if (vars != null) {
			if (fUserSupplier.setWorkspaceEnvironment(vars))
				if (page instanceof PrefPage_Abstract)
					PrefPage_Abstract.isChanged = true;
		}
		vars = null;
		super.performOK();
	}
	
	@Override
	protected void performCancel() {
		vars = null;
		super.performCancel();
	}
	
	@Override
	protected void performDefaults() {
		ce.restoreDefaults(cfgd); // both for proj & prefs
		vars = null;
		updateData();
	}
	
	private boolean isUsers(IEnvironmentVariable var) {
		return cfgd == null || 
		      (ce.isUserVariable(cfgd, var) &&
			  ((EnvVarDescriptor)var).getContextInfo().getContext() != null);

	}
}
