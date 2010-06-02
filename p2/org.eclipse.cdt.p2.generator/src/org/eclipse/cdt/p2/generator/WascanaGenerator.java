/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.p2.generator;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository;
import org.eclipse.equinox.internal.p2.metadata.ArtifactKey;
import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.ILicense;
import org.eclipse.equinox.p2.metadata.IProvidedCapability;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.ITouchpointType;
import org.eclipse.equinox.p2.metadata.IUpdateDescriptor;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.repository.IRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.spi.ArtifactDescriptor;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

/**
 * @author DSchaefe
 *
 */
public class WascanaGenerator implements IApplication {

	private static Version binutilsVersion = Version.parseVersion("2.20.0.0");
	private static Version gccBaseVersion = Version.parseVersion("4.4.1.0");
	private static Version gccVersion = Version.parseVersion("4.4.1.0");
	private static Version msysVersion = Version.parseVersion("1.0.11.0");
	
	private static Version mingwrtVersion = Version.parseVersion("3.15.2.0");
	private static Version w32apiVersion = Version.parseVersion("3.13.0.0");
	private static Version qtVersion = Version.parseVersion("4.6.0.0");
	private static Version wascanaVersion = Version.parseVersion("1.0.0.0");

	private static final String REPO_NAME = "Wascana";
	
	private static final ITouchpointType NATIVE_TOUCHPOINT
		= MetadataFactory.createTouchpointType("org.eclipse.equinox.p2.native", Version.parseVersion("1.0.0"));
	
	private IMetadataRepository metaRepo;
	private IArtifactRepository artiRepo;
	
	private ILicense gpl30License;
	private ILicense lgpl21License;
	private ILicense pdLicense; // public domain
	
	private List<IInstallableUnit> iuList = new ArrayList<IInstallableUnit>();
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		context.applicationRunning();

		String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (args.length < 1) {
			System.err.println("usage: <repoDir>");
			return EXIT_OK;
		}
		
		File repoDir = new File(args[0]);
		createRepos(repoDir);
		loadLicenses();

		// tools
		
		IInstallableUnit binutilsIU = createIU(
				"wascana.binutils",
				"Wascana MinGW Binutils",
				binutilsVersion,
				gpl30License,
				null);
		IInstallableUnit binutilsSrcIU = createIU(
				"wascana.binutils.source",
				"Wascana MinGW Binutils Source",
				binutilsVersion,
				gpl30License,
				null);
		
		IInstallableUnit gccIU = createIU(
				"wascana.gcc.core",
				"Wascana MinGW/TDM GCC C Compiler",
				gccVersion,
				gpl30License,
				null);
				
		IInstallableUnit gppIU = createIU(
				"wascana.gcc.g++",
				"Wascana MinGW/TDM GCC C++ Compiler",
				gccVersion,
				gpl30License,
				null);
		
		IInstallableUnit gccSrcIU = createIU(
				"wascana.gcc.core.source",
				"Wascana Base GCC C Compiler Source",
				gccBaseVersion,
				gpl30License,
				null);
				
		IInstallableUnit gppSrcIU = createIU(
				"wascana.gcc.g++.source",
				"Wascana Base GCC C++ Compiler Source",
				gccBaseVersion,
				gpl30License,
				null);
				
		IInstallableUnit gccTDMSrcIU = createIU(
				"wascana.gcc.source.tdm",
				"Wascana TDM GCC Patches",
				gccVersion,
				gpl30License,
				null);
				
		IInstallableUnit gdbIU = createIU(
				"wascana.gdb",
				"Wascana MinGW GDB Debugger",
				Version.parseVersion("7.0.1.1"),
				gpl30License,
				null);

		IInstallableUnit gdbSrcIU = createIU(
				"wascana.gdb.source",
				"Wascana GDB Debugger Source",
				Version.parseVersion("7.0.1.0"),
				gpl30License,
				null);
				
		IInstallableUnit msysIU = createIU(
				"wascana.msys",
				"Wascana MSYS Shell Environment",
				msysVersion,
				gpl30License,
				null);
		
		IInstallableUnit msysSrcIU = createIU(
				"wascana.msys.source",
				"Wascana MSYS Shell Environment Source",
				msysVersion,
				gpl30License,
				null);
		
