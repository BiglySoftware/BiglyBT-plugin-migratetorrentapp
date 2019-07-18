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

package com.biglybt.plugins.migratetorrentapp.utorrent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.biglybt.core.util.SystemProperties;
import com.biglybt.platform.win32.access.AEWin32Access;
import com.biglybt.platform.win32.access.AEWin32AccessException;
import com.biglybt.platform.win32.access.AEWin32Manager;
import com.biglybt.plugins.migratetorrentapp.MigrateTorrentAppUI;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.ui.UIManager;
import com.biglybt.pif.ui.config.*;
import com.biglybt.pif.ui.model.BasicPluginConfigModel;

public class ConfigModel_uTorrent
{

	public interface MigrateListener
	{
		void initMigrateListener();

		default void analysisStatus(String status) {
		}

		void analysisComplete(Importer_uTorrent importer_uTorrent);

		void migrationComplete(String migrateLog);
	}

	private final PluginInterface pi;

	private BasicPluginConfigModel configModel;

	public DirectoryParameter paramConfigDir;

	public BooleanParameter paramShowAdditionalOptions;

	public StringParameter paramDataDirsRecursive;

	public StringParameter paramDataDirsSingle;

	public StringParameter paramTorrentDirs;

	public StringParameter paramFolderReplacements;

	private final List<MigrateListener> listeners = new ArrayList<>();

	private MigrateTorrentAppUI appUI;

	private ActionParameter actionMigrate;

	private Importer_uTorrent importer;

	public ConfigModel_uTorrent(PluginInterface pi) {
		this.pi = pi;
	}

	public BasicPluginConfigModel getConfigModel(UIManager uiManager,
			MigrateTorrentAppUI appUI) {
		this.appUI = appUI;
		if (configModel == null) {
			buildConfigModel(uiManager);
		}
		return configModel;
	}

	private void buildConfigModel(UIManager uiManager) {
		configModel = uiManager.createBasicPluginConfigModel("utMigrate.title");

		File configDir = getDefaultConfigDir();
		paramConfigDir = configModel.addDirectoryParameter2("utConfigDir",
				"utMigrate.configDir",
				configDir == null ? "" : configDir.getAbsolutePath());

		LabelParameter paramConfigDirInfo = configModel.addLabelParameter2(
				"utMigrate.configDir.info");
		paramConfigDirInfo.setIndent(1, true);

		configModel.createGroup(null, paramConfigDir, paramConfigDirInfo);

		paramShowAdditionalOptions = configModel.addBooleanParameter2(
				"utShowAdditionalOptions", "migrateapp.showAdditionalFolders", false);

		final List<Parameter> listToggle = new ArrayList<>();

		////

		List<Parameter> listTorrentDirParams = new ArrayList<>();

		LabelParameter paramTorrentDirsInfo = configModel.addLabelParameter2(
				"migrateapp.torrentDirs.info");
		listTorrentDirParams.add(paramTorrentDirsInfo);

		paramTorrentDirs = configModel.addStringParameter2("utTorrentDirs", "", "");
		listTorrentDirParams.add(paramTorrentDirs);
		paramTorrentDirs.setMultiLine(4);
		//does not work until 2.0.0.1
		//paramTorrentDirs.setSuffixLabelKey("migrateapp.torrentDirs.info");

		if (appUI.canBrowseDir()) {
			ActionParameter btnAddTorrentDir = configModel.addActionParameter2("",
					"migrateapp.button.addTorrentDir");
			listTorrentDirParams.add(btnAddTorrentDir);
			btnAddTorrentDir.addListener(
					p -> appUI.browseAndAddDir(paramTorrentDirs));
		}

		ParameterGroup groupTorrentDirs = configModel.createGroup(
				"migrateapp.torrentDirs",
				listTorrentDirParams.toArray(new Parameter[0]));
		listToggle.add(groupTorrentDirs);

		////

		List<Parameter> listDataDirParams = new ArrayList<>();

		paramDataDirsRecursive = configModel.addStringParameter2(
				"utDataDirsRecursive", "migrateapp.dataDirs.recursive", "");
		listDataDirParams.add(paramDataDirsRecursive);
		paramDataDirsRecursive.setMultiLine(4);

		if (appUI.canBrowseDir()) {
			ActionParameter btnAddDataDirRecursive = configModel.addActionParameter2(
					"", "migrateapp.button.addDataDir");
			listDataDirParams.add(btnAddDataDirRecursive);
			btnAddDataDirRecursive.addListener(
					p -> appUI.browseAndAddDir(paramDataDirsRecursive));
		}

		paramDataDirsSingle = configModel.addStringParameter2("utDataDirsSingle",
				"migrateapp.dataDirs.single", "");
		listDataDirParams.add(paramDataDirsSingle);
		paramDataDirsSingle.setMultiLine(3);

		if (appUI.canBrowseDir()) {
			ActionParameter btnAddDataDirSingle = configModel.addActionParameter2("",
					"migrateapp.button.addDataDir");
			listDataDirParams.add(btnAddDataDirSingle);
			btnAddDataDirSingle.addListener(
					p -> appUI.browseAndAddDir(paramDataDirsSingle));
		}

		ParameterGroup groupDataDirs = configModel.createGroup(
				"migrateapp.group.dataDirs",
				listDataDirParams.toArray(new Parameter[0]));

		listToggle.add(groupDataDirs);

		LabelParameter paramFolderReplacementsInfo = configModel.addLabelParameter2(
				"migrateapp.folderReplacements.info");

		paramFolderReplacements = configModel.addStringParameter2(
				"utFolderReplacements", "", "");
		paramFolderReplacements.setMultiLine(3);

		ParameterGroup groupFolderReplacements = configModel.createGroup(
				"migrateapp.folderReplacements", paramFolderReplacementsInfo,
				paramFolderReplacements);
		listToggle.add(groupFolderReplacements);

		////

		ParameterListener parameterListener = param -> {
			boolean visible = ((BooleanParameter) param).getValue();
			for (Parameter parameter : listToggle) {
				parameter.setVisible(visible);
			}
		};
		parameterListener.parameterChanged(paramShowAdditionalOptions);
		paramShowAdditionalOptions.addListener(parameterListener);

		////

		ActionParameter paramAnalyze = configModel.addActionParameter2(null,
				"migrateapp.button.analyze");
		paramAnalyze.addConfigParameterListener(param -> {
			paramAnalyze.setEnabled(false);
			for (MigrateListener listener : listeners) {
				listener.initMigrateListener();
			}
			importer = new Importer_uTorrent(pi, this);
			MigrateListener l = new MigrateListener() {
				@Override
				public void initMigrateListener() {
				}

				@Override
				public void analysisComplete(Importer_uTorrent importer) {
					removeListener(this);
					paramAnalyze.setActionResource("migrateapp.button.reanalyze");
					paramAnalyze.setEnabled(true);
					// Can do this after 2001
					//actionMigrate.setVisible(true);
					actionMigrate.setEnabled(importer.canMigrate());
				}

				@Override
				public void migrationComplete(String migrateLog) {

				}
			};
			addListener(l);
		});

		actionMigrate = configModel.addActionParameter2(null,
				"migrateapp.button.migrate");
		// Can do this after 2001
		//actionMigrate.setVisible(false);
		actionMigrate.setEnabled(false);
		actionMigrate.addConfigParameterListener(param -> {
			actionMigrate.setEnabled(false);
			MigrateListener l = new MigrateListener() {
				@Override
				public void initMigrateListener() {

				}

				@Override
				public void analysisComplete(Importer_uTorrent importer) {
				}

				@Override
				public void migrationComplete(String migrateLog) {
					removeListener(this);
					actionMigrate.setEnabled(true);
				}
			};
			addListener(l);

			importer.migrate();
		});

	}

