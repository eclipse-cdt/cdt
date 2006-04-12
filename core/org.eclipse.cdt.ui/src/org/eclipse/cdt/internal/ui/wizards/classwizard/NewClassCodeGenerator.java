/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.util.IQualifiedTypeName;
import org.eclipse.cdt.core.model.util.QualifiedTypeName;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileGenerator;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

public class NewClassCodeGenerator {

    private IPath fHeaderPath = null;
    private IPath fSourcePath = null;
    private String fClassName = null;
    private IQualifiedTypeName fNamespace = null;
    private String fLineDelimiter;
    private IBaseClassInfo[] fBaseClasses = null;
    private IMethodStub[] fMethodStubs = null;
    private ITranslationUnit fCreatedHeaderTU = null;
    private ITranslationUnit fCreatedSourceTU = null;
    private ICElement fCreatedClass = null;
    
	public static class CodeGeneratorException extends CoreException {
        /**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 1L;
		
        public CodeGeneratorException(String message) {
            super(new Status(Status.ERROR, CUIPlugin.getPluginId(), IStatus.OK, message, null));
        }
        
        public CodeGeneratorException(Throwable e) {
            super(new Status(Status.ERROR, CUIPlugin.getPluginId(), IStatus.OK, e.getMessage(), e));
        }
	}

    public NewClassCodeGenerator(IPath headerPath, IPath sourcePath, String className, String namespace, IBaseClassInfo[] baseClasses, IMethodStub[] methodStubs) {
        fHeaderPath = headerPath;
        fSourcePath = sourcePath;
        if (className != null && className.length() > 0) {
            fClassName = className;
        }
        if (namespace != null && namespace.length() > 0) {
            fNamespace = new QualifiedTypeName(namespace);
        }
        fBaseClasses = baseClasses;
        fMethodStubs = methodStubs;
        fLineDelimiter = NewSourceFileGenerator.getLineDelimiter();
    }

    public ICElement getCreatedClass() {
        return fCreatedClass;
    }

    public ITranslationUnit getCreatedHeaderTU() {
        return fCreatedHeaderTU;
    }
    
    public IFile getCreatedHeaderFile() {
        if (fCreatedHeaderTU != null) {
            return (IFile)fCreatedHeaderTU.getResource();
        }
        return null;
    }

    public ITranslationUnit getCreatedSourceTU() {
        return fCreatedSourceTU;
    }

    public IFile getCreatedSourceFile() {
        if (fCreatedSourceTU != null) {
            return (IFile)fCreatedSourceTU.getResource();
        }
        return null;
    }

    /**
     * Creates the new class.
     * 
     * @param monitor
     *            a progress monitor to report progress.
     * @throws CoreException
     *             Thrown when the creation failed.
     * @throws InterruptedException
     *             Thrown when the operation was cancelled.
     */
    public ICElement createClass(IProgressMonitor monitor) throws CodeGeneratorException, CoreException, InterruptedException {
        if (monitor == null)
            monitor = new NullProgressMonitor();

        monitor.beginTask(NewClassWizardMessages.getString("NewClassCodeGeneration.createType.mainTask"), 400); //$NON-NLS-1$

	    ITranslationUnit headerTU = null;
        ITranslationUnit sourceTU = null;
        ICElement createdClass = null;

        IWorkingCopy headerWorkingCopy = null;
        IWorkingCopy sourceWorkingCopy = null;
        try {
            if (fHeaderPath != null) {

                // get method stubs
                List publicMethods = getStubs(ASTAccessVisibility.PUBLIC, false);
                List protectedMethods = getStubs(ASTAccessVisibility.PROTECTED, false);
                List privateMethods = getStubs(ASTAccessVisibility.PRIVATE, false);
                
	            IFile headerFile = NewSourceFileGenerator.createHeaderFile(fHeaderPath, true, new SubProgressMonitor(monitor, 50));
	            if (headerFile != null) {
	                headerTU = (ITranslationUnit) CoreModel.getDefault().create(headerFile);
	            }
	
	            // create a working copy with a new owner
	            headerWorkingCopy = headerTU.getWorkingCopy();
	            // headerWorkingCopy = headerTU.getSharedWorkingCopy(null, CUIPlugin.getDefault().getBufferFactory());
	
	            String headerContent = constructHeaderFileContent(headerTU, publicMethods, protectedMethods, privateMethods, headerWorkingCopy.getBuffer().getContents(), new SubProgressMonitor(monitor, 100));
	            headerWorkingCopy.getBuffer().setContents(headerContent);
	
	            if (monitor.isCanceled()) {
	                throw new InterruptedException();
	            }
	
	            headerWorkingCopy.reconcile();
	            headerWorkingCopy.commit(true, monitor);
	            monitor.worked(50);
	
	            createdClass = headerWorkingCopy.getElement(fClassName.toString());
	            fCreatedClass = createdClass;
	            fCreatedHeaderTU = headerTU;
            }

            if (fSourcePath != null) {
                
                // get method stubs
                List publicMethods = getStubs(ASTAccessVisibility.PUBLIC, true);
                List protectedMethods = getStubs(ASTAccessVisibility.PROTECTED, true);
                List privateMethods = getStubs(ASTAccessVisibility.PRIVATE, true);
                
                if (publicMethods.isEmpty() && protectedMethods.isEmpty() && privateMethods.isEmpty()) {
                    //TODO need prefs option
                    // don't create source file if no method bodies
                    monitor.worked(100);
                } else {
		            IFile sourceFile = NewSourceFileGenerator.createSourceFile(fSourcePath, true, new SubProgressMonitor(monitor, 50));
		            if (sourceFile != null) {
		                sourceTU = (ITranslationUnit) CoreModel.getDefault().create(sourceFile);
		            }
		            monitor.worked(50);
		
		            // create a working copy with a new owner
		            sourceWorkingCopy = sourceTU.getWorkingCopy();
		
		            String sourceContent = constructSourceFileContent(sourceTU, headerTU, publicMethods, protectedMethods, privateMethods, sourceWorkingCopy.getBuffer().getContents(), new SubProgressMonitor(monitor, 100));
		            sourceWorkingCopy.getBuffer().setContents(sourceContent);
		
		            if (monitor.isCanceled()) {
		                throw new InterruptedException();
		            }
		
		            sourceWorkingCopy.reconcile();
		            sourceWorkingCopy.commit(true, monitor);
		            monitor.worked(50);
		
		            fCreatedSourceTU = sourceTU;
                }
            }
        } finally {
            if (headerWorkingCopy != null) {
                headerWorkingCopy.destroy();
            }
            if (sourceWorkingCopy != null) {
                sourceWorkingCopy.destroy();
            }
            monitor.done();
        }

        return fCreatedClass;
    }

