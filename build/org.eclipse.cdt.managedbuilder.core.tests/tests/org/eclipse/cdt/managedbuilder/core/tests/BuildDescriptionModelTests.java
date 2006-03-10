/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildIOType;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildDescription;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildIOType;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildResource;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildStep;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.DbgUtil;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class BuildDescriptionModelTests extends TestCase {
	private static final String PREFIX = "BuildDescription_";
	private static final String PROJ_PATH = "testBuildDescriptionProjects";
	
	private CompositeCleaner fCompositeCleaner = new CompositeCleaner();
	private Runnable fCleaner = fCompositeCleaner;
	
	private class CompositeCleaner implements Runnable{
		private List fRunnables = new ArrayList();

		public void addRunnable(Runnable r){
			fRunnables.add(r);
		}

		public void run() {
			for(Iterator iter = fRunnables.iterator(); iter.hasNext();){
				Runnable r = (Runnable)iter.next();
				r.run();
			}
			fRunnables.clear();
		}
		
	}
	private class ProjectCleaner implements Runnable{
		List fProjList = new ArrayList();
		
		public ProjectCleaner(){
		}

		public ProjectCleaner(String name){
			addProject(name);
		}

		public ProjectCleaner(IProject project){
			addProject(project);
		}

		public void addProject(IProject project){
			addProject(project.getName());
		}

		public void addProject(String name){
			fProjList.add(name);
		}

		public void run() {
			for(Iterator iter = fProjList.iterator(); iter.hasNext();){
				String name = (String)iter.next();
				ManagedBuildTestHelper.removeProject(name);
			}
			fProjList.clear();
		}
		
	}

	public static Test suite() {
		return new TestSuite(BuildDescriptionModelTests.class);
	}
	
	public void testDes_Model(){
		IProject project = createProject(PREFIX + "1", "test30_2.tar");
		IFile aAsm = ManagedBuildTestHelper.createFile(project, "a.asm");
		ManagedBuildTestHelper.createFile(project, "b.asm");
		ManagedBuildTestHelper.createFile(project, "c.cpp");
		ManagedBuildTestHelper.createFile(project, "d.cpp");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		cfg.setArtifactExtension("tmp");
		String cName = cfg.getName();

		BuildDescription des = new BuildDescription(cfg);
		
		BuildResource aAsmRc = des.createResource("a.asm");
		assertNotNull("failed to create resource a.asm", aAsmRc);
		
		if(aAsmRc != des.createResource(aAsm))
			fail("new build resource created for the same resource");

		if(aAsmRc != des.createResource("a.asm"))
			fail("new build resource created for the same resource");

		if(aAsmRc != des.createResource(aAsm.getLocation(), aAsm.getFullPath()))
			fail("new build resource created for the same resource");

		assertEquals(aAsmRc.getProducerIOType(), null);
		assertEquals(aAsmRc.getDependentIOTypes().length, 0);

		BuildStep inStep = (BuildStep)des.getInputStep();
		assertEquals("input step has inputs", inStep.getInputIOTypes().length, 0);
		assertEquals("initial input step has outputs", inStep.getOutputIOTypes().length, 0);
		
		BuildIOType type = inStep.createIOType(false, true, null);
		if(type == null)
			fail("failed to create output type");
		
		assertEquals(type.getStep(), inStep);
		
		type.addResource(aAsmRc);
		
		assertEquals(aAsmRc.getProducerIOType(), type);
		assertEquals(aAsmRc.getDependentIOTypes().length, 0);

		assertEquals("input step has inputs", inStep.getInputIOTypes().length, 0);
		assertEquals(inStep.getOutputIOTypes().length, 1);
		assertEquals(inStep.getOutputIOTypes()[0], type);
		
		assertEquals(type.getResources().length, 1);
		assertEquals(type.getResources()[0], aAsmRc);
		
		BuildResource bAsmRc = des.createResource("b.asm");
		assertEquals(bAsmRc.getProducerIOType(), null);
		assertEquals(bAsmRc.getDependentIOTypes().length, 0);

		type.addResource(bAsmRc);
		assertEquals(bAsmRc.getProducerIOType(), type);
		assertEquals(bAsmRc.getDependentIOTypes().length, 0);

		assertEquals("input step has inputs", inStep.getInputIOTypes().length, 0);
		assertEquals(inStep.getOutputIOTypes().length, 1);
		assertEquals(inStep.getOutputIOTypes()[0], type);
		
		assertEquals(type.getResources().length, 2);
		
		BuildStep step = des.createStep(null, null);
		assertEquals("new step has inputs", inStep.getInputIOTypes().length, 0);
		assertEquals("new step has outputs", inStep.getOutputIOTypes().length, 1);
		
		BuildIOType iType = step.createIOType(true, true, null);
		if(iType == null)
			fail("failed to create in type");
		
		assertEquals(iType.getStep(), step);
		
		assertEquals(aAsmRc.getProducerIOType(), type);
		assertEquals(aAsmRc.getDependentIOTypes().length, 0);

		iType.addResource(aAsmRc);
		
		assertEquals(aAsmRc.getProducerIOType(), type);
		assertEquals(aAsmRc.getDependentIOTypes().length, 1);
		assertEquals(aAsmRc.getDependentIOTypes()[0], iType);

		assertEquals("input step has inputs", inStep.getInputIOTypes().length, 0);
		assertEquals(inStep.getOutputIOTypes().length, 1);
		assertEquals(inStep.getOutputIOTypes()[0], type);
		
		assertEquals(iType.getResources().length, 1);
		assertEquals(iType.getResources()[0], aAsmRc);
		
		assertEquals(bAsmRc.getProducerIOType(), type);
		assertEquals(bAsmRc.getDependentIOTypes().length, 0);

		iType.addResource(bAsmRc);
		assertEquals(bAsmRc.getProducerIOType(), type);
		assertEquals(bAsmRc.getDependentIOTypes().length, 1);
		assertEquals(bAsmRc.getDependentIOTypes()[0], iType);

		assertEquals("input step has inputs", inStep.getInputIOTypes().length, 0);
		assertEquals(inStep.getOutputIOTypes().length, 1);
		assertEquals(inStep.getOutputIOTypes()[0], type);
		
		assertEquals(type.getResources().length, 2);
		
		
//		ManagedBuildTestHelper.removeProject(PREFIX + "1");
	}
	
	public void testDesTest30_2_asm_only(){
		IProject project = createProject(PREFIX + "1", "test30_2.tar");
		ManagedBuildTestHelper.createFile(project, "a.asm");
		ManagedBuildTestHelper.createFile(project, "b.asm");

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		cfg.setArtifactExtension("tmp");
		String cName = cfg.getName();

		BuildDescription tDes = new BuildDescription(cfg);
		
		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);
		
//		ManagedBuildTestHelper.removeProject(PREFIX + "1");
	}


	public void testDesTest30_2_empty(){
		IProject project = createProject(PREFIX + "1", "test30_2.tar");

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		cfg.setArtifactExtension("tmp");
		String cName = cfg.getName();

		BuildDescription tDes = new BuildDescription(cfg);
		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);
		
//		ManagedBuildTestHelper.removeProject(PREFIX + "1");
	}

	public void testDesDesTest30_2_cpp_only(){
		IProject project = createProject(PREFIX + "1", "test30_2.tar");
		ManagedBuildTestHelper.createFile(project, "c.cpp");
		ManagedBuildTestHelper.createFile(project, "d.cpp");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		cfg.setArtifactExtension("tmp");
		String cName = cfg.getName();

		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
		//
		//
		step = tDes.createStep(null, null);
		
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/new.tar"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/new.tar"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/new.log"));
		//
		//
		step = tDes.createStep(null, null);
				
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/new.log"));
				
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/" + cfg.getArtifactName() + ".tmp"));
		//
		//
		step = (BuildStep)tDes.getOutputStep();
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/" + cfg.getArtifactName() + ".tmp"));
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);
		
