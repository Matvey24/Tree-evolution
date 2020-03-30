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
    public void render(int type, Batch batch) {
        if(this.type == OTR)
            batch.setColor(Color.GREEN);
        else if(this.type == SEED)
            batch.setColor(Color.RED);
        else
            batch.setColor(tree.c);
        batch.drawColor(x, y, x+1,y+1);
        batch.setColor(Color.BLACK);
        if(type == 1){
            batch.drawString(x+0.1f,y, "" + genomID);
        }else if(type == 2){
            batch.drawString(x,y+0.2f, "" + ((this.type == OTR)?energy:tree.energy));
        }
    }
}
