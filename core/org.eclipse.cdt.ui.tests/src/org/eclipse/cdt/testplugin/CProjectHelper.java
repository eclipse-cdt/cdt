package org.eclipse.cdt.testplugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipFile;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICFolder;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;

/**
 * Helper methods to set up a IJavaProject.
 */
public class CProjectHelper {
    
    public static final IPath RT_STUBS= new Path("testresources/rtstubs.jar");
    public static final IPath JUNIT_SRC= new Path("testresources/junit37-noUI-src.zip");
    
    public static final IPath MYLIB= new Path("testresources/mylib.jar");
    

    /**
     * Creates a ICProject.
     */    
    public static ICProject createCProject(String projectName, String binFolderName) throws CoreException {
        IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
        IProject project= root.getProject(projectName);
        if (!project.exists()) {
            project.create(null);
        } else {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        }
        
        if (!project.isOpen()) {
            project.open(null);
        }
        
        
        if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
            addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
        }
        
        ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);
        
        return cproject;    
    }
    
    /**
     * Removes a ICProject.
     */        
    public static void delete(ICProject cproject) throws CoreException {
        performDummySearch();
        cproject.getProject().delete(true, true, null);
    }

    public static void performDummySearch() throws CModelException {
        /*  SearchEngine().searchAllTypeNames(
             ResourcesPlugin.getWorkspace(),
            null,
            null,
            IJavaSearchConstants.EXACT_MATCH,
            IJavaSearchConstants.CASE_SENSITIVE,
            IJavaSearchConstants.CLASS,
            SearchEngine.createJavaSearchScope(new IJavaElement[0]),
            new Requestor(),
            IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
            null); */
    }


    /**
     * Adds a source container to a ICProject.
     */        
    public static ICFolder addSourceContainer(ICProject cproject, String containerName) throws CoreException {
        IProject project= cproject.getProject();
        IContainer container= null;
        if (containerName == null || containerName.length() == 0) {
            container= project;
        } else {
            IFolder folder= project.getFolder(containerName);
            if (!folder.exists()) {
                folder.create(false, true, null);
            }
            container= folder;
        }

        return (ICFolder)container;
    }

    /**
     * Adds a source container to a ICProject and imports all files contained
     * in the given Zip file.
     */    
    public static ICFolder addSourceContainerWithImport(ICProject cproject, String containerName, ZipFile zipFile) throws InvocationTargetException, CoreException {
        ICFolder root= addSourceContainer(cproject, containerName);
        importFilesFromZip(zipFile, root.getPath(), null);
        return root;
    }

    /**
     * Removes a source folder from a IJavaProject.
     */        
    public static void removeSourceContainer(ICProject cproject, String containerName) throws CoreException {
        IFolder folder= cproject.getProject().getFolder(containerName);
        folder.delete(true, null);
    }


    /**
     * Adds a required project entry.
     */        
    public static void addRequiredProject(ICProject cproject, ICProject required) throws CModelException {
        //IClasspathEntry cpe= JavaCore.newProjectEntry(required.getProject().getFullPath());
        //addToClasspath(cproject, cpe);
    }    

    /**
     * Attempts to find an archive with the given name in the workspace
     */
    public static IArchive findArchive(ICProject testProject,String name) {
        int x;
           IArchive[] myArchives;
        IArchiveContainer archCont;
        /***
         * Since ArchiveContainer.getArchives does not wait until 
         * all the archives in the project have been parsed before 
         * returning the list, we have to do a sync ArchiveContainer.getChildren
         * first to make sure we find all the archives.
         */
        archCont=testProject.getArchiveContainer();

        myArchives=archCont.getArchives();
        if (myArchives.length<1) 
            return(null);
        for (x=0;x<myArchives.length;x++) {
            if (myArchives[x].getElementName().equals(name))
                    return(myArchives[x]);
                
        }
        return(null);
    }    
    /**
     * Attempts to find a binary with the given name in the workspace
     */
    public static IBinary findBinary(ICProject testProject,String name) {
        IBinaryContainer binCont;
        int x;
           IBinary[] myBinaries;
        binCont=testProject.getBinaryContainer();
        
        myBinaries=binCont.getBinaries();
        if (myBinaries.length<1) 
            return(null);
        for (x=0;x<myBinaries.length;x++) {
            if (myBinaries[x].getElementName().equals(name))
                return(myBinaries[x]);
                
                
        }
        return(null);
    }    

    /**
     * Attempts to find an object with the given name in the workspace
     */
    public static IBinary findObject(ICProject testProject,String name) {
        int x;
        ICElement[] myElements;
        myElements=testProject.getChildren();
        if (myElements.length<1) 
            return(null);
        for (x=0;x<myElements.length;x++) {
            if (myElements[x].getElementName().equals(name))
                if (myElements[x] instanceof ICElement) {
                    if (myElements[x] instanceof IBinary) {
                         return((IBinary) myElements[x]);
                    }
                }                 
        }
        return(null);
    }    
    /**
     * Attempts to find a TranslationUnit with the given name in the workspace
     */
    public static ITranslationUnit findTranslationUnit(ICProject testProject,String name) {
        int x;
           ICElement[] myElements;
        myElements=testProject.getChildren();
        if (myElements.length<1) 
            return(null);
        for (x=0;x<myElements.length;x++) {
            if (myElements[x].getElementName().equals(name))
                if (myElements[x] instanceof ICElement) {
                    if (myElements[x] instanceof ITranslationUnit) {
                         return((ITranslationUnit) myElements[x]);
                    }
                }                 
        }
        return(null);
    }    



    /**
     * Attempts to find an element with the given name in the workspace
     */
    public static ICElement findElement(ICProject testProject,String name) {
        int x;
           ICElement[] myElements;
        myElements=testProject.getChildren();
        if (myElements.length<1) 
            return(null);
        for (x=0;x<myElements.length;x++) {
            if (myElements[x].getElementName().equals(name))
                if (myElements[x] instanceof ICElement) {
                    return((ICElement) myElements[x]);
                } 
                
                
        }
        return(null);
    }    
        
    
    /**
     * Try to find rt.jar
     */
    public static IPath[] findRtJar() {
        File rtStubs= CTestPlugin.getDefault().getFileInPlugin(RT_STUBS);
        if (rtStubs != null && rtStubs.exists()) {
            return new IPath[] {
                new Path(rtStubs.getPath()),
                null,
                null
            };
        }
        
        /*
        IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
        if (vmInstall != null) {
            LibraryLocation loc= vmInstall.getVMInstallType().getDefaultLibraryLocation(vmInstall.getInstallLocation());
            if (loc != null) {
                return new IPath[] {
                       new Path(loc.getSystemLibrary().getPath()),
                    new Path(loc.getSystemLibrarySource().getPath()),
                    loc.getPackageRootPath()
                };
            }
        }*/
        return null;
    }
        
    private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = proj.getDescription();
        String[] prevNatures= description.getNatureIds();
        String[] newNatures= new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length]= natureId;
        description.setNatureIds(newNatures);
        proj.setDescription(description, monitor);
    }
    
    private static void importFilesFromZip(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException {        
        ZipFileStructureProvider structureProvider=    new ZipFileStructureProvider(srcZipFile);
        try {
            ImportOperation op= new ImportOperation(destPath, structureProvider.getRoot(), structureProvider, new ImportOverwriteQuery());
            op.run(monitor);
        } catch (InterruptedException e) {
            // should not happen
        }
    }
    
    private static class ImportOverwriteQuery implements IOverwriteQuery {
        public String queryOverwrite(String file) {
            return ALL;
        }    
    }        
    
    
}

