package com.fun.tc.nc.until;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


/**
 * @author Motey
 * @time 2016-05-03
 */
public class MyFile {
	
	private String path = "D:/Siemens/";
    private String filenameTemp;
	private boolean autoLineFeed = false;
    

	public MyFile(String name, String path){
	
		try {
			setPath(path);
			creatTxtFile(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
    public void setTxt(String text, boolean isCover) {
    	
        //MyFile myFile = new MyFile();
        try {
               writeTxtFile(text, isCover);
               String str = readData();
               System.out.println("*********\n" + str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean creatTxtFile(String name) throws IOException {
    	
        boolean flag = false;
        
        filenameTemp = path + name + ".txt";
        File filename = new File(filenameTemp);
        if (!filename.exists()) {
        	
            filename.createNewFile();
            flag = true;
        }
        
        return flag;
    }
    
    public void setPath(String path){
    	if(path != null){
    		this.path = path;
    	}
    }
    
    public String getPath(){
    	return path;
    }
    
    public void setAutoLineFeed(boolean flag){
    	
    	autoLineFeed = flag;
    	
    }

    public boolean writeTxtFile(String newStr, boolean isCover) throws IOException {
        boolean flag = false;
        String filein = autoLineFeed ? newStr + "\r\n" : newStr;
        String temp = "";

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        FileOutputStream fos = null;
        PrintWriter pw = null;
        
        try {
            File file = new File(filenameTemp);
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            if(!isCover){
            	for (int j = 1; (temp = br.readLine()) != null; j++) {
                    buf = buf.append(temp);
                    buf = buf.append(System.getProperty("line.separator"));
                    System.out.println(j);
                }
            }
            
            
            buf.append(filein);

            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            flag = true;
        } catch (IOException e1) {
            throw e1;
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        return flag;
    }

    public String readData() {
        String strs = "";
        FileReader read = null;
        try {
            read = new FileReader(new File(filenameTemp));
            StringBuffer sb = new StringBuffer();
            char ch[] = new char[1024];
            int d = read.read(ch);
            while (d != -1) {
                String str = new String(ch, 0, d);
                sb.append(str);
                d = read.read(ch);
            }
            //System.out.print(sb.toString());
            String a = sb.toString().replaceAll("@@@@@", ",");
            strs = a.substring(0, a.length() - 1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strs;
    }
}