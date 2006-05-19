/**
 * 
 */
package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author dschaefer
 *
 */
public class CountNodeAction extends IndexAction {

	public CountNodeAction(TreeViewer viewer) {
		super(viewer, CUIPlugin.getResourceString("IndexView.CountSymbols.name")); //$NON-NLS-1$
	}

	public boolean valid() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return false;
		Object[] objs = ((IStructuredSelection)selection).toArray();
		for (int i = 0; i < objs.length; ++i)
			if (objs[i] instanceof ICProject)
				return true;
		return false;
	}

	public void run() {
		final int[] count = new int[1];
		
		try {
			ISelection selection = viewer.getSelection();
			if (!(selection instanceof IStructuredSelection))
				return;

			Object[] objs = ((IStructuredSelection)selection).toArray();
			for (int i = 0; i < objs.length; ++i) {
				if (!(objs[i] instanceof ICProject))
					continue;

				ICProject project = (ICProject)objs[i];
				final PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project);
				pdom.getFileIndex().accept(new IBTreeVisitor() {
					public int compare(int record) throws CoreException {
						return 1;
					}
					public boolean visit(int record) throws CoreException {
						if (record != 0) {
							PDOMFile file = new PDOMFile(pdom, record);
							PDOMMacro macro = file.getFirstMacro();
							while (macro != null) {
								++count[0];
								macro = macro.getNextMacro();
							}
						}
						return true;
					}
				});
				pdom.accept(new IPDOMVisitor() {
					public boolean visit(IPDOMNode node) throws CoreException {
						count[0]++;
						return true;
					}
				});
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		
		MessageDialog.openInformation(null,
				CUIPlugin.getResourceString("IndexView.CountSymbols.title"), //$NON-NLS-1$
				CUIPlugin.getFormattedString("IndexView.CountSymbols.message", //$NON-NLS-1$
						new String[] { String.valueOf(count[0]) }));
	}

}
