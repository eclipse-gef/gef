/********************************************************************************************
 * Copyright (c) 2015, 2019 itemis AG and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tamas Miklossy (itemis AG) - Exporting *.dot files in different formats (bug #446647)
 *
 *********************************************************************************************/
package org.eclipse.gef.dot.internal.ui.preferences;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class DotExportRadioGroupFieldEditor extends RadioGroupFieldEditor {

	/**
	 * List of radio button entries of the form [label,value].
	 */
	private String[][] labelsAndValues;

	/**
	 * Number of columns into which to arrange the radio buttons.
	 */
	private int numColumns;

	/**
	 * Indent used for the first column of the radio button matrix.
	 */
	private int indent = HORIZONTAL_GAP;

	/**
	 * The current value, or <code>null</code> if none.
	 */
	private String value;

	/**
	 * The box of radio buttons, or <code>null</code> if none (before creation
	 * and after disposal).
	 */
	private Composite radioBox;

	/**
	 * The radio buttons, or <code>null</code> if none (before creation and
	 * after disposal).
	 */
	private Button[] radioButtons;

	/**
	 * Whether to use a Group control.
	 */
	private boolean useGroup;

	/**
	 * Parent Composite of the fieldEditor
	 */
	private Composite parent;

	private Label dotExportHintLabel;

	private String dotExportHintText;

	private BooleanFieldEditor2 openExportedFileBooleanFieldEditor;

	/**
	 * Creates a new radio group field editor
	 */
	protected DotExportRadioGroupFieldEditor() {
	}

	public DotExportRadioGroupFieldEditor(String name, String labelText,
			String dotExportHintText, int numColumns,
			String[][] labelsAndValues, Composite parent) {
		this(name, labelText, dotExportHintText, numColumns, labelsAndValues,
				parent, false);
	}

	public DotExportRadioGroupFieldEditor(String name, String labelText,
			String dotExportHintText, int numColumns,
			String[][] labelsAndValues, Composite parent, boolean useGroup) {
		init(name, labelText);
		if (labelsAndValues != null) {
			Assert.isTrue(checkArray(labelsAndValues));
		}
		this.labelsAndValues = labelsAndValues;
		this.numColumns = numColumns;
		this.useGroup = useGroup;
		this.parent = parent;
		this.dotExportHintText = dotExportHintText;
		createControl(parent);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		if (control != null) {
			((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		}
		((GridData) radioBox.getLayoutData()).horizontalSpan = numColumns;
	}

	private boolean checkArray(String[][] table) {
		if (table == null) {
			return false;
		}
		for (int i = 0; i < table.length; i++) {
			String[] array = table[i];
			if (array == null || array.length != 2) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		if (useGroup) {
			Control control = getRadioBoxControl(parent);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			control.setLayoutData(gd);
		} else {
			Control control = getLabelControl(parent);
			GridData gd = new GridData();
			gd.horizontalSpan = numColumns;
			control.setLayoutData(gd);
			control = getRadioBoxControl(parent);
			gd = new GridData();
			gd.horizontalSpan = numColumns;
			gd.horizontalIndent = indent;
			control.setLayoutData(gd);
		}

	}

	@Override
	protected void doLoad() {
		updateValue(getPreferenceStore().getString(getPreferenceName()));
	}

	@Override
	protected void doLoadDefault() {
		// do nothing, since the DotExportRadioGroupFieldEditor has no default
		// value
	}

	@Override
	protected void doStore() {
		if (value == null) {
			getPreferenceStore().setToDefault(getPreferenceName());
			return;
		}

		getPreferenceStore().setValue(getPreferenceName(), value);
	}

	@Override
	public Composite getRadioBoxControl(Composite parent) {
		if (radioBox == null) {

			Font font = parent.getFont();

			if (useGroup) {
				Group group = new Group(parent, SWT.NONE);
				group.setFont(font);
				String text = getLabelText();
				if (text != null) {
					group.setText(text);
				}
				radioBox = group;
				GridLayout layout = new GridLayout();
				layout.horizontalSpacing = HORIZONTAL_GAP;
				layout.numColumns = numColumns;
				radioBox.setLayout(layout);
			} else {
				radioBox = new Composite(parent, SWT.NONE);
				GridLayout layout = new GridLayout();
				layout.marginWidth = 0;
				layout.marginHeight = 0;
				layout.horizontalSpacing = HORIZONTAL_GAP;
				layout.numColumns = numColumns;
				radioBox.setLayout(layout);
				radioBox.setFont(font);
			}

			dotExportHintLabel = new Label(parent, SWT.NONE);
			dotExportHintLabel.setText(dotExportHintText);
			FontData fontData = dotExportHintLabel.getFont().getFontData()[0];
			Font boldFont = new Font(Display.getCurrent(), new FontData(
					fontData.getName(), fontData.getHeight() - 1, SWT.BOLD));
			dotExportHintLabel.setFont(boldFont);
			GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			dotExportHintLabel.setLayoutData(gridData);

			if (labelsAndValues != null) {
				radioButtons = new Button[labelsAndValues.length];
				for (int i = 0; i < labelsAndValues.length; i++) {
					Button radio = new Button(radioBox, SWT.RADIO | SWT.LEFT);
					radioButtons[i] = radio;
					String[] labelAndValue = labelsAndValues[i];
					radio.setText(labelAndValue[0]);
					radio.setData(labelAndValue[1]);
					radio.setFont(font);
					radio.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent event) {
							String oldValue = value;
							value = (String) event.widget.getData();
							setPresentsDefaultValue(false);
							fireValueChanged(VALUE, oldValue, value);
						}
					});
				}
				hideDotExportHintLabel();
			}

			radioBox.addDisposeListener(new DisposeListener() {

				@Override
				public void widgetDisposed(DisposeEvent event) {
					radioBox = null;
					radioButtons = null;
					boldFont.dispose();
				}

			});
		} else {
			checkParent(radioBox, parent);
		}
		return radioBox;
	}

	@Override
	public void setIndent(int indent) {
		if (indent < 0) {
			this.indent = 0;
		} else {
			this.indent = indent;
		}
	}

	private void updateValue(String selectedValue) {
		this.value = selectedValue;
		if (radioButtons == null) {
			return;
		}

		if (this.value != null) {
			boolean found = false;
			for (int i = 0; i < radioButtons.length; i++) {
				Button radio = radioButtons[i];
				boolean selection = false;
				if (((String) radio.getData()).equals(this.value)) {
					selection = true;
					found = true;
				}
				radio.setSelection(selection);
			}
			if (found) {
				return;
			}
		}

		// We weren't able to find the value. So we select the first
		// radio button as a default.
		if (radioButtons.length > 0) {
			radioButtons[0].setSelection(true);
			this.value = (String) radioButtons[0].getData();
		}
		return;
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		if (!useGroup) {
			super.setEnabled(enabled, parent);
		}
		for (int i = 0; i < radioButtons.length; i++) {
			radioButtons[i].setEnabled(enabled);
		}

	}

	public void update(final String[][] newLabelsAndValues) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				clear();

				labelsAndValues = newLabelsAndValues;
				if (radioBox == null) {
					if (parent.isDisposed()) {
						return;
					}
					if (useGroup) {
						Group group = new Group(parent, SWT.NONE);
						String text = getLabelText();
						if (text != null) {
							group.setText(text);
						}
						radioBox = group;
						GridLayout layout = new GridLayout();
						layout.horizontalSpacing = HORIZONTAL_GAP;
						layout.numColumns = numColumns;
						radioBox.setLayout(layout);
					} else {
						radioBox = new Composite(parent, SWT.NONE);
						GridLayout layout = new GridLayout();
						layout.marginWidth = 0;
						layout.marginHeight = 0;
						layout.horizontalSpacing = HORIZONTAL_GAP;
						layout.numColumns = numColumns;
						radioBox.setLayout(layout);
					}
				}

				if (labelsAndValues != null) {
					radioButtons = new Button[labelsAndValues.length];
					for (int i = 0; i < labelsAndValues.length; i++) {
						Button radio = new Button(radioBox,
								SWT.RADIO | SWT.LEFT);
						radioButtons[i] = radio;
						String[] labelAndValue = labelsAndValues[i];
						radio.setText(labelAndValue[0]);
						radio.setData(labelAndValue[1]);
						radio.setFont(parent.getFont());
						radio.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent event) {
								String oldValue = value;
								value = (String) event.widget.getData();
								setPresentsDefaultValue(false);
								fireValueChanged(VALUE, oldValue, value);
							}
						});
					}
					load();
					hideDotExportHintLabel();
					showOpenExportedFileBooleanFieldEditor();
					parent.layout();
				}
			}

		});
	}

	protected void hideDotExportHintLabel() {
		// hide the dot export hint label
		dotExportHintLabel.setVisible(false);
		((GridData) dotExportHintLabel.getLayoutData()).exclude = true;
	}

	protected void showDotExportHintLabel() {
		// show the dot export hint label
		dotExportHintLabel.setVisible(true);
		((GridData) dotExportHintLabel.getLayoutData()).exclude = false;
	}

	protected void hideOpenExportedFileBooleanFieldEditor() {
		// hide the open exported file automatically boolean field editor
		Button checkBox = openExportedFileBooleanFieldEditor
				.getChangeControl(parent);
		checkBox.setVisible(false);
		((GridData) checkBox.getLayoutData()).exclude = true;
	}

	protected void showOpenExportedFileBooleanFieldEditor() {
		// show the open exported file automatically boolean field editor
		Button checkBox = openExportedFileBooleanFieldEditor
				.getChangeControl(parent);
		checkBox.setVisible(true);
		((GridData) checkBox.getLayoutData()).exclude = false;
	}

	protected void addOpenExportedFileBooleanFieldEditor(
			BooleanFieldEditor2 openExportedFileBooleanFieldEditor) {
		this.openExportedFileBooleanFieldEditor = openExportedFileBooleanFieldEditor;
	}

	public void clear() {
		this.labelsAndValues = null;
		if (radioButtons != null) {
			for (Button radioButton : radioButtons) {
				radioButton.dispose();
			}

			showDotExportHintLabel();
			hideOpenExportedFileBooleanFieldEditor();
			// do synchronous layout
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					parent.layout();
				}
			});
		}
	}

}
