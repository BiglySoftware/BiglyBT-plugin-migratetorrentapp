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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

import com.biglybt.core.CoreFactory;
import com.biglybt.core.global.GlobalManager;
import com.biglybt.core.tag.Tag;
import com.biglybt.core.tag.TagException;
import com.biglybt.core.tag.TagManagerFactory;
import com.biglybt.core.tag.TagType;
import com.biglybt.core.torrent.TOTorrentException;
import com.biglybt.core.util.BDecoder;
import com.biglybt.core.util.Constants;
import com.biglybt.core.util.TorrentUtils;
import com.biglybt.core.util.TorrentUtils.ExtendedTorrent;
import com.biglybt.plugins.migratetorrentapp.Importer;
import com.biglybt.plugins.migratetorrentapp.utorrent.ConfigModel_uTorrent.MigrateListener;
import com.biglybt.util.MapUtils;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.logging.LoggerChannel;

public class Importer_uTorrent
	extends Importer
{

	private static final boolean TEST_ONLY = true;

	public static final String PLUGINID_AZEXEC = "azexec";

	public static final String PLUGINID_MLDHT = "mlDHT";

	final LoggerChannel loggerChannel;

	private final Collection<String> requiredPlugins = new HashSet<>();

	public final List<TorrentImportInfo> listTorrentsToImport = new ArrayList<>();

	public final Map<String, TagToAddInfo> mapTagsToAdd = new HashMap<>();

	private final Map<String, String> mapFolderReplacements = new HashMap<>();

	private boolean isOSCaseSensitive;

	final List<String> listAdditionalTorrentDirs = new ArrayList<>();

	/**
	 * Map&lt;Path, Search Recursive>
	 */
	final Map<String, Boolean> mapAdditionalDataDirs = new HashMap<>();

	final Map<String, File> mapInfoHashToFile = new HashMap<>();

	private final ConfigModel_uTorrent configModelInfo;

	GlobalManager gm;

	public SettingsImportInfo settingsImportInfo;

	boolean hasRunProgramEnabled = false;

	public Importer_uTorrent(PluginInterface pi,
			ConfigModel_uTorrent configModelInfo) {
		super(pi);
		this.configModelInfo = configModelInfo;
		String[] dirsSingle = configModelInfo.paramDataDirsSingle.getValue().split(
				"\n");
		for (String dir : dirsSingle) {
			if (!dir.isEmpty()) {
				mapAdditionalDataDirs.put(dir, false);
			}
		}
		String[] dirsRecursive = configModelInfo.paramDataDirsRecursive.getValue().split(
				"\n");
		for (String dir : dirsRecursive) {
			if (!dir.isEmpty()) {
				mapAdditionalDataDirs.put(dir, true);
			}
		}
		String[] dirsTorrents = configModelInfo.paramTorrentDirs.getValue().split(
				"\n");
		for (String dir : dirsTorrents) {
			if (!dir.isEmpty()) {
				listAdditionalTorrentDirs.add(dir);
			}
		}

		try {
			File tempFile = File.createTempFile("BIG", ".tmp");
			isOSCaseSensitive = !new File(
					tempFile.getAbsolutePath().toLowerCase()).isFile();
			tempFile.delete();
		} catch (IOException e) {
			isOSCaseSensitive = true;
		}
		String[] folderReplacements = configModelInfo.paramFolderReplacements.getValue().split(
				"\n");
		for (String folderReplacement : folderReplacements) {
			if (!isOSCaseSensitive) {
				folderReplacement = folderReplacement.toLowerCase();
			}
			String[] split = folderReplacement.split("\\|", 2);
			if (split.length == 2) {
				mapFolderReplacements.put(split[0], split[1]);
			}
		}

		pi.getUtilities().createThread("uTorrentImporter", this);
		loggerChannel = pi.getLogger().getChannel("migratetorrentapp");
	}

	@Override
	public void run() {
		gm = CoreFactory.getSingleton().getGlobalManager();

		for (String listTorrentDir : listAdditionalTorrentDirs) {
			File[] files = new File(listTorrentDir).listFiles(pathname -> {
				if (pathname.length() > 1024L * 1024 * 32) {
					// Skip files larger than 32M
					return false;
				}
				return pathname.getName().toLowerCase().endsWith(".torrent");
			});
			if (files == null) {
				continue;
			}
			for (File file : files) {
				try {
					ExtendedTorrent torrent = TorrentUtils.readDelegateFromFile(file,
							false);
					if (torrent == null) {
						continue;
					}
					String hash = torrent.getHashWrapper().toBase32String();
					mapInfoHashToFile.put(hash, file);
				} catch (TOTorrentException ignore) {
				}
			}
		}

		File configDir = new File(configModelInfo.paramConfigDir.getValue());
		settingsImportInfo = new SettingsImportInfo(this);
		settingsImportInfo.processSettings(configDir);

		processResumeFile(configDir);

		///////////

		if (hasRunProgramEnabled || settingsImportInfo.hasRunProgram) {
			//addRequiredPlugin(PLUGINID_AZEXEC);
			settingsImportInfo.logWarnings.append(
					"uT has 'Run Program' settings.  Please install Command Runner (azexec) plugin.\n");
		}

		Collections.sort(listTorrentsToImport);

		MigrateListener[] listeners = configModelInfo.getListeners();
		for (MigrateListener l : listeners) {
			l.analysisComplete(this);
		}
	}

	public void migrate() {
		if (!TEST_ONLY) {
			TagType ttManual = TagManagerFactory.getTagManager().getTagType(
					TagType.TT_DOWNLOAD_MANUAL);
			if (ttManual != null) {
				for (TagToAddInfo tagToAddInfo : mapTagsToAdd.values()) {

					try {
						Tag tag = ttManual.createTag(tagToAddInfo.name, false);
						if (tag != null && tagToAddInfo.group != null) {
							tag.setGroup(tagToAddInfo.group);
						}

					} catch (TagException e) {
						// TODO: Report
						e.printStackTrace();
					}
				}
			}

			for (TorrentImportInfo importInfo : listTorrentsToImport) {
				if (importInfo.torrentFile != null) {
					importInfo.addDownloadManager();
				}
			}
		}
	}

	private void processResumeFile(File configDir) {
		File fileResume = new File(configDir, "resume.dat");

		try {
			BufferedInputStream is;
			is = new BufferedInputStream(new FileInputStream(fileResume));
			BDecoder decoder = new BDecoder(Constants.UTF_8);
			decoder.setMapDecodeListener((context, map, nestingLevel) -> {

				if (nestingLevel != 1) {
					return;
				}
				try {
//					System.out.println(nestingLevel + "]: \"" + context
//							+ "\" Map Decoded: " + map.size() + " entries");

					context = replaceFolders(context);
					File torrentFile = new File(context);
					if (!torrentFile.isAbsolute()) {
						torrentFile = new File(configModelInfo.paramBaseDir.getValue(),
								context);
					}
					TorrentImportInfo importInfo = new TorrentImportInfo(this,
							torrentFile, map);
					if (importInfo.execOnComplete != null) {
						hasRunProgramEnabled = true;
					}
					listTorrentsToImport.add(importInfo);
				} catch (Throwable t) {
					t.printStackTrace();
					loggerChannel.log(t);
				}
				//System.out.println("--");

				// Reduce memory usage
				map.clear();
//				for (String key : map.keySet()) {
//					System.out.println(key);
//					System.out.println("-> " + map.get(key));
//				}
//				System.out.println("--");
			});
			decoder.decodeStream(is, false);
			is.close();
		} catch (Throwable t) {
			t.printStackTrace();
			loggerChannel.log(t);
		}
	}

	String replaceFolders(String path) {
		String result = path;
		String lowerPath = "";
		if (!isOSCaseSensitive) {
			lowerPath = path.toLowerCase();
		}
		for (String key : mapFolderReplacements.keySet()) {
			if (path.startsWith(key)
					|| (!isOSCaseSensitive && lowerPath.startsWith(key))) {
				String replaceWith = mapFolderReplacements.get(key);
				result = replaceWith + path.substring(replaceWith.length());
				if (new File(result).exists()) {
					return result;
				}
			}
		}
		return result;
	}

	public TagToAddInfo addTagIgnoreGroup(TorrentImportInfo importInfo,
			String name, String group) {
		for (TagToAddInfo tagToAddInfo : mapTagsToAdd.values()) {
			if (tagToAddInfo.name.equals(name)) {
				return tagToAddInfo;
			}
		}
		String id = name + "/" + group;
		TagToAddInfo tagToAddInfo = mapTagsToAdd.get(id);
		if (tagToAddInfo == null) {
			tagToAddInfo = new TagToAddInfo(name, group);
			mapTagsToAdd.put(id, tagToAddInfo);
		}
		if (importInfo != null) {
			tagToAddInfo.items.add(new WeakReference<>(importInfo));
			importInfo.tags.put(tagToAddInfo, null);
		}
		return tagToAddInfo;
	}

	@SuppressWarnings("UnusedReturnValue")
	public TagToAddInfo addTag(TorrentImportInfo importInfo, String name,
			String group) {
		String id = name + "/" + group;
		TagToAddInfo tagToAddInfo = mapTagsToAdd.get(id);
		if (tagToAddInfo == null) {
			tagToAddInfo = new TagToAddInfo(name, group);
			mapTagsToAdd.put(id, tagToAddInfo);
		}
		if (importInfo != null) {
			tagToAddInfo.items.add(new WeakReference<>(importInfo));
			importInfo.tags.put(tagToAddInfo, null);
		}
		return tagToAddInfo;
	}

	void addTagFromMap(TorrentImportInfo importInfo,
			Map<String, Object> mapWithTagName, String tagNameKey, String group) {
		String tagName = MapUtils.getMapString(mapWithTagName, tagNameKey, null);
		if (tagName == null || tagName.isEmpty()) {
			return;
		}

		addTag(importInfo, tagName, group);
	}

	public void addRequiredPlugin(String pluginID) {
		if (!requiredPlugins.contains(pluginID)) {
			requiredPlugins.add(pluginID);
		}
	}
}
