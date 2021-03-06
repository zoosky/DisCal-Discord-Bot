package com.cloudcraftgaming.discal;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.message.MessageManager;
import com.cloudcraftgaming.discal.api.network.google.Authorization;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.utils.ExceptionHandler;
import com.cloudcraftgaming.discal.bot.internal.consolecommand.ConsoleCommandExecutor;
import com.cloudcraftgaming.discal.bot.internal.network.discordpw.UpdateListData;
import com.cloudcraftgaming.discal.bot.listeners.ReadyEventListener;
import com.cloudcraftgaming.discal.bot.module.command.*;
import com.cloudcraftgaming.discal.web.utils.SparkUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Nova Fox on 1/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("ThrowableNotThrown")
public class Main {
	public static String version = "1.2.0";
    public static IDiscordClient client;

	public static void main(String[] args) throws IOException {
        //Get bot settings
		Properties p = new Properties();
		p.load(new FileReader(new File("settings.properties")));
		BotSettings.init(p);

		client = createClient(BotSettings.TOKEN.get());
        if (client == null)
            throw new NullPointerException("Failed to log in! Client cannot be null!");

		UpdateListData.init();

		Authorization.getAuth().init();

        //Connect to MySQL
		DatabaseManager.getManager().connectToMySQL();
        DatabaseManager.getManager().createTables();

		//Start spark (catch any issues from it so only the site goes down without affecting bot....
		try {
			SparkUtils.initSpark();
		} catch (Exception e) {
			ExceptionHandler.sendException(null, "'Spark ERROR' by 'PANIC! AT THE WEBSITE'", e, Main.class);
		}

        //Register events
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new ReadyEventListener());

        //Register modules
        CommandExecutor executor = CommandExecutor.getExecutor().enable();
        executor.registerCommand(new HelpCommand());
        executor.registerCommand(new DisCalCommand());
        executor.registerCommand(new CalendarCommand());
        executor.registerCommand(new AddCalendarCommand());
        executor.registerCommand(new LinkCalendarCommand());
        executor.registerCommand(new TimeCommand());
        executor.registerCommand(new EventListCommand());
        executor.registerCommand(new EventCommand());
        executor.registerCommand(new RsvpCommand());
        executor.registerCommand(new AnnouncementCommand());
        executor.registerCommand(new DevCommand());

		//Load language files.
		MessageManager.loadLangs();

		//Accept commands
		ConsoleCommandExecutor.init();
	}

	/**
     * Creates the DisCal bot client.
     *
     * @param token The Bot Token.
     * @return The client if successful, otherwise <code>null</code>.
     */
    private static IDiscordClient createClient(String token) {
        ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
        clientBuilder.withToken(token).withRecommendedShardCount(); // Adds the login info to the builder
        try {
			return clientBuilder.login();
        } catch (DiscordException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets The {@link IUser} Object of DisCal.
     *
     * @return The {@link IUser} Object of DisCal.
     */
    public static IUser getSelfUser() {
        return client.getOurUser();
    }
}