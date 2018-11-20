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
 *     Red Hat Inc. - Modified from MakefileContentOutlinePage for Automake files
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.autotools.ui.MakeUIImages;
import org.eclipse.cdt.internal.autotools.ui.editors.LexicalSortingAction;
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
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class AutomakefileContentOutlinePage extends ContentOutlinePage {

	protected IMakefile makefile;
	protected IMakefile nullMakefile = new NullMakefile();
	private LexicalSortingAction sortAction;

	private class AutomakefileContentProvider implements ITreeContentProvider {

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
			List<IDirective> list = new ArrayList<>(directives.length);
			for (int i = 0; i < directives.length; i++) {
				if (showMacroDefinition && directives[i] instanceof IMacroDefinition) {
					list.add(directives[i]);
				} else if (showInferenceRule && directives[i] instanceof IInferenceRule) {
					list.add(directives[i]);
				} else if (showTargetRule && directives[i] instanceof ITargetRule) {
					list.add(directives[i]);
				} else if (showTargetRule && directives[i] instanceof AutomakeIfElse) {
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

	private static class AutomakefileLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			if (element instanceof ITargetRule) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_TARGET_RULE);
			} else if (element instanceof IInferenceRule) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_INFERENCE_RULE);
			} else if (element instanceof IMacroDefinition) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_MACRO);
			} else if (element instanceof IAutomakeConditional) {
				// Must process this before ICommand because if/else are also ICommands
				return super.getImage(element);
			} else if (element instanceof ICommand) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_COMMAND);
			} else if (element instanceof IInclude) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_INCLUDE);
			} else if (element instanceof IBadDirective) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_ERROR);
			} else if (element instanceof AutomakeConfigMacro) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_ACMACRO);
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
			} else if (element instanceof AutomakeIfElse) {
				AutomakeIfElse ifelse = (AutomakeIfElse) element;
				// FIXME:  make this not a string comparison
				if (ifelse.getType().equals("if")) {
					name = "if " + ifelse.getCondition();
				} else
					name = "else";
			} else if (element instanceof AutomakeConfigMacro) {
				AutomakeConfigMacro macro = (AutomakeConfigMacro) element;
				name = macro.getName();
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

	protected AutomakeEditor fEditor;
	protected Object fInput;

	public AutomakefileContentOutlinePage(AutomakeEditor editor) {
		super();
		fEditor = editor;
	}

	/**
	 * Sets the input of the outline page
	 */
	public void setInput(Object input) {
		fInput = input;
		update();
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

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new AutomakefileContentProvider());
		viewer.setLabelProvider(new AutomakefileLabelProvider());
		if (fInput != null) {
			viewer.setInput(fInput);
		}
		sortAction.setTreeViewer(viewer);
	}

	public void inputChanged(Object oldInput, Object newInput) {
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

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		IToolBarManager toolBarManager = pageSite.getActionBars().getToolBarManager();
		sortAction = new LexicalSortingAction();
		toolBarManager.add(sortAction);
	}

}
