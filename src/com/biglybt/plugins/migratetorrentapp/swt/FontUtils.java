/*
 * Copyright (C) Bigly Software.  All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.biglybt.plugins.migratetorrentapp.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;

public class FontUtils
{
	static Font fontMonospace;

	private static final String[] MONO_FONT_NAMES = {
		"Monaco",
		"Consolas",
		"Lucida Console",
		"Courier",
		"Courier New",
		"Monospace"
	};

	public static Font getMonospaceFont(Device device, int heightInPoints) {
		if (fontMonospace != null) {
			return fontMonospace;
		}

		for (String tryName : MONO_FONT_NAMES) {
			fontMonospace = new Font(device, tryName, heightInPoints, SWT.NORMAL);
			FontData[] fontData = fontMonospace.getFontData();
			if (fontData.length > 0) {
				int w1 = getTextWidth(device, fontMonospace, "i");
				int w2 = getTextWidth(device, fontMonospace, "w");

				//System.out.println("MonoFont " + tryName + ". widths=" + w1 + "," + w2);

				if (w1 == w2) {
					break;
				}
			}
			fontMonospace.dispose();
			fontMonospace = null;
		}

		if (fontMonospace == null) {
			return device.getSystemFont();
		}

		return fontMonospace;
	}

	public static int getTextWidth(Device device, Font font, String text) {
		int width = 0;
		try {
			GC gc = new GC(device);
			gc.setFont(font);
			width = gc.textExtent(text).x;
			gc.dispose();
		} catch (Exception ex) {
			width = text.length() * 5;
		}
		return width;
	}

}
