/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.internal.ui.codemanipulation.AddIncludesOperation;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

// TODO this is a big TODO. 
public class AddIncludeOnSelectionAction extends Action implements IUpdate {
		
	private ITextEditor fEditor;
	private IRequiredInclude[] fRequiredIncludes;
	private String[] fUsings;

	class RequiredIncludes implements IRequiredInclude {
		String name;
		boolean isStandard;

		RequiredIncludes(String n) {
			name = n;
			isStandard = true;
		}

		RequiredIncludes(String n, boolean isStandard) {
			name = n;
			this.isStandard = isStandard;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.IRequiredInclude#getIncludeName()
		 */
		public String getIncludeName() {
			return name;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.IRequiredInclude#isStandard()
		 */
		public boolean isStandard() {
			return isStandard;
		}

	}

	public AddIncludeOnSelectionAction(ITextEditor editor) {	
		super(CEditorMessages.getString("AddIncludeOnSelection.label"));		 //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("AddIncludeOnSelection.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
		
		fEditor= editor;
		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.ADD_INCLUDE_ON_SELECTION_ACTION);
	}
	
	private void addInclude(ITranslationUnit tu) {
		AddIncludesOperation op= new AddIncludesOperation(tu, fRequiredIncludes, fUsings, false);
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(
				PlatformUI.getWorkbench().getProgressService(),
				new WorkbenchRunnableAdapter(op), op.getScheduleRule());
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message1"), null); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}

	}
	
	protected ITranslationUnit getTranslationUnit () {
		ITranslationUnit unit = null;
		if (fEditor != null) {
			IEditorInput editorInput= fEditor.getEditorInput();
			unit = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		}
		return unit;
	}
	
	private Shell getShell() {
		return fEditor.getSite().getShell();
	}

	public void run() {
		ITranslationUnit tu= getTranslationUnit();
		if (tu != null) {
			extractIncludes(fEditor);
			addInclude(tu);
		}
		fUsings = null;
		fRequiredIncludes = null;
	}
	
	/**
	 * To be used by ElementListSelectionDialog for user to choose which declarations/
	 * definitions for "add include" when there are more than one to choose from.  
	 */
	private class DisplayName extends Object 
	{
		private PDOMName name;
		
		public DisplayName(PDOMName name) {
			this.name = name;
		}

		public String toString()
		{			
			try {				
				PDOMBinding binding = (PDOMBinding) name.resolveBinding();
				if (binding != null)
				{
					return getBindingQualifiedName(binding) + " - " + name.getFileName(); //$NON-NLS-1$
				}
				else
					return null;
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return null;
			}
		}
		
		public PDOMName getPDOMName()
		{
			return name;
		}
		
	}
	

