/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tools.templates.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class TagListViewer extends ListViewer {

	private Tag[] tags;

	public TagListViewer(Composite parent, int style) {
		super(parent, style);

		setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Tag) {
					return ((Tag) element).getLabel();
				} else {
					return super.getText(element);
				}
			}
		});

		setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				if (newInput != null) {
					@SuppressWarnings("unchecked")
					Collection<Tag> tagsList = (Collection<Tag>) newInput;
					tags = tagsList.toArray(new Tag[tagsList.size()]);
					Arrays.sort(tags, new Comparator<Tag>() {
						@Override
						public int compare(Tag o1, Tag o2) {
							// Keep all at the top
							if (o1.getId().equals(Tag.ALL_ID)) {
								return -1;
							}
							if (o2.getId().equals(Tag.ALL_ID)) {
								return 1;
							}
							return o1.getLabel().compareTo(o2.getLabel());
						}
					});
				}
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return tags;
			}
		});
	}

	public Collection<Tag> getSelectedTags() {
		List<Tag> selectedTags = new ArrayList<>();
		Iterator<Object> i = getStructuredSelection().iterator();
		while (i.hasNext()) {
			Tag tag = (Tag) i.next();
			selectedTags.add(tag);
		}

		return selectedTags;
	}

}
