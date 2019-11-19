/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tools.templates.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
		table.removeAll();
		List<Template> sorted = new ArrayList<>(templates);
		Collections.sort(sorted, new Comparator<Template>() {
			@Override
			public int compare(Template o1, Template o2) {
				return o1.getLabel().compareToIgnoreCase(o2.getLabel());
			}
		});
		for (Template template : sorted) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(template);
			// Since we have nothing to help SWTBot find items, store the label
			item.setData("org.eclipse.swtbot.widget.key", template.getLabel()); //$NON-NLS-1$
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

	public void selectTemplate(Template template) {
		if (template == null) {
			return;
		}

		int i = 0;
		for (TableItem item : table.getItems()) {
			if (template.equals(item.getData())) {
				table.select(i);
				break;
			}
			i++;
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

	private void initFonts(GC gc) {
		if (fontDefault == null) {
			Font font = gc.getFont();
			FontData[] data = font.getFontData();
			if (data.length > 0) {
				Display display = table.getDisplay();
				FontData d = data[0];
				FontData normal = new FontData(d.getName(), d.getHeight(), d.getStyle() | SWT.ITALIC);
				fontDefault = new Font(display, normal);
				FontData bold = new FontData(d.getName(), Math.round(d.getHeight() * 1.15F), d.getStyle() | SWT.BOLD);
				fontBold = new Font(display, bold);
			}
		}
	}

	private void computeItemArea(Event event) {
		GC gc = event.gc;
		FontMetrics metrics = gc.getFontMetrics();
		int height = (int) (metrics.getHeight() * 3.15);

		event.width = table.getClientArea().width - event.x;
		event.height = Math.max(48, height) + 8; // 48 for icon/text, 8 margin
	}

	private void paintItem(Event event) {
		Widget w = event.item;
		GC gc = event.gc;
		initFonts(gc);
		if (w instanceof TableItem) {
			TableItem item = (TableItem) w;
			Template template = (Template) item.getData();

			// image
			ImageDescriptor imageDesc = template.getIcon();
			Image image = images.get(imageDesc);
			if (image == null && imageDesc != null) {
				image = imageDesc.createImage();
				images.put(imageDesc, image);
			}
			if (image != null) {
				gc.drawImage(image, event.x, event.y + Math.max(0, (event.height - 48) / 2));
			}

			int imageWidth = 48 + 6; // icon plus margin

			// name in bold
			gc.setFont(fontBold);
			String name = template.getLabel();
			Point nameExtent = gc.textExtent(name, SWT.DRAW_TRANSPARENT);
			gc.drawText(name, event.x + imageWidth, event.y, SWT.DRAW_TRANSPARENT);

			// description in one or two lines
			String description = template.getDescription();
			if (description != null) {
				gc.setFont(fontDefault);
				int flags = SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;
				int width = table.getClientArea().width;
				Point descExt = gc.textExtent(description, flags);
				int descWidth = width - imageWidth;
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
				gc.drawText(description, event.x + imageWidth, event.y + nameExtent.y, flags);
			}
		}
	}

}
