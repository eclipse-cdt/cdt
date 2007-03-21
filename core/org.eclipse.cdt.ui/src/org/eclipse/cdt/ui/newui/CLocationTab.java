/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.NewFolderDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;

import org.eclipse.cdt.internal.ui.CPluginImages;

public abstract class CLocationTab extends AbstractCPropertyTab {

	private final Image IMG_EN = CPluginImages.get(CPluginImages.IMG_OBJS_CFOLDER);
	private final Image IMG_FI = CPluginImages.get(CPluginImages.IMG_OBJS_EXCLUDSION_FILTER_ATTRIB);
	
	Label label;
	TreeViewer tree;
	ArrayList src;
	ICResourceDescription cfgd;
	ICProject cprj;
	
	class _Filter {
		_Entry entry; 
		_Filter(_Entry _entry) { entry = _entry; }
		
		public IPath[] getExtPaths() {
			IPath[] p = null;
			p = entry.ent.getExclusionPatterns();
			return p;
		}
		
		public String[] getExts() {
			IPath[] p = getExtPaths();
			if (p == null || p.length == 0) return new String[0];
			String[] s = new String[p.length];
			for (int i=0; i<p.length; i++) s[i] = p[i].toOSString();
			return s;
		}
		
		public String toString() {
			String[] s = getExts();
			if (s.length == 0) return UIMessages.getString("CLocationTab.0"); //$NON-NLS-1$
			String x = UIMessages.getString("CLocationTab.1"); //$NON-NLS-1$
			for (int i=0; i< s.length; i++) x = x + s[i] + UIMessages.getString("CLocationTab.2"); //$NON-NLS-1$
			x = x.substring(0, x.length() - 2) + UIMessages.getString("CLocationTab.3"); //$NON-NLS-1$
			return x;
		} 
	}
	
	class _Entry {
		ICExclusionPatternPathEntry ent;
		_Filter[] f = new _Filter[1];
		_Entry(ICExclusionPatternPathEntry _ent) { 
			ent = _ent;
			f[0] = new _Filter(this);
		}
		public String toString() { return getPath().toString(); } 

