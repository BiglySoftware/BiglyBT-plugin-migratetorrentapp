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
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import com.biglybt.core.disk.DiskManager;
import com.biglybt.core.disk.DiskManagerFileInfo;
import com.biglybt.core.disk.DiskManagerFileInfoSet;
import com.biglybt.core.disk.impl.resume.RDResumeHandler;
import com.biglybt.core.download.DownloadManager;
import com.biglybt.core.download.DownloadManagerInitialisationAdapter;
import com.biglybt.core.download.DownloadManagerState;
import com.biglybt.core.download.DownloadManagerStats;
import com.biglybt.core.tag.Tag;
import com.biglybt.core.torrent.TOTorrent;
import com.biglybt.core.torrent.TOTorrentFile;
import com.biglybt.core.util.*;
import com.biglybt.core.util.TorrentUtils.ExtendedTorrent;
import com.biglybt.plugins.migratetorrentapp.Utils;
import com.biglybt.util.MapUtils;

import com.biglybt.pif.torrent.TorrentAttribute;

import static com.biglybt.plugins.migratetorrentapp.Utils.NL;

/**
 * TODO: Do we do simple torrents correctly (save path, renames, etc)
 * TODO: If we find all the files in the same relative path, we should change the dirSavePath and remove links
 */
public class TorrentImportInfo
	implements Comparable<TorrentImportInfo>
{
	private static boolean PARTIAL_PIECES_IN_DL_BYTES = false;

	private static final String TG_UTORRENT = "uTorrent";

	private final Importer_uTorrent importer;

	private final String torrentKey;

	public ExtendedTorrent torrent;

	public final StringBuilder logWarnings = new StringBuilder();

	public final StringBuilder logInfo = new StringBuilder();

	/**
	 * {@link DownloadManagerState}#PARAM_*, set via {@link DownloadManagerState#setIntParameter(String, int)}, etc
	 */
	public final Map<String, Object> mapDMStateParam = new HashMap<>();

	/**
	 * {@link DownloadManagerState}#AT_*, set via {@link DownloadManagerState#setAttribute(String, String)}, etc
	 */
	public final Map<String, Object> mapDMStateAttr = new HashMap<>();

	/**
	 * {@link TorrentUtils#setObtainedFrom(TOTorrent, String)}
	 */
	public String obtainedFrom;

	/** Corrupt Bytes is probably just hashFails * piecelength **/
	public long corruptBytes;

	/** Need to be set via	{@link DownloadManagerStats#restoreSessionTotals} **/
	public long downloadedBytes;

	/** Need to be set via	{@link DownloadManagerStats#restoreSessionTotals} **/
	public long uploadedBytes;

	/** Need to be set via	{@link DownloadManagerStats#restoreSessionTotals} **/
	public int hashFails;

	/** Need to be set via	{@link DownloadManagerStats#restoreSessionTotals} **/
	public long wasteBytes;

	/** Need to be set via	{@link DownloadManagerStats#restoreSessionTotals} **/
	public long downloadingForSecs;

	/** Need to be set via	{@link DownloadManagerStats#restoreSessionTotals} **/
	public long seedingForSecs;

	/** Need to be set via {@link DownloadManagerStats#setDownloadRateLimitBytesPerSecond(int)} */
	public int downSpeed;

	/** Need to be set via {@link DownloadManagerStats#setUploadRateLimitBytesPerSecond(int)} */
	public int upSpeed;

	private static final byte PIECE_NOT_DONE = 0;

	private static final byte PIECE_DONE = 1;

	private static final byte PIECE_RECHECK_REQUIRED = 2;

	private static final byte PIECE_STARTED = 3;

	/**
	 * See {@link RDResumeHandler}<br>
	 * <br>
	 * <pre>
	 * private static final byte PIECE_NOT_DONE         = 0;
	 * private static final byte PIECE_DONE             = 1;
	 * private static final byte PIECE_RECHECK_REQUIRED = 2;
	 * private static final byte PIECE_STARTED          = 3;
	 * </pre>
	 * <br>
	 * <br>
	 * BiglyBT Stores blocks in "resume"->"data"->"resume data" in active directory
	 **/
	public byte[] pieceStates;

	public List<Long> filesBytesDownloaded = null;

	/**
	 * Map&lt;PieceNumber, List&lt;Block Numbers>>
	 * <br>
	 * <br>
	 * BiglyBT Stores blocks in "resume"->"data"->"blocks" in active directory
	 * The format is a map, with the key as the pieceNumber (String), and value
	 * a List of block numbers (List<Number>)
	 * <br>
	 * <br>
	 * {@link DownloadManagerState#setResumeData(Map)} takes a map which has one key of "data"
	 * <br>
	 * See Also {@link RDResumeHandler#saveResumeData(DownloadManagerState, Map)
	 */
	public Map<String, List<Long>> mapPieceBlocks;

	/**
	 * Position. -1 seeding.  Used for sorting before adding
	 */
	public long order;

	public String dirSavePath;

	/**
	 * For Peer Cache reference, there's
	 * {@link com.biglybt.core.tracker.client.impl.TRTrackerAnnouncerImpl#exportTrackerCache()}
	 *
	 * <p/>
	 * For Reading peer cache:
	 * {@link com.biglybt.core.tracker.client.impl.TRTrackerAnnouncerFactoryImpl#getCachedPeers(java.util.Map)}
	 *
	 * There's also {@link com.biglybt.core.util.TorrentUtils#setPeerCache(com.biglybt.core.torrent.TOTorrent, java.util.Map)
	 */
	public final List<InetSocketAddress> peers = new ArrayList<>();

	/**
	 * {@link com.biglybt.core.disk.DiskManagerFileInfoSet#setSkipped(boolean[], boolean)}
	 */
	public boolean[] fileSkipState;

	/**
	 * com.biglybt.core.disk.DiskManagerFileInfoSet#setPriority(int[])
	 */
	public int[] filePriorities;

	/**
	 * Map&lt;FileIndex, AbsolutePath>
	 */
	public final Map<Integer, String> fileLinks = new HashMap<>();

	// TODO
	public String execOnComplete;

	public int startMode = DownloadManager.STATE_STOPPED;

	public final List<List<String>> trackers = new ArrayList<>();

	/**
	 * List of tags stored in WeakHashMap so they go away if the tag goes away
	 */
	public final WeakHashMap<TagToAddInfo, Object> tags = new WeakHashMap<>();

	public File torrentFile;

	public byte[] infoHash;

	public boolean forceStart;

	private long haveBlockBytes;

	private long havePieceBytes;

	private String caption;

	public TorrentImportInfo(Importer_uTorrent importer, File torrentFile,
			String torrentKey, Map<String, Object> map, String origTorrentFilePath) {
		this.importer = importer;
		this.torrentKey = torrentKey;
		processTorrent(torrentFile, origTorrentFilePath, map);
	}

	private static boolean existsAndSizeOrZero(File file, long requiredSize) {
		if (file == null) {
			return false;
		}
		if (!file.exists()) {
			return false;
		}
		if (requiredSize < 0) {
			return true;
		}
		long length = file.length();
		if (length == 0) {
			return true;
		}
		return length == requiredSize;
	}

	private File findFile(final String relativeOrAbsoluteFile, String basePath,
			long requiredSize) {
		File file = new File(relativeOrAbsoluteFile);
		if (existsAndSizeOrZero(file, requiredSize)) {
			return file;
		}
		boolean originalAbsolute = file.isAbsolute();
		if (!originalAbsolute) {
			file = new File(basePath, relativeOrAbsoluteFile);
			if (existsAndSizeOrZero(file, requiredSize)) {
				return file;
			}
		}

		String relativeFilename = file.getName();
		boolean differs = !relativeFilename.equals(relativeOrAbsoluteFile);

		if (differs) {
			// because relativeFilename might be "foo.wmv", but
			// relativeOrAbsoluteFile might be "bar/foo.wmv"
			for (File dir : importer.listAdditionalDataDirs) {
				file = new File(dir, relativeOrAbsoluteFile);
				if (existsAndSizeOrZero(file, requiredSize)) {
					return file;
				}
			}
		}

		if (requiredSize > 0) {
			List<File> files = importer.mapFileSizeToScannedFile.get(requiredSize);
			if (files != null) {
				for (File fileMatchingSize : files) {
					if (fileMatchingSize.getName().equals(relativeFilename)) {
						return fileMatchingSize;
					}
				}
			}
		}

		List<File> files = importer.mapFilenameToScannedFile.get(relativeFilename);
		if (files != null) {
			// We could also pick the file with a size smaller than requiredSize, 
			// since it might be a partial file due to skipped state and overlapping 
			// piece usage, but that sounds dangerous without more logic
			for (File fileMatchingName : files) {
				if (fileMatchingName.length() == 0) {
					return fileMatchingName;
				}
			}
		}

		return originalAbsolute ? new File(relativeOrAbsoluteFile)
				: new File(basePath, relativeFilename);
	}

	public String getName() {
		if (caption != null) {
			return caption;
		}
		if (torrent != null) {
			return TorrentUtils.getLocalisedName(torrent);
		}
		return torrentKey;
	}

	public boolean canImport() {
		return torrent != null;
	}

	public String toDebugString(boolean showFullDetails) {
		StringBuilder sb = new StringBuilder();
		if (order >= 0) {
			sb.append("Incomplete ");
			sb.append("#");
			sb.append(order);
			sb.append(' ');
		} else {
			sb.append("Complete ");
		}

		sb.append(Utils.wrapString(getName()));
		sb.append(NL);

		if (hasWarnings()) {
			sb.append(NL).append("Warnings:").append(NL).append(logWarnings).append(
					NL);
		}

		if (obtainedFrom != null && obtainedFrom.length() > 0) {
			sb.append("Obtained from ").append(Utils.wrapString(obtainedFrom)).append(
					NL);
		}

		sb.append("Save Path: ").append(Utils.wrapString(dirSavePath)).append(NL);

		long addedOn = MapUtils.getMapLong(mapDMStateParam,
				DownloadManagerState.PARAM_DOWNLOAD_ADDED_TIME, 0);
		if (addedOn > 0) {
			sb.append("Added On ");
			sb.append(DisplayFormatters.formatDate(addedOn));
			sb.append(NL);
		}

		if (torrent != null) {
			sb.append(torrent.getFileCount()).append(" files, ");
		}

		if (showFullDetails) {
			if (tags.size() == 0) {
				sb.append("No Tags").append(NL);
			} else {
				int numTags = 0;
				StringBuilder sbTags = new StringBuilder();
				for (TagToAddInfo tagToAddInfo : tags.keySet()) {
					if (tagToAddInfo == null) {
						continue;
					}
					if (numTags > 0) {
						sbTags.append(", ");
					}
					Utils.wrapString(sbTags, tagToAddInfo.name);
					numTags++;
				}
				sb.append(numTags);
				sb.append(" Tags: ");
				sb.append(sbTags);
				sb.append(NL);
			}
		}

		if (order < 0) {
			long completedOn = MapUtils.getMapLong(mapDMStateParam,
					DownloadManagerState.PARAM_DOWNLOAD_COMPLETED_TIME, 0);
			if (completedOn > 0) {
				sb.append("Completed On ");
				sb.append(DisplayFormatters.formatDate(completedOn));
				sb.append(NL);
			}

		} else {
			if (pieceStates != null && showFullDetails) {
				int numDonePieces = 0;
				int numStartedPieces = 0;
				int numPiecesNeedRecheck = 0;
				for (byte pieceState : pieceStates) {
					switch (pieceState) {
						case RDResumeHandler.PIECE_DONE:
							numDonePieces++;
							break;
						case RDResumeHandler.PIECE_RECHECK_REQUIRED:
							numPiecesNeedRecheck++;
							break;
						case RDResumeHandler.PIECE_STARTED:
							numStartedPieces++;
							break;
					}
				}
				sb.append("Pieces: ").append(numDonePieces).append(" done, ").append(
						numPiecesNeedRecheck).append(" need recheck, ").append(
								numStartedPieces).append(" partial").append(NL);
			}

			sb.append(downloadedBytes).append(" bytes downloaded").append(NL);
			if (torrent != null) {
				sb.append(havePieceBytes).append(
						" bytes in fully downloaded pieces").append(NL);
				if (haveBlockBytes > 0) {
					sb.append(havePieceBytes + haveBlockBytes).append(
							" bytes in fully downloaded pieces and blocks").append(NL);
				}
				if (downloadedBytes != havePieceBytes + haveBlockBytes) {
					sb.append(
							"Piece & Block Info does not match downloaded results!").append(
									NL);
				}
			}
		}

		if (showFullDetails) {
			if (downSpeed > 0) {
				sb.append("Limit download speed to ").append(
						DisplayFormatters.formatByteCountToKiBEtcPerSec(downSpeed)).append(
								NL);
			}
			if (upSpeed > 0) {
				sb.append("Limit upload speed to ").append(
						DisplayFormatters.formatByteCountToKiBEtcPerSec(upSpeed)).append(
								NL);
			}

			if (mapDMStateParam.size() > 0) {
				sb.append("States: ").append(NL);
				for (String key : mapDMStateParam.keySet()) {
					Object val = mapDMStateParam.get(key);
					sb.append('\t').append(key).append(": ");
					if (val instanceof String) {
						sb.append(Utils.wrapString((String) val));
					} else {
						sb.append(val);
					}
					sb.append(NL);
				}
			}
			if (mapDMStateAttr.size() > 0) {
				sb.append("Attributes: ").append(NL);
				for (String key : mapDMStateAttr.keySet()) {
					Object val = mapDMStateAttr.get(key);
					sb.append('\t').append(key).append(": ");
					if (val instanceof String) {
						sb.append(Utils.wrapString((String) val));
					} else {
						sb.append(val);
					}
					sb.append(NL);
				}
			}
		}

		sb.append("Downloaded for ").append(
				TimeFormatter.formatColon(downloadingForSecs));
		sb.append(", Seeding for ").append(
				TimeFormatter.formatColon(seedingForSecs));
		sb.append(NL);

		if (fileLinks.size() > 0 && showFullDetails) {
			sb.append(fileLinks.size()).append(" linked files").append(NL);
			int numFakeRelinks = 0;
			for (Integer index : fileLinks.keySet()) {
				String absoluteFile = fileLinks.get(index);
				if (absoluteFile != null && absoluteFile.endsWith(".!ut")
						&& absoluteFile.startsWith(dirSavePath)) {
					numFakeRelinks++;
				} else {
					sb.append("\tIndex ").append(index).append(": ").append(
							Utils.wrapString(absoluteFile)).append(NL);
				}
			}
			if (numFakeRelinks > 0) {
				sb.append("\t").append(numFakeRelinks).append(
						" files relinked to incomplete extension .!ut").append(NL);
			}
		}

		if (hasInfo()) {
			sb.append(NL).append("Info:").append(NL).append(logInfo);
		}

		return sb.toString();
	}

	public boolean hasWarnings() {
		return logWarnings.length() > 0;
	}

	public boolean hasInfo() {
		return logInfo.length() > 0;
	}

	@Override
	public int compareTo(TorrentImportInfo o) {
		boolean complete0 = order == -1;
		boolean complete1 = o.order == -1;

		int c = Boolean.compare(complete0, complete1);
		if (c != 0) {
			return c;
		}

		c = Long.compare(order, o.order);
		if (c != 0) {
			return c;
		}

		long completedOn0 = MapUtils.getMapLong(mapDMStateParam,
				DownloadManagerState.PARAM_DOWNLOAD_COMPLETED_TIME, 0);
		long completedOn1 = MapUtils.getMapLong(o.mapDMStateParam,
				DownloadManagerState.PARAM_DOWNLOAD_COMPLETED_TIME, 0);
		c = Long.compare(completedOn0, completedOn1);
		if (c != 0) {
			return c;
		}

		if (infoHash == null || o.infoHash == null) {
			return infoHash == null ? 1 : o.infoHash == null ? -1 : 0;
		}
		return Base32.encode(infoHash).compareTo(Base32.encode(o.infoHash));
	}

	private void processTorrent(File _torrentFile, String origTorrentFilePath,
			Map<String, Object> map) {

		infoHash = MapUtils.getMapByteArray(map, ResumeConstants.INFO, null);

		if (!_torrentFile.exists()) {
			File file = infoHash == null ? null
					: importer.mapInfoHashToFile.get(Base32.encode(infoHash));
			if (file != null) {
				logInfo.append(".torrent not in ");
				logInfo.append(Utils.wrapString(
						Utils.objectToString(_torrentFile.getAbsolutePath())));
				logInfo.append(", but found as ");
				logInfo.append(Utils.wrapString(file.getAbsolutePath()));
				logInfo.append(NL);
				_torrentFile = file;
			} else {
				for (String torrentDir : importer.listAdditionalTorrentDirs) {
					File newTorrentFile = new File(torrentDir, _torrentFile.getName());
					if (newTorrentFile.exists()) {
						logInfo.append(".torrent not in ");
						logInfo.append(Utils.wrapString(
								Utils.objectToString(_torrentFile.getAbsolutePath())));
						logInfo.append(", but found at ");
						logInfo.append(Utils.wrapString(newTorrentFile.getAbsolutePath()));
						logInfo.append(NL);

						_torrentFile = newTorrentFile;
						break;
					}
				}
			}
		}

		if (_torrentFile.exists()) {

			// Fixup discrepencies between BiglyBT and uT when reading torrent files
			// with "encoding" key
			try {
				byte[] bytes = FileUtil.readFileAsByteArray(_torrentFile);
				Map<String, Object> existing_map = BDecoder.decode(bytes);
				boolean hasEncoding = existing_map != null
						&& existing_map.containsKey("encoding");
				if (hasEncoding) {
					String encoding = MapUtils.getMapString(existing_map, "encoding", "");
					boolean hasNonUTF8Encoding = !encoding.equalsIgnoreCase("utf8")
							&& !encoding.equalsIgnoreCase("utf-8");
					if (hasNonUTF8Encoding) {
						Map existing_info = (Map) existing_map.get("info");
						List files = (List) existing_info.get("files");
						if (files != null && files.size() > 0) {
							boolean hasUTF8Path = (((Map) files.get(0)).containsKey(
									"path.utf-8"));
							if (hasUTF8Path) {
								logInfo.append(
										"path.utf-8 and encoding key found in .torrent file. Removing encoding key so BiglyBT reads it properly").append(
												NL);
								existing_map.remove("encoding");
								File tempTorrentFile = File.createTempFile("Migrate_",
										".torrent", AETemporaryFileHandler.getTempDirectory());
								tempTorrentFile.deleteOnExit();
								FileUtil.writeBytesAsFile2(tempTorrentFile.getAbsolutePath(),
										BEncoder.encode(existing_map));
								_torrentFile = tempTorrentFile;
							}
						}
					}
				}

				torrent = TorrentUtils.readDelegateFromFile(_torrentFile, false);
				torrentFile = _torrentFile;

			} catch (Exception e) {
				String absolutePath = _torrentFile.getAbsolutePath();
				String s = e.getMessage();
				s = Utils.wrapSubString(s, absolutePath);
				s = Utils.wrapSubString(s, _torrentFile.getName());
				logWarnings.append("Error reading ");
				logWarnings.append(Utils.wrapString(absolutePath));
				if (!absolutePath.equals(origTorrentFilePath)) {
					logWarnings.append(" (Original path: ").append(
							Utils.wrapString(origTorrentFilePath)).append(")");
				}
				logWarnings.append(": ").append(NL).append("\t");
				logWarnings.append(s);
				logWarnings.append(NL);
			}
		} else {
			logWarnings.append("Could not find ").append(
					Utils.wrapString(origTorrentFilePath)).append(". ");
			if (!origTorrentFilePath.equals(_torrentFile.getAbsolutePath())) {
				logWarnings.append("Also tried ").append(
						Utils.wrapString(_torrentFile.getAbsolutePath())).append(". ");
			}
			logWarnings.append("Torrent will be skipped during migration").append(NL);
		}

		order = MapUtils.getMapLong(map, ResumeConstants.ORDER, 0);

		long addedOn = MapUtils.getMapLong(map, ResumeConstants.ADDED_ON, 0);
		if (addedOn > 0) {
			mapDMStateParam.put(DownloadManagerState.PARAM_DOWNLOAD_ADDED_TIME,
					addedOn * 1000);
		}

		importer.addTagFromMap(this, map, ResumeConstants.APP_OWNER,
				"uTorrent: App Owner");
		importer.addTagFromMap(this, map, ResumeConstants.APP_TYPE,
				"uTorrent: App Type");

		caption = MapUtils.getMapString(map, ResumeConstants.CAPTION, null);
		if (caption != null) {
			mapDMStateAttr.put(DownloadManagerState.AT_DISPLAY_NAME, caption);
		}

		long completedOn = MapUtils.getMapLong(map, ResumeConstants.COMPLETED_ON,
				0);
		if (completedOn > 0 && order < 0) {
			mapDMStateParam.put(DownloadManagerState.PARAM_DOWNLOAD_COMPLETED_TIME,
					completedOn * 1000);
		}

		corruptBytes = MapUtils.getMapLong(map, ResumeConstants.CORRUPT, 0);

		obtainedFrom = MapUtils.getMapString(map, ResumeConstants.DOWNLOAD_URL,
				null);

		downloadedBytes = MapUtils.getMapLong(map, ResumeConstants.DOWNLOADED, 0);

		downSpeed = MapUtils.getMapInt(map, ResumeConstants.DOWNSPEED, 0);

		hashFails = MapUtils.getMapInt(map, ResumeConstants.HASHFAILS, 0);

		processExtendedTorrent(map, torrent);

		String tagName = MapUtils.getMapString(map, ResumeConstants.LABEL, "");
		if (!tagName.isEmpty()) {
			importer.addTag(this, tagName, TG_UTORRENT);
		}

		List labels = MapUtils.getMapList(map, ResumeConstants.LABELS,
				Collections.emptyList());
		for (Object value : labels) {
			String label = Utils.objectToString(value);
			if (label == null || label.equals(tagName)) {
				continue;
			}
			importer.addTag(this, label, TG_UTORRENT);
		}

		long lastSeenComplete = MapUtils.getMapLong(map,
				ResumeConstants.LAST_SEEN_COMPLETE, 0);
		if (lastSeenComplete > 0) {
			mapDMStateAttr.put(DownloadManagerState.AT_TIME_STOPPED,
					lastSeenComplete * 1000);
		}

		long lastActiveSecsAgo = MapUtils.getMapLong(map,
				ResumeConstants.LAST_ACTIVE, 0);
		if (lastActiveSecsAgo > 0) {
			long time = MapUtils.getMapLong(map, ResumeConstants.TIME, 0);
			if (time > 0) {
				long lastactiveTimeMS = (time * 1000) + (lastActiveSecsAgo * 1000);
				mapDMStateAttr.put(DownloadManagerState.PARAM_DOWNLOAD_LAST_ACTIVE_TIME,
						lastactiveTimeMS);
			}
		}

		long maxPeers = MapUtils.getMapLong(map, ResumeConstants.MAX_CONNECTIONS,
				0);
		if (maxPeers > 0) {
			mapDMStateAttr.put(DownloadManagerState.PARAM_MAX_PEERS, maxPeers);
		}

		dirSavePath = MapUtils.getMapString(map, ResumeConstants.ROOTDIR,
				MapUtils.getMapString(map, ResumeConstants.PATH, ""));
		if (dirSavePath.length() > 0) {
			dirSavePath = importer.replaceFolders(dirSavePath);
		}

		byte[] peersIPV6 = MapUtils.getMapByteArray(map, ResumeConstants.PEERS6,
				new byte[0]);
		if (peersIPV6.length > 0 && peersIPV6.length % 18 == 0) {
			// 16 bytes IPv6, 2 bytes port

			byte[] peer6 = new byte[16];
			byte[] port = new byte[2];
			for (int i = 0; i < peersIPV6.length; i += 18) {
				System.arraycopy(peersIPV6, i, peer6, 0, 16);
				System.arraycopy(peersIPV6, i + 16, port, 0, 2);

				try {
					InetAddress address = Inet6Address.getByAddress(peer6);
					InetSocketAddress socketAddress = new InetSocketAddress(address,
							((port[0] & 0xff)) << 8 | (port[1] & 0xff));
					peers.add(socketAddress);
				} catch (Throwable t) {
					String err = Utils.getErrorAndHideStuff(t);
					logWarnings.append("Bad Peer: ").append(
							ByteFormatter.nicePrint(peer6, true)).append("; ").append(
									err).append(NL);
				}
			}
		}

		// File Priorities in uTorrent include skipped, whereas BiglyBT has a
		// separate flag for skipped (so it can remember the priority state if the user ever unskips)
		byte[] filePrioritiesUT = MapUtils.getMapByteArray(map,
				ResumeConstants.PRIO, new byte[0]);
		for (int i = 0, filePrioritiesLength = filePrioritiesUT.length; i < filePrioritiesLength; i++) {
			byte filePriority = filePrioritiesUT[i];
			if (filePriority == 0x08) {
				continue;
			}
			if (filePriority == (byte) 0x80 || filePriority == 0x00) { //skip
				if (fileSkipState == null) {
					fileSkipState = new boolean[filePrioritiesLength];
				}
				fileSkipState[i] = true;
			} else {
				if (filePriorities == null) {
					filePriorities = new int[filePrioritiesLength];
				}
				if (importer.settingsImportInfo.granularPriorities) {
					filePriorities[i] = filePriority - 0x08;
				} else {
					filePriorities[i] = filePriority < 0x08 ? -1 : 1;
				}
			}
		}

		execOnComplete = MapUtils.getMapString(map, ResumeConstants.RUN_PROGRAM,
				"");
		if (execOnComplete.length() > 0) {
			/**
			 * azexec:
			 *  supports  %D %N %F %L %T %I %K %M
			 *  doesn't support %S %P
			 *  differs in %M (uTorrent Status Message vs Full torrent file name)
			 */
			// TODO: split exec out and importer.replaceFolders
			execOnComplete = execOnComplete.replaceAll("\\s*%M", "");
			if (execOnComplete.contains("%S")) {
				logWarnings.append("%S not supported for exec-on-complete").append(NL);
			}
		} else {
			execOnComplete = null;
		}

		downloadingForSecs = MapUtils.getMapLong(map, ResumeConstants.RUNTIME, 0);
		seedingForSecs = MapUtils.getMapLong(map, ResumeConstants.SEEDTIME, 0);

		int utStartMode = MapUtils.getMapInt(map, ResumeConstants.STARTED, 0);
		switch (utStartMode) {
			case 0: {
				startMode = DownloadManager.STATE_STOPPED;
				break;
			}
			case 1: {
				startMode = DownloadManager.STATE_QUEUED;
				forceStart = true;
				break;
			}
			case 2: // started
			case 3: // running
			{
				startMode = DownloadManager.STATE_QUEUED;
				break;
			}
			case 4:
			default: {
				// TODO: Error State: Ensure we force recheck
				startMode = DownloadManager.STATE_STOPPED;
				String dlError = MapUtils.getMapString(map, ResumeConstants.DL_ERROR,
						"");
				logWarnings.append("Unknown startmode of ").append(startMode);
				if (dlError.length() > 0) {
					logWarnings.append(". DL Error Message: ").append(dlError);
				}
				logWarnings.append(NL);
				break;
			}
		}

		processFiles(map, torrent);

		List listTrackers = MapUtils.getMapList(map, ResumeConstants.TRACKERS,
				null);
		if (listTrackers != null) {
			for (Object trackerGroup : listTrackers) {
				if (trackerGroup instanceof String) {
					// Single tracker
					trackers.add(Collections.singletonList((String) trackerGroup));
				} else if (trackerGroup instanceof byte[]) {
					trackers.add(Collections.singletonList(
							new String((byte[]) trackerGroup, Constants.UTF_8)));
				} else if (trackerGroup instanceof Iterable) {
					List<String> group = new ArrayList<>();
					for (Object o : ((Iterable) trackerGroup)) {
						if (o instanceof String) {
							group.add((String) o);
						} else if (o instanceof byte[]) {
							group.add(new String((byte[]) o, Constants.UTF_8));
						}
					}
					trackers.add(group);
				}
			}
		}

		long ulSlots = MapUtils.getMapLong(map, ResumeConstants.ULSLOTS, 0);
		if (ulSlots > 0) {
			mapDMStateParam.put(DownloadManagerState.PARAM_MAX_PEERS, ulSlots);
		}

		uploadedBytes = MapUtils.getMapLong(map, ResumeConstants.UPLOADED, 0);

		upSpeed = MapUtils.getMapInt(map, ResumeConstants.UPSPEED, 0);

		boolean overrideSeedSettings = MapUtils.getMapBoolean(map,
				ResumeConstants.OVERRIDE_SEEDSETTINGS, false);
		if (overrideSeedSettings) {
			// Queue Rules has a global and per-torrent min share ratio, stored in thousandths
			long fpMinShareRatio = MapUtils.getMapLong(map,
					ResumeConstants.WANTED_RATIO, 0) * 100;
			if (fpMinShareRatio > 0) {
				mapDMStateAttr.put(DownloadManagerState.PARAM_MIN_SHARE_RATIO,
						fpMinShareRatio);
			}
			// No per-torrent settings for these two
			//long minSeedTimeSecs = MapUtils.getMapLong(map, ResumeConstants.WANTED_SEEDTIME, 0);
			//long seedUntilNumSeeds = MapUtils.getMapLong(map, ResumeConstants.WANTED_SEEDNUM, 0);
		}

		wasteBytes = MapUtils.getMapLong(map, ResumeConstants.WASTE, 0);
	}

	private void processFiles(Map<String, Object> map, TOTorrent torrent) {
		TOTorrentFile[] torrentFiles = torrent == null ? null : torrent.getFiles();

		byte[] suffixFlags = MapUtils.getMapByteArray(map, ResumeConstants.SUFFIXES,
				null);
		// Not sure if uT can have the "append .!ut" disabled but still have torrents with suffix flag bits on (and still have .!ut appended)
		// BiglyBT can.  In case uT can, we don't rely on the settings.dat flag, but let the bits indicate if we enabled suffix
		boolean anySuffixesEnabled = false;
		if (suffixFlags != null) {
			for (byte suffixFlag : suffixFlags) {
				if (suffixFlag != 0) {
					anySuffixesEnabled = true;
					break;
				}
			}
		}
		if (anySuffixesEnabled) {
			mapDMStateAttr.put(DownloadManagerState.AT_INCOMP_FILE_SUFFIX, ".!ut");
		}

		List listTargets = MapUtils.getMapList(map, ResumeConstants.TARGETS,
				Collections.emptyList());
		for (Object target : listTargets) {
			if (!(target instanceof List)) {
				continue;
			}

			List listTarget = (List) target;
			if (listTarget.size() != 2) {
				logWarnings.append("Unknown file retarget: ").append(
						listTargets.toString()).append(NL);
				continue;
			}

			Object o0 = listTarget.get(0);
			if (!(o0 instanceof Number)) {
				logWarnings.append(
						"Unknown file retarget: index 0 not number, but ").append(
								o0).append(NL);
				continue;
			}

			Object o1 = listTarget.get(1);
			String newPath = Utils.objectToString(o1);
			if (newPath == null) {
				logWarnings.append(
						"Unknown file retarget: index 1 not String, but ").append(
								o1).append(NL);
				continue;
			}

			newPath = importer.replaceFolders(newPath);
			int fileIndex = ((Number) o0).intValue();

			File file = findFile(newPath, dirSavePath,
					torrentFiles == null ? -1 : torrentFiles[fileIndex].getLength());
			fileLinks.put(fileIndex, file.getAbsolutePath());
		}

		if (torrentFiles == null) {
			return;
		}

		if (anySuffixesEnabled) {
			int pos = 0;
			for (byte suffixFlag : suffixFlags) {
				for (int bitPos = 0; bitPos < 8; bitPos++) {
					boolean isBitSet = (suffixFlag & (byte) (1 << bitPos)) != 0;
					if (isBitSet) {
						String s = fileLinks.get(pos);
						if (s == null) {
							s = new File(dirSavePath, torrentFiles[pos].getRelativePath()
									+ ".!ut").getAbsolutePath();
						} else {
							s += ".!ut";
						}
						File file = findFile(s, dirSavePath, torrentFiles[pos].getLength());
						fileLinks.put(pos, file.getAbsolutePath());
					}
					pos++;
				}
			}
		}

		// Check all files now that we have the target links

		havePieceBytes = 0;
		Map<Integer, String> mapNotFound = new LinkedHashMap<>();
		Map<String, String> mapRelinked = new LinkedHashMap<>();
		filesBytesDownloaded = new ArrayList<>();

		long pieceLength = torrent.getPieceLength();
		long runningTorrentSize = 0;

		haveBlockBytes = 0;
		PartFile partFile = null;
		for (int i = 0, filesLength = torrentFiles.length; i < filesLength; i++) {
			TOTorrentFile file = torrentFiles[i];

			long fileLength = file.getLength();
			int start = file.getFirstPieceNumber();
			int end = file.getLastPieceNumber();

			long fileStartsAtGlobalPos = runningTorrentSize;
			long fileStartsAtPieceNo = fileStartsAtGlobalPos / pieceLength;
			if (fileStartsAtPieceNo != start) {
				logWarnings.append("File #").append(i).append(
						" inconsistent piece start ").append(" (rs=").append(
								runningTorrentSize).append(";pl=").append(pieceLength).append(
										")").append(NL);
			}
			long fileStartsIntoPiecePos = fileStartsAtGlobalPos % pieceLength;
			long fileStartPieceBytes = Math.min(fileLength,
					pieceLength - fileStartsIntoPiecePos);

			long fileStartsAtBlockNo = fileStartsIntoPiecePos
					/ DiskManager.BLOCK_SIZE;
			long fileStartsIntoBlockPos = fileStartsIntoPiecePos
					% DiskManager.BLOCK_SIZE;
			long fileStartBlockBytes = Math.min(fileLength,
					DiskManager.BLOCK_SIZE - fileStartsIntoBlockPos);

			runningTorrentSize += fileLength;
			long fileEndsAtGlobalPos = runningTorrentSize - 1;
			long fileEndsAtPieceNo = fileEndsAtGlobalPos / pieceLength;
			if (fileEndsAtPieceNo != end) {
				logWarnings.append("File #").append(i).append(
						" inconsistent piece end ").append(end).append(" vs ").append(
								fileEndsAtPieceNo).append(" (rs=").append(
										runningTorrentSize).append(";pl=").append(
												pieceLength).append(")").append(NL);
			}
			long fileEndsIntoPiecePos = fileEndsAtGlobalPos % pieceLength;
			long fileEndPieceBytes = Math.min(fileLength, fileEndsIntoPiecePos + 1);
			long fileEndsAtBlockNo = fileEndsIntoPiecePos / DiskManager.BLOCK_SIZE;
			long fileEndsIntoBlockPos = fileEndsIntoPiecePos % DiskManager.BLOCK_SIZE;
			long fileEndBlockBytes = Math.min(fileLength, fileEndsIntoBlockPos + 1);

			// uT doesn't create the file until a byte in the piece is downloaded
			boolean needFile = false;
			long pieceBytesDownloaded = 0;
			if (pieceStates != null) {
				for (int j = start; j <= end; j++) {
					switch (pieceStates[j]) {
						case PIECE_STARTED: {
							if (mapPieceBlocks != null) {
								List<Long> listHaveBlocks = mapPieceBlocks.get("" + j);
								if (listHaveBlocks != null && listHaveBlocks.size() > 0) {
									if (j == start) {
										for (Long haveBlockNo : listHaveBlocks) {
											if (haveBlockNo >= fileStartsAtBlockNo
													&& (fileEndsAtPieceNo > start
															|| haveBlockNo <= fileEndsAtBlockNo)) {
												//System.out.println(i + ", start piece " + j + ", block " + haveBlockNo + "; fileStartsAtBlockNo=" + fileStartsAtBlockNo + ";" + ((haveBlockNo == fileStartsAtBlockNo) ? fileStartBlockBytes : DiskManager.BLOCK_SIZE));
												haveBlockBytes += (haveBlockNo == fileStartsAtBlockNo)
														? fileStartBlockBytes : DiskManager.BLOCK_SIZE;
												needFile = true;
											}
										}
									} else if (j == end) {
										for (Long haveBlockNo : listHaveBlocks) {
											if (haveBlockNo <= fileEndsAtBlockNo
													&& (fileStartsAtPieceNo < end
															|| haveBlockNo >= fileStartsAtBlockNo)) {
												//System.out.println(i + ", end piece " + j + ", block " + haveBlockNo + "; fileEndsAtBlockNo=" + fileEndsAtBlockNo + ";" + ((haveBlockNo == fileEndsAtBlockNo) ? fileEndBlockBytes : DiskManager.BLOCK_SIZE));
												haveBlockBytes += (haveBlockNo == fileEndsAtBlockNo)
														? fileEndBlockBytes : DiskManager.BLOCK_SIZE;
												needFile = true;
											}
										}
									} else {
										// all blocks are fully ours
										haveBlockBytes += listHaveBlocks.size()
												* DiskManager.BLOCK_SIZE;
										needFile = true;
									}
									break;
								}
							}
							// explicitly fall through if there's no blocks for piece
						}
						case PIECE_DONE: {
							needFile = true;
							pieceBytesDownloaded += (j == start) ? fileStartPieceBytes
									: (j == end) ? fileEndPieceBytes : pieceLength;
							break;
						}
					}
				}
			}
			havePieceBytes += pieceBytesDownloaded;
			filesBytesDownloaded.add(PARTIAL_PIECES_IN_DL_BYTES
					? pieceBytesDownloaded + haveBlockBytes : pieceBytesDownloaded);

			if (!needFile) {
				continue;
			}

			String fileLink = fileLinks.get(i);
			if (fileLink != null && new File(fileLink).isFile()) {
				continue;
			}
			// no link or link not a file, try relative path in default save path for torrent
			String relativePath = file.getRelativePath();
			if (new File(dirSavePath, relativePath).isFile()) {
				continue;
			}
			// Still no file, try to find it
			File foundFile = findFile(relativePath, dirSavePath, fileLength);
			if (foundFile.isFile()) {
				String existingFileLink = fileLinks.put(i, foundFile.getAbsolutePath());
				mapRelinked.put(i + (existingFileLink == null ? "" : "*"),
						relativePath);
				continue;
			}

			if (fileSkipState != null && fileSkipState[i]) {
				boolean needStartPart = fileStartsIntoPiecePos != 0;
				boolean needEndPart = fileEndsIntoPiecePos != pieceLength - 1;
				if (needStartPart || needEndPart) {
					if (partFile == null) {
						partFile = new PartFile(torrent);
					}
					if (partFile.hasPartFile()) {

						boolean hasBytes = true;
						if (needStartPart) {
							hasBytes &= partFile.hasByteRange(fileStartsAtGlobalPos,
									fileStartPieceBytes);
						}

						if (needEndPart) {
							hasBytes &= partFile.hasByteRange(
									fileEndsAtGlobalPos - fileEndPieceBytes, fileEndPieceBytes);
						}

						if (hasBytes) {
							logInfo.append("Found ");
							logInfo.append("#");
							logInfo.append(i);
							logInfo.append(":");
							logInfo.append(Utils.wrapString(relativePath));
							logInfo.append(" in ~uTorrentPartFile");
							logInfo.append(NL);
							
							// TODO: Store that we need to pull the part file data into
							//       a format BiglyBT can use.
							
							continue;
						} else {
							logWarnings.append(
									"Torrent has ~uTorrentPartFile, but no entry for ");
							logWarnings.append("#");
							logWarnings.append(i);
							logWarnings.append(":");
							logWarnings.append(Utils.wrapString(relativePath));
							if (needStartPart) {
								logWarnings.append(", global start location ").append(
										fileStartsAtGlobalPos).append(", length ").append(
												fileStartPieceBytes);
							}
							if (needEndPart) {
								logWarnings.append(", global end location ").append(
										fileEndsAtGlobalPos - fileEndPieceBytes).append(
												", length ").append(fileEndPieceBytes);
							}
							logWarnings.append(NL);
						}
					}
				}
			}

			mapNotFound.put(i, fileLink == null ? relativePath : fileLink);
		}

		if (mapNotFound.size() > 0) {
			logWarnings.append("Could not find the following files: ").append(NL);
			boolean first = true;
			boolean showNames = mapNotFound.size() <= 5;
			for (int idx : mapNotFound.keySet()) {
				if (first) {
					first = false;
				} else if (!showNames) {
					logWarnings.append(", ");
				}
				if (showNames) {
					logWarnings.append("\t");
				}
				logWarnings.append("#");
				logWarnings.append(idx);
				if (showNames) {
					logWarnings.append(":");
					logWarnings.append(Utils.wrapString(mapNotFound.get(idx))).append(NL);
				}
			}
			logWarnings.append(NL);
		}
		if (mapRelinked.size() > 0) {
			logInfo.append("Relinked files: ");
			boolean first = true;
			boolean showNames = mapRelinked.size() <= 5;
			for (String idx : mapRelinked.keySet()) {
				if (first) {
					first = false;
				} else {
					logInfo.append(", ");
				}
				logInfo.append("#");
				logInfo.append(idx);
				if (showNames) {
					logInfo.append(":");
					logInfo.append(Utils.wrapString(mapRelinked.get(idx)));
				}
			}
			logInfo.append(NL);
		}
	}

	private void processExtendedTorrent(Map<String, Object> map,
			TOTorrent torrent) {
		if (torrent == null) {
			return;
		}
		int numPieces = torrent.getNumberOfPieces();
		long pieceLength = torrent.getPieceLength();
		int numBlocksPerPiece = (int) ((pieceLength + DiskManager.BLOCK_SIZE - 1)
				/ DiskManager.BLOCK_SIZE);

		byte[] havePiecesBits = (byte[]) map.get(ResumeConstants.HAVE);
		byte[] hashedPiecesBits = (byte[]) map.get(ResumeConstants.HASHED);
		/**
		 * Note:
		 * BiglyBT Stores blocks in "resume"->"data"->"blocks" in active directory
		 * The format is a map, with the key as the pieceNumber (String), and value
		 * a List of block numbers (List&lt;Number>)
		 * <br>
		 * {@link DownloadManagerState#setResumeData(Map)} takes a map which has one key of "data"
		 * <br>
		 * See Also {@link RDResumeHandler#saveResumeData(DownloadManagerState, Map)
		 */
		List uTorrentPieceBlocks = MapUtils.getMapList(map, ResumeConstants.BLOCKS,
				Collections.emptyList());
		if (uTorrentPieceBlocks.size() > 0) {
			// Stolen from DiskManagerPieceImpl(DiskManagerHelper, int, int)
			int blockBitsLength = (numBlocksPerPiece + 7) / 8;

			mapPieceBlocks = new HashMap<>();
			for (int blockIndex = 0, numBlocks = uTorrentPieceBlocks.size(); blockIndex < numBlocks; blockIndex++) {
				Object uTorrentPieceBlock = uTorrentPieceBlocks.get(blockIndex);
				if (!(uTorrentPieceBlock instanceof byte[])) {
					continue;
				}
				byte[] rowBytes = (byte[]) uTorrentPieceBlock;
				ByteBuffer pieceNoBytes = ByteBuffer.wrap(rowBytes, 0, 4);
				pieceNoBytes.order(ByteOrder.LITTLE_ENDIAN);
				long pieceNo = pieceNoBytes.getInt();
				if (pieceNo < 0 || pieceNo >= numPieces) {
					logWarnings.append("Piece Number ").append(pieceNo).append(
							" out of range (max ").append(numPieces - 1).append("). ");
					logWarnings.append("blocks[").append(blockIndex).append("] = ");
					logWarnings.append(ByteFormatter.nicePrint(rowBytes, true)).append(
							NL);
				}
				String key = "" + pieceNo;

				List<Long> haveBlocks = new ArrayList<>();
				mapPieceBlocks.put(key, haveBlocks);

				if (rowBytes.length != 4 + blockBitsLength) {
					logWarnings.append("'blocks' length expected to be ").append(
							4 + blockBitsLength).append(", but was ").append(
									rowBytes.length).append(" for piece #").append(key).append(
											NL);
				}

				int curBlockNo = 0;
				for (int i = 4; i < rowBytes.length
						&& curBlockNo < numBlocksPerPiece; i++) {
					byte blockByte = rowBytes[i];
					for (int bitPos = 0; bitPos < 8; bitPos++) {
						boolean haveBitSet = (blockByte & (byte) (1 << bitPos)) != 0;
						if (haveBitSet) {
							haveBlocks.add((long) curBlockNo);
						}
						curBlockNo++;
						if (curBlockNo == numBlocksPerPiece) {
							break;
						}
					}
				}

			}
		}
		if (havePiecesBits != null) {
			try {
				pieceStates = new byte[numPieces];
				int pieceNo = 0;
				int pieceBitsPos = 0;
				while (pieceNo < numPieces) {
					for (int bitPos = 0; bitPos < 8; bitPos++) {
						boolean haveBitSet = (havePiecesBits[pieceBitsPos]
								& (byte) (1 << bitPos)) != 0;
						boolean hashedBitSet = hashedPiecesBits == null ? haveBitSet
								: (hashedPiecesBits[pieceBitsPos] & (byte) (1 << bitPos)) != 0;
						pieceStates[pieceNo] = haveBitSet
								? hashedBitSet ? PIECE_DONE : PIECE_RECHECK_REQUIRED
								: mapPieceBlocks != null
										&& mapPieceBlocks.containsKey("" + pieceNo) ? PIECE_STARTED
												: PIECE_NOT_DONE;
						pieceNo++;
						if (pieceNo == numPieces) {
							break;
						}
					}
					pieceBitsPos++;
				}
			} catch (Throwable t) {
				String err = Utils.getErrorAndHideStuff(t);
				logWarnings.append(err);
			}
		}
	}

	public StringBuilder migrate() {
		StringBuilder sbMigrateLog = new StringBuilder();

		if (!canImport()) {
			sbMigrateLog.append("Skipping Torrent ").append(
					Utils.wrapString(getName())).append(
							", .torrent file not found for ").append(
									Utils.wrapString(
											new HashWrapper(infoHash).toBase32String())).append(NL);
			return sbMigrateLog;
		}

		DownloadManager existingDM = importer.gm.getDownloadManager(torrent);
		if (existingDM != null) {
			sbMigrateLog.append("Skipping Migrating Torrent ");
			sbMigrateLog.append(Utils.wrapString(getName()));
			sbMigrateLog.append(". Already exists in BiglyBT as ");
			sbMigrateLog.append(Utils.wrapString(existingDM.getDisplayName())).append(
					NL);
			return sbMigrateLog;
		}

		startMode = DownloadManager.STATE_STOPPED; // TODO: Remove me or add option
		File fileDirSavePath = new File(dirSavePath);
		DownloadManager dm = importer.gm.addDownloadManager(
				torrentFile.getAbsolutePath(), infoHash, fileDirSavePath.getParent(),
				fileDirSavePath.getName(), startMode, true, order == -1,
				new DownloadManagerInitialisationAdapter() {
					@Override
					public void initialised(DownloadManager dm, boolean for_seeding) {
						initDM(dm, sbMigrateLog);
					}

					@Override
					public int getActions() {
						return ACT_ASSIGNS_TAGS;
					}
				});
		if (dm != null) {
			postInitDM(dm);
		}

		return sbMigrateLog;
	}

	private void initDM(DownloadManager dm, StringBuilder sbMigrateLog) {
		TOTorrent torrent = dm.getTorrent();
		TorrentUtils.setObtainedFrom(torrent, obtainedFrom);

		// State Settings
		DownloadManagerState downloadState = dm.getDownloadState();
		try {
			downloadState.suppressStateSave(true);
			for (String stateKey : mapDMStateParam.keySet()) {
				Object val = mapDMStateParam.get(stateKey);
				if (val instanceof Number) {
					downloadState.setLongParameter(stateKey, ((Number) val).longValue());
				} else if (val instanceof Boolean) {
					downloadState.setBooleanParameter(stateKey, (Boolean) val);
				} else {
					System.err.println("Bad state param key: " + stateKey);
				}
			}
			for (String attrKey : mapDMStateAttr.keySet()) {
				Object val = mapDMStateAttr.get(attrKey);
				if (val instanceof Number) {
					downloadState.setLongAttribute(attrKey, ((Number) val).longValue());
				} else if (val instanceof Boolean) {
					downloadState.setBooleanAttribute(attrKey, (Boolean) val);
				} else if (val instanceof String) {
					downloadState.setAttribute(attrKey, (String) val);
				} else {
					System.err.println("Bad state attr key: " + attrKey);
				}
			}
		} finally {
			downloadState.suppressStateSave(false);
		}

		// Stat Settings
		DownloadManagerStats dmStats = dm.getStats();
		if (downSpeed > 0) {
			dmStats.setDownloadRateLimitBytesPerSecond(downSpeed);
		}
		if (upSpeed > 0) {
			dmStats.setUploadRateLimitBytesPerSecond(upSpeed);
		}

		// Peer Cache. See com.biglybt.core.tracker.client.impl.TRTrackerAnnouncerFactoryImpl.getCachedPeers
		Map<String, List<Map<String, Object>>> trackerResponseCache = new LightHashMap<>(
				1);

		List<Map<String, Object>> biglyPeers = new ArrayList<>();
		for (InetSocketAddress peer : peers) {
			Map<String, Object> mapPeer = new HashMap<>();
			biglyPeers.add(mapPeer);

			mapPeer.put("ip", peer.getAddress().getHostAddress().getBytes());
			mapPeer.put("port", (long) peer.getPort());
			mapPeer.put("udpport", (long) peer.getPort());
		}

		trackerResponseCache.put("tracker_peers", biglyPeers);

		downloadState.setTrackerResponseCache(trackerResponseCache);

		// File specific settings
		DiskManagerFileInfoSet file_info_set = dm.getDiskManagerFileInfoSet();
		DiskManagerFileInfo[] fileInfos = file_info_set.getFiles();

		if (fileLinks.size() > 0) {
			// Can't use fileInfo.setLink(fDest) as it renames
			// the existing file if there is one
			int numFiles = fileInfos.length;

			List<Integer> source_indexes = new ArrayList<>();
			List<File> link_sources = new ArrayList<>();
			List<File> link_destinations = new ArrayList<>();

			for (Integer fileIndex : fileLinks.keySet()) {
				String linkPath = fileLinks.get(fileIndex);
				if (fileIndex >= 0 && fileIndex < numFiles) {
					DiskManagerFileInfo fileInfo = fileInfos[fileIndex];
					File fDest = new File(linkPath);

					source_indexes.add(fileIndex);
					link_sources.add(fileInfo.getFile(false));
					link_destinations.add(fDest);
				}
			}
			if (source_indexes.size() > 0) {
				downloadState.setFileLinks(source_indexes, link_sources,
						link_destinations);
			}
		}

		if (fileSkipState != null) {
			boolean[] toCompact = new boolean[fileSkipState.length];
			boolean doCompact = false;
			for (int i = 0, fileSkipStateLength = fileSkipState.length; i < fileSkipStateLength; i++) {
				if (fileSkipState[i]) {
					File file = fileInfos[i].getFile(true);
					toCompact[i] = !file.exists() || file.length() == 0;
					doCompact = true;
				}
			}
			if (doCompact) {
				// This will reset resume data
				file_info_set.setStorageTypes(toCompact,
						DiskManagerFileInfo.ST_COMPACT);
			}
			file_info_set.setSkipped(fileSkipState, true);
		}

		if (filePriorities != null) {
			file_info_set.setPriority(filePriorities);
		}

		// Resume data
		if (mapPieceBlocks != null || pieceStates != null) {
			Map<String, Map> mapResume = new HashMap<>();
			Map<String, Object> mapResumeData = new HashMap<>();
			mapResume.put("data", mapResumeData);
			if (mapPieceBlocks != null) {
				mapResumeData.put("blocks", mapPieceBlocks);
			}
			if (pieceStates != null) {
				mapResumeData.put("resume data", pieceStates);
			}
			mapResumeData.put("valid", 1L); // must be long
			downloadState.setResumeData(mapResume);
		}

		if (filesBytesDownloaded != null) {
			Map<String, Object> mapFileDownloaded = new HashMap<>();
			mapFileDownloaded.put("downloaded", filesBytesDownloaded);
			downloadState.setMapAttribute(DownloadManagerState.AT_FILE_DOWNLOADED,
					mapFileDownloaded);
			try {
				Method setFileLinks = dm.getClass().getDeclaredMethod("setFileLinks");
				setFileLinks.setAccessible(true);
				setFileLinks.invoke(dm);
			} catch (Exception e) {
				sbMigrateLog.append("Error calling setFileLinks. ");
				sbMigrateLog.append(
						"Per-file downloaded bytes stats may not be correct and torrent will probably do a re-check when started. ");
				sbMigrateLog.append(Debug.getNestedExceptionMessageAndStack(e)).append(
						NL);
			}
		}

		// BiglyBT requires non-skipped files exist (and probably skipped files that share a piece with non-skipped)
		// If uT had pre-allocate space turned off (default), then we switch BiglyBT to BCFG_ENABLE_INCREMENTAL_FILE_CREATION
		// But even that requires a 0 byte file minimum, otherwise it will do a full recheck on start.
		boolean dataAlreadyAllocated = true;
		for (int i = 0; i < fileInfos.length; i++) {
			DiskManagerFileInfo fileInfo = fileInfos[i];
			if (fileInfo.isSkipped()) {
				boolean sharedPieceWithNonSkipped = false;
				if (i > 0) {
					int firstPieceNumber = fileInfo.getFirstPieceNumber();
					for (int j = i - 1; j >= 0 && !sharedPieceWithNonSkipped; j--) {
						DiskManagerFileInfo prevFileInfo = fileInfos[j];
						int prevLastPieceNumber = prevFileInfo.getLastPieceNumber();
						if (prevLastPieceNumber < firstPieceNumber) {
							break;
						}
						if (!prevFileInfo.isSkipped()) {
							sharedPieceWithNonSkipped = true;
						}
					}
				}
				if (!sharedPieceWithNonSkipped && i < fileInfos.length - 1) {
					int lastPieceNumber = fileInfo.getLastPieceNumber();
					for (int j = i + 1; j < fileInfos.length
							&& !sharedPieceWithNonSkipped; j++) {
						DiskManagerFileInfo nextFileInfo = fileInfos[j];
						int nextFirstPieceNumber = nextFileInfo.getFirstPieceNumber();
						if (nextFirstPieceNumber > lastPieceNumber) {
							break;
						}
						if (!nextFileInfo.isSkipped()) {
							sharedPieceWithNonSkipped = true;
						}
					}
				}
				if (!sharedPieceWithNonSkipped) {
					continue;
				}
			}
			File file = fileInfo.getFile(true);
			if (!file.exists()) {
				if (importer.settingsImportInfo.preAllocSpace) {
					dataAlreadyAllocated = false;
					break;
				}

				try {
					if (!file.getParentFile().isDirectory()) {
						file.getParentFile().mkdirs();
					}
					file.createNewFile();
				} catch (IOException e) {
					System.err.println("create new file: " + file);
					e.printStackTrace();
					dataAlreadyAllocated = false;
				}
			}
		}
		dm.setDataAlreadyAllocated(dataAlreadyAllocated);

		// Can't set these until after DM is added to GM
		//dmStats.restoreSessionTotals(downloadedBytes, uploadedBytes, wasteBytes,
		//		hashFails, downloadingForSecs, seedingForSecs);

		// Tags

		for (TagToAddInfo tagToAddInfo : tags.keySet()) {
			Tag tag = tagToAddInfo.tag;
			if (tag != null) {
				tag.addTaggable(dm);
			}
		}

		// Trackers

		if (!trackers.isEmpty()) {
			List<List<String>> currentTrackers = TorrentUtils.announceGroupsToList(
					torrent);
			List<List<String>> merged = TorrentUtils.mergeAnnounceURLs(
					currentTrackers, trackers);
			TorrentUtils.listToAnnounceGroups(merged, torrent);

			try {
				TorrentUtils.writeToFile(torrent);

			} catch (Throwable t) {
				String err = Utils.getErrorAndHideStuff(t);
				sbMigrateLog.append("Error setting trackers for torrent. ");
				sbMigrateLog.append(err).append(NL);
			}
		}

		if (execOnComplete != null) {
			TorrentAttribute attr = importer.pi.getTorrentManager().getPluginAttribute(
					"command");
			downloadState.setAttribute(attr.getName(), execOnComplete);
		}
	}

	private void postInitDM(DownloadManager dm) {
		DownloadManagerStats dmStats = dm.getStats();
		dmStats.restoreSessionTotals(downloadedBytes, uploadedBytes, wasteBytes,
				hashFails, downloadingForSecs, seedingForSecs);

		if (forceStart) {
			dm.setForceStart(true);
		}
	}
}
