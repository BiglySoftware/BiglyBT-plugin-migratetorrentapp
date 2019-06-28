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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import com.biglybt.plugins.migratetorrentapp.MigrateTorrentAppUI;
import com.biglybt.plugins.migratetorrentapp.Plugin;
import com.biglybt.plugins.migratetorrentapp.utorrent.ConfigModel_uTorrent;
import com.biglybt.ui.swt.Utils;
import com.biglybt.ui.swt.pif.UISWTInstance;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.ui.UIInstance;
import com.biglybt.pif.ui.UIManager;
import com.biglybt.pif.ui.config.StringParameter;
import com.biglybt.pif.ui.menus.MenuItem;
import com.biglybt.pif.ui.menus.MenuManager;

public class MigrateTorrentAppUISWT
	implements MigrateTorrentAppUI
{

	public static final String VIEWID_MIGRATE = "migrate";

	private static MigrateTorrentAppUISWT instance;

	private final PluginInterface pi;

	private final UISWTInstance swtInstance;

	public static MigrateTorrentAppUISWT getSingleton() {
		return instance;
	}

	public static MigrateTorrentAppUISWT getSingleton(PluginInterface pi,
			UIInstance uiInstance, Plugin plugin) {
		if (instance == null) {
			instance = new MigrateTorrentAppUISWT(pi, (UISWTInstance) uiInstance);
		}
		return instance;
	}

	public MigrateTorrentAppUISWT(PluginInterface pi, UISWTInstance swtInstance) {
		this.pi = pi;
		this.swtInstance = swtInstance;

		UIManager uiManager = pi.getUIManager();
		ConfigModel_uTorrent configModel_uTorrent = new ConfigModel_uTorrent(
				pi).setupConfigModel(uiManager);

		MenuManager menuManager = uiManager.getMenuManager();
		MenuItem menuItem = menuManager.addMenuItem(MenuManager.MENU_MENUBAR_TOOLS,
				"menu.utorrent.migrate");
		menuItem.addListener((menu, target) -> {
			swtInstance.openView("", VIEWID_MIGRATE, configModel_uTorrent);
		});

		swtInstance.addView("", VIEWID_MIGRATE, MigrateViewEventListener.class,
				configModel_uTorrent);
	}

	@Override
	public boolean canBrowseDir() {
		return true;
	}

	@Override
	public void destroy() {
		swtInstance.removeViews("", VIEWID_MIGRATE);
	}

	@Override
	public void browseAndAddDir(StringParameter param) {
		Utils.execSWTThread(() -> {
			Shell shell = Utils.findAnyShell(false);
			DirectoryDialog dialog = new DirectoryDialog(shell,
					SWT.APPLICATION_MODAL);
			String open = dialog.open();
			if (open != null && open.length() > 0) {
				String value = param.getValue();
				param.setValue(value + "\n" + open);
			}
		});
	}

}
