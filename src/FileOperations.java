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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Vector;


public class FileOperations
{
	public static boolean renameFile(String oldName, String newName)
    {
    	File file = new File(oldName);
    	return renameFile(file, newName);
    }
    
    public static boolean renameFile(File file, String newName)
    {
    	if(file.exists() && file.isFile())
    	{
	    	//file.setWritable(true);
    		file.renameTo(new File(newName));
	    	return true;
    	}
    	return false;
    }
    
    public static boolean deleteFile(String fileName)
    {
    	File file = new File(fileName);
    	return deleteFile(file);
    }
    
    public static boolean deleteFile(File file)
    {
    	if(file.exists() && file.isFile())
    	{
    		//file.setWritable(true);
    		return file.delete();
    	}
    	return false;
    }
    
    public static boolean writeFile(String fileName,StringBuffer fileContent)
    {
    	File file = new File(fileName);
    	try
    	{
    		if(!file.exists())
    			file.createNewFile();
    		FileWriter fWriter = new FileWriter(file);
            BufferedWriter outBufferedWriter = new BufferedWriter(fWriter);
            outBufferedWriter.write(fileContent.toString());
            outBufferedWriter.close();
    		return true;
		}
    	catch (IOException e) 
		{
			e.printStackTrace();
		}
        return false;
    }
    
    public static boolean writeFile(File file,StringBuffer fileContent)
    {
        try
        {
            if(!file.exists())
                file.createNewFile();
            FileWriter fWriter = new FileWriter(file);
            BufferedWriter outBufferedWriter = new BufferedWriter(fWriter);
            outBufferedWriter.write(fileContent.toString());
            outBufferedWriter.close();
            return true;
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return false;
    }
    
    public static String getFileName(String canonicalFilePath)
    {
        int slash = canonicalFilePath.lastIndexOf("/");
        if(slash == -1)
            slash = canonicalFilePath.lastIndexOf("\\");
        return canonicalFilePath.substring(slash+1);
    }
    
    public static File createLogFileAndDirectory(String fileName, String dirPath)
    {
        File directory=null,file=null;
        try
        {
            directory = new File(dirPath);
            directory.mkdirs();
            file = new File(directory, fileName);        
            file.createNewFile();
        } 
        catch (IOException e) {e.printStackTrace();}
        return file;
    }
    
    public static Vector <String> getAllSubFiles(String directoryPath)
    {
        Vector <String>filesVector = new Vector <String>();
        getAllSubFiles(directoryPath,filesVector);
        return filesVector;
    }
    
    private static void getAllSubFiles(String directoryPath,Vector <String>filesVector)
    {
        final String ex = "jsp";
        File ff = new File(directoryPath);
        if(!ff.exists())
            return;
        if(!ff.isDirectory())
            return;
        File[] files = ff.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith("." + ex) && (name.length() > (ex.length() + 1));
            }
        });
        
        for(int j=0;j<files.length;j++)
        {
            try 
            {
                long length = files[j].length();// Get the size of the file
                if (length <= Integer.MAX_VALUE) {
                    filesVector.add(files[j].getCanonicalPath());
                }
            }
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
        File[] directories = ff.listFiles();
        for(int i=0;i<directories.length;i++)
        {
            if(directories[i].isDirectory())
            {
                try 
                {
                    getAllSubFiles(directories[i].getCanonicalPath(),filesVector);
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
