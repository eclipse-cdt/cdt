package org.eclipse.cdt.releng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.plugin.IFragment;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureImport;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.isite.ISite;
import org.eclipse.pde.internal.core.isite.ISiteBuild;
import org.eclipse.pde.internal.core.isite.ISiteBuildFeature;
import org.eclipse.pde.internal.core.isite.ISiteCategory;
import org.eclipse.pde.internal.core.isite.ISiteCategoryDefinition;
import org.eclipse.pde.internal.core.isite.ISiteFeature;
import org.eclipse.pde.internal.core.isite.ISiteModelFactory;
import org.eclipse.pde.internal.core.plugin.WorkspaceFragmentModel;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;
import org.eclipse.pde.internal.core.site.WorkspaceSiteBuildModel;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;
import org.eclipse.pde.internal.ui.editor.site.FeatureBuildOperation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * @see IPlatformRunnable
 */
public class DoBuild implements IPlatformRunnable {

	private IWorkspace workspace;
	private String version;
	private IProgressMonitor monitor;
	
	private static String locstr = ":pserver:anonymous@dev.eclipse.org:/home/tools";

	private static final String ftpHost = "download.eclipse.org";
	private static final String ftpPath = "cdt/updates/builds/1.2";
	private static final String ftpUser = System.getProperty("cdt.build.user");
	private static final String ftpPassword = System.getProperty("cdt.build.passwd");
	
	private static final String[] plugins = {
		// Code
		"org.eclipse.cdt.core",
		"org.eclipse.cdt.core.tests",
		"org.eclipse.cdt.ui",
		"org.eclipse.cdt.ui.tests",
		"org.eclipse.cdt.debug.core",
		"org.eclipse.cdt.debug.ui",
		"org.eclipse.cdt.debug.ui.tests",
		"org.eclipse.cdt.debug.mi.core",
		"org.eclipse.cdt.debug.mi.ui",
		"org.eclipse.cdt.launch",
		// Docs
		"org.eclipse.cdt.doc.user",
		// Features
		"org.eclipse.cdt",
		"org.eclipse.cdt.linux.gtk",
		"org.eclipse.cdt.linux.motif",
		"org.eclipse.cdt.qnx.photon",
		"org.eclipse.cdt.solaris.motif",
		"org.eclipse.cdt.win32",
		"org.eclipse.cdt.source",
		"org.eclipse.cdt.testing"
	};
	
	private static final String[] fragments = {
		"org.eclipse.cdt.core.linux",
		"org.eclipse.cdt.core.qnx",
		"org.eclipse.cdt.core.solaris",
		"org.eclipse.cdt.core.win32",
	};
	
	private static final String[] featureProjects = {
		"org.eclipse.cdt-feature",
		"org.eclipse.cdt.linux.gtk-feature",
		"org.eclipse.cdt.linux.motif-feature",
		"org.eclipse.cdt.qnx.photon-feature",
		"org.eclipse.cdt.solaris.motif-feature",
		"org.eclipse.cdt.win32-feature",
		"org.eclipse.cdt.source-feature",
		"org.eclipse.cdt.testing-feature"
	};

	private static final String[] buildFeatures = {
		"org.eclipse.cdt",
		"org.eclipse.cdt.linux.gtk",
		"org.eclipse.cdt.linux.motif",
		"org.eclipse.cdt.qnx.photon",
		"org.eclipse.cdt.solaris.motif",
		"org.eclipse.cdt.win32",
		"org.eclipse.cdt.source",
		"org.eclipse.cdt.testing"
	};
	
	private static final boolean[] externalFeatures = {
		false,	//"org.eclipse.cdt"
		true,	//"org.eclipse.cdt.linux.gtk"
		true,	//"org.eclipse.cdt.linux.motif"
		true,	//"org.eclipse.cdt.qnx.photon"
		true,	//"org.eclipse.cdt.solaris.motif"
		true,	//"org.eclipse.cdt.win32"
		true,	//"org.eclipse.cdt.source"
		true,	//"org.eclipse.cdt.testing"

	};
	
