package com.cattsoft.timers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


public class TTTF {

	
	public void start() throws JSchException, SftpException, IOException{
		BufferedReader br = null;
		PrintWriter out = null;
		try {
			Session session =null;
			JSch jsch = new JSch();
			session = jsch.getSession("root", "192.168.1.90",22);
			session.setConfig("StrictHostKeyChecking", "no");  
			session.setPassword("ys");
			session.connect();
			
			ChannelSftp channelSftp =(ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			
			br = new BufferedReader(
					new InputStreamReader(
							channelSftp.get("/home/html/a.html")));
			out = new PrintWriter(new FileOutputStream("C:\\Users\\yangshan\\Desktop\\aaaa.txt"));
			String line = "";
			while((line = br.readLine())!=null){
				out.write(line);
			}
			channelSftp.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			br.close();
			out.close();
		}
		
	}
	
	public static void main(String[] args) throws JSchException, SftpException, IOException {
		
		File f = new File("C:\\Users\\yangshan\\Desktop\\lib");
		File[] files = f.listFiles();
		StringBuffer s = new StringBuffer();
		for(File ff:files){
			s.append(" ").append(ff.getName());
		}
		System.out.println(s.toString());
	}
}
