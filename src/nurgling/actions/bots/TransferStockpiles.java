package nurgling.actions.bots;

import haven.*;
import nurgling.NGameUI;
import nurgling.NUtils;
import nurgling.actions.*;
import nurgling.areas.NArea;
import nurgling.areas.NContext;
import nurgling.conf.NStockpileTransferProp;
import nurgling.tasks.WaitCheckable;
import nurgling.tools.Finder;
import nurgling.tools.NAlias;
import nurgling.widgets.bots.StockpileTransferWnd;

import java.util.ArrayList;

public class TransferStockpiles implements Action {

    @Override
    public Results run(NGameUI gui) throws InterruptedException {
        StockpileTransferWnd w = null;
        NStockpileTransferProp prop = null;
        try {
            NUtils.getUI().core.addTask(new WaitCheckable(
                    NUtils.getGameUI().add((w = new StockpileTransferWnd()), UI.scale(200, 200))));
            prop = w.prop;
        } catch (InterruptedException e) {
            throw e;
        } finally {
            if (w != null)
                w.destroy();
        }

        if (prop == null || prop.itemAlias == null || prop.itemAlias.isEmpty()
                || prop.pileResName == null || prop.pileResName.isEmpty()) {
            return Results.ERROR("No config");
        }

        NContext context = new NContext(gui);

        String fromId = context.createArea("Please, select input (from) area",
                Resource.loadsimg("baubles/inputArea"));
        NArea fromArea = context.getAreaById(fromId);

        String whereId = context.createArea("Please, select output (where) area",
                Resource.loadsimg("baubles/outputArea"));
        NArea whereArea = context.getAreaById(whereId);

        NAlias pileAlias = new NAlias(prop.pileResName);
        NAlias itemAlias = new NAlias(prop.itemAlias);

        ArrayList<Gob> piles;
        while (!(piles = Finder.findGobs(fromArea, pileAlias)).isEmpty()) {
            // Filter to reachable piles
            ArrayList<Gob> reachable = new ArrayList<>();
            for (Gob pile : piles) {
                if (PathFinder.isAvailable(pile))
                    reachable.add(pile);
            }
            if (reachable.isEmpty()) {
                NUtils.getGameUI().msg("Can't reach any pile in the from area, stopping.");
                break;
            }

            reachable.sort(NUtils.d_comp);
            Gob targetPile = reachable.get(0);

            // Navigate to and open the stockpile
            new PathFinder(targetPile, false).run(gui);
            new OpenTargetContainer("Stockpile", targetPile).run(gui);

            if (gui.getStockpile() == null) {
                NUtils.getGameUI().msg("Failed to open stockpile, skipping.");
                continue;
            }

//            if (gui.getStockpile().calcCount() == 0) {
//                new CloseTargetWindow(gui.getStockpile()).run(gui);
//                continue;
//            }

            // Take items into inventory
            new TakeItemsFromPile(targetPile, gui.getStockpile(), Integer.MAX_VALUE).run(gui);

            // Close the stockpile window if still open
//            if (gui.getStockpile() != null)
//                new CloseTargetWindow(gui.getStockpile()).run(gui);

            // Check if we got anything
            if (gui.getInventory().getItems(itemAlias).isEmpty())
                continue;

            // Navigate to output area and place items into new piles
            NUtils.navigateToArea(whereArea);
            new CreateFreePiles(whereArea.getRCArea(), itemAlias,
                    new NAlias(prop.pileResName)).run(gui);

            // Navigate back to input area for next iteration
            NUtils.navigateToArea(fromArea);
        }

        return Results.SUCCESS();
    }
}
