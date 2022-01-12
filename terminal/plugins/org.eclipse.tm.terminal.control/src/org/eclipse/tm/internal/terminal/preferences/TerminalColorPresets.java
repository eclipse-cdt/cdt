/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.preferences;

import static org.eclipse.tm.terminal.model.TerminalColor.BACKGROUND;
import static org.eclipse.tm.terminal.model.TerminalColor.BLACK;
import static org.eclipse.tm.terminal.model.TerminalColor.BLUE;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_BLACK;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_BLUE;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_CYAN;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_GREEN;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_MAGENTA;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_RED;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_WHITE;
import static org.eclipse.tm.terminal.model.TerminalColor.BRIGHT_YELLOW;
import static org.eclipse.tm.terminal.model.TerminalColor.CYAN;
import static org.eclipse.tm.terminal.model.TerminalColor.FOREGROUND;
import static org.eclipse.tm.terminal.model.TerminalColor.GREEN;
import static org.eclipse.tm.terminal.model.TerminalColor.MAGENTA;
import static org.eclipse.tm.terminal.model.TerminalColor.RED;
import static org.eclipse.tm.terminal.model.TerminalColor.SELECTION_BACKGROUND;
import static org.eclipse.tm.terminal.model.TerminalColor.SELECTION_FOREGROUND;
import static org.eclipse.tm.terminal.model.TerminalColor.WHITE;
import static org.eclipse.tm.terminal.model.TerminalColor.YELLOW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.terminal.control.impl.TerminalMessages;
import org.eclipse.tm.internal.terminal.model.SystemDefaultColors;
import org.eclipse.tm.terminal.model.TerminalColor;

/**
 * @since 5.0
 */
public enum TerminalColorPresets {

	INSTANCE;

	private final List<Preset> presets = new ArrayList<>();

	public List<String> getPresets() {
		return presets.stream().map(Preset::getName).collect(Collectors.toList());
	}

	public Preset getPreset(int index) {
		return presets.get(index);
	}

	public static class Preset {
		private String name;
		private Map<TerminalColor, Supplier<RGB>> map = new EnumMap<>(TerminalColor.class);

		Preset(String name) {
			this.name = name;
			set(BLACK, 0, 0, 0);
			set(RED, 205, 0, 0);
			set(GREEN, 0, 205, 0);
			set(YELLOW, 205, 205, 0);
			set(BLUE, 0, 0, 238);
			set(MAGENTA, 205, 0, 205);
			set(CYAN, 0, 205, 205);
			set(WHITE, 229, 229, 229);

			set(BRIGHT_BLACK, 0, 0, 0);
			set(BRIGHT_RED, 255, 0, 0);
			set(BRIGHT_GREEN, 0, 255, 0);
			set(BRIGHT_YELLOW, 255, 255, 0);
			set(BRIGHT_BLUE, 92, 92, 255);
			set(BRIGHT_MAGENTA, 255, 0, 255);
			set(BRIGHT_CYAN, 0, 255, 255);
			set(BRIGHT_WHITE, 255, 255, 255);

			set(FOREGROUND, SystemDefaultColors.FOREGROUND);
			set(BACKGROUND, SystemDefaultColors.BACKGROUND);
			set(SELECTION_FOREGROUND, SystemDefaultColors.SELECTION_FOREGROUND);
			set(SELECTION_BACKGROUND, SystemDefaultColors.SELECTION_BACKGROUND);
		}

		Preset set(TerminalColor color, RGB rgb) {
			return set(color, () -> rgb);
		}

		Preset set(TerminalColor color, int r, int g, int b) {
			return set(color, new RGB(r, g, b));
		}

		Preset set(TerminalColor color, Supplier<RGB> rgbSupplier) {
			map.put(color, rgbSupplier);
			return this;
		}

		public String getName() {
			return name;
		}

		/**
		 * Returns the preset value for the given color. Will never return null
		 * because each color must be defined in the map.
		 *
		 * @param terminalColor to get RGB value for
		 * @return non-<code>null</code> color
		 */
		public RGB getRGB(TerminalColor terminalColor) {
			return map.getOrDefault(terminalColor, () -> new RGB(0, 0, 0)).get();
		}
	}

	TerminalColorPresets() {
		presets.add(new Preset(TerminalMessages.TerminalColorPresets_TerminalDefaults));
		presets.add(new Preset(TerminalMessages.TerminalColorPresets_EclipseLight) //
				.set(FOREGROUND, getDefaultPreset().getRGB(BLACK)) //
				.set(BACKGROUND, getDefaultPreset().getRGB(WHITE)));
		presets.add(new Preset(TerminalMessages.TerminalColorPresets_EclipseDark) //
				.set(FOREGROUND, getDefaultPreset().getRGB(WHITE)) //
				.set(BACKGROUND, getDefaultPreset().getRGB(BLACK)));
	}

	public Preset getDefaultPreset() {
		return presets.get(0);
	}
}
