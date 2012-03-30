package org.eclipse.cdt.autotools.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipFile;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.internal.autotools.ui.wizards.ConvertToAutotoolsProjectWizard;
import org.eclipse.cdt.internal.autotools.ui.wizards.ConvertToAutotoolsProjectWizardPage;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;


public class ProjectTools {

	static IWorkspace workspace;
    static IWorkspaceRoot root;
    static NullProgressMonitor monitor;
    static String pluginRoot;
    static ConvertProjectWizardPage page;
    static boolean setupComplete;

    /**
     * Setup routine for tests.
     * @return true if setup successful, false otherwise
     * @throws Exception
     */
	public static boolean setup() throws Exception {
		if (!setupComplete) {
			IWorkspaceDescription desc;
			workspace = ResourcesPlugin.getWorkspace();
			if (workspace == null) {
				return false;
			}
			root = workspace.getRoot();
			monitor = new NullProgressMonitor();
			if (root == null) {
				return false;
			}
			desc = workspace.getDescription();
			desc.setAutoBuilding(false);
			workspace.setDescription(desc);
		}
		setupComplete = true;
		return true;
	}
	
	/**
	 * Build the project.
	 * @return true if build started successfully or false otherwise
	 */
	public static boolean build() {
		try {
			workspace.build(IncrementalProjectBuilder.FULL_BUILD, getMonitor());
			workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		} catch (CoreException e) {
		    return false;	
		}
		return true;
	}
	
	/**
	 * Get the default monitor.
	 * @return The current monitor
	 */
	public static IProgressMonitor getMonitor() {
		return monitor;
	}

	/**
	 * Mark a specified file in a project as executable.
	 * @param project The project that the file is found in
	 * @param filePath The relative path to the file
	 * @return true if the change was successful, false otherwise
	 */
	public static boolean markExecutable(IProject project, String filePath) {
		// Get a launcher for the config command
		CommandLauncher launcher = new CommandLauncher();
		OutputStream stdout = new ByteArrayOutputStream();
		OutputStream stderr = new ByteArrayOutputStream();

		launcher.showCommand(true);
		IPath commandPath = new Path("chmod");
		IPath runPath = project.getLocation().append(filePath).removeLastSegments(1);
		String[] args = new String[2];
		args[0] = "+x";
		args[1] = project.getLocation().append(filePath).toOSString();
		try {
			Process proc = launcher.execute(commandPath, args, new String[0],
					runPath, new NullProgressMonitor());
			if (proc != null) {
				try {
					// Close the input of the process since we will never write to
					// it
					proc.getOutputStream().close();
				} catch (IOException e) {
				}

				if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(
						monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
					return false;
				}
			} else
				return false;
		} catch (CoreException e) {
			return false;
		}
		return true;
	}
	
	// Inner class to allow us to fake a project wizard without starting up
	// the UI for it.
	protected static class ConvertToAutotoolsProjectWizardTest extends ConvertToAutotoolsProjectWizard {
		ConvertToAutotoolsProjectWizardTest() {
			super();
		}

		// The following is a kludge to allow testing to occur for the
		// wizard code.  The regular applyOptions() method would also attempt
		// to call performApply() for the optionPage.  This doesn't work in
		// the test scenario because the UI display logic is needed to
		// initialize some things.  The performApply() call is only needed
		// to check out referenced projects.  In our test scenario, this is
		// not required.
		public void applyOptions(IProject project, IProgressMonitor monitor) {
			setCurrentProject(project);
	    }
		
		public IConfiguration[] getSelectedConfigurations() {
			IProjectType projectType = ManagedBuildManager.getExtensionProjectType("org.eclipse.linuxtools.cdt.autotools.core.projectType"); //$NON-NLS-1$
			IConfiguration[] cfgs = projectType.getConfigurations();
			return cfgs;
		}
	}
	
	/**
	 * Creates an empty Autotools project.
	 * @param name The name of the new project
	 * @return The newly created project or null
	 */		
	public static IProject createProject(String name) {
		IProject testProject = root.getProject(name);
		if (testProject == null) {
			return null;
        }
		IProjectDescription description = workspace.newProjectDescription(name);
		try {
			testProject.create(monitor);
			testProject.open(monitor);
//			IProjectDescription description = workspace.newProjectDescription(name);
//			if(location != null)
//				description.setLocationURI(location);
			IProject newProject = CCorePlugin.getDefault().createCDTProject(description, testProject, new SubProgressMonitor(monitor,25));
			ConvertToAutotoolsProjectWizardTest wizard = new ConvertToAutotoolsProjectWizardTest();
			wizard.addPages();
			ConvertToAutotoolsProjectWizardPage page = new ConvertToAutotoolsProjectWizardPage("test", wizard);
			page.convertProject(newProject, monitor, wizard.getProjectID());
		} catch (CoreException e) {
			testProject = null;
		}
		return testProject;
	}
	
