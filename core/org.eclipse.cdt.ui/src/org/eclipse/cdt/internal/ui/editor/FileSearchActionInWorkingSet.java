/*
 * Created on Sep 9, 2003
 */
package org.eclipse.cdt.internal.ui.editor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.search.CSearchScopeFactory;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.internal.core.SearchScope;
import org.eclipse.search.internal.ui.text.TextSearchOperation;
import org.eclipse.search.internal.ui.text.TextSearchResultCollector;
import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * @author bgheorgh
 */
public class FileSearchActionInWorkingSet extends Action {


	private static final String PREFIX= "FileSearchActionInWorkingSet."; //$NON-NLS-1$
	
	private ISelectionProvider fSelectionProvider;

	public FileSearchActionInWorkingSet(ISelectionProvider provider) {
		super(CUIPlugin.getResourceString(PREFIX + "label")); //$NON-NLS-1$
		setDescription(CUIPlugin.getResourceString(PREFIX + "description")); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString(PREFIX + "tooltip")); //$NON-NLS-1$
		
		if(provider instanceof CContentOutlinePage) {
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_OPEN_INCLUDE);
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
			SearchUI.activateSearchResultView();
			
			IWorkingSet[] workingSets= CSearchScopeFactory.getInstance().queryWorkingSets();
			ArrayList resourceList = new ArrayList();
			for (int i=0; i<workingSets.length; i++){
				IAdaptable[] elements = workingSets[i].getElements();
				
				for (int j=0; j< elements.length; j++){
					IResource resource= (IResource)elements[j].getAdapter(IResource.class);
					if (resource != null){
						resourceList.add(resource);
					}
				}
			}
			IResource[] result = new IResource[resourceList.size()];
			resourceList.toArray(result);
			
			SearchScope scope= new SearchScope("File Search", result); //$NON-NLS-1$
	
			
			TextSearchOperation op= new TextSearchOperation(
				CUIPlugin.getWorkspace(),
				search_name,
				"", //$NON-NLS-1$
				scope,
				col);
				
			IRunnableContext context=  null;
			
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
