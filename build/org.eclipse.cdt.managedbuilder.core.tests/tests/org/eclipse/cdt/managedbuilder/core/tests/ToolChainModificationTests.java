/*******************************************************************************
 * Copyright (c) 2007, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.tcmodification.IConfigurationModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IFolderInfoModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IModificationOperation;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolChainModificationManager;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolModification;
import org.eclipse.cdt.managedbuilder.testplugin.BuildSystemTestHelper;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ToolChainModificationTests extends TestCase {
	private static final String PROJ_NAME_PREFIX = "TCM_";

	public static Test suite() {
		TestSuite suite = new TestSuite(ToolChainModificationTests.class);

		return suite;
	}

	public void testRootToolChainStatus() throws Exception {
		final String projName = PROJ_NAME_PREFIX + "p1";
		IProject project = BuildSystemTestHelper.createProject(projName);
		BuildSystemTestHelper.createDescription(project, "tcm.pt");
		ICProjectDescriptionManager desMngr = CoreModel.getDefault().getProjectDescriptionManager();

		ICProjectDescription des = desMngr.getProjectDescription(project);
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(des.getConfigurations()[0]);
		IToolChainModificationManager mngr = ManagedBuildManager.getToolChainModificationManager();

		IConfigurationModification cfgM = (IConfigurationModification) mngr.createModification(cfg.getRootFolderInfo());
		assertTrue(cfgM.isToolChainCompatible());
		assertTrue(cfgM.isBuilderCompatible());

		IToolChain[] ctcs = cfgM.getCompatibleToolChains();
		HashSet<IHoldsOptions> set = new HashSet<>();
		FolderInfo foInfo = (FolderInfo) cfg.getRootFolderInfo();
		ToolChain tc = (ToolChain) foInfo.getToolChain();
		IToolChain[] allSys = ManagedBuildManager.getRealToolChains();
		filterPropsSupported(foInfo, tc, allSys, set);
		set.remove(ManagedBuildManager.getRealToolChain(tc));
		IToolChain incompatibleTc = ManagedBuildManager.getExtensionToolChain("tcm.tc2");
		incompatibleTc = ManagedBuildManager.getRealToolChain(incompatibleTc);
		set.remove(incompatibleTc);
		compare(Arrays.asList(ctcs), set);

		HashSet<IToolChain> incomp = new HashSet<>(Arrays.asList(allSys));
		incomp.removeAll(Arrays.asList(ctcs));
		assertTrue(incomp.contains(incompatibleTc));

		IBuilder[] cbs = cfgM.getCompatibleBuilders();
		Set<IHoldsOptions> bSet = new HashSet<>();
		IBuilder[] allSysB = ManagedBuildManager.getRealBuilders();
		filterPropsSupported(cfg, allSysB, bSet);
		IBuilder incompatibleB = ManagedBuildManager.getExtensionBuilder("tcm.tc4.b1");
		incompatibleB = ManagedBuildManager.getRealBuilder(incompatibleB);
		bSet.remove(cfgM.getRealBuilder());
		bSet.remove(incompatibleB);
		compare(Arrays.asList(cbs), bSet);

		HashSet<IBuilder> incompB = new HashSet<>(Arrays.asList(allSysB));
		incompB.removeAll(Arrays.asList(cbs));
		assertTrue(incompB.contains(incompatibleB));

		IToolChain tc3 = ManagedBuildManager.getExtensionToolChain("tcm.tc3");
		cfgM.setToolChain(tc3);
		assertEquals(tc3, cfgM.getToolChain());
		assertEquals(tc3.getBuilder(), cfgM.getBuilder());

		IBuilder b5 = ManagedBuildManager.getExtensionBuilder("tcm.tc5.b1");
		cfgM.setBuilder(b5);
		assertEquals(tc3, cfgM.getToolChain());
		assertEquals(b5, cfgM.getBuilder());

		project.delete(true, null);
	}

	private HashSet<IHoldsOptions> filterSupportedToolChains(IFolderInfo foInfo, IToolChain tc) {
		HashSet<IHoldsOptions> set = new HashSet<>();
		IToolChain[] allSys = ManagedBuildManager.getRealToolChains();
		filterPropsSupported((FolderInfo) foInfo, (ToolChain) tc, allSys, set);
		set.remove(ManagedBuildManager.getRealToolChain(tc));
		return set;
	}

	public void testChildToolChainStatus() throws Exception {
		final String projName = PROJ_NAME_PREFIX + "p2";
		IProject project = BuildSystemTestHelper.createProject(projName);
		BuildSystemTestHelper.createDescription(project, "tcm.pt2");
		ICProjectDescriptionManager desMngr = CoreModel.getDefault().getProjectDescriptionManager();

		ICProjectDescription des = desMngr.getProjectDescription(project);
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(des.getConfigurations()[0]);
		IToolChainModificationManager mngr = ManagedBuildManager.getToolChainModificationManager();

		IConfigurationModification cfgM = (IConfigurationModification) mngr.createModification(cfg.getRootFolderInfo());
		assertEquals(cfgM.getToolChain(), cfg.getToolChain());
		assertTrue(cfgM.isToolChainCompatible());
		assertTrue(cfgM.isBuilderCompatible());

		IPath foPath = new Path("a");
		IFolder fo = project.getFolder(foPath);
		fo.create(true, true, null);

		IFolderInfo foInfo = cfg.createFolderInfo(foPath);
		IFolderInfoModification foM = mngr.createModification(foInfo);
		assertEquals(foM.getToolChain(), foInfo.getToolChain());
		assertTrue(foM.isToolChainCompatible());

		cfgM = (IConfigurationModification) mngr.createModification(cfg.getRootFolderInfo());
		assertEquals(cfgM.getToolChain(), cfg.getToolChain());
		assertTrue(cfgM.isToolChainCompatible());
		assertTrue(cfgM.isBuilderCompatible());

		HashSet<IHoldsOptions> set = filterSupportedToolChains(cfg.getRootFolderInfo(), cfg.getToolChain());
		IToolChain[] tcs = cfgM.getCompatibleToolChains();
		compare(Arrays.asList(tcs), set);

		tcs = foM.getCompatibleToolChains();
		set = filterSupportedToolChains(foInfo, foInfo.getToolChain());
		IToolChain incompatibleTc = ManagedBuildManager.getExtensionToolChain("tcm.tc3");
		incompatibleTc = ManagedBuildManager.getRealToolChain(incompatibleTc);
		set.remove(incompatibleTc);
		compare(Arrays.asList(tcs), set);

		foM.setToolChain(incompatibleTc);
		assertFalse(foM.isToolChainCompatible());
		foInfo.changeToolChain(incompatibleTc, CDataUtil.genId("blah.blah"), incompatibleTc.getName());
		cfgM = (IConfigurationModification) mngr.createModification(cfg.getRootFolderInfo());
		assertTrue(cfgM.isToolChainCompatible());
		foM = mngr.createModification(foInfo);
		assertFalse(foM.isToolChainCompatible());

		project.delete(true, null);
	}

	public void testChildToolChainStatus2() throws Exception {
		final String projName = PROJ_NAME_PREFIX + "p3";
		IProject project = BuildSystemTestHelper.createProject(projName);
		BuildSystemTestHelper.createDescription(project, "tcm.pt.derive1");
		ICProjectDescriptionManager desMngr = CoreModel.getDefault().getProjectDescriptionManager();

		ICProjectDescription des = desMngr.getProjectDescription(project);
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(des.getConfigurations()[0]);
		IToolChainModificationManager mngr = ManagedBuildManager.getToolChainModificationManager();

		IConfigurationModification cfgM = (IConfigurationModification) mngr.createModification(cfg.getRootFolderInfo());
		assertEquals(cfgM.getToolChain(), cfg.getToolChain());
		assertTrue(cfgM.isToolChainCompatible());
		assertTrue(cfgM.isBuilderCompatible());

		IPath foPath = new Path("a");
		IFolder fo = project.getFolder(foPath);
		fo.create(true, true, null);

		IFolderInfo foInfo = cfg.createFolderInfo(foPath);
		IFolderInfoModification foM = mngr.createModification(foInfo);
		assertEquals(foM.getToolChain(), foInfo.getToolChain());
		assertTrue(foM.isToolChainCompatible());

		cfgM = (IConfigurationModification) mngr.createModification(cfg.getRootFolderInfo());
		assertEquals(cfgM.getToolChain(), cfg.getToolChain());
		assertTrue(cfgM.isToolChainCompatible());
		assertTrue(cfgM.isBuilderCompatible());

		HashSet<IHoldsOptions> set = filterSupportedToolChains(cfg.getRootFolderInfo(), cfg.getToolChain());
		IToolChain[] tcs = cfgM.getCompatibleToolChains();
		compare(Arrays.asList(tcs), set);

		tcs = foM.getCompatibleToolChains();
		set = filterSupportedToolChains(foInfo, foInfo.getToolChain());
		rmToolChains(set, new String[] { "tcm.base2.tc", "tcm.derive2.tc1", "tcm.derive2.tc2", "tcm2.tc2", "tcm2.tc",
				"tcm2.tc.derive", });

		compare(Arrays.asList(tcs), set);

		foM.setToolChain(ManagedBuildManager.getExtensionToolChain("tcm.derive2.tc1"));
		assertFalse(foM.isToolChainCompatible());
		foInfo.changeToolChain(ManagedBuildManager.getExtensionToolChain("tcm.derive2.tc1"),
				CDataUtil.genId("blah.blah"), null);
		cfgM = (IConfigurationModification) mngr.createModification(cfg.getRootFolderInfo());
		assertTrue(cfgM.isToolChainCompatible());
		foM = mngr.createModification(foInfo);
		assertFalse(foM.isToolChainCompatible());

		project.delete(true, null);
	}

	private void rmToolChains(Set<IHoldsOptions> set, String[] ids) {
		for (int i = 0; i < ids.length; i++) {
			IToolChain incompatibleTc = ManagedBuildManager.getExtensionToolChain(ids[i]);
			assertNotNull("no tool-chain of id " + ids[i], incompatibleTc);
			incompatibleTc = ManagedBuildManager.getRealToolChain(incompatibleTc);
			assertTrue("set does not contain tc \"" + incompatibleTc.getId() + "\" which is a real tc for \"" + ids[i]
					+ "\"", set.remove(incompatibleTc));
		}
	}

	private void compare(Collection<? extends IHoldsOptions> c1, Collection<IHoldsOptions> c2) {
		HashSet<? extends IHoldsOptions> s1 = new HashSet<IHoldsOptions>(c1);
		HashSet<? extends IHoldsOptions> s1c = new HashSet<IHoldsOptions>(s1);

		HashSet<IHoldsOptions> s2 = new HashSet<>(c2);

		s1.removeAll(s2);
		s2.removeAll(s1c);
		StringBuilder buf = new StringBuilder();
		buf.append("checking c1..\n");
		boolean fail = checkEmpty(s1, buf);
		buf.append("checking c2..\n");
		fail |= checkEmpty(s2, buf);

		if (fail)
			fail(buf.toString());
	}

	private boolean checkEmpty(Collection<? extends IBuildObject> c, StringBuilder buf) {
		if (c.size() != 0) {
			buf.append("non-empty dump:\n");
			for (IBuildObject bo : c)
				buf.append("\t ").append(bo.getId()).append('\n');
			buf.append("end\n");
			return true;
		}
		return false;
	}

	private Collection<IHoldsOptions> filterPropsSupported(FolderInfo foInfo, ToolChain tc, IToolChain[] tcs,
			Collection<IHoldsOptions> c) {
		if (c == null)
			c = new ArrayList<>();
		for (int i = 0; i < tcs.length; i++) {
			if (foInfo.isToolChainCompatible(tc, tcs[i]))
				c.add(tcs[i]);
		}

		return c;
	}

	private Collection<IHoldsOptions> filterPropsSupported(IConfiguration cfg, IBuilder[] bs,
			Collection<IHoldsOptions> c) {
		if (c == null)
			c = new ArrayList<>();
		for (int i = 0; i < bs.length; i++) {
			if (cfg.isBuilderCompatible(bs[i]))
				c.add(bs[i]);
		}

		return c;
	}

	private boolean getReplacementToolInfo(IModificationOperation[] ops, Set<ITool> set) {
		boolean removable = false;
		for (int i = 0; i < ops.length; i++) {
			ITool tool = ops[i].getReplacementTool();
			if (tool == null) {
				removable = true;
			} else {
				set.add(tool);
			}
		}
		return removable;
	}

	public void testToolModificationWithChild() throws Exception {
		final String projName = PROJ_NAME_PREFIX + "p4";
		IProject project = BuildSystemTestHelper.createProject(projName);
		BuildSystemTestHelper.createDescription(project, "tcm.pt");

		ICProjectDescriptionManager desMngr = CoreModel.getDefault().getProjectDescriptionManager();

		ICProjectDescription des = desMngr.getProjectDescription(project);
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(des.getConfigurations()[0]);
		IToolChain tc4 = ManagedBuildManager.getExtensionToolChain("tcm.tc4");
		cfg.getRootFolderInfo().changeToolChain(tc4, CDataUtil.genId(null), null);

		IToolChainModificationManager mngr = ManagedBuildManager.getToolChainModificationManager();

		IConfigurationModification cfgM = (IConfigurationModification) mngr.createModification(cfg.getRootFolderInfo());
		assertEquals(cfgM.getToolChain(), cfg.getToolChain());
		assertTrue(cfgM.isToolChainCompatible());
		assertTrue(cfgM.isBuilderCompatible());

		IPath foPath = new Path("a");
		IFolder fo = project.getFolder(foPath);
		fo.create(true, true, null);

		IFolderInfo foInfo = cfg.createFolderInfo(foPath);
		IFolderInfoModification foM = mngr.createModification(foInfo);
		assertEquals(foM.getToolChain(), foInfo.getToolChain());
		assertTrue(foM.isToolChainCompatible());

		cfgM = (IConfigurationModification) mngr.createModification(cfg.getRootFolderInfo());
		assertEquals(cfgM.getToolChain(), cfg.getToolChain());
		assertTrue(cfgM.isToolChainCompatible());
		assertTrue(cfgM.isBuilderCompatible());

		ITool tool41 = ManagedBuildManager.getExtensionTool("tcm.tc4.t1");
		IToolModification tm = cfgM.getToolModification(tool41);
		assertTrue(tm.isProjectTool());

		IModificationOperation[] ops = tm.getSupportedOperations();
		ITool tool31 = ManagedBuildManager.getExtensionTool("tcm.tc3.t1");
		Set<ITool> replacement = new HashSet<>();
		boolean removable = getReplacementToolInfo(ops, replacement);

		assertFalse(removable);
		assertTrue(replacement.contains(tool31));

		tm = foM.getToolModification(tool41);
		assertTrue(tm.isProjectTool());

		ops = tm.getSupportedOperations();
		replacement = new HashSet<>();
		removable = getReplacementToolInfo(ops, replacement);

		assertFalse(removable);
		assertFalse(replacement.contains(tool31));

		project.delete(true, null);
	}
}
