package main;

import java.awt.*;

public class Batch {
    Graphics g;
    public float scale;
    public float x;
    public float y;
    private CameraInputController controller;
    private int HEIGHT;
    private int d = 0;
    public Batch(MainPanel panel, int height){
        this.HEIGHT = height;
        controller = new CameraInputController(panel::repaint, ()->{
            this.x = controller.getX();
            this.y = controller.getY();
            this.scale = controller.getScale();
        }, height);
        panel.addMouseListener(controller);
        panel.addMouseMotionListener(controller);
        panel.addMouseWheelListener(controller);
    }
    public void start(Graphics g){
        this.g = g;
        this.x = controller.getX();
        this.y = controller.getY();
        this.scale = controller.getScale();
        if(scale < 5)
            d = 0;
        else{
            d = 1;
        }
    }
    public void drawColor(float x, float y, float x2, float y2){
        int dx = (int)((x-this.x) * scale);
        int dy = -(int)((y-this.y) * scale);
        int dw = (int)((x2-this.x) * scale);
        int dh = -(int)((y2-this.y) * scale);
        g.fillRect(dx + d, dy + HEIGHT, dw-dx - d, dh-dy + d);
    }
    public void drawLine(float x, float y, float x2, float y2){
        g.drawLine((int)((x-this.x)*scale), (int)((this.y-y)*scale)+HEIGHT,
                (int)((x2-this.x)*scale), (int)((this.y-y2)*scale)+HEIGHT);
    }
    public void drawString(float x, float y, String text){
        g.drawString(text, (int)((x-this.x)*scale), (int)((this.y-y)*scale)+HEIGHT);
    }
    public void drawStringOnScreen(int x, int y, String text){
        g.drawString(text, x, y);
    }
    public float getMX(){
        return controller.getPx() / scale + x;
    }
    public float getMY(){
        return (HEIGHT-controller.getPy()) / scale + y;
    }
    public void setColor(Color c){
        g.setColor(c);
    }
}
