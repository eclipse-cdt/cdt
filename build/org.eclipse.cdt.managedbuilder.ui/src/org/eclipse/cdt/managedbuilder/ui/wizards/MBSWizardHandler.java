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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
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
public class MBSWizardHandler extends CWizardHandler implements ICBuildWizardHandler {
	private static final String PROPERTY = "org.eclipse.cdt.build.core.buildType"; //$NON-NLS-1$
	private static final String PROP_VAL = PROPERTY + ".debug"; //$NON-NLS-1$
	private static final String tooltip = 
		Messages.getString("CWizardHandler.1")+ //$NON-NLS-1$
		Messages.getString("CWizardHandler.2") + //$NON-NLS-1$
		Messages.getString("CWizardHandler.3") + //$NON-NLS-1$
		Messages.getString("CWizardHandler.4") + //$NON-NLS-1$
		Messages.getString("CWizardHandler.5"); //$NON-NLS-1$
	
	protected SortedMap tcs = new TreeMap();
	private String propertyId = null;
	private IProjectType pt = null;
	protected IWizardItemsListListener listener;
	protected CDTConfigWizardPage fConfigPage;
	private IToolChain[] savedToolChains = null;
	private IWizard wizard;
	
	public MBSWizardHandler(String _name, IProjectType _pt, Image _image, Composite p, IWizardItemsListListener _listener, IWizard w) {
		super(p, Messages.getString("CWizardHandler.0"), _name, _image); //$NON-NLS-1$
		pt = _pt;
		listener = _listener;
		wizard = w;
	}

	public MBSWizardHandler(IBuildPropertyValue val, Image _image, Composite p, IWizardItemsListListener _listener, IWizard w) {
		super(p, Messages.getString("CWizardHandler.0"), val.getName(), _image); //$NON-NLS-1$
		propertyId = val.getId();
		listener = _listener;
		wizard = w;
	}
	
