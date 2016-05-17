package com.twoforboth.realtimescoring.data;

import com.twoforboth.realtimescoring.data.NFLTeam;
import com.twoforboth.communication.Servlet;
import com.twoforboth.communication.ServletInfo;
import com.twoforboth.communication.CantConnectException;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Object representing an ArrayList of NFLTeamsfs</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class NFLTeams
{
  private final String NEW_YORK = "NEW YORK";

  private final static String[] twoCharacterShortNames = { "KC",
                                                           "NE",
							   "TB",
							   "SF",
							   "NO",
							   "SD",
							   "GB"};
  private final static String[] threeCharacterShortNames = { "KAN",
                                                             "NWE",
							     "TAM",
							     "SFO",
							     "NOR",
							     "SDG",
							     "GNB" };

  private ArrayList nflTeamList_ = new ArrayList();
  private ListIterator li_ = null;
  private StringTokenizer st_ = null;
  private StringTokenizer ist_ = null;
  private String leagueName_ = "";
  private String leagueYear_ = "";

  private  Servlet getNFLTeamShortLongNamesAndNumbersServlet_ =
      new Servlet(ServletInfo.GET_NFL_TEAM_SHORT_LONG_NAMES_AND_NUMBERS_SERVLET_NAME,
		  ServletInfo.GET_NFL_TEAM_SHORT_LONG_NAMES_AND_NUMBERS_SERVLET_PARAMETERS);


  /**
   * Constructor that populates the list from a string representing all of the
   * NFL Teams
   * @param nflTeamNamesAndNumbers a string in the form #,ShortName,LongName~...
   */

  public NFLTeams(String leagueYear, String leagueName)
  {
    leagueYear_ = leagueYear;
    leagueName_ = leagueName;

    //9030,BAL,Baltimore~9029,JAC,Jacksonville~9011,OAK,Oakland~9008,PIT,Pittsburg~9024,ATL,Atlanta~
    try
    {
      st_ = new StringTokenizer(getNFLTeamNamesAndNumbers(), ServletInfo.DELIMITER, false);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    while (st_.hasMoreElements())
    {
      try
      {
	ist_ = new StringTokenizer(st_.nextToken(), ",", false);
	NFLTeam nt = new NFLTeam(new Integer(ist_.nextToken()).intValue(),
				 ist_.nextToken(),
				 ist_.nextToken());
	nflTeamList_.add(nt);
      }
      catch (Exception e)
      {
	e.printStackTrace();
      }
    }
  }

  public void getFirst()
  {
    li_ = nflTeamList_.listIterator();
  }

  public boolean hasNext()
  {
    return li_.hasNext();
  }

  public NFLTeam next()
  {
    return (NFLTeam)li_.next();
  }

/**
 * Retrieves NFL Team object from a team number
 * @param teamNumber
 * @returns NFLTeam object
 */

  public NFLTeam getNFLTeamFromNumber(int teamNumber)
  {
    NFLTeam retval = null;
    ListIterator li = nflTeamList_.listIterator();
    while (li.hasNext())
    {
      NFLTeam temp = (NFLTeam)li.next();
      if (temp.getNumber() == teamNumber)
      {
	retval = temp;
	break;
      }
    }
    return retval;
  }

  /**
   * Retrieves the long NFL team name from the short name
   * @param shortName Short NFL team name
   * @returns Long NFL team name
   */

  public String getLongnameFromShortName(String shortName)
  {
    String retval = "";
    ListIterator li = nflTeamList_.listIterator();
    while (li.hasNext())
    {
      NFLTeam temp = (NFLTeam)li.next();
      if (temp.getShortName().equalsIgnoreCase(shortName))
      {
	retval = temp.getLongName();
	break;
      }
    }
    return retval;
  }

  private String getNFLTeamNamesAndNumbers()
  {
    String retval = "";
    StringBuffer sb = new StringBuffer();

    sb.append(leagueYear_);

    try
    {
      retval = getNFLTeamShortLongNamesAndNumbersServlet_.execute(sb.toString());
    }
    catch (CantConnectException cce)
    {
      retval = "";
    }

    return retval;
  }

  /**
   * Retrieves the long NFL team name from the short name
   * @param shortName Short NFL team name
   * @returns Long NFL team name
   */

  public String getAlternateName(String origName)
  {
    String retval = origName;

    int index = origName.toUpperCase().indexOf(NEW_YORK);

    if (index != -1)
    {
      retval = "NY" + origName.substring(index + NEW_YORK.length());
    }

//    System.out.println("getAlternameName orig = " + origName + " final = " + retval);
    return retval;
  }

  public static String getThreeCharacterShortName(String origShortName)
  {
    String retval = origShortName;
    int i;

    for (i = 0; i < twoCharacterShortNames.length; i++)
    {
      if (origShortName.equalsIgnoreCase(twoCharacterShortNames[i]))
      {
	retval = threeCharacterShortNames[i];
	break;
      }
    }

    return retval;
  }

}