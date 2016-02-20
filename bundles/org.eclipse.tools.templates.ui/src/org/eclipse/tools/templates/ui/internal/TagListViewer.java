package org.eclipse.tools.templates.ui.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class TagListViewer extends ListViewer {

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
			private Tag[] tags;

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

}
