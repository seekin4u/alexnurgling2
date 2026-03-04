package nurgling.actions;

import haven.Coord;
import haven.Gob;
import nurgling.NGameUI;
import nurgling.NUtils;
import nurgling.tasks.FindWidget;
import nurgling.tasks.NTask;

import static haven.OCache.posres;

public class OpenTargetWindow implements Action
{
    public OpenTargetWindow(String name, Gob gob)
    {
        this.name = name;
        this.gob = gob;
    }

    @Override
    public Results run(NGameUI gui) throws InterruptedException
    {
        if (NUtils.getGameUI().getWindow(name) == null)
            gui.map.wdgmsg("click", Coord.z, gob.rc.floor(posres), 3, 0, 0, (int) gob.id,
                    gob.rc.floor(posres), 0, -1);
        gui.ui.core.addTask(new FindWidget(name, "wnd"));
        return Results.SUCCESS();
    }

    String name;
    Gob gob;
}
