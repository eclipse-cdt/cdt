/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * CLocationTab implements common features for "Source Location" and "Output Location"
 * tabs in project preferences.
 *
 */
public abstract class CLocationTab extends AbstractCPropertyTab {

	private final Image IMG_EN = CPluginImages.get(CPluginImages.IMG_OBJS_CFOLDER);
	private final Image IMG_FI = CPluginImages.get(CPluginImages.IMG_OBJS_EXCLUDSION_FILTER_ATTRIB);
	
	Label label;
	TreeViewer tree;
	ArrayList<_Entry> src;
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
			if (p == null || p.length == 0) 
				return new String[0];
			String[] s = new String[p.length];
			for (int i=0; i<p.length; i++) 
				s[i] = p[i].toOSString();
			return s;
		}
		
		@Override
		public String toString() {
			String[] s = getExts();
			if (s.length == 0) 
				return Messages.CLocationTab_0; 
			String x = Messages.CLocationTab_1; 
			for (String element : s)
				x = x + element + Messages.CLocationTab_2; 
			x = x.substring(0, x.length() - 2) + Messages.CLocationTab_3; 
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
		@Override
		public String toString() { 
			return getPath() == null ? 
					EMPTY_STR : 
					getPath().toString();
		} 

		public IPath getPath() { 
			return ent.isValueWorkspacePath() ? 
					ent.getFullPath() : 
					ent.getLocation(); 
		} 
	}
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));
		label = new Label(usercomp, SWT.NONE);
		label.setLayoutData(new GridData(GridData.BEGINNING));
		tree = new TreeViewer(usercomp);
		tree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.getTree().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}});
		
		initButtons(new String[] {Messages.CLocationTab_4, Messages.CLocationTab_5, Messages.CLocationTab_6, Messages.CLocationTab_7}, 150); 
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
			@Override
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
	@Override
	protected void updateButtons() {
		TreeItem[] sel = tree.getTree().getSelection();
    	buttonSetEnabled(2, sel.length == 1);
    	buttonSetEnabled(3, sel.length > 0 && sel[0].getData() instanceof _Entry);
	}
	
	@Override
	public void buttonPressed(int x) {
		Shell shell = usercomp.getShell();
		TreeItem[] sel = tree.getTree().getSelection();
		switch (x) {
		// add
		case 0:
			String[] ss = getProjectDialog(shell);
			if (ss != null) {
				for (String element : ss)
					src.add(new _Entry(newEntry(new Path(element), new IPath[0], true)));
				saveData();
			}
			break;
		// create / link	
		case 1:
			NewFolderDialog  d = new NewFolderDialog(shell, page.getProject()) {
				@Override
				public void create() {
					super.create();
					handleAdvancedButtonSelect();
				}
			};
			if (d.open() == Window.OK) {
				IFolder f = (IFolder)d.getFirstResult();
				src.add(new _Entry(newEntry(f, new IPath[0], !f.isLinked())));
				saveData();
			}
			break;
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
			for (TreeItem element : sel) {
					if (element.getData() instanceof _Entry) src.remove(element.getData());
				}
			saveData();
			break;
		default:
			break;
		}
	}
	
	private void saveData() {
		ICExclusionPatternPathEntry[] p = new ICExclusionPatternPathEntry[src.size()];
		Iterator<_Entry> it = src.iterator();
		int i=0;
		while(it.hasNext()) { p[i++] = (it.next()).ent; }
		setEntries(cfgd, p);
		tree.setInput(cfgd);
		updateData(cfgd);
		if (page instanceof AbstractPage) {
			ICConfigurationDescription cfgDescription = cfgd.getConfiguration();
			((AbstractPage)page).cfgChanged(cfgDescription);
		}
	}
	
	@Override
	public void updateData(ICResourceDescription _cfgd) {
		if (page.isMultiCfg()) {
			setAllVisible(false, ""); //$NON-NLS-1$
			return;
		}
		
		cfgd = _cfgd;
		IAdaptable ad = page.getElement();
		if (ad instanceof ICProject) {
			cprj = (ICProject)ad;
		}
		IResource rc = (IResource)ad;
		
		setAllVisible(true, null);
		
		src = new ArrayList<_Entry>();
		_Entry selectedSourcePath = null;
		for (ICExclusionPatternPathEntry e : getEntries(cfgd)) {
			_Entry entry = new _Entry(e);
			if (entry.ent.isValueWorkspacePath() && entry.ent.getFullPath().equals(rc.getFullPath())) {
				selectedSourcePath = entry;
			}
			src.add(entry);
		}
		tree.setInput(src);
		if (selectedSourcePath!=null) {
			ISelection selection = new StructuredSelection(new Object[] {selectedSourcePath});
			tree.setSelection(selection);
		}
		
		updateButtons();
	}

	protected abstract ICExclusionPatternPathEntry[] getEntries(ICResourceDescription cfgd);
	protected abstract void setEntries (ICResourceDescription cfgd, ICExclusionPatternPathEntry[] data);
	protected abstract ICExclusionPatternPathEntry newEntry(IPath p, IPath[] ex, boolean workspacePath);
	protected abstract ICExclusionPatternPathEntry newEntry(IFolder f, IPath[] ex, boolean workspacePath);
	
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		setEntries(dst, getEntries(src));
	}

	@Override
	protected void performDefaults() {
			setEntries(cfgd, null);
			updateData(cfgd);
		}

	
	// This page can be displayed either for project or for folder
	@Override
	public boolean canBeVisible() {
		if (page.getResDesc() instanceof ICMultiItemsHolder)
			return false; // cannot work with multi cfg
		
		return page.isForProject() || page.isForFolder();
	}
	
	private String[] getProjectDialog(Shell shell) {
		Set<IPath> set = new HashSet<IPath>(src.size());
		for (_Entry e : src)
			set.add(e.getPath());
		
		LocDialog dialog = new LocDialog(shell, set);
		dialog.setInput(page.getProject());
		
		dialog.setTitle(WORKSPACE_DIR_DIALOG_TITLE);
		dialog.setMessage(WORKSPACE_DIR_DIALOG_MSG);
		if (dialog.open() == Window.OK) {
			Object[] resources = dialog.getResult();
			if (resources != null) {
				String[] ss = new String[resources.length];
				for (int i=0; i<resources.length; i++)
					ss[i] = ((Holder)resources[i]).getPath().toString();
				return ss;
			}
		}
		return null;
	}

	/**
	 * This class should hold elements for source location tree 
	 */
	class Holder implements IAdaptable {
		private IFolder  f = null;
		private boolean isRoot = false;
		private IPath p = null;
		
		Holder(IProject _p) {
			f = _p.getFolder(_p.getName());
			isRoot = true;
			p = _p.getFullPath();
		}
		
		Holder(IFolder _f) {
			f = _f;
			isRoot = false;
			p = _f.getFullPath();
		}
		
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return f.getAdapter(adapter);
		}
		public boolean isRoot() {
			return isRoot;
		}
		public IFolder getFolder() {
			return f;
		}
		public IPath getPath() {
			return p;
		}

		private CLocationTab getOuterType() {
			return CLocationTab.this;
		}
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((p == null) ? 0 : p.hashCode());
			result = prime * result + (isRoot ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Holder other = (Holder) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (isRoot != other.isRoot)
				return false;
			if (p == null) {
				if (other.p != null)
					return false;
			} else if (!p.equals(other.p))
				return false;
			return true;
		}
		
		/**
		 * For debugging purpose only
		 * @see java.lang.Object#toString()
		 */
		@SuppressWarnings("nls")
		@Override
		public String toString() {
			return "[Holder] " + p;
		}
	}
	
	class LocDialog extends  ElementTreeSelectionDialog {
		Set <IPath> existing;
		
		public LocDialog(Shell parent, Set <IPath> ex) {
			super(parent,
					
			new WorkbenchLabelProvider() {
 			    @Override
				protected String decorateText(String input, Object element) {
			    	if (element instanceof Holder &&
			    	    ((Holder)element).isRoot())
			    			return Messages.CLocationTab_8; 
			    	return super.decorateText(input, element);
			    }
			},
			
			new WorkbenchContentProvider() {
				@Override
				public Object[] getChildren(Object element) {
					if (element instanceof IProject) {
						Object[] ob1 = super.getChildren(element);
						Object[] ob2 = new Object[ob1.length + 1];
						int cnt = 0;
						ob2[cnt++] = new Holder((IProject)element);
						for (Object ob: ob1) {
							if (ob instanceof IFolder)
								ob2[cnt++] = new Holder((IFolder)ob);
						}
						ob1 = new Object[cnt];
						System.arraycopy(ob2, 0, ob1, 0, cnt);
						return ob1;
					} else if (element instanceof Holder) {
						Holder h = (Holder)element;
						if (h.isRoot())
							return new Object[0];
						Object[] ob1 = super.getChildren(h.getFolder());
						Object[] ob2 = new Object[ob1.length];
						int cnt = 0;
						for (Object ob: ob1) {
							if (ob instanceof IFolder)
								ob2[cnt++] = new Holder((IFolder)ob);
						}
						ob1 = new Object[cnt];
						System.arraycopy(ob2, 0, ob1, 0, cnt);
						return ob1;
					} else 
						return super.getChildren(element);
				}
			});
			
			addFilter(new ViewerFilter () {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (! (element instanceof Holder))
						return false;
					if (existing == null || existing.size() == 0)
						return true;
					Holder h = (Holder)element;
					return (! existing.contains(h.getPath()));
				}});
			
			existing = ex;
		}
		
		@Override
		protected TreeViewer createTreeViewer(Composite parent) {
			TreeViewer treeViewer = super.createTreeViewer(parent);
			
			// Expand the tree and select current resource
			if (page.getElement() instanceof IFolder) {
				IFolder folder = (IFolder)page.getElement();
				
				List<Holder> list = new ArrayList<Holder>();
				list.add(new Holder(folder));
				for (IContainer parentFolder = folder.getParent();parentFolder instanceof IFolder;parentFolder=parentFolder.getParent()) {
					list.add(0,new Holder((IFolder) parentFolder));
				}
				treeViewer.expandToLevel(new TreePath(list.toArray()), 0);
				setInitialSelection(new Holder(folder));
			}
			return treeViewer;
		}
	}	
}
