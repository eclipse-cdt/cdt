/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.IBadDirective;
import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.IComment;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IEmptyLine;
import org.eclipse.cdt.make.core.makefile.IInferenceRule;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IParent;
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.cdt.make.core.makefile.ITargetRule;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;
import org.eclipse.cdt.make.core.makefile.gnu.ITerminal;
import org.eclipse.cdt.make.internal.core.makefile.NullMakefile;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * MakefileContentOutlinePage
 */
public class MakefileContentOutlinePage extends ContentOutlinePage implements IContentOutlinePage {

	private class MakefileContentProvider implements ITreeContentProvider {

		protected boolean showMacroDefinition = true;
		protected boolean showTargetRule = true;
		protected boolean showInferenceRule = true;
		protected boolean showIncludeChildren = false;

		protected IMakefile makefile;
		protected IMakefile nullMakefile = new NullMakefile();

		/* (non-Javadoc)
		* @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		*/
		public Object[] getChildren(Object element) {
			if (element == fInput) {
				return getElements(makefile);
			} else if (element instanceof IDirective) {
				return getElements(element);
			}
			return new Object[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof IMakefile) {
				return fInput;
			} else if (element instanceof IDirective) {
				return ((IDirective)element).getParent();
			}
			return fInput;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			if (element == fInput) {
				return true;
			} else if (element instanceof IParent) {
				// Do not drill down in includes.
				if (element instanceof IInclude && !showIncludeChildren) {
					return false;
				}
				return true;
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			IDirective[] directives;
			if (inputElement == fInput) {
				directives = makefile.getDirectives();
			} else if (inputElement instanceof IRule) {
				directives = ((IRule)inputElement).getCommands();
			} else if (inputElement instanceof IParent) {
				if (inputElement instanceof IInclude && !showIncludeChildren) {
					directives = new IDirective[0];
				} else {
					directives = ((IParent)inputElement).getDirectives();
				}
			} else {
				directives = new IDirective[0];
			}
			List list = new ArrayList(directives.length);
			for (int i = 0; i < directives.length; i++) {
				if (showMacroDefinition && directives[i] instanceof IMacroDefinition) {
					list.add(directives[i]);
				} else if (showInferenceRule && directives[i] instanceof IInferenceRule) {
					list.add(directives[i]);
				} else if (showTargetRule && directives[i] instanceof ITargetRule) {
					list.add(directives[i]);
				} else {
					boolean irrelevant = (directives[i] instanceof IComment ||
						directives[i] instanceof IEmptyLine ||
						directives[i] instanceof ITerminal); 
					if (!irrelevant) {
						list.add(directives[i]);
					}
				}
			}
			return list.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput != null) {
				makefile = nullMakefile;
			}

			if (newInput != null) {
				IWorkingCopyManager manager= MakeUIPlugin.getDefault().getWorkingCopyManager();
				makefile = manager.getWorkingCopy((IEditorInput)newInput);
				if (makefile == null) {
					makefile = nullMakefile;
				}
			}
		}

	}

	private class MakefileLabelProvider extends LabelProvider implements ILabelProvider {

		/* (non-Javadoc)
		* @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		*/
		public Image getImage(Object element) {
			if (element instanceof ITargetRule) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_TARGET_RULE);
			} else if (element instanceof IInferenceRule) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_INFERENCE_RULE);
			} else if (element instanceof IMacroDefinition) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_MACRO);
			} else if (element instanceof ICommand) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_COMMAND);
			} else if (element instanceof IInclude) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_INCLUDE);
			} else if (element instanceof IBadDirective) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_ERROR);
			} else if (element instanceof IParent) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_RELATION);
			}
			return super.getImage(element);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			String name;
			if (element instanceof IRule) {
				name = ((IRule) element).getTarget().toString().trim();
			} else if (element instanceof IMacroDefinition) {
				name = ((IMacroDefinition) element).getName().trim();
			} else {
				name = super.getText(element);
			}
			if (name != null) {
				name = name.trim();
				if (name.length() > 25) {
					name = name.substring(0, 25) + " ..."; //$NON-NLS-1$
				}
			}
			return name;
		}

	}

	protected MakefileEditor fEditor;
	protected Object fInput;
	protected AddBuildTargetAction fAddBuildTargetAction;

	public MakefileContentOutlinePage(MakefileEditor editor) {
		super();
		fEditor = editor;
		fAddBuildTargetAction = new AddBuildTargetAction(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new MakefileContentProvider());
		viewer.setLabelProvider(new MakefileLabelProvider());
		if (fInput != null) {
			viewer.setInput(fInput);
		}

		MenuManager manager= new MenuManager("#MakefileOutlinerContext"); //$NON-NLS-1$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager m) {
				contextMenuAboutToShow(m);
			}
		});
		Control tree = viewer.getControl();
		Menu menu = manager.createContextMenu(tree);
		tree.setMenu(menu);
                 
		IPageSite site= getSite();
		site.registerContextMenu(MakeUIPlugin.getPluginId() + ".outline", manager, viewer); //$NON-NLS-1$
		site.setSelectionProvider(viewer);

	}

	/**
	 * called to create the context menu of the outline
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		if (fAddBuildTargetAction.canActionBeAdded(getSelection()))
			menu.add(fAddBuildTargetAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$
	}

	/**
	 * Sets the input of the outline page
	 */
	public void setInput(Object input) {
		fInput = input;
		update();
	}

	public Object getInput() {
		return fInput;
	}

	/**
	 * Updates the outline page.
	 */
	public void update() {
		final TreeViewer viewer = getTreeViewer();

		if (viewer != null) {
			final Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!control.isDisposed()) {
							control.setRedraw(false);
							viewer.setInput(fInput);
							viewer.expandAll();
							control.setRedraw(true);
						}
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
		IToolBarManager toolBarManager= actionBars.getToolBarManager();

		LexicalSortingAction action= new LexicalSortingAction(getTreeViewer());
		toolBarManager.add(action);
	}

}
