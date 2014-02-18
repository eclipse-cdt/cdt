package org.eclipse.cdt.internal.ui.refactoring.pullup.ui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.ui.CDTSharedImages;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;
import org.eclipse.cdt.internal.ui.refactoring.pullup.PullUpInformation;

public class MemberTreeLabelProvider implements ITableLabelProvider {
	

	public MemberTreeLabelProvider(PullUpInformation information) {
	}

	@Override
	public Image getColumnImage(Object element, int column) {
		if (column == 0) {
			if (element instanceof SubClassTreeEntry) {
				final SubClassTreeEntry tte = (SubClassTreeEntry) element;
				return MemberTableEntry.DECLARATOR_LABEL_PROVIDER.getImage(tte.getMember());
			} else if (element instanceof InheritanceLevel) {
				return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CLASS);
			}
			throw new IllegalArgumentException();
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int column) {
		switch (column) {
		case 0:
			if (element instanceof SubClassTreeEntry) {
				final SubClassTreeEntry tte = (SubClassTreeEntry) element;
				return MemberTableEntry.DECLARATOR_LABEL_PROVIDER.getText(tte.getMember());
			} else if (element instanceof InheritanceLevel) {
				final InheritanceLevel lvl = (InheritanceLevel) element;
				return lvl.getClazz().getName();
			}
			throw new IllegalArgumentException();
		}
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}
}