    public String constructHeaderFileContent(ITranslationUnit headerTU, List publicMethods, List protectedMethods, List privateMethods, String oldContents, IProgressMonitor monitor) throws CodeGeneratorException {
        monitor.beginTask(NewClassWizardMessages.getString("NewClassCodeGeneration.createType.task.header"), 100); //$NON-NLS-1$
        
        if (oldContents != null && oldContents.length() == 0)
            oldContents = null;
        
        //TODO should use code templates
        StringBuffer text = new StringBuffer();
        
        int appendFirstCharPos = -1;
        if (oldContents != null) {
	        int insertionPos = getClassDefInsertionPos(oldContents);
	        if (insertionPos == -1) {
		        text.append(oldContents);
	        } else {
	            // skip over whitespace
	            int prependLastCharPos = insertionPos - 1;
	            while (prependLastCharPos >= 0 && Character.isWhitespace(oldContents.charAt(prependLastCharPos))) {
	                --prependLastCharPos;
	            }
	            if (prependLastCharPos >= 0) {
	                text.append(oldContents.substring(0, prependLastCharPos + 1));
	            }
	            appendFirstCharPos = prependLastCharPos + 1;
	        }
            text.append(fLineDelimiter);
            
            // insert a blank line before class definition
            text.append(fLineDelimiter);
        }

        if (fBaseClasses != null && fBaseClasses.length > 0) {
            addBaseClassIncludes(headerTU, text, new SubProgressMonitor(monitor, 50));
            text.append(fLineDelimiter);
        }
        
        if (fNamespace != null) {
            beginNamespace(text);
        }
        
        text.append("class "); //$NON-NLS-1$
        text.append(fClassName);
        addBaseClassInheritance(text);
        text.append(fLineDelimiter);
        text.append('{');
        text.append(fLineDelimiter);

        //TODO sort methods (eg constructor always first?)
        if (!publicMethods.isEmpty()
	            || !protectedMethods.isEmpty()
	            || !privateMethods.isEmpty()) {
            addMethodDeclarations(publicMethods, protectedMethods, privateMethods, text);
        }

        text.append("};"); //$NON-NLS-1$
        text.append(fLineDelimiter);

        if (fNamespace != null) {
            endNamespace(text);
        }

        if (oldContents != null && appendFirstCharPos != -1) {
            // insert a blank line after class definition
            text.append(fLineDelimiter);
            
            // skip over any extra whitespace
            int len = oldContents.length();
            while (appendFirstCharPos < len && Character.isWhitespace(oldContents.charAt(appendFirstCharPos))) {
                ++appendFirstCharPos;
            }
            if (appendFirstCharPos < len) {
                text.append(oldContents.substring(appendFirstCharPos));
            }
        }
        
        String newContents = text.toString();
        monitor.done();
        return newContents;
    }

