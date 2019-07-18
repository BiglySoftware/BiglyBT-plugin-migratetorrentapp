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

/**
 *  Descriptions from:
 *  https://forum.utorrent.com/topic/32745-dat-keys/
 *  https://robertpearsonblog.wordpress.com/2016/11/10/utorrent-forensic-artifacts/
 *  https://articles.forensicfocus.com/2018/11/02/forensic-analysis-of-the-%CE%BCtorrent-peer-to-peer-client-in-windows/
 */
public class ResumeConstants {
	/** Long<br>
	 * [TORRENT JOBS LIST] Added On (UNIX time, in seconds)
	 * <p/>
	 * Date and Time Torrent added to client – Epoch/Unix Time
	 */
	public static final String ADDED_ON = "added_on";

	/** List of [List of [Long]]<br>
	 * 1 entry per file
	 */
	public static final String ANTIVIRUS = "antivirus";

	/** byte[]<br>
	 *
	 */
	public static final String APP_OWNER = "app_owner";

	/** byte[]<br>
	 *
	 */
	public static final String APP_TYPE = "app_type";

	/** byte[]<br>
	 *
	 */
	public static final String APP_URL = "app_url";

	/** List of [byte[]]<br>
	 * [iNTERNAL] A list of the blocks currently in progress (?)
	 * <p/>
	 * Each Item:<br>
	 *  byte 0 to 3: piece number<br>
	 *  byte 4 to 4 + ((piece_length / 16384) / 8): each bit represents a block
	 */
	public static final String BLOCKS = "blocks";

	/** Long<br>
	 * [iNTERNAL] The size of each block in the torrent job (in bytes)
	 */
	public static final String BLOCKSIZE = "blocksize";

	/** byte[]<br>
	 * [TORRENT JOBS LIST] Name
	 */
	public static final String CAPTION = "caption";

	/** Long<br>
	 * [TORRENT JOBS LIST] Codec (obtained from RSS feed; currently unsure of what the integer represents)
	 */
	public static final String CODEC = "codec";

	/** Long<br>
	 * [TORRENT JOBS LIST] Completed On (UNIX time, in seconds)
	 * <p/>
	 * Date and Time Torrent was completely downloaded OR Created in Epoch/Unix Time.
	 * <BR><br>
	 * Seen in the wild with 0 for incomplete torrents, but the reference above
	 * suggests it might be set toe created_on (perhaps in previous versions)
	 */
	public static final String COMPLETED_ON = "completed_on";

	/** Long<br>
	 *
	 */
	public static final String COMPLETION_PERIOD = "completion_period";

	/** list<br>
	 *
	 */
	public static final String CONVERTED_MEDIA = "converted_media";

	/** Long<br>
	 * [iNTERNAL] The amount of data wasted through hashfails (in bytes)
	 * <br>
	 * {@link #HASHFAILS} appears to be displayed instead of {@link #CORRUPT}
	 */
	public static final String CORRUPT = "corrupt";

	/** Long<br>
	 * Did this client create the listed torrent:<br>
	 * 1 = Client created torrent<br>
	 * 0 = Client did not create torrent (downloaded)<br>
	 */
	public static final String CREATED_TORRENT = "created_torrent";

	/** Long<br>
	 * [TORRENT PROPERTES] Use DHT (currently unsure of what the integer represents)
	 */
	public static final String DHT = "dht";

	/** String<br>
	 * Error String, such as "Invalid download state, try resuming"
	 */
	public static final String DL_ERROR = "dl_error";

	/** String<br>
	 * [TORRENT JOBS LIST] Source URL
	 * <p/>
	 * If the user added the Torrent file using the clients “Add torrent from URL” function then this would list the URL here.
	 */
	public static final String DOWNLOAD_URL = "download_url";

	/** Long<br>
	 * [TORRENT JOBS LIST] Downloaded (in bytes)
	 * <p/>
	 * Number of bytes downloaded of the file by the client.
	 */
	public static final String DOWNLOADED = "downloaded";

