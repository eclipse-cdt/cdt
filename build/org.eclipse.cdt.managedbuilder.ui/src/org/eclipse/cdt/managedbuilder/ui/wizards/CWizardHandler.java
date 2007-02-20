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
package org.eclipse.cdt.managedbuilder.ui.wizards;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * This object is created per each Project type
 *  
 * It is responsible for:
 * - corresponding line in left pane of 1st wizard page
 * - whole view of right pane, including 
 *
 */
public class CWizardHandler implements ICWizardHandler {
	static final private String head = IDEWorkbenchMessages.getString("CWizardHandler.0"); //$NON-NLS-1$
	static final private String tooltip = 
		IDEWorkbenchMessages.getString("CWizardHandler.1")+ //$NON-NLS-1$
		IDEWorkbenchMessages.getString("CWizardHandler.2") +  //$NON-NLS-1$
		IDEWorkbenchMessages.getString("CWizardHandler.3") + //$NON-NLS-1$
		IDEWorkbenchMessages.getString("CWizardHandler.4") + //$NON-NLS-1$
		IDEWorkbenchMessages.getString("CWizardHandler.5"); //$NON-NLS-1$
	
	protected SortedMap tcs;
	private String name;
	private Image image;
	private Composite parent;
	private Table table;
	private String propertyId = null;
	private IProjectType pt = null;
	private boolean showProps = false;
	private IToolChainListListener listener;
	private boolean supportedOnly = true;
	
	public CWizardHandler(String _name, IProjectType _pt, Image _image, Composite p, IToolChainListListener _listener) {
		tcs = new TreeMap();
		name = _name;
		image = _image;
		parent = p;
		pt = _pt;
		listener = _listener;
	}

	public CWizardHandler(IBuildPropertyValue val, Image _image, Composite p, IToolChainListListener _listener) {
		tcs = new TreeMap();
		name = val.getName();
		propertyId = val.getId();
		image = _image;
		parent = p;
		listener = _listener;
	}
	
	public void handleSelection() {
		if (table == null) {
			table = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			table.setToolTipText(tooltip);
			Iterator it = tcs.keySet().iterator();
			while (it.hasNext()) {
				TableItem ti = new TableItem(table, SWT.NONE);
				String s = (String)it.next();
				Object obj = tcs.get(s);
				if (obj instanceof IToolChain) {
					IToolChain tc = (IToolChain)obj;
					//TODO: add version
					ti.setText(tc.getUniqueRealName());
					ti.setData(tc);
				} else {
					ti.setText(s);
				}
			}
			if (tcs.size() > 0) table.select(0);
			
			table.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (listener != null)
						listener.toolChainListChanged(table.getSelectionCount());
				}});
		}
		table.setVisible(true);
		parent.layout();
	}

	public void handleUnSelection() {
		if (table != null) {
			table.setVisible(false);
		}
	}

	protected void addTc(IToolChain tc) {
		if (tc.isAbstract() || tc.isSystemObject()) return;
		IConfiguration[] cfgs = null;
		// New style managed project type. Configurations are referenced via propertyId.
		if (propertyId != null) { 
			cfgs = ManagedBuildManager.getExtensionConfigurations(tc, ARTIFACT, propertyId);
		// Old style managewd project type. Configs are obtained via projectType
		} else if (pt != null) {
			cfgs = ManagedBuildManager.getExtensionConfigurations(tc, pt);
		} 
		if (cfgs == null || cfgs.length == 0) return;
		tcs.put(tc.getUniqueRealName(), tc);
	}
		
	public IToolChain[] getSelectedToolChains() {
		if (tcs.size() == 0)
			return null;
		else {
			TableItem[] sel = table.getSelection();
			IToolChain[] out = new IToolChain[sel.length];
			for (int i=0; i< sel.length; i++) 
				out[i] = (IToolChain)sel[i].getData();
			return out;
		}
	}

	public void createProject(IProject project, IConfiguration[] cfgs, String[] names) throws CoreException {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.getProjectDescription(project);
		des = coreModel.createProjectDescription(project, true);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);

		if (cfgs == null || cfgs.length == 0)
			cfgs = CConfigWizardPage.getDefaultCfgs(this);
		
		ManagedProject mProj = new ManagedProject(project, cfgs[0].getProjectType());
		info.setManagedProject(mProj);

		for(int i = 0; i < cfgs.length; i++){
			String id = ManagedBuildManager.calculateChildId(cfgs[i].getId(), null);
			Configuration config = new Configuration(mProj, (Configuration)cfgs[i], id, false, true);
			CConfigurationData data = config.getConfigurationData();
			ICConfigurationDescription cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
			config.setConfigurationDescription(cfgDes);
			config.exportArtifactInfo();

			IBuilder bld = config.getEditableBuilder();
			if (bld != null) { 	bld.setManagedBuildOn(true); }
			
			String s = project.getName();
			config.setName(names[i]);
			config.setArtifactName(s);
		}
		coreModel.setProjectDescription(project, des);
	}

// interface methods
	
	public String getHeader() { return head; }
	public String getName() { return name; }
	public Image getIcon() { return image; }
	public boolean isDummy() { return false; }
	public boolean needsConfig() { return true; }
	public int getToolChainsCount() { return tcs.size(); }

	public void setShowProperties(boolean show) { showProps = show; }
	public boolean showProperties() { return showProps; }
	public IProjectType getProjectType() { return pt; }
	public String getPropertyId() { return propertyId; }
	public boolean canCreateWithoutToolchain() { return false; }

	public void setSupportedOnly(boolean supp) { supportedOnly = supp;}
	public boolean supportedOnly() { return supportedOnly; }
}
