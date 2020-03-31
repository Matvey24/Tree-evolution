package map;

import main.Batch;

import java.awt.*;

public class Cell extends Position{
    public Tree tree;
    public static final int WOOD = 0;
    public static final int OTR = 1;
    public static final int SEED = 2;

    public static final int energyForGrow = 18;
    public static final int energyForLive = 13;

    public int energy;
    public int genomID;
    public int type;
    public void earnEnergy(int e){
        if(type == OTR)
            energy += e;
        else
            tree.energy += e;
    }
    public void render(int type, Batch batch,int startX) {
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
                batch.drawString(startX + x, y + 0.2f, "" + ((this.type == OTR) ? energy : tree.energy));
            }
        }
    }
}
