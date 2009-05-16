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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.ArtifactDescriptor;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.core.Version;
import org.eclipse.equinox.internal.provisional.p2.core.VersionRange;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.ILicense;
import org.eclipse.equinox.internal.provisional.p2.metadata.IProvidedCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.ITouchpointType;
import org.eclipse.equinox.internal.provisional.p2.metadata.IUpdateDescriptor;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.repository.IRepository;
import org.eclipse.equinox.spi.p2.publisher.PublisherHelper;
import org.osgi.framework.Bundle;

/**
 * @author DSchaefe
 *
 */
public class MinGWGenerator implements IApplication {

	private static final String REPO_NAME = "Wascana";
	
	private static final ITouchpointType NATIVE_TOUCHPOINT
		= MetadataFactory.createTouchpointType("org.eclipse.equinox.p2.native", new Version("1.0.0"));
	private static final String GZ_COMPRESSION = "gz";
	private static final String BZ2_COMPRESSION = "bz2";
	private static final String ZIP_COMPRESSION = "zip";
	
	IMetadataRepository metaRepo;
	IArtifactRepository artiRepo;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		context.applicationRunning();

		Activator.getDefault().getBundle("org.eclipse.equinox.p2.exemplarysetup").start(Bundle.START_TRANSIENT); //$NON-NLS-1$

		String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (args.length < 1) {
			System.err.println("usage: <repoDir>");
			return EXIT_OK;
		}
		
		File repoDir = new File(args[0]);
		repoDir.mkdirs();
		
		new File(repoDir, "artifacts.xml").delete();
		new File(repoDir, "content.xml").delete();
		
		URI repoLocation = repoDir.toURI();
		
		IMetadataRepositoryManager metaRepoMgr = Activator.getDefault().getService(IMetadataRepositoryManager.class);
		IArtifactRepositoryManager artiRepoMgr = Activator.getDefault().getService(IArtifactRepositoryManager.class);
		