    private int getClassDefInsertionPos(String contents) {
        if (contents.length() == 0) {
            return -1;
        }
        //TODO temporary hack
        int insertPos = contents.lastIndexOf("#endif"); //$NON-NLS-1$
        if (insertPos != -1) {
            // check if any code follows the #endif
            if ((contents.indexOf('}', insertPos) != -1)
                    || (contents.indexOf(';', insertPos) != -1)) {
                return -1;
            }
        }
        return insertPos;
    }
    
    private void beginNamespace(StringBuffer text) {
        for (int i = 0; i < fNamespace.segmentCount(); ++i) {
	        text.append("namespace "); //$NON-NLS-1$
	        text.append(fNamespace.segment(i));
	        text.append(fLineDelimiter);
	        text.append('{');
	        text.append(fLineDelimiter);
	        text.append(fLineDelimiter);
        }
    }

    private void endNamespace(StringBuffer text) {
        for (int i = 0; i < fNamespace.segmentCount(); ++i) {
            text.append(fLineDelimiter);
	        text.append("}"); //$NON-NLS-1$
	        text.append(fLineDelimiter);
        }
    }

    private void addMethodDeclarations(List publicMethods, List protectedMethods, List privateMethods, StringBuffer text) {
        if (!publicMethods.isEmpty()) {
            text.append("public:"); //$NON-NLS-1$
            text.append(fLineDelimiter);
            for (Iterator i = publicMethods.iterator(); i.hasNext();) {
                IMethodStub stub = (IMethodStub) i.next();
                String code = stub.createMethodDeclaration(fClassName, fBaseClasses, fLineDelimiter);
                text.append('\t');
                text.append(code);
                text.append(fLineDelimiter);
            }
        }

        if (!protectedMethods.isEmpty()) {
            text.append("protected:"); //$NON-NLS-1$
            text.append(fLineDelimiter);
            for (Iterator i = protectedMethods.iterator(); i.hasNext();) {
                IMethodStub stub = (IMethodStub) i.next();
                String code = stub.createMethodDeclaration(fClassName, fBaseClasses, fLineDelimiter);
                text.append('\t');
                text.append(code);
                text.append(fLineDelimiter);
            }
        }

        if (!privateMethods.isEmpty()) {
            text.append("private:"); //$NON-NLS-1$
            text.append(fLineDelimiter);
            for (Iterator i = privateMethods.iterator(); i.hasNext();) {
                IMethodStub stub = (IMethodStub) i.next();
                String code = stub.createMethodDeclaration(fClassName, fBaseClasses, fLineDelimiter);
                text.append('\t');
                text.append(code);
                text.append(fLineDelimiter);
            }
        }
    }

    private List getStubs(ASTAccessVisibility access, boolean skipInline) {
        List list = new ArrayList();
        if (fMethodStubs != null) {
            for (int i = 0; i < fMethodStubs.length; ++i) {
                IMethodStub stub = fMethodStubs[i];
                if (stub.getAccess() == access && (!skipInline || !stub.isInline())) {
                    list.add(stub);
                }
            }
        }
        return list;
    }

