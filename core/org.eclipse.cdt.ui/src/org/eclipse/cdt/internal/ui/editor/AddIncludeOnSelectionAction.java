/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.internal.ui.codemanipulation.AddIncludesOperation;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;

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
		IIndex index;
		try {
			index = CCorePlugin.getIndexManager().getIndex(tu.getCProject(), IIndexManager.ADD_DEPENDENCIES);
			index.acquireReadLock();
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
			return;
		} catch (InterruptedException e) {
			return;
		}
		try {
			if (tu != null) {
				extractIncludes(fEditor, index);
				addInclude(tu);
			}
		}
		finally {
			index.releaseReadLock();
		}
		fUsings = null;
		fRequiredIncludes = null;
	}
	
	/**
	 * To be used by ElementListSelectionDialog for user to choose which declarations/
	 * definitions for "add include" when there are more than one to choose from.  
	 */
	private static class DisplayName extends Object 
	{
		private IIndexName name;
		private IIndexBinding binding;
		
		public DisplayName(IIndexName name, IIndexBinding binding) {
			this.name = name;
			this.binding= binding;
		}

		public String toString()
		{			
			try {				
				if (binding != null)
				{
					return getBindingQualifiedName(binding) + " - " + name.getFileLocation().getFileName(); //$NON-NLS-1$
				}
				else
					return null;
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return null;
			}
		}
		
		public IIndexName getName() {
			return name;
		}
		
		public IIndexBinding getBinding() {
			return binding;
		}
		
	}
	

	/**
	 * Extract the includes for the given selection.  This can be both used to perform
	 * the work as well as being invoked when there is a change.  The actual results 
	 * can and should be cached as the lookup process could be potentially costly.
	 * @param index 
	 */
	private void extractIncludes(ITextEditor editor, IIndex index) {
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

			try {			
				Pattern pattern = Pattern.compile(name);
				
				IndexFilter filter= new IndexFilter() {
				};
				IIndexBinding[] bindings= index.findBindings(pattern, false, filter, new NullProgressMonitor());
				ArrayList pdomNames= new ArrayList();
				for (int i = 0; i < bindings.length; ++i) {
					IIndexBinding binding= bindings[i];
					IIndexName[] defs= null;
					// class, struct union, enumeration
					if (binding instanceof ICompositeType || binding instanceof IEnumeration) {
						defs= index.findDefinitions(binding);
					}
					else if (binding instanceof ITypedef || binding instanceof IFunction) {
						defs= index.findDeclarations(binding);
					}
					if (defs != null) {
						for (int j = 0; j < defs.length; j++) {
							pdomNames.add(new DisplayName(defs[j], binding));
						}
					}
				}
																
				if (pdomNames.size() > 1)
				{				
					ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_NAME_ONLY));
					dialog.setElements(pdomNames.toArray());
					dialog.setTitle(CEditorMessages.getString("AddIncludeOnSelection.label")); //$NON-NLS-1$
					dialog.setMessage(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
					if (dialog.open() == Window.OK) {
						//get selection
						Object[] selects = dialog.getResult();

						fRequiredIncludes = new IRequiredInclude[selects.length];
						List usings = new ArrayList(selects.length);
						for (int i = 0; i < fRequiredIncludes.length; i++) {
							IRequiredInclude include = getRequiredInclude(((DisplayName)selects[i]).getName().getFileLocation().getFileName(), getTranslationUnit());
							if (include != null) {
								fRequiredIncludes[i] = include;
								IIndexBinding binding = ((DisplayName)selects[i]).getBinding();
								if (binding instanceof ICPPBinding)
								{
									//find the enclosing namespace, if there's one
									IQualifiedTypeName qualifiedName = new QualifiedTypeName(getBindingQualifiedName(binding));
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
					// we should use the IIndexName.getLocation here rather than getFileLocation
					String fileName = ((DisplayName)pdomNames.get(0)).getName().getFileLocation().getFileName();
					fRequiredIncludes = new IRequiredInclude[] {getRequiredInclude(fileName, getTranslationUnit())};
					IIndexBinding binding = ((DisplayName)pdomNames.get(0)).getBinding();

					if (binding instanceof ICPPBinding) {
						//find the enclosing namespace, if there's one
						IQualifiedTypeName qualifiedName = new QualifiedTypeName(getBindingQualifiedName(binding));
						String qualifiedEnclosingName = new QualifiedTypeName(qualifiedName.getEnclosingNames()).getFullyQualifiedName();
						if (!qualifiedEnclosingName.equals("")) //$NON-NLS-1$
							fUsings = new String[] {qualifiedEnclosingName};						
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
	private static String getBindingQualifiedName(IIndexBinding binding) throws CoreException
	{
		StringBuffer buf = new StringBuffer();
		String[] qname= binding.getQualifiedName();
		for (int i = 0; i < qname.length; i++) {
			if (i>0) {
				buf.append("::"); //$NON-NLS-1$
			}
			buf.append(qname[i]);
		}
		return buf.toString();
	}
}


