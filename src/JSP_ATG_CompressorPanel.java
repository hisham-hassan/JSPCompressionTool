/*
*    Copyright (c) 2010 Hesham Hassan (hh_242@hotmail.com)
*
*    This file is part of JSPCompression Tool V2.0
*
*    JSPCompression Tool V2.0 is free software: you can redistribute it 
*    and/or modify it under the terms of the GNU General Public License 
*    as published by the Free Software Foundation, either version 3 of 
*    the License, or any later version.
*
*    JSPCompression Tool V2.0 is distributed in the hope that it will 
*    be useful, but WITHOUT ANY WARRANTY; without even the implied 
*    warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*    See the GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with source code of JSPCompression Tool V2.0.
*    If not, see <http://www.gnu.org/licenses/>.
*/

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;

public class JSP_ATG_CompressorPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JButton chooseButton;
	private JButton compressButton;
	private JTextField directoryPathTextField;
	private JTextArea infoTextArea;
	private JFileChooser chooser;
	private String choosertitle = "Please select the JSP directory";
	JCheckBox truncateSpacesCheckBox;
	JCheckBox removeServerSideCommentsCheckBox;
	JCheckBox removeHTMLCommentsCheckBox;
	JCheckBox compressJSCheckBox;
	JScrollPane scrollingResult;

	public JSP_ATG_CompressorPanel() 
	{
		//setLayout(new GridLayout(3,1));

		JPanel choosePanel = new JPanel();
		choosePanel.setLayout(new GridLayout(1, 2));

		JPanel checkBoxsPanel = new JPanel();
		checkBoxsPanel.setLayout(new GridLayout(2, 2));

		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(1, 1));
		
		JPanel compressPanel = new JPanel();
		compressPanel.setLayout(new GridLayout(1, 1));

		chooseButton = new JButton("Choose Directory");
		chooseButton.addActionListener(new FileChooserListener());
		chooseButton.setLocation(0,0);

		truncateSpacesCheckBox = new JCheckBox("Truncate Spaces");
		truncateSpacesCheckBox.setSelected(true);
		truncateSpacesCheckBox.setEnabled(false);
		removeServerSideCommentsCheckBox = new JCheckBox("Remove Server Side Comments");
		removeServerSideCommentsCheckBox.setSelected(true);
		removeHTMLCommentsCheckBox = new JCheckBox("Remove HTML Comments");
		removeHTMLCommentsCheckBox.setSelected(true);
		compressJSCheckBox = new JCheckBox("Compress Java Script");
		compressJSCheckBox.setSelected(true);
		
		directoryPathTextField = new JTextField(15);
		directoryPathTextField.setEditable(false);
		directoryPathTextField.setLocation(50,0);
		

		infoTextArea = new JTextArea(6,31);
		infoTextArea.setEditable(false);
		//infoTextArea.setLineWrap(true);
		//infoTextArea.setWrapStyleWord(true);
		scrollingResult = new JScrollPane(infoTextArea);
		scrollingResult.setLocation(0,50);
		
		compressButton = new JButton("Compress");
		compressButton.addActionListener(new CompressButtonListener());
		compressButton.setLocation(50,380);
				
		choosePanel.add(chooseButton);
		choosePanel.add(directoryPathTextField);
        checkBoxsPanel.add(removeServerSideCommentsCheckBox);
        checkBoxsPanel.add(removeHTMLCommentsCheckBox);
		checkBoxsPanel.add(truncateSpacesCheckBox);
		checkBoxsPanel.add(compressJSCheckBox);
		infoPanel.add(scrollingResult);
		compressPanel.add(compressButton);
		
		add(choosePanel);
		add(checkBoxsPanel);
		add(infoPanel);
		add(compressPanel);
	}

	public Dimension getPreferredSize() 
	{
		return new Dimension(450, 290);
	}
	
	class FileChooserListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			if(isRunning)
			{
				JOptionPane.showMessageDialog(JSP_ATG_CompressorPanel.this,"Operation is in progress, please wait");
				return;
			}
			chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle(choosertitle);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(JSP_ATG_CompressorPanel.this) == JFileChooser.APPROVE_OPTION) 
			{
				infoTextArea.append("Current directory: " + chooser.getSelectedFile()+"\n");
				directoryPathTextField.setText(""+chooser.getSelectedFile());
			} 
			else 
			{
				if(directoryPathTextField.getText().equals(""))
					infoTextArea.append("No Selection\n");
			}
		}
	}
	
	class CompressButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			if(isRunning)
			{
				JOptionPane.showMessageDialog(JSP_ATG_CompressorPanel.this,"Operation is in progress, please wait");
				return;
			}
			String directoryPath = directoryPathTextField.getText();
			if (directoryPath!=null && directoryPath.length()>0)
			{
				Thread t = new Thread(new Runnable() 
				{
					public void run() 
					{
						isRunning = true;
						refactorAllFiles("" + chooser.getSelectedFile());
				  		isRunning = false;
					}
				});
				t.start();
			}
			else
			{
				infoTextArea.append("Please select a valid directory\n");
			}
		}
	}
	
	private void refactorAllFiles(String directoryPath)
	{
		infoTextArea.append("((Refactoring Started))\nPlease wait . . .\n");
		Vector <String>filesVector = FileOperations.getAllSubFiles(directoryPath);
	  	for(int j=0;j<filesVector.size();j++)
	  	{
  			String fileName = filesVector.get(j);
  			infoTextArea.append("Compressing:"+ fileName+"\n");
  			
  			/*
  			 *the following code is added between try and catch because it might produce a classCastException if 
  			 *the scrollingResult.getVerticalScrollBar().getMaximum() returned a wrong value out of the textArea bounds. 
  			 */
  			try{
  			    scrollingResult.getVerticalScrollBar().setValue(scrollingResult.getVerticalScrollBar().getMaximum());
  			}
  			catch(ClassCastException e){}
  			
  			CompressJSP.refactorFile(fileName, truncateSpacesCheckBox.isSelected(), removeServerSideCommentsCheckBox.isSelected(), removeHTMLCommentsCheckBox.isSelected(), compressJSCheckBox.isSelected());
  		}
	  	infoTextArea.append("Refactoring finished successfully\n");
	  	scrollingResult.getVerticalScrollBar().setValue(scrollingResult.getVerticalScrollBar().getMaximum());
	}
	
	private boolean isRunning = false;
}

