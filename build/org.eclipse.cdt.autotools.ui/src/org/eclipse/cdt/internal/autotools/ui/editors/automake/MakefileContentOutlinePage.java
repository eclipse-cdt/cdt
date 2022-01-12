/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - convert to use with Automake editor
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.internal.autotools.ui.MakeUIImages;
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
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * MakefileContentOutlinePage
 */
public class MakefileContentOutlinePage extends ContentOutlinePage {

	private class MakefileContentProvider implements ITreeContentProvider {

		protected boolean showMacroDefinition = true;
		protected boolean showTargetRule = true;
		protected boolean showInferenceRule = true;
		protected boolean showIncludeChildren = false;

		protected IMakefile makefile;
		protected IMakefile nullMakefile = new NullMakefile();

		@Override
		public Object[] getChildren(Object element) {
			if (element == fInput) {
				return getElements(makefile);
			} else if (element instanceof IDirective) {
				return getElements(element);
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IMakefile) {
				return fInput;
			} else if (element instanceof IDirective) {
				return ((IDirective) element).getParent();
			}
			return fInput;
		}

		@Override
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

		@Override
		public Object[] getElements(Object inputElement) {
			IDirective[] directives;
			if (inputElement == fInput) {
				directives = makefile.getDirectives();
			} else if (inputElement instanceof IRule) {
				directives = ((IRule) inputElement).getCommands();
			} else if (inputElement instanceof IParent) {
				if (inputElement instanceof IInclude && !showIncludeChildren) {
					directives = new IDirective[0];
				} else {
					directives = ((IParent) inputElement).getDirectives();
				}
			} else {
				directives = new IDirective[0];
			}
			List<Object> list = new ArrayList<>(directives.length);
			for (int i = 0; i < directives.length; i++) {
				if (showMacroDefinition && directives[i] instanceof IMacroDefinition) {
					list.add(directives[i]);
				} else if (showInferenceRule && directives[i] instanceof IInferenceRule) {
					list.add(directives[i]);
				} else if (showTargetRule && directives[i] instanceof ITargetRule) {
					list.add(directives[i]);
				} else {
					boolean irrelevant = (directives[i] instanceof IComment || directives[i] instanceof IEmptyLine
							|| directives[i] instanceof ITerminal);
					if (!irrelevant) {
						list.add(directives[i]);
					}
				}
			}
			return list.toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput != null) {
				makefile = nullMakefile;
			}

			if (newInput != null) {
				IWorkingCopyManager manager = AutomakeEditorFactory.getDefault().getWorkingCopyManager();
				makefile = manager.getWorkingCopy((IEditorInput) newInput);
				if (makefile == null) {
					makefile = nullMakefile;
				}
			}
		}

	}

	private static class MakefileLabelProvider extends LabelProvider {

		@Override
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

		@Override
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
	protected OpenIncludeAction fOpenIncludeAction;

	public MakefileContentOutlinePage(MakefileEditor editor) {
		super();
		fEditor = editor;
		fOpenIncludeAction = new OpenIncludeAction(this);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new MakefileContentProvider());
		viewer.setLabelProvider(new MakefileLabelProvider());
		if (fInput != null) {
			viewer.setInput(fInput);
		}

		MenuManager manager = new MenuManager("#MakefileOutlinerContext"); //$NON-NLS-1$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(m -> contextMenuAboutToShow(m));
		Control tree = viewer.getControl();
		Menu menu = manager.createContextMenu(tree);
		tree.setMenu(menu);

		viewer.addDoubleClickListener(event -> {
			if (fOpenIncludeAction != null) {
				fOpenIncludeAction.run();
			}
		});

		IPageSite site = getSite();
		//FIXME: should pluginid below be MakeUIPlugin id?
		site.registerContextMenu(AutotoolsUIPlugin.getPluginId() + ".outline", manager, viewer); //$NON-NLS-1$
		site.setSelectionProvider(viewer);

	}

	/**
	 * called to create the context menu of the outline
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		if (OpenIncludeAction.canActionBeAdded(getSelection())) {
			menu.add(fOpenIncludeAction);
		}
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));//$NON-NLS-1$
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
				control.getDisplay().asyncExec(() -> {
					if (!control.isDisposed()) {
						control.setRedraw(false);
						viewer.setInput(fInput);
						viewer.expandAll();
						control.setRedraw(true);
					}
				});
			}
		}
	}

}
