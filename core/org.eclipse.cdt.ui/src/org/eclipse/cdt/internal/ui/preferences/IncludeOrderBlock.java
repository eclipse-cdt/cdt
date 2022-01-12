/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle.IncludeKind;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The preference block for configuring relative order of include statements.
 */
public class IncludeOrderBlock extends OptionsConfigurationBlock {
	private static final int IDX_UP = 0;
	private static final int IDX_DOWN = 1;
	private static final String[] UP_DOWN_LABELS = { PreferencesMessages.IncludeOrderBlock_up,
			PreferencesMessages.IncludeOrderBlock_down };

	private final List<IncludeGroupStyle> styles;
	private Map<IncludeKind, IncludeGroupStyle> stylesByKind;
	private GroupListField includeGroupList;
	private PixelConverter pixelConverter;

	public IncludeOrderBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container,
			List<IncludeGroupStyle> styles) {
		super(context, project, new Key[0], container);
		this.styles = styles;
	}

	@Override
	protected Control createContents(Composite parent) {
		pixelConverter = new PixelConverter(parent);

		setShell(parent.getShell());

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		includeGroupList = new GroupListField();
		includeGroupList.setLabelText(PreferencesMessages.IncludeOrderBlock_order_of_includes);

		Label label = includeGroupList.getLabelControl(composite);
		label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));

		Control control = includeGroupList.getListControl(composite);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = pixelConverter.convertWidthInCharsToPixels(50);
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(5);
		control.setLayoutData(gd);
		Control buttonsControl = includeGroupList.getButtonBox(composite);
		buttonsControl.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));

		updateControls();
		return composite;
	}

	@Override
	protected void updateControls() {
		super.updateControls();
		stylesByKind = getStylesByKind(styles);
		List<IncludeGroupStyle> orderedStyles = new ArrayList<>(styles);
		Collections.sort(orderedStyles); // Sort according to values returned by getOrder() method.
		List<IncludeGroupStyle> groupedStyles = new ArrayList<>();
		int order = 0;
		for (IncludeGroupStyle style : orderedStyles) {
			style.setOrder(order++);
			IncludeKind includeKind = style.getIncludeKind();
			if (style.isKeepTogether()
					&& (!includeKind.hasChildren() || hasUngroupedChildren(includeKind, stylesByKind))) {
				groupedStyles.add(style);
			}
		}
		// Adjust order of groups to satisfy higher order grouping.
		for (int i = 0; i < groupedStyles.size(); i++) {
			IncludeGroupStyle style = groupedStyles.get(i);
			IncludeKind groupingKind = getGroupingParentKind(style);
			if (groupingKind != null) {
				while (++i < groupedStyles.size() && getGroupingParentKind(groupedStyles.get(i)) == groupingKind) {
				}
				for (int j = i + 1; j < groupedStyles.size(); j++) {
					if (getGroupingParentKind(groupedStyles.get(j)) == groupingKind) {
						groupedStyles.add(i++, groupedStyles.remove(j));
					}
				}
			}
		}
		includeGroupList.setElements(groupedStyles);
	}

	private boolean areKeptTogether(IncludeGroupStyle style1, IncludeGroupStyle style2) {
		IncludeKind kind = getGroupingParentKind(style1);
		return kind != null && kind == getGroupingParentKind(style2);
	}

	private IncludeKind getGroupingParentKind(IncludeGroupStyle style) {
		IncludeKind kind = style.getIncludeKind().parent;
		if (kind == null)
			return null;
		while (true) {
			// "Other" include kind is special since it applies only to non grouped includes.
			if (kind == IncludeKind.OTHER && style.isKeepTogether())
				break;
			IncludeGroupStyle parent = stylesByKind.get(kind);
			if (parent != null && parent.isKeepTogether())
				return kind;
			if (kind == IncludeKind.OTHER)
				break;
			kind = IncludeKind.OTHER;
		}
		return null;
	}

	private static Map<IncludeKind, IncludeGroupStyle> getStylesByKind(List<IncludeGroupStyle> styles) {
		Map<IncludeKind, IncludeGroupStyle> stylesByKind = new HashMap<>();
		for (IncludeGroupStyle style : styles) {
			if (style.getIncludeKind() != IncludeKind.MATCHING_PATTERN)
				stylesByKind.put(style.getIncludeKind(), style);
		}
		return stylesByKind;
	}

	private boolean hasUngroupedChildren(IncludeKind includeKind, Map<IncludeKind, IncludeGroupStyle> stylesByKind) {
		// This code relies on the fact that IncludeKind hierarchy is only two levels deep.
		for (IncludeKind childKind : includeKind.children) {
			if (!stylesByKind.get(childKind).isKeepTogether())
				return true;
		}
		// "Other" include kind is special since it effectively includes all other ungrouped includes.
		if (includeKind == IncludeKind.OTHER) {
			for (IncludeKind kind : stylesByKind.keySet()) {
				if (kind != IncludeKind.OTHER && kind.hasChildren() && !stylesByKind.get(kind).isKeepTogether()
						&& hasUngroupedChildren(kind, stylesByKind)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		fContext.statusChanged(new StatusInfo());
	}

	private class GroupListField extends ListDialogField<IncludeGroupStyle> {

		GroupListField() {
			super(null, UP_DOWN_LABELS, new GroupLabelProvider());
		}

		@Override
		public void dialogFieldChanged() {
			super.dialogFieldChanged();
			int order = 0;
			for (IncludeGroupStyle style : getElements()) {
				style.setOrder(order++);
			}
		}

		@Override
		protected boolean getManagedButtonState(ISelection sel, int index) {
			if (index == IDX_UP) {
				return !sel.isEmpty() && canMoveUp();
			} else if (index == IDX_DOWN) {
				return !sel.isEmpty() && canMoveDown();
			}
			return true;
		}

		@Override
		protected boolean managedButtonPressed(int index) {
			if (index == IDX_UP) {
				up();
			} else if (index == IDX_DOWN) {
				down();
			} else {
				return false;
			}
			return true;
		}

		@Override
		protected void up() {
			boolean[] selected = getSelectionMask(false);
			extendSelectionForMovingUp(selected, fElements);
			if (selected != null) {
				setElements(moveUp(fElements, selected));
				fTable.reveal(fElements.get(getFirstSelected(selected)));
			}
		}

		@Override
		protected void down() {
			boolean[] selected = getSelectionMask(true);
			List<IncludeGroupStyle> reversed = reverse(fElements);
			extendSelectionForMovingUp(selected, reversed);
			if (selected != null) {
				setElements(reverse(moveUp(reversed, selected)));
				fTable.reveal(fElements.get(getFirstSelected(selected)));
			}
		}

		private List<IncludeGroupStyle> reverse(List<IncludeGroupStyle> p) {
			List<IncludeGroupStyle> reverse = new ArrayList<>(p.size());
			for (int i = p.size(); --i >= 0;) {
				reverse.add(p.get(i));
			}
			return reverse;
		}

		private boolean[] getSelectionMask(boolean reverse) {
			boolean[] selectionMask = null;
			if (isOkToUse(fTableControl)) {
				int nElements = fElements.size();
				for (int i : fTable.getTable().getSelectionIndices()) {
					if (selectionMask == null)
						selectionMask = new boolean[nElements];
					selectionMask[reverse ? nElements - 1 - i : i] = true;
				}
			}
			return selectionMask;
		}

		private void extendSelectionForMovingUp(boolean[] selection, List<IncludeGroupStyle> styles) {
			for (int i = 1; i < selection.length; i++) {
				int j = i - 1;
				if (!selection[i] && selection[j] && areKeptTogether(styles.get(i), styles.get(j))) {
					selection[i] = true;
				}
			}
		}

		private List<IncludeGroupStyle> moveUp(List<IncludeGroupStyle> elements, boolean[] selected) {
			int nElements = elements.size();
			List<IncludeGroupStyle> res = new ArrayList<>(nElements);
			List<IncludeGroupStyle> floating = new ArrayList<>();
			for (int i = 0; i < nElements; i++) {
				IncludeGroupStyle curr = elements.get(i);
				if (selected[i]) {
					res.add(curr);
				} else {
					if (!floating.isEmpty() && !areKeptTogether(curr, floating.get(0))) {
						res.addAll(floating);
						floating.clear();
					}
					floating.add(curr);
				}
			}
			res.addAll(floating);
			return res;
		}

		private int getFirstSelected(boolean[] selected) {
			for (int i = 0; i < selected.length; i++) {
				if (selected[i])
					return i;
			}
			return -1;
		}

		@Override
		protected boolean canMoveUp() {
			boolean[] selected = getSelectionMask(false);
			if (selected == null || selected[0])
				return false;

			return canMoveUp(fElements, selected);
		}

		@Override
		protected boolean canMoveDown() {
			boolean[] selected = getSelectionMask(true);
			if (selected == null || selected[0])
				return false;

			return canMoveUp(reverse(fElements), selected);
		}

		private boolean canMoveUp(List<IncludeGroupStyle> elements, boolean[] selected) {
			for (int i = 1; i < selected.length; i++) {
				int j = i - 1;
				if (selected[i] && !selected[j] && areKeptTogether(elements.get(i), elements.get(j))) {
					while (++i < selected.length && selected[i]) {
					}
					if (!areKeptTogether(elements.get(i - 1), elements.get(j)))
						return false; // Cannot break a group.
				}
			}
			return true;
		}
	}

	private static class GroupLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			IncludeGroupStyle style = (IncludeGroupStyle) element;
			String name = style.getName();
			if (name == null) {
				name = style.getIncludeKind().name;
			}
			return name;
		}
	}
}