		IInstallableUnit toolsIU = createCategory(
				"wascana.tools",
				"Wascana Tools",
				wascanaVersion,
				new IRequirement[] {
						createRequiredCap(binutilsIU),
						createRequiredCap(gccIU),
						createRequiredCap(gppIU),
						createRequiredCap(gdbIU),
						createRequiredCap(msysIU),
				});

		// sdks
		
		IInstallableUnit mingwrtIU = createIU(
				"wascana.mingwrt",
				"Wascana MinGW Runtime",
				mingwrtVersion,
				pdLicense,
				null);
				
		IInstallableUnit mingwrtSrcIU = createIU(
				"wascana.mingwrt.source",
				"Wascana MinGW Runtime Source",
				mingwrtVersion,
				pdLicense,
				null);
				
		IInstallableUnit w32apiIU = createIU(
				"wascana.w32api",
				"Wascana Win32 Headers and Stub Libraries",
				w32apiVersion,
				pdLicense,
				null);
		
		IInstallableUnit w32apiSrcIU = createIU(
				"wascana.w32api.source",
				"Wascana Win32 Headers and Stub Libraries Source",
				w32apiVersion,
				pdLicense,
				null);
		
		IInstallableUnit qtIU = createIU(
				"wascana.qt",
				"Wascana Qt",
				qtVersion,
				lgpl21License,
				null);
				
		IInstallableUnit qtSrcIU = createIU(
				"wascana.qt.source",
				"Wascana Qt Source",
				qtVersion,
				lgpl21License,
				null);
				
		IInstallableUnit sdksIU = createCategory(
				"wascana.sdks",
				"Wascana SDKs",
				wascanaVersion,
				new IRequirement[] {
						createRequiredCap(mingwrtIU),
						createRequiredCap(w32apiIU),
						createRequiredCap(qtIU),
				});

		IInstallableUnit sourceIU = createCategory(
				"wascana.source",
				"Wascana Desktop Developer Source",
				wascanaVersion,
				new IRequirement[] {
						createRequiredCap(binutilsSrcIU),
						createRequiredCap(gccSrcIU),
						createRequiredCap(gppSrcIU),
						createRequiredCap(gccTDMSrcIU),
						createRequiredCap(gdbSrcIU),
						createRequiredCap(msysSrcIU),
						createRequiredCap(mingwrtSrcIU),
						createRequiredCap(w32apiSrcIU),
						createRequiredCap(qtSrcIU),
				});
		
		IInstallableUnit wascanaIU = createCategory(
				"wascana",
				"Wascana Desktop Developer",
				wascanaVersion,
				new IRequirement[] {
						createRequiredCap(toolsIU),
						createRequiredCap(sdksIU),
				});

		metaRepo.addInstallableUnits(iuList);

		System.out.println("done");
		
