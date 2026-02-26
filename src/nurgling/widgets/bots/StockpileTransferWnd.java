package nurgling.widgets.bots;

import haven.*;
import nurgling.NMapView;
import nurgling.NUtils;
import nurgling.conf.NStockpileTransferProp;

public class StockpileTransferWnd extends Window implements Checkable {

    private final TextEntry itemAliasEntry;
    private final TextEntry pileResEntry;
    public NStockpileTransferProp prop = null;
    private boolean isReady = false;
    private boolean selectingGob = false;
    private haven.Gob pendingGob = null;

    public StockpileTransferWnd() {
        super(new Coord(200, 200), "Stockpile Transfer");
        NStockpileTransferProp saved = NStockpileTransferProp.get(NUtils.getUI().sessInfo);

        prev = add(new Label("Item alias:"));
        prev = add(itemAliasEntry = new TextEntry(300,
                saved == null || saved.itemAlias == null ? "" : saved.itemAlias),
                prev.pos("bl").add(UI.scale(0, 5)));

        prev = add(new Label("Pile resource name:"), prev.pos("bl").add(UI.scale(0, 8)));
        prev = add(pileResEntry = new TextEntry(300,
                saved == null || saved.pileResName == null ? "" : saved.pileResName),
                prev.pos("bl").add(UI.scale(0, 5)));

        prev = add(new Button(UI.scale(150), "Select Gob") {
            @Override
            public void click() {
                super.click();
                NMapView mapView = (NMapView) NUtils.getGameUI().map;
                if (!mapView.isGobSelectionMode.get()) {
                    mapView.isGobSelectionMode.set(true);
                    selectingGob = true;
                    pendingGob = null;
                    hide();
                }
            }
        }, prev.pos("bl").add(UI.scale(0, 8)));

        prev = add(new Button(UI.scale(150), "Start") {
            @Override
            public void click() {
                super.click();
                prop = NStockpileTransferProp.get(NUtils.getUI().sessInfo);
                if (prop != null) {
                    prop.itemAlias = itemAliasEntry.text();
                    prop.pileResName = pileResEntry.text();
                    NStockpileTransferProp.set(prop);
                }
                isReady = true;
            }
        }, prev.pos("bl").add(UI.scale(0, 5)));

        pack();
    }

    @Override
    public void tick(double dt) {
        super.tick(dt);
        if (!selectingGob) return;

        NMapView mapView = (NMapView) NUtils.getGameUI().map;
        if (mapView.isGobSelectionMode.get()) return;

        // Gob selection mode just ended
        if (pendingGob == null) {
            pendingGob = mapView.selectedGob;
            mapView.selectedGob = null;
        }

        if (pendingGob == null) {
            // User cancelled without selecting
            selectingGob = false;
            show();
            return;
        }

        // Wait for gob name to be resolved
        if (pendingGob.ngob == null || pendingGob.ngob.name == null) return;

        pileResEntry.settext(pendingGob.ngob.name);
        pendingGob = null;
        selectingGob = false;
        show();
    }

    @Override
    public boolean check() {
        return isReady;
    }

    @Override
    public void wdgmsg(String msg, Object... args) {
        if (msg.equals("close")) {
            isReady = true;
            hide();
        }
        super.wdgmsg(msg, args);
    }
}