		metaRepo = metaRepoMgr.createRepository(repoLocation, REPO_NAME, IMetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
		metaRepo.setProperty(IRepository.PROP_COMPRESSED, Boolean.TRUE.toString());
		
		artiRepo = artiRepoMgr.createRepository(repoLocation, REPO_NAME, IArtifactRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
		artiRepo.setProperty(IRepository.PROP_COMPRESSED, Boolean.TRUE.toString());
		
		ILicense publicDomainLic = MetadataFactory.createLicense(null, publicDomain);
		ILicense gplLic = MetadataFactory.createLicense(new URI(gplURL), gpl);
		ILicense lgplLic = MetadataFactory.createLicense(new URI(lgplURL), lgpl);
		ILicense zlibLic = MetadataFactory.createLicense(new URI(zlibLicURL), zlibLicText);
		ILicense wxLic = MetadataFactory.createLicense(new URI(wxLicURL), wxLicText);
		
		Version wascanaVersion = new Version("1.0.0");
		
		String mingwSubdir = "mingw";
		
		boolean toolGroups = true;
		
		// MinGW Runtime DLL
		Version runtimeVersion = new Version("4.15.2");
		IInstallableUnit runtimeDLLIU = createIU(
				"wascana.mingw.mingwrt.dll",
				runtimeVersion,
				"Wascana MinGW Runtime DLL",
				"http://downloads.sourceforge.net/mingw/mingwrt-3.15.2-mingw32-dll.tar.gz",
				mingwSubdir,
				GZ_COMPRESSION,
				null,
				publicDomainLic,
				toolGroups);
		
		// MinGW Runtime Library
		IInstallableUnit runtimeLibIU = createIU(
				"wascana.mingw.mingwrt.lib",
				runtimeVersion,
				"Wascana MinGW Runtime Library",
				"http://downloads.sourceforge.net/mingw/mingwrt-3.15.2-mingw32-dev.tar.gz",
				mingwSubdir,
				GZ_COMPRESSION,
				new IRequiredCapability[] {
					createStrictRequiredCap(runtimeDLLIU)
				},
				publicDomainLic,
				toolGroups);

		// w32api
		IInstallableUnit w32apiIU = createIU(
				"wascana.mingw.w32api",
				new Version("3.13"),
				"Wascana MinGW Windows Library",
				"http://downloads.sourceforge.net/mingw/w32api-3.13-mingw32-dev.tar.gz",
				mingwSubdir,
				GZ_COMPRESSION,
				null,
				publicDomainLic,
				toolGroups);

		// binutils
		IInstallableUnit binutilsIU = createIU(
				"wascana.mingw.binutils",
				new Version("2.19.1"),
				"Wascana MinGW binutils",
				"http://downloads.sourceforge.net/mingw/binutils-2.19.1-mingw32-bin.tar.gz",
				mingwSubdir,
				GZ_COMPRESSION,
				null,
				gplLic,
				toolGroups);
		
		// gcc-4 core
		Version gcc4version = new Version("4.3.3.tdm-1");
		IInstallableUnit gcc4coreIU = createIU(
				"wascana.mingw.tdm.gcc4.core", 
				gcc4version,
				"Wascana MinGW TDM gcc-4 core",
				"http://downloads.sourceforge.net/tdm-gcc/gcc-4.3.3-tdm-1-core.tar.gz",
				mingwSubdir,
				GZ_COMPRESSION,
				new IRequiredCapability[] {
						createRequiredCap(runtimeDLLIU),
						createRequiredCap(runtimeLibIU),
						createRequiredCap(w32apiIU),
						createRequiredCap(binutilsIU)
				},
				gplLic,
				toolGroups);

		// gcc-4 g++
		IInstallableUnit gcc4gppIU = createIU(
				"wascana.mingw.tdm.gcc4.g++",
				gcc4version,
				"Wascana MinGW TDM gcc-4 g++",
				"http://downloads.sourceforge.net/tdm-gcc/gcc-4.3.3-tdm-1-g++.tar.gz",
				mingwSubdir,
				GZ_COMPRESSION,
				new IRequiredCapability[] {
						createStrictRequiredCap(gcc4coreIU)
				},
				gplLic,
				toolGroups);
		
		// gdb
		IInstallableUnit gdbIU = createIU(
				"wascana.mingw.gdb",
				new Version("6.8.0.3"),
				"Wascana MinGW gdb",
				"http://downloads.sourceforge.net/mingw/gdb-6.8-mingw-3.tar.bz2",
				mingwSubdir,
				BZ2_COMPRESSION,
				null,
				gplLic,
				toolGroups);
		
		InstallableUnitDescription toolchainIUDesc = createIUDesc(
				"wascana.toolchain",
				new Version("4.3.3"), // Same as gcc
				"Wascana Toolchain (gcc, gdb, runtime libs)",
				null);
		toolchainIUDesc.setProperty(IInstallableUnit.PROP_TYPE_GROUP, Boolean.TRUE.toString());
		toolchainIUDesc.setRequiredCapabilities(new IRequiredCapability[] {
				createRequiredCap(runtimeDLLIU),
				createRequiredCap(runtimeLibIU),
				createRequiredCap(w32apiIU),
				createRequiredCap(binutilsIU),
				createRequiredCap(gcc4coreIU),
				createRequiredCap(gcc4gppIU),
				createRequiredCap(gdbIU),
		});
		IInstallableUnit toolchainIU = MetadataFactory.createInstallableUnit(toolchainIUDesc);
		
		// msys
		IInstallableUnit msysIU = createIU(
				"wascana.msys.core",
				new Version("1.0.11.20080826"),
				"Wascana Shell (MSYS)",
				"http://downloads.sourceforge.net/mingw/msysCORE-1.0.11-20080826.tar.gz",
				"msys",
				GZ_COMPRESSION,
				null,
				gplLic,
				true);
		
		// zlib
		IInstallableUnit zlibIU = createIU(
				"wascana.zlib",
				new Version("1.2.3"),
				"Wascana zlib Library",
				"http://downloads.sourceforge.net/wascana/zlib-mingw-1.2.3.zip",
				mingwSubdir,
				ZIP_COMPRESSION,
				null,
				zlibLic,
				true);
		
		// SDL
		IInstallableUnit sdlIU = createIU(
				"wascana.sdl",
				new Version("1.2.13"),
				"Wascana SDL (Simple Directmedia Layer) Library",
				"http://downloads.sourceforge.net/wascana/SDL-mingw-1.2.13.zip",
				mingwSubdir,
				ZIP_COMPRESSION,
				null,
				lgplLic,
				true);

		// wxWidgets
		IInstallableUnit wxIU = createIU(
				"wascana.wxWidgets", 
				new Version("2.8.9"),
				"Wascana wxWidgets Library",
				"http://downloads.sourceforge.net/wascana/wxMSW-mingw-2.8.9.zip",
				mingwSubdir,
				ZIP_COMPRESSION,
				null,
				wxLic,
				true);

		InstallableUnitDescription libsIUDesc = createIUDesc(
				"wascana.libraries",
				wascanaVersion,
				"Wascana Libraries",
				null);
		libsIUDesc.setProperty(IInstallableUnit.PROP_TYPE_CATEGORY, Boolean.TRUE.toString());
		libsIUDesc.setRequiredCapabilities(new IRequiredCapability[] {
				createRequiredCap(zlibIU),
				createRequiredCap(sdlIU),
				createRequiredCap(wxIU),
		});
		IInstallableUnit libsIU = MetadataFactory.createInstallableUnit(libsIUDesc);

		// Libraries toolchain category
		InstallableUnitDescription wascanaIUDesc = createIUDesc("wascana", wascanaVersion, "Wascana Desktop Developer", null);;
		wascanaIUDesc.setProperty(IInstallableUnit.PROP_TYPE_CATEGORY, Boolean.TRUE.toString());
		wascanaIUDesc.setRequiredCapabilities(new IRequiredCapability[] {
				createRequiredCap(toolchainIU),
				createRequiredCap(msysIU),
				createRequiredCap(libsIU),
		});
		IInstallableUnit wascanaIU = MetadataFactory.createInstallableUnit(wascanaIUDesc);

		metaRepo.addInstallableUnits(new IInstallableUnit[] {
				runtimeDLLIU,
				runtimeLibIU,
				w32apiIU,
				binutilsIU,
				gcc4coreIU,
				gcc4gppIU,
				gdbIU,
				msysIU,
				
				toolchainIU,

				wxIU,
				zlibIU,
				sdlIU,
				
				libsIU,
				
				wascanaIU
			});

		System.out.println("done");
		
		return EXIT_OK;
	}

	@Override
	public void stop() {
	}

	private InstallableUnitDescription createIUDesc(String id, Version version, String name, ILicense license) throws ProvisionException {
		InstallableUnitDescription iuDesc = new MetadataFactory.InstallableUnitDescription();
		iuDesc.setId(id);
		iuDesc.setVersion(version);
		iuDesc.setLicense(license);
		iuDesc.setSingleton(true);
		iuDesc.setProperty(IInstallableUnit.PROP_NAME, name);
		iuDesc.setCapabilities(new IProvidedCapability[] {
				MetadataFactory.createProvidedCapability(IInstallableUnit.NAMESPACE_IU_ID, id, version)
			});
		iuDesc.setUpdateDescriptor(MetadataFactory.createUpdateDescriptor(id, new VersionRange(null), IUpdateDescriptor.NORMAL, ""));
		return iuDesc;
	}

	private IInstallableUnit createIU(String id, Version version, String name, String location, String subdir, String compression,
			IRequiredCapability[] reqs, ILicense license, boolean group) throws ProvisionException {
		InstallableUnitDescription iuDesc = createIUDesc(id, version, name, license);
		if (reqs != null)
			iuDesc.setRequiredCapabilities(reqs);

		iuDesc.setProperty(IInstallableUnit.PROP_TYPE_GROUP, String.valueOf(group));
		iuDesc.setTouchpointType(NATIVE_TOUCHPOINT);
		Map<String, String> tpdata = new HashMap<String, String>();
		
		String cmd, uncmd;
		if (compression.equals(ZIP_COMPRESSION)) {
			cmd = "unzip(source:@artifact, target:${installFolder}/" + subdir + ");";
			uncmd = "cleanupzip(source:@artifact, target:${installFolder}/" + subdir + ");";
		} else {
			cmd = "untar(source:@artifact, target:${installFolder}/" + subdir
				+ ", compression:" + compression + ");";
			uncmd = "cleanup" + cmd;
		}
		
		tpdata.put("install", cmd);
		tpdata.put("uninstall", uncmd);
		
		iuDesc.addTouchpointData(MetadataFactory.createTouchpointData(tpdata));
		IArtifactKey artiKey = PublisherHelper.createBinaryArtifactKey(id, version);
		ArtifactDescriptor artiDesc = new ArtifactDescriptor(artiKey);
		artiDesc.setRepositoryProperty("artifact.reference", location);
		artiRepo.addDescriptor(artiDesc);
		iuDesc.setArtifacts(new IArtifactKey[] { artiKey });
		return MetadataFactory.createInstallableUnit(iuDesc);
	}

	private IRequiredCapability createRequiredCap(IInstallableUnit iu) {
		return MetadataFactory.createRequiredCapability(
				IInstallableUnit.NAMESPACE_IU_ID,
				iu.getId(), new VersionRange(null), null, false, false);
	}
	
	private IRequiredCapability createStrictRequiredCap(IInstallableUnit iu) {
		return MetadataFactory.createRequiredCapability(
				IInstallableUnit.NAMESPACE_IU_ID,
				iu.getId(), new VersionRange(iu.getVersion(), true, iu.getVersion(), true), null, false, false);
	}
	// TODO make these more legal...
	
	public static final String publicDomain = "This package is placed in the Public Domain."
		+ " No warranty is given; refer to the header files within the package.";

	public static final String gplURL = "http://www.gnu.org/copyleft/gpl.html";
	
	public static final String gpl = "GNU GENERAL PUBLIC LICENSE\n" + gplURL;
	
	public static final String lgplURL = "http://www.gnu.org/copyleft/lesser.html";
	
	public static final String lgpl = "GNU LESSER GENERAL PUBLIC LICENSE\n" + lgplURL;
	
	public static final String zlibLicURL = "http://www.zlib.net/zlib_license.html";
	
	public static final String zlibLicText = "http://www.zlib.net/zlib_license.html";
	
	public static final String wxLicURL = "http://www.wxwidgets.org/about/newlicen.htm";
	
	public static final String wxLicText = "wxWindows license\n" + wxLicURL;
	
}