    private void addBaseClassInheritance(StringBuffer text) {
        if (fBaseClasses != null && fBaseClasses.length > 0) {
            text.append(" : "); //$NON-NLS-1$
            for (int i = 0; i < fBaseClasses.length; ++i) {
//                IBaseClassInfo baseClass = fBaseClasses[i];
//                String baseClassName = baseClass.getType().getQualifiedTypeName().getFullyQualifiedName();
//                if (i > 0)
//                    text.append(", "); //$NON-NLS-1$
//                if (baseClass.getAccess() == ASTAccessVisibility.PRIVATE)
//                    text.append("private"); //$NON-NLS-1$
//                else if (baseClass.getAccess() == ASTAccessVisibility.PROTECTED)
//                    text.append("private"); //$NON-NLS-1$
//                else
//                    text.append("public"); //$NON-NLS-1$
//                text.append(' ');
//
//                if (baseClass.isVirtual())
//                    text.append("virtual "); //$NON-NLS-1$
//
//                text.append(baseClassName);
            }
        }
    }

    private void addBaseClassIncludes(ITranslationUnit headerTU, StringBuffer text, IProgressMonitor monitor) throws CodeGeneratorException {

        monitor.beginTask(NewClassWizardMessages.getString("NewClassCodeGeneration.createType.task.header.includePaths"), 100); //$NON-NLS-1$
        
        ICProject cProject = headerTU.getCProject();
        IProject project = cProject.getProject();
        IPath projectLocation = project.getLocation();
        IPath headerLocation = headerTU.getResource().getLocation();
        
        List includePaths = getIncludePaths(headerTU);
        List baseClassPaths = getBaseClassPaths(verifyBaseClasses());
        
	    // add the missing include paths to the project
        if (createIncludePaths()) {
	        List newIncludePaths = getMissingIncludePaths(projectLocation, includePaths, baseClassPaths);
	        if (!newIncludePaths.isEmpty()) {
			    addIncludePaths(cProject, newIncludePaths, monitor);
	        }
        }

        List systemIncludes = new ArrayList();
        List localIncludes = new ArrayList();
        
        // sort the include paths into system and local
        for (Iterator bcIter = baseClassPaths.iterator(); bcIter.hasNext(); ) {
            IPath baseClassLocation = (IPath) bcIter.next();
            boolean isSystemIncludePath = false;

            IPath includePath = PathUtil.makeRelativePathToProjectIncludes(baseClassLocation, project);
            if (includePath != null && !projectLocation.isPrefixOf(baseClassLocation)) {
                isSystemIncludePath = true;
            } else if (projectLocation.isPrefixOf(baseClassLocation)
                    && projectLocation.isPrefixOf(headerLocation)) {
                includePath = PathUtil.makeRelativePath(baseClassLocation, headerLocation.removeLastSegments(1));
            }
            if (includePath == null)
                includePath = baseClassLocation;
            
            // make the new #include path in the source file only point to a relative file (i.e. now that the path has been included above in the project)
            includePath = includePath.removeFirstSegments(includePath.segmentCount() - 1);
            
            if (isSystemIncludePath)
                systemIncludes.add(includePath);
            else
                localIncludes.add(includePath);
        }
        
        // write the system include paths, e.g. #include <header.h>
        for (Iterator i = systemIncludes.iterator(); i.hasNext(); ) {
            IPath includePath = (IPath) i.next();
			if (!(headerTU.getElementName().equals(includePath.toString()))) {
			    String include = getIncludeString(includePath.toString(), true);
			    text.append(include);
			    text.append(fLineDelimiter);
			}
        }
        
        // write the local include paths, e.g. #include "header.h"
        for (Iterator i = localIncludes.iterator(); i.hasNext(); ) {
            IPath includePath = (IPath) i.next();
			if (!(headerTU.getElementName().equals(includePath.toString()))) {
			    String include = getIncludeString(includePath.toString(), false);
			    text.append(include);
			    text.append(fLineDelimiter);
			}
        }
        
        monitor.done();
    }
    
    /**
     * Checks if the base classes need to be verified (ie they must exist in the project)
     * 
     * @return <code>true</code> if the base classes should be verified
     */
    private boolean verifyBaseClasses() {
        return NewClassWizardPrefs.verifyBaseClasses();
    }

    /**
     * Checks if include paths can be added to the project as needed.
     *
     * @return <code>true</code> if the include paths should be added
     */
    private boolean createIncludePaths() {
        return NewClassWizardPrefs.createIncludePaths();
    }

