/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.p2.internal.repo.artifact.InstallArtifactRepository;
import org.eclipse.cdt.p2.internal.touchpoint.SDKTouchpoint;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.ArtifactDescriptor;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
import org.eclipse.equinox.internal.provisional.p2.metadata.ProvidedCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.RequiredCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.internal.provisional.p2.metadata.generator.MetadataGeneratorHelper;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * @author DSchaefe
 *
 */
public class MinGWGenerator implements IApplication {

	private static final String REPO_NAME = "Wascana";
	
	IMetadataRepository metaRepo;
	IArtifactRepository artiRepo;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		context.applicationRunning();

		Activator.getDefault().getBundle("org.eclipse.equinox.p2.exemplarysetup").start(Bundle.START_TRANSIENT); //$NON-NLS-1$

		File repoDir = new File("C:\\Wascana\\repo");
		new File(repoDir, "artifacts.xml").delete();
		new File(repoDir, "content.xml").delete();
		
		URL repoLocation = new File("C:\\Wascana\\repo").toURI().toURL();
		
		IMetadataRepositoryManager metaRepoMgr = Activator.getDefault().getService(IMetadataRepositoryManager.class);
		IArtifactRepositoryManager artiRepoMgr = Activator.getDefault().getService(IArtifactRepositoryManager.class);
		
