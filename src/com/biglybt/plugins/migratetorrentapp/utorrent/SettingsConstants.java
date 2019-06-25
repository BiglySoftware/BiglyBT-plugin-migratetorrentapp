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
 *  Descriptions from:<br>
 *  https://robertpearsonblog.wordpress.com/2016/11/10/utorrent-forensic-artifacts/ <br>
 *  https://articles.forensicfocus.com/2018/11/02/forensic-analysis-of-the-%CE%BCtorrent-peer-to-peer-client-in-windows/ <br>
 *  https://www.sb-innovation.de/showthread.php?29925-How-to-edit-your-uTorrent-statistics <br>
 *  uTorrent User Manual<br>
 */
public interface SettingsConstants
{
	/** byte[]<br>
	 *
	 **/
//	String _FILEGUARD = ".fileguard";

	/** List of String<br>
	 *
	 * History of paths.  It appears each dir is stored twice, once with trailing sep and once without
	 **/
	String ADD_DIALOG_HIST = "add_dialog_hist";

	/** byte[]<br>
	 * UNKNOWN
	 **/
//	String ADDPRELOC = "addpreloc";

	/** byte[]<br>
	 * UNKNOWN
	 **/
//	String ADDPREWND = "addprewnd";

	/** Number<br/>
	 * UNKNOWN
	 **/
//	String ADDPREWNDEXPANDED = "addprewndexpanded";

	/** Number<br/>
	 * UNKNOWN
	 **/
//	String ASSZ = "assz";

	/** Number<br/>
	 *
	 * UNKNOWN
	 **/
//	String ATTEMPTED_TO_RECEIVE_SERVER_SEARCH_URL = "attempted_to_receive_server_search_url";

	interface General
	{

		/** Flag<br>
		 * Preferences->General->Windows Integration->Start uTorrent when Windows Starts<br>
		 **/
		String AUTOSTART = "autostart";

		boolean AUTOSTART_DEF = true;

		/** Flag<br>
		 * Preferences->General->Windows Integration->Start Minimized<br>
		 **/
		String START_MINIMIZED = "start_minimized";

		boolean START_MINIMIZED_DEF = true;

		/** Flag<br>
		 * Preferences->General->When Downloading->Append .!ut to incomplete files<br>
		 */
		String APPEND_INCOMPLETE = "append_incomplete";

		boolean APPEND_INCOMPLETEDEF = false;

		/** Flag<br>
		 * Preferences->General->When Downloading->Pre-allocate all files<br>
		 */
		String PREALLOC_SPACE = "prealloc_space";

		boolean PREALLOC_SPACE_DEF = false;
	}

	interface UI_Settings
	{
		/**
		 * Flag<br>
		 * Preferences->UI Settings->Display Options->Confirm when deleting torrents<br>
		 */
		String CONFIRM_WHEN_DELETING = "confirm_when_deleting";

		boolean CONFIRM_WHEN_DELETING_DEF = true;

		/** Flag<br>
		 * Preferences->UI Settings->Display Options->Confirm when deleting trackers<br>
		 */
		String CONFIRM_REMOVE_TRACKER = "confirm_remove_tracker";

		boolean CONFIRM_REMOVE_TRACKER_DEF = false;

		/** Flag<br>
		 * Preferences->UI Settings->Display Options->Show confirmation dialog on exit<br>
		 **/
		String CONFIRM_EXIT = "confirm_exit";

		boolean CONFIRM_EXIT_DEF = true;

		/** Flag<br>
		 * Preferences->UI Settings->Display Options->Confirm exit if critical seeder<br>
		 **/
		String CONFIRM_EXIT_CRITICAL_SEEDER = "confirm_exit_critical_seeder";

		boolean CONFIRM_EXIT_CRITICAL_SEEDER_DEF = true;

		/** Flag<br>
		 * Preferences->UI Settings->Display Options->Alternate list background color<br>
		 **/
		String GUI_ALTERNATE_COLOR = "gui.alternate_color";

		boolean GUI_ALTERNATE_COLOR_DEF = false;

		/** Flag<br>
		 * Preferences->UI Settings->Display Options->Show current speed in the title bar<br>
		 **/
		String GUI_SPEED_IN_TITLE = "gui.speed_in_title";

		boolean GUI_SPEED_IN_TITLE_DEF = false;

		/** Flag<br>
		 * Preferences->UI Settings->Display Options->Show speed limits in the status bar<br>
		 **/
		String GUI_LIMITS_IN_STATUSBAR = "gui.limits_in_statusbar";

		boolean GUI_LIMITS_IN_STATUSBAR_DEF = false;

		/** Flag<br>
		 * Preferences->UI Settings->Display Options->Use fine grained file priorities<br>
		 **/
		String GUI_GRANULAR_PRIORITY = "gui.granular_priority";

		boolean GUI_GRANULAR_PRIORITY_DEF = false;

		/** Flag<br>
		 * Preferences->UI Settings->System Tray->Minimize button minimized UT to tray<br>
		 **/
		String MINIMIZE_TO_TRAY = "minimize_to_tray";

		boolean MINIMIZE_TO_TRAY_DEF = false;

		/** Flag<br>
		 * Preferences->UI Settings->System Tray->Always show tray icon<br>
		 **/
		String TRAY_SHOW = "tray.show";

		boolean TRAY_SHOW_DEF = true;

		/** Flag<br>
		 * Preferences->UI Settings->System Tray->Close button closes uT to tray<br>
		 **/
		String CLOSE_TO_TRAY = "close_to_tray";

		boolean CLOSE_TO_TRAY_DEF = true;

		/** Flag<br>
		 * Preferences->UI Settings->System Tray->single click on tray icon to open<br>
		 **/
		String TRAY_SINGLE_CLICK = "tray.single_click";

		boolean TRAY_SINGLE_CLICK_DEF = false;

		/** Flag<br>
		 * Preferences->UI Settings->System Tray->Show balloon notifications in tray<br>
		 **/
		String NOTIFY_COMPLETE = "notify_complete";

		boolean NOTIFY_COMPLETE_DEF = true;

		/** Flag<br>
		 * Preferences->UI Settings->System Tray->Always activate when clicked<br>
		 **/
		String TRAY_ACTIVATE = "tray_activate";

		boolean TRAY_ACTIVATE_DEF = true;

		/** Flag<br>
		 * Preferences->UI Settings->When Adding Torrents->Don't start the download automatically<br>
		 **/
		String TORRENTS_START_STOPPED = "torrents_start_stopped";

		boolean TORRENTS_START_STOPPED_DEF = false;

		/** Flag<br>
		 * Preferences->UI Settings->When Adding Torrents->Activate the program window<br>
		 **/
		String ACTIVATE_ON_FILE = "activate_on_file";

		boolean ACTIVATE_ON_FILE_DEF = true;

		/** Flag<br>
		 * Preferences->UI Settings->When Adding Torrents->Show optons to change the name and location of the torrent data<br>
		 **/
		String SHOW_ADD_DIALOG = "show_add_dialog";

		boolean SHOW_ADD_DIALOG_DEF = true;
	}

	interface Connection
	{

		/** Number<br/>
		 * The networking port this client is running on.  Varies on each install.<br>
		 * Connection->Listening Port->Port used for incoming connections
		 **/
		String BIND_PORT = "bind_port";

		/** Flag
		 * Preferences->Connection->Listening Port->Enable UPnP port mapping
		 */
		String UPNP = "upnp";

		boolean UPNP_DEF = true;

		/** Flag
		 * Preferences->Connection->Listening Port->Randomize port each start
		 */
		String RAND_PORT_ON_START = "rand_port_on_start";

		boolean RAND_PORT_ON_START_DEF = false;

		/** Flag
		 * Preferences->Connection->Listening Port->Enable NAT-PNP port mapping
		 */
		String NATPMP = "natpmp";

		boolean NATPMP_DEF = true;

		/** Flag
		 * Preferences->Connection->Listening Port->Add Windows Firewall exception
		 */
		String DISABLE_FW = "disable_fw";

		boolean DISABLE_FW_DEF = true;

		////

		/** Number<br/>
		 * Preferences->Connection->Proxy Server->Type<br/>
		 * <br/>
		 * 0 : None<br/>
		 * 1 : Socks4<br/>
		 * 2 : Socks5<br/>
		 * 3 : HTTPS<br/>
		 * 4 : HTTP<br/>
		 */
		String PROXY_TYPE = "proxy.type";

		int PROXY_TYPE_DEF = 0;

		/** String<br/>
		 * Preferences->Connection->Proxy Server->Proxy<br/>
		 */
		String PROXY_PROXY = "proxy.proxy";

		/** Number<br/>
		 * Preferences->Connection->Proxy Server->Port<br/>
		 */
		String PROXY_PORT = "proxy.port";

		int PROXY_PORT_DEF = 8080;

		/** Flag<br/>
		 * Preferences->Connection->Proxy Server->Authentication<br/>
		 */
		String PROXY_AUTH = "proxy.auth";

		boolean PROXY_AUTH_DEF = false;

		/** String<br/>
		 * Preferences->Connection->Proxy Server->Username<br/>
		 */
		String PROXY_USERNAME = "proxy.username";

		/** String<br/>
		 * Preferences->Connection->Proxy Server->pw<br/>
		 */
		String PROXY_PASSWORD = "proxy.password";

		/** Flag<br/>
		 * Preferences->Connection->Proxy Server->Use proxy for peer-to-peer connections<br/>
		 */
		String PROXY_P2P = "proxy.p2p";

		boolean PROXY_P2P_DEF = false;

		/** Flag<br/>
		 * Preferences->Connection->Proxy Server->Use proxy for hostname lookups<br/>
		 */
		String PROXY_RESOLVE = "proxy.resolve";

		boolean PROXY_RESOLVE_DEF = false;

		/** Flag<br/>
		 * Preferences->Connection->Proxy Privacy->Disable all local DNS lookups<br/>
		 */
		String NO_LOCAL_DNS = "no_local_dns";

		boolean NO_LOCAL_DNS_DEF = false;
	}

	public interface Bandwidth
	{
		/** Number<br/>
		 * Preferences->Bandwidth->Max upload rate (kB/s)<br/>
		 */
		String MAX_UL_RATE = "max_ul_rate";

		int MAX_UL_RATE_DEF = 0; // unlimited

		/** Number<br/>
		 * Preferences->Bandwidth->Alternate upload rate when not downloading (kB/s)<br/>
		 * <br/>
		 * {@link #MAX_UL_RATE_SEED_FLAG} must be enabled
		 */
		String MAX_UL_RATE_SEED = "max_ul_rate_seed";

		int MAX_UL_RATE_SEED_DEF = 0; // unlimited

		/** Flag<br/>
		 * Preferences->Bandwidth->Alternate upload rate when not downloading<br/>
		 */
		String MAX_UL_RATE_SEED_FLAG = "max_ul_rate_seed_flag";

		boolean MAX_UL_RATE_SEED_FLAG_DEF = false;

		/** Number<br/>
		 * Preferences->Bandwidth->Max download rate (kB/s)<br/>
		 */
		String MAX_DL_RATE = "max_dl_rate";

		int MAX_DL_RATE_DEF = 0; // unlimited

		/** Flag<br/>
		 * Preferences->Bandwidth->Global Rate Limit Options->Apply rate limit to transport overhead<br/>
		 */
		String NET_CALC_OVERHEAD = "net.calc_overhead";

		boolean NET_CALC_OVERHEAD_DEF = false;

		/** Flag<br/>
		 * Preferences->Bandwidth->Global Rate Limit Options->Apply rate limit to uTP connections<br/>
		 */
		String NET_RATELIMIT_UTP = "net.ratelimit_utp";

		boolean NET_RATELIMIT_UTP_DEF = true;

		/** Flag<br/>
		 * Preferences->Bandwidth->Global Rate Limit Options->Stop transfers on user interaction<br/>
		 */
		String SCHED_INTERACTION = "sched_interaction";

		boolean SCHED_INTERACTION_DEF = false;

		/** Number<br/>
		 * Preferences->Bandwidth->Global maximiumum number of Connections [200]<br/>
		 */
		String CONNS_GLOBALLY = "conns_globally";

