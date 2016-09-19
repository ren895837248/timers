package com.cattsoft.pub;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SPSEncrypt {
  private static String Algorithm = "DES"; //���� �����㷨,���� DES,DESede,Blowfish
  private static String mykey = "EF6167D986E3B529";

  static {
    Security.addProvider(new com.sun.crypto.provider.SunJCE());
  }

  //�����Կ, ע��˲���ʱ��Ƚϳ�
  public static byte[] getKey() throws Exception {
    KeyGenerator keygen = KeyGenerator.getInstance(Algorithm);
    SecretKey deskey = keygen.generateKey();
    return deskey.getEncoded();
  }

  //����
  public static byte[] encode(byte[] input, byte[] key) throws Exception {
    SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
    Cipher c1 = Cipher.getInstance(Algorithm);
    c1.init(Cipher.ENCRYPT_MODE, deskey);
    byte[] cipherByte = c1.doFinal(input);
    return cipherByte;
  }

  //����
  public static byte[] decode(byte[] input, byte[] key) throws Exception {
    SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
    Cipher c1 = Cipher.getInstance(Algorithm);
    c1.init(Cipher.DECRYPT_MODE, deskey);
    byte[] clearByte = c1.doFinal(input);
    return clearByte;
  }

  //�ֽ���ת����16�����ַ�
  public static String byte2hex(byte bytes[]) {
    StringBuffer retString = new StringBuffer();
    for(int i = 0; i < bytes.length; ++i) {
      retString.append(Integer.toHexString(0x0100 + (bytes[i] & 0x00FF)).substring(1).toUpperCase());
    }
    return retString.toString();
  }

  //��16�����ַ�ת�����ֽ���
  public static byte[] hex2byte(String hex) {
    byte[] bts = new byte[hex.length() / 2];
    for(int i = 0; i < bts.length; i++) {
      bts[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
    }
    return bts;
  }



  public static String JieM(String PWD){
    try{
      String password = PWD.trim();
      byte[] key = hex2byte(mykey);
      byte[] afterDecode = decode(hex2byte(password.toUpperCase()), key);
      return new String(afterDecode);
    }catch(Exception e){
      e.printStackTrace();
      return "";
    }
  }

//
  public static void main(String[] args) {
    String action = "";
    String input = null;

    try {
      System.out.println("-= ����ͨ�������/���ܳ��� =- ");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in), 1);
      while(action.equals("")) {
        input = br.readLine();
        if(input.trim().equals("0")) {
          action = "0";
        }
        else if(input.trim().equals("1")) {
          action = "1";
        }
      }

      if(action.equals("0")) {
        System.out.print("��ѡ������Ҫ���ܵ����룺");
      }
      else {
        System.out.print("��ѡ��������ܺ�����룺");
      }

      input = br.readLine();
      String password = input.trim();

      byte[] key = hex2byte(mykey);
      if(action.equals("0")) {
        byte[] afterEncode = encode(password.getBytes(), key);
        System.out.println("xxx" + byte2hex(afterEncode));
      }
      else {
        byte[] afterDecode = decode(hex2byte(password.toUpperCase()), key);
        System.out.println("xxx" + new String(afterDecode));
      }
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}
