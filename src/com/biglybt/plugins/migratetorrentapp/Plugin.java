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

package com.biglybt.plugins.migratetorrentapp;

import com.biglybt.core.util.Debug;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.UnloadablePlugin;
import com.biglybt.pif.ui.UIInstance;
import com.biglybt.pif.ui.UIManager;
import com.biglybt.pif.ui.UIManagerListener;

public class Plugin
	implements UnloadablePlugin
{

	private MigrateTorrentAppUI ui;

	private boolean destroyed;

	@Override
	public void unload() {
		synchronized (this) {

			if (destroyed) {

				return;
			}

			destroyed = true;
		}

		if (ui != null) {

			ui.destroy();

			ui = null;
		}
	}

	@Override
	public void initialize(PluginInterface pi) {
		UIManager uiManager = pi.getUIManager();

		uiManager.addUIListener(new UIManagerListener() {
			@Override
			public void UIAttached(UIInstance instance) {
				if (instance.getUIType().equals(UIInstance.UIT_SWT)) {
					if (destroyed) {

						return;
					}

					try {
						Class<?> cla = Plugin.class.forName(
								"com.biglybt.plugins.migratetorrentapp.swt.MigrateTorrentAppUISWT");
						ui = (MigrateTorrentAppUI) cla.getMethod("getSingleton",
								PluginInterface.class, UIInstance.class, Plugin.class).invoke(
										null, pi, instance, Plugin.this);

					} catch (Throwable e) {

						Debug.out(e);
					}
				}
			}

			@Override
			public void UIDetached(UIInstance instance) {
				if (instance.getUIType().equals(UIInstance.UIT_SWT) && ui != null) {
					ui.destroy();
					ui = null;
				}

			}
		});
	}

	public boolean isDestroyed() {
		return destroyed;
	}
}
