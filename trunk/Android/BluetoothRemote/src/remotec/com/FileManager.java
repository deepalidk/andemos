package remotec.com;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public class FileManager {

	public static boolean saveAs(Context context, int ressound,String path, String filename){   
		 byte[] buffer=null;   
		 int size=0;   
		 
		 boolean exists = (new File(path)).exists();   
		 if (!exists){new File(path).mkdirs();}  
		 
		 exists=(new File(path+"/"+filename)).exists();
		 if (exists){return true;}  
		 
		 InputStream fIn = context.getResources().openRawResource(ressound);   
	 
		 try {   
		  size = fIn.available();   
		  buffer = new byte[size];   
		  fIn.read(buffer);   
		  fIn.close();   
		 } catch (IOException e) {   
		  // TODO Auto-generated catch block   
		  return false;   
		 }   
		  
		 FileOutputStream save;   
		 try {   
		  save = new FileOutputStream(path+"/"+filename);   
		  save.write(buffer);   
		  save.flush();   
		  save.close();   
		 } catch (FileNotFoundException e) {   
		  // TODO Auto-generated catch block   
		  return false;   
		 } catch (IOException e) {   
		  // TODO Auto-generated catch block   
		  return false;   
		 }       
		    
		 return true;   
		}  
	
}
