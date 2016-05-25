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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CompressJSP
{
    enum CompressionType{TRUNCATE_SPACES,REMOVE_SERVER_SIDE_COMMENTS,REMOVE_HTML_COMMENTS,COMPRESS_JS};
    
	private static String TEMP_PATH = "_tmp";	

    public static void refactorFile(String fileName,boolean truncateSpaces, boolean reomveServerSideComments, boolean removeHTMLComments, boolean compressJavaScript)
    {
        if(reomveServerSideComments)
            compressFileByType(fileName,CompressionType.REMOVE_SERVER_SIDE_COMMENTS);
        if(removeHTMLComments)
            compressFileByType(fileName,CompressionType.REMOVE_HTML_COMMENTS);
        if(truncateSpaces)
            compressFileByType(fileName,CompressionType.TRUNCATE_SPACES);
        if(compressJavaScript)
            compressFileByType(fileName,CompressionType.COMPRESS_JS);
    }
	

    private static void compressFileByType(String fileName,CompressionType compressionType)
    {
        String newBackupFileName = fileName+TEMP_PATH;
        try 
        {            
            FileOperations.renameFile(fileName, newBackupFileName);
            // opening the original file, after it's renamed
            FileInputStream input = new FileInputStream(newBackupFileName);
            FileChannel channel = input.getChannel();
            // Create a read-only CharBuffer on the file
            ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int)channel.size());
            CharBuffer cbuf = Charset.forName("UTF-8").newDecoder().decode(bbuf);
            
            StringBuffer matchResultSB = null;
            
            if (compressionType.equals(CompressionType.REMOVE_SERVER_SIDE_COMMENTS)) {
                matchResultSB = removeServerSideComments(cbuf);
            } else if (compressionType.equals(CompressionType.REMOVE_HTML_COMMENTS)) {
                matchResultSB = removeHTMLComments(cbuf);
            } else if (compressionType.equals(CompressionType.COMPRESS_JS)) {
                matchResultSB = compressJS(cbuf);
            } else if (compressionType.equals(CompressionType.TRUNCATE_SPACES)) {
                matchResultSB = truncateSpaces(cbuf);
            }
     
            // close open channels
            channel.close();
            channel = null;
            input.close();
            input = null;
            cbuf.clear();
            cbuf = null;
            bbuf.clear();
            bbuf = null;
            System.gc();
             
            FileOperations.writeFile(fileName, matchResultSB);
            matchResultSB = null; 
            FileOperations.deleteFile(newBackupFileName);
        }
        catch (IOException e) 
        {
            File bkup = new File(newBackupFileName);
            File file = new File(fileName);
            if(file.exists() && bkup.exists())
            {
                FileOperations.deleteFile(fileName);
                FileOperations.renameFile(newBackupFileName, fileName);
            }
            else if(bkup.exists())
            {
                FileOperations.renameFile(newBackupFileName, fileName);
            }
            e.printStackTrace();
        }
    }
    
    
    
    private static StringBuffer removeServerSideComments(CharSequence input)
    {
        if(input == null)
            return null;
        
        String serverSideCommentStringPattern =  "(?s)(" //(1 groups)Dot matchs all flag 
            + "((\\s*)(<%--((.)*?)--%>)(\\s*))" //(6 groups) detects server side comments <%-- --%>
            + ")";
        Pattern serverSideCommentPattern = Pattern.compile(serverSideCommentStringPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = serverSideCommentPattern.matcher(input);
        
        StringBuffer matchResultSB = new StringBuffer();

        while (matcher.find())
        {
            if (matcher.group(4) != null)// server side comment
            {
                String space = "";
                if (matcher.group(3).toString().length() > 0)
                    space = spOrNl(matcher.group(3).toString());
                if (matcher.group(7).toString().length() > 0)
                    space = spOrNl(matcher.group(7).toString());
                matcher.appendReplacement(matchResultSB, space);
            }
        }
        matcher.appendTail(matchResultSB);
        return matchResultSB;
    }
    
    
    
    
    private static StringBuffer removeHTMLComments(CharSequence input)
    {
        String HTMLCommentsStringPattern =  "(?s)(" //(1 groups)Dot matchs all flag 
            + "((\\s*)(<!--\\s*[^\\[]((.)*?)-->)(\\s*))" + "|" //(6 groups) detects HTML comments <!-- -->
            + ")";
        
        Pattern HTMLCommentsPattern = Pattern.compile(HTMLCommentsStringPattern,Pattern.CASE_INSENSITIVE);
        Matcher matcher = HTMLCommentsPattern.matcher(input);
        
        StringBuffer matchResultSB = new StringBuffer();

        while (matcher.find())
        {
            if (matcher.group(4) != null)// HTML comment
            {
                String space = "";
                if (matcher.group(3).toString().length() > 0)
                    space = spOrNl(matcher.group(3).toString());
                if (matcher.group(7).toString().length() > 0)
                    space = spOrNl(matcher.group(7).toString());
                matcher.appendReplacement(matchResultSB, space);
            }
        }
        matcher.appendTail(matchResultSB);
        return matchResultSB;
    }
    
    
    
    
    private static StringBuffer truncateSpaces(CharSequence input)
    {
        String whiteSpaceRemoverStringPattern =  "(?s)(" //(1 groups)Dot matchs all flag 
            + "((\\s*)((\"(.*?)\")|('(.*?)'))(\\s*))"  + "|" //(8 groups) detects single and double quotes
            + "((\\s*)<(\\s*)script(.*?)<(\\s*)/(\\s*)script(\\s*)>(\\s*))" + "|"//(8 groups) to detect the scripts
            + "(>(\\s+))" + "|" //(2 groups) detects spaces after > 
            + "((\\s+)<)" + "|" //(2 groups) detects spaces before <
            + "((\\s*?)\n(\\s*?)([^(<|>|\\s)]|;))" //5 groups //append lines
            + ")";
        Pattern whiteSpaceRemoverPattern = Pattern.compile(whiteSpaceRemoverStringPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = whiteSpaceRemoverPattern.matcher(input);
        
        StringBuffer matchResultSB = new StringBuffer();

        while (matcher.find())
        {
            if (matcher.group(4) != null)// single and double quotes
            {
                String prefix = "";
                String suffeix = "";
                if (matcher.group(3).toString().length() > 0)
                    prefix = spOrNl(matcher.group(3).toString());
                if (matcher.group(9).toString().length() > 0)
                    suffeix = spOrNl(matcher.group(9).toString());
                matcher.appendReplacement(matchResultSB, prefix + Matcher.quoteReplacement(matcher.group(4)) + suffeix);
            } 
            else if (matcher.group(10) != null)// script tags
            {
                matcher.appendReplacement(matchResultSB, Matcher.quoteReplacement(matcher.group(10)));//Ignore Script Tags
            } 
            else if (matcher.group(18) != null)//GT angle bracket //(>(\\s+))
                matcher.appendReplacement(matchResultSB, ">");
            else if (matcher.group(20) != null)//LT angle bracket //((\\s+)<)
                matcher.appendReplacement(matchResultSB, "<");
            else if (matcher.group(25) != null)//Others //([^(<|>|\\s)]|;)
                matcher.appendReplacement(matchResultSB, " " + Matcher.quoteReplacement(matcher.group(25)));
        }
        matcher.appendTail(matchResultSB);
        return matchResultSB;
    }
    

    private static StringBuffer compressJS(CharSequence input)
    {
        String compressJavaScriptStringPattern =  "(?s)(" //(1 groups)Dot matchs all flag 
            + "((\\s*)<(\\s*)script(.*?)<(\\s*)/(\\s*)script(\\s*)>(\\s*))" + "|"//(8 groups) to detect the scripts
            + ")";
        Pattern compressJavaScriptPattern = Pattern.compile(compressJavaScriptStringPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compressJavaScriptPattern.matcher(input);
        
        StringBuffer matchResultSB = new StringBuffer();

        while (matcher.find())
        {
            if (matcher.group(5) != null)// script tags //([\\s\\S]*?)
            {
                matcher.appendReplacement(matchResultSB, " <script"
                    + compressJS(Matcher.quoteReplacement(matcher.group(5))) + "</script> ");
            } 
        }
        matcher.appendTail(matchResultSB);
        return matchResultSB;
    }
    
    private static String compressJS(String jsStr)
    {
        return appendJSLines(removeJSComments(jsStr));
    }
    
    
    private static String removeJSComments(String jsStr)
    {
        if(jsStr == null)
            return "";

        StringBuffer compressedSB = new StringBuffer();
        String JSPatternStr =  "(?s)(" //(1 groups)Dot match all flag 
                             + "((\\s*)((\"(.*?)\")|('(.*?)'))(\\s*))"  + "|" //(8 groups) detects single and double quotes, added here to be ignored
                             + "((\\s*)//((.)*?)\n)" + "|" //(4 groups) detects single line JavaScript comments
                             + "((\\s*)/\\*((.)*?)\\*/)" + "|" //(6 groups) detects multiple lines JavaScript comments
                             + ")";
        
        Pattern commentPattern = Pattern.compile(JSPatternStr);
        Matcher matcher = commentPattern.matcher(jsStr);

        while(matcher.find())
        {
            if(matcher.group(10)!=null)
            {
                matcher.appendReplacement(compressedSB, "\n"); //remove single line comments
            }
            else if(matcher.group(14)!=null)//(>(\\s+))
            {
                String space = "";
                if(matcher.group(15).toString().length()>0)
                    space = spOrNl(matcher.group(15).toString());
                matcher.appendReplacement(compressedSB,space);
            }

            //ignoring any double or single quote match
        }
        matcher.appendTail(compressedSB);
        return compressedSB.toString();        
    }
    
    

    
    private static String appendJSLines(String jsStr)
    {
        if(jsStr == null)
            return "";

        StringBuffer compressedSB = new StringBuffer();
        String JSPatternStr =  "(?s)(" //(1 groups)Dot matchs all flag 
                             + "((\\s*)((\"(.*?)\")|('(.*?)'))(\\s*))"  + "|" //(8 groups) detects single and double quotes
                             + "(\\s*;\\s*)" + "|" //(1 Group) clear spaces after and before the semi-colon.
                             + "(\\s*\\{\\s*)" + "|" //(1 Group) clear spaces after and before the opened curly braces.
                             + "(\\s*\\}\\s*)" + "|" //(1 Group) clear spaces after and before the closed curly braces.
                             + "(\\s*\\(\\s*)" + "|" //(1 Group) clear spaces after and before the open braces.
                             + "(\\s*=\\s*)" + "|" //(1 Group) clear spaces after and before the equal sign.
                             + "(\\s*\\-\\s*)" + "|" //(1 Group) clear spaces after and before the minus sign.
                             + "(\\s*\\+\\s*)" + "|" //(1 Group) clear spaces after and before the plus sign.
                             + "(\\s*\\.\\s*)" + "|" //(1 Group) clear spaces after and before the dot sign.                             
                             + "(\\s*>\\s+<\\s*)" + "|" //(1 groups) detects spaces after >
                             + "(>(\\s+))" + "|" //(2 groups) detects spaces after > 
                             + "((\\s+)<)" + "|" //(2 groups) detects spaces before <
                             + "((\\s*?)\n(\\s*?)\n(\\s*?)([^(<|>|\\s)]|;))" //5 groups //append lines
                             + ")";
        
        Pattern commentPattern = Pattern.compile(JSPatternStr);
        Matcher matcher = commentPattern.matcher(jsStr);
        while(matcher.find())
        {
            if(matcher.group(4)!=null)
            {
                String prefix = "";
                String suffeix = "";
                if(matcher.group(3).toString().length()>0)
                    prefix = spOrNl(matcher.group(3).toString());
                if(matcher.group(9).toString().length()>0)
                    suffeix = spOrNl(matcher.group(9).toString());
                matcher.appendReplacement(compressedSB, prefix + Matcher.quoteReplacement(matcher.group(4))+ suffeix);
            }
            if(matcher.group(10)!=null)
            {
                matcher.appendReplacement(compressedSB, ";");
            }
            if(matcher.group(11)!=null)
            {
                matcher.appendReplacement(compressedSB, "{");
            }
            if(matcher.group(12)!=null)
            {
                matcher.appendReplacement(compressedSB, "}");
            }
            if(matcher.group(13)!=null)
            {
                matcher.appendReplacement(compressedSB, "(");
            }
            if(matcher.group(14)!=null)
            {
                matcher.appendReplacement(compressedSB, "=");
            }
            if(matcher.group(15)!=null)
            {
                matcher.appendReplacement(compressedSB, "-");
            }
            if(matcher.group(16)!=null)
            {
                matcher.appendReplacement(compressedSB, "+");
            }
            if(matcher.group(17)!=null)
            {
                matcher.appendReplacement(compressedSB, ".");
            }
            if(matcher.group(18)!=null)
            {
                matcher.appendReplacement(compressedSB, "><");
            }
            else if(matcher.group(19)!=null)//(>(\\s+))
            {
                String space = "";
                if(matcher.group(20).toString().length()>0)
                    space = spOrNl(matcher.group(20).toString());
                matcher.appendReplacement(compressedSB, ">" + space);
            }
            else if(matcher.group(21)!=null)//((\\s+)<)
            {
                String space = "";
                if(matcher.group(22).toString().length()>0)
                    space = spOrNl(matcher.group(22).toString());
                matcher.appendReplacement(compressedSB, space + "<");
            }
            else if(matcher.group(27)!=null)//([^(<|>|\\s)]|;)
                matcher.appendReplacement(compressedSB, "\n"+Matcher.quoteReplacement(matcher.group(27)));
        }
        matcher.appendTail(compressedSB);
        return compressedSB.toString();
    }
    
    private static String spOrNl(String str)
    {
    	if(str == null)
    		return "";
    	if(str.contains("\n"))
    		return "\n";
    	return " ";
    }
}
