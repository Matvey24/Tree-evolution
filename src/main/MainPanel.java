package main;

import framesLib.Screen;
import map.Map;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainPanel extends Screen {
    public Map map;
    public Batch batch;
    public JToggleButton btn_timer;
    private int paint_type;
    public MainPanel() {
        setLayout(null);
        map = new Map(256,100);
        Timer t = new Timer(1000,(e)->{
           map.update();
           repaint();
        });
        batch = new Batch(this, 120, 5);
        btn_timer = new JToggleButton("Timer");
        btn_timer.addActionListener((e)->{
            if(btn_timer.isSelected()){
                t.start();
            }else{
                t.stop();
            }
        });
        add(btn_timer);
        btn_timer.setBounds(5,5,100,30);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getModifiers() != InputEvent.BUTTON1_MASK)
                    return;
                int x = (int)batch.getMX();
                int y = (int)batch.getMY();
                map.setSelected(x,y);
                repaint();
            }
        });
        setBackground(Color.DARK_GRAY);
        JSlider speed = new JSlider(0,100,1);
        speed.addChangeListener(e -> {
            int value = speed.getValue();
            value = Math.max(value, 1);
            t.setDelay(1000/value);
        });
        speed.setBounds(5,40,100,60);
        add(speed);
        speed.setMajorTickSpacing(50);
        speed.setMinorTickSpacing(10);
        speed.setPaintTicks(true);
        speed.setPaintLabels(true);
        JButton btn_clear = new JButton("Clear");
        btn_clear.setBounds(5,150,100,30);
        btn_clear.addActionListener((e)->{map.clear();repaint();});
        add(btn_clear);
        JButton btn_1 = new JButton("Normal");
        btn_1.addActionListener((e)->{paint_type = 0; repaint();});
        add(btn_1);
        btn_1.setBounds(5, 190, 100, 30);
        JButton btn_2 = new JButton("Genom");
        btn_2.addActionListener((e)->{paint_type = 1; repaint();});
        add(btn_2);
        btn_2.setBounds(5, 225, 100, 30);
        JButton btn_3 = new JButton("Energy");
        btn_3.addActionListener((e)->{paint_type = 2; repaint();});
        add(btn_3);
        btn_3.setBounds(5, 260, 100, 30);
    }

    @Override
    public void onSetSize() {
        setSize(1280,720);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        batch.start(g);
        g.translate(120, 5);
        map.render(paint_type,batch);
        g.translate(-120,-5);
    }
}
