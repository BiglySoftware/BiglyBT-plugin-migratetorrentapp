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

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import com.biglybt.core.internat.MessageText;
import com.biglybt.core.util.FileUtil;
import com.biglybt.pifimpl.local.ui.config.ParameterImpl;
import com.biglybt.pifimpl.local.ui.config.ParameterImplListener;
import com.biglybt.plugins.migratetorrentapp.utorrent.ConfigModel_uTorrent;
import com.biglybt.plugins.migratetorrentapp.utorrent.ConfigModel_uTorrent.MigrateListener;
import com.biglybt.plugins.migratetorrentapp.utorrent.Importer_uTorrent;
import com.biglybt.plugins.migratetorrentapp.utorrent.TagToAddInfo;
import com.biglybt.plugins.migratetorrentapp.utorrent.TorrentImportInfo;
import com.biglybt.ui.UIFunctionsManager;
import com.biglybt.ui.swt.Messages;
import com.biglybt.ui.swt.Utils;
import com.biglybt.ui.swt.config.BaseSwtParameter;
import com.biglybt.ui.swt.pif.UISWTView;
import com.biglybt.ui.swt.pif.UISWTViewEvent;
import com.biglybt.ui.swt.pif.UISWTViewEventListener;
import com.biglybt.ui.swt.pifimpl.MultiParameterImplListenerSWT;
import com.biglybt.ui.swt.utils.FontUtils;
import com.biglybt.ui.swt.views.ConfigView;

import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.ui.config.Parameter;
import com.biglybt.pif.ui.config.ParameterListener;
import com.biglybt.pif.ui.model.BasicPluginConfigModel;

import static com.biglybt.plugins.migratetorrentapp.Utils.NL;
import static com.biglybt.plugins.migratetorrentapp.Utils.hidePrivate;