	private static String[] projects; 

	static {
		// Create the projects array
		projects = new String[plugins.length + fragments.length + featureProjects.length];
		System.arraycopy(plugins, 0, projects, 0, plugins.length);
		int i = plugins.length;
		System.arraycopy(fragments, 0, projects, i, fragments.length);
		i += fragments.length;
		System.arraycopy(featureProjects, 0, projects, i, featureProjects.length);
	}

	/**
	 * @see IPlatformRunnable#run
	 */
	public Object run(Object args) throws Exception {
		long start = System.currentTimeMillis();
		workspace = ResourcesPlugin.getWorkspace();
		monitor = new NullProgressMonitor();

		deleteOldProjects();
		downloadNewProjects();
		downloadUpdateSite();
		updateVersions();
		buildUpdateSite();
		uploadUpdateSite();
		
		long time = System.currentTimeMillis() - start;
		long minutes = time / 60000;
		time -= minutes * 60000;
		long seconds = time / 1000;
		time -= seconds * 1000;

		System.out.println("Done: "
			+ minutes + ":" + (seconds < 10 ? "0" : "") + seconds
			+ "." + time);
		return null;
	}

	private void deleteOldProjects() throws CoreException {
		System.out.println("Deleting old projects");
		for (int i = 0; i < projects.length; ++i) {
			IProject project = workspace.getRoot().getProject(projects[i]);
			if (project.exists()) { 
				project.delete(false, monitor);
			}
		}
	}
	
	private void downloadNewProjects() throws CVSException, TeamException {
		// Download the new projects
		ICVSRepositoryLocation location = CVSRepositoryLocation.fromString(locstr);
		for (int i = 0; i < projects.length; ++i) {
			System.out.println("Downloading " + projects[i]);
			CVSWorkspaceRoot.checkout(
				location,
				null,
				projects[i],
				CVSTag.DEFAULT,
				monitor);
		}
	}
	
	private void downloadUpdateSite() throws IOException, CoreException, FTPException {
		System.out.println("Downloading update site");
		
		// Find and create the local location to download to
		IProject buildSite = workspace.getRoot().getProject("build.site");
		
		// Download from the FTP site
		FTPClient ftp = new FTPClient(ftpHost);
		ftp.setConnectMode(FTPConnectMode.ACTIVE);
		ftp.login(ftpUser, ftpPassword);
		ftp.chdir(ftpPath);
		
		IPath resultDir = Platform.getLocation().removeLastSegments(1).append("results");
		OutputStream stream = new FileOutputStream(resultDir.append("index.html").toOSString());
		ftp.get(stream, "index.html");
		stream.close();

		IFile file = buildSite.getFile("version.xml");
		stream = new FileOutputStream(file.getRawLocation().toOSString());
		ftp.get(stream, "version.xml");
		stream.close();
		
		file = buildSite.getFile("site.xml");
		stream = new FileOutputStream(file.getRawLocation().toOSString());
		ftp.get(stream, "site.xml");
		stream.close();
		
		IFolder folder = buildSite.getFolder(".sitebuild");
		file = folder.getFile("sitebuild.xml");
		stream = new FileOutputStream(file.getRawLocation().toOSString());
		ftp.chdir(".sitebuild");
		ftp.get(stream, "sitebuild.xml");
		stream.close();
		
		ftp.quit();
		buildSite.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}
	
