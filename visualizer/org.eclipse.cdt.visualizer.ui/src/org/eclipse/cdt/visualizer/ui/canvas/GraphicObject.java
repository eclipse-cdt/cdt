/*******************************************************************************
 * Copyright (c) 2012, 2015 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *     Xavier Raynaud (Kalray) - Bug 430804
 *     Marc Dumais (Ericsson) - Bug 458644
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.canvas;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

// ---------------------------------------------------------------------------
// GraphicObject
// ---------------------------------------------------------------------------

/**
 * Graphic object base class.
 * Base class for objects that can be displayed and manipulated on a GraphicCanvas.
 */
public class GraphicObject implements IGraphicObject, ITooltipProvider {
	// --- members ---

	/** Data object, if any, associated with this graphic object. */
	protected Object m_data = null;

	/** Bounding rectangle of this element. */
	protected Rectangle m_bounds = new Rectangle(0, 0, 0, 0);

	/** Whether this element is visible. */
	protected boolean m_visible = true;

	/** Whether this element is selected. */
	protected boolean m_selected = false;

	/** Foreground color (null means inherit from canvas) */
	protected Color m_foreground = null;

	/** Background color (null means inherit from canvas) */
	protected Color m_background = null;

	// --- enums ---

	/**
	 * Pre-defined sizes/positions for super-imposing images on graphic objects.
	 * @since 1.3
	 */
	public enum ImageSizeAndPosition {
		/** Image occupies upper-left quadrant of graphic object's area */
		UPPER_LEFT_QUADRANT(new Rectangle(0, 0, 50, 50)),
		/** Image occupies upper-right quadrant of graphic object's area */
		UPPER_RIGHT_QUADRANT(new Rectangle(50, 0, 50, 50)),
		/** Image occupies lower-left quadrant of graphic object's area */
		LOWER_LEFT_QUADRANT(new Rectangle(0, 50, 50, 50)),
		/** Image occupies lower-right quadrant of graphic object's area */
		LOWER_RIGHT_QUADRANT(new Rectangle(50, 50, 50, 50)),
		/** Image occupies lower-right 1/8 of graphic object's area */
		LOWER_RIGHT_HEIGHTH(new Rectangle(75, 75, 25, 25)),
		/** Image occupies upper-left 7/8 of graphic object's area */
		UPPER_LEFT_SEVENTH_HEIGHTH(new Rectangle(0, 0, 75, 75)),
		/** Image completely occupies the graphic object's area */
		MAXSIZE(new Rectangle(0, 0, 100, 100));

		private Rectangle value;

		private ImageSizeAndPosition(Rectangle sizeAndPos) {
			value = sizeAndPos;
		}

		public Rectangle getValue() {
			return value;
		}
	}

	// --- constructors/destructors ---

	/** Constructor. */
	public GraphicObject() {
	}

	/** Constructor. */
	public GraphicObject(int x, int y, int width, int height) {
		m_bounds.x = x;
		m_bounds.y = y;
		m_bounds.width = width;
		m_bounds.height = height;
	}

	/** Constructor. */
	public GraphicObject(Rectangle bounds) {
		m_bounds.x = bounds.x;
		m_bounds.y = bounds.y;
		m_bounds.width = bounds.width;
		m_bounds.height = bounds.height;
	}

	/** Constructor. */
	public GraphicObject(Object data) {
		m_data = data;
	}

	/** Constructor. */
	public GraphicObject(int x, int y, int width, int height, Object data) {
		m_bounds.x = x;
		m_bounds.y = y;
		m_bounds.width = width;
		m_bounds.height = height;
		m_data = data;
	}

	/** Constructor. */
	public GraphicObject(Rectangle bounds, Object data) {
		m_bounds.x = bounds.x;
		m_bounds.y = bounds.y;
		m_bounds.width = bounds.width;
		m_bounds.height = bounds.height;
		m_data = data;
	}

	/** Dispose method. */
	public void dispose() {
		m_data = null;
	}

	// --- accessors ---

	/** Gets data object associated with this view element. */
	@Override
	public Object getData() {
		return m_data;
	}

	/** Sets data object associated with this view element. */
	@Override
	public void setData(Object data) {
		m_data = data;
	}

	/** Gets x location of this element */
	public int getX() {
		return m_bounds.x;
	}

	/** Sets x location of this element */
	public void setX(int x) {
		m_bounds.x = x;
	}

	/** Gets y location of this element */
	public int getY() {
		return m_bounds.y;
	}

	/** Sets y location of this element */
	public void setY(int y) {
		m_bounds.y = y;
	}

	/** Sets x/y position of this element */
	public void setPosition(int x, int y) {
		m_bounds.x = x;
		m_bounds.y = y;
	}

