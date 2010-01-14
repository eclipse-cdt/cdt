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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository;
import org.eclipse.equinox.internal.p2.metadata.ArtifactKey;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.ArtifactDescriptor;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.ILicense;
import org.eclipse.equinox.internal.provisional.p2.metadata.IProvidedCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.ITouchpointType;
import org.eclipse.equinox.internal.provisional.p2.metadata.IUpdateDescriptor;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory;
import org.eclipse.equinox.internal.provisional.p2.metadata.Version;
import org.eclipse.equinox.internal.provisional.p2.metadata.VersionRange;
import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.repository.IRepository;

/**
 * @author DSchaefe
 *
 */
public class WascanaGenerator implements IApplication {

	private static Version wascanaVersion = Version.parseVersion("1.0.0.0");
	private static Version binutilsVersion = Version.parseVersion("2.20.0.0");

	private static final String REPO_NAME = "Wascana";
	
	private static final ITouchpointType NATIVE_TOUCHPOINT
		= MetadataFactory.createTouchpointType("org.eclipse.equinox.p2.native", Version.parseVersion("1.0.0"));
	
	private IMetadataRepository metaRepo;
	private IArtifactRepository artiRepo;
	
	private ILicense gpl30License;
	private ILicense lgpl21License;
	
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

		// binutils
		IInstallableUnit binutilsIU = createIU(
				"wascana.binutils",
				"Wascana MinGW binutils",
				binutilsVersion,
				gpl30License,
				null);
		
		// toolchain
		InstallableUnitDescription toolsIUDesc = createIUDesc(
				"wascana.tools",
				"Wascana Tools",
				wascanaVersion,
				null);
		toolsIUDesc.setProperty(IInstallableUnit.PROP_TYPE_CATEGORY, Boolean.TRUE.toString());
		toolsIUDesc.setRequiredCapabilities(new IRequiredCapability[] {
				createRequiredCap(binutilsIU),
		});
		IInstallableUnit toolsIU = MetadataFactory.createInstallableUnit(toolsIUDesc);
		
		InstallableUnitDescription sdksIUDesc = createIUDesc(
				"wascana.sdks",
				"Wascana SDKs",
				wascanaVersion,
				null);
		sdksIUDesc.setProperty(IInstallableUnit.PROP_TYPE_CATEGORY, Boolean.TRUE.toString());
		sdksIUDesc.setRequiredCapabilities(new IRequiredCapability[] {
		});
		IInstallableUnit sdksIU = MetadataFactory.createInstallableUnit(sdksIUDesc);

		// The main Wascana category
		InstallableUnitDescription wascanaIUDesc = createIUDesc(
				"wascana",
				"Wascana Desktop Developer",
				wascanaVersion,
				null);
		wascanaIUDesc.setProperty(IInstallableUnit.PROP_TYPE_CATEGORY, Boolean.TRUE.toString());
		wascanaIUDesc.setRequiredCapabilities(new IRequiredCapability[] {
				createRequiredCap(toolsIU),
				createRequiredCap(sdksIU),
		});
		IInstallableUnit wascanaIU = MetadataFactory.createInstallableUnit(wascanaIUDesc);

		metaRepo.addInstallableUnits(new IInstallableUnit[] {
				binutilsIU,
				toolsIU,
				sdksIU,
				wascanaIU
			});

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

		IMetadataRepositoryManager metaRepoMgr = Activator.getService(IMetadataRepositoryManager.class);
		IArtifactRepositoryManager artiRepoMgr = Activator.getService(IArtifactRepositoryManager.class);
		
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
	}
	
	private InstallableUnitDescription createIUDesc(String id, String name, Version version, ILicense license) throws ProvisionException {
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

	private IInstallableUnit createIU(String id, String name, Version version, ILicense license,
			IRequiredCapability[] reqs) throws ProvisionException {
		InstallableUnitDescription iuDesc = createIUDesc(id, name, version, license);
		if (reqs != null)
			iuDesc.setRequiredCapabilities(reqs);

		iuDesc.setProperty(IInstallableUnit.PROP_TYPE_GROUP, String.valueOf(true));
		iuDesc.setTouchpointType(NATIVE_TOUCHPOINT);
		Map<String, String> tpdata = new HashMap<String, String>();
		
		tpdata.put("install", "untar(source:@artifact, target:${installFolder}, compression: bz2)");
		tpdata.put("uninstall", "cleanupuntar(source:@artifact, target:${installFolder})");
		
		iuDesc.addTouchpointData(MetadataFactory.createTouchpointData(tpdata));
		IArtifactKey artiKey = new ArtifactKey("package", id, version);
		ArtifactDescriptor artiDesc = new ArtifactDescriptor(artiKey);
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
	
}
