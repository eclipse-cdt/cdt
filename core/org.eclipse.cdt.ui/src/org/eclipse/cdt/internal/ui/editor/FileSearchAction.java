package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.search.internal.core.text.TextSearchEngine;
import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.text.TextSearchOperation;
import org.eclipse.search.internal.ui.text.TextSearchResultCollector;
import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;



public class FileSearchAction extends Action {


	private static final String PREFIX= "FileSearchAction.";
	
	private ISelectionProvider fSelectionProvider;

	public FileSearchAction(ISelectionProvider provider) {
		super(CUIPlugin.getResourceString(PREFIX + "label"));
		setDescription(CUIPlugin.getResourceString(PREFIX + "description"));
		setToolTipText(CUIPlugin.getResourceString(PREFIX + "tooltip"));
		
		if(provider instanceof CContentOutlinePage) {
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_OPEN_INCLUDE);
			//setText("Search for References"); // $NON-NLS
		}
		
		fSelectionProvider= provider;
	}
			
	public void run() {
		String search_name;
		
		ISelection selection= fSelectionProvider.getSelection();
		if(selection instanceof ITextSelection) {
			search_name = ((ITextSelection)selection).getText();
			if(search_name.length() == 0) return;
		} else {
			ICElement element= getElement(selection);
			if (element == null) {
				return;
			}
			search_name = element.getElementName();
		}
		
		// @@@ we rely on the internal functions of the Search plugin, since
		// none of these are actually exported. This is probably going to change
		// with 2.0.
		TextSearchResultCollector col = new TextSearchResultCollector();
		try {
			//TextSearchPage
			//ProgressMonitor monitor = new ProgressMonitor();
			//col.setProgressMonitor(monitor)
			SearchUI.activateSearchResultView();
			//col.aboutToStart();
		
			// We now have the element, start a search on the string
			//TextSearchEngine engine = new TextSearchEngine();
			TextSearchScope scope= TextSearchScope.newWorkspaceScope();
			// Add the extensions from the C editor definition for now
			// FIXME: For C/C++ not all files rely on extension to be C++ for <cstring>
			String[] cexts = CoreModel.getDefault().getTranslationUnitExtensions();
			for (int i = 0; i < cexts.length; i++) {
				scope.addExtension("*." + cexts[i]);
			}
//			scope.addExtension("*.c");
//			scope.addExtension("*.h");
//			scope.addExtension("*.cc");
//			scope.addExtension("*.hh");
			
			TextSearchOperation op= new TextSearchOperation(
				CUIPlugin.getWorkspace(),
				search_name,
				"",
				scope,
				col);


			//engine.search(CUIPlugin.getWorkspace(), element.getName(),
			//	null, scope, col);
			IRunnableContext context=  null;
			//context= getContainer().getRunnableContext();
			
			Shell shell= new Shell(); // getShell();
			if (context == null)
				context= new ProgressMonitorDialog(shell);


			try {			
				context.run(true, true, op);
			} catch (InvocationTargetException ex) {
				ExceptionHandler.handle(ex, "Error","Error"); //$NON-NLS-2$ //$NON-NLS-1$
			} catch (InterruptedException e) {
			}
		} catch (Exception e) {}
		
	}


	private static ICElement getElement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof ICElement) {
					return (ICElement)element;
				}
			}
		}
		return null;
	}
	
	public static boolean canActionBeAdded(ISelection selection) {
		if(selection instanceof ITextSelection) {
			return (((ITextSelection)selection).getLength() > 0);
		} else {
			return getElement(selection) != null;
		}
	}	


	public static String getEditorID(String name) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		if (registry != null) {
			IEditorDescriptor descriptor = registry.getDefaultEditor(name);
			if (descriptor != null) {
				return descriptor.getId();
			} else {
				return registry.getDefaultEditor().getId();
			}
		}
		return null;
	}
}


