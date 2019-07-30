# BiglyBT Plugin - Migrate Torrent App

```
This plugin is in pre-release.  It's working but not widely tested.
```

This BiglyBT plugin migrates uTorrent and BitTorrent client configurations and torrents to BiglyBT.

You will be able to review the changes before applying them.

## Features

### uTorrent/BitTorrent Client Migration

#### Torrents
1. Partial and Full Block completion status
1. File renames and relocations
1. File priorities
1. Skipped files sharing pieces with non-skipped (~uTorrentPartFile_`[0-9A-Z]+`.dat parsing)
1. Statistics (bytes downloaded, uploaded, etc)
1. Speed Limits
1. Labels
   
#### Configuration
1. Directories (new downloads, move on complete, .torrent location, etc)
1. Port settings
1. Proxy settings
1. Bandwidth Limits
1. Protocol settings (DHT, uTP, PEX, etc)
1. Queue settings
1. Scheduler
1. Auto-Labels and Label-Directory Mappings

#### Migration Options
1. Scan additional directories for .torrent files
1. Find moved files (handy if you organized your files outside of the client)
1. Remap data directories (handy if you renamed or moved your base data directory)
1. Cross platform migration

#### Features Not Supported
* Altruistic Mode. Depending on your needs the Share Ratio Maximizer plugin might be a good replacement.
* uT's Advanced->Run Program (global and per-torrent) migration not supported.  Equivilant functionality can be obtained using the Command Runner plugin.
* Automatic deleting of imported .torrent files
* uTorrent scheduler option to disable DHT when in 'Turn off' state
* "Confirm exit if critical seeder" option

#### Incompatible Features
The following features aren't migrated, however, there are alternative solutions:
* Adjusting or stopping torrents based on Seeding Goals.  You will most likely need to rebuild your goals using First Priority Rules, Ignore Rules, Tagging logic, and/or the AutoPilot plugin.
* 'Append the torrent's label' option on 'Move completed downloads'.  BiglyBT can handle this feature by setting the "Move On Complete" directory on a specific Tag.
* Transfer Cap and daily bandwidth usage stats.
  To manually create Transfer Caps in BiglyBT, see [Speed Limit Scheduler: Network Limits](https://github.com/BiglySoftware/BiglyBT/wiki/Speed-Limit-Scheduler#Network_Limits) wiki page.
  For example, if you want a monthly cap of 200G, in Tools->Speed Limits->Schedule and Settings, you'd need the following lines:
    ```
    net_limit monthly total=200G
    ```
  Make sure your stats start day is set to the right day in Tools->Options->Statistics->Long Term

#### Skipped
* Disk Cache settings
* Column setup
* RSS Feeds
* Most uT settings listed in Preferences->Advanced are not migrated yet. The entries you changed manually will be listed during the analysis phase.
* WebUI and uT Remote settings.  BiglyBT has a Transmission compatible remote rpc that can be accessed via Tools->Remote Pairing, or by installing the xmwebui plugin.
* Paired Devices

#### Migration Notes

* "Enable DHT for new torrents" flag in uT will also change accepting peer sources from any plugin (including P2P)
* Moving .torrent files on completion also required moving data files on completion (Plugin limitation, not a BiglyBT one. In BiglyBT, this can be done with Tags)
