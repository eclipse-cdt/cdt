/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class NewClassCodeGenerator {

    private IPath fHeaderPath = null;
    private IPath fSourcePath = null;
    private String fClassName = null;
    private String fNamespace = null;
    private String fLineDelimiter;
    private IBaseClassInfo[] fBaseClasses = null;
    private IMethodStub[] fMethodStubs = null;
    private ITranslationUnit fCreatedHeaderTU = null;
    private ITranslationUnit fCreatedSourceTU = null;
    private ICElement fCreatedClass = null;

    public NewClassCodeGenerator(IPath headerPath, IPath sourcePath, String className, String namespace, IBaseClassInfo[] baseClasses, IMethodStub[] methodStubs) {
        fHeaderPath = headerPath;
        fSourcePath = sourcePath;
        fClassName = className;
        fNamespace = namespace;
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

    public ITranslationUnit getCreatedSourceTU() {
        return fCreatedSourceTU;
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
    public ICElement createClass(IProgressMonitor monitor) throws CoreException, InterruptedException {
        if (monitor == null)
            monitor = new NullProgressMonitor();

        monitor.beginTask(NewClassWizardMessages.getString("NewClassCodeGeneration.createType.task"), 10); //$NON-NLS-1$

        ITranslationUnit headerTU = null;
        ITranslationUnit sourceTU = null;
        ICElement createdClass = null;

        IWorkingCopy headerWorkingCopy = null;
        IWorkingCopy sourceWorkingCopy = null;
        try {
            monitor.worked(1);

            if (fHeaderPath != null) {
	            IFile headerFile = NewSourceFileGenerator.createHeaderFile(fHeaderPath, true, monitor);
	            if (headerFile != null) {
	                headerTU = (ITranslationUnit) CoreModel.getDefault().create(headerFile);
	            }
	            monitor.worked(1);
	
	            // create a working copy with a new owner
	            headerWorkingCopy = headerTU.getWorkingCopy();
	            // headerWorkingCopy = headerTU.getSharedWorkingCopy(null, CUIPlugin.getDefault().getBufferFactory());
	
	            String headerContent = constructHeaderFileContent(headerTU, headerWorkingCopy.getBuffer().getContents());
	            headerWorkingCopy.getBuffer().setContents(headerContent);
	
	            if (monitor.isCanceled()) {
	                throw new InterruptedException();
	            }
	
	            headerWorkingCopy.reconcile();
	            headerWorkingCopy.commit(true, monitor);
	
	            createdClass = headerWorkingCopy.getElement(fClassName);
	            fCreatedClass = createdClass;
	            fCreatedHeaderTU = headerTU;
            }

            if (fSourcePath != null) {
	            IFile sourceFile = NewSourceFileGenerator.createHeaderFile(fSourcePath, true, monitor);
	            if (sourceFile != null) {
	                sourceTU = (ITranslationUnit) CoreModel.getDefault().create(sourceFile);
	            }
	            monitor.worked(1);
	
	            // create a working copy with a new owner
	            sourceWorkingCopy = sourceTU.getWorkingCopy();
	
	            String sourceContent = constructSourceFileContent(sourceTU, headerTU);
	            sourceWorkingCopy.getBuffer().setContents(sourceContent);
	
	            if (monitor.isCanceled()) {
	                throw new InterruptedException();
	            }
	
	            sourceWorkingCopy.reconcile();
	            sourceWorkingCopy.commit(true, monitor);
	
	            fCreatedSourceTU = sourceTU;
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

    public String constructHeaderFileContent(ITranslationUnit headerTU, String oldContents) {
        //TODO should use code templates
        StringBuffer text = new StringBuffer();
        
        int appendFirstCharPos = -1;
        if (oldContents != null) {
	        int insertionPos = getInsertionPos(oldContents);
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
            addBaseClassIncludes(headerTU, text);
            text.append(fLineDelimiter);
        }
        
        if (fNamespace != null && fNamespace.length() > 0) {
            beginNamespace(text);
        }
        
        text.append("class "); //$NON-NLS-1$
        text.append(fClassName);
        addBaseClassInheritance(text);
        text.append(fLineDelimiter);
        text.append('{');
        text.append(fLineDelimiter);

        //TODO sort methods (eg constructor always first?)
        List publicMethods = getStubs(ASTAccessVisibility.PUBLIC, false);
        List protectedMethods = getStubs(ASTAccessVisibility.PROTECTED, false);
        List privateMethods = getStubs(ASTAccessVisibility.PRIVATE, false);

        if (publicMethods.isEmpty()
	            && protectedMethods.isEmpty()
	            && privateMethods.isEmpty()) {
            text.append(' ');
        } else {
            addMethodDeclarations(publicMethods, protectedMethods, privateMethods, text);
        }

        text.append("};"); //$NON-NLS-1$
        text.append(fLineDelimiter);

        if (fNamespace != null && fNamespace.length() > 0) {
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

        return text.toString();
    }

    private int getInsertionPos(String contents) {
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
        text.append("namespace "); //$NON-NLS-1$
        text.append(fNamespace);
        text.append(fLineDelimiter);
        text.append('{');
        text.append(fLineDelimiter);
        text.append(fLineDelimiter);
    }

    private void endNamespace(StringBuffer text) {
        text.append(fLineDelimiter);
        text.append("};"); //$NON-NLS-1$
        text.append(fLineDelimiter);
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
                IBaseClassInfo baseClass = fBaseClasses[i];
                String baseClassName = baseClass.getType().getQualifiedTypeName().getFullyQualifiedName();
                if (i > 0)
                    text.append(", "); //$NON-NLS-1$
                if (baseClass.getAccess() == ASTAccessVisibility.PRIVATE)
                    text.append("private"); //$NON-NLS-1$
                else if (baseClass.getAccess() == ASTAccessVisibility.PROTECTED)
                    text.append("private"); //$NON-NLS-1$
                else
                    text.append("public"); //$NON-NLS-1$
                text.append(' ');

                if (baseClass.isVirtual())
                    text.append("virtual "); //$NON-NLS-1$

                text.append(baseClassName);
            }
        }
    }

    private void addBaseClassIncludes(ITranslationUnit headerTU, StringBuffer text) {
        IProject project = headerTU.getCProject().getProject();
        IPath projectLocation = project.getLocation();
        IPath headerLocation = headerTU.getResource().getLocation();
        for (int i = 0; i < fBaseClasses.length; ++i) {
            String baseClassFileName = null;
            boolean isSystemIncludePath = false;
            IBaseClassInfo baseClass = fBaseClasses[i];
            ITypeReference ref = baseClass.getType().getResolvedReference();
            if (ref != null) {
                IPath baseClassLocation = ref.getLocation();
                IPath includePath = makeRelativePathToProjectIncludes(baseClassLocation, project);
                if (includePath != null && !projectLocation.isPrefixOf(baseClassLocation)) {
                    isSystemIncludePath = true;
                } else if (projectLocation.isPrefixOf(baseClassLocation)
                        && projectLocation.isPrefixOf(headerLocation)) {
                    includePath = makeRelativePath(baseClassLocation, headerLocation);
                }
                if (includePath == null)
                    includePath = baseClassLocation;
                baseClassFileName = includePath.toString();
            }
            if (baseClassFileName == null) {
                baseClassFileName = NewSourceFileGenerator.generateHeaderFileNameFromClass(baseClass.getType().getName());
            }

            // add the include statement if we are extending a base class
            // and we are not already in the base class header file
            // (enclosing type)
            if (!(headerTU.getElementName().equals(baseClassFileName))) {
                String include = getIncludeString(baseClassFileName, isSystemIncludePath);
                text.append(include);
                text.append(fLineDelimiter);
            }
        }
    }

    public String constructSourceFileContent(ITranslationUnit sourceTU, ITranslationUnit headerTU) {
        //TODO should use code templates
        StringBuffer text = new StringBuffer();

        if (headerTU != null) {
            addHeaderInclude(sourceTU, headerTU, text);
            text.append(fLineDelimiter);
        }
        
        //TODO sort methods (eg constructor always first?)
        List publicMethods = getStubs(ASTAccessVisibility.PUBLIC, true);
        List protectedMethods = getStubs(ASTAccessVisibility.PROTECTED, true);
        List privateMethods = getStubs(ASTAccessVisibility.PRIVATE, true);

        if (publicMethods.isEmpty()
                && protectedMethods.isEmpty()
                && privateMethods.isEmpty()) {
            // no methods
        } else {
            if (fNamespace != null && fNamespace.length() > 0) {
                beginNamespace(text);
            }

            addMethodBodies(publicMethods, protectedMethods, privateMethods, text);

            if (fNamespace != null && fNamespace.length() > 0) {
                endNamespace(text);
            }
        }

        return text.toString();
    }

    private void addHeaderInclude(ITranslationUnit sourceTU, ITranslationUnit headerTU, StringBuffer text) {
        IProject project = headerTU.getCProject().getProject();
        IPath projectLocation = project.getLocation();
        IPath headerLocation = headerTU.getResource().getLocation();
        IPath sourceLocation = sourceTU.getResource().getLocation();

        IPath includePath = makeRelativePathToProjectIncludes(headerLocation, project);
        boolean isSystemIncludePath = false;
        if (includePath != null && !projectLocation.isPrefixOf(headerLocation)) {
            isSystemIncludePath = true;
        } else if (projectLocation.isPrefixOf(headerLocation)
                && projectLocation.isPrefixOf(sourceLocation)) {
            includePath = makeRelativePath(headerLocation, sourceLocation);
        }
        if (includePath == null)
            includePath = headerLocation;

        String include = getIncludeString(includePath.toString(), isSystemIncludePath);
        text.append(include);
        text.append(fLineDelimiter);
    }

    private void addMethodBodies(List publicMethods, List protectedMethods, List privateMethods, StringBuffer text) {
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

    public static IPath makeRelativePathToProjectIncludes(IPath fullPath, IProject project) {
        IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
        if (provider != null) {
            IScannerInfo info = provider.getScannerInformation(project);
            if (info != null) {
                String[] includePaths = info.getIncludePaths();
                IPath relativePath = null;
                int mostSegments = 0;
                for (int i = 0; i < includePaths.length; ++i) {
                    IPath includePath = new Path(includePaths[i]);
                    if (includePath.isPrefixOf(fullPath)) {
                        int segments = includePath.matchingFirstSegments(fullPath);
                        if (segments > mostSegments) {
                            relativePath = fullPath.removeFirstSegments(segments).setDevice(null);
                            mostSegments = segments;
                        }
                    }
                }
                if (relativePath != null)
                    return relativePath;
            }
        }
        return null;
    }

    public static IPath makeRelativePath(IPath path, IPath relativeTo) {
        int segments = relativeTo.matchingFirstSegments(path);
        if (segments > 0) {
            IPath prefix = relativeTo.removeFirstSegments(segments).removeLastSegments(1);
            IPath suffix = path.removeFirstSegments(segments);
            IPath relativePath = new Path(""); //$NON-NLS-1$
            for (int i = 0; i < prefix.segmentCount(); ++i) {
                relativePath = relativePath.append(".." + IPath.SEPARATOR); //$NON-NLS-1$
            }
            return relativePath.append(suffix);
        }
        return null;
    }

    private static String getIncludeString(String fileName, boolean isSystemInclude) {
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
 }