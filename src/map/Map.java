package map;

import main.Batch;

import java.awt.*;
import java.util.*;
import java.util.List;

import static map.Cell.*;
import static map.Tree.GENS;

public class Map {
    public Cell[][] map;
    public List<Cell> free_cells;
    public List<Tree> trees;
    public List<Tree> freeTrees;
    public List<Cell> seeds;
    public Queue<Cell> changes;
    public long cycle = 0;
    public boolean drawNet = true;
    public int startX;
    private int selectedX;
    private int selectedY;
    public Map(int x, int y, int startX) {
        map = new Cell[x][y];
        free_cells = new ArrayList<>();
        trees = new ArrayList<>();
        changes = new LinkedList<>();
        seeds = new ArrayList<>();
        freeTrees = new ArrayList<>();
        trees.add(new Tree());
        this.startX = startX;
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
                c.type = OTR; // оно теперь отросток в новом дереве
                c.tree.otrs.add(c);
                c.tree.age = 0;//молодом дереве
                c.tree.isSeed = false; // но уже не семени
            } else if (map[c.x][c.y - 1] == null) {//под семенем свободно и оно может падать
                map[c.x][c.y--] = null;
                map[c.x][c.y] = c;
            } else {//если семя упало на что-то, кроме земли
                disposeTree(c.tree);// разобрать дерево
            }
        }

        for (int i = 0; i < map.length; ++i) {
            Cell[] value = map[i];
            int n = 3;//свет проходит 3 слоя
            for (int j = value.length - 1; j >= 0; --j) {
                if (value[j] != null && value[j].type != SEED) {//семя пропускает свет и не получает энергии
                    value[j].earnEnergy((n--) * (j + 6));//
                    if (n == 0)
                        break;
                }
            }
        }
        for (Tree tree : trees) {// растем вверх
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
        for (Tree tree : trees) {// растем вправо
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
        for (Tree tree : trees) {// растем влево
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
        for (Tree tree : trees) {// растем вниз
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
        while (!changes.isEmpty()) {
            Cell c = changes.poll();
            if (map[c.x][c.y] == null) {
                map[c.x][c.y] = c;
                c.tree.otrs.add(c);
                c.tree.cells.add(c);
            } else {
                free_cells.add(c);
                c.tree.energy += energyForGrow;
            }
        }

        for (int i = trees.size() - 1; i >= 0; --i) {
            Tree tree = trees.get(i);
            for (int j = tree.otrs.size() - 1; j >= 0; --j) {
                if (tree.otrs.get(j).type == WOOD) {
                    tree.otrs.remove(j);
                }
            }
            if (!tree.isSeed)//семена не тратят энергии
                tree.energy -= tree.cells.size() * Cell.energyForLive;
            ++tree.age;
            if (tree.age > 100 || tree.energy < 0) {
                disposeTree(tree);
            }
        }
    }

    private Cell initCell(int genomID, int x, int y, Tree tree) {
        if (free_cells.size() == 0) {
            free_cells.add(new Cell());
        }
        Cell c = free_cells.remove(free_cells.size() - 1); // нашли ненужню клетку
        c.x = x;
        c.y = y;
        c.energy = 0;
        c.genomID = genomID;
        c.tree = tree;
        c.type = OTR;// вырос бы отросток
        return c;
    }

    public void initTree(int[][] genom, Cell seed) {
        if (freeTrees.size() == 0)
            freeTrees.add(new Tree());
        Tree tree = freeTrees.remove(freeTrees.size() - 1);// нашли объект дерева
        seed.genomID = 0; //ген семя всегда 0
        tree.energy = seed.energy; // передадим дереву всю энергию семя
        seed.energy = 0; // в семени не осталось энергии
        tree.age = 0; // возраст семя
        for (int i = 0; i < genom.length; ++i) {
            System.arraycopy(genom[i], 0, tree.genom[i], 0, genom[0].length);
        } // скопироваль все гены старого дерева
        if (Math.random() < 0.25) {
            int genomID = (int) (Math.random() * genom.length);
            int genID = (int) (Math.random() * genom[0].length);
            int gen = (int) (Math.random() * genom.length * 2);
            tree.genom[genomID][genID] = gen;
            tree.c = new Color(Color.HSBtoRGB((float) Math.random(), 1, 1)); // установим случайный цвет
            // изменили один ген
        } else {
            tree.c = seed.tree.c;
        }
        tree.cells.add(seed); // семя принадлежит дереву
        seed.tree = tree; // связываем
        tree.isSeed = true; // дерево пока еще семя
        trees.add(tree); // теперь дерево существует
    }

    public void disposeTree(Tree tree) {
        tree.cells.removeAll(tree.otrs);//cells - теперь только древесина и семена
        for (Cell c : tree.cells) {
            if (map[c.x][c.y] != c) {
                System.out.println("Problem with dispose cells");
            }
            map[c.x][c.y] = null; // опустошение карты от древесины и семян
            if (c.type == SEED) {
                if (!seeds.remove(c)) {
                    System.out.println("Seed is not in seeds");
                }
            }
            c.tree = null;
        }
        free_cells.addAll(tree.cells); // добавить ненужную древесину к ненужным клеткам
        tree.cells.clear(); // работа с древесиной окончена
        for (Cell c : tree.otrs) {
            c.type = SEED; // каждый отросток теперь семечко
            c.energy = Math.min(300, tree.energy / tree.otrs.size() + c.energy);// энергия семечка
            if (c.energy > 0) {//если энергия есть
                seeds.add(c); // добавим его в массив с семенами
                initTree(tree.genom, c);
            } else { //если нет, семя погибло
                free_cells.add(c);
                map[c.x][c.y] = null;
            }
        }
        tree.otrs.clear();// работа с отростками закончена

        if (!trees.remove(tree)) {
            System.out.println("Tree is not in trees on dispose");
        } // дерево больше не существует
        freeTrees.add(tree);
    }

    private int mod(int a, int b) {
        int c = a % b;
        if (c < 0)
            c += b;
        return c;
    }

    public synchronized void render(int type, Batch batch) {
        if (drawNet)
            drawLines(batch);
        for (Tree tree : trees) {
            for (int j = 0; j < tree.cells.size(); ++j) {
                tree.cells.get(j).render(type, batch, startX);
            }
        }
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

    public void clear() {
        clearMap();
        int[][] genom = new int[16][4];
        for (int[] ints : genom) {
            Arrays.fill(ints, 30);
        }
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < genom.length / 2; ++j) {
                for (int k = 0; k < genom[0].length; ++k) {
                    genom[j][k] = (int) (Math.random() * genom.length * 2);
                }
            }
            if (free_cells.size() == 0)
                free_cells.add(new Cell());
            Cell cell = free_cells.remove(free_cells.size() - 1);
            cell.x = map.length * i / 5 + 5;
            cell.y = 0;
            cell.type = SEED;
            Tree tree = freeTrees.remove(freeTrees.size() - 1);
            cell.tree = tree;
            cell.energy = 300;
            cell.tree.c = new Color(Color.HSBtoRGB((float) Math.random(), 1, 1));
            map[cell.x][cell.y] = cell;
            seeds.add(cell);
            initTree(genom, cell);
            freeTrees.add(tree);
        }
    }

    public void externalMakeSeed(int[][] genom) {
        clearMap();
        if (genom == null)
            return;
        if (free_cells.size() == 0)
            free_cells.add(new Cell());
        Cell cell = free_cells.remove(free_cells.size() - 1);
        cell.x = map.length / 2;
        cell.y = 0;
        cell.type = SEED;
        Tree tree = freeTrees.remove(freeTrees.size() - 1);
        cell.tree = tree;
        cell.energy = 300;
        cell.tree.c = new Color(Color.HSBtoRGB((float) Math.random(), 1, 1));
        map[cell.x][cell.y] = cell;
        seeds.add(cell);
        initTree(genom, cell);
        freeTrees.add(tree);
    }

    public int[][] getGenom() {
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
        cycle = 0;
        seeds.clear();
        freeTrees.addAll(trees);
        trees.clear();
    }

    public void setSelected(int x, int y) {
        if (x < 0 || x >= map.length || y < 0 || y >= map[0].length) {
            return;
        }
        this.selectedX = x;
        this.selectedY = y;
    }
}
