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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import com.biglybt.core.util.SystemProperties;
import com.biglybt.pifimpl.local.ui.config.ParameterImpl;
import com.biglybt.platform.win32.access.AEWin32Access;
import com.biglybt.platform.win32.access.AEWin32AccessException;
import com.biglybt.platform.win32.access.AEWin32Manager;
import com.biglybt.ui.swt.Utils;
import com.biglybt.ui.swt.pif.UISWTInstance;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.ui.UIInstance;
import com.biglybt.pif.ui.UIManager;
import com.biglybt.pif.ui.config.*;
import com.biglybt.pif.ui.menus.MenuItem;
import com.biglybt.pif.ui.menus.MenuManager;
import com.biglybt.pif.ui.model.BasicPluginConfigModel;

import static com.biglybt.plugins.migratetorrentapp.Plugin.VIEWID_MIGRATE;

public class ConfigModel_uTorrent
{
	public interface MigrateListener
	{
		void analysisComplete(Importer_uTorrent importer_uTorrent);
	}

	private final PluginInterface pi;

	private BasicPluginConfigModel configModel;

	public DirectoryParameter paramConfigDir;

	public DirectoryParameter paramBaseDir;

	public StringParameter paramDataDirsRecursive;

	public StringParameter paramDataDirsSingle;

	public StringParameter paramTorrentDirs;

	public StringParameter paramFolderReplacements;

	private BooleanParameter paramBaseDirCustom;

	private final List<MigrateListener> listeners = new ArrayList<>();

	public ConfigModel_uTorrent(PluginInterface pi) {
		this.pi = pi;
	}

	public BasicPluginConfigModel getConfigModel(UIManager uiManager) {
		if (configModel == null) {
			buildConfigModel(uiManager);
		}
		return configModel;
	}

	private void buildConfigModel(UIManager uiManager) {
		configModel = uiManager.createBasicPluginConfigModel("utMigrate.title");

		File configDir = getConfigDir();
		paramConfigDir = configModel.addDirectoryParameter2("utConfigDir",
				"utMigrate.configDir",
				configDir == null ? "" : configDir.getAbsolutePath());
		paramConfigDir.addConfigParameterListener(param -> {
			if (!paramBaseDirCustom.getValue()) {
				paramBaseDir.setValue(paramConfigDir.getValue());
			}
		});

		LabelParameter paramConfigDirInfo = configModel.addLabelParameter2(
				"utMigrate.configDir.info");
		paramConfigDirInfo.setIndent(1, true);

		configModel.createGroup(null, paramConfigDir, paramConfigDirInfo);

		BooleanParameter paramShowAdditionalOptions = configModel.addBooleanParameter2(
				"utShowAdditionalOptions", "utMigrate.showAdditionalFolders", false);

		final List<Parameter> listToggle = new ArrayList<>();

		////

		paramBaseDirCustom = configModel.addBooleanParameter2("utBaseDirCustom",
				"utMigrate.baseDir.custom", false);

		paramBaseDir = configModel.addDirectoryParameter2("utBaseDir", "",
				configDir == null ? "" : configDir.getAbsolutePath());

		ParameterGroup groupBaseCustom = configModel.createGroup(null,
				paramBaseDirCustom, paramBaseDir);
		groupBaseCustom.setNumberOfColumns(2);

		LabelParameter paramBaseDirInfo = configModel.addLabelParameter2(
				"utMigrate.baseDir.info");
		paramBaseDirInfo.setIndent(1, true);

		ParameterGroup groupBaseDir = configModel.createGroup(null, groupBaseCustom,
				paramBaseDirInfo);
		listToggle.add(groupBaseDir);

		paramBaseDirCustom.addEnabledOnSelection(paramBaseDir, paramBaseDirInfo);

		////

		LabelParameter paramTorrentDirsInfo = configModel.addLabelParameter2(
				"utMigrate.torrentDirs.info");

		paramTorrentDirs = configModel.addStringParameter2("utTorrentDirs", "", "");
		paramTorrentDirs.setMultiLine(4);
		//does not work until 2.0.0.1
		//paramTorrentDirs.setSuffixLabelKey("utMigrate.torrentDirs.info");

		ActionParameter btnAddTorrentDir = configModel.addActionParameter2("",
				"utMigrate.button.addTorrentDir");
		if (btnAddTorrentDir instanceof ParameterImpl) {
			((ParameterImpl) btnAddTorrentDir).setAllowedUiTypes(UIInstance.UIT_SWT);
		}
		btnAddTorrentDir.addListener(p -> addDir(paramTorrentDirs));

		ParameterGroup groupTorrentDirs = configModel.createGroup(
				"utMigrate.torrentDirs", paramTorrentDirs, btnAddTorrentDir,
				paramTorrentDirsInfo);
		listToggle.add(groupTorrentDirs);

		////

		paramDataDirsRecursive = configModel.addStringParameter2(
				"utDataDirsRecursive", "utMigrate.dataDirs.recursive", "");
		paramDataDirsRecursive.setMultiLine(4);

		ActionParameter btnAddDataDirRecursive = configModel.addActionParameter2("",
				"utMigrate.button.addDataDir");
		if (btnAddDataDirRecursive instanceof ParameterImpl) {
			((ParameterImpl) btnAddDataDirRecursive).setAllowedUiTypes(
					UIInstance.UIT_SWT);
		}
		btnAddDataDirRecursive.addListener(p -> addDir(paramDataDirsRecursive));

		paramDataDirsSingle = configModel.addStringParameter2("utDataDirsSingle",
				"utMigrate.dataDirs.single", "");
		paramDataDirsSingle.setMultiLine(3);

		ActionParameter btnAddDataDirSingle = configModel.addActionParameter2("",
				"utMigrate.button.addDataDir");
		if (btnAddDataDirSingle instanceof ParameterImpl) {
			((ParameterImpl) btnAddDataDirSingle).setAllowedUiTypes(
					UIInstance.UIT_SWT);
		}
		btnAddDataDirSingle.addListener(p -> addDir(paramDataDirsSingle));

		ParameterGroup groupDataDirs = configModel.createGroup(
				"utMigrate.group.dataDirs", paramDataDirsRecursive,
				btnAddDataDirRecursive, paramDataDirsSingle, btnAddDataDirSingle);

		listToggle.add(groupDataDirs);

		LabelParameter paramFolderReplacementsInfo = configModel.addLabelParameter2(
				"utMigrate.folderReplacements.info");

		paramFolderReplacements = configModel.addStringParameter2(
				"utFolderReplacements", "", "");
		paramFolderReplacements.setMultiLine(3);

		ParameterGroup groupFolderReplacements = configModel.createGroup(
				"utMigrate.folderReplacements", paramFolderReplacementsInfo,
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
				"utMigrate.button.analyze");
		paramAnalyze.addConfigParameterListener(
				param -> {
					paramAnalyze.setEnabled(false);
					new Importer_uTorrent(pi, this);
					MigrateListener l = new MigrateListener() {
						@Override
						public void analysisComplete(Importer_uTorrent importer_uTorrent) {
							removeListener(this);
							paramAnalyze.setEnabled(true);
						}
					};
					addListener(l);
				});

	}

