/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *     Warren Paul (Nokia) - 173555
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.CodeGeneration;
import org.eclipse.cdt.utils.PathUtil;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.corext.util.Strings;
import org.eclipse.cdt.internal.formatter.scanner.Scanner;
import org.eclipse.cdt.internal.formatter.scanner.Token;

import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileGenerator;

public class NewClassCodeGenerator {
    private final IPath fHeaderPath;
    private final IPath fSourcePath;
    private final IPath fTestPath;
    private String fClassName;
    private IQualifiedTypeName fNamespace;
    private final IBaseClassInfo[] fBaseClasses;
    private final IMethodStub[] fMethodStubs;
    private ITranslationUnit fCreatedHeaderTU;
    private ITranslationUnit fCreatedSourceTU;
    private ITranslationUnit fCreatedTestTU;
    private ICElement fCreatedClass;
	private String fFullyQualifiedClassName;
	private boolean fForceSourceFileCreation;
    
	/**
	 * When set to <code>true</code>, the source file is created, even if no stubs have
	 * been selected.
	 */
	public void setForceSourceFileCreation(boolean force) {
		fForceSourceFileCreation = force;
	}

	public static class CodeGeneratorException extends CoreException {
        /**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 1L;
		
        public CodeGeneratorException(String message) {
            super(new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.OK, message, null));
        }
        
        public CodeGeneratorException(Throwable e) {
            super(new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.OK, e.getMessage(), e));
        }
	}

	/**
	 * @param headerPath the header file path
	 * @param sourcePath the source file path
	 * @param testPath the test file path, can be {@code null}
	 * @param className the class name
	 * @param namespace the namespace name
	 * @param baseClasses the base classes
	 * @param methodStubs the method stubs
	 */
    public NewClassCodeGenerator(IPath headerPath, IPath sourcePath, IPath testPath,
    		String className, String namespace, IBaseClassInfo[] baseClasses, IMethodStub[] methodStubs) {
        fHeaderPath = headerPath;
        fSourcePath = sourcePath;
        fTestPath = testPath;
        if (className != null && className.length() > 0) {
            fClassName = className;
        }
        if (namespace != null && namespace.length() > 0) {
            fNamespace = new QualifiedTypeName(namespace);
        }
        if (fNamespace != null) {
        	fFullyQualifiedClassName= fNamespace.append(fClassName).getFullyQualifiedName();
        } else {
        	fFullyQualifiedClassName= fClassName;
        }
        fBaseClasses = baseClasses;
        fMethodStubs = methodStubs;
    }

    public ICElement getCreatedClass() {
        return fCreatedClass;
    }

    public ITranslationUnit getCreatedHeaderTU() {
        return fCreatedHeaderTU;
    }
    
    public IFile getCreatedHeaderFile() {
        if (fCreatedHeaderTU != null) {
            return (IFile) fCreatedHeaderTU.getResource();
        }
        return null;
    }

    public ITranslationUnit getCreatedSourceTU() {
        return fCreatedSourceTU;
    }

    public IFile getCreatedSourceFile() {
        if (fCreatedSourceTU != null) {
            return (IFile) fCreatedSourceTU.getResource();
        }
        return null;
    }

