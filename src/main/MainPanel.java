package main;

import framesLib.Screen;
import map.Map;
import map.Tree;
import threads.Tasks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class MainPanel extends Screen {
    public Map map;
    public Map laboratory;
    public Batch batch;
    public JToggleButton btn_timer;
    private int paint_type;
    private int delay = 125;
    private boolean running = true;
    private Runnable update;
    private Runnable lab_update;
    public int[][] lab_genom;

    public MainPanel() {
        setLayout(null);
        map = new Map(256, 100, 0);
        map.clear();
        lab_genom = new int[Tree.GENS][4];
        laboratory = new Map(150, 100, -180);
        Timer painter = new Timer(33, e -> repaint());
        Tasks tasks = new Tasks(2);
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
            while (running && laboratory.cycle < 100) {
                if (delay != 0) {
                    laboratory.updatePainting();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    laboratory.update();
                }
            }
        };
        tasks.runTask(update);
        tasks.runTask(lab_update);
        painter.start();
        batch = new Batch(this, 720);
        btn_timer = new JToggleButton("Timer");
        btn_timer.addActionListener((e) -> {
            if (btn_timer.isSelected()) {
                running = true;
                tasks.runTask(update);
                tasks.runTask(lab_update);
            } else {
                running = false;
            }
        });
        add(btn_timer);
        btn_timer.setBounds(5, 5, 100, 30);
        btn_timer.setSelected(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getModifiersEx() != 0)
                    return;
                int x = (int) batch.getMX();
                int y = (int) batch.getMY();
                map.setSelected(x, y);
                if (laboratory.cycle < 100) {
                    laboratory.cycle = 100;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                int[][] gen = map.getGenom();
                if(gen != null) {
                    for (int i = 0; i < gen.length; ++i) {
                        System.arraycopy(gen[i], 0, lab_genom[i], 0, gen[i].length);
                    }
                }
                laboratory.externalMakeSeed(lab_genom);
                tasks.runTask(lab_update);
            }
        });
        setBackground(Color.BLACK);
        JSlider speed = new JSlider(0, 10, 3);
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
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                map.clear();
                running = true;
                tasks.runTask(update);
                tasks.runTask(lab_update);
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
        btn_net.setSelected(true);
        btn_net.setBounds(5, 295, 100, 30);
        btn_net.addActionListener((e) -> {
            map.drawNet = btn_net.isSelected();
            laboratory.drawNet = btn_net.isSelected();
        });
        add(btn_net);
        JButton btn_lab = new JButton("Lab genom");
        btn_lab.setBounds(5, 330, 100, 30);
        btn_lab.addActionListener((e) -> {

        });
        add(btn_lab);
    }

    @Override
    public void onSetSize() {
        setSize(1280, 720);
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
            laboratory.render(paint_type, batch);
        }
        batch.setColor(Color.WHITE);
        long t = System.currentTimeMillis();
        long delta = t - lastTime;
        if (delta > 1000) {
            lastTime = t;
            long cycle = map.cycle;
            long cc = cycle - lastCycle;
            lastCycle = cycle;
            speed = 1000 * cc / delta;
        }
        batch.drawStringOnScreen(5, 140, "Cycle: " + map.cycle);
        batch.drawStringOnScreen(5, 120, "Speed: " + speed);
        int y0 = 600;
        for (int i = 0; i < lab_genom.length; ++i) {
            batch.drawStringOnScreen(5 + 70*i, y0, i + ":");
            batch.drawStringOnScreen( 35 + 70*i, y0 - 15, "" + lab_genom[i][0]);
            batch.drawStringOnScreen(45 + 70*i, y0, "" + lab_genom[i][1]);
            batch.drawStringOnScreen(35 + 70*i, y0 + 15, "" + lab_genom[i][2]);
            batch.drawStringOnScreen(25 + 70*i, y0, "" + lab_genom[i][3]);
        }
    }
}