	/** Gets width of this element */
	public int getWidth() {
		return m_bounds.width;
	}

	/** Sets width of this element */
	public void setWidth(int w) {
		m_bounds.width = w;
	}

	/** Gets y location of this element */
	public int getHeight() {
		return m_bounds.height;
	}

	/** Sets y location of this element */
	public void setHeight(int h) {
		m_bounds.height = h;
	}

	/** Sets width/height of this element */
	public void setSize(int w, int h) {
		m_bounds.width = w;
		m_bounds.height = h;
	}

	/** Gets bounding rectangle of this element. */
	public Rectangle getBounds() {
		return m_bounds;
	}

	/** Sets bounding rectangle of this element. */
	public void setBounds(int x, int y, int w, int h) {
		m_bounds.x = x;
		m_bounds.y = y;
		m_bounds.width = w;
		m_bounds.height = h;
	}

	/** Sets bounding rectangle of this element. */
	public void setBounds(Rectangle bounds) {
		m_bounds.x = bounds.x;
		m_bounds.y = bounds.y;
		m_bounds.width = bounds.width;
		m_bounds.height = bounds.height;
	}

	/** Returns true if element bounds contains point. */
	@Override
	public boolean contains(int x, int y) {
		return m_bounds.contains(x, y);
	}

	/** Returns true if element bounds are within specified rectangle. */
	@Override
	public boolean isWithin(Rectangle region) {
		return (region.x <= m_bounds.x && region.y <= m_bounds.y
				&& region.x + region.width >= m_bounds.x + m_bounds.width
				&& region.y + region.height >= m_bounds.y + m_bounds.height);
	}

	/** Gets whether this element is visible. */
	public boolean isVisible() {
		return m_visible;
	}

	/** Sets whether this element is visible. */
	public void setVisible(boolean visible) {
		m_visible = visible;
	}

	/** Gets whether this element is selected. */
	public boolean isSelected() {
		return m_selected;
	}

	/** Sets whether this element is selected. */
	public void setSelected(boolean selected) {
		m_selected = selected;
	}

	/** Sets foreground color (null means inherit from container) */
	public void setForeground(Color color) {
		m_foreground = color;
	}

	/** Gets foreground color (null means inherit from container) */
	public Color getForeground() {
		return m_foreground;
	}

	/** Sets foreground color (null means inherit from container) */
	public void setBackground(Color color) {
		m_background = color;
	}

	/** Gets background color (null means inherit from container) */
	public Color getBackground() {
		return m_background;
	}

	// --- methods ---

	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paint(GC gc, boolean decorations) {
		if (isVisible()) {
			// Set GC to reflect object properties, if set.
			Color oldForeground = null;
			Color oldBackground = null;
			if (m_foreground != null) {
				oldForeground = gc.getForeground();
				gc.setForeground(m_foreground);
			}
			if (m_background != null) {
				oldBackground = gc.getBackground();
				gc.setBackground(m_background);
			}

			// Paint the object.
			if (!decorations)
				paintContent(gc);
			else
				paintDecorations(gc);

			// Restore old state.
			if (m_foreground != null)
				gc.setForeground(oldForeground);
			if (m_background != null)
				gc.setBackground(oldBackground);
		}
	}

	/**
	 * Paints content of graphic object.
	 * GC has already been set to this object's
	 * current foreground/background colors.
	 * Default implementation draws object's bounding box.
	 */
	public void paintContent(GC gc) {
		// Draw boundary rectangle of object.
		gc.drawRectangle(m_bounds);

		// Display selection as thicker boundary.
		if (isSelected()) {
			int x = m_bounds.x + 1;
			int y = m_bounds.y + 1;
			int width = m_bounds.width - 2;
			if (width < 0)
				width = 0;
			int height = m_bounds.height - 2;
			if (height < 0)
				height = 0;
			gc.drawRectangle(x, y, width, height);
		}
	}

	/** Returns true if object has decorations to paint. */
	@Override
	public boolean hasDecorations() {
		return false;
	}

	/** Invoked to allow element to paint decorations
	 *  on top of other items drawn on top of it.
	 */
	public void paintDecorations(GC gc) {
	}

	/**
	 * @since 1.1
	 */
	@Override
	public String getTooltip(int x, int y) {
		return null;
	}

	/**
	 * Draws an image on the current canvas graphic element. Where the image is
	 * located, relative to the graphic object and it's horizontal and vertical
	 * scale is configurable.
	 *
	 * @param gc
	 * @param imgPath : Absolute path and name of image to display
	 * by this margin, in each dimension.
	 * @param imgRelPositionAndScale : Rectangle object, where x, y  are in % and
	 *  represent the relative position where the upper left corner of the image
	 *  will be positioned, relative to the parent graphic object.
	 *  The width and height are in % and represent the scale of the object relative
	 *  to the parent object. For example. a relative width and height of 25 means
	 *  that the image will be scaled to be 1/4 of the width and height of the parent
	 *  graphic object.
	 * @throws FileNotFoundException
	 * @since 1.3
	 */
	public void drawImage(GC gc, String imgPath, Rectangle imgRelPositionAndScale) throws FileNotFoundException {
		// by default no margin
		drawImageWithMargin(gc, imgPath, imgRelPositionAndScale, 0);
	}