		int CONNS_GLOBALLY_DEF = 200;

		/** Number<br/>
		 * Preferences->Bandwidth->Max number of connected peers per torrent [50]<br/>
		 */
		String CONNS_PER_TORRENT = "conns_per_torrent";

		int CONNS_PER_TORRENT_DEF = 50;

		/** Number<br/>
		 * Preferences->Bandwidth->Number of upload slots per torrent [4]<br/>
		 */
		String UL_SLOTS_PER_TORRENT = "ul_slots_per_torrent";

		int UL_SLOTS_PER_TORRENT_DEF = 4;

		/** Flag<br/>
		 * Preferences->Bandwidth->Number of Connections->Use additional upload slots if upload speed < 90%<br/>
		 */
		String EXTRA_ULSLOTS = "extra_ulslots";

		boolean EXTRA_ULSLOTS_DEF = true;
	}

	interface BitTorrent
	{
		/** Flag<br/>
		 * Preferences->BitTorrent->Enable DHT Network
		 */
		String DHT = "dht";

		boolean DHT_DEF = true;

		/** Flag<br/>
		 * Preferences->BitTorrent->Enable DHT for new torrents
		 */
		String DHT_PER_TORRENT = "dht_per_torrent";

		boolean DHT_PER_TORRENT_DEF = true;

		/** Flag<br/>
		 * Preferences->BitTorrent->Enable Local Peer Discovery
		 */
		String LPD = "lsd";

		boolean LPD_DEF = true;

		/** Flag<br/>
		 * Preferences->BitTorrent->Enable bandwidth management [uTP]<br/>
		 * <br/>
		 * Set via {@link adv#BT_TRANSP_DISPOSITION}, (0x8 | 0x2)
		 */

		/** Flag<br/>
		 * Preferences->BitTorrent->Enable UDP tracker support<br/>
		 */
		String USE_UDP_TRACKERS = "use_udp_trackers";

		boolean USE_UDP_TRACKERS_DEF = true;

		/** String<br/>
		 * Preferences->BitTorrent->IP/Hostname to report to tracker<br/>
		 */
		String TRACKER_IP = "tracker_ip";

		/** Flag<br/>
		 * Preferences->BitTorrent->Ask tracker for scrape information<br/>
		 */
		String ENABLE_SCRAPE = "enable_scrape";

		boolean ENABLE_SCRAPE_DEF = true;

		/** Flag<br/>
		 * Preferences->BitTorrent->Enable Peer Exchange<br/>
		 */
		String PEX = "pex";

		boolean PEX_DEF = true;

		/** Flag<br/>
		 * Preferences->BitTorrent->Limit local peer bandwidth<br/>
		 * Disabled means local peers are unlimited bandwidth usage
		 */
		String RATE_LIMIT_LOCAL_PEERS = "rate_limit_local_peers";

		boolean RATE_LIMIT_LOCAL_PEERS_DEF = false;

		/** Flag<br/>
		 * Preferences->BitTorrent->Enable Altruistic Mode<br/>
		 */
		String ENABLE_ALTRUISTIC = "enable_share";

		boolean ENABLE_ALTRUISTIC_DEF = false;

		/** Number<br/>
		 * Preferences->BitTorrent->Protocol Encryption->Outgoing<br/>
		 * <br/>
		 * 0 (Missing): Disabled<br/>
		 * 1 : Enabled<br/>
		 * 2 : Forced
		 */
		String ENCRYPTION_MODE = "encryption_mode";

		int ENCRYPTION_MODE_DEF = 0;

		/** Flag<br/>
		 * Preferences->BitTorrent->Protocol Encryption->Allow incoming legacy connections<br/>
		 */
		String ENCRYPTION_ALLOW_LEGACY = "encryption_allow_legacy";

		boolean ENCRYPTION_ALLOW_LEGACY_DEF = true;
	}

	interface TransferCap
	{
		/** Flag<br/>
		 * Preferences->Transfer Cap->Enable Transfer Cap<br/>
		 */
		String MULTI_DAY_TRANSFER_LIMIT_EN = "multi_day_transfer_limit_en";

		boolean MULTI_DAY_TRANSFER_LIMIT_EN_DEF = false;

		/** Number<br/>
		 * Preferences->Transfer Cap->Transfer Cap<br/>
		 */
		String MULTI_DAY_TRANSFER_LIMIT_VALUE = "multi_day_transfer_limit_value";

		int MULTI_DAY_TRANSFER_LIMIT_VALUE_DEF = 200;

		/** Flag<br/>
		 * Preferences->Transfer Cap->Transfer Cap (units)<br/>
		 * 0 : MB<br/>
		 * 1 : GB<br/>
		 */
		String MULTI_DAY_TRANSFER_LIMIT_UNIT = "multi_day_transfer_limit_unit";

		int MULTI_DAY_TRANSFER_LIMIT_UNIT_DEF = 1;

		/** Flag<br/>
		 * Preferences->Transfer Cap->Time Period<br/>
		 * 0 : 1 Day<br/>
		 * 1 : 2 Days<br/>
		 * 2 : 5 Days<br/>
		 * 3 : 7 Days<br/>
		 * 4 : 10 Days<br/>
		 * 5 : 14 Days<br/>
		 * 6 : 15 Days<br/>
		 * 7 : 20 Days<br/>
		 * 8 : 21 Days<br/>
		 * 9 : 28 Days<br/>
		 * 10 : 30 Days<br/>
		 * 11 : 31 Days<br/>
		 */
		String MULTI_DAY_TRANSFER_LIMIT_SPAN = "multi_day_transfer_limit_span";

		int MULTI_DAY_TRANSFER_LIMIT_SPAN_DEF = 11;

		/** Flag<br/>
		 * Preferences->Transfer Cap->Limit Type->Uploads<br/>
		 * <br/>
		 * Only one of {@link #MULTI_DAY_TRANSFER_MODE_UL},
		 * {@link #MULTI_DAY_TRANSFER_MODE_DL},
		 * {@link #MULTI_DAY_TRANSFER_MODE_ULDL} is set to true (1)
		 */
		String MULTI_DAY_TRANSFER_MODE_UL = "multi_day_transfer_mode_ul";

		boolean MULTI_DAY_TRANSFER_MODE_UL_DEF = false;

		/** Flag<br/>
		 * Preferences->Transfer Cap->Limit Type->Downloads<br/>
		 * <br/>
		 * Only one of {@link #MULTI_DAY_TRANSFER_MODE_UL},
		 * {@link #MULTI_DAY_TRANSFER_MODE_DL},
		 * {@link #MULTI_DAY_TRANSFER_MODE_ULDL} is set to true (1)
		 */
		String MULTI_DAY_TRANSFER_MODE_DL = "multi_day_transfer_mode_dl";

		boolean MULTI_DAY_TRANSFER_MODE_DL_DEF = false;

		/** Flag<br/>
		 * Preferences->Transfer Cap->Limit Type->Uploads + Downloads<br/>
		 * <br/>
		 * Only one of {@link #MULTI_DAY_TRANSFER_MODE_UL},
		 * {@link #MULTI_DAY_TRANSFER_MODE_DL},
		 * {@link #MULTI_DAY_TRANSFER_MODE_ULDL} is set to true (1)
		 */
		String MULTI_DAY_TRANSFER_MODE_ULDL = "multi_day_transfer_mode_uldl";

		boolean MULTI_DAY_TRANSFER_MODE_ULDL_DEF = true;
	}

	interface Queueing
	{
		/** Number<br/>
		 * Preferences->Queueing->Queue Settings->Max # of active torrents (UL or DL) [8]<br/>
		 */
		String MAX_ACTIVE_TORRENT = "max_active_torrent";

		int MAX_ACTIVE_TORRENT_DEF = 8;

		/** Number<br/>
		 * Preferences->Queueing->Queue Settings->Max # of active downoads [5]<br/>
		 */
		String MAX_ACTIVE_DOWNLOADS = "max_active_downloads";

		int MAX_ACTIVE_DOWNLOADS_DEF = 5;

		/** Number<br/>
		 * Preferences->Queueing->Seeding Goals->Min ratio (%) [200]<br/>
		 */
		String SEED_RATIO_X10 = "seed_ratio";

		int SEED_RATIO_DEF = 2000;

		/** Number<br/>
		 * Preferences->Queueing->Seeding Goals->Min seeting time (minutes) [0]<br/>
		 */
		String SEED_TIME_SECS = "seed_time";

		int SEED_TIME_SECS_DEF = 0;

		/** Number<br/>
		 * Preferences->Queueing->Seeding Goals->Min number of available seeds [0]<br/>
		 */
		String SEED_NUM = "seed_num";

		int SEED_NUM_DEF = 0;

		/** Number<br/>
		 * Preferences->Queueing->Seeding Goals->Seeding tasks have higher priority than downloading tasks<br/>
		 */
		String SEEDS_PRIORITIZED = "seeds_prioritized";

		boolean SEEDS_PRIORITIZED_DEF = false;

		/** Flag<br/>
		 * Preferences->Queueing->When uT reaches the Seeting Goal->Limit the upload rate to (kB/s):<br/>
		 */
		String SEED_PRIO_LIMITUL_FLAG = "seed_prio_limitul_flag";

		boolean SEED_PRIO_LIMITUL_FLAG_DEF = false;

		/** Number<br/>
		 * Preferences->Queueing->When uT reaches the Seeting Goal->Limit the upload rate to (kB/s): [0 = stop] [4]<br/>
		 */
		String SEED_PRIO_LIMITUL = "seed_prio_limitul";

		int SEED_PRIO_LIMITUL_DEF = 0;
	}
	
	interface advRunProgram {
		String FINISH_CMD = "finish_cmd";
	
		String STATE_CMD = "state_cmd";
	}

	/** Number<br/>
	 * The number indicates the time and date the client program was installed on.
	 * This time is in Lightweight Directory Access Protocol (LDAP) time representing the number of “100-nanosecond intervals since January 1, 1601 UTC”.
	 * To be converted the user must add seven additional 0’s to create the 18 digit time representation.
	 * <br>
	 **/
	String BORN_ON = "born_on";

	/** Number<br/>
	 *
	 * UNKNOWN
	 **/
//	String BORN_ON_REMOTE = "born_on_remote";

	/** Number<br/>
	 *
	 * UNKNOWN
	 **/
//	String CFU_SEQ = "cfu_seq";

	/** String<br>
	 * Preferences->General->Windows Integration->Check association on startup
	 * <BR>
	 *   0 : off<br>
	 *   Missing Key : On<br>
	 */
	String CHECK_ASSOC_ON_START = "check_assoc_on_start";

	/** Number<br/>
	 **/
//	String CHECK_UPDATE_BETA = "check_update_beta";

	/** Number<br/>
	 *
	 **/
//	String COLD_ON = "cold_on";

	/** list<br>
	 * Number of .torrent files created by this client (within brackets),
	 * includes path and name of files/folders that the user used to create the .torrent file;
	 * good indicator of knowledge and intent;
	 * may point to external media or other storage drive/directory locations
	 **/
	String CT_HIST = "ct_hist";

	/** byte[31 * 8]<br>
	 * Downloaded Today = daily_download_hist (binary array; first of the 31 64-bit integers in reverse byte order)
	 * <br>
	 *   8 bytes per day, LE<br>
	 **/
	String DAILY_DOWNLOAD_HIST = "daily_download_hist";

	/** byte[31 * 8]<br>
	 **/
	String DAILY_LOCAL_DOWNLOAD_HIST = "daily_local_download_hist";

	/** byte[31 * 8]<br>
	 *   8 bytes per day, LE<br>
	 **/
	String DAILY_LOCAL_UPLOAD_HIST = "daily_local_upload_hist";

	/** byte[31 * 8]<br>
	 * Uploaded Today = daily_upload_hist (binary array; first of the 31 64-bit integers in reverse byte order)
	 * <br>
	 *   8 bytes per day, LE<br>
	 **/
	String DAILY_UPLOAD_HIST = "daily_upload_hist";

	/** Number<br/>
	 *
	 **/
	String DEFAULT_TORRENT_HANDLER = "default_torrent_handler";

