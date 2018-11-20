/*******************************************************************************
 * Copyright (c) 2005, 2016 Intel Corporation and others.
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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OptionEnablementTests extends TestCase implements IManagedOptionValueHandler, IOptionApplicability {
	private static final String PROJECT_TYPE = "test.four.dot.zero.cdt.managedbuild.target.gnu.exe";
	private static final String CFG_NAME = "Test 4.0 ConfigName.Dbg";
	private static final String TOOL_ID = "test.four.dot.zero.cdt.managedbuild.tool.gnu.c.compiler";
	private static final String OPTION_ID = "test.gnu.c.compiler.option.optimization.level";
	private static final String OPTION_VALUE_ENABLEMENT = "test.gnu.c.optimization.level.none";
	private static final String OPTION_VALUE_ROOT = "test.value.root";
	private static final String OPTION_VALUE_FOLDER = "test.value.folder";
	private static final String OPTION_VALUE_FILE = "test.value.file";

	private static boolean fEnUiVisible;
	private static boolean fEnUiEnabled;
	private static boolean fEnCmdUsed;

	private static boolean fHandleValueCalled;

	private static final String thisEnumIds[] = new String[] { "testgnu.enablement.c.optimization.level.optimize",
			"testgnu.enablement.c.optimization.level.more" };
	private static final String thisStrings[] = new String[] {
			//		"",
			//		"test a b c",
			//		"some buggy string",
			"start 1.2.3 stop" };

	@Override
	public boolean handleValue(IBuildObject configuration, IHoldsOptions holder, IOption option, String extraArgument,
			int event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDefaultValue(IBuildObject configuration, IHoldsOptions holder, IOption option,
			String extraArgument) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnumValueAppropriate(IBuildObject configuration, IHoldsOptions holder, IOption option,
			String extraArgument, String enumValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOptionUsedInCommandLine(IBuildObject configuration, IHoldsOptions holder, IOption option) {
		return fEnCmdUsed;
	}

	@Override
	public boolean isOptionVisible(IBuildObject configuration, IHoldsOptions holder, IOption option) {
		return fEnUiVisible;
	}

	@Override
	public boolean isOptionEnabled(IBuildObject configuration, IHoldsOptions holder, IOption option) {
		return fEnUiEnabled;
	}

	public static Test suite() {
		return new TestSuite(OptionEnablementTests.class);
	}

	private void resetValueHandler() {
		fHandleValueCalled = false;
	}

	private void setEnablement(boolean cmdUs, boolean uiVis, boolean uiEn) {
		fEnUiVisible = uiVis;
		fEnUiEnabled = uiEn;
		fEnCmdUsed = cmdUs;
	}

	public void testEnablement() {
		resetValueHandler();

		IProject project = ManagedBuildTestHelper.createProject("en", "cdt.managedbuild.target.enablement.exe");
		IFile aFile = ManagedBuildTestHelper.createFile(project, "a.c");
		IFile bFile = ManagedBuildTestHelper.createFile(project, "b.c");

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration cfg = info.getManagedProject().getConfigurations()[0];
		assertFalse(fHandleValueCalled);

		doTestEnablement(cfg);

		doEnumAllValues(cfg);

		ManagedBuildTestHelper.removeProject("en");
	}

	private void doEnumAllValues(IBuildObject cfgBo) {
		ITool thisTool = getTool(cfgBo, "enablement.this.child_1.2.3");
		ITool otherTool = getTool(cfgBo, "enablement.other");

		IBuildObject thisCfg = thisTool.getParent();
		IBuildObject otherCfg = otherTool.getParent();

		for (int i = 0; i < thisStrings.length; i++) {
			String strVal = thisStrings[i];
			setOption(cfgBo, thisTool, "this.string", strVal);
			doTestEnablement(cfgBo);
		}
		/*
				for(int i = 0; i < thisEnumIds.length; i++){
					String strVal = thisEnumIds[i];
					setOption(cfgBo, thisTool, "this.enum", strVal);
					doTestEnablement(cfgBo);
				}
		*/
		setOption(cfgBo, thisTool, "this.boolean", false);
		doTestEnablement(cfgBo);

		setOption(cfgBo, thisTool, "this.boolean", true);
		doTestEnablement(cfgBo);
	}

	private ITool getTool(IBuildObject cfgBo, String id) {
		IResourceConfiguration rcCfg = null;
		IConfiguration cfg = null;
		ITool tool = null;
		if (cfgBo instanceof IResourceConfiguration) {
			rcCfg = (IResourceConfiguration) cfgBo;
			cfg = rcCfg.getParent();
			ITool tools[] = rcCfg.getTools();
			for (int i = 0; i < tools.length; i++) {
				for (ITool tmp = tools[i]; tmp != null; tmp = tmp.getSuperClass()) {
					if (tmp.getId().equals(id)) {
						tool = tools[i];
						break;
					}
				}
			}
		} else if (cfgBo instanceof IConfiguration) {
			cfg = (IConfiguration) cfgBo;
			tool = cfg.getToolsBySuperClassId(id)[0];
		} else
			fail("wrong argument");
		return tool;
	}

	private IOption setOption(IBuildObject cfg, IHoldsOptions holder, String id, boolean value) {
		return setOption(cfg, holder, holder.getOptionBySuperClassId(id), value);
	}

	private IOption setOption(IBuildObject cfg, IHoldsOptions holder, IOption option, boolean value) {
		try {
			if (cfg instanceof IConfiguration)
				return ((IConfiguration) cfg).setOption(holder, option, value);
			else if (cfg instanceof IResourceConfiguration)
				return ((IResourceConfiguration) cfg).setOption(holder, option, value);
		} catch (BuildException e) {
			fail(e.getLocalizedMessage());
		}
		fail("wrong arg");
		return null;
	}

	private IOption setOption(IBuildObject cfg, IHoldsOptions holder, String id, String value) {
		return setOption(cfg, holder, holder.getOptionBySuperClassId(id), value);
	}

	private IOption setOption(IBuildObject cfg, IHoldsOptions holder, IOption option, String value) {
		try {
			if (cfg instanceof IConfiguration)
				return ((IConfiguration) cfg).setOption(holder, option, value);
			else if (cfg instanceof IResourceConfiguration)
				return ((IResourceConfiguration) cfg).setOption(holder, option, value);
		} catch (BuildException e) {
			fail(e.getLocalizedMessage());
		}
		fail("wrong arg");
		return null;
	}

	//	private IOption setOption(IBuildObject cfg, IHoldsOptions holder, String id, String value[]){
	//		return setOption(cfg, holder, holder.getOptionBySuperClassId(id), value);
	//	}
	//
	//	private IOption setOption(IBuildObject cfg, IHoldsOptions holder, IOption option, String value[]){
	//		try{
	//		if(cfg instanceof IConfiguration)
	//			return ((IConfiguration)cfg).setOption(holder, option, value);
	//		else if(cfg instanceof IResourceConfiguration)
	//			return ((IResourceConfiguration)cfg).setOption(holder, option, value);
	//		} catch(BuildException e){
	//			fail(e.getLocalizedMessage());
	//		}
	//		fail("wrong arg");
	//		return null;
	//	}

	private void doTestEnablement(IBuildObject cfg) {
		ITool tool = getTool(cfg, "enablement.this.child_1.2.3");
		ITool otherTool = getTool(cfg, "enablement.other");
		ITool tool2 = getTool(cfg, "enablement.this.child.2_1.2.3");

		//		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);

		IOption thisBoolean = tool.getOptionBySuperClassId("this.boolean");
		IOption thisString = tool.getOptionBySuperClassId("this.string");
		IOption thisEnum = tool.getOptionBySuperClassId("this.enum");

		IOption otherString = otherTool.getOptionBySuperClassId("other.string");
		IOption otherBoolean = otherTool.getOptionBySuperClassId("other.boolean");

		try {

			IOption option = tool.getOptionBySuperClassId("enablement.command.c1");

			assertEquals(option.getCommand(), "c1");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.command.c2");
			assertEquals(option.getCommand(), "c2");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.commandFalse.c1");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "c1");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.commandFalse.c2");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "c2");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.command.c1.commandFalse.cF1");
			assertEquals(option.getCommand(), "c1");
			assertEquals(option.getCommandFalse(), "cF1");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.command.cmd.commandFalse.cmdF");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.command.c1.commandFalse.cmdF");
			assertEquals(option.getCommand(), "c1");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.command.cmd.commandFalse.cF1");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cF1");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.ui.en");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cF1");
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.ui.vis");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cF1");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.cmd.us");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.cmdUs.or.uiVis");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cF1");
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.uiEn.or.uiVis");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cF1");
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.all");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cF1");
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.all.ac.vh");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cF1");

			setEnablement(false, false, false);
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			setEnablement(false, true, false);
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			setEnablement(false, false, true);
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			setEnablement(true, false, false);
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			setEnablement(false, false, false);
			option = tool.getOptionBySuperClassId("enablement.all.cF1.ac.vh");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cF1");
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			setEnablement(true, false, true);

			option = tool.getOptionBySuperClassId("enablement.all.cF.cmdF");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.all.cF.cF1");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cF1");
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("this.boolean");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkOpt.all.Q.this.boolean.True");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertEquals(thisBoolean.getBooleanValue() == true,
					option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertEquals(thisBoolean.getBooleanValue() == true,
					option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertEquals(thisBoolean.getBooleanValue() == true,
					option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkOpt.all.Q.this.boolean.False");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertEquals(thisBoolean.getBooleanValue() == false,
					option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertEquals(thisBoolean.getBooleanValue() == false,
					option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertEquals(thisBoolean.getBooleanValue() == false,
					option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkOpt.all.Q.this.string.Q.empty");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertEquals(thisString.getStringValue().isEmpty(),
					option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertEquals(thisString.getStringValue().isEmpty(),
					option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertEquals(thisString.getStringValue().isEmpty(),
					option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkOpt.all.Q.this.string.Q.test a b c");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertEquals(thisString.getStringValue().equals("test a b c"),
					option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertEquals(thisString.getStringValue().equals("test a b c"),
					option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertEquals(thisString.getStringValue().equals("test a b c"),
					option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId(
					"enablement.checkOpt.all.Q.this.enum.Q.testgnu.enablement.c.optimization.level.optimize");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			String id = thisEnum.getEnumeratedId(thisEnum.getStringValue());
			if (id == null)
				id = "";
			assertEquals(id.equals("testgnu.enablement.c.optimization.level.optimize"),
					option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertEquals(id.equals("testgnu.enablement.c.optimization.level.optimize"),
					option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertEquals(id.equals("testgnu.enablement.c.optimization.level.optimize"),
					option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkOpt.all.Q.this.Q.true");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertEquals(option.getBooleanValue() == true,
					option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertEquals(option.getBooleanValue() == true,
					option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertEquals(option.getBooleanValue() == true,
					option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool
					.getOptionBySuperClassId("enablement.checkOpt.all.Q.this.string.Q.start ${ParentVersion} stop");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertEquals(thisString.getStringValue().equals("start 1.2.3 stop"),
					option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertEquals(thisString.getStringValue().equals("start 1.2.3 stop"),
					option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertEquals(thisString.getStringValue().equals("start 1.2.3 stop"),
					option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkOpt.all.Q.this.string.Q.other.string");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertEquals(thisString.getStringValue().equals(otherString.getStringValue()),
					option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertEquals(thisString.getStringValue().equals(otherString.getStringValue()),
					option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertEquals(thisString.getStringValue().equals(otherString.getStringValue()),
					option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkOpt.all.Q.this.string.Q.other.string");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertEquals(thisString.getStringValue().equals(otherString.getStringValue()),
					option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertEquals(thisString.getStringValue().equals(otherString.getStringValue()),
					option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertEquals(thisString.getStringValue().equals(otherString.getStringValue()),
					option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkString");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkString.2");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkString.3");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkFalse.false");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkNot.false");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkOr.true");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkAnd.false");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool.getOptionBySuperClassId("enablement.checkHolder.true.1.false.2");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertTrue(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionVisible(cfg, tool, option));
			assertTrue(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool, option));

			option = tool2.getOptionBySuperClassId("enablement.checkHolder.true.1.false.2");
			assertEquals(option.getCommand(), "cmd");
			assertEquals(option.getCommandFalse(), "cmdF");
			assertFalse(option.getApplicabilityCalculator().isOptionUsedInCommandLine(cfg, tool2, option));
			assertFalse(option.getApplicabilityCalculator().isOptionVisible(cfg, tool2, option));
			assertFalse(option.getApplicabilityCalculator().isOptionEnabled(cfg, tool2, option));

		} catch (BuildException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * This method assumes that folder has only one qualified tool.
	 */
	private IOption getOptionForFolder(IFolderInfo rcInfo, String toolId, String optionId) {
		ITool[] tools = null;
		tools = rcInfo.getToolsBySuperClassId(toolId);
		assertNotNull(tools);
		assertEquals(1, tools.length);
		ITool tool = tools[0];
		assertNotNull(tool);

		IOption option = tool.getOptionBySuperClassId(optionId);
		return option;
	}

	/**
	 * This method assumes that file has only one tool.
	 */
	private IOption getOptionForFile(IFileInfo rcInfo, String optionId) {
		ITool[] tools = null;
		tools = rcInfo.getTools();
		assertNotNull(tools);
		assertEquals(1, tools.length);
		ITool tool = tools[0];
		assertNotNull(tool);

		IOption option = tool.getOptionBySuperClassId(optionId);
		return option;
	}

	public void testEnablement_Bug250686() throws Exception {
		final String testName = getName();
		IProject project = ManagedBuildTestHelper.createProject(testName, PROJECT_TYPE);
		assertNotNull(project);

		IFolder folder = ManagedBuildTestHelper.createFolder(project, "Folder");
		assertNotNull(folder);

		IFile file = ManagedBuildTestHelper.createFile(project, "Folder/file.c");
		assertNotNull(file);

		ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project);
		prjDescription.getConfigurationByName(CFG_NAME);

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration cfg = info.getManagedProject().getConfigurations()[0];
		assertNotNull(cfg);
		assertEquals(CFG_NAME, cfg.getName());

		{
			// Round 1. Test root folder option
			IFolderInfo rootFolderInfo = cfg.getRootFolderInfo();
			assertNotNull(rootFolderInfo);

			IOption option = getOptionForFolder(rootFolderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_ENABLEMENT, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 1. Test subfolder option
			IResourceInfo folderInfo = cfg.getResourceInfo(folder.getFullPath(), false);
			assertNotNull(folderInfo);
			assertTrue(folderInfo instanceof IFolderInfo);

			IOption option = getOptionForFolder((IFolderInfo) folderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_ENABLEMENT, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 1. Test file option
			IResourceInfo fileInfo = cfg.getResourceInfo(file.getFullPath(), false);
			assertNotNull(fileInfo);
			assertTrue(fileInfo instanceof IFolderInfo);

			// Option is taken from root folder here
			IOption option = getOptionForFolder((IFolderInfo) fileInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_ENABLEMENT, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 2. Override the value of the option for the root folder
			IFolderInfo rootFolderInfo = cfg.getRootFolderInfo();
			ITool[] tools = rootFolderInfo.getToolsBySuperClassId(TOOL_ID);
			assertEquals(1, tools.length);
			IOption option = getOptionForFolder(rootFolderInfo, TOOL_ID, OPTION_ID);
			rootFolderInfo.setOption(tools[0], option, OPTION_VALUE_ROOT);
		}

		{
			// Round 2. Test root folder option
			IFolderInfo rootFolderInfo = cfg.getRootFolderInfo();
			assertNotNull(rootFolderInfo);

			IOption option = getOptionForFolder(rootFolderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_ROOT, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 2. Test subfolder option
			IResourceInfo folderInfo = cfg.getResourceInfo(folder.getFullPath(), false);
			assertNotNull(folderInfo);
			assertTrue(folderInfo instanceof IFolderInfo);

			IOption option = getOptionForFolder((IFolderInfo) folderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_ROOT, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 2. Test file option
			IResourceInfo fileInfo = cfg.getResourceInfo(file.getFullPath(), false);
			assertNotNull(fileInfo);
			assertTrue(fileInfo instanceof IFolderInfo);

			// Option is taken from root folder here
			IOption option = getOptionForFolder((IFolderInfo) fileInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_ROOT, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 3. Override the value of the option for the subfolder
			IFolderInfo folderInfo = cfg.createFolderInfo(folder.getFullPath());
			assertNotNull(folderInfo);

			ITool[] tools = folderInfo.getToolsBySuperClassId(TOOL_ID);
			assertEquals(1, tools.length);

			IOption option = getOptionForFolder(folderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_ROOT, option.getValue());
			assertFalse(option.isExtensionElement());

			folderInfo.setOption(tools[0], option, OPTION_VALUE_FOLDER);
		}

		{
			// Round 3. Test root folder option
			IFolderInfo rootFolderInfo = cfg.getRootFolderInfo();
			assertNotNull(rootFolderInfo);

			IOption option = getOptionForFolder(rootFolderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_ROOT, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 3. Test subfolder option
			IResourceInfo folderInfo = cfg.getResourceInfo(folder.getFullPath(), false);
			assertNotNull(folderInfo);
			assertTrue(folderInfo instanceof IFolderInfo);

			IOption option = getOptionForFolder((IFolderInfo) folderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_FOLDER, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 3. Test file option
			IResourceInfo fileInfo = cfg.getResourceInfo(file.getFullPath(), false);
			assertNotNull(fileInfo);
			assertTrue(fileInfo instanceof IFolderInfo);

			// Option is taken from parent folder here
			IOption option = getOptionForFolder((IFolderInfo) fileInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_FOLDER, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 4. Override the value of the option for the file
			IFileInfo fileInfo = cfg.createFileInfo(file.getFullPath());
			assertNotNull(fileInfo);

			ITool[] tools = fileInfo.getTools();
			assertEquals(1, tools.length);
			ITool tool = tools[0];
			assertNotNull(tool);

			IOption option = getOptionForFile(fileInfo, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_FOLDER, option.getValue());
			assertFalse(option.isExtensionElement());

			fileInfo.setOption(tool, option, OPTION_VALUE_FILE);
		}

		{
			// Round 4. Test root folder option
			IFolderInfo rootFolderInfo = cfg.getRootFolderInfo();
			assertNotNull(rootFolderInfo);

			IOption option = getOptionForFolder(rootFolderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_ROOT, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 4. Test subfolder option
			IResourceInfo folderInfo = cfg.getResourceInfo(folder.getFullPath(), false);
			assertNotNull(folderInfo);
			assertTrue(folderInfo instanceof IFolderInfo);

			IOption option = getOptionForFolder((IFolderInfo) folderInfo, TOOL_ID, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_FOLDER, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		{
			// Round 4. Test file option
			IResourceInfo fileInfo = cfg.getResourceInfo(file.getFullPath(), false);
			assertNotNull(fileInfo);
			assertTrue(fileInfo instanceof IFileInfo);

			IOption option = getOptionForFile((IFileInfo) fileInfo, OPTION_ID);
			assertNotNull(option);
			assertEquals(OPTION_VALUE_FILE, option.getValue());
			assertFalse(option.isExtensionElement());
		}

		ManagedBuildTestHelper.removeProject(testName);
	}

}
