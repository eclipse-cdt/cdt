/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig.CfgDiscoveredPathManager;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.make.ui.dialogs.AbstractDiscoveryPage;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.internal.core.InputType;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class DiscoveryTab extends AbstractCBuildPropertyTab implements IBuildInfoContainer {
	/**
	 * @deprecated since CDT 6.1
	 */
	@Deprecated
	protected static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$

	private static final String MAKEFILE_PROJECT_TOOLCHAIN_ID = "org.eclipse.cdt.build.core.prefbase.toolchain"; //$NON-NLS-1$
	private static final String NAMESPACE = "org.eclipse.cdt.make.ui"; //$NON-NLS-1$
	private static final String POINT = "DiscoveryProfilePage"; //$NON-NLS-1$
	private static final String PROFILE_PAGE = "profilePage"; //$NON-NLS-1$
	private static final String PROFILE_ID = "profileId"; //$NON-NLS-1$
	private static final String PROFILE_NAME = "name"; //$NON-NLS-1$
	private static final int DEFAULT_HEIGHT = 150;
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 10, 20 };
	private Label fTableDefinition;
	private Combo scopeComboBox;
	private Table resTable;
	private Group autoDiscoveryGroup;
	private Button autoDiscoveryCheckBox;
	private Button reportProblemsCheckBox;
	private Combo profileComboBox;
	private Composite profileOptionsComposite;

	private ICfgScannerConfigBuilderInfo2Set cbi;
	private Map<InfoContext, Object> baseInfoMap;
	private IScannerConfigBuilderInfo2 buildInfo;
	private CfgInfoContext iContext;
	private List<DiscoveryProfilePageConfiguration> pagesList = null;
	private List<String> visibleProfilesList = null;
	private IPath configPath;
	private AbstractDiscoveryPage[] realPages;
	protected SashForm sashForm;

	private DiscoveryPageWrapper wrapper = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		wrapper = new DiscoveryPageWrapper(this.page, this);
		usercomp.setLayout(new GridLayout(1, false));

		if (page.isForProject() || page.isForPrefs()) {
			Group scopeGroup = setupGroup(usercomp, Messages.getString("DiscoveryTab.0"), //$NON-NLS-1$
					1, GridData.FILL_HORIZONTAL);
			scopeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			scopeComboBox = new Combo(scopeGroup, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			scopeComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			scopeComboBox.add(Messages.getString("DiscoveryTab.1")); //$NON-NLS-1$
			scopeComboBox.add(Messages.getString("DiscoveryTab.2")); //$NON-NLS-1$
			scopeComboBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (cbi == null)
						return;
					cbi.setPerRcTypeDiscovery(scopeComboBox.getSelectionIndex() == 0);
					updateData();
				}
			});
		}

		// Create the sash form
		sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite comp = new Composite(sashForm, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));

		fTableDefinition = new Label(comp, SWT.LEFT);
		fTableDefinition.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		resTable = new Table(comp, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 150;
		resTable.setLayoutData(gd);
		resTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleToolSelected();
			}
		});
		initializeProfilePageMap();

		Composite c = new Composite(sashForm, 0);
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(GridData.FILL_BOTH));

		createScannerConfigControls(c);

		profileOptionsComposite = new Composite(c, SWT.NONE);
		gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		gd.heightHint = Dialog.convertVerticalDLUsToPixels(getFontMetrics(parent), DEFAULT_HEIGHT);
		profileOptionsComposite.setLayoutData(gd);
		profileOptionsComposite.setLayout(new TabFolderLayout());

		sashForm.setWeights(DEFAULT_SASH_WEIGHTS);
	}

	private void createScannerConfigControls(Composite parent) {
		autoDiscoveryGroup = setupGroup(parent, UIMessages.getString("ScannerConfigOptionsDialog.scGroup.label"), //$NON-NLS-1$
				2, GridData.FILL_HORIZONTAL);

		autoDiscoveryCheckBox = setupCheck(autoDiscoveryGroup, UIMessages.getString("ScannerConfigOptionsDialog.scGroup.enabled.button"), //$NON-NLS-1$
				2, GridData.FILL_HORIZONTAL);
		autoDiscoveryCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableAllControls();
				boolean isSCDEnabled = autoDiscoveryCheckBox.getSelection();
				buildInfo.setAutoDiscoveryEnabled(isSCDEnabled);
				if (isSCDEnabled) {
					String id = visibleProfilesList.get(profileComboBox.getSelectionIndex());
					buildInfo.setSelectedProfileId(id);
					handleDiscoveryProfileChanged();
				}
			}
		});
		reportProblemsCheckBox = setupCheck(autoDiscoveryGroup,
				UIMessages.getString("ScannerConfigOptionsDialog.scGroup.problemReporting.enabled.button"), //$NON-NLS-1$
				2, GridData.FILL_HORIZONTAL);
		reportProblemsCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				buildInfo.setProblemReportingEnabled(reportProblemsCheckBox.getSelection());
			}
		});

		// Add profile combo box
		setupLabel(autoDiscoveryGroup, UIMessages.getString("ScannerConfigOptionsDialog.scGroup.selectedProfile.combo"), //$NON-NLS-1$
				1, GridData.BEGINNING);
		profileComboBox = new Combo(autoDiscoveryGroup, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		profileComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		profileComboBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int x = profileComboBox.getSelectionIndex();
				String s = visibleProfilesList.get(x);
				buildInfo.setSelectedProfileId(s);
				handleDiscoveryProfileChanged();
			}
		});
	}

	private void enableAllControls() {
		boolean isSCDEnabled = autoDiscoveryCheckBox.getSelection();
		reportProblemsCheckBox.setEnabled(isSCDEnabled);
		profileComboBox.setEnabled(isSCDEnabled);
		profileOptionsComposite.setVisible(isSCDEnabled);
	}

	@Override
	public void updateData(ICResourceDescription rcfg) {
		if (page.isMultiCfg()) {
			setAllVisible(false, null);
			return;
		} else {
			setAllVisible(true, null);
			configPath = rcfg.getPath();
			IConfiguration cfg = getCfg(rcfg.getConfiguration());
			cbi = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg);
			if (!page.isForPrefs() && baseInfoMap == null) {
				try {
					IProject project = cfg.getOwner().getProject();
					IScannerConfigBuilderInfo2Set baseCbi = ScannerConfigProfileManager.createScannerConfigBuildInfo2Set(project);
					baseInfoMap = baseCbi.getInfoMap();
				} catch (CoreException e) {
				}
			}
			updateData();
		}
	}

	private void updateData() {
		int selScope = 0;
		String lblText = "Tools:";
		if (!cbi.isPerRcTypeDiscovery()) {
			selScope = 1;
			lblText = "Configuration:";
		}
		if (scopeComboBox != null)
			scopeComboBox.select(selScope);
		fTableDefinition.setText(lblText);

		Map<CfgInfoContext, IScannerConfigBuilderInfo2> m = cbi.getInfoMap();
		int pos = resTable.getSelectionIndex();
		resTable.removeAll();
		for (CfgInfoContext ic : m.keySet()) {
			String s = null;
			IResourceInfo rci = ic.getResourceInfo();
			if (rci == null) { // per configuration
				s = ic.getConfiguration().getName();
			} else { // per resource
				if (!configPath.equals(rci.getPath()))
					continue;
				IInputType typ = ic.getInputType();
				if (typ != null)
					s = typ.getName();
				if (s == null) {
					ITool tool = ic.getTool();
					if (tool != null)
						s = tool.getName();
				}
				if (s == null)
					s = Messages.getString("DiscoveryTab.3"); //$NON-NLS-1$
			}
			IScannerConfigBuilderInfo2 bi2 = m.get(ic);
			TableItem ti = new TableItem(resTable, SWT.NONE);
			ti.setText(s);
			ti.setData("cont", ic); //$NON-NLS-1$
			ti.setData("info", bi2); //$NON-NLS-1$
		}
		int len = resTable.getItemCount();
		if (len > 0) {
			setVisibility(null);
			resTable.select((pos < len && pos > -1) ? pos : 0);
			handleToolSelected();
		} else {
			setVisibility(Messages.getString("DiscoveryTab.6")); //$NON-NLS-1$
		}
	}

	private void setVisibility(String errMsg) {
		autoDiscoveryGroup.setVisible(errMsg == null);
		profileOptionsComposite.setVisible(errMsg == null);
		resTable.setEnabled(errMsg == null);
		if (errMsg != null) {
			String[] ss = errMsg.split("\n"); //$NON-NLS-1$
			for (String line : ss)
				new TableItem(resTable, SWT.NONE).setText(line);
		}
	}

	private String getProfileName(String id) {
		int x = id.lastIndexOf("."); //$NON-NLS-1$
		return (x == -1) ? id : id.substring(x + 1);
	}

	private boolean toolContainsProfile(ITool tool, String profileId) {
		IInputType[] inputTypes = ((Tool) tool).getAllInputTypes();
		if (inputTypes == null)
			return false;
		
		for (IInputType inputType : inputTypes) {
			String[] requiedProfiles = getDiscoveryProfileIds(tool, inputType);
			if (requiedProfiles != null) {
				for (String requiredProfile : requiedProfiles) {
					if (profileId.equals(requiredProfile)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * @param toolchain to check
	 * @return if this toolchain is a toolchain created by Makefile project "Other Toolchain".
	 * Note that name of this toolchain set to "No ToolChain" in plugin.xml.
	 */
	private boolean isMakefileProjectToolChain(IToolChain toolchain) {
		return toolchain!=null
			&& (toolchain.getId().equals(MAKEFILE_PROJECT_TOOLCHAIN_ID) || isMakefileProjectToolChain(toolchain.getSuperClass()));
	}
	
	private void handleToolSelected() {
		if (resTable.getSelectionCount() == 0)
			return;

		performOK(false);

		TableItem ti = resTable.getSelection()[0];
		buildInfo = (IScannerConfigBuilderInfo2) ti.getData("info"); //$NON-NLS-1$
		String selectedProfileId = buildInfo.getSelectedProfileId();
		iContext = (CfgInfoContext) ti.getData("cont"); //$NON-NLS-1$
		autoDiscoveryCheckBox.setSelection(buildInfo.isAutoDiscoveryEnabled()
				&& !selectedProfileId.equals(ScannerConfigProfileManager.NULL_PROFILE_ID));
		reportProblemsCheckBox.setSelection(buildInfo.isProblemReportingEnabled());

		profileComboBox.removeAll();
		List<String> profilesList = buildInfo.getProfileIdList();
		Collections.sort(profilesList, CDTListComparator.getInstance());
		visibleProfilesList = new ArrayList<String>(profilesList.size());

		if (realPages != null && realPages.length > 0) {
			for (AbstractDiscoveryPage realPage : realPages) {
				if (realPage != null) {
					realPage.setVisible(false);
					realPage.dispose();
				}
			}
		}

		realPages = new AbstractDiscoveryPage[profilesList.size()];
		String[] labels = new String[profilesList.size()];
		String[] profiles = new String[profilesList.size()];
		int counter = 0;
		int pos = 0;
		ITool[] tools = null;
		IConfiguration conf = iContext.getConfiguration();
		IToolChain toolchain = conf!=null ? conf.getToolChain() : null;
		boolean needPerRcProfile = cbi.isPerRcTypeDiscovery();
		if (!page.isForPrefs()) {
			Tool tool = (Tool) iContext.getTool();
			if (tool != null) {
				tools = new ITool[] { tool };
			} else {
				if (toolchain != null) {
					tools = toolchain.getTools();
				}
				if (tools == null)
					return;
			}
		}
		for (String profileId : profilesList) {
			
			// for Makefile project with default tool-chain any profiles can be selected
			if (!isMakefileProjectToolChain(toolchain) || needPerRcProfile) {
				if (tools != null) {
					boolean foundProfile = false;
					for (ITool tool : tools) {
						foundProfile = toolContainsProfile(tool, profileId);
						if (foundProfile)
							break;
					}
					if (!foundProfile)
						continue;
				}
				if (needPerRcProfile && !CfgScannerConfigProfileManager.isPerFileProfile(profileId))
					continue;
			}

			visibleProfilesList.add(profileId);
			labels[counter] = profiles[counter] = getProfileName(profileId);
			if (profileId.equals(selectedProfileId))
				pos = counter;
			buildInfo.setSelectedProfileId(profileId); // needs to create page
			for (DiscoveryProfilePageConfiguration p : pagesList) {
				if (p != null && p.profId.equals(profileId)) {
					AbstractDiscoveryPage pg = p.getPage();
					if (pg != null) {
						realPages[counter] = pg;
						String s = p.name;
						if (s != null && s.length() > 0)
							labels[counter] = s;
						pg.setContainer(wrapper);
						pg.createControl(profileOptionsComposite);
						profileOptionsComposite.layout(true);
						break;
					}
				}
			}
			counter++;
		}
		profileComboBox.setItems(normalize(labels, profiles, counter));

		buildInfo.setSelectedProfileId(selectedProfileId);
		if (profileComboBox.getItemCount() > 0)
			profileComboBox.select(pos);
		enableAllControls();
		handleDiscoveryProfileChanged();
	}

	private String[] getDiscoveryProfileIds(ITool iTool, IInputType it) {
		String attribute = ((InputType) it).getDiscoveryProfileIdAttribute();
		if (null == attribute)
			return new String[0];
		// FIXME: temporary; we should add new method to IInputType instead of
		// that
		String[] profileIds = attribute.split("\\|"); //$NON-NLS-1$
		for (int i = 0; i < profileIds.length; i++)
			profileIds[i] = profileIds[i].trim();
		return profileIds;
	}

	private String[] normalize(String[] labels, String[] ids, int counter) {
		int mode = CDTPrefUtil.getInt(CDTPrefUtil.KEY_DISC_NAMES);
		String[] tmp = new String[counter];
		// Always show either Name + ID, or ID only
		// These cases do not require checking for doubles.
		if (mode == CDTPrefUtil.DISC_NAMING_ALWAYS_BOTH || mode == CDTPrefUtil.DISC_NAMING_ALWAYS_IDS) {
			for (int i = 0; i < counter; i++) {
				tmp[i] = (mode == CDTPrefUtil.DISC_NAMING_ALWAYS_IDS)
						? ids[i]
						: combine(labels[i], ids[i]);
			}
			return tmp;
		}

		// For not-unique names only, either display ID or name + ID
		boolean doubles = false;
		// quick check for at least one double
		outer:
		for (int i = 0; i < counter; i++) {
			for (int j = 0; j < counter; j++) {
				// sic! i < j, to avoid repeated comparison
				if (i < j && labels[i].equals(labels[j])) {
					doubles = true;
					break outer;
				}
			}
		}
		if (!doubles) { // all names are unique.
			for (int i = 0; i < counter; i++)
				tmp[i] = labels[i];
		} else {
			for (int i = 0; i < counter; i++) {
				doubles = false;
				for (int j = 0; j < counter; j++) {
					if (i != j && labels[i].equals(labels[j])) {
						doubles = true;
						break;
					}
				}
				if (doubles) {
					if (mode == CDTPrefUtil.DISC_NAMING_UNIQUE_OR_IDS)
						tmp[i] = ids[i];
					else
						// replace with Name + Id
						tmp[i] = combine(labels[i], ids[i]);
				} else { // this name is unique - no changes !
					tmp[i] = labels[i];
				}
			}
		}
		return tmp;
	}

	private String combine(String s1, String s2) {
		if (s1.equals(s2))
			return s1;
		else
			return s1 + " (" + s2 + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void handleDiscoveryProfileChanged() {
		int pos = profileComboBox.getSelectionIndex();
		for (int i = 0; i < realPages.length; i++)
			if (realPages[i] != null)
				realPages[i].setVisible(i == pos);
	}

	/**
	 * 
	 */
	private void initializeProfilePageMap() {
		pagesList = new ArrayList<DiscoveryProfilePageConfiguration>(5);
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACE, POINT);
		if (point == null)
			return;
		IConfigurationElement[] infos = point.getConfigurationElements();
		for (IConfigurationElement info : infos) {
			if (info.getName().equals(PROFILE_PAGE)) {
				pagesList.add(new DiscoveryProfilePageConfiguration(info));
			}
		}
	}

	/**
	 * Create a profile page only on request
	 * 
	 * @author vhirsl
	 */
	protected static class DiscoveryProfilePageConfiguration {
		IConfigurationElement fElement;
		private String profId, name;

		protected DiscoveryProfilePageConfiguration(
				IConfigurationElement element) {
			fElement = element;
			profId = fElement.getAttribute(PROFILE_ID);
			name = fElement.getAttribute(PROFILE_NAME);
		}

		protected String getName() {
			return name;
		}

		private AbstractDiscoveryPage getPage() {
			try {
				return (AbstractDiscoveryPage) fElement.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				return null;
			}
		}
	}

	@Override
	public void performApply(ICResourceDescription src,
			ICResourceDescription dst) {
		if (page.isMultiCfg())
			return;
		ICfgScannerConfigBuilderInfo2Set cbi1 = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(getCfg(src.getConfiguration()));
		ICfgScannerConfigBuilderInfo2Set cbi2 = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(getCfg(dst.getConfiguration()));
		cbi2.setPerRcTypeDiscovery(cbi1.isPerRcTypeDiscovery());

		Map<CfgInfoContext, IScannerConfigBuilderInfo2> m1 = cbi1.getInfoMap();
		Map<CfgInfoContext, IScannerConfigBuilderInfo2> m2 = cbi2.getInfoMap();
		for (CfgInfoContext ic : m2.keySet()) {
			if (m1.keySet().contains(ic)) {
				IScannerConfigBuilderInfo2 bi1 = m1.get(ic);
				try {
					cbi2.applyInfo(ic, bi1);
				} catch (CoreException e) {
					ManagedBuilderUIPlugin.log(e);
				}
			} else {
				CUIPlugin.getDefault().logErrorMessage(Messages.getString("DiscoveryTab.7")); //$NON-NLS-1$
			}
		}

		clearChangedDiscoveredInfos();
	}

	@Override
	protected void performOK() {
		performOK(true);
	}

	private void performOK(boolean ok) {
		if (page.isMultiCfg())
			return;
		if (buildInfo == null)
			return;
		String savedId = buildInfo.getSelectedProfileId();
		for (int i = 0; i < realPages.length; i++) {
			if (realPages != null && realPages[i] != null) {
				String s = visibleProfilesList.get(i);
				buildInfo.setSelectedProfileId(s);
				realPages[i].performApply();
				realPages[i].setVisible(false);
			}
		}
		buildInfo.setSelectedProfileId(savedId);
		handleDiscoveryProfileChanged();
		if (ok)
			clearChangedDiscoveredInfos();
	}

	private void clearChangedDiscoveredInfos() {
		IProject project = getProject();
		List<CfgInfoContext> changedContexts = checkChanges();
		for (CfgInfoContext c : changedContexts) {
			CfgDiscoveredPathManager.getInstance().removeDiscoveredInfo(project, c);
			// MakeCorePlugin.getDefault().getDiscoveryManager().removeDiscoveredInfo(c.getProject(), c);
		}
	}

	private List<CfgInfoContext> checkChanges() {
		if (cbi == null || baseInfoMap == null)
			return new ArrayList<CfgInfoContext>(0);

		Map<CfgInfoContext, IScannerConfigBuilderInfo2> cfgInfoMap = cbi.getInfoMap();
		HashMap<InfoContext, Object> baseCopy = new HashMap<InfoContext, Object>(baseInfoMap);
		List<CfgInfoContext> list = new ArrayList<CfgInfoContext>();
		for (Map.Entry<CfgInfoContext, IScannerConfigBuilderInfo2> entry : cfgInfoMap.entrySet()) {
			CfgInfoContext cic = entry.getKey();
			InfoContext c = cic.toInfoContext();
			if (c == null)
				continue;

			IScannerConfigBuilderInfo2 changed = entry.getValue();
			IScannerConfigBuilderInfo2 old = (IScannerConfigBuilderInfo2) baseCopy.remove(c);

			if (old == null) {
				list.add(cic);
			} else if (!settingsEqual(changed, old)) {
				list.add(cic);
			}
		}

		if (baseCopy.size() != 0) {
			IConfiguration cfg = cbi.getConfiguration();
			for (InfoContext c : baseCopy.keySet()) {
				CfgInfoContext cic = CfgInfoContext.fromInfoContext(cfg, c);
				if (cic != null)
					list.add(cic);
			}
		}

		return list;
	}

	private boolean settingsEqual(IScannerConfigBuilderInfo2 info1,
			IScannerConfigBuilderInfo2 info2) {
		if (!CDataUtil.objectsEqual(info1.getSelectedProfileId(), info2.getSelectedProfileId()))
			return false;
		if (!CDataUtil.objectsEqual(info1.getBuildOutputFilePath(), info2.getBuildOutputFilePath()))
			return false;
		if (!CDataUtil.objectsEqual(info1.getContext(), info2.getContext()))
			return false;
		if (!CDataUtil.objectsEqual(info1.getSelectedProfileId(), info2.getSelectedProfileId()))
			return false;
		if (info1.isAutoDiscoveryEnabled() != info2.isAutoDiscoveryEnabled()
				|| info1.isBuildOutputFileActionEnabled() != info2.isBuildOutputFileActionEnabled()
				|| info1.isBuildOutputParserEnabled() != info2.isBuildOutputParserEnabled()
				|| info1.isProblemReportingEnabled() != info2.isProblemReportingEnabled())
			return false;
		if (!listEqual(info1.getProfileIdList(), info2.getProfileIdList()))
			return false;
		if (!listEqual(info1.getProviderIdList(), info2.getProviderIdList()))
			return false;
		return true;
	}

	private boolean listEqual(List<String> l1, List<String> l2) {
		if (l1 == null && l2 == null)
			return true;
		if (l1 == null || l2 == null)
			return false;
		if (l1.size() != l2.size())
			return false;
		// both lists have items in the same order ?
		// since it's most probable, try it first.
		if (l1.equals(l2))
			return true;
		// order may differ...
		for (String s : l1)
			if (!l2.contains(s))
				return false;
		return true;
	}

	@Override
	public boolean canBeVisible() {
		if (page.isMultiCfg()) {
			setAllVisible(false, null);
			return false;
		}
		setAllVisible(true, null);
		if (page.isForProject() || page.isForPrefs())
			return true;
		// Hide this page for folders and files
		// if Discovery scope is "per configuration", not "per resource"
		IConfiguration cfg = getCfg(page.getResDesc().getConfiguration());
		ICfgScannerConfigBuilderInfo2Set _cbi = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg);
		return _cbi.isPerRcTypeDiscovery();
	}

	/**
	 * IBuildInfoContainer methods - called from dynamic pages
	 */
	public IScannerConfigBuilderInfo2 getBuildInfo() {
		return buildInfo;
	}

	public CfgInfoContext getContext() {
		return iContext;
	}

	public IProject getProject() {
		return page.getProject();
	}

	public ICConfigurationDescription getConfiguration() {
		return getResDesc().getConfiguration();
	}

	@Override
	protected void performDefaults() {
		if (page.isMultiCfg())
			return;
		cbi.setPerRcTypeDiscovery(true);
		for (CfgInfoContext cic : cbi.getInfoMap().keySet()) {
			try {
				cbi.applyInfo(cic, null);
			} catch (CoreException e) {
			}
		}
		updateData();
	}

	@Override
	protected void updateButtons() {
		// Do nothing. No buttons to update.
	}
}