	interface devices
	{
		/** Map<br>
		 * Paired devices will be listed here with device name, USB VID&PID and serial number
		 **/
		String DEVICES = "devices";

		/** list<br>
		 *
		 **/
		String DEVICES_DEVICES = "devices_devices";

		/** Long
		 *
		 */
		String DEVICES_DEVICES_AUTO_TRANSFER = "auto_transfer";

		/** String
		 *
		 */
		String DEVICES_DEVICES_USB_ID = "usb_id";

		/** Long
		 *
		 */
		String DEVICES_DEVICES_IS_DEFAULT = "is_default";

		/** String
		 *
		 */
		String DEVICES_DEVICES_NAME = "name";

		/** String
		 *
		 */
		String DEVICES_DEVICES_ID = "id";
	}

	/**
	 * Preferences->Directories
	 */
	interface Directories
	{
		/** Number<br/>
		 *   Preferences->Directories->Put new downloads in (checkbox)<br>
		 * 1 if {@link #DIR_ACTIVE_DOWNLOAD} is enabled<br>
		 * no key if {@link #DIR_ACTIVE_DOWNLOAD} disabled<br>
		 **/
		String DIR_ACTIVE_DOWNLOAD_FLAG = "dir_active_download_flag";

		/** String<br>
		 * Location set by user to save “new downloads” in.<br>
		 *   Preferences->Directories->Put new downloads in<br>
		 **/
		String DIR_ACTIVE_DOWNLOAD = "dir_active_download";

		/** Number<br/>
		 * Preferences->Directories->Location of Downloaded Files->Move completed downloads to (checkbox)<br>
		 * <br>
		 * 1 if {@link #DIR_COMPLETED_DOWNLOAD} is enabled<br>
		 * no key if {@link #DIR_COMPLETED_DOWNLOAD} disabled
		 **/
		String DIR_COMPLETED_DOWNLOAD_FLAG = "dir_completed_download_flag";

		/** Number<br/>
		 * Preferences->Directories->Location of Downloaded Files->Append the torrent's label<br>
		 *   <br>
		 * 1 if {@link #DIR_COMPLETED_DOWNLOAD_FLAG} is enabled<br>
		 * no key if {@link #DIR_COMPLETED_DOWNLOAD_FLAG} disabled<br>
		 **/
		String DIR_COMPLETED_ADD_LABEL = "dir_add_label";

		/** String<br>
		 * Preferences->Directories->Location of Downloaded Files->Move completed downloads to<br>
		 * <br>
		 * Location set by the user to store Completed downloads in.
		 **/
		String DIR_COMPLETED_DOWNLOAD = "dir_completed_download";

		/**
		 * Number<br/>
		 * Preferences->Directories->Location of Downloaded Files->Only move from the default download directory<br>
		 * <br>
		 * no key if {@link #DIR_COMPLETED_DOWNLOAD} is enabled
		 * 0 if {@link #DIR_COMPLETED_DOWNLOAD} disabled
		 */
		String DIR_COMPLETED_MOVE_IF_DEFDIR = "move_if_defdir";

		/** Number<br/>
		 * Preferences->Directories->Location of .torrents->Store .torrents in (checkbox)<br>
		 * <br>
		 * 1 if {@link #DIR_TORRENT_FILES} is enabled<br>
		 * no key if {@link #DIR_TORRENT_FILES} disabled.  Disabled stores .torrent in appdir<br>
		 **/
		String DIR_TORRENT_FILES_FLAG = "dir_torrent_files_flag";

		/** String<br>
		 * Preferences->Directories->Location of .torrents->Store .torrents<br>
		 * <br>
		 * Location set by user to store .torrent files.<br>
		 * If not set or not enabled, .torrent files are stored in %APPDATA%\\uTorrent\
		 **/
		String DIR_TORRENT_FILES = "dir_torrent_files";

		/** Number<br/>
		 *
		 * 1 if {@link #DIR_COMPLETED_TORRENTS} is enabled<br>
		 * no key if {@link #DIR_COMPLETED_TORRENTS} disabled<br>
		 **/
		String DIR_COMPLETED_TORRENTS_FLAG = "dir_completed_torrents_flag";

		/** String<br>
		 * Location set by the user to store completed .torrent files in. An archive essentially.
		 **/
		String DIR_COMPLETED_TORRENTS = "dir_completed_torrents";

		/** Number<br/>
		 * Preferences->Directories->Location of .torrents->Automatically load .torrents from (checkbox)<br>
		 *
		 * 1 if {@link #DIR_AUTOLOAD} is enabled
		 * no key if {@link #DIR_AUTOLOAD} disabled
		 **/
		String DIR_AUTOLOAD_FLAG = "dir_autoload_flag";

		/** String<br>
		 * Preferences->Directories->Location of .torrents->Automatically load .torrents from<br>
		 *   {@link #DIR_AUTOLOAD_FLAG} must be 1<br>
		 * Location set by user to Autoload Torrent files from.
		 * The client will scan the folder looking for .torrent files to start loading.
		 **/
		String DIR_AUTOLOAD = "dir_autoload";

		/** Number<br/>
		 * Preferences->Directories->Location of .torrents->Delete loaded torrents<br>
		 * {@link #DIR_AUTOLOAD_FLAG} must be 1<br>
		 * <br>
		 * 1 deletes .torrent file in {@link #DIR_AUTOLOAD} after import<br>
		 * no key if disabled
		 **/
		String DIR_AUTOLOAD_DELETE = "dir_autoload_delete";
	}

	/** String<br>
	 * Right Click on Torrent->Advanced->Set Download Location
	 **/
	String DIR_LAST = "dir_last";

	/** List of [String]<br>
	 * String appears to be "URL\tDate"
	 **/
	//String DL_IMAGE_MODIFIEDSINCE = "dl_image_modifiedsince";

	/**
	 * key "webui.pair_hashes" -> "entries" -> List<br>
	 * Probably webapps
	 */
	public class WEBUI_PAIR_HASHES
	{
		/** Number<br/>
		 *
		 **/
		String ENTRIES_ACCESS_BITS = "access_bits";

		/** byte[]<br>
		 *
		 **/
		String ENTRIES_APPID = "appid";

		/** byte[]<br>
		 *
		 **/
		String ENTRIES_KEY = "key";

		/** byte[]<br>
		 *
		 **/
		String ENTRIES_NAME = "name";
	}

	/** Number<br/>
	 *
	 **/
	//String EXE_SERIAL = "exe_serial";

	/** Number<br/>
	 * Something related to number of torrents added to uTorrent (not an active count of torrents, but total number added through life)
	 **/
	String FD = "fd";

	/** Number<br/>
	 *
	 **/
	String FGT = "fgt";

	/** Number<br/>
	 * 0 : Not a fresh install<br>
	 * Missing : Fresh Install<br>
	 **/
	String FRESH_INSTALL = "fresh_install";

	/** Number<br/>
	 *
	 **/
	//String GUI_LAST_BADGING_CHECK = "gui.last_badging_check";

	/** Number<br/>
	 *
	 **/
	//String GUI_LAST_BUNDLE_VISIT = "gui.last_bundle_visit";

	/**
	 * String<br>
	 *  Appears to be delimited with pipe '|'<br>
	 *  Labels listed here will always show in the sidebar
	 */
	String GUI_PERSISTENT_LABELS = "gui.persistent_labels";

	/** Number<br/>
	 *
	 **/
	//String GUI_PLUS_UPSELL_FOREGROUND = "gui.plus_upsell_foreground";

	/** Number<br/>
	 *
	 **/
	String INITIAL_INSTALL_VERSION = "initial_install_version";

	/** Map<br>
	 *
	 **/
	String INPUT_CODECS = "input_codecs";

	/** Number<br/>
	 *
	 **/
	String INSTALL_MODIFICATION_TIME = "install_modification_time";

	/** Number<br/>
	 *
	 **/
	String INSTALL_REVISION = "install_revision";

	/** list<br>
	 *
	 **/
	String INSTALLS = "installs";

	/** Number<br/>
	 *
	 **/
	String ISP_PEER_POLICY_EXPY = "isp.peer_policy_expy";

	interface label
	{

		/** Map<br>
		 * Used only if {@link #USEAUTOLABEL} is 1<br>
		 *   <br>
		 * 
		 * key: label name<br>
		 * value: save to dir<br>
		 **/
		String LABELDIRECTORYMAP = "labelDirectoryMap";

		/** Map<br>
		 * Used only if {@link #USEAUTOLABEL} is 1<br>
		 *   <br>
		 * key: label name<br>
		 * value: <ul>Rule:<br>
		 *   <li>"Default audio rule"</li>
		 *   <li>"Default document rule"</li>
		 *   <li>"Default video rule"</li>
		 *   <li>"contains:" + String</li>
		 *   </ul>
		 **/
		String LABELRULEMAP = "labelRuleMap";

		/**
		 * Number<br/>
		 *
		 * Preferences->Label->Use Label and Directory Rules<br>
		 * <br>
		 * 1 if {@link #LABELDIRECTORYMAP} and @link {@link #LABELRULEMAP} are enabled<br>
		 * no key if {@link #LABELDIRECTORYMAP} and @link {@link #LABELRULEMAP} are disabled<br>
		 */
		String USEAUTOLABEL = "useAutoLabel";
	}

	/** Number<br/>
	 *
	 **/
	String LANGUAGE = "language";

	/** Number<br/>
	 *
	 **/
	//String LAST_CAU_TIME = "last_cau_time";

	/** Number<br/>
	 *
	 **/
	String LAST_SHUTDOWN_DURATION = "last_shutdown_duration";

	/** Number<br/>
	 *
	 **/
	//String LIT = "lit";

	/** Number<br/>
	 *
	 **/
	//String LIT_REMOTE = "lit_remote";

	/** Number<br/>
	 *
	 **/
	//String LRECENABLED = "lrecenabled";

	/** Number<br/>
	 *
	 **/
	//String LSC = "lsc";

	public static class ListViewColumns
	{
		/** String<br>
		 * lvc_XXX->columns[]->id<br>
		 **/
		String COLUMNS_ID = "id";

		/** Number<br/>
		 * lvc_XXX->columns[]->state<br>
		 *   0 - 128 : Visible Position<br>
		 *   >= 128 : Invisible at position (value - 128)
		 **/
		String COLUMNS_STATE = "state";

		/** Number<br/>
		 * lvc_XXX->columns[]->width<br>
		 * Width probably in pixels
		 **/
		String COLUMNS_WIDTH = "width";

		/** Number<br/>
		 * lvc_XXX->sortmode<br>
		 * Index of "columns" for current sort column (NOT the state value).<br>
		 * Reverse sort is +128
		 **/
		String LVC_SORTMODE = "sortmode";

		/** Map<br>
		 * "Open Torrent Options" list view columns<BR>
		 * Columns:<BR>
		 *   <ol start="0">
		 *   <li>FI_COL_NAME</li>
		 *   <li>FI_COL_DESTINATION</li>
		 *   <li>FI_COL_SIZE</li>
		 *   </ol>
		 **/
		String LVC_ADDPRE = "lvc_addpre";

		/** Map<br>
		 * No Columns?
		 **/
		String LVC_CAT = "lvc_cat";

		/** Map<br>
		 * Columns:<BR>
		 *   <ol start="0">
		 *   <li>FI_COL_ORDER</li>
		 *   <li>STRID_FI_COL_DESTINATION</li>
		 *   <li>Size</li>
		 *   <li>FI_COL_ENCODING</li>
		 *   <li>FI_COL_STATUS</li>
		 *   <li>FI_COL_TRANSFERS</li>
		 *   <li>FI_COL_MEDIA_LENGTH</li>
		 *   <li>FI_COL_TORRENT</li>
		 *   <li>Resolution</li>
		 *   <li>FI_COL_ETA</li>
		 *   <li>FI_COL_DEVICE</li>
		 *   </ol>
		 **/
		String LVC_CONVERSION = "lvc_conversion";

		/** Map<br>
		 *
		 **/
		String LVC_DEVICE = "lvc_device";

