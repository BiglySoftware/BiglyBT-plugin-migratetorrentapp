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
import com.biglybt.pif.utils.LocaleUtilities;

public class MigrateTorrentAppUISWT
	implements MigrateTorrentAppUI
{

	public static final String VIEWID_MIGRATE_UT = "migrate.uT";

	private static MigrateTorrentAppUISWT instance;

	private static Plugin plugin;

	private final PluginInterface pi;

	private final UISWTInstance swtInstance;

	public static MigrateTorrentAppUISWT getSingleton() {
		return instance;
	}

	public static MigrateTorrentAppUISWT getSingleton(PluginInterface pi,
			UIInstance uiInstance, Plugin plugin) {
		MigrateTorrentAppUISWT.plugin = plugin;
		if (instance == null) {
			instance = new MigrateTorrentAppUISWT(pi, (UISWTInstance) uiInstance);
		}
		return instance;
	}

	public MigrateTorrentAppUISWT(PluginInterface pi, UISWTInstance swtInstance) {
		this.pi = pi;
		this.swtInstance = swtInstance;

		ConfigModel_uTorrent configModel_uTorrent = ConfigModel_uTorrent.getInstance(pi);

		UIManager uiManager = pi.getUIManager();
		MenuManager menuManager = uiManager.getMenuManager();
		MenuItem menuItem = menuManager.addMenuItem(MenuManager.MENU_MENUBAR_TOOLS,
				"menu.utorrent.migrate");
		menuItem.addListener((menu, target) -> swtInstance.openView(UISWTInstance.VIEW_MAIN, VIEWID_MIGRATE_UT,
				configModel_uTorrent));

		swtInstance.addView(UISWTInstance.VIEW_MAIN, VIEWID_MIGRATE_UT,
				MigrateVEL_uTorrent.class, configModel_uTorrent);

		boolean showImportPopup = ConfigModel_uTorrent.showImportPopup(pi);
		if (showImportPopup) {
			LocaleUtilities localeUtilities = pi.getUtilities().getLocaleUtilities();
			swtInstance.promptUser(
					localeUtilities.getLocalisedMessageText("utMigrate.foundApp.title"),
					localeUtilities.getLocalisedMessageText("utMigrate.foundApp.message"),
					new String[] {
						localeUtilities.getLocalisedMessageText("Button.yes"),
						localeUtilities.getLocalisedMessageText("Button.no"),
					}, 1, result -> {
						if (result == 1) {
							pi.getPluginconfig().setPluginParameter("show.initial.popup",
									false);
							plugin.restoreSpeedTest(true);
							return;
						}
						plugin.restoreSpeedTest(false);
						swtInstance.openView(UISWTInstance.VIEW_MAIN, VIEWID_MIGRATE_UT,
								configModel_uTorrent);
					});
		}
	}

	@Override
	public boolean canBrowseDir() {
		return true;
	}

	@Override
	public void destroy() {
		swtInstance.removeViews("", VIEWID_MIGRATE_UT);
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
				if (value.isEmpty()) {
					param.setValue(open);
				} else {
					param.setValue(value + System.lineSeparator() + open);
				}
			}
		});
	}

}