	private void updateVersions() throws Exception {
		System.out.println("Setting versions");
	
		// Get and increment the version
		IProject siteProject = workspace.getRoot().getProject("build.site");
		IFile versionFile = siteProject.getFile("version.xml");
		versionFile.refreshLocal(IResource.DEPTH_ONE, monitor);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document versionDoc = builder.parse(versionFile.getContents());
		Element versionElem = versionDoc.getDocumentElement();
		String versionId = versionElem.getAttribute("id");
		String buildNum = versionElem.getAttribute("build");
		buildNum = String.valueOf(Integer.decode(buildNum).intValue() + 1);
		versionElem.setAttribute("build", buildNum);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		File versionResult = new File(versionFile.getRawLocation().toOSString());
		transformer.transform(new DOMSource(versionDoc), new StreamResult(versionResult));
		versionFile.refreshLocal(IResource.DEPTH_ONE, monitor);
		version = versionId + "." + buildNum;
		
		// Save the version in a text file for inclusion in the build page
		IPath resultDir = Platform.getLocation().removeLastSegments(1).append("results");
		OutputStream stream = new FileOutputStream(resultDir.append("version.txt").toOSString());
		PrintStream versionText = new PrintStream(stream);
		versionText.println(version);
		stream.close();
		
		System.out.println("Version: " + version);

		// Go through the projects and update the version info
		for (int i = 0; i < plugins.length; ++i) {
			IProject project = workspace.getRoot().getProject(plugins[i]);
			IFile pluginxml = project.getFile("plugin.xml");
			pluginxml.refreshLocal(IResource.DEPTH_ONE, monitor);
			WorkspacePluginModel pluginModel = new WorkspacePluginModel(pluginxml);
			pluginModel.load();
			IPlugin plugin = pluginModel.getPlugin();
			plugin.setVersion(version);
			pluginModel.save();
			pluginxml.refreshLocal(IResource.DEPTH_ONE, monitor);
		}

		for (int i = 0; i < fragments.length; ++i) {			
			IProject project = workspace.getRoot().getProject(fragments[i]);
			IFile fragmentxml = project.getFile("fragment.xml");
			fragmentxml.refreshLocal(IResource.DEPTH_ONE, monitor);
			WorkspaceFragmentModel fragmentModel = new WorkspaceFragmentModel(fragmentxml);
			fragmentModel.load();
			IFragment fragment = fragmentModel.getFragment();
			fragment.setVersion(version);
			fragment.setPluginVersion(version);
			fragmentModel.save();
			fragmentxml.refreshLocal(IResource.DEPTH_ONE, monitor);
		}
		
		for (int i = 0; i < featureProjects.length; ++i) {
			IProject project = workspace.getRoot().getProject(featureProjects[i]);
			IFile featurexml = project.getFile("feature.xml");
			featurexml.refreshLocal(IResource.DEPTH_ONE, monitor);
			WorkspaceFeatureModel featureModel = new WorkspaceFeatureModel(featurexml);
			featureModel.load();
			IFeature feature = featureModel.getFeature();
			feature.setVersion(version);
			IFeaturePlugin[] plugins = feature.getPlugins();
			for (int j = 0; j < plugins.length; ++j)
				if (plugins[j].getId().startsWith("org.eclipse.cdt"))
					plugins[j].setVersion(version);
			IFeatureChild[] children = feature.getIncludedFeatures();
			for (int j = 0; j < children.length; ++j)
				if (children[j].getId().startsWith("org.eclipse.cdt"))
					children[j].setVersion(version);
			IFeatureImport[] imports = feature.getImports();
			for (int j = 0; j < imports.length; ++j)
				if (imports[j].getId().startsWith("org.eclipse.cdt"))
					imports[j].setVersion(version);
			featureModel.save();
			featurexml.refreshLocal(IResource.DEPTH_ONE, monitor);
		}
	}
	