		/** Map<br>
		 *
		 * Columns:<BR>
		 *   <ol start="0">
		 *   <li>FI_COL_NAME</li>
		 *   <li>FI_COL_DESTINATION</li>
		 *   <li>FI_COL_SIZE</li>
		 *   <li>FI_COL_DONE</li>
		 *   <li>FI_COL_PCT</li>
		 *   <li>FI_COL_FIRST</li>
		 *   <li>FI_COL_NUM</li>
		 *   <li>FI_COL_PRIO</li>
		 *   <li>FI_COL_MODE</li>
		 *   <li>FI_COL_ANTIVIRUS</li>
		 *   <li>FI_COL_RATE</li>
		 *   <li>FI_COL_RESOLUTION</li>
		 *   <li>FI_COL_DURATION</li>
		 *   <li>FI_COL_STREAMABLE</li>
		 *   <li>FI_COL_HAS_HEADER</li>
		 *   <li>FI_COL_CODECS</li>
		 *   </ol>
		 **/
		String LVC_FILES = "lvc_files";

		/** Map<br>
		 *
		 * No Columns?
		 **/
		String LVC_HIST = "lvc_hist";

		/** Map<br>
		 *
		 * No Columns?
		 **/
		String LVC_MIN = "lvc_min";

		/** Map<br>
		 * Torrent list view<br>
		 * Columns:<BR>
		 *   <ol start="0">
		 *   <li>OV_COL_NAME</li>
		 *   <li>OV_COL_ORDER</li>
		 *   <li>OV_COL_STREAMABLE_PROGRESS</li>
		 *   <li>OV_COL_SIZE</li>
		 *   <li>OV_COL_SELECTED_SIZE</li>
		 *   <li>OV_COL_COMPLETE</li>
		 *   <li>OV_COL_DOWNLOADED</li>
		 *   <li>OV_COL_REMAINING</li>
		 *   <li>OV_COL_DONE</li>
		 *   <li>OV_COL_STATUS</li>
		 *   <li>OV_COL_HEALTH</li>
		 *   <li>OV_COL_DOWNSPD</li>
		 *   <li>OV_COL_UPSPD</li>
		 *   <li>OV_COL_ETA</li>
		 *   <li>OV_COL_ANTIVIRUS</li>
		 *   <li>OV_COL_SEEDS</li>
		 *   <li>OV_COL_PEERS</li>
		 *   <li>OV_COL_SEEEDS_PEERS</li>
		 *   <li>OV_COL_UPPED</li>
		 *   <li>OV_COL_SHARED</li>
		 *   <li>OV_COL_AVAIL</li>
		 *   <li>OV_COL_LABEL</li>
		 *   <li>OV_COL_ADDED_ON</li>
		 *   <li>OV_COL_COMPLETED_ON</li>
		 *   <li>OV_COL_TRACKER</li>
		 *   <li>OV_COL_UPRATE_LIMIT</li>
		 *   <li>OV_COL_DOWNRATE_LIMIT</li>
		 *   <li>OV_COL_BWALLOC</li>
		 *   <li>OV_COL_TRACKERSTATUS</li>
		 *   <li>OV_COL_DEBUG</li>
		 *   <li>OV_COL_LAST_ACTIVE</li>
		 *   <li>OV_COL_ELAPSED</li>
		 *   <li>OV_COL_SOURCE_URL</li>
		 *   <li>OV_COL_EPISODE</li>
		 *   <li>OV_COL_FORMAT</li>
		 *   <li>OV_COL_SAVE_DIR</li>
		 *   <li>OV_COL_DISK_JOB</li>
		 *   </ol>
		 **/
		String LVC_OVERVIEW_APPS = "lvc_overview_apps";

		/** Map<br>
		 *
		 **/
		String LVC_OVERVIEW_RSS = "lvc_overview_rss";

		/** Map<br>
		 *
		 **/
		String LVC_PEERS = "lvc_peers";

		/** Map<br>
		 *
		 **/
		String LVC_PIECE = "lvc_piece";

		/** Map<br>
		 *
		 **/
		String LVC_PLAYBACK = "lvc_playback";

		/** Map<br>
		 *
		 **/
		String LVC_RSSREL = "lvc_rssrel";

		/** Map<br>
		 *
		 **/
		String LVC_TRACKER = "lvc_tracker";

	}

	/** Number<br/>
	 *
	 **/
	String MAINWND_SPLIT = "mainwnd_split";

	/** Number<br/>
	 *
	 **/
	String MAINWND_SPLIT_X = "mainwnd_split_x";

	/** Number<br/>
	 *
	 **/
	String MAINWNDSTATUS = "mainwndstatus";

	interface offers
	{
		/** Map<br>
		 *
		 **/
		String OFFERS_ADREFRESHRATEPATTERN = "offers.adRefreshRatePattern";

		/** List of [Long]<br>
		 *
		 **/
		String OFFERS_ADREFRESHRATEPATTERN_ADREFRESHRATEPATTERN = "offers.adRefreshRatePattern_adRefreshRatePattern";

		/** byte[]<br>
		 *
		 **/
		String OFFERS_ADURL = "offers.adUrl";

		/** Number<br/>
		 *
		 **/
		String OFFERS_ADRESOURCE_ENABLED = "offers.adresource_enabled";

		/** Number<br/>
		 *
		 **/
		String OFFERS_ADRESOURCE_KILL_ENABLED = "offers.adresource_kill_enabled";

		/** Number<br/>
		 *
		 **/
		String OFFERS_ADS_REQUEST_LREC_COUNT = "offers.ads_request_lrec_count";

		/** Number<br/>
		 *
		 **/
		String OFFERS_ADS_REQUEST_MREC_COUNT = "offers.ads_request_mrec_count";

		/** byte[]<br>
		 *
		 **/
		String OFFERS_ADZERK_ID = "offers.adzerk_id";

		/** Number<br/>
		 *
		 **/
		String OFFERS_BADADS_REPORT_ENABLED = "offers.badAds_report_enabled";

		/** Number<br/>
		 *
		 **/
		String OFFERS_BIGADS_LAST_SHOWTIME = "offers.bigads_last_showtime";

		/** Number<br/>
		 *
		 **/
		String OFFERS_BIGADS_REFRESH_TIME_INTERVAL = "offers.bigads_refresh_time_interval";

		/** Number<br/>
		 *
		 **/
		String OFFERS_CLIENTRECEIVEDADRULESFROMSERVER = "offers.clientReceivedAdRulesFromServer";

		/** Number<br/>
		 *
		 **/
		String OFFERS_CONTACTRATE = "offers.contactRate";

		/** Number<br/>
		 *
		 **/
		String OFFERS_COOKIE_DROP_ENABLED = "offers.cookie_drop_enabled";

		/** byte[]<br>
		 *
		 **/
		String OFFERS_COOKIE_DROP_URL = "offers.cookie_drop_url";

		/** Number<br/>
		 *
		 **/
		String OFFERS_COOKIEPAGE_LAST_SHOWTIME = "offers.cookiepage_last_showtime";

		/** list<br>
		 *
		 **/
		String OFFERS_DISPLAY_TOOLTIPS = "offers.display_tooltips";

		/** Number<br/>
		 *
		 **/
		String OFFERS_FEATURED_CONTENT_BADGE_ENABLED = "offers.featured_content_badge_enabled";

		/** Number<br/>
		 *
		 **/
		String OFFERS_FEATURED_CONTENT_NOTIFICATIONS_ENABLED = "offers.featured_content_notifications_enabled";

		/** Number<br/>
		 *
		 **/
		String OFFERS_FEATURED_CONTENT_RSS_ENABLED = "offers.featured_content_rss_enabled";

		/** byte[]<br>
		 *
		 **/
		String OFFERS_FEATURED_CONTENT_URL = "offers.featured_content_url";

		/** Number<br/>
		 *
		 **/
		String OFFERS_FIRST_TIME_UI_SHOW = "offers.first_time_ui_show";

		/** Map<br>
		 *
		 **/
		String OFFERS_FTADTYPES = "offers.ftAdTypes";

		/** List of [Long]<br>
		 *
		 **/
		String OFFERS_FTADTYPES_FTADTYPES = "offers.ftAdTypes_ftAdTypes";

		/** Map<br>
		 *
		 **/
		String OFFERS_FTREFRESHRATEPATTERN = "offers.ftRefreshRatePattern";

		/** list<br>
		 *
		 **/
		String OFFERS_FTREFRESHRATEPATTERN_FTREFRESHRATEPATTERN = "offers.ftRefreshRatePattern_ftRefreshRatePattern";

		/** byte[]<br>
		 *
		 **/
		String OFFERS_GDPR_COUNTRIES = "offers.gdpr_countries";

		/** Number<br/>
		 *
		 **/
		String OFFERS_GDPR_LAST_SHOWTIME = "offers.gdpr_last_showtime";

		/** Number<br/>
		 *
		 **/
		String OFFERS_GDPR_REFRESH_TIME_INTERVAL = "offers.gdpr_refresh_time_interval";

		/** byte[]<br>
		 *
		 **/
		String OFFERS_GEOIP_COUNTRY = "offers.geoip_country";

		/** Number<br/>
		 *
		 **/
		String OFFERS_GRAPHICSCARD_CHECK_TIMESTAMP = "offers.graphicscard_check_timestamp";

		/** Map<br>
		 *
		 **/
		String OFFERS_LRECADTYPES = "offers.lrecAdTypes";

		/** List of [Long]<br>
		 *
		 **/
		String OFFERS_LRECADTYPES_LRECADTYPES = "offers.lrecAdTypes_lrecAdTypes";

		/** Map<br>
		 *
		 **/
		String OFFERS_LRECREFRESHRATEPATTERN = "offers.lrecRefreshRatePattern";

		/** list<br>
		 *
		 **/
		String OFFERS_LRECREFRESHRATEPATTERN_LRECREFRESHRATEPATTERN = "offers.lrecRefreshRatePattern_lrecRefreshRatePattern";

		/** Number<br/>
		 *
		 **/
		String OFFERS_LRECXBUTTON = "offers.lrecXButton";

		/** byte[]<br>
		 *
		 **/
		String OFFERS_LRECXUPGRADEURL = "offers.lrecXUpgradeUrl";

		/** Number<br/>
		 *
		 **/
		String OFFERS_ONBOARD_ENABLED = "offers.onboard_enabled";

		/** byte[]<br>
		 *
		 **/
		String OFFERS_ONBOARDING_FIRST_URL = "offers.onboarding_first_url";

		/** byte[]<br>
		 *
		 **/
		String OFFERS_ONBOARDING_SECOND_URL = "offers.onboarding_second_url";

		/** Number<br/>
		 *
		 **/
		String OFFERS_SHOW_TIP_NOW = "offers.show_tip_now";

		/** Number<br/>
		 *
		 **/
		String OFFERS_STAT_SENT_TIME = "offers.stat_sent_time";

		/** Number<br/>
		 *
		 **/
		String OFFERS_SUPERAD_LAST_STARTTIME = "offers.superad_last_starttime";

		/** Number<br/>
		 *
		 **/
		String OFFERS_UPGRADE_TOOLBAR = "offers.upgrade_toolbar";

		/** Number<br/>
		 *
		 **/
		String ORIGINAL_CAMPAIGN_CODE = "original_campaign_code";

		/** Number<br/>
		 *
		 * smells
		 **/
		String CAMPAIGN_CODE = "campaign_code";
	}

	/** Map<br>
	 *
	 **/
	String OUTPUT_CODECS = "output_codecs";

	/** Map<br>
	 *
	 **/
	String PEAKRATE = "peakrate";

	/** byte[]<br>
	 *
	 **/
	String PEAKRATE_RATE_DATA = "peakrate_rate_data";

	/** Number<br/>
	 *
	 **/
	String RELEASE_NOTES_SHOWN_VERSION = "release_notes_shown_version";

	/** Number<br/>
	 *
	 **/
	String RESOLVE_PEERIPS = "resolve_peerips";

	/** byte[]<br>
	 *
	 **/
	String RSSWND = "rsswnd";

	/** Number<br/>
	 *
	 **/
	String RT = "rt";

	/** List of [String]<br>
	 * Drop down entries for Torrent Properties->Advanced->Run Program->Run this program when the download finishes:
	 **/
	String RUNPROG_HIST = "runprog_hist";