		metaRepo = metaRepoMgr.createRepository(repoLocation, REPO_NAME, IMetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
		artiRepo = artiRepoMgr.createRepository(repoLocation, REPO_NAME, IArtifactRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
		
		Version wascanaVersion = new Version("1.0.0");
		String mingwSubdir = "mingw";
		
		// MinGW Runtime
		String runtimeId = "wascana.mingw.mingwrt";
		Version runtimeVersion = new Version("4.15.1");
		InstallableUnitDescription runtimeIUDesc = createIUDesc(runtimeId, runtimeVersion, "MinGW Runtime");
		IInstallableUnit runtimeIU = createIU(runtimeIUDesc, runtimeId,	runtimeVersion,
				"http://downloads.sourceforge.net/mingw/mingwrt-3.15.1-mingw32.tar.gz",
				mingwSubdir,
				InstallArtifactRepository.GZIP_COMPRESSON);

		// w32api
		String w32apiId = "wascana.mingw.w32api";
		Version w32apiVersion = new Version("3.12");
		InstallableUnitDescription w32apiIUDesc = createIUDesc(w32apiId, w32apiVersion, "MinGW Windows SDK");
		IInstallableUnit w32apiIU = createIU(w32apiIUDesc, w32apiId, w32apiVersion,
				"http://downloads.sourceforge.net/mingw/w32api-3.12-mingw32-dev.tar.gz",
				mingwSubdir,
				InstallArtifactRepository.GZIP_COMPRESSON);

		// binutils
		String binutilsId = "wascana.mingw.binutils";
		Version binutilsVersion = new Version("2.18.50.20080109-2");
		InstallableUnitDescription binutilsIUDesc = createIUDesc(binutilsId, binutilsVersion, "MinGW binutils"); 
		IInstallableUnit binutilsIU = createIU(binutilsIUDesc, binutilsId, binutilsVersion,
				"http://downloads.sourceforge.net/mingw/binutils-2.18.50-20080109-2.tar.gz",
				mingwSubdir,
				InstallArtifactRepository.GZIP_COMPRESSON);
		
		// gcc-4 core
		String gcc4coreId = "wascana.mingw.gcc4.core";
		Version gcc4Version = new Version("4.3.2.tdm-1");
		InstallableUnitDescription gcc4coreIUDesc = createIUDesc(gcc4coreId, gcc4Version, "MinGW gcc-4 core");
		RequiredCapability[] gcc4coreReqs = new RequiredCapability[] {
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						runtimeIU.getId(), new VersionRange(null), null, false, false),
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						w32apiIU.getId(), new VersionRange(null), null, false, false),
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						binutilsIU.getId(), new VersionRange(null), null, false, false),
		};
		gcc4coreIUDesc.setRequiredCapabilities(gcc4coreReqs);
		IInstallableUnit gcc4coreIU = createIU(gcc4coreIUDesc, gcc4coreId, gcc4Version,
				"http://downloads.sourceforge.net/tdm-gcc/gcc-4.3.2-tdm-1-core.tar.gz",
				mingwSubdir,
				InstallArtifactRepository.GZIP_COMPRESSON);

		// gcc-4 g++
		String gcc4gppId = "wascana.mingw.gcc4.g++";
		InstallableUnitDescription gcc4gppIUDesc = createIUDesc(gcc4gppId, gcc4Version, "MinGW gcc-4 g++");
		RequiredCapability[] gcc4gppReqs = new RequiredCapability[] {
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						gcc4coreIU.getId(), new VersionRange(gcc4Version, true, gcc4Version, true), null, false, false),
		};
		gcc4gppIUDesc.setRequiredCapabilities(gcc4gppReqs);
		IInstallableUnit gcc4gppIU = createIU(gcc4gppIUDesc, gcc4gppId, gcc4Version,
				"http://downloads.sourceforge.net/tdm-gcc/gcc-4.3.2-tdm-1-g++.tar.gz",
				mingwSubdir,
				InstallArtifactRepository.GZIP_COMPRESSON);
		
		// gdb
		String gdbId = "wascana.mingw.gdb";
		Version gdbVersion = new Version("6.8.0.3");
		InstallableUnitDescription gdbIUDesc = createIUDesc(gdbId, gdbVersion, "MinGW gdb");
		IInstallableUnit gdbIU = createIU(gdbIUDesc, gdbId, gdbVersion,
				"http://downloads.sourceforge.net/mingw/gdb-6.8-mingw-3.tar.bz2",
				mingwSubdir,
				InstallArtifactRepository.BZIP2_COMPRESSION);
		
		// MinGW toolchain category
		InstallableUnitDescription mingwToolchainDesc = createIUDesc("wascana.mingw", wascanaVersion, "MinGW Toolchain");
		mingwToolchainDesc.setProperty(IInstallableUnit.PROP_TYPE_CATEGORY, Boolean.TRUE.toString());
		RequiredCapability[] mingwToolchainReqs = new RequiredCapability[] {
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						runtimeIU.getId(), new VersionRange(null), null, false, false),
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						w32apiIU.getId(), new VersionRange(null), null, false, false),
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						binutilsIU.getId(), new VersionRange(null), null, false, false),
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						gcc4coreIU.getId(), new VersionRange(null), null, false, false),
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						gcc4gppIU.getId(), new VersionRange(null), null, false, false),
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						gdbIU.getId(), new VersionRange(null), null, false, false),
		};
		mingwToolchainDesc.setRequiredCapabilities(mingwToolchainReqs);
		IInstallableUnit mingwToolchainIU = MetadataFactory.createInstallableUnit(mingwToolchainDesc);
		
		metaRepo.addInstallableUnits(new IInstallableUnit[] {
				runtimeIU,
				w32apiIU,
				binutilsIU,
				gcc4coreIU,
				gcc4gppIU,
				gdbIU,
				mingwToolchainIU,
			});

		System.out.println("done");
		
		return EXIT_OK;
	}

	@Override
	public void stop() {
	}

	private InstallableUnitDescription createIUDesc(String id, Version version, String name) throws ProvisionException {
		InstallableUnitDescription iuDesc = new MetadataFactory.InstallableUnitDescription();
		iuDesc.setId(id);
		iuDesc.setVersion(version);
		iuDesc.setSingleton(true);
		iuDesc.setProperty(IInstallableUnit.PROP_NAME, name);
		iuDesc.setCapabilities(new ProvidedCapability[] {
				MetadataFactory.createProvidedCapability(IInstallableUnit.NAMESPACE_IU_ID, id, version)
			});
		return iuDesc;
	}

	private IInstallableUnit createIU(InstallableUnitDescription iuDesc, String id, Version version, String location, String subdir, String compression) throws ProvisionException {
		iuDesc.setProperty(IInstallableUnit.PROP_TYPE_GROUP, Boolean.TRUE.toString());
		iuDesc.setTouchpointType(SDKTouchpoint.TOUCHPOINT_TYPE);
		Map<String, String> tpdata = new HashMap<String, String>();
		tpdata.put("uninstall", "uninstall()");
		iuDesc.addTouchpointData(MetadataFactory.createTouchpointData(tpdata));
		IArtifactKey artiKey = MetadataGeneratorHelper.createLauncherArtifactKey(id, version);
		ArtifactDescriptor artiDesc = new ArtifactDescriptor(artiKey);
		artiDesc.setProperty(InstallArtifactRepository.SUB_DIR, subdir);
		artiDesc.setProperty(InstallArtifactRepository.COMPRESSION, compression);
		artiDesc.setRepositoryProperty("artifact.reference", location);
		artiRepo.addDescriptor(artiDesc);
		iuDesc.setArtifacts(new IArtifactKey[] { artiKey });
		return MetadataFactory.createInstallableUnit(iuDesc);
	}

}
