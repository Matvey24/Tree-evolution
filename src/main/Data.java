package main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.File;

public class Data {
    private static final String FILE_SETTINGS = "settings.xml";
    private static final String FILE_GENOM = "genom.xml";
    public static float MUTATION_CHANCE;
    public static float FIGHT_MUTATION_CHANCE;
    public static int MAP_WIDTH;
    public static int MAP_HEIGHT;
    public static int LAB_WIDTH;
    public static int LIGHT_LAYERS;
    public static int LAB_HEIGHT;
    public static int GENS;
    public static int MAX_AGE;
    public static int FOG;
    private static Properties makeSettings() {
        Properties def = new Properties();
        def.setProperty("mutation_chance", "0.25");
        def.setProperty("fight_mutation_chance", "0");
        def.setProperty("map_width", "256");
        def.setProperty("map_height", "100");
        def.setProperty("lab_width", "150");
        def.setProperty("lab_height", "100");
        def.setProperty("gens", "16");
        def.setProperty("max_age", "50");
        def.setProperty("light_layers", "3");
        def.setProperty("fog", "-6");
        return def;
    }
    public static void loadSettings() {
        File f = new File(FILE_SETTINGS);
        Properties def = makeSettings();
        Properties settings = new Properties(def);
        if (!f.exists()) {
            try(FileOutputStream fos = new FileOutputStream(f)){
                def.storeToXML(fos, "Settings are here", "UTF-8");
            }catch (IOException e){
                e.printStackTrace();
            }
        }else {
            try (FileInputStream fis = new FileInputStream(f)) {
                settings.loadFromXML(fis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parseSettings(settings, def);
    }
    private static void parseSettings(Properties settings, Properties def){
        MUTATION_CHANCE = (float)Double.parseDouble(settings.getProperty("mutation_chance", def.getProperty("mutation_chance")));
        FIGHT_MUTATION_CHANCE = (float)Double.parseDouble(settings.getProperty("fight_mutation_chance", def.getProperty("fight_mutation_chance")));
        MAP_WIDTH = Integer.parseInt(settings.getProperty("map_width", def.getProperty("map_width")));
        MAP_HEIGHT = Integer.parseInt(settings.getProperty("map_height", def.getProperty("map_height")));
        LAB_WIDTH = Integer.parseInt(settings.getProperty("lab_width", def.getProperty("lab_width")));
        LAB_HEIGHT = Integer.parseInt(settings.getProperty("lab_height", def.getProperty("lab_height")));
        GENS = Integer.parseInt(settings.getProperty("gens", def.getProperty("gens")));
        MAX_AGE = Integer.parseInt(settings.getProperty("max_age", def.getProperty("max_age")));
        LIGHT_LAYERS = Integer.parseInt(settings.getProperty("light_layers", def.getProperty("light_layers")));
        FOG = Integer.parseInt(settings.getProperty("fog", def.getProperty("fog")));
    }
    public static void saveGenom(int[][] genom){
        File f = new File(FILE_GENOM);
        Properties p  = new Properties();
        if(!f.exists()){
            p.setProperty("num", "0");
        }else{
            try (FileInputStream fis = new FileInputStream(f)) {
                p.loadFromXML(fis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int n = Integer.parseInt(p.getProperty("num"));
        p.setProperty("num", (n+1) + "");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < GENS; ++i){
            for(int j = 0; j < 4; ++j){
                sb.append(genom[i][j]);
                sb.append(" ");
            }
            sb.append("\n");
        }
        p.setProperty("genom" + n, sb.toString());
        try(FileOutputStream fos = new FileOutputStream(FILE_GENOM)) {
            p.storeToXML(fos, "Add your genoms here", "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static int[][][] loadGenom(){
        File f = new File(FILE_GENOM);
        if(!f.exists())
            return null;
        try(FileInputStream fis = new FileInputStream(f)){
            Properties p = new Properties();
            p.loadFromXML(fis);
            int n = Integer.parseInt(p.getProperty("num"));
            if(n <= 0)
                return null;
            int[][][] genom = new int[n][GENS][4];
            for(int i = 0; i < n; ++i){
                String s = p.getProperty("genom"+i);
                String[] gens = s.split("\n");
                for(int j = 0; j < gens.length; ++j){
                    String[] gn = gens[j].split(" ");
                    for(int k = 0; k < gn.length; ++k){
                        genom[i][j][k] = Integer.parseInt(gn[k]);
                    }
                }
            }
            return genom;
        }catch (Exception e){
            System.out.println("Error loading");
            return null;
        }
    }
}