	public void handleSelection() {
		List preferred = CDTPrefUtil.getPreferredTCs();
		
		if (table == null) {
			table = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			table.setToolTipText(tooltip);
			Iterator it = tcs.keySet().iterator();
			int counter = 0;
			int position = 0;
			while (it.hasNext()) {
				TableItem ti = new TableItem(table, SWT.NONE);
				String s = (String)it.next();
				Object obj = tcs.get(s);
				String id = CDTPrefUtil.NULL;
				if (obj instanceof IToolChain) {
					IToolChain tc = (IToolChain)obj;
					String name = tc.getUniqueRealName();
					id = tc.getId();
					//TODO: add version
					ti.setText(name);
					ti.setData(tc);
				} else { // NULL for -NO TOOLCHAIN-
					ti.setText(s);
				}
				if (position == 0 && preferred.contains(id)) position = counter;
				counter++;
			}
			if (tcs.size() > 0) table.select(position);
			
			table.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (listener != null)
						listener.toolChainListChanged(table.getSelectionCount());
				}});
		}
		updatePreferred(preferred);
		table.setVisible(true);
		parent.layout();
		if (fConfigPage != null) fConfigPage.pagesLoaded = false;
	}

	public void handleUnSelection() {
		if (table != null) {
			table.setVisible(false);
		}
		if (fConfigPage != null) fConfigPage.pagesLoaded = false;
	}

	public void addTc(IToolChain tc) {
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
		
	public void createProject(IProject project, boolean defaults) throws CoreException {
		CoreModel coreModel = CoreModel.getDefault();
		CfgHolder[] cfgs = fConfigPage.getCfgItems(defaults);
		ICProjectDescription des = coreModel.createProjectDescription(project, false);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);

		if (cfgs == null || cfgs.length == 0) 
			cfgs = CDTConfigWizardPage.getDefaultCfgs(this);
		
		if (cfgs[0].getConfiguration() == null) {
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderUIPlugin.getUniqueIdentifier(),
					Messages.getString("CWizardHandler.6"))); //$NON-NLS-1$
		}
		Configuration cf = (Configuration)cfgs[0].getConfiguration();
		ManagedProject mProj = new ManagedProject(project, cf.getProjectType());
		info.setManagedProject(mProj);

		cfgs = CfgHolder.unique(cfgs);
		
		ICConfigurationDescription active = null;
		
		for(int i = 0; i < cfgs.length; i++){
			cf = (Configuration)cfgs[i].getConfiguration();
			String id = ManagedBuildManager.calculateChildId(cf.getId(), null);
			Configuration config = new Configuration(mProj, cf, id, false, true);
			CConfigurationData data = config.getConfigurationData();
			ICConfigurationDescription cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
			config.setConfigurationDescription(cfgDes);
			config.exportArtifactInfo();

			IBuilder bld = config.getEditableBuilder();
			if (bld != null) { 	bld.setManagedBuildOn(true); }
			
			String s = project.getName();
			config.setName(cfgs[i].getName());
			config.setArtifactName(s);
			
			IBuildProperty b = config.getBuildProperties().getProperty(PROPERTY);
			if (b != null && b.getValue() != null && PROP_VAL.equals(b.getValue().getId()))
				active = cfgDes;
			else if (active == null) // select at least first configuration 
				active = cfgDes; 
		}
		if (active != null) active.setActive();
		coreModel.setProjectDescription(project, des);
		
		// process custom pages
		doCustom();
	}

	public IWizardPage getSpecificPage() {
		if (fConfigPage == null) {
			fConfigPage = new CDTConfigWizardPage(this);
		}
		return fConfigPage; 
	}
	
	/**
	 * Mark preferred toolchains with specific images
	 */
	public void updatePreferred(List prefs) {
		int x = table.getItemCount();
		for (int i=0; i<x; i++) {
			TableItem ti = table.getItem(i);
			IToolChain tc = (IToolChain)ti.getData();
			String id = (tc == null) ? CDTPrefUtil.NULL : tc.getId();
			ti.setImage( prefs.contains(id) ? IMG1 : IMG0);
		}
	}
	public String getHeader() { return head; }
	public String getName() { return name; }
	public Image getIcon() { return image; }
	public boolean isDummy() { return false; }
	public boolean supportsPreferred() { return true; }

	public boolean isChanged() { 
		if (savedToolChains == null)
			return true;
		IToolChain[] tcs = getSelectedToolChains();
		if (savedToolChains.length != tcs.length) 
			return true;
		for (int i=0; i<savedToolChains.length; i++) {
			boolean found = false;
			for (int j=0; j<tcs.length; j++) {
				if (savedToolChains[i] == tcs[j]) {
					found = true; break;
				}
			}
			if (!found) return true;
		}
		return false;
	}
	
	public void saveState() {
		savedToolChains = getSelectedToolChains();
	}
	
	// Methods specific for MBSWizardHandler

	public IToolChain[] getSelectedToolChains() {
		TableItem[] tis = table.getSelection();
		if (tis == null || tis.length == 0)
			return new IToolChain[0];
		IToolChain[] ts = new IToolChain[tis.length];
		for (int i=0; i<tis.length; i++) {
			ts[i] = (IToolChain)tis[i].getData();
		}
		return ts;
	}
	public int getToolChainsCount() {
		return tcs.size();
	}
	public String getPropertyId() {
		return propertyId;
	}
	public IProjectType getProjectType() {
		return pt;
	}
	public IWizard getWizard() {
		return wizard;
	}
	public CfgHolder[] getCfgItems(boolean defaults) {
		return fConfigPage.getCfgItems(defaults);
	}
	public String getErrorMessage() { 
		TableItem[] tis = table.getSelection();
		if (tis == null || tis.length == 0)
			return Messages.getString("MBSWizardHandler.0"); //$NON-NLS-1$
		if (fConfigPage != null && fConfigPage.isVisible && !fConfigPage.isCustomPageComplete())
			return Messages.getString("MBSWizardHandler.1"); //$NON-NLS-1$
		return null;
	}
	
	private void doCustom() {
		IRunnableWithProgress[] operations = MBSCustomPageManager.getOperations();
		if(operations != null)
			for(int k = 0; k < operations.length; k++)
				try {
					wizard.getContainer().run(false, true, operations[k]);
				} catch (InvocationTargetException e) {
					ManagedBuilderUIPlugin.log(e);
				} catch (InterruptedException e) {
					ManagedBuilderUIPlugin.log(e);
				}
	}
	
	public void postProcess(IProject newProject) {
		deleteExtraConfigs(newProject);
	}
	
	private void deleteExtraConfigs(IProject newProject) {
		if (isChanged()) return; // no need to delete 
		if (listener.isCurrent()) return; // nothing to delete
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(newProject, true);
		if (prjd == null) return;
		ICConfigurationDescription[] all = prjd.getConfigurations();
		if (all == null) return;
		CfgHolder[] req = getCfgItems(false);
		boolean modified = false;
		for (int i=0; i<all.length; i++) {
			boolean found = false;
			for (int j=0; j<req.length; j++) {
				if (all[i].getName().equals(req[j].getName())) {
					found = true; break;
				}
			}
			if (!found) {
				modified = true;
				prjd.removeConfiguration(all[i]);
			}
		}
		if (modified) try {
			CoreModel.getDefault().setProjectDescription(newProject, prjd);
		} catch (CoreException e) {}
	}

}
