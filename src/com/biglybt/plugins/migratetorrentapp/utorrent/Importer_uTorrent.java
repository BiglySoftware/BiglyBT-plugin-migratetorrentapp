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
import java.lang.ref.WeakReference;
import java.util.*;

import com.biglybt.core.CoreFactory;
import com.biglybt.core.global.GlobalManager;
import com.biglybt.core.tag.*;
import com.biglybt.core.tag.TagFeatureProperties.TagProperty;
import com.biglybt.core.torrent.TOTorrentException;
import com.biglybt.core.util.*;
import com.biglybt.core.util.TorrentUtils.ExtendedTorrent;
import com.biglybt.plugins.migratetorrentapp.Importer;
import com.biglybt.plugins.migratetorrentapp.Utils;
import com.biglybt.plugins.migratetorrentapp.utorrent.ConfigModel_uTorrent.MigrateListener;
import com.biglybt.util.MapUtils;
import com.biglybt.util.StringCompareUtils;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.logging.LoggerChannel;

import static com.biglybt.plugins.migratetorrentapp.Utils.NL;

/**
 * TODO: RSS Feeds
 */
public class Importer_uTorrent
	extends Importer
{

	public static final String PLUGINID_AZEXEC = "azexec";

	public static final String PLUGINID_MLDHT = "mlDHT";

	private static char utDirSeparator = 0;

	final LoggerChannel loggerChannel;

	private final Collection<String> requiredPlugins = new HashSet<>();

	public final List<TorrentImportInfo> listTorrentsToImport = new ArrayList<>();

	public final Map<String, TagToAddInfo> mapTagsToAdd = new HashMap<>();

	private final Map<String, String> mapFolderReplacements = new HashMap<>();

	final List<String> listAdditionalTorrentDirs = new ArrayList<>();

	/**
	 * Map&lt;Path, Search Recursive>
	 */
	private final Map<String, Boolean> mapAdditionalDataDirs = new HashMap<>();

	/**
	 * Includes subdirs when recursive is set
	 */
	final List<File> listAdditionalDataDirs = new ArrayList<>();

	final Map<Long, List<File>> mapFileSizeToScannedFile = new HashMap<>();

	final Map<String, List<File>> mapFilenameToScannedFile = new HashMap<>();

	final Map<String, File> mapInfoHashToFile = new HashMap<>();

	private final ConfigModel_uTorrent configModelInfo;

	GlobalManager gm;

	public SettingsImportInfo settingsImportInfo;

	boolean hasRunProgramEnabled = false;

	public final Map<String, HashSet<String>> needsPathReplacement = new HashMap<>();

	public Importer_uTorrent(PluginInterface pi,
			ConfigModel_uTorrent configModelInfo) {
		super(pi);

		configModelInfo.analysisStatus("migrateapp.status.initializingAnalysis");

		this.configModelInfo = configModelInfo;
		String[] dirsSingle = configModelInfo.paramDataDirsSingle.getValue().split(
				"[\\r\\n]+");
		for (String dir : dirsSingle) {
			if (!dir.isEmpty()) {
				mapAdditionalDataDirs.put(dir, false);
			}
		}
		String[] dirsRecursive = configModelInfo.paramDataDirsRecursive.getValue().split(
				"[\\r\\n]+");
		for (String dir : dirsRecursive) {
			if (!dir.isEmpty()) {
				mapAdditionalDataDirs.put(dir, true);
			}
		}
		String[] dirsTorrents = configModelInfo.paramTorrentDirs.getValue().split(
				"[\\r\\n]+");
		for (String dir : dirsTorrents) {
			if (!dir.isEmpty()) {
				listAdditionalTorrentDirs.add(dir);
			}
		}

		String[] folderReplacements = configModelInfo.paramFolderReplacements.getValue().split(
				"[\\r\\n]+");
		for (String folderReplacement : folderReplacements) {
			String[] split = folderReplacement.split("\\|", 2);
			if (split.length == 2) {
				mapFolderReplacements.put(split[0].toLowerCase(), split[1]);
			}
		}

		pi.getUtilities().createThread("uTorrentImporter", this);
		loggerChannel = pi.getLogger().getChannel("migratetorrentapp");
	}

	@Override
	public void run() {
		gm = CoreFactory.getSingleton().getGlobalManager();

		for (String listTorrentDir : listAdditionalTorrentDirs) {
			if (listTorrentDir.isEmpty()) {
				continue;
			}
			configModelInfo.analysisStatus(
					"migrateapp.status.scanningAdditionalTorrentDir", listTorrentDir);

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
			configModelInfo.analysisStatus("migrateapp.status.scanningTorrentFiles",
					"" + files.length, listTorrentDir);
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

		detectUTOS(configDir);

		configModelInfo.analysisStatus("migrateapp.status.analyzingSettings");

		settingsImportInfo = new SettingsImportInfo(this);
		settingsImportInfo.processSettings(configDir);

		configModelInfo.analysisStatus("migrateapp.status.analyzingScanDirs");

		buildAdditionalDataDirs();

		configModelInfo.analysisStatus("migrateapp.status.analyzingTorrents");

		processResumeFile(configDir);

		configModelInfo.analysisStatus("");

		///////////

		if (hasRunProgramEnabled || settingsImportInfo.hasRunProgram) {
			//addRequiredPlugin(PLUGINID_AZEXEC);
			settingsImportInfo.logWarnings.append(
					"uT has 'Run Program' settings.  Please install Command Runner (azexec) plugin.").append(
							NL);
		}

		Collections.sort(listTorrentsToImport);

		configModelInfo.analysisStatus("migrateapp.status.analysisDone");

		MigrateListener[] listeners = configModelInfo.getListeners();
		for (MigrateListener l : listeners) {
			l.analysisComplete(this);
		}
	}

	private void buildAdditionalDataDirs() {
		for (String dataDir : mapAdditionalDataDirs.keySet()) {
			Boolean recursive = mapAdditionalDataDirs.get(dataDir);

			addDataDirFiles(new File(dataDir), recursive);
		}
	}

	private void addDataDirFiles(File dir, Boolean recursive) {
		if (!dir.isDirectory()) {
			return;
		}
		listAdditionalDataDirs.add(dir);
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		configModelInfo.analysisStatus("migrateapp.status.analyzingScanDir",
				dir.toString());
		for (File file : files) {
			if (file.isFile()) {
				List<File> filesWithSize = mapFileSizeToScannedFile.computeIfAbsent(
						file.length(), k -> new ArrayList<>());
				filesWithSize.add(file);
				List<File> filesWithName = mapFilenameToScannedFile.computeIfAbsent(
						file.getName(), k -> new ArrayList<>());
				filesWithName.add(file);
			} else if (recursive && file.isDirectory()) {
				addDataDirFiles(file, true);
			}
		}
	}

	private void detectUTOS(File configDir) {
		if (new File(configDir, "utserver").isFile()) {
			utDirSeparator = '/';
			return;
		}
		if (new File(configDir, "utorrent.exe").isFile()) {
			utDirSeparator = '\\';
			return;
		}

		File defaultConfigDir = configModelInfo.getDefaultConfigDir();
		if (defaultConfigDir != null && defaultConfigDir.equals(configDir)) {
			utDirSeparator = pi.getUtilities().isWindows() ? '\\' : '/';
			return;
		}

		utDirSeparator = 0;
	}

	public void migrate() {
		pi.getUtilities().createThread("Migrate uT", () -> migrateNow());
	}

	private void migrateNow() {
		StringBuilder sbMigrateLog = new StringBuilder();

		sbMigrateLog.append("Migration started at ").append(
				DisplayFormatters.formatDate(System.currentTimeMillis())).append(NL);

		try {
			StringBuilder results = settingsImportInfo.migrate();
			if (results.length() > 0) {
				sbMigrateLog.append("Config Migration Log:").append(NL);
				sbMigrateLog.append(results).append(NL);
			}
		} catch (Throwable t) {
			String err = Utils.getErrorAndHideStuff(t);
			sbMigrateLog.append("Error Migrating Settings: ").append(err).append(NL);
		}

		TagType ttManual = TagManagerFactory.getTagManager().getTagType(
				TagType.TT_DOWNLOAD_MANUAL);
		if (ttManual != null) {
			for (TagToAddInfo tagToAddInfo : mapTagsToAdd.values()) {

				try {
					Tag existingTag = ttManual.getTag(tagToAddInfo.name, true);
					boolean exists = existingTag != null && StringCompareUtils.equals(
							existingTag.getGroup(), tagToAddInfo.group);

					Tag tag = exists ? existingTag
							: ttManual.createTag(tagToAddInfo.name, false);
					if (tagToAddInfo.group != null) {
						tag.setGroup(tagToAddInfo.group);
					}
					tag.setVisible(tagToAddInfo.showInSidebar);
					if (tagToAddInfo.constraint != null) {
						if (tag.getTagType().hasTagTypeFeature(TagFeature.TF_PROPERTIES)
								&& (tag instanceof TagFeatureProperties)) {
							TagFeatureProperties tfp = (TagFeatureProperties) tag;

							// private static final String CM_ADD_REMOVE 	= "am=0;";
							// private static final String CM_ADD_ONLY	 	= "am=1;";
							// private static final String CM_REMOVE_ONLY	= "am=2;";
							// private static final String CM_NEW_DLS		= "am=3;";

							final TagProperty propConstraint = tfp.getProperty(
									TagFeatureProperties.PR_CONSTRAINT);
							if (propConstraint != null) {
								propConstraint.setStringList(new String[] {
									tagToAddInfo.constraint,
									"am=0;"
								});
							}

						}
					}

					if (tagToAddInfo.initialSaveFolder != null) {
						if (tag.getTagType().hasTagTypeFeature(
								TagFeature.TF_FILE_LOCATION)) {
							TagFeatureFileLocation fl = (TagFeatureFileLocation) tag;

							if (fl.supportsTagInitialSaveFolder()) {
								fl.setTagInitialSaveFolder(
										new File(tagToAddInfo.initialSaveFolder));
							}
						}
					}

					if (tagToAddInfo.maxDown != 0 || tagToAddInfo.maxUp != 0) {
						if (tag.getTagType().hasTagTypeFeature(TagFeature.TF_RATE_LIMIT)) {
							TagFeatureRateLimit rl = (TagFeatureRateLimit) tag;
							if (tagToAddInfo.maxDown != 0 && rl.supportsTagDownloadLimit()) {
								rl.setTagDownloadLimit(tagToAddInfo.maxDown);
							}
							if (tagToAddInfo.maxUp != 0 && rl.supportsTagUploadLimit()) {
								rl.setTagUploadLimit(tagToAddInfo.maxUp);
							}
						}
					}

					ttManual.addTag(tag);

					tagToAddInfo.tag = tag;

				} catch (Throwable t) {
					String err = Utils.getErrorAndHideStuff(t, tagToAddInfo.name);
					sbMigrateLog.append("Error Migrating Tag: ").append(err).append(NL);
					sbMigrateLog.append("\t").append(
							tagToAddInfo.toDebugString().replaceAll(NL, NL + "\t"));
				}
			}
		}

		for (TorrentImportInfo importInfo : listTorrentsToImport) {
			try {
				StringBuilder results = importInfo.migrate();
				if (results.length() > 0) {
					sbMigrateLog.append(NL);
					sbMigrateLog.append("Torrent ").append(
							Utils.wrapString(importInfo.getName())).append(
									" Migration Log:").append(NL);
					sbMigrateLog.append(results).append(NL);
					if (results.indexOf("Already exists in BiglyBT") == -1) {
						sbMigrateLog.append("\t");
						sbMigrateLog.append(
								importInfo.toDebugString(true).replaceAll(NL, NL + "\t"));
						sbMigrateLog.append(NL);
					}
				}
			} catch (Throwable t) {
				String err = Utils.getErrorAndHideStuff(t, importInfo.dirSavePath);
				sbMigrateLog.append("Error Migrating Torrent: ").append(err).append(NL);
				sbMigrateLog.append("\t").append(
						importInfo.toDebugString(true).replaceAll(NL, NL + "\t"));
				sbMigrateLog.append(NL);
			}
		}

		sbMigrateLog.append(NL).append("Migration ended at ").append(
				DisplayFormatters.formatDate(System.currentTimeMillis())).append(NL);

		File logFile = new File(new File(pi.getUtilities().getUserDir(), "logs"),
				"uT_Migrate_Secret_" + (System.currentTimeMillis() / 1000) + ".log");
		File logFileHidden = new File(
				new File(pi.getUtilities().getUserDir(), "logs"),
				"uT_Migrate_Shareable_" + (System.currentTimeMillis() / 1000) + ".log");

		sbMigrateLog.append("This log was written with secrets to ").append(
				logFile.getAbsolutePath()).append(NL);
		sbMigrateLog.append("This log was written with secrets hidden to ").append(
				logFileHidden.getAbsolutePath()).append(NL);

		String migrateLog = sbMigrateLog.toString();

		FileUtil.writeStringAsFile(logFile, migrateLog);
		FileUtil.writeStringAsFile(logFileHidden, Utils.hidePrivate(migrateLog));

		MigrateListener[] listeners = configModelInfo.getListeners();
		for (MigrateListener l : listeners) {
			l.migrationComplete(migrateLog);
		}
	}

	private void processResumeFile(File configDir) {
		File fileResume = new File(configDir, "resume.dat");

		if (!fileResume.isFile()) {
			settingsImportInfo.logWarnings.append(
					"Could not find resume.dat in ").append(
							Utils.wrapString(configDir.toString())).append(NL);
			return;
		}

		try {
			BufferedInputStream is;
			is = new BufferedInputStream(new FileInputStream(fileResume));
			BDecoder decoder = new BDecoder(Constants.UTF_8);
			decoder.setMapDecodeListener((context, map, nestingLevel) -> {

				if (nestingLevel != 1) {
					return;
				}

				String torrentFileString = replaceFolders(context);
				File torrentFile = new File(torrentFileString);
				boolean isMagnet = torrentFileString.startsWith("magnet:")
						&& !torrentFileString.endsWith(".torrent");

				try {
					String origTorrentFilePath;
					if (!torrentFile.isAbsolute() && !isMagnet) {
						torrentFile = new File(configModelInfo.paramConfigDir.getValue(),
								torrentFileString);
						origTorrentFilePath = torrentFile.getAbsolutePath();
					} else {
						origTorrentFilePath = context;
					}
					configModelInfo.analysisStatus("migrateapp.status.analyzingTorrent",
							context);

					TorrentImportInfo importInfo = new TorrentImportInfo(this,
							torrentFile, context, map, origTorrentFilePath);

					if (isMagnet) {
						importInfo.logWarnings.setLength(0);
						importInfo.logWarnings.append(
								"Torrent entry is actually a magnet URI. Skipping ").append(
										Utils.wrapString(torrentFileString)).append(NL);
					}

					configModelInfo.analysisStatus("");
					if (importInfo.execOnComplete != null) {
						hasRunProgramEnabled = true;
					}
					listTorrentsToImport.add(importInfo);
				} catch (Throwable t) {
					String err = Utils.getErrorAndHideStuff(t, torrentFile.toString());
					settingsImportInfo.logWarnings.append(
							"Error analyzing torrent entry: ").append(err).append(NL);
				}
				// Reduce memory usage
				map.clear();
			});
			decoder.decodeStream(is, false);
			is.close();
		} catch (Throwable t) {
			String err = Utils.getErrorAndHideStuff(t, fileResume.toString());
			settingsImportInfo.logWarnings.append(
					"Error Analyzing resume.dat: ").append(err).append(NL);
		}
	}

	String replaceFolders(String path) {
		if (path.isEmpty()) {
			return path;
		}

		if (path.startsWith("~/")) {
			path = System.getProperty("user.home") + path.substring(1);
		}

		String result;
		char pathSeparator = detectDirSeparator(path);
		if (pathSeparator != File.separatorChar) {
			result = path.replace(pathSeparator, File.separatorChar);
		} else {
			result = path;
		}

		if (isRelativeAnyOS(path)) {
			return result;
		}

		String lowerPath = path.toLowerCase();
		for (String startsWith : mapFolderReplacements.keySet()) {
			if (lowerPath.startsWith(startsWith)) {
				String replaceWith = mapFolderReplacements.get(startsWith);
				result = replaceWith + path.substring(startsWith.length());
				if (pathSeparator != File.separatorChar) {
					result = result.replace(pathSeparator, File.separatorChar);
				}
				if (new File(result).exists()) {
					break;
				}
			}
		}
		if (utDirSeparator == '\\' && path.length() > 2
				&& result.substring(1, 3).equals(":/")) {
			// uT Config is Windows and referencing drive, but we are on non-windows
			// Log
			String drive = result.substring(0, 1).toUpperCase() + ":\\";
			HashSet<String> paths = needsPathReplacement.get(drive);
			if (paths == null) {
				paths = new HashSet<>();
				needsPathReplacement.put(drive, paths);
			}
			int lastSlashPos = result.lastIndexOf('/');
			int lastDotPos = result.lastIndexOf('.');
			String dir;
			if (lastDotPos > lastSlashPos && lastSlashPos >= 0) {
				dir = new File(result).getParent();
			} else if (lastSlashPos == result.length() - 1) {
				dir = result.substring(0, lastSlashPos);
			} else {
				dir = result;
			}
			dir = dir.substring(0, 1).toUpperCase() + dir.substring(1);
			dir = dir.replace("/", "\\");
			paths.add(dir);

			result = result.substring(2);
		} else if (utDirSeparator == '/' && result.startsWith("\\")) {
			// uT Config is non-windows and referencing /, but we are windows and need a drive
			HashSet<String> paths = needsPathReplacement.get("/");
			if (paths == null) {
				paths = new HashSet<>();
				needsPathReplacement.put("/", paths);
			}
			int lastSlashPos = result.lastIndexOf('\\');
			int lastDotPos = result.lastIndexOf('.');
			String dir;
			if (lastDotPos > lastSlashPos && lastSlashPos >= 0) {
				dir = new File(result).getParent();
			} else if (lastSlashPos == result.length() - 1) {
				dir = result.substring(0, lastSlashPos);
			} else {
				dir = result;
			}
			dir = dir.replace("\\", "/");
			paths.add(dir);
		}
		return result;
	}

	private boolean isRelativeAnyOS(String path) {
		boolean isAbsolute = new File(path).isAbsolute();
		if (isAbsolute) {
			return false;
		}
		if (path.length() > 2 && path.substring(1, 3).equals(":\\")) {
			return false;
		}
		if (path.startsWith("/")) {
			return false;
		}
		return true;
	}

	private static char detectDirSeparator(String path) {
		if (utDirSeparator > 0) {
			return utDirSeparator;
		}
		if (path == null) {
			return File.separatorChar;
		}
		if (path.length() > 2 && path.substring(1, 3).equalsIgnoreCase(":\\")) {
			utDirSeparator = '\\';
			return utDirSeparator;
		} else {
			boolean hasUnixSlash = path.contains("/");
			boolean hasWinSlash = path.contains("\\");
			if (hasUnixSlash != hasWinSlash) {
				utDirSeparator = hasUnixSlash ? '/' : '\\';
				return utDirSeparator;
			}
		}

		return File.separatorChar;
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

	public boolean canMigrate() {
		return needsPathReplacement.isEmpty();
	}
}
