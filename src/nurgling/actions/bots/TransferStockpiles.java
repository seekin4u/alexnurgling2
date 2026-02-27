package nurgling.actions.bots;

import haven.*;
import nurgling.NGameUI;
import nurgling.NISBox;
import nurgling.NUtils;
import nurgling.actions.*;
import nurgling.areas.NArea;
import nurgling.areas.NContext;
import nurgling.conf.NStockpileTransferProp;
import nurgling.tasks.WaitCheckable;
import nurgling.tools.Finder;
import nurgling.tools.NAlias;
import nurgling.tools.StockpileUtils;
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

        NAlias itemAlias = new NAlias(prop.itemAlias);

        ArrayList<Gob> gobs;
        while (!(gobs = Finder.findGobs(fromArea, new NAlias("stockpile"))).isEmpty()) {
            for (Gob pile : gobs) {
                if (PathFinder.isAvailable(pile)) {
                    Coord size = StockpileUtils.itemMaxSize.get(pile.ngob.name);
                    new PathFinder(pile).run(gui);
                    new OpenTargetContainer("Stockpile", pile).run(gui);
                    int target_size = 0;
                    while (Finder.findGob(pile.id) != null) {
                        if (getFreeSlots(gui, itemAlias, size) > 0) {
                            NISBox spbox = gui.getStockpile();
                            if (spbox != null) {
                                do {
                                    if (Finder.findGob(pile.id) == null && target_size != 0)
                                        break;
                                    target_size = getFreeSlots(gui, itemAlias, size);
                                    if (target_size == 0) {
                                        new TransferToPiles(whereArea.getRCArea(), itemAlias).run(gui);
                                        if (Finder.findGob(pile.id) != null) {
                                            new PathFinder(pile).run(gui);
                                            new OpenTargetContainer("Stockpile", pile).run(gui);
                                        } else break;
                                    } else {
                                        new TakeItemsFromPile(pile, spbox, target_size).run(gui);
                                    }
                                } while (target_size != 0);
                            }
                        } else {
                            new TransferToPiles(whereArea.getRCArea(), itemAlias).run(gui);
                            if (Finder.findGob(pile.id) != null) {
                                new PathFinder(pile).run(gui);
                                new OpenTargetContainer("Stockpile", pile).run(gui);
                            }
                        }
                        context.navigateToAreaIfNeeded(fromId);
                    }
                }
            }
        }

        return Results.SUCCESS();
    }

    private int getFreeSlots(NGameUI gui, NAlias itemAlias, Coord fallback) throws InterruptedException {
        ArrayList<WItem> items = null;
        if (items.isEmpty())
            items = gui.getInventory().getItems();
        if (!items.isEmpty())
            return gui.getInventory().getNumberFreeCoord(items.get(0));
        return gui.getInventory().getNumberFreeCoord(fallback != null ? fallback : new Coord(1, 1));
    }

}