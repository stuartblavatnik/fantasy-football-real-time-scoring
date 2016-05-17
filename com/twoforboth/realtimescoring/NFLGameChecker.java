package com.twoforboth.realtimescoring;

import com.twoforboth.realtimescoring.data.NFLGame;
import com.twoforboth.realtimescoring.data.NFLTeam;
import com.twoforboth.realtimescoring.statparser.HTMLConsts;
import com.twoforboth.realtimescoring.statparser.YahooFinalStatParser;
import com.twoforboth.realtimescoring.statparser.YahooLiveStatParser;
import com.twoforboth.realtimescoring.statparser.StatParser;

import com.twoforboth.communication.Servlet;
import com.twoforboth.communication.ServletInfo;
import com.twoforboth.communication.CantConnectException;

import com.twoforboth.realtimescoring.events.GameStatsEvent;
import com.twoforboth.realtimescoring.events.GameStatsListener;
import com.twoforboth.realtimescoring.events.GameStartedEvent;
import com.twoforboth.realtimescoring.events.GameEndedEvent;
import com.twoforboth.realtimescoring.events.GameInitializedEvent;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Vector;

/**
 * <p>Title: NFLGameChecker.java</p>
 * <p>Description: Thread that parses HTML pages to retrieve NFL game stats for<BR>
 * one game. Uses the strategy pattern to assign a particular parser based on NFL game
 * state.  If the game has ended it uses YahooFinalStatParser otherwise it uses
 * YahooLiveStatParser.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class NFLGameChecker implements Runnable, HTMLConsts
{
  private final String SCORE_BOARD =
      "http://sports.yahoo.com/nfl/scoreboard/week"; //Base string for main scoreboard area (should actually be a paramter in the constructor)
  private final String HTML_EXTENSION = ".html";     //Should be in a base class
  private final String SCORES =
      "http://sports.yahoo.com/nfl/scores/";         //Individual score area (should be either passed or put in a base class)

  private  Servlet getURLServlet_ =
      new Servlet(ServletInfo.GET_URL_NAME,
		  ServletInfo.GET_URL_PARAMETERS);   //Servlet to read in a URL from another machine (for applet security)

  private Servlet getServerTimeServlet_ =
      new Servlet(ServletInfo.GET_SERVER_TIME,
                  "");                               //Servlet that retrieves the server's current time

  private final long LIVE_GAME_WAIT  = 120000L;        //Time between reading new stats for a game in progress (One minute)
  private final long FINAL_GAME_WAIT = 300000L;      //Time between reading end of game stats -- time between Yahoo changing format from live to end (Five minutes)

  private final String SCOREBOARD_NFL_TEAMS_AREA_STRING = "/NFL/TEAMS/";
  private final String SCOREBOARD_FINAL_AS_F_STRING = "<TD>F</TD>";
  private final String SCOREBOARD_FINAL_AS_F_OT_STRING = "<TD>F<BR>OT</TD>";
  private final String SCOREBOARD_FINAL_AS_FINAL_STRING = "<TD>FINAL</TD>";

  private StatParser parser_ = null;                 //Parser to read in the HTML pages containing the stats (can be live or end)
  private String scoreboardURL_ = "";                //Main scoreboard area URL (week must be appended to it for Yahoo)
  private String scoresURL_ = "";                    //Individual NFL game URL
  private String homeTeamLongName_ = "";             //Full name for home NFL team
  private String visitingTeamLongName_ = "";         //Full name for away NFL team
  private String homeTeamShortName_ = "";            //Short name for home NFL team
  private String visitingTeamShortName_ = "";        //Short name for away NFL team
  private ArrayList stats_ = new ArrayList();        //Array of all of the stats for this game
  private NFLGame nflGame_ = null;                   //NFLGame object
  private static int week_ = 0;                      //Current week
  private Date gameDate_ = null;                     //Full date / time of NFL game

  //Thread
  private boolean keepAlive_ = true;                 //Flag controling if thread is running
  private long sleepTime_ = 100L;                    //Default time between thread invocations

  //Listeners
  private Vector gameStatsListeners_ = new Vector(); //Objects listening for events that this thread generates

  private boolean init_ = true;               //Flag to indicate when the first stats are retrieved
  private long count_ = 0L;                   //Number of times this thread has run
  private boolean gameHadNotStarted_ = false; //Flag indicating that when this object was created the game had not started yet
  private boolean notifiedLive_ = false;      //Flag indicating when liveStats have been notified for this game

  /**
   * Constructor
   * @param nflGame NFLGame object
   * @param week Current week
   */
  public NFLGameChecker(NFLGame nflGame, int week)
  {
    nflGame_ = nflGame;
    week_ = week;
    //Build the URL string to get the scoreboard using the week
    scoreboardURL_ = SCORE_BOARD + week_ + HTML_EXTENSION;
    //Get home and away teams from NFL Game object
    visitingTeamShortName_ = nflGame_.getVisitingTeamShortName();
    homeTeamShortName_ = nflGame_.getHomeTeamShortName();
    visitingTeamLongName_ = nflGame_.getVisitingTeamLongName();
    homeTeamLongName_ = nflGame_.getHomeTeamLongName();
    //Get the time that the game starts
    long gameDate = nflGame_.getGameDate();
    Date date = new Date(gameDate);
    //Convert to YYYYMMDD for URL
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(date);
    gameDate_ = date;

    String year = new Integer(cal.get(cal.YEAR)).toString();
    String month = new Integer(cal.get(cal.MONTH) + 1).toString();
    String day = new Integer(cal.get(cal.DATE)).toString();

    if (month.length() == 1)
    {
      month = "0" + month;
    }

    if (day.length() == 1)
    {
      day = "0" + day;
    }

    String gameDateString = year + month + day;

    //Some teams are different from standard shortnames
    visitingTeamShortName_ = YahooFinalStatParser.getWebName(visitingTeamShortName_);
    homeTeamShortName_ = YahooFinalStatParser.getWebName(homeTeamShortName_);
    //Build URL
    scoresURL_ = SCORES + gameDateString + "/" + visitingTeamShortName_ +
		 homeTeamShortName_ + HTML_EXTENSION;
    //May not need this
    //getURLServlet_.setDebug(true);
    //Indicate that this data should be encoded (it's HTML)
    getURLServlet_.setWantEncoded(true);
  }

  //Getters
  public String getHomeTeamLongName() { return homeTeamLongName_; }
  public String getVisitingTeamLongName() { return visitingTeamLongName_; }
  public String getVisitingTeamShortName() { return visitingTeamShortName_; }
  public String getHomeTeamShortName() { return homeTeamShortName_; }
  public boolean getInit() { return init_; }

  /**
   * Main thread.  Determines if the game has started.  If not, it puts the
   * thread to sleep for that many milliseconds.  If it has started, it
   * determines if the game has ended by reading information from the main
   * scoreboard.  If it has ended, it reads in the stats one last time and kills
   * its own thread.  Otherwise it reads the stats and sleeps for a given amount
   * of time.  If the game is over and there is an error in reading the stats,
   * this means the game has just ended and the stats provider is currently
   * updating their data.  In this case, the thread sleeps for a longer period
   * of time to check again.  Once stats are read, this object triggers an event
   * that new stats have arrived for this game.
   */

  public void run()
  {
    try
    {
      while(keepAlive_)
      {
	try
	{
	  Thread.sleep(sleepTime_);
	}
	catch (java.lang.InterruptedException ie)
	{
	}
	if (count_ > 0)
	{
	  init_ = false;
	}
	//Determine if the game has started or finished
	checkGameStatus();
	if (nflGame_.getStarted())
	{
	  if (count_ == 0)
	  {
	    notifyGameInitialized();
	  }

	  if (gameHadNotStarted_ == true)
	  {
	    //Mark as such so that this code is not run again
	    gameHadNotStarted_ = false;

	    /**
	     * @todo Generate an event here to indicate that the game has started
	     * so that the tables can be highlighted indicating that the game has
	     * started
	     */
	    notifyGameStarted();

	  }
	  if (nflGame_.getFinished())
	  {
	    //Check one last time (or first time if application brought up after game ended)
	    if (checkGame())
	    {
	      notifyNewGameStats();
	      keepAlive_ = false;         //Turn off this thread
/*
	      System.out.println("Final for " +
				 nflGame_.getHomeTeamLongName() +
				 " vs " +
				 nflGame_.getVisitingTeamLongName() +
				 " all went well turned off thread");
*/
	      /**
               * @todo Generate game is finished event here so that tables can be
	       * modified to reflect this state
	       */
	      notifyGameEnded();

	    }
	    else
	    {
/*
	      System.out.println("Error found in game " +
				 nflGame_.getHomeTeamLongName() +
				 " vs " +
				 nflGame_.getVisitingTeamLongName());
*/
	      //Probably Changing the stats, so wait until change is made and check again
	      sleepTime_ = FINAL_GAME_WAIT;
	    }
	  }
	  else
	  {
	    //Game is currently live, mark thread to wake up in n milliseconds
	    sleepTime_ = LIVE_GAME_WAIT;
	    //Get the stats
	    if (checkGame())
	    {
	      //Indicate that new stats have arrived
	      notifyNewGameStats();

	      if (notifiedLive_ == false)
	      {
		notifyGameStarted();
	      }
	    }
	  }
	}
	else
	{
	  long timeTillGame = timeTillGame();
	  if (timeTillGame == Long.MAX_VALUE)
	  {
	    //Problem...set timer to check again
	    sleepTime_ = LIVE_GAME_WAIT;
	  }
	  else
	  {
	    //Sleep until game time
	    sleepTime_ = timeTillGame;
	    //Indicate that the game had not started yet
	    gameHadNotStarted_ = true;
	    //System.out.println("Set sleepTime_ to " + sleepTime_);
	    notifyGameInitialized();
	  }
	}
      }
    }
    catch (java.lang.IllegalArgumentException iae)
    {
      sleepTime_ = LIVE_GAME_WAIT;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Determines if the game started yet by getting the time from the webserver
   * and comparing it to the game start time from the database.
   * @returns True if the game has started
   */

  private boolean gameStarted()
  {
    boolean retval = false;

    try
    {
      Date date = new Date(new Long(getServerTimeServlet_.execute()).longValue());
      if (date.after(gameDate_))
      {
	retval = true;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return retval;
  }

  /**
   * Returns the time until the game begins in milliseconds
   * @returns Number of milliseconds until the game begins (or Long.MAX_VALUE
   * if a problem ocurred)
   */

  private long timeTillGame()
  {
    long retval = 0;

    try
    {
      Date date = new Date(new Long(getServerTimeServlet_.execute()).longValue());
      retval = gameDate_.getTime() - date.getTime();
    }
    catch (Exception e)
    {
      retval = Long.MAX_VALUE;
    }

    return retval;
  }

  /**
   * Determines if a game has started or finished by first checking the game
   * time and then checking the main scoreboard.  Modifies internal NFLGame object
   */
  public void checkGameStatus()
  {
    //Check server time vs. game time
    if (gameStarted())
    {
      String newBuffer;

      //Indicate that the game has begin
      nflGame_.setStarted(true);
      StringBuffer sb = new StringBuffer();
      sb.append(scoreboardURL_);
      //Read the main scoreboard
      try
      {
	newBuffer = getURLServlet_.execute(sb.toString()).toUpperCase();
      }
      catch (CantConnectException cce)
      {
	newBuffer = "";
      }
      if (newBuffer.length() > 0)
      {
	//Determine if the game has ended
	if (gameEnded(newBuffer, visitingTeamShortName_, homeTeamShortName_))
	{
	  nflGame_.setFinished(true);
	}
      }
    }
  }

  /**
   * Assigns the proper parser based on the game status and parses the game
   * @returns True if stats were parsed properly, false indicates a problem
   */

  public boolean checkGame()
  {
    boolean retval = false;

    //Read in the individual game stats
    try
    {
      String newBuffer = "";
      StringBuffer sb = new StringBuffer();
      sb.append(scoresURL_);
      try
      {
	newBuffer = getURLServlet_.execute(sb.toString()).toUpperCase();
      }
      catch (CantConnectException cce)
      {
	newBuffer = "";
      }

      //Need to choose which parser to instantiate based on the status of the game
      if (nflGame_.getFinished())
      {
	parser_ = new YahooFinalStatParser(homeTeamLongName_,
					   visitingTeamLongName_,
					   newBuffer);
      }
      else if (nflGame_.getStarted())
      {
	parser_ = new YahooLiveStatParser(homeTeamLongName_,
					  visitingTeamLongName_,
                                          newBuffer,
					  homeTeamShortName_,
                                          visitingTeamShortName_);
      }
      else
      {
	parser_ = null;
      }
      if (parser_ != null)
      {
	//Parse the stats
	retval = parser_.parse();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return retval;
  }

  /**
   * Retrives stats for a particular player (not currently being used)
   * @param playerName Name of player
   * @param nflTeam Team for player (may need multiple versions for Yahoo)
   * @param nameNoMiddleInitial Player name with first and last only
   * @returns array of filled score objects
   */

  public ArrayList getStatsForPlayer(String playerName,
				     String nflTeam,
                                     String nameNoMiddleInitial)
  {
    //Clear out the old stats
    stats_.clear();
    if (parser_ != null)
    {
      stats_ = parser_.getScoresForPlayer(playerName, nflTeam, nameNoMiddleInitial);
    }
    return stats_;
  }

  /**
   * Retrives all of the stats for this game
   * @returns array of filled score objects
   */

  public ArrayList getStats()
  {
    //Clear out the old stats
    stats_.clear();
    //Get the new stats
    stats_ = parser_.getScores();
    //Return the stats
    return stats_;
  }

  /**
   * Determines if the game has ended by looking for certain strings in the Yahoo
   * scoreboard page
   * @param buffer Scoreboard HTML Page contents
   * @param visitingTeam Team name
   * @param homeTeam Team name
   * @returns true if the game has ended
   */

  private boolean gameEnded(String buffer,
			    String visitingTeamShortName,
			    String homeTeamShortName)
  {
    /* <a href=/nfl/teams/nor/>New&nbsp;Orleans</a>
       </b>&nbsp;at<br>
       <a href=/nfl/teams/chi/>Chicago</a>
       </td>
       <td>
       <b>29</b><br>23
       </td>
       <td>F</td>
       </tr>
       <tr bgcolor=eeeeee>
       <td colspan=3><nobr><font size=-1>
       <a href=/nfl/recaps/20020922/norchi.html>Recap</a>&nbsp;|&nbsp;
       <a href=/nfl/scores/20020922/norchi.html>Box Score</a><br></font></nobr>
       </td>
       </tr>
       </table>
    */

    //1) get index of /nfl/teams/visitingTeam    A
    //2) get index of </table>                   B
    //3) get index of <td>F</td>                 C
    //4) If index C != -1 and index C < index B return true

    boolean retval = false;

    int indexA = buffer.indexOf(SCOREBOARD_NFL_TEAMS_AREA_STRING + visitingTeamShortName);
    int indexB = buffer.indexOf(HTML_TABLE_END, indexA);
    int indexC = buffer.indexOf(SCOREBOARD_FINAL_AS_F_STRING, indexA);
    if (indexC == -1 || indexC > indexB)
    {
      indexC = buffer.indexOf(SCOREBOARD_FINAL_AS_F_OT_STRING, indexA);
    }
    if (indexC == -1 || indexC > indexB)
    {
      indexC = buffer.indexOf(SCOREBOARD_FINAL_AS_FINAL_STRING, indexA);
    }
    //MAY HAVE TO LOOK AT THIS TOO <TD>END<BR>4TH</TD>

    if (indexC != -1 && (indexC < indexB))
    {
      retval = true;
    }

    return retval;
  }

  /**
   * Adds a listener for this objects game stats events
   * @param l GameStatsListener
   */

  public synchronized void addGameStatsListener(GameStatsListener l)
  {
    //add a listener if it is not already registered
    if (!gameStatsListeners_.contains(l))
    {
      gameStatsListeners_.add(l);
    }
  }

  /**
   * Removes a listener for this objects game stats events
   * @param l GameStatsListener
   */

  public synchronized void removeGameStatsListener(GameStatsListener l)
  {
    //remove listener if registered
    if (gameStatsListeners_.contains(l))
    {
      gameStatsListeners_.remove(l);
    }
  }

  /**
   * Notifies all of the listeners when new game stats have arriced
   */
  protected void notifyNewGameStats()
  {
    //Create an event object
    GameStatsEvent evt = new GameStatsEvent(this, this);
    //Make a copy of the listener vector so that it cannot be changed while firing events
    Vector v;
    synchronized(this)
    {
      v = (Vector)gameStatsListeners_.clone();
    }
    //Fire the event to all listeners
    int cnt = v.size();
    for (int i = 0; i < cnt; i++)
    {
      GameStatsListener client = (GameStatsListener)v.elementAt(i);
      client.newStatsArrived(evt);
    }
    if (count_ == 0)
    {
      count_++;        //Bumping count from 0 indicates that initialization mode is complete
    }
  }

  /**
   * Notifies all of the listeners when new game stats have arriced
   */
  protected void notifyGameStarted()
  {
    //Indicate that this method has been called once
    notifiedLive_ = true;
    //Create an event object
    GameStartedEvent evt = new GameStartedEvent(this, this);
    //Make a copy of the listener vector so that it cannot be changed while firing events
    Vector v;
    synchronized(this)
    {
      v = (Vector)gameStatsListeners_.clone();
    }
    //Fire the event to all listeners
    int cnt = v.size();
    for (int i = 0; i < cnt; i++)
    {
      GameStatsListener client = (GameStatsListener)v.elementAt(i);
      client.gameStarted(evt);
    }
    if (count_ == 0)
    {
      count_++;        //Bumping count from 0 indicates that initialization mode is complete
    }
  }

  /**
   * Notifies all of the listeners when new game stats have arriced
   */
  protected void notifyGameEnded()
  {
    //Create an event object
    GameEndedEvent evt = new GameEndedEvent(this, this);
    //Make a copy of the listener vector so that it cannot be changed while firing events
    Vector v;
    synchronized(this)
    {
      v = (Vector)gameStatsListeners_.clone();
    }
    //Fire the event to all listeners
    int cnt = v.size();
    for (int i = 0; i < cnt; i++)
    {
      GameStatsListener client = (GameStatsListener)v.elementAt(i);
      client.gameEnded(evt);
    }
    if (count_ == 0)
    {
      count_++;        //Bumping count from 0 indicates that initialization mode is complete
    }
  }

  /**
   * Notifies all of the listeners when game has been checked (started or not) for initialization purposes
   */
  protected void notifyGameInitialized()
  {
    //Create an event object
    GameInitializedEvent evt = new GameInitializedEvent(this, this);
    //Make a copy of the listener vector so that it cannot be changed while firing events
    Vector v;
    synchronized(this)
    {
      v = (Vector)gameStatsListeners_.clone();
    }
    //Fire the event to all listeners
    int cnt = v.size();
    for (int i = 0; i < cnt; i++)
    {
      GameStatsListener client = (GameStatsListener)v.elementAt(i);
      client.gameInitialized(evt);
    }
    if (count_ == 0)
    {
      count_++;        //Bumping count from 0 indicates that initialization mode is complete
    }
  }
}