//		ManagedBuildTestHelper.removeProject(PREFIX + "1");
	}	
	
	public void testDesTest30_1(){
		IProject project = loadProject("test30_1");
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration cfg = info.getDefaultConfiguration();
		String out = cfg.getName() + "/";
		
		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getOutputStep();

			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(out + "Test30_1.so.4.5.6"));
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource(out + "CDT.jpeg"));
		//
		//
			step = tDes.createStep(null, null);

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "CDT.jpeg"));
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(out + "CDT.bmp"));
		//
		//
			step = tDes.createStep(null, null);

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(out + "f1.c"));
			type.addResource(tDes.createResource(out + "f2.c"));

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(out + "test_ar.h"));

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(out + "CDT.bmp"));

			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("test.tar"));
		//
		//
			step = tDes.createStep(null, null);

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "Test30_1.so.4.5.6"));

			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(out + "test30_1.so.1.2.3"));
		//
		//
			step = tDes.createStep(null, null);

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "test30_1.so.1.2.3"));

			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(out + "f1.o"));
			type.addResource(tDes.createResource(out + "f2.o"));
		//
		//
			step = tDes.createStep(null, null);

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "f1.o"));

			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(out + "f1.oprestripped"));
		//
		//
			step = tDes.createStep(null, null);

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "f2.o"));

			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(out + "f2.oprestripped"));
		//
		//
			step = tDes.createStep(null, null);

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "f1.oprestripped"));

			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(out + "f1.c"));
		//
		//
			step = tDes.createStep(null, null);

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "f2.oprestripped"));

			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(out + "f2.c"));
		//
		//
			step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource("test.tar"));
		//
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}
		
		doTestBuildDescription(des, tDes);

	}
	
	public void testDesTest30_2(){
		IProject project = createProject(PREFIX + "1", "test30_2.tar");
		ManagedBuildTestHelper.createFile(project, "a.asm");
		ManagedBuildTestHelper.createFile(project, "b.asm");
		ManagedBuildTestHelper.createFile(project, "c.cpp");
		ManagedBuildTestHelper.createFile(project, "d.cpp");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		cfg.setArtifactExtension("tmp");
		String cName = cfg.getName();

		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("a.asm"));
			type.addResource(tDes.createResource("b.asm"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
		//
		//
		step = tDes.createStep(null, null);
		
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("a.asm"));
			type.addResource(tDes.createResource("b.asm"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/new.tar"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/new.tar"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/new.log"));
		//
		//
		step = tDes.createStep(null, null);
				
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/new.log"));
				
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/" + cfg.getArtifactName() + ".tmp"));
		//
		//
		step = (BuildStep)tDes.getOutputStep();
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/" + cfg.getArtifactName() + ".tmp"));
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);
		
//		ManagedBuildTestHelper.removeProject(PREFIX + "1");
	}
/*	
	private void doTestStep(IBuildStep step, StepDes testStep){
		IBuildIOType iTs[] = step.getInputIOTypes();
		TypeDes tITs[] = testStep.fInTypes;

		IBuildIOType oTs[] = step.getOutputIOTypes();
		TypeDes tOTs[] = testStep.fOutTypes;
		
		assertEquals(iTs.length, tITs.length);
		assertEquals(oTs.length, tOTs.length);
		
		
	}
*/
	private void doTestStep(IBuildStep step, IBuildStep oStep, boolean up){
		Map inMap = new HashMap();
		Map outMap = new HashMap();
		
		stepsMatch(step, oStep, inMap, outMap, true);
		
		Map map = up ? outMap : inMap;
		
		for(Iterator iter = map.entrySet().iterator();iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			doTestType((IBuildIOType)entry.getKey(), (IBuildIOType)entry.getValue());
		}
	}
	
	private void doTestType(IBuildIOType type, IBuildIOType oType){
		Map map = new HashMap();
		
		typesMatch(type, oType, map, true);
		
		for(Iterator iter = map.entrySet().iterator();iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			doTestResource((IBuildResource)entry.getKey(), (IBuildResource)entry.getValue(), !type.isInput());
		}
	}
	
	private void doTestResource(IBuildResource rc, IBuildResource oRc, boolean up){
		Map outMap = new HashMap();
		
		doTestResourceMatch(rc, oRc, outMap);
		
		if(!up){
			typesMatch(rc.getProducerIOType(), oRc.getProducerIOType(), null, true);
			doTestStep(rc.getProducerIOType().getStep(), oRc.getProducerIOType().getStep(), up);
		} else {
			Set stepSet = new HashSet();
			
			for(Iterator iter = outMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				IBuildIOType type = (IBuildIOType)entry.getKey();
				
				IBuildStep step = type.getStep();
				if(stepSet.add(step)){
					IBuildIOType oType = (IBuildIOType)entry.getValue();
					typesMatch(type, oType, null, true);
					doTestStep(step, oType.getStep(), up);
				}
			}
		}
	}
	
	private void doTestResourceMatch(IBuildResource rc, IBuildResource oRc, Map outTypeMap){
		
		doTrace("matching resource " + DbgUtil.resourceName(rc));
		
		if(!rc.getLocation().equals(oRc.getLocation()))
			doFail("different resource locsations", rc, oRc);
		
		IBuildIOType inType = rc.getProducerIOType();
		IBuildIOType oInType = oRc.getProducerIOType();
		
		typesMatch(inType, oInType, null, true);
		
		
		IBuildIOType outTypes[] = rc.getDependentIOTypes();
		IBuildIOType oOutTypes[] = oRc.getDependentIOTypes();
		
		if(outTypes.length != oOutTypes.length)
			doFail("resources do not match: different number of output types", rc, oRc);
		
		for(int i = 0; i < outTypes.length; i++){
			IBuildIOType oType = getCorType(outTypes[i], oOutTypes);
			if(oType == null)
				doFail("resources not match: no cor dep type found", rc, oRc);
			
			Object obj = outTypeMap.put(outTypes[i], oType);
			
			if(obj != null){
				doFail("there was corresponding type",rc, oRc);
			}
		}
		
		doTrace("end matching resource");
	}


	private boolean stepsMatch(IBuildStep step, IBuildStep oStep, Map inTypeMap, Map outTypeMap, boolean failOnErr){
		return stepsMatch(step, oStep, inTypeMap, outTypeMap, true, failOnErr);
	}

	private boolean stepsMatch(IBuildStep step, IBuildStep oStep, Map inTypeMap, Map outTypeMap, boolean checkSteps, boolean failOnErr){
		IBuildIOType inTypes[] = step.getInputIOTypes();
		IBuildIOType oInTypes[] = oStep.getInputIOTypes();
		
		doTrace("matching step " + DbgUtil.stepName(step));
		
		if(inTypes.length != oInTypes.length){
			if(failOnErr)
				doFail("steps do not match: different number of input types",step, oStep);
			return false;
		}
		for(int i = 0; i < inTypes.length; i++){
			IBuildIOType oType = getCorType(inTypes[i], oInTypes, null, checkSteps);
			if(oType == null){
				if(failOnErr)
					doFail("steps not match, no corresponding input type found", step, oStep);
				return false;
			}
			
			Object obj = inTypeMap.put(inTypes[i], oType);
			
			if(obj != null){
				if(failOnErr)
					doFail("there was already corresponding input type", step, oStep);
				return false;
			}
		}

		IBuildIOType outTypes[] = step.getOutputIOTypes();
		IBuildIOType oOutTypes[] = oStep.getOutputIOTypes();
		
		if(outTypes.length != oOutTypes.length){
			if(failOnErr)
				doFail("steps do not match: different number of output types", step, oStep);
			return false;
		}
		
		for(int i = 0; i < outTypes.length; i++){
			IBuildIOType oType = getCorType(outTypes[i], oOutTypes, null, checkSteps);
			if(oType == null){
				if(failOnErr)
					doFail("steps not match, no corresponding output type found", step, oStep);
				return false;
			}
			
			Object obj = outTypeMap.put(outTypes[i], oType);
			
			if(obj != null){
				if(failOnErr)
					doFail("there was already corresponding output type", step, oStep);
				return false;
			}
		}
		
		doTrace("end matching step");
		
		return true;
	}

	private IBuildIOType getCorType(IBuildIOType type, IBuildIOType oTypes[]){
		return getCorType(type, oTypes, null, true);
	}

	private IBuildIOType getCorType(IBuildIOType type, IBuildIOType oTypes[], Map rcMap, boolean checkSteps){
		for(int i = 0; i < oTypes.length; i++){
			if(typesMatch(type, oTypes[i], rcMap, checkSteps, false))
				return oTypes[i];
			
			if(rcMap != null)
				rcMap.clear();
		}
		
		return null;
/*		
		IBuildStep step = type.getStep();
		IBuildResource rcs[] = type.getResources();
		for(int i = 0; i < oTypes.length; i++){
			if(type.isInput() != oTypes[i].isInput())
				continue;
			
			IBuildResource oRcs[] = oTypes[i].getResources();
			
			if(rcs.length != oRcs.length)
				continue;
			
			if(resourcesMatch(rcs, oRcs, null)){
				if(!checkSteps)
					return oTypes[i];
				IBuildIOType oType = oTypes[i];
				IBuildStep step = type.getStep();
				IBuildStep oStep = oType.getStep();
				
				if(typesMatch(step.get))
				for(int j = 0; j < )
			}
		}
		return null;
*/
	}

	private boolean typesMatch(IBuildIOType type, IBuildIOType oType, Map rcMap, boolean failOnError){
		return typesMatch(type, oType, rcMap, true, failOnError);
	}

	private boolean typesMatch(IBuildIOType type, IBuildIOType oType, Map rcMap, boolean checkStep, boolean failOnError){
		
//		doTrace("matching io type");
		if(type.isInput() != oType.isInput()){
			if(failOnError){
				doFail("types have different I/O property", type, oType);
			}
			return false;
		}
		
		IBuildResource rcs[] = type.getResources();
		IBuildResource oRcs[] = oType.getResources();
		if(rcs.length != oRcs.length)
			return false;
		
		if(resourcesMatch(rcs, oRcs, rcMap)){
			Map inMap = new HashMap();
			Map outMap = new HashMap();
			if(!checkStep)
				return true;
			return stepsMatch(type.getStep(), oType.getStep(), inMap, outMap, false, failOnError);
		} else if(failOnError) {
			doFail("resources not match", type, oType);
		}
//		doTrace("end matching io type");

		return false;
	}
	
	private boolean resourcesMatch(IBuildResource rcs[], IBuildResource oRcs[], Map rcMap){
		if(rcs.length != oRcs.length)
			return false;
		
		for(int j = 0; j < rcs.length; j++){
			IPath location = rcs[j].getLocation();
			int k;
			for(k = 0; k < oRcs.length; k++){
				if(oRcs[k].getLocation().equals(location)){
					if(rcMap != null)
						rcMap.put(rcs[j], oRcs[k]);
					break;
				}
			}
			if(k == oRcs.length)
				return false;
		}
		return true;
	}

	private void doFail(String dump, IBuildIOType type, IBuildIOType oType){
		doFail(dump + "\nType:\n" + DbgUtil.dumpType(type) + "\noType:\n" + DbgUtil.dumpType(oType));
	}

	private void doFail(String dump, IBuildResource rc, IBuildResource oRc){
		doFail(dump + "\nRc:\n" + DbgUtil.dumpResource(rc) + "\noRc:\n" + DbgUtil.dumpResource(oRc));
	}

	private void doFail(String dump, IBuildStep step, IBuildStep oStep){
		doFail(dump + "\nStep:\n" + DbgUtil.dumpStep(step) + "\noStep:\n" + DbgUtil.dumpStep(oStep));
	}

	private void doFail(String dump){
		doTrace(dump);
		fail(dump);
	}
	
	private void doTrace(String str){
		if(DbgUtil.DEBUG)
			DbgUtil.traceln(str);
	}
	
	private void doTestBuildDescription(IBuildDescription des, IBuildDescription tDes){
		assertEquals(des.getConfiguration(), tDes.getConfiguration());
		
		assertNotNull(des.getConfiguration());
		
		IProject project = des.getConfiguration().getOwner().getProject();
		
		IBuildStep inStep = des.getInputStep();
		IBuildStep outStep = des.getOutputStep();
		
		if(inStep.getInputIOTypes().length !=  0){
			doFail("input step contains inputs, " + DbgUtil.dumpStep(inStep));
		}
		if(outStep.getOutputIOTypes().length !=  0){
			doFail("output step contains outputs, " + DbgUtil.dumpStep(outStep)); 
		}

		IBuildStep tInStep = tDes.getInputStep();
		IBuildStep tOutStep = tDes.getOutputStep();

		doTrace("*****testing down to up..");
		doTestStep(inStep, tInStep, true);
		doTrace("*****down to up passed");
		
		doTrace("*****testing up to down..");
		doTestStep(outStep, tOutStep, false);
		doTrace("*****up to down passed");
	}
	
	protected void tearDown() throws Exception {
		fCleaner.run();
		if(DbgUtil.DEBUG)
			DbgUtil.flush();
	}
	
	private IProject createProject(String name, String id){
		IProject proj = ManagedBuildTestHelper.createProject(name, id);
		if(proj != null)
			fCompositeCleaner.addRunnable(new ProjectCleaner(proj));
		
		return proj;
	}
	
	private IProject loadProject(String name){
		IProject proj = ManagedBuildTestHelper.loadProject(name, PROJ_PATH);
		if(proj != null)
			fCompositeCleaner.addRunnable(new ProjectCleaner(proj));
		
		return proj;
	}

	
	public void testDes_gnu30_exe(){
		IProject project = createProject(PREFIX + "1", "cdt.managedbuild.target.gnu30.exe");
		try {
			CCProjectNature.addCCNature(project, null);
		} catch (CoreException e1) {
			fail("fail to add CC nature");
		}
		
		ManagedBuildTestHelper.createFile(project, "a.c");
		ManagedBuildTestHelper.createFile(project, "b.c");
		ManagedBuildTestHelper.createFile(project, "c.cpp");
		ManagedBuildTestHelper.createFile(project, "d.cpp");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		String art = cfg.getArtifactName();
		String ext = cfg.getArtifactExtension();
		if(ext != null && ext.length() > 0)
			art = art + "." + ext;
		
		String cName = cfg.getName();

		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("a.c"));
			type.addResource(tDes.createResource("b.c"));

		//
		//
		step = tDes.createStep(null, null);
		
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("a.c"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("b.c"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/b.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("c.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/c.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("d.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/d.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
			type.addResource(tDes.createResource(cName + "/b.o"));
			type.addResource(tDes.createResource(cName + "/c.o"));
			type.addResource(tDes.createResource(cName + "/d.o"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//
		//
		step = (BuildStep)tDes.getOutputStep();
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);
	}
	
	
	public void testDesTestgnu21_exe(){
		IProject project = createProject(PREFIX + "1", "cdt.managedbuild.target.testgnu21.exe");
		try {
			CCProjectNature.addCCNature(project, null);
		} catch (CoreException e1) {
			fail("fail to add CC nature");
		}
		
		ManagedBuildTestHelper.createFile(project, "a.c");
		ManagedBuildTestHelper.createFile(project, "b.c");
		ManagedBuildTestHelper.createFile(project, "c.cpp");
		ManagedBuildTestHelper.createFile(project, "d.cpp");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		String art = cfg.getArtifactName();
		String ext = cfg.getArtifactExtension();
		if(ext != null && ext.length() > 0)
			art = art + "." + ext;
		
		String cName = cfg.getName();

		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("a.c"));
			type.addResource(tDes.createResource("b.c"));

		//
		//
		step = tDes.createStep(null, null);
		
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("a.c"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("b.c"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/b.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("c.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/c.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("d.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/d.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
			type.addResource(tDes.createResource(cName + "/b.o"));
			type.addResource(tDes.createResource(cName + "/c.o"));
			type.addResource(tDes.createResource(cName + "/d.o"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//
		//
		step = (BuildStep)tDes.getOutputStep();
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);
	}
	
	public void testDesRcCfg(){
		IProject project = createProject(PREFIX + "1", "cdt.managedbuild.target.gnu30.exe");
		try {
			CCProjectNature.addCCNature(project, null);
		} catch (CoreException e1) {
			fail("fail to add CC nature");
		}
		
		IFile ac = ManagedBuildTestHelper.createFile(project, "a.c");
		IFile bc = ManagedBuildTestHelper.createFile(project, "b.c");
		IFile ccpp = ManagedBuildTestHelper.createFile(project, "c.cpp");
		IFile dcpp = ManagedBuildTestHelper.createFile(project, "d.cpp");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		String art = cfg.getArtifactName();
		String ext = cfg.getArtifactExtension();
		if(ext != null && ext.length() > 0)
			art = art + "." + ext;
		
		String cName = cfg.getName();
		IResourceConfiguration rcCfg = cfg.createResourceConfiguration(ac);
		assertNotNull(rcCfg);
		rcCfg = cfg.createResourceConfiguration(ccpp);
		assertNotNull(rcCfg);

		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("a.c"));
			type.addResource(tDes.createResource("b.c"));

		//
		//
		step = tDes.createStep(null, null);
		
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("a.c"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("b.c"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/b.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("c.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/c.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("d.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/d.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
			type.addResource(tDes.createResource(cName + "/b.o"));
			type.addResource(tDes.createResource(cName + "/c.o"));
			type.addResource(tDes.createResource(cName + "/d.o"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//
		//
		step = (BuildStep)tDes.getOutputStep();
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);

	}

	public void testDesRcbs(){
		IProject project = createProject(PREFIX + "1", "cdt.managedbuild.target.gnu30.exe");
		try {
			CCProjectNature.addCCNature(project, null);
		} catch (CoreException e1) {
			fail("fail to add CC nature");
		}
		
		IFile ac = ManagedBuildTestHelper.createFile(project, "a.c");
		IFile bc = ManagedBuildTestHelper.createFile(project, "b.c");
		IFile ccpp = ManagedBuildTestHelper.createFile(project, "c.cpp");
		IFile dcpp = ManagedBuildTestHelper.createFile(project, "d.cpp");
		IFile er = ManagedBuildTestHelper.createFile(project, "e.r");
		IFile fr = ManagedBuildTestHelper.createFile(project, "f.r");
		IFile gr = ManagedBuildTestHelper.createFile(project, "dir1/g.r");
		IFile hr = ManagedBuildTestHelper.createFile(project, "dir2/h.r");
		IFile ir = ManagedBuildTestHelper.createFile(project, "dir2/i.r");
		
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		String art = cfg.getArtifactName();
		String ext = cfg.getArtifactExtension();
		if(ext != null && ext.length() > 0)
			art = art + "." + ext;
		
		String cName = cfg.getName();
		String out = cName + "/";
		
		
		ManagedBuildTestHelper.createRcbsTool(cfg, ccpp, "f.r;dir1/g.r;dir2/h.r", "q.o;w.o;e.o", "a;b;c");
		ManagedBuildTestHelper.createRcbsTool(cfg, er, "f.r;dir1/g.r;dir2/h.r;dir2/i.r", "z.cpp;x.c", "d;e;f");
//		IResourceConfiguration rcCfg = cfg.createResourceConfiguration(ac);
//		assertNotNull(rcCfg);
//		rcCfg = cfg.createResourceConfiguration(ccpp);
//		assertNotNull(rcCfg);

		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("a.c"));
			type.addResource(tDes.createResource("b.c"));

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("e.r"));
			type.addResource(tDes.createResource("f.r"));
			type.addResource(tDes.createResource("dir1/g.r"));
			type.addResource(tDes.createResource("dir2/h.r"));
			type.addResource(tDes.createResource("dir2/i.r"));
		//
		//
		step = tDes.createStep(null, null);
		
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("a.c"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("b.c"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/b.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("c.cpp"));

			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("f.r"));
			type.addResource(tDes.createResource("dir1/g.r"));
			type.addResource(tDes.createResource("dir2/h.r"));

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/q.o"));
			type.addResource(tDes.createResource(cName + "/w.o"));
			type.addResource(tDes.createResource(cName + "/e.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("d.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/d.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("e.r"));

			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("f.r"));
			type.addResource(tDes.createResource("dir1/g.r"));
			type.addResource(tDes.createResource("dir2/h.r"));
			type.addResource(tDes.createResource("dir2/i.r"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "z.cpp"));
			type.addResource(tDes.createResource(out + "x.c"));
//			type.addResource(tDes.createResource(out + "r.o"));
//			type.addResource(tDes.createResource(out + "t.o"));
//			type.addResource(tDes.createResource(out + "y.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource(out + "z.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/z.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource(out + "x.c"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/x.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
			type.addResource(tDes.createResource(cName + "/b.o"));
//			type.addResource(tDes.createResource(cName + "/c.o"));
			type.addResource(tDes.createResource(cName + "/d.o"));
			type.addResource(tDes.createResource(cName + "/q.o"));
			type.addResource(tDes.createResource(cName + "/w.o"));
			type.addResource(tDes.createResource(cName + "/e.o"));
			type.addResource(tDes.createResource(cName + "/z.o"));
			type.addResource(tDes.createResource(cName + "/x.o"));
//			type.addResource(tDes.createResource(cName + "/r.o"));
//			type.addResource(tDes.createResource(cName + "/t.o"));
//			type.addResource(tDes.createResource(cName + "/y.o"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//
		//
		step = (BuildStep)tDes.getOutputStep();
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);

	}

	public void testDesAddlInVarUserObjs(){
		IProject project = createProject(PREFIX + "1", "cdt.managedbuild.target.gnu30.exe");
		try {
			CCProjectNature.addCCNature(project, null);
		} catch (CoreException e1) {
			fail("fail to add CC nature");
		}
		
		IFile ac = ManagedBuildTestHelper.createFile(project, "a.c");
		IFile bc = ManagedBuildTestHelper.createFile(project, "b.c");
		IFile ccpp = ManagedBuildTestHelper.createFile(project, "c.cpp");
		IFile dcpp = ManagedBuildTestHelper.createFile(project, "d.cpp");
		IFile er = ManagedBuildTestHelper.createFile(project, "e.r");
		IFile fr = ManagedBuildTestHelper.createFile(project, "f.r");
		IFile gr = ManagedBuildTestHelper.createFile(project, "dir1/g.r");
		IFile hr = ManagedBuildTestHelper.createFile(project, "dir2/h.r");
		IFile ir = ManagedBuildTestHelper.createFile(project, "dir2/i.r");
		IFile o1 = ManagedBuildTestHelper.createFile(project, "o1.o");
		IFile o2 = ManagedBuildTestHelper.createFile(project, "dir3/o2.o");
		
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		String art = cfg.getArtifactName();
		String ext = cfg.getArtifactExtension();
		if(ext != null && ext.length() > 0)
			art = art + "." + ext;
		
		String cName = cfg.getName();
		String out = cName + "/";
		
		ManagedBuildTestHelper.setObjs(cfg, new String[]{"o1.o", "dir3/o2.o", "dir4/d/o3.o"});
		
		
		ManagedBuildTestHelper.createRcbsTool(cfg, ccpp, "f.r;dir1/g.r;dir2/h.r", "q.o;w.o;e.o", "a;b;c");
		ManagedBuildTestHelper.createRcbsTool(cfg, er, "f.r;dir1/g.r;dir2/h.r;dir2/i.r", "z.cpp;x.c", "d;e;f");
//		IResourceConfiguration rcCfg = cfg.createResourceConfiguration(ac);
//		assertNotNull(rcCfg);
//		rcCfg = cfg.createResourceConfiguration(ccpp);
//		assertNotNull(rcCfg);

		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("a.c"));
			type.addResource(tDes.createResource("b.c"));

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("e.r"));
			type.addResource(tDes.createResource("f.r"));
			type.addResource(tDes.createResource("dir1/g.r"));
			type.addResource(tDes.createResource("dir2/h.r"));
			type.addResource(tDes.createResource("dir2/i.r"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("o1.o"));
			type.addResource(tDes.createResource("dir3/o2.o"));
			type.addResource(tDes.createResource("dir4/d/o3.o"));

		//
		//
		step = tDes.createStep(null, null);
		
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("a.c"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("b.c"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/b.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("c.cpp"));

			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("f.r"));
			type.addResource(tDes.createResource("dir1/g.r"));
			type.addResource(tDes.createResource("dir2/h.r"));

			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/q.o"));
			type.addResource(tDes.createResource(cName + "/w.o"));
			type.addResource(tDes.createResource(cName + "/e.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("d.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/d.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("e.r"));

			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("f.r"));
			type.addResource(tDes.createResource("dir1/g.r"));
			type.addResource(tDes.createResource("dir2/h.r"));
			type.addResource(tDes.createResource("dir2/i.r"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "z.cpp"));
			type.addResource(tDes.createResource(out + "x.c"));
//			type.addResource(tDes.createResource(out + "r.o"));
//			type.addResource(tDes.createResource(out + "t.o"));
//			type.addResource(tDes.createResource(out + "y.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource(out + "z.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/z.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource(out + "x.c"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/x.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
			type.addResource(tDes.createResource(cName + "/b.o"));
//			type.addResource(tDes.createResource(cName + "/c.o"));
			type.addResource(tDes.createResource(cName + "/d.o"));
			type.addResource(tDes.createResource(cName + "/q.o"));
			type.addResource(tDes.createResource(cName + "/w.o"));
			type.addResource(tDes.createResource(cName + "/e.o"));
			type.addResource(tDes.createResource(cName + "/z.o"));
			type.addResource(tDes.createResource(cName + "/x.o"));
//			type.addResource(tDes.createResource(cName + "/r.o"));
//			type.addResource(tDes.createResource(cName + "/t.o"));
//			type.addResource(tDes.createResource(cName + "/y.o"));

	//		type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("o1.o"));
			type.addResource(tDes.createResource("dir3/o2.o"));
			type.addResource(tDes.createResource("dir4/d/o3.o"));

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//
		//
		step = (BuildStep)tDes.getOutputStep();
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);

	}
	
	public void testDesAddlInVar(){
		IProject project = createProject(PREFIX + "1", "cdt.managedbuild.target.bdm.exe");
		try {
			CCProjectNature.addCCNature(project, null);
		} catch (CoreException e1) {
			fail("fail to add CC nature");
		}
		
		ManagedBuildTestHelper.createFile(project, "a.c");
		ManagedBuildTestHelper.createFile(project, "b.c");
		ManagedBuildTestHelper.createFile(project, "c.cpp");
		ManagedBuildTestHelper.createFile(project, "d.cpp");
		ManagedBuildTestHelper.createFile(project, "e.s1");
		ManagedBuildTestHelper.createFile(project, "dir/f.s1");
		ManagedBuildTestHelper.createFile(project, "g.s2");
		ManagedBuildTestHelper.createFile(project, "dir/h.s2");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		String art = cfg.getArtifactName();
		String ext = cfg.getArtifactExtension();
		if(ext != null && ext.length() > 0)
			art = art + "." + ext;
		
		String cName = cfg.getName();
		String out = cName + "/";

		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("a.c"));
			type.addResource(tDes.createResource("b.c"));

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("e.s1"));
			type.addResource(tDes.createResource("dir/f.s1"));

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("g.s2"));
			type.addResource(tDes.createResource("dir/h.s2"));
		//
		//
			step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("e.s1"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "e.o1"));
		//
		//
			step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("dir/f.s1"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "dir/f.o1"));
		//
		//
			step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("g.s2"));

			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource(out + "e.o1"));
			type.addResource(tDes.createResource(out + "dir/f.o1"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "g.o"));
		//
		//
			step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("dir/h.s2"));

			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource(out + "e.o1"));
			type.addResource(tDes.createResource(out + "dir/f.o1"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(out + "dir/h.o"));
		//
		//
		step = tDes.createStep(null, null);
		
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("a.c"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("b.c"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/b.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("c.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/c.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("d.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/d.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
			type.addResource(tDes.createResource(cName + "/b.o"));
			type.addResource(tDes.createResource(cName + "/c.o"));
			type.addResource(tDes.createResource(cName + "/d.o"));
			type.addResource(tDes.createResource(out + "g.o"));
			type.addResource(tDes.createResource(out + "dir/h.o"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//
		//
		step = (BuildStep)tDes.getOutputStep();
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);
	}

	public void testDes_gnu30_exe_objsInProj(){
		IProject project = createProject(PREFIX + "1", "cdt.managedbuild.target.gnu30.exe");
		try {
			CCProjectNature.addCCNature(project, null);
		} catch (CoreException e1) {
			fail("fail to add CC nature");
		}
		
		ManagedBuildTestHelper.createFile(project, "a.c");
		ManagedBuildTestHelper.createFile(project, "b.c");
		ManagedBuildTestHelper.createFile(project, "c.cpp");
		ManagedBuildTestHelper.createFile(project, "d.cpp");
		ManagedBuildTestHelper.createFile(project, "e.o");
		ManagedBuildTestHelper.createFile(project, "dir/f.o");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		String art = cfg.getArtifactName();
		String ext = cfg.getArtifactExtension();
		if(ext != null && ext.length() > 0)
			art = art + "." + ext;
		
		String cName = cfg.getName();

		BuildDescription tDes = new BuildDescription(cfg);
		BuildStep step;
		BuildIOType type;
		
		//
		step = (BuildStep)tDes.getInputStep();

			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("c.cpp"));
			type.addResource(tDes.createResource("d.cpp"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource("a.c"));
			type.addResource(tDes.createResource("b.c"));

		//
		//
		step = tDes.createStep(null, null);
		
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("a.c"));
			
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("b.c"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/b.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("c.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/c.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, false, null);
			type.addResource(tDes.createResource("d.cpp"));
		
			type = step.createIOType(false, true, null);
			type.addResource(tDes.createResource(cName + "/d.o"));
		//
		//
		step = tDes.createStep(null, null);
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/a.o"));
			type.addResource(tDes.createResource(cName + "/b.o"));
			type.addResource(tDes.createResource(cName + "/c.o"));
			type.addResource(tDes.createResource(cName + "/d.o"));
			
			type = step.createIOType(false, false, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//
		//
		step = (BuildStep)tDes.getOutputStep();
			
			type = step.createIOType(true, true, null);
			type.addResource(tDes.createResource(cName + "/" + art));
		//

		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}

		doTestBuildDescription(des, tDes);
	}
	
	public void testDesRebuildState(){
		IProject project = createProject(PREFIX + "1", "cdt.managedbuild.target.gnu30.exe");
		try {
			CCProjectNature.addCCNature(project, null);
		} catch (CoreException e1) {
			fail("fail to add CC nature");
		}
		
		ManagedBuildTestHelper.createFile(project, "a.c");
		ManagedBuildTestHelper.createFile(project, "b.c");
		ManagedBuildTestHelper.createFile(project, "c.cpp");
		ManagedBuildTestHelper.createFile(project, "d.cpp");
		ManagedBuildTestHelper.createFile(project, "e.o");
		ManagedBuildTestHelper.createFile(project, "dir/f.o");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		IConfiguration cfg = mProj.getConfigurations()[0];
		
		cfg.setRebuildState(false);
		
		assertFalse(cfg.needsRebuild());
		
		ITool targetTool = ((Configuration)cfg).calculateTargetTool();

		cfg.setArtifactName("asdafasdfasdfasdfasdf");
		assertTrue(targetTool.needsRebuild());
		assertTrue(cfg.needsRebuild());
		targetTool.setRebuildState(false);
		assertFalse(targetTool.needsRebuild());
		assertFalse(cfg.needsRebuild());

		cfg.setBuildArguments("-fgsdfg -sdfg -sdfg -sfdg");
//		assertFalse(targetTool.needsRebuild());
//		assertTrue(cfg.needsRebuild());
//		cfg.setRebuildState(false);
//		assertFalse(targetTool.needsRebuild());
		assertFalse(cfg.needsRebuild());

		cfg.setBuildCommand("fdgsdfbvvcbsdfvcx");
		assertFalse(cfg.needsRebuild());

		cfg.setCleanCommand("sadgvfcxvsdfgvxc");
		assertFalse(cfg.needsRebuild());

		cfg.setDescription("sfgsdfgsdfcxbvxcbxcvb");
		assertFalse(cfg.needsRebuild());

		cfg.setErrorParserIds("fgdsfgsdfgsdfgsdfgsdfgdfs;sdfg;sdfg;sdg;g;sdg");
		assertFalse(cfg.needsRebuild());

		cfg.setName("sdfgsdfgsdfgsdfgsdfgdsfgsdf");
//		assertFalse(targetTool.needsRebuild());
//		assertTrue(cfg.needsRebuild());
//		cfg.setRebuildState(false);
		assertFalse(targetTool.needsRebuild());
		assertFalse(cfg.needsRebuild());
		
		cfg.setPostannouncebuildStep("sdfasdfasdfsdfadfasf");
		assertFalse(cfg.needsRebuild());

		cfg.setPostbuildStep("asdf;asdf;asdf;asdf;asdf");
		assertFalse(cfg.needsRebuild());
		
		cfg.setPreannouncebuildStep("sdfgsdgsdhnbvxcvbxcv");
		assertFalse(cfg.needsRebuild());

		cfg.setPrebuildStep("sdfg;sdfg;sdfg;sdfgvnbcvbn");
		assertFalse(cfg.needsRebuild());

		ITool tools[] = cfg.getFilteredTools();
		ITool tool = null;
		
		for(int i = 0; i < tools.length; i++){
			tool = tools[i];
			if(tool != targetTool)
				break;
		}
		
		cfg.setToolCommand(tool, "sdgsdcxvzcxvzxc");
		assertTrue(tool.needsRebuild());
		assertTrue(cfg.needsRebuild());
		tool.setRebuildState(false);
		assertFalse(tool.needsRebuild());
		assertFalse(cfg.needsRebuild());
		
		cfg.setArtifactExtension("adsfasdfasdfasdfasdf");
		assertTrue(targetTool.needsRebuild());
		assertTrue(cfg.needsRebuild());
		targetTool.setRebuildState(false);
		assertFalse(targetTool.needsRebuild());
		assertFalse(cfg.needsRebuild());

//		public static final int BOOLEAN = 0;
//		public static final int ENUMERATED = 1;
//		public static final int STRING = 2;
//		public static final int STRING_LIST = 3;
//		public static final int INCLUDE_PATH = 4;
//		public static final int PREPROCESSOR_SYMBOLS = 5;
//		public static final int LIBRARIES = 6;
//		public static final int OBJECTS = 7;

		IBuildObject obj[] = ManagedBuildTestHelper.getOption(cfg, IOption.BOOLEAN);
		if(obj != null){
			IHoldsOptions ho = (IHoldsOptions)obj[0];
			IOption o = (IOption)obj[1];
			
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			try {
				cfg.setOption(ho, o, !o.getBooleanValue());
			} catch (BuildException e) {
				fail("failed to set the option: " + e.getLocalizedMessage());
			}
			
			assertTrue(ho.needsRebuild());
			assertTrue(cfg.needsRebuild());
			ho.setRebuildState(false);
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}

		obj = ManagedBuildTestHelper.getOption(cfg, IOption.ENUMERATED);
		if(obj != null){
			IHoldsOptions ho = (IHoldsOptions)obj[0];
			IOption o = (IOption)obj[1];
			
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			try {
				cfg.setOption(ho, o, "sdfgsdfcvsdfgvxcsdf");
			} catch (BuildException e) {
				fail("failed to set the option: " + e.getLocalizedMessage());
			}
			
			assertTrue(ho.needsRebuild());
			assertTrue(cfg.needsRebuild());
			ho.setRebuildState(false);
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}

		obj = ManagedBuildTestHelper.getOption(cfg, IOption.STRING);
		if(obj != null){
			IHoldsOptions ho = (IHoldsOptions)obj[0];
			IOption o = (IOption)obj[1];
			
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			try {
				cfg.setOption(ho, o, "sdfgsdfcvsdfgvxcfdgvsdf");
			} catch (BuildException e) {
				fail("failed to set the option: " + e.getLocalizedMessage());
			}
			
			assertTrue(ho.needsRebuild());
			assertTrue(cfg.needsRebuild());
			ho.setRebuildState(false);
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}

		obj = ManagedBuildTestHelper.getOption(cfg, IOption.STRING_LIST);
		if(obj != null){
			IHoldsOptions ho = (IHoldsOptions)obj[0];
			IOption o = (IOption)obj[1];
			
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			try {
				cfg.setOption(ho, o, new String[]{"sdfgsd","fcvsdfgvxcfdgvsdf"});
			} catch (BuildException e) {
				fail("failed to set the option: " + e.getLocalizedMessage());
			}
			
			assertTrue(ho.needsRebuild());
			assertTrue(cfg.needsRebuild());
			ho.setRebuildState(false);
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}

		obj = ManagedBuildTestHelper.getOption(cfg, IOption.INCLUDE_PATH);
		if(obj != null){
			IHoldsOptions ho = (IHoldsOptions)obj[0];
			IOption o = (IOption)obj[1];
			
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			try {
				cfg.setOption(ho, o, new String[]{"sdfgsd","fcvsdfgvxcfdgvsdf"});
			} catch (BuildException e) {
				fail("failed to set the option: " + e.getLocalizedMessage());
			}
			
			assertTrue(ho.needsRebuild());
			assertTrue(cfg.needsRebuild());
			ho.setRebuildState(false);
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}

		
		obj = ManagedBuildTestHelper.getOption(cfg, IOption.PREPROCESSOR_SYMBOLS);
		if(obj != null){
			IHoldsOptions ho = (IHoldsOptions)obj[0];
			IOption o = (IOption)obj[1];
			
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			try {
				cfg.setOption(ho, o, new String[]{"sdfgsd","fcvsdfgvxcfdgvsdf"});
			} catch (BuildException e) {
				fail("failed to set the option: " + e.getLocalizedMessage());
			}
			
			assertTrue(ho.needsRebuild());
			assertTrue(cfg.needsRebuild());
			ho.setRebuildState(false);
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}

		obj = ManagedBuildTestHelper.getOption(cfg, IOption.LIBRARIES);
		if(obj != null){
			IHoldsOptions ho = (IHoldsOptions)obj[0];
			IOption o = (IOption)obj[1];
			
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			try {
				cfg.setOption(ho, o, new String[]{"sdfgsd","fcvsdfgvxcfdgvsdf"});
			} catch (BuildException e) {
				fail("failed to set the option: " + e.getLocalizedMessage());
			}
			
			assertTrue(ho.needsRebuild());
			assertTrue(cfg.needsRebuild());
			ho.setRebuildState(false);
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}

		obj = ManagedBuildTestHelper.getOption(cfg, IOption.OBJECTS);
		if(obj != null){
			IHoldsOptions ho = (IHoldsOptions)obj[0];
			IOption o = (IOption)obj[1];
			
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			try {
				cfg.setOption(ho, o, new String[]{"sdfgsd","fcvsdfgvxcfdgvsdf"});
			} catch (BuildException e) {
				fail("failed to set the option: " + e.getLocalizedMessage());
			}
			
			assertTrue(ho.needsRebuild());
			assertTrue(cfg.needsRebuild());
			ho.setRebuildState(false);
			assertFalse(ho.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}

		IToolChain tch = cfg.getToolChain();
		tch.setRebuildState(true);
		assertTrue(tch.needsRebuild());
		assertTrue(cfg.needsRebuild());

		tch.setRebuildState(false);
		assertFalse(tch.needsRebuild());
		assertFalse(cfg.needsRebuild());
		
		for(int i = 0; i < tools.length; i++){
			doTestTool(tools[i]);
		}
		
		IResourceConfiguration rcCfgs[] = cfg.getResourceConfigurations();
		for(int i = 0; i < rcCfgs.length; i++){
			IResourceConfiguration rcCfg = rcCfgs[i];
			
			assertFalse(rcCfg.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			rcCfg.setRebuildState(true);
			assertTrue(rcCfg.needsRebuild());
			assertTrue(cfg.needsRebuild());
			
			rcCfg.setRebuildState(false);
			assertFalse(rcCfg.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			rcCfg.setExclude(!rcCfg.isExcluded());
			assertTrue(rcCfg.needsRebuild());
			assertTrue(cfg.needsRebuild());

			rcCfg.setRebuildState(false);
			assertFalse(rcCfg.needsRebuild());
			assertFalse(cfg.needsRebuild());

			rcCfg.setExclude(rcCfg.isExcluded());
			assertFalse(rcCfg.needsRebuild());
			assertFalse(cfg.needsRebuild());

			int appl = rcCfg.getRcbsApplicability() + 1;
			if(appl > 4)
				appl = 1;
			rcCfg.setRcbsApplicability(appl);
			assertTrue(rcCfg.needsRebuild());
			assertTrue(cfg.needsRebuild());
			rcCfg.setRebuildState(false);
			assertFalse(rcCfg.needsRebuild());
			assertFalse(cfg.needsRebuild());

			rcCfg.setRcbsApplicability(rcCfg.getRcbsApplicability());
			assertFalse(rcCfg.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			ITool t = rcCfg.getToolsToInvoke()[0];
			assertFalse(t.needsRebuild());
			rcCfg.setToolCommand(t, "sdsdcdsffewffdvcx");
			assertTrue(t.needsRebuild());
			assertTrue(rcCfg.needsRebuild());
			assertTrue(cfg.needsRebuild());
			t.setRebuildState(false);
			assertFalse(t.needsRebuild());
			assertFalse(rcCfg.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			rcCfg.setToolCommand(t, t.getToolCommand());
			assertFalse(t.needsRebuild());
			assertFalse(rcCfg.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}
	}
	
	private void doTestTool(ITool tool){
		IBuildObject obj = tool.getParent();
		IConfiguration cfg;
		if(obj instanceof IResourceConfiguration)
			cfg = ((IResourceConfiguration)obj).getParent();
		else
			cfg = ((IToolChain)obj).getParent();
		
		cfg.setRebuildState(false);
		
		assertFalse(tool.needsRebuild());
		
		tool.setRebuildState(true);
		assertTrue(tool.needsRebuild());
		
		assertTrue(cfg.needsRebuild());
		
		cfg.setRebuildState(false);

		assertFalse(tool.needsRebuild());
		assertFalse(cfg.needsRebuild());
		
		tool.setRebuildState(true);
		assertTrue(tool.needsRebuild());
		
		assertTrue(cfg.needsRebuild());
		
		tool.setRebuildState(false);

		assertFalse(tool.needsRebuild());
		assertFalse(cfg.needsRebuild());
		
		tool.setCommandLinePattern("asdfasdfasdfasdfasdfasdsdfghdsfg");
		assertTrue(tool.needsRebuild());
		assertTrue(cfg.needsRebuild());
		tool.setRebuildState(false);
		assertFalse(tool.needsRebuild());
		assertFalse(cfg.needsRebuild());

		tool.setOutputFlag("asdfasdfasdfasdgsdf");
		assertTrue(tool.needsRebuild());
		assertTrue(cfg.needsRebuild());
		tool.setRebuildState(false);
		assertFalse(tool.needsRebuild());
		assertFalse(cfg.needsRebuild());
		
		tool.setOutputPrefix("afgsdfgfadcvwerfdvsdczxv");
		assertTrue(tool.needsRebuild());
		assertTrue(cfg.needsRebuild());
		tool.setRebuildState(false);
		assertFalse(tool.needsRebuild());
		assertFalse(cfg.needsRebuild());

		tool.setRebuildState(true);
		assertTrue(tool.needsRebuild());
		assertTrue(cfg.needsRebuild());
		tool.setRebuildState(false);
		assertFalse(tool.needsRebuild());
		assertFalse(cfg.needsRebuild());

		tool.setToolCommand("dfacvzxcgrsedfxcvsdfcgv");
		assertTrue(tool.needsRebuild());
		assertTrue(cfg.needsRebuild());
		tool.setRebuildState(false);
		assertFalse(tool.needsRebuild());
		assertFalse(cfg.needsRebuild());

		IInputType iTypes[] = tool.getInputTypes();
		
		for(int i = 0; i < iTypes.length; i++){
			IInputType iType = iTypes[i];
			
			if(iType.isExtensionElement())
				continue;
			
			iType.setAssignToOptionId("qwertyuiop");
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());
			
			iType.setBuildVariable("asdfghjkl");
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());

			iType.setMultipleOfType(!iType.getMultipleOfType());
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());

			iType.setOptionId("zxcvbnm");
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());

			iType.setPrimaryInput(!iType.getPrimaryInput());
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());

			IAdditionalInput addlIns[] = iType.getAdditionalInputs();
			for(int j = 0; j < addlIns.length; j++){
				IAdditionalInput a = addlIns[j];
				
				a.setPaths("as;sd;fgl;fg;qw;er;ty;ui;op");
				assertTrue(tool.needsRebuild());
				assertTrue(cfg.needsRebuild());
				tool.setRebuildState(false);
				assertFalse(tool.needsRebuild());
				assertFalse(cfg.needsRebuild());
			}
		}
		
		IOutputType oTypes[] = tool.getOutputTypes();
		
		for(int i = 0; i < oTypes.length; i++){
			IOutputType oType = oTypes[i];

			if(oType.isExtensionElement())
				continue;

			oType.setBuildVariable("qwertyuiop");
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());

			oType.setMultipleOfType(!oType.getMultipleOfType());
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());
		
			oType.setNamePattern("qwerytuuioioyuioghjgfd");
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());

			oType.setOptionId("asdfghjklkxcvx");
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());

			oType.setOutputNames("asdf;dfgh;hj;jk;ghjk;fgg;sdaf;asdf");
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());

			oType.setOutputPrefix("asdfscvbdfgsdfgsdfvcx");
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());

			oType.setPrimaryOutput(!oType.getPrimaryOutput());
			assertTrue(tool.needsRebuild());
			assertTrue(cfg.needsRebuild());
			tool.setRebuildState(false);
			assertFalse(tool.needsRebuild());
			assertFalse(cfg.needsRebuild());
		}
	}
	
	public void testDesRebuildStateInDescription(){
		IProject project = createProject(PREFIX + "1", "cdt.managedbuild.target.bdm.exe");
		try {
			CCProjectNature.addCCNature(project, null);
		} catch (CoreException e1) {
			fail("fail to add CC nature");
		}
		
		IFile ac = ManagedBuildTestHelper.createFile(project, "a.c");
		IFile bc = ManagedBuildTestHelper.createFile(project, "b.c");
		IFile ccpp = ManagedBuildTestHelper.createFile(project, "c.cpp");
		IFile dcpp = ManagedBuildTestHelper.createFile(project, "d/d.cpp");
		IFile es2 = ManagedBuildTestHelper.createFile(project, "d2/e.s2");
		IFile fs2 = ManagedBuildTestHelper.createFile(project, "f.s2");
		
		ManagedBuildTestHelper.createFile(project, "e.o");
		ManagedBuildTestHelper.createFile(project, "dir/f.o");
	
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IManagedProject mProj = info.getManagedProject();
		Configuration cfg = (Configuration)mProj.getConfigurations()[0];
		String out = cfg.getName() + "/";
		
		cfg.setRebuildState(false);
		
		IBuildDescription des = null;
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}
		
		IBuildResource rcs[] = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REBUILD);
		assertEquals(rcs.length, 0);
		
		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REMOVED);
		assertEquals(rcs.length, 0);

		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		assertEquals(rcs.length, 0);

		//target
		ITool tool = cfg.calculateTargetTool();
		
		tool.setToolCommand("fgdfgcvbcbv");
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}
		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REBUILD);
		IBuildResource oRcs[] = des.getOutputStep().getInputResources();
		IBuildResource targetRc;
		assertEquals(rcs.length, 1);
		assertEquals(oRcs.length, 1);
		if(rcs[0] != oRcs[0])
			fail("rebuild resources do not match");
		
		targetRc = oRcs[0];
		
		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REMOVED);
		assertEquals(rcs.length, 0);

		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		assertEquals(rcs.length, 1);
		assertEquals(oRcs.length, 1);
		if(rcs[0] != oRcs[0])
			fail("rebuild resources do not match");
		
		cfg.setRebuildState(false);
		
		//cpp
		tool = getToolForInExt(cfg, "cpp");
		tool.setToolCommand("sdfgzxcvzxcvzxv");
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}
		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REBUILD);
		oRcs = des.getOutputStep().getInputResources();
		assertEquals(oRcs.length, 1);
		targetRc = oRcs[0];

		oRcs = new IBuildResource[5];
		oRcs[0] = getResourceForProjPath(des, out + "c.o");
		assertNotNull(oRcs[0]);
		oRcs[1] = getResourceForProjPath(des, out + "d/d.o");
		assertNotNull(oRcs[1]);
		oRcs[2] = targetRc;
		oRcs[3] = getResourceForProjPath(des, out + "a.o");
		assertNotNull(oRcs[0]);
		oRcs[4] = getResourceForProjPath(des, out + "b.o");
		assertNotNull(oRcs[1]);
		
		assertEquals(rcs.length, 5);
		assertEquals(oRcs.length, 5);
		if(!resourcesEqual(rcs, oRcs))
			fail("rebuild resources do not match");
		
		
		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REMOVED);
		assertEquals(rcs.length, 0);

		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		assertEquals(rcs.length, 5);
		if(!resourcesEqual(rcs, oRcs))
			fail("rebuild resources do not match");
		
		cfg.setRebuildState(false);
	
		//s2
		tool = getToolForInExt(cfg, "s2");
		tool.setToolCommand("sdfgzxcvzxcvzxv");
		try {
			des = BuildDescriptionManager.createBuildDescription(cfg, null, BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		} catch (CoreException e) {
			fail("build description creation failed: " + e.getLocalizedMessage());
		}
		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REBUILD);
		oRcs = des.getOutputStep().getInputResources();
		assertEquals(oRcs.length, 1);
		targetRc = oRcs[0];

		oRcs = new IBuildResource[3];
		oRcs[0] = getResourceForProjPath(des, out + "d2/e.o");
		assertNotNull(oRcs[0]);
		oRcs[1] = getResourceForProjPath(des, out + "f.o");
		assertNotNull(oRcs[1]);
		oRcs[2] = targetRc;
		
		assertEquals(rcs.length, 3);
		assertEquals(oRcs.length, 3);
		if(!resourcesEqual(rcs, oRcs))
			fail("rebuild resources do not match");
		
		
		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REMOVED);
		assertEquals(rcs.length, 0);

		rcs = BuildDescriptionManager.filterBuildResources(des.getResources(), BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED);
		assertEquals(rcs.length, 3);
		if(!resourcesEqual(rcs, oRcs))
			fail("rebuild resources do not match");
		
		cfg.setRebuildState(false);
		
	}
	
	private IBuildResource getResourceForProjPath(IBuildDescription des, String path){
		return getResourceForProjPath(des, new Path(path));
	}

	private IBuildResource getResourceForProjPath(IBuildDescription des, IPath path){
		IPath location = des.getConfiguration().getOwner().getProject().getLocation().append(path);
		return des.getResourceForLocation(location);
	}

	private ITool getToolForInExt(IConfiguration cfg, String ext){
		ITool tools[] = cfg.getFilteredTools();
		for(int i = 0; i < tools.length; i++){
			if(tools[i].buildsFileType(ext))
				return tools[i];
		}
		return null;
	}
	
	private boolean resourcesEqual(IBuildResource rcs[], IBuildResource oRcs[]){
		if(rcs.length != oRcs.length)
			return false;
		
		for(int j = 0; j < rcs.length; j++){
			int k;
			for(k = 0; k < oRcs.length; k++){
				if(oRcs[k] == rcs[j]){
					break;
				}
			}
			if(k == oRcs.length)
				return false;
		}
		return true;
	}
}