	/**
	 * Extract the includes for the given selection.  This can be both used to perform
	 * the work as well as being invoked when there is a change.  The actual results 
	 * can and should be cached as the lookup process could be potentially costly.
	 */
	private void extractIncludes(ITextEditor editor) {
		if (editor == null) {
			return;
		}
		
		ISelection s= editor.getSelectionProvider().getSelection();
		IDocument doc= editor.getDocumentProvider().getDocument(editor.getEditorInput());

		if (s.isEmpty() || !(s instanceof ITextSelection) || doc == null) {
			return;
		}
	
		ITextSelection selection= (ITextSelection) s;
		try {
			IRegion region = CWordFinder.findWord(doc, selection.getOffset());
			if (region == null || region.getLength() == 0) {
				return;
			}
			String name = doc.get(region.getOffset(), region.getLength());
			if (name.length() == 0) {
				return;
			}

			// Try contribution from plugins.
			IFunctionSummary fs = findContribution(name);
			if (fs != null) {
				fRequiredIncludes = fs.getIncludes();
				String ns = fs.getNamespace();
				if (ns != null && ns.length() > 0) {
					fUsings = new String[] {fs.getNamespace()};
				}
			}

			IPDOMManager pdomManager = CCorePlugin.getPDOMManager();
			try {			
				ITranslationUnit unit = getTranslationUnit();
				//get all referenced projects
				if (unit != null)
				{
					ICProject cProj = unit.getCProject();
					if (cProj != null)
					{
						IProject proj = cProj.getProject();
						if (proj != null)
						{
							IProjectDescription projectDescription = proj.getDescription();
							if (projectDescription != null)
							{
								IProject[] projects = projectDescription.getReferencedProjects();
								List cProjectsToSearch = new ArrayList();
								//get all the ICProjects for the referenced projects
								for(int i = 0; i < projects.length; i++)
								{
									IProject project = projects[i];
									try {
										ICProject[] cProjects = CoreModel.getDefault().getCModel().getCProjects();
										if (cProjects != null) {
											for (int j = 0; j < cProjects.length; j++) {
												ICProject cProject = cProjects[j];
												if (project.equals(cProjects[j].getProject()))
													cProjectsToSearch.add(cProject);
											}
										}
									} catch (CModelException e) {
									}
								}
								
								cProjectsToSearch.add(cProj); //current project
								Pattern pattern = Pattern.compile(name);
								List pdomBindings = new ArrayList();
								//search the projects and get name matching bindings
								for (int n = 0; n < cProjectsToSearch.size(); n++)
								{							
									PDOM pdom = (PDOM)pdomManager.getPDOM((ICProject) cProjectsToSearch.get(n));
									IBinding[] bindings = pdom.findBindings(pattern, new NullProgressMonitor());
									
									for (int i = 0; i < bindings.length; ++i) {
										PDOMBinding binding = (PDOMBinding)bindings[i];
										PDOMBinding pdomBinding = pdom.getLinkage(getTranslationUnit().getLanguage()).adaptBinding(binding);
										pdomBindings.add(pdomBinding);
									}
								}
								
								List pdomNames = new ArrayList();
								
								//get all the declarations/definitions of the pdomBindings found
								for (int i = 0; i < pdomBindings.size(); ++i)
								{
									PDOMBinding pdomBinding = (PDOMBinding) pdomBindings.get(i);
									
									if (pdomBinding instanceof PDOMMemberOwner //class or struct
											|| pdomBinding instanceof IEnumeration)
									{
										PDOMName currentDef = pdomBinding.getFirstDefinition();
										while(currentDef != null) //get all the definitions of the file to include
										{
											pdomNames.add(new DisplayName(currentDef));
											currentDef = currentDef.getNextInBinding();
										}
									}
									if (pdomBinding instanceof ITypedef || pdomBinding instanceof IFunction)
									{
										PDOMName currentDec = pdomBinding.getFirstDeclaration();
										while(currentDec != null) //get all the declarations of the file to include
										{
											pdomNames.add(new DisplayName(currentDec));
											currentDec = currentDec.getNextInBinding();
										}
									}
								}				
								
								if (pdomNames.size() > 1)
								{				
									ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_TYPE_ONLY));
									dialog.setElements(pdomNames.toArray());
									dialog.setTitle(CEditorMessages.getString("AddIncludeOnSelection.label")); //$NON-NLS-1$
									dialog.setMessage(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
									if (dialog.open() == Window.OK) {
										//get selection
										Object[] selects = dialog.getResult();
										
										fRequiredIncludes = new IRequiredInclude[selects.length];
										List usings = new ArrayList(selects.length);
										for (int i = 0; i < fRequiredIncludes.length; i++) {
											IRequiredInclude include = getRequiredInclude(((DisplayName)selects[i]).getPDOMName().getFileName(), getTranslationUnit());
											if (include != null) {
												fRequiredIncludes[i] = include;
												PDOMBinding pdomBinding = ((PDOMBinding)(((DisplayName)selects[i]).getPDOMName().resolveBinding()));
												if (pdomBinding instanceof ICPPBinding)
												{
													//find the enclosing namespace, if there's one
													IQualifiedTypeName qualifiedName = new QualifiedTypeName(getBindingQualifiedName(pdomBinding));
													String qualifiedEnclosingName = (new QualifiedTypeName(qualifiedName.getEnclosingNames())).getFullyQualifiedName();
													if (!qualifiedEnclosingName.equals(""))  //$NON-NLS-1$
														usings.add(qualifiedEnclosingName);									
												}
											} 
										}
										if(usings.size() > 0)
										{
											fUsings = new String[usings.size()];
											for (int i = 0; i < usings.size(); i++)
											{
												fUsings[i] = (String) usings.get(i);
											}
										}						
									}
								}
								else if (pdomNames.size() == 1)
								{					
									String fileName = ((DisplayName)pdomNames.get(0)).getPDOMName().getFileName();
									fRequiredIncludes = new IRequiredInclude[] {getRequiredInclude(fileName, getTranslationUnit())};
									PDOMBinding pdomBinding = (PDOMBinding) ((DisplayName)pdomNames.get(0)).getPDOMName().resolveBinding();
									
									if (pdomBinding instanceof ICPPBinding)
									{
										//find the enclosing namespace, if there's one
										IQualifiedTypeName qualifiedName = new QualifiedTypeName(getBindingQualifiedName(pdomBinding));
										String qualifiedEnclosingName = new QualifiedTypeName(qualifiedName.getEnclosingNames()).getFullyQualifiedName();
										if (!qualifiedEnclosingName.equals("")) //$NON-NLS-1$
											fUsings = new String[] {qualifiedEnclosingName};						
									}
								}
							}
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
			
			// Try the type caching.
			if (fRequiredIncludes == null && fUsings == null) {
			}

			// Do a full search
			if (fRequiredIncludes == null && fUsings == null) {
			}
		} catch (BadLocationException e) {
			MessageDialog.openError(getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message3"), CEditorMessages.getString("AddIncludeOnSelection.error.message4") + e.getMessage()); //$NON-NLS-2$ //$NON-NLS-1$
		}
		
	}

	private IFunctionSummary findContribution (final String name) {
		final IFunctionSummary[] fs = new IFunctionSummary[1];
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ICHelpInvocationContext context = new ICHelpInvocationContext() {

					public IProject getProject() {
						ITranslationUnit u = getTranslationUnit();
						if (u != null) {
							return u.getCProject().getProject();
						}
						return null;
					}

					public ITranslationUnit getTranslationUnit() {
						return AddIncludeOnSelectionAction.this.getTranslationUnit();
					}	
				};

				fs[0] = CHelpProviderManager.getDefault().getFunctionInfo(context, name);
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(op);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message1"), null); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}
		return fs[0];
	}

	public void setContentEditor(ITextEditor editor) {
		fEditor= editor;
	}
	
	public void update() {
		setEnabled(true);
	}
	
	/**
	 * Returns the RequiredInclude object to be added to the include list
	 * @param path - the full path of the file to include
	 * @param tu - the translation unit which requires the include
	 * @return the required include
	 */
	private IRequiredInclude getRequiredInclude(String path, ITranslationUnit tu) {
        if (path != null) {
            IPath typeLocation = new Path(path);
    		IProject project = tu.getCProject().getProject();
            IPath projectLocation = project.getLocation();
            IPath workspaceLocation = project.getWorkspace().getRoot().getLocation();
        	IPath headerLocation = tu.getResource().getLocation();
            boolean isSystemIncludePath = false;

            IPath includePath = makeRelativePathToProjectIncludes(typeLocation, tu);
            if (includePath != null && !projectLocation.isPrefixOf(typeLocation)) {
                isSystemIncludePath = true;
            }
            //create a relative path - the include file is in the same project as the file we're currently at
            else if (projectLocation.isPrefixOf(typeLocation)
                    && projectLocation.isPrefixOf(headerLocation)) {
                includePath = PathUtil.makeRelativePath(typeLocation, headerLocation.removeLastSegments(1));
            }
            //create a relative path - the include file is in the same workspace as the file we're currently at
            else if (workspaceLocation.isPrefixOf(typeLocation)) 
            {
            	includePath = PathUtil.makeRelativePath(typeLocation, projectLocation);
            }
            if (includePath == null) 
                includePath = typeLocation; //the full path
        	return new RequiredIncludes(includePath.toString(), isSystemIncludePath);
        }
        return null;
    }
	
	/**
	 * Create a relative path to the project includes.
	 * @param fullPath the full path to the project
	 * @param tu a translation unit in the project
	 * @return IPath corresponding to a relative path to the project includes
	 */
	private static IPath makeRelativePathToProjectIncludes(IPath fullPath, ITranslationUnit tu) {
        IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(tu.getCProject().getProject());
        if (provider != null) {
            IScannerInfo info = provider.getScannerInformation(tu.getResource());
            if (info != null) {
                return PathUtil.makeRelativePathToIncludes(fullPath, info.getIncludePaths());
            }
        }
        return null;
    }
	
	/**
	 * Get the fully qualified name for a given PDOMBinding
	 * @param pdomBinding
	 * @return binding's fully qualified name
	 * @throws CoreException
	 */
	private String getBindingQualifiedName(PDOMBinding pdomBinding) throws CoreException
	{
		StringBuffer buf = new StringBuffer(pdomBinding.getName());	
		PDOMNode parent = pdomBinding.getParentNode();
		while (parent != null)
		{
			if (parent instanceof PDOMBinding)
			{							
				buf.insert(0, ((PDOMBinding)parent).getName() + "::"); //$NON-NLS-1$
			}
			parent = parent.getParentNode();
		}
		return buf.toString();
	}
}


