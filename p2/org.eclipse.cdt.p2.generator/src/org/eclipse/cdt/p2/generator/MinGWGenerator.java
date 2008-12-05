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

	private static final String REPO_NAME = "MinGW";
	
	IMetadataRepository metaRepo;
	IArtifactRepository artiRepo;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		context.applicationRunning();

		Activator.getDefault().getBundle("org.eclipse.equinox.p2.exemplarysetup").start(Bundle.START_TRANSIENT); //$NON-NLS-1$

		URL repoLocation = new File("C:\\Wascana\\repo").toURI().toURL();

		IMetadataRepositoryManager metaRepoMgr = Activator.getDefault().getService(IMetadataRepositoryManager.class);
		IArtifactRepositoryManager artiRepoMgr = Activator.getDefault().getService(IArtifactRepositoryManager.class);
		
		metaRepo = metaRepoMgr.createRepository(repoLocation, REPO_NAME, IMetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
		artiRepo = artiRepoMgr.createRepository(repoLocation, REPO_NAME, IArtifactRepositoryManager.TYPE_SIMPLE_REPOSITORY, null);
		
		IInstallableUnit binutilsIU = createBinutils();
		
		// toolchain
		InstallableUnitDescription mingwToolchainDesc = createIUDesc(
				"wascana.mingw",
				new Version(1, 0, 0), 
				"MinGW Toolchain");
		RequiredCapability[] mingwToolchainReqs = new RequiredCapability[] {
				MetadataFactory.createRequiredCapability(
						IInstallableUnit.NAMESPACE_IU_ID,
						binutilsIU.getId(), new VersionRange(null), null, false, false)
		};
		mingwToolchainDesc.setRequiredCapabilities(mingwToolchainReqs);
		mingwToolchainDesc.setProperty(IInstallableUnit.PROP_TYPE_CATEGORY, Boolean.TRUE.toString());
		IInstallableUnit mingwToolchainIU = MetadataFactory.createInstallableUnit(mingwToolchainDesc);
		
		metaRepo.addInstallableUnits(new IInstallableUnit[] {
				binutilsIU,
//				gccCoreIU,
//				gccGppIU,
				mingwToolchainIU,
//				wascanaIU
			});

		System.out.println("done");
		
		return EXIT_OK;
	}

	@Override
	public void stop() {
	}

	private IInstallableUnit createBinutils() throws ProvisionException, MalformedURLException {
		String binutilsId = "wascana.mingw.binutils";
		Version binutilsVersion = new Version("2.18.50.20080109-2");
		InstallableUnitDescription binutilsDesc = createIUDesc(
				binutilsId, binutilsVersion, "MinGW binutils");
		binutilsDesc.setProperty(IInstallableUnit.PROP_TYPE_GROUP, Boolean.TRUE.toString());
		IArtifactKey binutilsArti = addRemoteArtifact(
				binutilsDesc,
				binutilsId,
				binutilsVersion,
				new File("C:\\Wascana\\tars\\binutils-2.18.50-20080109-2.tar.gz").toURI().toURL(),
				"mingw",
				InstallArtifactRepository.GZIP_COMPRESSON);
		binutilsDesc.setArtifacts(new IArtifactKey[] { binutilsArti });
		return MetadataFactory.createInstallableUnit(binutilsDesc);
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

	private IArtifactKey addRemoteArtifact(
			InstallableUnitDescription iuDesc, 
			String id, Version version, URL location, String subdir, String compression) throws ProvisionException {
		iuDesc.setTouchpointType(SDKTouchpoint.TOUCHPOINT_TYPE);
		Map<String, String> tpdata = new HashMap<String, String>();
		tpdata.put("uninstall", "uninstall()");
		iuDesc.addTouchpointData(MetadataFactory.createTouchpointData(tpdata));
		IArtifactKey artiKey = MetadataGeneratorHelper.createLauncherArtifactKey(id, version);
		ArtifactDescriptor artiDesc = new ArtifactDescriptor(artiKey);
		artiDesc.setProperty(InstallArtifactRepository.SUB_DIR, subdir);
		artiDesc.setProperty(InstallArtifactRepository.COMPRESSION, compression);
		artiDesc.setRepositoryProperty("artifact.reference", location.toString());
		artiRepo.addDescriptor(artiDesc);
		return artiKey;
	}
	
}