	public ConfigModel_uTorrent setupConfigModel(UIManager uiManager) {
		MenuManager menuManager = uiManager.getMenuManager();
		MenuItem menuItem = menuManager.addMenuItem(MenuManager.MENU_MENUBAR_TOOLS,
				"menu.utorrent.migrate");
		menuItem.addListener((menu, target) -> {
			//uiManager.showConfigSection("utMigrate.title");
			UIInstance[] uiInstances = uiManager.getUIInstances();
			for (UIInstance uiInstance : uiInstances) {
				if (uiInstance instanceof UISWTInstance) {
					((UISWTInstance) uiInstance).openView("", VIEWID_MIGRATE, this);
					break;
				}
			}
		});

		return this;
	}

	private static void addDir(StringParameter param) {
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

	private File getConfigDir() {
		File dir;
		if (pi.getUtilities().isWindows()) {
			dir = getConfigDir_Windows();
			if (dir != null) {
				return dir;
			}
		}

		return null;
	}

	private static File getConfigDir_Windows() {
		AEWin32Access accessor = AEWin32Manager.getAccessor(true);
		if (accessor != null) {
			try {
				String installLocation = accessor.readStringValue(
						AEWin32Access.HKEY_CURRENT_USER,
						"software\\microsoft\\windows\\currentversion\\uninstall\\uTorrent",
						"InstallLocation");
				if (installLocation != null || !installLocation.isEmpty()) {
					return new File(installLocation);
				}
			} catch (AEWin32AccessException e) {
			}

			try {
				String userAppData = accessor.getUserAppData();
				if (userAppData != null && !userAppData.isEmpty()) {
					return new File(userAppData, "uTorrent");
				}
			} catch (AEWin32AccessException e) {
			}
		}

		String temp_user_path = SystemProperties.getEnvironmentalVariable(
				"APPDATA");
		if (temp_user_path != null && !temp_user_path.isEmpty()) {
			return new File(temp_user_path, "uTorrent");
		}

		return null;
	}

	public void addListener(MigrateListener l) {
		if (listeners.contains(l)) {
			return;
		}
		listeners.add(l);
	}

	public void removeListener(MigrateListener l) {
		listeners.remove(l);
	}

	public MigrateListener[] getListeners() {
		return listeners.toArray(new MigrateListener[0]);
	}

}
