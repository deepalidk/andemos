/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 * Description: Loads data from xml file and save data into xml file.
 * 
 *      Author: Walker
 */
package com.remote.universalremote.persistence;

import android.util.Xml;

import com.remote.universalremote.data.AcDevice;
import com.remote.universalremote.data.AvDevice;
import com.remote.universalremote.data.Device;
import com.remote.universalremote.data.Extender;
import com.remote.universalremote.data.Key;
import com.remote.universalremote.data.RemoteUi;
import com.remote.universalremote.data.Key.Mode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/*
 * Loads data from xml file and save data into xml file.
 */
public class XmlManager {

	/*
	 * Loads data from xml file.
	 */
	public boolean loadData(RemoteUi uiData, String filePath) {
		boolean result = false;

		try {

			File xmlFile = new File(filePath);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(xmlFile);

			Element root = dom.getDocumentElement();

			loadData(uiData, root);

			result = true;

		} catch (FileNotFoundException e) {
			result = false;
		} catch (ParserConfigurationException e) {
			result = false;
		} catch (SAXException e) {
			result = false;
		} catch (IOException e) {
			result = false;
		} catch (Exception e){
			result = false;
		}

		return result;

	}

	/*
	 * Saves data to xml file.
	 */
	public boolean saveData(RemoteUi uiData, String filePath) {

		boolean result = false;
		File xmlFile = new File(filePath);
		File tempFile = new File(filePath + "tmp");
		FileOutputStream outStream;

		try {
			outStream = new FileOutputStream(tempFile);

			OutputStreamWriter outStreamWriter = new OutputStreamWriter(
					outStream, "UTF-8");
			BufferedWriter writer = new BufferedWriter(outStreamWriter);
			XmlSerializer serializer = Xml.newSerializer();

			serializer.setOutput(writer);

			serializer.startDocument("UTF-8", true);

			saveData(uiData, serializer);

			serializer.endDocument();

			writer.flush();
			writer.close();
			tempFile.renameTo(xmlFile);

			result = true;
		} catch (FileNotFoundException e) {
			result = false;
		} catch (UnsupportedEncodingException e) {
			result = false;
		} catch (IllegalArgumentException e) {
			result = false;
		} catch (IllegalStateException e) {
			result = false;
		} catch (IOException e) {
			result = false;
		} catch (Exception e) {
			result = false;
		}

		return result;
	}