    public IFile getCreatedTestFile() {
        if (fCreatedTestTU != null) {
            return (IFile) fCreatedTestTU.getResource();
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
    public ICElement createClass(IProgressMonitor monitor)
    		throws CodeGeneratorException, CoreException, InterruptedException {
        if (monitor == null)
            monitor = new NullProgressMonitor();

        monitor.beginTask(NewClassWizardMessages.NewClassCodeGeneration_createType_mainTask, 400); 

	    ITranslationUnit headerTU = null;
        ITranslationUnit sourceTU = null;
        ITranslationUnit testTU = null;
        ICElement createdClass = null;

        IWorkingCopy headerWorkingCopy = null;
        IWorkingCopy sourceWorkingCopy = null;
        IWorkingCopy testWorkingCopy = null;
        try {
            if (fHeaderPath != null) {
                // Get method stubs
                List<IMethodStub> publicMethods = getStubs(ASTAccessVisibility.PUBLIC, false);
                List<IMethodStub> protectedMethods = getStubs(ASTAccessVisibility.PROTECTED, false);
                List<IMethodStub> privateMethods = getStubs(ASTAccessVisibility.PRIVATE, false);
                
	            IFile headerFile = NewSourceFileGenerator.createHeaderFile(fHeaderPath, true,
	            		new SubProgressMonitor(monitor, 50));
	            if (headerFile != null) {
	                headerTU = (ITranslationUnit) CoreModel.getDefault().create(headerFile);
	                if (headerTU == null) {
	                    throw new CodeGeneratorException("Failed to create " + headerFile); //$NON-NLS-1$
	                }
	
	                // Create a working copy with a new owner
	                headerWorkingCopy = headerTU.getWorkingCopy();
	                // headerWorkingCopy = headerTU.getSharedWorkingCopy(null, CUIPlugin.getDefault().getBufferFactory());

	                String headerContent = constructHeaderFileContent(headerTU, publicMethods,
	                		protectedMethods, privateMethods, headerWorkingCopy.getBuffer().getContents(),
	                		new SubProgressMonitor(monitor, 100));
	                if (headerContent != null) {
		                headerContent= formatSource(headerContent, headerTU);	
	                } else {
	                	headerContent = ""; //$NON-NLS-1$
	                }
	                headerWorkingCopy.getBuffer().setContents(headerContent);

	                if (monitor.isCanceled()) {
	                	throw new InterruptedException();
	                }

	                headerWorkingCopy.reconcile();
	                headerWorkingCopy.commit(true, monitor);
	                monitor.worked(50);

	                createdClass = headerWorkingCopy.getElement(fFullyQualifiedClassName);
	            }
	            fCreatedClass = createdClass;
	            fCreatedHeaderTU = headerTU;
            }

            if (fSourcePath != null) {
                // Get method stubs
                List<IMethodStub> publicMethods = getStubs(ASTAccessVisibility.PUBLIC, true);
                List<IMethodStub> protectedMethods = getStubs(ASTAccessVisibility.PROTECTED, true);
                List<IMethodStub> privateMethods = getStubs(ASTAccessVisibility.PRIVATE, true);
                
                if (!fForceSourceFileCreation && publicMethods.isEmpty() &&
                		protectedMethods.isEmpty() && privateMethods.isEmpty()) {
                    monitor.worked(100);
                } else {
		            IFile sourceFile = NewSourceFileGenerator.createSourceFile(fSourcePath, true,
		            		new SubProgressMonitor(monitor, 50));
		            if (sourceFile != null) {
		                sourceTU = (ITranslationUnit) CoreModel.getDefault().create(sourceFile);
		                if (sourceTU == null) {
		                    throw new CodeGeneratorException("Failed to create " + sourceFile); //$NON-NLS-1$
		                }
		                monitor.worked(50);

		                // Create a working copy with a new owner
		                sourceWorkingCopy = sourceTU.getWorkingCopy();

		                String sourceContent = constructSourceFileContent(sourceTU, headerTU,
		                		publicMethods, protectedMethods, privateMethods,
		                		sourceWorkingCopy.getBuffer().getContents(), new SubProgressMonitor(monitor, 100));
		                if (sourceContent != null) {
			                sourceContent = formatSource(sourceContent, sourceTU);	
		                } else {
		                	sourceContent = ""; //$NON-NLS-1$
		                }
		                sourceWorkingCopy.getBuffer().setContents(sourceContent);

		                if (monitor.isCanceled()) {
		                	throw new InterruptedException();
		                }

		                sourceWorkingCopy.reconcile();
		                sourceWorkingCopy.commit(true, monitor);
		                monitor.worked(50);
		            }
		
		            fCreatedSourceTU = sourceTU;
                }
            }

            if (fTestPath != null) {
	            IFile testFile = NewSourceFileGenerator.createTestFile(fTestPath, true,
	            		new SubProgressMonitor(monitor, 50));
	            if (testFile != null) {
	                testTU = (ITranslationUnit) CoreModel.getDefault().create(testFile);
	                if (testTU == null) {
	                    throw new CodeGeneratorException("Failed to create " + testFile); //$NON-NLS-1$
	                }
	                monitor.worked(50);

	                // Create a working copy with a new owner
	                testWorkingCopy = testTU.getWorkingCopy();

	                String testContent = constructTestFileContent(testTU, headerTU,
	                		testWorkingCopy.getBuffer().getContents(), new SubProgressMonitor(monitor, 100));
	                testContent= formatSource(testContent, testTU);
	                testWorkingCopy.getBuffer().setContents(testContent);

	                if (monitor.isCanceled()) {
	                	throw new InterruptedException();
	                }

	                testWorkingCopy.reconcile();
	                testWorkingCopy.commit(true, monitor);
	                monitor.worked(50);
	            }
	
	            fCreatedTestTU = testTU;
            }
        } catch (CodeGeneratorException e) {
        	deleteAllCreatedFiles();
        } finally {
            if (headerWorkingCopy != null) {
                headerWorkingCopy.destroy();
            }
            if (sourceWorkingCopy != null) {
                sourceWorkingCopy.destroy();
            }
            if (testWorkingCopy != null) {
                testWorkingCopy.destroy();
            }
            monitor.done();
        }

        return fCreatedClass;
    }

	private void deleteAllCreatedFiles() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IPath path : new IPath[] { fHeaderPath, fSourcePath, fSourcePath }) {
			if (path != null) {
				try {
					IFile file = root.getFile(path);
					file.delete(true, null);
				} catch (CoreException e) {
				}
			}
		}
	}

