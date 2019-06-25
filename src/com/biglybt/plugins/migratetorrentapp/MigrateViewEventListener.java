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

package com.biglybt.plugins.migratetorrentapp;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.biglybt.pifimpl.local.ui.config.ParameterImpl;
import com.biglybt.pifimpl.local.ui.config.ParameterImplListener;
import com.biglybt.plugins.migratetorrentapp.utorrent.ConfigModel_uTorrent;
import com.biglybt.plugins.migratetorrentapp.utorrent.Importer_uTorrent;
import com.biglybt.plugins.migratetorrentapp.utorrent.TagToAddInfo;
import com.biglybt.plugins.migratetorrentapp.utorrent.TorrentImportInfo;
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
import com.biglybt.pif.ui.model.BasicPluginConfigModel;

public class MigrateViewEventListener
	implements UISWTViewEventListener
{
	private UISWTView swtView;

	private Composite parent;

	private PluginInterface pi;

	private ConfigModel_uTorrent configModelInfo;

	private Text resultTextArea;

	private ScrolledComposite sc;

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
	}

	private void initialize(Composite parent) {
		this.parent = parent;

		sc = new ScrolledComposite(parent, SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.addListener(SWT.Resize, event -> recalcScrolledComposite());

		GridData gridData = new GridData(GridData.FILL_BOTH);
		sc.setLayoutData(gridData);

		Composite composite = new Composite(sc, SWT.NULL);

		sc.setContent(composite);

		gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 20;
		composite.setLayout(gridLayout);

		Label label = new Label(composite, SWT.BORDER);
		label.setText("ALPHA RELEASE -- Only Analyzes.  Does not migrate or modify anything.");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		FontUtils.setFontHeight(label, 12, SWT.BOLD);
		label.setLayoutData(gridData);

		// Hack the config model into our composite. muah ha ha!

		BasicPluginConfigModel ourModel = configModelInfo.getConfigModel(
				pi.getUIManager());

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

		Composite cResultsArea = new Composite(composite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		cResultsArea.setLayoutData(gridData);
		cResultsArea.setLayout(new GridLayout());

		resultTextArea = new Text(cResultsArea,
				SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
		gridData = new GridData(GridData.FILL_BOTH);
		resultTextArea.setLayoutData(gridData);

		configModelInfo.addListener(this::analysisComplete);
	}

	private void recalcScrolledComposite() {
		int width = sc.getClientArea().width;
		Point size = parent.computeSize(width, SWT.DEFAULT);
		sc.setMinSize(size);
	}

	private void analysisComplete(Importer_uTorrent importer) {
		StringBuilder sb = buildAnalysisResults(importer, true);
		Utils.execSWTThread(() -> {
			resultTextArea.setText(sb.toString());
			recalcScrolledComposite();
		});
	}

	private StringBuilder buildAnalysisResults(Importer_uTorrent importer,
			boolean showPrivate) {
		StringBuilder sb = new StringBuilder();
		String nl = "\n╏ ";
		String s;
		sb.append("┎╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌").append(nl);
		boolean first = true;
		for (TorrentImportInfo importInfo : importer.listTorrentsToImport) {
			if (first) {
				first = false;
			} else {
				sb.append(
						"\n┠╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌").append(
								nl).append(nl);
			}
			s = importInfo.toDebugString(showPrivate).replaceAll("\n", nl);
			sb.append(s);
		}
		sb.append("\n└╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌\n\n");

		sb.append("┎╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌").append(nl);
		s = importer.settingsImportInfo.toDebugString(showPrivate).replaceAll("\n",
				nl);
		sb.append(s);

		sb.append("\n└╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌\n\n");

		sb.append("Tags\n");
		sb.append("----\n");
		for (TagToAddInfo value : importer.mapTagsToAdd.values()) {
			sb.append(value.toDebugString()).append("\n");
		}
		return sb;
	}

	public Composite getComposite() {
		return parent;
	}
}
