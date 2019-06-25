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
import com.biglybt.core.tag.TagManagerFactory;
import com.biglybt.core.tag.TagType;
import com.biglybt.core.torrent.TOTorrent;
import com.biglybt.core.torrent.TOTorrentFile;
import com.biglybt.core.util.*;
import com.biglybt.core.util.TorrentUtils.ExtendedTorrent;
import com.biglybt.plugins.migratetorrentapp.Utils;
import com.biglybt.util.MapUtils;

import com.biglybt.pif.torrent.TorrentAttribute;

public class TorrentImportInfo
	implements Comparable<TorrentImportInfo>
{

	private final Importer_uTorrent importer;

	public ExtendedTorrent torrent;

	public StringBuilder logWarnings = new StringBuilder();

	public StringBuilder logInfo = new StringBuilder();

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
	 * com.biglybt.core.disk.DiskManagerFileInfoSet#setSkipped(boolean[], boolean)
	 */
	public boolean[] fileSkipState;

	/**
	 * com.biglybt.core.disk.DiskManagerFileInfoSet#setPriority(int[])
	 */
	public int[] filePriorities;

	public final Map<Integer, String> fileLinks = new HashMap<>();

	// TODO
	public String execOnComplete;

	public int startMode;

	public final List<List<String>> trackers = new ArrayList<>();

	/**
	 * List of tags stored in WeakHashMap so they go away if the tag goes away
	 */
	public final WeakHashMap<TagToAddInfo, Object> tags = new WeakHashMap<>();

	public File torrentFile;

	public byte[] infoHash;

	public boolean forceStart;

	public TorrentImportInfo(Importer_uTorrent importer, File torrentFile,
			Map<String, Object> map) {
		this.importer = importer;
		processTorrent(torrentFile, map);
	}

	private File findFile(final String relativeOrAbsoluteFile, String basePath) {
		File file = new File(relativeOrAbsoluteFile);
		if (file.isFile()) {
			return file;
		}
		boolean originalAbsolute = file.isAbsolute();
		if (!originalAbsolute) {
			file = new File(basePath, relativeOrAbsoluteFile);
			if (file.isFile()) {
				return file;
			}
		}

		String relativeFilename = file.getName();
		boolean differs = !relativeFilename.equals(relativeOrAbsoluteFile);

		for (String dataDir : importer.mapAdditionalDataDirs.keySet()) {
			Boolean recursive = importer.mapAdditionalDataDirs.get(dataDir);
			if (recursive != null && recursive) {
				file = lookForFile(relativeFilename, new File(dataDir));
			} else {
				// because relativeFilename might be "foo.wmv", but
				// relativeOrAbsoluteFile might be "bar/foo.wmv"
				if (differs) {
					file = new File(dataDir, relativeOrAbsoluteFile);
					if (file.isFile()) {
						return file;
					}
				}
				file = new File(dataDir, relativeFilename);
			}
			if (file != null && file.exists()) {
				return file;
			}
		}
		return originalAbsolute ? new File(relativeOrAbsoluteFile)
				: new File(basePath, relativeFilename);
	}

	static File lookForFile(String relativeOrAbsoluteFile, File path) {
		File file = new File(path, relativeOrAbsoluteFile);
		if (file.exists()) {
			return file;
		}

		if (!path.isDirectory()) {
			return null;
		}

		File[] dirs = path.listFiles(File::isDirectory);
		if (dirs == null) {
			return null;
		}
		for (File dir : dirs) {
			file = lookForFile(relativeOrAbsoluteFile, dir);
			if (file != null) {
				return file;
			}
		}
		return null;
	}

	public String toDebugString(boolean showPrivate) {
		String s = toDebugString();
		if (showPrivate) {
			return s;
		}

		return Utils.hidePrivate(s);
	}

	public String toDebugString() {
		StringBuilder sb = new StringBuilder();
		if (order >= 0) {
			sb.append("Incomplete ");
			sb.append("#");
			sb.append(order);
			sb.append(' ');
		} else {
			sb.append("Complete ");
		}

		if (torrent == null) {
			sb.append("<missing .torrent file>");
		} else {
			Utils.wrapString(sb, TorrentUtils.getLocalisedName(torrent));
		}

		sb.append('\n');

		if (hasWarnings()) {
			sb.append("\nWarnings:\n").append(logWarnings).append("\n");
		}

		if (obtainedFrom != null && obtainedFrom.length() > 0) {
			sb.append("Obtained from ").append(Utils.wrapString(obtainedFrom)).append(
					"\n");
		}

		sb.append("Save Path: ").append(Utils.wrapString(dirSavePath)).append("\n");

		long addedOn = MapUtils.getMapLong(mapDMStateParam,
				DownloadManagerState.PARAM_DOWNLOAD_ADDED_TIME, 0);
		if (addedOn > 0) {
			sb.append("Added On ");
			sb.append(DisplayFormatters.formatDate(addedOn));
			sb.append('\n');
		}

		if (torrent != null) {
			sb.append(torrent.getFileCount()).append(" files, ");
		}

		if (tags.size() == 0) {
			sb.append("No Tags\n");
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
			sb.append("\n");
		}

		if (order < 0) {
			long completedOn = MapUtils.getMapLong(mapDMStateParam,
					DownloadManagerState.PARAM_DOWNLOAD_COMPLETED_TIME, 0);
			if (completedOn > 0) {
				sb.append("Completed On ");
				sb.append(DisplayFormatters.formatDate(completedOn));
				sb.append('\n');
			}

		} else {
			if (pieceStates != null) {
				int numDonePieces = 0;
				int numStartedPieces = 0;
				int numPiecesNeedRecheck = 0;
				for (byte pieceState : pieceStates) {
					switch (pieceState) {
						case 1:
							numDonePieces++;
							break;
						case 2:
							numPiecesNeedRecheck++;
							break;
						case 3:
							numStartedPieces++;
							break;
					}
				}
				sb.append("Pieces: ").append(numDonePieces).append(" done, ").append(
						numPiecesNeedRecheck).append(" need recheck, ").append(
								numStartedPieces).append(" partial\n");
			}

			sb.append(downloadedBytes).append(" bytes downloaded\n");
			if (pieceStates != null && torrent != null) {
				long pieceLength = torrent.getPieceLength();
				int numberOfPieces = torrent.getNumberOfPieces();
				int lastPieceLength = (int) (torrent.getSize()
						- ((long) (numberOfPieces - 1) * (long) pieceLength));

				int lastBlockLength = lastPieceLength % DiskManager.BLOCK_SIZE;
				int lastBlock = lastPieceLength / DiskManager.BLOCK_SIZE;

				if (numberOfPieces != pieceStates.length) {
					sb.append("# state pieces (").append(pieceStates.length).append(
							" does not match real piece count of ").append(
									numberOfPieces).append("\n");
				}

				long haveBytes = 0;
				for (int i = 0, pieceStatesLength = pieceStates.length; i < pieceStatesLength; i++) {
					byte pieceState = pieceStates[i];
					if (pieceState == 1) {
						if (i == pieceStatesLength - 1) {

							haveBytes += lastPieceLength;
						} else {
							haveBytes += pieceLength;
						}
					}
				}
				sb.append(haveBytes).append(" bytes in fully downloaded pieces\n");

				if (mapPieceBlocks != null) {
					for (String pieceNumberString : mapPieceBlocks.keySet()) {
						List<Long> listBlockNumbers = mapPieceBlocks.get(pieceNumberString);
						boolean isLastPiece = pieceNumberString.equals(
								"" + (numberOfPieces - 1));
						for (Long listBlockNumber : listBlockNumbers) {
							if (isLastPiece && listBlockNumber == lastBlock) {
								haveBytes += lastBlockLength;
							} else {
								haveBytes += DiskManager.BLOCK_SIZE;
							}
						}
					}
				}
				sb.append(haveBytes).append(
						" bytes in fully downloaded pieces and blocks\n");

				if (downloadedBytes != haveBytes) {
					sb.append("Piece & Block Info does not match downloaded results!\n");
				}
			}
		}

		if (downSpeed > 0) {
			sb.append("Limit download speed to ").append(
					DisplayFormatters.formatByteCountToKiBEtcPerSec(downSpeed)).append(
							"\n");
		}
		if (upSpeed > 0) {
			sb.append("Limit upload speed to ").append(
					DisplayFormatters.formatByteCountToKiBEtcPerSec(downSpeed)).append(
							"\n");
		}

		if (mapDMStateParam.size() > 0) {
			sb.append("States: \n");
			for (String key : mapDMStateParam.keySet()) {
				Object val = mapDMStateParam.get(key);
				sb.append('\t').append(key).append(": ");
				if (val instanceof String) {
					sb.append(Utils.wrapString((String) val));
				} else {
					sb.append(val);
				}
				sb.append('\n');
			}
		}
		if (mapDMStateAttr.size() > 0) {
			sb.append("Attributes: \n");
			for (String key : mapDMStateAttr.keySet()) {
				Object val = mapDMStateAttr.get(key);
				sb.append('\t').append(key).append(": ");
				if (val instanceof String) {
					sb.append(Utils.wrapString((String) val));
				} else {
					sb.append(val);
				}
				sb.append('\n');
			}
		}

		sb.append("Downloaded for ").append(
				TimeFormatter.formatColon(downloadingForSecs));
		sb.append(", Seeding for ").append(
				TimeFormatter.formatColon(seedingForSecs));
		sb.append("\n");

		if (fileLinks.size() > 0) {
			sb.append(fileLinks.size()).append(" linked files\n");
			for (Integer index : fileLinks.keySet()) {
				sb.append("Index ").append(index).append(": ").append(
						Utils.wrapString(fileLinks.get(index))).append("\n");
			}
		}

		if (hasInfo()) {
			sb.append("\nInfo:\n").append(logInfo);
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
		int c = Long.compare(order, o.order);
		if (c == 0) {
			long completedOn0 = MapUtils.getMapLong(mapDMStateParam,
					DownloadManagerState.PARAM_DOWNLOAD_COMPLETED_TIME, 0);
			long completedOn1 = MapUtils.getMapLong(o.mapDMStateParam,
					DownloadManagerState.PARAM_DOWNLOAD_COMPLETED_TIME, 0);
			c = Long.compare(order, o.order);

			if (c == 0) {
				c = Base32.encode(infoHash).compareTo(Base32.encode(o.infoHash));
			}
		}
		return c;
	}

	private void processTorrent(File _torrentFile, Map<String, Object> map) {

		infoHash = MapUtils.getMapByteArray(map, ResumeConstants.INFO, null);

		if (!_torrentFile.exists()) {
			File file = importer.mapInfoHashToFile.get(Base32.encode(infoHash));
			if (file != null) {
				logInfo.append(".torrent not in ");
				logInfo.append(Utils.wrapString(
						Utils.objectToString(_torrentFile.getAbsolutePath())));
				logInfo.append(", but found as ");
				logInfo.append(Utils.wrapString(file.getAbsolutePath()));
				logInfo.append("\n");
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
						logInfo.append("\n");

						_torrentFile = newTorrentFile;
					}
				}
			}
		}

		try {
			byte[] bytes = FileUtil.readFileAsByteArray(_torrentFile);
			Map existing_map = BDecoder.decode(bytes);
			boolean hasEncoding = existing_map.containsKey("encoding");
			if (hasEncoding) {
				Map existing_info = (Map) existing_map.get("info");
				List files = (List) existing_info.get("files");
				if (files != null && files.size() > 0) {
					boolean hasUTF8Path = (((Map) files.get(0)).containsKey(
							"path.utf-8"));
					if (hasUTF8Path) {
						logWarnings.append(
								"path.utf-8 and encoding key found in .torrent file. Removing encoding key so BiglyBT reads it properly\n");
						existing_map.remove("encoding");
						File tempTorrentFile = File.createTempFile("Migrate_", ".torrent");
						tempTorrentFile.deleteOnExit();
						FileUtil.writeBytesAsFile2(tempTorrentFile.getAbsolutePath(),
								BEncoder.encode(existing_map));
						_torrentFile = tempTorrentFile;
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
			logWarnings.append(": \n\t");
			logWarnings.append(s);
			logWarnings.append('\n');
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

		String caption = MapUtils.getMapString(map, ResumeConstants.CAPTION, null);
		if (caption != null) {
			mapDMStateAttr.put(DownloadManagerState.AT_DISPLAY_NAME, caption);
		}

		long completedOn = MapUtils.getMapLong(map, ResumeConstants.COMPLETED_ON,
				0);
		if (completedOn > 0 && order < 0) {
			// TODO: when not complete, this value might be created on..
			mapDMStateParam.put(DownloadManagerState.PARAM_DOWNLOAD_COMPLETED_TIME,
					completedOn * 1000);
		}

		corruptBytes = MapUtils.getMapLong(map, ResumeConstants.CORRUPT, 0);

		obtainedFrom = MapUtils.getMapString(map, ResumeConstants.DOWNLOAD_URL,
				null);

		downloadedBytes = MapUtils.getMapLong(map, ResumeConstants.DOWNLOADED, 0);

		downSpeed = MapUtils.getMapInt(map, ResumeConstants.DOWNSPEED, 0);

		hashFails = MapUtils.getMapInt(map, ResumeConstants.HASHFAILS, 0);

		if (torrent != null) {
			int numPieces = torrent.getNumberOfPieces();
			byte[] havePiecesBits = (byte[]) map.get(ResumeConstants.HAVE);
			byte[] hashedPiecesBits = (byte[]) map.get(ResumeConstants.HASHED);
			/**
			 * Note:
			 * BiglyBT Stores blocks in "resume"->"data"->"blocks" in active directory
			 * The format is a map, with the key as the pieceNumber (String), and value
			 * a List of block numbers (List<Number>)
			 * <br>
			 * {@link DownloadManagerState#setResumeData(Map)} takes a map which has one key of "data"
			 * <br>
			 * See Also {@link RDResumeHandler#saveResumeData(DownloadManagerState, Map)
			 */
			List uTorrentPieceBlocks = MapUtils.getMapList(map,
					ResumeConstants.BLOCKS, Collections.EMPTY_LIST);
			if (uTorrentPieceBlocks.size() > 0) {
				long pieceLength = torrent.getPieceLength();
				// Stolen from DiskManagerPieceImpl(DiskManagerHelper, int, int)
				int numBlocks = (int) ((pieceLength + DiskManager.BLOCK_SIZE - 1)
						/ DiskManager.BLOCK_SIZE);
				int blockBitsLength = (numBlocks + 7) / 8;

				mapPieceBlocks = new HashMap<>();
				for (int blockArrayIndex = 0, uTorrentPieceBlocksSize = uTorrentPieceBlocks.size(); blockArrayIndex < uTorrentPieceBlocksSize; blockArrayIndex++) {
					Object uTorrentPieceBlock = uTorrentPieceBlocks.get(blockArrayIndex);
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
						logWarnings.append("blocks[").append(blockArrayIndex).append(
								"] = ");
						logWarnings.append(ByteFormatter.nicePrint(rowBytes, true)).append(
								"\n");
					}
					String key = "" + pieceNo;

					List<Long> haveBlocks = new ArrayList<>();
					mapPieceBlocks.put(key, haveBlocks);

					if (rowBytes.length != 4 + blockBitsLength) {
						logWarnings.append("'blocks' length expected to be "
								+ (4 + blockBitsLength) + ", but was " + rowBytes.length
								+ " for piece #" + key + "\n");
					}

					int curBlockNo = 0;
					for (int i = 4; i < rowBytes.length && curBlockNo < numBlocks; i++) {
						byte blockByte = rowBytes[i];
						for (int bitPos = 0; bitPos < 8; bitPos++) {
							boolean haveBitSet = (blockByte & (byte) (1 << bitPos)) != 0;
							if (haveBitSet) {
								haveBlocks.add((long) curBlockNo);
							}
							curBlockNo++;
							if (curBlockNo == numBlocks) {
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
									: (hashedPiecesBits[pieceBitsPos]
											& (byte) (1 << bitPos)) != 0;
							/*
							 * private static final byte PIECE_NOT_DONE         = 0;
							 * private static final byte PIECE_DONE             = 1;
							 * private static final byte PIECE_RECHECK_REQUIRED = 2;
							 * private static final byte PIECE_STARTED          = 3;
							 */
							pieceStates[pieceNo] = (byte) (haveBitSet ? hashedBitSet ? 1 : 2
									: mapPieceBlocks != null
											&& mapPieceBlocks.containsKey("" + pieceNo) ? 3 : 0);
							pieceNo++;
							if (pieceNo == numPieces) {
								break;
							}
						}
						pieceBitsPos++;
					}
				} catch (Throwable t) {
					logWarnings.append(Debug.getNestedExceptionMessageAndStack(t));
				}
			}
		}

		String tagName = MapUtils.getMapString(map, ResumeConstants.LABEL, "");
		if (!tagName.isEmpty()) {
			importer.addTag(this, tagName, "uTorrent");
		}

		List labels = MapUtils.getMapList(map, ResumeConstants.LABELS,
				Collections.emptyList());
		for (int i = 0, labelsSize = labels.size(); i < labelsSize; i++) {
			String label = Utils.objectToString(labels.get(i));
			if (label == null || label.equals(tagName)) {
				continue;
			}
			importer.addTag(this, label, "uTorrent");
		}

		long lastSeenComplete = MapUtils.getMapLong(map,
				ResumeConstants.LAST_SEEN_COMPLETE, 0);
		if (lastSeenComplete > 0) {
			mapDMStateAttr.put(DownloadManagerState.AT_TIME_STOPPED,
					lastSeenComplete * 1000);
		}

		long lastActiveSecsAgo = MapUtils.getMapLong(map,
				ResumeConstants.LAST_ACTIVE, 0);
		// Value doesn't make sends.  xx seconds ago from what? maybe "time"?
		//if (lastActiveSecsAgo > 0) {
		//	mapDMStateAttr.put(DownloadManagerState.PARAM_DOWNLOAD_LAST_ACTIVE_TIME, lastActiveSecsAgo * 1000);
		//}

		long maxPeers = MapUtils.getMapLong(map, ResumeConstants.MAX_CONNECTIONS,
				0);
		if (maxPeers > 0) {
			mapDMStateAttr.put(DownloadManagerState.PARAM_MAX_PEERS, maxPeers);
		}

		dirSavePath = MapUtils.getMapString(map, ResumeConstants.PATH, "");
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
				} catch (Throwable e) {
					System.err.println(
							"Bad Peer: " + ByteFormatter.nicePrint(peer6, true));
					e.printStackTrace();
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
			if (filePriority == (byte) 0x80) { //skip
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
			execOnComplete = execOnComplete.replaceAll("\\s*%M", "");
			if (execOnComplete.contains("%S")) {
				logWarnings.append("%S not supported for exec-on-complete\n");
			}
		} else {
			execOnComplete = null;
		}

		downloadingForSecs = MapUtils.getMapLong(map, ResumeConstants.RUNTIME, 0);
		seedingForSecs = MapUtils.getMapLong(map, ResumeConstants.SEEDTIME, 0);

		int startMode = MapUtils.getMapInt(map, ResumeConstants.STARTED, 0);
		switch (startMode) {
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
				logWarnings.append("Unknown startmode of " + startMode);
				if (dlError.length() > 0) {
					logWarnings.append(". DL Error Message: ").append(dlError);
				}
				logWarnings.append("\n");
				break;
			}
		}

		List listTargets = MapUtils.getMapList(map, ResumeConstants.TARGETS,
				Collections.emptyList());
		if (listTargets.size() > 0) {
			for (Object target : listTargets) {
				if (!(target instanceof List)) {
					continue;
				}
				List listTarget = (List) target;
				if (listTarget.size() != 2) {
					logWarnings.append("Unknown file retarget: ").append(
							listTargets.toString()).append("\n");
					continue;
				}
				Object o0 = listTarget.get(0);
				if (!(o0 instanceof Number)) {
					logWarnings.append(
							"Unknown file retarget: index 0 not number, but ").append(
									o0).append("\n");
					;
					continue;
				}
				Object o1 = listTarget.get(1);
				String newPath = Utils.objectToString(o1);
				if (newPath == null) {
					logWarnings.append(
							"Unknown file retarget: index 1 not String, but ").append(
									o1).append("\n");
					continue;
				}
				int fileIndex = ((Number) o0).intValue();

				if (new File(newPath).isAbsolute()) {
					newPath = importer.replaceFolders(newPath);
				}
				File file = findFile(newPath, dirSavePath);
				fileLinks.put(fileIndex, file.getAbsolutePath());
			}
		}

		// Check all files now that we have the target links

		if (torrent != null) {
			Map<Integer, String> mapNotFound = new LinkedHashMap<>();
			Map<Integer, String> mapRelinked = new LinkedHashMap<>();

			TOTorrentFile[] files = torrent.getFiles();
			for (int i = 0, filesLength = files.length; i < filesLength; i++) {
				String s = fileLinks.get(i);
				if (s != null) {
					continue;
				}
				// TODO: What if file skipped? We don't need it, unless it shares a piece with a wanted file

				TOTorrentFile file = files[i];
				String relativePath = file.getRelativePath();
				if (new File(dirSavePath, relativePath).isFile()) {
					continue;
				}
				File foundFile = findFile(relativePath, dirSavePath);
				if (foundFile != null && foundFile.isFile()) {
					fileLinks.put(i, foundFile.getAbsolutePath());
					mapRelinked.put(i, relativePath);
				} else {
					mapNotFound.put(i, relativePath);
				}
			}

			if (mapNotFound.size() > 0) {
				logWarnings.append("Could not find the following files: ");
				boolean first = true;
				if (mapNotFound.size() > 5) {
					for (Integer integer : mapNotFound.keySet()) {
						if (first) {
							first = false;
						} else {
							logWarnings.append(", ");
						}
						logWarnings.append("#");
						logWarnings.append(integer);
					}
				} else {
					for (String value : mapNotFound.values()) {
						if (first) {
							first = false;
						} else {
							logWarnings.append(", ");
						}
						logWarnings.append(Utils.wrapString(value));
					}
				}
				logWarnings.append('\n');
			}
			if (mapRelinked.size() > 0) {
				logInfo.append("Relinked files: ");
				boolean first = true;
				if (mapRelinked.size() > 5) {
					for (Integer integer : mapNotFound.keySet()) {
						if (first) {
							first = false;
						} else {
							logInfo.append(", ");
						}
						logInfo.append("#");
						logInfo.append(integer);
					}
				} else {
					for (String value : mapRelinked.values()) {
						if (first) {
							first = false;
						} else {
							logInfo.append(", ");
						}
						logInfo.append(Utils.wrapString(value));
					}
				}
				logInfo.append('\n');
			}
		}

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
				} else if (trackerGroup instanceof List) {
					List<String> group = new ArrayList<>();
					for (Object o : ((List) trackerGroup)) {
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

		//String localisedName = TorrentUtils.getLocalisedName(torrent);
		//System.out.println(localisedName);
	}

	public void addDownloadManager() {
		DownloadManager dm = importer.gm.addDownloadManager(
				torrentFile.getAbsolutePath(), infoHash, dirSavePath, startMode, true,
				order == -1, new DownloadManagerInitialisationAdapter() {
					@Override
					public void initialised(DownloadManager dm, boolean for_seeding) {
						initDM(dm);
					}

					@Override
					public int getActions() {
						return ACT_ASSIGNS_TAGS;
					}
				});
		if (dm != null && forceStart) {
			dm.setForceStart(true);
		}
	}

	private void initDM(DownloadManager dm) {
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

		dmStats.restoreSessionTotals(downloadedBytes, uploadedBytes, wasteBytes,
				hashFails, downloadingForSecs, seedingForSecs);

		// Peer Cache
		Map trackerResponseCache = new LightHashMap(1);

		List biglyPeers = new ArrayList();
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
		if (fileSkipState != null) {
			file_info_set.setSkipped(fileSkipState, true);
		}

		if (filePriorities != null) {
			file_info_set.setPriority(filePriorities);
		}

		if (fileLinks.size() > 0) {
			// Can't use fileInfo.setLink(fDest) as it renames
			// the existing file if there is one
			DiskManagerFileInfo[] fileInfos = file_info_set.getFiles();
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

		// Resume data
		if (mapPieceBlocks != null || pieceStates != null) {
			Map mapResume = new HashMap();
			Map mapResumeData = new HashMap();
			mapResume.put("data", mapResumeData);
			if (mapPieceBlocks != null) {
				mapResumeData.put("blocks", mapPieceBlocks);
			}
			if (pieceStates != null) {
				mapResumeData.put("resume data", pieceStates);
			}
			mapResumeData.put("valid", 1);
			downloadState.setResumeData(mapResumeData);
		}

		// Tags

		TagType ttManual = TagManagerFactory.getTagManager().getTagType(
				TagType.TT_DOWNLOAD_MANUAL);
		for (TagToAddInfo tagToAddInfo : tags.keySet()) {
			Tag tag = ttManual.getTag(tagToAddInfo.name, false);
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

			} catch (Throwable e) {

				Debug.out(e);
			}
		}

		if (execOnComplete != null) {
			TorrentAttribute attr = importer.pi.getTorrentManager().getPluginAttribute(
					"command");
			downloadState.setAttribute(attr.getName(), execOnComplete);
		}
	}

}
