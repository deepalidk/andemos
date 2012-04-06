/*
 * Copyright 2012 @ Copyright Remotec Technology Ltd., All rights reserved.
 *     
 * Description: Loads data from xml file and save data into xml file.
 * 
 *      Author: Walker
 */
package com.remotec.universalremote.persistence;

import android.util.Xml;

import com.remotec.universalremote.data.Device;
import com.remotec.universalremote.data.Extender;
import com.remotec.universalremote.data.Key;
import com.remotec.universalremote.data.RemoteUi;
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
			    Element e= (Element) items.item(i);
			    if(e.getNodeName().equals("Extender")){
					Extender ext = new Extender();
					loadData(ext, e);
					uiData.getExtenderMap().put(ext.getAddress(),ext);
			    }
			    else if(e.getNodeName().equals("Device")){
			    	Device dev = new Device();
					loadData(dev, e);
					uiData.getChildren().add(dev);
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

	}

	/*
	 * Loads data to device object from an xml element
	 */
	private void loadData(Device uiData, Element elem) {
		
		uiData.setName(elem.getAttribute("name"));
		uiData.setIconName(elem.getAttribute("icon_name"));
		uiData.setDeviceType(elem.getAttribute("category"));
		uiData.setDeviceTypeId(Integer.parseInt(elem.getAttribute("category_id")));
		uiData.setManufacturer(elem.getAttribute("manufacturer"));
		uiData.setIrCode(Integer.parseInt(elem.getAttribute("ircode")));
		
		NodeList items = elem.getChildNodes();
		Node child = null;
		for (int i = 0; i < items.getLength(); i++) {
			child = items.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
			    Element e= (Element) items.item(i);
			    if(e.getNodeName().equals("Key")){
					Key key = new Key();
					loadData(key, e);
					uiData.getChildren().add(key);
			    }   
			}
		}
	}

	/*
	 * Loads data to key object from an xml element
	 */
	private void loadData(Key key, Element elem) {	
		key.setKeyId(Integer.parseInt(elem.getAttribute("key_id")));
		key.setIsLearned(Boolean.parseBoolean(elem.getAttribute("islearned")));
		key.setVisible(Boolean.parseBoolean(elem.getAttribute("visible")));
		key.setText(elem.getAttribute("text"));
	}

	/*
	 * Saves data to an xml element.
	 */
	private void saveData(RemoteUi uiData, XmlSerializer serializer)
			throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag("", "RemoteUi");
		serializer.attribute("","version", uiData.getVersion());

		Map<String,Extender> extMap = uiData.getExtenderMap();

		for (Extender ext : extMap.values()) {
			saveData(ext, serializer);
		}
		
		List<Device> devList = uiData.getChildren();

		for (Device dev : devList) {
			saveData(dev, serializer);
		}

		serializer.endTag("", "RemoteUi");
	}

	/*
	 * Saves data to an xml element.
	 */
	private void saveData(Extender ext, XmlSerializer serializer)
			throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag("", "Extender");
		serializer.attribute("","name", ext.getName());
		serializer.attribute("","address", ext.getAddress());

		serializer.endTag("", "Extender");
	}

	/*
	 * Saves data to an xml element.
	 */
	private void saveData(Device dev, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag("", "Device");
		serializer.attribute("","name", dev.getName());
		serializer.attribute("","icon_name", dev.getIconName());
		serializer.attribute("","manufacturer", dev.getManufacturer());
		serializer.attribute("","category", dev.getDeviceType());
		serializer.attribute("","category_id", ""+dev.getDeviceTypeId());
		serializer.attribute("","ircode", ""+dev.getIrCode());

		List<Key> children = dev.getChildren();

		for (Key key : children) {
			saveData(key,serializer);
		}

		serializer.endTag("", "Device");
	}

	/*
	 * Saves data to an xml element.
	 */
	private void saveData(Key key, XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
		serializer.startTag("", "Key");
		serializer.attribute("","key_id", ""+key.getKeyId());
		serializer.attribute("","text", key.getText());
		serializer.attribute("","islearned", ""+key.getIsLearned());
		serializer.attribute("","visible", ""+key.getVisible());
		
		serializer.endTag("", "Key");
	}
}