		public IPath getPath() { return ent.isValueWorkspacePath() ? ent.getFullPath() : ent.getLocation(); } 

	}
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));
		label = new Label(usercomp, SWT.NONE);
		label.setLayoutData(new GridData(GridData.BEGINNING));
		tree = new TreeViewer(usercomp);
		tree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.getTree().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}});
		
		initButtons(new String[] {UIMessages.getString("CLocationTab.4"),UIMessages.getString("CLocationTab.5"), UIMessages.getString("CLocationTab.6"), UIMessages.getString("CLocationTab.7")}, 150); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		tree.setContentProvider(new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof _Entry)
					return ((_Entry)parentElement).f;
				return null;
			}
			public Object getParent(Object element) {
				if (element instanceof _Filter)
					return ((_Filter)element).entry;
				return null;
			}
			public boolean hasChildren(Object element) {
				return (element instanceof _Entry);
			}
			public Object[] getElements(Object inputElement) {
				return src.toArray(new _Entry[src.size()]);
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}});

		tree.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (element instanceof _Entry)
					return IMG_EN;
				else if (element instanceof _Filter)
					return IMG_FI;
				else 
					return null;
			}
		});
	}

	/**
	 * Add, Create/Link: always enabled
	 * Edit: enabled if 1 element selected (entry or filter)
	 * Delete: enabled if selected element is entry
	 */
	private void updateButtons() {
		TreeItem[] sel = tree.getTree().getSelection();
    	buttonSetEnabled(2, sel.length == 1);
    	buttonSetEnabled(3, sel.length > 0 && sel[0].getData() instanceof _Entry);
	}
	
	public void buttonPressed(int x) {
		String s;
		Shell shell = usercomp.getShell();
		TreeItem[] sel = tree.getTree().getSelection();
		switch (x) {
		// add
		case 0:
			s = getProjectDialog(shell, EMPTY_STR);
			if (s != null) {
				src.add(new _Entry(newEntry(new Path(s), new IPath[0], true)));
				saveData();
			}
			break;
		// create / link	
		case 1:
			NewFolderDialog  d = new NewFolderDialog(shell, page.getProject()); 
			if (d.open() == Window.OK) {
				IFolder f = (IFolder)d.getFirstResult();
				src.add(new _Entry(newEntry(f, new IPath[0], !f.isLinked())));
				saveData();
			}
		// edit filter	
		case 2:
			if (sel.length == 0) return;
			Object data = sel[0].getData();
			_Entry entry = null;
			if (data instanceof _Entry) 
				entry = (_Entry)data;
			else if (data instanceof _Filter) 
				entry = ((_Filter)data).entry;
			else return;
			ExPatternDialog dialog = new ExPatternDialog(usercomp.getShell(), entry.ent.getExclusionPatterns(), entry.getPath(), page.getProject());
			if (dialog.open() == Window.OK) {
				IPath[] ps = dialog.getExclusionPattern();
				IPath path = entry.getPath();
				boolean isWsp = entry.ent.isValueWorkspacePath();
				entry.ent = newEntry(path, ps, isWsp);
				saveData();
			}
			break;
		case 3:
			if (sel.length == 0) return;
			for (int i = 0; i < sel.length; i++) {
				if (sel[i].getData() instanceof _Entry) src.remove(sel[i].getData());
			}
			saveData();
			break;
		default:
			break;
		}
	}
	
	public void saveData() {
		ICExclusionPatternPathEntry[] p = new ICExclusionPatternPathEntry[src.size()];
		Iterator it = src.iterator();
		int i=0;
		while(it.hasNext()) { p[i++] = ((_Entry)it.next()).ent; }
		setEntries(cfgd, p);
		tree.setInput(cfgd);
		updateData(cfgd);
	}
	
	public void updateData(ICResourceDescription _cfgd) {
		cfgd = _cfgd;
		ICExclusionPatternPathEntry[] ent = getEntries(cfgd);
		src = new ArrayList(ent.length);
		for (int i=0; i<ent.length; i++) {
			src.add(new _Entry(ent[i]));
		}
		tree.setInput(src);
		// get CProject 
		IAdaptable ad = page.getElement();
		if (ad instanceof ICProject)
			cprj = (ICProject)ad;
		
		updateButtons();
	}

	protected abstract ICExclusionPatternPathEntry[] getEntries(ICResourceDescription cfgd);
	protected abstract void setEntries (ICResourceDescription cfgd, ICExclusionPatternPathEntry[] data);
	protected abstract ICExclusionPatternPathEntry newEntry(IPath p, IPath[] ex, boolean workspacePath);
	protected abstract ICExclusionPatternPathEntry newEntry(IFolder f, IPath[] ex, boolean workspacePath);
	
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		setEntries(dst, getEntries(src));
	}

	protected void performDefaults() {
			setEntries(cfgd, null);
			updateData(cfgd);
		}

	
	// This page can be displayed for project only
	public boolean canBeVisible() {
		return page.isForProject();
	}
	
	private String getProjectDialog(Shell shell, String text) {
		IPath path = new Path(text);
		
		LocDialog dialog = new LocDialog(shell);
		dialog.setInput(page.getProject());
	
		IResource container = null;
		if(path.isAbsolute()){
			IContainer cs[] = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(path);
			if(cs != null && cs.length > 0)
				container = cs[0];
		}
		dialog.setInitialSelection(container);
		dialog.setTitle(WORKSPACE_DIR_DIALOG_TITLE); 
           dialog.setMessage(WORKSPACE_DIR_DIALOG_MSG); 
		if (dialog.open() == Window.OK) {
			IResource resource = (IResource) dialog.getFirstResult();
			if (resource != null) { 
				return resource.getFullPath().toString();
			}
		}
		return null;
	}

	class LocDialog extends  ElementTreeSelectionDialog {
	    public LocDialog(Shell parent) {
	        super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
			addFilter(new ViewerFilter () {
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					return element instanceof IFolder;
				}});
	    }
	}	
}
