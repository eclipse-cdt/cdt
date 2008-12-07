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
import org.eclipse.equinox.internal.provisional.p2.metadata.IUpdateDescriptor;
import org.eclipse.equinox.internal.provisional.p2.metadata.License;
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
		
		License publicDomainLic = MetadataFactory.createLicense(null, publicDomain);
		License gplLic = MetadataFactory.createLicense(gplURL, gpl);
		License lgplLic = MetadataFactory.createLicense(lgplURL, lgpl);
		
		Version wascanaVersion = new Version("1.0.0");
		String mingwSubdir = "mingw";
		
		// MinGW Runtime
		String runtimeId = "wascana.mingw.mingwrt";
		Version runtimeVersion = new Version("4.15.1");
		InstallableUnitDescription runtimeIUDesc = createIUDesc(runtimeId, runtimeVersion, "MinGW Runtime Library", publicDomainLic);
		IInstallableUnit runtimeIU = createIU(runtimeIUDesc, runtimeId,	runtimeVersion,
				"http://downloads.sourceforge.net/mingw/mingwrt-3.15.1-mingw32.tar.gz",
				mingwSubdir,
				InstallArtifactRepository.GZIP_COMPRESSON);

		// w32api
		String w32apiId = "wascana.mingw.w32api";
		Version w32apiVersion = new Version("3.13");
		InstallableUnitDescription w32apiIUDesc = createIUDesc(w32apiId, w32apiVersion, "MinGW Windows Library", publicDomainLic);
		IInstallableUnit w32apiIU = createIU(w32apiIUDesc, w32apiId, w32apiVersion,
				"http://downloads.sourceforge.net/mingw/w32api-3.13-mingw32-dev.tar.gz",
				mingwSubdir,
				InstallArtifactRepository.GZIP_COMPRESSON);

		// binutils
		String binutilsId = "wascana.mingw.binutils";
		Version binutilsVersion = new Version("2.18.50.20080109-2");
		InstallableUnitDescription binutilsIUDesc = createIUDesc(binutilsId, binutilsVersion, "MinGW binutils", gplLic);
		IInstallableUnit binutilsIU = createIU(binutilsIUDesc, binutilsId, binutilsVersion,
				"http://downloads.sourceforge.net/mingw/binutils-2.18.50-20080109-2.tar.gz",
				mingwSubdir,
				InstallArtifactRepository.GZIP_COMPRESSON);
		
		// gcc-4 core
		String gcc4coreId = "wascana.mingw.gcc4.core";
		Version gcc4Version = new Version("4.3.2.tdm-1");
		InstallableUnitDescription gcc4coreIUDesc = createIUDesc(gcc4coreId, gcc4Version, "MinGW gcc-4 core", gplLic);
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
		InstallableUnitDescription gcc4gppIUDesc = createIUDesc(gcc4gppId, gcc4Version, "MinGW gcc-4 g++", gplLic);
		gcc4gppIUDesc.setLicense(gplLic);
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
		Version gdbVersion = new Version("6.8.0.4");
		InstallableUnitDescription gdbIUDesc = createIUDesc(gdbId, gdbVersion, "MinGW gdb", gplLic);
		IInstallableUnit gdbIU = createIU(gdbIUDesc, gdbId, gdbVersion,
				"http://downloads.sourceforge.net/mingw/gdb-6.8-mingw-3.tar.bz2",
				mingwSubdir,
				InstallArtifactRepository.BZIP2_COMPRESSION);
		
		// msys
		String msysId = "wascana.msys.core";
		Version msysVersion = new Version("1.0.11.20080826");
		InstallableUnitDescription msysIUDesc = createIUDesc(msysId, msysVersion, "MSYS Build System", gplLic);
		IInstallableUnit msysIU = createIU(msysIUDesc, msysId, msysVersion,
				"http://downloads.sourceforge.net/mingw/msysCORE-1.0.11-20080826.tar.gz",
				"msys",
				InstallArtifactRepository.GZIP_COMPRESSON);
		
		// MinGW toolchain category
		InstallableUnitDescription mingwToolchainDesc = createIUDesc("wascana.mingw", wascanaVersion, "MinGW Toolchain", null);;
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
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						msysIU.getId(), new VersionRange(null), null, false, false),
		};
		mingwToolchainDesc.setRequiredCapabilities(mingwToolchainReqs);
		IInstallableUnit mingwToolchainIU = MetadataFactory.createInstallableUnit(mingwToolchainDesc);
		
		// SDL
		String sdlId = "wascana.sdl";
		Version sdlVersion = new Version("1.2.13");
		InstallableUnitDescription sdlIUDesc = createIUDesc(sdlId, sdlVersion, "SDL (Simple Directmedia Layer) Library", lgplLic);
		IInstallableUnit sdlIU = createIU(sdlIUDesc, sdlId, sdlVersion,
				"http://www.libsdl.org/release/SDL-devel-1.2.13-mingw32.tar.gz",
				mingwSubdir,
				InstallArtifactRepository.GZIP_COMPRESSON);
		
		// Libraries toolchain category
		InstallableUnitDescription libsIUDesc = createIUDesc("wascana.libs", wascanaVersion, "Libraries", null);;
		libsIUDesc.setProperty(IInstallableUnit.PROP_TYPE_CATEGORY, Boolean.TRUE.toString());
		RequiredCapability[] libsReqs = new RequiredCapability[] {
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						sdlIU.getId(), new VersionRange(null), null, false, false),
		};
		libsIUDesc.setRequiredCapabilities(libsReqs);
		IInstallableUnit libsIU = MetadataFactory.createInstallableUnit(libsIUDesc);

		metaRepo.addInstallableUnits(new IInstallableUnit[] {
				runtimeIU,
				w32apiIU,
				binutilsIU,
				gcc4coreIU,
				gcc4gppIU,
				gdbIU,
				msysIU,
				mingwToolchainIU,
				
//				sdlIU,
//				libsIU
			});

		System.out.println("done");
		
		return EXIT_OK;
	}

	@Override
	public void stop() {
	}

	private InstallableUnitDescription createIUDesc(String id, Version version, String name, License license) throws ProvisionException {
		InstallableUnitDescription iuDesc = new MetadataFactory.InstallableUnitDescription();
		iuDesc.setId(id);
		iuDesc.setVersion(version);
		iuDesc.setLicense(license);
		iuDesc.setSingleton(true);
		iuDesc.setProperty(IInstallableUnit.PROP_NAME, name);
		iuDesc.setCapabilities(new ProvidedCapability[] {
				MetadataFactory.createProvidedCapability(IInstallableUnit.NAMESPACE_IU_ID, id, version)
			});
		iuDesc.setUpdateDescriptor(MetadataFactory.createUpdateDescriptor(id, new VersionRange(null), IUpdateDescriptor.NORMAL, ""));
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

	public static final String publicDomain = "This package is placed in the Public Domain."
		+ " No warranty is given; refer to the header files within the package.";

	public static final String gplURL = "http://www.gnu.org/copyleft/gpl.html";
	
	public static final String gpl = "GNU GENERAL PUBLIC LICENSE\n" + gplURL;
	
	public static final String lgplURL = "http://www.gnu.org/copyleft/lesser.html";
	
	public static final String lgpl = "GNU LESSER GENERAL PUBLIC LICENSE\n" + lgplURL;
	
}
