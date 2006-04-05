/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.Set;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.internal.ui.codemanipulation.AddIncludesOperation;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.cdt.ui.browser.typeinfo.TypeInfoLabelProvider;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
	 * Extract the includes for the given selection.  This can be both used to perform
	 * the work as well as being invoked when there is a change.  The actual results 
	 * can and should be cached as the lookup process could be potentially costly.
	 * 
	 * @return IRequiredInclude [] An array of the required includes, or null if this action is invalid.
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

			// Try the type caching.
			if (fRequiredIncludes == null && fUsings == null) {
				ITypeInfo[] typeInfos= findTypeInfos(name);
				if (typeInfos != null && typeInfos.length > 0) {
					selectResult(typeInfos, name, getShell());
				}
			}

			// Do a full search
			if (fRequiredIncludes == null && fUsings == null) {
				IMatch[] matches = findMatches(name);
				if (matches != null && matches.length > 0) {
					selectResult(matches, name, getShell());
				}
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

	/**
	 * Finds a type by the simple name.
	 */
	private ITypeInfo[] findTypeInfos(final String name) {
		final ITypeInfo[][] infos = new ITypeInfo[1][];
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ITranslationUnit unit = getTranslationUnit();
				int[] types= {ICElement.C_CLASS, ICElement.C_UNION, ICElement.C_STRUCT, ICElement.C_ENUMERATION, ICElement.C_TYPEDEF};
				ITypeSearchScope scope = new TypeSearchScope();
				scope.add(unit.getCProject().getProject());
				if (!AllTypesCache.isCacheUpToDate(scope)) {
					AllTypesCache.updateCache(scope, monitor);
				}
				ITypeInfo[] results = null;
			    if (!monitor.isCanceled()) {
					results = AllTypesCache.getTypes(scope, new QualifiedTypeName(name), types, true);
				    if (!monitor.isCanceled()) {
						for (int i = 0; i < results.length; ++i) {
						    ITypeInfo info = results[i];
						    AllTypesCache.resolveTypeLocation(info, monitor);
						    if (monitor.isCanceled())
						        break;
						}
				    }
			    }
				infos[0] = results;
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(op);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message1"), null); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}
		return infos[0];
	}

	private IMatch[] findMatches(final String name) {
		final BasicSearchResultCollector searchResultCollector = new BasicSearchResultCollector();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ICProject cproject = getTranslationUnit().getCProject();
				ICSearchScope scope = SearchEngine.createCSearchScope(new ICElement[]{cproject}, true);
				OrPattern orPattern = new OrPattern();
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						name, ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS, false));
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						name, ICSearchConstants.TYPE, ICSearchConstants.DEFINITIONS, false));
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						name, ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, false));
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						name, ICSearchConstants.MACRO, ICSearchConstants.DECLARATIONS, false));				
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						name, ICSearchConstants.VAR, ICSearchConstants.DECLARATIONS, false));
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						name, ICSearchConstants.FUNCTION, ICSearchConstants.DECLARATIONS, false));

				SearchEngine searchEngine = new SearchEngine();
				searchEngine.setWaitingPolicy(ICSearchConstants.FORCE_IMMEDIATE_SEARCH);
				searchEngine.search(CUIPlugin.getWorkspace(), orPattern, scope, searchResultCollector, true);
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(op);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message1"), null); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}

		Set set = searchResultCollector.getSearchResults();
		if (set != null) {
			IMatch[] matches = new IMatch[set.size()];
			set.toArray(matches);
			return matches;
		}
		return null;
	}

	private void selectResult(ITypeInfo[] results, String name, Shell shell) {
		int nResults= results.length;
		ITranslationUnit unit = getTranslationUnit();
		if (nResults == 0) {
			return; // bail out
		}

		int occurences = 0;
		int index = 0;
		for (int i = 0; i < results.length; i++) {
			if (name.equals(results[i].getName())) {
				occurences++;
				index = i;
			}
		}

		// if only one
		if (occurences == 1 || results.length == 1) {
			ITypeInfo curr= results[index];
			IRequiredInclude include = getRequiredInclude(curr, unit);
			if (include != null) {
		    	fRequiredIncludes = new IRequiredInclude[] { include };
			}
			if (curr.hasEnclosedTypes()) {
				ITypeInfo[] ns = curr.getEnclosedTypes();
				fUsings = new String[ns.length];
				for (int j = 0; j < fUsings.length; j++) {
					fUsings[j] = ns[j].getName();
				}
			}
			return;
		}

		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), new TypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_TYPE_ONLY));
		dialog.setElements(results);
		dialog.setTitle(CEditorMessages.getString("AddIncludeOnSelection.label")); //$NON-NLS-1$
		dialog.setMessage(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
		if (dialog.open() == Window.OK) {
			ITypeInfo[] selects = (ITypeInfo[])dialog.getResult();
			fRequiredIncludes = new IRequiredInclude[selects.length];
			List usings = new ArrayList(selects.length);
			for (int i = 0; i < fRequiredIncludes.length; i++) {
				IRequiredInclude include = getRequiredInclude(selects[i], unit);
				if (include != null) {
					fRequiredIncludes[i] = include;
					if (selects[i].hasEnclosedTypes()) {
						ITypeInfo[] ns = results[0].getEnclosedTypes();
						for (int j = 0; j < ns.length; j++) {
							usings.add(ns[j].getName());
						}
					}
				} else {
					fRequiredIncludes[i] = new RequiredIncludes(""); //$NON-NLS-1$
				}
			}
			if (!usings.isEmpty()) {
				fUsings = new String[usings.size()];
				usings.toArray(fUsings);
			}
		}
	}
	
	private IRequiredInclude getRequiredInclude(ITypeInfo curr, ITranslationUnit tu) {
        ITypeReference ref = curr.getResolvedReference();
        if (ref != null) {
            IPath typeLocation = ref.getLocation();
    		IProject project = tu.getCProject().getProject();
            IPath projectLocation = project.getLocation();
        	IPath headerLocation = tu.getResource().getLocation();
            boolean isSystemIncludePath = false;

            IPath includePath = PathUtil.makeRelativePathToProjectIncludes(typeLocation, project);
            if (includePath != null && !projectLocation.isPrefixOf(typeLocation)) {
                isSystemIncludePath = true;
            } else if (projectLocation.isPrefixOf(typeLocation)
                    && projectLocation.isPrefixOf(headerLocation)) {
                includePath = PathUtil.makeRelativePath(typeLocation, headerLocation.removeLastSegments(1));
            }
            if (includePath == null)
                includePath = typeLocation;
        	return new RequiredIncludes(includePath.toString(), isSystemIncludePath);
        }
        return null;
    }

    private void selectResult(IMatch[] results, String name, Shell shell) {
		int nResults = results.length;
		if (nResults == 0) {
			return;
		}

		int occurences = 0;
		int index = 0;	
		for (int i= 0; i < results.length; i++) {
			IMatch curr= results[i];
			if (curr.getName().startsWith(name)) {
				occurences++;
				index = i;
			}
		}

		// if only one
		if (occurences == 1 || results.length == 1) {
			IMatch curr = results[index];
			fRequiredIncludes = new IRequiredInclude[1];
			fRequiredIncludes[0] = new RequiredIncludes(curr.getLocation().lastSegment());
			String parentName = curr.getParentName();
			if (parentName != null && parentName.length() > 0) {
				fUsings = new String[] {parentName};
			}
			return;
		}

		// Make them choose
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), new CSearchResultLabelProvider(null));
		dialog.setElements(results);
		dialog.setTitle(CEditorMessages.getString("AddIncludeOnSelection.label")); //$NON-NLS-1$
		dialog.setMessage(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
		if (dialog.open() == Window.OK) {
			IMatch[] selects = (IMatch[])dialog.getResult();
			fRequiredIncludes = new IRequiredInclude[selects.length];
			List usings = new ArrayList(selects.length);
			for (int i = 0; i < fRequiredIncludes.length; i++) {
				fRequiredIncludes[i] = new RequiredIncludes(selects[i].getLocation().lastSegment());
				String parentName = selects[i].getParentName();
				if (parentName != null && parentName.length() > 0) {
					usings.add(parentName);
				}
			}
			if (!usings.isEmpty()) {
				fUsings = new String [usings.size()];
				usings.toArray(fUsings);
			}
		}
	}

	public void setContentEditor(ITextEditor editor) {
		fEditor= editor;
	}
	
	public void update() {
		setEnabled(true);
	}
}


