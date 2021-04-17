package map;

import main.Data;

import java.awt.*;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static map.Cell.OTR;
import static map.Cell.SEED;

public class DataBase {
    public static final String SAVE_NAME = "save.s";
    public static long cycle;
    public static long died_seeds;
    public static long grown_seeds;

    public static float mutation_chance;
    public static List<Tree> trees;

    public static void save() throws IOException {
        File f = new File(SAVE_NAME);
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
        dos.writeInt(Data.GENS);
        dos.writeInt(Data.MAP_WIDTH);
        dos.writeInt(Data.MAP_HEIGHT);
        dos.writeInt(Data.MAX_AGE);
        dos.writeLong(cycle);
        dos.writeLong(died_seeds);
        dos.writeLong(grown_seeds);
        dos.writeFloat(mutation_chance);
        dos.writeInt(trees.size());
        for(Tree t : trees){
            dos.writeInt(t.energy);
            dos.writeInt(t.age);
            dos.writeInt(t.c.getRGB());
            for(int i = 0; i < t.genom.length; ++i){
                dos.writeInt(t.genom[i][0]);
                dos.writeInt(t.genom[i][1]);
                dos.writeInt(t.genom[i][2]);
                dos.writeInt(t.genom[i][3]);
            }
            dos.writeInt(t.cells.size());
            for(Cell c: t.cells){
                dos.writeInt(c.energy);
                dos.writeInt(c.x);
                dos.writeInt(c.y);
                dos.writeInt(c.genomID);
                dos.writeByte(c.type);
            }
        }
        dos.flush();
        dos.close();
    }
    public static void load() throws IOException{
        File f = new File(SAVE_NAME);
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        //
        int gens = dis.readInt();
        if(gens != Data.GENS)
            throw new RuntimeException("Gens count in file is incorrect");
        //
        int map_width = dis.readInt();
        if(map_width != Data.MAP_WIDTH)
            throw new RuntimeException("Map width is incorrect");
        //
        int map_height = dis.readInt();
        if(map_height != Data.MAP_HEIGHT)
            throw new RemoteException("Map height is incorrect");
        //
        int max_age = dis.readInt();
        if(Data.MAX_AGE != max_age)
            throw new RuntimeException("Max age is incorrect");
        //
        cycle = dis.readLong();
        died_seeds = dis.readLong();
        grown_seeds = dis.readLong();
        mutation_chance = dis.readFloat();
        //
        int tr_c = dis.readInt();
        trees = new ArrayList<>(tr_c);
        //
        for(int i = 0; i < tr_c; ++i){
            Tree t = new Tree();
            t.energy = dis.readInt();
            t.age = dis.readInt();
            t.c = new Color(dis.readInt());
            for(int j = 0; j < t.genom.length; ++j){
                t.genom[j][0] = dis.readInt();
                t.genom[j][1] = dis.readInt();
                t.genom[j][2] = dis.readInt();
                t.genom[j][3] = dis.readInt();
            }
            int cel_c = dis.readInt();
            t.cells = new ArrayList<>(cel_c);
            for(int j = 0; j < cel_c; ++j){
                Cell c = new Cell();
                c.energy = dis.readInt();
                c.x = dis.readInt();
                c.y = dis.readInt();
                c.genomID = dis.readInt();
                c.type = dis.readByte();
                c.tree = t;
                if(c.type == OTR)
                    t.otrs.add(c);
                t.cells.add(c);
            }
            if(t.cells.get(0).type == SEED){
                t.isSeed = true;
            }
            trees.add(t);
        }

        dis.close();
    }

}