	/*
	 * Loads data to RemoteUi object from an xml element
	 */
	private void loadData(RemoteUi uiData, Element elem) {

		uiData.setVersion(elem.getAttribute("version"));

		NodeList items = elem.getChildNodes();
		Node child = null;
		for (int i = 0; i < items.getLength(); i++) {
			child = items.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) items.item(i);
				if (e.getNodeName().equals("Extender")) {
					Extender ext = new Extender();
					loadData(ext, e);
					uiData.getExtenderMap().put(ext.getAddress(), ext);
				} else if (e.getNodeName().equals("Device")) {			
					String categoryId=e.getAttribute("category_id");
					if(categoryId!=null&&Integer.parseInt(categoryId)==0){//ac
						AcDevice dev = (AcDevice)Device.createDevice(Integer.parseInt(categoryId));
						loadData(dev, e);
						uiData.getChildren().add(dev);
					}else{	
						AvDevice dev = (AvDevice)Device.createDevice(Integer.parseInt(categoryId));
						loadData(dev, e);
						uiData.getChildren().add(dev);
					}
				}

			}
		}
	}

	/*
	 * Loads data to Extender object from an xml element
	 */
	private void loadData(Extender uiData, Element elem) {

		uiData.setName(elem.getAttribute("name"));
		uiData.setAddress(elem.getAttribute("address"));
		if(elem.hasAttribute("isLastActive")){
			uiData.setIsLastActive(Boolean.parseBoolean(elem.getAttribute("isLastActive")));
		}

	}

	/*
	 * Loads data to device object from an xml element
	 */
	private void loadData(AvDevice uiData, Element elem) {

		if(elem.hasAttribute("region")){
			String region=elem.getAttribute("region");
			uiData.setRegion(region);
		}
		uiData.setName(elem.getAttribute("name"));
		uiData.setIconName(elem.getAttribute("icon_name"));
		uiData.setDeviceType(elem.getAttribute("category"));
		uiData.setDeviceTypeId(Integer.parseInt(elem
				.getAttribute("category_id")));
		uiData.setManufacturer(elem.getAttribute("manufacturer"));
		uiData.setIrCode(Integer.parseInt(elem.getAttribute("ircode")));

		NodeList items = elem.getChildNodes();
		Node child = null;
		for (int i = 0; i < items.getLength(); i++) {
			child = items.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) items.item(i);
				if (e.getNodeName().equals("Key")) {
					Key key = new Key();
					loadData(key, e);
					uiData.getChildren().add(key);
				}
			}
		}
	}
	
	/*
	 * Loads data to device object from an xml element
	 */
	private void loadData(AcDevice uiData, Element elem) {

		if(elem.hasAttribute("region")){
			String region=elem.getAttribute("region");
			uiData.setRegion(region);
		}
		uiData.setName(elem.getAttribute("name"));
		uiData.setIconName(elem.getAttribute("icon_name"));
		uiData.setDeviceType(elem.getAttribute("category"));
		uiData.setDeviceTypeId(Integer.parseInt(elem
				.getAttribute("category_id")));
		uiData.setManufacturer(elem.getAttribute("manufacturer"));
		uiData.setIrCode(Integer.parseInt(elem.getAttribute("ircode")));

		NodeList items = elem.getChildNodes();
		Node child = null;
		for (int i = 0; i < items.getLength(); i++) {
			child = items.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) items.item(i);
				if (e.getNodeName().equals("LearnKey")) {
					
					String keyString=e.getAttribute("key");
					
					Node keyNode=e.getFirstChild();
					
					Key key = new Key();
					loadData(key, (Element)keyNode);
					uiData.setLearnKey(keyString, key);
					
				}
			}
		}
	}

	/*
	 * Loads data to key object from an xml element
	 */
	private void loadData(Key key, Element elem) {
		key.setKeyId(Integer.parseInt(elem.getAttribute("key_id")));
		key.setMode(Mode.values()[Integer.parseInt(elem.getAttribute("mode"))]);
		key.setVisible(Boolean.parseBoolean(elem.getAttribute("visible")));
		key.setText(elem.getAttribute("text"));
		
		if(elem.hasAttribute("data")){
			String strData=elem.getAttribute("data");
			key.setData(hexStringToByteArray(strData));
		}
	}

	/*
	 * Saves data to an xml element.
	 */
	private void saveData(RemoteUi uiData, XmlSerializer serializer)
			throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag("", "RemoteUi");
		serializer.attribute("", "version", uiData.getVersion());

		Map<String, Extender> extMap = uiData.getExtenderMap();

		for (Extender ext : extMap.values()) {
			saveData(ext, serializer);
		}

		List<Device> devList = uiData.getChildren();

		for (Device dev : devList) {
			if(dev instanceof AvDevice){
			 saveData((AvDevice)dev, serializer);
			}else{
			 saveData((AcDevice)dev,serializer);
			}
		}

		serializer.endTag("", "RemoteUi");
	}

	/*
	 * Saves data to an xml element.
	 */
	private void saveData(Extender ext, XmlSerializer serializer)
			throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag("", "Extender");
		serializer.attribute("", "name", ext.getName());
		serializer.attribute("", "address", ext.getAddress());
		serializer.attribute("", "isLastActive", "" + ext.isLastactive());
		
		serializer.endTag("", "Extender");
	}

	/*
	 * Saves data to an xml element.
	 */
	private void saveData(AvDevice dev, XmlSerializer serializer)
			throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag("", "Device");
		serializer.attribute("", "region", dev.getRegion());
		serializer.attribute("", "name", dev.getName());
		serializer.attribute("", "icon_name", dev.getIconName());
		serializer.attribute("", "manufacturer", dev.getManufacturer());
		serializer.attribute("", "category", dev.getDeviceType());
		serializer.attribute("", "category_id", "" + dev.getDeviceTypeId());
		serializer.attribute("", "ircode", "" + dev.getIrCode());

		List<Key> children = dev.getChildren();

		for (Key key : children) {
			saveData(key, serializer);
		}

		serializer.endTag("", "Device");
	}
	
	/*
	 * Saves data to an xml element.
	 */
	private void saveData(AcDevice dev, XmlSerializer serializer)
			throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag("", "Device");
		serializer.attribute("", "region", dev.getRegion());
		serializer.attribute("", "name", dev.getName());
		serializer.attribute("", "icon_name", dev.getIconName());
		serializer.attribute("", "manufacturer", dev.getManufacturer());
		serializer.attribute("", "category", dev.getDeviceType());
		serializer.attribute("", "category_id", "" + dev.getDeviceTypeId());
		serializer.attribute("", "ircode", "" + dev.getIrCode());

		Map<String,Key> learnKeyMap=dev.getLearnKeyMap();
		
		for(Entry<String, Key> entry: learnKeyMap.entrySet()){
			
            serializer.startTag("", "LearnKey");
            serializer.attribute("", "key",entry.getKey());
            saveData(entry.getValue(), serializer);
            serializer.endTag("","LearnKey");
			
		}
		

		serializer.endTag("", "Device");
	}

	/*
	 * Saves data to an xml element.
	 */
	private void saveData(Key key, XmlSerializer serializer)
			throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag("", "Key");
		serializer.attribute("", "key_id", "" + key.getKeyId());
		serializer.attribute("", "text", key.getText());
		serializer.attribute("", "mode", "" + key.getMode().getValue());
		serializer.attribute("", "visible", "" + key.getVisible());
		if(key.getData()!=null){
			String strData=byteArrayToHexString(key.getData());
			serializer.attribute("", "data", strData);
		}
			
		serializer.endTag("", "Key");
	}

	/**
	 * converts given byte array to a hex string
	 * 
	 * @param bytes
	 * @return
	 */
	public static String byteArrayToHexString(byte[] bytes) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			if (((int) bytes[i] & 0xff) < 0x10)
				buffer.append("0");
			buffer.append(Long.toString((int) bytes[i] & 0xff, 16));
		}
		return buffer.toString();
	}

	/**
	 * converts given hex string to a byte array (ex: "0D0A" => {0x0D, 0x0A,})
	 * 
	 * @param str
	 * @return
	 */
	public static final byte[] hexStringToByteArray(String str) {
		int i = 0;
		byte[] results = new byte[str.length() / 2];
		for (int k = 0; k < str.length();) {
			results[i] = (byte) (Character.digit(str.charAt(k++), 16) << 4);
			results[i] += (byte) (Character.digit(str.charAt(k++), 16));
			i++;
		}
		return results;
	}

}
