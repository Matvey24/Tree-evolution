package map;

import main.Batch;

import java.awt.*;

public class Cell extends Position{
    public Tree tree;
    public static final byte WOOD = 0;
    public static final byte OTR = 1;
    public static final byte SEED = 2;
    public static final int MAX_ENERGY = 300;
    public static final int energyForGrow = 18;
    public static final int energyForLive = 13;

    public int energy;
    public int genomID;
    public byte type;
    public void earnEnergy(int e){
        if(type == OTR)
            energy += e;
        else
            tree.energy += e;
    }
    public void render(int type, Batch batch, int startX) {
        if(this.type == OTR || this.type == SEED)
            batch.setColor(Color.WHITE);
        else
            batch.setColor(tree.c);
        batch.drawColor(startX + x, y, startX + x+1,y+1);
        if(type != 0) {
                batch.setColor(Color.BLACK);
            if (type == 1) {
                batch.drawString(startX + x + 0.1f, y, "" + genomID);
            } else if (type == 2) {
                batch.drawString(startX + x, y + 0.2f, "" + ((this.type == WOOD) ? tree.energy : energy));
            }
        }
    }

    @Override
    public String toString() {
        return "Cell{" +
                "type=" + type +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