	/** Long<br>
	 * [TORRENT PROPERTES] Maximum downloadrate (in bytes/second)
	 */
	public static final String DOWNSPEED = "downspeed";

	/** Long<br>
	 * [TORRENT JOBS LIST] Episode (obtained from RSS feed)
	 */
	public static final String EPISODE = "episode";

	/** Long<br>
	 *
	 */
	public static final String EPISODE_TO = "episode_to";

	/** Long<br>
	 *
	 */
	public static final String FEED_ID = "feed_id";

	/** byte[]<br>
	 * [iNTERNAL] The RSS feed that the .torrent file was originally listed on
	 */
	public static final String FEED_URL = "feed_url";

	/** byte[]<br>
	 *
	 */
	public static final String GATE_INFO_URL = "gate_info_url";

	/** Long<br>
	 *
	 */
	public static final String GATE_LAUNCH_EXTERNAL = "gate_launch_external";

	/** Long<br>
	 *
	 */
	public static final String GATE_PROGRESS = "gate_progress";

	/** byte[]<br>
	 *
	 */
	public static final String GATE_STRING = "gate_string";

	/** byte[]<br>
	 *
	 */
	public static final String GATE_URL = "gate_url";

	/** byte[]<br>
	 *
	 */
	public static final String HASHED = "hashed";

	/** Long<br>
	 * [GENERAL/TRANSFER TAB] Wasted
	 * <br>
	 * {@link #HASHFAILS} appears to be displayed instead of {@link #CORRUPT}
	 */
	public static final String HASHFAILS = "hashfails";

	/** byte[pieces in bits]<br>
	 * [iNTERNAL] A bit array containing indicating which pieces you currently
	 * have completed, each bit representing one piece (padded up to the next
	 * closest byte)
	 * <p/>
	 * bit 0 of byte 0 is 1st piece<br>
	 * bit 0 of byte 1 is 9th piece<br>
	 */
	public static final String HAVE = "have";

	/** Long<br>
	 *
	 */
	public static final String HELPER_INTEGRATION_CHANGED = "helper_integration_changed";

	/** list<br>
	 *
	 */
	public static final String INFECTED = "infected";

	/** byte[]<br>
	 * [GENERAL/TRANSFER TAB] Hash
	 */
	public static final String INFO = "info";

	/** Long<br>
	 *
	 */
	public static final String IS_UPDATE_AVAILABLE = "is_update_available";

	/** Long<br>
	 *
	 */
	public static final String IS_UPDATE_TORRENT = "is_update_torrent";

	/** byte[pieces in bits]<br>
	 *
	 * <i>Tux Note:</i> This looks important.  Appears to be a copy of "have",
	 * but unsure when or why it would differ.
	 */
	public static final String KNOWN = "known";

	/** String<br>
	 * [CATEGORY/LABEL LIST] The label the torrent job uses
	 */
	public static final String LABEL = "label";

	/** List of String<br>
	 * All labels assinged to torrent, including the on in "label"
	 */
	public static final String LABELS = "labels";

	/** Long<br>
	 * Last time client was seeding complete file.  In Epoch/Unix time.
	 */
	public static final String LAST_SEEN_COMPLETE = "last seen complete";

	/** Long<br>
	 * [TORRENT JOBS LIST] Last Active (in seconds)
	 * <p/>
	 * Last Time the file was being actively seeded or shared from client PC.
	 */
	public static final String LAST_ACTIVE = "last_active";

	/** Long<br>
	 * [TORRENT PROPERTES] Local Peer Discovery
	 */
	public static final String LSD = "lsd";

	/** Long<br>
	 *
	 */
	public static final String MAX_CONNECTIONS = "max_connections";

	/**
	 * List of String<br>
	 * 1 entry per file<br>
	 * Ex: pcm
	 */
	public static final String META_AUDIO_CODEC = "meta_audio_codec";