	/** Number<br/>
	 * Number of times the program started and closed since the install.  Updated on close of program.
	 **/
	String RUNS_SINCE_BORN = "runs_since_born";

	/** Number<br/>
	 * Time in seconds the program has been running on the PC.
	 **/
	String RUNTIME_SINCE_BORN = "runtime_since_born";

	interface crap
	{
		/** byte[8]<br>
		 *
		 * UNKNOWN
		 **/
		String AV2 = "av2";

		/** Map<br>
		 *
		 * UNKNOWN
		 **/
		String BENCHRECORDER = "benchrecorder";

		/** Number<br/>
		 *
		 * UNKNOWN
		 **/
		String BIN_CHANGE = "bin_change";

		/** String<br>
		 * Survey URL
		 **/
		String S_URL = "s_url";

		/** Number<br/>
		 *
		 **/
		String NEXT_MARKET_SHARE_REPORT = "next_market_share_report";

		/** byte[]<br>
		 *
		 **/
//	String CID = "cid";

		/** byte[]<br>
		 *
		 **/
//	String CIDS = "cids";
	}

	interface Scheduler
	{
		/** byte[168]<br>
		 *  24 hours * 7 days = 168<br>
		 *  [0] = Monday, 0:00 - 0:59
		 *
		 * Byte Value<br>
		 *   '0': 0x30 : Full Speed<br>
		 *   '1': 0x31 : Limited<br>
		 *   '2': 0x32 : Turn off<br>
		 *   '3': 0x33 : Seeding Only<br>
		 **/
		String SCHED_TABLE = "sched_table";

		/** Number<br/>
		 * Preferences->Scheduler->Scheduler Settings->Limit download rate (kB/s):<br>
		 */
		String SCHED_DL_RATE = "sched_dl_rate";

		/** Number<br/>
		 * Preferences->Scheduler->Scheduler Settings->Limit upload rate (kB/s):<br>
		 */
		String SCHED_UL_RATE = "sched_ul_rate";

		/** Number<br/>
		 * Preferences->Scheduler->Enable Scheduler<br>
		 *   <br>
		 * 1 if enabled<br>
		 * not set if disabled<br>
		 */
		String SCHED_ENABLE = "sched_enable";

		/** Number<br/>
		 * Preferences->Scheduler->Scheduler Settings->Disable DHT when turning off<br>
		 *   <br>
		 * not set if enabled<br>
		 * 0 if disabled<br>
		 */
		String SCHED_DIS_DHT = "sched_dis_dht";
	}

	/** Number<br/>
	 *
	 **/
	String SDUR = "sdur";

	/** String<br>
	 * List of Search sites used in clients toolbar.
	 * This can be added to by the user to assist in locating torrents.
	 * This will open up the user’s web browser so any artifacts of its usage may be found in the Internet History files.
	 **/
	String SEARCH_LIST = "search_list";

	/** list<br>
	 *
	 **/
	String SELECTED_CATS = "selected_cats";

	/** byte[]<br>
	 *
	 **/
	String SELFCERT = "selfcert";

	/** byte[]<br>
	 *
	 **/
	String SERVED_SEARCH_LIST = "served_search_list";

	/** byte[]<br>
	 *
	 **/
	String SERVED_SEARCH_TARGET = "served_search_target";

	/** Number<br/>
	 * Last time the client settings were changed.
	 * This is based on Epoch or Unix system time.
	 * This is the number of seconds that have elapsed since January 1, 1970 (UTC).
	 **/
	String SETTINGS_SAVED_SYSTIME = "settings_saved_systime";

	/** Number<br/>
	 *
	 **/
	String SHOW_PLAYBACK_TAB = "show_playback_tab";

	/** Number<br/>
	 *
	 **/
	String SID1 = "sid1";

	/** Number<br/>
	 *
	 **/
	String SMAXAGE = "smaxage";

	/** Number<br/>
	 *
	 **/
	String SMINAGE = "sminage";

	/** Number<br/>
	 *
	 **/
	String SMODE = "smode";

	/** Number<br/>
	 *
	 **/
	String SSAMPER = "ssamper";

	/** Number<br/>
	 *
	 **/
	String SST = "sst";

	/** Number<br/>
	 *
	 **/
	String ST = "st";

	/** byte[]<br>
	 *
	 **/
	String STITLE = "stitle";

	/** byte[]<br>
	 *
	 **/
	String STREAMING_PLAYBACK_PLAYER = "streaming.playback_player";

	/** byte[]<br>
	 *
	 **/
	String STREAMING_PREVIEW_PLAYER = "streaming.preview_player";

	/** Number<br/>
	 *
	 **/
	String STREAMING_ENABLED = "streaming_enabled";

	/** Number<br/>
	 *
	 **/
	String TAB_FS = "tab_fs";

	/** Number<br/>
	 *
	 **/
	String TAB_GL = "tab_gl";

	/** Number<br/>
	 *
	 **/
	String TAB_PE = "tab_pe";

	/** Number<br/>
	 *
	 **/
	String TAB_SD = "tab_sd";

	/** Number<br/>
	 *
	 **/
	String TAB_TS = "tab_ts";

	/** Number<br/>
	 *
	 **/
	String TD = "td";

	public class torrenttrack
	{
		/** Map<br>
		 *
		 **/
		String TORRENTTRACK = "torrenttrack";

		/** List of [Long]<br>
		 *
		 **/
		String TORRENTTRACK_ADDED = "added";

		/** List of [Long]<br>
		 *
		 **/
		String TORRENTTRACK_DELETED = "deleted";

		/** List of [Long]<br>
		 *
		 **/
		String TORRENTTRACK_DOWNLOAD = "download";

		/** List of [Long]<br>
		 *
		 **/
		String TORRENTTRACK_NI_ADDED = "ni_added";

		/** List of [Long]<br>
		 *
		 **/
		String TORRENTTRACK_UPLOAD = "upload";
	}

	/** Number<br/>
	 *
	 **/
	String TTA = "tta";

	/** Number<br/>
	 *
	 **/
	String TTD = "ttd";

	/** Number<br/>
	 *
	 **/
	String TTDAY = "ttday";

	/** Number<br/>
	 *
	 **/
	String TTT = "ttt";

	/** Number<br/>
	 *
	 **/
	String TU = "tu";

	/** List of [Long]<br>
	 *
	 **/
	String UPTIME = "uptime";

	class utp
	{
		/** Number<br/>
		 *
		 **/
		String UTP_RAW_RECV_BG = "utp_raw_recv_bg";

		/** Number<br/>
		 *
		 **/
		String UTP_RAW_RECV_EM = "utp_raw_recv_em";

		/** Number<br/>
		 *
		 **/
		String UTP_RAW_RECV_HG = "utp_raw_recv_hg";

		/** Number<br/>
		 *
		 **/
		String UTP_RAW_RECV_MD = "utp_raw_recv_md";

		/** Number<br/>
		 *
		 **/
		String UTP_RAW_RECV_SM = "utp_raw_recv_sm";

		/** Number<br/>
		 *
		 **/
		String UTP_RAW_SEND_BG = "utp_raw_send_bg";

		/** Number<br/>
		 *
		 **/
		String UTP_RAW_SEND_EM = "utp_raw_send_em";

		/** Number<br/>
		 *
		 **/
		String UTP_RAW_SEND_HG = "utp_raw_send_hg";

		/** Number<br/>
		 *
		 **/
		String UTP_RAW_SEND_MD = "utp_raw_send_md";

		/** Number<br/>
		 *
		 **/
		String UTP_RAW_SEND_SM = "utp_raw_send_sm";
	}

	/** Number<br/>
	 *
	 **/
	String V = "v";

	/** byte[]<br>
	 *
	 **/
	String WNDMAIN = "wndmain";

	interface webui
	{
		/** list<br>
		 *
		 **/
		String WEBUI_DOWNLOAD_FOLDERS = "webui.download_folders";

		/** Map<br>
		 *
		 **/
		String WEBUI_PAIR_HASHES = "webui.pair_hashes";

		/** List of [Map]<br>
		 *
		 **/
		String WEBUI_PAIR_HASHES_ENTRIES = "webui.pair_hashes_entries";

		/** byte[]<br>
		 *
		 **/
		String WEBUI_SSDP_UUID = "webui.ssdp_uuid";

		/** Number<br/>
		 *
		 **/
		String WEBUI_UCONNECT_TOOLBAR_EVER = "webui.uconnect_toolbar_ever";
	}

	interface adv
	{
		/**
		 * Enabling this option allows multiple incoming connections from the same
		 * IP address. This option affects a single torrent job at a time, so you
		 * can still have the same IP address connect to you on different torrent
		 * swarms. It is recommended that this option be left disabled, as it
		 * weakens the anti-leech protection.
		 */
		String BT_ALLOW_SAME_IP = "bt.allow_same_ip";

		/**
		 * The lowest acceptable ratio of good to bad pieces a peer can send before
		 * it gets banned. The lower this option is set, the more forgiving µTorrent
		 * will be toward bad pieces, meaning that it will be less likely to ban a
		 * peer. This takes effect after bt.ban_threshold is exceeded and
		 * bt.use_ban_ratio is enabled.
		 */
		String BT_BAN_RATIO = "bt.ban_ratio";

		/**
		 * This option specifies the maximum number of hashfailed pieces any single
		 * peer can send before µTorrent takes action against it (either banning it
		 * outright, or enforcing bt.ban_ratio if bt.use_ban_ratio is enabled).
		 */
		String BT_BAN_THRESHOLD = "bt.ban_threshold";

		/**
		 * Enabling this option allows µTorrent to create files in a manner such
		 * that the data are incrementally written to disk without file
		 * pre-allocation. Because writes are compact, enabling this option may
		 * potentially lead to an increased level of disk fragmentation while the
		 * file remains incomplete. In addition, this option further decreases the
		 * already-low probability that a file can be previewed before completion,
		 * since it may write the data for in-progress files out of order. Here are
		 * some things to take note of when using this option:
		 * <br>
		 * If you tell µTorrent to pre-allocate all disk space, this option is
		 * ignored, and µTorrent will pre-allocate the file anyway.
		 * <br>
		 * If this option is enabled, files can't be skipped. If a torrent job has
		 * skipped files, it will not use compact allocation.
		 */
		String BT_COMPACT_ALLOCATION = "bt.compact_allocation";

		/**
		 * This option specifies the number of connections µTorrent should allow to
		 * be attempted and/or established each second, whether the connections use
		 * uTP or TCP.
		 */
		String BT_CONNECT_SPEED = "bt.connect_speed";

		/**
		 * Enabling this option shows media files' rate in Files tab
		 */
		String BT_DETERMINE_ENCODED_RATE_FOR_STREAMABLES = "bt.determine_encoded_rate_for_streamables";

		/**
		 * Enabling this option effects ratings, and disables comments too.
		 */
		String BT_ENABLE_PULSE = "bt.enable_pulse";

		/**
		 * Enabling this option enables the rudimentary tracker embedded in
		 * µTorrent. If you wish to use this tracker, the URL is located at
		 * http://IP:port/announce, where IP is your WAN IP address, and port is the
		 * port µTorrent is listening on (or the alternative listening port if set
		 * and enabled). If you use a dynamic DNS service, your domain may be used
		 * instead of your IP address. The embedded tracker allows tracking of
		 * external .torrent files, and provides no way to limit them. There is no
		 * interface for viewing the .torrent files that are tracked. It is
		 * imperative that µTorrent is able to listen for incoming connections for
		 * this feature to work properly, so you have to make sure you have
		 * completely forwarded your ports in order to use the embedded tracker.
		 */
		String BT_ENABLE_TRACKER = "bt.enable_tracker";

		/**
		 * If enabled, µTorrent will take as long as it needs to finish its shutdown
		 * sequence (writing in-progress pieces to disk, deleting files in deletion
		 * queue, and waiting for tracker replies to stop messages -- among other
		 * things). That means that even if it takes several minutes to shutdown
		 * gracefully, it will wait for that long, and the process will remain in
		 * memory until then. If disabled, µTorrent will limit how long it waits to
		 * to 10 seconds, and regardless of the state of the shutdown sequence,
		 * µTorrent will force itself to exit.
		 */
		String BT_GRACEFUL_SHUTDOWN = "bt.graceful_shutdown";

