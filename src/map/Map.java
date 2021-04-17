package map;

import main.Batch;
import main.Data;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static main.Data.GENS;
import static main.Data.MAX_AGE;
import static map.Cell.*;

public class Map {
    public Cell[][] map;
    public ArrayList<Cell> free_cells;
    public List<Tree> trees;
    public ArrayList<Tree> freeTrees;
    public List<Cell> seeds;
    public boolean drawNet;
    public int startX;
    private int selectedX;
    private int selectedY;
    public float mutation_chance;
    public Cell[][] sunLayer;

    public long cycle = 0;
    public long died_seeds = 0;
    public long grown_seeds = 0;


    public Map(int x, int y, int startX) {
        map = new Cell[x][y];
        free_cells = new ArrayList<>();
        trees = new ArrayList<>();
        seeds = new ArrayList<>();
        freeTrees = new ArrayList<>();
        this.startX = startX;
        sunLayer = new Cell[x][Data.LIGHT_LAYERS];
    }

    public synchronized void updatePainting() {
        update();
    }

    public void update() {
        ++cycle;
        for (int i = seeds.size() - 1; i >= 0; --i) {
            Cell c = seeds.get(i);
            if (c.y == 0) {//семя достигло земли
                seeds.remove(i); // семя существует только в падении
                initTree(c); // создаем дерево
                ++grown_seeds;
            } else if (map[c.x][c.y - 1] == null) {//под семенем свободно и оно может падать
                if (map[c.x][c.y] != c) {
                    System.out.println("Problem with falling seeds: map[" + c.x + "][" + c.y + "] = " + map[c.x][c.y]);
                }
                map[c.x][c.y--] = null;
                map[c.x][c.y] = c;
            } else {//если семя упало на что-то, кроме земли
                seeds.remove(i); // семя погибло
                c.tree.cells.remove(c);// удаляем семя из дерева
                if (c.tree.cells.isEmpty()) { // если в дереве ничего не осталось
                    trees.remove(c.tree); // удаляем дерево
                    freeTrees.add(c.tree);
                }
                c.tree = null;
                if (map[c.x][c.y] != c) {
                    System.out.println("Error with dead seed");
                }
                map[c.x][c.y] = null;
                free_cells.add(c);
                ++died_seeds;
            }
        }
        for (Cell[] cells : sunLayer) {
            for (int j = 0; j < cells.length; ++j) {
                if (cells[j] != null) {
                    cells[j].earnEnergy((Data.LIGHT_LAYERS - j) * 18);
                } else {
                    break;
                }
            }
        }
        for (Tree tree : trees) {// растем вверх
            for (int j = 0; j < tree.otrs.size(); ++j) {
                Cell c = tree.otrs.get(j);
                if (c.energy >= energyForGrow) {
                    int gen = tree.genom[c.genomID][0];
                    if (gen < GENS && c.y < map[0].length - 1 && map[c.x][c.y + 1] == null) {
                        Cell nc = initCell(gen, c.x, c.y + 1, tree);
                        c.energy -= energyForGrow;
                        executeGrowing(nc);
                    }
                    c.type = WOOD;
                }
            }
        }
        for (Tree tree : trees) {// растем вправо
            for (int j = 0; j < tree.otrs.size(); ++j) {
                Cell c = tree.otrs.get(j);
                if (c.energy >= energyForGrow) {
                    int gen = tree.genom[c.genomID][1];
                    if (gen < GENS && map[mod(c.x + 1, map.length)][c.y] == null) {
                        Cell nc = initCell(gen, mod(c.x + 1, map.length), c.y, tree);
                        c.energy -= energyForGrow;
                        executeGrowing(nc);
                    }
                    c.type = WOOD;
                }

            }
        }
        for (Tree tree : trees) {// растем влево
            for (int j = 0; j < tree.otrs.size(); ++j) {
                Cell c = tree.otrs.get(j);
                if (c.energy >= energyForGrow) {
                    int gen = tree.genom[c.genomID][3];
                    if (gen < GENS && map[mod(c.x - 1, map.length)][c.y] == null) {
                        Cell nc = initCell(gen, mod(c.x - 1, map.length), c.y, tree);
                        c.energy -= energyForGrow;
                        executeGrowing(nc);
                    }
                    c.type = WOOD;
                }
            }
        }
        for (Tree tree : trees) {// растем вниз
            for (int j = 0; j < tree.otrs.size(); ++j) {
                Cell c = tree.otrs.get(j);
                if (c.energy >= energyForGrow) {
                    int gen = tree.genom[c.genomID][2];
                    if (gen < GENS && c.y > 0 && map[c.x][c.y - 1] == null) {
                        Cell nc = initCell(gen, c.x, c.y - 1, tree);
                        c.energy -= energyForGrow;
                        executeGrowing(nc);
                    }
                    c.type = WOOD;
                }
            }
        }

        for (int i = trees.size() - 1; i >= 0; --i) {
            Tree tree = trees.get(i);
            if (tree.isSeed)//семена не тратят энергию и жизнь впустую
                continue;
            for (int j = tree.otrs.size() - 1; j >= 0; --j) {
                if (tree.otrs.get(j).type == WOOD) {
                    tree.energy += tree.otrs.get(j).energy;
                    tree.otrs.remove(j);
                }
            }
            tree.energy -= tree.cells.size() * Cell.energyForLive;
            ++tree.age;
            if (tree.age > MAX_AGE || tree.energy < 0) {
                treeDie(tree);
            }
        }
    }

