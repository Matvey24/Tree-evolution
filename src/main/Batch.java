package main;

import java.awt.*;

public class Batch {
    Graphics g;
    float scale;
    float x;
    float y;
    private CameraInputController controller;
    public Batch(MainPanel panel, int offsetX, int offsetY){
        controller = new CameraInputController(panel::repaint, ()->{
            this.x = controller.getX();
            this.y = controller.getY();
            this.scale = controller.getScale();
        }, offsetX, offsetY);
        panel.addMouseListener(controller);
        panel.addMouseMotionListener(controller);
        panel.addMouseWheelListener(controller);
    }
    public void start(Graphics g){
        this.g = g;
        this.x = controller.getX();
        this.y = controller.getY();
        this.scale = controller.getScale();
    }
    public void drawColor(float x, float y, float x2, float y2){
        int dx = (int)((x-this.x) * scale);
        int dy = (int)((this.y-y) * scale);
        int dw = (int)((x2-this.x) * scale);
        int dh = (int)((this.y-y2) * scale);
        g.fillRect(dx + 1, dy + 1 + 720, dw-dx - 1, dh-dy - 1);
    }
    public void drawLine(float x, float y, float x2, float y2){
        g.drawLine((int)((x-this.x)*scale), (int)((this.y-y)*scale)+720,
                (int)((x2-this.x)*scale), (int)((this.y-y2)*scale)+720);
    }
    public void drawString(float x, float y, String text){
        g.drawString(text, (int)((x-this.x)*scale), (int)((this.y-y)*scale)+720);
    }
    public float getMX(){
        return controller.getPx() / scale + x;
    }
    public float getMY(){
        return (720-controller.getPy()) / scale + y;
    }
    public void setColor(Color c){
        g.setColor(c);
    }
}