    /**
     * Format given source content according to the project's code style options.
     * 
	 * @param content  the source content
	 * @param tu  the translation unit
	 * @return the formatted source text or the original if the text could not be formatted successfully
     * @throws CModelException 
	 */
	private String formatSource(String content, ITranslationUnit tu) throws CModelException {
        String lineDelimiter= StubUtility.getLineDelimiterUsed(tu);
        Map<String, Object> options = new HashMap<String, Object>(tu.getCProject().getOptions(true));
        options.put(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT, tu);
		TextEdit edit= CodeFormatterUtil.format(CodeFormatter.K_TRANSLATION_UNIT, content, 0,
				lineDelimiter, options);
        if (edit != null) {
        	IDocument doc= new Document(content);
        	try {
				edit.apply(doc);
            	content= doc.get();
			} catch (MalformedTreeException e) {
				CUIPlugin.log(e);
			} catch (BadLocationException e) {
				CUIPlugin.log(e);
			}
        }
        return content;
	}

	public String constructHeaderFileContent(ITranslationUnit headerTU, List<IMethodStub> publicMethods,
			List<IMethodStub> protectedMethods, List<IMethodStub> privateMethods, String oldContents,
			IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(NewClassWizardMessages.NewClassCodeGeneration_createType_task_header, 100); 
        
        String lineDelimiter= StubUtility.getLineDelimiterUsed(headerTU);

    	String namespaceBegin = fNamespace == null ?
    			null : constructNamespaceBegin(headerTU, lineDelimiter);
    	String namespaceEnd = fNamespace == null ?
    			null : constructNamespaceEnd(headerTU, lineDelimiter);
    	String classDefinition = constructClassDefinition(headerTU, publicMethods, protectedMethods,
        		privateMethods, lineDelimiter);

        String includes = null;
        if (fBaseClasses != null && fBaseClasses.length > 0) {
            includes = constructBaseClassIncludes(headerTU, lineDelimiter,
            		new SubProgressMonitor(monitor, 50));
        }

        if (oldContents != null) {
        	if (oldContents.length() == 0) {
        		oldContents = null;
        	} else if (!oldContents.endsWith(lineDelimiter)) {
        		oldContents += lineDelimiter;
        	}
        }

        String fileContent;
        if (oldContents != null) {
            int appendFirstCharPos = -1;
            StringBuilder text = new StringBuilder();
	        int insertionPos = getClassDefInsertionPos(oldContents);
	        if (insertionPos == -1) {
		        text.append(oldContents);
	        } else {
	            // Skip over whitespace
	            int prependLastCharPos = insertionPos - 1;
	            while (prependLastCharPos >= 0 && Character.isWhitespace(oldContents.charAt(prependLastCharPos))) {
	                --prependLastCharPos;
	            }
	            if (prependLastCharPos >= 0) {
	                text.append(oldContents.substring(0, prependLastCharPos + 1));
	            }
	            appendFirstCharPos = prependLastCharPos + 1;
	        }
            text.append(lineDelimiter);
            
            // Insert a blank line before class definition
            text.append(lineDelimiter);
            if (namespaceBegin != null) {
            	text.append(namespaceBegin);
            	text.append(lineDelimiter);
            	text.append(lineDelimiter);
            }
            text.append(classDefinition);
            if (namespaceEnd != null) {
            	if (!classDefinition.endsWith(lineDelimiter))
            		text.append(lineDelimiter);
            	text.append(lineDelimiter);
            	text.append(namespaceEnd);
            }
            if (appendFirstCharPos != -1) {
                // Insert a blank line after class definition
                text.append(lineDelimiter);
                
                // Skip over any extra whitespace
                int len = oldContents.length();
                while (appendFirstCharPos < len &&
                		Character.isWhitespace(oldContents.charAt(appendFirstCharPos))) {
                    ++appendFirstCharPos;
                }
                if (appendFirstCharPos < len) {
                    text.append(oldContents.substring(appendFirstCharPos));
                }
            }
            if (Strings.endsWith(text, lineDelimiter))
            	text.append(lineDelimiter);
            fileContent= text.toString();
        } else {
        	String namespaceName = fNamespace == null ?
        			null : fNamespace.getFullyQualifiedName();
        	String classComment = getClassComment(headerTU, lineDelimiter);
    		fileContent= CodeGeneration.getHeaderFileContent(headerTU, includes, namespaceBegin,
        			namespaceEnd, namespaceName, classComment, classDefinition, fClassName,
        			lineDelimiter);
        }

        monitor.done();
        return fileContent;
    }

