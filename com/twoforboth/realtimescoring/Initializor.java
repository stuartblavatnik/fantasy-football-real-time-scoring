package com.twoforboth.realtimescoring;

import com.twoforboth.communication.Servlet;
import com.twoforboth.communication.ServletInfo;
import com.twoforboth.communication.CantConnectException;
import com.twoforboth.realtimescoring.data.FantasyTeam;
import com.twoforboth.realtimescoring.data.FantasyTeams;
import com.twoforboth.realtimescoring.data.NFLTeam;
import com.twoforboth.realtimescoring.data.NFLTeams;
import com.twoforboth.realtimescoring.data.ScoringRule;
import com.twoforboth.realtimescoring.data.ScoringRules;
import com.twoforboth.realtimescoring.data.NFLPlayer;
import com.twoforboth.realtimescoring.data.Score;
import com.twoforboth.realtimescoring.data.NFLGame;
import com.twoforboth.realtimescoring.data.NFLGames;
import com.twoforboth.realtimescoring.NFLGameCheckers;
import com.twoforboth.realtimescoring.data.FilledScore;
import com.twoforboth.realtimescoring.data.FilledScores;
import com.twoforboth.realtimescoring.data.FantasyTeamMatchup;
import com.twoforboth.realtimescoring.data.FantasyTeamMatchups;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Date;
import java.text.DateFormat;
import java.util.Locale;
import java.util.GregorianCalendar;
import java.util.Calendar;

import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.lang.Thread;

