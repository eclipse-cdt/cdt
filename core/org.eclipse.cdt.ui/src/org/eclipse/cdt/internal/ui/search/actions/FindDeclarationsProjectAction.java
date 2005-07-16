package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchScopeFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

public class FindDeclarationsProjectAction extends FindAction {

	public FindDeclarationsProjectAction(CEditor editor, String label, String tooltip){
		super(editor);
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1$
	}
	
	public FindDeclarationsProjectAction(CEditor editor){
		this(editor,
			CSearchMessages.getString("CSearch.FindDeclarationsProjectAction.label"), //$NON-NLS-1$
			CSearchMessages.getString("CSearch.FindDeclarationsProjectAction.tooltip")); //$NON-NLS-1$
	}
	
	public FindDeclarationsProjectAction(IWorkbenchSite site){
		this(site,
			CSearchMessages.getString("CSearch.FindDeclarationsProjectAction.label"), //$NON-NLS-1$
			CSearchMessages.getString("CSearch.FindDeclarationsProjectAction.tooltip")); //$NON-NLS-1$
	}
	/**
	 * @param site
	 * @param string
	 * @param string2
	 * @param string3
	 */
	public FindDeclarationsProjectAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScope(org.eclipse.core.resources.IProject)
	 */
	protected ICSearchScope getScope() {
		
		ICProject proj = null;
		if (fEditor != null){
			proj= fEditor.getInputCElement().getCProject();			 
		} else if (fSite != null){
			IStructuredSelection sel = (IStructuredSelection) getSelection();
			return CSearchScopeFactory.getInstance().createEnclosingProjectScope(sel);
		}
		
		ICElement[] element = new ICElement[1];
		element[0]=proj;
		return SearchEngine.createCSearchScope(element);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScopeDescription()
	 */
	protected String getScopeDescription() {
		// TODO Auto-generated method stub
		return CSearchMessages.getString("ProjectScope"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getLimitTo()
	 */
	protected LimitTo getLimitTo() {
		// TODO Auto-generated method stub
		return ICSearchConstants.DECLARATIONS_DEFINITIONS;
	}

}