	public String constructNamespaceBegin(ITranslationUnit tu, String lineDelimiter) throws CoreException {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < fNamespace.segmentCount(); i++) {
		    String namespaceName = fNamespace.segment(i);
		    if (i > 0) {
		    	text.append(lineDelimiter);
		    }
		    text.append(CodeGeneration.getNamespaceBeginContent(tu, namespaceName, lineDelimiter));
		}
		return text.toString();
	}

	public String constructNamespaceEnd(ITranslationUnit tu, String lineDelimiter) throws CoreException {
		StringBuilder text = new StringBuilder();
		for (int i = fNamespace.segmentCount(); --i >= 0;) {
		    String namespaceName = fNamespace.segment(i);
		    text.append(CodeGeneration.getNamespaceEndContent(tu, namespaceName, lineDelimiter));
		    if (i > 0) {
		    	text.append(lineDelimiter);
		    }
		}
		return text.toString();
	}

	public String constructClassDefinition(ITranslationUnit tu, List<IMethodStub> publicMethods,
			List<IMethodStub> protectedMethods,	List<IMethodStub> privateMethods,
			String lineDelimiter) throws CoreException {
        StringBuilder code = new StringBuilder();
        String comment = getClassComment(tu, lineDelimiter);
        if (comment != null) {
        	code.append(comment);
        	code.append(lineDelimiter);
        }
        code.append("class "); //$NON-NLS-1$
        code.append(fClassName);
        code.append(constructBaseClassInheritance());
        code.append(" {"); //$NON-NLS-1$
    	code.append(lineDelimiter);
		String body = constructMethodDeclarations(tu, publicMethods, protectedMethods,
				privateMethods, lineDelimiter);
        body = CodeGeneration.getClassBodyContent(tu, fClassName, body, lineDelimiter);
        if (body != null) {
	        code.append(body);
	        if (!body.endsWith(lineDelimiter)) {
	        	code.append(lineDelimiter);
	        }
        }
        code.append("};"); //$NON-NLS-1$
        return removeRedundantVisibilityLabels(code.toString());
	}