	/** byte[filecount]<br>
	 */
	public static final String META_AVAILABLE = "meta_available";

	/** byte[filecount*2]<br>
	 *
	 */
	public static final String META_CONTAINERS = "meta_containers";

	/** byte[filecount*4]<br>
	 *
	 */
	public static final String META_DURATIONS = "meta_durations";

	/** byte[filecount*4]<br>
	 *
	 */
	public static final String META_ENCODING_RATES = "meta_encoding_rates";

	/** List of String<br>
	 * 1 entry per file<br>
	 * Ex: DIVX, MP42
	 */
	public static final String META_VIDEO_CODEC = "meta_video_codec";

	/** byte[filecount*2]<br>
	 *
	 */
	public static final String META_VIDEO_HEIGHTS = "meta_video_heights";

	/** byte[filecount*2]<br>
	 *
	 */
	public static final String META_VIDEO_WIDTHS = "meta_video_widths";

	/** byte[filecount]<br>
	 *
	 */
	public static final String META_VLC_SUPPORTED = "meta_vlc_supported";

	/** List of [Long]<br>
	 * 1 entry per file<br>
	 */
	public static final String MODTIMES = "modtimes";

	/** Long<br>
	 * [iNTERNAL] 0 if the torrent contents were not moved after completion, 1 if they were
	 */
	public static final String MOVED = "moved";

	/** byte[]<br>
	 *
	 */
	public static final String NEXT_GATE = "next_gate";

	/** List of [Map]<br>
	 *
	 */
	public static final String OBSERVERS = "observers";

	/** Long<br>
	 * [TORRENT JOBS LIST] # (-1 means the torrent job has finished downloading)
	 */
	public static final String ORDER = "order";

	/** Long<br>
	 *
	 */
	public static final String OUTOFSPACE = "outofspace";

	/** Long<br>
	 * [TORRENT PROPERTES] General -> Seeding Goal -> Override default settings
	 */
	public static final String OVERRIDE_SEEDSETTINGS = "override_seedsettings";

	/** byte[20]<br>
	 *
	 */
	public static final String PARENT_INFO = "parent_info";

	/** String<br>
	 * [GENERAL/TRANSFER TAB] Save As
	 * <p/>
	 * For non-Simple Torrents: Path on the Local Machine where Incoming Files are saved (the Folder etc.)<br/>
	 * For Simple Torrents: Full path to data file. {@link #ROOTDIR} will have path without filename.
	 */
	public static final String PATH = "path";

	/** byte[]<br>
	 * [iNTERNAL] Peer cache, with peers stored in IPv6
	 * <p/>
	 * Other Peers sharing this particular file at the time of exiting (the updating of the resume.dat file).  User must convert from HEX to decimal to get IP Value.
	 */
	public static final String PEERS6 = "peers6";

	/** byte[filecount]<br>
	 * [iNTERNAL] A byte mask containing indicating which files have what
	 * priority, each byte representing one file. The order of the bytes is the
	 * same as the order found in the .torrent file
	 * <p/>
	 * 0x00: Skip<br>
	 * 0x01-0x07: Granular Low<br/>
	 * 0x04: Non-Granular Low<br>
	 * 0x08: Normal<br>
	 * 0x09-0x0F: Granular Low<br/>
	 * 0x0C: Non-Granular High<br>
	 * 0x80: Also Skip<br/>
	 * <p/>
	 * TODO: Determine the difference between 0x00 and 0x00.  
	 * Doesn't appear to be related to partfile or file existance or piece sharing
	 */
	public static final String PRIO = "prio";

	/** Long<br>
	 * maybe bool?
	 */
	public static final String PRIO2 = "prio2";

	/** Long<br>
	 *
	 */
	public static final String PUBLISHED_ON = "published_on";

	/** Long<br>
	 * [iNTERNAL] The quality of the video file (obtained from RSS feed)
	 */
	public static final String QUALITY = "quality";

	/** list<br>
	 *
	 */
	public static final String QUARANTINED = "quarantined";