		/**
		 * Enabling this option allows µTorrent to send multiple hashes each time it
		 * scrapes a tracker, which is more efficient than sending one hash at a
		 * time. In most circumstances, this option should not need to be disabled,
		 * as µTorrent will fall back to single scraping if it detects that the
		 * tracker does not support multi-scraping.
		 */
		String BT_MULTISCRAPE = "bt.multiscrape";

		/**
		 * This option tells µTorrent not to connect to peers using ports specified
		 * in bt.no_connect_to_services_list as their listening ports. This stops
		 * firewalls from complaining about µTorrent trying to send an e-mail.
		 */
		String BT_NO_CONNECT_TO_SERVICES = "bt.no_connect_to_services";

		/**
		 * This option specifies which ports µTorrent should not connect to when
		 * bt.no_connect_to_services is enabled.
		 */
		String BT_NO_CONNECT_TO_SERVICES_LIST = "bt.no_connect_to_services_list";

		/**
		 * Enabling this option prioritizes the first and last pieces of each file
		 * in a torrent job, increasing the chances that they can be previewed
		 * before download completion. µTorrent will prioritize at least the first
		 * and last 1 MiB of data in a file.
		 */
		String BT_PRIO_FIRST_LAST_PIECE = "bt.prio_first_last_piece";

		/**
		 * Enabling this option ('True'), tells µTorrent to always try to request
		 * blocks from pieces we've already started.
		 */
		String BT_PRIORITIZE_PARTIAL_PIECES = "bt.prioritize_partial_pieces";

		/**
		 * Disabling this option tells µTorrent not to change peerid on every
		 * connection. It still uses unique one for tracker, and should also be
		 * random limit. This randomization is not being used for private torrents.
		 */
		String BT_RANDOMIZE_PEER_ID = "bt.randomize_peer_id";

		/**
		 * Enabling this option tells µTorrent to limit the upload and download
		 * rates for TCP connections based on information received over the uTP
		 * transport rather than using static global rate limits. This option is
		 * ignored if bt.tcp_rate_control is disabled.
		 */
		String BT_RATELIMIT_TCP_ONLY = "bt.ratelimit_tcp_only";

		/**
		 * This debugging option defines µTorrent's local receive buffer socket
		 * size. Tweaking can reduce memory usage, but may hurt performance if too
		 * low. 3.3
		 */
		String BT_RCV_MAX_SOCKBUF = "bt.rcv_max_sockbuf";

		/**
		 * This option makes downloaded files have the read-only attribute set when
		 * torrent is complete. This prevents MP3s and the like from being retagged
		 * by media players and corrupted.3.3
		 */
		String BT_READ_ONLY_ON_COMPLETE = "bt.read_only_on_complete";

		/**
		 * This option set the time interval for saving the "resume" data to every n
		 * seconds.
		 */
		String BT_SAVE_RESUME_RATE = "bt.save_resume_rate";

		/**
		 * Enabling this option allows µTorrent to get seed and peer counts for
		 * torrent jobs that are stopped.
		 */
		String BT_SCRAPE_STOPPED = "bt.scrape_stopped";

		/**
		 * Enabling this option tells µTorrent to send a message to other seeds
		 * indicating how many pieces you currently have.
		 */
		String BT_SEND_HAVE_TO_SEED = "bt.send_have_to_seed";

		/**
		 * This debugging option allows µTorrent to automatically detect the TCP
		 * buffer size periodically (so_sndbuf) and adjust it based on your upload
		 * speed. It does not adjust based on latency.
		 */
		String BT_SET_SOCKBUF = "bt.set_sockbuf";

		/**
		 * This option controls the maximum amount of time µTorrent will wait, when
		 * exiting, for each tracker to respond to a stopped event before it forces
		 * itself to terminate. This value is interpreted in seconds, so please
		 * enter it as such. Setting this value to 0 tells µTorrent to wait for an
		 * indefinite amount of time until it receives a response.
		 */
		String BT_SHUTDOWN_TRACKER_TIMEOUT = "bt.shutdown_tracker_timeout";

		/**
		 * This option controls the maximum amount of time µTorrent will wait, when
		 * exiting, for routers to respond to a request to un-map the listening
		 * ports before it forces itself to terminate. This value is interpreted in
		 * seconds, so please enter it as such. Setting this value to 0 tells
		 * µTorrent to wait for an indefinite amount of time until it receives a
		 * response.
		 */
		String BT_SHUTDOWN_UPNP_TIMEOUT = "bt.shutdown_upnp_timeout";

		/**
		 * This debugging option defines µTorrent's local send buffer socket size.
		 * Tweaking can reduce memory usage, but may hurt performance if too low.
		 * 3.3
		 */
		String BT_SEND_MAX_SOCKBUF = "bt.send_max_sockbuf";

		/**
		 * Enabling this option tells µTorrent to use information from the uTP
		 * transport as hints for limiting TCP transfer rates.
		 */
		String BT_TCP_RATE_CONTROL = "bt.tcp_rate_control";

		/**
		 * This option controls µTorrent's level of bias towards using TCP or uTP
		 * for transporting data (assuming the peer at the other end of the
		 * connection supports both transport protocols). The following is a list of
		 * the accepted values:
		 * <br>
		 * 1 allows µTorrent to attempt outgoing TCP connections
		 * <br>
		 * 2 allows µTorrent to attempt outgoing uTP connections
		 * <br>
		 * 4 allows µTorrent to accept incoming TCP connections
		 * <br>
		 * 8 allows µTorrent to accept incoming uTP connections
		 * <br>
		 * 16 tells µTorrent to use the new uTP header. This is an improved
		 * communication header, but is not backwards compatible with clients that
		 * do not understand it.
		 * <br>
		 * This option is interpreted as a bitfield, so values can be added together
		 * to obtain a combination of behaviors. Setting this value to 255
		 * guarantees that all behaviors are enabled.
		 */
		String BT_TRANSP_DISPOSITION = "bt.transp_disposition";

		/**
		 * This option tells µTorrent to use bt.ban_ratio to decide when a peer gets
		 * banned after it has exceeded bt.ban_threshold.
		 */
		String BT_USE_BAN_RATIO = "bt.use_ban_ratio";

		/**
		 * This option tells µTorrent to respect or ignore BEP34.
		 */
		String BT_USE_DNS_TRACKER_PREFS = "bt.use_dns_tracker_prefs";

		/**
		 * When enabled, µTorrent will automatically attempt to determine whether an
		 * entire range of IP addresses should be banned for sending hashfailed
		 * pieces rather than banning individual IPs one at a time. When µTorrent
		 * bans 4 IPs from the same /24 CIDR block, it will ban the entire /24 CIDR
		 * block. When µTorrent bans 4 CIDR blocks of size /24 from the same /16
		 * CIDR block, it will ban the entire /16 CIDR block. When µTorrent bans 4
		 * CIDR blocks of size /16 from the same /8 CIDR block, it will ban the
		 * entire /8 CIDR block.
		 */
		String BT_USE_RANGEBLOCK = "bt.use_rangeblock";

		/**
		 * Enabling this option causes µTorrent to open files in synchronous mode so
		 * all writes are immediately flushed to disk 3.3
		 */
		String DISKIO_ALL_WRITES_SYNC = "diskio.all_writes_sync";

		/**
		 * This option determine how often (in minutes) µTorrent compacts the disk
		 * cache
		 */
		String DISKIO_CACHE_REDUCE_MINUTES = "diskio.cache_reduce_minutes";

		/**
		 * This option tells µTorrent the size of blocks of memory used in it's disk
		 * cache in KiB. Minimum of piece size and cache stripe are in KiB.
		 */
		String DISKIO_CACHE_STRIPE = "diskio.cache_stripe";

		/**
		 * This option determines the size threshold for which µTorrent should write
		 * data out coalesced, and is relevant only if diskio.coalesce_writes is
		 * enabled. This value is interpreted in bytes per second, so please enter
		 * it as such.
		 */
		String DISKIO_COALESCE_WRITE_SIZE = "diskio.coalesce_write_size";

		/**
		 * This option tells µTorrent to try to minimize the number of writes to
		 * disk by writing more data at once. It doesn't have any effect on download
		 * speeds, but might increase memory and CPU usage to achieve less disk
		 * writes.
		 */
		String DISKIO_COALESCE_WRITES = "diskio.coalesce_writes";

		/**
		 * Enabling this option causes µTorrent to close file handles every minute.
		 * It helps to reduce the effect of Windows managing the system cache badly
		 * for some people and causing apparent "memory leaks."
		 */
		String DISKIO_FLUSH_FILES = "diskio.flush_files";

		/**
		 * This option sets NTFS ADS that tells Windows that this file was
		 * downloaded from the Internet 3.3
		 */
		String DISKIO_MARK_OF_THE_WEB = "diskio.mark_of_the_web";

		/**
		 * This option sets the maximum depth of the write queue before the client
		 * starts showing disk overloaded 3.3
		 */
		String DISKIO_MAX_WRITE_QUEUE = "diskio.max_write_queue";

		/**
		 * This option disables compact allocation, might be POSIX only 3.3
		 */
		String DISKIO_MINIMIZE_KERNEL_CACHING = "diskio.minimize_kernel_caching ";

		/**
		 * Enabling this option causes µTorrent to skip the zero-filling process for
		 * file allocation. This option works only on Windows XP or newer, and
		 * requires administrator privileges by default. However, it is possible to
		 * make this work on limited accounts by setting the "Perform volume
		 * maintenance tasks" policy appropriately in the Windows Group Policy
		 * Editor. Skipping zero-filling speeds up the file allocation process, but
		 * because the allocated files have shared read access, there is a risk that
		 * any sensitive data that may have once existed at that location in disk
		 * but isn't wiped will potentially be exposed for other applications and
		 * users to read, including those without volume maintenance privileges.
		 */
		String DISKIO_NO_ZERO = "diskio.no_zero";

		/**
		 * Toggles advanced optimization when verifying a torrent data's integrity.
		 */
		String DISKIO_QUICK_HASH = "diskio.quick_hash";

		/**
		 * Megabytes to be free on disk before torrent resumes
		 */
		String DISKIO_RESUME_MIN = "diskio.resume_min";

		/**
		 * This option makes µTorrent hash data from memory (if in the write queue)
		 * instead of flushing to disk, re-reading from disk, and then hashing. This
		 * should help reduce hard disk reads, especially when transferring at high
		 * speeds.
		 */
		String DISKIO_SMART_HASH = "diskio.smart_hash";

		/**
		 * This option is a workaround for a problem in some versions of Windows
		 * that return incorrect data to µTorrent regarding sparse files and the
		 * amount of data that has actually been completed on disk.
		 */
		String DISKIO_SMART_SPARSE_HASH = "diskio.smart_sparse_hash";

		/**
		 * Enabling this option causes µTorrent to allocate only the data that it
		 * writes, but will inform the filesystem of the file's size (so that it can
		 * attempt to reserve enough contiguous space on the hard drive without
		 * having to physically zero all of the space out for the file). Even though
		 * space is reserved for the file, no space will be taken for the unwritten
		 * parts of the file. Enabling this option may potentially lead to increased
		 * disk fragmentation in rare cases where the drive does not have enough
		 * free space available to honor the space reservation for sparse files.
		 * Here are some things to take note of when using this option:
		 * <br>
		 * Sparse files work only on partitions that are formatted as NTFS.
		 * <br>
		 * Hash checking sparse files tends to be quicker than hash checking
		 * pre-allocated files, as µTorrent won't have to hash zeroed-out
		 * pre-allocated data.
		 * <br>
		 * On Windows Vista, sparse files can cause µTorrent to run into a file
		 * system limitation.
		 * <br>
		 * If you are using a non-administrator account with a disk quota, sparse
		 * files won't work, and the file will still get fully allocated. This is a
		 * limitation with Windows that µTorrent can't do anything about.
		 * <br>
		 * This option cannot be used in conjunction with pre-allocate all files. If
		 * both options are enabled simultaneously, pre-allocation will take
		 * precedence.
		 * <br>
		 * When used in conjunction with bt.compact_allocation, µTorrent will
		 * reserve space for each file in the filesystem, but it will continue to
		 * use compact writes.
		 */
		String DISKIO_SPARSE_FILES = "diskio.sparse_files";

