package nurgling.actions.bots;

import haven.Button;
import haven.Gob;
import haven.Label;
import haven.Widget;
import nurgling.NGameUI;
import nurgling.NUtils;
import nurgling.actions.*;
import nurgling.conf.NDiscordNotification;
import nurgling.tasks.FindWidget;
import nurgling.tasks.WaitForGobsWithNAlias;
import nurgling.tools.Finder;
import nurgling.tools.NAlias;
import nurgling.widgets.Specialisation;

import java.util.ArrayList;

public class Stoner implements Action  {
	@Override
	public Results run(NGameUI gui) throws InterruptedException {
		java.time.LocalDateTime now = java.time.LocalDateTime.now();
		log("Starting at " + now.toString());

		ArrayList<String> milestones = new ArrayList<>();
		for (Gob cc : Finder.findGobs(new NAlias("gfx/terobjs/road/milestone-stone-e"))) {
			if (cc.ngob != null && cc.ngob.hash != null) {
				milestones.add(cc.ngob.hash);
			}
		}
		gui.msg("There are " + milestones.size() + " milestones");
		log("There are " + milestones.size() + " milestones");
		for (String hash : milestones) {
			Gob gob = Finder.findGob(hash);
			if (gob == null) continue;

			new PathFinder(gob).run(gui);
			if (!(new OpenTargetWindow("Milestone", gob).run(gui).IsSuccess())) {
				log("Wasn't able to open milestone window to count buttons");
				return Results.FAIL();
			}

			FindWidget findButtons = new FindWidget("Milestone", "btn");
			NUtils.addTask(findButtons);
			ArrayList<Button> travelButtons = new ArrayList<>();
			for (Widget w : findButtons.getResult()) {
				Button btn = (Button) w;
				if (btn.text != null && "Travel".equals(btn.text.text))
					travelButtons.add(btn);
			}
			gui.msg("There are " + travelButtons.size() + " Travel buttons");
			log("There are " + travelButtons.size() + " Travel buttons");

			for (int i = 0; i < travelButtons.size(); i++) {
				if (!(new OpenTargetWindow("Milestone", gob).run(gui).IsSuccess())){
					log("Wasn't able to open milestone window to travel");
					return Results.FAIL();
				}

				FindWidget reFindBtns = new FindWidget("Milestone", "btn");
				NUtils.addTask(reFindBtns);
				ArrayList<Button> freshTravel = new ArrayList<>();
				for (Widget w : reFindBtns.getResult()) {
					Button btn = (Button) w;
					if (btn.text != null && "Travel".equals(btn.text.text))
						freshTravel.add(btn);
				}

				FindWidget reFindLabels = new FindWidget("Milestone", "lbl");
				NUtils.addTask(reFindLabels);
				ArrayList<Label> freshLabels = new ArrayList<>();
				for (Widget w : reFindLabels.getResult())
					freshLabels.add((Label) w);

				if (i >= freshTravel.size()) break;

				String roadLabel = (i < freshLabels.size()) ? freshLabels.get(i).texts : "";
				Button toClick = freshTravel.get(i);
				int wdgid = toClick.wdgid();
				log("Clicking travel button ");
				toClick.click();

				NUtils.addTask(new WaitForGobsWithNAlias(new NAlias("roadball")));
				Gob roadball = Finder.findGobAnywhere(new NAlias("roadball"));
				Thread.sleep(3000);
				if (roadball != null) {
					Gob fish = Finder.findGobAnywhere(new NAlias("caveangler"));
					if(fish != null){
						gui.msg("There is a caveangler");
						log("There is a caveangler");
						NDiscordNotification discordSettings = NDiscordNotification.get("general");
						if (discordSettings != null && discordSettings.webhookUrl != null && !discordSettings.webhookUrl.isEmpty()) {
							log("Sending discord notification");
							gui.msgToDiscord(discordSettings, "Bot [" + gui.chrid + "] found a caveangler on road: " + roadLabel + "<@&1479929946666176554>");
						}
					} else {
						log("No fish found in " + roadLabel);
					}
					NUtils.rclick(roadball.rc);
				}
				final int id = wdgid;
				travelButtons.removeIf(b -> b.wdgid() == id);
			}
		}
		Thread.sleep(5000);
		log("Exiting the client");
		System.exit(0);
		return null;
	}

	private static void log(String message) {
		System.out.println("[Stoner] " + message);
	}
}