	/** List of [Long]<br>
	 * 1 entry per file<br>
	 */
	public static final String READ_ONLY = "read_only";

	/** Long<br>
	 *
	 */
	public static final String RELATIVE = "relative";

	/** String<br>
	 * Present for Simple Torrents.  Path file will be saved to, excluding filename
	 */
	public static final String ROOTDIR = "rootdir";

	/** byte[]<br>
	 * [iNTERNAL] The original name of the torrent as shown in the RSS feed
	 */
	public static final String RSS_NAME = "rss_name";

	/** String<br>
	 * Run this program when the download finishes<br>
	 * <p/>
	 * %F - Name of download (for single file torrents)<br>
	 * %D - Directory where files are saved<br>
	 * %N - Title of Torrent<br>
	 * %S - State of torrent (started=1, checking=2, start-after-check=4, checked=8, error=16, paused=32, paused=32, auto=64, loaded=128<br>
	 * %L - Label<br>
	 * %T - Tracker<br>
	 * %M - Status message string (status column)<br>
	 * %I - hex encoded info-hash<br>
	 * %K - kind of torrent (single|multi)<br>
	 */
	public static final String RUN_PROGRAM = "run_program";

	/** Long<br>
	 * [GENERAL/TRANSFER TAB] Time Elapsed (in seconds)
	 * <p/>
	 * Time File has been downloading in client (or total being seeded following downloading)
	 */
	public static final String RUNTIME = "runtime";

	/** Long<br>
	 * [TORRENT JOBS LIST] Episode (obtained from RSS feed)
	 */
	public static final String SEASON = "season";

	/** Long<br>
	 * [iNTERNAL] The total amount of time the torrent job has been in seeding mode (in seconds)
	 * <p/>
	 * Time File has been seeded by client in seconds
	 */
	public static final String SEEDTIME = "seedtime";

	/** Long<br>
	 *
	 */
	public static final String SHARE_MODE = "share_mode";

	/** Long<br>
	 *
	 */
	public static final String SID = "sid";

	/** Long<br>
	 * [TORRENT JOBS LIST] Status
	 * <p/>
	 * File status when resume.dat was last written:<br>
	 * “0”= Stopped<br>
	 *
	 * “1”=Force Started<br>
	 *
	 * “2”=Started<br>
	 *
	 * “3”=Running/not downloading<br>
	 * “4”=Error<br>
	 */
	public static final String STARTED = "started";

	/** Long<br>
	 *
	 */
	public static final String STATS_ALL_PEERS = "stats_all_peers";

	/** Long<br>
	 *
	 */
	public static final String STATS_ALL_SLOTS = "stats_all_slots";

	/** Long<br>
	 *
	 */
	public static final String STATS_WALLET_PEERS = "stats_wallet_peers";

	/** Long<br>
	 *
	 */
	public static final String STATS_WALLET_SLOTS = "stats_wallet_slots";

	/** byte[]<br>
	 * bit per file.<br/>
	 * bit is 0 : No suffix<br/>
	 * bit is 1 : Suffix (.!ut) appended to filename
	 */
	public static final String SUFFIXES = "suffixes";

	/** Long<br>
	 * [TORRENT PROPERTIES] Initial Seeding
	 */
	public static final String SUPERSEED = "superseed";

	/** Long<br>
	 * [iNTERNAL] The marker indicating the last piece being transferred for
	 * Initial Seeding mode
	 */
	public static final String SUPERSEED_CUR_PIECE = "superseed_cur_piece";

	/** List of [List of [Long, String]]<br>
	 * <pre>
	 * "targets"
	 *     [0] ITEM 1
	 *         [0] File Index
	 *         [1] File Path, either absolute or relative to {@link #PATH}
	 *     [X] ITEM X
	 *         [0] File Index
	 *         [1] File Path
	 * </pre>
	 */
	public static final String TARGETS = "targets";

