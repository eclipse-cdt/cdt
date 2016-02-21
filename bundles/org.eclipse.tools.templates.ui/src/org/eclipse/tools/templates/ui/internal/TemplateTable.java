/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tools.templates.ui.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

public class TemplateTable implements Listener {

	private final Table table;
	private Font fontBold;
	private Font fontDefault;
	private Map<ImageDescriptor, Image> images = new HashMap<>();

	private static final Rectangle EMPTY_IMAGE_BOUNDS = new Rectangle(0, 0, 48, 48);

	public TemplateTable(Composite parent, int style) {
		table = new Table(parent, style);
		table.addListener(SWT.MeasureItem, this);
		table.addListener(SWT.EraseItem, this);
		table.addListener(SWT.PaintItem, this);
		table.addListener(SWT.Dispose, this);
	}

	public Table getTable() {
		return table;
	}

	public void setTemplates(Collection<Template> templates) {
		table.clearAll();
		for (Template template : templates) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(template);
		}
	}

	public Template getSelectedTemplate() {
		TableItem[] items = table.getSelection();
		if (items.length > 0) {
			return (Template) items[0].getData();
		} else {
			return null;
		}
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
		case SWT.MeasureItem:
			computeItemArea(event);
			break;
		case SWT.PaintItem:
			paintItem(event);
			break;
		case SWT.EraseItem:
			computeItemArea(event);
			break;
		case SWT.Dispose:
			for (Image image : images.values()) {
				image.dispose();
			}
			break;
		}
	}

	private void computeItemArea(Event event) {
		event.width = table.getClientArea().width - event.x;
		event.height = 56; // 48 for icon, 8 buffer
	}

	private void paintItem(Event event) {
		Widget w = event.item;
		GC gc = event.gc;
		if (fontDefault == null) {
			Font font = gc.getFont();
			FontData[] data = font.getFontData();
			if (data.length > 0) {
				FontData d = data[0];
				FontData normal = new FontData(d.getName(), d.getHeight(), // Math.round(d.getHeight()
																			// *
																			// .85F),
						d.getStyle() | SWT.ITALIC);
				fontDefault = new Font(event.display, normal);
				FontData bold = new FontData(d.getName(), Math.round(d.getHeight() * 1.15F), d.getStyle() | SWT.BOLD);
				fontBold = new Font(event.display, bold);
			}
		}
		if (w instanceof TableItem) {
			TableItem item = (TableItem) w;
			Template template = (Template) item.getData();
			ImageDescriptor imageDesc = template.getIcon();
			Image image = images.get(imageDesc);
			if (image == null) {
				image = imageDesc.createImage();
				images.put(imageDesc, image);
			}
			Rectangle rect = EMPTY_IMAGE_BOUNDS;
			int y = 0;
			if (image != null) {
				rect = image.getBounds();
				y = event.y + Math.max(0, (event.height - rect.height) / 2);
				gc.drawImage(image, event.x + 4, y);
			}
			gc.setFont(fontBold);
			String name = template.getLabel();
			Point nameExtent = gc.textExtent(name, SWT.DRAW_TRANSPARENT);
			int iconMargin = 10;
			gc.drawText(name, rect.x + rect.width + iconMargin, y, SWT.DRAW_TRANSPARENT);
			gc.setFont(fontDefault);
			int flags = SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;
			String description = template.getDescription();
			int width = table.getClientArea().width;
			if (description != null) {
				Point descExt = gc.textExtent(description, flags);
				int descWidth = width - (rect.x + rect.width + iconMargin);
				if (descExt.x > descWidth) {
					FontMetrics fm = gc.getFontMetrics();
					int averageCharWidth = fm.getAverageCharWidth();
					int charsMaxNumberInRow = descWidth / averageCharWidth;
					if (description.length() > charsMaxNumberInRow) {
						String firstLine = description.substring(0, charsMaxNumberInRow);
						int lastWS = firstLine.lastIndexOf(' ');
						int endIndex = lastWS + 1 + charsMaxNumberInRow;
						description = firstLine.substring(0, lastWS) + '\n'
								+ description.substring(lastWS + 1, Math.min(endIndex, description.length()));
					}
				}
				gc.drawText(description, rect.x + rect.width + iconMargin, y + nameExtent.y + 2, flags);
			}
		}
	}

}
