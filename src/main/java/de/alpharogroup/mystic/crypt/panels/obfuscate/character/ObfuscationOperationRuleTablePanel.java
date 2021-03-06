/**
 * The MIT License
 *
 * Copyright (C) 2015 Asterios Raptis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.alpharogroup.mystic.crypt.panels.obfuscate.character;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.apache.commons.codec.DecoderException;

import com.thoughtworks.xstream.XStream;

import de.alpharogroup.collections.list.ListFactory;
import de.alpharogroup.collections.map.MapFactory;
import de.alpharogroup.collections.pairs.KeyValuePair;
import de.alpharogroup.crypto.hex.HexExtensions;
import de.alpharogroup.crypto.obfuscation.rule.ObfuscationOperationRule;
import de.alpharogroup.file.read.ReadFileExtensions;
import de.alpharogroup.file.write.WriteFileQuietlyExtensions;
import de.alpharogroup.model.BaseModel;
import de.alpharogroup.model.api.Model;
import de.alpharogroup.swing.GenericJTable;
import de.alpharogroup.swing.base.BasePanel;
import de.alpharogroup.swing.renderer.TableCellButtonRenderer;
import de.alpharogroup.swing.table.editor.TableCellButtonEditor;
import de.alpharogroup.xml.ObjectToXmlExtensions;
import de.alpharogroup.xml.XmlToObjectExtensions;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


@Getter
@Slf4j
public class ObfuscationOperationRuleTablePanel extends BasePanel<ObfuscationOperationModelBean>
{

	private static final long serialVersionUID = 1L;
	private JLabel lblKeyRules;
	private JScrollPane scpKeyRules;
	private GenericJTable<KeyValuePair<Character, ObfuscationOperationRule<Character, Character>>> tblKeyRules;
	private javax.swing.JButton btnExport;
	private javax.swing.JButton btnImport;
	private JFileChooser fileChooser;
	private XStream xStream;
	private Map<String, Class<?>> aliases;
	
	{
		xStream = new XStream();
		XStream.setupDefaultSecurity(xStream);
		xStream.allowTypesByWildcard(new String[] {
			    "de.alpharogroup.**"
			});
		aliases = MapFactory.newLinkedHashMap();
		aliases.put("KeyValuePair", KeyValuePair.class);
		aliases.put("ObfuscationOperationRule", ObfuscationOperationRule.class);
	}

	public ObfuscationOperationRuleTablePanel()
	{
		this(BaseModel.<ObfuscationOperationModelBean> of(ObfuscationOperationModelBean.builder().build()));

	}

	public ObfuscationOperationRuleTablePanel(@NonNull final Model<ObfuscationOperationModelBean> model)
	{
		super(model);
	}

	protected void onExport(final ActionEvent actionEvent)
	{
		List<KeyValuePair<Character, ObfuscationOperationRule<Character, Character>>> data = getModelObject()
			.getTableModel().getData();
		
		final int returnVal = fileChooser.showSaveDialog(ObfuscationOperationRuleTablePanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			final File obfuscationRules = fileChooser.getSelectedFile();
			String xmlString = ObjectToXmlExtensions.toXmlWithXStream(xStream, data, aliases);
			final String hexXmlString = HexExtensions.encodeHex(xmlString, Charset.forName("UTF-8"), true);
			WriteFileQuietlyExtensions.writeStringToFile(obfuscationRules, hexXmlString, "UTF-8");			
		}
	}

	protected void onImport(final ActionEvent actionEvent)
	{
		final int returnVal = fileChooser.showOpenDialog(ObfuscationOperationRuleTablePanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			final File obfuscationRules = fileChooser.getSelectedFile();
			try
			{
				final String hexXmlString = ReadFileExtensions.readFromFile(obfuscationRules);
				String xmlString = HexExtensions.decodeHex(hexXmlString);				
				
				List<KeyValuePair<Character, ObfuscationOperationRule<Character, Character>>> data = XmlToObjectExtensions.toObjectWithXStream(xStream, xmlString, aliases);
				
				getModelObject().getTableModel().setData(data);
				getModelObject().getTableModel().fireTableDataChanged();
			}
			catch (final IOException e)
			{
				log.error("IOException ", e);
			}
			catch (DecoderException e)
			{
				log.error("DecoderException ", e);
			}
		}
	}

	@Override
	protected void onInitializeComponents()
	{
		super.onInitializeComponents();

		fileChooser = new JFileChooser();

		scpKeyRules = new JScrollPane();
		tblKeyRules = new GenericJTable<>(getModelObject().getTableModel());
		

		final TableColumn editValueColumn = tblKeyRules.getColumn("Edit");

		editValueColumn.setCellRenderer(new TableCellButtonRenderer(null, null)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value,
				final boolean isSelected, final boolean hasFocus, final int row, final int column)
			{
				if (isSelected)
				{
					setForeground(newSelectionForeground(table));
					setBackground(newSelectionBackround(table));
				}
				else
				{
					setForeground(newForeground(table));
					setBackground(newBackround(table));
				}
				final String text = "Edit";
				setText(text);
				return this;
			}
		});
		editValueColumn.setCellEditor(new TableCellButtonEditor(new JCheckBox())
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public Object getCellEditorValue()
			{
				ObfuscationOperationRule<Character, Character> sel = (ObfuscationOperationRule<Character, Character>)this.getValue();
				KeyValuePair<Character, ObfuscationOperationRule<Character, Character>> selected = KeyValuePair.<Character, ObfuscationOperationRule<Character, Character>>builder()
					.key(sel.getCharacter())
					.value(sel)
					.build();
				int indexOfRow = getModelObject().getTableModel().getData().indexOf(selected);
				List<KeyValuePair<Character, ObfuscationOperationRule<Character, Character>>> data = getModelObject().getTableModel().getData();
				List<KeyValuePair<Character, ObfuscationOperationRule<Character, Character>>> newDataList = ListFactory.newArrayList(data);
				newDataList.remove(indexOfRow);
				getModelObject().getTableModel().clear();
				getModelObject().getTableModel().setData(newDataList);
				onEditObfuscationOperationRule(sel);
				tblKeyRules.setModel(getModelObject().getTableModel());
				revalidate();
				
				final String text = "Edit";
				return text;

			}
			
			@Override
			public Component getTableCellEditorComponent(final JTable table, final Object value,
				final boolean isSelected, final int row, final int column)
			{
				setRow(row);
				setColumn(column);
				setValue(value);
				if (isSelected)
				{
					getButton().setForeground(table.getSelectionForeground());
					getButton().setBackground(table.getSelectionBackground());
				}
				else
				{
					getButton().setForeground(table.getForeground());
					getButton().setBackground(table.getBackground());
				}
				final String text = "Edit";
				getButton().setText(text);
				setClicked(true);
				return getButton();
			}
		});
		
		
		
		
		lblKeyRules = new JLabel();

		btnImport = new javax.swing.JButton();
		btnExport = new javax.swing.JButton();

		scpKeyRules.setViewportView(tblKeyRules);

		lblKeyRules.setText("Table of key rules for obfuscate");

		btnImport.setText("Import");
		btnExport.setText("Export");

		btnImport.addActionListener(actionEvent -> onImport(actionEvent));
		btnExport.addActionListener(actionEvent -> onExport(actionEvent));
	}
	
	protected void onEditObfuscationOperationRule(
		ObfuscationOperationRule<Character, Character> selected)
	{
		// TODO Auto-generated method stub
		
	}

	protected void onInitializeGroupLayout()
	{
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout
			.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(lblKeyRules, javax.swing.GroupLayout.PREFERRED_SIZE, 280,
						javax.swing.GroupLayout.PREFERRED_SIZE)
					.addComponent(scpKeyRules, javax.swing.GroupLayout.PREFERRED_SIZE, 1188,
						javax.swing.GroupLayout.PREFERRED_SIZE)
					.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
							.addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, 128,
								javax.swing.GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
							.addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 128,
								javax.swing.GroupLayout.PREFERRED_SIZE)
							.addGap(3, 3, 3)))
				.addContainerGap(31, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout
			.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup().addGap(29, 29, 29).addComponent(lblKeyRules)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scpKeyRules, javax.swing.GroupLayout.PREFERRED_SIZE, 217,
					javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
					.addComponent(btnImport).addComponent(btnExport))
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	}

	@Override
	protected void onInitializeLayout()
	{
		super.onInitializeLayout();
		onInitializeGroupLayout();
	}

}