    private void addIncludePaths(ICProject cProject, List newIncludePaths, IProgressMonitor monitor) throws CodeGeneratorException {
        monitor.beginTask(NewClassWizardMessages.getString("NewClassCodeGeneration.createType.task.header.addIncludePaths"), 100); //$NON-NLS-1$

        //TODO prefs option whether to add to project or parent source folder?
        IPath addToResourcePath = cProject.getPath();
        try {
            List pathEntryList = new ArrayList();
            List checkEntryList = new ArrayList();
            
            IPathEntry[] checkEntries = cProject.getResolvedPathEntries();
            IPathEntry[] pathEntries = cProject.getRawPathEntries();
            if (pathEntries != null) {
                for (int i = 0; i < pathEntries.length; ++i) {
                	pathEntryList.add(pathEntries[i]);
                }
            }
            for (int i=0; i < checkEntries.length; i++) {
            	if (checkEntries[i] instanceof IIncludeEntry)
            		checkEntryList.add(checkEntries[i]);
            }
            
            for (Iterator ipIter = newIncludePaths.iterator(); ipIter.hasNext(); ) {
                IPath folderToAdd = (IPath) ipIter.next();
                
                // do not add any #includes that are local to the project
                if (cProject.getPath().segment(0).equals(folderToAdd.segment(0)))
                	continue;
                
                IProject includeProject = PathUtil.getEnclosingProject(folderToAdd);

                // make sure that the include is made the same way that build properties for projects makes them, so .contains below is a valid check
                IIncludeEntry entry = CoreModel.newIncludeEntry(addToResourcePath, null, includeProject.getLocation(), true);
                
                if (!checkEntryList.contains(entry)) // if the path already exists in the #includes then don't add it
                	pathEntryList.add(entry);
            }
            pathEntries = (IPathEntry[]) pathEntryList.toArray(new IPathEntry[pathEntryList.size()]);
            cProject.setRawPathEntries(pathEntries, new SubProgressMonitor(monitor, 80));
        } catch (CModelException e) {
            throw new CodeGeneratorException(e);
        }
        monitor.done();
    }

    private List getMissingIncludePaths(IPath projectLocation, List includePaths, List baseClassPaths) {
        // check for missing include paths
        List newIncludePaths = new ArrayList();
        for (Iterator bcIter = baseClassPaths.iterator(); bcIter.hasNext(); ) {
            IPath baseClassLocation = (IPath) bcIter.next();
            
            // skip any paths inside the same project
            //TODO possibly a preferences option?
            if (projectLocation.isPrefixOf(baseClassLocation)) {
                continue;
            }

            IPath folderToAdd = baseClassLocation.removeLastSegments(1);
            IPath canonPath = PathUtil.getCanonicalPath(folderToAdd);
            if (canonPath != null)
                folderToAdd = canonPath;

            // see if folder or its parent hasn't already been added
            for (Iterator newIter = newIncludePaths.iterator(); newIter.hasNext(); ) {
                IPath newFolder = (IPath) newIter.next();
	            if (newFolder.isPrefixOf(folderToAdd)) {
	                folderToAdd = null;
	                break;
	            }
            }

            if (folderToAdd != null) {
	            // search include paths
                boolean foundPath = false;
	            for (Iterator ipIter = includePaths.iterator(); ipIter.hasNext(); ) {
	                IPath includePath = (IPath) ipIter.next();
		            if (includePath.isPrefixOf(folderToAdd) || includePath.equals(folderToAdd)) {
		                foundPath = true;
		                break;
		            }
	            }
	            if (!foundPath) {
                    // remove any children of this folder
                    for (Iterator newIter = newIncludePaths.iterator(); newIter.hasNext(); ) {
                        IPath newFolder = (IPath) newIter.next();
        	            if (folderToAdd.isPrefixOf(newFolder)) {
        	                newIter.remove();
        	            }
                    }
                    if (!newIncludePaths.contains(folderToAdd)) {
                        newIncludePaths.add(folderToAdd);
                    }
	            }
            }
        }
        return newIncludePaths;
    }

    private List getIncludePaths(ITranslationUnit headerTU) throws CodeGeneratorException {
        IProject project = headerTU.getCProject().getProject();
        // get the parent source folder
        ICContainer sourceFolder = CModelUtil.getSourceFolder(headerTU);
        if (sourceFolder == null) {
            throw new CodeGeneratorException("Could not find source folder"); //$NON-NLS-1$
        }

        // get the include paths
        IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
        if (provider != null) {
            IScannerInfo info = provider.getScannerInformation(sourceFolder.getResource());
            if (info != null) {
                String[] includePaths = info.getIncludePaths();
                if (includePaths != null) {
                    List list = new ArrayList();
                    for (int i = 0; i < includePaths.length; ++i) {
                        //TODO do we need to canonicalize these paths first?
                        IPath path = new Path(includePaths[i]);
                        if (!list.contains(path)) {
                            list.add(path);
                        }
                    }
                    return list;
                }
            }
        }
        return null;
    }
    