    private void executeGrowing(Cell c) {
        map[c.x][c.y] = c;
        c.tree.otrs.add(c);
        c.tree.cells.add(c);
        int i = 0;
        for (; i < Data.LIGHT_LAYERS; ++i) {
            if (sunLayer[c.x][i] == null || sunLayer[c.x][i].y < c.y) {
                break;
            }
        }
        if (Data.LIGHT_LAYERS > i) {
            System.arraycopy(sunLayer[c.x], i, sunLayer[c.x], i + 1, Data.LIGHT_LAYERS - i - 1);
            sunLayer[c.x][i] = c;
        }
    }

    private void reloadLayer(int x_start, int x_end) {
        for (int j = x_start; j <= x_end; ++j) {
            int n = Data.LIGHT_LAYERS;
            for (int i = map[j].length - 1; i >= 0; --i) {
                if (map[j][i] != null && map[j][i].type != SEED) {
                    sunLayer[j][Data.LIGHT_LAYERS - n] = map[j][i];
                    --n;
                    if (n == 0)
                        break;
                }
            }
            for (int i = 0; i < n; ++i) {
                sunLayer[j][Data.LIGHT_LAYERS - i - 1] = null;
            }
        }
    }

    private Cell initCell(int genomID, int x, int y, Tree tree) {//добывляем клетку дереву
        if (free_cells.size() == 0) {
            free_cells.add(new Cell());
        }
        Cell c = free_cells.remove(free_cells.size() - 1); // нашли ненужную клетку
        if (map[c.x][c.y] == c) {
            System.out.println("Found copy of cell");
            if (c.tree == null) {
                System.out.println("this sell is a problem of the next error");
            }
        }
        c.x = x;
        c.y = y;
        c.energy = 0;

        c.genomID = genomID;
        c.tree = tree;
        c.type = OTR;// вырос бы отросток

        return c;
    }

    public void initTree(Cell seed) {//создаем свое дерево для семени
        int[][] genom = seed.tree.genom;
        if (freeTrees.size() == 0)
            freeTrees.add(new Tree());
        Tree tree = freeTrees.remove(freeTrees.size() - 1);// нашли объект дерева
        seed.genomID = 0; //ген семя всегда 0
        tree.age = 0; // возраст семя
        for (int i = 0; i < genom.length; ++i) {
            System.arraycopy(genom[i], 0, tree.genom[i], 0, 4);
        } // скопироваль все гены старого дерева
        if (Math.random() < mutation_chance) {
            int genomID = (int) (Math.random() * (genom.length));
            int genID = (int) (Math.random() * 4);
            int gen = (int) (Math.random() * genom.length * 2);
            tree.genom[genomID][genID] = gen; // изменили один ген
            tree.c = new Color(Color.HSBtoRGB((float) Math.random(), 1, 1)); // установим случайный цвет
        } else {
            tree.c = seed.tree.c;
        }
        tree.isSeed = false; // дерево - не семя

        seed.energy = Math.min(MAX_ENERGY, seed.tree.energy / seed.tree.cells.size());
        seed.tree.energy -= seed.energy;
        tree.energy = 0;

        seed.tree.cells.remove(seed);
        if (seed.tree.cells.isEmpty()) {
            trees.remove(seed.tree);
            freeTrees.add(seed.tree);
        }

        seed.tree = tree; // связываем
        tree.cells.add(seed); // семя принадлежит дереву
        seed.type = OTR;
        tree.otrs.add(seed);
        trees.add(tree); // теперь дерево существует

        reloadLayer(seed.x, seed.x); // обновить освещение
    }

