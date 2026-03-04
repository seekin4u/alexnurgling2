package nurgling.tasks;

import haven.*;
import nurgling.NUtils;

import java.util.ArrayList;

public class FindWidget extends NTask
{
    public FindWidget(String name, String widgetType)
    {
        this.name = name;
        this.widgetType = widgetType;
    }

    String name;
    String widgetType;
    ArrayList<Widget> result = new ArrayList<>();

    @Override
    public boolean check()
    {
        Window wnd = NUtils.getGameUI().getWindow(name);
        if (wnd == null)
            return false;
        if (widgetType.equals("wnd"))
            return true;
        Class<? extends Widget> targetClass = resolve(widgetType);
        if (targetClass == null)
            return false;
        result.clear();
        for (Widget w2 = wnd.lchild; w2 != null; w2 = w2.prev)
        {
            if (targetClass.isInstance(w2))
                result.add(w2);
        }
        return !result.isEmpty();
    }

    public ArrayList<Widget> getResult()
    {
        return result;
    }

    private static Class<? extends Widget> resolve(String type)
    {
        switch (type)
        {
            case "btn":  return Button.class;
            case "lbl":  return Label.class;
            case "chk":  return CheckBox.class;
            case "text": return TextEntry.class;
            default:     return null;
        }
    }
}