	private void buildUpdateSite() throws Exception {
		System.out.println("Seting up build site");
		
		// Get the models set up.
		IProject siteProject = workspace.getRoot().getProject("build.site");
		IFile siteFile = siteProject.getFile("site.xml");
		siteFile.refreshLocal(IResource.DEPTH_ONE, monitor);
		WorkspaceSiteModel siteModel = new WorkspaceSiteModel(siteFile);
		siteModel.load();
		ISiteModelFactory siteModelFactory = siteModel.getFactory();
		ISite site = siteModel.getSite();
		IFile siteBuildFile = siteProject.getFile(".sitebuild/sitebuild.xml");
		siteBuildFile.refreshLocal(IResource.DEPTH_ONE, monitor);
		WorkspaceSiteBuildModel buildModel = new WorkspaceSiteBuildModel(siteBuildFile);
		buildModel.load();
		ISiteBuild siteBuild = buildModel.getSiteBuild();

		// Add in the features
		ISiteCategoryDefinition categoryDef = siteModelFactory.createCategoryDefinition();
		String categoryName = "CDT Build " + version;
		categoryDef.setLabel(categoryName);
		categoryDef.setName(categoryName);
		site.addCategoryDefinitions(new ISiteCategoryDefinition[] {categoryDef});
		
		ArrayList buildList = new ArrayList();
		ArrayList externalList = new ArrayList();
		for (int i = 0; i < buildFeatures.length; ++i) {
			ISiteBuildFeature buildFeature = buildModel.createFeature();
			buildFeature.setId(buildFeatures[i]);
			buildFeature.setVersion(version);
			buildList.add(buildFeature);

			if (externalFeatures[i]) {
				ISiteFeature feature = siteModelFactory.createFeature();
				feature.setId(buildFeatures[i]);
				feature.setVersion(version);
				feature.setURL("features/" + buildFeatures[i] + "_" + version + ".jar");
				ISiteCategory category = siteModelFactory.createCategory(feature);
				category.setName(categoryName);
				feature.addCategories(new ISiteCategory[] {category});
				externalList.add(feature);
			}
		}
		
		siteBuild.addFeatures((ISiteBuildFeature[])buildList.toArray(new ISiteBuildFeature[buildList.size()]));
		site.addFeatures((ISiteFeature[])externalList.toArray(new ISiteFeature[externalList.size()]));

		// Save the models
		siteModel.save();
		siteFile.refreshLocal(IResource.DEPTH_ONE, monitor);
		buildModel.save();
		siteBuildFile.refreshLocal(IResource.DEPTH_ONE, monitor);
		
		// Do the build
		System.out.println("Building");
		FeatureBuildOperation op
			= new FeatureBuildOperation(
				buildList, null, true, true);
		op.run(monitor);
	}
	
	private void uploadUpdateSite() throws Exception {
		System.out.println("Uploading to site");
		
		IProject buildSite = workspace.getRoot().getProject("build.site");
		buildSite.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		// Open the ftp site
		FTPClient ftp = new FTPClient(ftpHost);
		ftp.setConnectMode(FTPConnectMode.ACTIVE);
		ftp.login(ftpUser, ftpPassword);
		ftp.setType(FTPTransferType.BINARY);
		ftp.chdir(ftpPath);

		IFile file = buildSite.getFile("version.xml");
		ftp.put(file.getContents(), "version.xml");
		
		file = buildSite.getFile("site.xml");
		ftp.put(file.getContents(), "site.xml");
		
		IFolder folder = buildSite.getFolder(".sitebuild");
		ftp.chdir(".sitebuild");
		
		file = folder.getFile("sitebuild.xml");
		ftp.put(file.getContents(), "sitebuild.xml");

		folder = buildSite.getFolder("plugins");	
		ftp.chdir("../plugins");
		
		for (int i = 0; i < plugins.length; ++i) {
			String name = plugins[i] + "_" + version + ".jar";
			System.out.println("Uploading plugin: " + name);
			file = folder.getFile(name);
			ftp.put(file.getContents(), name);
		}
		
		for (int i = 0; i < fragments.length; ++i) {
			String name = fragments[i] + "_" + version + ".jar";
			System.out.println("Uploading fragment: " + name);
			file = folder.getFile(name);
			ftp.put(file.getContents(), name);
		}
		
		folder = buildSite.getFolder("features");
		ftp.chdir("../features");
		
		for (int i = 0; i < buildFeatures.length; ++i) {
			String name = buildFeatures[i] + "_" + version + ".jar";
			System.out.println("Uploading feature: " + name);
			file = folder.getFile(name);
			ftp.put(file.getContents(), name);
		}
		
		ftp.quit();
	}
	
}
