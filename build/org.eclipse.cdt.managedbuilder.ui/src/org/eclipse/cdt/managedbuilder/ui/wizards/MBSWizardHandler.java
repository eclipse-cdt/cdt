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
package org.eclipse.cdt.managedbuilder.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildException;
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
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.cdt.ui.templateengine.Template;
import org.eclipse.cdt.ui.templateengine.TemplateEngineUI;
import org.eclipse.cdt.ui.templateengine.TemplateEngineUIUtil;
import org.eclipse.cdt.ui.templateengine.pages.UIWizardPage;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * This object is created per each Project type
 *  
 * It is responsible for:
 * - corresponding line in left pane of 1st wizard page
 * - whole view of right pane, including 
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MBSWizardHandler extends CWizardHandler {
	public static final String ARTIFACT = "org.eclipse.cdt.build.core.buildArtefactType";  //$NON-NLS-1$
	public static final String EMPTY_STR = "";  //$NON-NLS-1$
	
	private static final String PROPERTY = "org.eclipse.cdt.build.core.buildType"; //$NON-NLS-1$
	private static final String PROP_VAL = PROPERTY + ".debug"; //$NON-NLS-1$
	private static final String tooltip = 
		Messages.getString("CWizardHandler.1")+ //$NON-NLS-1$
		Messages.getString("CWizardHandler.2") + //$NON-NLS-1$
		Messages.getString("CWizardHandler.3") + //$NON-NLS-1$
		Messages.getString("CWizardHandler.4") + //$NON-NLS-1$
		Messages.getString("CWizardHandler.5"); //$NON-NLS-1$
	
	protected SortedMap<String, IToolChain> full_tcs = new TreeMap<String, IToolChain>();
	private String propertyId = null;
	private IProjectType pt = null;
	protected IWizardItemsListListener listener;
	protected CDTConfigWizardPage fConfigPage;
	private IToolChain[] savedToolChains = null;
	private IWizard wizard;
	private IWizardPage startingPage;
//	private EntryDescriptor entryDescriptor = null;
	private EntryInfo entryInfo;
	protected CfgHolder[] cfgs = null;
	protected IWizardPage[] customPages;
	
	/**
	 * Current list of preferred toolchains
	 */
	private List<String> preferredTCs = new ArrayList<String>();
		
	protected static final class EntryInfo {
		private SortedMap<String, IToolChain> tcs;
		private EntryDescriptor entryDescriptor;
		private Template template;
		private boolean initialized;
		private boolean isValid;
		private String projectTypeId;
		private String templateId;
		private IWizardPage[] templatePages;
		private IWizardPage predatingPage;
		private IWizardPage followingPage;
		private IWizard wizard;
		
		public EntryInfo(EntryDescriptor dr, SortedMap<String, IToolChain> _tcs){
			entryDescriptor = dr;
			tcs = _tcs;
		}
	
		/**
		 * @since 5.1
		 */
		public EntryInfo(EntryDescriptor dr, SortedMap<String, IToolChain> _tcs, IWizard w){
			this(dr, _tcs);
			wizard = w;
		}
		
		public boolean isValid(){
			initialize();
			return isValid;
		}

		public Template getTemplate(){
			initialize();
			return template;
		}
		
		public EntryDescriptor getDescriptor(){
			return entryDescriptor;
		}

		private void initialize(){
			if(initialized)
				return;
			
			do {
				if(entryDescriptor == null)
					break;
				String path[] = entryDescriptor.getPathArray();
				if(path == null || path.length == 0)
					break;
			
				projectTypeId = path[0];
				if(!entryDescriptor.isDefaultForCategory() && 
						path.length > 1 && (!path[0].equals(ManagedBuildWizard.OTHERS_LABEL))){
					templateId = path[path.length - 1]; 
					Template templates[] = null; 
					if(wizard instanceof ICDTCommonProjectWizard) {
						ICDTCommonProjectWizard wz = (ICDTCommonProjectWizard)wizard;
						String[] langIDs = wz.getLanguageIDs();
						if(langIDs.length > 0) {
							List<Template> lstTemplates = new ArrayList<Template>();
							for (String id : langIDs) {
								lstTemplates.addAll(Arrays.asList(TemplateEngineUI.getDefault().
									getTemplates(projectTypeId, null, id)));
							} 
							templates = lstTemplates.toArray(new Template[lstTemplates.size()]);
						}
					} 
					if(null == templates) {
						 templates = TemplateEngineUI.getDefault().getTemplates(projectTypeId);
					}
					if((null == templates) || (templates.length == 0))
						break;
					
					for (Template t : templates) {
						if(t.getTemplateId().equals(templateId)){
							template = t;
							break;
						}
					}
					
					if(template == null)
						break;
				}
				
				isValid = true;
			} while(false);

			initialized = true;
		}
		
		public Template getInitializedTemplate(IWizardPage predatingPage, IWizardPage followingPage, Map<String, String> map){
			getNextPage(predatingPage, followingPage);
			
			Template template = getTemplate();
			
			if(template != null){
				Map<String, String> valueStore = template.getValueStore();
//				valueStore.clear();
				for (IWizardPage page : templatePages) {
					if (page instanceof UIWizardPage)
						valueStore.putAll(((UIWizardPage)page).getPageData());
					if (page instanceof IWizardDataPage)
						valueStore.putAll(((IWizardDataPage)page).getPageData());
				}
				if (map != null) {
					valueStore.putAll(map);
				}
			}
			return template;
		}
		
		public IWizardPage getNextPage(IWizardPage predatingPage, IWizardPage followingPage) {
			initialize();
			if(this.templatePages == null 
					|| this.predatingPage != predatingPage 
					|| this.followingPage != followingPage){
				this.predatingPage = predatingPage;
				this.followingPage = followingPage;
				if (template != null) {
					this.templatePages = template.getTemplateWizardPages(predatingPage, followingPage, predatingPage.getWizard());
				} else {
					templatePages = new IWizardPage[0];
					followingPage.setPreviousPage(predatingPage);
				}
			}
			
			if(templatePages.length != 0)
				return templatePages[0];
			return followingPage;
		}
		
		private boolean canFinish(IWizardPage predatingPage, IWizardPage followingPage){
			getNextPage(predatingPage, followingPage);
			for(int i = 0; i < templatePages.length; i++){
				if(!templatePages[i].isPageComplete())
					return false;
			}
			return true;
		}
		
		/**
		 * Filters toolchains   
		 * 
		 * @return - set of compatible toolchain's IDs
		 */
		protected Set<String> tc_filter() {
			Set<String> full = tcs.keySet();
			if (entryDescriptor == null) 
				return full;
			Set<String> out = new LinkedHashSet<String>(full.size());
			for (String s : full)
				if (isToolChainAcceptable(s)) 
					out.add(s);
			return out;
		}

		/**
		 * Checks whether given toolchain can be displayed
		 * 
		 * @param tcId - toolchain _NAME_ to check
		 * @return - true if toolchain can be displayed
		 */
		public boolean isToolChainAcceptable(String tcId) {
			if (template == null || template.getTemplateInfo() == null) 
				return true;
			
			String[] toolChainIds = template.getTemplateInfo().getToolChainIds();
			if (toolChainIds == null || toolChainIds.length == 0) 
				return true;
			
			Object ob = tcs.get(tcId);
			if (ob == null)
				return true; // sic ! This can occur with Other Toolchain only
			if (!(ob instanceof IToolChain))
				return false;
			
			String id1 = ((IToolChain)ob).getId();
			IToolChain sup = ((IToolChain)ob).getSuperClass();
			String id2 = sup == null ? null : sup.getId();
			
			for (String id : toolChainIds) {
				if ((id != null && id.equals(id1)) ||
					(id != null && id.equals(id2)))
					return true;
			}
			return false;
		}

		public int getToolChainsCount() {
			return tc_filter().size();
		}
	}
	
	public MBSWizardHandler(IProjectType _pt, Composite p, IWizard w) {
		super(p, Messages.getString("CWizardHandler.0"), _pt.getName()); //$NON-NLS-1$
		pt = _pt;
		setWizard(w);
	}

	public MBSWizardHandler(String name, Composite p, IWizard w) {
		super(p, Messages.getString("CWizardHandler.0"), name); //$NON-NLS-1$
		setWizard(w);
	}

	public MBSWizardHandler(IBuildPropertyValue val, Composite p, IWizard w) {
		super(p, Messages.getString("CWizardHandler.0"), val.getName()); //$NON-NLS-1$
		propertyId = val.getId();
		setWizard(w);
	}
	private void setWizard(IWizard w) {
		if (w != null) {
			if (w.getStartingPage() instanceof IWizardItemsListListener)
				listener = (IWizardItemsListListener)w.getStartingPage();
			wizard = w;
			startingPage = w.getStartingPage();
		}
	}
	
	protected IWizardPage getStartingPage(){
		return startingPage;
	}
	
	public Map<String, String> getMainPageData() {
		WizardNewProjectCreationPage page = (WizardNewProjectCreationPage)getStartingPage();
		Map<String, String> data = new HashMap<String, String>();
		String projName = page.getProjectName();
		projName = projName != null ? projName.trim() : EMPTY_STR; 
		data.put("projectName", projName); //$NON-NLS-1$
		data.put("baseName", getBaseName(projName)); //$NON-NLS-1$
		data.put("baseNameUpper", getBaseName(projName).toUpperCase() ); //$NON-NLS-1$
		data.put("baseNameLower", getBaseName(projName).toLowerCase() ); //$NON-NLS-1$
		String location = page.getLocationPath().toOSString();
		if(location == null)
			location = EMPTY_STR;
		data.put("location", location); //getProjectLocation().toPortableString()); //$NON-NLS-1$
		return data;
	}
	
	private String getBaseName(String name) {
		String baseName = name;
		int dot = baseName.lastIndexOf('.');
		if (dot != -1) {
			baseName = baseName.substring(dot + 1);
		}
		dot = baseName.indexOf(' ');
		if (dot != -1) {
			baseName = baseName.substring(0, dot);
		}
		return baseName;
	}
	
	@Override
	public void handleSelection() {
		List<String> preferred = CDTPrefUtil.getPreferredTCs();
		
		if (table == null) {
			table = new Table(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			table.getAccessible().addAccessibleListener(
					new AccessibleAdapter() {
						@Override
						public void getName(AccessibleEvent e) {
							if (e.result == null)
								e.result = head;
						}
					});
			table.setToolTipText(tooltip);
			if (entryInfo != null) {
				int counter = 0;
				int position = 0;
				for (String s : entryInfo.tc_filter()) {
					TableItem ti = new TableItem(table, SWT.NONE);
					Object obj = full_tcs.get(s);
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
				if (counter > 0) table.select(position);
			}			
			table.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleToolChainSelection();
				}
			});
		}
		updatePreferred(preferred);
		loadCustomPages();
		table.setVisible(true);
		parent.layout();
		if (fConfigPage != null) fConfigPage.pagesLoaded = false;
	}

	private void handleToolChainSelection() {
		loadCustomPages();
		// Notify listener, if any.
		if (listener != null)
			listener.toolChainListChanged(table.getSelectionCount());
	}
	
	private void loadCustomPages() {
		if (! (getWizard() instanceof ICDTCommonProjectWizard)) 
			return; // not probable 
		
		ICDTCommonProjectWizard wz = (ICDTCommonProjectWizard)getWizard();
		
		if (customPages == null) {
			MBSCustomPageManager.init();
			MBSCustomPageManager.addStockPage(getStartingPage(), CDTMainWizardPage.PAGE_ID);
			MBSCustomPageManager.addStockPage(getConfigPage(), CDTConfigWizardPage.PAGE_ID);

			// load all custom pages specified via extensions
			try	{
				MBSCustomPageManager.loadExtensions();
			} catch (BuildException e) { e.printStackTrace(); }

			customPages = MBSCustomPageManager.getCustomPages();

			if (customPages == null) 
				customPages = new IWizardPage[0];
			
			for (IWizardPage customPage : customPages)
				customPage.setWizard(wz);
		}
		setCustomPagesFilter(wz);
	}

	private void setCustomPagesFilter(ICDTCommonProjectWizard wz) {
		String[] natures = wz.getNatures();
		if (natures == null || natures.length == 0)
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.NATURE, null);
		else if (natures.length == 1)
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.NATURE, natures[0]);
		else {
			TreeSet<String> x = new TreeSet<String>();
			for (String nature : natures)
				x.add(nature);
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.NATURE, x);
		}
		// Project type can be obtained either from Handler (for old-style projects),
		// or multiple values will be got from separate ToolChains (for new-style).
		boolean ptIsNull = (getProjectType() == null);
		if (!ptIsNull)
			MBSCustomPageManager.addPageProperty(
					MBSCustomPageManager.PAGE_ID, 
					MBSCustomPageManager.PROJECT_TYPE, 
					getProjectType().getId()
				);

		IToolChain[] tcs = getSelectedToolChains();
		ArrayList<IToolChain> x = new ArrayList<IToolChain>();			
		TreeSet<String> y = new TreeSet<String>();
		if (tcs!=null) {
			int n = tcs.length;
			for (int i=0; i<n; i++) {
				if (tcs[i] == null) // --- NO TOOLCHAIN ---
					continue;       // has no custom pages.
				x.add(tcs[i]);
	
				IConfiguration cfg = tcs[i].getParent();
				if (cfg == null)
					continue;
				IProjectType pt = cfg.getProjectType();
				if (pt != null)
					y.add(pt.getId());
			}
		}
		MBSCustomPageManager.addPageProperty(
				MBSCustomPageManager.PAGE_ID, 
				MBSCustomPageManager.TOOLCHAIN, 
				x);
		
		if (ptIsNull) {
			if (y.size() > 0)
				MBSCustomPageManager.addPageProperty(
						MBSCustomPageManager.PAGE_ID, 
						MBSCustomPageManager.PROJECT_TYPE, 
						y);
			else
				MBSCustomPageManager.addPageProperty(
						MBSCustomPageManager.PAGE_ID, 
						MBSCustomPageManager.PROJECT_TYPE, 
						null);
		}
	}
	
	@Override
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
		full_tcs.put(tc.getUniqueRealName(), tc);
	}
		
	@Override
	public void createProject(IProject project, boolean defaults, boolean onFinish, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask("", 100); //$NON-NLS-1$
			setProjectDescription(project, defaults, onFinish, monitor);
			doTemplatesPostProcess(project);
			doCustom(project);
			monitor.worked(30);
		} finally {
			monitor.done();
		}
	}

	@Override
	public void convertProject(IProject proj, IProgressMonitor monitor) throws CoreException {
		setProjectDescription(proj, true, true, monitor);
	}
	
	private void setProjectDescription(IProject project, boolean defaults, boolean onFinish, IProgressMonitor monitor) throws CoreException {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription des = mngr.createProjectDescription(project, false, !onFinish);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		monitor.worked(10);
		cfgs = getCfgItems(false);
		if (cfgs == null || cfgs.length == 0) 
			cfgs = CDTConfigWizardPage.getDefaultCfgs(this);
		
		if (cfgs == null || cfgs.length == 0 || cfgs[0].getConfiguration() == null) {
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderUIPlugin.getUniqueIdentifier(),
					Messages.getString("CWizardHandler.6"))); //$NON-NLS-1$
		}
		Configuration cf = (Configuration)cfgs[0].getConfiguration();
		ManagedProject mProj = new ManagedProject(project, cf.getProjectType());
		info.setManagedProject(mProj);
		monitor.worked(10);
		cfgs = CfgHolder.unique(cfgs);
		cfgs = CfgHolder.reorder(cfgs);
		
		ICConfigurationDescription cfgDebug = null;
		ICConfigurationDescription cfgFirst = null;
		
		int work = 50/cfgs.length;
		
		for (CfgHolder cfg : cfgs) {
			cf = (Configuration)cfg.getConfiguration();
			String id = ManagedBuildManager.calculateChildId(cf.getId(), null);
			Configuration config = new Configuration(mProj, cf, id, false, true);
			CConfigurationData data = config.getConfigurationData();
			ICConfigurationDescription cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
			config.setConfigurationDescription(cfgDes);
			config.exportArtifactInfo();

			IBuilder bld = config.getEditableBuilder();
			if (bld != null) { 	bld.setManagedBuildOn(true); }
			
			config.setName(cfg.getName());
			config.setArtifactName(mProj.getDefaultArtifactName());
			
			IBuildProperty b = config.getBuildProperties().getProperty(PROPERTY);
			if (cfgDebug == null && b != null && b.getValue() != null && PROP_VAL.equals(b.getValue().getId()))
				cfgDebug = cfgDes;
			if (cfgFirst == null) // select at least first configuration 
				cfgFirst = cfgDes; 
			monitor.worked(work);
		}
		mngr.setProjectDescription(project, des);
	}
	
	@Override
	protected void doTemplatesPostProcess(IProject prj) {
		if(entryInfo == null)
			return;
		
		Template template = entryInfo.getInitializedTemplate(getStartingPage(), getConfigPage(), getMainPageData());
		if(template == null)
			return;

		List<IConfiguration> configs = new ArrayList<IConfiguration>();
		for (CfgHolder cfg : cfgs) {
			configs.add((IConfiguration)cfg.getConfiguration());
		}
		template.getTemplateInfo().setConfigurations(configs);

		IStatus[] statuses = template.executeTemplateProcesses(null, false);
		if (statuses.length == 1 && statuses[0].getException() instanceof ProcessFailureException) {
			TemplateEngineUIUtil.showError(statuses[0].getMessage(), statuses[0].getException());
		}
	}
	
	protected CDTConfigWizardPage getConfigPage() {
		if (fConfigPage == null) {
			fConfigPage = new CDTConfigWizardPage(this);
		}
		return fConfigPage;
	}
	
	@Override
	public IWizardPage getSpecificPage() {
		return entryInfo.getNextPage(getStartingPage(), getConfigPage());
	}
	
	/**
	 * Mark preferred toolchains with specific images
	 * @
	 */
	
	@Override
	public void updatePreferred(List<String> prefs) {
		preferredTCs.clear();
		int x = table.getItemCount();
		for (int i=0; i<x; i++) {
			TableItem ti = table.getItem(i);
			IToolChain tc = (IToolChain)ti.getData();
			if (tc!=null && prefs.contains(tc.getId())) {
				ti.setImage(IMG1);
				preferredTCs.add(tc.getName());
			} else {
				ti.setImage(IMG0);
			}
		}
	}
	
	public List<String> getPreferredTCNames() {
		return preferredTCs;
	}
	
	@Override
	public String getHeader() { return head; }
	public boolean isDummy() { return false; }
	@Override
	public boolean supportsPreferred() { return true; }

	@Override
	public boolean isChanged() { 
		if (savedToolChains == null)
			return true;
		IToolChain[] tcs = getSelectedToolChains();
		if (savedToolChains.length != tcs.length) 
			return true;
		for (IToolChain savedToolChain : savedToolChains) {
			boolean found = false;
			for (IToolChain tc : tcs) {
				if (savedToolChain == tc) {
					found = true;
					break;
				}
			}
			if (!found)
				return true;
		}
		return false;
	}
	
	@Override
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
		if (entryInfo == null)
			return full_tcs.size();
		else 
			return entryInfo.tc_filter().size();
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
		getConfigPage(); // ensure that page is created
		return fConfigPage.getCfgItems(defaults);
	}
	@Override
	public String getErrorMessage() { 
		TableItem[] tis = table.getSelection();
		if (tis == null || tis.length == 0)
			return Messages.getString("MBSWizardHandler.0"); //$NON-NLS-1$
		return null;
	}
	
	@Override
	protected void doCustom(IProject newProject) {
		IRunnableWithProgress[] operations = MBSCustomPageManager.getOperations();
		if(operations != null)
			for (IRunnableWithProgress op: operations)
				try {
					wizard.getContainer().run(false, true, op);
				} catch (InvocationTargetException e) {
					ManagedBuilderUIPlugin.log(e);
				} catch (InterruptedException e) {
					ManagedBuilderUIPlugin.log(e);
				}
	}
	
	@Override
	public void postProcess(IProject newProject, boolean created) {
		deleteExtraConfigs(newProject);
		// calls are required only if the project was
		// created before for <Advanced Settings> feature.
		if (created) {
			doTemplatesPostProcess(newProject);
			doCustom(newProject);
		}
	}
	
	/**
	 * Deletes configurations 
	 * 
	 * @param newProject - affected project
	 */
	private void deleteExtraConfigs(IProject newProject) {
		if (isChanged()) return; // no need to delete 
		if (listener != null && listener.isCurrent()) return; // nothing to delete
		if (fConfigPage == null || !fConfigPage.pagesLoaded) return;
		
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(newProject, true);
		if (prjd == null) return;
		ICConfigurationDescription[] all = prjd.getConfigurations();
		if (all == null) return;
		CfgHolder[] req = getCfgItems(false);
		boolean modified = false;
		for (ICConfigurationDescription cfgDes : all) {
			boolean found = false;
			for (CfgHolder cfgh : req) {
				if (cfgDes.getName().equals(cfgh.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				modified = true;
				prjd.removeConfiguration(cfgDes);
			}
		}
		if (modified) try {
			CoreModel.getDefault().setProjectDescription(newProject, prjd);
		} catch (CoreException e) {}
	}
	
	@Override
	public boolean isApplicable(EntryDescriptor data) { 
		EntryInfo info = new EntryInfo(data, full_tcs, wizard);
		return info.isValid() && (info.getToolChainsCount() > 0);
	}
	
	@Override
	public void initialize(EntryDescriptor data) throws CoreException {
		EntryInfo info = new EntryInfo(data, full_tcs, wizard);
		if(!info.isValid())
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderUIPlugin.getUniqueIdentifier(), "inappropriate descriptor")); //$NON-NLS-1$
		
		entryInfo = info;
	}

	/**
	 * Clones itself.
	 */
	@Override
	public Object clone() {
		MBSWizardHandler clone = (MBSWizardHandler)super.clone();
		if (clone != null) {
			clone.propertyId = propertyId;
			clone.pt = pt;
			clone.listener = listener;
			clone.wizard = wizard;
			clone.entryInfo = entryInfo; // the same !
			clone.fConfigPage = fConfigPage; // the same !
			clone.full_tcs = full_tcs;       // the same !
		}
		return clone;
	}

	@Override
	public boolean canFinish() {
		if(entryInfo == null)
			return false;
		
		if (!getConfigPage().isCustomPageComplete())
			return false;
		
		if(!entryInfo.canFinish(startingPage, getConfigPage()))
			return false;
		
		if (customPages != null)
			for (int i=0; i<customPages.length; i++)
				if (!customPages[i].isPageComplete())
					return false;
		
		return super.canFinish();
	}
	
	
}
