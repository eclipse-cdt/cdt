/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Patrick Hofer  [bug 325799]
 *     Lidia Popescu (Wind River) [536255] Extension point for open call hierarchy view
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.HashMap;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class CHLabelProvider extends AppearanceAwareLabelProvider {
	private final static long LABEL_OPTIONS_SIMPLE = CElementLabels.ALL_FULLY_QUALIFIED
			| CElementLabels.M_PARAMETER_TYPES | CElementLabels.M_APP_RETURNTYPE | CElementLabels.F_APP_TYPE_SIGNATURE
			| CElementLabels.TEMPLATE_ARGUMENTS;
	private final static long LABEL_OPTIONS_SHOW_FILES = LABEL_OPTIONS_SIMPLE | CElementLabels.MF_POST_FILE_QUALIFIED;

	private CUILabelProvider fCLabelProvider = new CUILabelProvider(LABEL_OPTIONS_SIMPLE,
			CElementImageProvider.OVERLAY_ICONS);
	private CHContentProvider fContentProvider;
	private HashMap<String, Image> fCachedImages = new HashMap<>();
	private Color fColorInactive;
	private IStyledLabelProvider[] fProviders;
	private CHViewPart fView;

	public CHLabelProvider(CHViewPart view, Display display, CHContentProvider cp) {
		fColorInactive = display.getSystemColor(SWT.COLOR_DARK_GRAY);
		fContentProvider = cp;
		fView = view;
	}

	@Override
	public Image getImage(Object element) {
		if (fProviders == null) {
			fProviders = fView.getLabelProviders();
		}
		if (fProviders != null) {
			for (IStyledLabelProvider provider : fProviders) {
				Image img = provider.getImage(element);
				if (img != null)
					return img;
			}
		}
		if (element instanceof CHNode) {
			CHNode node = (CHNode) element;
			Image image = null;
			if (node.isInitializer()) {
				ImageDescriptor desc = CElementImageProvider.getFunctionImageDescriptor();
				image = CUIPlugin.getImageDescriptorRegistry().get(desc);
			} else {
				ICElement decl = node.getOneRepresentedDeclaration();
				if (decl != null) {
					image = fCLabelProvider.getImage(decl);
				}
			}
			if (image != null) {
				return decorateImage(image, node);
			}
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof CHNode) {
			CHNode node = (CHNode) element;
			ICElement decl = node.getOneRepresentedDeclaration();
			if (decl != null) {
				String label;
				if (node.isMultiDef()) {
					long options = fCLabelProvider.getTextFlags();
					fCLabelProvider.setTextFlags(LABEL_OPTIONS_SIMPLE);
					label = fCLabelProvider.getText(decl);
					fCLabelProvider.setTextFlags(options);
				} else {
					label = fCLabelProvider.getText(decl);
					if (node.isInitializer()) {
						label = addInitializerDecoration(label);
					}
				}
				int refCount = node.getReferenceCount();
				if (refCount > 1) {
					label += NLS.bind(" ({0} {1})", Integer.valueOf(refCount), CHMessages.CHLabelProvider_matches); //$NON-NLS-1$
				}
				return decorateText(label, element);
			}
		}
		return super.getText(element);
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (fProviders == null) {
			fProviders = fView.getLabelProviders();
		}
		if (fProviders != null) {
			for (IStyledLabelProvider provider : fProviders) {
				StyledString styledString = provider.getStyledText(element);
				if (styledString != null)
					return styledString;
			}
		}
		if (element instanceof CHNode) {
			CHNode node = (CHNode) element;
			ICElement decl = node.getOneRepresentedDeclaration();
			if (decl != null) {
				StyledString label;
				if (node.isMultiDef()) {
					long options = fCLabelProvider.getTextFlags();
					fCLabelProvider.setTextFlags(LABEL_OPTIONS_SIMPLE);
					label = fCLabelProvider.getStyledText(decl);
					fCLabelProvider.setTextFlags(options);
				} else {
					label = fCLabelProvider.getStyledText(decl);
					if (node.isInitializer()) {
						label = addInitializerDecoration(label);
					}
				}
				int refCount = node.getReferenceCount();
				if (refCount > 1) {
					final int offset = label.length();
					label.append(NLS.bind(" ({0} {1})", Integer.valueOf(refCount), CHMessages.CHLabelProvider_matches)); //$NON-NLS-1$
					label.setStyle(offset, label.length() - offset, StyledString.COUNTER_STYLER);

				}
				String decorated = decorateText(label.getString(), element);
				if (decorated != null) {
					return StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.DECORATIONS_STYLER,
							label);
				}
				return label;
			}
		}
		return fCLabelProvider.getStyledText(element);
	}

	private String addInitializerDecoration(String label) {
		int i = 0;
		char[] content = label.toCharArray();
		for (i = 0; i < content.length; i++) {
			char c = content[i];
			if (c == '-' || Character.isWhitespace(c)) {
				break;
			}
		}
		StringBuilder buf = new StringBuilder(label.length() + 10);
		buf.append("{init "); //$NON-NLS-1$
		buf.append(content, 0, i);
		buf.append("}()"); //$NON-NLS-1$
		buf.append(content, i, content.length - i);

		return buf.toString();
	}

	private StyledString addInitializerDecoration(StyledString label) {
		int i = 0;
		char[] content = label.toString().toCharArray();
		for (i = 0; i < content.length; i++) {
			char c = content[i];
			if (c == '-' || Character.isWhitespace(c)) {
				break;
			}
		}
		StyledString label2 = new StyledString();
		label2.append("{init "); //$NON-NLS-1$
		i += label2.length();
		label2.append(label);
		label2.insert('}', i++);
		label2.insert('(', i++);
		label2.insert(')', i++);
		label2.setStyle(0, i, null);
		return label2;
	}

	@Override
	public void dispose() {
		fCLabelProvider.dispose();
		for (Image image : fCachedImages.values()) {
			image.dispose();
		}
		fCachedImages.clear();
		super.dispose();
	}

	private Image decorateImage(Image image, CHNode node) {
		int flags = 0;
		if (node.isRecursive()) {
			flags |= CElementImageDescriptor.RECURSIVE_RELATION;
		} else if (fContentProvider.hasChildren(node)) {
			if (fContentProvider.getComputeReferencedBy()) {
				flags |= CElementImageDescriptor.REFERENCED_BY;
			} else {
				if (node.isMultiDef()) {
					flags |= CElementImageDescriptor.RELATES_TO_MULTIPLE;
				} else {
					flags |= CElementImageDescriptor.RELATES_TO;
				}
			}
		}
		if (node.isReadAccess()) {
			flags |= CElementImageDescriptor.READ_ACCESS;
		}
		if (node.isWriteAccess()) {
			flags |= CElementImageDescriptor.WRITE_ACCESS;
		}

		String key = image.toString() + String.valueOf(flags);
		Image result = fCachedImages.get(key);
		if (result == null) {
			ImageDescriptor desc = new CElementImageDescriptor(new ImageImageDescriptor(image), flags,
					new Point(20, 16));
			result = desc.createImage();
			fCachedImages.put(key, result);
		}
		return result;
	}

	@Override
	public Color getForeground(Object element) {
		if (element instanceof CHMultiDefNode) {
			return fColorInactive;
		}
		return null;
	}

	public void setShowFiles(boolean show) {
		fCLabelProvider.setTextFlags(show ? LABEL_OPTIONS_SHOW_FILES : LABEL_OPTIONS_SIMPLE);
	}
}