    private List getBaseClassPaths(boolean verifyLocation) throws CodeGeneratorException {
        List list = new ArrayList();
	    for (int i = 0; i < fBaseClasses.length; ++i) {
//	        IBaseClassInfo baseClass = fBaseClasses[i];
//	        ITypeReference ref = baseClass.getType().getResolvedReference();
//	        IPath baseClassLocation = null;
//	        if (ref != null) {
//	            baseClassLocation = ref.getLocation();
//            }
//            
//	        if (baseClassLocation == null) {
//                if (verifyLocation) {
//                    throw new CodeGeneratorException("Could not find base class " + baseClass.toString()); //$NON-NLS-1$
//                }
//	        } else if (!list.contains(baseClassLocation)) {
//                list.add(baseClassLocation);
//            }
	    }
	    return list;
    }
    
    public String constructSourceFileContent(ITranslationUnit sourceTU, ITranslationUnit headerTU, List publicMethods, List protectedMethods, List privateMethods, String oldContents, IProgressMonitor monitor) {
        monitor.beginTask(NewClassWizardMessages.getString("NewClassCodeGeneration.createType.task.source"), 150); //$NON-NLS-1$
        
        if (oldContents != null && oldContents.length() == 0)
            oldContents = null;

        //TODO should use code templates
        StringBuffer text = new StringBuffer();

        String includeString = null;
        if (headerTU != null) {
            includeString = getHeaderIncludeString(sourceTU, headerTU, text, new SubProgressMonitor(monitor, 50));
	        if (includeString != null) {
	            // check if file already has the include
	            if (oldContents != null && hasInclude(oldContents, includeString)) {
	                // don't bother to add it
	                includeString = null;
	            }
	        }
        }
        
        if (includeString != null) {
            if (oldContents != null) {
    	        int insertionPos = getIncludeInsertionPos(oldContents);
    	        if (insertionPos == -1) {
    		        text.append(oldContents);
                    text.append(fLineDelimiter);
                    text.append(includeString);
                    text.append(fLineDelimiter);
    	        } else {
    	            text.append(oldContents.substring(0, insertionPos));
                    text.append(includeString);
                    text.append(fLineDelimiter);
                    text.append(oldContents.substring(insertionPos));
    	        }
            } else {
                text.append(includeString);
                text.append(fLineDelimiter);
            }

            // add a blank line
            text.append(fLineDelimiter);
        } else if (oldContents != null) {
	        text.append(oldContents);

	        // add a blank line
            text.append(fLineDelimiter);
        }
        
        //TODO sort methods (eg constructor always first?)
        if (publicMethods.isEmpty()
                && protectedMethods.isEmpty()
                && privateMethods.isEmpty()) {
            // no methods
        } else {
            if (fNamespace != null) {
                beginNamespace(text);
            }

            addMethodBodies(publicMethods, protectedMethods, privateMethods, text, new SubProgressMonitor(monitor, 50));

            if (fNamespace != null) {
                endNamespace(text);
            }
        }

        String newContents = text.toString();
        monitor.done();
        return newContents;
    }
    
    private String getHeaderIncludeString(ITranslationUnit sourceTU, ITranslationUnit headerTU, StringBuffer text, IProgressMonitor monitor) {
        IProject project = headerTU.getCProject().getProject();
        IPath projectLocation = project.getLocation();
        IPath headerLocation = headerTU.getResource().getLocation();
        IPath sourceLocation = sourceTU.getResource().getLocation();

        IPath includePath = PathUtil.makeRelativePathToProjectIncludes(headerLocation, project);
        boolean isSystemIncludePath = false;
        if (includePath != null && !projectLocation.isPrefixOf(headerLocation)) {
            isSystemIncludePath = true;
        } else if (projectLocation.isPrefixOf(headerLocation)
                && projectLocation.isPrefixOf(sourceLocation)) {
            includePath = PathUtil.makeRelativePath(headerLocation, sourceLocation.removeLastSegments(1));
        }
        if (includePath == null)
            includePath = headerLocation;

        return getIncludeString(includePath.toString(), isSystemIncludePath);
    }

