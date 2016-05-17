package com.twoforboth.realtimescoring.data;

import com.twoforboth.communication.Servlet;
import com.twoforboth.communication.ServletInfo;
import com.twoforboth.communication.CantConnectException;
import com.twoforboth.realtimescoring.data.FantasyTeam;

import com.twoforboth.realtimescoring.data.NFLTeams;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * <p>Title: FantasyTeams.java</p>
 * <p>Description: Array of fantasy teams and the methods to retrive them.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class FantasyTeams
{
  private String leagueName_ = "";          //League name
  private String leagueYear_ = "";          //League year
  private ListIterator li_ = null;          //Array iterator

  //Servlet to retrieve the fantasy team names
  private  Servlet getFantasyTeamsServlet_ =
      new Servlet(ServletInfo.GET_FANTASY_TEAMS_SERVLET_NAME,
		  ServletInfo.GET_FANTASY_TEAMS_SERVLET_PARAMETERS);

  //Servlet to retrieve the fantasy team lineups
  private  Servlet getFantasyTeamWeeklyLineupServlet_ =
      new Servlet(ServletInfo.GET_FANTASY_TEAM_WEEKLY_LINEUP_SERVLET_NAME ,
		  ServletInfo.GET_FANTASY_TEAM_WEEKLY_LINEUP_SERVLET_PARAMETERS);

  //Servlet to retrieve the fantasy team matchups
  private  Servlet getFantasyTeamMatchupsServlet_ =
      new Servlet(ServletInfo.GET_FANTASY_MATCHUPS_SERVLET_NAME,
		  ServletInfo.GET_FANTASY_MATCHUPS_SERVLET_PARAMETERS);

  private StringTokenizer st_ = null;                    //String tokenizer
  private StringTokenizer ist_ = null;                   //String tokenizer
  private ArrayList fantasyTeamList_ = new ArrayList();  //Array to hold fantasy teams
  private int currentWeek_ = 0;                          //League week
  private NFLTeams nflTeams_ = null;                     //Array of NFL Teams

  /**
   * Constructor -- initializes variables
   * @param leagueName Name of league
   * @param leagueYear Year of league
   * @param currentWeek League week
   * @param nflTeams NFL Teams collection
   */
  public FantasyTeams(String leagueName,
		      String leagueYear,
		      int currentWeek,
		      NFLTeams nflTeams)
  {
    leagueName_ = leagueName;
    leagueYear_ = leagueYear;
    currentWeek_ = currentWeek;
    nflTeams_ = nflTeams;
  }

  /**
   * Initializes the fantasy team objects from the database
   */

  public void init()
  {
    createFantasyTeamObjects(getFantasyTeams());
  }

  /**
   * Retrieves the size of the array of FantasyTeam objects
   * @returns size
   */

  public int getSize() { return fantasyTeamList_.size(); }

  /**
   * Retrieve a particular fantasy team
   * @param i Fantasy team number
   * @returns FantasyTeam object
   */

  public FantasyTeam getAt(int i)
  {
    return (FantasyTeam)fantasyTeamList_.get(i);
  }

  /**
   * Retrieves the lineup for a fantasy team as an array of NFL players
   * @param i Fantasy team number
   * @returns Array of NFLPlayer objects
   */

  public ArrayList getFantasyTeamLineup(int i)
  {
    return ((FantasyTeam)fantasyTeamList_.get(i)).getWeeklyLineup();
  }

  /**
   * Resets the iteration to the first item in the array
   */

  public void setFirst() { li_ = fantasyTeamList_.listIterator(); }

  /**
   * Retrieves the next fantasy team in the array
   * @returns FantasyTeam object or null if end of array is found
   */

  public FantasyTeam getNext()
  {
    FantasyTeam ft = null;

    if (li_.hasNext())
    {
      ft = (FantasyTeam)li_.next();
    }
    return ft;
  }

  /**
   * Retrieves and fills the fantasy team lineups
   */

  public void fillLineups()
  {
    FantasyTeam ft = null;
    setFirst();
    while ((ft = getNext()) != null)
    {
      ft.fillLineup(getFantasyTeamLineupServlet(ft.getNumber()), nflTeams_);
    }
  }

  /**
   * Retrieves and populates the fantasy team matchups
   */

  public void initializeFantasyMatchups()
  {
    fillFantasyMatchups(getFantasyTeamMatchups());
  }

  /**
   * Calls servlet to retrieve the fantasy team lineup for a particular fantasy team
   * @param fantasyTeamNumber Team number
   * @returns delimited string containing the entire fantasy team lineup
   */

  private String getFantasyTeamLineupServlet(int fantasyTeamNumber)
  {
    String retval = "";
    StringBuffer sb = new StringBuffer();
//$leagueName, $leagueYear, $fantasyTeamNumber, $week
    sb.append(leagueYear_);
    sb.append(ServletInfo.DELIMITER);
    sb.append(leagueName_);
    sb.append(ServletInfo.DELIMITER);
    sb.append(fantasyTeamNumber);
    sb.append(ServletInfo.DELIMITER);
    sb.append(currentWeek_);

    try
    {
      //getFantasyTeamWeeklyLineupServlet_.setDebug(true);
      retval = getFantasyTeamWeeklyLineupServlet_.execute(sb.toString());
    }
    catch (CantConnectException cce)
    {
      retval = "";
    }

    return retval;
  }

  /**
   * Kludge to get Yahoo team names -- should not be here
   * @param orig original team name
   * @returns New Team name
   */

  protected static String getYahooLongName(String orig)
  {
    String retval = orig;

    if (orig.equalsIgnoreCase("NEW YORK JETS"))
    {
      retval = "NY JETS";
    }
    else if (orig.equalsIgnoreCase("NEW YORK GIANTS"))
    {
      retval = "NY GIANTS";
    }

    return retval;
  }

  /**
   * Populates matchups within the fantasy teams for each matchup
   * @param fantasyTeamMatchups delimited string containing each matchups
   */

  private void fillFantasyMatchups(String fantasyTeamMatchups)
  {
    int number;
    int opponent;
    //2,1~1,2   (number,opponent~...
    st_ = new StringTokenizer(fantasyTeamMatchups, ServletInfo.DELIMITER, false);
    while (st_.hasMoreElements())
    {
      ist_ = new StringTokenizer(st_.nextToken(), ",", false);

      number = new Integer(ist_.nextToken()).intValue();
      opponent = new Integer(ist_.nextToken()).intValue();
      li_ = fantasyTeamList_.listIterator();
      while (li_.hasNext())
      {
	FantasyTeam ft = (FantasyTeam)li_.next();
	if (ft.getNumber() == number)
	{
	  ft.setOpponent(opponent);
	  break;
	}
      }
    }
  }

  /**
   * Retrieves the fantasy team matchups from the database using a servlet
   * @returns Delimited string representing the fantasy team matchups
   */

  private String getFantasyTeamMatchups()
  {
    String retval = "";
    StringBuffer sb = new StringBuffer();

    sb.append(leagueYear_);
    sb.append(ServletInfo.DELIMITER);
    sb.append(leagueName_);
    sb.append(ServletInfo.DELIMITER);
    sb.append(currentWeek_);

    try
    {
      retval = getFantasyTeamMatchupsServlet_.execute(sb.toString());
    }
    catch (CantConnectException cce)
    {
      retval = "";
    }

    return retval;
  }

  /**
   * Create fantasyTeamObjects and put them in fantasy team list
   * @param fantasyTeams Delimited string containing all of the fantasy teams
   */

  private void createFantasyTeamObjects(String fantasyTeams)
  {
    //<Team1~Team2>
    st_ = new StringTokenizer(fantasyTeams, ServletInfo.DELIMITER, false);
    int teamNumber = 1;
    while (st_.hasMoreElements())
    {
      FantasyTeam ft = new FantasyTeam(st_.nextToken(), teamNumber);
      fantasyTeamList_.add(ft);
      teamNumber++;
    }
  }

  /**
   * Retrieves the fantasy teams from the database using a servlet
   * @returns Teams as a delimited string or an empty string if error
   */

  private String getFantasyTeams()
  {
    String retval = "";
    StringBuffer sb = new StringBuffer();

    sb.append(leagueYear_);
    sb.append(ServletInfo.DELIMITER);
    sb.append(leagueName_);

    try
    {
      retval = getFantasyTeamsServlet_.execute(sb.toString());
    }
    catch (CantConnectException cce)
    {
      retval = "";
    }

    return retval;
  }

}