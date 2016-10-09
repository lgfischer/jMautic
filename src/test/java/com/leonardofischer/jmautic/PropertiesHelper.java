package com.leonardofischer.jmautic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class PropertiesHelper {

    public static Properties getFromResource(String resourceName) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = PropertiesHelper.class.getClassLoader().getResourceAsStream(resourceName);
            if(input!=null){
                prop.load(input);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally{
            if(input!=null){
                try {
                    input.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }

    public static Properties getFromFile(String fileName) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(fileName);
            if(input!=null) {
                prop.load(input);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally{
            if(input!=null){
                try {
                    input.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }

    public static void saveToFile(Properties prop, String fileName) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(fileName);
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
