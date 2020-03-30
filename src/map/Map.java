package map;
import main.Batch;

import java.awt.*;
import java.util.*;
import java.util.List;

import static map.Cell.*;
import static map.Tree.GENS;

public class Map {
    public Cell[][] map;
    public List<Cell> cells;
    public List<Tree> trees;
    public List<Tree> freeTrees;
    public List<Cell> seeds;
    public Queue<Cell> changes;
    public Map(int x, int y){
        map = new Cell[x][y];
        cells = new ArrayList<>();
        trees = new ArrayList<>();
        changes = new LinkedList<>();
        seeds = new ArrayList<>();
        freeTrees = new ArrayList<>();
        Tree tree = new Tree();
        tree.c = Color.BLUE;
        tree.genom = new int[][]{{1,30,30,30},{30,2,30,3},{4,30,30,30},{9,30,30,30},
                {5,30,30,30},{30,6,30,30},{30,7,30,30},{4,30,8,30},
                {30,30,8,30},{30,30,30,30},{30,30,30,30},{30,30,30,30},
                {30,30,30,30},{30,30,30,30},{30,30,30,30},{30,30,30,30}};
//        for(int i = 0; i < tree.genom.length; ++i){
//            for(int j = 0; j < tree.genom[0].length; ++j){
//                tree.genom[i][j] = (int)(Math.random() * tree.genom.length * 2);
//            }
//        }
        Cell cell = initCell(0, x / 2, y / 2, tree);
        cell.type = SEED;
        map[cell.x][cell.y] = cell;
        tree.energy = 300;
        tree.cells.add(cell);
        seeds.add(cell);
        trees.add(tree);
    }
    public void update(){
        for(int i = seeds.size() - 1; i >= 0; --i){
            Cell c = seeds.get(i);
            if(c.y == 0){
                seeds.remove(i);
                c.type = OTR;
                c.tree.otrs.add(c);
//                c.tree.energy = 300;
//                c.energy = 0;
                c.tree.age = 0;
            }else if(map[c.x][c.y - 1] == null) {
                map[c.x][c.y--] = null;
                map[c.x][c.y] = c;
            }else{
                map[c.x][c.y] = null;
                seeds.remove(i);
                c.tree.otrs.clear();
                c.tree.cells.clear();
                trees.remove(c.tree);
                freeTrees.add(c.tree);
                cells.add(c);
            }
        }
        for (Cell[] value : map) {
            int n = 3;
            for (int j = value.length - 1; j >= 0; --j) {
                if (value[j] != null) {
                    value[j].earnEnergy((n--) * (j + 6));
                    if (n == 0)
                        break;
                }
            }
        }
        for (Tree tree : trees) {
            for (int j = 0; j < tree.otrs.size(); ++j) {
                Cell c = tree.otrs.get(j);
                if (c.energy >= Cell.energyForGrow) {
                    int gen = tree.genom[c.genomID][0];
                    if (gen < GENS && c.y < map[0].length - 1 && map[c.x][c.y + 1] == null) {
                        Cell nc = initCell(gen, c.x, c.y + 1, tree);
                        c.energy -= Cell.energyForGrow;
                        changes.offer(nc);
                        c.type = WOOD;
                    }
                }
            }
        }
        for (Tree tree : trees) {
            for (int j = 0; j < tree.otrs.size(); ++j) {
                Cell c = tree.otrs.get(j);
                if (c.energy >= Cell.energyForGrow) {
                    int gen = tree.genom[c.genomID][1];
                    if (gen < GENS && map[mod(c.x + 1, map.length)][c.y] == null) {
                        Cell nc = initCell(gen, mod(c.x + 1, map.length), c.y, tree);
                        c.energy -= Cell.energyForGrow;
                        changes.offer(nc);
                        c.type = WOOD;
                    }
                }
            }
        }
        for (Tree tree : trees) {
            for (int j = 0; j < tree.otrs.size(); ++j) {
                Cell c = tree.otrs.get(j);
                if (c.energy >= Cell.energyForGrow) {
                    int gen = tree.genom[c.genomID][3];
                    if (gen < GENS && map[mod(c.x - 1, map.length)][c.y] == null) {
                        Cell nc = initCell(gen, mod(c.x - 1, map.length), c.y, tree);
                        c.energy -= Cell.energyForGrow;
                        changes.offer(nc);
                        c.type = WOOD;
                    }
                }
            }
        }
        for (Tree tree : trees) {
            for (int j = 0; j < tree.otrs.size(); ++j) {
                Cell c = tree.otrs.get(j);
                if (c.energy >= Cell.energyForGrow) {
                    int gen = tree.genom[c.genomID][2];
                    if (gen < GENS && c.y > 0 && map[c.x][c.y - 1] == null) {
                        Cell nc = initCell(gen, c.x, c.y - 1, tree);
                        c.energy -= Cell.energyForGrow;
                        changes.offer(nc);
                        c.type = WOOD;
                    }
                }
            }
        }
        while (!changes.isEmpty()){
            Cell c = changes.poll();
            if(map[c.x][c.y] == null) {
                map[c.x][c.y] = c;
                c.tree.otrs.add(c);
                c.tree.cells.add(c);
            }else{
                cells.add(c);
                c.tree.energy += energyForGrow;
            }
        }
        for (int i = trees.size() - 1; i >= 0; --i) {
            Tree tree = trees.get(i);
            for (int j = tree.otrs.size() - 1; j >= 0; --j) {
                if (tree.otrs.get(j).type == WOOD)
                    tree.otrs.remove(j);
            }
            tree.energy -= tree.cells.size() * Cell.energyForLive;
            ++tree.age;
            if(tree.age > 100 || tree.energy < 0){
                tree.cells.removeAll(tree.otrs);
                for(Cell c: tree.cells){
                    cells.add(c);
                    map[c.x][c.y] = null;
                }
                tree.cells.clear();
                List<Cell> seeds = tree.otrs;
                this.seeds.addAll(seeds);
                if(seeds.size() != 0)
                for(Cell c: seeds){
                    c.type = SEED;
                    c.energy = tree.energy / seeds.size();
                    initTree(tree.genom, c);
                }
                seeds.clear();
                freeTrees.add(trees.remove(i));
            }
        }
    }
    private Cell initCell(int genomID, int x, int y, Tree tree){
        if(cells.size() == 0) {
            cells.add(new Cell());
        }
//        while (cells.get(cells.size() - 1) == null){
//            cells.remove(cells.size() - 1);
//            if(cells.size() == 0) {
//                cells.add(new Cell());
//            }
//        }
        Cell c = cells.remove(cells.size() - 1);
        c.x = x;
        c.y = y;
        c.energy = 0;
        c.genomID = genomID;
        c.tree = tree;
        c.type = OTR;
        return c;
    }
    public void initTree(int[][] genom, Cell seed){
        if(freeTrees.size() == 0)
            freeTrees.add(new Tree());
        Tree tree = freeTrees.remove(freeTrees.size() - 1);
        tree.c = Color.BLUE;
        seed.genomID = 0;
        tree.cells.clear();
        tree.otrs.clear();
        tree.energy = seed.energy;
        seed.energy = 0;
        tree.age = 0;
        for(int i = 0; i < genom.length; ++i){
            System.arraycopy(genom[i], 0, tree.genom[i], 0, genom[0].length);
        }
        seed.energy = 0;
        if(Math.random() < 0.25) {
            int genomID = (int) (Math.random() * genom.length);
            int genID = (int) (Math.random() * genom[0].length);
            int gen = (int) (Math.random() * genom.length * 2);
            tree.genom[genomID][genID] = gen;
        }
        tree.cells.add(seed);
        seed.tree = tree;
        trees.add(tree);
    }
    private int mod(int a, int b){
        int c = a % b;
        if(c < 0)
            c += b;
        return c;
    }
    public void render(int type, Batch batch){
//        for(Tree tree: trees){
//            for(Cell cell: tree.cells){
//                cell.render(type, batch);
//            }
//        }
        drawLines(batch);
        for (Cell[] value : map) {
            for (int j = value.length - 1; j >= 0; --j) {
                if (value[j] != null) {
                    value[j].render(type, batch);
                }
            }
        }
        batch.drawString(0,-5,"" + trees.size());
        if(map[x][y] != null){
            batch.drawString(3,-5, Arrays.deepToString(map[x][y].tree.genom));
        }

    }
    private void drawLines(Batch batch){
        batch.setColor(Color.GRAY);
        for(int i = 0; i < map.length+1; ++i){
            batch.drawLine(i, 0, i, map[0].length);
        }
        for(int i = 0; i < map[0].length + 1; ++i){
            batch.drawLine(0, i, map.length, i);
        }
    }
    public void clear(){

    }
    int x; int y;
    public void setSelected(int x, int y){
        this.x = x;
        this.y = y;
    }
}