		/**
		 * This option is used to store data that is downloaded from files that you
		 * told µTorrent to skip. This is necessary to prevent the file from being
		 * allocated. It separately stores the parts of the skipped files that come
		 * with a piece, since µTorrent must download and save the entire piece in
		 * order to confirm that it is uncorrupted, and each piece can contain data
		 * from multiple files. The partfile is removed when you remove the torrent
		 * job from the torrent job list.
		 */
		String DISKIO_USE_PARTFILE = "diskio.use_partfile";

		/**
		 * This option enables the participation in distributed backups 3.3
		 */
		String DISTRIBUTED_SHARE_ENABLE = "distributed_share.enable";

		/**
		 * This option configures crash recovery in µTorrent. When this option is
		 * enabled, if µTorrent crashes while it is minimized and the user has been
		 * idle at the computer for more than 1 minute, this option will cause
		 * µTorrent to automatically restart, and a notification of the crash
		 * (without a crash dump or any personally identifiable information) will be
		 * sent to the developers. If µTorrent crashes more than once within an
		 * hour, this option will not cause it to automatically restart again after
		 * the first crash, as such frequency of crashes is indicative of some
		 * important underlying problem that should not be ignored by the user. In
		 * this situation, µTorrent will fall back to the regular behavior (as if
		 * this option were disabled), where it displays a crash dialog that allows
		 * the user to choose how to proceed.
		 */
		String GUI_AUTO_RESTART = "gui.auto_restart";

		/**
		 * This option enables or disables color in the torrent progress bars. <br>
		 */
		String GUI_COLOR_PROGRESS_BARS = "gui.color_progress_bars";

		/**
		 * This option will show you the progress of the download (% done
		 * downloading) inside the colored status bar/column.<br>
		 */
		String GUI_COMBINE_LISTVIEW_STATUS_DONE = "gui.combine_listview_status_done";

		/**
		 * If you experience abnormal behaviors while browsing directories in
		 * µTorrent, such as a blank browsing dialog, try enabling this option.
		 */
		String GUI_COMPAT_DIROPEN = "gui.compat_diropen";

		/**
		 * This option tells µTorrent how it should remove torrent jobs when
		 * pressing the Remove button or Delete on your keyboard. Note that any
		 * value above 3 will cause the "Remove" button and the Delete button on
		 * your keyboard to do nothing in µTorrent. To be safer, you'd best be
		 * setting this option in the GUI through the toolbar method.
		 * <br>
		 * 0 means "Remove"
		 * <br>
		 * 1 means "Remove and delete .torrent"
		 * <br>
		 * 2 means "Remove and delete Data"
		 * <br>
		 * 3 means "Remove and delete .torrent + Data"
		 */
		String GUI_DEFAULT_DEL_ACTION = "gui.default_del_action";

		/**
		 * Enabling this option tells µTorrent to attempt to delete files to the
		 * Recycle Bin rather than directly erasing them from the disk. It is easier
		 * to set this option in the GUI through the toolbar method.
		 */
		String GUI_DELETE_TO_TRASH = "gui.delete_to_trash";

		/**
		 * This option enables or disables the torrent comment feature.
		 */
		String GUI_ENABLE_COMMENTS = "gui.enable_comments";

		/**
		 * This option tells µTorrent to draw a legend over the graphs displayed in
		 * the Speed tab to describe each of the lines drawn on the graph.
		 */
		String GUI_GRAPH_LEGEND = "gui.graph_legend";

		/**
		 * If enabled, this option tells µTorrent to draw communication overhead
		 * lines in the Speed tab's transfer rate graphs. Otherwise, only the
		 * "Network Overhead" graph will display information about communication
		 * overhead.
		 */
		String GUI_GRAPH_OVERHEAD = "gui.graph_overhead";

		/**
		 * This option tells µTorrent to draw the TCP rate control/limit graph on
		 * the download speed graphs<br>
		 */
		String GUI_GRAPH_TCP_RATE_CONTROL = "gui.graph_tcp_rate_control";

		/**
		 * This option tells µTorrent to draw a progress bar for each torrent job in
		 * the torrent jobs list, behind the Done column.
		 */
		String GUI_GRAPHIC_PROGRESS = "gui.graphic_progress";

		/**
		 * This option causes the date to be included in the timestamp shown in the
		 * Logger tab.
		 */
		String GUI_LOG_DATE = "gui.log_date";

		/**
		 * This optio shows the protocol-network overhead in the status bar.
		 */
		String GUI_OVERHEAD_IN_STATUSBAR = "gui.overhead_in_statusbar";

		/**
		 * If enabled, this option tells µTorrent to draw the lower Downloaded bar
		 * as the progress bar for each torrent job in the torrent jobs list, behind
		 * the Done column. This option works only if gui.graphic_progress is
		 * enabled, and will hide the percentage from the column.
		 */
		String GUI_PIECEBAR_PROGRESS = "gui.piecebar_progress";

		/**
		 * If enabled, this option tells µTorrent to report hangs in the user
		 * interface thread back to the µTorrent servers anonymously. The
		 * information sent is not personally identifiable, but can assist the
		 * developers in fixing (or identifying the cause of) the user interface
		 * hang. Whenever a report is sent to the server, a message is added to the
		 * Logger tab.
		 */
		String GUI_REPORT_PROBLEMS = "gui.report_problems";

		/**
		 * This option shows an antivirus icon when Plus version is installed.
		 */
		String GUI_SHOW_AV_ICON = "gui.show_av_icon";

		/**
		 * This option show devices pane in sidebar.
		 */
		String GUI_SHOW_DEVICES = "gui.show_devices";

		/**
		 * This option will show a page in the sidebar when no torrents are loaded.
		 */
		String GUI_SHOW_NOTORRENTS_NODE = "gui.show_notorrents_node";

		/**
		 * This option puts a new item in the sidebar when streaming a video in the
		 * client.
		 */
		String GUI_SHOW_PLAYER_NODE = "gui.show_player_node";

		/**
		 * This option shows the "Plus" box on the sidebar.
		 */
		String GUI_SHOW_PLUS_UPSELL = "gui.show_plus_upsell";

		/**
		 * This option shows favicons for your RSS feeds
		 */
		String GUI_SHOW_RSS_FAVICONS = "gui.show_rss_favicons";

		/**
		 * This option shows status icons in the main listview of torrents.
		 */
		String GUI_SHOW_STATUS_ICON_ON_DL_LIST = "gui.show_status_icon_on_dl_list";

		/**
		 * This option toggles the Category List's height between short and tall.
		 * When taller, the Category List displaces the Detailed Info Pane's
		 * left-hand side. When shorter, the Category List's lower section is
		 * displaced by the Detailed Info pane. A taller list might be more optimal
		 * for users with many labels and RSS feeds
		 */
		String GUI_TALL_CATEGORY_LIST = "gui.tall_category_list";

		/**
		 * If enabled, this option tells µTorrent to draw a transparent background
		 * behind the legend (otherwise, the background is opaque).
		 */
		String GUI_TRANSPARENT_GRAPH_LEGEND = "gui.transparent_graph_legend";

		/**
		 * This option controls the amount of time between each update of the
		 * µTorrent main window. The higher it is, the less frequently µTorrent
		 * updates the main window, meaning that if you select 1000, the information
		 * displayed on the main window is at most 1000 milliseconds (1 second) old.
		 * For users of slower computers, you might want to increase this number to
		 * decrease resource usage when the main window is displayed. Any value
		 * below 500 will be ignored (and 500 will be used instead).
		 */
		String GUI_UPDATE_RATE = "gui.update_rate";

		/**
		 * This option shows inexact dates instead of timestamps (e.g. "5 minutes
		 * ago")
		 */
		String GUI_USE_FUZZY_DATES = "gui.use_fuzzy_dates";

		/**
		 * This option, when enabled, tells µTorrent to load ipfilter.dat and apply
		 * the rules on connections established after it is loaded. Note that
		 * disabling and re-enabling this option will force µTorrent to reload
		 * ipfilter.dat.
		 */
		String IPFILTER_ENABLE = "ipfilter.enable";

		/**
		 * This option enables Local Tracker Discovery accordonmg to BEP22, allowing
		 * µTorrent to attempt to discover ISP-local trackers via a series of
		 * reverse DNS lookups. The ISP-local tracker can return a list of peers and
		 * caches (most likely ISP-local). Note that if your ISP is known to
		 * interfere with BitTorrent traffic, careful consideration should be taken
		 * in deciding to enable this option. Announcing to a ISP-hosted tracker
		 * indicates to the ISP that you are using BitTorrent, and as such, can make
		 * it easier for the ISP to interfere. Private torrent jobs are not
		 * announced to local trackers.
		 */
		String ISP_BEP22 = "isp.bep22";

		/**
		 * If your ISP does not return a correct reverse-DNS name, this allows you
		 * to set your reverse lookup name for the purposes of BEP22
		 */
		String ISP_FQDN = "isp.fqdn";

		/**
		 * This option enables peer policy functionality, which sets weights to
		 * different IP ranges.
		 */
		String ISP_PEER_POLICY_ENABLE = "isp.peer_policy_enable";

		/**
		 * This option overrides the peer policy.
		 */
		String ISP_PEER_POLICY_OVERRIDE = "isp.peer_policy_override";

		/**
		 * This option sets a URL to the ISP's peer policy.
		 */
		String ISP_PEER_POLICY_URL = "isp.peer_policy_url";

		/**
		 * This option sets the primary DNS server Ip of your ISP.
		 */
		String ISP_PRIMARY_DNS = "isp.primary_dns";

		/**
		 * This option sets the primary DNS server Ip of your ISP.
		 */
		String ISP_SECONDARY_DNS = "isp.secondary_dns";

		/**
		 * This options enables new "Offers" (Ads) by Bittorrent Inc located at the
		 * left pane. Disable it if you like not to be alerted to new offers.<br>
		 */
		String left_rail_offer_enabled = "left_rail_offer_enabled";

		/**
		 * This option logs debug output of UPnP to a file (warning: spammy).
		 */
		String LOGGER_LOG_UPNP_TO_FILE = "logger.log_upnp_to_file";

		/**
		 * If your computer setup requires that you use a specific LAN adapter for
		 * incoming connections, you may specify that adapter's IP address here.
		 */
		String NET_BIND_IP = "net.bind_ip";

		/**
		 * This option applies ratelimits to RSS traffic as well.
		 */
		String NET_CALC_RSS_OVERHEAD = "net.calc_rss_overhead";

		/**
		 * This option applies ratelimits to tracker traffic (warning: could break
		 * tracker communication under load).
		 */
		String NET_CALC_TRACKER_OVERHEAD = "net.calc_tracker_overhead";

		/**
		 * If enabled, this option tells µTorrent to listen on one of a sequence of
		 * well-known ports for incoming connections in addition to the standard and
		 * alternative listening ports. Because the sequence of ports is well-known
		 * to applications attempting to interface with µTorrent, it allows for such
		 * applications to connect to µTorrent with less effort on the user's part.
		 */
		String NET_DISCOVERABLE = "net.discoverable";

		/**
		 * This option blocks all incoming IPV6 connections.
		 */
		String NET_DISABLE_INCOMING_IPV6 = "net.disable_incoming_ipv6";

		/**
		 * Local network UPnP device name.
		 */
		String NET_FRIENDLY_NAME = "net.friendly_name";

		/**
		 * This option decides whether µTorrent should apply the Transfer Cap limits
		 * to traffic between itself and peers on the local network. Peers are
		 * considered local if they are discovered by Local Peer Discovery, or if
		 * they are on the same LAN as the client.
		 */
		String NET_LIMIT_EXCLUDESLOCAL = "net.limit_excludeslocal";

		/**
		 * Enabling this option reduces CPU usage slightly. You may achieve faster
		 * speeds with this option disabled. In general, this option is useless for
		 * most people unless they have extremely fast connections.
		 */
		String NET_LOW_CPU = "net.low_cpu";