public class MigrateViewEventListener
	implements UISWTViewEventListener, MigrateListener
{
	private UISWTView swtView;

	private Composite parent;

	private PluginInterface pi;

	private ConfigModel_uTorrent configModelInfo;

	private StyledText resultTextArea;

	private ScrolledComposite sc;

	private Composite cResultsArea;

	private boolean showOnlyWarningTorrents = true;

	private boolean goingToRecalcSC;

	private Label lblStatus;

	private ParameterListener paramSAOListener;

	@Override
	public boolean eventOccurred(UISWTViewEvent event) {
		switch (event.getType()) {
			case UISWTViewEvent.TYPE_CREATE:
				swtView = (UISWTView) event.getData();
				pi = swtView.getPluginInterface();
				swtView.setTitle("Migrate");
				break;

			case UISWTViewEvent.TYPE_DESTROY:
				delete();
				break;

			case UISWTViewEvent.TYPE_INITIALIZE:

				initialize((Composite) event.getData());
				break;

			case UISWTViewEvent.TYPE_DATASOURCE_CHANGED:
				configModelInfo = (ConfigModel_uTorrent) event.getData();
				break;

			case UISWTViewEvent.TYPE_LANGUAGEUPDATE:
				Messages.updateLanguageForControl(getComposite());
				if (swtView != null) {
					swtView.setTitle("Migrate");
				}
				break;
		}

		return true;
	}

	private void delete() {
		if (configModelInfo != null) {
			configModelInfo.paramShowAdditionalOptions.removeListener(
					paramSAOListener);
			configModelInfo.removeListener(this);
		}
	}

	private void initialize(Composite parent) {
		this.parent = parent;

		sc = new ScrolledComposite(parent, SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.addListener(SWT.Resize, event -> {
			if (goingToRecalcSC) {
				return;
			}
			goingToRecalcSC = true;
			Utils.execSWTThreadLater(10, () -> recalcScrolledComposite());
		});

		GridData gridData = new GridData(GridData.FILL_BOTH);
		sc.setLayoutData(gridData);

		Composite composite = new Composite(sc, SWT.NULL);

		sc.setContent(composite);

		gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 10;
		composite.setLayout(gridLayout);

		Label label = new Label(composite, SWT.BORDER);
		label.setText("ALPHA RELEASE");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		FontUtils.setFontHeight(label, 12, SWT.BOLD);
		label.setLayoutData(gridData);

		// Hack the config model into our composite. muah ha ha!

		BasicPluginConfigModel ourModel = configModelInfo.getConfigModel(
				pi.getUIManager(), MigrateTorrentAppUISWT.getSingleton());

		Parameter[] parameters = ourModel.getParameters();

		try {

			Map<ParameterImpl, BaseSwtParameter> map = new HashMap<>();

			Method mBuildScreen = ConfigView.class.getDeclaredMethod("buildScreen",
					Composite.class, Parameter[].class, Map.class,
					ParameterImplListener.class);
			mBuildScreen.setAccessible(true);
			mBuildScreen.invoke(null, composite, parameters, map,
					new MultiParameterImplListenerSWT(map));
		} catch (Exception e) {
			e.printStackTrace();
		}

		////

		lblStatus = new Label(composite, SWT.WRAP);
		lblStatus.setLayoutData(
				Utils.getWrappableLabelGridData(2, GridData.FILL_HORIZONTAL));

		cResultsArea = new Composite(composite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		cResultsArea.setLayoutData(gridData);
		cResultsArea.setLayout(new GridLayout());

		paramSAOListener = param -> {
			recalcScrolledComposite();
		};
		configModelInfo.paramShowAdditionalOptions.addListener(paramSAOListener);

		configModelInfo.addListener(this);
	}

	private void buildResultsArea(Importer_uTorrent importer) {
		GridData gridData;

		Utils.disposeComposite(cResultsArea, false);

		Composite cButtonArea = new Composite(cResultsArea, SWT.NONE);
		gridData = new GridData();
		cButtonArea.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		cButtonArea.setLayout(gridLayout);

		Button btnSaveAnalysis = new Button(cButtonArea, SWT.PUSH);
		Messages.setLanguageText(btnSaveAnalysis, "migrateapp.button.saveAnalysis");
		Label lblCopy = new Label(cButtonArea, SWT.WRAP);
		Messages.setLanguageText(lblCopy, "migrateapp.button.saveAnalysis.info");

		btnSaveAnalysis.addListener(SWT.Selection, event -> {
			FileDialog dialog = new FileDialog(Utils.findAnyShell(), SWT.SAVE);
			dialog.setText(MessageText.getString("migrateapp.button.saveAnalysis"));
			dialog.setFilterNames(new String[] {
				"Text Files",
				"All Files (*.*)"
			});
			dialog.setFilterExtensions(new String[] {
				"*.txt",
				"*.*"
			});
			dialog.setFileName("uT_Migrate_Analysis.txt");
			String name = dialog.open();
			if (name != null) {
				StringBuilder sb = buildAnalysisResults(importer, false, false);
				File file = new File(name);
				FileUtil.writeStringAsFile(file, hidePrivate(sb.toString()));
				UIFunctionsManager.getUIFunctions().showInExplorer(file);
			}
		});

		Button btnShowOnlyWarnings = new Button(cButtonArea, SWT.CHECK);
		btnShowOnlyWarnings.setSelection(showOnlyWarningTorrents);
		Messages.setLanguageText(btnShowOnlyWarnings,
				"migrateapp.checkbox.onlyWwarnings");
		btnShowOnlyWarnings.addListener(SWT.Selection, event -> {
			showOnlyWarningTorrents = btnShowOnlyWarnings.getSelection();
			StringBuilder sb = buildAnalysisResults(importer, showOnlyWarningTorrents,
					true);
			resultTextArea.setText(sb.toString());
			recalcScrolledComposite();
		});
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		btnShowOnlyWarnings.setLayoutData(gridData);

		// Using StyledText instead of Text, because Text with focus messed up
		// scrolling with mouse wheel.
		resultTextArea = new StyledText(cResultsArea,
				SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		resultTextArea.setFont(
				com.biglybt.plugins.migratetorrentapp.swt.FontUtils.getMonospaceFont(
						resultTextArea.getDisplay(), 10));
		gridData = new GridData(GridData.FILL_BOTH);
		resultTextArea.setLayoutData(gridData);
		resultTextArea.addListener(SWT.KeyDown, event -> {
			int key = event.character;

			if (key <= 26 && key > 0) {
				key += 'a' - 1;
			}

			if (key == 'a' && event.stateMask == SWT.MOD1) {
				event.doit = false;
				resultTextArea.selectAll();
			}
		});
	}

	private void recalcScrolledComposite() {
		int width = sc.getClientArea().width;
		Point size = parent.computeSize(width, SWT.DEFAULT);
		sc.setMinSize(size);
		sc.layout(true, true);
		goingToRecalcSC = false;
	}

	@Override
	public void initMigrateListener() {
		// not needed after 2001
		Utils.execSWTThread(() -> {
			cResultsArea.forceFocus();
			Utils.disposeComposite(cResultsArea, false);
		}, false);
	}

	@Override
	public void analysisStatus(String status) {
		Utils.execSWTThread(() -> {
			if (lblStatus == null || lblStatus.isDisposed()) {
				return;
			}
			lblStatus.setText(status);
			lblStatus.requestLayout();
		});
	}

	public void analysisComplete(Importer_uTorrent importer) {
		StringBuilder sb = buildAnalysisResults(importer, showOnlyWarningTorrents,
				true);
		Utils.execSWTThread(() -> {
			buildResultsArea(importer);
			resultTextArea.setText(sb.toString());
			recalcScrolledComposite();
		});
	}

	@Override
	public void migrationComplete(String migrateLog) {
		pi.getUIManager().showTextMessage("migrateapp.results.title", null,
				migrateLog);
	}

	private StringBuilder buildAnalysisResults(Importer_uTorrent importer,
			boolean onlyWarningTorrents, boolean autoTrim) {
		StringBuilder sb = new StringBuilder();

		if (!importer.canMigrate()) {
			for (String s : importer.needsPathReplacement.keySet()) {
				sb.append("Require folder replacement for paths starting with ").append(
						s).append(NL);
				String[] strings = importer.needsPathReplacement.get(s).toArray(
						new String[0]);
				Arrays.sort(strings);
				int maxToShow = 20;
				for (int i = 0, stringsLength = strings.length; i < stringsLength
						&& i < maxToShow; i++) {
					String string = strings[i];
					sb.append("\t").append(string).append(NL);
				}
				if (strings.length > maxToShow) {
					sb.append("\t.. and ").append(strings.length - maxToShow).append(
							" others");
				}
			}
			return sb;
		}

		String nl = NL + "│ ";
		String s;


		sb.append("┌─────────────────────────────────────────────────").append(nl);
		s = importer.settingsImportInfo.toDebugString(
				showOnlyWarningTorrents).replaceAll(NL, nl);
		sb.append(s);

		sb.append(NL);
		sb.append("└──────────────────────────────────────────────────");
		sb.append(NL).append(NL);


		sb.append("┌─────────────────────────────────────────────────").append(nl);
		sb.append(importer.listTorrentsToImport.size()).append(
				" torrents found in uT. Any torrents already in BiglyBT will be skipped.").append(
						nl);
		for (TorrentImportInfo importInfo : importer.listTorrentsToImport) {
			if (onlyWarningTorrents && !importInfo.hasWarnings()) {
				continue;
			}

			boolean showFullDetails = !autoTrim
					|| sb.length() < Integer.MAX_VALUE / 2;

			sb.append(NL);
			sb.append("├─────────────────────────────────────────────────");
			sb.append(nl).append(nl);
			s = importInfo.toDebugString(showFullDetails).replaceAll(NL, nl);
			sb.append(s);
		}
		sb.append(NL);
		sb.append("└─────────────────────────────────────────────────");
		sb.append(NL).append(NL);

		sb.append("┌─────────────────────────────────────────────────").append(nl);
		sb.append("Tags").append(nl);
		sb.append("----");
		if (importer.mapTagsToAdd.size() == 0) {
			sb.append(nl).append("No labels found to import as tags");
		}
		for (TagToAddInfo value : importer.mapTagsToAdd.values()) {
			sb.append(nl);
			sb.append(value.toDebugString());
		}
		sb.append(NL);
		sb.append("└──────────────────────────────────────────────────");
		return sb;
	}

	public Composite getComposite() {
		return parent;
	}
}