    public void treeDie(Tree tree) {
        if (tree.energy >= 0) {
            tree.cells.removeAll(tree.otrs);
//            choosing:
//            {
//                if(tree.otrs.isEmpty())
//                    break choosing;
//                Cell a = tree.otrs.get(0), b;
//                int index = 0;
//                for (int i = tree.otrs.size() - 1; i >= 0; --i) {
//                    Cell c = tree.otrs.get(i);
//                    if (a.y < c.y) {
//                        a = c;
//                        index = i;
//                    }
//                }
//                tree.otrs.remove(index);
//                if(tree.otrs.isEmpty()) {
//                    tree.otrs.add(a);
//                    tree.cells.remove(a);
//                    break choosing;
//                }
//                b = tree.otrs.get(0);
//                index = 0;
//                for (int i = tree.otrs.size() - 1; i >= 0; --i) {
//                    Cell c = tree.otrs.get(i);
//                    if (b.y < c.y) {
//                        b = c;
//                        index = i;
//                    }
//                }
//                tree.otrs.remove(index);
//                tree.otrs.clear();
//                tree.otrs.add(a);
//                tree.otrs.add(b);
//                tree.cells.removeAll(tree.otrs);
//            }
        } else {
            tree.otrs.clear();
        }
        int x_start = map.length, x_end = -1;
        for (Cell c : tree.cells) {
            int x = c.x;
            if (map[x][c.y] != c) {
                System.out.println("Problem with dispose cells: map[" + x + "][" + c.y + "] = " + map[x][c.y]);
            } else
                map[x][c.y] = null; // опустошение карты от погибшей древесины
            c.tree = null;
            if (x_start > x)
                x_start = x;
            if (x_end < x)
                x_end = x;
        }
        free_cells.addAll(tree.cells);
        tree.cells.clear();

        if (tree.otrs.isEmpty()) { // от дерева вообще ничего не осталось
            trees.remove(tree);
            freeTrees.add(tree);
            reloadLayer(x_start, x_end);// обновление света
            return;
        }
        for (Cell c : tree.otrs) {
//            c.energy = MAX
            c.type = SEED;
            int x = c.x;
            if (x_start > x)
                x_start = x;
            if (x_end < x)
                x_end = x;
        }
        reloadLayer(x_start, x_end);// обновление света

        seeds.addAll(tree.otrs);
        tree.cells.addAll(tree.otrs);
        tree.otrs.clear();
        tree.isSeed = true;
        //теперь в дереве есть только семена
    }

    private int mod(int a, int b) {
        int c = a % b;
        if (c < 0)
            c += b;
        return c;
    }

    public synchronized void render(int type, Batch batch) {
        if (drawNet && batch.scale > 5)
            drawLines(batch);
        else
            drawCircuit(batch);
        if (batch.scale <= 10)
            type = 0;
//        for(int i = 0; i < map.length; ++i){
//            for(int j = 0; j < map[i].length; ++j){
//                if(map[i][j] != null)
//                    map[i][j].render(type, batch, startX);
//            }
//        }
        for (Tree tree : trees) {
            for (int j = 0; j < tree.cells.size(); ++j) {
                tree.cells.get(j).render(type, batch, startX);
            }
        }
    }

    private void drawCircuit(Batch batch) {
        batch.setColor(Color.DARK_GRAY);
        batch.drawLine(startX, 0, startX + map.length, 0);
        batch.drawLine(startX, map[0].length, startX + map.length, map[0].length);
        batch.drawLine(startX, 0, startX, map[0].length);
        batch.drawLine(startX + map.length, 0, startX + map.length, map[0].length);
    }

    private void drawLines(Batch batch) {
        batch.setColor(Color.DARK_GRAY);
        for (int i = 0; i < map.length + 1; ++i) {
            batch.drawLine(i + startX, 0, i + startX, map[0].length);
        }
        for (int i = 0; i < map[0].length + 1; ++i) {
            batch.drawLine(startX, i, map.length + startX, i);
        }
    }