	/** Long<br>
	 * Last Time torrent was Listed in BitTorrent Client (any status).
	 * All listed Torrent in “resume.dat” appear to have the same time (Epoch/Unix).  (Last Shutdown time of Client?)
	 */
	public static final String TIME = "time";

	/** Long<br>
	 *
	 */
	public static final String TORRENTS_ADDED_WHILE_DOWNLOADING = "torrents_added_while_downloading";

	/** Long<br>
	 *
	 */
	public static final String TRACKERMODE = "trackermode";

	/** List of Trackers.  Value can be<br>
	 * String: Tracker URL<br>
	 * List of String: Tracker group<BR>
	 */
	public static final String TRACKERS = "trackers";

	/** LightHashMap<br>
	 *
	 */
	public static final String TRANSCODE = "transcode";

	/** Long<br>
	 *
	 */
	public static final String TT_1PCT = "tt_1pct";

	/** Long<br>
	 *
	 */
	public static final String TT_20PCT = "tt_20pct";

	/** Long<br>
	 *
	 */
	public static final String TT_5PCT = "tt_5pct";

	/** Long<br>
	 *
	 */
	public static final String TTFB = "ttfb";

	/** Long<br>
	 * [TORRENT PROPERTES] Number of upload slots
	 */
	public static final String ULSLOTS = "ulslots";

	/** Long<br>
	 *
	 */
	public static final String UNCHOKE_ROUNDS = "unchoke_rounds";

	/** Long<br>
	 *
	 */
	public static final String UPDATE_FREQ = "update_freq";

	/** byte[]<br>
	 *
	 */
	public static final String UPDATE_URL = "update_url";

	/** Long<br>
	 * [TORRENT JOBS LIST] Uploaded (in bytes)
	 */
	public static final String UPLOADED = "uploaded";

	/** Long<br>
	 *
	 */
	public static final String UPLOADED_WITH_HELPER = "uploaded_with_helper";

	/** Long<br>
	 * [TORRENT PROPERTES] Maximum upload rate (in bytes/second)
	 */
	public static final String UPSPEED = "upspeed";

	/** Long<br>
	 *
	 */
	public static final String USE_UTP = "use_utp";

	/** Long<br>
	 *
	 */
	public static final String USE_UTP_ONLY = "use_utp_only";

	/** Long<br>
	 *
	 */
	public static final String VALID = "valid";

	/** Long<br>
	 *
	 */
	public static final String VISIBLE = "visible";

	/** Long<br>
	 *
	 */
	public static final String VOTE = "vote";

	/** List of [Long]<br>
	 *
	 */
	public static final String VOTES = "votes";

	/** Long<br>
	 * [TORRENT PROPERTES] General -> Seeding Goal -> Minimum Ratio (in 1/10 of a percent)<br>
	 * Only applied when {@link #OVERRIDE_SEEDSETTINGS} is 1
	 */
	public static final String WANTED_RATIO = "wanted_ratio";

	/** Long<br>
	 * [TORRENT PROPERTES] General -> Seeding Goal -> Minimum number of available seeeds<br>
	 * Only applied when {@link #OVERRIDE_SEEDSETTINGS} is 1
	 */
	public static final String WANTED_SEEDNUM = "wanted_seednum";

	/** Long<br>
	 * [TORRENT PROPERTES] General -> Seeding Goal -> Minimum Seeding Time (in seconds)<br>
 	 * Only applied when {@link #OVERRIDE_SEEDSETTINGS} is 1
	 */
	public static final String WANTED_SEEDTIME = "wanted_seedtime";

	/** Long<br>
	 *
	 */
	public static final String WASFORCE = "wasforce";

	/** Long<br>
	 * [GENERAL/TRANSFER TAB] Wasted (in bytes)
	 */
	public static final String WASTE = "waste";

	public static final String WEBSEEDS = "webseeds";

	/** Long<br>
	 *
	 */
	public static final String WEB_SEEDING_ENABLED = "web_seeding_enabled";

}
