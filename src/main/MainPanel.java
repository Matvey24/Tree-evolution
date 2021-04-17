package main;

import framesLib.Screen;
import map.Map;
import threads.Tasks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainPanel extends Screen {
    public Map map;
    public Map lab;
    public Batch batch;
    public JToggleButton btn_timer;
    private int paint_type;
    private int delay;
    private boolean running;
    private final Runnable update;
    private final Runnable lab_update;
    public int[][] lab_genom;
    public Tasks map_tasks;
    public Tasks lab_tasks;
    public MainPanel() {
        setLayout(null);
        Data.loadSettings();
        map = new Map(Data.MAP_WIDTH, Data.MAP_HEIGHT, 0);
        lab_genom = new int[Data.GENS][4];
        lab = new Map(Data.LAB_WIDTH, Data.LAB_HEIGHT, -Data.LAB_WIDTH - 30);
        Timer painter = new Timer(16, e -> repaint());
        lab_tasks = new Tasks();
        map_tasks = new Tasks();
        update = () -> {
            while (running) {
                if (delay != 0) {
                    map.updatePainting();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    map.update();
                }
            }
        };
        lab_update = () -> {
            while (running && lab.cycle < Data.MAX_AGE) {
                if (delay != 0) {
                    lab.updatePainting();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    lab.update();
                }
            }
        };
        batch = new Batch(this, 720);
        btn_timer = new JToggleButton("Timer");
        btn_timer.addActionListener((e) -> {
            if (btn_timer.isSelected()) {
                running = true;
                map_tasks.runTask(update);
                lab_tasks.runTask(lab_update);
            } else {
                running = false;
            }
        });
        add(btn_timer);
        btn_timer.setBounds(5, 5, 100, 30);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON3)
                    return;
                int x = (int) batch.getMX();
                int y = (int) batch.getMY();
                map.setSelected(x, y);
                if (lab.cycle < Data.MAX_AGE) {
                    lab.cycle = Data.MAX_AGE;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                int[][] gen = map.getGenom();
                if (gen != null) {
                    for (int i = 0; i < gen.length; ++i) {
                        System.arraycopy(gen[i], 0, lab_genom[i], 0, gen[i].length);
                    }
                }
                lab.externalMakeSeed(lab_genom);
                lab_tasks.runTask(lab_update);
            }
        });
        setBackground(Color.BLACK);
        JSlider speed = new JSlider(0, 10, 5);
        delay = 1000 / 32;
        speed.addChangeListener(e -> {
            int value = speed.getValue();
            float v = 1000 / (float) Math.pow(2, value);
            delay = (int) v;
        });
        speed.setBounds(5, 40, 100, 60);
        add(speed);
        speed.setMajorTickSpacing(2);
        speed.setMinorTickSpacing(1);
        speed.setPaintTicks(true);
        speed.setPaintLabels(true);
        JButton btn_clear = new JButton("Clear");
        btn_clear.setBounds(5, 150, 100, 30);
        btn_clear.addActionListener((e) -> {
            if (!running) {
                map.clear();
                repaint();
            } else {
                running = false;
                map_tasks.runTask(()->{
                    map.clear();
                    running = true;
                    map_tasks.runTask(update);
                    lab_tasks.runTask(lab_update);
                });
            }
        });
        add(btn_clear);
        JButton btn_1 = new JButton("Normal");
        btn_1.addActionListener((e) -> {
            paint_type = 0;
            repaint();
        });
        add(btn_1);
        btn_1.setBounds(5, 190, 100, 30);
        JButton btn_2 = new JButton("Genom");
        btn_2.addActionListener((e) -> {
            paint_type = 1;
            repaint();
        });
        add(btn_2);
        btn_2.setBounds(5, 225, 100, 30);
        JButton btn_3 = new JButton("Energy");
        btn_3.setBounds(5, 260, 100, 30);
        btn_3.addActionListener((e) -> {
            paint_type = 2;
            repaint();
        });
        add(btn_3);

        JToggleButton btn_net = new JToggleButton("Show net");
        btn_net.setBounds(5, 295, 100, 30);
        btn_net.addActionListener((e) -> {
            map.drawNet = btn_net.isSelected();
            lab.drawNet = btn_net.isSelected();
        });
        add(btn_net);
        JButton btn_lab = new JButton("Save gen");
        btn_lab.setBounds(5, 330, 100, 30);
        btn_lab.addActionListener(e -> Data.saveGenom(lab_genom));
        add(btn_lab);
        int[][][] genom = Data.loadGenom();
        if (genom == null) {
//            map.clear();
            map.loadMap();
        } else {
            map.clear(genom);
        }
        painter.start();
    }

    @Override
    public void onSetSize() {
        setSize(1280, 760);
    }

    @Override
    public void onHide() {
        running = false;
        map_tasks.join();
        map.saveMap();
    }

    private long lastCycle;
    private long lastTime = System.currentTimeMillis();
    private long speed;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        batch.start(g);
        if (delay > 2) {
            map.render(paint_type, batch);
            lab.render(paint_type, batch);
        }
        batch.setColor(Color.WHITE);
        long t = System.currentTimeMillis();
        long delta = t - lastTime;
        if (delta > 1000) {
            lastTime = t;
            long cycle = map.cycle;
            long cc = cycle - lastCycle;
            lastCycle = cycle;
            speed = Math.round(1000d * cc / delta);
        }
        batch.drawStringOnScreen(5, 120, "Speed: " + speed);
        batch.drawStringOnScreen(5, 140, "Cycle: " + map.cycle);
        batch.drawStringOnScreen(5, 420, "Died seeds: " + map.died_seeds);
        batch.drawStringOnScreen(5, 440, "Grown seeds: " + map.grown_seeds);
        int y0 = 600;
        if (paint_type == 1)
            for (int i = 0, j = 0; j < lab_genom.length; ++i, ++j) {
                if (i == 16) {
                    i = 0;
                    y0 += 45;
                }
                batch.drawStringOnScreen(5 + 70 * i, y0, j + ":");
                batch.drawStringOnScreen(35 + 70 * i, y0 - 15, "" + lab_genom[j][0]);
                batch.drawStringOnScreen(45 + 70 * i, y0, "" + lab_genom[j][1]);
                batch.drawStringOnScreen(35 + 70 * i, y0 + 15, "" + lab_genom[j][2]);
                batch.drawStringOnScreen(25 + 70 * i, y0, "" + lab_genom[j][3]);
            }
    }
}