/**
 * <p>Title: Initializor.java</p>
 * <p>Description: Bridge between the data and the user interface.  This class
 * acts as a facade with regard to data initialization from the database via
 * servlets and data retrieval from the NFLGame checkers.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class Initializor
{
  private int currentWeek_ = 0;               //Week for stats
  private ScoringRules scoringRules_ = null;  //Array of scoring rules
  private String userName_ = "";              //User name
  private String leagueName_ = "";            //League name
  private String leagueYear_ = "";            //League year
  private NFLGameCheckers gameCheckers_ =     //Array of game checkers
      new NFLGameCheckers();
  private FantasyTeams fantasyTeams_ = null;  //Array of fantasy teams
  private NFLGames nflGames_ = null;          //Array of NFL Games
  private NFLTeams nflTeams_ = null;          //Array of NFL Teams
  private FilledScores filledScores_ = null;  //Array of calculated scores for all of the games
  private FantasyTeamMatchups fantasyTeamMatchups_ =
      new FantasyTeamMatchups();              //Array of fantasy team matchups

  private  Servlet loginServlet_ =
      new Servlet(ServletInfo.DO_LOGIN_SERVLET_NAME,
		  ServletInfo.DO_LOGIN_SERVLET_PARAMETERS);  //Servlet to log the user into the system

  private  Servlet getCurrentWeekServlet_ =
      new Servlet(ServletInfo.GET_CURRENT_WEEK_SERVLET_NAME,
		  ServletInfo.GET_CURRENT_WEEK_SERVLET_PARAMETERS );  //Servlet to retrieve the current week from the database

  /**
   * Initializes the object.  Get the following from the database: current week,
   * fantasy teams, lineups, nfl teams, nfl games, scoring rules and fantasy team
   * matchups.
   * @param leagueName Name of league
   * @param leagueYear Year of league
   */

  public void doInit(String leagueName, String leagueYear)
  {
    /**
     * @todo This function should return a boolean if all went ok.  Each fill
     * function should also return a boolean
     */

    leagueName_ = leagueName;
    leagueYear_ = leagueYear;

    //Get the current week from the database
    currentWeek_ = getCurrentWeek();
//    currentWeek_ = 3;
    System.out.println("Current week = " + currentWeek_);
    //Get the NFL Teams and their corresponding numbers from the database
    nflTeams_ = new NFLTeams(leagueYear_, leagueName_);
    nflGames_ = new NFLGames(leagueYear_, leagueName_);
    nflGames_.initializeMatchups(currentWeek_);
    //Get the scoring rules from the database
    //Build the Scoring Rules objects
    scoringRules_ = new ScoringRules(leagueName_, leagueYear_);
    //Get each fantasy team's lineup for the week from the database and
    //update the fantasy team lineup arraylist
    fantasyTeams_ = new FantasyTeams(leagueName_,
				     leagueYear_,
				     currentWeek_,
				     nflTeams_);

    fantasyTeams_.init();
    fantasyTeams_.fillLineups();
    //Get the fantasy matchups from the database Build the NFLGame objects
    fantasyTeams_.initializeFantasyMatchups();
    //Also build the fantasyTeamMatchup objects
    fillFantasyTeamMatchups();

    filledScores_ = new FilledScores();
    //Use fantasyTeams to fill FilledScores
    initializeFilledScores();

    fillNFLGameCheckers();
  }

  /**
   * Fills the fantasy team matchup object
   */

  private void fillFantasyTeamMatchups()
  {
    FantasyTeam ftHome = null;
    FantasyTeam ftAway = null;
    //Get Number of Fantasy Teams
    int fantasyTeamCount = getFantasyTeamCount();
    int j;
    boolean []used = new boolean[fantasyTeamCount];
    for (int i = 1; i < fantasyTeamCount; i++)
    {
      j = i - 1;
      if (used[j] == false)
      {
	ftHome = fantasyTeams_.getAt(j);
	used[j] = true;
	ftAway = fantasyTeams_.getAt(ftHome.getOpponent() -1);
	used[ftHome.getOpponent() -1] = true;
	FantasyTeamMatchup ftm = new FantasyTeamMatchup(ftHome, ftAway);
	fantasyTeamMatchups_.add(ftm);
      }
    }
  }

  /**
   * Retrieves the number of fantasy team matchups
   * @returns number of fantasy team matchups
   */

  public int getFantasyTeamMatchupsCount()
  {
    return fantasyTeamMatchups_.getSize();
  }

  /**
   * Retrieves an individual fantasy team matchup
   * @param i index into the FantasyTeamMatchups object
   * @returns An individual FantasyTeamMatchup object
   */

  public FantasyTeamMatchup getFantasyTeamMatchup(int i)
  {
    return fantasyTeamMatchups_.getAt(i);
  }

  /**
   * Calculates an individual score for a stat
   * @param sc Score object representing a raw score
   * @param fc FilledScore object representing a processed score
   */

  private void doScore(Score sc, FilledScore fc)
  {
    int value = scoringRules_.doScore(sc.getLength(),
				      sc.getType(),
				      fc.getPosition());
    sc.setWorth(value);
  }

  /**
   * Processes all of the scores for a particular NFLGame
   * @param gc NFLGameChecker object
   */

  public void doScoresForGame(NFLGameChecker gc)
  {
    ArrayList stats = new ArrayList();
    FilledScore fc = null;
    String playerName = "";
    String nflTeam = "";
/*
    System.out.println("Welcome to doScoresForGame() " +
		       gc.getHomeTeamLongName() + " vs " +
		       gc.getVisitingTeamLongName());
*/
    //Zero the points allowed for each NFLTeam
    filledScores_.zeroPointsForTeam(gc.getHomeTeamLongName());
    filledScores_.zeroPointsForTeam(gc.getVisitingTeamLongName());

    //Retrieve all of the raw stats from the game checker
    stats = gc.getStats();
    ListIterator li = stats.listIterator();
    while (li.hasNext())
    {
      Score sc = (Score)li.next();
      playerName = sc.getName();
      nflTeam = sc.getNFLTeam();
      //Find the filled score object
      if ((fc = filledScores_.findFilledScore(playerName, nflTeam)) != null)
      {
	//Calculate the individual socre
	doScore(sc, fc);
	//Add the points that the score was worth to the FilledScore object
	fc.addPoints(sc.getWorth());
      }
    }
  }

  //Getters
  public FilledScores getFilledScores() { return filledScores_; }
  public int getGameCheckersSize() { return gameCheckers_.getSize(); }
  public NFLGameChecker getNextGameChecker() { return gameCheckers_.getNext(); }
  public int getFantasyTeamCount() { return fantasyTeams_.getSize(); }
  //Setters
  public void setLeagueYear(String leagueYear) { leagueYear_ = leagueYear; }
  public void setLeagueName(String leagueName) { leagueName_ = leagueName; }
  public void setUserName(String userName) { userName_ = userName; }
  public void setFirstGameChecker() { gameCheckers_.setFirst(); }

  /**
   * Retrieves the filled scores for a particular game
   * @param gc NFLGameChecker
   * @returns Array of filled scores as a FilledScores object
   */

  public FilledScores getFilledScoresForGame(NFLGameChecker gc)
  {
    FilledScores filledScoresForGame = new FilledScores();
    FilledScore filledScore = null;
    String homeTeamLongName = gc.getHomeTeamLongName();
    String visitingTeamLongName = gc.getVisitingTeamLongName();
    //Get all for home team
    //Reset list pointer
    filledScores_.first();
    while ((filledScore =
	    filledScores_.findNextFilledScoreForNFLTeam(homeTeamLongName))
	    != null)
    {
      filledScoresForGame.add(filledScore);
    }
    //Get all for away team
    //Reset list pointer
    filledScores_.first();
    while ((filledScore =
	    filledScores_.findNextFilledScoreForNFLTeam(visitingTeamLongName))
	    != null)
    {
      filledScoresForGame.add(filledScore);
    }
    return filledScoresForGame;
  }

  /**
   * Attempts to log the user into the system
   * @param leagueYear league year
   * @param leagueName league name
   * @param userName user name
   * @param password user password
   * @returns true if successful login
   */

  public boolean doLogin(String leagueYear,
			 String leagueName,
			 String userName,
			 String password)
  {
    boolean retval = false;
    String loginResult = "";
    StringBuffer sb = new StringBuffer();

    sb.setLength(0);
    sb.append(leagueYear);
    sb.append(ServletInfo.DELIMITER);
    sb.append(leagueName);
    sb.append(ServletInfo.DELIMITER);
    sb.append(userName);
    sb.append(ServletInfo.DELIMITER);
    sb.append(password);

    try
    {
      loginResult = loginServlet_.execute(sb.toString());
      if (loginResult.compareToIgnoreCase("Y") == 0)
      {
	retval = true;
	setLeagueYear(leagueYear);
	setLeagueName(leagueName);
	setUserName(userName);
      }
    }
    catch (CantConnectException cce)
    {
      retval = false;
    }
    return retval;
  }

  /**
   * Calls a servlet to get the current week from the database
   * @returns Current week (0 if error)
   */

  private int getCurrentWeek()
  {
    int retval = 0;
    StringBuffer sb = new StringBuffer();

    sb.append(leagueYear_);
    sb.append(ServletInfo.DELIMITER);
    sb.append(leagueName_);

    try
    {
      retval = new Integer(getCurrentWeekServlet_.execute(sb.toString())).intValue();
    }
    catch (CantConnectException cce)
    {
      retval = 0;
    }
    catch (Exception e)
    {
      retval = 0;
    }

    return retval;
  }

  /**
   * Initializes the objects that get the status for each NFL game
   */

  private void fillNFLGameCheckers()
  {
    //Go through each game [this should be done initially on startup]
    NFLGame nflGame = null;
    nflGames_.first();
    while((nflGame = nflGames_.getNext()) != null)
    {
      NFLTeam homeTeam = nflTeams_.getNFLTeamFromNumber(nflGame.getHomeNumber());
      NFLTeam visitingTeam = nflTeams_.getNFLTeamFromNumber(nflGame.getVisitorNumber());
      if (visitingTeam != null)         //exclude the byes
      {
	nflGame.setHomeTeamLongName(homeTeam.getLongName());
	nflGame.setHomeTeamShortName(homeTeam.getShortName());
	nflGame.setVisitingTeamLongName(visitingTeam.getLongName());
	nflGame.setVisitingTeamShortName(visitingTeam.getShortName());

	NFLGameChecker gameChecker = new NFLGameChecker(nflGame, currentWeek_);

	gameCheckers_.add(gameChecker);
      }
      else       //10/12/02 -- fill in the rest of the nflGame information for the home team
      {
	nflGame.setHomeTeamLongName(homeTeam.getLongName());
	nflGame.setHomeTeamShortName(homeTeam.getShortName());
      }
    }
  }

  public boolean nflTeamOnBye(String teamName)
  {
    return nflGames_.teamOnBye(teamName);
  }

  /**
   * Create FilledScore objects from weekly lineups
   */

  private void initializeFilledScores()
  {
    FantasyTeam fantasyTeam = null;
    ListIterator weeklyLineupIterator = null;
    NFLPlayer nflPlayer = null;
    String nflTeamLongName = "";
    StringTokenizer stringtokenizer = null;
    String lastName = "";
    String firstName = "";
    String firstNameNoInitial = "";
    String positionPlayed = "";
    FilledScore filledScore = null;
    String playerName = "";
    String playerNameNoInitial = "";
    String nflTeamAlternativeName = "";

    //Go through each fantasy team
    fantasyTeams_.setFirst();
    while ((fantasyTeam = fantasyTeams_.getNext()) != null)
    {
      //Go through each player within the fantasy team's lineup
      weeklyLineupIterator = fantasyTeam.getWeeklyLineup().listIterator();
      while (weeklyLineupIterator.hasNext())
      {
	//Get the name of the player
	nflPlayer = (NFLPlayer)weeklyLineupIterator.next();
	//Get the NFL team name
	nflTeamLongName = nflTeams_.getLongnameFromShortName(nflPlayer.getNFLTeam());
	//Get the alternative NFL team name
	nflTeamAlternativeName = nflTeams_.getAlternateName(nflTeamLongName);
	//If the name has a comma in it then the name is in the form LastName, FirstName
	if (nflPlayer.getName().indexOf(',') != -1)
	{
	  stringtokenizer = new StringTokenizer(nflPlayer.getName(), ",", false);
	  lastName = stringtokenizer.nextToken().trim();
	  firstName = stringtokenizer.nextToken().trim();
	  firstNameNoInitial = firstName;
	  //Have a middle initial try without
	  if (firstName.endsWith("."))
	  {
	    firstNameNoInitial = firstName.substring(0, firstName.length() - 3);
	  }
	  playerName = firstName + " " + lastName;
	  playerNameNoInitial = firstNameNoInitial + " " + lastName;
	}
	else
	{
	  playerName = nflPlayer.getName();
	  playerNameNoInitial = nflPlayer.getName();   //10/6/02
	}
	//Position played is for allowing players to play at different (or imaginary positions)
	positionPlayed = nflPlayer.getPosition();
	//Create the new object
	filledScore = new FilledScore(fantasyTeam.getName(),
				      playerName,
				      playerNameNoInitial,
				      nflTeamLongName,
				      nflTeamAlternativeName,
				      positionPlayed);
	//Add the new object to the array
	filledScores_.add(filledScore);
      }
    }
  }
}