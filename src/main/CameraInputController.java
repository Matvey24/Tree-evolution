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
    private int offsetX;
    private int offsetY;
    public CameraInputController(Runnable repaint, Runnable resize, int offsetX, int offsetY) {
        this.repaint = repaint;
        this.resize = resize;
        this.x = 0;
        this.y = -20;
        this.scale = 5;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        px = e.getX();
        py = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int sx = e.getX() - offsetX;
        int sy = 720 - e.getY()+offsetY;
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
        if(e.getModifiersEx() != InputEvent.BUTTON3_DOWN_MASK)
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
        return px - offsetX;
    }

    public int getPy() {
        return py - offsetY;
    }
}
