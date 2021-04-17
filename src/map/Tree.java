package map;
import main.Data;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
public class Tree {
    public List<Cell> cells;
    public List<Cell> otrs;
    public int[][] genom;
    public int energy;
    public Color c;
    public int age;
    public boolean isSeed;
    public Tree(){
        cells = new ArrayList<>();
        otrs = new ArrayList<>();
        genom = new int[Data.GENS][4];
    }
}