	public ConfigModel_uTorrent setupConfigModel(UIManager uiManager) {
		return this;
	}

	File getDefaultConfigDir() {
		// TODO: More checks. ie. bittorrent vs utorrent
		File dir = null;
		if (pi.getUtilities().isWindows()) {
			dir = getConfigDir_Windows();
		} else if (pi.getUtilities().isOSX()) {
			dir = getConfigDir_OSX();
		}

		return dir;
	}

	private File getConfigDir_OSX() {
		String userHome = System.getProperty("user.home");
		File dirUT = new File(userHome + "/Library/Application Support/uTorrent");
		File dirBT = new File(userHome + "/Library/Application Support/BitTorrent");
		long utLastModified = dirUT.lastModified();
		long btLastModified = dirBT.lastModified();
		return btLastModified > utLastModified ? dirBT : dirUT;
	}

	private static File getConfigDir_Windows() {
		File dirUT = getConfigDir_Windows("uTorrent");
		File dirBT = getConfigDir_Windows("BitTorrent");
		long utLastModified = dirUT.lastModified();
		long btLastModified = dirBT.lastModified();
		return btLastModified > utLastModified ? dirBT : dirUT;
	}

	private static File getConfigDir_Windows(String appName) {
		File dir = null;

		AEWin32Access accessor = AEWin32Manager.getAccessor(true);
		if (accessor != null) {
			try {
				String installLocation = accessor.readStringValue(
						AEWin32Access.HKEY_CURRENT_USER,
						"software\\microsoft\\windows\\currentversion\\uninstall\\"
								+ appName,
						"InstallLocation");
				if (installLocation != null || !installLocation.isEmpty()) {
					dir = new File(installLocation);
				}
			} catch (AEWin32AccessException e) {
			}

			if (dir == null) {
				try {
					String userAppData = accessor.getUserAppData();
					if (userAppData != null && !userAppData.isEmpty()) {
						dir = new File(userAppData, appName);
					}
				} catch (AEWin32AccessException e) {
				}
			}
		}

		if (dir == null) {
			String temp_user_path = SystemProperties.getEnvironmentalVariable(
					"APPDATA");
			if (temp_user_path != null && !temp_user_path.isEmpty()) {
				dir = new File(temp_user_path, appName);
			}
		}

		if (dir == null) {
			dir = new File(System.getProperty("user.home"),
					"AppData\\Roaming\\" + appName);
		}

		return dir;
	}

	public void addListener(MigrateListener l) {
		if (listeners.contains(l)) {
			return;
		}
		listeners.add(l);
	}

	public void analysisStatus(String s) {
		MigrateListener[] listeners = getListeners();
		for (MigrateListener listener : listeners) {
			listener.analysisStatus(s);
		}
	}

	public void removeListener(MigrateListener l) {
		listeners.remove(l);
	}

	public MigrateListener[] getListeners() {
		return listeners.toArray(new MigrateListener[0]);
	}

}
