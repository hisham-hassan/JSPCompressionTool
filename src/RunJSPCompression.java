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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class RunJSPCompression 
{
	public static void main(String[] args)
	{
		if(args.length>0)
		{
			String directoryPath = args[0];
			boolean truncateSpaces=true ,removeServerSideComments=false, removeHTMLComments=false, compressJS = false;
			if(args.length>1)
			    if(args[1].equalsIgnoreCase("y"))
			        removeServerSideComments=true;
			if(args.length>2)
                if(args[2].equalsIgnoreCase("y"))
                    removeHTMLComments=true;
			if(args.length>3)
                if(args[3].equalsIgnoreCase("y"))
                    compressJS=true;
			startConsoleApplication(directoryPath,truncateSpaces,removeServerSideComments,removeHTMLComments,compressJS);
		}
		else
		{
			startGUIApplication();
		}
	}
	
	public static void startConsoleApplication(String directoryPath,boolean truncateSpaces,boolean removeServerSideComments,boolean removeHTMLComments,boolean compressJS )
	{
		System.out.println("####JSP Compression Tool V2.0####");
		System.out.println(">>>>Application Started<<<<");
		System.out.println("Compressing all JSP files in:" + directoryPath);
		
		if(directoryPath !=null && directoryPath.trim().length()>0)
		{
			System.out.println("((Refactoring Started))");
			System.out.println("Please wait . . .");
			Vector <String>filesVector = FileOperations.getAllSubFiles(directoryPath);
		  	for(int j=0;j<filesVector.size();j++)
		  	{
	  			String fileName = filesVector.get(j);
	  			System.out.println("Compressing:"+ fileName);
	  			CompressJSP.refactorFile(fileName,truncateSpaces,removeServerSideComments,removeHTMLComments,compressJS);
	  		}
		  	System.out.println("((Refactoring finished successfully))");
		}
		System.out.println("(>>>Application Ended<<<");
	}
	
	public static void startGUIApplication()
	{
		JFrame frame = new JFrame("JSP Compression Tool V2.0");
		JSP_ATG_CompressorPanel panel = new JSP_ATG_CompressorPanel();
		frame.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) 
			{
				System.exit(0);
			}
		});
        setMyMenuBar(frame);
		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());
		frame.setVisible(true);
	}
	
	public static void setMyMenuBar(final JFrame frame)
	{
	    JMenuBar menuBar = new JMenuBar();
	    
        JMenu fileMenu = new JMenu("File");
        JMenu aboutMenu = new JMenu("About");
        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);

        JMenuItem exitAction = new JMenuItem("Exit");
        JMenuItem aboutAction = new JMenuItem("About");
        fileMenu.add(exitAction);
        aboutMenu.add(aboutAction);
        
        exitAction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        aboutAction.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(frame,"      JSP Compression tool\nDesigned and implemented by:\n Name\t: Hisham Hassan\nE-mail\t: hisham.hassan@hp.com\nmobile\t: 0020105061744");
            }
        });        
        frame.setJMenuBar(menuBar);
	}
}
