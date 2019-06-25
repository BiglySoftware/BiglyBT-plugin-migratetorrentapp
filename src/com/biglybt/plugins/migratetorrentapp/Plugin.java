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

import com.biglybt.plugins.migratetorrentapp.utorrent.ConfigModel_uTorrent;
import com.biglybt.ui.swt.pif.UISWTInstance;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.UnloadablePlugin;
import com.biglybt.pif.ui.UIInstance;
import com.biglybt.pif.ui.UIManager;
import com.biglybt.pif.ui.UIManagerListener;
import com.biglybt.pif.ui.menus.MenuItem;
import com.biglybt.pif.ui.menus.MenuManager;

public class Plugin
	implements UnloadablePlugin
{

	public static final String VIEWID_MIGRATE = "migrate";

	private UISWTInstance swtInstance;

	@Override
	public void unload() {
		if (swtInstance != null) {
			swtInstance.removeViews("", VIEWID_MIGRATE);
		}
	}

	@Override
	public void initialize(PluginInterface pi) {
		UIManager uiManager = pi.getUIManager();

		ConfigModel_uTorrent configModel_uTorrent = new ConfigModel_uTorrent(
				pi).setupConfigModel(uiManager);

		uiManager.addUIListener(new UIManagerListener() {
			@Override
			public void UIAttached(UIInstance instance) {
				if (instance instanceof UISWTInstance) {
					swtInstance = (UISWTInstance) instance;
					swtInstance.addView("", VIEWID_MIGRATE,
							MigrateViewEventListener.class, configModel_uTorrent);
				}
			}

			@Override
			public void UIDetached(UIInstance instance) {

			}
		});
	}
}