    private boolean hasInclude(String contents, String include) {
        int maxStartPos = contents.length() - include.length() - 1;
        if (maxStartPos < 0) {
            return false;
        }
        int startPos = 0;
        while (startPos <= maxStartPos) {
	        int includePos = contents.indexOf(include, startPos);
	        if (includePos == -1) {
	            return false;
	        }
	        if (includePos == startPos) {
	        	return true;
	        }
	        
	        // TODO detect if it's commented out
	        
	        // make sure it's on a line by itself
	        int linePos = findFirstLineChar(contents, includePos);
	        if (linePos == -1 || linePos == includePos) {
	        	return true;
	        }
	        boolean badLine = false;
	        for (int pos = linePos; pos < includePos; ++pos) {
	        	char c = contents.charAt(pos);
	        	if (!Character.isWhitespace(c)) {
	        		badLine = true;
	        		break;
	        	}
	        }
	        if (!badLine) {
	        	return true;
	        }
	        
	        // keep searching
	        startPos = includePos + include.length();
        }
        return false;
    }

    private int getIncludeInsertionPos(String contents) {
        if (contents.length() == 0) {
            return -1;
        }
        //TODO temporary hack
        int includePos = contents.lastIndexOf("#include "); //$NON-NLS-1$
        if (includePos != -1) {
            // find the end of line
            int startPos = includePos + "#include ".length(); //$NON-NLS-1$
            int eolPos = findLastLineChar(contents, startPos);
            if (eolPos != -1) {
                int insertPos = eolPos + 1;
                if (insertPos < (contents.length() - 1)) {
                    return insertPos;
                }
            }
        }
        return -1;
    }

    private void addMethodBodies(List publicMethods, List protectedMethods, List privateMethods, StringBuffer text, IProgressMonitor monitor) {
        if (!publicMethods.isEmpty()) {
            for (Iterator i = publicMethods.iterator(); i.hasNext();) {
                IMethodStub stub = (IMethodStub) i.next();
                String code = stub.createMethodImplementation(fClassName, fBaseClasses, fLineDelimiter);
                text.append(code);
                text.append(fLineDelimiter);
                if (i.hasNext())
                    text.append(fLineDelimiter);
            }
        }

        if (!protectedMethods.isEmpty()) {
            for (Iterator i = protectedMethods.iterator(); i.hasNext();) {
                IMethodStub stub = (IMethodStub) i.next();
                String code = stub.createMethodImplementation(fClassName, fBaseClasses, fLineDelimiter);
                text.append(code);
                text.append(fLineDelimiter);
                if (i.hasNext())
                    text.append(fLineDelimiter);
            }
        }

        if (!privateMethods.isEmpty()) {
            for (Iterator i = privateMethods.iterator(); i.hasNext();) {
                IMethodStub stub = (IMethodStub) i.next();
                String code = stub.createMethodImplementation(fClassName, fBaseClasses, fLineDelimiter);
                text.append(code);
                text.append(fLineDelimiter);
                if (i.hasNext())
                    text.append(fLineDelimiter);
            }
        }
    }

    private String getIncludeString(String fileName, boolean isSystemInclude) {
        StringBuffer buf = new StringBuffer();
        buf.append("#include "); //$NON-NLS-1$
        if (isSystemInclude)
            buf.append('<'); //$NON-NLS-1$
        else
            buf.append('\"'); //$NON-NLS-1$
        buf.append(fileName);
        if (isSystemInclude)
            buf.append('>'); //$NON-NLS-1$
        else
            buf.append('\"'); //$NON-NLS-1$
        return buf.toString();
    }
    
    private int findLastLineChar(String contents, int startPos) {
        int endPos = contents.length() - 1;
        int linePos = startPos;
        while (linePos <= endPos) {
            char c = contents.charAt(linePos);
            if (c == '\r') {
                // could be '\r\n' as one delimiter
                if (linePos < endPos && contents.charAt(linePos + 1) == '\n') {
                    return linePos + 1;
                }
                return linePos;
            } else if (c == '\n') {
                return linePos;
            }
            ++linePos;
        }
        return -1;
    }
    
    private int findFirstLineChar(String contents, int startPos) {
        int linePos = startPos;
        while (linePos >= 0) {
            char c = contents.charAt(linePos);
            if (c == '\n' || c == '\r') {
                if (linePos + 1 < startPos) {
                    return linePos + 1;
                }
                return -1;
            }
            --linePos;
        }
        return -1;
    }
    
}