	/**
	 * Set the configuration source directory for an Autotools project.
	 * @param project The Autotools project to modify
	 * @param dir The relative project directory to use
	 */
	public static void setConfigDir(IProject project, String dir) {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration c = info.getDefaultConfiguration();
		ICConfigurationDescription cfgd = ManagedBuildManager.getDescriptionForConfiguration(c);
		String id = cfgd.getId();
		IAConfiguration cfg = AutotoolsConfigurationManager.getInstance().getConfiguration(project, id, true);
		cfg.setConfigToolDirectory(dir);
//		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
//		ITool tool = info.getToolFromOutputExtension("status"); //$NON-NLS-1$
//		IOption[] options = tool.getOptions();
//		try {
//		for (int i = 0; i < options.length; ++i) {
//			if (options[i].getValueType() == IOption.STRING) {
//				String id = options[i].getId();
//				if (id.indexOf("configdir") > 0) { //$NON-NLS-1$
//					options[i].setValue(dir);
//					break;
//				}
//			}
//		}
//		} catch (BuildException e) {
//			// do nothing
//		}
	}
	
	private static void importFilesFromZip(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException {		
		ZipFileStructureProvider structureProvider=	new ZipFileStructureProvider(srcZipFile);
		try {
			ImportOperation op= new ImportOperation(destPath, structureProvider.getRoot(), structureProvider, new ImportOverwriteQuery());
			op.run(monitor);
		} catch (InterruptedException e) {
			// should not happen
		}
	}

	private static boolean generateFiles(IPath destPath) {
		// Get a launcher for the config command
		CommandLauncher launcher = new CommandLauncher();
		OutputStream stdout = new ByteArrayOutputStream();
		OutputStream stderr = new ByteArrayOutputStream();
		
		IPath runPath = root.getLocation().append(destPath);

		// Run the genfiles.sh shell script which will simulate
		// running aclocal, autoconf, and automake
		launcher.showCommand(true);
		IPath commandPath = new Path("sh");
		String[] cmdargs = new String[]{"genfiles.sh"};
		try {
			Process proc = launcher.execute(commandPath, cmdargs, new String[0],
					runPath, new NullProgressMonitor());
			if (proc != null) {
				try {
					// Close the input of the process since we will never write to
					// it
					proc.getOutputStream().close();
				} catch (IOException e) {
				}

				if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(
						monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
					return false;
				}
			} else
				return false;
		} catch (CoreException e) {
			return false;
		}

		return true;
	}

	private static void importFilesFromZipAndGenerate(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException {
		importFilesFromZip(srcZipFile, destPath, monitor);
		if (!generateFiles(destPath))
			throw new InvocationTargetException(new Exception("Unsuccessful test file generation"));
	}
	
	private static class ImportOverwriteQuery implements IOverwriteQuery {
		public String queryOverwrite(String file) {
			return ALL;
		}	
	}		
	
	/**
	 * Adds a source container to a IProject.
	 * @param jproject The parent project
	 * @param containerName The name of the new source container
	 * @return The handle to the new source container
	 * @throws CoreException Creation failed
	 */		
	public static IContainer addSourceContainer(IProject jproject, String containerName) throws CoreException {
		return addSourceContainer(jproject, containerName, new Path[0]);
	}

	/**
	 * Adds a source container to a IProject.
	 * @param jproject The parent project
	 * @param containerName The name of the new source container
	 * @param exclusionFilters Exclusion filters to set
	 * @return The handle to the new source container
	 * @throws CoreException Creation failed
	 */		
	public static IContainer addSourceContainer(IProject jproject, String containerName, IPath[] exclusionFilters) throws CoreException {
		return addSourceContainer(jproject, containerName, new Path[0], exclusionFilters);
	}
	
	/**
	 * Adds a source container to a IProject.
	 * @param jproject The parent project
	 * @param containerName The name of the new source container
	 * @param inclusionFilters Inclusion filters to set
	 * @param exclusionFilters Exclusion filters to set
	 * @return The handle to the new source container
	 * @throws CoreException Creation failed
	 */				
	public static IContainer addSourceContainer(IProject jproject, String containerName, IPath[] inclusionFilters, IPath[] exclusionFilters) throws CoreException {
		IProject project= jproject.getProject();
		IContainer container= null;
		if (containerName == null || containerName.length() == 0) {
			container= project;
		} else {
			IFolder folder= project.getFolder(containerName);
			if (!folder.exists()) {
				CoreUtility.createFolder(folder, false, true, null);
			}
			container= folder;
		}
	
		return container;
	}
	
	/**
	 * Adds a source container to a IProject and imports all files contained
	 * in the given ZIP file.
	 * @param project The parent project
	 * @param containerName Name of the source container
	 * @param zipFile Archive to import
	 * @param containerEncoding encoding for the generated source container
	 * @param generate true if configuration files need to be pre-generated
	 * @param exclusionFilters Exclusion filters to set
	 * @return The handle to the new source container
	 * @throws InvocationTargetException Creation failed
	 * @throws CoreException Creation failed
	 * @throws IOException Creation failed
	 */		
	public static IContainer addSourceContainerWithImport(IProject project, String containerName, File zipFile, String containerEncoding, boolean generate, IPath[] exclusionFilters) throws InvocationTargetException, CoreException, IOException {
		ZipFile file= new ZipFile(zipFile);
		try {
//			IPackageFragmentRoot root= addSourceContainer(jproject, containerName, exclusionFilters);
//			((IContainer) root.getCorrespondingResource()).setDefaultCharset(containerEncoding, null);
			IContainer root= addSourceContainer(project, containerName, exclusionFilters);
			if (generate)
				importFilesFromZipAndGenerate(file, root.getFullPath(), null);
			else
				importFilesFromZip(file, root.getFullPath(), null);
			return root;
		} finally {
			if (file != null) {
				file.close();
			}
		}
	}

	/**
	 * Adds a source container to a IProject and imports all files contained
	 * in the given ZIP file and generates configuration files if needed.
	 * @param project The parent project
	 * @param containerName Name of the source container
	 * @param path path of zipFile Archive to import
	 * @param containerEncoding encoding for the generated source container
	 * @param generate true if configuration files need to be pre-generated
	 * @return The handle to the new source container
	 * @throws InvocationTargetException Creation failed
	 * @throws CoreException Creation failed
	 * @throws IOException Creation failed
	 */		
	public static IContainer addSourceContainerWithImport(IProject project, String containerName, Path zipFilePath, String containerEncoding, boolean generate) throws InvocationTargetException, CoreException, IOException {
		File zipFile = new File(FileLocator.toFileURL(FileLocator.find(AutotoolsTestsPlugin.getDefault().getBundle(), zipFilePath, null)).getFile());
		return addSourceContainerWithImport(project, containerName, zipFile, containerEncoding, generate, null);
	}
	
	/**
	 * Adds a source container to a IProject and imports all files contained
	 * in the given ZIP file.
	 * @param project The parent project
	 * @param containerName Name of the source container
	 * @param path path of zipFile Archive to import
	 * @param containerEncoding encoding for the generated source container
	 * @return The handle to the new source container
	 * @throws InvocationTargetException Creation failed
	 * @throws CoreException Creation failed
	 * @throws IOException Creation failed
	 */		
	public static IContainer addSourceContainerWithImport(IProject project, String containerName, Path zipFilePath, String containerEncoding) throws InvocationTargetException, CoreException, IOException {
		return addSourceContainerWithImport(project, containerName, zipFilePath, containerEncoding, false);
	}

	/**
	 * Create an empty file for a project.
	 * @param project The project to create the file for
	 * @param filename The name of the new file
	 * @return the created file
	 * @throws CoreException
	 */
	public IFile createEmptyFile(IProject project, String filename) throws CoreException {
		IFile emptyFile = project.getFile(filename);
		emptyFile.create(null, false, null);
		return emptyFile;
	}

	/**
	 * Create a file for a project and initialize the contents.
	 * @param project The project to create a file for
	 * @param filename Name of the new file
	 * @param contents String containing the initial contents of the file
	 * @return the created file
	 * @throws CoreException
	 */
	public IFile createFile(IProject project, String filename, String contents) throws CoreException {
		IFile file = project.getFile(filename);
		file.create(null, false, null);
		file.setContents(new ByteArrayInputStream(contents.getBytes()), false, false, null);
		return file;
	}
	
}