		/**
		 * This option specifies how many connections µTorrent should attempt to
		 * establish simultaneously at any given time. On systems running Windows XP
		 * with Service Pack 2 (SP2) or newer, if your TCPIP.sys file is unpatched,
		 * you should leave this option at its default value.
		 */
		String NET_MAX_HALFOPEN = "net.max_halfopen";

		/**
		 * If your computer setup requires that you use a specific LAN adapter for
		 * outgoing connections, you may specify that adapter's IP address here.
		 * Note that Windows will sometimes ignore this setting and use other
		 * adapters due to their binding orders in Windows. To fix this, read
		 * Microsoft's knowledge base article KB894564.
		 */
		String NET_OUTGOING_IP = "net.outgoing_ip";

		/**
		 * This sets the upper limit for the outgoing port range. If this option is
		 * set to some invalid port number or some value less than
		 * net.outgoing_port, it gets ignored, and only net.outgoing_port gets
		 * looked at (meaning the outgoing port "range" will actually be a single
		 * outgoing port).
		 */
		String NET_OUTGOING_MAX_PORT = "net.outgoing_max_port";

		/**
		 * This option specifies the port that µTorrent should use to make outgoing
		 * connections. Normally, µTorrent selects a port from the ephemeral port
		 * range at random. "This can be used with full cone NAT routers to reduce
		 * the number of NAT table entries and thus prevent cashes on some router
		 * models. When the outgoing port is bound to the same as the incoming port
		 * that might even solve NAT problems on full cone NAT routers" (Advanced
		 * Network Settings on AzureusWiki). This option only works on Windows 2000
		 * and above. This option is ignored if it is not a valid port number.
		 */
		String NET_OUTGOING_PORT = "net.outgoing_port";

		/**
		 * This option disables automatic forwarding of the listening port for UDP
		 * via UPnP, telling µTorrent to forward the port for TCP only. This fixes
		 * an issue with some broken routers that overwrite the TCP forwarding with
		 * the UDP forwarding.
		 */
		String NET_UPNP_TCP_ONLY = "net.upnp_tcp_only";

		/**
		 * If enabled, this option allows µTorrent to adjust the uTP packet size in
		 * response to connection conditions detected through information gathered
		 * by uTP, changing up to as often as net.utp_packet_size_interval allows.
		 * If disabled, µTorrent uses the initial packet size for all uTP
		 * communication, as set by net.utp_initial_packet_size.
		 */
		String NET_UTP_DYNAMIC_PACKET_SIZE = "net.utp_dynamic_packet_size";

		/**
		 * This controls the initial size of the uTP packets that µTorrent uses when
		 * initiating a uTP connection. If net.utp_dynamic_packet_size is enabled,
		 * packet sizes can change dynamically during the lifetime of the uTP
		 * connection, depending on the connection conditions; this option only
		 * controls how µTorrent starts off. This option is interpreted as a
		 * multiplier of 150 bytes, so please enter it as such. Any value below 1
		 * will be ignored (and 1 will be used instead), and any value above 8 will
		 * be ignored (and 8 will be used instead). Effectively, that means that the
		 * initial packet sizes selectable by the user are the multiples of 150
		 * bytes between (and including) 150 bytes and 1200 bytes.
		 */
		String NET_UTP_INITIAL_PACKET_SIZE = "net.utp_initial_packet_size";

		/**
		 * This controls how often uTP alters its packet size in response to network
		 * conditions, assuming net.utp_dynamic_packet_size is enabled. This value
		 * is interpreted in seconds, so please enter it as such.
		 */
		String NET_UTP_PACKET_SIZE_INTERVAL = "net.utp_packet_size_interval";

		/**
		 * This controls the threshold detected connection receive delay that, if
		 * surpassed, will cause µTorrent to throttle back on bandwidth usage. The
		 * higher this option is set, the more forgiving µTorrent will be toward
		 * connection delays, meaning that it will be less likely to throttle back
		 * on bandwidth usage. Receive delay is detected by tracking the changes in
		 * the deltas between uTP packet timestamps and packet receive times. This
		 * option is interpreted in milliseconds, to please enter it as such.
		 */
		String NET_UTP_RECEIVE_TARGET_DELAY = "net.utp_receive_target_delay";

		/**
		 * This option controls the threshold detected connection send delay that,
		 * if surpassed, will cause µTorrent to throttle back on bandwidth usage.
		 * The higher this option is set, the more forgiving µTorrent will be toward
		 * connection delays, meaning that it will be less likely to throttle back
		 * on bandwidth usage. Send delay is the receive delay as observed by
		 * recipient uTP peers, which is reported back to the client by the
		 * recipient peers. This option is interpreted in milliseconds, so please
		 * enter it as such.
		 */
		String NET_UTP_TARGET_DELAY = "net.utp_target_delay";

		/**
		 * When enabled, if torrent offer (featured content) has autoexec flag AND
		 * is signed correctly by us, µTorrent will auto-execute the content.
		 */
		String OFFERS_CONTENT_OFFER_AUTOEXEC = "offers.content_offer_autoexec";

		/**
		 * Used to test install-time content offers
		 */
		String OFFERS_CONTENT_OFFER_URL = "offers.content_offer_url";

		/**
		 * Enabling this option tells µTorrent to disconnect from a peer that is not
		 * transferring with you after peer.disconnect_inactive_interval seconds of
		 * inactivity. A peer gets disconnected by this option only if the
		 * connection limit has been reached.
		 */
		String PEER_DISCONNECT_INACTIVE = "peer.disconnect_inactive";

		/**
		 * This option sets the amount of time µTorrent should wait before breaking
		 * an inactive connection. This value is interpreted in seconds, so please
		 * enter it as such. Any value below 300 will be ignored (and 300 will be
		 * used instead).
		 */
		String PEER_DISCONNECT_INACTIVE_INTERVAL = "peer.disconnect_inactive_interval";

		/**
		 * Some ISPs block seeding by looking for the complete bitfield and closing
		 * the connection. When enabled, µTorrent does not send the complete
		 * bitfield, but a sample of it, so as to prevent blocking of seeding.
		 */
		String PEER_LAZY_BITFIELD = "peer.lazy_bitfield";

		/**
		 * Enabling this option tells µTorrent to use an Internet database of IP
		 * addresses (a DNSBL) to determine a peer's country. Even if the settings
		 * directory contains flags.conf and flags.bmp, this option will take
		 * precedence, and the internal flag images will be used instead.
		 */
		String PEER_RESOLVE_COUNTRY = "peer.resolve_country";

		/**
		 * Enabling this option tells µTorrent to ignore slow downloading torrent
		 * jobs as part of the queue. If a torrent job is downloading at less than
		 * the value specified by queue.slow_dl_threshold, it will not prevent the
		 * next item in the queue from starting.
		 */
		String QUEUE_DONT_COUNT_SLOW_DL = "queue.dont_count_slow_dl";

		/**
		 * Enabling this option tells µTorrent to ignore slow uploading torrent jobs
		 * as part of the queue. If a torrent job is uploading at less than the
		 * value specified by queue.slow_ul_threshold, it will not prevent the next
		 * item in the queue from starting.
		 */
		String QUEUE_DONT_COUNT_SLOW_UL = "queue.dont_count_slow_ul";

		/**
		 * Enabling this option gives torrent jobs without seeds higher priority
		 * when seeding than other torrent jobs.
		 */
		String QUEUE_PRIO_NO_SEEDS = "queue.prio_no_seeds";

		/**
		 * The rate below which µTorrent should consider a torrent job to be
		 * downloading slowly. If µTorrent is downloading at a rate above this
		 * value, it is considered to be actively downloading. This value is
		 * interpreted in bytes per second, so please enter it as such.
		 */
		String QUEUE_SLOW_DL_THRESHOLD = "queue.slow_dl_threshold";

		/**
		 * The rate below which µTorrent should consider a torrent job to be
		 * uploading slowly. If µTorrent is uploading at a rate above this value, it
		 * is considered to be actively uploading. This value is interpreted in
		 * bytes per second, so please enter it as such.
		 */
		String QUEUE_SLOW_UL_THRESHOLD = "queue.slow_ul_threshold";

		/**
		 * When this option is enabled, µTorrent will determine the seeding queue
		 * order based on the ratio of the number of seeds to the number of peers
		 * connected in the swarm. The lower the seed:peer ratio is for a torrent
		 * job, the higher priority it will be given in the seeding queue. If a
		 * torrent job has 0 peers and queue.dont_count_slow_ul is disabled, it will
		 * be given the lowest priority. Otherwise, if the aforementioned option is
		 * enabled, the torrent job is treated as if there is 1 peer in the swarm.
		 */
		String QUEUE_USE_SEED_PEER_RATIO = "queue.use_seed_peer_ratio";

		/**
		 * This option deletes torrents in the .torrent files directory that are not
		 * loaded into client.
		 */
		String remove_torrent_files_with_private_data = "remove_torrent_files_with_private_data";

		/**
		 * When this option is enabled, µTorrent will use an RSS feed's name as the
		 * default label for any torrent jobs added without a label from the RSS
		 * feed.
		 */
		String RSS_FEED_AS_DEFAULT_LABEL = "rss.feed_as_default_label";

		/**
		 * This option tells µTorrent to select an RSS item designated as REPACK
		 * over an item without the REPACK designation if both show up in the RSS
		 * feed.
		 */
		String RSS_SMART_REPACK_FILTER = "rss.smart_repack_filter";

		/**
		 * This option sets the length of time µTorrent should wait between each RSS
		 * feed update check. This value is interpreted in minutes, so please enter
		 * it as such. Any value below 5 will be ignored (and 5 will be used
		 * instead).
		 */
		String RSS_UPDATE_INTERVAL = "rss.update_interval";

		/**
		 * This options enables new "Offer"-torrents by Bittorrent Inc located at
		 * the top of your main view. Only new offers will be disable, and you can
		 * skip/cancel them on screen at will.  Disable it if you like not to be
		 * offered new torrents.
		 */
		String sponsored_torrent_offer_enabled = "sponsored_torrent_offer_enabled";

		/**
		 * This option saves the .torrent file as INFOHASH.torrent (e.g.
		 * ABCDEF1234567890ABCD.torrent).
		 */
		String store_torr_infohash = "store_torr_infohash";

		/**
		 * This option sets the maximum size of the failover set, expressed as
		 * percentage of the total number of peers.
		 */
		String STREAMING_FAILOVER_SET_PERCENTAGE = "streaming.failover_set_percentage";

		/**
		 * This option sets minimum number of pieces to hold in the streaming
		 * buffer.
		 */
		String STREAMING_MIN_BUFFER_PIECE = "streaming.min_buffer_piece";

		/**
		 * Thie option ensures download rate is faster in % than the calculated rate
		 * needed.
		 */
		String STREAMING_SAFETY_FACTOR = "streaming.safety_factor";

		/**
		 * This option enables several workarounds for bugs found in Wine (like
		 * list-view flickering, or improper display of files list-view in Add New
		 * Torrent dialog). This option has no effect on Windows. For changes to
		 * this option to take effect, you must restart µTorrent. This applies only
		 * if we're on Wine.
		 */
		String SYS_ENABLE_WINE_HACKS = "sys.enable_wine_hacks";

		/**
		 * This option allows device/service to pair with the client and control it
		 * via WebUI.
		 */
		String WEBUI_ALLOW_PAIRING = "webui.allow_pairing";

		/**
		 * This option enables the token authentication system for the Web UI, which
		 * is a method for preventing cross-site request forgery attacks that use
		 * the authenticated browser session to issue commands to µTorrent. This
		 * option breaks backwards compatibility with applications that are unaware
		 * of the token system.
		 */
		String WEBUI_TOKEN_AUTH = "webui.token_auth";

		/**
		 * If token_auth = true, then this token_auth_filter changes the scope of
		 * token authentication: 0 means that it applies to all connections, 1 means
		 * that it applies only to remote connections (i.e. not localhost).
		 */
		String WEBUI_TOKEN_AUTH_FILTER = "webui.token_auth_filter";
	}
}
