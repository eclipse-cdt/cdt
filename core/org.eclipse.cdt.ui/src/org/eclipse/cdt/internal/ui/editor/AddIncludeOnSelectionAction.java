package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
import org.eclipse.cdt.internal.ui.CCompletionContributorManager;
import org.eclipse.cdt.internal.ui.codemanipulation.AddIncludeOperation;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IRequiredInclude;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;


public class AddIncludeOnSelectionAction extends Action implements IUpdate {
		
	private ITextEditor fEditor;

	class RequiredIncludes implements IRequiredInclude {
		String name;

		RequiredIncludes(String n) {
			name = n;
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
			return true;
		}
		
	}


	public AddIncludeOnSelectionAction() {
		this(null);
	}
	
	public AddIncludeOnSelectionAction(ITextEditor editor) {	
		super(CEditorMessages.getString("AddIncludeOnSelection.label"));		 //$NON-NLS-1$
		setToolTipText(CEditorMessages.getString("AddIncludeOnSelection.tooltip")); //$NON-NLS-1$
		setDescription(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
		
		fEditor= editor;
		//WorkbenchHelp.setHelp(this,	new Object[] { IJavaHelpContextIds.ADD_IMPORT_ON_SELECTION_ACTION });	
	}
	
	private void addInclude(IRequiredInclude[] inc, ITranslationUnit tu) {
		AddIncludeOperation op= new AddIncludeOperation(fEditor, tu, inc, false);
		try {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
			dialog.run(false, true, op);
		} catch (InvocationTargetException e) {
			//e.printStackTrace();
			MessageDialog.openError(getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message1"), e.getTargetException().getMessage()); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}
	}
	
	ITranslationUnit getTranslationUnit () {
		ITranslationUnit unit = null;
		if(fEditor != null) {
			IEditorInput editorInput= fEditor.getEditorInput();
			unit = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		}
		return unit;
	}
	
	protected Shell getShell() {
		return fEditor.getSite().getShell();
	}
	
	private int getNameStart(IDocument doc, int pos) throws BadLocationException {
		if (pos > 0 && doc.getChar(pos - 1) == '.') {
			pos--;
			while (pos > 0) {
				char ch= doc.getChar(pos - 1);
				if (!Character.isJavaIdentifierPart(ch) && ch != '.') {
					return pos;
				}
				pos--;
			}
		}
		return pos;
	}

/*	private void removeQualification(IDocument doc, int nameStart, IType type) throws BadLocationException {
		String packName= type.getPackageFragment().getElementName();
		int packLen= packName.length();
		if (packLen > 0) {
			for (int k= 0; k < packLen; k++) {
				if (doc.getChar(nameStart + k) != packName.charAt(k)) {
					return;
				}
			}
			doc.replace(nameStart, packLen + 1, ""); //$NON-NLS-1$
		}
	} */
	
	/**
	 * @see IAction#actionPerformed
	 */
	public void run() {
		IRequiredInclude [] requiredIncludes;
		requiredIncludes = extractIncludes(fEditor);		

		if (requiredIncludes != null && requiredIncludes.length > 0) {
			ITranslationUnit tu= getTranslationUnit();
			if (tu != null) {
				addInclude(requiredIncludes, tu);
			}
		} 
	}

	/**
	 * Extract the includes for the given selection.  This can be both used to perform
	 * the work as well as being invoked when there is a change.  The actual results 
	 * can and should be cached as the lookup process could be potentially costly.
	 * 
	 * @return IRequiredInclude [] An array of the required includes, or null if this action is invalid.
	 */
	private IRequiredInclude [] extractIncludes(ITextEditor editor) {
		if(editor == null) {
			return null;
		}
		
		ISelection s= editor.getSelectionProvider().getSelection();
		IDocument doc= editor.getDocumentProvider().getDocument(editor.getEditorInput());

		if (s.isEmpty() || !(s instanceof ITextSelection) || doc == null) {
			return null;
		}
	
		ITextSelection selection= (ITextSelection) s;
		IRequiredInclude [] requiredIncludes = null;
		try {
			int selStart= selection.getOffset();
			int nameStart= getNameStart(doc, selStart);
			int len= selStart - nameStart + selection.getLength();
					
			String name= doc.get(nameStart, len).trim();
					
			IFunctionSummary fs = CCompletionContributorManager.getDefault().getFunctionInfo(name);
			if(fs != null) {
				requiredIncludes = fs.getIncludes();
			}

			// Try the type caching.
			if (requiredIncludes == null) {
				ITypeInfo[] types= findTypeInfos(name);
				requiredIncludes = selectResult(types, name, getShell());
			}

			// Do a full search
			if (requiredIncludes == null) {
				IMatch[] matches = findMatches(name);
				requiredIncludes = selectResult(matches, name, getShell());				
			}
		} catch (BadLocationException e) {
			MessageDialog.openError(getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message3"), CEditorMessages.getString("AddIncludeOnSelection.error.message4") + e.getMessage()); //$NON-NLS-2$ //$NON-NLS-1$
		}
		
		return requiredIncludes;
	}

	/**
	 * Finds a type by the simple name.
	 */
	ITypeInfo[] findTypeInfos(final String name) {
		final ITypeInfo[][] infos = new ITypeInfo[1][];
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ITranslationUnit unit = getTranslationUnit();
				int[] types= {ICElement.C_CLASS, ICElement.C_UNION, ICElement.C_STRUCT, ICElement.C_ENUMERATION, ICElement.C_TYPEDEF};
				ITypeSearchScope scope = new TypeSearchScope();
				scope.add(unit.getCProject().getProject());
				infos[0] = AllTypesCache.getTypes(scope, new QualifiedTypeName(name), types);				
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

	IMatch[] findMatches(final String name) {
		final BasicSearchResultCollector searchResultCollector = new BasicSearchResultCollector();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ICProject cproject = getTranslationUnit().getCProject();
				ICSearchScope scope = SearchEngine.createCSearchScope(new ICElement[]{cproject}, true);
				OrPattern orPattern = new OrPattern();
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						name, ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS, false));
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

	private IRequiredInclude[] selectResult(ITypeInfo[] results, String name, Shell shell) {
		int nResults= results.length;
		IProject project = getTranslationUnit().getCProject().getProject();
		if (nResults == 0) {
			return null;
		} else if (nResults == 1) {
			ITypeReference ref = results[0].getResolvedReference();
			if (ref != null) {
				return new IRequiredInclude[] {new RequiredIncludes(ref.getRelativeIncludePath(project).toString())};
			}
		}
		
		if (name.length() != 0) {
			for (int i= 0; i < results.length; i++) {
				ITypeInfo curr= results[i];
				if (name.equals(curr.getName())) {
					ITypeReference ref = results[0].getResolvedReference();
					if (ref != null) {
						return new IRequiredInclude[]{new RequiredIncludes(ref.getRelativeIncludePath(project).toString())};
					}
				}
			}
		}

		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), new WorkbenchLabelProvider());
		dialog.setElements(results);
		dialog.setTitle(CEditorMessages.getString("AddIncludeOnSelection.label")); //$NON-NLS-1$
		dialog.setMessage(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
		if (dialog.open() == Window.OK) {
			ITypeInfo[] selects = (ITypeInfo[])dialog.getResult();
			IRequiredInclude[] reqs = new IRequiredInclude[selects.length];
			for (int i = 0; i < reqs.length; i++) {
				ITypeReference ref = selects[0].getResolvedReference();
				if (ref != null) {
					reqs[i] = new RequiredIncludes(ref.getRelativeIncludePath(project).toString());
				} else {
					reqs[i] = new RequiredIncludes(""); //$NON-NLS-1$
				}
			}
			return reqs;
		}
		return null;
	}
	
	private IRequiredInclude[] selectResult(IMatch[] results, String name, Shell shell) {
		int nResults = results.length;
		if (nResults == 0) {
			return null;
		} else if (nResults == 1) {
			return new IRequiredInclude[] {new RequiredIncludes(results[0].getLocation().lastSegment())};
		}
		
		if (name.length() != 0) {
			for (int i= 0; i < results.length; i++) {
				IMatch curr= results[i];
				if (name.equals(curr.getName())) {
					return new IRequiredInclude[]{new RequiredIncludes(curr.getLocation().lastSegment())};
				}
			}
		}

		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), new WorkbenchLabelProvider());
		dialog.setElements(results);
		dialog.setTitle(CEditorMessages.getString("AddIncludeOnSelection.label")); //$NON-NLS-1$
		dialog.setMessage(CEditorMessages.getString("AddIncludeOnSelection.description")); //$NON-NLS-1$
		if (dialog.open() == Window.OK) {
			IMatch[] selects = (IMatch[])dialog.getResult();
			IRequiredInclude[] reqs = new IRequiredInclude[selects.length];
			for (int i = 0; i < reqs.length; i++) {
				reqs[i] = new RequiredIncludes(selects[i].getLocation().lastSegment());
			}
			return reqs;
		}
		return null;
	}

	public void setContentEditor(ITextEditor editor) {
		fEditor= editor;
	}
	
	public void update() {
		setEnabled(true);
	}
}


