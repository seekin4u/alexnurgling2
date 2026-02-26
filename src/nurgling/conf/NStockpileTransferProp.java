package nurgling.conf;

import nurgling.NConfig;
import nurgling.NUI;
import nurgling.NUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class NStockpileTransferProp implements JConf {
    final private String username;
    final private String chrid;
    public String itemAlias = null;
    public String pileResName = null;

    public NStockpileTransferProp(String username, String chrid) {
        this.username = username;
        this.chrid = chrid;
    }

    public NStockpileTransferProp(HashMap<String, Object> values) {
        chrid = (String) values.get("chrid");
        username = (String) values.get("username");
        if (values.get("itemAlias") != null)
            itemAlias = (String) values.get("itemAlias");
        if (values.get("pileResName") != null)
            pileResName = (String) values.get("pileResName");
    }

    public static void set(NStockpileTransferProp prop) {
        ArrayList<NStockpileTransferProp> props = (ArrayList<NStockpileTransferProp>) NConfig.get(NConfig.Key.stockpiletransferprop);
        if (props != null) {
            for (Iterator<NStockpileTransferProp> i = props.iterator(); i.hasNext(); ) {
                NStockpileTransferProp old = i.next();
                if (old.username.equals(prop.username) && old.chrid.equals(prop.chrid)) {
                    i.remove();
                    break;
                }
            }
        } else {
            props = new ArrayList<>();
        }
        props.add(prop);
        NConfig.set(NConfig.Key.stockpiletransferprop, props);
    }

    public static NStockpileTransferProp get(NUI.NSessInfo sessInfo) {
        if (sessInfo == null || NUtils.getGameUI() == null || NUtils.getGameUI().getCharInfo() == null)
            return null;
        String chrid = NUtils.getGameUI().getCharInfo().chrid;
        ArrayList<NStockpileTransferProp> props = (ArrayList<NStockpileTransferProp>) NConfig.get(NConfig.Key.stockpiletransferprop);
        if (props == null)
            props = new ArrayList<>();
        for (NStockpileTransferProp prop : props) {
            if (prop.username.equals(sessInfo.username) && prop.chrid.equals(chrid))
                return prop;
        }
        return new NStockpileTransferProp(sessInfo.username, chrid);
    }

    @Override
    public String toString() {
        return "NStockpileTransferProp[" + username + "|" + chrid + "]";
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "NStockpileTransferProp");
        json.put("username", username);
        json.put("chrid", chrid);
        json.put("itemAlias", itemAlias);
        json.put("pileResName", pileResName);
        return json;
    }
}
