package nurgling.actions.bots;

import haven.*;
import nurgling.NGItem;
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
import java.util.HashSet;

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

		ArrayList<Gob> gobs;
		HashSet<String> targets = new HashSet<>();
		while(!(gobs = Finder.findGobs(fromArea, new NAlias("stockpile"))).isEmpty())
		{
			for (Gob pile : gobs) {
				if(PathFinder.isAvailable(pile))
				{
					Coord size = StockpileUtils.itemMaxSize.get(pile.ngob.name);
					new PathFinder(pile).run(gui);
					new OpenTargetContainer("Stockpile", pile).run(gui);
					int target_size = 0;
					while (Finder.findGob(pile.id) != null)
					{
						if (NUtils.getGameUI().getInventory().getNumberFreeCoord((size != null) ? size : new Coord(1, 1)) > 0)
						{
							NISBox spbox = gui.getStockpile();
							if (spbox != null)
							{
								do {
									if (Finder.findGob(pile.id) == null && target_size != 0)
										break;
									target_size = NUtils.getGameUI().getInventory().getNumberFreeCoord((size != null) ? size : new Coord(1, 1));
									if (target_size == 0) {
										new FreeInventory2(context).run(gui);
										context.navigateToAreaIfNeeded(whereId);
										targets.clear();
										if (Finder.findGob(pile.id) != null)
										{
											new PathFinder(pile).run(gui);
											new OpenTargetContainer("Stockpile", pile).run(gui);
										} else break;
									} else {
										TakeItemsFromPile tifp = new TakeItemsFromPile(pile, spbox, target_size);
										tifp.run(gui);
										for (NGItem item : tifp.newItems())
											targets.add((item).name());
									}
								}
								while (target_size != 0);
							}
						} else {
							new FreeInventory2(context).run(gui);
							context.navigateToAreaIfNeeded(whereId);
							if (Finder.findGob(pile.id) != null) {
								new PathFinder(pile).run(gui);
								new OpenTargetContainer("Stockpile", pile).run(gui);
							}
						}
						context.navigateToAreaIfNeeded(whereId);
					}
				}
			}
		}

//        ArrayList<Gob> piles;
//        while (!(piles = Finder.findGobs(fromArea, pileAlias)).isEmpty()) {
//            // Filter to reachable piles
//            ArrayList<Gob> reachable = new ArrayList<>();
//            for (Gob pile : piles) {
//                if (PathFinder.isAvailable(pile))
//                    reachable.add(pile);
//            }
//            if (reachable.isEmpty()) {
//                NUtils.getGameUI().msg("Can't reach any pile in the from area, stopping.");
//                break;
//            }
//
//            reachable.sort(NUtils.d_comp);
//            Gob targetPile = reachable.get(0);
//
//            // Navigate to and open the stockpile
//            new PathFinder(targetPile, false).run(gui);
//            new OpenTargetContainer("Stockpile", targetPile).run(gui);
//
//            if (gui.getStockpile() == null) {
//                NUtils.getGameUI().msg("Failed to open stockpile, skipping.");
//                continue;
//            }
//
////            if (gui.getStockpile().calcCount() == 0) {
////                new CloseTargetWindow(gui.getStockpile()).run(gui);
////                continue;
////            }
//
//            // Take items into inventory
//            new TakeItemsFromPile(targetPile, gui.getStockpile(), Integer.MAX_VALUE).run(gui);
//
//            // Close the stockpile window if still open
////            if (gui.getStockpile() != null)
////                new CloseTargetWindow(gui.getStockpile()).run(gui);
//
//            // Check if we got anything
//            if (gui.getInventory().getItems(itemAlias).isEmpty())
//                continue;
//
//            // Navigate to output area and place items into new piles
//            NUtils.navigateToArea(whereArea);
//            new CreateFreePiles(whereArea.getRCArea(), itemAlias,
//                    new NAlias(prop.pileResName)).run(gui);
//
//            // Navigate back to input area for next iteration
//            NUtils.navigateToArea(fromArea);
//        }

        return Results.SUCCESS();
    }
}
