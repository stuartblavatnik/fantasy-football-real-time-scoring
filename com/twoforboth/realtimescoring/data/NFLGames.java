package com.twoforboth.realtimescoring.data;

import com.twoforboth.realtimescoring.data.NFLGame;
import com.twoforboth.communication.CantConnectException;
import com.twoforboth.communication.Servlet;
import com.twoforboth.communication.ServletInfo;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class NFLGames
{
  private ArrayList lst_ = new ArrayList();
  private ListIterator li_ = null;
  private String leagueYear_ = "";
  private String leagueName_ = "";
  private StringTokenizer st_ = null;
  private StringTokenizer ist_ = null;

  private Servlet getNFLTeamMatchupsServlet_ =
      new Servlet(ServletInfo.GET_NFL_MATCHUPS_SERVLET_NAME,
		  ServletInfo.GET_NFL_MATCHUPS_SERVLET_PARAMETERS);



  public NFLGames(String leagueYear, String leagueName)
  {
    leagueYear_ = leagueYear;
    leagueName_ = leagueName;
  }

  public void add(NFLGame game) { lst_.add(game); }
  public void first() { li_ = lst_.listIterator(); }
  public NFLGame getNext()
  {
    NFLGame game = null;

    if (li_.hasNext())
    {
      game = (NFLGame)li_.next();
    }

    return game;
  }

  private  String getNFLTeamMatchups(int week)
  {
    String retval = "";
    StringBuffer sb = new StringBuffer();

    sb.append(leagueYear_);
    sb.append(ServletInfo.DELIMITER);
    sb.append(leagueName_);
    sb.append(ServletInfo.DELIMITER);
    sb.append(week);

    try
    {
      retval = getNFLTeamMatchupsServlet_.execute(sb.toString());
    }
    catch (CantConnectException cce)
    {
      retval = "";
    }

    return retval;
  }

  public void initializeMatchups(int currentWeek)
  {
    //Retrieve the weekly NFL Schedule from the database
    //Build the NFLGame objects
    fillNFLTeamMatchups(getNFLTeamMatchups(currentWeek));
  }

  public boolean teamOnBye(String teamName)
  {
    boolean retval = false;
    NFLGame nflGame = null;

    first();

    while ((nflGame = getNext()) != null)
    {
      if (nflGame.getHomeTeamLongName().equalsIgnoreCase(teamName) ||
	  nflGame.getHomeTeamShortName().equalsIgnoreCase(teamName))
      {
	if (nflGame.getVisitingTeamShortName().length() == 0)
	{
	  retval = true;
	}
	break;
      }
    }

    return retval;
  }

  private void fillNFLTeamMatchups(String nflTeamMatchups)
  {
    //9027,9015,1031272200~9004,9000,1031504400~
    //Visitor,Home,time
    int homeTeam;
    int visitingTeam;
    long date;

    st_ = new StringTokenizer(nflTeamMatchups, ServletInfo.DELIMITER, false);
    while (st_.hasMoreElements())
    {
      String subLine = st_.nextToken();
      ist_ = new StringTokenizer(subLine, ",", false);

      visitingTeam = new Integer(ist_.nextToken()).intValue();
      homeTeam = new Integer(ist_.nextToken()).intValue();

      date = new Long(ist_.nextToken()).longValue();

      NFLGame nflGame = new NFLGame(homeTeam, visitingTeam, date);
      lst_.add(nflGame);
    }
  }
}