		return EXIT_OK;
	}

	@Override
	public void stop() {
	}

	private void createRepos(File repoDir) throws ProvisionException {
		repoDir.mkdirs();
		
		new File(repoDir, "artifacts.jar").delete();
		new File(repoDir, "artifacts.xml").delete();
		new File(repoDir, "content.jar").delete();
		new File(repoDir, "content.xml").delete();
		
		URI repoLocation = repoDir.toURI();
		
		IProvisioningAgent agent = Activator.getService(IProvisioningAgent.class);
		if (agent == null) {
			IProvisioningAgentProvider provider = Activator.getService(IProvisioningAgentProvider.class);
			agent = provider.createAgent(null);
		}

		IMetadataRepositoryManager metaRepoMgr = (IMetadataRepositoryManager)agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
		IArtifactRepositoryManager artiRepoMgr = (IArtifactRepositoryManager)agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
		
		metaRepo = metaRepoMgr.createRepository(repoLocation, REPO_NAME, IMetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
		metaRepo.setProperty(IRepository.PROP_COMPRESSED, Boolean.TRUE.toString());
		
		artiRepo = artiRepoMgr.createRepository(repoLocation, REPO_NAME, IArtifactRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
		artiRepo.setProperty(IRepository.PROP_COMPRESSED, Boolean.TRUE.toString());
		
		// add our own mapping
		SimpleArtifactRepository simpleArtiRepo = (SimpleArtifactRepository)artiRepo;
		String[][] rules = simpleArtiRepo.getRules();
		String[][] newrules = new String[rules.length + 1][];
		System.arraycopy(rules, 0, newrules, 0, rules.length);
		String[] pkgrule = new String[] {
				"(& (classifier=package))",
				"${repoUrl}/packages/${id}-${version}.tar.bz2"
		};
		newrules[rules.length] = pkgrule;
		simpleArtiRepo.setRules(newrules);
	}

	private void loadLicenses() throws IOException, URISyntaxException {
		gpl30License = MetadataFactory.createLicense(
				new URI("http://www.gnu.org/licenses/gpl.html"),
				Activator.getFileContents(new Path("licenses/gpl-3.0.txt")));
		lgpl21License = MetadataFactory.createLicense(
				new URI("http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html"),
				Activator.getFileContents(new Path("licenses/lgpl-2.1.txt")));
		pdLicense = MetadataFactory.createLicense(null, "This package has no copyright assignment and is placed in the Public Domain.");
	}
	
	private InstallableUnitDescription createIUDesc(String id, String name, Version version, ILicense license) throws ProvisionException {
		InstallableUnitDescription iuDesc = new MetadataFactory.InstallableUnitDescription();
		iuDesc.setId(id);
		iuDesc.setVersion(version);
		iuDesc.setLicenses(new ILicense[] { license });
		iuDesc.setSingleton(true);
		iuDesc.setProperty(IInstallableUnit.PROP_NAME, name);
		iuDesc.setCapabilities(new IProvidedCapability[] {
				MetadataFactory.createProvidedCapability(IInstallableUnit.NAMESPACE_IU_ID, id, version)
			});
		iuDesc.setUpdateDescriptor(MetadataFactory.createUpdateDescriptor(id, new VersionRange(null), IUpdateDescriptor.NORMAL, ""));
		return iuDesc;
	}

	private IInstallableUnit createIU(String id, String name, Version version, ILicense license,
			IRequiredCapability[] reqs) throws ProvisionException {
		InstallableUnitDescription iuDesc = createIUDesc(id, name, version, license);
		if (reqs != null)
			iuDesc.setRequiredCapabilities(reqs);

		iuDesc.setProperty(InstallableUnitDescription.PROP_TYPE_GROUP, String.valueOf(true));
		iuDesc.setTouchpointType(NATIVE_TOUCHPOINT);
		Map<String, String> tpdata = new HashMap<String, String>();
		
		tpdata.put("install", "untar(source:@artifact, target:${installFolder}, compression: bz2)");
		tpdata.put("uninstall", "cleanupuntar(source:@artifact, target:${installFolder})");
		
		iuDesc.addTouchpointData(MetadataFactory.createTouchpointData(tpdata));
		IArtifactKey artiKey = new ArtifactKey("package", id, version);
		ArtifactDescriptor artiDesc = new ArtifactDescriptor(artiKey);
		artiRepo.addDescriptor(artiDesc);
		iuDesc.setArtifacts(new IArtifactKey[] { artiKey });
		IInstallableUnit iu = MetadataFactory.createInstallableUnit(iuDesc);
		iuList.add(iu);
		return iu;
	}

	private IInstallableUnit createCategory(String id, String name, Version version,
			IRequirement[] reqs) throws ProvisionException {
		InstallableUnitDescription iuDesc = createIUDesc(id, name, version, null);
		if (reqs != null)
			iuDesc.setRequiredCapabilities(reqs);
		iuDesc.setProperty(InstallableUnitDescription.PROP_TYPE_CATEGORY, String.valueOf(true));
		IInstallableUnit iu = MetadataFactory.createInstallableUnit(iuDesc);
		iuList.add(iu);
		return iu;
	}

	private IRequirement createRequiredCap(IInstallableUnit iu) {
		return MetadataFactory.createRequirement(
				IInstallableUnit.NAMESPACE_IU_ID,
				iu.getId(), new VersionRange(null), null, false, false);
	}
	
	private IRequirement createStrictRequiredCap(IInstallableUnit iu) {
		return MetadataFactory.createRequirement(
				IInstallableUnit.NAMESPACE_IU_ID,
				iu.getId(), new VersionRange(iu.getVersion(), true, iu.getVersion(), true), null, false, false);
	}
	
}