    private String removeRedundantVisibilityLabels(String code) {
		Scanner scanner = new Scanner();
		scanner.setSource(code.toCharArray());
		scanner.resetTo(0, code.length());
		IDocument doc = new Document(code);
		try {
			MultiTextEdit edit = new MultiTextEdit();
			int sectionType = Token.tBADCHAR;
			int previousTokenType = Token.tBADCHAR;
			int previousTokenOffset = -1;
			Token token;
			while ((token = scanner.nextToken()) != null) {
				if (token.type == Token.tCOLON) {
					switch (previousTokenType) {
					case Token.t_public:
					case Token.t_protected:
					case Token.t_private:
						if (previousTokenType == sectionType) {
							IRegion region1 = doc.getLineInformationOfOffset(previousTokenOffset);
							IRegion region2 = doc.getLineInformationOfOffset(token.offset);
							edit.addChild(new DeleteEdit(region1.getOffset(), region2.getOffset() + region2.getLength() - region1.getOffset()));
						}
						sectionType = previousTokenType;
					}
				}
				previousTokenType = token.type;
				previousTokenOffset = token.offset;
			}
			edit.apply(doc, 0);
		} catch (MalformedTreeException e) {
			CUIPlugin.log(e);
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
		return doc.get();
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
    
	/**
	 * Retrieve the class comment. Returns the content of the 'type comment' template.
	 * 
	 * @param tu the translation unit
	 * @param lineDelimiter the line delimiter to use
	 * @return the type comment or <code>null</code> if a type comment 
	 * is not desired
     *
     * @since 5.0
	 */		
	private String getClassComment(ITranslationUnit tu, String lineDelimiter) {
		if (isAddComments(tu)) {
			try {
				String fqName= fFullyQualifiedClassName;
				String comment= CodeGeneration.getClassComment(tu, fqName, lineDelimiter);
				if (comment != null && isValidComment(comment)) {
					return comment;
				}
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
		return null;
	}

	private boolean isValidComment(String template) {
		// TODO verify comment
		return true;
	}
	
	/**
	 * Returns if comments are added. The settings as specified in the preferences is used.
	 * 
	 * @param tu
	 * @return Returns <code>true</code> if comments can be added
	 * @since 5.0
	 */
	public boolean isAddComments(ITranslationUnit tu) {
		return StubUtility.doAddComments(tu.getCProject()); 
	}
			
    private String constructMethodDeclarations(ITranslationUnit tu, List<IMethodStub> publicMethods,
    		List<IMethodStub> protectedMethods, List<IMethodStub> privateMethods, String lineDelimiter)
    		throws CoreException {
    	StringBuilder text = new StringBuilder();
        if (!publicMethods.isEmpty()) {
            text.append("public:"); //$NON-NLS-1$
            text.append(lineDelimiter);
            for (IMethodStub stub : publicMethods) {
                String code = stub.createMethodDeclaration(tu, fClassName, fBaseClasses, lineDelimiter);
                text.append('\t');
                text.append(code);
                text.append(lineDelimiter);
            }
        }

        if (!protectedMethods.isEmpty()) {
        	if (text.length() > 0) {
                text.append(lineDelimiter);
        	}
            text.append("protected:"); //$NON-NLS-1$
            text.append(lineDelimiter);
            for (IMethodStub stub : protectedMethods) {
                String code = stub.createMethodDeclaration(tu, fClassName, fBaseClasses, lineDelimiter);
                text.append('\t');
                text.append(code);
                text.append(lineDelimiter);
            }
        }

        if (!privateMethods.isEmpty()) {
        	if (text.length() > 0) {
                text.append(lineDelimiter);
        	}
            text.append("private:"); //$NON-NLS-1$
            text.append(lineDelimiter);
            for (IMethodStub stub : privateMethods) {
                String code = stub.createMethodDeclaration(tu, fClassName, fBaseClasses, lineDelimiter);
                text.append('\t');
                text.append(code);
                text.append(lineDelimiter);
            }
        }

        return text.toString();
    }

    private List<IMethodStub> getStubs(ASTAccessVisibility access, boolean skipInline) {
        List<IMethodStub> list = new ArrayList<IMethodStub>();
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

    private String constructBaseClassInheritance() {
        if (fBaseClasses == null || fBaseClasses.length == 0) {
        	return ""; //$NON-NLS-1$
        }
    	StringBuilder text = new StringBuilder();
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
        return text.toString();
    }

    private String constructBaseClassIncludes(ITranslationUnit headerTU, String lineDelimiter,
    		IProgressMonitor monitor) throws CodeGeneratorException {
        monitor.beginTask(NewClassWizardMessages.NewClassCodeGeneration_createType_task_header_includePaths, 100); 
        
        ICProject cProject = headerTU.getCProject();
        IProject project = cProject.getProject();
        IPath projectLocation = new Path(project.getLocationURI().getPath());
        IPath headerLocation = new Path(headerTU.getResource().getLocationURI().getPath());
        
        List<IPath> includePaths = getIncludePaths(headerTU);
        List<IPath> baseClassPaths = getBaseClassPaths(verifyBaseClasses());
        
	    // Add the missing include paths to the project
        if (createIncludePaths()) {
	        List<IPath> newIncludePaths = getMissingIncludePaths(projectLocation, includePaths, baseClassPaths);
	        if (!newIncludePaths.isEmpty()) {
			    addIncludePaths(cProject, newIncludePaths, monitor);
	        }
        }

        List<IPath> systemIncludes = new ArrayList<IPath>();
        List<IPath> localIncludes = new ArrayList<IPath>();
        
        // Sort the include paths into system and local
        for (IPath baseClassLocation : baseClassPaths) {
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
            
            // Make the new #include path in the source file only point to a relative file
            // (i.e. now that the path has been included above in the project)
            includePath = includePath.removeFirstSegments(includePath.segmentCount() - 1).setDevice(null);
            
            if (isSystemIncludePath)
                systemIncludes.add(includePath);
            else
                localIncludes.add(includePath);
        }
        
    	StringBuilder text = new StringBuilder();
        // Write the system include paths, e.g. #include <header.h>
        for (IPath includePath : systemIncludes) {
            if (!(headerTU.getElementName().equals(includePath.toString()))) {
			    String include = getIncludeString(includePath.toString(), true);
			    text.append(include);
			    text.append(lineDelimiter);
			}
        }
        
        // Write the local include paths, e.g. #include "header.h"
        for (IPath includePath : localIncludes) {
            if (!(headerTU.getElementName().equals(includePath.toString()))) {
			    String include = getIncludeString(includePath.toString(), false);
			    text.append(include);
			    text.append(lineDelimiter);
			}
        }
        
        monitor.done();
        return text.toString();
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

    private void addIncludePaths(ICProject cProject, List<IPath> newIncludePaths, IProgressMonitor monitor) throws CodeGeneratorException {
        monitor.beginTask(NewClassWizardMessages.NewClassCodeGeneration_createType_task_header_addIncludePaths, 100); 

        //TODO prefs option whether to add to project or parent source folder?
        IPath addToResourcePath = cProject.getPath();
        try {
            List<IPathEntry> pathEntryList = new ArrayList<IPathEntry>();
            List<IPathEntry> checkEntryList = new ArrayList<IPathEntry>();
            
            IPathEntry[] checkEntries = cProject.getResolvedPathEntries();
            IPathEntry[] pathEntries = cProject.getRawPathEntries();
            if (pathEntries != null) {
                for (int i = 0; i < pathEntries.length; ++i) {
                	pathEntryList.add(pathEntries[i]);
                }
            }
            for (IPathEntry checkEntrie : checkEntries) {
            	if (checkEntrie instanceof IIncludeEntry)
            		checkEntryList.add(checkEntrie);
            }
            
            for (IPath folderToAdd : newIncludePaths) {
                // do not add any #includes that are local to the project
                if (cProject.getPath().segment(0).equals(folderToAdd.segment(0)))
                	continue;
                
                ICProject includeProject = toCProject(PathUtil.getEnclosingProject(folderToAdd));
                if (includeProject != null) {
                    // Make sure that the include is made the same way that build properties for
                	// projects makes them, so .contains below is a valid check
                    IIncludeEntry entry = CoreModel.newIncludeEntry(addToResourcePath, null, new Path(includeProject.getProject().getLocationURI().getPath()), true);
                    
                    if (!checkEntryList.contains(entry)) // if the path already exists in the #includes then don't add it
                    	pathEntryList.add(entry);
                }
            }
            pathEntries = pathEntryList.toArray(new IPathEntry[pathEntryList.size()]);
            cProject.setRawPathEntries(pathEntries, new SubProgressMonitor(monitor, 80));
        } catch (CModelException e) {
            throw new CodeGeneratorException(e);
        }
        monitor.done();
    }

	private ICProject toCProject(IProject enclosingProject) {
		if (enclosingProject != null)
			return CoreModel.getDefault().create(enclosingProject);
		return null;
	}

	private List<IPath> getMissingIncludePaths(IPath projectLocation, List<IPath> includePaths, List<IPath> baseClassPaths) {
        // check for missing include paths
        List<IPath> newIncludePaths = new ArrayList<IPath>();
        for (IPath baseClassLocation : baseClassPaths) {
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
            for (IPath newFolder : newIncludePaths) {
                if (newFolder.isPrefixOf(folderToAdd)) {
	                folderToAdd = null;
	                break;
	            }
            }

            if (folderToAdd != null) {
	            // search include paths
                boolean foundPath = false;
	            for (IPath includePath : includePaths) {
	                if (includePath.isPrefixOf(folderToAdd) || includePath.equals(folderToAdd)) {
		                foundPath = true;
		                break;
		            }
	            }
	            if (!foundPath) {
                    // remove any children of this folder
                    for (Iterator<IPath> newIter = newIncludePaths.iterator(); newIter.hasNext(); ) {
                        IPath newFolder = newIter.next();
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

    private List<IPath> getIncludePaths(ITranslationUnit headerTU) throws CodeGeneratorException {
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
                    List<IPath> list = new ArrayList<IPath>();
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
    
    private List<IPath> getBaseClassPaths(boolean verifyLocation) throws CodeGeneratorException {
        List<IPath> list = new ArrayList<IPath>();
	    for (int i = 0; i < fBaseClasses.length; ++i) {
	        IBaseClassInfo baseClass = fBaseClasses[i];
	        ITypeReference ref = baseClass.getType().getResolvedReference();
	        IPath baseClassLocation = null;
	        if (ref != null) {
	            baseClassLocation = ref.getLocation();
            }
            
	        if (baseClassLocation == null) {
                if (verifyLocation) {
                    throw new CodeGeneratorException("Could not find base class " + baseClass.toString()); //$NON-NLS-1$
                }
	        } else if (!list.contains(baseClassLocation)) {
                list.add(baseClassLocation);
            }
	    }
	    return list;
    }
    
    public String constructSourceFileContent(ITranslationUnit sourceTU, ITranslationUnit headerTU,
    		List<IMethodStub> publicMethods, List<IMethodStub> protectedMethods,
    		List<IMethodStub> privateMethods, String oldContents, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(NewClassWizardMessages.NewClassCodeGeneration_createType_task_source, 150); 
        
        String lineDelimiter= StubUtility.getLineDelimiterUsed(sourceTU);
        String includeString = null;
        if (headerTU != null) {
            includeString = getHeaderIncludeString(sourceTU, headerTU, new SubProgressMonitor(monitor, 50));
	        if (includeString != null) {
	            // Check if file already has the include.
	            if (oldContents != null && hasInclude(oldContents, includeString)) {
	                // Don't bother to add it.
	                includeString = null;
	            }
	        }
        }

        String methodBodies = null;
        if (!publicMethods.isEmpty() || !protectedMethods.isEmpty() || !privateMethods.isEmpty()) {
            // TODO sort methods (e.g. constructor always first?)
            methodBodies = constructMethodBodies(sourceTU, publicMethods, protectedMethods,
            		privateMethods, lineDelimiter, new SubProgressMonitor(monitor, 50));
        }

    	String namespaceBegin = fNamespace == null ?
    			null : constructNamespaceBegin(sourceTU, lineDelimiter);
    	String namespaceEnd = fNamespace == null ?
    			null : constructNamespaceEnd(sourceTU, lineDelimiter);

        if (oldContents != null) {
        	if (oldContents.length() == 0) {
        		oldContents = null;
        	} else if (!oldContents.endsWith(lineDelimiter)) {
        		oldContents += lineDelimiter;
        	}
        }

    	String fileContent;
        if (oldContents != null) {
            StringBuilder text = new StringBuilder();

        	if (includeString != null) {
    	        int insertionPos = getIncludeInsertionPos(oldContents);
    	        if (insertionPos == -1) {
    		        text.append(oldContents);
                    text.append(lineDelimiter);
                    text.append(includeString);
                    text.append(lineDelimiter);
    	        } else {
    	            text.append(oldContents.substring(0, insertionPos));
                    text.append(includeString);
                    text.append(lineDelimiter);
                    text.append(oldContents.substring(insertionPos));
    	        }
        	} else {
        		text.append(oldContents);
        	}
            // Add a blank line
            text.append(lineDelimiter);

            if (methodBodies != null) {
	            if (namespaceBegin != null) {
	            	text.append(namespaceBegin);
	            	text.append(lineDelimiter);
	            	text.append(lineDelimiter);
	            }
	            text.append(methodBodies);
	            if (namespaceEnd != null) {
	            	if (!methodBodies.endsWith(lineDelimiter))
		            	text.append(lineDelimiter);
	            	text.append(lineDelimiter);
	            	text.append(namespaceEnd);
	            }
            }
            
            if (Strings.endsWith(text, lineDelimiter))
            	text.append(lineDelimiter);
        	fileContent = text.toString();
        } else {
        	String namespaceName = fNamespace == null ?
        			null : fNamespace.getFullyQualifiedName();
        	fileContent= CodeGeneration.getBodyFileContent(sourceTU, includeString, namespaceBegin,
        			namespaceEnd, namespaceName, null, methodBodies, fClassName, lineDelimiter);
        }
        monitor.done();
        return fileContent;
    }
    
    public String constructTestFileContent(ITranslationUnit testTU, ITranslationUnit headerTU,
    		String oldContents, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(NewClassWizardMessages.NewClassCodeGeneration_createType_task_source, 150); 
        
        String lineDelimiter= StubUtility.getLineDelimiterUsed(testTU);

        String includeString = null;
        if (headerTU != null) {
            includeString = getHeaderIncludeString(testTU, headerTU, new SubProgressMonitor(monitor, 50));
	        if (includeString != null) {
	            // Check if file already has the include.
	            if (oldContents != null && hasInclude(oldContents, includeString)) {
	                // Don't bother to add it.
	                includeString = null;
	            }
	        }
        }
        
        if (oldContents != null) {
        	if (oldContents.length() == 0) {
        		oldContents = null;
        	} else if (!oldContents.endsWith(lineDelimiter)) {
        		oldContents += lineDelimiter;
        	}
        }

        String fileContent;
        if (oldContents != null) {
            StringBuilder text = new StringBuilder();

        	if (includeString != null) {
    	        int insertionPos = getIncludeInsertionPos(oldContents);
    	        if (insertionPos == -1) {
    		        text.append(oldContents);
                    text.append(lineDelimiter);
                    text.append(includeString);
                    text.append(lineDelimiter);
    	        } else {
    	            text.append(oldContents.substring(0, insertionPos));
                    text.append(includeString);
                    text.append(lineDelimiter);
                    text.append(oldContents.substring(insertionPos));
    	        }
        	} else {
        		text.append(oldContents);
        	}

        	if (Strings.endsWith(text, lineDelimiter))
            	text.append(lineDelimiter);
        	fileContent = text.toString();
        } else {
        	String namespaceBegin = fNamespace == null ?
        			null : constructNamespaceBegin(testTU, lineDelimiter);
        	String namespaceEnd = fNamespace == null ?
        			null : constructNamespaceEnd(testTU, lineDelimiter);
        	String namespaceName = fNamespace == null ?
        			null : fNamespace.getFullyQualifiedName();
        	fileContent= CodeGeneration.getTestFileContent(testTU, includeString, namespaceBegin,
        			namespaceEnd, namespaceName, null, fClassName, lineDelimiter);
        }
        monitor.done();
        return fileContent;
    }
    
    private String getHeaderIncludeString(ITranslationUnit sourceTU, ITranslationUnit headerTU,
    		IProgressMonitor monitor) {
        IProject project = headerTU.getCProject().getProject();
        IPath projectLocation = new Path(project.getLocationURI().getPath());
        IPath headerLocation = new Path(headerTU.getResource().getLocationURI().getPath());
        IPath sourceLocation = new Path(sourceTU.getResource().getLocationURI().getPath());

        IPath includePath = PathUtil.makeRelativePathToProjectIncludes(headerLocation, project);
        boolean isSystemIncludePath = false;
        if (headerTU.getResource() == null && includePath != null
        		&& !projectLocation.isPrefixOf(headerLocation)) {
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
            // Find the end of line
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

    private String constructMethodBodies(ITranslationUnit tu, List<IMethodStub> publicMethods,
    		List<IMethodStub> protectedMethods, List<IMethodStub> privateMethods, String lineDelimiter,
    		IProgressMonitor monitor) throws CoreException {
    	StringBuilder text = new StringBuilder();
        if (!publicMethods.isEmpty()) {
            for (Iterator<IMethodStub> i = publicMethods.iterator(); i.hasNext();) {
                IMethodStub stub = i.next();
                String code = stub.createMethodImplementation(tu, fClassName, fBaseClasses, lineDelimiter);
                text.append(code);
                text.append(lineDelimiter);
                if (i.hasNext())
                    text.append(lineDelimiter);
            }
        }

        if (!protectedMethods.isEmpty()) {
            for (Iterator<IMethodStub> i = protectedMethods.iterator(); i.hasNext();) {
                IMethodStub stub = i.next();
                String code = stub.createMethodImplementation(tu, fClassName, fBaseClasses, lineDelimiter);
                text.append(code);
                text.append(lineDelimiter);
                if (i.hasNext())
                    text.append(lineDelimiter);
            }
        }

        if (!privateMethods.isEmpty()) {
            for (Iterator<IMethodStub> i = privateMethods.iterator(); i.hasNext();) {
                IMethodStub stub = i.next();
                String code = stub.createMethodImplementation(tu, fClassName, fBaseClasses, lineDelimiter);
                text.append(code);
                text.append(lineDelimiter);
                if (i.hasNext())
                    text.append(lineDelimiter);
            }
        }
        return text.toString();
    }

    private String getIncludeString(String fileName, boolean isSystemInclude) {
        StringBuilder buf = new StringBuilder();
        buf.append("#include "); //$NON-NLS-1$
        if (isSystemInclude)
            buf.append('<'); 
        else
            buf.append('\"'); 
        buf.append(fileName);
        if (isSystemInclude)
            buf.append('>'); 
        else
            buf.append('\"'); 
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

