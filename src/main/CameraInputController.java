package main;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class CameraInputController extends MouseAdapter {
    private Runnable repaint;
    private Runnable resize;
    private float x;
    private float y;
    private float scale;
    private int px;
    private int py;
    private int HEIGHT;
    public CameraInputController(Runnable repaint, Runnable resize, int height) {
        this.HEIGHT = height;
        this.repaint = repaint;
        this.resize = resize;
        this.x = -25;
        this.y = -50;
        this.scale = 4.4f;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        px = e.getX();
        py = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int sx = e.getX();
        int sy = HEIGHT - e.getY();
        float deltaScale = 1.1f;
        float deltaX = sx / scale;
        float deltaY = sy / scale;
        scale /= Math.pow(deltaScale, e.getPreciseWheelRotation());
        x -= sx / scale - deltaX;
        y -= sy / scale - deltaY;
        resize.run();
        repaint.run();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(e.getModifiersEx() != InputEvent.BUTTON1_DOWN_MASK)
            return;
        int dScreenX = px - e.getX();
        int dScreenY = py - e.getY();
        px = e.getX();
        py = e.getY();
        float dOffsetX = dScreenX / scale;
        float dOffsetY = dScreenY / scale;
        x += dOffsetX;
        y -= dOffsetY;
        resize.run();
        repaint.run();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getScale() {
        return scale;
    }

    public int getPx() {
        return px;
    }

    public int getPy() {
        return py;
    }
}