	/**
	 * Draws an image on the current canvas graphic element. Where the image is
	 * located, relative to the graphic object and it's horizontal and vertical
	 * scale is configurable.
	 *
	 * @param gc
	 * @param imgPath : Absolute path and name of image to draw
	 * @param sizeAndpos : ImageSizeAndPosition enum value
	 *  represent the relative position where the upper left corner of the image
	 *  will be positioned, relative to the parent graphic object.
	 *  The width and height are in % and represent the scale of the object relative
	 *  to the parent object. For example. a relative width and height of 25 means
	 *  that the image will be scaled to be 1/4 of the width and height of the parent
	 *  graphic object.
	 * @throws FileNotFoundException
	 * @since 1.3
	 */
	public void drawImage(GC gc, String imgPath, ImageSizeAndPosition sizeAndpos) throws FileNotFoundException {
		drawImageWithMargin(gc, imgPath, sizeAndpos.getValue(), 0);
	}

	/**
	 * Draws an image on the current canvas graphic element. Where the image is
	 * located, relative to the graphic object and it's horizontal and vertical
	 * scale is configurable. This version of the method allows to specify a margin,
	 * in pixels, that is to be left, by reducing the size of the image
	 *
	 * @param gc
	 * @param imgPath : Absolute path and name of image to draw
	 * @param sizeAndpos : ImageSizeAndPosition enum value
	 *  represent the relative position where the upper left corner of the image
	 *  will be positioned, relative to the parent graphic object.
	 *  The width and height are in % and represent the scale of the object relative
	 *  to the parent object. For example. a relative width and height of 25 means
	 *  that the image will be scaled to be 1/4 of the width and height of the parent
	 *  graphic object.
	 * @param margin: margin in pixels: the image will be reduced in size
	 *  by this margin, in each dimension.
	 * @throws FileNotFoundException
	 * @since 1.3
	 */
	public void drawImageWithMargin(GC gc, String imgPath, ImageSizeAndPosition sizeAndpos, int margin)
			throws FileNotFoundException {
		drawImageWithMargin(gc, imgPath, sizeAndpos.getValue(), margin);
	}

	/**
	 * Draws an image on the current canvas graphic element. Where the image is
	 * located, relative to the graphic object and it's horizontal and vertical
	 * scale is configurable. This version of the method allows to specify a margin,
	 * in pixels, that is to be left, by reducing the size of the image
	 *
	 * @param gc
	 * @param imgPath : Absolute path and name of image to draw
	 * @param imgRelPositionAndScale : Rectangle object, where x, y  are in % and
	 *  represent the relative position where the upper left corner of the image
	 *  will be positioned, relative to the parent graphic object.
	 *  The width and height are in % and represent the scale of the object relative
	 *  to the parent object. For example. a relative width and height of 25 means
	 *  that the image will be scaled to be 1/4 of the width and height of the parent
	 *  graphic object.
	 * @param margin: margin in pixels: the image will be reduced in size
	 *  by this margin, in each dimension.
	 * @throws FileNotFoundException
	 * @since 1.3
	 */
	public void drawImageWithMargin(GC gc, String imgPath, Rectangle imgRelPositionAndScale, int margin)
			throws FileNotFoundException {
		File file = new File(imgPath);
		if (!file.exists()) {
			throw new FileNotFoundException();
		}
		if (margin < 0)
			margin = 0;

		// extract params
		int imgPosX = imgRelPositionAndScale.x;
		int imgPosY = imgRelPositionAndScale.y;
		int imgScaleW = imgRelPositionAndScale.width;
		int imgScaleH = imgRelPositionAndScale.height;

		// compute wanted image pixel position and size
		float posX = (m_bounds.x + (float) m_bounds.width * imgPosX / 100) + margin;
		float posY = (m_bounds.y + (float) m_bounds.height * imgPosY / 100) + margin;
		float width = ((float) m_bounds.width * imgScaleW / 100) - 2 * margin;
		float height = ((float) m_bounds.height * imgScaleH / 100) - 2 * margin;

		Image img = new Image(gc.getDevice(), imgPath);
		// draw image
		gc.drawImage(img, 0, 0, img.getBounds().width, img.getBounds().height, Math.round(posX), Math.round(posY),
				Math.round(width), Math.round(height));
	}
}