    public void clear(int[][][] genom) {
        if (genom[0].length > GENS)
            throw new RuntimeException("Too long genom");
        mutation_chance = Data.FIGHT_MUTATION_CHANCE;
        clearMap();
        for (int i = 0; i < genom.length; ++i) {
            if (free_cells.size() == 0)
                free_cells.add(new Cell());
            if (freeTrees.isEmpty())
                freeTrees.add(new Tree());

            Cell cell = free_cells.remove(free_cells.size() - 1);
            cell.x = map.length * i / genom.length + 5;
            cell.y = 0;
            map[cell.x][cell.y] = cell;

            cell.type = OTR;
            cell.energy = MAX_ENERGY;
            cell.genomID = 0;

            Tree tree = freeTrees.remove(freeTrees.size() - 1);
            tree.isSeed = false;
            tree.energy = 0;
            tree.age = 0;

            tree.c = new Color(Color.HSBtoRGB((float) Math.random(), 1, 1));
            tree.genom = genom[i];

            cell.tree = tree;
            tree.cells.add(cell);
            tree.otrs.add(cell);

            trees.add(tree);
        }
    }

    public void clear() {
        int[][][] genom = new int[5][GENS][4];
        for (int i = 0; i < 5; ++i)
            for (int j = 0; j < GENS; ++j)
                for (int k = 0; k < 4; ++k)
                    genom[i][j][k] = (int) (Math.random() * GENS * 2);
        clear(genom);
        mutation_chance = Data.MUTATION_CHANCE;
    }

    public void externalMakeSeed(int[][] genom) {
        clearMap();
        if (genom == null)
            return;
        if (free_cells.size() == 0)
            free_cells.add(new Cell());
        if (freeTrees.isEmpty())
            freeTrees.add(new Tree());

        Cell cell = free_cells.remove(free_cells.size() - 1);
        cell.x = map.length / 2;
        cell.y = 0;
        cell.genomID = 0;
        cell.energy = MAX_ENERGY;
        cell.type = OTR;
        Tree tree = freeTrees.remove(freeTrees.size() - 1);
        cell.tree = tree;
        tree.c = new Color(Color.HSBtoRGB((float) Math.random(), 1, 1));
        tree.genom = genom;
        tree.age = 0;
        tree.otrs.add(cell);
        tree.cells.add(cell);
        map[cell.x][cell.y] = cell;
        reloadLayer(cell.x, cell.x);
        trees.add(tree);
    }

    public int[][] getGenom() {
        if (selectedX < 0 || selectedY < 0 || selectedX >= map.length || selectedY >= map[0].length)
            return null;
        if (map[selectedX][selectedY] != null)
            return map[selectedX][selectedY].tree.genom;
        else
            return null;
    }

    private void clearMap() {
        for (Tree tree : trees) {
            for (int j = 0; j < tree.cells.size(); ++j) {
                Cell c = tree.cells.get(j);
                map[c.x][c.y] = null;
                free_cells.add(c);
            }
            tree.cells.clear();
            tree.otrs.clear();
        }
        reloadLayer(0, map.length - 1);
        cycle = 0;
        died_seeds = 0;
        grown_seeds = 0;
        seeds.clear();
        freeTrees.addAll(trees);
        trees.clear();
    }

    public void saveMap() {
        try {
            DataBase.trees = trees;
            DataBase.cycle = cycle;
            DataBase.died_seeds = died_seeds;
            DataBase.grown_seeds = grown_seeds;
            DataBase.mutation_chance = mutation_chance;
            DataBase.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap() {
        clearMap();
        try {
            DataBase.load();
            this.trees = DataBase.trees;
            this.cycle = DataBase.cycle;
            died_seeds = DataBase.died_seeds;
            grown_seeds = DataBase.grown_seeds;
            this.mutation_chance = DataBase.mutation_chance;
        } catch (FileNotFoundException e) {
            System.out.println("Creating new data");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        for (Tree t : trees) {
            if (t.isSeed) {
                seeds.addAll(t.cells);
            }
            for (Cell c : t.cells) {
                if (map[c.x][c.y] != null) {
                    System.out.println("Problem with loading: map[" + c.x + "][" + c.y + "] = " + map[c.x][c.y]);
                } else
                    map[c.x][c.y] = c;
            }
            reloadLayer(0, map.length - 1);
        }
    }

    public void setSelected(int x, int y) {
        this.selectedX = x;
        this.selectedY = y;
    }
}
